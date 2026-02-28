package app;

import javafx.scene.chart.ValueAxis;

import java.util.List;

public class LogNumberAxis extends ValueAxis<Number> {
    @Override
    protected List calculateMinorTickMarks() {
        return List.of();
    }

    @Override
    protected void setRange(Object o, boolean b) {

    }

    @Override
    protected Object getRange() {
        return null;
    }

    @Override
    protected List<Number> calculateTickValues(double v, Object o) {
        return List.of();
    }

    @Override
    protected String getTickMarkLabel(Number number) {
        return "";
    }


}
