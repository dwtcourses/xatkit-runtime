package fr.zelus.jarvis.stubs;

import fr.zelus.jarvis.core.JarvisAction;
import fr.zelus.jarvis.core.JarvisModule;
import fr.zelus.jarvis.intent.RecognizedIntent;
import fr.zelus.jarvis.orchestration.ActionInstance;
import fr.zelus.jarvis.stubs.action.StubJarvisAction;
import org.apache.commons.configuration2.Configuration;

public class StubJarvisModule extends JarvisModule {

    private StubJarvisAction jarvisAction;

    public StubJarvisModule(Configuration configuration) {
        super(configuration);
        this.jarvisAction = new StubJarvisAction();
    }

    public StubJarvisAction getAction() {
        return jarvisAction;
    }

    @Override
    public JarvisAction createJarvisAction(ActionInstance actionInstance, RecognizedIntent intent) {
        return jarvisAction;
    }

}
