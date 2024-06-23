package com.example.kafka2postgres;

import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedList;

/**
 * The MovingAverageFunction calculates moving average for
 * lon and lat coordinates.
 */
public class MovingAverageFunction extends RichMapFunction<DataPoint, DataPoint> {
    private transient ListState<Double> lonValuesState;
    private transient ListState<Double> latValuesState;
    private final int windowSize;

    public MovingAverageFunction(int windowSize) {
        this.windowSize = windowSize;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        ListStateDescriptor<Double> lonDescriptor = new ListStateDescriptor<>("lonValues", Types.DOUBLE);
        lonValuesState = getRuntimeContext().getListState(lonDescriptor);

        ListStateDescriptor<Double> latDescriptor = new ListStateDescriptor<>("latValues", Types.DOUBLE);
        latValuesState = getRuntimeContext().getListState(latDescriptor);
    }

    @Override
    public DataPoint map(DataPoint value) throws Exception {
        LinkedList<Double> lonValues = new LinkedList<>();
        for (Double lon : lonValuesState.get()) {
            lonValues.add(lon);
        }
        lonValues.add(value.getLon());
        if (lonValues.size() > windowSize) {
            lonValues.removeFirst();
        }
        lonValuesState.update(lonValues);

        LinkedList<Double> latValues = new LinkedList<>();
        for (Double lat : latValuesState.get()) {
            latValues.add(lat);
        }
        latValues.add(value.getLat());
        if (latValues.size() > windowSize) {
            latValues.removeFirst();
        }
        latValuesState.update(latValues);

        double averageLon = lonValues.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double averageLat = latValues.stream().mapToDouble(Double::doubleValue).average().orElse(0);

        value.setFilteredLon(averageLon);
        value.setFilteredLat(averageLat);

        return value;
    }
}
