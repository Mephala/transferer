import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by mephala on 5/24/17.
 */
public class TestObject implements Serializable {
    private String stringField = "string";
    private Boolean booleanField = Boolean.TRUE;
    private Integer integerField = Integer.valueOf(8);
    private Double doubleField = Double.valueOf(88d);
    private Float floatField = Float.valueOf(93f);
    private BigDecimal bigDecimalField = new BigDecimal("124125112215251251521.2121512521521515125");
    private Character characterField = Character.valueOf('g');
    private byte[] byteArrayField;

    public TestObject() {
        this.byteArrayField = new byte[1024];
        for (int i = 0; i < 1024; i++) {
            byteArrayField[i] = 1;
        }
    }

    public String getStringField() {
        return stringField;
    }

    public void setStringField(String stringField) {
        this.stringField = stringField;
    }

    public Boolean getBooleanField() {
        return booleanField;
    }

    public void setBooleanField(Boolean booleanField) {
        this.booleanField = booleanField;
    }

    public Integer getIntegerField() {
        return integerField;
    }

    public void setIntegerField(Integer integerField) {
        this.integerField = integerField;
    }

    public Double getDoubleField() {
        return doubleField;
    }

    public void setDoubleField(Double doubleField) {
        this.doubleField = doubleField;
    }

    public Float getFloatField() {
        return floatField;
    }

    public void setFloatField(Float floatField) {
        this.floatField = floatField;
    }

    public BigDecimal getBigDecimalField() {
        return bigDecimalField;
    }

    public void setBigDecimalField(BigDecimal bigDecimalField) {
        this.bigDecimalField = bigDecimalField;
    }

    public Character getCharacterField() {
        return characterField;
    }

    public void setCharacterField(Character characterField) {
        this.characterField = characterField;
    }

    public byte[] getByteArrayField() {
        return byteArrayField;
    }

    public void setByteArrayField(byte[] byteArrayField) {
        this.byteArrayField = byteArrayField;
    }
}
