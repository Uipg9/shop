# Changelog

## [v1.0.50] - Phase 3: Worker Management System (January 7, 2026)
### Added - Worker Management System ⭐ NEW!
- **3 Worker Types**:
  - Farm Hands: +25% crop yield on assigned farms
  - Miners: +20% mining income on assigned mines
  - Property Managers: -30% repair costs on properties
  
- **Skills & Training**:
  - 5 trainable skills (Harvesting, Mining, Maintenance, Efficiency, Speed)
  - Levels 1-10 per skill
  - $1,000 per training session, 1-day cooldown between training
  
- **Loyalty System**:
  - 0-100 loyalty scale (+1 when paid, -5 for missed payments)
  - Workers quit if loyalty < 20 (10% chance weekly)
  
- **Economics**:
  - Hiring cost: $5,000 per worker (max 10 workers)
  - Daily salaries: $100-$500 based on total skill level
  - Unassigned workers still cost salary but provide no benefits
  
- **GUI & Commands**:
  - 5 view modes: Overview, Hire, Manage, Training, Assignments
  - Commands: `/workers`, `/worker hire/fire/assign/list/stats`

### Integration
- Farm efficiency boost applied automatically when worker assigned
- Mine income bonus on collection
- Property repair cost reduction
- Daily salary payments in ShopMod
- Weekly loyalty updates

## [v1.0.49] - Phase 2: Automation + Banking + Insurance (January 7, 2026)
### Added - Automation Hub ⭐ NEW!
- 5 automation toggles (loans/farms/deposit/sell/invest)
- Notification center (last 10 actions)
- Daily statistics tracking
- Configurable deposit threshold
- Manual "Run All Now" trigger

### Added - Banking Expansion
- 4 account types (Checking/Savings/Investment/Credit)
- Credit card system ($50K limit, 10% monthly interest)
- Transaction history (last 100 records)
- Multi-account overview GUI

### Added - Insurance System ⭐ NEW!
- 4 policy types (Property/Farm/Mine/Business)
- Claims system with fraud detection
- Premium adjustments (10% increase per claim, max 200%)
- 2-payment grace period before cancellation

## [v1.0.48] - Phase 1: Stock Market + Interactive Games (January 6, 2026)
### Added - Stock Market Trading ⭐ NEW!
- 17 fictional companies across 7 industries
- Realistic price fluctuations with trends and events
- Portfolio management with gains/losses tracking
- Dividend payments every 7 days (0-5% quarterly)
- 4-view GUI (Market/Trading/Portfolio/News)

### Added - Interactive Games V2.0 ⭐ REDESIGNED!
- Enhanced 5 existing games (Number Guess, Coin Flip, Dice Roll, High-Low, Slots)
- Added Blackjack with dealer AI and proper rules
- Added Roulette (European wheel) with 8 bet types
- Dedicated lobby and individual game screens
- Session persistence for multi-round games

## [Unreleased] - v1.0.26
### Added
- **🌐 Village Web Dashboard**: Revolutionary localhost web interface for village management
  - Access villages at `http://localhost:8080` from any web browser
  - Real-time village overview with level, worker count, daily profit, total value
  - Complete worker management: hire, fire, upgrade all worker types
  - Resource management: view stocks, transfer to trade center, bulk selling
  - Building control: construct buildings directly from web interface
  - Auto-manage toggle with live efficiency display (100% manual, 70% auto)
  - Responsive design works on desktop and mobile devices
  - Auto-refresh every 30 seconds + manual refresh button
  - Similar to minecraft-web-chat but specifically for village system
  - Comprehensive API endpoints for potential integrations
  - Full documentation in `Information/VILLAGE_WEB_DASHBOARD_GUIDE.txt`

### Technical
- Added Javalin 5.6.3 web server framework
- Added Java-WebSocket 1.5.4 for future real-time updates
- Added Jackson 2.15.2 for JSON processing
- Added SLF4J 2.0.7 for proper logging
- Integrated web server lifecycle with Minecraft server start/stop
- CORS configuration for local development
- RESTful API design following best practices

All notable changes to the Shop Mod will be documented in this file.

## [1.0.25] - 2025-01-06

### MASSIVE VILLAGE SYSTEM OVERHAUL

Complete redesign with interconnected resource economy, supply chains, and building mechanics.

### Added
- **Resource Economy**: 10 resource types (Food, Wood, Fish, Ore, Leather, Wool, Tools, Armor, Enchanted, Rare)
- **Worker Supply Chains**: 8 workers with input/output mechanics across 4 tiers
- **Building System**: 10 buildings with daily upkeep (House, Granary, Warehouse, Workshop, Market, etc.)
- **Housing System**: Build unlimited houses for 100+ worker capacity
- **Food & Strikes**: Workers need 1 food/day or go on strike
- **Village Progression**: 4 levels (Hamlet → Village → Town → City)
- **Auto-Manage Mode**: Toggle 70% efficiency auto vs 100% manual
- **Trade Center**: Separate storage with auto-sell functionality
- **Enhanced GUI**: 5 tabs including new comprehensive Guide tab
- **Enhanced /bal**: Shows village economics (salaries, income, profit)

### Changed
- VillageManager completely rewritten for complex economy
- Daily processing now includes village production and trade center auto-sell

## [1.0.24] - 2024-12-31
- Simple village system with passive income from workers
- Worker hire/fire/upgrade mechanics
- Daily salary payments

## [1.0.23] - Previous
- Bank space upgrades, bank returns, enhanced loans, /shop balance command

## [1.0.22] - Previous
- Loan system with daily payments and credit scores

## [1.0.21] - Previous
- Bank system with risky investments

## [1.0.20] - Previous
- Night vision toggle, teleportation system (/sethome, /home)

## [1.0.19] - Previous
- Tree harvester feature (auto-chop logs)

## [1.0.18] - Previous
- Bulk discount system (10+: 5% off, 50+: 10% off, 100+: 15% off)

## [1.0.17] - Previous
- Wallet interest system (10% daily)

## [1.0.15] - Previous
- 300+ shop items added across 5 tiers

## [1.0.10] - Initial Release
- Basic shop system with currency management
