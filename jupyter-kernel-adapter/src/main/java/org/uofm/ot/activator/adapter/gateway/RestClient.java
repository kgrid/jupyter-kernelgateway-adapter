package org.uofm.ot.activator.adapter.gateway;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * Created by grosscol on 2017-06-13.
 */
public class RestClient {
  private URI restURI;
  public RestTemplate restTemplate;

  public RestClient() {
    this(URI.create("http://localhost:8888"));
  }

  public RestClient(final URI restURI) {
    this.restTemplate = new RestTemplate();
    this.restURI = restURI;
  }

  public String gatewayVersion() {
    HashMap<String, Object> jb = new HashMap<>();
    URI targetURI = restURI.resolve("/api");
    try {
      jb = restTemplate.getForObject(targetURI, HashMap.class);
    } catch (ResourceAccessException e){
      System.out.println(e.getMessage());
    }
    return (String) jb.get("version");
  }

  public List<KernelMetadata> getKernels() {
    List<KernelMetadata> kernels = new ArrayList<>();
    try {
      URI targetUri = restURI.resolve("/api/kernels");
      ResponseEntity<List<KernelMetadata>> kernelsResponse =
          restTemplate.exchange(targetUri,
              HttpMethod.GET, null, new ParameterizedTypeReference<List<KernelMetadata>>() {
              });
      kernels = kernelsResponse.getBody();
    } catch (HttpClientErrorException | ResourceAccessException e) {
      System.out.println(e.getMessage());
    }

    return kernels;
  }

  public KernelMetadata startKernel() {
    KernelMetadata resp = null;
    URI targetUri = restURI.resolve("/api/kernels");
    try {
      String request = "{\"name\": \"python\"}";
      resp = restTemplate.postForObject(targetUri, request, KernelMetadata.class);
    } catch (HttpClientErrorException | ResourceAccessException e) {
      System.out.println(e.getMessage());
    }
    return resp;
  }

  public SessionMetadata startSession(KernelMetadata kernelMd) {
    SessionMetadata resp = null;
    URI targetUri = restURI.resolve("/api/sessions");
    try {
      Map<String, Object> body = new LinkedHashMap<>();
      body.put("path", UUID.randomUUID().toString());
      body.put("type", "kernelgateway");
      body.put("kernel", kernelMd);
      resp = restTemplate.postForObject(targetUri, body, SessionMetadata.class);
    } catch (HttpClientErrorException | ResourceAccessException e) {
      System.out.println(e.getMessage());
    }
    return resp;
  }

  public void deleteSession(SessionMetadata sessMd){
    URI targetUri = restURI.resolve("/api/sessions/"+sessMd.getId());
    try {
      restTemplate.delete(targetUri);
    } catch (HttpClientErrorException | ResourceAccessException e) {
      System.out.println(e.getMessage());
    }
  }
}
