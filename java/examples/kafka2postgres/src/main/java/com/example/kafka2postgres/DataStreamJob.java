package com.example.kafka2postgres;

import java.sql.Timestamp;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * The DataStreamJob ingests data from a Kafka topic. The lon and lat
 * coordinates are then filtered with a moving average, and the data
 * is enriched with it. Then the original datapoint is pushed into a
 * Postgres database.
 */
public class DataStreamJob {
	public static void main(String[] args) throws Exception {
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		String brokers = "data-toolbox-kafka-1:9092";

		KafkaSource<DataPoint> source = KafkaSource.<DataPoint>builder()
		.setBootstrapServers(brokers)
		.setTopics("test")
		.setGroupId("test")
		.setStartingOffsets(OffsetsInitializer.latest())
		.setValueOnlyDeserializer(new DataPointDeserializationSchema())
		.build();
	
		DataStream<DataPoint> dataStream = env.fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Source");
		DataStream<DataPoint> dataStreamFiltered = dataStream
			.keyBy(DataPoint::getId)
			.map(new MovingAverageFunction(5));
		
		dataStreamFiltered
			.addSink(
				JdbcSink.sink(
					"INSERT INTO iot (id, timestamp, lon, lat, filtered_lon, filtered_lat) VALUES (?, ?, ?, ?, ?, ?)",
				(statement, datapoint) -> {
                    statement.setString(1, datapoint.getId());
                    statement.setTimestamp(2, Timestamp.valueOf(datapoint.getTimestamp()));
                    statement.setDouble(3, datapoint.getLon());
                    statement.setDouble(4, datapoint.getLat());
                    statement.setDouble(5, datapoint.getFilteredLon());
                    statement.setDouble(6, datapoint.getFilteredLat());
				},
                JdbcExecutionOptions.builder()
                        .withBatchSize(1000)
                        .withBatchIntervalMs(200)
                        .withMaxRetries(5)
                        .build(),
                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                        .withUrl("jdbc:postgresql://data-toolbox-postgres-1:5432/postgres")
                        .withDriverName("org.postgresql.Driver")
                        .withUsername("postgres")
                        .withPassword("postgres")
                        .build()
				)
			);

		env.execute();
	}
}
