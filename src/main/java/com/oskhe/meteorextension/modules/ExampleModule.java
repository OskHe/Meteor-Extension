package com.oskhe.meteorextension.modules;

import meteordevelopment.meteorclient.systems.modules.Module;
import com.oskhe.meteorextension.MeteorExtension;

public class ExampleModule extends Module {
    public ExampleModule() {
        super(MeteorExtension.CATEGORY_MAIN, "example", "An example module in a custom category.");
    }
}
