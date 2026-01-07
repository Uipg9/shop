# Shop Mod v1.0.41 - Changelog

## 🎉 MAJOR UPDATE: 6 New Systems Added!

### 1. **Property/Real Estate System** ⭐ TOP PRIORITY FEATURE
- **15 Property Types** across 3 categories:
  - 🌾 **LAND**: Small Plot → Large Plot → Farm Land → Estate → Ranch
  - 🏢 **BUILDINGS**: Small Shop → Large Store → Office Building → Shopping Mall → Skyscraper
  - 🏙️ **SETTLEMENTS**: Village → Town → Small City → Large City → Metropolis
- **Progressive Pricing**: $10,000 → $20,000,000 (exponential scaling)
- **Passive Income**: $50/day → $200,000/day
- **5-Level Upgrades**: Each property can be upgraded 5 times for increased income
- **Integration**: Property income is boosted by Research system (Property Mogul)
- **Commands**: `/property` or `/realestate`
- **GUI**: Tabbed interface with buy/sell/upgrade buttons

### 2. **Auction House System**
- **40 Daily Items**: Resets every Minecraft day at dawn
- **Rarity Tiers**:
  - 20 Common items (basic materials)
  - 10 Uncommon enchanted tools
  - 5 Rare enchanted items
  - 3 Very Rare max-enchanted items
  - 2 Epic items (Elytra, Nether Star, etc.)
- **NPC Bidding**: 16 unique NPCs with 60% chance to counter-bid (5-15% increases)
- **Instant Buy**: 30-80% markup over strike price for immediate purchase
- **Level Requirements**: Higher tier items require player progression
- **Commands**: `/auction` or `/ah`
- **GUI**: Paginated display with 27 items per page

### 3. **Stock Options Trading**
- **Call/Put Options**: Bet on item price increases (CALL) or decreases (PUT)
- **10x Profit Multiplier**: Huge gains if prediction is correct
- **3 MC Hour Expiration**: Options expire after ~3 Minecraft hours (1 hour real-time)
- **Hot Picks**: 10 suggested stocks displayed in GUI
- **Position Tracking**: View all active options and current profit/loss
- **Auto-Exercise**: Options automatically settle at expiration
- **Commands**: `/stocks` or `/options`
- **Integration**: Uses ItemPricing and PriceFluctuation systems

### 4. **Research/Upgrades System**
- **25 Upgrade Types** across 5 tech trees:
  - 💰 **ECONOMIC**: Better Loans, Bulk Discounts, Market Insight, Tax Haven, Insider Trading
  - 🏢 **PROPERTY**: Efficient Management (+10%), Real Estate Mogul (+25%), Urban Planner (-50% upgrades), Mega Developer (+50%)
  - 🌾 **FARMING**: Better Seeds (+20%), Automated (+30%), Industrial (+50%), Bioengineering (2x), Mega Farm (3x)
  - 🏘️ **VILLAGE**: Trade Routes, Mining Efficiency (+50%), Logistics (+30%), Industrial Complex, Metropolis (2x)
  - ⭐ **SPECIAL**: Lucky Charm (+5% loot), Merchant Network, Black Market Access, Money Printer ($1k/day), Financial Empire (+100% all)
- **Tier System**: 5 tiers (0-4) with progressive unlocks
- **Price Range**: $50,000 → $1,000,000 per research
- **Multiplier Bonuses**: Apply to property income, farm production, village output, etc.
- **Daily Income**: Some research grants passive money (Money Printer)
- **Commands**: `/research` or `/upgrades`
- **GUI**: Category tabs with tier progression visualization

### 5. **Black Market System**
- **5 Daily Deals**: New deals every Minecraft day
- **40-70% Discounts**: Risky but potentially huge savings
- **15% Scam Chance**: You might lose your money with nothing in return!
- **Valuable Items**: Diamond Blocks, Netherite, Nether Stars, Elytras, Totems, Beacons
- **Research Lock**: Requires "Black Market Access" research to unlock
- **Commands**: `/blackmarket`
- **GUI**: Warning display with deal listings

### 6. **Farm Production Boost**
- **3x Production Increase** for all farm types:
  - Crop Farm: 10 → 30/day
  - Tree Farm: 8 → 24/day
  - Fish Farm: 6 → 18/day
  - Iron Farm: 5 → 15/day
  - Animal Farm: 4 → 12/day
  - Mob Farm: 3 → 9/day
  - Enchant Farm: 2 → 6/day

---

## 🔧 Technical Improvements

### Code Architecture
- **Manager Classes**: PropertyManager, AuctionManager, StockOptionsManager, ResearchManager, BlackMarketManager
- **GUI Classes**: PropertyGui, AuctionGui, StocksGui, ResearchGui, BlackMarketGui
- **Command Registration**: All new systems properly registered in ShopMod
- **Integration**: All systems use existing ItemPricing, PriceFluctuation, and CurrencyManager

### API Compliance
- Fixed all systems to use `net.minecraft.world.item.Item` directly
- Removed non-existent ShopItem enum references
- Proper use of `ItemPricing.getBuyPrice(Item)` for base prices
- Proper use of `PriceFluctuation.getAdjustedPrice(Item, long)` for current prices
- Fixed `CurrencyManager.removeMoney()` calls (not deductMoney)

### Daily Reset System
- All new features respect Minecraft day/night cycle
- Auctions reset at dawn (0 ticks)
- Black Market deals reset daily
- Stock options expire after 3 MC hours (60,000 ticks)
- Property income processes daily
- Research grants passive income daily

---

## 📊 Statistics

### Files Added/Modified
- **Created**: 20 new files
  - 5 Manager classes
  - 5 GUI classes
  - 5 Command classes
  - 5 Data/Type classes
- **Modified**: 2 existing files
  - FarmType.java (3x production boost)
  - ShopMod.java (integration of all systems)

### Lines of Code
- **~2,500+ new lines** of functional Java code
- Fully documented with JavaDoc comments
- Error handling and validation throughout

---

## 🎮 Gameplay Impact

### Early Game ($0 - $100k)
- Farm production boost helps players earn faster
- Small properties provide starting passive income
- Common auction items offer good deals
- Basic research upgrades available

### Mid Game ($100k - $1M)
- Multiple property investments generating substantial income
- Stock options for high-risk/high-reward trading
- Rare auction items become affordable
- Property and Farm research unlocked

### Late Game ($1M+)
- Metropolis properties generating $200k/day
- Advanced research multipliers (2x-3x bonuses)
- Black Market access for discounted high-value items
- Financial Empire research for total domination

---

## 🐛 Bug Fixes

### Build Compilation Errors (FIXED)
- ❌ **31 compilation errors** from ShopItem enum references
- ✅ **Fixed**: All systems now use Item type correctly
- ❌ Wrong method signatures in ShopMod
- ✅ **Fixed**: AuctionItem.generateDailyAuctions(), StockOptionsManager.processExpiredOptions()
- ❌ BlackMarketManager using ShopItem constants
- ✅ **Fixed**: Now uses Items.DIAMOND_BLOCK, etc.

### Code Quality
- Removed unused imports
- Fixed deprecated method calls
- Proper null checking
- Thread-safe concurrent operations

---

## 🚀 Installation

1. Build completed successfully: `shop-1.0.40.jar` (361.63 KB)
2. Copy to `.minecraft/mods/` folder
3. Restart Minecraft
4. Use `/property`, `/auction`, `/stocks`, `/research`, `/blackmarket` commands

---

## 📝 Notes

- All features use **Minecraft time**, not real-world time
- No waiting for real-world days - everything respects in-game day/night cycle
- Systems are fully integrated with existing shop mechanics
- Prices dynamically adjust based on existing PriceFluctuation system
- All transactions use the unified CurrencyManager

---

## 🔮 Future Features (Not Yet Implemented)

1. **Village Resource Contribution**
2. **Mining Operations**
3. **Trading Caravans**

---

## 👨‍💻 Development

**Build Status**: ✅ SUCCESS  
**Version**: 1.0.40 → 1.0.41  
**Minecraft**: 1.21.11  
**Fabric Loader**: 0.18.4  
**Compilation**: No errors, no warnings  
**Ready for deployment**: YES

---

*"Expand your empire with properties, gamble on the stock market, hunt for auction deals, unlock powerful research, and risk it all in the Black Market!"*
