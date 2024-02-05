/*
 * Copyright 2021-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dk.cloudcreate.essentials.components.foundation.postgresql;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.*;
import org.slf4j.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.*;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import reactor.core.Disposable;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class ListenNotifyIT {
    private static final Logger log        = LoggerFactory.getLogger(ListenNotifyIT.class);
    private static       String TABLE_NAME = "test_table";

    @Container
    private final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("listen-notify-db")
            .withUsername("test-user")
            .withPassword("secret-password");

    protected Jdbi         jdbi;
    private   Disposable   subscription;
    private   ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        jdbi = Jdbi.create(getPostgreSQLContainer().getJdbcUrl(), getPostgreSQLContainer().getUsername(), getPostgreSQLContainer().getPassword());

        jdbi.useTransaction(handle -> handle.execute("CREATE TABLE " + TABLE_NAME + " (id bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, column1 TEXT, column2 TEXT)"));
    }

    protected PostgreSQLContainer<?> getPostgreSQLContainer() {
        return postgreSQLContainer;
    }

    @AfterEach
    void cleanup() {
        if (subscription != null) {
            subscription.dispose();
        }
    }

    @Test
    void single_insert_listen_notify_test() throws InterruptedException, JsonProcessingException {
        jdbi.useTransaction(handle -> {
            ListenNotify.addChangeNotificationTriggerToTable(handle, TABLE_NAME, List.of(ListenNotify.SqlOperation.INSERT), "id", "column1", "column2");
        });

        jdbi.useTransaction(handle -> {
            var functions = handle.createQuery("SELECT routine_name\n" +
                                                       "FROM information_schema.routines\n" +
                                                       "WHERE routine_type = 'FUNCTION' AND routine_name LIKE 'notify_%'")
                                  .mapTo(String.class)
                                  .list();
            log.info("Functions: {}", functions);

        });


        jdbi.useTransaction(handle -> {
            var functions = handle.createQuery("SELECT\n" +
                                                       "    trigger_schema,\n" +
                                                       "    trigger_name,\n" +
                                                       "    event_manipulation,\n" +
                                                       "    action_statement\n" +
                                                       "FROM information_schema.triggers\n" +
                                                       "WHERE event_object_table = '" + TABLE_NAME + "'")
                                  .mapToMap()
                                  .list();

            log.info("Triggers: {}", functions);

        });

        var receivedNotification = new AtomicReference<String>();
        subscription = ListenNotify.listen(jdbi,
                                           TABLE_NAME,
                                           Duration.ofMillis(200))
                                   .subscribe(notification -> {
                                       receivedNotification.set(notification);
                                   });

        Thread.sleep(200);

        jdbi.useTransaction(handle -> {
            log.info("INSERTING one row");
            handle.execute("INSERT INTO " + TABLE_NAME + " (column1, column2) VALUES ('Column1Value', 'Column2Value')");
        });
        log.info("AFTER inserting one row");

        var content = jdbi.withHandle(handle -> handle.createQuery("SELECT * from " + TABLE_NAME).mapToMap().list());
        log.info("Content in table: {}", content);

        Awaitility.waitAtMost(Duration.ofMillis(2000))
                  .untilAsserted(() -> assertThat(receivedNotification.get()).isNotNull());

        System.out.println(receivedNotification.get());
        var notification = objectMapper.readValue(receivedNotification.get(), TestTableNotification.class);
        assertThat(notification.getTableName()).isEqualTo(TABLE_NAME);
        assertThat(notification.getOperation()).isEqualTo(ListenNotify.SqlOperation.INSERT);
        assertThat(notification.id).isEqualTo(1);
        assertThat(notification.column1).isEqualTo("Column1Value");
        assertThat(notification.column2).isEqualTo("Column2Value");
    }

    @Test
    void for_multi_inserts_notifications_are_received_in_order() throws InterruptedException {
        jdbi.useTransaction(handle -> {
            ListenNotify.addChangeNotificationTriggerToTable(handle, TABLE_NAME, List.of(ListenNotify.SqlOperation.INSERT), "id");
        });


        var receivedNotifications = new CopyOnWriteArrayList<TestTableNotification>();
        subscription = ListenNotify.listen(jdbi,
                                           TABLE_NAME,
                                           Duration.ofMillis(200))
                                   .subscribe(notification -> {
                                       try {
                                           receivedNotifications.add(objectMapper.readValue(notification, TestTableNotification.class));
                                       } catch (JsonProcessingException e) {
                                           throw new RuntimeException(e);
                                       }
                                   });

        Thread.sleep(200);

        var numberOfInserts = 100;
        for (var insertIndex = 0; insertIndex < numberOfInserts; insertIndex++) {
            jdbi.useTransaction(handle -> {
                log.info("Inserting");
                handle.execute("INSERT INTO " + TABLE_NAME + " (column1, column2) VALUES ('Column1Value', 'Column2Value')");
            });
        }

        Awaitility.waitAtMost(Duration.ofMillis(2000))
                  .untilAsserted(() -> assertThat(receivedNotifications.size()).isEqualTo(numberOfInserts));

        log.info("Received ids: {}", receivedNotifications.stream().map(testTableNotification -> testTableNotification.id).collect(Collectors.toList()));

        for (var insertIndex = 0; insertIndex < numberOfInserts; insertIndex++) {
            var notification = receivedNotifications.get(insertIndex);
            assertThat(notification.getTableName())
                    .describedAs("InsertIndex " + insertIndex)
                    .isEqualTo(TABLE_NAME);
            assertThat(notification.getOperation())
                    .describedAs("InsertIndex " + insertIndex)
                    .isEqualTo(ListenNotify.SqlOperation.INSERT);
            assertThat(notification.id)
                    .describedAs("InsertIndex " + insertIndex)
                    .isEqualTo(insertIndex + 1);
        }
    }

    @Test
    void verify_remove_notifications() throws InterruptedException {
        jdbi.useTransaction(handle -> {
            ListenNotify.addChangeNotificationTriggerToTable(handle, TABLE_NAME, List.of(ListenNotify.SqlOperation.INSERT), "id");
        });

        var receivedNotifications = new CopyOnWriteArrayList<TestTableNotification>();
        subscription = ListenNotify.listen(jdbi,
                                           TABLE_NAME,
                                           Duration.ofMillis(200))
                                   .subscribe(notification -> {
                                       try {
                                           receivedNotifications.add(objectMapper.readValue(notification, TestTableNotification.class));
                                       } catch (JsonProcessingException e) {
                                           throw new RuntimeException(e);
                                       }
                                   });

        Thread.sleep(200);

        var numberOfInserts = 10;
        for (var insertIndex = 0; insertIndex < numberOfInserts; insertIndex++) {
            jdbi.useTransaction(handle -> {
                log.info("Inserting");
                handle.execute("INSERT INTO " + TABLE_NAME + " (column1, column2) VALUES ('Column1Value', 'Column2Value')");
            });
        }

        Awaitility.waitAtMost(Duration.ofMillis(2000))
                  .untilAsserted(() -> assertThat(receivedNotifications.size()).isEqualTo(numberOfInserts));

        // Remove notification function/trigger
        receivedNotifications.clear();
        jdbi.useTransaction(handle -> {
            ListenNotify.removeChangeNotificationTriggerFromTable(handle, TABLE_NAME);
        });

        jdbi.useTransaction(handle -> {
            log.info("Inserting");
            handle.execute("INSERT INTO " + TABLE_NAME + " (column1, column2) VALUES ('Column1Value1', 'Column2Value1')");
        });

        Awaitility.await()
                  .during(Duration.ofSeconds(1))
                  .atMost(Duration.ofSeconds(2))
                  .until(receivedNotifications::isEmpty);
    }


    @Test
    void for_multithreaded_inserts_all_notifications_are_received() throws InterruptedException {
        jdbi.useTransaction(handle -> {
            ListenNotify.addChangeNotificationTriggerToTable(handle, TABLE_NAME, List.of(ListenNotify.SqlOperation.INSERT), "id");
        });


        var receivedNotifications = new CopyOnWriteArrayList<TestTableNotification>();
        subscription = ListenNotify.listen(jdbi,
                                           TABLE_NAME,
                                           Duration.ofMillis(200))
                                   .subscribe(notification -> {
                                       try {
                                           receivedNotifications.add(objectMapper.readValue(notification, TestTableNotification.class));
                                       } catch (JsonProcessingException e) {
                                           throw new RuntimeException(e);
                                       }
                                   });

        Thread.sleep(200);

        var executor        = Executors.newFixedThreadPool(10);
        var numberOfInserts = 100;
        for (var insertIndex = 0; insertIndex < numberOfInserts; insertIndex++) {
            executor.execute(() -> jdbi.useTransaction(handle -> {
                log.info("Inserting");
                handle.execute("INSERT INTO " + TABLE_NAME + " (column1, column2) VALUES ('Column1Value', 'Column2Value')");
            }));

        }

        Awaitility.waitAtMost(Duration.ofMillis(4000))
                  .untilAsserted(() -> assertThat(receivedNotifications.size()).isEqualTo(numberOfInserts));

        var sortedNotificationsReceived = receivedNotifications.stream().map(testTableNotification -> testTableNotification.id).sorted().collect(Collectors.toList());
        log.info("Received ids: {}", sortedNotificationsReceived);

        for (var insertIndex = 0; insertIndex < numberOfInserts; insertIndex++) {
            var notification = sortedNotificationsReceived.get(insertIndex);
            assertThat(notification)
                    .describedAs("InsertIndex " + insertIndex)
                    .isEqualTo(insertIndex + 1);
        }
    }

    private static class TestTableNotification extends TableChangeNotification {
        @JsonProperty("id")
        private long   id;
        @JsonProperty("column1")
        private String column1;
        @JsonProperty("column2")
        private String column2;
    }

}