package com.thehecklers.externalservice;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.security.KeyStore;
import java.util.Objects;

//@EnableDiscoveryClient
@SpringBootApplication
public class ExternalServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExternalServiceApplication.class, args);
    }

    //    @Bean
//    RestOperations restOperations() {
//        return new RestTemplate();
//    }
    @Bean
    public RestTemplate restTemplate() throws Exception {
        KeyStore ks = KeyStore.getInstance("AzureKeyVault");
        SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial(ks, new TrustSelfSignedStrategy())
                .build();

        HostnameVerifier allowAll = (String hostName, SSLSession session) -> true;
        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, allowAll);

        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(csf)
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory
                = new HttpComponentsClientHttpRequestFactory();

        requestFactory.setHttpClient(httpClient);
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        return restTemplate;
    }
}

@RestController
@RequestMapping("/")
class ExternalServiceController {
    private final RestOperations ops;

    ExternalServiceController(RestOperations ops) {
        this.ops = ops;
    }

    @GetMapping
    public String status() {
        return "You've reached the external service!";
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello there from the external service!";
    }

    @GetMapping("/internalstatus")
    public String getInternalStatus() {
        return ops.getForObject(URI.create("https://internal-service"), String.class);
    }

    @GetMapping("/internalgreeting")
    public String getInternalGreeting() {
        return ops.getForObject(URI.create("https://internal-service/greeting"), String.class);
    }

    @GetMapping("/internalpeople")
    public Iterable getPeopleAsString() {
        return ops.getForObject(URI.create("https://internal-service/people"), Iterable.class);
    }

    @GetMapping("/internalperson")
    public Person getFirstAsString() {
        return ops.getForObject(URI.create("https://internal-service/person"), Person.class);
    }

    @GetMapping("/sslcheck")
    public String getTlsSslStatus(HttpServletRequest request) {
        return ops.getForObject(URI.create("https://internal-service/sslcheck"), String.class);
    }
}

class Person {
    private String id;
    private String firstName;
    private String lastName;

    public Person() {
    }

    public Person(String id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return Objects.equals(id, person.id) && Objects.equals(firstName, person.firstName) && Objects.equals(lastName, person.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName);
    }

    @Override
    public String toString() {
        return "Person{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}