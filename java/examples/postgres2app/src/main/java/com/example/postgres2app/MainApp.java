package com.example.postgres2app;

import org.apache.camel.main.Main;
import org.apache.commons.dbcp2.BasicDataSource;

/**
 * Main function for the postgres2app Camel application.
 */
public class MainApp {
    /**
     * Initialize the datasource, processors and route, and
     * then bind them to the main function.
     */
    public static void main(String... args) throws Exception {
        Main main = new Main();
        BasicDataSource dataSource = new BasicDataSource();

        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://data-toolbox-postgres-1:5432/postgres");
        dataSource.setUsername("postgres");
        dataSource.setPassword("postgres");

        main.bind("dataSource", dataSource);

        NotFoundExceptionProcessor notFoundExceptionProcessor = new NotFoundExceptionProcessor();
        InternalServerErrorProcessor internalServerErrorProcessor = new InternalServerErrorProcessor();

        main.bind("notFoundExceptionProcessor", new NotFoundExceptionProcessor());
        main.bind("internalServerErrorProcessor", new InternalServerErrorProcessor());

        main.configure()
            .addRoutesBuilder(new PostgresToAppRoute(
                notFoundExceptionProcessor,
                internalServerErrorProcessor
            )
        );

        main.run(args);
    }
}

