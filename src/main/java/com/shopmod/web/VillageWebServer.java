package com.shopmod.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopmod.village.*;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.staticfiles.Location;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Village Web Dashboard - Localhost interface for village management
 * Similar to minecraft-web-chat but for village system
 */
public class VillageWebServer {
    private static VillageWebServer instance;
    private Javalin app;
    private final int port = 8081;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private MinecraftServer server;
    private final Map<String, Long> lastUpdateTimes = new ConcurrentHashMap<>();
    
    public static VillageWebServer getInstance() {
        if (instance == null) {
            instance = new VillageWebServer();
        }
        return instance;
    }
    
    public void start(MinecraftServer server) {
        this.server = server;
        
        if (app != null) {
            stop();
        }
        
        try {
            app = Javalin.create(config -> {
                config.addStaticFiles("/web", Location.CLASSPATH);
                // Simple configuration without WebSocket issues
            }).start(port);
            
            // Add CORS headers manually
            app.before("/*", ctx -> {
                ctx.header("Access-Control-Allow-Origin", "*");
                ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
            });
            
            setupRoutes();
            
            System.out.println("=================================");
            System.out.println("🌐 Village Web Dashboard Started!");
            System.out.println("📍 URL: http://localhost:" + port);
            System.out.println("🏘️ Manage your villages from your browser!");
            System.out.println("=================================");
            
        } catch (Exception e) {
            System.err.println("Failed to start Village Web Dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void stop() {
        if (app != null) {
            app.stop();
            app = null;
            System.out.println("Village Web Dashboard stopped.");
        }
    }
    
    private void setupRoutes() {
        // Main dashboard page
        app.get("/", ctx -> ctx.redirect("/village"));
        
        // Village dashboard page
        app.get("/village", this::serveDashboard);
        
        // API endpoints
        app.get("/api/players", this::getOnlinePlayers);
        app.get("/api/village/{playerName}", this::getVillageData);
        app.post("/api/village/{playerName}/worker/{workerType}/hire", this::hireWorker);
        app.post("/api/village/{playerName}/worker/{workerType}/fire", this::fireWorker);
        app.post("/api/village/{playerName}/worker/{workerType}/upgrade", this::upgradeWorker);
        app.post("/api/village/{playerName}/building/{buildingType}/build", this::buildBuilding);
        app.post("/api/village/{playerName}/auto-manage/toggle", this::toggleAutoManage);
        app.get("/api/village/{playerName}/resources", this::getResources);
        app.post("/api/village/{playerName}/trade/{resourceType}/transfer", this::transferToTradeCenter);
        app.post("/api/village/{playerName}/trade/{resourceType}/sell", this::sellResource);
        app.post("/api/village/{playerName}/trade/{resourceType}/auto-sell/toggle", this::toggleAutoSell);
    }
    
    private void serveDashboard(Context ctx) {
        String html = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Village Dashboard - Shop Mod</title>
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        min-height: 100vh;
                        color: #333;
                    }
                    
                    .container {
                        max-width: 1400px;
                        margin: 0 auto;
                        padding: 20px;
                    }
                    
                    .header {
                        text-align: center;
                        margin-bottom: 30px;
                        color: white;
                    }
                    
                    .header h1 {
                        font-size: 2.5rem;
                        margin-bottom: 10px;
                        text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
                    }
                    
                    .header p {
                        font-size: 1.1rem;
                        opacity: 0.9;
                    }
                    
                    .player-selector {
                        background: white;
                        border-radius: 12px;
                        padding: 20px;
                        margin-bottom: 20px;
                        box-shadow: 0 8px 32px rgba(0,0,0,0.1);
                    }
                    
                    .player-selector h3 {
                        margin-bottom: 15px;
                        color: #4a5568;
                    }
                    
                    select, button {
                        padding: 10px 15px;
                        border: 2px solid #e2e8f0;
                        border-radius: 8px;
                        font-size: 1rem;
                        margin-right: 10px;
                    }
                    
                    button {
                        background: #667eea;
                        color: white;
                        border: none;
                        cursor: pointer;
                        transition: all 0.3s ease;
                    }
                    
                    button:hover {
                        background: #5a67d8;
                        transform: translateY(-2px);
                    }
                    
                    .dashboard-grid {
                        display: grid;
                        grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
                        gap: 20px;
                        margin-bottom: 20px;
                    }
                    
                    .card {
                        background: white;
                        border-radius: 12px;
                        padding: 20px;
                        box-shadow: 0 8px 32px rgba(0,0,0,0.1);
                        transition: all 0.3s ease;
                    }
                    
                    .card:hover {
                        transform: translateY(-4px);
                        box-shadow: 0 16px 48px rgba(0,0,0,0.15);
                    }
                    
                    .card h3 {
                        color: #2d3748;
                        margin-bottom: 15px;
                        border-bottom: 2px solid #e2e8f0;
                        padding-bottom: 8px;
                    }
                    
                    .status-grid {
                        display: grid;
                        grid-template-columns: repeat(2, 1fr);
                        gap: 10px;
                        margin-bottom: 15px;
                    }
                    
                    .status-item {
                        background: #f7fafc;
                        padding: 10px;
                        border-radius: 8px;
                        text-align: center;
                    }
                    
                    .status-value {
                        font-size: 1.5rem;
                        font-weight: bold;
                        color: #667eea;
                    }
                    
                    .worker-list, .resource-list {
                        max-height: 300px;
                        overflow-y: auto;
                    }
                    
                    .worker-item, .resource-item {
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        padding: 10px;
                        border-bottom: 1px solid #e2e8f0;
                    }
                    
                    .worker-actions button, .resource-actions button {
                        padding: 5px 10px;
                        margin: 0 2px;
                        font-size: 0.8rem;
                    }
                    
                    .auto-manage-toggle {
                        background: #f7fafc;
                        border: 2px solid #e2e8f0;
                        border-radius: 8px;
                        padding: 15px;
                        text-align: center;
                        margin: 15px 0;
                    }
                    
                    .auto-manage-toggle.active {
                        background: #e6fffa;
                        border-color: #38b2ac;
                    }
                    
                    .refresh-btn {
                        position: fixed;
                        bottom: 20px;
                        right: 20px;
                        background: #48bb78;
                        border-radius: 50%;
                        width: 60px;
                        height: 60px;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        font-size: 1.5rem;
                        box-shadow: 0 8px 16px rgba(0,0,0,0.2);
                    }
                    
                    .offline {
                        opacity: 0.5;
                        filter: grayscale(1);
                    }
                    
                    .hidden { display: none; }
                    
                    .loading {
                        text-align: center;
                        padding: 40px;
                        color: #718096;
                    }
                    
                    @media (max-width: 768px) {
                        .dashboard-grid {
                            grid-template-columns: 1fr;
                        }
                        
                        .status-grid {
                            grid-template-columns: 1fr;
                        }
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🏘️ Village Dashboard</h1>
                        <p>Manage your Minecraft villages from your web browser</p>
                    </div>
                    
                    <div class="player-selector">
                        <h3>Select Player</h3>
                        <select id="playerSelect">
                            <option value="">Choose a player...</option>
                        </select>
                        <button onclick="loadVillageData()">Load Village</button>
                        <button onclick="loadPlayers()">Refresh Players</button>
                    </div>
                    
                    <div id="villageContent" class="hidden">
                        <div class="dashboard-grid">
                            <!-- Village Status Card -->
                            <div class="card">
                                <h3>🏡 Village Status</h3>
                                <div class="status-grid">
                                    <div class="status-item">
                                        <div class="status-value" id="villageLevel">1</div>
                                        <div>Level</div>
                                    </div>
                                    <div class="status-item">
                                        <div class="status-value" id="workerCount">0/3</div>
                                        <div>Workers</div>
                                    </div>
                                    <div class="status-item">
                                        <div class="status-value" id="dailyProfit">$0</div>
                                        <div>Daily Profit</div>
                                    </div>
                                    <div class="status-item">
                                        <div class="status-value" id="totalValue">$0</div>
                                        <div>Total Value</div>
                                    </div>
                                </div>
                                
                                <div id="autoManageToggle" class="auto-manage-toggle" onclick="toggleAutoManage()">
                                    <strong>Auto-Manage: <span id="autoManageStatus">OFF</span></strong>
                                    <div style="font-size: 0.9rem; color: #718096; margin-top: 5px;">
                                        Click to toggle between Manual (100%) and Auto (70%) mode
                                    </div>
                                </div>
                            </div>
                            
                            <!-- Workers Card -->
                            <div class="card">
                                <h3>👥 Workers</h3>
                                <div class="worker-list" id="workerList">
                                    <div class="loading">Loading workers...</div>
                                </div>
                            </div>
                            
                            <!-- Resources Card -->
                            <div class="card">
                                <h3>📦 Resources</h3>
                                <div class="resource-list" id="resourceList">
                                    <div class="loading">Loading resources...</div>
                                </div>
                            </div>
                            
                            <!-- Buildings Card -->
                            <div class="card">
                                <h3>🏗️ Buildings</h3>
                                <div class="worker-list" id="buildingList">
                                    <div class="loading">Loading buildings...</div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                
                <button class="refresh-btn" onclick="loadVillageData()" title="Refresh Data">
                    🔄
                </button>
                
                <script>
                    let currentPlayer = '';
                    
                    // Load online players
                    async function loadPlayers() {
                        try {
                            const response = await fetch('/api/players');
                            const players = await response.json();
                            const select = document.getElementById('playerSelect');
                            select.innerHTML = '<option value="">Choose a player...</option>';
                            players.forEach(player => {
                                select.innerHTML += `<option value="${player}">${player}</option>`;
                            });
                        } catch (error) {
                            console.error('Failed to load players:', error);
                        }
                    }
                    
                    // Load village data for selected player
                    async function loadVillageData() {
                        const player = document.getElementById('playerSelect').value;
                        if (!player) {
                            alert('Please select a player first');
                            return;
                        }
                        
                        currentPlayer = player;
                        document.getElementById('villageContent').classList.remove('hidden');
                        
                        try {
                            const response = await fetch(`/api/village/${player}`);
                            const villageData = await response.json();
                            updateVillageUI(villageData);
                        } catch (error) {
                            console.error('Failed to load village data:', error);
                        }
                    }
                    
                    // Update UI with village data
                    function updateVillageUI(data) {
                        // Village status
                        document.getElementById('villageLevel').textContent = data.villageLevel || 1;
                        document.getElementById('workerCount').textContent = `${data.totalWorkers || 0}/${data.workerSlots || 3}`;
                        document.getElementById('dailyProfit').textContent = formatMoney(data.dailyProfit || 0);
                        document.getElementById('totalValue').textContent = formatMoney(data.totalValue || 0);
                        
                        // Auto-manage status
                        const autoManageEl = document.getElementById('autoManageToggle');
                        const statusEl = document.getElementById('autoManageStatus');
                        if (data.autoManage) {
                            autoManageEl.classList.add('active');
                            statusEl.textContent = 'ON (70% efficiency)';
                        } else {
                            autoManageEl.classList.remove('active');
                            statusEl.textContent = 'OFF (100% efficiency)';
                        }
                        
                        // Workers
                        updateWorkersList(data.workers || {});
                        
                        // Resources
                        updateResourcesList(data.resources || {});
                        
                        // Buildings
                        updateBuildingsList(data.buildings || {});
                    }
                    
                    // Update workers list
                    function updateWorkersList(workers) {
                        const container = document.getElementById('workerList');
                        container.innerHTML = '';
                        
                        const workerTypes = ['FARMER', 'LUMBERJACK', 'FISHERMAN', 'MINER', 'RANCHER', 'BLACKSMITH', 'MERCHANT', 'ENCHANTER'];
                        
                        workerTypes.forEach(type => {
                            const worker = workers[type] || { count: 0, level: 1 };
                            const div = document.createElement('div');
                            div.className = 'worker-item';
                            div.innerHTML = `
                                <div>
                                    <strong>${type}</strong><br>
                                    <small>Count: ${worker.count}, Level: ${worker.level}</small>
                                </div>
                                <div class="worker-actions">
                                    <button onclick="workerAction('${type}', 'hire')">Hire</button>
                                    <button onclick="workerAction('${type}', 'upgrade')">Upgrade</button>
                                    <button onclick="workerAction('${type}', 'fire')">Fire</button>
                                </div>
                            `;
                            container.appendChild(div);
                        });
                    }
                    
                    // Update resources list
                    function updateResourcesList(resources) {
                        const container = document.getElementById('resourceList');
                        container.innerHTML = '';
                        
                        const resourceTypes = ['FOOD', 'WOOD', 'FISH', 'ORE', 'LEATHER', 'WOOL', 'TOOLS', 'ARMOR', 'ENCHANTED', 'RARE'];
                        
                        resourceTypes.forEach(type => {
                            const amount = resources[type] || 0;
                            const div = document.createElement('div');
                            div.className = 'resource-item';
                            div.innerHTML = `
                                <div>
                                    <strong>${type}</strong><br>
                                    <small>Amount: ${amount}</small>
                                </div>
                                <div class="resource-actions">
                                    <button onclick="transferResource('${type}', 10)">Transfer 10</button>
                                    <button onclick="sellResource('${type}', 'all')">Sell All</button>
                                </div>
                            `;
                            container.appendChild(div);
                        });
                    }
                    
                    // Update buildings list
                    function updateBuildingsList(buildings) {
                        const container = document.getElementById('buildingList');
                        container.innerHTML = '';
                        
                        const buildingTypes = ['HOUSE', 'GRANARY', 'WAREHOUSE', 'WORKSHOP', 'MARKET', 'FARM_EXPANSION', 'MINE_SHAFT', 'TRADING_POST', 'LIBRARY', 'BARRACKS'];
                        
                        buildingTypes.forEach(type => {
                            const count = buildings[type] || 0;
                            const div = document.createElement('div');
                            div.className = 'worker-item';
                            div.innerHTML = `
                                <div>
                                    <strong>${type.replace('_', ' ')}</strong><br>
                                    <small>Owned: ${count}</small>
                                </div>
                                <div class="worker-actions">
                                    <button onclick="buildBuilding('${type}')">Build</button>
                                </div>
                            `;
                            container.appendChild(div);
                        });
                    }
                    
                    // Worker actions
                    async function workerAction(workerType, action) {
                        if (!currentPlayer) return;
                        
                        try {
                            const response = await fetch(`/api/village/${currentPlayer}/worker/${workerType}/${action}`, {
                                method: 'POST'
                            });
                            const result = await response.json();
                            if (result.success) {
                                loadVillageData(); // Refresh data
                            } else {
                                alert(result.message || 'Action failed');
                            }
                        } catch (error) {
                            console.error('Worker action failed:', error);
                        }
                    }
                    
                    // Building actions
                    async function buildBuilding(buildingType) {
                        if (!currentPlayer) return;
                        
                        try {
                            const response = await fetch(`/api/village/${currentPlayer}/building/${buildingType}/build`, {
                                method: 'POST'
                            });
                            const result = await response.json();
                            if (result.success) {
                                loadVillageData(); // Refresh data
                            } else {
                                alert(result.message || 'Build failed');
                            }
                        } catch (error) {
                            console.error('Build action failed:', error);
                        }
                    }
                    
                    // Resource actions
                    async function transferResource(resourceType, amount) {
                        if (!currentPlayer) return;
                        
                        try {
                            const response = await fetch(`/api/village/${currentPlayer}/trade/${resourceType}/transfer`, {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/json' },
                                body: JSON.stringify({ amount })
                            });
                            const result = await response.json();
                            if (result.success) {
                                loadVillageData(); // Refresh data
                            } else {
                                alert(result.message || 'Transfer failed');
                            }
                        } catch (error) {
                            console.error('Transfer failed:', error);
                        }
                    }
                    
                    async function sellResource(resourceType, amount) {
                        if (!currentPlayer) return;
                        
                        try {
                            const response = await fetch(`/api/village/${currentPlayer}/trade/${resourceType}/sell`, {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/json' },
                                body: JSON.stringify({ amount })
                            });
                            const result = await response.json();
                            if (result.success) {
                                loadVillageData(); // Refresh data
                            } else {
                                alert(result.message || 'Sell failed');
                            }
                        } catch (error) {
                            console.error('Sell failed:', error);
                        }
                    }
                    
                    // Toggle auto-manage
                    async function toggleAutoManage() {
                        if (!currentPlayer) return;
                        
                        try {
                            const response = await fetch(`/api/village/${currentPlayer}/auto-manage/toggle`, {
                                method: 'POST'
                            });
                            const result = await response.json();
                            if (result.success) {
                                loadVillageData(); // Refresh data
                            } else {
                                alert(result.message || 'Toggle failed');
                            }
                        } catch (error) {
                            console.error('Toggle auto-manage failed:', error);
                        }
                    }
                    
                    // Utility functions
                    function formatMoney(amount) {
                        return new Intl.NumberFormat('en-US', {
                            style: 'currency',
                            currency: 'USD'
                        }).format(amount);
                    }
                    
                    // Auto-refresh every 30 seconds
                    setInterval(() => {
                        if (currentPlayer) {
                            loadVillageData();
                        }
                    }, 30000);
                    
                    // Load players on page load
                    loadPlayers();
                </script>
            </body>
            </html>
            """;
        
        ctx.html(html);
    }
    
    // API endpoint implementations
    private void getOnlinePlayers(Context ctx) {
        List<String> playerNames = new ArrayList<>();
        if (server != null) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                playerNames.add(player.getName().getString());
            }
        }
        ctx.json(playerNames);
    }
    
    private void getVillageData(Context ctx) {
        String playerName = ctx.pathParam("playerName");
        ServerPlayer player = server.getPlayerList().getPlayerByName(playerName);
        
        if (player == null) {
            ctx.status(404).json(Map.of("error", "Player not found"));
            return;
        }
        
        VillageManager.Village village = VillageManager.getVillage(player.getUUID());
        
        Map<String, Object> data = new HashMap<>();
        data.put("playerName", playerName);
        data.put("villageLevel", village.getVillageLevel());
        data.put("totalWorkers", village.getTotalWorkerCount());
        data.put("workerSlots", village.getTotalWorkerSlots());
        data.put("autoManage", village.isAutoManage());
        
        // Calculate daily profit
        long dailySalaries = 0;
        long estimatedIncome = 0;
        for (var entry : village.getWorkers().entrySet()) {
            VillagerWorker workerType = entry.getKey();
            VillageManager.WorkerData workerData = entry.getValue();
            dailySalaries += workerType.getDailySalary() * workerData.getCount();
            estimatedIncome += workerType.getEstimatedDailyValue() * workerData.getCount();
        }
        data.put("dailyProfit", estimatedIncome - dailySalaries);
        
        // Calculate total village value
        long totalValue = 0;
        for (var entry : village.getResources().entrySet()) {
            totalValue += entry.getKey().getValuePerUnit() * entry.getValue();
        }
        data.put("totalValue", totalValue);
        
        // Workers data
        Map<String, Object> workersData = new HashMap<>();
        for (var entry : village.getWorkers().entrySet()) {
            VillagerWorker workerType = entry.getKey();
            VillageManager.WorkerData workerData = entry.getValue();
            Map<String, Object> workerInfo = new HashMap<>();
            workerInfo.put("count", workerData.getCount());
            workerInfo.put("level", workerData.getLevel());
            workerInfo.put("status", workerData.getStatus().toString());
            workersData.put(workerType.toString(), workerInfo);
        }
        data.put("workers", workersData);
        
        // Resources data
        Map<String, Long> resourcesData = new HashMap<>();
        for (ResourceType type : ResourceType.values()) {
            resourcesData.put(type.toString(), village.getResources().getOrDefault(type, 0L));
        }
        data.put("resources", resourcesData);
        
        // Buildings data
        Map<String, Integer> buildingsData = new HashMap<>();
        for (VillageBuilding building : VillageBuilding.values()) {
            buildingsData.put(building.toString(), village.getBuildings().getOrDefault(building, 0));
        }
        data.put("buildings", buildingsData);
        
        ctx.json(data);
    }
    
    private void hireWorker(Context ctx) {
        executeWorkerAction(ctx, "hire");
    }
    
    private void fireWorker(Context ctx) {
        executeWorkerAction(ctx, "fire");
    }
    
    private void upgradeWorker(Context ctx) {
        executeWorkerAction(ctx, "upgrade");
    }
    
    private void executeWorkerAction(Context ctx, String action) {
        String playerName = ctx.pathParam("playerName");
        String workerTypeStr = ctx.pathParam("workerType");
        
        ServerPlayer player = server.getPlayerList().getPlayerByName(playerName);
        if (player == null) {
            ctx.status(404).json(Map.of("success", false, "message", "Player not found"));
            return;
        }
        
        try {
            VillagerWorker workerType = VillagerWorker.valueOf(workerTypeStr);
            boolean success = false;
            String message = "";
            
            switch (action) {
                case "hire":
                    success = VillageManager.hireWorker(player, workerType);
                    message = success ? "Worker hired successfully" : "Failed to hire worker";
                    break;
                case "fire":
                    success = VillageManager.fireWorker(player, workerType);
                    message = success ? "Worker fired successfully" : "Failed to fire worker";
                    break;
                case "upgrade":
                    success = VillageManager.upgradeWorker(player, workerType);
                    message = success ? "Worker upgraded successfully" : "Failed to upgrade worker";
                    break;
            }
            
            ctx.json(Map.of("success", success, "message", message));
        } catch (Exception e) {
            ctx.status(400).json(Map.of("success", false, "message", "Invalid worker type or action failed"));
        }
    }
    
    private void buildBuilding(Context ctx) {
        String playerName = ctx.pathParam("playerName");
        String buildingTypeStr = ctx.pathParam("buildingType");
        
        ServerPlayer player = server.getPlayerList().getPlayerByName(playerName);
        if (player == null) {
            ctx.status(404).json(Map.of("success", false, "message", "Player not found"));
            return;
        }
        
        try {
            VillageBuilding buildingType = VillageBuilding.valueOf(buildingTypeStr);
            boolean success = VillageManager.buildBuilding(player, buildingType);
            String message = success ? "Building constructed successfully" : "Failed to construct building";
            
            ctx.json(Map.of("success", success, "message", message));
        } catch (Exception e) {
            ctx.status(400).json(Map.of("success", false, "message", "Invalid building type or construction failed"));
        }
    }
    
    private void toggleAutoManage(Context ctx) {
        String playerName = ctx.pathParam("playerName");
        
        ServerPlayer player = server.getPlayerList().getPlayerByName(playerName);
        if (player == null) {
            ctx.status(404).json(Map.of("success", false, "message", "Player not found"));
            return;
        }
        
        VillageManager.toggleAutoManage(player);
        ctx.json(Map.of("success", true, "message", "Auto-manage toggled"));
    }
    
    private void getResources(Context ctx) {
        // Implementation for resource endpoint
        getVillageData(ctx); // Reuse village data for now
    }
    
    private void transferToTradeCenter(Context ctx) {
        String playerName = ctx.pathParam("playerName");
        String resourceTypeStr = ctx.pathParam("resourceType");
        
        ServerPlayer player = server.getPlayerList().getPlayerByName(playerName);
        if (player == null) {
            ctx.status(404).json(Map.of("success", false, "message", "Player not found"));
            return;
        }
        
        try {
            Map<String, Object> body = objectMapper.readValue(ctx.body(), Map.class);
            int amount = (Integer) body.get("amount");
            ResourceType resourceType = ResourceType.valueOf(resourceTypeStr);
            
            boolean success = TradeCenterManager.transferFromVillage(player, resourceType, amount);
            String message = success ? "Resources transferred successfully" : "Failed to transfer resources";
            
            ctx.json(Map.of("success", success, "message", message));
        } catch (Exception e) {
            ctx.status(400).json(Map.of("success", false, "message", "Transfer failed"));
        }
    }
    
    private void sellResource(Context ctx) {
        String playerName = ctx.pathParam("playerName");
        String resourceTypeStr = ctx.pathParam("resourceType");
        
        ServerPlayer player = server.getPlayerList().getPlayerByName(playerName);
        if (player == null) {
            ctx.status(404).json(Map.of("success", false, "message", "Player not found"));
            return;
        }
        
        try {
            Map<String, Object> body = objectMapper.readValue(ctx.body(), Map.class);
            Object amountObj = body.get("amount");
            ResourceType resourceType = ResourceType.valueOf(resourceTypeStr);
            
            boolean success;
            if ("all".equals(amountObj)) {
                success = TradeCenterManager.sellAllResource(player, resourceType);
            } else {
                int amount = (Integer) amountObj;
                success = TradeCenterManager.sellResource(player, resourceType, amount);
            }
            
            String message = success ? "Resources sold successfully" : "Failed to sell resources";
            ctx.json(Map.of("success", success, "message", message));
        } catch (Exception e) {
            ctx.status(400).json(Map.of("success", false, "message", "Sell failed"));
        }
    }
    
    private void toggleAutoSell(Context ctx) {
        String playerName = ctx.pathParam("playerName");
        String resourceTypeStr = ctx.pathParam("resourceType");
        
        ServerPlayer player = server.getPlayerList().getPlayerByName(playerName);
        if (player == null) {
            ctx.status(404).json(Map.of("success", false, "message", "Player not found"));
            return;
        }
        
        try {
            ResourceType resourceType = ResourceType.valueOf(resourceTypeStr);
            TradeCenterManager.toggleAutoSell(player, resourceType);
            
            ctx.json(Map.of("success", true, "message", "Auto-sell toggled"));
        } catch (Exception e) {
            ctx.status(400).json(Map.of("success", false, "message", "Toggle failed"));
        }
    }
}