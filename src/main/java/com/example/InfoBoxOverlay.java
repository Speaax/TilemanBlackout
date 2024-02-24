package com.example;

import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;

import javax.inject.Inject;
import javax.xml.crypto.Data;
import java.awt.*;

public class InfoBoxOverlay extends Overlay
{
    private final ExamplePlugin plugin;
    private final PanelComponent panelComponent = new PanelComponent();

    @Inject
    public InfoBoxOverlay(ExamplePlugin plugin)
    {
        this.plugin = plugin;
        setPosition(OverlayPosition.TOP_LEFT);
        panelComponent.setPreferredSize(new Dimension(150, 0));
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        // Update dynamic values
        int playerTiles = plugin.getPlayerTiles();
        int xpUntilNextPlayerTile = plugin.getXPUntilNextPlayerTile();
        int xpUntilNextRandomTile = plugin.getXPUntilNextRandomTile();
        int prestigePoints = plugin.getPrestigePoints();

        panelComponent.getChildren().clear();
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Player Tiles: ")
                .leftColor(Color.WHITE)
                .right(String.valueOf(playerTiles))
                .leftColor(Color.WHITE)
                .build());
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Next Player tile: ")
                .leftColor(Color.WHITE)
                .right(Integer.toString(xpUntilNextPlayerTile))
                .leftColor(Color.WHITE)
                .build());
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Next Random tile: ")
                .leftColor(Color.WHITE)
                .right(Integer.toString(xpUntilNextRandomTile))
                .leftColor(Color.WHITE)
                .build());
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Total tiles: ")
                .leftColor(Color.WHITE)
                .right(String.valueOf(prestigePoints))
                .leftColor(Color.WHITE)
                .build());

        return panelComponent.render(graphics);
    }
}
