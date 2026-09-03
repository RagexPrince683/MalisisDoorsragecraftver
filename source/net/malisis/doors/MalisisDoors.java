package net.malisis.doors;

import net.malisis.doors.internal.renderer.font.MalisisFont;
import net.malisis.doors.block.BlockMixer;
import net.malisis.doors.block.DoorFactory;
import net.malisis.doors.block.GarageDoor;
import net.malisis.doors.block.MixedBlock;
import net.malisis.doors.block.PlayerSensor;
import net.malisis.doors.block.RustyLadder;
import net.malisis.doors.block.VanishingBlock;
import net.malisis.doors.block.VanishingDiamondBlock;
import net.malisis.doors.door.block.BigDoor;
import net.malisis.doors.door.block.FenceGate;
import net.malisis.doors.door.block.ForcefieldDoor;
import net.malisis.doors.door.block.RustyHatch;
import net.malisis.doors.door.item.ForcefieldItem;
import net.malisis.doors.proxy.IProxy;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import net.malisis.doors.network.DigicodeMessage;
import net.malisis.doors.network.DoorFactoryMessage;
import net.malisis.doors.network.VanishingDiamondFrameMessage;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = MalisisDoors.modid, name = MalisisDoors.modname, version = MalisisDoors.version)
public class MalisisDoors
{
	@SidedProxy(clientSide = "net.malisis.doors.proxy.ClientProxy", serverSide = "net.malisis.doors.proxy.ServerProxy")
	public static IProxy proxy;

	public static final String modid = "malisisdoors";
	public static final String modname = "Malisis' Doors";
	public static final String version = "${version}";

	public static MalisisDoors instance;
	public static SimpleNetworkWrapper network;
	public static MalisisDoorsSettings settings;

	public static CreativeTabs tab = new MalisisDoorsTab();
	public static MalisisFont digitalFont;

	public MalisisDoors()
	{
		instance = this;
		network = NetworkRegistry.INSTANCE.newSimpleChannel(modid);
		network.registerMessage(DigicodeMessage.class, DigicodeMessage.Packet.class, 0, Side.SERVER);
		network.registerMessage(DoorFactoryMessage.class, DoorFactoryMessage.Packet.class, 1, Side.SERVER);
		network.registerMessage(VanishingDiamondFrameMessage.class, VanishingDiamondFrameMessage.Packet.class, 2, Side.SERVER);
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		settings = new MalisisDoorsSettings(event.getSuggestedConfigurationFile());

		Registers.init();

		proxy.initRenderers();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		proxy.initFonts();
	}

	public static class Blocks
	{
		public static Block doorAcacia;
		public static Block doorBirch;
		public static Block doorDarkOak;
		public static Block doorJungle;
		public static Block doorSpruce;

		public static FenceGate oakFenceGate;
		public static FenceGate acaciaFenceGate;
		public static FenceGate birchFenceGate;
		public static FenceGate darkOakFenceGate;
		public static FenceGate jungleFenceGate;
		public static FenceGate spruceFenceGate;
		public static FenceGate camoFenceGate;

		public static Block woodSlidingDoor;
		public static Block ironSlidingDoor;
		public static PlayerSensor playerSensor;
		public static VanishingBlock vanishingBlock;
		public static VanishingDiamondBlock vanishingDiamondBlock;
		public static BlockMixer blockMixer;
		public static MixedBlock mixedBlock;
		public static GarageDoor garageDoor;
		public static Block jailDoor;
		public static DoorFactory doorFactory;
		public static Block customDoor;
		public static Block laboratoryDoor;
		public static Block factoryDoor;
		public static Block shojiDoor;
		public static RustyHatch rustyHatch;
		public static Block curtains;
		public static BigDoor carriageDoor;
		public static BigDoor medievalDoor;
		public static ForcefieldDoor forcefieldDoor;
		public static RustyLadder rustyLadder;
		public static Block ironTrapDoor;
		public static Block slidingTrapDoor;
		public static Block saloonDoor;
	}

	public static class Items
	{
		public static Item woodSlidingDoorItem;
		public static Item ironSlidingDoorItem;
		public static Item doorAcaciaItem;
		public static Item doorBirchItem;
		public static Item doorDarkOakItem;
		public static Item doorJungleItem;
		public static Item doorSpruceItem;
		public static Item jailDoorItem;
		public static Item customDoorItem;
		public static Item laboratoryDoorItem;
		public static Item factoryDoorItem;
		public static Item shojiDoorItem;
		public static Item rustyHandle;
		public static Item curtainsItem;
		public static Item carriageDoorItem;
		public static ForcefieldItem forcefieldItem;
		public static Item ironTrapDoorItem;
		public static Item slidingTrapDoorItem;
		public static Item saloonDoorItem;
	}

}
