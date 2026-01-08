# 🔧 Shop Mod v1.0.28 - Complete Dependency Fix

## Critical Bug Fix Release

This release resolves the final dependency issue that prevented the Village Web Dashboard from starting properly.

## 🐛 **Issues Fixed**

### Jetty WebSocket Dependencies Missing
- **Problem**: `NoClassDefFoundError: org/eclipse/jetty/websocket/server/JettyWebSocketServlet`
- **Cause**: Javalin requires complete Jetty WebSocket server dependencies that weren't bundled
- **Solution**: Added all required Jetty 11.0.25 WebSocket dependencies to the mod JAR

## ✨ **Technical Changes**

### New Dependencies Included
- `jetty-server:11.0.25` - Core Jetty server
- `jetty-servlet:11.0.25` - Servlet support  
- `jetty-util:11.0.25` - Jetty utilities
- `jetty-http:11.0.25` - HTTP protocol support
- `jetty-io:11.0.25` - I/O utilities
- `websocket-jetty-server:11.0.25` - WebSocket server implementation
- `websocket-jetty-api:11.0.25` - WebSocket API
- `websocket-core-server:11.0.25` - WebSocket core server
- `websocket-core-common:11.0.25` - WebSocket common utilities
- `websocket-servlet:11.0.25` - WebSocket servlet support

### JAR Size Increase
- **Previous**: ~3.1MB (incomplete dependencies)
- **Current**: **~5.3MB** (complete dependency bundle)
- **Verification**: All web server dependencies now properly included

## 🚀 **Village Web Dashboard Features**

Now fully functional without crashes:

### Real-time Village Management
- **Worker Management**: Assign, monitor, and optimize villager jobs
- **Resource Tracking**: Live inventory and production monitoring  
- **Building Planning**: Construction management and progress tracking
- **Trade Analysis**: Market optimization and profit analysis

### Web Interface Access
- **URL**: http://localhost:8080 (automatic startup with Minecraft server)
- **Mobile-Friendly**: Responsive design for tablets and phones
- **Real-time Updates**: Live data synchronization with game world

### Player Experience
- **Welcome Messages**: New players get web dashboard introduction
- **Seamless Integration**: No additional setup required
- **Cross-Platform**: Access from any device on your network

## 📋 **Installation & Upgrade**

### New Installation
1. Download `shop-1.0.28.jar`
2. Place in your `mods` folder
3. Start Minecraft - web dashboard auto-starts
4. Look for "Village Web Dashboard Started!" message
5. Access http://localhost:8080

### Upgrading from Previous Versions
1. **Remove old version** (`shop-1.0.27.jar` or earlier)
2. **Install new version** (`shop-1.0.28.jar`)
3. **Restart Minecraft server**
4. **Verify startup**: Check for successful web dashboard launch

## 🔧 **Troubleshooting**

### Expected Startup Messages
```
[Server thread/INFO]: Village Web Dashboard Started!
[Server thread/INFO]: Javalin started in XXXms \o/
[Server thread/INFO]: Listening on http://localhost:8080/
```

### If Issues Persist
- Ensure old mod versions are completely removed
- Check that port 8080 is available
- Verify JAR file size is approximately 5.3MB
- Check Minecraft logs for any remaining dependency errors

## 🎯 **What's Next**

This release completes the dependency resolution work. The Village Web Dashboard is now production-ready with:
- ✅ Complete dependency bundle
- ✅ No more ClassNotFoundException errors  
- ✅ Full web server functionality
- ✅ Mobile-responsive interface
- ✅ Real-time village management

## 📊 **Compatibility**

- **Minecraft**: 1.21.11
- **Fabric Loader**: 0.18.4+
- **Java**: 21+
- **Fabric API**: 0.141.1+

## 👥 **Community**

Experience seamless village management through the web interface! The dashboard provides comprehensive village oversight with real-time data and mobile accessibility.

---

**Full Changelog**: https://github.com/Uipg9/shop/compare/v1.0.27...v1.0.28