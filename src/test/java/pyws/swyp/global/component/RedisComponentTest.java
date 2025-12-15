package pyws.swyp.global.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import pyws.swyp.config.TestRedisConfig;

@SpringBootTest
@Import(TestRedisConfig.class)
class RedisComponentTest {

    @Autowired
    RedisComponent redisComponent;

    @Test
    @DisplayName("String 저장/조회에 성공한다")
    void setGetString_success() {
        // given
        String key = "test:string:" + UUID.randomUUID();
        String value = "foo";

        // when
        redisComponent.setString(key, value, 10_000);

        // then
        String loaded = redisComponent.getString(key);
        assertEquals(loaded, value);
    }

    @Test
    @DisplayName("delete 호출 시 key가 삭제된다")
    void delete_success() {
        // given
        String key = "test:del:" + UUID.randomUUID();
        redisComponent.setString(key, "bar", 10_000);

        // when
        redisComponent.delete(key);

        // then
        assertNull(redisComponent.getString(key));
    }

    @Test
    @DisplayName("JSON 저장/조회에 성공한다")
    void setGetJson_success() {
        // given
        String key = "test:json:" + UUID.randomUUID();
        TestDto dto = new TestDto(1L, "baz");

        // when
        redisComponent.setJson(key, dto, 10_000);

        // then
        TestDto loaded = redisComponent.getJson(key, TestDto.class);
        assertEquals(loaded, dto);
    }

    @Test
    @DisplayName("존재하지 않는 key의 getJson은 null을 반환한다")
    void getJson_nullWhenNotExists() {
        // given
        String key = "test:json:missing:" + UUID.randomUUID();

        // when
        TestDto loaded = redisComponent.getJson(key, TestDto.class);

        // then
        assertNull(loaded);
    }

    @Test
    @DisplayName("TTL이 지나면 String 값이 만료된다")
    void ttl_expire_string() throws InterruptedException {
        // given
        String key = "test:ttl:string:" + UUID.randomUUID();

        // when
        redisComponent.setString(key, "qux", 200);
        Thread.sleep(350);

        // then
        assertNull(redisComponent.getString(key));
    }

    @Test
    @DisplayName("TTL이 지나면 JSON 값이 만료된다")
    void ttl_expire_json() throws InterruptedException {
        // given
        String key = "test:ttl:json:" + UUID.randomUUID();
        TestDto dto = new TestDto(2L, "quux");

        // when
        redisComponent.setJson(key, dto, 200);
        Thread.sleep(350);

        // then
        assertNull(redisComponent.getString(key));
    }

    private record TestDto(Long id, String name) {}
}