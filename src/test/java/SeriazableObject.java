import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by Mephalay on 4/15/2016.
 */
public class SeriazableObject implements Serializable{
    private String name = "Dev Object";
    private String age = "Yas kac panp?";
    private BigDecimal realAge = new BigDecimal("1555");

    public SeriazableObject(){

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public BigDecimal getRealAge() {
        return realAge;
    }

    public void setRealAge(BigDecimal realAge) {
        this.realAge = realAge;
    }
}
