package com.aionemu.gameserver.network.aion.gmhandler;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.CubeExpandService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/** 
* @author Waii new adaptation made by Dezalmado 
*/

public final class CmdSetInventoryGrowth extends AbstractGMHandler {

    public CmdSetInventoryGrowth(Player admin, String params) {
        super(admin, params);
        run();
    }
    public void run() {
        Player playerToExpand = (target != null && target instanceof Player) ? (Player) target : admin;

        if (playerToExpand == null) {
            PacketSendUtility.sendMessage(admin, "Error: No valid player target found for expansion.");
            return;
        }
        CubeExpandService.expand(playerToExpand, true);
        PacketSendUtility.sendMessage(admin, "9 cube slots successfully added");
    }
}