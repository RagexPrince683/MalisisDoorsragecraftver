# Add experimental hybrid rendering groundwork for opaque doors

## Changed

- Added the client `hybridDoorRendering` option, disabled by default, with explicit eligibility limited to built-in opaque `WoodDoor` and `VanillaDoor` descriptors using ordinary `Door`, `DoorTileEntity`, and exactly `RotatingDoorMovement`.
- Added isolated per-callback stationary lower/upper chunk geometry generation that preserves the existing door poses and rendering parameters without sharing mutable TESR model state or issuing direct OpenGL calls.
- Kept all doors on TESR after confirming that Forge 1.7.10 and the repository's Angelica integration surface provide no reliable chunk rebuild/upload completion hook; the incomplete handoff is safety-gated rather than enabled.
- Documented supported renderer evidence, fallback cases, deferred door families, chunk-section ownership, transition requirements, and the experimental and unmeasured status of the option.

# Cache garage door structures and animations

## Changed

- Cached each loaded garage door column's controller coordinate, ordered segment coordinates, height, and conservative movement render bounds without retaining tile entity references.
- Invalidated garage door structure data for placement, removal, replacement, rotation, neighbor, synchronization, tile invalidation, and chunk lifecycle changes, rebuilding only from loaded blocks with bounded iterative traversal.
- Reused cached column data for opening duration, rendering, damage selection, and render bounds, and reused per-height segment animation transformations instead of allocating them in every segment render.
- Rejected non-controller garage door tile renders before the shared renderer binds textures or initializes OpenGL and tessellator state while leaving inventory rendering unchanged.

# Reduce custom-door render allocations

## Changed

- Reused the custom door model after the renderer's guarded initialization and reset its mutable geometry and render parameters for each world or item render.
- Reused renderer-owned, depth-isolated temporary parameter sets for texture application and face drawing while retaining the existing merge precedence.

# Restore Door Factory block-item rendering

## Fixed

- Pinned the Door Factory to vanilla render type `0`, keeping its registered
  `ItemBlock` on Minecraft 1.7.10's three-dimensional `RenderBlocks` inventory
  and held-item path instead of the flat item-icon fallback.
- Kept the existing `door_factory` registry name and block-atlas front and side
  textures unchanged.

# Fix Door Factory and GUI texture resolution

## Fixed

- Pointed the standalone GUI renderer at the GUI atlas now owned by the
  `malisisdoors` resource namespace.
- Registered the Door Factory front and side icons explicitly and retained the
  side icon as the vanilla block-item fallback used in inventories and hands.

# Fix curtain, Door Factory, and standalone GUI textures

## Fixed

- Normalized Minecraft dye color tokens to the existing snake_case curtain texture convention without changing persisted registry names or dye metadata.
- Verified the Door Factory's existing front, side, and Forge block-item icon paths against the packaged block textures.
- Restored the MalisisCore GUI atlas required by the vendored standalone GUI renderer and documented its original MIT license.

# Replace large-door coremod hooks with Forge multiblocks

## Fixed

- Replaced Carriage and Medieval Door's origin-only world representation with
  persistent, invisible proxy blocks. Each occupied 4-by-5 cell now clips and
  exposes the origin door's live thin collision and ray-trace geometry.
- Added transactional large-door placement, remote-part activation and
  breaking, single-item drops, owned-proxy cleanup, orphan cleanup, and safe
  missing-proxy repair.
- Restored the Saloon Door's intended world mesh by rendering the hinged
  `Plane` object while retaining the displaced `Plane.001` item-preview helper.
- Made OBJ group collisions lossless instead of merging unrelated groups.
- Initialized the Door Factory's canonical texture name, fixing the ItemBlock
  inventory lookup while retaining explicit front and side atlas entries.

## Removed

- Removed the MalisisDoors loading plugin and transformer. No Minecraft
  `World`, `Chunk`, `ItemBlock`, or `NetHandlerPlayServer` transformation is
  required.
- Removed the unused chunk collision/listener/persistence subsystem formerly
  used only to project a large door beyond its real origin block.

# Restore standalone MalisisCore runtime behavior

## Fixed

- Added a minimal MalisisDoors loading plugin that restores the four large-block
  collision hooks and the chunk-coordinate update hook formerly supplied by
  MalisisCore. Hook discovery is based on Forge 1.7.10 descriptors and semantic
  bytecode call sites, and startup now fails loudly if an essential hook cannot
  be applied.
- Restored chunk coordinate persistence/watch synchronization and vanilla
  replacement texture event registration during mod pre-initialization.
- Registered the chunk coordinate packet on the standalone internal network in
  addition to the retained inventory packets.
- Corrected OBJ object/group handling so names without underscores are retained,
  while underscore-prefixed legacy groups continue to merge instead of silently
  overwriting earlier faces. This restores both halves of the Saloon Door model.

## Audited

- Confirmed the shared big-door path used by Carriage and Medieval doors still
  uses the original block, tile entity, movement, renderer, chunk collision,
  listener, and persistence implementations; the missing runtime hooks and event
  bootstrap were the regressions in that path.
- Confirmed Forcefield Door and Rusty Hatch retain their original multiblock NBT
  ownership path and use their concrete tile entities rather than the unused
  generic `MultiBlockTileEntity` base class.
- Confirmed the standalone Syncer classes have no `@Syncable` consumers in
  MalisisDoors 1.13.2; registering that dormant subsystem would add an unused
  packet rather than restore gameplay behavior.
- Confirmed the Door Factory block textures and case-sensitive tab-icon resource
  paths exist in the source tree and in the existing development JAR. The
  current block name is retained by `MalisisBlock.setBlockName`, yielding
  `malisisdoors:door_factory` and `malisisdoors:door_factory_side`.

# Fix vanilla block replacement startup crash

## Fixed

- Resolved required reflected fields by trying both their environment-specific
  and alternate supplied names, with descriptive immediate failures when neither
  name exists.
- Updated vanilla block replacement to use the `field_150939_a` name exposed by
  the local Forge 1.7.10 `ItemBlock` source and retained the required item-block
  reference update.

# Fix final Forge 1.7.10 input compatibility errors

## Fixed

- Re-enabled Lombok as a compile-only dependency and annotation processor so
  the existing proxy delegates are generated during Java compilation.
- Added the SRG-named Minecraft 1.7.10 container drag-reset bridge while keeping
  the readable internal drag helper and its existing behavior.
- Corrected the GUI mouse-release override to the Minecraft 1.7.10
  `GuiScreen.mouseMovedOrUp()` signature.
- Restored Lombok compile-time delegation support for block-access and world
  proxies.

# Fix remaining stable_12 Java compilation errors

## Fixed

- Corrected the inventory custom-name override to the Minecraft 1.7.10
  `IInventory.hasCustomInventoryName()` signature.
- Restored the shared block item-icon hook used by big doors and rusty hatches.
- Updated multiblock origin resolution to use the Minecraft 1.7.10
  `TileEntity.getWorldObj()` accessor.

# Fix stable_12 Java source compatibility

## Fixed

- Updated MalisisDoors block, item, tile-entity, inventory, rendering, sound,
  packet, and proxy-world calls to the Minecraft 1.7.10 Forge stable 12 API.
- Corrected the malformed anonymous rusty-handle item method declaration.
- Preserved item metadata, forcefield energy, curtain dye naming, inventory
  lifecycle, packet NBT, and vanishing-block proxy behavior while using the
  members exposed by the current workspace.

# Fix GTNH project layout

## Changed

- Moved all Java sources and resources into the standard Gradle
  `src/main/java/` and `src/main/resources/` directories required by the GTNH
  convention structure checker.
- Removed the obsolete custom source-set redirects for the former top-level
  `source/` and `resources/` directories.
- Replaced deprecated in-place version token substitution with the GTNH-generated
  `net.malisis.doors.Tags` class.

# Modernize the Forge 1.7.10 development build

## Changed

- Replaced ForgeGradle 1.2 and Gradle 4.5 with the GTNH convention plugins,
  RetroFuturaGradle, and Gradle 8.11.1.
- Configured Java 8, Forge 10.13.4.1614, stable 12 mappings, and the existing
  `source/` and `resources/` project layout.
- Replaced the legacy ChickenBones runtime with the GTNH CodeChickenCore and Not
  Enough Items development artifacts.
- Added separate support for remapped production development mods in `devmods/`
  and direct MCP development jars in `devmods/deobf/`.
- Migrated version token and `mcmod.info` expansion to the GTNH convention and
  modern Gradle resource processing.
- Removed obsolete CurseForge and Maven upload configuration.

# Fix Forge and NEI development environment

## Fixed

- Updated the Minecraft 1.7.10 development environment to Forge 10.13.4.1614
  so the legacy NotEnoughItems transformer targets the expected Forge classes.
- Preserved stable 12 mappings and the existing ChickenBones development
  dependency stack for CodeChickenLib, CodeChickenCore, and NotEnoughItems.

# Restore legacy NotEnoughItems development dependencies

## Changed

- Restored the ChickenBones Maven repository used by the ForgeGradle 1.2
  development environment.
- Added the legacy Minecraft 1.7.10 CodeChickenLib, CodeChickenCore, and Not
  Enough Items development artifacts as runtime dependencies.
- Kept optional local development JARs available through `devmods` without
  packaging them into the MalisisDoors release JAR.

# Fix local development mod dependencies

## Fixed

- Removed the unavailable remote CodeChicken development artifact dependencies
  and their now-unused Maven repository.
- Kept all JARs placed directly in `devmods` on the compile and development client
  classpaths without packaging them in the MalisisDoors release JAR.
- Clarified that contributors must supply classic NEI, CodeChickenCore,
  CodeChickenLib, and other development mods locally through `devmods`.

# Add Forge 1.7.10 development mod dependencies

## Changed

- Added a tracked `devmods` location for local development mod JARs while keeping
  those third-party files ignored by Git and out of release artifacts.
- Added the classic Minecraft 1.7.10 NEI, CodeChickenCore, and CodeChickenLib
  development artifacts to the compile and runtime environments through the
  ChickenBones-compatible Maven mirror.
- Documented how contributors can add local development mods without editing the
  Gradle build.

# Fix MalisisIcon Java compilation

## Fixed

- Replaced direct access to private Minecraft `TextureAtlasSprite` UV and
  anisotropic-filtering fields with synchronized state owned by `MalisisIcon`.
- Preserved custom UV interpolation, clipping, offsets, flips, dependent icons,
  sprite copying, and anisotropic texture borders on Forge 1.7.10.

# Standalone MalisisCore removal

## Changed

- Removed the MalisisCore Gradle and CurseForge dependency declarations.
- Moved mod lifecycle and packet channel setup to standard Forge 1.7.10 APIs.
- Replaced the MalisisCore settings framework with Forge `Configuration` while
  preserving all eight setting defaults.
- Migrated the three existing server packet registrations to an explicit
  `SimpleNetworkWrapper` channel and direct typed tile-entity checks.
- Replaced MalisisCore logging in proxy-world fallback handling with Forge logging.
- Documented the ownership boundaries for standalone rendering, animation, and
  focused compatibility helpers.

# Finish standalone MalisisCore implementation port

## Changed

- Ported the block, position, ray-tracing, multi-block, collision, timing, inventory,
  GUI, icon, font, model, renderer, and animation support used by MalisisDoors into
  `net.malisis.doors.internal`.
- Updated MalisisDoors blocks, items, tile entities, movement strategies, renderers,
  and GUI screens to consume the locally owned support packages.
- Added an explicit private inventory synchronization channel without restoring a
  standalone core mod or external dependency.
- Retained the Forge configuration and packet migrations from the first standalone
  removal pass.
