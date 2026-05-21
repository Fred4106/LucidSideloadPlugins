package packetUtils;
public final class ObfuscatedNames
{

	public static final String EVENT_MOUSE_CLICK_OBFUSCATEDNAME = "ds";
	public static final String EVENT_MOUSE_CLICK_WRITE1 = "mouseX";
	public static final String EVENT_MOUSE_CLICK_METHOD_NAME1 = "ed";
	public static final String EVENT_MOUSE_CLICK_WRITE2 = "mouseY";
	public static final String EVENT_MOUSE_CLICK_METHOD_NAME2 = "ko";
	public static final String EVENT_MOUSE_CLICK_WRITE3 = "0";
	public static final String EVENT_MOUSE_CLICK_METHOD_NAME3 = "dq";
	public static final String EVENT_MOUSE_CLICK_WRITE4 = "mouseInfo";
	public static final String EVENT_MOUSE_CLICK_METHOD_NAME4 = "bw";
	public static final String[][] EVENT_MOUSE_CLICK_WRITES = new String[][]{
		{"r 8", "a 128"},
		{"a 128", "r 8"},
		{"v"},
		{"r 8", "v"},
	};
	public static final String IF_BUTTONT_OBFUSCATEDNAME = "ew";
	public static final String IF_BUTTONT_WRITE1 = "destinationWidgetId";
	public static final String IF_BUTTONT_WRITE2 = "sourceSlot";
	public static final String IF_BUTTONT_WRITE3 = "sourceItemId";
	public static final String IF_BUTTONT_WRITE4 = "sourceWidgetId";
	public static final String IF_BUTTONT_WRITE5 = "destinationSlot";
	public static final String IF_BUTTONT_WRITE6 = "destinationItemId";
	public static final String[][] IF_BUTTONT_WRITES = new String[][]{
		{"r 24", "r 16", "r 8", "v"},
		{"r 8", "a 128"},
		{"a 128", "r 8"},
		{"r 8", "v", "r 24", "r 16"},
		{"r 8", "v"},
		{"a 128", "r 8"},
	};
	public static final String IF_BUTTONX_OBFUSCATEDNAME = "dx";
	public static final String IF_BUTTONX_WRITE1 = "widgetId";
	public static final String IF_BUTTONX_METHOD_NAME1 = "az";
	public static final String IF_BUTTONX_WRITE2 = "slot";
	public static final String IF_BUTTONX_METHOD_NAME2 = "bw";
	public static final String IF_BUTTONX_WRITE3 = "itemId";
	public static final String IF_BUTTONX_METHOD_NAME3 = "bw";
	public static final String IF_BUTTONX_WRITE4 = "opCode";
	public static final String IF_BUTTONX_METHOD_NAME4 = "dq";
	public static final String[][] IF_BUTTONX_WRITES = new String[][]{
		{"r 24", "r 16", "r 8", "v"},
		{"r 8", "v"},
		{"r 8", "v"},
		{"v"},
	};
	public static final String IF_SUBOP_OBFUSCATEDNAME = "dy";
	public static final String IF_SUBOP_WRITE1 = "widgetId";
	public static final String IF_SUBOP_METHOD_NAME1 = "az";
	public static final String IF_SUBOP_WRITE2 = "slot";
	public static final String IF_SUBOP_METHOD_NAME2 = "bw";
	public static final String IF_SUBOP_WRITE3 = "itemId";
	public static final String IF_SUBOP_METHOD_NAME3 = "bw";
	public static final String IF_SUBOP_WRITE4 = "menuIndex";
	public static final String IF_SUBOP_METHOD_NAME4 = "dq";
	public static final String IF_SUBOP_WRITE5 = "subActionIndex";
	public static final String IF_SUBOP_METHOD_NAME5 = "dq";
	public static final String[][] IF_SUBOP_WRITES = new String[][]{
		{"r 24", "r 16", "r 8", "v"},
		{"r 8", "v"},
		{"r 8", "v"},
		{"v"},
		{"v"},
	};
	public static final String MOVE_GAMECLICK_OBFUSCATEDNAME = "eo";
	public static final String MOVE_GAMECLICK_WRITE1 = "5";
	public static final String MOVE_GAMECLICK_METHOD_NAME1 = "dq";
	public static final String MOVE_GAMECLICK_WRITE2 = "worldPointY";
	public static final String MOVE_GAMECLICK_METHOD_NAME2 = "eb";
	public static final String MOVE_GAMECLICK_WRITE3 = "worldPointX";
	public static final String MOVE_GAMECLICK_METHOD_NAME3 = "bw";
	public static final String MOVE_GAMECLICK_WRITE4 = "ctrlDown";
	public static final String MOVE_GAMECLICK_METHOD_NAME4 = "di";
	public static final String[][] MOVE_GAMECLICK_WRITES = new String[][]{
		{"v"},
		{"v", "r 8"},
		{"r 8", "v"},
		{"v"},
	};
	public static final String OPHELDD_OBFUSCATEDNAME = "bb";
	public static final String OPHELDD_WRITE1 = "selectedId"; // cj
	public static final String OPHELDD_WRITE2 = "selectedChildIndex"; // eb
	public static final String OPHELDD_WRITE3 = "destId"; //az
	public static final String OPHELDD_WRITE4 = "selectedItemId"; // eb
	public static final String OPHELDD_WRITE5 = "destItemId"; // ko
	public static final String OPHELDD_WRITE6 = "destChildIndex"; // ed
	public static final String[][] OPHELDD_WRITES = new String[][]{
		{"r 16", "r 24", "v", "r 8"},
		{"v", "r 8",},
		{"r 24", "r 16", "r 8", "v",},
		{"v", "r 8",},
		{"a 128", "r 8",},
		{"r 8", "a 128",},
	};
	public static final String OPLOC1_OBFUSCATEDNAME = "af";
	public static final String OPLOC1_WRITE1 = "objectId";
	public static final String OPLOC1_METHOD_NAME1 = "ko";
	public static final String OPLOC1_WRITE2 = "subop";
	public static final String OPLOC1_METHOD_NAME2 = "di";
	public static final String OPLOC1_WRITE3 = "worldPointX";
	public static final String OPLOC1_METHOD_NAME3 = "ed";
	public static final String OPLOC1_WRITE4 = "ctrlDown";
	public static final String OPLOC1_METHOD_NAME4 = "di";
	public static final String OPLOC1_WRITE5 = "worldPointY";
	public static final String OPLOC1_METHOD_NAME5 = "ko";
	public static final String[][] OPLOC1_WRITES = new String[][]{
		{"a 128", "r 8"},
		{"s 0"},
		{"r 8", "a 128"},
		{"s 0"},
		{"a 128", "r 8"},
	};
	public static final String OPLOC2_OBFUSCATEDNAME = "ce";
	public static final String OPLOC2_WRITE1 = "worldPointX";
	public static final String OPLOC2_METHOD_NAME1 = "ed";
	public static final String OPLOC2_WRITE2 = "objectId";
	public static final String OPLOC2_METHOD_NAME2 = "bw";
	public static final String OPLOC2_WRITE3 = "ctrlDown";
	public static final String OPLOC2_METHOD_NAME3 = "dq";
	public static final String OPLOC2_WRITE4 = "subop";
	public static final String OPLOC2_METHOD_NAME4 = "zu";
	public static final String OPLOC2_WRITE5 = "worldPointY";
	public static final String OPLOC2_METHOD_NAME5 = "ko";
	public static final String[][] OPLOC2_WRITES = new String[][]{
		{"r 8", "a 128"},
		{"r 8", "v"},
		{"v"},
		{"a 128"},
		{"a 128", "r 8"},
	};
	public static final String OPLOC3_OBFUSCATEDNAME = "cb";
	public static final String OPLOC3_WRITE1 = "objectId";
	public static final String OPLOC3_METHOD_NAME1 = "eb";
	public static final String OPLOC3_WRITE2 = "subop";
	public static final String OPLOC3_METHOD_NAME2 = "di";
	public static final String OPLOC3_WRITE3 = "worldPointX";
	public static final String OPLOC3_METHOD_NAME3 = "ed";
	public static final String OPLOC3_WRITE4 = "worldPointY";
	public static final String OPLOC3_METHOD_NAME4 = "ed";
	public static final String OPLOC3_WRITE5 = "ctrlDown";
	public static final String OPLOC3_METHOD_NAME5 = "di";
	public static final String[][] OPLOC3_WRITES = new String[][]{
		{"v", "r 8"},
		{"s 0"},
		{"r 8", "a 128"},
		{"r 8", "a 128"},
		{"s 0"},
	};
	public static final String OPLOC4_OBFUSCATEDNAME = "dd";
	public static final String OPLOC4_WRITE1 = "worldPointX";
	public static final String OPLOC4_METHOD_NAME1 = "ko";
	public static final String OPLOC4_WRITE2 = "worldPointY";
	public static final String OPLOC4_METHOD_NAME2 = "ko";
	public static final String OPLOC4_WRITE3 = "subop";
	public static final String OPLOC4_METHOD_NAME3 = "zu";
	public static final String OPLOC4_WRITE4 = "objectId";
	public static final String OPLOC4_METHOD_NAME4 = "bw";
	public static final String OPLOC4_WRITE5 = "ctrlDown";
	public static final String OPLOC4_METHOD_NAME5 = "dn";
	public static final String[][] OPLOC4_WRITES = new String[][]{
		{"a 128", "r 8"},
		{"a 128", "r 8"},
		{"a 128"},
		{"r 8", "v"},
		{"s 128"},
	};
	public static final String OPLOC5_OBFUSCATEDNAME = "bk";
	public static final String OPLOC5_WRITE1 = "worldPointY";
	public static final String OPLOC5_METHOD_NAME1 = "ko";
	public static final String OPLOC5_WRITE2 = "objectId";
	public static final String OPLOC5_METHOD_NAME2 = "eb";
	public static final String OPLOC5_WRITE3 = "worldPointX";
	public static final String OPLOC5_METHOD_NAME3 = "ko";
	public static final String OPLOC5_WRITE4 = "ctrlDown";
	public static final String OPLOC5_METHOD_NAME4 = "dq";
	public static final String OPLOC5_WRITE5 = "subop";
	public static final String OPLOC5_METHOD_NAME5 = "zu";
	public static final String[][] OPLOC5_WRITES = new String[][]{
		{"a 128", "r 8"},
		{"v", "r 8"},
		{"a 128", "r 8"},
		{"v"},
		{"a 128"},
	};
	public static final String OPLOCT_OBFUSCATEDNAME = "di";
	public static final String OPLOCT_WRITE1 = "worldPointY";
	public static final String OPLOCT_METHOD_NAME1 = "bw";
	public static final String OPLOCT_WRITE2 = "worldPointX";
	public static final String OPLOCT_METHOD_NAME2 = "eb";
	public static final String OPLOCT_WRITE3 = "widgetId";
	public static final String OPLOCT_METHOD_NAME3 = "bw";
	public static final String OPLOCT_WRITE4 = "itemId";
	public static final String OPLOCT_METHOD_NAME4 = "eb";
	public static final String OPLOCT_WRITE5 = "slot";
	public static final String OPLOCT_METHOD_NAME5 = "tv";
	public static final String OPLOCT_WRITE6 = "ctrlDown";
	public static final String OPLOCT_METHOD_NAME6 = "dn";
	public static final String OPLOCT_WRITE7 = "objectId";
	public static final String OPLOCT_METHOD_NAME7 = "bw";
	public static final String[][] OPLOCT_WRITES = new String[][]{
		{"r 8", "v"},
		{"v", "r 8"},
		{"r 8", "v"},
		{"v", "r 8"},
		{"v", "r 8", "r 16", "r 24"},
		{"s 128"},
		{"r 8", "v"},
	};
	public static final String OPNPC1_OBFUSCATEDNAME = "ed";
	public static final String OPNPC1_WRITE1 = "ctrlDown";
	public static final String OPNPC1_METHOD_NAME1 = "dn";
	public static final String OPNPC1_WRITE2 = "subop";
	public static final String OPNPC1_METHOD_NAME2 = "dn";
	public static final String OPNPC1_WRITE3 = "npcIndex";
	public static final String OPNPC1_METHOD_NAME3 = "ko";
	public static final String[][] OPNPC1_WRITES = new String[][]{
		{"s 128"},
		{"s 128"},
		{"a 128", "r 8"},
	};
	public static final String OPNPC2_OBFUSCATEDNAME = "cr";
	public static final String OPNPC2_WRITE1 = "subop";
	public static final String OPNPC2_METHOD_NAME1 = "dq";
	public static final String OPNPC2_WRITE2 = "npcIndex";
	public static final String OPNPC2_METHOD_NAME2 = "ed";
	public static final String OPNPC2_WRITE3 = "ctrlDown";
	public static final String OPNPC2_METHOD_NAME3 = "dn";
	public static final String[][] OPNPC2_WRITES = new String[][]{
		{"v"},
		{"r 8", "a 128"},
		{"s 128"},
	};
	public static final String OPNPC3_OBFUSCATEDNAME = "ax";
	public static final String OPNPC3_WRITE1 = "ctrlDown";
	public static final String OPNPC3_METHOD_NAME1 = "dq";
	public static final String OPNPC3_WRITE2 = "subop";
	public static final String OPNPC3_METHOD_NAME2 = "di";
	public static final String OPNPC3_WRITE3 = "npcIndex";
	public static final String OPNPC3_METHOD_NAME3 = "ko";
	public static final String[][] OPNPC3_WRITES = new String[][]{
		{"v"},
		{"s 0"},
		{"a 128", "r 8"},
	};
	public static final String OPNPC4_OBFUSCATEDNAME = "cs";
	public static final String OPNPC4_WRITE1 = "ctrlDown";
	public static final String OPNPC4_METHOD_NAME1 = "di";
	public static final String OPNPC4_WRITE2 = "npcIndex";
	public static final String OPNPC4_METHOD_NAME2 = "ko";
	public static final String OPNPC4_WRITE3 = "subop";
	public static final String OPNPC4_METHOD_NAME3 = "zu";
	public static final String[][] OPNPC4_WRITES = new String[][]{
		{"s 0"},
		{"a 128", "r 8"},
		{"a 128"},
	};
	public static final String OPNPC5_OBFUSCATEDNAME = "ah";
	public static final String OPNPC5_WRITE1 = "subop";
	public static final String OPNPC5_METHOD_NAME1 = "dq";
	public static final String OPNPC5_WRITE2 = "npcIndex";
	public static final String OPNPC5_METHOD_NAME2 = "ed";
	public static final String OPNPC5_WRITE3 = "ctrlDown";
	public static final String OPNPC5_METHOD_NAME3 = "dq";
	public static final String[][] OPNPC5_WRITES = new String[][]{
		{"v"},
		{"r 8", "a 128"},
		{"v"},
	};
	public static final String OPNPCT_OBFUSCATEDNAME = "av";
	public static final String OPNPCT_WRITE1 = "npcIndex";
	public static final String OPNPCT_METHOD_NAME1 = "ko";
	public static final String OPNPCT_WRITE2 = "itemId";
	public static final String OPNPCT_METHOD_NAME2 = "ed";
	public static final String OPNPCT_WRITE3 = "widgetId";
	public static final String OPNPCT_METHOD_NAME3 = "tv";
	public static final String OPNPCT_WRITE4 = "ctrlDown";
	public static final String OPNPCT_METHOD_NAME4 = "di";
	public static final String OPNPCT_WRITE5 = "slot";
	public static final String OPNPCT_METHOD_NAME5 = "ko";
	public static final String[][] OPNPCT_WRITES = new String[][]{
		{"a 128", "r 8"},
		{"r 8", "a 128"},
		{"v", "r 8", "r 16", "r 24"},
		{"s 0"},
		{"a 128", "r 8"},
	};
	public static final String OPOBJ1_OBFUSCATEDNAME = "ai";
	public static final String OPOBJ1_WRITE1 = "worldPointX";
	public static final String OPOBJ1_METHOD_NAME1 = "eb";
	public static final String OPOBJ1_WRITE2 = "worldPointY";
	public static final String OPOBJ1_METHOD_NAME2 = "eb";
	public static final String OPOBJ1_WRITE3 = "ctrlDown";
	public static final String OPOBJ1_METHOD_NAME3 = "dq";
	public static final String OPOBJ1_WRITE4 = "objectId";
	public static final String OPOBJ1_METHOD_NAME4 = "ko";
	public static final String OPOBJ1_WRITE5 = "subop";
	public static final String OPOBJ1_METHOD_NAME5 = "dn";
	public static final String[][] OPOBJ1_WRITES = new String[][]{
		{"v", "r 8"},
		{"v", "r 8"},
		{"v"},
		{"a 128", "r 8"},
		{"s 128"},
	};
	public static final String OPOBJ2_OBFUSCATEDNAME = "at";
	public static final String OPOBJ2_WRITE1 = "subop";
	public static final String OPOBJ2_METHOD_NAME1 = "di";
	public static final String OPOBJ2_WRITE2 = "worldPointX";
	public static final String OPOBJ2_METHOD_NAME2 = "eb";
	public static final String OPOBJ2_WRITE3 = "objectId";
	public static final String OPOBJ2_METHOD_NAME3 = "eb";
	public static final String OPOBJ2_WRITE4 = "ctrlDown";
	public static final String OPOBJ2_METHOD_NAME4 = "dn";
	public static final String OPOBJ2_WRITE5 = "worldPointY";
	public static final String OPOBJ2_METHOD_NAME5 = "ed";
	public static final String[][] OPOBJ2_WRITES = new String[][]{
		{"s 0"},
		{"v", "r 8"},
		{"v", "r 8"},
		{"s 128"},
		{"r 8", "a 128"},
	};
	public static final String OPOBJ3_OBFUSCATEDNAME = "ab";
	public static final String OPOBJ3_WRITE1 = "worldPointX";
	public static final String OPOBJ3_METHOD_NAME1 = "ed";
	public static final String OPOBJ3_WRITE2 = "worldPointY";
	public static final String OPOBJ3_METHOD_NAME2 = "ko";
	public static final String OPOBJ3_WRITE3 = "ctrlDown";
	public static final String OPOBJ3_METHOD_NAME3 = "di";
	public static final String OPOBJ3_WRITE4 = "subop";
	public static final String OPOBJ3_METHOD_NAME4 = "dn";
	public static final String OPOBJ3_WRITE5 = "objectId";
	public static final String OPOBJ3_METHOD_NAME5 = "bw";
	public static final String[][] OPOBJ3_WRITES = new String[][]{
		{"r 8", "a 128"},
		{"a 128", "r 8"},
		{"s 0"},
		{"s 128"},
		{"r 8", "v"},
	};
	public static final String OPOBJ4_OBFUSCATEDNAME = "cq";
	public static final String OPOBJ4_WRITE1 = "worldPointY";
	public static final String OPOBJ4_METHOD_NAME1 = "eb";
	public static final String OPOBJ4_WRITE2 = "ctrlDown";
	public static final String OPOBJ4_METHOD_NAME2 = "di";
	public static final String OPOBJ4_WRITE3 = "subop";
	public static final String OPOBJ4_METHOD_NAME3 = "dq";
	public static final String OPOBJ4_WRITE4 = "worldPointX";
	public static final String OPOBJ4_METHOD_NAME4 = "bw";
	public static final String OPOBJ4_WRITE5 = "objectId";
	public static final String OPOBJ4_METHOD_NAME5 = "ed";
	public static final String[][] OPOBJ4_WRITES = new String[][]{
		{"v", "r 8"},
		{"s 0"},
		{"v"},
		{"r 8", "v"},
		{"r 8", "a 128"},
	};
	public static final String OPOBJ5_OBFUSCATEDNAME = "dj";
	public static final String OPOBJ5_WRITE1 = "subop";
	public static final String OPOBJ5_METHOD_NAME1 = "zu";
	public static final String OPOBJ5_WRITE2 = "ctrlDown";
	public static final String OPOBJ5_METHOD_NAME2 = "zu";
	public static final String OPOBJ5_WRITE3 = "objectId";
	public static final String OPOBJ5_METHOD_NAME3 = "bw";
	public static final String OPOBJ5_WRITE4 = "worldPointY";
	public static final String OPOBJ5_METHOD_NAME4 = "ko";
	public static final String OPOBJ5_WRITE5 = "worldPointX";
	public static final String OPOBJ5_METHOD_NAME5 = "ed";
	public static final String[][] OPOBJ5_WRITES = new String[][]{
		{"a 128"},
		{"a 128"},
		{"r 8", "v"},
		{"a 128", "r 8"},
		{"r 8", "a 128"},
	};
	public static final String OPOBJT_OBFUSCATEDNAME = "cj";
	public static final String OPOBJT_WRITE1 = "ctrlDown";
	public static final String OPOBJT_METHOD_NAME1 = "di";
	public static final String OPOBJT_WRITE2 = "ctrlDown";
	public static final String OPOBJT_METHOD_NAME2 = "ek";
	public static final String OPOBJT_WRITE3 = "itemId";
	public static final String OPOBJT_METHOD_NAME3 = "eb";
	public static final String OPOBJT_WRITE4 = "objectId";
	public static final String OPOBJT_METHOD_NAME4 = "eb";
	public static final String OPOBJT_WRITE5 = "worldPointX";
	public static final String OPOBJT_METHOD_NAME5 = "ed";
	public static final String OPOBJT_WRITE6 = "worldPointY";
	public static final String OPOBJT_METHOD_NAME6 = "ko";
	public static final String OPOBJT_WRITE7 = "widgetId";
	public static final String OPOBJT_METHOD_NAME7 = "ed";
	public static final String[][] OPOBJT_WRITES = new String[][]{
		{"s 0"},
		{"r 8", "v", "r 24", "r 16"},
		{"v", "r 8"},
		{"v", "r 8"},
		{"r 8", "a 128"},
		{"a 128", "r 8"},
		{"r 8", "a 128"},
	};
	public static final String OPPLAYER1_OBFUSCATEDNAME = "ca";
	public static final String OPPLAYER1_WRITE1 = "ctrlDown";
	public static final String OPPLAYER1_METHOD_NAME1 = "di";
	public static final String OPPLAYER1_WRITE2 = "playerIndex";
	public static final String OPPLAYER1_METHOD_NAME2 = "eb";
	public static final String[][] OPPLAYER1_WRITES = new String[][]{
		{"s 0"},
		{"v", "r 8"},
	};
	public static final String OPPLAYER2_OBFUSCATEDNAME = "bf";
	public static final String OPPLAYER2_WRITE1 = "ctrlDown";
	public static final String OPPLAYER2_METHOD_NAME1 = "dn";
	public static final String OPPLAYER2_WRITE2 = "playerIndex";
	public static final String OPPLAYER2_METHOD_NAME2 = "ko";
	public static final String[][] OPPLAYER2_WRITES = new String[][]{
		{"s 128"},
		{"a 128", "r 8"},
	};
	public static final String OPPLAYER3_OBFUSCATEDNAME = "by";
	public static final String OPPLAYER3_WRITE1 = "ctrlDown";
	public static final String OPPLAYER3_METHOD_NAME1 = "dq";
	public static final String OPPLAYER3_WRITE2 = "playerIndex";
	public static final String OPPLAYER3_METHOD_NAME2 = "eb";
	public static final String[][] OPPLAYER3_WRITES = new String[][]{
		{"v"},
		{"v", "r 8"},
	};
	public static final String OPPLAYER4_OBFUSCATEDNAME = "aj";
	public static final String OPPLAYER4_WRITE1 = "ctrlDown";
	public static final String OPPLAYER4_METHOD_NAME1 = "dq";
	public static final String OPPLAYER4_WRITE2 = "playerIndex";
	public static final String OPPLAYER4_METHOD_NAME2 = "bw";
	public static final String[][] OPPLAYER4_WRITES = new String[][]{
		{"v"},
		{"r 8", "v"},
	};
	public static final String OPPLAYER5_OBFUSCATEDNAME = "ec";
	public static final String OPPLAYER5_WRITE1 = "ctrlDown";
	public static final String OPPLAYER5_METHOD_NAME1 = "zu";
	public static final String OPPLAYER5_WRITE2 = "playerIndex";
	public static final String OPPLAYER5_METHOD_NAME2 = "ko";
	public static final String[][] OPPLAYER5_WRITES = new String[][]{
		{"a 128"},
		{"a 128", "r 8"},
	};
	public static final String OPPLAYER6_OBFUSCATEDNAME = "db";
	public static final String OPPLAYER6_WRITE1 = "playerIndex";
	public static final String OPPLAYER6_METHOD_NAME1 = "eb";
	public static final String OPPLAYER6_WRITE2 = "ctrlDown";
	public static final String OPPLAYER6_METHOD_NAME2 = "dq";
	public static final String[][] OPPLAYER6_WRITES = new String[][]{
		{"v", "r 8"},
		{"v"},
	};
	public static final String OPPLAYER7_OBFUSCATEDNAME = "bp";
	public static final String OPPLAYER7_WRITE1 = "ctrlDown";
	public static final String OPPLAYER7_METHOD_NAME1 = "dn";
	public static final String OPPLAYER7_WRITE2 = "playerIndex";
	public static final String OPPLAYER7_METHOD_NAME2 = "ed";
	public static final String[][] OPPLAYER7_WRITES = new String[][]{
		{"s 128"},
		{"r 8", "a 128"},
	};
	public static final String OPPLAYER8_OBFUSCATEDNAME = "bt";
	public static final String OPPLAYER8_WRITE1 = "ctrlDown";
	public static final String OPPLAYER8_METHOD_NAME1 = "di";
	public static final String OPPLAYER8_WRITE2 = "playerIndex";
	public static final String OPPLAYER8_METHOD_NAME2 = "ed";
	public static final String[][] OPPLAYER8_WRITES = new String[][]{
		{"s 0"},
		{"r 8", "a 128"},
	};
	public static final String OPPLAYERT_OBFUSCATEDNAME = "am";
	public static final String OPPLAYERT_WRITE1 = "playerIndex";
	public static final String OPPLAYERT_METHOD_NAME1 = "ko";
	public static final String OPPLAYERT_WRITE2 = "slot";
	public static final String OPPLAYERT_METHOD_NAME2 = "tv";
	public static final String OPPLAYERT_WRITE3 = "widgetId";
	public static final String OPPLAYERT_METHOD_NAME3 = "ed";
	public static final String OPPLAYERT_WRITE4 = "ctrlDown";
	public static final String OPPLAYERT_METHOD_NAME4 = "dn";
	public static final String OPPLAYERT_WRITE5 = "itemId";
	public static final String OPPLAYERT_METHOD_NAME5 = "bw";
	public static final String[][] OPPLAYERT_WRITES = new String[][]{
		{"a 128", "r 8"},
		{"v", "r 8", "r 16", "r 24"},
		{"r 8", "a 128"},
		{"s 128"},
		{"r 8", "v"},
	};
	public static final String RESUME_COUNTDIALOG_OBFUSCATEDNAME = "do";
	public static final String RESUME_COUNTDIALOG_WRITE1 = "var0";
	public static final String RESUME_COUNTDIALOG_METHOD_NAME1 = "az";
	public static final String[][] RESUME_COUNTDIALOG_WRITES = new String[][]{
		{"r 24", "r 16", "r 8", "v"},
	};
	// {"r 8", "v"},

	public static final String RESUME_NAMEDIALOG_OBFUSCATEDNAME = "bz";
	public static final String RESUME_NAMEDIALOG_WRITE1 = "length";
	public static final String RESUME_NAMEDIALOG_METHOD_NAME1 = "writeByte";
	public static final String RESUME_NAMEDIALOG_WRITE2 = "string";
	public static final String RESUME_NAMEDIALOG_METHOD_NAME2 = "writeStringCp1252NullTerminated";
	public static final String[][] RESUME_NAMEDIALOG_WRITES = new String[][] {
		{"v"},
		{"strn"},
	};
	public static final String RESUME_OBJDIALOG_OBFUSCATEDNAME = "ex";
	public static final String RESUME_OBJDIALOG_WRITE1 = "var0";
	public static final String RESUME_OBJDIALOG_METHOD_NAME1 = "bw";
	public static final String[][] RESUME_OBJDIALOG_WRITES = new String[][]{
		{"r 8", "v"},
	};

	public static final String RESUME_PAUSEBUTTON_OBFUSCATEDNAME = "bu";
	public static final String RESUME_PAUSEBUTTON_WRITE1 = "var0";
	public static final String RESUME_PAUSEBUTTON_METHOD_NAME1 = "cj";
	public static final String RESUME_PAUSEBUTTON_WRITE2 = "var1";
	public static final String RESUME_PAUSEBUTTON_METHOD_NAME2 = "ko";
	public static final String[][] RESUME_PAUSEBUTTON_WRITES = new String[][]{
		{"r 16", "r 24", "v", "r 8"},
		{"a 128", "r 8"},
	};
	public static final String RESUME_STRINGDIALOG_OBFUSCATEDNAME = "cd";
	public static final String RESUME_STRINGDIALOG_WRITE1 = "length";
	public static final String RESUME_STRINGDIALOG_METHOD_NAME1 = "writeByte";
	public static final String RESUME_STRINGDIALOG_WRITE2 = "string";
	public static final String RESUME_STRINGDIALOG_METHOD_NAME2 = "writeStringCp1252NullTerminated";
	public static final String[][] RESUME_STRINGDIALOG_WRITES = new String[][] {
		{"v"},
		{"strn"},
	};
	public static final String SET_HEADING_OBFUSCATEDNAME = "df";
	public static final String SET_HEADING_WRITE1 = "orientation";
	public static final String SET_HEADING_METHOD_NAME1 = "di";
	public static final String[][] SET_HEADING_WRITES = new String[][]{
		{"s 0"},
	};
	public static final String offsetMultiplier = "228932457";
	public static final String indexMultiplier = "-661977895";
	public static final String addNodeGarbageValue = "-2131500547";
	public static final String getPacketBufferNodeGarbageValue = "-124";
	public static final String packetWriterFieldName = "aq";
	public static final String isaacCipherFieldName = "av";
	public static final String addNodeMethodName = "az";
	public static final String clientPacketClassName = "jb";
	public static final String packetWriterClassName = "df";
	public static final String classContainingGetPacketBufferNodeName = "gi";
	public static final String packetBufferNodeClassName = "jm";
	public static final String packetBufferFieldName = "ay";
	public static final String bufferOffsetField = "au";
	public static final String bufferArrayField = "al";
	public static final String MouseHandler_lastPressedTimeMillisClass = "tj";
	public static final String MouseHandler_lastPressedTimeMillisField = "af";
	public static final String mouseHandlerMillisMultiplier = "3767455460529623151";
	public static final String clientMillisMultiplier = "-6682804461438542089";
	public static final String clientMillisField = "gx";
	public static final int getAnimationMultiplier = 685222699;
	public static final int skullIconMultiplier = 0;
	public static final String skullIconField = "null";
	public static final int pathLengthMultiplier = 0;
	public static final String doActionClassName = "qd";
	public static final String doActionMethodName = "fa";
}