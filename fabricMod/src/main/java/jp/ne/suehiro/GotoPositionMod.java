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
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class GotoPositionMod implements ModInitializer {
    // カスタムパケットの識別子
    private static final Identifier PLAYER_POSITION_PACKET_ID = new Identifier("goto-position-mod", "player_position_packet");
    private static final Identifier NAMED_OBJECT_POSITION_PACKET_ID = new Identifier("goto-position-mod", "named_object_position_packet");
    private ServerPlayerEntity serverPlayer = null;
    private ServerPlayerEntity targetPlayer = null;
    private Entity targetEntity = null;

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("gotouser")
                .executes(context -> {
                    targetPlayer = null;
                    serverPlayer = null;
                    targetEntity = null;
                    context.getSource().sendFeedback(() -> Text.literal("ターゲットプレイヤーの設定を解除しました"), false);
                    return 1;
                })
                .then(CommandManager.argument("username", StringArgumentType.word())
                .executes(context -> {
                    String username = StringArgumentType.getString(context, "username");
                    targetPlayer = context.getSource().getServer().getPlayerManager().getPlayer(username);
                    serverPlayer = context.getSource().getPlayer();
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
            dispatcher.register(CommandManager.literal("gotonamedobject")
                .executes(context -> {
                    targetPlayer = null;
                    serverPlayer = null;
                    targetEntity = null;
                    context.getSource().sendFeedback(() -> Text.literal("ターゲットオブジェクトの設定を解除しました"), false);
                    return 1;
                })
                .then(CommandManager.argument("objectname", StringArgumentType.word())
                .executes(context -> {
                    String objectName = StringArgumentType.getString(context, "objectname");
                    serverPlayer = context.getSource().getPlayer();
                    targetEntity = null;
                    serverPlayer.getWorld().getEntitiesByClass(Entity.class, serverPlayer.getBoundingBox().expand(1000), entity -> {
                        if (entity.hasCustomName() && entity.getCustomName().getString().equals(objectName)) {
                            targetEntity = entity;
                            sendNamedObjectPositionPacket();
                            context.getSource().sendFeedback(() -> Text.literal("指定した名前のオブジェクトを見つけました"), false);
                            return true;
                        }
                        return false;
                    });
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
        if (serverPlayer == null) {
            return;
        }
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeDouble(targetPlayer.getX());
        buf.writeDouble(targetPlayer.getZ());
        ServerPlayNetworking.send(serverPlayer, PLAYER_POSITION_PACKET_ID, buf);
    }

    public void sendNamedObjectPositionPacket() {
        if (targetEntity == null) {
            return;
        }
        if (serverPlayer == null) {
            return;
        }
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeDouble(targetEntity.getX());
        buf.writeDouble(targetEntity.getZ());
        ServerPlayNetworking.send(serverPlayer, NAMED_OBJECT_POSITION_PACKET_ID, buf);
    }

}