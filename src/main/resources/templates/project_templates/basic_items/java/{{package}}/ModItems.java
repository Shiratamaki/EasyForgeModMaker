package {{package}};

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, "{{modId}}");

    public static final RegistryObject<Item> EXAMPLE_ITEM = ITEMS.register("example_item",
            ExampleItem::new);

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}