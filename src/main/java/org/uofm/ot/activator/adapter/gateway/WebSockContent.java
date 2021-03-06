package org.uofm.ot.activator.adapter.gateway;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for mapping web socket message content section Created by grosscol on 2017-06-20.
 */
@JsonIgnoreProperties({"payload", "transient"})
public class WebSockContent {

  @JsonProperty
  public String code;
  @JsonProperty
  public boolean silent;
  @JsonProperty
  public boolean store_history;
  @JsonProperty("user_expressions")
  public Map<String, Object> userExpressions;
  @JsonProperty("allow_stdin")
  public boolean allowStdin;
  @JsonProperty
  public Map<String, Object> metadata;
  @JsonProperty
  public List<String> buffers;
  @JsonProperty
  public String text;
  @JsonProperty("execution_count")
  public Integer executionCount;
  @JsonProperty("execution_state")
  public String executionState;
  @JsonProperty
  public String status;
  @JsonProperty
  public String name;
  @JsonProperty
  public boolean restart;
  @JsonProperty
  public Map<String, Object> data;

  // Error message fields
  @JsonProperty
  public List<String> traceback;
  @JsonProperty
  public String evalue;
  @JsonProperty
  public String ename;
  @JsonProperty("engine_info")
  public Map<String, Object> engineInfo;


  public WebSockContent() {
    code = "";
    silent = false;
    store_history = false;
    userExpressions = new HashMap<>();
    allowStdin = false;

    metadata = new HashMap<>();
    buffers = new ArrayList<>();
  }

}
