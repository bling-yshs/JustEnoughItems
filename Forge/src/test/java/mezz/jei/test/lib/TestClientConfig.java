package mezz.jei.test.lib;

import mezz.jei.common.config.GiveMode;
import mezz.jei.common.config.HistoryDisplaySide;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IngredientSortStage;
import mezz.jei.common.config.file.IConfigListener;

import java.util.List;

public class TestClientConfig implements IClientConfig {
	private final boolean lowMemorySlowSearchEnabled;

	public TestClientConfig(boolean lowMemorySlowSearchEnabled) {
		this.lowMemorySlowSearchEnabled = lowMemorySlowSearchEnabled;
	}

	@Override
	public boolean isCenterSearchBarEnabled() {
		return false;
	}

	@Override
	public boolean isLowMemorySlowSearchEnabled() {
		return lowMemorySlowSearchEnabled;
	}

	@Override
	public boolean isCatchRenderErrorsEnabled() {
		return false;
	}

	@Override
	public boolean isCheatToHotbarUsingHotkeysEnabled() {
		return false;
	}

	@Override
	public boolean isAddingBookmarksToFrontEnabled() {
		return false;
	}

	@Override
	public boolean isLookupFluidContentsEnabled() {
		return false;
	}

	@Override
	public GiveMode getGiveMode() {
		return GiveMode.INVENTORY;
	}

	@Override
	public boolean isDragToRearrangeBookmarksEnabled() {
		return false;
	}

	@Override
	public boolean isLookupHistoryEnabled() {
		return false;
	}

	@Override
	public void setLookupHistoryEnabled(boolean enabled) {
	}

	@Override
	public void addLookupHistoryEnabledListener(IConfigListener<Boolean> listener) {
	}

	@Override
	public int getMaxLookupHistoryRows() {
		return 0;
	}

	@Override
	public int getMaxLookupHistoryIngredients() {
		return 0;
	}

	@Override
	public HistoryDisplaySide getLookupHistoryDisplaySide() {
		return HistoryDisplaySide.LEFT;
	}

	@Override
	public void addLookupHistoryDisplaySideListener(IConfigListener<HistoryDisplaySide> listener) {
	}

	@Override
	public int getDragDelayMs() {
		return 0;
	}

	@Override
	public boolean isHideSingleIngredientTagsEnabled() {
		return true;
	}

	@Override
	public int getMaxRecipeGuiHeight() {
		return 500;
	}

	@Override
	public List<IngredientSortStage> getIngredientSorterStages() {
		return List.of();
	}
}
