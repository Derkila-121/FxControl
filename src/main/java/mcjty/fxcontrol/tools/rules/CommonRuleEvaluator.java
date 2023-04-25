package mcjty.fxcontrol.tools.rules;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mcjty.fxcontrol.ErrorHandler;
import mcjty.fxcontrol.tools.cache.StructureCache;
import mcjty.fxcontrol.tools.typed.AttributeMap;
import mcjty.fxcontrol.tools.typed.Key;
import mcjty.fxcontrol.tools.varia.LookAtTools;
import mcjty.fxcontrol.tools.varia.Tools;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static mcjty.fxcontrol.tools.rules.CommonRuleKeys.*;

public class CommonRuleEvaluator {

    protected final List<BiFunction<Object, IEventQuery, Boolean>> checks = new ArrayList<>();
    private final Logger logger;
    private final IModRuleCompatibilityLayer compatibility;

    public CommonRuleEvaluator(AttributeMap map, Logger logger, IModRuleCompatibilityLayer compatibility) {
        this.logger = logger;
        this.compatibility = compatibility;
        addChecks(map);
    }

    // Rules in this routine are sorted so that the more expensive checks are added later
    protected void addChecks(AttributeMap map) {
        if (map.has(RANDOM)) {
            addRandomCheck(map);
        }
        if (map.has(DIMENSION)) {
            addDimensionCheck(map);
        }
        if (map.has(DIMENSION_MOD)) {
            addDimensionModCheck(map);
        }
        if (map.has(MINTIME)) {
            addMinTimeCheck(map);
        }
        if (map.has(MAXTIME)) {
            addMaxTimeCheck(map);
        }

        if (map.has(MINHEIGHT)) {
            addMinHeightCheck(map);
        }
        if (map.has(MAXHEIGHT)) {
            addMaxHeightCheck(map);
        }
        if (map.has(WEATHER)) {
            addWeatherCheck(map);
        }
        if (map.has(CATEGORY)) {
            addCategoryCheck(map);
        }
        if (map.has(DIFFICULTY)) {
            addDifficultyCheck(map);
        }

        if (map.has(MINSPAWNDIST)) {
            addMinSpawnDistCheck(map);
        }
        if (map.has(MAXSPAWNDIST)) {
            addMaxSpawnDistCheck(map);
        }

        if (map.has(MINLIGHT)) {
            addMinLightCheck(map);
        }
        if (map.has(MAXLIGHT)) {
            addMaxLightCheck(map);
        }

        if (map.has(MINDIFFICULTY)) {
            addMinAdditionalDifficultyCheck(map);
        }
        if (map.has(MAXDIFFICULTY)) {
            addMaxAdditionalDifficultyCheck(map);
        }

        if (map.has(SEESKY)) {
            addSeeSkyCheck(map);
        }
        if (map.has(SLIME)) {
            addSlimeChunkCheck(map);
        }
        if (map.has(BLOCK)) {
            addBlocksCheck(map);
        }
        if (map.has(BIOME)) {
            addBiomesCheck(map);
        }
        if (map.has(BIOMETYPE)) {
            addBiomeTypesCheck(map);
        }
        if (map.has(HELMET)) {
            addHelmetCheck(map, HELMET, false);
        }
        if (map.has(CHESTPLATE)) {
            addChestplateCheck(map, CHESTPLATE, false);
        }
        if (map.has(LEGGINGS)) {
            addLeggingsCheck(map, LEGGINGS, false);
        }
        if (map.has(BOOTS)) {
            addBootsCheck(map, BOOTS, false);
        }
        if (map.has(LACKHELMET)) {
            addHelmetCheck(map, LACKHELMET, true);
        }
        if (map.has(LACKCHESTPLATE)) {
            addChestplateCheck(map, LACKCHESTPLATE, true);
        }
        if (map.has(LACKLEGGINGS)) {
            addLeggingsCheck(map, LACKLEGGINGS, true);
        }
        if (map.has(LACKBOOTS)) {
            addBootsCheck(map, LACKBOOTS, true);
        }
        if (map.has(PLAYER_HELDITEM)) {
            addHeldItemCheck(map, PLAYER_HELDITEM, false);
        }
        if (map.has(HELDITEM)) {
            addHeldItemCheck(map, HELDITEM, false);
        }
        if (map.has(LACKHELDITEM)) {
            addHeldItemCheck(map, LACKHELDITEM, true);
        }
        if (map.has(OFFHANDITEM)) {
            addOffHandItemCheck(map, OFFHANDITEM, false);
        }
        if (map.has(LACKOFFHANDITEM)) {
        	addOffHandItemCheck(map, LACKOFFHANDITEM, true);
        }
        if (map.has(BOTHHANDSITEM)) {
            addBothHandsItemCheck(map);
        }

        if (map.has(STRUCTURE)) {
            addStructureCheck(map);
        }

        if (map.has(STATE)) {
            if (compatibility.hasEnigmaScript()) {
                addStateCheck(map);
            } else {
                logger.warn("EnigmaScript is missing: this test cannot work!");
            }
        }
        if (map.has(PSTATE)) {
            if (compatibility.hasEnigmaScript()) {
                addPStateCheck(map);
            } else {
                logger.warn("EnigmaScript is missing: this test cannot work!");
            }
        }

        if (map.has(SUMMER)) {
            if (compatibility.hasSereneSeasons()) {
                addSummerCheck(map);
            } else {
                logger.warn("Serene Seaons is missing: this test cannot work!");
            }
        }
        if (map.has(WINTER)) {
            if (compatibility.hasSereneSeasons()) {
                addWinterCheck(map);
            } else {
                logger.warn("Serene Seaons is missing: this test cannot work!");
            }
        }
        if (map.has(SPRING)) {
            if (compatibility.hasSereneSeasons()) {
                addSpringCheck(map);
            } else {
                logger.warn("Serene Seaons is missing: this test cannot work!");
            }
        }
        if (map.has(AUTUMN)) {
            if (compatibility.hasSereneSeasons()) {
                addAutumnCheck(map);
            } else {
                logger.warn("Serene Seaons is missing: this test cannot work!");
            }
        }
        if (map.has(GAMESTAGE)) {
            if (compatibility.hasGameStages()) {
                addGameStageCheck(map);
            } else {
                logger.warn("Game Stages is missing: the 'gamestage' test cannot work!");
            }
        }
        if (map.has(INCITY)) {
            if (compatibility.hasLostCities()) {
                addInCityCheck(map);
            } else {
                logger.warn("The Lost Cities is missing: the 'incity' test cannot work!");
            }
        }
        if (map.has(INSTREET)) {
            if (compatibility.hasLostCities()) {
                addInStreetCheck(map);
            } else {
                logger.warn("The Lost Cities is missing: the 'instreet' test cannot work!");
            }
        }
        if (map.has(INSPHERE)) {
            if (compatibility.hasLostCities()) {
                addInSphereCheck(map);
            } else {
                logger.warn("The Lost Cities is missing: the 'insphere' test cannot work!");
            }
        }
        if (map.has(INBUILDING)) {
            if (compatibility.hasLostCities()) {
                addInBuildingCheck(map);
            } else {
                logger.warn("The Lost Cities is missing: the 'inbuilding' test cannot work!");
            }
        }

        if (map.has(AMULET)) {
            if (compatibility.hasBaubles()) {
                addBaubleCheck(map, AMULET, compatibility::getAmuletSlots);
            } else {
                logger.warn("Baubles is missing: this test cannot work!");
            }
        }
        if (map.has(RING)) {
            if (compatibility.hasBaubles()) {
                addBaubleCheck(map, RING, compatibility::getRingSlots);
            } else {
                logger.warn("Baubles is missing: this test cannot work!");
            }
        }
        if (map.has(BELT)) {
            if (compatibility.hasBaubles()) {
                addBaubleCheck(map, BELT, compatibility::getBeltSlots);
            } else {
                logger.warn("Baubles is missing: this test cannot work!");
            }
        }
        if (map.has(TRINKET)) {
            if (compatibility.hasBaubles()) {
                addBaubleCheck(map, TRINKET, compatibility::getTrinketSlots);
            } else {
                logger.warn("Baubles is missing: this test cannot work!");
            }
        }
        if (map.has(HEAD)) {
            if (compatibility.hasBaubles()) {
                addBaubleCheck(map, HEAD, compatibility::getHeadSlots);
            } else {
                logger.warn("Baubles is missing: this test cannot work!");
            }
        }
        if (map.has(BODY)) {
            if (compatibility.hasBaubles()) {
                addBaubleCheck(map, BODY, compatibility::getBodySlots);
            } else {
                logger.warn("Baubles is missing: this test cannot work!");
            }
        }
        if (map.has(CHARM)) {
            if (compatibility.hasBaubles()) {
                addBaubleCheck(map, CHARM, compatibility::getCharmSlots);
            } else {
                logger.warn("Baubles is missing: this test cannot work!");
            }
        }
    }

    private static Random rnd = new Random();

    private void addRandomCheck(AttributeMap map) {
        final float r = map.get(RANDOM);
        checks.add((event,query) -> rnd.nextFloat() < r);
    }

    private void addSeeSkyCheck(AttributeMap map) {
        if (map.get(SEESKY)) {
            checks.add((event,query) -> query.getWorld(event).canSeeSkyFromBelowWater(query.getPos(event)));
        } else {
            checks.add((event,query) -> !query.getWorld(event).canSeeSkyFromBelowWater(query.getPos(event)));
        }
    }

    private void addSlimeChunkCheck(AttributeMap map) {
        if (map.get(SLIME)) {
            checks.add((event,query) -> isSlimeChunk(new ChunkPos(query.getPos(event)), query.getWorld(event)));
        } else {
            checks.add((event,query) -> !isSlimeChunk(new ChunkPos(query.getPos(event)), query.getWorld(event)));
        }
    }

    private boolean isSlimeChunk(ChunkPos cp, LevelAccessor world) {
        long seed = 0;
        if (world instanceof WorldGenLevel level) {
            seed = level.getSeed();
        }
        return WorldgenRandom.seedSlimeChunk(cp.x, cp.z, seed, 987234911L).nextInt(10) == 0;
    }

    private void addDimensionCheck(AttributeMap map) {
        List<ResourceKey<Level>> dimensions = map.getList(DIMENSION);
        if (dimensions.size() == 1) {
            ResourceKey<Level> dim = dimensions.get(0);
            checks.add((event,query) -> Tools.getDimensionKey(query.getWorld(event)).equals(dim));
        } else {
            Set<ResourceKey<Level>> dims = new HashSet<>(dimensions);
            checks.add((event,query) -> dims.contains(Tools.getDimensionKey(query.getWorld(event))));
        }
    }

    private void addDimensionModCheck(AttributeMap map) {
        List<String> dimensions = map.getList(DIMENSION_MOD);
        if (dimensions.size() == 1) {
            String dimmod = dimensions.get(0);
            checks.add((event,query) -> Tools.getDimensionKey(query.getWorld(event)).location().getNamespace().equals(dimmod));
        } else {
            Set<String> dims = new HashSet<>(dimensions);
            checks.add((event,query) -> dims.contains(Tools.getDimensionKey(query.getWorld(event)).location().getNamespace()));
        }
    }

    private void addDifficultyCheck(AttributeMap map) {
        String difficulty = map.get(DIFFICULTY).toLowerCase();
        Difficulty diff = Difficulty.byName(difficulty);
        if (diff != null) {
            Difficulty finalDiff = diff;
            checks.add((event,query) -> query.getWorld(event).getDifficulty() == finalDiff);
        } else {
            ErrorHandler.error("Unknown difficulty '" + difficulty + "'! Use one of 'easy', 'normal', 'hard',  or 'peaceful'");
        }
    }

    private void addWeatherCheck(AttributeMap map) {
        String weather = map.get(WEATHER);
        boolean raining = weather.toLowerCase().startsWith("rain");
        boolean thunder = weather.toLowerCase().startsWith("thunder");
        if (raining) {
            checks.add((event,query) -> {
                LevelAccessor world = query.getWorld(event);
                if (world instanceof Level) {
                    return ((Level) world).isRaining();
                } else {
                    return false;
                }
            });
        } else if (thunder) {
            checks.add((event, query) -> {
                LevelAccessor world = query.getWorld(event);
                if (world instanceof Level) {
                    return ((Level) world).isThundering();
                } else {
                    return false;
                }
            });
        } else {
            ErrorHandler.error("Unknown weather '" + weather + "'! Use 'rain' or 'thunder'");
        }
    }

    private void addCategoryCheck(AttributeMap map) {
        List<String> list = map.getList(CATEGORY);
        Set<Biome.BiomeCategory> categories = list.stream().map(s -> Biome.BiomeCategory.byName(s.toLowerCase())).collect(Collectors.toSet());
        checks.add((event,query) -> {
            Holder<Biome> biome = query.getWorld(event).getBiome(query.getPos(event));
            return categories.contains(Biome.getBiomeCategory(biome));
        });
    }


    private void addStructureCheck(AttributeMap map) {
        String structure = map.get(STRUCTURE);
        checks.add((event,query) -> StructureCache.CACHE.isInStructure(query.getWorld(event), structure, query.getPos(event)));
    }

    private void addBiomesCheck(AttributeMap map) {
        List<String> biomes = map.getList(BIOME);
        if (biomes.size() == 1) {
            String biomename = biomes.get(0);
            checks.add((event,query) -> {
                Holder<Biome> biome = query.getWorld(event).getBiome(query.getPos(event));
                return Tools.getBiomeId(biome).equals(biomename);
            });
        } else {
            Set<String> biomenames = new HashSet<>(biomes);
            checks.add((event,query) -> {
                Holder<Biome> biome = query.getWorld(event).getBiome(query.getPos(event));
                String biomeId = Tools.getBiomeId(biome);
                return biomenames.contains(biomeId);
            });
        }
    }

    private void addBiomeTypesCheck(AttributeMap map) {
        List<String> biomeTypes = map.getList(BIOMETYPE);
        Set<Biome> biomes = new HashSet<>();
        biomeTypes.stream().map(s -> BiomeManager.BiomeType.valueOf(s.toUpperCase())).
                forEach(type -> BiomeManager.getBiomes(type).stream().forEach(t -> biomes.add(ForgeRegistries.BIOMES.getValue(t.getKey().getRegistryName()))));

        checks.add((event,query) -> {
            Holder<Biome> biome = query.getWorld(event).getBiome(query.getPos(event));
            return biomes.contains(biome.value());
        });
    }

    private static final int[] EMPTYINTS = new int[0];

    public static <T extends Comparable<T>> BlockState set(BlockState state, Property<T> property, String value) {
        Optional<T> optionalValue = property.getValue(value);
        if (optionalValue.isPresent()) {
            return state.setValue(property, optionalValue.get());
        } else {
            return state;
        }
    }

    @Nonnull
    private BiFunction<Object, IEventQuery, BlockPos> parseOffset(String json) {
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(json);
        JsonObject obj = element.getAsJsonObject();

        int offsetX;
        int offsetY;
        int offsetZ;

        if (obj.has("offset")) {
            JsonObject offset = obj.getAsJsonObject("offset");
            offsetX = offset.has("x") ? offset.get("x").getAsInt() : 0;
            offsetY = offset.has("y") ? offset.get("y").getAsInt() : 0;
            offsetZ = offset.has("z") ? offset.get("z").getAsInt() : 0;
        } else {
            offsetX = 0;
            offsetY = 0;
            offsetZ = 0;
        }

        if (obj.has("look")) {
            return (event, query) -> {
                HitResult result = LookAtTools.getMovingObjectPositionFromPlayer(query.getWorld(event), query.getPlayer(event), false);
                if (result instanceof BlockHitResult) {
                    return ((BlockHitResult) result).getBlockPos().offset(offsetX, offsetY, offsetZ);
                } else {
                    return query.getValidBlockPos(event).offset(offsetX, offsetY, offsetZ);
                }
            };

        }
        return (event, query) -> query.getValidBlockPos(event).offset(offsetX, offsetY, offsetZ);
    }

    private static boolean testBlockStateSafe(LevelAccessor world, BlockPos pos, Block block) {
        LevelChunk chunk = world.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
        if (chunk != null) {
            BlockState state = world.getBlockState(pos);
            return state.getBlock() == block;
        } else {
            return false;
        }
    }

    private static boolean testBlockStateSafe(LevelAccessor world, BlockPos pos, BlockState block) {
        LevelChunk chunk = world.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
        if (chunk != null) {
            BlockState state = world.getBlockState(pos);
            return state == block;
        } else {
            return false;
        }
    }

    @Nullable
    private BiPredicate<LevelAccessor, BlockPos> parseBlock(String json) {
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(json);
        if (element.isJsonPrimitive()) {
            String blockname = element.getAsString();
            if (blockname.startsWith("tag:")) {
                ResourceLocation tagname = new ResourceLocation(blockname.substring(4));
                TagKey<Block> key = TagKey.create(Registry.BLOCK.key(), tagname);
                return (world, pos) -> {
                    BlockState state = world.getBlockState(pos);
                    return state.is(key);
                };
            } else {
                if (!ForgeRegistries.BLOCKS.containsKey(new ResourceLocation(blockname))) {
                    ErrorHandler.error("Block '" + blockname + "' is not valid!");
                    return null;
                }
                Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockname));
                return (world, pos) -> testBlockStateSafe(world, pos, block);
            }
        } else if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            BiPredicate<LevelAccessor, BlockPos> test;
            if (obj.has("tag")) {
                ResourceLocation tagname = new ResourceLocation(obj.get("tag").getAsString());
                TagKey<Block> key = TagKey.create(Registry.BLOCK.key(), tagname);
                test = (world, pos) -> {
                    BlockState state = world.getBlockState(pos);
                    return state.is(key);
                };
            } else if (obj.has("block")) {
                String blockname = obj.get("block").getAsString();
                if (!ForgeRegistries.BLOCKS.containsKey(new ResourceLocation(blockname))) {
                    ErrorHandler.error("Block '" + blockname + "' is not valid!");
                    return null;
                }
                Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockname));
                if (obj.has("properties")) {
                    BlockState blockState = block.defaultBlockState();
                    JsonArray propArray = obj.get("properties").getAsJsonArray();
                    for (JsonElement el : propArray) {
                        JsonObject propObj = el.getAsJsonObject();
                        String name = propObj.get("name").getAsString();
                        String value = propObj.get("value").getAsString();
                        for (Property<?> key : blockState.getProperties()) {
                            if (name.equals(key.getName())) {
                                blockState = set(blockState, key, value);
                            }
                        }
                    }
                    BlockState finalBlockState = blockState;
                    test = (world, pos) -> testBlockStateSafe(world, pos, finalBlockState);
                } else {
                    test = (world, pos) -> testBlockStateSafe(world, pos, block);
                }
            } else {
                test = (world, pos) -> true;
            }

            if (obj.has("mod")) {
                String mod = obj.get("mod").getAsString();
                BiPredicate<LevelAccessor, BlockPos> finalTest = test;
                test = (world, pos) -> {
                    LevelChunk chunk = world.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
                    if (chunk != null) {
                        return finalTest.test(world, pos) && mod.equals(world.getBlockState(pos).getBlock().getRegistryName().getNamespace());
                    } else {
                        return false;
                    }
                };
            }
            if (obj.has("energy")) {
                Predicate<Integer> energy = getExpression(obj.get("energy"));
                if (energy != null) {
                    Direction side;
                    if (obj.has("side")) {
                        side = Direction.byName(obj.get("side").getAsString().toLowerCase());
                    } else {
                        side = null;
                    }
                    BiPredicate<LevelAccessor, BlockPos> finalTest = test;
                    test = (world, pos) -> finalTest.test(world, pos) && energy.test(getEnergy(world, pos, side));
                }
            }
            if (obj.has("contains")) {
                Direction side;
                if (obj.has("side")) {
                    side = Direction.byName(obj.get("energyside").getAsString().toLowerCase());
                } else {
                    side = null;
                }
                List<Predicate<ItemStack>> items = getItems(obj.get("contains"));
                BiPredicate<LevelAccessor, BlockPos> finalTest = test;
                test = (world, pos) -> finalTest.test(world, pos) && contains(world, pos, side, items);
            }

            return test;
        } else {
            ErrorHandler.error("Block description '" + json + "' is not valid!");
        }
        return null;
    }

    protected List<Predicate<ItemStack>> getItems(JsonElement itemObj) {
        List<Predicate<ItemStack>> items = new ArrayList<>();
        if (itemObj.isJsonObject()) {
            Predicate<ItemStack> matcher = getMatcher(itemObj.getAsJsonObject(), logger);
            if (matcher != null) {
                items.add(matcher);
            }
        } else if (itemObj.isJsonArray()) {
            for (JsonElement element : itemObj.getAsJsonArray()) {
                JsonObject obj = element.getAsJsonObject();
                Predicate<ItemStack> matcher = getMatcher(obj, logger);
                if (matcher != null) {
                    items.add(matcher);
                }
            }
        } else {
            ErrorHandler.error("Item description is not valid!");
        }
        return items;
    }

    private void addBlocksCheck(AttributeMap map) {
        BiFunction<Object, IEventQuery, BlockPos> posFunction;
        if (map.has(BLOCKOFFSET)) {
            posFunction = parseOffset(map.get(BLOCKOFFSET));
        } else {
            posFunction = (event, query) -> query.getValidBlockPos(event);
        }

        List<String> blocks = map.getList(BLOCK);
        if (blocks.size() == 1) {
            String json = blocks.get(0);
            BiPredicate<LevelAccessor, BlockPos> blockMatcher = parseBlock(json);
            if (blockMatcher != null) {
                checks.add((event, query) -> {
                    BlockPos pos = posFunction.apply(event, query);
                    return pos != null && blockMatcher.test(query.getWorld(event), pos);
                });
            }
        } else {
            List<BiPredicate<LevelAccessor, BlockPos>> blockMatchers = new ArrayList<>();
            for (String block : blocks) {
                BiPredicate<LevelAccessor, BlockPos> blockMatcher = parseBlock(block);
                if (blockMatcher == null) {
                    return;
                }
                blockMatchers.add(blockMatcher);
            }

            checks.add((event,query) -> {
                BlockPos pos = posFunction.apply(event, query);
                if (pos != null) {
                    LevelAccessor world = query.getWorld(event);
                    for (BiPredicate<LevelAccessor, BlockPos> matcher : blockMatchers) {
                        if (matcher.test(world, pos)) {
                            return true;
                        }
                    }
                }
                return false;
            });
        }
    }

    private static boolean isMatchingOreId(int[] oreIDs, int oreId) {
        if (oreIDs.length > 0) {
            for (int id : oreIDs) {
                if (id == oreId) {
                    return true;
                }
            }
        }
        return false;
    }


    private void addMinTimeCheck(AttributeMap map) {
        final int mintime = map.get(MINTIME);
        checks.add((event,query) -> {
            LevelAccessor world = query.getWorld(event);
            if (world instanceof Level) {
                long time = ((Level)world).getDayTime();
                return (time % 24000) >= mintime;
            } else {
                return false;
            }
        });
    }

    private void addMaxTimeCheck(AttributeMap map) {
        final int maxtime = map.get(MAXTIME);
        checks.add((event,query) -> {
            LevelAccessor world = query.getWorld(event);
            if (world instanceof Level) {
                long time = ((Level)world).getDayTime();
                return (time % 24000) <= maxtime;
            } else {
                return false;
            }
        });
    }

    private void addMinSpawnDistCheck(AttributeMap map) {
        final Float d = map.get(MINSPAWNDIST) * map.get(MINSPAWNDIST);
        checks.add((event,query) -> {
            BlockPos pos = query.getPos(event);
            ServerLevel sw = Tools.getServerWorld(query.getWorld(event));
            double sqdist = pos.distSqr(sw.getSharedSpawnPos());
            return sqdist >= d;
        });
    }

    private void addMaxSpawnDistCheck(AttributeMap map) {
        final Float d = map.get(MAXSPAWNDIST) * map.get(MAXSPAWNDIST);
        checks.add((event,query) -> {
            BlockPos pos = query.getPos(event);
            ServerLevel sw = Tools.getServerWorld(query.getWorld(event));
            double sqdist = pos.distSqr(sw.getSharedSpawnPos());
            return sqdist <= d;
        });
    }


    private void addMinLightCheck(AttributeMap map) {
        final int minlight = map.get(MINLIGHT);
        checks.add((event,query) -> {
            BlockPos pos = query.getPos(event);
            return query.getWorld(event).getBrightness(LightLayer.BLOCK, pos) >= minlight;
        });
    }

    private void addMaxLightCheck(AttributeMap map) {
        final int maxlight = map.get(MAXLIGHT);
        checks.add((event,query) -> {
            BlockPos pos = query.getPos(event);
            return query.getWorld(event).getBrightness(LightLayer.BLOCK, pos) <= maxlight;
        });
    }

    private void addMinAdditionalDifficultyCheck(AttributeMap map) {
        final Float mindifficulty = map.get(MINDIFFICULTY);
        checks.add((event,query) -> query.getWorld(event).getCurrentDifficultyAt(query.getPos(event)).getEffectiveDifficulty() >= mindifficulty);
    }

    private void addMaxAdditionalDifficultyCheck(AttributeMap map) {
        final Float maxdifficulty = map.get(MAXDIFFICULTY);
        checks.add((event,query) -> query.getWorld(event).getCurrentDifficultyAt(query.getPos(event)).getEffectiveDifficulty() <= maxdifficulty);
    }

    private void addMaxHeightCheck(AttributeMap map) {
        final int maxheight = map.get(MAXHEIGHT);
        checks.add((event,query) -> query.getY(event) <= maxheight);
    }

    private void addMinHeightCheck(AttributeMap map) {
        final int minheight = map.get(MINHEIGHT);
        checks.add((event,query) -> query.getY(event) >= minheight);
    }


    public boolean match(Object event, IEventQuery query) {
        for (BiFunction<Object, IEventQuery, Boolean> rule : checks) {
            if (!rule.apply(event, query)) {
                return false;
            }
        }
        return true;
    }

    @Nullable
    private static Predicate<Integer> getExpressionInteger(String expression, boolean onlyInt) {
        try {
            if (expression.startsWith(">=")) {
                int amount = Integer.parseInt(expression.substring(2));
                return i -> i >= amount;
            }
            if (expression.startsWith(">")) {
                int amount = Integer.parseInt(expression.substring(1));
                return i -> i > amount;
            }
            if (expression.startsWith("<=")) {
                int amount = Integer.parseInt(expression.substring(2));
                return i -> i <= amount;
            }
            if (expression.startsWith("<")) {
                int amount = Integer.parseInt(expression.substring(1));
                return i -> i < amount;
            }
            if (expression.startsWith("=")) {
                int amount = Integer.parseInt(expression.substring(1));
                return i -> i == amount;
            }
            if (expression.startsWith("!=") || expression.startsWith("<>")) {
                int amount = Integer.parseInt(expression.substring(2));
                return i -> i != amount;
            }

            if (expression.contains("-")) {
                String[] split = StringUtils.split(expression, "-");
                int amount1 = Integer.parseInt(split[0]);
                int amount2 = Integer.parseInt(split[1]);
                return i -> i >= amount1 && i <= amount2;
            }

            int amount = Integer.parseInt(expression);
            return i -> i == amount;
        } catch (NumberFormatException e) {
            if (onlyInt) {
                ErrorHandler.error("Bad expression '" + expression + "'!");
            }
            return null;
        }
    }

    private static Predicate<Integer> getExpression(JsonElement element) {
        if (element.isJsonPrimitive()) {
            if (element.getAsJsonPrimitive().isNumber()) {
                int amount = element.getAsInt();
                return i -> i == amount;
            } else {
                return getExpressionInteger(element.getAsString(), true);
            }
        } else {
            ErrorHandler.error("Bad expression!");
            return null;
        }
    }

    private static Predicate<CompoundTag> getExpressionOrString(JsonElement element, String tag) {
        if (element.isJsonPrimitive()) {
            if (element.getAsJsonPrimitive().isNumber()) {
                int amount = element.getAsInt();
                return tagCompound -> tagCompound.getInt(tag) == amount;
            } else if (element.getAsJsonPrimitive().isBoolean()) {
                boolean v = element.getAsBoolean();
                return tagCompound -> tagCompound.getBoolean(tag) == v;
            } else {
                String str = element.getAsString();
                Predicate<Integer> predicate = getExpressionInteger(str, false);
                if (predicate == null) {
                    return tagCompound -> str.equals(tagCompound.getString(tag));
                }
                return tagCompound -> predicate.test(tagCompound.getInt(tag));
            }
        } else {
            ErrorHandler.error("Bad expression!");
            return null;
        }
    }

    private static Predicate<ItemStack> getMatcher(String name, Logger logger) {
        ItemStack stack = Tools.parseStack(name, logger);
        if (!stack.isEmpty()) {
            // Stack matching
            if (name.contains("/") && name.contains("@")) {
                return s -> ItemStack.isSame(s, stack) && ItemStack.tagMatches(s, stack);
            } else if (name.contains("/")) {
                return s -> ItemStack.isSameIgnoreDurability(s, stack) && ItemStack.tagMatches(s, stack);
            } else if (name.contains("@")) {
                return s -> ItemStack.isSame(s, stack);
            } else {
                return s -> s.getItem() == stack.getItem();
            }
        }
        return null;
    }

    private static Predicate<ItemStack> getMatcher(JsonObject obj, Logger logger) {
        if (obj.has("empty")) {
            boolean empty = obj.get("empty").getAsBoolean();
            return s -> s.isEmpty() == empty;
        }

        String name = obj.get("item").getAsString();
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(name));
        if (item == null) {
            ErrorHandler.error("Unknown item '" + name + "'!");
            return null;
        }

        Predicate<ItemStack> test;
        if (obj.has("damage")) {
            Predicate<Integer> damage = getExpression(obj.get("damage"));
            if (damage == null) {
                return null;
            }
            test = s -> s.getItem() == item && damage.test(s.getDamageValue());
        } else {
            test = s -> s.getItem() == item;
        }

        if (obj.has("count")) {
            Predicate<Integer> count = getExpression(obj.get("count"));
            if (count != null) {
                Predicate<ItemStack> finalTest = test;
                test = s -> finalTest.test(s) && count.test(s.getCount());
            }
        }
        if (obj.has("tag")) {
            ResourceLocation tagname = new ResourceLocation(obj.get("tag").getAsString());
            TagKey<Item> key = TagKey.create(Registry.ITEM.key(), tagname);
            Predicate<ItemStack> finalTest = test;
            test = s -> finalTest.test(s) && s.is(key);
        }
        if (obj.has("mod")) {
            String mod = obj.get("mod").getAsString();
            Predicate<ItemStack> finalTest = test;
            test = s -> finalTest.test(s) && "mod".equals(s.getItem().getRegistryName().getNamespace());
        }
        if (obj.has("nbt")) {
            List<Predicate<CompoundTag>> nbtMatchers = getNbtMatchers(obj, logger);
            if (nbtMatchers != null) {
                Predicate<ItemStack> finalTest = test;
                test = s -> finalTest.test(s) && nbtMatchers.stream().allMatch(p -> p.test(s.getTag()));
            }
        }
        if (obj.has("energy")) {
            Predicate<Integer> energy = getExpression(obj.get("energy"));
            if (energy != null) {
                Predicate<ItemStack> finalTest = test;
                test = s -> finalTest.test(s) && energy.test(getEnergy(s));
            }
        }

        return test;
    }

    private static int getEnergy(ItemStack stack) {
        return stack.getCapability(CapabilityEnergy.ENERGY).map(IEnergyStorage::getEnergyStored).orElse(0);
    }

    private boolean contains(LevelAccessor world, BlockPos pos, @Nullable Direction side, @Nonnull List<Predicate<ItemStack>> matchers) {
        BlockEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity != null) {
            return tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side).map(h -> {
                for (int i = 0 ; i < h.getSlots() ; i++) {
                    ItemStack stack = h.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        for (Predicate<ItemStack> matcher : matchers) {
                            if (matcher.test(stack)) {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }).orElse(false);
        }
        return false;
    }

    private int getEnergy(LevelAccessor world, BlockPos pos, @Nullable Direction side) {
        BlockEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity != null) {
            return tileEntity.getCapability(CapabilityEnergy.ENERGY, side).map(IEnergyStorage::getEnergyStored).orElse(0);
        }
        return 0;
    }

    private static List<Predicate<CompoundTag>> getNbtMatchers(JsonObject obj, Logger logger) {
        JsonArray nbtArray = obj.getAsJsonArray("nbt");
        return getNbtMatchers(nbtArray, logger);
    }

    private static List<Predicate<CompoundTag>> getNbtMatchers(JsonArray nbtArray, Logger logger) {
        List<Predicate<CompoundTag>> nbtMatchers = new ArrayList<>();
        for (JsonElement element : nbtArray) {
            JsonObject o = element.getAsJsonObject();
            String tag = o.get("tag").getAsString();
            if (o.has("contains")) {
                List<Predicate<CompoundTag>> subMatchers = getNbtMatchers(o.getAsJsonArray("contains"), logger);
                nbtMatchers.add(tagCompound -> {
                    if (tagCompound != null) {
                        ListTag list = tagCompound.getList(tag, Tag.TAG_COMPOUND);
                        for (Tag base : list) {
                            for (Predicate<CompoundTag> matcher : subMatchers) {
                                if (matcher.test((CompoundTag) base)) {
                                    return true;
                                }
                            }
                        }
                    }
                    return false;
                });
            } else {
                Predicate<CompoundTag> nbt = getExpressionOrString(o.get("value"), tag);
                if (nbt != null) {
                    nbtMatchers.add(nbt);
                }
            }

        }
        return nbtMatchers;
    }


    public static List<Predicate<ItemStack>> getItems(List<String> itemNames, Logger logger) {
        List<Predicate<ItemStack>> items = new ArrayList<>();
        for (String json : itemNames) {
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(json);
            if (element.isJsonPrimitive()) {
                String name = element.getAsString();
                Predicate<ItemStack> matcher = getMatcher(name, logger);
                if (matcher != null) {
                    items.add(matcher);
                }
            } else if (element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();
                Predicate<ItemStack> matcher = getMatcher(obj, logger);
                if (matcher != null) {
                    items.add(matcher);
                }
            } else {
                ErrorHandler.error("Item description '" + json + "' is not valid!");
            }
        }
        return items;
    }

    public void addHelmetCheck(AttributeMap map, Key<String> key, Boolean lacking) {
        List<Predicate<ItemStack>> items = getItems(map.getList(key), logger);
        addArmorCheck(items, EquipmentSlot.HEAD, lacking);
    }

    public void addChestplateCheck(AttributeMap map, Key<String> key, Boolean lacking) {
        List<Predicate<ItemStack>> items = getItems(map.getList(key), logger);
        addArmorCheck(items, EquipmentSlot.CHEST, lacking);
    }

    public void addLeggingsCheck(AttributeMap map, Key<String> key, Boolean lacking) {
        List<Predicate<ItemStack>> items = getItems(map.getList(key), logger);
        addArmorCheck(items, EquipmentSlot.LEGS, lacking);
    }

    public void addBootsCheck(AttributeMap map, Key<String> key, Boolean lacking) {
        List<Predicate<ItemStack>> items = getItems(map.getList(key), logger);
        addArmorCheck(items, EquipmentSlot.FEET, lacking);
    }

    private void addArmorCheck(List<Predicate<ItemStack>> items, EquipmentSlot slot, Boolean lacking) {
        checks.add((event,query) -> {
            Player player = query.getPlayer(event);
            if (player != null) {
                ItemStack armorItem = player.getItemBySlot(slot);
                if (!armorItem.isEmpty()) {
                    for (Predicate<ItemStack> item : items) {
                        if (item.test(armorItem)) {
                            return !lacking;
                        }
                    }
                }
            }
            return lacking;
        });
    }

    public void addHeldItemCheck(AttributeMap map, Key<String> key, Boolean lacking) {
        List<Predicate<ItemStack>> items = getItems(map.getList(key), logger);
        checks.add((event,query) -> {
            Player player = query.getPlayer(event);
            if (player != null) {
                ItemStack mainhand = player.getMainHandItem();
                if (!mainhand.isEmpty()) {
                    for (Predicate<ItemStack> item : items) {
                        if (item.test(mainhand)) {
                            return !lacking;
                        }
                    }
                }
            }
            return lacking;
        });
    }

    public void addOffHandItemCheck(AttributeMap map, Key<String> key, Boolean lacking) {
        List<Predicate<ItemStack>> items = getItems(map.getList(key), logger);
        checks.add((event,query) -> {
            Player player = query.getPlayer(event);
            if (player != null) {
                ItemStack offhand = player.getOffhandItem();
                if (!offhand.isEmpty()) {
                    for (Predicate<ItemStack> item : items) {
                        if (item.test(offhand)) {
                            return !lacking;
                        }
                    }
                }
            }
            return lacking;
        });
    }

    public void addBothHandsItemCheck(AttributeMap map) {
        List<Predicate<ItemStack>> items = getItems(map.getList(BOTHHANDSITEM), logger);
        checks.add((event,query) -> {
            Player player = query.getPlayer(event);
            if (player != null) {
                ItemStack offhand = player.getOffhandItem();
                if (!offhand.isEmpty()) {
                    for (Predicate<ItemStack> item : items) {
                        if (item.test(offhand)) {
                            return true;
                        }
                    }
                }
                ItemStack mainhand = player.getMainHandItem();
                if (!mainhand.isEmpty()) {
                    for (Predicate<ItemStack> item : items) {
                        if (item.test(mainhand)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        });
    }

    private void addStateCheck(AttributeMap map) {
        String s = map.get(STATE);
        String[] split = StringUtils.split(s, '=');
        String state;
        String value;
        try {
            state = split[0];
            value = split[1];
        } catch (Exception e) {
            ErrorHandler.error("Bad state=value specifier '" + s + "'!");
            return;
        }

        checks.add((event, query) -> value.equals(compatibility.getState(query.getWorld(event), state)));
    }

    private void addPStateCheck(AttributeMap map) {
        String s = map.get(PSTATE);
        String[] split = StringUtils.split(s, '=');
        String state;
        String value;
        try {
            state = split[0];
            value = split[1];
        } catch (Exception e) {
            ErrorHandler.error("Bad state=value specifier '" + s + "'!");
            return;
        }

        checks.add((event, query) -> value.equals(compatibility.getPlayerState(query.getPlayer(event), state)));
    }

    private void addSummerCheck(AttributeMap map) {
        Boolean s = map.get(SUMMER);
        checks.add((event, query) -> s == compatibility.isSummer(query.getWorld(event)));
    }

    private void addWinterCheck(AttributeMap map) {
        Boolean s = map.get(WINTER);
        checks.add((event, query) -> s == compatibility.isWinter(query.getWorld(event)));
    }

    private void addSpringCheck(AttributeMap map) {
        Boolean s = map.get(SPRING);
        checks.add((event, query) -> s == compatibility.isSpring(query.getWorld(event)));
    }

    private void addAutumnCheck(AttributeMap map) {
        Boolean s = map.get(AUTUMN);
        checks.add((event, query) -> s == compatibility.isAutumn(query.getWorld(event)));
    }

    private void addGameStageCheck(AttributeMap map) {
        String stage = map.get(GAMESTAGE);
        checks.add((event, query) -> compatibility.hasGameStage(query.getPlayer(event), stage));
    }

    private void addInCityCheck(AttributeMap map) {
        if (map.get(INCITY)) {
            checks.add((event,query) -> compatibility.isCity(query, event));
        } else {
            checks.add((event,query) -> !compatibility.isCity(query, event));
        }
    }

    private void addInStreetCheck(AttributeMap map) {
        if (map.get(INSTREET)) {
            checks.add((event,query) -> compatibility.isStreet(query, event));
        } else {
            checks.add((event,query) -> !compatibility.isStreet(query, event));
        }
    }

    private void addInSphereCheck(AttributeMap map) {
        if (map.get(INSPHERE)) {
            checks.add((event,query) -> compatibility.inSphere(query, event));
        } else {
            checks.add((event,query) -> !compatibility.inSphere(query, event));
        }
    }

    private void addInBuildingCheck(AttributeMap map) {
        if (map.get(INBUILDING)) {
            checks.add((event,query) -> compatibility.isBuilding(query, event));
        } else {
            checks.add((event,query) -> !compatibility.isBuilding(query, event));
        }
    }

    public void addBaubleCheck(AttributeMap map, Key<String> key, Supplier<int[]> slotSupplier) {
        List<Predicate<ItemStack>> items = getItems(map.getList(key), logger);
        checks.add((event,query) -> {
            Player player = query.getPlayer(event);
            if (player != null) {
                for (int slot : slotSupplier.get()) {
                    ItemStack stack = compatibility.getBaubleStack(player, slot);
                    if (!stack.isEmpty()) {
                        for (Predicate<ItemStack> item : items) {
                            if (item.test(stack)) {
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        });
    }
}
