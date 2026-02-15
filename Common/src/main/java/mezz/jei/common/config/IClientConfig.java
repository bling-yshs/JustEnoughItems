package mezz.jei.common.config;

import mezz.jei.common.config.file.IConfigListener;

import java.util.List;

public interface IClientConfig {
	int minRecipeGuiHeight = 175;
	int defaultRecipeGuiHeight = 350;
	boolean defaultCenterSearchBar = false;

	boolean isCenterSearchBarEnabled();

	boolean isLowMemorySlowSearchEnabled();

	boolean isCatchRenderErrorsEnabled();

	boolean isCheatToHotbarUsingHotkeysEnabled();

	GiveMode getGiveMode();

	boolean isDragToRearrangeBookmarksEnabled();

	boolean isLookupHistoryEnabled();

	void setLookupHistoryEnabled(boolean enabled);

	void addLookupHistoryEnabledListener(IConfigListener<Boolean> listener);

	int getMaxLookupHistoryRows();

	int getMaxLookupHistoryIngredients();

	HistoryDisplaySide getLookupHistoryDisplaySide();

	void addLookupHistoryDisplaySideListener(IConfigListener<HistoryDisplaySide> listener);

	int getDragDelayMs();

	int getMaxRecipeGuiHeight();

	List<IngredientSortStage> getIngredientSorterStages();

	boolean isHideSingleIngredientTagsEnabled();

	boolean isLookupFluidContentsEnabled();

	boolean isAddingBookmarksToFrontEnabled();
}
