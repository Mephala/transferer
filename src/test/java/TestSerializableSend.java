import com.fasterxml.jackson.databind.ObjectMapper;
import com.mephalay.transferer.engine.PortInUseException;
import com.mephalay.transferer.engine.Transferer;
import mockit.integration.junit4.JMockit;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;
import java.util.Vector;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by Mephalay on 4/15/2016.
 */

@RunWith(JMockit.class)
public class TestSerializableSend {
    private static final String LARGE_FILE_PATH = "C:\\Users\\masraf\\Downloads\\calibre-64bit-2.54.0.msi";
    private static final String RECEIVE_FILE_FOLDER_PATH = "C:\\Users\\masraf\\Desktop\\Ezgim";
    private static final String VERY_LARGE_FILE_PATH = "J:\\Root\\13_Torrents\\win8.1\\Windows_8.1_Pro_X64_Activated.iso";
    Logger logger = Logger.getLogger(this.getClass());

    @Test
    public void testSendingSerializable() {
        try {
            ObjectMapper om = new ObjectMapper();
            Serializable s = new SeriazableObject();
            String serialized = om.writeValueAsString(s);
            System.out.println();
            System.out.println(serialized);
        } catch (Throwable t) {
            t.printStackTrace();
            fail();
        }
    }

    @Test
    public void testTransfererSendString() {
        try {
            Serializable s = new SeriazableObject();
            long timeout = 1000L;
            String receivedString = testTransferringSerializable(s, timeout, 4567);
            System.out.println(receivedString);
        } catch (Throwable t) {
            t.printStackTrace();
            fail();
        }
    }

    private String testTransferringSerializable(Serializable s, long timeout, int port) throws PortInUseException, IOException, InterruptedException {
        Transferer receiver = new Transferer(logger);
        TestReceiveNotifier rn = new TestReceiveNotifier();
        receiver.receiveRAM(port, rn);
        Transferer transferer = new Transferer(logger);
        transferer.transfer(s, "localhost", port);
        Thread.sleep(timeout);
        String receivedString = rn.getS();
        assertTrue(receivedString != null);
        assertTrue(receivedString.length() > 0);
        System.out.println();
        return receivedString;
    }

    @Test
    public void testSendingVeryLargeRAMObject() {
        try {
            Vector<String> largeVector = new Vector<>();
            for (int i = 0; i < 100000; i++) {
                largeVector.add(UUID.randomUUID().toString());
            }
            Serializable s = largeVector;
            long timeout = 5000L;
            String receivedString = testTransferringSerializable(s, timeout, 4568);
            System.out.println(receivedString);

        } catch (Throwable t) {
            t.printStackTrace();
            fail();
        }
    }

    @Test(expected = PortInUseException.class)
    public void testReceivingPortInUseException() throws PortInUseException, IOException {
        Transferer transferer = new Transferer(logger);
        int port = 4569;
        TestReceiveNotifier rn = new TestReceiveNotifier();
        transferer.receiveRAM(port, rn);
        transferer.receiveRAM(port, rn);
    }

    @Test
    public void testSendingLargeHddFile() {
        try {
            Transferer transferer = new Transferer(logger);
            int port = 4570;
            long timeout = 3000L;
            File sentFile = new File(LARGE_FILE_PATH);
            sendAndReceiveFile(transferer, port, timeout, sentFile);
        } catch (Throwable t) {
            t.printStackTrace();
            fail();
        }
    }

    private void sendAndReceiveFile(Transferer transferer, int port, long timeout, File sentFile) throws PortInUseException, IOException, InterruptedException {
        TestReceiveNotifier rn = new TestReceiveNotifier();
        transferer.receiveHDD(RECEIVE_FILE_FOLDER_PATH, port, rn);
        Transferer sender = new Transferer(logger);
        sender.transferHDD(sentFile, "localhost", port);
        Thread.sleep(timeout);
        File receivedFile = rn.getF();
        assertTrue(receivedFile != null);
        assertTrue(receivedFile.length() > 0);
        System.out.println();
        System.out.println("Received File:" + receivedFile.getAbsolutePath());
        assertTrue(receivedFile.length() == sentFile.length());
    }

    @Test
    public void testReadingWithFis() {
        try {
            File file = new File(LARGE_FILE_PATH);
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[4096];
            int maxCount = (int) (file.length() / 4096);
            int count = 0;
            while (fis.read(buffer) != -1) {
                count++;
                if (count > maxCount)
                    fail();
            }
            if (count > maxCount)
                fail();
            assertTrue(count <= maxCount);
        } catch (Throwable t) {
            t.printStackTrace();
            fail();
        }
    }

    @Test
    public void testSendingVeryLargeFile() {
        try {
            Transferer transferer = new Transferer(logger);
            int port = 4571;
            long timeout = 10000L;
            File sentFile = new File(VERY_LARGE_FILE_PATH);
            sendAndReceiveFile(transferer, port, timeout, sentFile);
        } catch (Throwable t) {
            t.printStackTrace();
            fail();
        }
    }

    @Test
    public void testSendingFileAndSerializableAtTheSameTime() {
        try {
            Serializable s = new SeriazableObject();
            File f = new File(LARGE_FILE_PATH);
            Transferer receiver = new Transferer(logger);
            int port = 4572;
            long timeout = 3000L;
            Transferer sender = new Transferer(logger);
            TestReceiveNotifier rn = new TestReceiveNotifier();
            receiver.receiveComposite(port, RECEIVE_FILE_FOLDER_PATH, rn);
            sender.transferComposite(s, f, "localhost", port);
            Thread.sleep(3000L);
            File receivedFile = rn.getF();
            assertTrue(receivedFile.length() == f.length());
            assertTrue(rn.getS().length() > 0);
        } catch (Throwable t) {
            t.printStackTrace();
            fail();
        }
    }

    @Test
    public void testTermination() {
        try {
            Transferer t = new Transferer(logger);
            TestReceiveNotifier rn = new TestReceiveNotifier();
            for (int i = 4573; i < 4583; i++) {
                t.receiveRAM(i, rn);
            }
            Thread.sleep(2000L);
            t.terminateAll();
            Thread.sleep(10000L);
        } catch (Throwable t) {
            t.printStackTrace();
            fail();
        }
    }


}
