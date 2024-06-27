package com.reivax;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

public class MiningPBsClient implements ClientModInitializer {
	private int oresMined = 0;
	private int personalBest = 0;
	private boolean isMining = false;

	@Override
	public void onInitializeClient() {
		// Register event listeners
		ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
		UseBlockCallback.EVENT.register(this::onBlockBreak);
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> resetMiningSession());
	}

	private ActionResult onBlockBreak(PlayerEntity playerEntity, World world, Hand hand, BlockHitResult blockHitResult) {
		return null;
	}

	private void onClientTick(MinecraftClient client) {
		if (client.player != null && !isMining) {
			startMiningSession(client.player);
		}
	}

	private ActionResult onBlockBreak(ClientPlayerEntity player, ClientWorld world, net.minecraft.util.Hand hand, net.minecraft.util.hit.BlockHitResult hitResult) {
		Block block = world.getBlockState(hitResult.getBlockPos()).getBlock();
		if (isOre(block)) {
			oresMined++;
			player.sendMessage(Text.literal("Ores mined this trip: " + oresMined), false);

			if (oresMined > personalBest) {
				personalBest = oresMined;
				player.sendMessage(Text.literal("New personal best!"), false);
			}
			return ActionResult.SUCCESS;
		}
		return ActionResult.PASS;
	}

	private boolean isOre(Block block) {
		return block == Blocks.DIAMOND_ORE || block == Blocks.IRON_ORE || block == Blocks.GOLD_ORE; // Add other ores as needed
	}

	private void startMiningSession(ClientPlayerEntity player) {
		oresMined = 0;
		isMining = true;
		player.sendMessage(Text.literal("Started a new mining session!"), false);
	}

	private void resetMiningSession() {
		oresMined = 0;
		isMining = false;
	}
}
