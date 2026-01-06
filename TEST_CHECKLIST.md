# 🧪 Shop Mod Testing Checklist

## 🎯 Quick Test List

### 1. New Categories (5 min)
- [ ] Open `/shop` and verify all 17 categories appear
- [ ] Click through: Combat, Potions, Nether, End, Ocean tabs
- [ ] Verify items display in each new category
- [ ] Check that old categories still work (Food, Ores, etc.)

### 2. Bulk Buy System (3 min)
- [ ] Look at bottom-middle slot (hopper icon)
- [ ] Click it to cycle through modes: 1x → 16x → 64x → 128x → Max
- [ ] Set to 16x mode and buy an item (should buy 16 at once)
- [ ] Set to Max mode with low money (should buy as many as possible)
- [ ] Verify cost tooltip updates for each mode

### 3. Selling System (5 min)
**Method 1: Sell Box**
- [ ] Find red glass pane "SELL BOX" in bottom-left
- [ ] Drag an item from inventory to sell box
- [ ] Verify money increases instantly

**Method 2: Right-Click**
- [ ] Right-click any item in your inventory while shop is open
- [ ] Entire stack should sell instantly
- [ ] Money should increase immediately

### 4. Tier Progress Bar (2 min)
- [ ] Go to "Tier System" tab
- [ ] Look for colored progress bar at top
- [ ] Verify it shows green (progress) and red (remaining) blocks
- [ ] Check if percentage and amounts display

### 5. Upgrade Shop (10 min)
- [ ] Go to "Upgrades" tab
- [ ] See all 5 upgrades displayed
- [ ] Click "Income Multiplier" if you can afford it (starts at $100)
- [ ] Upgrade should increase to Level 1
- [ ] Close and reopen shop - level should persist

**Test Income Multiplier:**
- [ ] Break a block (stone, dirt, etc.)
- [ ] Money message should show bonus % if you have levels
- [ ] Example: "+$5 (110%)" if you have 10 levels

**Test Sell Price Boost:**
- [ ] Upgrade Sell Price Boost if you can afford it
- [ ] Sell an item in the shop
- [ ] Should get more money than base price

**Test Mining Speed:**
- [ ] Upgrade Mining Speed
- [ ] Check if you have Haste effect (should see icon)
- [ ] Mine faster than normal

### 6. Persistence (2 min)
- [ ] Purchase at least one upgrade
- [ ] Close shop and reopen - upgrade level still there?
- [ ] Restart the game - upgrades should persist

### 7. Edge Cases (5 min)
- [ ] Try buying with insufficient funds - should show error
- [ ] Try selling non-sellable items - should reject
- [ ] Set buy mode to Max with $0 - should not crash
- [ ] Spam-click upgrade buttons - should not duplicate

---

## ⚡ Priority Tests (If Short on Time)

**Must Test (5 min):**
1. Bulk buy works (try 16x mode)
2. Right-click selling works
3. Upgrade shop opens and shows levels
4. Income multiplier increases money earned

**Nice to Test (10 min):**
1. All new categories display properly
2. Tier progress bar shows correctly
3. Sell box drag-and-drop works
4. Mining speed grants Haste

---

## 🐛 What to Look For

### Expected Issues:
- Haste effect might flicker (reapplies every 10 seconds - normal)
- Sell box might need refinement for edge cases
- Upgrade costs scale quickly (intentional)

### Report if You See:
- ❌ Any crashes or errors
- ❌ Money not updating
- ❌ Upgrades not saving
- ❌ GUI display issues
- ❌ Items not buying/selling

---

## ✅ Success Criteria

**All systems working if:**
- Can buy items in bulk (1/16/64/128/Max)
- Can sell via drag-drop OR right-click
- Can see and purchase upgrades
- Upgrades persist after closing shop
- Income multiplier shows in money messages
- Sell price boost increases sell value

---

**Estimated Total Test Time:** 15-30 minutes
**Critical Features:** Bulk buy, Selling, Upgrades
**Optional:** Daily Deals (not implemented yet)
