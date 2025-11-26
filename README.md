# Minecraft Server Android Control

An Android app for remotely controlling Minecraft servers via RCON protocol.

## Features

### 🖥️ Servers Tab
- Connect to Minecraft servers via RCON
- Save multiple server configurations
- Manage connection credentials (IP, port, password)

### 🎁 Items Tab
- Give items to players with one tap
- 30+ popular Minecraft items including:
  - Resources (Diamond, Emerald, Netherite)
  - Tools & Weapons (Swords, Pickaxes, Trident)
  - Special Items (Elytra, Totem of Undying)
  - Enchanted Books (Mending, Sharpness V, Fortune III)
  - Blocks (TNT, Beacon, Shulker Box)

### 🎮 Controls Tab
- Teleport players to other players
- Change gamemode (Survival, Creative, Adventure, Spectator)
- Quick commands (Set time, Clear weather)

### 📊 Info Tab
- Server uptime monitoring
- Active player count
- TPS (Ticks Per Second) display
- Memory usage statistics
- Live player list

### 💬 Chat Tab
- Send messages to server
- Real-time chat with players

## Tech Stack

- **Language**: Kotlin
- **UI**: Material Design Components
- **Architecture**: MVVM with ViewModels
- **Navigation**: Android Navigation Component with Bottom Navigation
- **Protocol**: RCON for Minecraft server communication

## Project Structure

```
app/
├── src/main/
│   ├── java/com/kilma/raspberrypi/
│   │   ├── MainActivity.kt
│   │   ├── api/
│   │   │   └── MinecraftRconClient.kt
│   │   ├── data/
│   │   │   ├── MinecraftItem.kt
│   │   │   └── ItemsRepository.kt
│   │   └── ui/
│   │       ├── servers/
│   │       ├── items/
│   │       ├── controls/
│   │       ├── info/
│   │       ├── chat/
│   │       └── minecraft/
│   └── res/
│       ├── layout/
│       ├── navigation/
│       ├── menu/
│       └── drawable/       # Minecraft item icons
```

## Requirements

- Android 8.0 (API 26) or higher
- Minecraft server with RCON enabled

## Setup

1. Clone the repository
```bash
git clone https://github.com/Kilma9/Minecraft_server_android_control.git
```

2. Open in Android Studio

3. Build and run on your device
```bash
.\gradlew assembleDebug
```

## RCON Configuration

To connect to your Minecraft server, enable RCON in `server.properties`:

```properties
enable-rcon=true
rcon.port=25575
rcon.password=your_password
```

## Usage

1. Open the app
2. Go to **Servers** tab
3. Enter server details (IP, port, password)
4. Tap **Connect**
5. Navigate to other tabs to control your server

## Building

```powershell
# Build debug APK
.\gradlew assembleDebug

# Install on device
.\gradlew installDebug

# Build release
.\gradlew assembleRelease
```

## License

MIT License
