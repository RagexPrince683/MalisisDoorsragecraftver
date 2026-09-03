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
