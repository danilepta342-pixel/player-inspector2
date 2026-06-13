package com.playerinspector;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerInspector implements ModInitializer {
    public static final String MOD_ID = "playerinspector";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Player Inspector loaded!");
    }
}
