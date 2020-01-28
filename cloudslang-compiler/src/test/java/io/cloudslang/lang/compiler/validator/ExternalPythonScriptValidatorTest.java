package io.cloudslang.lang.compiler.validator;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.List;

public class ExternalPythonScriptValidatorTest {
    private static String LINE_SEPARATOR = System.lineSeparator();
    private static final String VALID_PYTHON_SCRIPT = "def execute({params}):" + LINE_SEPARATOR
            + "  a = in1" + LINE_SEPARATOR
            + "  return locals()" + LINE_SEPARATOR;
    @Rule
    public ExpectedException exception = ExpectedException.none();


    @Test
    public void testInvalidMethodInputs() {
        List<String> inputs = Arrays.asList("in1", "pass");
        ExternalPythonScriptValidator validator = new ExternalPythonScriptValidatorImpl();
        expectException("Illegal input names: pass");
        validator.validateExecutionMethodAndInputs(generateScript(inputs), inputs);
    }

    @Test
    public void testInvalidMethodInputs2() {
        List<String> inputs = Arrays.asList("in1", "in2");
        List<String> scriptInputs = Arrays.asList("in1", "invalid");
        ExternalPythonScriptValidator validator = new ExternalPythonScriptValidatorImpl();
        expectException("Inputs are not defined for all execute method parameters.");
        validator.validateExecutionMethodAndInputs(generateScript(scriptInputs), inputs);
    }

    @Test
    public void testValidMethodInputs() {
        List<String> inputs = Arrays.asList("in1", "in2");
        ExternalPythonScriptValidator validator = new ExternalPythonScriptValidatorImpl();
        validator.validateExecutionMethodAndInputs(generateScript(inputs), inputs);
    }

    @Test
    public void testValidMethodInputs2() {
        List<String> inputs = Arrays.asList("in1", "in2", "in3");
        List<String> scriptInputs = Arrays.asList("in1", "in2");
        ExternalPythonScriptValidator validator = new ExternalPythonScriptValidatorImpl();
        validator.validateExecutionMethodAndInputs(generateScript(scriptInputs), inputs);
    }

    private void expectException(String message) {
        exception.expect(RuntimeException.class);
        exception.expectMessage(message);
    }

    private String generateScript(List<String> params) {
        return VALID_PYTHON_SCRIPT.replace("{params}", String.join(", ", params));
    }
}
