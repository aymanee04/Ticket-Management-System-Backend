package ma.bank.ticketmanagementsystembackend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

//@SpringBootTest(properties = {
//        "spring.profiles.active=test",
//        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false",
//        "spring.datasource.driver-class-name=org.h2.Driver",
//        "spring.datasource.username=sa",
//        "spring.datasource.password=",
//        "spring.jpa.hibernate.ddl-auto=create-drop"
//})
class TicketManagementSystemBackendApplicationTests {

//    @MockitoBean
//    private RedisTemplate<String, String> redisTemplate;

    @Test
    void contextLoads() {
    }

}
