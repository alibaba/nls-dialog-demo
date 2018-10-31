package com.alibaba.idst.nls.dialog;

import com.alibaba.idst.nls.uds.DialogEngine;
import lombok.Data;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Log
@Data
@PropertySources({@PropertySource("config.properties")})
@SpringBootApplication(scanBasePackages = {"com.alibaba.idst.nls"})
public class TestStock implements CommandLineRunner {
    @Autowired
    private DialogEngine engine;

    @Value("${stock.appkey}")
    private String appkey;

    @Value("${package}")
    private String packageAddr;

    @Value("${accessKeyId}")
    private String akId;

    @Value("${accessKeySecret}")
    private String akKey;

    public boolean init() {
        String sessionId = UUID.randomUUID().toString();
        Map<String, String> params = new HashMap<>();
        params.put("appKey", appkey);
        params.put("package", packageAddr);
        params.put("sessionId", sessionId);
        params.put("accessKeyId", akId);
        params.put("accessKeySecret", akKey);
        return engine.init(params);
    }

    public void testStock(){
        if(!init()) {
            System.exit(0);
        }

        String result = engine.ask("阿里的股价多少");
        System.out.println(result);

        result = engine.ask("耐克的呢");
        System.out.println(result);
    }

    @Override
    public void run(String... args) throws Exception {
        testStock();
    }

    public static void main(String[] args) {
        SpringApplication.run(TestStock.class, args);
    }
}
