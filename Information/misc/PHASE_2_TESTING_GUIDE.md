# Phase 2 Testing Guide

## Quick Start Testing

### 1. Test Automation Hub
```
/automation
```
- Toggle each setting ON/OFF (green/red wool)
- Adjust deposit threshold using +/- buttons
- Click "Run All Now" to test manual execution
- Check notification center for recent actions

### 2. Test Banking Expansion
```
/bank
```
**Checking Account:**
- Deposit money to checking
- Withdraw from checking
- Transfer between accounts

**Credit Card:**
- Borrow various amounts ($1K, $5K, $10K, MAX)
- Check available credit
- Make payments (minimum, partial, full)
- Wait 30 days to test interest

**Transaction History:**
- View recent transactions
- Verify all actions are recorded

### 3. Test Insurance System
```
/insurance
```
**Purchase Policies:**
- Browse available policies
- Purchase Property Insurance ($500/month)
- Purchase Farm Insurance ($300/month)
- Purchase Mine Insurance ($800/month)
- Purchase Business Insurance ($1,400/month)

**File Claims:**
- File a valid claim (under coverage amount)
- File an invalid claim (over coverage amount)
- Test fraud detection (file 6+ claims in a month)

**Billing:**
- Wait 30 days for monthly billing
- Verify premium charged from checking/wallet
- Test grace period (miss 2 payments)

### 4. Integration Testing

**Daily Processing:**
- Wait for a new Minecraft day
- Check automation ran (if enabled)
- Verify notifications appeared
- Check statistics updated

**Automation Features:**
- Enable "Auto-Pay Loans" - take a loan and watch it auto-pay
- Enable "Auto-Collect Farms" - buy farms and watch them auto-collect
- Enable "Auto-Deposit Wallet" - earn money and watch it auto-deposit

**Cross-System:**
- Insurance claims should go to checking account
- Automation should record notifications
- All transactions should appear in history

## Commands Quick Reference

### Automation
- `/automation` or `/auto` - Open GUI
- `/automation toggle <setting>` - Toggle specific feature
- `/automation run` - Run all automations now

### Insurance
- `/insurance` or `/insure` - Open GUI
- `/insurance buy <type>` - Purchase (property/farm/mine/business)
- `/insurance claim <type> <amount>` - File claim
- `/insurance list` - List active policies

### Banking
- Use `/bank` GUI for all banking operations
- Credit card, checking, savings, investment accounts
- Full transaction history

## Expected Behaviors

### Automation Hub
✅ FREE to use
✅ Notifications appear immediately
✅ Statistics update daily
✅ "Run All Now" executes immediately

### Banking
✅ Credit card charges 10% monthly interest
✅ Late payments add 20% penalty (30% total)
✅ Transaction history shows last 100 transactions
✅ All accounts track separately

### Insurance
✅ Monthly premiums charged automatically
✅ Claims process instantly if valid
✅ Premium increases 10% per claim
✅ Cancellation after 2 missed payments
✅ Max 5 claims per month (fraud protection)

## Known Features

### Automation Benefits
- **Auto-Pay Loans**: Never miss a payment
- **Auto-Collect Farms**: Maximize production
- **Auto-Deposit Wallet**: Safe money management
- **Notifications**: Track all automated actions
- **Statistics**: See daily savings

### Banking Features
- **4 Account Types**: Checking, Savings, Investment, Credit
- **Credit Card**: $50K limit, emergency funds
- **Transaction History**: Full audit trail
- **Easy Transfers**: Move money between accounts

### Insurance Protection
- **Property**: $100K coverage for tenant damage
- **Farm**: $50K coverage for crop failures
- **Mine**: $150K coverage for equipment
- **Business**: $250K all-in-one (20% discount)

## Debug Commands

Check data in logs:
- Look for "[AUTOMATION]" messages
- Look for "[INSURANCE]" messages
- Look for "[BANK]" messages

Verify integration:
- Automation should process daily with other systems
- Insurance should bill monthly (every 30 days)
- Credit interest should charge monthly (every 30 days)

## Troubleshooting

**Automation not working?**
- Check if toggles are green (enabled)
- Verify you have active loans/farms to process
- Check notification center for activity

**Insurance not billing?**
- Must be 30 days since last payment
- Need funds in checking or wallet
- Check active policies tab

**Credit card issues?**
- Check available credit (limit - balance)
- Can't borrow more than available
- Interest charges monthly only

## Success Indicators

✅ All three systems accessible from Hub GUI
✅ No compilation errors
✅ All commands work
✅ Daily/monthly processing integrates smoothly
✅ User-friendly interfaces
✅ Clear error messages

---

**Version:** 1.0.49
**Implementation:** Complete
**Status:** Ready for Testing
