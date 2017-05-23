package gr.cyberstream.workflow.engine.controller.v2.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ DefinitionControllerTest.class, ExecutionControllerTest.class, PublicFormControllerTest.class,
		RealmControllerTest.class, TaskControllerTest.class })
public class ControllerTests {

}
