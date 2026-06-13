package com.playerinspector.client;

import com.playerinspector.client.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class PlayerInspectorHud {

    private float fadeAlpha = 0f;
    private PlayerEntity targetPlayer = null;
    private long lastSeenTime = 0;

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    public void render(DrawContext context, net.minecraft.client.render.RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        if (client.currentScreen != null) return;

        PlayerEntity hovered = getHoveredPlayer(client);
        float delta = tickCounter.getLastFrameDuration();
        long now = System.currentTimeMillis();

        if (hovered != null) {
            targetPlayer = hovered;
            lastSeenTime = now;
            fadeAlpha = Math.min(1f, fadeAlpha + delta * 0.15f);
        } else {
            long holdMs = ModConfig.get().holdTimeMs;
            long elapsed = now - lastSeenTime;
            if (elapsed >= holdMs) {
                fadeAlpha = Math.max(0f, fadeAlpha - delta * 0.10f);
                if (fadeAlpha <= 0f) targetPlayer = null;
            }
        }

        if (targetPlayer == null || fadeAlpha <= 0f) return;
        renderInspectorPanel(context, client, targetPlayer, fadeAlpha);
    }

    private PlayerEntity getHoveredPlayer(MinecraftClient client) {
        if (client.crosshairTarget == null) return null;
        if (client.crosshairTarget.getType() != HitResult.Type.ENTITY) return null;
        EntityHitResult hit = (EntityHitResult) client.crosshairTarget;
        Entity entity = hit.getEntity();
        if (entity instanceof PlayerEntity p && p != client.player) return p;
        return null;
    }

    private void renderInspectorPanel(DrawContext context, MinecraftClient client, PlayerEntity player, float alpha) {
        int screenW = client.getWindow().getScaledWidth();
        int screenH = client.getWindow().getScaledHeight();

        int panelW = 220;
        int panelH = 115;

        ModConfig cfg = ModConfig.get();
        int panelX = getPanelX(cfg, screenW, panelW);
        int panelY = getPanelY(cfg, screenH, panelH);

        int a = (int)(alpha * 255);

        drawRoundedRect(context, panelX, panelY, panelW, panelH, withAlpha(0xFF0A0A0F, a));
        drawRoundedRect(context, panelX + 1, panelY + 1, panelW - 2, panelH - 2, withAlpha(0xFF12121C, a));
        drawRoundedRect(context, panelX + 1, panelY + 1, panelW - 2, 2, withAlpha(0xFF5B6BE8, a));

        TextRenderer font = client.textRenderer;
        String name = player.getName().getString();
        context.drawText(font, name, panelX + 62, panelY + 8, withAlpha(0xFFE8E8FF, a), false);

        renderPlayerHead(context, player, panelX + 8, panelY + 14, 44, alpha);
        renderArmorSection(context, client, player, panelX + 62, panelY + 22, a);
        renderHpBar(context, font, player, panelX + 62, panelY + 72, panelW - 74, a);
    }

    private int getPanelX(ModConfig cfg, int screenW, int panelW) {
        int x = switch (cfg.position) {
            case TOP_LEFT, CENTER_LEFT, BOTTOM_LEFT -> 12;
            case TOP_RIGHT, CENTER_RIGHT, BOTTOM_RIGHT -> screenW - panelW - 12;
        };
        return x + cfg.offsetX;
    }

    private int getPanelY(ModConfig cfg, int screenH, int panelH) {
        int y = switch (cfg.position) {
            case TOP_LEFT, TOP_RIGHT -> 12;
            case CENTER_LEFT, CENTER_RIGHT -> screenH / 2 - panelH / 2;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> screenH - panelH - 12;
        };
        return y + cfg.offsetY;
    }

    private void renderArmorSection(DrawContext context, MinecraftClient client, PlayerEntity player, int x, int y, int a) {
        ModConfig cfg = ModConfig.get();
        int slotSize = 18;
        int gap = 4;

        for (int i = 0; i < ARMOR_SLOTS.length; i++) {
            EquipmentSlot slot = ARMOR_SLOTS[i];
            ItemStack stack = player.getEquippedStack(slot);
            int slotX = x + i * (slotSize + gap + 10);

            if (!stack.isEmpty()) {
                context.drawItem(stack, slotX, y);
                float durPct = getDurabilityPercent(stack);
                int barColor = getDurabilityColor(durPct, a);

                if (cfg.armorDisplay == ModConfig.ArmorDisplay.BAR || cfg.armorDisplay == ModConfig.ArmorDisplay.BOTH) {
                    int barW = slotSize + 10;
                    int barY = y + slotSize + 2;
                    drawRect(context, slotX, barY, barW, 3, withAlpha(0xFF1E1E2E, a));
                    drawRect(context, slotX, barY, (int)(barW * durPct), 3, barColor);
                }
                if (cfg.armorDisplay == ModConfig.ArmorDisplay.PERCENT || cfg.armorDisplay == ModConfig.ArmorDisplay.BOTH) {
                    String pctStr = (int)(durPct * 100) + "%";
                    context.drawText(client.textRenderer, pctStr, slotX, y + slotSize + 7, barColor, false);
                }
            } else {
                drawRect(context, slotX, y, slotSize, slotSize, withAlpha(0xFF1E1E2E, a));
                context.drawText(client.textRenderer, "—", slotX + 4, y + 5, withAlpha(0xFF333355, a), false);
            }
        }
    }

    private void renderHpBar(DrawContext context, TextRenderer font, PlayerEntity player,
                              int x, int y, int barW, int a) {
        float maxHp = player.getMaxHealth();
        float curHp = player.getHealth();
        float pct = Math.max(0f, Math.min(1f, curHp / maxHp));
        ModConfig cfg = ModConfig.get();

        if (cfg.showHpBar) {
            int barH = 10;
            drawRect(context, x, y, barW, barH, withAlpha(0xFF1A1A28, a));
            drawRect(context, x, y, (int)(barW * pct), barH, interpolateHpColor(pct, a));
            drawRect(context, x, y, (int)(barW * pct), 2, withAlpha(0x22FFFFFF, a));
        }

        if (cfg.showHpNumber) {
            String hpText = (int)curHp + " / " + (int)maxHp + " HP";
            int textY = cfg.showHpBar ? y + 13 : y;
            context.drawText(font, hpText, x, textY, withAlpha(0xFFDDDDFF, a), false);
        }
    }

    private void renderPlayerHead(DrawContext context, PlayerEntity player, int x, int y, int size, float alpha) {
        if (!(player instanceof AbstractClientPlayerEntity clientPlayer)) return;
        var texture = clientPlayer.getSkinTextures().texture();
        int a = (int)(alpha * 255);

        drawRect(context, x - 2, y - 2, size + 4, size + 4, withAlpha(0xFF0A0A14, a));
        drawRect(context, x - 2, y - 2, size + 4, 1, withAlpha(0xFF5B6BE8, a));
        drawRect(context, x - 2, y + size + 2, size + 4, 1, withAlpha(0xFF5B6BE8, a));
        drawRect(context, x - 2, y - 2, 1, size + 4, withAlpha(0xFF5B6BE8, a));
        drawRect(context, x + size + 2, y - 2, 1, size + 4, withAlpha(0xFF5B6BE8, a));

        context.drawTexture(texture, x, y, size, size, 8, 8, 8, 8, 64, 64);
        context.drawTexture(texture, x, y, size, size, 40, 8, 8, 8, 64, 64);
    }

    private float getDurabilityPercent(ItemStack stack) {
        if (!stack.isDamageable()) return 1f;
        int max = stack.getMaxDamage();
        if (max == 0) return 1f;
        return 1f - ((float) stack.getDamage() / max);
    }

    private int getDurabilityColor(float pct, int a) {
        if (pct > 0.75f) return withAlpha(0xFF3DDB84, a);
        if (pct > 0.50f) return withAlpha(0xFFFFD23F, a);
        if (pct > 0.25f) return withAlpha(0xFFFF8C42, a);
        if (pct > 0.05f) return withAlpha(0xFFE63946, a);
        return withAlpha(0xFF2D2D2D, a);
    }

    private int interpolateHpColor(float pct, int a) {
        int r, g, b;
        if (pct > 0.5f) {
            float t = (pct - 0.5f) * 2f;
            r = (int)(0xFF * (1f - t) + 0x3D * t);
            g = (int)(0xD2 * (1f - t) + 0xDB * t);
            b = (int)(0x3F * (1f - t) + 0x84 * t);
        } else {
            float t = pct * 2f;
            r = (int)(0xE6 * (1f - t) + 0xFF * t);
            g = (int)(0x39 * (1f - t) + 0xD2 * t);
            b = (int)(0x46 * (1f - t) + 0x3F * t);
        }
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private int withAlpha(int argb, int a) {
        return (a << 24) | (argb & 0x00FFFFFF);
    }

    private void drawRect(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + h, color);
    }

    private void drawRoundedRect(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x + 2, y, x + w - 2, y + h, color);
        ctx.fill(x, y + 2, x + 2, y + h - 2, color);
        ctx.fill(x + w - 2, y + 2, x + w, y + h - 2, color);
    }
}
