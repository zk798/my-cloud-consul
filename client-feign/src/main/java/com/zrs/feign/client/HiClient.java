package com.zrs.feign.client;

import feign.hystrix.FallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "api-a",
        /*以下两种断路方式配置其一即可*/
        fallback = HiClient.HiHystric.class
//        , fallbackFactory = HiClient.HiFallbackFactory.class
)
public interface HiClient {

    @RequestMapping(value = "/hi", method = RequestMethod.GET)
    String sayHiFromClientOne(@RequestParam(value = "name") String name);

    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    String sayHello();


    @Component
    class HiHystric implements HiClient {
        @Override
        public String sayHiFromClientOne(String name) {
            return "断路，sorry " + name;
        }

        @Override
        public String sayHello() {
            return "hello断路";
        }
    }

    @Component
    class HiFallbackFactory implements FallbackFactory<HiClient> {

        @Override
        public HiClient create(Throwable cause) {
            return new HiClient() {
                @Override
                public String sayHiFromClientOne(String name) {
                    return "fallback; reason was: " + cause.getMessage();
                }

                @Override
                public String sayHello() {
                    return "hello断路原因：" + cause.getMessage();
                }
            };
        }

    }


}

