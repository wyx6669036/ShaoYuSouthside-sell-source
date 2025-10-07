package com.rebane2001.livemessage.gui;

import com.google.gson.Gson;
import com.rebane2001.livemessage.Livemessage;
import com.rebane2001.livemessage.util.LiveProfileCache;
import com.rebane2001.livemessage.util.LiveProfileCache.LiveProfile;
import com.rebane2001.livemessage.util.LiveSkinUtil;
import com.rebane2001.livemessage.util.LivemessageUtil;
import dev.diona.southside.util.chat.Chat;
import dev.diona.southside.util.chat.ChatChannel;
import dev.diona.southside.util.chat.Member;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
import org.lwjglx.input.Keyboard;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import static com.rebane2001.livemessage.gui.GuiUtil.*;

public class ChatWindow extends LiveWindow {

    boolean valid;

    String liveProfile;
    boolean pastNamesB = false;
    int longestPastName = 80;
    String msgString;

    final int scrollBarWidth = 10;
    int scrollBarHeight = 50;
    int chatScrollPosition = 0;
    boolean scrolling = false;
    public boolean chatScrolledToBottom = true;

    protected GuiTextField inputField;
    public LivemessageUtil.ChatSettings chatSettings;

    final int chatBoxY = titlebarHeight + 44;
    final int chatBoxX = 5;
    final int chatBoxSize = 13;

    List<ChatMessage> chatHistory = new ArrayList<>();

    QuintAnimation hatFade = new QuintAnimation(300, 1f);
    QuintAnimation fullSkinAnim = new QuintAnimation(600, 0f);

    /**
     * (the pony race not the human race).
     */

    public static class ChatMessage {
        public Member member;
        public String message;
        public boolean sentByMe;
        public long timestamp;
        public UUID myUUID;

        ChatMessage(final Member member, String message, boolean sentByMe, long timestamp) {
            this.member = member;
            this.message = message;
            this.sentByMe = sentByMe;
            this.timestamp = timestamp;
        }
    }

    //TODO: Implement offline-mode players
    /**
     * Initializes ChatWindow.
     */
    public ChatWindow(String liveProfile) {
        if (liveProfile == null) {
            System.out.println("[Livemessage] Tried to open an invalid chat window - offline mode?");
            valid = false;
            return;
        }
        valid = true;
        minw = 280;

        this.liveProfile = liveProfile;
        chatSettings = LivemessageUtil.getChatSettings(liveProfile);
        loadWindowColor();
        loadChatHistory();

        initButtons();

        //liveSkinUtil = new LiveSkinUtil(liveProfile.uuid);

        msgString = "/msg " + liveProfile + " ";

        // Initialize text box
        this.inputField = new GuiTextField(0, this.fontRenderer, 9, this.h - 16, this.w - 18, 12);
        this.inputField.setMaxStringLength(256 - msgString.length());
        this.inputField.setEnableBackgroundDrawing(false);
        this.inputField.setFocused(true);
        this.inputField.setText("");
        this.inputField.setCanLoseFocus(false);
        this.inputField.setTextColor(getSingleRGB(255));

        // Hide text box cursor
        for (int j = 0; j < 6; j++)
            this.inputField.updateCursorCounter();

        chatScrollPosition = Math.max(chatHistory.size() - 6, 0);
        animateInStart = System.currentTimeMillis();
    }

    /**
     * Initializes buttons.
     */
    public void initButtons() {
    }


    /**
     * Sets the primary color of the window.
     */
    public void loadWindowColor() {
        primaryColor = GuiUtil.getWindowColor(liveProfile);
    }

    /**
     * Loads chat history from disk.
     */
    public void loadChatHistory() {
        Gson gson = new Gson();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(
                    String.valueOf(Livemessage.modFolder.resolve("messages/" + liveProfile.toString() + ".jsonl"))));
            String line = reader.readLine();
            while (line != null) {
                try {
                    chatHistory.add(gson.fromJson(line, ChatMessage.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            //TODO: Add better error handling
            //e.printStackTrace();
        }
    }

    public void keyTyped(char typedChar, int keyCode) {
        markAsRead();
        if (keyCode != Keyboard.KEY_RETURN && keyCode != Keyboard.KEY_NUMPADENTER) {
            if (keyCode == Keyboard.KEY_UP) {
                //this.getSentHistory(-1);
            } else if (keyCode == Keyboard.KEY_DOWN) {
                //this.getSentHistory(1);
            } else if (keyCode == Keyboard.KEY_PRIOR) {
                chatScrollPosition = MathHelper.clamp(chatScrollPosition - 10, 0, Math.max(chatHistory.size() - 1, 0));
            } else if (keyCode == Keyboard.KEY_NEXT) {
                chatScrollPosition = MathHelper.clamp(chatScrollPosition + 10, 0, Math.max(chatHistory.size() - 1, 0));
            } else {
                this.inputField.textboxKeyTyped(typedChar, keyCode);
            }
        } else {
            String s = this.inputField.getText().trim();
            if (!s.isEmpty()) {
                final var chatChannel = Chat.getInstance().getChannelsFromName().get(liveProfile);
                Chat.getInstance().sendMessage(chatChannel, s);
                this.inputField.setText("");
            }
        }
        super.keyTyped(typedChar, keyCode);
    }

    public void mouseWheel(int mWheelState) {
        markAsRead();
        chatScrollPosition += (mWheelState < 0 ? 1 : -1) * (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ? 10 : 1);
        chatScrollPosition = MathHelper.clamp(chatScrollPosition, 0, Math.max(chatHistory.size() - 1, 0));
        super.mouseWheel(mWheelState);
    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
        scrolling = false;
        super.mouseReleased(mouseX, mouseY, state);
    }

    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (scrolling) {
            int totalPixels = (h - (chatBoxY + 10 + chatBoxSize + scrollBarHeight));
            float oneLine = totalPixels * 1f / (chatHistory.size() - 1);
            float onePixel = 1f / oneLine;
            chatScrollPosition = (int) MathHelper.clamp((mouseY - (dragY + chatBoxY + this.y)) * onePixel, 0, chatHistory.size() - 1);
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseInRect(0, 0, w, h, mouseX, mouseY))
            markAsRead();
        liveButtons.forEach(LiveButton::runIfClicked);
        if (pastNamesB) {
            pastNamesB = false;
            return;
        }
        if (mouseInRect(chatBoxX + w - 10 - scrollBarWidth, chatBoxY, scrollBarWidth, h - (chatBoxY + 10 + chatBoxSize), mouseX, mouseY) && chatHistory.size() > 1) {
            scrolling = true;
            dragY = mouseY - (this.y + chatBoxY + (h - (chatBoxY + 10 + chatBoxSize + scrollBarHeight)) * chatScrollPosition / (chatHistory.size() - 1));
        }
        longestPastName = Math.max(fontRenderer.getStringWidth(liveProfile), longestPastName);
        if (mouseX > x + 40 && mouseX < x + 40 + longestPastName + 4 && mouseY > y + titlebarHeight + 4 && mouseY < y + titlebarHeight + 4 + 12) {
            pastNamesB = true;
            return;
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public void activateWindow() {
        markAsRead();
        super.activateWindow();
    }

    public void markAsRead() {
        LivemessageGui.unreadMessages.put(liveProfile, 0);
    }

    /**
     * Draws the text part of the chat history.
     */
    private void drawChatHistory(int chatBoxX, int chatBoxY, int chatColorMe, int chatColorOther) {
        if (chatHistory.size() == 0) {
            fontRenderer.drawString("You're chatting with " + liveProfile, chatBoxX + 4, chatBoxY + 5, getSingleRGB(96));
            chatScrolledToBottom = false;
            return;
        }
        int drawHeight = 0;
        chatScrolledToBottom = true;
        for (int i = chatScrollPosition; i < chatHistory.size(); i++) {
            ChatMessage chatMessage = chatHistory.get(i);
            if (chatMessage.member == null) continue;
            boolean isTrimmed = false;
            String message = chatMessage.message;
            while (true) {
                if (chatBoxY + 5 + 12 * drawHeight > h - 34) {
                    chatScrolledToBottom = false;
                    break;
                }
                if (!isTrimmed)
                    message = String.format("<%s> %s", chatMessage.member.getName(), message);
                    //message = String.format("<%s> %s %s %s %s %s", chatMessage.member.getName(), chatMessage.member.getLocation(), chatMessage.member.getClient(), chatMessage.member.getMcName(), chatMessage.member.getUuid(), message);
                int maxWidth = w - (chatBoxX * 2 + 8 + (isTrimmed ? fontRenderer.getStringWidth("<00:00> ") : 0) + scrollBarWidth - 5);
                String trimmed = fontRenderer.trimStringToWidth(message, maxWidth);
                fontRenderer.drawString(trimmed, chatBoxX + 4 + (isTrimmed ? fontRenderer.getStringWidth("<00:00> ") : 0), chatBoxY + 5 + 12 * drawHeight, chatMessage.sentByMe ? chatColorMe : chatColorOther);

                drawHeight++;
                if (message.equals(trimmed))
                    break;
                message = message.substring(trimmed.length());
                isTrimmed = true;
            }
            if (!chatScrolledToBottom)
                break;
        }
        if (chatScrolledToBottom && chatBoxY + 5 + 12 * (drawHeight + 2) <= h - 34)
            chatScrolledToBottom = false;
    }

    /**
     * Draws profile pic and determines race (the pony race not the human race).
     */
    private void drawProfilePic(int x, int y) {
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        boolean removeHat = (lastMouseX > this.x + x && lastMouseX < this.x + x + 32 && lastMouseY > this.y + y && lastMouseY < this.y + y + 32);
        float progress = fullSkinAnim.animate(removeHat && clicked && !dragging && !resizing && !scrolling ? 1F : 0F);
        int sizeInt = Math.round(8 + (progress * 56));
        if (progress > 0)
            drawRect(-this.x, -this.y, LivemessageGui.screenWidth, LivemessageGui.screenHeight, getRGBA(0, 0, 0, (int) (progress * 128f) + 1));

        //GlStateManager.color(1.0F, 1.0F, 1.0F, 1F);

        //ResourceLocation skinTexture = liveSkinUtil.getLocationSkin();

        //Minecraft.getMinecraft().getTextureManager().bindTexture(skinTexture);

        /*
        if (liveSkinUtil.customSkinLoaded() && race == -1) {
            race = 0;
            try {
                byte[] pixels = new byte[64 * 64 * 4];
                ByteBuffer buffer = ByteBuffer.allocateDirect(pixels.length);
                GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
                buffer.get(pixels);
                int raceColor = getRGB(pixels[0] & 0xFF, pixels[1] & 0xFF, pixels[2] & 0xFF);
                if (races.containsKey(raceColor))
                    race = raceColor;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Gui.drawScaledCustomSizeModalRect(Math.round(x - (progress * 32)), Math.round(y - (progress * 32)), 8f - progress * 8f, 8f - progress * 8f, sizeInt, sizeInt, sizeInt * 4, sizeInt * 4, 64.0F, 64.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, hatFade.animate(removeHat ? 0F : 1F));
        Gui.drawScaledCustomSizeModalRect(x, y, 40.0F, (float) 8, 8, 8, 32, 32, 64.0F, 64.0F);

         */
    }

    /**
     * Mane drawing bit.
     */
    public void drawWindow(int bgColor, int fgColor) {
        boolean online = true;
        title = liveProfile;
        int unreads = LivemessageGui.unreadMessages.getOrDefault(liveProfile, 0);
        if (unreads > 0)
            title += " \u00A7l(" + unreads + ")";
        scrollBarHeight = (chatHistory.size() < 2) ? 0 : (int) MathHelper.clamp(Math.floor((h - (chatBoxY + 10 + chatBoxSize)) / Math.max((chatHistory.size() - 1) / 10, 1)), 10, (h - (chatBoxY + 10 + chatBoxSize)) / 2);
        super.drawWindow(bgColor, fgColor);
        // Profile pic
        //drawRect(3, titlebarHeight + 3, 36, 36, (online) ? getRGB(60, 148, 100) : getSingleRGB(128));
        if (lastMouseX > x + 40 && lastMouseX < x + 40 + fontRenderer.getStringWidth(liveProfile) + 4 && lastMouseY > y + titlebarHeight + 3 && lastMouseY < y + titlebarHeight + 4 + 12)
            drawRect(40, titlebarHeight + 3, fontRenderer.getStringWidth(liveProfile) + 4, 12, getSingleRGB(64));
        String displayUsername = liveProfile;
        if (chatSettings.isFriend)
            displayUsername += " (friend)";
        if (chatSettings.isBlocked)
            displayUsername += " (blocked)";
        fontRenderer.drawString(displayUsername, 15, titlebarHeight + 5, getSingleRGB(255));
        fontRenderer.drawString(liveProfile, 15, titlebarHeight + 5 + 11, getSingleRGB(128));
        fontRenderer.drawString((online) ? "online" : "offline", 15, titlebarHeight + 5 + 21, getSingleRGB(128));

        // Buttons
        liveButtons.forEach(LiveButton::draw);
        int chatbg = 36;
        int textbg = 24;

        // Chathistory box
        drawRect(chatBoxX - 1, chatBoxY - 1, w - 10 + 2, h - (chatBoxY + 10 + chatBoxSize) + 2, getSingleRGB(64));
        drawRect(chatBoxX, chatBoxY, w - 10, h - (chatBoxY + 10 + chatBoxSize), getSingleRGB(chatbg));
        // Message box
        drawRect(chatBoxX - 1, chatBoxY - 1 + h - (chatBoxY + 5 + chatBoxSize), w - 10 + 2, chatBoxSize + 2, getSingleRGB(64));
        drawRect(chatBoxX, chatBoxY + h - (chatBoxY + 5 + chatBoxSize), w - 10, chatBoxSize, getSingleRGB(textbg));

        //scrollbar
        if (chatHistory.size() > 1)
            drawRect(chatBoxX + w - 10 - scrollBarWidth, chatBoxY + (h - (chatBoxY + 10 + chatBoxSize + scrollBarHeight)) * chatScrollPosition / (chatHistory.size() - 1), scrollBarWidth, scrollBarHeight,
                    (scrolling) ? getSingleRGB(128) : (mouseInRect(chatBoxX + w - 10 - scrollBarWidth, chatBoxY, scrollBarWidth, h - (chatBoxY + 10 + chatBoxSize), lastMouseX, lastMouseY)) ? getSingleRGB(96) : getSingleRGB(64)
            );

        this.inputField.setTextColor(getSingleRGB((active) ? 255 : 128));
        this.inputField.x = 8;
        this.inputField.y = this.h - chatBoxSize - 2;
        this.inputField.width = this.w - 18;
        this.inputField.drawTextBox();

        drawChatHistory(chatBoxX, chatBoxY, getSingleRGB(255), new Color(255, 255, 255).getRGB());
        //drawProfilePic(5, titlebarHeight + 5);

        // Tooltips
        liveButtons.forEach(LiveButton::drawTooltips);
    }
}
