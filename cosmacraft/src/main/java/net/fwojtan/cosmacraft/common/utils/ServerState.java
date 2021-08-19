package net.fwojtan.cosmacraft.common.utils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ServerState {

    public List<String> serverName;
    public List<String> status;
    public List<String> jobName;
    public List<String> jobDuration;
    public List<String> updateTime;
    public List<String> cpuUsage;
    public List<String> memUsage;

    public int ejected;
    public int ejectProgress;

    public float averageUsage = 0.0f;
    public ColorChoice colorChoice = ColorChoice.NONE;

    public boolean isComputeNode;

    public ServerState(String serverName, int ejected, boolean isComputeNode){

        this.isComputeNode = isComputeNode;
        this.serverName = new ArrayList<>();
        if (isComputeNode){
            String[] names = serverName.split("/");
            this.serverName.addAll(Arrays.asList(names));
        } else {
            this.serverName.add(serverName);
        }
        this.memUsage = new ArrayList<>();
        this.cpuUsage = new ArrayList<>();
        this.status = new ArrayList<>();
        this.jobName = new ArrayList<>();
        this.jobDuration = new ArrayList<>();
        this.updateTime = new ArrayList<>();
        this.ejected = ejected;
        this.ejectProgress = 0;


    }

    public void printStateToPlayer(PlayerEntity player, ServerType type){
        String message = "";
        if (!(type.getSerializedName().contains("gap"))) {
            if (isComputeNode && serverName.size() > 0) {
                message += "This chassis contains 4 compute nodes:\n";
                message += "NAME   STATUS  CPU USE  MEMORY USE  RUNTIME    JOB NAME \n";
                for (int i=0; i<serverName.size(); i++){
                    message += serverName.get(i) + "  "
                            + StringUtils.rightPad(status.get(i), 6, " ") + "    "
                            + StringUtils.rightPad(cpuUsage.get(i).substring(0, Math.min(5, cpuUsage.get(i).length())), 5, "0") + "%    "
                            + StringUtils.rightPad(memUsage.get(i).substring(0, Math.min(5, memUsage.get(i).length())), 5, "0") + "%         "
                            + StringUtils.rightPad(jobDuration.get(i), 12, " ") + "  "
                            + jobName.get(i) + "\n";
                }
                message += "\n Data collected at: "+updateTime.get(0)+"\n";

            } else {
                message += serverName.get(0)+"\n";
                message += type.getSerializedName()+"\n";
                // come back and add descriptions for each of these rather than the gibberish names
            }


            player.sendMessage(new TranslationTextComponent(message, new Object()), new UUID(16, 0));

        }


    }

    public String combineNodeNames(){
        String retval = "";
        for (String name : this.serverName){
            retval += name + "/";
        }
        StringBuffer sb = new StringBuffer(retval);
        sb.deleteCharAt(sb.length()-1);
        retval = sb.toString();
        return retval;
    }

    public void pickColorChoice(){
        for (String usage : cpuUsage){
            averageUsage += Float.parseFloat(usage);
        }
        averageUsage /= 4.0f;

        for (String state : status){
            if (state == "drain" || state =="maint"){colorChoice = ColorChoice.YELLOW;}
        }
        for (String state : status){
            if (state == "fail" || state == "down"){colorChoice = ColorChoice.RED;}
        }

        if (colorChoice == ColorChoice.NONE && averageUsage > 0.0f){
            if (averageUsage < 20.0f){
                colorChoice = ColorChoice.GREEN1;
            } else if (averageUsage < 40.0f){
                colorChoice = ColorChoice.GREEN2;
            } else if (averageUsage < 60.0f){
                colorChoice = ColorChoice.GREEN3;
            } else if (averageUsage < 80.0f){
                colorChoice = ColorChoice.GREEN4;
            } else {
                colorChoice = ColorChoice.GREEN5;
            }
        }
    }

}
