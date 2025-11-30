# RCON Integration Guide

## ✅ RCON Client Integrated!

The app now includes a full RCON client implementation that communicates with Minecraft servers.

## Features Implemented

### 1. **Servers Tab**
- Connect to Minecraft server via RCON
- Save multiple server configurations
- Real-time connection status indicator

### 2. **Items Tab**
- Fetches active players from server (`/list` command)
- Sends `/give` commands to players
- Real item distribution to connected server

### 3. **Controls Tab**
- Fetches active players for dropdowns
- Teleport players (`/tp` command)
- Change gamemode (`/gamemode` command)
- Time control (`/time set` command)
- Weather control (`/weather` command)

### 4. **Info Tab**
- Display online player count
- Show server seed
- Show difficulty setting
- Live player list refresh

### 5. **Chat Tab**
- Send broadcast messages (`/say` command)
- Messages appear in-game as "[App] Your message"

## Setting Up Your Minecraft Server for RCON

### Step 1: Enable RCON on Server

Edit `server.properties` in your Minecraft server folder:

```properties
enable-rcon=true
rcon.port=25575
rcon.password=YourSecurePassword123
```

### Step 2: Restart Minecraft Server

After saving `server.properties`, restart your Minecraft server.

### Step 3: Firewall Configuration

Make sure port `25575` (or your custom RCON port) is open:

**Windows Firewall:**
```powershell
New-NetFirewallRule -DisplayName "Minecraft RCON" -Direction Inbound -Protocol TCP -LocalPort 25575 -Action Allow
```

**Linux (ufw):**
```bash
sudo ufw allow 25575/tcp
```

## Using the App

### Connect to Server

1. Open the app
2. Go to **Servers** tab
3. Enter:
   - Server Name: "My Server"
   - Server IP: Your server IP (e.g., `192.168.1.100` or `play.example.com`)
   - RCON Port: `25575` (default)
   - RCON Password: Password from `server.properties`
4. Click **Connect**
5. Wait for "✓ Connected" status

### Give Items to Players

1. Go to **Items** tab
2. Select a player from dropdown (automatically fetched from server)
3. Browse categories and click on any item
4. Item is instantly given to the player on the server

### Control Server

1. Go to **Controls** tab
2. Use gamemode buttons to change player modes
3. Teleport players between each other
4. Change time and weather

### View Server Info

1. Go to **Info** tab
2. Click **Refresh** to update stats
3. See online players, seed, difficulty

### Send Chat Messages

1. Go to **Chat** tab
2. Type your message
3. Click send
4. Message appears in-game as "[App] Your message"

## Troubleshooting

### "Not connected to server"
- Verify RCON is enabled in `server.properties`
- Check server IP and port are correct
- Ensure firewall allows RCON port
- Verify password matches `server.properties`

### "Authentication failed: Invalid password"
- Double-check password in `server.properties`
- Restart server after changing RCON settings

### "Connection refused"
- Server might not be running
- RCON port might be blocked by firewall
- Check if you're using correct IP address

### Commands not working
- Some commands require operator permissions
- Make sure server is in the correct state (e.g., players online for teleport)

## Testing Locally

### Using Localhost

If running Minecraft server on your PC:
1. Use IP: `127.0.0.1` or `localhost`
2. Port: `25575`
3. Password: From your `server.properties`

### Android Emulator → PC Server

Android emulator special IP to access host PC:
- Use IP: `10.0.2.2`
- This maps to `127.0.0.1` on your host machine

## Technical Details

### RCON Protocol
- Implements Source RCON Protocol
- Little-endian byte order
- Packet structure: Size (4) + ID (4) + Type (4) + Body + Null terminators (2)
- Authentication via SERVERDATA_AUTH (type 3)
- Commands via SERVERDATA_EXECCOMMAND (type 2)

### Supported Commands
All standard Minecraft commands work through RCON:
- `/list` - Get online players
- `/give <player> <item> <amount>` - Give items
- `/gamemode <mode> <player>` - Change gamemode
- `/tp <from> <to>` - Teleport players
- `/time set <day|night>` - Change time
- `/weather <clear|rain>` - Change weather
- `/say <message>` - Broadcast message
- `/seed` - Get world seed
- `/difficulty` - Get difficulty
- And many more...

## Security Notes

⚠️ **Important Security Considerations:**

1. **Use Strong Passwords**: RCON password should be strong and unique
2. **Firewall Rules**: Only open RCON port to trusted networks
3. **Local Network**: Best used on local network, not public internet
4. **Encryption**: RCON protocol is NOT encrypted - consider VPN for remote access
5. **Rate Limiting**: Server has built-in rate limiting to prevent spam

## Next Steps

Consider adding:
- Connection persistence across app restarts
- Command history
- Batch operations
- Server performance monitoring (requires server-side plugin)
- Real-time chat monitoring (requires server-side plugin)

---

**Enjoy controlling your Minecraft server from your phone! 🎮📱**
