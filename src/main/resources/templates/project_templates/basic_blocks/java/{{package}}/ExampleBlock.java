package {{package}};

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.item.CreativeModeTab;

public class ExampleBlock extends Block {
    public ExampleBlock() {
        super(BlockBehaviour.Properties.of(Material.STONE)
                .strength(3.0f, 6.0f)
                .requiresCorrectToolForDrops()
        );
    }
}