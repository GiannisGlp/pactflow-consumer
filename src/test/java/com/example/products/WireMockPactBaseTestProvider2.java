package com.example.products;

import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.test.context.TestPropertySource;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.maciejwalkowiak.wiremock.spring.ConfigureWireMock;
import com.maciejwalkowiak.wiremock.spring.EnableWireMock;
import com.maciejwalkowiak.wiremock.spring.WireMockConfigurationCustomizer;

import se.bjurr.wiremockpact.wiremockpactextensionjunit5.WireMockPactExtension;
import se.bjurr.wiremockpact.wiremockpactlib.api.WireMockPactConfig;

@EnableWireMock({
        @ConfigureWireMock(name = "wiremock-service-name-provider2", property = "wiremock.server.url", stubLocation = "wiremock", configurationCustomizers = {
                WireMockPactBaseTestProvider2.class })
})
@TestPropertySource(locations = "classpath:autotest.properties")
public class WireMockPactBaseTestProvider2 implements WireMockConfigurationCustomizer {
    @RegisterExtension
    static WireMockPactExtension WIREMOCK_PACT_EXTENSION = new WireMockPactExtension(
            WireMockPactConfig.builder() //
                    .setConsumerDefaultValue("pactflow-consumer") //
                    .setProviderDefaultValue(System.getenv().getOrDefault("PACT_PROVIDER", "pactflow-provider2")) //
                    .setPactJsonFolder("build/pacts"));

    @Override
    public void customize(
            final WireMockConfiguration configuration, final ConfigureWireMock options) {
        configuration.extensions(WIREMOCK_PACT_EXTENSION);
    }
}