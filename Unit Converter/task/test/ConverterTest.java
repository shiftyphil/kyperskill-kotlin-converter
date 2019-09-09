import converter.MainKt;
import org.hyperskill.hstest.v5.stage.BaseStageTest;


abstract public class ConverterTest<T> extends BaseStageTest<T> {
    public ConverterTest() throws Exception {
        super(MainKt.class);
    }
}
