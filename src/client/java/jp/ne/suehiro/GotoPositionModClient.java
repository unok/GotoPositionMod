package jp.ne.suehiro;

import com.mojang.brigadier.arguments.DoubleArgumentType;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public class GotoPositionModClient implements ClientModInitializer {
    private double targetX = 100; // 目標位置のX座標
    private double targetZ = 200; // 目標位置のZ座標
    private boolean isDisplaying = false; // 表示フラグ

    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
        HudRenderCallback.EVENT.register((matrixStack, tickDelta) -> {
            this.onRenderGameOverlay(matrixStack);
        });

        // コマンドを登録
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("gotoposition")
                    .executes(context -> {
                        // 引数なしの場合、表示を解除
                        setDisplaying(false);
                        context.getSource().sendFeedback(Text.literal("目標位置の表示を解除しました"));
                        return 1;
                    })
                    .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument("x", DoubleArgumentType.doubleArg())
                            .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument("z", DoubleArgumentType.doubleArg())
                                    .executes(context -> {
                                        double x = DoubleArgumentType.getDouble(context, "x");
                                        double z = DoubleArgumentType.getDouble(context, "z");
                                        setTargetPosition(x, z);
                                        setDisplaying(true);
                                        context.getSource().sendFeedback(Text.literal("目標位置を (" + x + ", " + z + ") に設定しました"));
                                        return 1;
                                    }))));
        });
    }

    public void onRenderGameOverlay(DrawContext drawContext) {
        if (!isDisplaying) return; // 表示フラグがfalseの場合、描画をスキップ

        // プレイヤーの現在位置と向きを取得
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        double playerX = player.getX();
        double playerZ = player.getZ();
        float playerYaw = player.getYaw();

        // 目標位置との角度を計算
        double deltaX = targetX - playerX;
        double deltaZ = targetZ - playerZ;
        double targetAngle = Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90;

        // プレイヤーの向きと目標位置との角度差を計算
        double angleDifference = Math.floorMod((long)(targetAngle - playerYaw), 360);
        if (angleDifference > 180) {
            angleDifference -= 360;
        }

        // 目標位置までの距離を計算
        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        TextRenderer textRenderer = client.textRenderer;

        // 角度差を画面上部中央に表示
        int leftArrows = 0;
        int rightArrows = 0;
        // 角度差が小さいほどarrowの増加率を大きくする
        if (angleDifference < 0) {
            leftArrows = getArrowCount(angleDifference);
        } else {
            rightArrows = getArrowCount(angleDifference);
        }
        String angleText = String.format("%-12s Angle: %6.1f° %12s", "<".repeat(leftArrows), angleDifference, ">".repeat(rightArrows));
        int angleColor = rightArrows > 0 ? 0xFF0000 : (leftArrows > 0 ? 0x0000FF : 0xFFFFFF);
        // 距離を画面上部中央に表示
        String distanceText = String.format("// Distance:  %.1f m // Target: (%.1f, %.1f) //  Player: (%.1f, %.1f) //", distance, targetX, targetZ, playerX, playerZ);
        int distanceColor = 0xFFFFFF; // 距離の色は白で統一

        int angleX = MinecraftClient.getInstance().getWindow().getScaledWidth() / 2 - textRenderer.getWidth(angleText) / 2;
        int angleY = 10;
        int distanceX = MinecraftClient.getInstance().getWindow().getScaledWidth() / 2 - textRenderer.getWidth(distanceText) / 2;
        int distanceY = 30; // 角度の下に距離を表示するため、Y座標を下げる

        // DrawContextを使用して、プレイヤーの現在位置から目標位置までの角度差を画面上部中央に表示
        drawContext.drawText(textRenderer, angleText, angleX, angleY, angleColor, true);
        // DrawContextを使用して、プレイヤーの現在位置から目標位置までの距離を画面上部中央に表示
        drawContext.drawText(textRenderer, distanceText, distanceX, distanceY, distanceColor, true);
    }
    
    private int getArrowCount(double angleDifference) {
        return (int)Math.ceil(18 * (1 - Math.exp(-0.005 * Math.abs(angleDifference))));
    }

    private void setTargetPosition(double x, double z) {
        targetX = x;
        targetZ = z;
		setDisplaying(true);
    }

    private void setDisplaying(boolean displaying) {
        isDisplaying = displaying;
    }
}