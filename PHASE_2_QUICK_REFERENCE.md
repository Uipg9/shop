# Phase 2 Quick Reference Card

## Commands

### Automation Hub
```
/automation  or  /auto       Open automation GUI
/automation toggle <setting>  Toggle specific automation
/automation run              Run all automations now
```

### Insurance Center
```
/insurance  or  /insure           Open insurance GUI
/insurance buy <type>             Purchase policy
/insurance claim <type> <amount>  File claim
/insurance list                   List active policies
```

**Types:** property, farm, mine, business

### Banking (via GUI)
```
/bank    Access all banking features
```

---

## Pricing

### Automation
- **FREE** - Quality of life feature

### Insurance (Monthly)
- Property: $500/month ($100K coverage)
- Farm: $300/month ($50K coverage)
- Mine: $800/month ($150K coverage)
- Business: $1,400/month ($250K coverage - 20% discount)

### Credit Card
- Limit: $50,000
- Interest: 10% monthly
- Late Penalty: +20% (30% total)
- Min Payment: 5% or $100

---

## Automation Features

### Available Automations:
1. ✅ **Auto-Pay Loans** - Never miss payments
2. ✅ **Auto-Collect Farms** - Maximize production
3. ✅ **Auto-Deposit Wallet** - Safe money above threshold
4. 🔜 **Auto-Sell Harvests** - Coming soon
5. 🔜 **Auto-Invest Dividends** - Coming soon

### Settings:
- Deposit threshold adjustable ($1K increments)
- Default: $10,000
- Notifications track last 10 actions
- Statistics reset daily

---

## Banking Accounts

### 4 Account Types:
1. **Checking** - Free transactions, no interest
2. **Savings** - Risky investment returns
3. **Investment** - Stock portfolio holder
4. **Credit Card** - $50K limit, borrow money

### Transaction History:
- Last 100 transactions stored
- 12 transaction types tracked
- Timestamp for each record

---

## Insurance System

### Policy Features:
- Monthly billing (auto-deduction)
- Grace period: 2 missed payments = cancel
- Premium increases 10% per claim (max 200%)
- Fraud detection: 5 claims/month limit
- Instant payouts to checking

### Claim Types:
- Property Damage
- Crop Failure
- Equipment Failure
- Tenant Loss

---

## Hub GUI Locations

**Row 1:** Core shop features
- Slot 12: Bank (EMERALD)

**Row 3:** Upgrades & Systems
- Slot 31: Automation Hub (COMPARATOR) ⭐ NEW
- Slot 32: Insurance (SHIELD) ⭐ NEW

---

## Integration Points

### Daily Processing (Midnight):
- ✅ Automation runs all enabled features
- ✅ Statistics reset
- ✅ Notifications recorded

### Monthly Processing (Every 30 days):
- ✅ Insurance premiums charged
- ✅ Credit card interest applied
- ✅ Grace periods checked

### Cross-System:
- ✅ Insurance claims → Checking account
- ✅ Automation → Records all actions
- ✅ Credit card → Emergency funds
- ✅ Transaction history → Full audit

---

## Color Guide

**In GUIs:**
- §a GREEN = Enabled/Good/Approved
- §c RED = Disabled/Bad/Denied
- §e YELLOW = Neutral/Pending
- §6 GOLD = Money amounts
- §b CYAN = Automation
- §9 BLUE = Insurance

---

## Tips

### Automation:
✓ Enable auto-pay loans to never face penalties
✓ Set deposit threshold above daily spending
✓ Check notifications to verify actions
✓ Use "Run All Now" for manual testing

### Banking:
✓ Keep checking balance for daily transactions
✓ Use savings for risky high returns
✓ Credit card is expensive (10% monthly!)
✓ Check transaction history regularly

### Insurance:
✓ Business insurance = 20% discount
✓ Premium increases with claims
✓ Max 5 claims per month
✓ Keep funds for monthly premiums

---

## Troubleshooting

**Automation not working?**
→ Check toggles are GREEN
→ Verify you have loans/farms
→ Wait until midnight

**Insurance not billing?**
→ Must be 30 days since purchase
→ Need funds in checking/wallet
→ Check active policies tab

**Credit card issues?**
→ Check available credit
→ Remember 10% monthly interest
→ Make payments to reduce balance

---

## File Structure

```
automation/
  ├── AutomationSettings.java
  ├── AutomationNotification.java
  ├── AutomationManager.java
  ├── AutomationGui.java
  └── AutomationCommand.java

insurance/
  ├── InsuranceType.java
  ├── InsurancePolicy.java
  ├── InsuranceClaim.java
  ├── InsuranceManager.java
  ├── InsuranceGui.java
  └── InsuranceCommand.java

bank/
  ├── BankManager.java (expanded)
  ├── AccountType.java (new)
  ├── TransactionRecord.java (new)
  └── CreditCardData.java (new)
```

---

**Version:** 1.0.49
**Phase:** 2 COMPLETE
**Status:** Ready for Testing

🎮 **ENJOY THE NEW FEATURES!** 🎮
