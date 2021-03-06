package org.uofm.ot.activator.adapter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import java.util.HashMap;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.uofm.ot.activator.exception.OTExecutionStackException;

/**
 * Created by grosscol on 2017-06-20.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {JupyterKernelAdapter.class})
@TestPropertySource("classpath:application.properties")
@Category(org.uofm.ot.activator.adapter.IntegrationTest.class)
public class JupyterKernelIntegrationTest {

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  @Autowired
  JupyterKernelAdapter adapter;

  @Test
  public void runHello() throws Exception {
    String code = TestUtils.loadFixture("payload-hello-no-params.py");
    Object result = adapter.execute(new HashMap<>(), code, "hello", Map.class);
    assertThat(((Map) result).get("Hello"), equalTo("World"));
  }

  @Test
  public void runNumbers() throws Exception {
    String code = TestUtils.loadFixture("payload-numbers.py");
    HashMap<String, Object> args = new HashMap<>();
    int[] val = new int[]{0, 1, 2, 3, 4, 5};
    args.put("inputs", val);

    Object result = adapter.execute(args, code, "numbers", Map.class);
    assertThat(((Map) result).get("sum"), equalTo(15));
  }

  @Test
  public void runBadSyntax() throws Exception {
    String code = TestUtils.loadFixture("payload-syntax-error.py");

    expectedEx.expect(OTExecutionStackException.class);
    expectedEx.expectMessage("Error in exec environment: SyntaxError");

    adapter.execute(new HashMap<>(), code, "bad_function", Map.class);
  }

  @Test
  public void runRuntimeError() throws Exception {
    String code = TestUtils.loadFixture("payload-runtime-error.py");

    expectedEx.expect(OTExecutionStackException.class);
    expectedEx.expectMessage("Error in exec environment");

    Object result = adapter.execute(new HashMap<>(), code, "bad_function", Map.class);

  }
}

