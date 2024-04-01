package jp.ne.suehiro;

import com.mojang.brigadier.arguments.StringArgumentType;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.network.PacketByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class GotoPositionMod implements ModInitializer {
    // カスタムパケットの識別子
    private static final Identifier PLAYER_POSITION_PACKET_ID = new Identifier("goto-position-mod", "player_position_packet");
    private ServerPlayerEntity targetPlayer = null;

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("gotouser")
                .executes(context -> {
                    targetPlayer = null;
                    return 1;
                })
                .then(CommandManager.argument("username", StringArgumentType.word())
                    .executes(context -> {
                        String username = StringArgumentType.getString(context, "username");
                        targetPlayer = context.getSource().getServer().getPlayerManager().getPlayer(username);
                        if (targetPlayer != null) {
                            // カスタムパケットを送信する処理をここに追加
                            sendPlayerPositionPacket();
                            context.getSource().sendFeedback(() -> Text.literal("プレイヤーの座標を送信しました"), false);
                        } else {
                            context.getSource().sendError(Text.literal("プレイヤーが見つかりませんでした"));
                        }
                        return 1;
                    })
            ));
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (targetPlayer == null) {
                return;
            }
            sendPlayerPositionPacket();
        });
    }

    // カスタムパケットの送信メソッド
    public void sendPlayerPositionPacket() {
        if (targetPlayer == null) {
            return;
        }
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeDouble(targetPlayer.getX());
        buf.writeDouble(targetPlayer.getZ());
        ServerPlayNetworking.send(targetPlayer, PLAYER_POSITION_PACKET_ID, buf);
    }
}