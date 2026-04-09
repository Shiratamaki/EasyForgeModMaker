package {{package}};

import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, "{{modId}}");

    public static final RegistryObject<Block> EXAMPLE_BLOCK = BLOCKS.register("example_block",
            ExampleBlock::new);

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }
}