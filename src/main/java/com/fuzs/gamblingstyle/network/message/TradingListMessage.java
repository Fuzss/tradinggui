package com.fuzs.gamblingstyle.network.message;

import com.fuzs.gamblingstyle.GamblingStyle;
import com.fuzs.gamblingstyle.client.gui.GuiVillager;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.village.MerchantRecipeList;

import java.io.IOException;

public class TradingListMessage extends Message<TradingListMessage> {

    private PacketBuffer data;

    @SuppressWarnings("unused")
    public TradingListMessage() {

    }

    public TradingListMessage(PacketBuffer bufIn) {

        this.data = bufIn;
        if (bufIn.writerIndex() > 1048576) {

            throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
        }
    }

    @Override
    public void read(ByteBuf buf) {

        int bytes = buf.readableBytes();
        if (bytes >= 0 && bytes <= 1048576) {

            this.data = new PacketBuffer(buf.readBytes(bytes));
        } else {

            throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
        }
    }

    @Override
    public void write(ByteBuf buf) {

        synchronized (this.data) {

            this.data.markReaderIndex();
            buf.writeBytes(this.data);
            this.data.resetReaderIndex();
        }
    }

    @Override
    protected MessageProcessor createProcessor() {

        return new TradingListProcessor();
    }

    private class TradingListProcessor implements MessageProcessor {

        @Override
        public void accept(EntityPlayer player) {

            try {

                Minecraft mc = Minecraft.getMinecraft();
                int windowId = TradingListMessage.this.data.readInt();
                GuiScreen guiscreen = mc.currentScreen;
                if (guiscreen instanceof GuiVillager && windowId == mc.player.openContainer.windowId) {

                    IMerchant imerchant = ((GuiVillager) guiscreen).getMerchant();
                    MerchantRecipeList merchantrecipelist = MerchantRecipeList.readFromBuf(TradingListMessage.this.data);
                    imerchant.setRecipes(merchantrecipelist);
                }
            } catch (IOException e) {

                GamblingStyle.LOGGER.error("Couldn't load trade info", e);
            }
        }

    }
}