// Copyright (c) 2013, Webit Team. All Rights Reserved.
package webit.script;

import java.io.IOException;
import webit.script.exceptions.ResourceNotFoundException;
import webit.script.test.util.DiscardOutputStream;

/**
 *
 * @author Zqq
 */
public class TestForVisualVM {

    public static class Start {

        public static void start() {
        }
    }

    //@Test
    public void test() throws ResourceNotFoundException, IOException {
        int times = 10000;
        Engine engine = EngineManager.getEngine();

        Template template = engine.getTemplate("/firstTmpl.wtl");
        DiscardOutputStream out = new DiscardOutputStream();
        template.prepareTemplate();

        //webit.script.TestForVisualVM.Start
        TestForVisualVM.Start.start();
        for (int i = 0; i < times; i++) {
            //template.reset();
            //template.prepareTemplate();
            template.merge(null, out);
        }
    }
}
