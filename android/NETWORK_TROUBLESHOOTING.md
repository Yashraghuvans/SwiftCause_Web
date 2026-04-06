# Android Emulator Network Issues - Troubleshooting Guide

## Problem
Your Android emulator can't resolve DNS or reach external URLs like Cloud Functions.

## Solutions (Try in Order)

### 1. **Restart Emulator with DNS Fix**
Close the emulator and restart with custom DNS:

```bash
# Close current emulator
adb emu kill

# Start emulator with Google DNS
emulator -avd <your_avd_name> -dns-server 8.8.8.8,8.8.4.4
```

### 2. **Check Emulator Internet in Settings**
1. Open **Settings** on emulator
2. Go to **Network & Internet** → **Wi-Fi**
3. Make sure Wi-Fi is **ON** and connected
4. Long press the network → **Modify network**
5. Advanced options → Set DNS to `8.8.8.8`

### 3. **Test Internet in Emulator Browser**
1. Open Chrome in the emulator
2. Try visiting `https://www.google.com`
3. If it doesn't work, internet is not configured

### 4. **Reset ADB and Network**
```bash
# Reset ADB
adb kill-server
adb start-server

# Check connected devices
adb devices
```

### 5. **Use 10.0.2.2 for Local Development (If Testing Locally)**
If you're running Cloud Functions locally, use:
- `10.0.2.2` instead of `localhost` or `127.0.0.1`
- Example: `http://10.0.2.2:5001/swiftcause-app/us-central1/kioskLogin`

### 6. **Check Emulator Network Settings**
In Android Studio:
1. Go to **AVD Manager**
2. Click **Edit** (pencil icon) on your emulator
3. Click **Show Advanced Settings**
4. Under **Network**, ensure it's set to **NAT** (not Internal only)

### 7. **Try Different Emulator API Level**
- API 29-31 have better network stability
- Create a new AVD with API 30 if current one fails

### 8. **Cold Boot Emulator**
```bash
# In Android Studio AVD Manager
# Click dropdown next to Play button
# Select "Cold Boot Now"
```

## Quick Test Commands

```bash
# Check if device has internet
adb shell ping -c 3 8.8.8.8

# Check if DNS works
adb shell ping -c 3 google.com

# Test HTTPS
adb shell curl -I https://google.com

# Check network interfaces
adb shell ip addr show
```

## Verify in Logcat

```bash
# Watch network logs
adb logcat | grep -i "network\|dns\|internet"
```

## If Nothing Works: Use Physical Device

1. Enable **USB Debugging** on phone:
   - Settings → About Phone → Tap Build Number 7 times
   - Settings → Developer Options → USB Debugging ON

2. Connect via USB and run:
   ```bash
   adb devices
   ./gradlew installDebug
   ```

## Common Emulator Network Issues

| Issue | Solution |
|-------|----------|
| DNS not working | Restart with `-dns-server 8.8.8.8` |
| No internet at all | Check AVD network mode (should be NAT) |
| HTTPS fails | Update emulator system image |
| Slow/timeout | Increase timeout in code (already 30s) |
| Works on phone, not emulator | Emulator-specific issue, use phone |

## Already Fixed in Code

✅ **30 second timeouts** in RetrofitClient  
✅ **HTTP logging** enabled for debugging  
✅ **Internet permissions** in AndroidManifest.xml  

## Next Steps

1. Try Solution #1 (restart with DNS)
2. If that fails, try #6 (check NAT mode)
3. If still failing, use a physical device
