# 🐛 Shop Mod v1.0.27 - Critical Dependency Fix

## 🚨 Critical Bug Fix
This release fixes a **critical issue** that prevented the Village Web Dashboard from starting properly.

### 🔧 What Was Fixed
- **NoClassDefFoundError for Jackson ObjectMapper** - The main cause of crashes when creating worlds
- **Missing dependencies** - All web server libraries now properly bundled in the JAR
- **Incomplete JAR packaging** - JAR size increased from ~300KB to ~3.1MB with all dependencies included

### 📦 Dependencies Now Included
- **Jackson Core** (2.15.2) - JSON processing
- **Jackson Databind** (2.15.2) - Object mapping
- **Jackson Annotations** (2.15.2) - Annotation support
- **Javalin** (5.6.3) - Web server framework
- **Java-WebSocket** (1.5.4) - WebSocket support
- **SLF4J Simple** (2.0.7) - Logging

## 🌐 Village Web Dashboard Now Works!
With this fix, the Village Web Dashboard should now start properly and be accessible at `http://localhost:8080`.

### Features Available:
✅ **Real-time village overview** with responsive design  
✅ **Worker management** - hire, fire, upgrade from browser  
✅ **Resource management** - transfer and sell resources remotely  
✅ **Building construction** - build and upgrade from web interface  
✅ **Auto-manage toggle** - switch modes from anywhere  
✅ **Mobile-friendly** design for phones and tablets  
✅ **Welcome system** - players notified about web dashboard when joining  

## 🔄 Upgrading from v1.0.26
Simply replace your existing `shop-1.0.26.jar` with `shop-1.0.27.jar` in your mods folder. All your village data will be preserved.

## 🙏 Thank You
Thanks for your patience with this critical bug! The Village Web Dashboard is now fully functional and ready to revolutionize how you manage your village empire.

## 📋 Technical Details
- **Fixed**: `modImplementation include()` instead of `implementation` for web dependencies
- **Added**: All required Jackson dependencies for proper JSON processing
- **Verified**: JAR now contains all necessary libraries for web server functionality
- **Tested**: Web dashboard starts correctly without dependency errors

---
**Full Changelog**: https://github.com/Uipg9/shop/compare/v1.0.26...v1.0.27