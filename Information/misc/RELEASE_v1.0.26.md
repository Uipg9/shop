# Shop Mod v1.0.26 - VILLAGE WEB DASHBOARD & WELCOME SYSTEM

## 🌐 **Revolutionary Web Interface**

**Access your village from anywhere: `http://localhost:8080`**

v1.0.26 introduces a groundbreaking localhost web dashboard that transforms village management from an in-game-only experience to a modern, accessible web application. Similar to minecraft-web-chat but specifically designed for village operations.

## 🎯 **Major Features Added**

### **🏘️ Complete Web Dashboard**
- **Modern responsive interface** that works on desktop, tablet, and mobile
- **Real-time village overview** with level, worker count, daily profit, total value
- **Auto-refresh every 30 seconds** + manual refresh button for live monitoring
- **Beautiful gradient design** with intuitive card-based layout

### **👥 Full Worker Management**
- **Hire workers** instantly from the web interface
- **Upgrade workers** to improve production efficiency
- **Fire workers** to reduce operational costs
- **Live status display** for all 8 worker types:
  - FARMER, LUMBERJACK, FISHERMAN, MINER
  - RANCHER, BLACKSMITH, MERCHANT, ENCHANTER

### **📦 Advanced Resource Control**
- **Resource overview** with current amounts for all 10 resource types
- **Quick transfer** functionality (transfer 10 resources to Trade Center)
- **Bulk selling** options (sell all resources of specific type)
- **Resource types**: FOOD, WOOD, FISH, ORE, LEATHER, WOOL, TOOLS, ARMOR, ENCHANTED, RARE

### **🏗️ Building Management**
- **Direct construction** of all 10 building types from web interface
- **Ownership tracking** with current building counts
- **Building types**: HOUSE, GRANARY, WAREHOUSE, WORKSHOP, MARKET, FARM_EXPANSION, MINE_SHAFT, TRADING_POST, LIBRARY, BARRACKS

### **⚡ Auto-Manage Toggle**
- **Visual status indicator** showing current mode
- **Efficiency display**: 100% efficiency (manual) vs 70% efficiency (auto)
- **One-click toggle** between management modes

### **👋 Welcome System**
- **Beautiful welcome message** for new players
- **Clear instructions** about web dashboard availability
- **Prominent link display** with formatted URL
- **Delayed delivery** to avoid interference with other join messages

## 🔧 **Technical Enhancements**

### **Web Framework Integration**
- **Javalin 5.6.3** web server framework for professional-grade performance
- **Jackson 2.15.2** for efficient JSON data processing
- **WebSocket 1.5.4** support for future real-time updates
- **SLF4J 2.0.7** for proper logging and debugging

### **RESTful API Architecture**
- **15 comprehensive endpoints** for complete village control
- **JSON response format** for clean data structure
- **Proper HTTP status codes** and error handling
- **CORS configuration** for local development

### **Server Lifecycle Management**
- **Automatic startup** when Minecraft server starts
- **Clean shutdown** when server stops
- **Port 8080** configuration (localhost only for security)
- **Resource management** with proper cleanup

### **Mobile-Responsive Design**
- **Adaptive grid layout** (4-column to 1-column responsive)
- **Touch-friendly controls** with large buttons and tap targets
- **Scrollable sections** for handling long lists
- **Viewport optimized** for all device sizes

## 📚 **Documentation & Guides**

### **New Documentation Files**
- **Village Web Dashboard Guide** - Complete user manual with API documentation
- **Implementation Summary** - Technical details for developers
- **Updated Changelog** - Comprehensive feature tracking

### **API Endpoints Reference**
```
GET  /api/players                                    - List online players
GET  /api/village/{playerName}                       - Get village data
POST /api/village/{playerName}/worker/{type}/hire    - Hire worker
POST /api/village/{playerName}/worker/{type}/fire    - Fire worker
POST /api/village/{playerName}/worker/{type}/upgrade - Upgrade worker
POST /api/village/{playerName}/building/{type}/build - Build structure
POST /api/village/{playerName}/auto-manage/toggle    - Toggle auto-manage
POST /api/village/{playerName}/trade/{type}/transfer - Transfer resources
POST /api/village/{playerName}/trade/{type}/sell     - Sell resources
```

## 🚀 **How to Access**

1. **Start your Minecraft server** with Shop Mod v1.0.26
2. **Look for the startup message** in console showing web dashboard URL
3. **Open your web browser** and navigate to `http://localhost:8080`
4. **Select your player** from the dropdown menu
5. **Click "Load Village"** to access your management dashboard
6. **Enjoy remote village management** from any device on your network!

## 🎨 **User Experience Improvements**

### **Visual Design**
- **Professional gradient background** (purple to blue)
- **Color-coded information** (red for costs, green for income)
- **Hover effects** and smooth animations throughout
- **Loading states** with clear feedback
- **Error handling** with user-friendly messages

### **Intuitive Navigation**
- **Player selection dropdown** for multi-player servers
- **Card-based information display** for easy scanning
- **Grouped functionality** (workers, resources, buildings, status)
- **Quick action buttons** for common operations

## 🔮 **Future Enhancement Ready**

The web dashboard provides a solid foundation for future enhancements:
- Real-time WebSocket notifications for instant updates
- Historical performance charts and analytics
- Multi-village comparison views
- Export/import functionality
- Mobile app companion support

## ⚡ **Performance & Security**

- **Localhost-only access** for security (no external network exposure)
- **Efficient JSON processing** for fast data transfer
- **Minimal resource usage** with optimized web server
- **Clean resource management** preventing memory leaks

---

**🎉 This release represents a major milestone in village management accessibility, bringing modern web technology to Minecraft village operations while maintaining the depth and complexity of the existing village system.**

## Installation Notes

- Requires Minecraft 1.21.11 with Fabric Loader 0.18.4+
- Compatible with existing v1.0.25 save data
- Automatic migration of village data
- No configuration required - web dashboard starts automatically

## Compatibility

- **Minecraft**: 1.21.11
- **Fabric Loader**: 0.18.4+
- **Fabric API**: 0.141.1+1.21.11
- **Java**: 21+ (recommended for optimal performance)

---

**Download the JAR file below and place it in your `mods` folder. The web dashboard will be available immediately at `http://localhost:8080` when you start your server!**