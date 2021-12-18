package uks.gmde2122;

import org.fulib.builder.ClassModelDecorator;
import org.fulib.builder.ClassModelManager;

public class GenModel implements ClassModelDecorator {


    class JGraph extends JNode {

    }

    class JNode {

    }

    class JRule {

    }

    class ApplyRuleParams {

    }

    @Override
    public void decorate(ClassModelManager mm) {
        mm.haveNestedClasses(GenModel.class);
    }
}
