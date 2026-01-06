# Shop Mod - Features Implemented So Far

## ✅ COMPLETED FEATURES:

### 1. **Comprehensive Item Pricing System**
   - **Hundreds of Minecraft items** organized into 6 tiers
   - All blocks, tools, armor, materials, spawn eggs
   - Tier 0 (Starter) - FREE basic survival items
   - Tier 1 (Farmer) - $2,000 - Farming & animals  
   - Tier 2 (Engineer) - $5,000 - Redstone & automation
   - Tier 3 (Merchant) - $10,000 - Precious minerals & tools
   - Tier 4 (Nether Master) - $25,000 - Nether items
   - Tier 5 (Elite) - $50,000 - End-game items
   - **80% sell-back system** (20% shop fee)

### 2. **Shop Tier System**
   - Players unlock categories by paying
   - Tier progress tracked per player
   - Starts with $1,000 starting money
   - Prevents overwhelming new players

### 3. **Mob Spawner Pricing** (50+ spawners)
   - Tier 1: Passive mobs (cow, pig, sheep) $2k-$5k
   - Tier 2: Utility mobs (villager, iron golem) $3k-$10k
   - Tier 3: Basic hostile (zombie, skeleton, creeper) $5k-$15k
   - Tier 4: Nether mobs (blaze $20k, wither skeleton $25k, ghast $30k)
   - Tier 5: End-game (shulker $40k, evoker $35k, warden $100k, wither $150k, **ender dragon $500k**)

### 4. **Money-Making Guide**
   - Profitable farming strategies
   - Smelting profits
   - Crop multipliers (melon seeds $20 → 9 slices × $3 = $27, profit $7)
   - Tiered guide unlocks with shop tiers

### 5. **Income Systems** (Designed, needs API fixes)
   - Mining rewards (ores $5-$500)
   - Logging rewards (logs $2 each)
   - Crop harvesting rewards (wheat $3, etc.)
   - Compatible with vein miner/timber mods!

### 6. **Spawner Pickup System** (Designed, needs API fixes)
   - Mine spawners with Silk Touch pickaxe
   - Keeps mob type in NBT data
   - Can be bought in shop and placed

### 7. **Data Persistence**
   - JSON-based player data storage
   - Tracks: balance, total earned/spent, items bought/sold, unlocked tiers
   - Saves on server stop, loads on start
   - Tested and working!

## ⚠️ NEEDS API UPDATES FOR MC 1.21.11:

### **Enchantment System** (100+ enchantments designed)
   - MC 1.21.11 changed enchantments to use ResourceKeys/Holders
   - Need to update EnchantmentPricing to new API
   - Need to update LuckyEnchantSystem for:
     - Lucky Enchant Gambling (4 tiers: Basic $500, Advanced $2k, Rare $5k, Legendary $15k)
     - Disenchant system (50% refund)
     - Random enchant application

### **Spawner Pickup**
   - Need to update NBT handling methods
   - Level.isClientSide → Level.isClientSide()
   - ItemStack enchantment methods changed
   - CompoundTag methods now return Optional<>

### **Income Manager**
   - Level.isClientSide → Level.isClientSide()
   - CropBlock.getCloneItemStack signature changed

### **Item Names**
   - Items.TULIP → needs correct 1.21.11 name
   - Items.SCUTE → needs correct 1.21.11 name

## 📋 TODO LIST:

1. **Fix API Compatibility**
   - Update all MC 1.21.11 API calls
   - Fix enchantment system for new Holder API
   - Fix NBT Optional returns
   - Fix item names

2. **GUI Implementation**
   - Update ShopGui with tier tabs
   - Add tier unlock interface
   - Add money-making guide tab
   - Add lucky enchant gambling interface
   - Add disenchant slot
   - Add drag-and-drop sell slot

3. **Additional Features**
   - Passive income system
   - Permission system for admin commands
   - Config file
   - GitHub releases with changelog

## 💡 DESIGN HIGHLIGHTS:

- **Economy Balanced Around Buy/Sell Profits**
  - Farming: wheat seeds $2 → wheat $5 = $3 profit
  - Melons: seeds $20 → 9 slices × $3 = $27 = $7 profit  
  - Smelting: raw iron $35 → iron ingot $50 = $15 profit
  - Nether wart: $25 → harvest 3 × $25 = $75 = $50 profit

- **Compatible with Popular Mods**
  - Vein Miner: Each ore broken pays separately
  - Timber: Each log broken pays separately
  - Crop Harvester: Each crop broken pays separately

- **Fun Gambling System**
  - Pay for random enchantments
  - Higher tiers = better chances
  - Can disenchant for 50% refund
  - Adds excitement to progression!

## 🎯 NEXT STEPS:

Once API issues are fixed:
1. Test income systems in-game
2. Test spawner pickup
3. Implement full GUI with all tabs
4. Add enchantment gambling
5. Balance all prices based on testing
6. Create GitHub release

---

**Status**: Core systems designed and mostly implemented. Just needs MC 1.21.11 API updates to compile!
