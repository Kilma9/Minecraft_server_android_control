# Google Play Store Release Guide

## Step 1: Generate Signing Key

Open terminal in your project directory and run:

```bash
keytool -genkey -v -keystore kilma-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias kilma-key
```

**Save these credentials securely - you'll need them forever!**
- Keystore password
- Key alias: `kilma-key`
- Key password

## Step 2: Configure Signing in Gradle

Add to `app/build.gradle` (inside `android` block):

```gradle
android {
    ...
    
    signingConfigs {
        release {
            storeFile file("../kilma-release-key.jks")
            storePassword "YOUR_KEYSTORE_PASSWORD"
            keyAlias "kilma-key"
            keyPassword "YOUR_KEY_PASSWORD"
        }
    }
    
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

## Step 3: Build Release AAB (Android App Bundle)

```bash
.\gradlew bundleRelease
```

Output: `app\build\outputs\bundle\release\app-release.aab`

## Step 4: Prepare Play Store Assets

### Required:
- **App Name**: "Kilma Raspberry Pi Control" (or similar, under 30 chars)
- **Short Description**: Max 80 characters
- **Full Description**: Max 4000 characters
- **Screenshots**: Minimum 2 (phone + tablet recommended)
- **Feature Graphic**: 1024x500px PNG/JPG
- **App Icon**: 512x512px PNG (32-bit with alpha)
- **Privacy Policy**: Required for apps with network permissions

### Screenshots Needed:
- Phone: 16:9 or 9:16 aspect ratio (e.g., 1080x1920)
- 7-inch tablet: 1024x600 minimum
- 10-inch tablet: 1920x1200 minimum

## Step 5: Privacy Policy

Since your app connects to external devices (Raspberry Pi, MikroTik), you need a privacy policy.

**Sample Privacy Policy** (host on GitHub Pages or your website):

```markdown
# Privacy Policy for Kilma Raspberry Pi Control

Last updated: November 22, 2025

## Data Collection
This app does NOT collect, store, or transmit any personal data to our servers.

## Network Connections
The app connects directly to:
- Your Raspberry Pi (Proxmox hypervisor)
- Your MikroTik router
- Your Minecraft server

All connections are made locally within your network. No data is sent to third parties.

## Credentials
Login credentials (IP addresses, usernames, passwords) are stored locally on your device only.

## Permissions
- INTERNET: Required to connect to your local devices
- ACCESS_NETWORK_STATE: Check network connectivity

## Contact
Email: your.email@example.com
```

## Step 6: Create Play Store Listing

1. Go to https://play.google.com/console
2. Create new app
3. Fill out:
   - App details (name, description, category)
   - Store listing (screenshots, graphics)
   - Content rating (complete questionnaire)
   - Pricing (free or paid)
   - Distribution (countries)
   - App content (privacy policy, ads, etc.)

## Step 7: Upload AAB

1. Go to Production → Releases
2. Create new release
3. Upload `app-release.aab`
4. Add release notes
5. Review and rollout

## Step 8: Review Process

- Google reviews your app (usually 1-7 days)
- You'll receive email notification
- Fix any issues if rejected
- Once approved, app goes live!

## Important Notes

### App Requirements:
- ✅ Minimum SDK 24 (Android 7.0)
- ✅ Target SDK 34 (Android 14) - **Update before Nov 2025!**
- ✅ 64-bit support
- ✅ Privacy policy
- ✅ Appropriate content rating

### Common Rejection Reasons:
- Missing privacy policy
- Broken functionality
- Misleading screenshots
- Inappropriate content
- Security vulnerabilities

### After Publishing:
- Monitor reviews and ratings
- Respond to user feedback
- Release updates regularly
- Monitor crash reports in Play Console

## Cost Summary

- Google Play Developer Account: **$25 one-time**
- App publishing: **Free**
- Updates: **Free forever**

## Alternative: Internal Testing

Before public release, use internal testing:
1. Create internal testing track
2. Add testers by email
3. Share test link
4. Collect feedback
5. Fix bugs
6. Then release to production

## Need Help?

- Play Console Help: https://support.google.com/googleplay/android-developer
- Android Developer Docs: https://developer.android.com/distribute
