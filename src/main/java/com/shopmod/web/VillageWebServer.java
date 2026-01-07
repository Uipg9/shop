package com.shopmod.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopmod.village.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import static spark.Spark.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Village Web Dashboard - Localhost interface for village management
 * Now using Spark instead of Javalin to avoid WebSocket dependency issues
 */
public class VillageWebServer {
    private static VillageWebServer instance;
    private final int port = 8081;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private MinecraftServer server;
    private final Map<String, Long> lastUpdateTimes = new ConcurrentHashMap<>();
    private boolean isStarted = false;
    
    public static VillageWebServer getInstance() {
        if (instance == null) {
            instance = new VillageWebServer();
        }
        return instance;
    }
    
    public void start(MinecraftServer server) {
        this.server = server;
        
        if (isStarted) {
            stop();
        }
        
        try {
            // Configure Spark
            port(this.port);
            
            // Enable CORS
            before((req, res) -> {
                res.header("Access-Control-Allow-Origin", "*");
                res.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                res.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
            });
            
            // Static files
            staticFiles.location("/web");
            
            setupRoutes();
            
            isStarted = true;
            
            System.out.println("=================================");
            System.out.println("🌐 Village Web Dashboard Started!");
            System.out.println("📍 URL: http://localhost:" + this.port);
            System.out.println("🏘️ Manage your villages from your browser!");
            System.out.println("=================================");
            
        } catch (Exception e) {
            System.err.println("Failed to start Village Web Dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void stop() {
        if (isStarted) {
            spark.Spark.stop();
            isStarted = false;
            System.out.println("Village Web Dashboard stopped.");
        }
    }
    
    private void setupRoutes() {
        // Main dashboard page
        get("/", (req, res) -> {
            res.redirect("/village");
            return null;
        });
        
        // Village dashboard page
        get("/village", (req, res) -> {
            res.type("text/html");
            return serveDashboard();
        });
        
        // API endpoints
        get("/api/players", (req, res) -> {
            res.type("application/json");
            return getOnlinePlayers();
        });
        
        get("/api/village/:playerName", (req, res) -> {
            res.type("application/json");
            return getVillageData(req.params(":playerName"));
        });
        
        post("/api/village/:playerName/worker/:workerType/hire", (req, res) -> {
            res.type("application/json");
            return hireWorker(req.params(":playerName"), req.params(":workerType"));
        });
        
        post("/api/village/:playerName/worker/:workerType/fire", (req, res) -> {
            res.type("application/json");
            return fireWorker(req.params(":playerName"), req.params(":workerType"));
        });
        
        post("/api/village/:playerName/worker/:workerType/upgrade", (req, res) -> {
            res.type("application/json");
            return upgradeWorker(req.params(":playerName"), req.params(":workerType"));
        });
        
        post("/api/village/:playerName/building/:buildingType/build", (req, res) -> {
            res.type("application/json");
            return buildBuilding(req.params(":playerName"), req.params(":buildingType"));
        });
        
        post("/api/village/:playerName/auto-manage/toggle", (req, res) -> {
            res.type("application/json");
            return toggleAutoManage(req.params(":playerName"));
        });
        
        get("/api/village/:playerName/resources", (req, res) -> {
            res.type("application/json");
            return getResources(req.params(":playerName"));
        });
        
        post("/api/village/:playerName/trade/:resourceType/transfer", (req, res) -> {
            res.type("application/json");
            return transferToTradeCenter(req.params(":playerName"), req.params(":resourceType"), req.body());
        });
        
        post("/api/village/:playerName/trade/:resourceType/sell", (req, res) -> {
            res.type("application/json");
            return sellResource(req.params(":playerName"), req.params(":resourceType"), req.body());
        });
        
        post("/api/village/:playerName/trade/:resourceType/auto-sell/toggle", (req, res) -> {
            res.type("application/json");
            return toggleAutoSell(req.params(":playerName"), req.params(":resourceType"));
        });
    }
    
    private String serveDashboard() {
        return """
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
        
        .card {
            background: white;
            border-radius: 12px;
            padding: 20px;
            margin-bottom: 20px;
            box-shadow: 0 8px 32px rgba(0,0,0,0.1);
        }
        
        .success {
            background: #10b981;
            color: white;
            padding: 10px;
            border-radius: 8px;
            margin-top: 10px;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🏘️ Village Dashboard</h1>
            <p>Manage your Minecraft villages from your browser</p>
        </div>
        
        <div class="card">
            <h3>Village Web Dashboard</h3>
            <p>Successfully started on port 8081!</p>
            <p>This is a simplified version using Spark instead of Javalin to avoid dependency conflicts.</p>
            <div class="success">✅ Web server is running without WebSocket dependencies!</div>
        </div>
        
        <div class="card">
            <h3>Available Endpoints</h3>
            <ul>
                <li>GET /api/players - List online players</li>
                <li>GET /api/village/{player} - Get village data</li>
                <li>POST /api/village/{player}/worker/{type}/hire - Hire worker</li>
                <li>And more...</li>
            </ul>
        </div>
    </div>
</body>
</html>
""";
    }
    
    // API endpoint implementations (simplified for now)
    private String getOnlinePlayers() {
        List<String> playerNames = new ArrayList<>();
        if (server != null) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                playerNames.add(player.getName().getString());
            }
        }
        
        try {
            return objectMapper.writeValueAsString(playerNames);
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }
    
    private String getVillageData(String playerName) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("playerName", playerName);
            response.put("success", true);
            response.put("message", "Village data for " + playerName);
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    private String hireWorker(String playerName, String workerType) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Hired " + workerType + " for " + playerName);
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    private String fireWorker(String playerName, String workerType) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Fired " + workerType + " for " + playerName);
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    private String upgradeWorker(String playerName, String workerType) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Upgraded " + workerType + " for " + playerName);
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    private String buildBuilding(String playerName, String buildingType) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Built " + buildingType + " for " + playerName);
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    private String toggleAutoManage(String playerName) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Toggled auto-manage for " + playerName);
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    private String getResources(String playerName) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("emeralds", 100);
            response.put("wood", 250);
            response.put("food", 180);
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    private String transferToTradeCenter(String playerName, String resourceType, String body) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Transferred " + resourceType + " to trade center for " + playerName);
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    private String sellResource(String playerName, String resourceType, String body) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Sold " + resourceType + " for " + playerName);
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    private String toggleAutoSell(String playerName, String resourceType) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Toggled auto-sell " + resourceType + " for " + playerName);
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    private int getMarketPrice(String resourceType) {
        // Simple market prices
        return switch (resourceType.toLowerCase()) {
            case "wood" -> 2;
            case "food" -> 3;
            case "stone" -> 1;
            default -> 1;
        };
    }
}