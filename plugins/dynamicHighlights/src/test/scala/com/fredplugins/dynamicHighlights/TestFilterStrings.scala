package com.fredplugins.dynamicHighlights

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.jdk.StreamConverters.*
import scala.util.chaining.*
import scala.util.{Random, Try}

import scala.compiletime.uninitialized

object TestFilterStrings {
	val preabmle: String = {
		"""#define     RED   "ffff0000"
			|#define GREEN "ff00ff00"
			|#define BLUE "ff0000ff"
			|#define CYAN "ff00ffff"
			|#define MAGENTA "ffff00ff"
			|#define YELLOW "ffffff00"
			|#define WHITE "ffffffff"
			|#define BLACK "ff000000"
			|
			|#define BRONZE "ff5b462a"
			|#define BLURITE "ff8f90d8"
			|#define IRON "ff635c5b"
			|#define ELEMENTAL "ff70119f"
			|#define SILVER "ffa8a8b4"
			|#define STEEL "ff908686"
			|#define GOLD "ffd8b01a"
			|#define LOVAKITE "ff641509"
			|#define MITHRIL "ff4c4c70"
			|#define LUNAR "ff727b72"
			|#define ADAMANT "ff506450"
			|#define RUNITE "ff516d78"
			|
			|#define GROUNDITEMS_INSANE "ffff66b2"
			|#define GROUNDITEMS_HIGH "ffff9600"
			|#define GROUNDITEMS_MEDIUM "ff99ff99"
			|#define GROUNDITEMS_LOW "ff66b2ff"
			|
			|#define HIGHLIGHT(_name, _color) if (name:_name) { color = _color; }
			|#define HIDE(_name) if (name:_name) { hidden = true; }
			|
			|#define RARE(_name, _color) if (name:_name) { color = _color; borderColor = _color; }
			|#define RARE2(_name, _color) if (name:_name) { color = _color; borderColor = _color; backgroundColor = "80000000"; showLootbeam = true; }
			|
			|#define TEXTACCENT_SHADOW  1
			|#define TEXTACCENT_OUTLINE 2
			|#define TEXTACCENT_NONE    3
			|
			|#define FONTTYPE_NORMAL 1
			|#define FONTTYPE_LARGER 2
			|#define FONTTYPE_BOLD   3
			|
			|#define OWNERSHIP_NONE  0
			|#define OWNERSHIP_SELF  1
			|#define OWNERSHIP_OTHER 2
			|#define OWNERSHIP_GROUP 3
			|
			|""".stripMargin
	}


	val realFilter: String  = {
			"""meta { name = "Fred's filter"; description = "Offers an original color scheme with extensive support for category-based styling/filtering."; }
				|
				|#define VAR_GLOBAL_CUSTOMSTYLE textColor = "#FFFFFFFF";showDespawn = true;icon = CurrentItem();
				|
				|apply (true) {
				|    VAR_GLOBAL_CUSTOMSTYLE
				|}
				|
				|
				|#define VAR_DEFAULT_STACKABLE_NOTED_CUSTOMSTYLE
				|
				|apply (stackable:true || noted:true) {
				|    VAR_DEFAULT_STACKABLE_NOTED_CUSTOMSTYLE
				|}
				|
				|#define VAR_DEFAULT_UNTRADEABLE_CUSTOMSTYLE
				|
				|apply (tradeable:false && !name:CONST_GLOBAL_TRADEABLE_NON_TRADEABLE_ITEMS) {
				|    VAR_DEFAULT_UNTRADEABLE_CUSTOMSTYLE
				|}
				|
				|#define VAR_GLOBAL_SHOW_TRADEABLE_VALUE true
				|
				|apply ((tradeable:true || name:CONST_GLOBAL_TRADEABLE_NON_TRADEABLE_ITEMS) && VAR_GLOBAL_SHOW_TRADEABLE_VALUE) {
				|    showValue = true;
				|}
				|
				|#define VAR_GLOBAL_SHOW_UNTRADEABLE_VALUE true
				|
				|apply (tradeable:false && VAR_GLOBAL_SHOW_UNTRADEABLE_VALUE) {
				|    showValue = true;
				|}
				|
				|#define VAR_GLOBAL_DESPAWN_TIMER_MINIMUM_VALUE 0
				|
				|apply ((tradeable:true || name:CONST_GLOBAL_TRADEABLE_NON_TRADEABLE_ITEMS) && value:<VAR_GLOBAL_DESPAWN_TIMER_MINIMUM_VALUE) {
				|    showDespawn = false;
				|}
				|
				|#define VAR_GLOBAL_SHOW_PRICES_MINIMUM_VALUE 5000
				|
				|apply ((tradeable:true || name:CONST_GLOBAL_TRADEABLE_NON_TRADEABLE_ITEMS) && value:<VAR_GLOBAL_SHOW_PRICES_MINIMUM_VALUE) {
				|    showValue = false;
				|}
				|
				|#define VAR_GLOBAL_ICON_MINIMUM_VALUE 0
				|
				|apply ((tradeable:true || name:CONST_GLOBAL_TRADEABLE_NON_TRADEABLE_ITEMS) && value:<VAR_GLOBAL_ICON_MINIMUM_VALUE) {
				|    // this is a hack to turn off the icon
				|    icon = Sprite(9000000, 0);
				|}
				|/*@ define:input:filtering
				|type: number
				|label: Hide stackable items below this value
				|group: Value-Based
				|*/
				|#define VAR_GLOBAL_STACKABLE_MINIMUM_VALUE 100
				|
				|apply ((tradeable:true || name:CONST_GLOBAL_TRADEABLE_NON_TRADEABLE_ITEMS) && (stackable:true || noted:true) && value:<VAR_GLOBAL_STACKABLE_MINIMUM_VALUE) {
				|    hidden = true;
				|}
				|
				|/*@ define:input:filtering
				|type: number
				|label: Hide non-stackable items below this value
				|group: Value-Based
				|*/
				|#define VAR_GLOBAL_NON_STACKABLE_MINIMUM_VALUE 0
				|
				|apply ((tradeable:true || name:CONST_GLOBAL_TRADEABLE_NON_TRADEABLE_ITEMS) && stackable:false && noted:false && value:<VAR_GLOBAL_NON_STACKABLE_MINIMUM_VALUE) {
				|    hidden = true;
				|}
				|
				|
				|/*@ define:input:filtering
				|type: boolean
				|label: Hide others' drops
				|group: Ownership
				|*/
				|#define VAR_GLOBAL_HIDE_OTHER_PLAYERS_ITEMS true
				|
				|/*@ define:input:filtering
				|type: boolean
				|label: Hide world spawns
				|group: Ownership
				|*/
				|#define VAR_GLOBAL_HIDE_SPAWN_ITEMS false
				|
				|/*@ define:input:filtering
				|type: stringlist
				|label: Exceptions to ownership
				|group: Ownership
				|*/
				|#define VAR_OWNERSHIP_FILTERING_EXCEPTIONS ["Water container","Dawnbringer","Elder*","Twisted*","Kodai*","Revitalisation*","Prayer enhance*","Xeric's aid*","Antipoison*","Overload*","Golpar seed","Buchu seed","Noxifer seed","Endarkened juice","Stinkhorn mushroom","Cicely","Mallignum root plank"]
				|
				|rule (VAR_GLOBAL_HIDE_SPAWN_ITEMS && ownership:OWNERSHIP_NONE && !name:VAR_OWNERSHIP_FILTERING_EXCEPTIONS) {
				|    hidden = true;
				|    sound = 2411; // sound name: silence
				|    notify = false;
				|}
				|
				|rule (VAR_GLOBAL_HIDE_OTHER_PLAYERS_ITEMS && ownership:OWNERSHIP_OTHER && !name:VAR_OWNERSHIP_FILTERING_EXCEPTIONS) {
				|    hidden = true;
				|    sound = 2411; // sound name: silence
				|    notify = false;
				|}
				|
				|
				|/*@ define:input:filtering
				|type: enumlist
				|label: Clue tiers to force hidden
				|group: Clue Scrolls
				|enum: [
				|  {
				|    "value": "Clue scroll (beginner)",
				|    "label": "Beginner"
				|  },
				|  {
				|    "value": "Clue scroll (easy)",
				|    "label": "Easy"
				|  },
				|  {
				|    "value": "Clue scroll (medium)",
				|    "label": "Medium"
				|  },
				|  {
				|    "value": "Clue scroll (hard)",
				|    "label": "Hard"
				|  },
				|  {
				|    "value": "Clue scroll (elite)",
				|    "label": "Elite"
				|  },
				|  {
				|    "value": "Clue scroll (master)",
				|    "label": "Master"
				|  }
				|]
				|*/
				|#define VAR_CLUE_SCROLLS_HIDE []
				|
				|apply (name:VAR_CLUE_SCROLLS_HIDE) {
				|    hidden = true;
				|}
				|
				|
				|/*@ define:input:remains
				|type: number
				|label: Minimum value to display remains at
				|group: Remains
				|*/
				|#define VAR_REMAINS_MINIMUM_VALUE 0
				|
				|apply (name:CONST_BONES_LIST && value:<VAR_REMAINS_MINIMUM_VALUE) {
				|    hidden = true;
				|}
				|
				|apply (name:CONST_ASHES_LIST && value:<VAR_REMAINS_MINIMUM_VALUE) {
				|    hidden = true;
				|}
				|
				|apply (name:CONST_SHADES_REMAINS_LIST && value:<VAR_REMAINS_MINIMUM_VALUE) {
				|    hidden = true;
				|}
				|
				|apply (name:CONST_ENSOULED_HEAD_LIST && value:>=VAR_REMAINS_MINIMUM_VALUE) {
				|    hidden = false;
				|}
				|
				|/*@ define:input:remains
				|type: enumlist
				|label: Bones to force hidden
				|group: Remains
				|enum: [
				|  "Bones",
				|  "Wolf bones",
				|  "Burnt bones",
				|  "Monkey bones",
				|  "Bat bones",
				|  "Big bones",
				|  "Jogre bones",
				|  "Wyrmling bones",
				|  "Zogre bones",
				|  "Shaikahan bones",
				|  "Babydragon bones",
				|  "Wyrm bones",
				|  "Wyvern bones",
				|  "Dragon bones",
				|  "Drake bones",
				|  "Fayrg bones",
				|  "Lava dragon bones",
				|  "Raurg bones",
				|  "Hydra bones",
				|  "Dagannoth bones",
				|  "Ourg bones",
				|  "Superior dragon bones",
				|  "Bleached bones",
				|  "Small zombie monkey bones",
				|  "Large zombie monkey bones",
				|  "Small ninja monkey bones",
				|  "Medium ninja monkey bones",
				|  "Gorilla bones",
				|  "Bearded gorilla bones"
				|]
				|*/
				|#define VAR_BONES_HIDE ["Bones","Wolf bones","Burnt bones","Monkey bones","Bat bones","Big bones","Jogre bones","Wyrmling bones","Zogre bones"]
				|
				|apply (name:VAR_BONES_HIDE) {
				|    hidden = true;
				|}
				|
				|/*@ define:input:remains
				|type: enumlist
				|label: Ashes to force hidden
				|group: Remains
				|enum: [
				|  "Fiendish ashes",
				|  "Vile ashes",
				|  "Malicious ashes",
				|  "Abyssal ashes",
				|  "Infernal ashes"
				|]
				|*/
				|#define VAR_ASHES_HIDE ["Fiendish ashes","Vile ashes"]
				|
				|apply (name:VAR_ASHES_HIDE) {
				|    hidden = true;
				|}
				|
				|/*@ define:input:remains
				|type: enumlist
				|label: Shades of morton remains to force hidden
				|group: Remains
				|enum: [
				|  "Loar remains",
				|  "Phrin remains",
				|  "Riyl remains",
				|  "Asyn remains",
				|  "Fiyr remains",
				|  "Urium remains"
				|]
				|*/
				|#define VAR_SHADES_REMAINS_HIDE []
				|
				|apply (name:VAR_SHADES_REMAINS_HIDE) {
				|    hidden = true;
				|}
				|
				|/*@ define:input:food
				|type: stringlist
				|label: Food to hide
				|group: Food
				|*/
				|#define VAR_FOOD_HIDE ["Cooked wild kebbit","Cooked larupia","Cooked barb-tailed kebbit","Cooked graahk","Shrimps","Sardine","Herring","Anchovies","Mackerel","Trout","Cod","Pike","Salmon","Tuna"]
				|
				|apply (name:VAR_FOOD_HIDE) {
				|    hidden = true;
				|}
				|
				|/*@ define:input:food
				|type: stringlist
				|label: Food to show
				|group: Food
				|*/
				|#define VAR_FOOD_SHOW ["Cooked sunlight antelope","Cooked dashing kebbit","Cooked moonlight antelope","Swordfish","Lava eel","Monkfish","Cooked karambwan","Shark","Sea turtle","Manta ray","Anglerfish","Dark crab","Pineapple pizza","1/2 pineapple pizza","Summer pie","Half a summer pie","Wild pie","Half a wild pie","Tuna potato","Mushroom potato"]
				|
				|apply (name:VAR_FOOD_SHOW) {
				|    hidden = false;
				|}
				|
				|
				|/*@ define:input:potions
				|type: enumlist
				|label: Potions to force hidden
				|group: Potions
				|enum: [
				|  {
				|    "value": "Attack potion(*",
				|    "label": "Attack potion"
				|  },
				|  {
				|    "value": "Antipoison(*",
				|    "label": "Antipoison"
				|  },
				|  {
				|    "value": "Strength potion(*",
				|    "label": "Strength potion"
				|  },
				|  {
				|    "value": "Serum 207(*",
				|    "label": "Serum 207"
				|  },
				|  {
				|    "value": "Compost potion(*",
				|    "label": "Compost potion"
				|  },
				|  {
				|    "value": "Restore potion(*",
				|    "label": "Restore potion"
				|  },
				|  {
				|    "value": "Energy potion(*",
				|    "label": "Energy potion"
				|  },
				|  {
				|    "value": "Defence potion(*",
				|    "label": "Defence potion"
				|  },
				|  {
				|    "value": "Agility potion(*",
				|    "label": "Agility potion"
				|  },
				|  {
				|    "value": "Combat potion(*",
				|    "label": "Combat potion"
				|  },
				|  {
				|    "value": "Prayer potion(*",
				|    "label": "Prayer potion"
				|  },
				|  {
				|    "value": "Super attack(*",
				|    "label": "Super attack"
				|  },
				|  {
				|    "value": "Superantipoison(*",
				|    "label": "Superantipoison"
				|  },
				|  {
				|    "value": "Fishing potion(*",
				|    "label": "Fishing potion"
				|  },
				|  {
				|    "value": "Super energy(*",
				|    "label": "Super energy"
				|  },
				|  {
				|    "value": "Hunter potion(*",
				|    "label": "Hunter potion"
				|  },
				|  {
				|    "value": "Goading potion(*",
				|    "label": "Goading potion"
				|  },
				|  {
				|    "value": "Super strength(*",
				|    "label": "Super strength"
				|  },
				|  {
				|    "value": "Prayer regeneration potion(*",
				|    "label": "Prayer regeneration potion"
				|  },
				|  {
				|    "value": "Super restore(*",
				|    "label": "Super restore"
				|  },
				|  {
				|    "value": "Sanfew serum(*",
				|    "label": "Sanfew serum"
				|  },
				|  {
				|    "value": "Super defence(*",
				|    "label": "Super defence"
				|  },
				|  {
				|    "value": "Antidote+(*",
				|    "label": "Antidote+"
				|  },
				|  {
				|    "value": "Antifire potion(*",
				|    "label": "Antifire potion"
				|  },
				|  {
				|    "value": "Divine super attack potion(*",
				|    "label": "Divine super attack potion"
				|  },
				|  {
				|    "value": "Divine super defence potion(*",
				|    "label": "Divine super defence potion"
				|  },
				|  {
				|    "value": "Divine super strength potion(*",
				|    "label": "Divine super strength potion"
				|  },
				|  {
				|    "value": "Ranging potion(*",
				|    "label": "Ranging potion"
				|  },
				|  {
				|    "value": "Divine ranging potion(*",
				|    "label": "Divine ranging potion"
				|  },
				|  {
				|    "value": "Magic potion(*",
				|    "label": "Magic potion"
				|  },
				|  {
				|    "value": "Stamina potion(*",
				|    "label": "Stamina potion"
				|  },
				|  {
				|    "value": "Zamorak brew(*",
				|    "label": "Zamorak brew"
				|  },
				|  {
				|    "value": "Divine magic potion(*",
				|    "label": "Divine magic potion"
				|  },
				|  {
				|    "value": "Antidote++(*",
				|    "label": "Antidote++"
				|  },
				|  {
				|    "value": "Bastion potion(*",
				|    "label": "Bastion potion"
				|  },
				|  {
				|    "value": "Battlemage potion(*",
				|    "label": "Battlemage potion"
				|  },
				|  {
				|    "value": "Saradomin brew(*",
				|    "label": "Saradomin brew"
				|  },
				|  {
				|    "value": "Extended antifire(*",
				|    "label": "Extended antifire"
				|  },
				|  {
				|    "value": "Ancient brew(*",
				|    "label": "Ancient brew"
				|  },
				|  {
				|    "value": "Divine bastion potion(*",
				|    "label": "Divine bastion potion"
				|  },
				|  {
				|    "value": "Divine battlemage potion(*",
				|    "label": "Divine battlemage potion"
				|  },
				|  {
				|    "value": "Anti-venom(*",
				|    "label": "Anti-venom"
				|  },
				|  {
				|    "value": "Menaphite remedy(*",
				|    "label": "Menaphite remedy"
				|  },
				|  {
				|    "value": "Super combat potion(*",
				|    "label": "Super combat potion"
				|  },
				|  {
				|    "value": "Forgotten brew(*",
				|    "label": "Forgotten brew"
				|  },
				|  {
				|    "value": "Super antifire potion(*",
				|    "label": "Super antifire potion"
				|  },
				|  {
				|    "value": "Anti-venom+(*",
				|    "label": "Anti-venom+"
				|  },
				|  {
				|    "value": "Extended anti-venom+(*",
				|    "label": "Extended anti-venom+"
				|  },
				|  {
				|    "value": "Divine super combat potion(*",
				|    "label": "Divine super combat potion"
				|  },
				|  {
				|    "value": "Extended super antifire(*",
				|    "label": "Extended super antifire"
				|  },
				|  {
				|    "value": "Surge potion(*",
				|    "label": "Surge potion"
				|  }
				|]
				|*/
				|#define VAR_POTIONS_HIDE ["Attack potion(*","Strength potion(*","Serum 207(*","Compost potion(*","Restore potion(*","Energy potion(*","Defence potion(*","Agility potion(*","Combat potion(*","Fishing potion(*","Hunter potion(*","Antidote+(*"]
				|
				|apply (name:VAR_POTIONS_HIDE) {
				|    hidden = true;
				|}""".stripMargin
	}
				
	val realFilterP2: String = {
			"""/*@ define:input:potions
				|type: enumlist
				|label: Potions to force shown
				|group: Potions
				|enum: [
				|  {
				|    "value": "Attack potion(*",
				|    "label": "Attack potion"
				|  },
				|  {
				|    "value": "Antipoison(*",
				|    "label": "Antipoison"
				|  },
				|  {
				|    "value": "Strength potion(*",
				|    "label": "Strength potion"
				|  },
				|  {
				|    "value": "Serum 207(*",
				|    "label": "Serum 207"
				|  },
				|  {
				|    "value": "Compost potion(*",
				|    "label": "Compost potion"
				|  },
				|  {
				|    "value": "Restore potion(*",
				|    "label": "Restore potion"
				|  },
				|  {
				|    "value": "Energy potion(*",
				|    "label": "Energy potion"
				|  },
				|  {
				|    "value": "Defence potion(*",
				|    "label": "Defence potion"
				|  },
				|  {
				|    "value": "Agility potion(*",
				|    "label": "Agility potion"
				|  },
				|  {
				|    "value": "Combat potion(*",
				|    "label": "Combat potion"
				|  },
				|  {
				|    "value": "Prayer potion(*",
				|    "label": "Prayer potion"
				|  },
				|  {
				|    "value": "Super attack(*",
				|    "label": "Super attack"
				|  },
				|  {
				|    "value": "Superantipoison(*",
				|    "label": "Superantipoison"
				|  },
				|  {
				|    "value": "Fishing potion(*",
				|    "label": "Fishing potion"
				|  },
				|  {
				|    "value": "Super energy(*",
				|    "label": "Super energy"
				|  },
				|  {
				|    "value": "Hunter potion(*",
				|    "label": "Hunter potion"
				|  },
				|  {
				|    "value": "Goading potion(*",
				|    "label": "Goading potion"
				|  },
				|  {
				|    "value": "Super strength(*",
				|    "label": "Super strength"
				|  },
				|  {
				|    "value": "Prayer regeneration potion(*",
				|    "label": "Prayer regeneration potion"
				|  },
				|  {
				|    "value": "Super restore(*",
				|    "label": "Super restore"
				|  },
				|  {
				|    "value": "Sanfew serum(*",
				|    "label": "Sanfew serum"
				|  },
				|  {
				|    "value": "Super defence(*",
				|    "label": "Super defence"
				|  },
				|  {
				|    "value": "Antidote+(*",
				|    "label": "Antidote+"
				|  },
				|  {
				|    "value": "Antifire potion(*",
				|    "label": "Antifire potion"
				|  },
				|  {
				|    "value": "Divine super attack potion(*",
				|    "label": "Divine super attack potion"
				|  },
				|  {
				|    "value": "Divine super defence potion(*",
				|    "label": "Divine super defence potion"
				|  },
				|  {
				|    "value": "Divine super strength potion(*",
				|    "label": "Divine super strength potion"
				|  },
				|  {
				|    "value": "Ranging potion(*",
				|    "label": "Ranging potion"
				|  },
				|  {
				|    "value": "Divine ranging potion(*",
				|    "label": "Divine ranging potion"
				|  },
				|  {
				|    "value": "Magic potion(*",
				|    "label": "Magic potion"
				|  },
				|  {
				|    "value": "Stamina potion(*",
				|    "label": "Stamina potion"
				|  },
				|  {
				|    "value": "Zamorak brew(*",
				|    "label": "Zamorak brew"
				|  },
				|  {
				|    "value": "Divine magic potion(*",
				|    "label": "Divine magic potion"
				|  },
				|  {
				|    "value": "Antidote++(*",
				|    "label": "Antidote++"
				|  },
				|  {
				|    "value": "Bastion potion(*",
				|    "label": "Bastion potion"
				|  },
				|  {
				|    "value": "Battlemage potion(*",
				|    "label": "Battlemage potion"
				|  },
				|  {
				|    "value": "Saradomin brew(*",
				|    "label": "Saradomin brew"
				|  },
				|  {
				|    "value": "Extended antifire(*",
				|    "label": "Extended antifire"
				|  },
				|  {
				|    "value": "Ancient brew(*",
				|    "label": "Ancient brew"
				|  },
				|  {
				|    "value": "Divine bastion potion(*",
				|    "label": "Divine bastion potion"
				|  },
				|  {
				|    "value": "Divine battlemage potion(*",
				|    "label": "Divine battlemage potion"
				|  },
				|  {
				|    "value": "Anti-venom(*",
				|    "label": "Anti-venom"
				|  },
				|  {
				|    "value": "Menaphite remedy(*",
				|    "label": "Menaphite remedy"
				|  },
				|  {
				|    "value": "Super combat potion(*",
				|    "label": "Super combat potion"
				|  },
				|  {
				|    "value": "Forgotten brew(*",
				|    "label": "Forgotten brew"
				|  },
				|  {
				|    "value": "Super antifire potion(*",
				|    "label": "Super antifire potion"
				|  },
				|  {
				|    "value": "Anti-venom+(*",
				|    "label": "Anti-venom+"
				|  },
				|  {
				|    "value": "Extended anti-venom+(*",
				|    "label": "Extended anti-venom+"
				|  },
				|  {
				|    "value": "Divine super combat potion(*",
				|    "label": "Divine super combat potion"
				|  },
				|  {
				|    "value": "Extended super antifire(*",
				|    "label": "Extended super antifire"
				|  },
				|  {
				|    "value": "Surge potion(*",
				|    "label": "Surge potion"
				|  }
				|]
				|*/
				|#define VAR_POTIONS_SHOW ["Anti-venom(*","Anti-venom+(*","Antidote++(*","Antifire potion(*","Bastion potion(*","Battlemage potion(*","Divine bastion potion(*","Divine battlemage potion(*","Divine magic potion(*","Divine ranging potion(*","Divine super attack potion(*","Divine super combat potion(*","Divine super defence potion(*","Divine super strength potion(*","Extended anti-venom+(*","Extended antifire(*","Extended super antifire(*","Forgotten brew(*","Goading potion(*","Menaphite remedy(*","Prayer regeneration potion(*","Ranging potion(*","Sanfew serum(*","Saradomin brew(*","Stamina potion(*","Super antifire potion(*","Super combat potion(*","Super restore(*","Surge potion(*"]
				|
				|apply (name:VAR_POTIONS_SHOW) {
				|    hidden = false;
				|}
				|
				|
				|/*@ define:input:ammo
				|type: enumlist
				|label: Standard arrows to force hidden
				|group: Ammo
				|enum: [
				|  {
				|    "value": "Bronze*",
				|    "label": "Bronze Arrows"
				|  },
				|  {
				|    "value": "Iron*",
				|    "label": "Iron Arrows"
				|  },
				|  {
				|    "value": "Steel*",
				|    "label": "Steel Arrows"
				|  },
				|  {
				|    "value": "Mithril*",
				|    "label": "Mithril Arrows"
				|  },
				|  {
				|    "value": "Adamant*",
				|    "label": "Adamant Arrows"
				|  },
				|  {
				|    "value": "Rune*",
				|    "label": "Rune Arrows"
				|  },
				|  {
				|    "value": "Amethyst*",
				|    "label": "Amethyst Arrows"
				|  },
				|  {
				|    "value": "Dragon*",
				|    "label": "Dragon Arrows"
				|  }
				|]
				|*/
				|
				|/* --- putting certain items first in the menu sorting --- */
				|
				|apply (name:VAR_MENU_SORT_ITEMS_FIRST) {
				|    menuSort = 300;
				|}
				|
				|/*@ define:module:constants
				|hidden: true
				|name: Constants
				|*/
				|
				|#define CONST_ACCOUNT_TYPE_MAIN 0
				|#define CONST_ACCOUNT_TYPE_IRONMAN 1
				|#define CONST_ACCOUNT_TYPE_ULTIMATE_IRONMAN 2
				|#define CONST_ACCOUNT_TYPE_HARDCORE_IRONMAN 3
				|#define CONST_ACCOUNT_TYPE_GROUP_IRONMAN 4
				|#define CONST_ACCOUNT_TYPE_HARDCORE_GROUP_IRONMAN 5
				|#define CONST_ACCOUNT_TYPE_UNRANKED_GROUP_IRONMAN 6
				|
				|#define CONST_SPINDEL_AREA [1595, 11520, 2, 1672, 11588, 2]
				|#define CONST_CALVARION_AREA [1871, 11530, 1, 1901, 11564, 1]
				|#define CONST_ARTIO_AREA [1735, 11519, 0, 1783, 11568, 0]
				|#define CONST_VENENATIS_AREA [3391, 10181, 2, 3457, 10241, 2]
				|#define CONST_VETION_AREA [3283, 10189, 1, 3307, 10217, 1]
				|#define CONST_CALLISTO_AREA [3335, 10305, 0, 3383, 10353, 0]
				|
				|#define CONST_COX_AREA [3200, 5120, 0, 3391, 5759, 2]
				|
				|#define CONST_COX_OLM_AREA [3211, 5703, 0, 3254, 5761, 0]
				|#define CONST_COX_VASA_AREA [3263, 5279, 0, 3360, 5312, 0]
				|#define CONST_COX_SHAMANS_AREA [3263, 5247, 0, 3362, 5279, 0]
				|#define CONST_COX_MUTADILE_AREA [3263, 5309, 1, 3360, 5344, 1]
				|#define CONST_COX_TEKTON_AREA [3263, 5278, 1, 3361, 5311, 1]
				|#define CONST_COX_TIGHTROPE_AREA [3263, 5341, 1, 3361, 5375, 1]
				|#define CONST_COX_ICE_DEMON_AREA [3263, 5341, 0, 3361, 5375, 0]
				|#define CONST_COX_THIEVING_AREA [3263, 5374, 0, 3360, 5408, 0]
				|
				|#define CONST_TOB_AREA [3153, 4233, 0, 3322, 4479, 0]
				|
				|#define CONST_TOA_AREA [3515, 5126, 0, 3965, 5432, 0]
				|
				|#define CONST_GOD_WARS_DUNGEON_AREA [2816, 5184, 0, 2943, 5375, 3]
				|#define CONST_COMMANDER_ZILYANA_AREA [2884, 5257, 0, 2908, 5276, 0]
				|#define CONST_GENERAL_GRAARDOR_AREA [2863, 5350, 2, 2877, 5373, 2]
				|#define CONST_KREEARRA_AREA [2820, 5295, 2, 2843, 5309, 2]
				|#define CONST_KRIL_TSUTSAROTH_AREA [2917, 5317, 2, 2941, 5332, 2]
				|#define CONST_NEX_AREA [2900, 5188, 0, 2943, 5218, 0]
				|
				|#define CONST_AMOXLIATL_AREA [1347, 4493, 0, 1388, 4530, 0]
				|
				|#define CONST_HUEYCOATL_AREA [1502, 3275, 0, 1521, 3294, 0]
				|#define CONST_MUSPAH_AREA [2831, 4242, 0, 2862, 4275, 0]
				|#define CONST_THE_WHISPERER_AREA [2631, 6337, 0, 2682, 6394, 0]
				|#define CONST_VARDORVIS_AREA [1122, 3411, 0, 1137, 3426, 0]
				|#define CONST_DUKE_SUCELLUS_AREA [3027, 6432, 0, 3052, 6460, 0]
				|#define CONST_THE_LEVIATHAN_AREA [2068, 6360, 0, 2095, 6386, 0]
				|
				|#define CONST_YAMA_AREA [1485, 10063, 0, 1524, 10092, 0]
				|
				|#define CONST_ANCIENT_GUTHIXIAN_TEMPLE_AREA [4020, 4350, 0, 4163, 4484, 0]
				|
				|#define CONST_GAUNTLET_AREA [1856, 5632, 0, 1983, 5695, 3]
				|
				|#define CONST_BOUNTY_HUNTER_AREA [3338, 3989, 0, 3504, 4140, 3]
				|
				|#define CONST_LAST_MAN_STANDING_DESERT_ISLAND_AREA [3391, 5758, 0, 3521, 5889, 3]
				|#define CONST_LAST_MAN_STANDING_WILD_VARROCK_AREA [3457, 6017, 0, 3646, 6205, 3]
				|
				|#define CONST_SLAYER_WILDERNESS_SLAYER_CAVE_AREA [3323, 10044, 0, 3458, 10178, 0]
				|#define CONST_SLAYER_HYDRAS_AREA [1291, 10221, 0, 1341, 10279, 0]
				|#define CONST_SLAYER_SMOKE_DEVILS_AREA [2376, 9408, 0, 2432, 9472, 0]
				|#define CONST_SLAYER_ARAXYTES_AREA [3650, 9793, 0, 3716, 9870, 0]
				|#define CONST_SLAYER_DARK_BEASTS_MOURNER_TUNNELS_AREA [1979, 4629, 0, 2043, 4678, 0]
				|#define CONST_SLAYER_DARK_BEASTS_IORWERTH_DUNGEON_1_AREA [3151, 12415, 0, 3182, 12439, 0]
				|#define CONST_SLAYER_DARK_BEASTS_IORWERTH_DUNGEON_2_AREA [3200, 12381, 0, 3243, 12407, 0]
				|#define CONST_SLAYER_CAVE_KRAKEN_AREA [2238, 9983, 0, 2304, 10022, 0]
				|#define CONST_SLAYER_ABYSSAL_DEMONS_ABYSSAL_AREA_AREA [3007, 4865, 0, 3070, 4926, 0]
				|#define CONST_SLAYER_ABYSSAL_DEMONS_CATACOMBS_CENTER_AREA [1667, 10049, 0, 1685, 10065, 0]
				|#define CONST_SLAYER_ABYSSAL_DEMONS_CATACOMBS_NORTH_AREA [1667, 10083, 0, 1681, 10102, 0]
				|#define CONST_SLAYER_ABYSSAL_DEMONS_SLAYER_TOWER_TOP_AREA [3404, 3555, 2, 3431, 3581, 2]
				|#define CONST_SLAYER_ABYSSAL_DEMONS_SLAYER_TOWER_BASEMENT_AREA [3429, 9953, 3, 3449, 9977, 3]
				|#define CONST_SLAYER_ABYSSAL_DEMONS_WILDERNESS_SLAYER_CAVE_AREA [3331, 10151, 0, 3353, 10174, 0]
				|#define CONST_SLAYER_DRAKES_AREA [1297, 10224, 1, 1363, 10260, 1]
				|#define CONST_SLAYER_NECHRYAEL_SLAYER_TOWER_TOP_AREA [3431, 3555, 2, 3453, 3580, 2]
				|#define CONST_SLAYER_NECHRYAEL_SLAYER_TOWER_BASEMENT_AREA [3399, 9977, 3, 3429, 9954, 3]
				|#define CONST_SLAYER_GREATER_NECHRYAEL_CATACOMBS_AREA [1689, 10087, 0, 1712, 10075, 0]
				|#define CONST_SLAYER_GREATER_NECHRYAEL_IORWERTH_NORTH_AREA [3216, 12449, 0, 3241, 12469, 0]
				|#define CONST_SLAYER_GREATER_NECHRYAEL_IORWERTH_SOUTH_AREA [3213, 12410, 0, 3233, 12432, 0]
				|#define CONST_SLAYER_GREATER_NECHRYAEL_WILDERNESS_SLAYER_CAVE_AREA [3329, 10095, 0, 3345, 10120, 0]
				|#define CONST_SLAYER_GARGOYLES_AREA [0000, 0000, 0, 0000, 0000, 0]
				|#define CONST_SLAYER_SKELETAL_WYVERNS_AREA [0000, 0000, 0, 0000, 0000, 0]
				|#define CONST_SLAYER_KURASKS_AREA [0000, 0000, 0, 0000, 0000, 0]
				|#define CONST_SLAYER_FOSSIL_ISLAND_WYVERNS_AREA [0000, 0000, 0, 0000, 0000, 0]
				|#define CONST_SLAYER_DUST_DEVILS_AREA [0000, 0000, 0, 0000, 0000, 0]
				|#define CONST_SLAYER_WYRMS_AREA [0000, 0000, 0, 0000, 0000, 0]
				|#define CONST_SLAYER_CAVE_HORRORS_AREA [0000, 0000, 0, 0000, 0000, 0]
				|#define CONST_SLAYER_ZYGOMITES_AREA [0000, 0000, 0, 0000, 0000, 0]
				|#define CONST_SLAYER_WARPED_CREATURES_AREA [0000, 0000, 0, 0000, 0000, 0]
				|#define CONST_SLAYER_TUROTHS_AREA [0000, 0000, 0, 0000, 0000, 0]
				|#define CONST_SLAYER_JELLIES_AREA [0000, 0000, 0, 0000, 0000, 0]
				|#define CONST_SLAYER_BLOODVELDS_AREA [0000, 0000, 0, 0000, 0000, 0]
				|
				|#define CONST_PRIF_SPAWN_AREA [3238, 6066, 0, 3284, 6102, 0]
				|#define CONST_FALADOR_SPAWN_AREA [2958, 3335, 0, 2983, 3355, 0]
				|#define CONST_EDGEVILLE_NETTLES_AREA [3088, 3468, 0, 3092, 3470, 0]
				|#define CONST_EDGEVILLE_MONASTERY_SECOND_FLOOR_AREA [3042, 3480, 1, 3062, 3501, 1]
				|#define CONST_ARDY_MARKET_CHEST_AREA [2672, 3305, 0, 2678, 3312, 0]
				|#define CONST_COLO_LOBBY_AREA [1794, 9479, 0, 1856, 9538, 0]
				|#define CONST_LUMBRIDGE_BASEMENT_CAVE_EXIT_AREA [3206, 9600, 0, 3237, 9628, 0]
				|
				|#define CONST_CLUE_HARD_WILDY_IDS [   2723,   2725,   2727,   2729,   2731,   2733,   2735,   2737,   2739,   2741,   2743,   2745,   2747,   2786,   2788,   3525,   3556,   3558,   7239,   7260,   7262,   7264,   10244,   10234,   12546,   12564,   19888,   23168,   23169, ]
				|
				|#define CONST_CLUE_ELITE_WILDY_IDS [   12075,   12093,   12095,   12097,   12109,   12101,   12134,   12150,   19807,   23144,   23146, ]
				|
				|#define CONST_GLOBAL_TRADEABLE_NON_TRADEABLE_ITEMS [     "1/2 anchovy pizza",     "1/2 meat pizza",     "1/2 pineapple pizza",     "1/2 plain pizza",     "2/3 cake",     "2/3 chocolate cake",     "Adamant fire arrow",     "Air rune pack",     "Amethyst fire arrow",     "An empty box",     "Anchovy paste",     "Apples(1)",     "Apples(2)",     "Apples(3)",     "Apples(4)",     "Astronomy book",     "Bailing bucket",     "Bananas(1)",     "Bananas(2)",     "Bananas(3)",     "Bananas(4)",     "Bandages",     "Barricade",     "Basket pack",     "Black mask (1)",     "Black mask (10)",     "Black mask (2)",     "Black mask (3)",     "Black mask (4)",     "Black mask (5)",     "Black mask (6)",     "Black mask (7)",     "Black mask (8)",     "Black mask (9)",     "Black satchel",     "Blue flowers",     "Broken pole",     "Bronze fire arrow",     "Bruised banana",     "Bucket of rubble",     "Burnt anglerfish",     "Burnt bat",     "Burnt batta",     "Burnt beast meat",     "Burnt bird meat",     "Burnt bread",     "Burnt cake",     "Burnt cave eel",     "Burnt chicken",     "Burnt crunchies",     "Burnt curry",     "Burnt dark crab",     "Burnt eel",     "Burnt egg",     "Burnt fish",     "Burnt fish",     "Burnt fish",     "Burnt fish",     "Burnt fish",     "Burnt fish",     "Burnt gnomebowl",     "Burnt jubbly",     "Burnt karambwan",     "Burnt lobster",     "Burnt manta ray",     "Burnt meat",     "Burnt monkfish",     "Burnt mushroom",     "Burnt onion",     "Burnt oomlie",     "Burnt oomlie wrap",     "Burnt pie",     "Burnt pizza",     "Burnt potato",     "Burnt rabbit",     "Burnt rainbow fish",     "Burnt sea turtle",     "Burnt shark",     "Burnt shrimp",     "Burnt snail",     "Burnt spider (stick)",     "Burnt stew",     "Burnt sweetcorn",     "Burnt swordfish",     "Cadarn lineage",     "Cat training medal",     "Chaos rune pack",     "Chaotic handegg",     "Chocolate slice",     "Climbing rope",     "Coconut shell",     "Coins",     "Cooked chicken",     "Cooked meat",     "Crate",     "Crushed gem",     "Crystal singing for beginners",     "Damp egg",     "Damp tinderbox",     "Dragon fire arrow",     "Earth rune pack",     "Empty oyster",     "Explosive potion",     "Feather pack",     "Field ration",     "Fire rune pack",     "Flyer",     "Fossilised Dung",     "Glassblowing book",     "Gold satchel",     "Green satchel",     "Ground bat bones",     "Guthix balance (unf)",     "Half a admiral pie",     "Half a apple pie",     "Half a botanical pie",     "Half a dragonfruit pie",     "Half a fish pie",     "Half a garden pie",     "Half a meat pie",     "Half a mud pie",     "Half a mushroom pie",     "Half a redberry pie",     "Half a summer pie",     "Half a wild pie",     "Half certificate (left)",     "Half certificate (right)",     "Half coconut",     "Holy handegg",     "Incomplete pizza",     "Iron fire arrow",     "Iron sickle",     "Mind rune pack",     "Miscellaneous key",     "Mithril fire arrow",     "Mud",     "Muddy rock",     "Newcomer map",     "Newspaper",     "Old wool",     "Oranges(1)",     "Oranges(2)",     "Oranges(3)",     "Oranges(4)",     "Part admiral pie",     "Part fish pie",     "Part garden pie",     "Part mud pie",     "Part summer pie",     "Part wild pie",     "Peaceful handegg",     "Pete's candlestick",     "Phoenix crossbow",     "Plain satchel",     "Plant pot pack",     "Platinum token",     "Potion",     "Raw beef",     "Raw chicken",     "Raw swamp paste",     "Red flowers",     "Red satchel",     "Rock",     "Rock",     "Rotten food",     "Rotten tomato",     "Ruined chompy",     "Ruined herb tea",     "Rune fire arrow",     "Rune satchel",     "Sack pack",     "Sample bottle",     "Shayzien supply crate",     "Slice of cake",     "Smashed glass",     "Smelly sock",     "Spare controls",     "Spider on shaft",     "Spooky egg",     "Steel fire arrow",     "Strawberries(1)",     "Strawberries(2)",     "Strawberries(3)",     "Strawberries(4)",     "Strip of cloth",     "Sulphur",     "Tankard",     "Tea leaves",     "Tile",     "Tomatoes(1)",     "Tomatoes(2)",     "Tomatoes(3)",     "Tomatoes(4)",     "Toolkit",     "Tzhaar air rune pack",     "Tzhaar earth rune pack",     "Tzhaar fire rune pack",     "Tzhaar water rune pack",     "Ugthanki kebab",     "Uncooked pizza",     "Unfinished broad bolt pack",     "Washing bowl",     "Water rune pack",     "Water-filled vial pack",     "Weapon store key",     "Ensouled goblin head",     "Ensouled monkey head",     "Ensouled imp head",     "Ensouled minotaur head",     "Ensouled scorpion head",     "Ensouled bear head",     "Ensouled unicorn head",     "Ensouled dog head",     "Ensouled chaos druid head",     "Ensouled giant head",     "Ensouled ogre head",     "Ensouled elf head",     "Ensouled troll head",     "Ensouled horror head",     "Ensouled kalphite head",     "Ensouled dagannoth head",     "Ensouled bloodveld head",     "Ensouled tzHaar head",     "Ensouled demon head",     "Ensouled hellhound head",     "Ensouled aviansie head",     "Ensouled abyssal head",     "Ensouled dragon head",     "Waterskin(1)",     "Waterskin(2)",     "Waterskin(3)", ]
				|
				|#define CONST_FARMING_SEEDS_ALLOTMENT_LIST [   "Potato seed",   "Onion seed",   "Cabbage seed",   "Tomato seed",   "Sweetcorn seed",   "Strawberry seed",   "Watermelon seed",   "Snape grass seed", ]
				|
				|#define CONST_FARMING_SEEDS_FLOWER_LIST [   "Marigold seed",   "Rosemary seed",   "Nasturtium seed",   "Woad seed",   "Limpwurt seed",   "White lily seed", ]
				|
				|#define CONST_FARMING_SEEDS_HERB_LIST [   "Guam seed",   "Marrentill seed",   "Tarromin seed",   "Harralander seed",   "Ranarr seed",   "Toadflax seed",   "Irit seed",   "Avantoe seed",   "Kwuarm seed",   "Snapdragon seed",   "Huasca seed",   "Cadantine seed",   "Lantadyme seed",   "Dwarf weed seed",   "Torstol seed", ]
				|
				|#define CONST_FARMING_SEEDS_HOPS_LIST [   "Barley seed",   "Hammerstone seed",   "Asgarnian seed",   "Jute seed",   "Yanillian seed",   "Krandorian seed",   "Wildblood seed", ]
				|
				|#define CONST_FARMING_SEEDS_BUSH_LIST [   "Redberry seed",   "Cadavaberry seed",   "Dwellberry seed",   "Jangerberry seed",   "Whiteberry seed",   "Poison ivy seed", ]
				|
				|#define CONST_FARMING_SEEDS_TREE_LIST [   "Acorn",   "Willow seed",   "Maple seed",   "Yew seed",   "Magic seed", ]
				|
				|#define CONST_FARMING_SEEDS_FRUIT_TREE_LIST [   "Apple tree seed",   "Banana tree seed",   "Orange tree seed",   "Curry tree seed",   "Pineapple seed",   "Papaya tree seed",   "Palm tree seed",   "Dragonfruit tree seed", ]
				|
				|#define CONST_FARMING_SEEDS_SPECIAL_TREE_LIST [   "Teak seed",   "Mahogany seed",   "Calquat tree seed",   "Crystal acorn",   "Spirit seed",   "Celastrus seed",   "Redwood tree seed", ]
				|
				|#define CONST_FARMING_SEEDS_SPECIAL_LIST [   "Seaweed spore",   "Grape seed",   "Mushroom spore",   "Belladonna seed",   "Hespori seed",   "Cactus seed",   "Potato cactus seed", ]
				|
				|#define CONST_FARMING_SEEDS_ANIMA_LIST [   "Kronos seed",   "Iasor seed",   "Attas seed", ]
				|
				|#define CONST_CURRENCY_LIST [   "Abyssal pearls",   "Agility arena ticket",   "Anima-infused bark",   "Archery ticket",   "Barronite shards",   "Blood money",   "Brimhaven voucher",   "Castle wars ticket",   "Coins",   "Ecto-token",   "Frog token",   "Glistening tear",   "Golden nugget",   "Hallowed mark",   "Intelligence",   "Mark of grace",   "Mermaid's tear",   "Minnow",   "Molch pearl",   "Mole claw",   "Mole skin",   "Nuggets",   "Numulite",   "Paramaya ticket",   "Platinum token",   "Rare creature parts",   "Reward token credits",   "Shantay pass",   "Ship ticket",   "Stardust",   "Termites",   "Tokkul",   "Trading sticks",   "Unidentified minerals",   "Warrior guild token", ]
				|
				|#define CONST_FISHING_EQUIPMENT_LIST [   "Small fishing net",   "Big fishing net",   "Drift net",   "Fishing rod",   "Fly fishing rod",   "Oily fishing rod",   "Harpoon",   "Lobster pot",   "Karambwan vessel", ]
				|
				|#define CONST_FISHING_BAIT_LIST [   "Sandworms",   "Fishing bait",   "Feather",   "Stripy feather",   "Dark fishing bait",   "King worm",   "Fish chunks",   "Raw karambwanji",   "Diabolic worms", ]
				|
				|#define CONST_FISHING_RAW_FISH_LIST [   "Raw shrimps",   "Raw anchovies",   "Raw guppy",   "Raw cavefish",   "Raw tetra",   "Frog spawn",   "Raw monkfish",   "Minnow",   "Raw mackerel",   "Raw cod",   "Raw bass",   "Raw catfish",   "Raw sardine",   "Raw herring",   "Raw pike",   "Raw slimy eel",   "Raw cave eel",   "Raw anglerfish",   "Sacred eel",   "Raw trout",   "Raw salmon",   "Raw rainbow fish",   "Raw lava eel",   "Infernal eel",   "Leaping trout",   "Leaping salmon",   "Leaping sturgeon",   "Raw tuna",   "Raw swordfish",   "Raw shark",   "Raw lobster",   "Raw dark crab",   "Bluegill",   "Common tench",   "Mottled eel",   "Greater siren",   "Raw karambwan", ]
				|
				|#define CONST_ALL_HERBS [   "*guam leaf",   "*marrentill",   "*tarromin",   "*harralander",   "*ranarr weed",   "*toadflax",   "*irit leaf",   "*avantoe",   "*kwuarm",   "*huasca",   "*snapdragon",   "*cadantine",   "*lantadyme",   "*dwarf weed",   "*torstol", ]
				|
				|#define CONST_ALL_HERBLORE_EQUIPMENT [   "Pestle and mortar",   "Vial",   "Vial of water",   "Alchemist gloves",   "Alchemist pants",   "Alchemist labcoat",   "Prescription goggles",   "Alchemist's amulet", ]
				|
				|#define CONST_HUNTER_FOODS_LIST [   "Cooked wild kebbit",   "Cooked larupia",   "Cooked barb-tailed kebbit",   "Cooked graahk",   "Cooked kyatt",   "Cooked pyre fox",   "Cooked sunlight antelope",   "Cooked dashing kebbit",   "Cooked moonlight antelope", ]
				|
				|#define CONST_FISH_FOODS_LIST [   "Shrimps",   "Sardine",   "Karambwanji",   "Herring",   "Anchovies",   "Mackerel",   "Trout",   "Cod",   "Pike",   "Cooked slimy eel",   "Salmon",   "Tuna",   "Rainbow fish",   "Cave eel",   "Lobster",   "Bass",   "Swordfish",   "Lava eel",   "Monkfish",   "Cooked karambwan",   "Shark",   "Sea turtle",   "Manta ray",   "Anglerfish",   "Dark crab", ]
				|
				|#define CONST_POTATOES_FOODS_LIST [   "Baked potato",   "Potato with butter",   "Chilli potato",   "Potato with cheese",   "Egg potato",   "Mushroom potato",   "Tuna potato", ]
				|
				|#define CONST_PIE_FOODS_LIST [   "Redberry pie",   "Half a redberry pie",   "Meat pie",   "Half a meat pie",   "Apple pie",   "Half a apple pie",   "Garden pie",   "Half a garden pie",   "Fish pie",   "Half a fish pie",   "Botanical pie",   "Half a botanical pie",   "Mushroom pie",   "Half a mushroom pie",   "Admiral pie",   "Half a admiral pie",   "Dragonfruit pie",   "Half a dragonfruit pie",   "Wild pie",   "Half a wild pie",   "Summer pie",   "Half a summer pie", ]
				|
				|#define CONST_PIZZA_FOODS_LIST [   "1/2 Plain pizza",   "Plain pizza",   "1/2 Meat pizza",   "Meat pizza",   "1/2 Anchovy pizza",   "Anchovy pizza",   "1/2 Pineapple pizza",   "Pineapple pizza", ]
				|
				|#define CONST_CAKE_FOODS_LIST [   "Cake",   "2/3 Cake",   "Slice of Cake",   "Chocolate cake",   "2/3 Chocolate cake",   "Chocolate Slice", ]
				|
				|#define CONST_DEFENDERS_LIST [   "Bronze defender",   "Bronze defender (broken)",   "Bronze defender (l)",   "Iron defender",   "Iron defender (broken)",   "Iron defender (l)",   "Steel defender",   "Steel defender (broken)",   "Steel defender (l)",   "Black defender",   "Black defender (broken)",   "Black defender (l)",   "Mithril defender",   "Mithril defender (broken)",   "Mithril defender (l)",   "Adamant defender",   "Adamant defender (broken)",   "Adamant defender (l)",   "Rune defender",   "Rune defender (broken)",   "Rune defender (l)",   "Dragon defender",   "Dragon defender (broken)",   "Dragon defender (l)",   "Rune defender (t)",   "Dragon defender (t)",   "Avernic defender",   "Avernic defender (broken)",   "Avernic defender (l)",   "Ghommal's avernic defender 5",   "Ghommal's avernic defender 5 (l)",   "Ghommal's avernic defender 6",   "Ghommal's avernic defender 6 (l)", ]
				|
				|#define CONST_BRONZE_ONE_HANDED_MELEE_WEAPONS [   "Bronze dagger",   "Bronze axe",   "Bronze mace",   "Bronze sword",   "Bronze pickaxe",   "Bronze scimitar",   "Bronze hasta",   "Bronze longsword",   "Bronze warhammer",   "Bronze battleaxe", ]
				|
				|#define CONST_BRONZE_TWO_HANDED_MELEE_WEAPONS [   "Bronze claws",   "Bronze spear",   "Bronze halberd",   "Bronze 2h sword", ]
				|
				|#define CONST_BRONZE_RANGED_WEAPONS [   "Bronze crossbow", ]
				|
				|#define CONST_BRONZE_AMMO [   "Bronze dart",   "Bronze knife",   "Bronze thrownaxe",   "Bronze arrow",   "Bronze bolts",   "Bronze brutal",   "Bronze javelin", ]
				|
				|#define CONST_BRONZE_ARMOUR [   "Bronze boots",   "Bronze gloves",   "Bronze defender",   "Bronze defender (broken)",   "Bronze defender (l)",   "Bronze med helm",   "Bronze full helm",   "Bronze sq shield",   "Bronze platelegs",   "Bronze plateskirt",   "Bronze kiteshield",   "Bronze chainbody",   "Bronze platebody", ]
				|
				|#define CONST_BRONZE_TRIMMED_ARMOUR [   "Bronze full helm (t)",   "Bronze platebody (t)",   "Bronze platelegs (t)",   "Bronze kiteshield (t)",   "Bronze plateskirt (t)", ]
				|
				|#define CONST_BRONZE_GOLD_TRIMMED_ARMOUR [   "Bronze full helm (g)",   "Bronze platebody (g)",   "Bronze platelegs (g)",   "Bronze kiteshield (g)",   "Bronze plateskirt (g)", ]
				|
				|#define CONST_BRONZE_OTHER [   "Bronze Nails",   "Bronze Javelin heads",   "Bronze Arrowtips",   "Bronze Dart tips",   "Bronze Unfinished bolts", ]
				|
				|#define CONST_IRON_ONE_HANDED_MELEE_WEAPONS [   "Iron dagger",   "Iron axe",   "Iron mace",   "Iron sword",   "Iron pickaxe",   "Iron scimitar",   "Iron hasta",   "Iron longsword",   "Iron warhammer",   "Iron battleaxe", ]
				|
				|#define CONST_IRON_TWO_HANDED_MELEE_WEAPONS [   "Iron claws",   "Iron spear",   "Iron halberd",   "Iron 2h sword", ]
				|
				|#define CONST_IRON_RANGED_WEAPONS [   "Iron crossbow", ]
				|
				|#define CONST_IRON_AMMO [   "Iron dart",   "Iron knife",   "Iron thrownaxe",   "Iron arrow",   "Iron bolts",   "Iron brutal",   "Iron javelin", ]
				|
				|#define CONST_IRON_ARMOUR [   "Iron boots",   "Iron gloves",   "Iron defender",   "Iron defender (broken)",   "Iron defender (l)",   "Iron med helm",   "Iron full helm",   "Iron sq shield",   "Iron platelegs",   "Iron plateskirt",   "Iron kiteshield",   "Iron chainbody",   "Iron platebody", ]
				|
				|#define CONST_IRON_TRIMMED_ARMOUR [   "Iron full helm (t)",   "Iron platebody (t)",   "Iron platelegs (t)",   "Iron kiteshield (t)",   "Iron plateskirt (t)", ]
				|
				|#define CONST_IRON_GOLD_TRIMMED_ARMOUR [   "Iron full helm (g)",   "Iron platebody (g)",   "Iron platelegs (g)",   "Iron kiteshield (g)",   "Iron plateskirt (g)", ]
				|
				|#define CONST_IRON_OTHER [   "Iron Nails",   "Iron Javelin heads",   "Iron Arrowtips",   "Iron Dart tips",   "Iron Unfinished bolts",   "Oil lantern frame",   "Cooking spit", ]
				|
				|#define CONST_STEEL_ONE_HANDED_MELEE_WEAPONS [   "Steel dagger",   "Steel axe",   "Steel mace",   "Steel sword",   "Steel pickaxe",   "Steel scimitar",   "Steel hasta",   "Steel longsword",   "Steel warhammer",   "Steel battleaxe", ]
				|
				|#define CONST_STEEL_TWO_HANDED_MELEE_WEAPONS [   "Steel claws",   "Steel spear",   "Steel halberd",   "Steel 2h sword", ]
				|
				|#define CONST_STEEL_RANGED_WEAPONS [   "Steel crossbow", ]
				|
				|#define CONST_STEEL_AMMO [   "Steel dart",   "Steel knife",   "Steel thrownaxe",   "Steel arrow",   "Steel bolts",   "Steel brutal",   "Steel javelin", ]
				|
				|#define CONST_STEEL_ARMOUR [   "Steel boots",   "Steel gloves",   "Steel defender",   "Steel defender (broken)",   "Steel defender (l)",   "Steel med helm",   "Steel full helm",   "Steel sq shield",   "Steel platelegs",   "Steel plateskirt",   "Steel kiteshield",   "Steel chainbody",   "Steel platebody", ]
				|
				|#define CONST_STEEL_TRIMMED_ARMOUR [   "Steel full helm (t)",   "Steel platebody (t)",   "Steel platelegs (t)",   "Steel kiteshield (t)",   "Steel plateskirt (t)", ]
				|
				|#define CONST_STEEL_GOLD_TRIMMED_ARMOUR [   "Steel full helm (g)",   "Steel platebody (g)",   "Steel platelegs (g)",   "Steel kiteshield (g)",   "Steel plateskirt (g)", ]
				|
				|#define CONST_STEEL_OTHER [   "Steel Nails",   "Steel Javelin heads",   "Steel Arrowtips",   "Steel Dart tips",   "Steel Unfinished bolts",   "Steel studs", ]
				|
				|#define CONST_BLACK_ONE_HANDED_MELEE_WEAPONS [   "Black dagger",   "Black pickaxe",   "Black axe",   "Black sword",   "Black mace",   "Black scimitar",   "Black longsword",   "Black warhammer",   "Black battleaxe", ]
				|
				|#define CONST_BLACK_TWO_HANDED_MELEE_WEAPONS [   "Black claws",   "Black spear",   "Black halberd",   "Black 2h sword", ]
				|
				|#define CONST_BLACK_RANGED_WEAPONS [ ]
				|
				|#define CONST_BLACK_AMMO [   "Black dart",   "Black knife",   "Black brutal", ]
				|
				|#define CONST_BLACK_ARMOUR [   "Black gloves",   "Black boots",   "Black defender",   "Black defender (broken)",   "Black defender (l)",   "Black med helm",   "Black full helm",   "Black sq shield",   "Black kiteshield",   "Black platelegs",   "Black plateskirt",   "Black chainbody",   "Black platebody", ]
				|
				|#define CONST_BLACK_TRIMMED_ARMOUR [   "Black full helm (t)",   "Black platebody (t)",   "Black platelegs (t)",   "Black kiteshield (t)",   "Black plateskirt (t)", ]
				|
				|#define CONST_BLACK_GOLD_TRIMMED_ARMOUR [   "Black full helm (g)",   "Black platebody (g)",   "Black platelegs (g)",   "Black kiteshield (g)",   "Black plateskirt (g)", ]
				|
				|#define CONST_BLACK_OTHER [   "Black Nails",   "Elite black full helm",   "Elite black platelegs",   "Elite black platebody", ]
				|
				|#define CONST_BLACK_HERALDIC_ARMOUR [   "Black helm (h1)",   "Black helm (h2)",   "Black helm (h3)",   "Black helm (h4)",   "Black helm (h5)",   "Black platebody (h1)",   "Black platebody (h2)",   "Black platebody (h3)",   "Black platebody (h4)",   "Black platebody (h5)",   "Black shield (h1)",   "Black shield (h2)",   "Black shield (h3)",   "Black shield (h4)",   "Black shield (h5)", ]
				|
				|#define CONST_MITHRIL_ONE_HANDED_MELEE_WEAPONS [   "Mithril dagger",   "Mithril axe",   "Mithril mace",   "Mithril sword",   "Mithril pickaxe",   "Mithril scimitar",   "Mithril hasta",   "Mithril longsword",   "Mithril warhammer",   "Mithril battleaxe", ]
				|
				|#define CONST_MITHRIL_TWO_HANDED_MELEE_WEAPONS [   "Mithril claws",   "Mithril spear",   "Mithril halberd",   "Mithril 2h sword", ]
				|
				|#define CONST_MITHRIL_RANGED_WEAPONS [   "Mithril crossbow", ]
				|
				|#define CONST_MITHRIL_AMMO [   "Mithril dart",   "Mithril knife",   "Mithril thrownaxe",   "Mithril arrow",   "Mithril bolts",   "Mithril brutal",   "Mithril javelin", ]
				|
				|#define CONST_MITHRIL_ARMOUR [   "Mithril boots",   "Mithril gloves",   "Mithril defender",   "Mithril defender (broken)",   "Mithril defender (l)",   "Mithril med helm",   "Mithril full helm",   "Mithril sq shield",   "Mithril platelegs",   "Mithril plateskirt",   "Mithril kiteshield",   "Mithril chainbody",   "Mithril platebody", ]
				|
				|#define CONST_MITHRIL_TRIMMED_ARMOUR [   "Mithril full helm (t)",   "Mithril platebody (t)",   "Mithril platelegs (t)",   "Mithril kiteshield (t)",   "Mithril plateskirt (t)", ]
				|
				|#define CONST_MITHRIL_GOLD_TRIMMED_ARMOUR [   "Mithril full helm (g)",   "Mithril platebody (g)",   "Mithril platelegs (g)",   "Mithril kiteshield (g)",   "Mithril plateskirt (g)", ]
				|
				|#define CONST_MITHRIL_OTHER [   "Mithril Nails",   "Mithril Javelin heads",   "Mithril Arrowtips",   "Mithril Dart tips",   "Mithril Unfinished bolts",   "Mith grapple tip",   "Mith grapple", ]
				|
				|#define CONST_ADAMANT_ONE_HANDED_MELEE_WEAPONS [   "Adamant dagger",   "Adamant axe",   "Adamant mace",   "Adamant cane",   "Adamant sword",   "Adamant pickaxe",   "Adamant scimitar",   "Adamant hasta",   "Adamant longsword",   "Adamant warhammer",   "Adamant battleaxe", ]
				|
				|#define CONST_ADAMANT_TWO_HANDED_MELEE_WEAPONS [   "Adamant claws",   "Adamant spear",   "Adamant halberd",   "Adamant 2h sword", ]
				|
				|#define CONST_ADAMANT_RANGED_WEAPONS [   "Adamant crossbow", ]
				|
				|#define CONST_ADAMANT_AMMO [   "Adamant dart",   "Adamant knife",   "Adamant thrownaxe",   "Adamant arrow",   "Adamant bolts",   "Adamant brutal",   "Adamant javelin", ]
				|
				|#define CONST_ADAMANT_ARMOUR [   "Adamant boots",   "Adamant gloves",   "Adamant defender",   "Adamant defender (broken)",   "Adamant defender (l)",   "Adamant med helm",   "Adamant full helm",   "Adamant sq shield",   "Adamant platelegs",   "Adamant plateskirt",   "Adamant kiteshield",   "Adamant chainbody",   "Adamant platebody", ]
				|
				|#define CONST_ADAMANT_TRIMMED_ARMOUR [   "Adamant full helm (t)",   "Adamant platebody (t)",   "Adamant platelegs (t)",   "Adamant kiteshield (t)",   "Adamant plateskirt (t)", ]
				|
				|#define CONST_ADAMANT_GOLD_TRIMMED_ARMOUR [   "Adamant full helm (g)",   "Adamant platebody (g)",   "Adamant platelegs (g)",   "Adamant kiteshield (g)",   "Adamant plateskirt (g)", ]
				|
				|#define CONST_ADAMANT_OTHER [   "Adamant Nails",   "Adamant Javelin heads",   "Adamant Arrowtips",   "Adamant Dart tips",   "Adamant Unfinished bolts", ]
				|
				|#define CONST_ADAMANT_HERALDIC_ARMOUR [   "Adamant helm (h1)",   "Adamant helm (h2)",   "Adamant helm (h3)",   "Adamant helm (h4)",   "Adamant helm (h5)",   "Adamant platebody (h1)",   "Adamant platebody (h2)",   "Adamant platebody (h3)",   "Adamant platebody (h4)",   "Adamant platebody (h5)",   "Adamant shield (h1)",   "Adamant shield (h2)",   "Adamant shield (h3)",   "Adamant shield (h4)",   "Adamant shield (h5)", ]
				|
				|#define CONST_RUNE_ONE_HANDED_MELEE_WEAPONS [   "Rune dagger",   "Rune axe",   "Rune mace",   "Rune sword",   "Rune pickaxe",   "Rune scimitar",   "Rune hasta",   "Rune longsword",   "Rune warhammer",   "Rune battleaxe", ]
				|
				|#define CONST_RUNE_TWO_HANDED_MELEE_WEAPONS [   "Rune claws",   "Rune spear",   "Rune halberd",   "Rune 2h sword", ]
				|
				|#define CONST_RUNE_RANGED_WEAPONS [   "Rune crossbow", ]
				|
				|#define CONST_RUNE_AMMO [   "Rune dart",   "Rune knife",   "Rune thrownaxe",   "Rune arrow",   "Rune bolts",   "Rune brutal",   "Rune javelin", ]
				|
				|#define CONST_RUNE_ARMOUR [   "Rune boots",   "Rune gloves",   "Rune defender",   "Rune defender (broken)",   "Rune defender (l)",   "Rune med helm",   "Rune full helm",   "Rune sq shield",   "Rune platelegs",   "Rune plateskirt",   "Rune kiteshield",   "Rune chainbody",   "Rune platebody", ]
				|
				|#define CONST_RUNE_TRIMMED_ARMOUR [   "Rune full helm (t)",   "Rune platebody (t)",   "Rune platelegs (t)",   "Rune kiteshield (t)",   "Rune plateskirt (t)", ]
				|
				|#define CONST_RUNE_GOLD_TRIMMED_ARMOUR [   "Rune full helm (g)",   "Rune platebody (g)",   "Rune platelegs (g)",   "Rune kiteshield (g)",   "Rune plateskirt (g)", ]
				|
				|#define CONST_RUNE_OTHER [   "Rune Nails",   "Rune Javelin heads",   "Rune Arrowtips",   "Rune Dart tips",   "Rune Unfinished bolts", ]
				|
				|#define CONST_RUNE_HERALDIC_ARMOUR [   "Rune helm (h1)",   "Rune helm (h2)",   "Rune helm (h3)",   "Rune helm (h4)",   "Rune helm (h5)",   "Rune platebody (h1)",   "Rune platebody (h2)",   "Rune platebody (h3)",   "Rune platebody (h4)",   "Rune platebody (h5)",   "Rune shield (h1)",   "Rune shield (h2)",   "Rune shield (h3)",   "Rune shield (h4)",   "Rune shield (h5)", ]
				|
				|#define CONST_RUNE_GILDED [   "Gilded med helm",   "Gilded full helm",   "Gilded chainbody",   "Gilded platebody",   "Gilded platelegs",   "Gilded plateskirt",   "Gilded boots",   "Gilded sq shield",   "Gilded kiteshield",   "Gilded hasta",   "Gilded spear",   "Gilded scimitar",   "Gilded 2h sword", ]
				|
				|#define CONST_RUNE_ANCIENT_TRIMMED_ARMOUR [   "Ancient full helm",   "Ancient platebody",   "Ancient platelegs",   "Ancient kiteshield",   "Ancient plateskirt", ]
				|
				|#define CONST_RUNE_ARMADYL_TRIMMED_ARMOUR [   "Armadyl full helm",   "Armadyl platebody",   "Armadyl platelegs",   "Armadyl kiteshield",   "Armadyl plateskirt", ]
				|
				|#define CONST_RUNE_BANDOS_TRIMMED_ARMOUR [   "Bandos full helm",   "Bandos platebody",   "Bandos platelegs",   "Bandos kiteshield",   "Bandos plateskirt", ]
				|
				|#define CONST_RUNE_GUTHIX_TRIMMED_ARMOUR [   "Guthix full helm",   "Guthix platebody",   "Guthix platelegs",   "Guthix kiteshield",   "Guthix plateskirt", ]
				|
				|#define CONST_RUNE_SARADOMIN_TRIMMED_ARMOUR [   "Saradomin full helm",   "Saradomin platebody",   "Saradomin platelegs",   "Saradomin kiteshield",   "Saradomin plateskirt", ]
				|
				|#define CONST_RUNE_ZAMORAK_TRIMMED_ARMOUR [   "Zamorak full helm",   "Zamorak platebody",   "Zamorak platelegs",   "Zamorak kiteshield",   "Zamorak plateskirt", ]
				|
				|#define CONST_DRAGON_ONE_HANDED_MELEE_WEAPONS [   "Dragon dagger",   "Dragon axe",   "Dragon pickaxe",   "Dragon harpoon",   "Dragon mace",   "Dragon cane",   "Dragon hasta",   "Dragon sword",   "Dragon scimitar",   "Dragon longsword",   "Dragon warhammer",   "Dragon battleaxe", ]
				|
				|#define CONST_DRAGON_TWO_HANDED_MELEE_WEAPONS [   "Dragon claws",   "Dragon spear",   "Dragon halberd",   "Dragon 2h sword", ]
				|
				|#define CONST_DRAGON_RANGED_WEAPONS [   "Dragon crossbow", ]
				|
				|#define CONST_DRAGON_AMMO [   "Dragon knife",   "Dragon dart",   "Dragon thrownaxe",   "Dragon arrow",   "Dragon bolts",   "Dragon javelin", ]
				|
				|#define CONST_DRAGON_ARMOUR [   "Dragon med helm",   "Dragon full helm",   "Dragon chainbody",   "Dragon platebody",   "Dragon platelegs",   "Dragon plateskirt",   "Dragon gloves",   "Dragon boots",   "Dragon sq shield",   "Dragon kiteshield",   "Dragon defender",   "Dragon defender (broken)",   "Dragon defender (l)", ]
				|
				|#define CONST_LAST_MAN_STANDING_STARTER_GEAR_LIST [   "Abyssal whip",   "Ahrim's staff",   "Amulet of glory",   "Ancient staff",   "Barrows gloves",   "Berserker helm",   "Berserker ring",   "Black d'hide body",   "Black d'hide chaps",   "Climbing boots",   "Diamond bolts (e)",   "Dragon dagger",   "Dragon defender",   "Dragon scimitar",   "Ghostly hood",   "Ghostly robe (bottom)",   "Ghostly robe (top)",   "Helm of neitiznot",   "Imbued guthix cape",   "Imbued saradomin cape",   "Imbued zamorak cape",   "Mithril gloves",   "Mystic robe bottom",   "Mystic robe bottom (dark)",   "Mystic robe bottom (light)",   "Mystic robe top",   "Mystic robe top (dark)",   "Mystic robe top (light)",   "Occult necklace",   "Rune crossbow",   "Rune defender",   "Rune platelegs",   "Spirit shield",   "Tome of fire",   "Unholy book",  ]
				|
				|#define CONST_LAST_MAN_STANDING_AMMO_LIST [   "Atlatl dart",   "Diamond bolts (e)",   "Dragon arrows",   "Dragon javelins",   "Dragon knife",   "Morrigan's javelin",   "Opal dragon bolts (e)", ]
				|
				|#define CONST_BONES_LIST [   "Bones",   "Wolf bones",   "Burnt bones",   "Monkey bones",   "Bat bones",   "Big bones",   "Jogre bones",   "Wyrmling bones",   "Zogre bones",   "Shaikahan bones",   "Babydragon bones",   "Wyrm bones",   "Wyvern bones",   "Dragon bones",   "Drake bones",   "Fayrg bones",   "Lava dragon bones",   "Raurg bones",   "Hydra bones",   "Dagannoth bones",   "Ourg bones",   "Superior dragon bones",   "Bleached bones",   "Small zombie monkey bones",   "Large zombie monkey bones",   "Small ninja monkey bones",   "Medium ninja monkey bones",   "Gorilla bones",   "Bearded gorilla bones", ]
				|
				|#define CONST_ASHES_LIST [   "Fiendish ashes",   "Vile ashes",   "Malicious ashes",   "Abyssal ashes",   "Infernal ashes", ]
				|
				|#define CONST_SHADES_REMAINS_LIST [   "Loar remains",   "Phrin remains",   "Riyl remains",   "Asyn remains",   "Fiyr remains",   "Urium remains", ]
				|
				|#define CONST_ENSOULED_HEAD_T1_LIST [   "Ensouled goblin head",   "Ensouled monkey head",   "Ensouled imp head",   "Ensouled minotaur head",   "Ensouled scorpion head", "Ensouled bear head",   "Ensouled unicorn head"]
				|
				|#define CONST_ENSOULED_HEAD_T2_LIST [     "Ensouled dog head",   "Ensouled chaos druid head",   "Ensouled giant head",   "Ensouled ogre head",   "Ensouled elf head",   "Ensouled troll head",   "Ensouled horror head"]
				|
				|#define CONST_ENSOULED_HEAD_T3_LIST ["Ensouled kalphite head",   "Ensouled dagannoth head",   "Ensouled bloodveld head",   "Ensouled tzHaar head",   "Ensouled demon head",   "Ensouled hellhound head"]
				|
				|#define CONST_ENSOULED_HEAD_T4_LIST ["Ensouled aviansie head",   "Ensouled abyssal head",   "Ensouled dragon head"]
				|
				|#define CONST_ENSOULED_HEAD_LIST [   "Ensouled goblin head",   "Ensouled monkey head",   "Ensouled imp head",   "Ensouled minotaur head",   "Ensouled scorpion head",   "Ensouled bear head",   "Ensouled unicorn head",   "Ensouled dog head",   "Ensouled chaos druid head",   "Ensouled giant head",   "Ensouled ogre head",   "Ensouled elf head",   "Ensouled troll head",   "Ensouled horror head",   "Ensouled kalphite head",   "Ensouled dagannoth head",   "Ensouled bloodveld head",   "Ensouled tzHaar head",   "Ensouled demon head",   "Ensouled hellhound head",   "Ensouled aviansie head",   "Ensouled abyssal head",   "Ensouled dragon head", ]
				|
				|#define CONST_RUNE_NAMES [   "Air rune",   "Mind rune",   "Water rune",   "Earth rune",   "Fire rune",   "Body rune",   "Cosmic rune",   "Chaos rune",   "Nature rune",   "Law rune",   "Death rune",   "Sunfire rune",   "Astral rune",   "Blood rune",   "Soul rune",   "Wrath rune",   "Mist rune",   "Dust rune",   "Mud rune",   "Smoke rune",   "Steam rune",   "Lava rune", ]
				|
				|#define CONST_SECONDARIES_LIST [   "Eye of newt",   "Unicorn horn dust",   "Limpwurt root",   "Volcanic ash",   "Red spiders' eggs",   "Chocolate dust",   "White berries",   "Toad's legs",   "Goat horn dust",   "Snape grass",   "Mort myre fungus",   "Kebbit teeth dust",   "Dragon scale dust",   "Nail beast nails",   "Yew roots",   "Wine of zamorak",   "Potato cactus",   "Jangerberries",   "Magic roots",   "Crushed nest",   "Poison ivy berries",   "Nihil dust",   "Lily of the sands",   "Aldarium",   "Araxyte venom sack", ]
				|
				|#define CONST_AMMO_STANDARD_ARROWS_LIST [   "Bronze Arrow*",   "Iron Arrow*",   "Steel Arrow*",   "Mithril Arrow*",   "Adamant Arrow*",   "Rune Arrow*",   "Amethyst Arrow*",   "Dragon Arrow*",   "Bronze fire arrow",   "Iron fire arrow",   "Steel fire arrow",   "Mithril fire arrow",   "Adamant fire arrow",   "Rune fire arrow",   "Amethyst fire arrow",   "Dragon fire arrow", ]
				|
				|#define CONST_AMMO_DARTS_LIST [   "Bronze dart*",   "Iron dart*",   "Steel dart*",   "Black dart*",   "Mithril dart*",   "Adamant dart*",   "Rune dart*",   "Amethyst dart*",   "Dragon dart*",   "Atlatl dart", ]
				|
				|#define CONST_AMMO_BOLTS_LIST [   "Bronze bolts*",   "Blurite bolts*",   "Silver bolts*",   "Iron bolts*",   "Steel bolts*",   "Mithril bolts*",   "Adamant bolts*",   "Runite bolts*",   "Dragon bolts*", ]
				|
				|#define CONST_AMMO_BOLTS_E_LIST [   "Opal bolts (e)",   "Jade bolts (e)",   "Pearl bolts (e)",   "Topaz bolts (e)",   "Sapphire bolts (e)",   "Emerald bolts (e)",   "Ruby bolts (e)",   "Diamond bolts (e)",   "Dragonstone bolts (e)",   "Onyx bolts (e)", ]
				|
				|#define CONST_AMMO_DRAGON_BOLTS_E_LIST [   "Opal dragon bolts (e)",   "Jade dragon bolts (e)",   "Pearl dragon bolts (e)",   "Topaz dragon bolts (e)",   "Sapphire dragon bolts (e)",   "Emerald dragon bolts (e)",   "Ruby dragon bolts (e)",   "Diamond dragon bolts (e)",   "Dragonstone dragon bolts (e)",   "Onyx dragon bolts (e)", ]
				|
				|#define CONST_AMMO_JAVELINS_LIST [   "Bronze javelin*",   "Iron javelin*",   "Steel javelin*",   "Mithril javelin*",   "Adamant javelin*",   "Rune javelin*",   "Amethyst javelin*",   "Dragon javelin*",   "Morrigan's javelin (bh)*", ]
				|
				|#define CONST_AMMO_SPECIAL_BOLTS_LIST [   "Moonlight antler bolts",   "Sunlight antler bolts",   "Kebbit bolts",   "Long kebbit bolts",   "Broad bolts",   "Amethyst broad bolts",   "Barbed bolts",   "Bone bolts", ]
				|
				|#define CONST_AMMO_BRUTAL_ARROWS_LIST [   "Bronze brutal",   "Iron brutal",   "Steel brutal",   "Black brutal",   "Mithril brutal",   "Adamant brutal",   "Rune brutal", ]
				|
				|#define CONST_AMMO_SPECIAL_ARROWS_LIST [   "Training Arrow",   "Ogre Arrow",   "Ice Arrow",   "Broad Arrow", ]
				|
				|#define CONST_AMMO_KNIVES_LIST [   "Bronze knife",   "Iron knife",   "Steel knife",   "Black knife",   "Mithril knife",   "Adamant knife",   "Rune knife",   "Dragon knife", ]
				|
				|#define CONST_AMMO_THROWING_AXES_LIST [   "Bronze thrownaxe",   "Iron thrownaxe",   "Steel thrownaxe",   "Mithril thrownaxe",   "Adamant thrownaxe",   "Rune thrownaxe",   "Dragon thrownaxe",   "Morrigan's throwing axe (bh)*", ]
				|
				|#define CONST_AMMO_CHINCHOMPAS_LIST [   "Chinchompa",   "Red chinchompa",   "Black chinchompa", ]
				|
				|#define CONST_AMMO_MISC_LIST [   "Atlatl dart",   "Toktz-xil-ul",   "Hunter's spear",   "Holy water",   "Mud pie", ]
				|
				|#define CONST_LOGS_LIST [   "Logs",   "Oak logs",   "Willow logs",   "Maple logs",   "Yew logs",   "Magic logs",   "Redwood logs",   "Teak logs",   "Mahogany logs",   "Achey tree logs",   "Arctic pine logs",   "Scrapey tree logs",   "Juniper logs",   "Bark",   "Blisterwood logs", ]
				|
				|#define CONST_BARS_LIST [   "Bronze bar",   "Iron bar",   "Silver bar",   "Steel bar",   "Gold bar",   "Mithril bar",   "Adamantite bar",   "Runite bar",   "Elemental metal",   "Primed bar",   "Primed mind bar",   "Blurite bar",   "Enchanted bar",   "Lovakite bar",   "'Perfect' gold bar", ]
				|
				|#define CONST_ORES_LIST [   "copper ore",   "tin ore",   "iron ore",   "silver ore",   "coal",   "gold ore",   "mithril ore",   "adamantite ore",   "runite ore",   "daeyalt ore",   "blasted ore",   "blurite ore",   "lovakite ore", ]
				|
				|#define CONST_RUNES_LIST [   "Air rune",   "Mind rune",   "Water rune",   "Earth rune",   "Fire rune",   "Body rune",   "Cosmic rune",   "Chaos rune",   "Nature rune",   "Law rune",   "Death rune",   "Sunfire rune",   "Astral rune",   "Blood rune",   "Soul rune",   "Wrath rune",   "Mist rune",   "Dust rune",   "Mud rune",   "Smoke rune",   "Steam rune",   "Lava rune",   "Aether rune", ]
				|
				|#define CONST_RUNES_ELEMENTAL_LIST [   "Air rune",   "Water rune",   "Earth rune",   "Fire rune",   "Sunfire rune", ]
				|
				|#define CONST_RUNES_CATALYTIC_LIST [   "Mind rune",   "Body rune",   "Cosmic rune",   "Chaos rune",   "Nature rune",   "Law rune",   "Death rune",   "Astral rune",   "Blood rune",   "Soul rune",   "Wrath rune", ]
				|
				|#define CONST_RUNES_COMBINATION_LIST [   "Mist rune",   "Dust rune",   "Mud rune",   "Smoke rune",   "Steam rune",   "Lava rune",   "Aether rune", ]
				|
				|#define CONST_POTIONS_LIST [   "Attack potion(*",   "Antipoison(*",   "Strength potion(*",   "Serum 207(*",   "Compost potion(*",   "Restore potion(*",   "Energy potion(*",   "Defence potion(*",   "Agility potion(*",   "Combat potion(*",   "Prayer potion(*",   "Super attack(*",   "Superantipoison(*",   "Fishing potion(*",   "Super energy(*",   "Hunter potion(*",   "Goading potion(*",   "Super strength(*",   "Prayer regeneration potion(*",   "Super restore(*",   "Sanfew serum(*",   "Super defence(*",   "Antidote+(*",   "Antifire potion(*",   "Divine super attack potion(*",   "Divine super defence potion(*",   "Divine super strength potion(*",   "Ranging potion(*",   "Divine ranging potion(*",   "Magic potion(*",   "Stamina potion(*",   "Zamorak brew(*",   "Divine magic potion(*",   "Antidote++(*",   "Bastion potion(*",   "Battlemage potion(*",   "Saradomin brew(*",   "Extended antifire(*",   "Ancient brew(*",   "Divine bastion potion(*",   "Divine battlemage potion(*",   "Anti-venom(*",   "Menaphite remedy(*",   "Super combat potion(*",   "Forgotten brew(*",   "Super antifire potion(*",   "Anti-venom+(*",   "Extended anti-venom+(*",   "Divine super combat potion(*",   "Extended super antifire(*",   "Surge potion(*",   "Blighted super restore(*", ]
				|
				|#define CONST_BARB_POTIONS_LIST [   "Attack mix*",   "Antipoison mix*",   "Relicym's mix*",   "Strength mix*",   "Restore mix*",   "Energy mix*",   "Defence mix*",   "Agility mix*",   "Combat mix*",   "Prayer mix*",   "Superattack mix*",   "Anti-poison supermix*",   "Fishing mix*",   "Super energy mix*",   "Hunting mix*",   "Super str. mix*",   "Magic essence mix*",   "Super restore mix*",   "Super def. mix*",   "Antidote+ mix*",   "Antifire mix*",   "Ranging mix*",   "Magic mix*",   "Zamorak mix*",   "Stamina mix*",   "Extended antifire mix*",   "Ancient mix*",   "Super antifire mix*",   "Extended super antifire mix*", ]
				|
				|#define CONST_FLETCHING_UNSTRUNG_SHORTBOW_LIST [   "Shortbow (u)",   "Oak shortbow (u)",   "Willow shortbow (u)",   "Maple shortbow (u)",   "Yew shortbow (u)",   "Magic shortbow (u)", ]
				|
				|#define CONST_FLETCHING_UNSTRUNG_LONGBOW_LIST [   "Longbow (u)",   "Oak Longbow (u)",   "Willow Longbow (u)",   "Maple Longbow (u)",   "Yew Longbow (u)",   "Magic Longbow (u)", ]
				|
				|#define CONST_FLETCHING_CROSSBOW_STOCK_LIST [   "Wooden stock",   "Oak stock",   "Willow stock",   "Teak stock",   "Maple stock",   "Mahogany stock",   "Yew stock",   "Magic stock", ]
				|
				|#define CONST_FLETCHING_CROSSBOW_LIMBS_LIST [   "Bronze limbs",   "Blurite limbs",   "Iron limbs",   "Steel limbs",   "Mithril limbs",   "Adamantite limbs",   "Runite limbs", ]
				|
				|#define CONST_FLETCHING_UNSTRUNG_CROSSBOW_LIST [   "Bronze crossbow (u)",   "Blurite crossbow (u)",   "Iron crossbow (u)",   "Steel crossbow (u)",   "Mithril crossbow (u)",   "Adamant crossbow (u)",   "Runite crossbow (u)", ]
				|
				|#define CONST_FLETCHING_ARROW_TIPS_LIST [   "Bronze arrowtips",   "Iron arrowtips",   "Steel arrowtips",   "Mithril arrowtips",   "Adamant arrowtips",   "Rune arrowtips",   "Amethyst arrowtips",   "Dragon arrowtips",   "Broad arrowheads",   "Wolfbone arrowtips", ]
				|
				|#define CONST_FLETCHING_DART_TIP_LIST [   "Bronze dart tip",   "Iron dart tip",   "Steel dart tip",   "Mithril dart tip",   "Adamant dart tip",   "Rune dart tip",   "Amethyst dart tip",   "Dragon dart tip", ]
				|
				|#define CONST_FLETCHING_JAVELIN_HEADS_LIST [   "Bronze javelin heads",   "Iron javelin heads",   "Steel javelin heads",   "Mithril javelin heads",   "Adamant javelin heads",   "Rune javelin heads",   "Amethyst javelin heads",   "Dragon javelin heads", ]
				|
				|#define CONST_FLETCHING_UNFINISHED_BOLTS_LIST [   "Bronze bolts (unf)",   "Iron bolts (unf)",   "Steel bolts (unf)",   "Mithril bolts (unf)",   "Adamant bolts(unf)",   "Runite bolts (unf)",   "Dragon bolts (unf)",   "Blurite bolts (unf)",   "Silver bolts (unf)",   "Unfinished broad bolts", ]
				|
				|#define CONST_FLETCHING_BOLT_TIPS_LIST [   "Opal bolt tips",   "Jade bolt tips",   "Pearl bolt tips",   "Red topaz bolt tips",   "Sapphire bolt tips",   "Emerald bolt tips",   "Ruby bolt tips",   "Diamond bolt tips",   "Amethyst bolt tips",   "Dragonstone bolt tips",   "Onyx bolt tips", ]
				|
				|#define CONST_FLETCHING_MISC_LIST [   "Bow string",   "Crossbow string",   "Unstrung comp bow",   "Javelin shaft",   "Arrow shaft",   "Ogre arrow shaft",   "Headless arrow",   "Flighted ogre arrow", ]
				|
				|#define CONST_CONSTRUCTION_PLANKS_LIST [   "Plank",   "Oak Plank",   "Teak Plank",   "Mahogany Plank", ]
				|
				|#define CONST_CONSTRUCTION_NAILS_LIST [   "Bronze Nails",   "Iron Nails",   "Steel Nails",   "Black Nails",   "Mithril Nails",   "Adamant Nails",   "Rune Nails", ]
				|
				|#define CONST_CONSTRUCTION_STONEMASON_LIST [   "Limestone brick",   "Marble block",   "Gold leaf",   "Condensed gold",   "Magic stone", ]
				|
				|#define CONST_CONSTRUCTION_MISC_LIST [   "Bolt of cloth",   "Saw", ]
				|
				|""".stripMargin
	}
}
