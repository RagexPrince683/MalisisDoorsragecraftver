/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Ordinastie
 */
package net.malisis.doors.internal;

import net.malisis.doors.internal.inventory.message.CloseInventoryMessage;
import net.malisis.doors.internal.inventory.message.InventoryActionMessage;
import net.malisis.doors.internal.inventory.message.OpenInventoryMessage;
import net.malisis.doors.internal.inventory.message.UpdateInventorySlotsMessage;
import net.malisis.doors.internal.network.MalisisNetwork;
import net.minecraft.launchwrapper.Launch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Shared runtime services needed by the locally ported support code.
 *
 * <p>This deliberately contains no Forge mod entry point. MalisisDoors owns the
 * support implementation and its private inventory packet channel.</p>
 */
public final class InternalSupport
{
    public static final Logger log = LogManager.getLogger("MalisisDoors");
    public static final MalisisNetwork network = new MalisisNetwork("malisisdoors_inv");
    public static final boolean isObfEnv = !Boolean.TRUE.equals(Launch.blackboard.get("fml.deobfuscatedEnvironment"));

    static
    {
        new CloseInventoryMessage();
        new InventoryActionMessage();
        new OpenInventoryMessage();
        new UpdateInventorySlotsMessage();
    }

    private InternalSupport()
    {
    }

    public static void message(String message, Object... parameters)
    {
        log.info(message, parameters);
    }
}
