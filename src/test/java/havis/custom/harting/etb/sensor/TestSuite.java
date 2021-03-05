package havis.custom.harting.etb.sensor;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ ConfigurationManagerTest.class, ConverterTest.class, ExceptionHandlerTest.class, HistoryManagerTest.class, ValidatorTest.class })
public class TestSuite {

}