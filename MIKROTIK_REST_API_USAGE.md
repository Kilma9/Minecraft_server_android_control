# MikroTik REST API Usage Guide

## Prerequisites

On your MikroTik router (RouterOS v7.1+), enable the `www-ssl` service:

```
/ip service set www-ssl disabled=no port=443
```

Or for HTTP (not recommended for production):
```
/ip service set www disabled=no port=80
```

## App Configuration

1. Enter your router's IP address (e.g., `192.168.88.1`)
2. Enter username (default: `admin`)
3. Enter password
4. Click **Connect**

The app uses **HTTPS by default** and trusts self-signed certificates.

## REST API Endpoints

### List All Interfaces
```
GET /interface
```
Returns JSON array of all network interfaces with their status and configuration.

### Get Specific Interface
```
GET /interface/ether1
```
Replace `ether1` with the actual interface name.

### Enable Interface
To enable an interface, you need to:
1. First, get the interface ID using `GET /interface`
2. Find the `.id` value (e.g., `*1`)
3. Use: `PATCH /interface/*1` with params `{"disabled": "false"}`

### Disable Interface  
```
PATCH /interface/*1
Params: {"disabled": "true"}
```

### System Information
```
GET /system/resource
```
Returns system resources (CPU, memory, uptime, etc.)

### IP Addresses
```
GET /ip/address
```
Lists all configured IP addresses.

## Common API Paths

- `/interface` - Network interfaces
- `/ip/address` - IP addresses
- `/ip/route` - Routing table
- `/ip/firewall/filter` - Firewall rules
- `/system/resource` - System info
- `/system/identity` - Router identity
- `/interface/wireless` - Wireless interfaces
- `/ip/dhcp-server` - DHCP server config

## HTTP Methods

- **GET** - Read data
- **PATCH** - Update existing record
- **PUT** - Create new record
- **DELETE** - Delete record
- **POST** - Execute commands

## Example Workflow

### 1. Check Router Status
```
GET /system/resource
```

### 2. List Interfaces
```
GET /interface
```
Response will include interface IDs like `".id":"*1"`.

### 3. Disable an Interface
```
PATCH /interface/*1
Params: {"disabled": "true"}
```

### 4. Enable Interface Back
```
PATCH /interface/*1
Params: {"disabled": "false"}
```

## Notes

- All JSON values are returned as strings (e.g., `"disabled":"false"`)
- Interface names can be used instead of IDs when applicable
- The app automatically adds `/rest` prefix to all paths
- HTTP Basic Authentication is used with your RouterOS credentials
- Self-signed certificates are automatically trusted

## Troubleshooting

**Connection Failed:**
- Verify `www-ssl` service is enabled: `/ip service print`
- Check firewall allows HTTPS: `/ip firewall filter print`
- Verify router IP is accessible from your phone

**Authentication Failed:**
- Double-check username and password
- Verify user has appropriate permissions: `/user print`

**HTTP 404:**
- Check the API path is correct (use `/rest/interface`, not `/interface`)
- The app automatically adds `/rest` prefix, so you only enter `/interface`

## Security Recommendations

1. Use HTTPS (www-ssl) instead of HTTP (www)
2. Create a dedicated API user with limited permissions
3. Use firewall to restrict API access to specific IPs
4. Use strong passwords
5. Regularly update RouterOS to latest version

## MikroTik Documentation

Official REST API docs: https://help.mikrotik.com/docs/display/ROS/REST+API
