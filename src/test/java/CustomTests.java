import com.mephalay.transferer.engine.Transferer;
import mockit.integration.junit4.JMockit;
import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.fail;

/**
 * Created by Mephalay on 4/15/2016.
 */
@RunWith(JMockit.class)
public class CustomTests {

    @Ignore
    public void testSendingToAcerSrv() {
        try {
            Logger logger = Logger.getLogger(this.getClass());
            Transferer t = new Transferer(logger);
            t.transferHDD(new File("J:\\Root\\4_Entertainment"), "192.168.0.10", 1888);
            t.terminateAll();
        } catch (Throwable t) {
            t.printStackTrace();
            fail();
        }
    }
}
