package com.aiq;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PointManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("aiq-mod");
    private static final String OBJECTIVE = "aiq_points";
    private static final Map<UUID, Long> LAST_USE_TIME = new HashMap<>();

    public static int getPoints(ServerPlayerEntity player) {
        try {
            Scoreboard scoreboard = player.getScoreboard();
            if (scoreboard.getNullableObjective(OBJECTIVE) == null) {
                scoreboard.addObjective(
                    OBJECTIVE,
                    net.minecraft.scoreboard.ScoreboardCriterion.DUMMY,
                    net.minecraft.text.Text.literal("AI点数"),
                    net.minecraft.scoreboard.ScoreboardCriterion.RenderType.INTEGER
                );
            }
            return scoreboard.getOrCreateScore(player, scoreboard.getObjective(OBJECTIVE)).getScore();
        } catch (Exception e) {
            LOGGER.warn("获取点数失败", e);
            return AIQConfig.get().defaultPoints;
        }
    }

    public static void setPoints(ServerPlayerEntity player, int points) {
        try {
            Scoreboard scoreboard = player.getScoreboard();
            if (scoreboard.getNullableObjective(OBJECTIVE) == null) {
                scoreboard.addObjective(
                    OBJECTIVE,
                    net.minecraft.scoreboard.ScoreboardCriterion.DUMMY,
                    net.minecraft.text.Text.literal("AI点数"),
                    net.minecraft.scoreboard.ScoreboardCriterion.RenderType.INTEGER
                );
            }
            scoreboard.getOrCreateScore(player, scoreboard.getObjective(OBJECTIVE)).setScore(points);
            if (scoreboard instanceof ServerScoreboard serverScoreboard) {
                // 自动保存由服务器定期处理
            }
        } catch (Exception e) {
            LOGGER.warn("设置点数失败", e);
        }
    }

    public static void addPoints(ServerPlayerEntity player, int delta) {
        int current = getPoints(player);
        setPoints(player, Math.max(0, current + delta));
    }

    public static boolean tryConsume(ServerPlayerEntity player, int cost) {
        int current = getPoints(player);
        if (current < cost) {
            return false;
        }
        setPoints(player, current - cost);
        return true;
    }

    public static void ensureInitialized(ServerPlayerEntity player) {
        // 初次访问时若分数为 0 且从未初始化，则设为默认值
        int current = getPoints(player);
        if (current == 0) {
            // 初次登录赠送默认点数
            setPoints(player, AIQConfig.get().defaultPoints);
        }
    }

    public static boolean checkCooldown(UUID playerUuid) {
        long now = System.currentTimeMillis();
        long cooldownMs = (long) AIQConfig.get().cooldownSeconds * 1000L;
        Long last = LAST_USE_TIME.get(playerUuid);
        if (last != null && (now - last) < cooldownMs) {
            return false;
        }
        LAST_USE_TIME.put(playerUuid, now);
        return true;
    }

    public static long remainingCooldownMs(UUID playerUuid) {
        long now = System.currentTimeMillis();
        long cooldownMs = (long) AIQConfig.get().cooldownSeconds * 1000L;
        Long last = LAST_USE_TIME.get(playerUuid);
        if (last == null) return 0;
        long remaining = cooldownMs - (now - last);
        return Math.max(0, remaining);
    }

    public static void initializeServer(MinecraftServer server) {
        // 服务器启动时确保目标存在
        try {
            ServerScoreboard scoreboard = server.getScoreboard();
            if (scoreboard.getNullableObjective(OBJECTIVE) == null) {
                scoreboard.addObjective(
                    OBJECTIVE,
                    net.minecraft.scoreboard.ScoreboardCriterion.DUMMY,
                    net.minecraft.text.Text.literal("AI点数"),
                    net.minecraft.scoreboard.ScoreboardCriterion.RenderType.INTEGER
                );
            }
        } catch (Exception e) {
            LOGGER.warn("初始化记分板失败", e);
        }
    }
}
