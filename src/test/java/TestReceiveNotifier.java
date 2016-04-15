import com.mephalay.transferer.ReceiveNotifier;

import java.io.File;

/**
 * Created by Mephalay on 4/15/2016.
 */
public class TestReceiveNotifier implements ReceiveNotifier {

    private String s;
    private File f;

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }

    @Override
    public void notifyRAMReceive(String s) {
        this.s = s;
    }

    public File getF() {
        return f;
    }

    public void setF(File f) {
        this.f = f;
    }

    @Override
    public void notifyHDDReceive(File f) {
        this.f = f;


    }

    @Override
    public void notifyComposite(String s, File f) {
        this.s = s;
        this.f = f;
    }
}
