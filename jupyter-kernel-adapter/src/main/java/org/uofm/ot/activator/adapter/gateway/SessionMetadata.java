package org.uofm.ot.activator.adapter.gateway;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Class for mapping the components of the /api/sessions REST responses from a Jupyter Kernel
 * Gateway Created by grosscol on 2017-08-01.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SessionMetadata {

  @JsonProperty
  private
  String id;
  @JsonProperty
  private
  String path;
  @JsonProperty()
  private
  String type;
  @JsonProperty()
  private
  KernelMetadata kernel;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public KernelMetadata getKernel() {
    return kernel;
  }

  public void setKernel(KernelMetadata kernel) {
    this.kernel = kernel;
  }
}
