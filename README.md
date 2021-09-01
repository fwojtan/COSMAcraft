
# COSMAcraft

A Minecraft-based MSc project! Repository includes minecraft mod, python tools and other resources. 

![2021-09-01_09 25 53](https://user-images.githubusercontent.com/65790202/131690507-fb4b43ea-1420-4d34-aeb7-5e5e966b822e.png)

Demo video: https://www.youtube.com/watch?v=JGUwQR4u91o

## User Guide

See "COSMACRAFT USER GUIDE.pdf"

## Installation

### Windows & macOS

1. Install Minecraft 1.16.5 and Forge 36.2.0 using either CurseForge (https://download.curseforge.com) or installing Minecraft directly and running the Forge installer (https://files.minecraftforge.net/net/minecraftforge/forge/index_1.16.5.html). 
2. Run the game to create a blank profile.
3. Copy cosmacraft_v1.x.jar into the mods folder.
4. Copy the COSMAcraft World directory into the saves folder.
5. Copy the utils directory (containing the cosma_usage json and the cosma_config json) into the Minecraft game directory (the directory containing "saves" and "mods").
6. Relaunch the game and it should now load with COSMAcraft installed. 

### Linux

Either follow the above steps (without using CurseForge) or use the cosmacraft_installer program (which essentially just automates a bunch of these steps). 

## Admin Info

Ensure all players using the world are in 'adventure mode'. If creating a local multiplayer world this can be configured when the world is shared to LAN. If hosting a server this can be configured on the server. If running single player worlds simply change the player's gamemode by typing /gamemode adventure while in-game. 

Setting up the live state updates to COSMA requires an SSH port-forwarding connection on port 5432, connnecting to a login node where socket_daemon.py is running. data_script.py should also be running in the background at regular intervals, the easiest way to configure this is with a crontab job. 

If users get stuck or lost they can be teleported back to the tutorial area using /tp *player_name* -259 19 -133.

## Gallery

![2021-08-31_18 27 39](https://user-images.githubusercontent.com/65790202/131690984-20ed2457-1645-4666-ba95-df3623fbeb77.png)

![2021-09-01_10 23 48](https://user-images.githubusercontent.com/65790202/131691612-9d51c0f6-4bbb-46c7-8623-12c545934f0a.png)

![2021-09-01_12 15 47](https://user-images.githubusercontent.com/65790202/131691622-114ace1b-178e-455f-bef7-1ed8f568bc6f.png)

![2021-09-01_12 16 00](https://user-images.githubusercontent.com/65790202/131691628-a74e421a-c1c6-4225-9a76-246d3feb85e7.png)

![2021-08-31_18 27 39](https://user-images.githubusercontent.com/65790202/131691635-85dc971e-c17b-4965-9346-8666ce8c80cb.png)

![2021-09-01_09 26 22](https://user-images.githubusercontent.com/65790202/131691645-3050250b-3a67-4c06-9d70-7faac42cda5c.png)

![2021-09-01_09 27 43](https://user-images.githubusercontent.com/65790202/131691650-81006909-8990-4e87-bf30-cb7fe587b080.png)

![2021-09-01_09 28 59](https://user-images.githubusercontent.com/65790202/131691659-73fc05f8-594f-454b-9e5b-724f7bfb3c55.png)




