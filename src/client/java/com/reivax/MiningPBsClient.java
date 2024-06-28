package com.reivax;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockState;
import net.minecraft.world.World;

import java.io.*;

public class MiningPBsClient implements ClientModInitializer {
	private int oresMined = 0;
	private int personalBest = 0;
	private boolean isMining = false;
	private long sessionStartTime = 0;
	private long sessionTimeoutMillis = 1 * 60 * 1000; // 1 minute in milliseconds

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final File configFile = new File("config/mining_session.json");

	@Override
	public void onInitializeClient() {
		// Register event listeners
		ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
		PlayerBlockBreakEvents.AFTER.register(this::onBlockBreak);

		// Load session data from file
		loadSessionData();
	}

	private void onClientTick(MinecraftClient client) {
		if (isMining) {
			long currentTime = System.currentTimeMillis();
			long elapsedTime = currentTime - sessionStartTime;

			if (elapsedTime >= sessionTimeoutMillis) {
				endMiningSession(client.player);
			}
		}
	}

	private void onBlockBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
		Block block = state.getBlock();
		if (isOre(block)) {
			oresMined++;
			if (!isMining) {
				startMiningSession(player);
			}
			System.out.println("Ores mined: " + oresMined);
			player.sendMessage(Text.literal("Ores mined this trip: " + oresMined), true);

			if (oresMined > personalBest) {
				personalBest = oresMined;
			}
		}
	}

	private boolean isOre(Block block) {
		// Define your ore blocks here
		return block == Blocks.DIAMOND_ORE || block == Blocks.IRON_ORE || block == Blocks.GOLD_ORE || block == Blocks.REDSTONE_ORE || block == Blocks.LAPIS_ORE || block == Blocks.EMERALD_ORE || block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE || block == Blocks.DEEPSLATE_COAL_ORE || block == Blocks.DEEPSLATE_COPPER_ORE || block == Blocks.DEEPSLATE_GOLD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE || block == Blocks.DEEPSLATE_IRON_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE || block == Blocks.NETHER_QUARTZ_ORE || block == Blocks.NETHER_GOLD_ORE || block == Blocks.ANCIENT_DEBRIS || block == Blocks.COAL_ORE;
	}

	private void startMiningSession(PlayerEntity player) {
		oresMined = 1;
		isMining = true;
		sessionStartTime = System.currentTimeMillis();
		player.sendMessage(Text.literal("Started a new mining session!"), true);
	}

	private void endMiningSession(PlayerEntity player) {
		isMining = false;
		player.sendMessage(Text.literal("Mining session ended. Ores mined: " + oresMined), false);

		// Check if it's a new personal best and announce it
		if (oresMined > personalBest) {
			personalBest = oresMined;
			player.sendMessage(Text.literal("New personal best! Ores mined: " + personalBest), false);
			saveSessionData(); // Save session data only if there's a new personal best
		} else {
			saveSessionData(); // Still save session data to update oresMined
		}
	}

	private void saveSessionData() {
		MiningSessionData sessionData = new MiningSessionData(oresMined, personalBest);
		String json = GSON.toJson(sessionData);

		try (FileWriter writer = new FileWriter(configFile)) {
			writer.write(json);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadSessionData() {
		if (configFile.exists()) {
			try (FileReader reader = new FileReader(configFile)) {
				MiningSessionData sessionData = GSON.fromJson(reader, MiningSessionData.class);
				if (sessionData != null) {
					oresMined = sessionData.getOresMined();
					personalBest = sessionData.getPersonalBest();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// Helper class for session data serialization
	private static class MiningSessionData {
		private final int oresMined;
		private final int personalBest;

		public MiningSessionData(int oresMined, int personalBest) {
			this.oresMined = oresMined;
			this.personalBest = personalBest;
		}

		public int getOresMined() {
			return oresMined;
		}

		public int getPersonalBest() {
			return personalBest;
		}
	}
}
