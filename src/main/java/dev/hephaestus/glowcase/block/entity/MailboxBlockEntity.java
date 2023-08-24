package dev.hephaestus.glowcase.block.entity;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

import dev.hephaestus.glowcase.Glowcase;
import dev.hephaestus.glowcase.block.MailboxBlock;
import org.jetbrains.annotations.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class MailboxBlockEntity extends BlockEntity {
    private final Deque<Message> messages = new ArrayDeque<>();
    private UUID owner;

    public MailboxBlockEntity(BlockPos pos, BlockState state) {
        super(Glowcase.MAILBOX_BLOCK_ENTITY, pos, state);
    }

    public void setOwner(ServerPlayerEntity player) {
        this.owner = player.getUuid();
        this.markDirty();
        this.dispatch();
    }

    public void addMessage(Message message) {
        this.messages.addFirst(message);

        if (this.world != null) {
            this.world.setBlockState(this.pos, this.getCachedState().with(MailboxBlock.HAS_MAIL, true));
        }

        this.markDirty();
        this.dispatch();
    }

    public void removeMessage() {
        if (this.world != null && this.messages.isEmpty()) {
            //whuh? This prooobably shouldn't happen, but if it does just reset the state to empty
            this.world.setBlockState(this.pos, this.getCachedState().with(MailboxBlock.HAS_MAIL, false));
        } else if (this.messages.removeFirst() != null && this.world != null && this.messages.isEmpty()) {
            this.world.setBlockState(this.pos, this.getCachedState().with(MailboxBlock.HAS_MAIL, false));
            this.markDirty();
            this.dispatch();
        }
    }

    public int messageCount() {
        return this.messages.size();
    }

    public Message getMessage() {
        return this.messages.getFirst();
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        nbt.putUuid("Owner", this.owner);

        NbtList list = nbt.getList("Messages", NbtElement.COMPOUND_TYPE);

        for (Message message : this.messages) {
            NbtCompound messageTag = new NbtCompound();

            messageTag.putUuid("Sender", message.sender);
            messageTag.putString("SenderName", message.senderName);
            messageTag.putString("Message", message.message);

            list.add(messageTag);
        }

        nbt.put("Messages", list);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        this.owner = nbt.getUuid("Owner");
        this.messages.clear();

        for (NbtElement element : nbt.getList("Messages", NbtElement.COMPOUND_TYPE)) {
            if (element instanceof NbtCompound message) {
                this.messages.addLast(new Message(
                        message.getUuid("Sender"),
                        message.getString("SenderName"),
                        message.getString("Message")
                ));
            }
        }
    }

    public UUID owner() {
        return this.owner;
    }

    public void removeAllMessagesFromMostRecentSender() {
        if (!this.messages.isEmpty()) {
            UUID sender = this.messages.pop().sender;

            this.messages.removeIf(message -> message.sender.equals(sender));
            this.markDirty();
            this.dispatch();
        }
    }

    public static record Message(UUID sender, String senderName, String message) {}

    // standard blockentity boilerplate

    public void dispatch() {
        if (world instanceof ServerWorld sworld) sworld.getChunkManager().markForUpdate(pos);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }
}
