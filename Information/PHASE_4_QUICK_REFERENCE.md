# Phase 4 Quick Reference - v1.0.51

## Files Created (6 total)

### Lottery System (3 files)
1. `src/main/java/com/shopmod/lottery/LotteryManager.java`
2. `src/main/java/com/shopmod/lottery/LotteryGui.java`
3. `src/main/java/com/shopmod/lottery/LotteryCommand.java`

### Business System (3 files)
4. `src/main/java/com/shopmod/business/BusinessManager.java`
5. `src/main/java/com/shopmod/business/BusinessGui.java`
6. `src/main/java/com/shopmod/business/BusinessCommand.java`

---

## Files Modified (9 total)

### Games System (2 files)
1. `src/main/java/com/shopmod/games/GamesManager.java`
   - Extended GameType enum: CRASH, WHEEL_OF_FORTUNE, KENO, MINES, PLINKO
   - Added state classes: CrashState, KenoState, MinesState, PlinkoState
   - Added game methods: startCrash, cashOutCrash, spinWheel, startKeno, startMines, revealMineTile, cashOutMines, dropPlinko

2. `src/main/java/com/shopmod/games/GamesGui.java`
   - Updated lobby to show all 12 games (slots 30-34 for new games)
   - Added display methods: showCrash, showWheelOfFortune, showKeno, showMines, showPlinko

### Manager Enhancements (5 files)
3. `src/main/java/com/shopmod/farm/FarmManager.java`
   - Added: getUpgradeCost(UUID farmId) - Returns $100K
   - Added: applyIrrigationUpgrade(player, farmId) - +20% yield

4. `src/main/java/com/shopmod/mining/MiningManager.java`
   - Added: getMineDepth(UUID mineId) - Track depth
   - Added: upgradeMineDepth(player, mineId) - $75K upgrade

5. `src/main/java/com/shopmod/property/PropertyManager.java`
   - Added: renovateProperty(player, propertyId) - $50K, +10% value
   - Added: getRenovationLevel(UUID propertyId) - Max 5 renovations

6. `src/main/java/com/shopmod/stocks/StockMarketManager.java`
   - Added: initiateIPO() - Monthly new company launches
   - Added: performStockSplit(companyName) - 2:1 split for >$500 stocks
   - Added: getMarketSentiment() - BULLISH/NEUTRAL/BEARISH

7. `src/main/java/com/shopmod/worker/WorkerManager.java`
   - Added: promoteWorker(player, workerId) - $25K, +1 all skills
   - Added: getWorkerPromotion(UUID workerId) - Track promotions

### Integration (2 files)
8. `src/main/java/com/shopmod/ShopMod.java`
   - Imports: LotteryManager, LotteryCommand, BusinessManager, BusinessCommand
   - Daily: BusinessManager.processDailyIncome()
   - Weekly: LotteryManager.processWeeklyDraw()
   - Commands: LotteryCommand.register(), BusinessCommand.register()

9. `src/main/java/com/shopmod/gui/HubGui.java`
   - Slot 41: Updated Games button (12 games total)
   - Slot 42: NEW Lottery button (Enchanted Golden Apple, glow)
   - Slot 43: NEW Business button (Emerald Block)

---

## Quick Feature Summary

### Lottery ($10K tickets, 6 numbers 1-50)
- Commands: `/lottery`, `/lottery buy`, `/lottery info`
- Prize Tiers: 6=JACKPOT, 5=$50K, 4=$10K, 3=$1K
- Weekly draws (every 7 days)
- 4 GUI modes: BUY, MY_TICKETS, LAST_DRAW, JACKPOT_INFO

### Business (7 types, $500K-$800K)
- Commands: `/business`, `/business buy <type>`, `/business collect`, `/business list`
- Daily income: $5K-$9K base (1.5x per upgrade level)
- Synergy bonuses: 3+=20%, 5+=50%, 7=100%
- Upgrade: 5 levels (2x cost each)
- Sell: 60% return

### New Games (5 total)
1. **Crash**: 1.00x-50x multiplier, cash out before crash
2. **Wheel of Fortune**: 8 segments, 0.5x-50x prizes
3. **Keno**: Pick 10/80, 20 drawn, 5-10 matches win
4. **Mines**: 5x5 grid, 5 mines, reveal tiles to win
5. **Plinko**: Drop ball, 11 slots, physics-based

### Enhancement Costs
- Farm Irrigation: $100K (+20% yield)
- Mine Depth: $75K (better ores)
- Property Renovation: $50K (+10% value, max 5)
- Stock IPO: Monthly (new companies)
- Worker Promotion: $25K (+1 all skills)

---

## Build & Test

### Compile
```bash
./gradlew build
```

### Run
```bash
./gradlew runClient
```

### Test Commands
```
/lottery
/business
/game
/hub
```

---

## Important Notes

✅ All features fully integrated  
✅ Daily/weekly processing active  
✅ CurrencyManager used throughout  
✅ Thread-safe ConcurrentHashMap storage  
✅ 9x6 GUI pattern maintained  
✅ HubGui buttons added  

⚠️ Requires testing before production  
⚠️ Data persistence not yet implemented  
⚠️ Consider backup before deployment  

---

## Status: ✅ COMPLETE

All Phase 4 features (v1.0.50 → v1.0.51) have been implemented and are ready for testing.

**Total**: 6 new files, 9 modified files, 3,500+ lines of code
