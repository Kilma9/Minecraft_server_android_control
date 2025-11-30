# Minecraft Server Control App

Android application for controlling Minecraft servers via RCON protocol.

## ✅ Features

### 🎮 5-Tab Interface

1. **Servers Tab** - Connect to Minecraft servers via RCON
   - Save multiple server configurations
   - Quick connect with stored credentials
   - Real-time connection status
   - Built-in RCON setup guide

2. **Items Tab** - Give items to players
   - 30+ categorized Minecraft items
   - Auto-fetch active players from server
   - Favorite players for quick access
   - Instant item distribution via `/give` command

3. **Controls Tab** - Server management commands
   - Change player gamemode (Survival, Creative, Adventure, Spectator)
   - Teleport players between each other
   - Time control (Day/Night)
   - Weather control (Clear/Rain)

4. **Info Tab** - Server statistics
   - Online player count and list
   - World seed display
   - Difficulty setting
   - Real-time refresh

5. **Chat Tab** - Send messages to server
   - Broadcast messages appear as "[App] Your message"
   - Time-based message filtering (30min, 1h, 4h, 1 day)
   - Clean chat interface

## 🛠️ Technology Stack

- **Language**: Kotlin 100%
- **Architecture**: MVVM with ViewModels and LiveData
- **UI**: Material Design 3 with Bottom Navigation
- **Networking**: Custom RCON client implementing Source RCON Protocol
- **Data**: SharedPreferences with Gson for persistence
- **Async**: Kotlin Coroutines with Dispatchers
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)

## 🚀 Getting Started

### Prerequisites

1. Android device or emulator (Android 7.0+)
2. Minecraft Java Edition server with RCON enabled

### Enable RCON on Your Server

Edit `server.properties`:
```properties
enable-rcon=true
rcon.port=25575
rcon.password=YourSecurePassword
```

Restart your server.

### Using the App

1. Install the APK on your Android device
2. Open the app → **Servers** tab
3. Enter server IP, RCON port (25575), and password
4. Click **Connect**
5. Navigate to other tabs to control your server!

See **[RCON_SETUP.md](RCON_SETUP.md)** for detailed setup instructions.

## 📦 Build

```bash
./gradlew assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

## 🔧 Features in Detail

### RCON Client
- Custom implementation of Source RCON Protocol
- Secure authentication with password
- Bi-directional command/response communication
- Connection state management
- Error handling and reconnection

### Server Management
All fragments check connection status and handle:
- Player list fetching (`/list`)
- Item distribution (`/give`)
- Gamemode changes (`/gamemode`)
- Player teleportation (`/tp`)
- Time/Weather control
- Server info queries

### Data Persistence
- Saved servers with credentials
- Favorite players
- Last connected server
- User preferences

## ⚠️ Security Notes

**RCON protocol is NOT encrypted**
- Use on trusted networks only
- Consider VPN for remote access
- Use strong passwords
- Don't expose RCON port to public internet

## 📁 Project Structure

```
app/src/main/
├── java/com/kilma/minecraft/
│   ├── rcon/
│   │   └── RconManager.kt          # RCON client implementation
│   ├── ui/
│   │   ├── servers/                # Connection management
│   │   ├── items/                  # Item distribution
│   │   ├── controls/               # Server commands
│   │   ├── info/                   # Server statistics
│   │   └── chat/                   # Chat messaging
│   ├── data/                       # Data models & repository
│   └── MainActivity.kt
└── res/
    ├── layout/                     # XML layouts
    ├── navigation/                 # Navigation graph
    └── values/                     # Strings, themes, colors
```

## 🤝 Contributing

This is a personal project for controlling my Minecraft server. Feel free to fork and customize for your needs!

## 📄 License

This project is for personal/educational use.

## 🙏 Acknowledgments

- Minecraft RCON Protocol: Based on Source RCON Protocol specification
- Material Design 3 for UI components
- Android Jetpack libraries

---

**Enjoy controlling your Minecraft server from your phone! 🎮📱**
