package dev.diona.southside.module.modules.client;

import dev.diona.southside.module.Category;
import dev.diona.southside.module.Module;
import dev.diona.southside.module.annotations.DefaultEnabled;

@DefaultEnabled
public class LiveFriendly extends Module {
    public static LiveFriendly INSTANCE;

    public LiveFriendly(String name, String description, Category category, boolean visible) {
        super(name, description, category, visible);
        INSTANCE = this;
    }
}
