package hu.motorworkshop.app.workorder;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class WorkOrderNumberService {

    private static final ZoneId BUDAPEST =
            ZoneId.of("Europe/Budapest");


    private final JdbcTemplate jdbcTemplate;


    public WorkOrderNumberService(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public String nextNumber() {

        int year =
                ZonedDateTime
                        .now(BUDAPEST)
                        .getYear();


        Long number =
                jdbcTemplate.queryForObject(
                        """
                        INSERT INTO work_order_number_counters
                            (counter_year, last_number)
                        VALUES
                            (?, 1)

                        ON CONFLICT (counter_year)

                        DO UPDATE SET
                            last_number =
                                work_order_number_counters.last_number + 1

                        RETURNING last_number
                        """,
                        Long.class,
                        year
                );


        if (number == null) {

            throw new IllegalStateException(
                    "Nem sikerült munkalapszámot generálni."
            );
        }


        return "ML-%d-%06d".formatted(
                year,
                number
        );
    }
}