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
