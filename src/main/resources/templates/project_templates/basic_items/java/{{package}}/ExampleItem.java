package {{package}};

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.CreativeModeTab;

public class ExampleItem extends Item {
    public ExampleItem() {
        super(new Properties()
                .stacksTo(64)
                .rarity(Rarity.COMMENT)
                .tab(CreativeModeTab.TAB_MISC)
        );
    }
}