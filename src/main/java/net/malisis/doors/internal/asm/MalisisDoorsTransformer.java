package net.malisis.doors.internal.asm;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IRETURN;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.launchwrapper.IClassTransformer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Narrow replacement for the two MalisisCore transformers used by big doors.
 *
 * <p>Hooks are selected by owner, descriptor, and semantic call sites rather
 * than by line numbers or hard-coded temporary local slots. A missing hook is
 * fatal: running without one would silently corrupt large-door behavior.</p>
 */
public class MalisisDoorsTransformer implements IClassTransformer
{
    private static final Logger LOG = LogManager.getLogger("MalisisDoorsCore");
    private static final String COLLISION =
            "net/malisis/doors/internal/util/chunkcollision/ChunkCollision";
    private static final String CHUNK_HANDLER =
            "net/malisis/doors/internal/util/chunkblock/ChunkBlockHandler";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass)
    {
        if (basicClass == null)
            return null;

        if ("net.minecraft.world.World".equals(transformedName))
            return transformWorld(basicClass);
        if ("net.minecraft.item.ItemBlock".equals(transformedName))
            return transformItemBlock(basicClass);
        if ("net.minecraft.network.NetHandlerPlayServer".equals(transformedName))
            return transformDigging(basicClass);
        if ("net.minecraft.world.chunk.Chunk".equals(transformedName))
            return transformChunk(basicClass);

        return basicClass;
    }

    private byte[] transformWorld(byte[] bytes)
    {
        ClassNode classNode = read(bytes);
        MethodNode collisions = findMethod(classNode, "getCollidingBoundingBoxes", "func_72945_a",
                "(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/AxisAlignedBB;)Ljava/util/List;");
        MethodNode rayTrace = findMethod(classNode, "rayTraceBlocks", "func_147447_a",
                "(Lnet/minecraft/util/Vec3;Lnet/minecraft/util/Vec3;ZZZ)Lnet/minecraft/util/MovingObjectPosition;");

        int collisionReturns = wrapObjectReturns(collisions, "appendCollisionBoxes",
                "(Ljava/util/List;Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;"
                        + "Lnet/minecraft/util/AxisAlignedBB;)Ljava/util/List;",
                new VarInsnNode(ALOAD, 0), new VarInsnNode(ALOAD, 1), new VarInsnNode(ALOAD, 2));
        int rayReturns = wrapObjectReturns(rayTrace, "mergeRayTrace",
                "(Lnet/minecraft/util/MovingObjectPosition;Lnet/minecraft/world/World;"
                        + "Lnet/minecraft/util/Vec3;Lnet/minecraft/util/Vec3;)Lnet/minecraft/util/MovingObjectPosition;",
                new VarInsnNode(ALOAD, 0), new VarInsnNode(ALOAD, 1), new VarInsnNode(ALOAD, 2));

        require(collisionReturns > 0, "World.getCollidingBoundingBoxes return hook");
        require(rayReturns > 0, "World.rayTraceBlocks return hooks");
        LOG.info("Applied World.getCollidingBoundingBoxes and World.rayTraceBlocks hooks ({} and {} returns)",
                collisionReturns, rayReturns);
        return write(classNode);
    }

    private byte[] transformItemBlock(byte[] bytes)
    {
        ClassNode classNode = read(bytes);
        MethodNode method = findMethod(classNode, "onItemUse", "func_77648_a",
                "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/EntityPlayer;"
                        + "Lnet/minecraft/world/World;IIIIFFF)Z");
        int hooks = 0;

        for (AbstractInsnNode instruction : method.instructions.toArray())
        {
            if (!(instruction instanceof MethodInsnNode))
                continue;

            MethodInsnNode call = (MethodInsnNode) instruction;
            if (!"net/minecraft/world/World".equals(call.owner)
                    || !("canPlaceEntityOnSide".equals(call.name) || "func_147472_a".equals(call.name))
                    || !("(Lnet/minecraft/block/Block;IIIZILnet/minecraft/entity/Entity;"
                            + "Lnet/minecraft/item/ItemStack;)Z").equals(call.desc))
                continue;

            LabelNode allowed = new LabelNode();
            InsnList hook = new InsnList();
            hook.add(new VarInsnNode(ALOAD, 1));
            hook.add(new VarInsnNode(ALOAD, 2));
            hook.add(new VarInsnNode(ALOAD, 3));
            hook.add(new VarInsnNode(ALOAD, 0));
            hook.add(new FieldInsnNode(GETFIELD, classNode.name, fieldName(classNode,
                    "blockInstance", "field_150939_a", "Lnet/minecraft/block/Block;"),
                    "Lnet/minecraft/block/Block;"));
            hook.add(new VarInsnNode(ILOAD, 4));
            hook.add(new VarInsnNode(ILOAD, 5));
            hook.add(new VarInsnNode(ILOAD, 6));
            hook.add(new VarInsnNode(ILOAD, 7));
            hook.add(new MethodInsnNode(INVOKESTATIC, COLLISION, "canPlaceBlock",
                    "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/EntityPlayer;"
                            + "Lnet/minecraft/world/World;Lnet/minecraft/block/Block;IIII)Z", false));
            hook.add(new JumpInsnNode(Opcodes.IFNE, allowed));
            hook.add(new InsnNode(Opcodes.ICONST_0));
            hook.add(new InsnNode(IRETURN));
            hook.add(allowed);
            method.instructions.insertBefore(instruction, hook);
            hooks++;
        }

        require(hooks == 1, "ItemBlock.onItemUse placement hook");
        LOG.info("Applied ItemBlock.onItemUse large-block placement hook");
        return write(classNode);
    }

    private byte[] transformDigging(byte[] bytes)
    {
        ClassNode classNode = read(bytes);
        MethodNode method = findMethod(classNode, "processPlayerDigging", "func_147345_a",
                "(Lnet/minecraft/network/play/client/C07PacketPlayerDigging;)V");
        List<Integer> coordinateVariables = packetCoordinateVariables(method);
        require(coordinateVariables.size() >= 3, "server digging packet coordinate discovery");

        int hooks = 0;
        for (AbstractInsnNode instruction : method.instructions.toArray())
        {
            if (!(instruction instanceof LdcInsnNode) || !Double.valueOf(36.0D).equals(((LdcInsnNode) instruction).cst))
                continue;

            InsnList hook = new InsnList();
            hook.add(new VarInsnNode(ALOAD, 0));
            hook.add(new FieldInsnNode(GETFIELD, classNode.name, fieldName(classNode,
                    "playerEntity", "field_147369_b", "Lnet/minecraft/entity/player/EntityPlayerMP;"),
                    "Lnet/minecraft/entity/player/EntityPlayerMP;"));
            hook.add(new VarInsnNode(ILOAD, coordinateVariables.get(0)));
            hook.add(new VarInsnNode(ILOAD, coordinateVariables.get(1)));
            hook.add(new VarInsnNode(ILOAD, coordinateVariables.get(2)));
            hook.add(new MethodInsnNode(INVOKESTATIC, COLLISION, "getBlockReachDistanceSquared",
                    "(Lnet/minecraft/entity/player/EntityPlayer;III)D", false));
            method.instructions.insert(instruction, hook);
            method.instructions.remove(instruction);
            hooks++;
        }

        require(hooks == 1, "NetHandlerPlayServer.processPlayerDigging reach hook");
        LOG.info("Applied NetHandlerPlayServer.processPlayerDigging large-block reach hook");
        return write(classNode);
    }

    private byte[] transformChunk(byte[] bytes)
    {
        ClassNode classNode = read(bytes);
        MethodNode method = findMethod(classNode, "setBlockIDWithMetadata", "func_150807_a",
                "(IIILnet/minecraft/block/Block;I)Z");
        int hooks = 0;

        for (AbstractInsnNode instruction : method.instructions.toArray())
        {
            if (!(instruction instanceof MethodInsnNode))
                continue;

            MethodInsnNode call = (MethodInsnNode) instruction;
            if (!"net/minecraft/world/chunk/storage/ExtendedBlockStorage".equals(call.owner)
                    || !("setExtBlockID".equals(call.name) || "func_150818_a".equals(call.name)))
                continue;

            LabelNode allowed = new LabelNode();
            InsnList hook = new InsnList();
            hook.add(new VarInsnNode(ALOAD, 0));
            hook.add(new VarInsnNode(ILOAD, 1));
            hook.add(new VarInsnNode(ILOAD, 2));
            hook.add(new VarInsnNode(ILOAD, 3));
            hook.add(new VarInsnNode(ALOAD, 4));
            hook.add(new MethodInsnNode(INVOKESTATIC, CHUNK_HANDLER, "beforeSetBlock",
                    "(Lnet/minecraft/world/chunk/Chunk;IIILnet/minecraft/block/Block;)Z", false));
            hook.add(new JumpInsnNode(Opcodes.IFNE, allowed));
            hook.add(new InsnNode(Opcodes.ICONST_0));
            hook.add(new InsnNode(IRETURN));
            hook.add(allowed);
            method.instructions.insertBefore(instruction, hook);
            hooks++;
        }

        require(hooks == 1, "Chunk.setBlockIDWithMetadata coordinate hook");
        LOG.info("Applied Chunk.setBlockIDWithMetadata coordinate tracking hook");
        return write(classNode);
    }

    private int wrapObjectReturns(MethodNode method, String hookName, String hookDescriptor,
            AbstractInsnNode... loads)
    {
        int count = 0;
        for (AbstractInsnNode instruction : method.instructions.toArray())
        {
            if (instruction.getOpcode() != Opcodes.ARETURN)
                continue;

            InsnList hook = new InsnList();
            for (AbstractInsnNode load : loads)
                hook.add(load.clone(null));
            hook.add(new MethodInsnNode(INVOKESTATIC, COLLISION, hookName, hookDescriptor, false));
            method.instructions.insertBefore(instruction, hook);
            count++;
        }
        return count;
    }

    private List<Integer> packetCoordinateVariables(MethodNode method)
    {
        List<Integer> variables = new ArrayList<>();
        for (AbstractInsnNode instruction : method.instructions.toArray())
        {
            if (!(instruction instanceof MethodInsnNode))
                continue;

            MethodInsnNode call = (MethodInsnNode) instruction;
            AbstractInsnNode next = nextOpcode(instruction);
            if ("net/minecraft/network/play/client/C07PacketPlayerDigging".equals(call.owner)
                    && "()I".equals(call.desc) && next instanceof VarInsnNode
                    && next.getOpcode() == Opcodes.ISTORE)
                variables.add(((VarInsnNode) next).var);
        }
        return variables;
    }

    private AbstractInsnNode nextOpcode(AbstractInsnNode instruction)
    {
        AbstractInsnNode next = instruction.getNext();
        while (next != null && next.getOpcode() < 0)
            next = next.getNext();
        return next;
    }

    private MethodNode findMethod(ClassNode classNode, String deobfuscatedName, String srgName, String descriptor)
    {
        for (MethodNode method : classNode.methods)
            if ((deobfuscatedName.equals(method.name) || srgName.equals(method.name)) && descriptor.equals(method.desc))
                return method;
        throw failure(classNode.name + '.' + deobfuscatedName + descriptor);
    }

    private String fieldName(ClassNode classNode, String deobfuscatedName, String srgName, String descriptor)
    {
        for (org.objectweb.asm.tree.FieldNode field : classNode.fields)
            if ((deobfuscatedName.equals(field.name) || srgName.equals(field.name)) && descriptor.equals(field.desc))
                return field.name;
        throw failure(classNode.name + '.' + deobfuscatedName);
    }

    private ClassNode read(byte[] bytes)
    {
        ClassNode classNode = new ClassNode();
        new ClassReader(bytes).accept(classNode, 0);
        return classNode;
    }

    private byte[] write(ClassNode classNode)
    {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    private void require(boolean condition, String hook)
    {
        if (!condition)
            throw failure(hook);
    }

    private IllegalStateException failure(String hook)
    {
        String message = "Essential MalisisDoors core hook could not be applied: " + hook;
        LOG.fatal(message);
        return new IllegalStateException(message);
    }
}
