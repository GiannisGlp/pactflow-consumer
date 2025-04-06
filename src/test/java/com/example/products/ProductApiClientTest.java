package com.example.products;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.ContainsPattern;
import com.maciejwalkowiak.wiremock.spring.InjectWireMock;

@SpringBootTest
@AutoConfigureMockMvc
class ProductApiClientTest extends WireMockPactBaseTest {
	@InjectWireMock("wiremock-service-name")
	private WireMockServer wiremock;

	@Autowired
	private ProductClient productClient;

	// Happy Path: Get a product by ID
	@Test
	void getProduct() throws IOException {
		// Arrange
		this.wiremock.stubFor(WireMock.get(WireMock.urlEqualTo("/product/10"))
				.willReturn(aResponse().withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
						.withBody("{ \"id\": \"10\", \"name\": \"pizza\", \"type\": \"food\", \"price\": 100.0 }")));

		// Act
		final Product product = this.productClient.getProduct("10");

		// Assert
		assertThat(product.getId(), is("10"));
		assertThat(product.getName(), is("pizza"));
		assertThat(product.getType(), is("food"));
		assertThat(product.getPrice(), is(100.0));
	}

	// Happy Path: Create a product
	@Test
	void createProduct() throws IOException {
		// Arrange
		final String productJson = "{ \"id\": \"27\", \"name\": \"pizza\", \"type\": \"food\", \"price\": 27.0 }";
		this.wiremock.stubFor(
				WireMock.post(WireMock.urlEqualTo("/products")).withRequestBody(equalToJson(productJson, true, true))
						.withHeader("Content-Type", new ContainsPattern("application/json"))
						.willReturn(aResponse().withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
								.withBody(productJson)));

		// Act
		final Product product = this.productClient.createProduct(new Product("27", "pizza", "food", 27.0));

		// Assert
		assertThat(product.getId(), is("27"));
		assertThat(product.getName(), is("pizza"));
		assertThat(product.getType(), is("food"));
		assertThat(product.getPrice(), is(27.0));
	}

	// Happy Path: Get all products
	@Test
	void getProducts() throws IOException {
		// Arrange
		this.wiremock.stubFor(WireMock.get(WireMock.urlEqualTo("/products"))
				.willReturn(aResponse().withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
						.withBody("[{ \"id\": \"10\", \"name\": \"pizza\", \"type\": \"food\", \"price\": 100.0 }]")));

		// Act
		final List<Product> products = this.productClient.getProducts();

		// Assert
		assertThat(products.size(), is(1));
		assertThat(products.get(0).getId(), is("10"));
		assertThat(products.get(0).getName(), is("pizza"));
		assertThat(products.get(0).getType(), is("food"));
		assertThat(products.get(0).getPrice(), is(100.0));
	}

	// Unhappy Path: Product not found (404)
	@Test
	void getProductNotFound() throws IOException {
		// Arrange
		this.wiremock.stubFor(WireMock.get(WireMock.urlEqualTo("/product/999"))
				.willReturn(aResponse().withStatus(404).withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)));

		// Act & Assert
		Exception exception = assertThrows(ProductNotFoundException.class, () -> {
			this.productClient.getProduct("999");
		});

		assertThat(exception.getMessage(), is("Product not found"));
	}

	// Unhappy Path: Invalid product ID (400)
	@Test
	void getProductInvalidId() throws IOException {
		// Arrange
		this.wiremock.stubFor(WireMock.get(WireMock.urlEqualTo("/product/invalid-id"))
				.willReturn(aResponse().withStatus(400).withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)));

		// Act & Assert
		Exception exception = assertThrows(InvalidProductException.class, () -> {
			this.productClient.getProduct("invalid-id");
		});

		assertThat(exception.getMessage(), is("Invalid product data"));
	}

	// Unhappy Path: Invalid product data during creation (400)
	@Test
	void createProductInvalidData() throws IOException {
		// Arrange
		final String invalidProductJson = "{ \"id\": \"\", \"name\": \"\", \"type\": \"\", \"price\": 0.0 }";
		this.wiremock.stubFor(
				WireMock.post(WireMock.urlEqualTo("/products"))
						.withRequestBody(equalToJson(invalidProductJson, true, true))
						.willReturn(aResponse().withStatus(400).withHeader("Content-Type",
								MediaType.APPLICATION_JSON_VALUE)));

		// Act & Assert
		Exception exception = assertThrows(InvalidProductException.class, () -> {
			this.productClient.createProduct(new Product("", "", "", 0.0));
		});

		assertThat(exception.getMessage(), is("Invalid product data"));
	}
}