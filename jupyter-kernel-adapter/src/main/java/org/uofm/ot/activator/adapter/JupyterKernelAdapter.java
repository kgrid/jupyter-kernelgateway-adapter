package org.uofm.ot.activator.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.uofm.ot.activator.adapter.gateway.KernelMetadata;
import org.uofm.ot.activator.adapter.gateway.RestClient;
import org.uofm.ot.activator.adapter.gateway.SessionMetadata;
import org.uofm.ot.activator.adapter.gateway.SockPuppet;
import org.uofm.ot.activator.adapter.gateway.SockResponseProcessor;
import org.uofm.ot.activator.adapter.gateway.WebSockMessage;
import org.uofm.ot.activator.exception.OTExecutionStackException;

@Component
public class JupyterKernelAdapter implements ServiceAdapter {

  public RestClient restClient;
  //public SockPuppet sockClient;
  //public SockResponseProcessor msgProcessor;

  @Value("${ipython.kernelgateway.host}")
  public String host = "localhost";
  @Value("${ipython.kernelgateway.port}")
  public String port = "8888";
  @Value("${ipython.kernelgateway.maxDuration}")
  long maxDuration = 10_000_000_000L;
  @Value("${ipython.kernelgateway.pollInterval}")
  long pollInterval = 50_000_000;

  public JupyterKernelAdapter() {
    URI restUri = URI.create("http://" + host + ":" + port);
    restClient = new RestClient(restUri);
    //sockClient = new SockPuppet();
    //msgProcessor = new SockResponseProcessor(sockClient.getMessageQ());
  }

  public Object execute(Map<String, Object> args, String code, String functionName,
      Class returnType)
      throws OTExecutionStackException {
    // Validate args
    if (code == null || code.isEmpty()) {
      throw new OTExecutionStackException(" No code to execute ");
    }
    if (functionName == null || functionName.isEmpty()) {
      throw new OTExecutionStackException(" No function name to execute ");
    }

    // Obtain kernel suitable for running python code
    KernelMetadata selectedKernel = selectKernel();
    if (selectedKernel == null) {
      throw new OTExecutionStackException(" No available Jupyter Kernel for payload ");
    }

    // Get a Session
    SessionMetadata sessionMd = restClient.startSession(selectedKernel);

    // Do web socket work and return a reference to the message que to be parsed.
    ArrayBlockingQueue<WebSockMessage> messageQ = executeViaWebSock(sessionMd, code, args, functionName);
    SockResponseProcessor msgProcessor = new SockResponseProcessor(messageQ);

    // Poll (if required) for responses
    msgProcessor.beginProcessing(maxDuration, pollInterval);

    // Terminate session
    restClient.deleteSession(sessionMd);

    // Check response for error or timeout
    if (msgProcessor.encounteredError()) {
      throw new OTExecutionStackException(
          "Error in exec environment: " + msgProcessor.getErrorMsg());
    } else if (msgProcessor.encounteredTimeout()) {
      throw new OTExecutionStackException(
          "Timeout occurred. Max duration reached before result returned.");
    }

    // Return result
    return msgProcessor.getResult();
  }

  public ArrayBlockingQueue<WebSockMessage> executeViaWebSock(SessionMetadata sessMd, String code, Map<String, Object> args, String functionName){
    // Start a new web socket client and message processor
    SockPuppet sockClient = new SockPuppet();

    // Connect to WebSocket
    String kernelId = sessMd.getKernel().getId();
    URI sockUri = URI.create(
        String.format("ws://%s:%s/api/kernels/%s/channels", host, port, kernelId));
    sockClient.connectToServer(sockUri);

    // Send code to allow JSON output from IPython kernel:
    sockClient.sendPayload("from IPython.display import JSON", sessMd.getId());

    // Send knowledge object function code to the kernel
    sockClient.sendPayload(code, sessMd.getId());

    // Send payload to call the kobject function
    sockClient.sendPayload(buildCallingPayload(args, functionName), sessMd.getId());

    // Instruct IPython to serialize the result as json
    sockClient.sendPayload("JSON(result)", sessMd.getId());

    //return reference to the WebSocket message queue
    return sockClient.getMessageQ();
  }

  // To which kernel should the code be sent?
  public KernelMetadata selectKernel() {
    KernelMetadata kernel;

    //Get list of kernels that match criteria
    List<KernelMetadata> kernels = restClient.getKernels();
    Optional<KernelMetadata> selectedKernel = kernels.stream()
        .filter(metadata -> metadata.getName().contains("python"))
        .findFirst();

    //Ask to create a new kernel if one is not available.
    if (selectedKernel.isPresent()) {
      kernel = selectedKernel.get();
    } else {
      kernel = restClient.startKernel();
    }

    return kernel;
  }

  // Generate payload for calling kobject function.
  public String buildCallingPayload(Map args, String functionName) {
    String payload;

    if (args.isEmpty()) {
      payload = "result = " + functionName + "()";
    } else {
      try {
        StringBuilder sb = new StringBuilder();
        sb.append("import json\n")
            .append(String
                .format("args = json.loads('%s')", new ObjectMapper().writeValueAsString(args)))
            .append("\n")
            .append(String.format("result = %s(args)", functionName));
        payload = sb.toString();
      } catch (JsonProcessingException e) {
        throw new OTExecutionStackException("Error serializing args.", e);
      }
    }

    return payload;
  }

  public List<String> supports() {
    List<String> languages = new ArrayList<>();
    languages.add("Python");
    languages.add("Python3");
    return languages;
  }
}
