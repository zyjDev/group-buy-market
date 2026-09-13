package com.groupbuy.market.test.infrastructure.gateway;

import com.groupbuy.market.infrastructure.gateway.GroupBuyNotifyService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GroupBuyNotifyServiceTest {

    @LocalServerPort
    private int port;

    @Resource
    private GroupBuyNotifyService groupBuyNotifyService;

    @Test
    public void test_notify_api() throws Exception {

        String notifyRequestDTOJSON = "{\"teamId\":\"57199993\",\"outTradeNoList\":[\"038426231487\",\"652896391719\",\"619401409195\"]}";

        String response = groupBuyNotifyService.groupBuyNotify("http://127.0.0.1:" + port + "/api/v1/test/group_buy_notify", notifyRequestDTOJSON);

        Assert.assertEquals("success", response);
        log.info("测试结果:{}", response);
    }

    @Test
    public void test() throws IOException {
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"teamId\":\"57199993\",\"outTradeNoList\":[\"038426231487\",\"652896391719\",\"619401409195\"]}");
        Request request = new Request.Builder()
                .url("http://127.0.0.1:" + port + "/api/v1/test/group_buy_notify")
                .post(body)
                .addHeader("content-type", "application/json")
                .build();

        Response response = client.newCall(request).execute();
        Assert.assertTrue(response.isSuccessful());
        Assert.assertEquals("success", response.body().string());
        log.info("测试结果:{}", response);
        response.close();
    }


}
