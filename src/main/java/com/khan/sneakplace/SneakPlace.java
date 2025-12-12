package com.khan.sneakplace;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

@Mod(modid = SneakPlace.MODID, version = SneakPlace.VERSION, name = SneakPlace.NAME, clientSideOnly = true)
public class SneakPlace {
    public static final String MODID = "sneakplace";
    public static final String VERSION = "1.0";
    public static final String NAME = "SneakPlace";

    private static boolean enabled = true;
    private static KeyBinding toggleKey;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);

        toggleKey = new KeyBinding("Toggle SneakPlace", Keyboard.KEY_V, "SneakPlace");
        ClientRegistry.registerKeyBinding(toggleKey);

        ClientCommandHandler.instance.registerCommand(new SneakPlaceCommand());
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (toggleKey.isPressed()) {
            enabled = !enabled;
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.thePlayer != null) {
                String status = enabled ? "§aenabled" : "§cdisabled";
                mc.thePlayer.addChatMessage(new ChatComponentText("§6[SneakPlace]§r SneakPlace is now " + status));
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (!enabled) return;
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return;

        EntityPlayer player = mc.thePlayer;

        if (player.isSneaking()) {
            MovingObjectPosition mop = mc.objectMouseOver;

            if (mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                BlockPos blockPos = mop.getBlockPos();
                Block block = mc.theWorld.getBlockState(blockPos).getBlock();

                if (block.isFullBlock()) {
                    int woolSlot = findWoolInHotbar(player);

                    if (woolSlot != -1) {
                        BlockPos placePos = blockPos.offset(mop.sideHit);

                        if (mc.theWorld.getBlockState(placePos).getBlock().isReplaceable(mc.theWorld, placePos)) {
                            int currentSlot = player.inventory.currentItem;

                            player.inventory.currentItem = woolSlot;

                            ItemStack stack = player.inventory.getStackInSlot(woolSlot);
                            if (stack != null && stack.getItem() instanceof ItemBlock) {
                                mc.playerController.onPlayerRightClick(
                                        mc.thePlayer,
                                        mc.theWorld,
                                        stack,
                                        blockPos,
                                        mop.sideHit,
                                        mop.hitVec
                                );
                            }

                            player.inventory.currentItem = currentSlot;
                        }
                    }
                }
            }
        }
    }

    private int findWoolInHotbar(EntityPlayer player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() instanceof ItemBlock) {
                Block block = ((ItemBlock) stack.getItem()).getBlock();
                if (block == Blocks.wool) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static class SneakPlaceCommand extends CommandBase {
        @Override
        public String getCommandName() {
            return "sneakplace";
        }

        @Override
        public String getCommandUsage(ICommandSender sender) {
            return "/sneakplace [on|off|toggle]";
        }

        @Override
        public int getRequiredPermissionLevel() {
            return 0;
        }

        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (args.length == 0) {
                String status = enabled ? "§aenabled" : "§cdisabled";
                sender.addChatMessage(new ChatComponentText("§6[SneakPlace]§r Currently " + status));
                return;
            }

            String action = args[0].toLowerCase();

            if (action.equals("on")) {
                enabled = true;
                sender.addChatMessage(new ChatComponentText("§6[SneakPlace]§r §aEnabled"));
            } else if (action.equals("off")) {
                enabled = false;
                sender.addChatMessage(new ChatComponentText("§6[SneakPlace]§r §cDisabled"));
            } else if (action.equals("toggle")) {
                enabled = !enabled;
                String status = enabled ? "§aenabled" : "§cdisabled";
                sender.addChatMessage(new ChatComponentText("§6[SneakPlace]§r Now " + status));
            } else {
                sender.addChatMessage(new ChatComponentText("§6[SneakPlace]§r §cUsage: " + getCommandUsage(sender)));
            }
        }
    }
}