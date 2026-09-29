package com.example.vehicleapi;

import com.example.vehicleapi.core.config.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 应用入口。
 *
 * <p>启动方式：
 *
 * <ul>
 *   <li>{@code mvn spring-boot:run}
 *   <li>或 {@code java -jar target/vehicle-api-1.0.0.jar}
 * </ul>
 */
@SpringBootApplication
public class VehicleApiApplication {

    private static final Logger log = LoggerFactory.getLogger("vehicle_api");

    public static void main(String[] args) {
        ConfigurableApplicationContext context =
                SpringApplication.run(VehicleApiApplication.class, args);

        Settings settings = context.getBean(Settings.class);
        log.info("vehicle-api 启动完成：内存存储已初始化（服务重启后数据会丢失）");
        log.info(
                "监听地址 http://{}:{}，接口前缀 {}",
                settings.getHost(),
                settings.getPort(),
                settings.getApiPrefix());
    }
}
