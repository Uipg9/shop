# QUICK TEST GUIDE - v1.0.42

## 🧪 Test Each Feature

### 1. Loan Interest Fix
```
/loan
- Take out a $10,000 loan for 30 days
- Check the "Total Interest" display
- Should show 20-50% (not 450%!)
- Daily payment should be reasonable
```

### 2. Middle-Click Property Rental
```
/property
- Buy any property
- Middle-click to rent out
- Middle-click again to evict
- Should not cause double actions
```

### 3. Mine Costs (50% Cheaper)
```
/mining
- Check Coal Mine: $12,500 (was $25,000)
- Check Iron Mine: $37,500 (was $75,000)
- Check Gold Mine: $75,000 (was $150,000)
- All mines should be half price
```

### 4. Teleport Commands
```
/tele           → Opens GUI
/warp           → Opens GUI
/tp 100 64 200  → Teleports to coordinates
/setwaypoint home → Creates home waypoint
/home           → Teleports home
```

### 5. Tenant Management
```
/property → Middle-click to rent property
/tenant   → View tenant management GUI
- See tenant name, relationship score
- Left-click to increase rent (+10%, relationship -5)
- Right-click to decrease rent (-10%, relationship +5)
- Middle-click to evict
- Wait for daily update to see random events
```

### 6. Farm QOL Features
```
/farms
Tab to "Harvest" view:
- Click "Harvest All" button → Sells everything
- Click Auto-Sell toggle → Green = ON
- See statistics: Total Harvested, Earned, Active Farms

Tab to "Management" view:
- Click Fertilizer button → Pay $5k for +50% next harvest
- Active indicator shows if fertilizer is applied
```

### 7. Daily Processing
```
Sleep in bed or wait for Minecraft dawn:
- Tenants will trigger random events (15% chance)
- Auto-sell will process if enabled
- Fertilizer will boost production if active
- Check messages for tenant gifts/damage
```

## 🎯 Expected Results

### Loan Example (30 days, good credit 80 score):
- $10,000 loan
- ~24% total interest = $2,400
- Total to repay: $12,400
- Daily payment: ~$413

### Tenant Events (Random, 15% daily chance):
**Positive:**
- "Tenant gave you $1,200!"
- "Tenant gave you 2 diamonds!"
- "Tenant paid double rent! (+$750)"

**Negative:**
- "Tenant caused damage! Repair: $600"
- "Tenant requested rent reduction. New rent: $675"

### Farm Auto-Sell:
- Resources harvested automatically
- Money added each day
- No need to manually collect

### Fertilizer:
- Purchase: -$5,000
- Next harvest: All farms produce 50% more
- One-time use, consumed after 1 day

## 🐛 What to Check

### Critical:
- ✅ Loans don't have insane interest
- ✅ Properties can be rented/evicted without issues
- ✅ Mines are affordable
- ✅ /teleport doesn't conflict
- ✅ Commands work

### Features:
- ✅ Tenants appear after renting
- ✅ Tenant events happen (wait multiple days)
- ✅ Harvest All collects everything
- ✅ Auto-Sell processes daily
- ✅ Fertilizer boosts production
- ✅ Statistics update correctly

### UI:
- ✅ Tenant button in Hub GUI (slot 20)
- ✅ All new buttons have proper descriptions
- ✅ Hover text is clear
- ✅ Click actions work as described

## 📊 Sample Session

```
Day 1:
/loan → Take $10k loan (daily payment ~$400)
/property → Buy house, middle-click to rent
/farms → Buy Crop Farm
/farms → Enable Auto-Sell
/farms → Buy Fertilizer ($5k)

Day 2:
- Loan payment deducted automatically
- Tenant pays rent (+$750)
- Farm produces resources (auto-sold!)
- Fertilizer boosts production (+50%)

Day 3:
- Check /tenant → See tenant relationship
- Tenant might have event (15% chance)
- Adjust rent if needed
- Farm continues producing

Day 30:
- Final loan payment
- Loan fully paid off!
- Tenant relationship affects events
```

## 🔍 Edge Cases to Test

1. **Rent a property then immediately evict**
   - Should work without errors

2. **Try to rent already-rented property**
   - Should show error message

3. **Harvest All with empty storage**
   - Should show "No resources to harvest!"

4. **Buy fertilizer when already active**
   - Should show "Fertilizer already active!"

5. **Adjust rent multiple times**
   - Relationship should increase/decrease accordingly

6. **Multiple tenants across different properties**
   - Each should track independently

## ✅ Success Criteria

- [x] No crashes or errors
- [x] All commands respond correctly
- [x] Money calculations are accurate
- [x] Daily events process properly
- [x] GUIs display correctly
- [x] Messages are clear and helpful
- [x] Relationships update as expected
- [x] Statistics track properly

---

**If all tests pass:** Ready for release! 🚀
**If any fail:** Check console for errors and report
