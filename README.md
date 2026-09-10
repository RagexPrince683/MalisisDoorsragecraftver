#[MalisisDoors](http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2076338-1-7-2-1-7-10-forge-malisisdoors-1-7-10-1-1-2)

##Features:

* Adds animations to doors, trap doors and fence gates.
* Adds new animated sliding doors that comes in wood or iron material.
* Adds sensors which detect players passing under and send a redstone signal to the block they are attached to.
* Adds vanishing blocks. Frames can be crafted and placed in the world, when supplied with redstone current, the 
vanish into thin air make all neighboring vanishing blocks vanishing as well. When the redstone current stop, 
the frames go back to being solid blocks. A frame can be activated with normal blocks which are used to "paint"
the frame. Different types of frames implies different vanishing propagation behavior :
  - wood frames propagate to all frames around them
  - iron frames propagate to all frames around painted with the same block
  - gold frames propagate to all frames around painted with the same block and the same metadata (ie red wool would not make a blue wool vanish)
  - diamond frames has their own GUI to configure their behavior. You can choose for each direction if they should propagate and the delay 
  - Frames not painted automatically propagate their state.

## Standalone migration architecture

This fork is migrating MalisisDoors to a standalone Forge 1.7.10 implementation. The
configuration and packet transport are owned by MalisisDoors and use Forge's
`Configuration` and `SimpleNetworkWrapper` APIs directly. Renderer and animation
replacements belong under `net.malisis.doors.renderer` and
`net.malisis.doors.door.movement`; focused Minecraft/Forge adapters belong under
`net.malisis.doors.util`. These boundaries are intended to keep future Angelica
compatibility fixes local and avoid assumptions about a third-party renderer's GL
state.

## Local development mods

The Gradle 8.11.1 GTNH/RetroFuturaGradle development environment uses Minecraft
1.7.10, Forge 10.13.4.1614, and stable 12 mappings. It supplies the GTNH builds of
Not Enough Items and CodeChickenCore for development runs.

Java sources use the standard Gradle `src/main/java/` directory, and mod assets
and metadata use `src/main/resources/`. The GTNH convention generates
`net.malisis.doors.Tags` during the build so Java code can read the project
version without editing tracked source files.

Place production, SRG-named optional mod JARs directly in `devmods/`; the
`prepareDevMods` task remaps them for the MCP workspace. Place MCP development
JARs in `devmods/deobf/` to load them directly. Both locations are ignored by Git,
and their contents are not included in the MalisisDoors output JAR.

## Shared renderer state ownership

The shared renderer appends chunk geometry to Forge's caller-owned tessellator
batch and only changes the batch translation for that callback. Inventory,
held/dropped item, tile-entity, and world-last callbacks own the batches they
start, submit them on success, and discard incomplete buffers on failure. The
tile-entity damage overlay submits the preceding geometry and uses a separate
state scope and fresh batch so its color and per-vertex settings cannot leak.

Non-chunk callbacks preserve incoming blend enablement and separate blend
factors, alpha-test function and reference, current color, lighting, color
material, shade model, texture enablement and bindings, culling enablement, and
matrix mode through scoped, targeted OpenGL attribute groups. Model-view and
forcefield texture-matrix pushes are balanced independently. Calls continue to
use LWJGL's `GL11` entry points and Minecraft's `OpenGlHelper` blend entry point
so an installed GL redirector can observe mutations and restorations; no native
OpenGL bypass or hard renderer dependency is used.

| Changed state | Restoration mechanism |
| --- | --- |
| Blend enablement and separate RGB/alpha factors; alpha-test enablement, function, and reference | Scoped color-buffer and enable attributes |
| Current RGBA color | Scoped current attributes |
| Lighting, color material, and shade model | Scoped lighting and enable attributes |
| Texture enablement, binding, and active unit | Scoped texture and enable attributes |
| Culling enablement | Scoped enable attributes |
| Incoming matrix mode | Scoped transform attributes |
| Inventory/TESR/world-last model transforms | Explicit model-view push/pop |
| Animated forcefield texture transform | Explicit texture-matrix push/pop |
| Damage-overlay color, blend, alpha test, and vertex settings | Nested overlay attributes and a completed or discarded overlay-owned batch |

No Angelica artifact or pinned Angelica version is present in this repository
or its local development-mod directory, so its exact redirect and state-cache
implementation could not be inspected here. Runtime visual behavior—especially
with Angelica installed—requires end-user development feedback.
# Standalone runtime architecture

MalisisDoors includes only the portions of MalisisCore that its 1.7.10 gameplay
uses. The runtime dependency path is:

```text
door block -> tile entity -> movement -> renderer -> OBJ/texture resource
           -> collision -> chunk coordinate tracking -> persistence/network
           -> Forge event registration -> minimal Minecraft ASM hooks
```

Large Carriage and Medieval doors implement both chunk collision and block
listener contracts. `MalisisDoorsCorePlugin` installs the World collision and
ray-trace, ItemBlock placement, server digging reach, and Chunk block-change
hooks needed by those contracts. `MalisisDoors.preInit` registers the chunk data
and vanilla replacement event handlers. Forcefield Door and Rusty Hatch use the
standalone multiblock implementation and their own tile entities for ownership
and NBT persistence.

The internal Syncer source remains for source compatibility, but no current
MalisisDoors gameplay class is annotated with `@Syncable`; active doors use their
existing tile-entity description packets and explicit gameplay messages.

## Experimental hybrid door rendering

The client configuration contains `hybridDoorRendering`, which defaults to
`false`. Its first planned batch is deliberately restricted to ordinary
`Door`/`DoorTileEntity` instances created by the built-in `WoodDoor` and
`VanillaDoor` descriptors, using exactly `RotatingDoorMovement` in opaque render
pass 0. Custom-material doors, glass or otherwise transparent doors, subclasses
with special renderers, nonstandard descriptors or tile entities, missing or
uncertain resources, and every non-rotating movement fall back to TESR. Garage
doors, custom doors, curtains, forcefields, trapdoors, and other special door
families are deferred.

The chunk path has isolated shape and parameter data for each lower or upper
block callback. Each half is emitted at its owning block position and applies
the same closed/open rotation, hinge, centering, icons, UV handling, brightness,
and damage override machinery as the existing renderer. It performs no direct
OpenGL calls. A complete transition would request rebuilds for both affected
chunk sections only when movement starts or ends, keep TESR visible while old
chunk geometry remains live, and switch visibility only after the replacement
geometry has actually been uploaded. This also covers a door whose halves cross
a chunk-section boundary, rapid reversal, resource reload, world unload, and
tile invalidation without frame-delay guesses or per-animation-frame rebuilds.

Forge 1.7.10 does not expose a chunk rebuild/upload completion event, and no
such public contract is present in the repository's existing Angelica-compatible
integration surface. An ISBRH callback only proves that compilation visited a
block; it does not prove that the resulting buffers are visible. Consequently,
the safety gate currently keeps every door on the established TESR path even
when the option is enabled. This is the only renderer configuration supported
by current code evidence; the isolated chunk emitter is staged but cannot be
activated until a verified renderer-specific completion adapter exists. The
option remains experimental pending end-user development feedback. Performance
gains and visual behavior remain unmeasured.
# Standalone large doors

Carriage and Medieval Doors use ordinary Forge multiblocks. The visible origin
owns rendering and animation; invisible, persistent proxy blocks occupy the
remaining 4-by-5 support volume and delegate collision, selection, activation,
and removal to that origin. MalisisDoors does not install a loading plugin or
transform Minecraft classes.

## Standalone resources

The vendored GUI implementation loads its atlas from
`assets/malisisdoors/textures/gui/gui.png`; MalisisCore is not required at
runtime. The Door Factory's vanilla `ItemBlock` uses the existing
`malisisdoors:door_factory` and `malisisdoors:door_factory_side` block-atlas
icons and render type `0`. Minecraft therefore draws it through the normal
three-dimensional `RenderBlocks` inventory and held-item path, without a
duplicate item texture or a custom item renderer.
