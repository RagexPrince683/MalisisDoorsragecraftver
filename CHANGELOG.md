# Fix final Forge 1.7.10 input compatibility errors

## Fixed

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
