package com.playerinspector.client.screen;

import com.playerinspector.client.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class ConfigScreen extends Screen {

    private final Screen parent;
    private ModConfig cfg;
    private int holdTimeMs;

    public ConfigScreen(Screen parent) {
        super(Text.literal("Player Inspector — Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        cfg = ModConfig.get();
        holdTimeMs = cfg.holdTimeMs;

        int cx = this.width / 2;
        int startY = 50;
        int gap = 28;

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Position: " + cfg.position.name()),
                btn -> {
                    ModConfig.HudPosition[] vals = ModConfig.HudPosition.values();
                    cfg.position = vals[(cfg.position.ordinal() + 1) % vals.length];
                    btn.setMessage(Text.literal("Position: " + cfg.position.name()));
                }).dimensions(cx - 120, startY, 240, 20).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Armor: " + armorLabel(cfg.armorDisplay)),
                btn -> {
                    ModConfig.ArmorDisplay[] vals = ModConfig.ArmorDisplay.values();
                    cfg.armorDisplay = vals[(cfg.armorDisplay.ordinal() + 1) % vals.length];
                    btn.setMessage(Text.literal("Armor: " + armorLabel(cfg.armorDisplay)));
                }).dimensions(cx - 120, startY + gap, 240, 20).build());

        this.addDrawableChild(new SliderWidget(
                cx - 120, startY + gap * 2, 240, 20,
                Text.literal("Hold time: " + holdTimeMs + "ms"),
                (holdTimeMs - 1000.0) / 9000.0) {
            @Override
            protected void updateMessage() {
                int ms = 1000 + (int)(this.value * 9000);
                ConfigScreen.this.holdTimeMs = (ms / 100) * 100;
                setMessage(Text.literal("Hold time: " + ConfigScreen.this.holdTimeMs + "ms"));
            }
            @Override
            protected void applyValue() {
                int ms = 1000 + (int)(this.value * 9000);
                ConfigScreen.this.holdTimeMs = (ms / 100) * 100;
            }
        });

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("HP Bar: " + (cfg.showHpBar ? "ON" : "OFF")),
                btn -> {
                    cfg.showHpBar = !cfg.showHpBar;
                    btn.setMessage(Text.literal("HP Bar: " + (cfg.showHpBar ? "ON" : "OFF")));
                }).dimensions(cx - 120, startY + gap * 3, 115, 20).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("HP Number: " + (cfg.showHpNumber ? "ON" : "OFF")),
                btn -> {
                    cfg.showHpNumber = !cfg.showHpNumber;
                    btn.setMessage(Text.literal("HP Number: " + (cfg.showHpNumber ? "ON" : "OFF")));
                }).dimensions(cx + 5, startY + gap * 3, 115, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("− X"),
                btn -> { cfg.offsetX -= 5; clearAndInit(); })
                .dimensions(cx - 120, startY + gap * 4, 55, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("+ X"),
                btn -> { cfg.offsetX += 5; clearAndInit(); })
                .dimensions(cx - 60, startY + gap * 4, 55, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("− Y"),
                btn -> { cfg.offsetY -= 5; clearAndInit(); })
                .dimensions(cx + 5, startY + gap * 4, 55, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("+ Y"),
                btn -> { cfg.offsetY += 5; clearAndInit(); })
                .dimensions(cx + 65, startY + gap * 4, 55, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Save & Close"),
                btn -> {
                    cfg.holdTimeMs = holdTimeMs;
                    ModConfig.save();
                    this.client.setScreen(parent);
                }).dimensions(cx - 120, startY + gap * 6, 115, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"),
                btn -> this.client.setScreen(parent))
                .dimensions(cx + 5, startY + gap * 6, 115, 20).build());
    }

    private void clearAndInit() {
        this.clearChildren();
        this.init();
    }

    private String armorLabel(ModConfig.ArmorDisplay d) {
        return switch (d) {
            case BAR -> "Bar";
            case PERCENT -> "Percent";
            case BOTH -> "Bar + Percent";
        };
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("§bPlayer Inspector §7— Settings"),
                this.width / 2, 20, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("§7Offset X: " + ModConfig.get().offsetX + "  Y: " + ModConfig.get().offsetY),
                this.width / 2, 175, 0xAAAAAA);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
