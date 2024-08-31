package com.fredplugins.giantsfoundry;

//import com.fredplugins.giantsfoundry.enums.CommissionType;
//import com.fredplugins.giantsfoundry.enums.Mould;

import java.util.LinkedHashMap;
//import java.util.Map;
//import scala.collection.immutable;
import javax.inject.Inject;

import com.fredplugins.giantsfoundry.enums.SCommissionType;
import com.fredplugins.giantsfoundry.enums.SCommissionType$;
import com.fredplugins.giantsfoundry.enums.SMould;
import com.fredplugins.giantsfoundry.enums.SMould$;
import net.runelite.api.Client;
import net.runelite.api.ScriptID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import scala.Option;

public class MouldHelper
{
	static final int MOULD_LIST_PARENT = 47054857;
	static final int DRAW_MOULD_LIST_SCRIPT = 6093;
	static final int REDRAW_MOULD_LIST_SCRIPT = 6095;
	static final int RESET_MOULD_SCRIPT = 6108;
	public static final int SELECT_MOULD_SCRIPT = 6098;
	static final int SWORD_TYPE_1_VARBIT = 13907; // 4=Broad
	static final int SWORD_TYPE_2_VARBIT = 13908; // 3=Flat
	private static final int DISABLED_TEXT_COLOR = 0x9f9f9f;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private FredsGiantsFoundryConfig config;

	public void selectBest(int scriptId)
	{
		Widget bestWidget = SMould$.MODULE$.selectBest(client).getOrElse(null);

		if (bestWidget != null)
		{
			bestWidget.setTextColor(config.mouldTextColour().getRGB());
			if (scriptId == DRAW_MOULD_LIST_SCRIPT || scriptId == REDRAW_MOULD_LIST_SCRIPT)
			{
				Widget scrollBar = client.getWidget(718, 11);
				Widget scrollList = client.getWidget(718, 9);
				if (scrollBar != null && scrollList != null)
				{
					int height = scrollList.getHeight();
					int scrollMax = scrollList.getScrollHeight();
					clientThread.invokeLater(() ->
					{
							client.runScript(
								ScriptID.UPDATE_SCROLLBAR,
								scrollBar.getId(),
								scrollList.getId(),
								Math.min(bestWidget.getOriginalY() - 2, scrollMax - height));
					});
				}
			}
		}
	}
}
