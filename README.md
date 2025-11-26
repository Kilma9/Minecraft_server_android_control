# Raspberry Pi Control Android App

Android application to control Raspberry Pi 5 running Proxmox hypervisor, MikroTik router, and Immich photo backup.

## Features

### 🖥️ Hypervisor Control
> ⚠️ **Not yet implemented** - UI template ready, backend integration pending

- Performance dashboard (CPU, memory, disk usage)
- VM control buttons (Start, Shutdown, Force Stop)
- Designed for Proxmox hypervisor integration

### 🐳 Docker Management
> ⚠️ **Not yet implemented** - UI template ready

- Docker container controls
- Container status monitoring
- Quick container actions

### ⛏️ Minecraft Server Control
> ✅ **Fully functional** - RCON protocol integration

**Features:**
- Remote server control via RCON protocol (port 25575)
- Player gamemode management (Creative/Survival)
- Item giving system with quantity control
- 12 quick item buttons with authentic Minecraft textures:
  - Tools: Diamond Sword, Iron Pickaxe, Elytra, Enchanted Book, Totem of Undying
  - Consumables: Golden Apple (16x), Ender Pearl (16x)
  - Resources: Diamond (64x), Emerald (64x), TNT (64x), Iron Ingot (64x), Obsidian (64x)
- Custom item command with any Minecraft item ID
- Real-time command output display

**Setup:**
1. Enable RCON in server.properties:
   ```properties
   enable-rcon=true
   rcon.port=25575
   rcon.password=your_password
   ```
2. In app: Enter server IP (e.g., 10.0.0.122), port (25575), password
3. Click Connect
4. Enter player name and use gamemode or item buttons

**Technical Implementation:**
- Binary RCON protocol with authentication
- Little-endian packet construction using ByteBuffer
- Socket-based TCP communication
- Packet types: SERVERDATA_AUTH (3), SERVERDATA_EXECCOMMAND (2)

### 🌐 MikroTik Router Control
> ✅ **Fully functional** - HTTP REST API

**Features:**
- Real-time router control via REST API (RouterOS 7.1+)
- 10 quick command buttons:
  - System Info & Identity
  - Network Interfaces
  - IP Addresses & Routes
  - Firewall Rules
  - DHCP Leases
  - Wireless Info & Clients
  - System Logs
- Custom API command execution
- JSON response formatting for readability

**Setup:**
1. Enable MikroTik www service: `/ip service set www port=1080`
2. In app: Enter IP (e.g., 10.0.0.1), port (1080), credentials
3. Keep HTTPS toggle OFF (HTTP mode)
4. Click Connect

**Limitations:**
- HTTP only (cleartext traffic enabled via network security config)
- HTTPS fails with self-signed certificates

### 📷 Immich Photo Backup
> ⚠️ **Partial implementation** - Manual upload works, auto backup pending

**Features:**
- Connect to self-hosted Immich server
- Manual photo selection and upload
- Upload progress tracking
- Multiple photo batch uploads

**Not yet implemented:**
- Auto backup functionality
- Duplicate detection
- Background sync
- Upload history tracking

**Setup:**
1. Install Immich on Raspberry Pi (see `IMMICH_SETUP.md`)
2. In app: Enter server URL (http://raspberrypi.local:2283) and API key
3. Click Connect
4. Select photos and upload manually

## Tech Stack

- **Language**: Kotlin 1.9.0
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Build System**: Gradle 8.9 + AGP 8.1.0
- **Architecture**: MVVM with LiveData
- **UI**: Material Design 3 with Bottom Navigation (5 tabs with colored icons)
- **Networking**: OkHttp 4.11.0 + Socket I/O for RCON
- **Async**: Kotlin Coroutines 1.7.3
- **View Binding**: Enabled

## Project Structure

```
app/
├── src/main/
│   ├── java/com/kilma/raspberrypi/
│   │   ├── MainActivity.kt
│   │   ├── api/
│   │   │   ├── MikrotikApiClient.kt
│   │   │   ├── ImmichApiClient.kt
│   │   │   └── MinecraftRconClient.kt
│   │   └── ui/
│   │       ├── hypervisor/
│   │       ├── docker/
│   │       ├── minecraft/
│   │       ├── mikrotik/
│   │       └── photos/
│   └── res/
│       ├── layout/
│       ├── navigation/
│       ├── menu/
│       ├── mipmap-*/          # Custom app icon (all densities)
│       └── xml/
│           └── network_security_config.xml
```

## Building

```powershell
# Build debug APK
.\gradlew assembleDebug

# Install on device
.\gradlew installDebug

# Run tests
.\gradlew test
```

## Icon Generation

Custom app icon is generated from `ICON_android.jpg` using the included PowerShell script:

```powershell
.\generate_icon.ps1
```

The script:
- Crops to inner dark circle only (excludes outer cyan ring)
- Generates all mipmap densities (mdpi to xxxhdpi)
- Creates circular masked versions for both square and round icons
- Outputs to `app/src/main/res/mipmap-*/`

## Configuration

### Network Security (HTTP Support)
`res/xml/network_security_config.xml`:
- Cleartext traffic enabled globally
- Trusts system and user certificates for self-signed HTTPS

### Permissions
- `INTERNET` - Network access
- `ACCESS_NETWORK_STATE` - WiFi detection
- `READ_MEDIA_IMAGES` - Photo access (Android 13+)
- `READ_EXTERNAL_STORAGE` - Photo access (Android 12-)

## Changelog

### November 26, 2025
**Minecraft Server Control & Colored Navigation Icons:**

**⛏️ Minecraft RCON Integration (Fully Functional):**
- Complete binary RCON protocol implementation with authentication
- Server connection UI with IP (10.0.0.122), port (25575), and password fields
- Player gamemode management (Creative/Survival mode switching)
- Item giving system with custom item name and quantity inputs
- **12 Quick Item Buttons** with authentic Minecraft texture icons:
  - **Tools (1x):** Diamond Sword, Iron Pickaxe, Elytra, Enchanted Book, Totem of Undying
  - **Consumables (16x):** Golden Apple, Ender Pearl
  - **Resources (64x):** Diamond, Emerald, TNT, Iron Ingot, Obsidian
- Custom item command field for any Minecraft item ID
- Real-time command output display with monospace font
- Full socket-based TCP communication with Minecraft servers

**🎨 Colored Navigation Icons:**
- Added branded PNG icons for all 5 navigation tabs
- Disabled Material Design icon tinting (programmatically via `itemIconTintList = null`)
- Icons show original colors: Proxmox orange, Docker blue, Minecraft green, MikroTik blue, Immich purple
- Fixed Android resource naming violations (removed numbered PNG files)

**🔧 Technical Implementation:**
- **MinecraftRconClient.kt**: Socket-based binary RCON protocol client
  - `sendPacket()`: Little-endian ByteBuffer packet construction
  - `receivePacket()`: Binary packet parsing with size headers
  - Authentication: SERVERDATA_AUTH (type 3)
  - Command execution: SERVERDATA_EXECCOMMAND (type 2)
- **MinecraftViewModel.kt**: Business logic for player and item management
- **MinecraftFragment.kt**: UI with connection handling and 12 quick item buttons
- **MainActivity.kt**: Programmatic icon tint disabling for BottomNavigationView
- **fragment_minecraft.xml**: Complete Minecraft control UI layout

**Icon Assets Added:**
- 12 Minecraft item textures: `mc_diamond_sword.png`, `mc_iron_pickaxe.png`, `mc_golden_apple.png`, `mc_ender_pearl.png`, `mc_diamond.png`, `mc_emerald.png`, `mc_tnt.png`, `mc_elytra.png`, `mc_enchanted_book.png`, `mc_totem_of_undying.png`, `mc_iron_ingot.png`, `mc_obsidian.png`
- Navigation logos: `proxmox.png`, `docker.png`, `minecraft_logo.png`, `mikrotik.png`, `immich.png`

**Bug Fixes:**
- Fixed XML syntax errors from malformed PowerShell regex replacement
- Cleaned up 200+ invalid Android resource files (PNG files starting with numbers)
- Fixed build failures from resource naming violations

### November 22, 2025
**Major Updates:**
- 🐛 **Fixed app crash**: Reduced navigation tabs from 6 to 5 (BottomNavigationView limit)
  - Removed Home Automation tab to stay within Material Design constraints
- 📷 **Added Immich photo backup integration**:
  - New Photos tab with manual upload functionality
  - Server connection testing
  - Multi-photo selection and batch upload
  - Upload progress tracking
  - Note: Auto backup toggle present but not functional yet
- 🎨 **Icon improvements**:
  - Regenerated app icon with proper circular cropping
  - Removed white corners and outer cyan ring
  - Shows only inner dark circle content
  - Transparent background for clean appearance
- 📝 **UI improvements**:
  - Added "not yet implemented" notices to Hypervisor tab
  - Added "auto backup not implemented" notice to Photos tab
  - Disabled non-functional auto backup switch
- 📚 **Documentation**:
  - Created comprehensive `IMMICH_SETUP.md` guide
  - Updated README with current feature status
  - Added changelog section

**Technical Details:**
- Created `ImmichApiClient.kt` for REST API communication
- Created `PhotosFragment.kt` and `PhotosViewModel.kt`
- Added photo picker using ActivityResultContracts
- Configured multipart form data uploads
- Added runtime permissions for photo access
- Updated network security config for Immich HTTP

### Previous Development
- ✅ MikroTik REST API integration with 10 quick commands
- ✅ HTTP cleartext traffic configuration
- ✅ JSON response formatting
- ✅ Custom app icon with PowerShell generation script
- ✅ Material Design 3 UI with bottom navigation
- ✅ MVVM architecture implementation

## Known Issues

1. **BottomNavigationView Limit**: Maximum 5 tabs supported
   - Removed Home Automation tab to accommodate Photos tab
   - Consider NavigationRail or Drawer for 6+ tabs in future
2. **Photo Auto Backup**: Not implemented
   - Manual upload only
   - No duplicate detection
   - No background sync
3. **MikroTik HTTPS**: Self-signed certificate errors
   - HTTP works fine on trusted networks
4. **Hypervisor/Docker**: UI templates only
   - Backend integration pending Raspberry Pi deployment

## Future Enhancements

**High Priority:**
- [ ] Implement photo auto backup with WorkManager
- [ ] Add photo duplicate detection (checksum-based)
- [ ] Background sync for photos (WiFi-only constraint)
- [ ] MikroTik HTTPS certificate pinning

**Medium Priority:**
- [ ] Proxmox API integration for hypervisor control
- [ ] Docker API integration
- [ ] Real-time performance graphs
- [ ] Push notifications for system alerts

**Low Priority:**
- [ ] NavigationRail for 6+ tabs support
- [ ] Dark mode theme optimization
- [ ] Custom Minecraft command builder
- [ ] SSH terminal integration

## Contributing

This is a personal project for controlling a Raspberry Pi 5 homelab. Features are implemented as needed for personal use.

## License

Private project - All rights reserved

---

**Project Status**: Active Development  
**Last Updated**: November 26, 2025  
**Platform**: Android 7.0+ (API 24+)
