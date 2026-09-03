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

Place production, SRG-named optional mod JARs directly in `devmods/`; the
`prepareDevMods` task remaps them for the MCP workspace. Place MCP development
JARs in `devmods/deobf/` to load them directly. Both locations are ignored by Git,
and their contents are not included in the MalisisDoors output JAR.
