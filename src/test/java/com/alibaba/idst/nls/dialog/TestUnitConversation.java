package com.alibaba.idst.nls.dialog;

import com.alibaba.idst.nls.uds.DialogEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@PropertySources({@PropertySource("config.properties")})
public class TestUnitConversation implements CommandLineRunner {
    @Autowired
    private DialogEngine engine;

    @Value("${unitconversation.appkey}")
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

    public void testUnitConversation(){
        if(!init()) {
            System.exit(0);
        }

        String result = engine.ask("1亩等于多少平方米");
        System.out.println(result);
    }

    @Override
    public void run(String... args) throws Exception {
        testUnitConversation();
    }

    public static void main(String[] args) {
        SpringApplication.run(TestUnitConversation.class, args);
    }
}
