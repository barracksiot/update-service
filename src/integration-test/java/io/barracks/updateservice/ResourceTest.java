/*
 * MIT License
 *
 * Copyright (c) 2017 Barracks Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.barracks.updateservice;

import io.barracks.commons.test.WebApplicationTest;
import io.barracks.updateservice.config.ExceptionConfig;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration({ResourceTest.TestApplication.class, EmbeddedMongoAutoConfiguration.class, ExceptionConfig.class})
@WebIntegrationTest(randomPort = true)
public abstract class ResourceTest extends WebApplicationTest {

    @Value("${local.server.port}")
    private int port;


    protected String buildRequestUrl(String endpoint, List<String> uriParams) {
        final String uriParamsStr = uriParams.stream()
                .map(entry -> entry + "={" + entry + "}")
                .reduce("", (a, b) -> a + "&" + b);
        return "http://localhost:" + port + endpoint + "?" + uriParamsStr;
    }

    protected String buildRequestUrl(String endpoint) {
        return buildRequestUrl(endpoint, Collections.emptyList());
    }

    @SpringBootApplication
    static class TestApplication {

    }
}
