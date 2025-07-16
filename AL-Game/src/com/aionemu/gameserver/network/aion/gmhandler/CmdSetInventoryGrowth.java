package com.aionemu.gameserver.network.aion.gmhandler;

import com.aionemu.gameserver.configs.administration.AdminConfig; // Importe AdminConfig para verificar o nível de acesso
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.CubeExpandService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World; // Para encontrar jogadores por nome, se necessário
import com.aionemu.gameserver.utils.Util; // Para converter nome do jogador, se necessário

/**
 * @author ginho1 adaptado por (Dezalmado)
 * Adaptado para a estrutura CmdGMHandler
 */
public class CmdSetInventoryGrowth extends AbstractGMHandler {

    public CmdSetInventoryGrowth(Player admin, String params) {
        super(admin, params);
        // O método run() será chamado automaticamente pelo construtor de AbstractGMHandler se a lógica for simples.
        // Se precisar de mais controle, você pode remover o run() do construtor super e chamá-lo aqui.
        run(); // Chama a lógica principal do comando
    }


    public void run() {
        // Verifica o nível de acesso do GM, usando AdminConfig.GM_PANEL como referência,
        // assim como no seu CM_GM_COMMAND_SEND.java.
        // Você pode criar uma constante específica em AdminConfig para SETINVENTORYGROWTH
        // se quiser um nível de acesso diferente.
        if (admin.getAccessLevel() < AdminConfig.GM_PANEL) {
            PacketSendUtility.sendMessage(admin, "You don't have enough access level to use this command.");
            return;
        }

        Player targetPlayer = admin; // Define o GM como alvo padrão

        // O comando original não aceita parâmetros além do comando em si,
        // mas pode ser útil ter a opção de expandir o inventário de outro jogador.
        // Se 'params' for vazio, o alvo é o próprio GM ou o alvo selecionado.
        // Se 'params' contiver um nome, tente encontrar esse jogador.
        if (!params.isEmpty()) {
            Player namedTarget = World.getInstance().findPlayer(Util.convertName(params));
            if (namedTarget != null) {
                targetPlayer = namedTarget;
            } else {
                // Se um parâmetro foi fornecido mas não é um nome de jogador válido
                // e o GM tem um alvo selecionado, use o alvo selecionado.
                // Caso contrário, informe que o jogador não foi encontrado.
                VisibleObject currentTarget = admin.getTarget();
                if (currentTarget instanceof Player) {
                    targetPlayer = (Player) currentTarget;
                    // Se o parâmetro for algo diferente de um nome de jogador, ignore e use o alvo
                    // ou mostre uma mensagem de uso incorreto.
                    // Por enquanto, vamos ignorar o parâmetro inválido se houver um alvo.
                } else {
                    PacketSendUtility.sendMessage(admin, "Player '" + params + "' not found or invalid parameter.");
                    return;
                }
            }
        } else {
            // Se nenhum parâmetro foi fornecido, verifica se há um alvo selecionado.
            VisibleObject currentTarget = admin.getTarget();
            if (currentTarget instanceof Player) {
                targetPlayer = (Player) currentTarget;
            } else {
                PacketSendUtility.sendMessage(admin, "No target selected, and no player name specified. Usage: //setinventorygrowth [player_name]");
                return;
            }
        }


        // A lógica central do comando, copiada do seu arquivo Setinventorygrowth.java
        if (CubeExpandService.canExpand(targetPlayer)) {
            CubeExpandService.expand(targetPlayer, true);
            PacketSendUtility.sendMessage(admin, "9 cube slots successfully added to player " + targetPlayer.getName() + "!");
            if (!targetPlayer.equals(admin)) { // Mensagem apenas para o alvo se não for o próprio GM
                PacketSendUtility.sendMessage(targetPlayer, "Admin " + admin.getName() + " gave you a cube expansion!");
            }
        } else {
            PacketSendUtility
                .sendMessage(admin, "Cube expansion cannot be added to " + targetPlayer.getName() + "!\nReason: player cube already fully expanded.");
        }
    }

    // O método info() do ConsoleCommand não é diretamente aplicável aqui,
    // pois a CM_GM_COMMAND_SEND não tem um método info para cada comando individual.
    // A mensagem de sintaxe pode ser incluída na lógica run() em caso de uso incorreto.
}