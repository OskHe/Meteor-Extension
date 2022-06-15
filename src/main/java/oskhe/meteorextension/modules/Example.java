package oskhe.meteorextension.modules;

import oskhe.meteorextension.MeteorExtension;
import meteordevelopment.meteorclient.systems.modules.Module;

public class Example extends Module {
    public Example() {
        super(MeteorExtension.CATEGORY_MAIN, "example", "An example module in a custom category.");
    }
}
