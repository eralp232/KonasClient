## 0.6
#### Modifications/Improvements
- Improved YawLock (based on raion's)
- Added AutoBlock to KillAura
- Added Spacial rotation mode to CA
- Added Spacial trace mode to CA
- Added Place Confirm mode to CA
- Improved default settings for CA
- Improved constrict in CA
- Rewrote accuracy Vanilla/NCP, removed accuracy exact in CA
- Added efficiency option to CA - this is useful for when you're low on crystals
- Added limiter option to CA - this limits packets, useful for 2b and const
- Added delay to AntiWeakness
- Added packet prioritization system
- Moved Time module into Sky
- Added CopyInventory option to FakePlayer
- Added ExtraSafe option to Offhand
- Added CSGO option to ESP
- Added Offhand mode to Swing
- Added NewChunks
- Added AutoDisable to Step
- Added AutoDisable to LongJump
- Added GhostFix option to FastUse
- Added Whitelist/Blacklist options to scaffold
- Added AllowEat to Speed for 2b2t
- Added "Hook" option to Velocity
- Added MultiAxis option to PacketFly (lets you fly up/down and horizontally at the same time while phasing)
- Added SnapEat to ViewModel
- Added Spacial option to KillAura

#### Fixes
- Fixed LiquidInteract
- Fixed sand phase not working on some servers
- Fixed Nametags outline being transparent
- Fixed AltManager taking forever to open/login
- Fixed Velocity NoPush not working in water
- Fixed KillAura not attacking certain mobs such as Slimes

#### New Modules
- AntiAFK
- CrystalBlocker
- TrapMiner (aka AutoCity)
- AntiHunger
- AntiChainPop
- AntiAim (aka SpinBot)
- AutoFish
- AntiLevitation

#### New Commands
- yaw
- macro
- fov
- grab
- hclip
- vclip
- teleport
- vanish
- config

#### Protocol
- Added ViaVersion (1.7-1.17, going lower than 1.7 compleatly breaks everything so we removed the option)
- Added protocol slider in Multiplayer GUI

This is very experimental, and some modules will crash you on certain versions. Use at your own risk. 0.7 will add lots of modules/options for 1.8.
