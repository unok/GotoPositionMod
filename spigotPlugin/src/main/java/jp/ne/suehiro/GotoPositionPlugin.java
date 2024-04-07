package jp.ne.suehiro;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import java.util.HashMap;
import java.util.Map;

public class GotoPositionPlugin extends JavaPlugin {
    private Map<Player, Player> targetPlayers = new HashMap<>();
    private Map<Player, Entity> targetEntities = new HashMap<>();

    @Override
    public void onEnable() {
        // プラグインが有効化されたときの処理
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("このコマンドはプレイヤーのみ実行可能です");
            return true;
        }

        Player userPlayer = (Player) sender;

        if (command.getName().equalsIgnoreCase("gotouser")) {
            if (args.length == 0) {
                targetPlayers.remove(userPlayer);
                userPlayer.sendMessage("ターゲットプレイヤーの設定を解除しました");
            } else {
                String username = args[0];
                Player targetPlayer = Bukkit.getPlayer(username);
                if (targetPlayer != null) {
                    targetPlayers.put(userPlayer, targetPlayer);
                    userPlayer.sendMessage("プレイヤーの座標を送信しました");
                    sendPlayerPositionPacket(userPlayer);
                } else {
                    userPlayer.sendMessage("プレイヤーが見つかりませんでした");
                }
            }
            return true;
        } else if (command.getName().equalsIgnoreCase("gotonamedobject")) {
            if (args.length == 0) {
                targetEntities.remove(userPlayer);
                userPlayer.sendMessage("ターゲットオブジェクトの設定を解除しました");
            } else {
                String objectName = args[0];
                Entity targetEntity = null;
                for (Entity entity : Bukkit.getWorlds().get(0).getEntities()) {
                    if (entity.getCustomName() != null && entity.getCustomName().equals(objectName)) {
                        targetEntity = entity;
                        break;
                    }
                }
                if (targetEntity != null) {
                    targetEntities.put(userPlayer, targetEntity);
                    userPlayer.sendMessage("指定した名前のオブジェクトを見つけました");
                    sendNamedObjectPositionPacket(userPlayer);
                } else {
                    userPlayer.sendMessage("指定した名前のオブジェクトが見つかりませんでした");
                }
            }
            return true;
        }
        return false;
    }

    private void sendPlayerPositionPacket(Player userPlayer) {
        Player targetPlayer = targetPlayers.get(userPlayer);
        if (targetPlayer == null)
            return;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!targetPlayers.containsKey(userPlayer)) {
                    cancel();
                    return;
                }
                updateTitleForPlayer(userPlayer);
                userPlayer.sendMessage(String.format("%.2f,%.2f", targetPlayer.getLocation().getX(),
                        targetPlayer.getLocation().getZ()));
            }
        }.runTaskTimer(this, 0, 20);
    }

    private void sendNamedObjectPositionPacket(Player userPlayer) {
        Entity targetEntity = targetEntities.get(userPlayer);
        if (targetEntity == null)
            return;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!targetEntities.containsKey(userPlayer)) {
                    cancel();
                    return;
                }
                userPlayer.sendMessage(String.format("%.2f,%.2f", targetEntity.getLocation().getX(),
                        targetEntity.getLocation().getZ()));
            }
        }.runTaskTimer(this, 0, 20);
    }

    public void updateTitleForPlayer(Player userPlayer) {
        Player targetPlayer = targetPlayers.get(userPlayer);
        if (targetPlayer == null)
            return;

        // 目標位置との角度を計算
        updateDisplay(userPlayer, targetPlayer.getLocation().getX(), targetPlayer.getLocation().getZ());
    }

    public void updateTitleForEntity(Player userPlayer) {
        Entity targetEntity = targetEntities.get(userPlayer);
        if (targetEntity == null)
            return;

        // 目標位置との角度を計算
        updateDisplay(userPlayer, targetEntity.getLocation().getX(), targetEntity.getLocation().getZ());
    }

    private void updateDisplay(Player userPlayer, double targetX, double targetZ) {
        double playerX = userPlayer.getLocation().getX();
        double playerZ = userPlayer.getLocation().getZ();
        double deltaX = targetX - playerX;
        double deltaZ = targetZ - playerZ;
        float playerYaw = userPlayer.getLocation().getYaw();

        // 目標位置との角度を計算

        double targetAngle = Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90;

        // プレイヤーの向きと目標位置との角度差を計算
        double angleDifference = Math.floorMod((long) (targetAngle - playerYaw), 360);
        if (angleDifference > 180) {
            angleDifference -= 360;
        }

        // 目標位置までの距離を計算
        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        // 角度差を表示
        int leftArrows = 0;
        int rightArrows = 0;
        String color = "#CCCCCC";
        // 角度差が小さいほどarrowの増加率を大きくする
        if (angleDifference < 0) {
            leftArrows = getArrowCount(angleDifference);
            color = "#0000FF";
        } else {
            rightArrows = getArrowCount(angleDifference);
            color = "#FF0000";
        }
        String angleText = String.format("%12s Angle: %6.1f° %-12s", "<".repeat(leftArrows), angleDifference,
                ">".repeat(rightArrows));

        // スコアボードにメッセージと距離を設定
        Objective objective = userPlayer.getScoreboard().getObjective("GotoPosition");
        if (objective == null) {
            objective = userPlayer.getScoreboard().registerNewObjective("GotoPosition", "dummy", "GotoPosition");
        }
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.getScore("<" + color + ">" + angleText + "</color>").setScore((int) distance);
    }

    private int getArrowCount(double angleDifference) {
        return (int) Math.ceil(18 * (1 - Math.exp(-0.005 * Math.abs(angleDifference))));
    }
}