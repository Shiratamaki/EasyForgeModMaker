package com.easyforge.model;

import java.util.ArrayList;
import java.util.List;

public class ModProject {
    private String modId;
    private String modName;
    private String version;
    private String author;
    private String outputPath;
    private String minecraftVersion;
    private String forgeVersion;
    private String mainClassPackage;

    // 已有的列表
    private List<ItemData> items = new ArrayList<>();
    private List<BlockData> blocks = new ArrayList<>();
    private List<RecipeData> recipes = new ArrayList<>();
    private List<Dependency> dependencies = new ArrayList<>();
    private List<FluidData> fluids = new ArrayList<>();
    private List<EntityData> entities = new ArrayList<>();
    private List<BiomeData> biomes = new ArrayList<>();
    private List<EnchantmentData> enchantments = new ArrayList<>();
    private List<StructureData> structures = new ArrayList<>();
    private List<SmithingRecipeData> smithingRecipes = new ArrayList<>();
    private List<DimensionData> dimensions = new ArrayList<>();

    // 新增的列表
    private List<SmeltingRecipeData> smeltingRecipes = new ArrayList<>();
    private List<BrewingRecipeData> brewingRecipes = new ArrayList<>();
    private List<AdvancementData> advancements = new ArrayList<>();
    private List<CommandData> commands = new ArrayList<>();
    private List<TagData> tags = new ArrayList<>();
    private List<LootTableData> lootTables = new ArrayList<>();
    private List<GameRuleData> gameRules = new ArrayList<>();
    private List<WorldGenData> worldGenConfigs = new ArrayList<>();

    public ModProject() {}

    // ========== 基础字段 Getters/Setters ==========
    public String getModId() { return modId; }
    public void setModId(String modId) { this.modId = modId; }
    public String getModName() { return modName; }
    public void setModName(String modName) { this.modName = modName; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getOutputPath() { return outputPath; }
    public void setOutputPath(String outputPath) { this.outputPath = outputPath; }
    public String getMinecraftVersion() { return minecraftVersion; }
    public void setMinecraftVersion(String minecraftVersion) { this.minecraftVersion = minecraftVersion; }
    public String getForgeVersion() { return forgeVersion; }
    public void setForgeVersion(String forgeVersion) { this.forgeVersion = forgeVersion; }
    public String getMainClassPackage() { return mainClassPackage; }
    public void setMainClassPackage(String mainClassPackage) { this.mainClassPackage = mainClassPackage; }

    // ========== 已有列表 Getters/Setters ==========
    public List<ItemData> getItems() { return items; }
    public void setItems(List<ItemData> items) { this.items = items; }
    public List<BlockData> getBlocks() { return blocks; }
    public void setBlocks(List<BlockData> blocks) { this.blocks = blocks; }
    public List<RecipeData> getRecipes() { return recipes; }
    public void setRecipes(List<RecipeData> recipes) { this.recipes = recipes; }
    public List<Dependency> getDependencies() { return dependencies; }
    public void setDependencies(List<Dependency> dependencies) { this.dependencies = dependencies; }
    public List<FluidData> getFluids() { return fluids; }
    public void setFluids(List<FluidData> fluids) { this.fluids = fluids; }
    public List<EntityData> getEntities() { return entities; }
    public void setEntities(List<EntityData> entities) { this.entities = entities; }
    public List<BiomeData> getBiomes() { return biomes; }
    public void setBiomes(List<BiomeData> biomes) { this.biomes = biomes; }
    public List<EnchantmentData> getEnchantments() { return enchantments; }
    public void setEnchantments(List<EnchantmentData> enchantments) { this.enchantments = enchantments; }
    public List<StructureData> getStructures() { return structures; }
    public void setStructures(List<StructureData> structures) { this.structures = structures; }
    public List<SmithingRecipeData> getSmithingRecipes() { return smithingRecipes; }
    public void setSmithingRecipes(List<SmithingRecipeData> smithingRecipes) { this.smithingRecipes = smithingRecipes; }
    public List<DimensionData> getDimensions() { return dimensions; }
    public void setDimensions(List<DimensionData> dimensions) { this.dimensions = dimensions; }

    // ========== 新增列表 Getters/Setters ==========
    public List<SmeltingRecipeData> getSmeltingRecipes() { return smeltingRecipes; }
    public void setSmeltingRecipes(List<SmeltingRecipeData> smeltingRecipes) { this.smeltingRecipes = smeltingRecipes; }
    public List<BrewingRecipeData> getBrewingRecipes() { return brewingRecipes; }
    public void setBrewingRecipes(List<BrewingRecipeData> brewingRecipes) { this.brewingRecipes = brewingRecipes; }
    public List<AdvancementData> getAdvancements() { return advancements; }
    public void setAdvancements(List<AdvancementData> advancements) { this.advancements = advancements; }
    public List<CommandData> getCommands() { return commands; }
    public void setCommands(List<CommandData> commands) { this.commands = commands; }
    public List<TagData> getTags() { return tags; }
    public void setTags(List<TagData> tags) { this.tags = tags; }
    public List<LootTableData> getLootTables() { return lootTables; }
    public void setLootTables(List<LootTableData> lootTables) { this.lootTables = lootTables; }
    public List<GameRuleData> getGameRules() { return gameRules; }
    public void setGameRules(List<GameRuleData> gameRules) { this.gameRules = gameRules; }
    public List<WorldGenData> getWorldGenConfigs() { return worldGenConfigs; }
    public void setWorldGenConfigs(List<WorldGenData> worldGenConfigs) { this.worldGenConfigs = worldGenConfigs; }
}