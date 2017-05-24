import com.fasterxml.jackson.databind.ObjectMapper;
import com.mephalay.transferer.ReceiveNotifier;
import com.mephalay.transferer.engine.Transferer;
import mockit.integration.junit4.JMockit;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static junit.framework.TestCase.fail;

/**
 * Created by mephala on 5/24/17.
 */
@RunWith(JMockit.class)
public class TestListener {
    private Logger logger = Logger.getLogger(this.getClass());

    @Test
    public void testCreatingListener() {
        try {
            Transferer t = new Transferer(logger);
            ReceiveNotifier receiveNotifier = new ReceiveNotifier() {
                @Override
                public void notifyRAMReceive(String s) {
                    System.out.println("Received:" + s);
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        TestObject testObject = objectMapper.readValue(s, TestObject.class);
                        System.out.println("Object received successfully :" + testObject);
                    } catch (Throwable t) {
                        t.printStackTrace();
                        fail();
                    }
                }

                @Override
                public void notifyHDDReceive(File f) {

                }

                @Override
                public void notifyComposite(String s, File f) {

                }
            };
            t.receiveRAM(1553, receiveNotifier);
            while (true) ;
        } catch (Throwable t) {
            t.printStackTrace();
            fail();
        }
    }
}
