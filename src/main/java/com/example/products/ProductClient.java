package com.example.products;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ProductClient {
  private final String url;

  public ProductClient(@Value("${basepath}") final String url) {
    this.url = url;
  }

  public Product createProduct(final Product p) throws IOException {
    return Request.Post(this.url + "/products")
        .addHeader("Accept", "application/json")
        .bodyString(this.toString(p), ContentType.APPLICATION_JSON)
        .execute().handleResponse(httpResponse -> handleResponse(httpResponse, Product.class));
  }

  public Product getProduct(final String id) throws IOException {
    return Request.Get(this.url + "/product/" + id)
        .addHeader("Accept", "application/json")
        .execute().handleResponse(httpResponse -> handleResponse(httpResponse, Product.class));
  }

  public List<Product> getProducts() throws IOException {
    return Request.Get(this.url + "/products")
        .addHeader("Accept", "application/json")
        .execute().handleResponse(httpResponse -> handleResponse(httpResponse, new TypeReference<List<Product>>() {
        }));
  }

  private <T> T handleResponse(HttpResponse httpResponse, Class<T> responseType) throws IOException {
    int statusCode = httpResponse.getStatusLine().getStatusCode();
    if (statusCode == 404) {
      throw new ProductNotFoundException("Product not found");
    } else if (statusCode == 400) {
      throw new InvalidProductException("Invalid product data");
    } else if (statusCode >= 200 && statusCode < 300) {
      final ObjectMapper mapper = new ObjectMapper();
      return mapper.readValue(httpResponse.getEntity().getContent(), responseType);
    } else {
      throw new IOException("Unexpected response status: " + statusCode);
    }
  }

  private <T> T handleResponse(HttpResponse httpResponse, TypeReference<T> responseType) throws IOException {
    int statusCode = httpResponse.getStatusLine().getStatusCode();
    if (statusCode == 404) {
      throw new ProductNotFoundException("Product not found");
    } else if (statusCode == 400) {
      throw new InvalidProductException("Invalid product data");
    } else if (statusCode >= 200 && statusCode < 300) {
      final ObjectMapper mapper = new ObjectMapper();
      return mapper.readValue(httpResponse.getEntity().getContent(), responseType);
    } else {
      throw new IOException("Unexpected response status: " + statusCode);
    }
  }

  private String toString(final Product p) throws IOException {
    try {
      final ObjectMapper mapper = new ObjectMapper();
      return mapper.writeValueAsString(p);
    } catch (final JsonMappingException e) {
      throw new IOException(e);
    }
  }
}