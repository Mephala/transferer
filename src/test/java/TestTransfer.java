import com.mephalay.transferer.engine.Transferer;
import mockit.integration.junit4.JMockit;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.TestCase.fail;

/**
 * Created by mephala on 5/24/17.
 */
@RunWith(JMockit.class)
public class TestTransfer {
    private Logger logger = Logger.getLogger(this.getClass());

    @Test
    public void testSendingObject() {
        try {
            TestObject testObject = new TestObject();
            Transferer t = new Transferer(logger);
            t.transfer(testObject, "192.168.0.109", 1553);
        } catch (Throwable t) {
            t.printStackTrace();
            fail();
        }
    }
}
