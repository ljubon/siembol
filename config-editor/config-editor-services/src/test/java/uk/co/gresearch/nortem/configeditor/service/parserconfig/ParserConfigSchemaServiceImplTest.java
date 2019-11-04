package uk.co.gresearch.nortem.configeditor.service.parserconfig;

import com.google.common.io.BaseEncoding;
import org.adrianwalker.multilinestring.Multiline;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.co.gresearch.nortem.configeditor.model.ConfigEditorResult;
import uk.co.gresearch.nortem.parsers.common.ParserResult;
import uk.co.gresearch.nortem.parsers.factory.ParserFactory;
import uk.co.gresearch.nortem.parsers.factory.ParserFactoryAttributes;
import uk.co.gresearch.nortem.parsers.factory.ParserFactoryResult;

import java.util.ArrayList;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;

public class ParserConfigSchemaServiceImplTest {
    /**
     * {
     *   "encoding" : "utf8_string",
     *   "log" : "dummy log"
     * }
     **/
    @Multiline
    public static String logUtf8;

    /**
     * {
     *   "encoding" : "hex_string",
     *   "log" : "64756D6D79206C6F67"
     * }
     **/
    @Multiline
    public static String logHex;

    private ParserConfigSchemaServiceImpl parserConfigSchemaService;
    private final String schema = "dummmy schema";
    private final String testConfig = "dummmy parser config";
    private final String testConfigs = "dummmy parser configs";
    private final String testLog = "dummy log";

    private ParserFactory parserFactory;
    private ParserFactoryResult parserFactoryResult;
    private ParserFactoryAttributes parserFactoryAttributes;
    private ParserResult parserResult;

    @Before
    public void Setup() throws Exception {
        parserFactory = Mockito.mock(ParserFactory.class);
        this.parserConfigSchemaService = new ParserConfigSchemaServiceImpl(parserFactory, schema);
        parserFactoryAttributes = new ParserFactoryAttributes();
        parserFactoryResult = new ParserFactoryResult(ParserFactoryResult.StatusCode.OK, parserFactoryAttributes);
        parserResult = new ParserResult();
        Mockito.when(parserFactory.create(any())).thenReturn(parserFactoryResult);

        Mockito.when(parserFactory.validateConfiguration(any())).thenReturn(parserFactoryResult);
        Mockito.when(parserFactory.validateConfigurations(any())).thenReturn(parserFactoryResult);
        Mockito.when(parserFactory.test(any(), any(), any())).thenReturn(parserFactoryResult);
    }

    @Test
    public void getSchemaOK() {
        ConfigEditorResult ret = parserConfigSchemaService.getSchema();
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertEquals(schema, ret.getAttributes().getRulesSchema());
    }

    @Test
    public void getTestSchemaOK() {
        ConfigEditorResult ret = parserConfigSchemaService.getTestSchema();
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertNotNull(ret.getAttributes().getTestSchema());
    }

    @Test
    public void validateConfigurationsOK() {
        ConfigEditorResult ret = parserConfigSchemaService.validateConfigurations(testConfigs);
        Mockito.verify(parserFactory, times(1)).validateConfigurations(testConfigs);
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
    }



    @Test
    public void validateConfigurationsError() {
        ConfigEditorResult ret = parserConfigSchemaService.validateConfigurations(testConfigs);
        Mockito.verify(parserFactory, times(1)).validateConfigurations(testConfigs);
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
    }

    @Test
    public void validateConfigurationOK() {
        ConfigEditorResult ret = parserConfigSchemaService.validateConfiguration(testConfig);
        Mockito.verify(parserFactory, times(1)).validateConfiguration(testConfig);
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
    }

    @Test
    public void ValidateRulesError() {
        parserFactoryAttributes.setMessage("error");
        parserFactoryResult = new ParserFactoryResult(ParserFactoryResult.StatusCode.ERROR, parserFactoryAttributes);
        Mockito.when(parserFactory.validateConfigurations(any())).thenReturn(parserFactoryResult);
        ConfigEditorResult ret = parserConfigSchemaService.validateConfigurations(testConfigs);
        Mockito.verify(parserFactory, times(1)).validateConfigurations(testConfigs);
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertEquals("error", ret.getAttributes().getMessage());
    }

    @Test
    public void ValidateRuleError() {
        parserFactoryAttributes.setMessage("error");
        
        parserFactoryResult = new ParserFactoryResult(ParserFactoryResult.StatusCode.ERROR, parserFactoryAttributes);
        Mockito.when(parserFactory.validateConfiguration(any())).thenReturn(parserFactoryResult);
        ConfigEditorResult ret = parserConfigSchemaService.validateConfiguration(testConfig);
        Mockito.verify(parserFactory, times(1)).validateConfiguration(testConfig);
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertEquals("error", ret.getAttributes().getMessage());
    }

    @Test
    public void TestConfigurationOK() {
        parserResult.setParsedMessages(new ArrayList<>());
        parserFactoryAttributes.setParserResult(parserResult);
        ConfigEditorResult ret = parserConfigSchemaService.testConfiguration(testConfig, logUtf8);
        Mockito.verify(parserFactory, times(1)).test(testConfig, null, testLog.getBytes());
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getTestResultComplete());
    }

    @Test
    public void TestConfigurationHexOK() {
        parserResult.setParsedMessages(new ArrayList<>());
        parserFactoryAttributes.setParserResult(parserResult);
        ConfigEditorResult ret = parserConfigSchemaService.testConfiguration(testConfig, logHex);
        Mockito.verify(parserFactory, times(1)).test(testConfig,
                null,
                testLog.getBytes());
        Assert.assertEquals(ConfigEditorResult.StatusCode.OK, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getTestResultComplete());
    }

    @Test
    public void TestConfigurationHexError() {
        parserResult.setParsedMessages(new ArrayList<>());
        parserFactoryAttributes.setParserResult(parserResult);
        ConfigEditorResult ret = parserConfigSchemaService.testConfiguration(testConfig,
                logHex.replace("64", "XX"));
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getException().contains("Unrecognized character: X"));
    }

    @Test
    public void TestConfigurationsError() {
        parserFactoryAttributes.setMessage("error");
        parserFactoryResult = new ParserFactoryResult(ParserFactoryResult.StatusCode.ERROR, parserFactoryAttributes);
        Mockito.when(parserFactory.test(testConfigs, null, testLog.getBytes())).thenReturn(parserFactoryResult);
        ConfigEditorResult ret = parserConfigSchemaService.testConfigurations(testConfigs, testLog);
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertEquals("Not implemented", ret.getAttributes().getMessage());
    }

    @Test
    public void TestConfigurationError() {
        parserFactoryAttributes.setMessage("error");
        parserFactoryResult = new ParserFactoryResult(ParserFactoryResult.StatusCode.ERROR, parserFactoryAttributes);
        Mockito.when(parserFactory.test(testConfig, null, testLog.getBytes())).thenReturn(parserFactoryResult);
        ConfigEditorResult ret = parserConfigSchemaService.testConfiguration(testConfig, logUtf8);
        Mockito.verify(parserFactory, times(1)).test(testConfig,
                null,
                testLog.getBytes());
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertEquals("error", ret.getAttributes().getMessage());
    }

    @Test
    public void TestConfigurationParserResultError() {
        parserResult.setException(new IllegalStateException("dummy"));
        parserFactoryAttributes.setParserResult(parserResult);

        parserFactoryResult = new ParserFactoryResult(ParserFactoryResult.StatusCode.OK, parserFactoryAttributes);
        Mockito.when(parserFactory.test(testConfig, null, testLog.getBytes())).thenReturn(parserFactoryResult);
        ConfigEditorResult ret = parserConfigSchemaService.testConfiguration(testConfig, logUtf8);
        Mockito.verify(parserFactory, times(1)).test(testConfig,
                null,
                testLog.getBytes());
        Assert.assertEquals(ConfigEditorResult.StatusCode.ERROR, ret.getStatusCode());
        Assert.assertTrue(ret.getAttributes().getMessage().contains("dummy"));
    }
}
