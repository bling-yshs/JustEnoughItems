package mezz.jei.gui.config;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.util.ServerConfigPathUtil;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.bookmarks.IBookmark;
import mezz.jei.gui.bookmarks.IngredientBookmark;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class BookmarkConfig implements IBookmarkConfig {
	private static final Logger LOGGER = LogManager.getLogger();

	static final String MARKER_STACK = "T:";
	static final String MARKER_INGREDIENT = "I:";
	static final String LEGACY_MARKER_OTHER = "O:";

	private final Path jeiConfigurationDir;

	private static Optional<Path> getPath(Path jeiConfigurationDir) {
		return ServerConfigPathUtil.getWorldPath(jeiConfigurationDir)
			.flatMap(configPath -> {
				try {
					configPath = Files.createDirectories(configPath);
				} catch (IOException e) {
					LOGGER.error("Unable to create bookmark config folder: {}", configPath);
					return Optional.empty();
				}
				Path path = configPath.resolve("bookmarks.ini");
				return Optional.of(path);
			});
	}

	public BookmarkConfig(Path jeiConfigurationDir) {
		this.jeiConfigurationDir = jeiConfigurationDir;
	}

	@Override
	public void saveBookmarks(IIngredientManager ingredientManager, List<IBookmark> ingredientList) {
		getPath(jeiConfigurationDir)
			.ifPresent(path -> {
				List<String> strings = new ArrayList<>();
				for (IBookmark bookmark : ingredientList) {
					ITypedIngredient<?> typedIngredient = bookmark.getElement().getTypedIngredient();
					if (typedIngredient.getIngredient() instanceof ItemStack stack) {
						strings.add(MARKER_STACK + stack.save(new CompoundTag()));
					} else {
						strings.add(MARKER_INGREDIENT + getUid(ingredientManager, typedIngredient));
					}
				}

				try {
					Files.write(path, strings);
				} catch (IOException e) {
					LOGGER.error("Failed to save bookmarks list to file {}", path, e);
				}
			});
	}

	@Override
	public void loadBookmarks(IIngredientManager ingredientManager, BookmarkList bookmarkList) {
		getPath(jeiConfigurationDir)
			.ifPresent(path -> {
				if (!Files.exists(path)) {
					return;
				}
				List<String> lines;
				try {
					lines = Files.readAllLines(path);
				} catch (IOException e) {
					LOGGER.error("Failed to load bookmarks from file {}", path, e);
					return;
				}

				IIngredientHelper<ItemStack> itemStackHelper = ingredientManager.getIngredientHelper(VanillaTypes.ITEM_STACK);
				for (String line : lines) {
					IBookmark bookmark = null;
					if (line.startsWith(MARKER_STACK)) {
						String itemStackAsJson = line.substring(MARKER_STACK.length());
						bookmark = loadItemStackBookmark(itemStackHelper, ingredientManager, itemStackAsJson);
					} else if (line.startsWith(MARKER_INGREDIENT)) {
						String uid = line.substring(MARKER_INGREDIENT.length());
						bookmark = loadIngredientBookmark(ingredientManager, uid);
					} else if (line.startsWith(LEGACY_MARKER_OTHER)) {
						String uid = line.substring(LEGACY_MARKER_OTHER.length());
						bookmark = loadIngredientBookmark(ingredientManager, uid);
					} else {
						LOGGER.error("Failed to load unknown bookmark type:\n{}", line);
					}
					if (bookmark != null) {
						bookmarkList.addToListWithoutNotifying(bookmark, false);
					}
				}
				bookmarkList.notifyListenersOfChange();
			});
	}

	static @Nullable IBookmark loadItemStackBookmark(
		IIngredientHelper<ItemStack> itemStackHelper,
		IIngredientManager ingredientManager,
		String itemStackAsJson
	) {
		try {
			CompoundTag itemStackAsNbt = TagParser.parseTag(itemStackAsJson);
			ItemStack itemStack = ItemStack.of(itemStackAsNbt);
			if (!itemStack.isEmpty()) {
				ItemStack normalized = itemStackHelper.normalizeIngredient(itemStack);
				Optional<ITypedIngredient<ItemStack>> typedIngredient = ingredientManager.createTypedIngredient(VanillaTypes.ITEM_STACK, normalized);
				if (typedIngredient.isEmpty()) {
					LOGGER.warn("Failed to load bookmarked ItemStack from json string, the item no longer exists:\n{}", itemStackAsJson);
				} else {
					return IngredientBookmark.create(typedIngredient.get(), ingredientManager);
				}
			} else {
				LOGGER.warn("Failed to load bookmarked ItemStack from json string, the item is empty:\n{}", itemStackAsJson);
			}
		} catch (CommandSyntaxException e) {
			LOGGER.error("Failed to load bookmarked ItemStack from json string:\n{}", itemStackAsJson, e);
		}
		return null;
	}

	static @Nullable IBookmark loadIngredientBookmark(IIngredientManager ingredientManager, String uid) {
		Collection<IIngredientType<?>> otherIngredientTypes = ingredientManager.getRegisteredIngredientTypes()
			.stream()
			.filter(i -> !i.equals(VanillaTypes.ITEM_STACK))
			.toList();
		Optional<ITypedIngredient<?>> typedIngredient = getNormalizedIngredientByUid(ingredientManager, otherIngredientTypes, uid);
		if (typedIngredient.isEmpty()) {
			LOGGER.error("Failed to load unknown bookmarked ingredient with uid:\n{}", uid);
			return null;
		}
		return IngredientBookmark.create(typedIngredient.get(), ingredientManager);
	}

	static <T> String getUid(IIngredientManager ingredientManager, ITypedIngredient<T> typedIngredient) {
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(typedIngredient.getType());
		return ingredientHelper.getUniqueId(typedIngredient.getIngredient(), UidContext.Ingredient);
	}

	private static Optional<ITypedIngredient<?>> getNormalizedIngredientByUid(IIngredientManager ingredientManager, Collection<IIngredientType<?>> ingredientTypes, String uid) {
		return ingredientTypes.stream()
			.map(t -> getNormalizedIngredientByUid(ingredientManager, t, uid))
			.flatMap(Optional::stream)
			.findFirst();
	}

	private static <T> Optional<ITypedIngredient<?>> getNormalizedIngredientByUid(IIngredientManager ingredientManager, IIngredientType<T> ingredientType, String uid) {
		return ingredientManager.getIngredientByUid(ingredientType, uid)
			.map(i -> {
				IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
				return ingredientHelper.normalizeIngredient(i);
			})
			.flatMap(i -> ingredientManager.createTypedIngredient(ingredientType, i));
	}
}
