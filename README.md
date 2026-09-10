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
