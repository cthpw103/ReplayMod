package com.replaymod.replay.handler;

import com.replaymod.replay.ReplayModReplay;
import com.replaymod.replay.gui.screen.GuiReplayViewer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiHandler {
    private static final int BUTTON_EXIT_SERVER = 1;
    private static final int BUTTON_ADVANCEMENTS = 5;
    private static final int BUTTON_STATS = 6;
    private static final int BUTTON_OPEN_TO_LAN = 7;

    private static final int BUTTON_REPLAY_VIEWER = 17890234;
    private static final int BUTTON_EXIT_REPLAY = 17890235;

    private static final Minecraft mc = Minecraft.getMinecraft();

    private final ReplayModReplay mod;

    public GuiHandler(ReplayModReplay mod) {
        this.mod = mod;
    }

    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void injectIntoIngameMenu(GuiScreenEvent.InitGuiEvent.Post event) {
        if (!(event.getGui() instanceof GuiIngameMenu)) {
            return;
        }

        if (mod.getReplayHandler() != null) {
            // Pause replay when menu is opened
            mod.getReplayHandler().getReplaySender().setReplaySpeed(0);

            GuiButton achievements = null, stats = null, openToLan = null;
            List<GuiButton> buttonList = event.getButtonList();
            for(GuiButton b : new ArrayList<>(buttonList)) {
                switch (b.id) {
                    // Replace "Exit Server" button with "Exit Replay" button
                    case BUTTON_EXIT_SERVER:
                        b.displayString = I18n.format("replaymod.gui.exit");
                        b.id = BUTTON_EXIT_REPLAY;
                        break;
                    // Remove "Advancements", "Stats" and "Open to LAN" buttons
                    case BUTTON_ADVANCEMENTS:
                        buttonList.remove(achievements = b);
                        break;
                    case BUTTON_STATS:
                        buttonList.remove(stats = b);
                        break;
                    case BUTTON_OPEN_TO_LAN:
                        buttonList.remove(openToLan = b);
                        break;
                }
            }
            if (achievements != null && stats != null) {
                moveAllButtonsDirectlyBelowUpwards(buttonList, achievements.y,
                        achievements.x, stats.x + stats.width);
            }
            if (openToLan != null) {
                moveAllButtonsDirectlyBelowUpwards(buttonList, openToLan.y,
                        openToLan.x, openToLan.x + openToLan.width);
            }
        }
    }

    /**
     * Moves all buttons that are within a rectangle below a certain y coordinate upwards by 24 units.
     * @param buttons List of buttons
     * @param belowY The Y limit
     * @param xStart Left x limit of the rectangle
     * @param xEnd Right x limit of the rectangle
     */
    private void moveAllButtonsDirectlyBelowUpwards(List<GuiButton> buttons, int belowY, int xStart, int xEnd) {
        for (GuiButton button : buttons) {
            if (button.y >= belowY && button.x <= xEnd && button.x + button.width >= xStart) {
                button.y -= 24;
            }
        }
    }

    @SubscribeEvent
    public void injectIntoMainMenu(GuiScreenEvent.InitGuiEvent event) {
        if (!(event.getGui() instanceof GuiMainMenu)) {
            return;
        }

        GuiButton button = new GuiButton(BUTTON_REPLAY_VIEWER, event.getGui().width / 2 - 100,
                event.getGui().height / 4 + 10 + 3 * 24, I18n.format("replaymod.gui.replayviewer"));
        button.width = button.width / 2 - 2;
        event.getButtonList().add(button);
    }

    @SubscribeEvent
    public void onButton(GuiScreenEvent.ActionPerformedEvent.Pre event) {
        if(!event.getButton().enabled) return;

        if (event.getGui() instanceof GuiMainMenu) {
            if (event.getButton().id == BUTTON_REPLAY_VIEWER) {
                new GuiReplayViewer(mod).display();
            }
        }

        if (event.getGui() instanceof GuiIngameMenu && mod.getReplayHandler() != null) {
            if (event.getButton().id == BUTTON_EXIT_REPLAY) {
                event.getButton().enabled = false;
                mc.displayGuiScreen(new GuiMainMenu());
                try {
                    mod.getReplayHandler().endReplay();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
