package org.example;

import org.apache.jmeter.assertions.ResponseAssertion;
import org.apache.jmeter.assertions.gui.AssertionGui;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.extractor.json.jsonpath.JSONPostProcessor;
import org.apache.jmeter.extractor.json.jsonpath.gui.JSONPostProcessorGui;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.gui.HeaderPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;

import java.io.FileOutputStream;
// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) throws Exception{
        // Create the Jmeter engine
        StandardJMeterEngine jm = new StandardJMeterEngine();

        // Set some configuration
        String jmeterHome = "D:\\Users\\jserna\\Downloads\\apache-jmeter-5.6.2\\apache-jmeter-5.6.2";
        JMeterUtils.setJMeterHome(jmeterHome);
        JMeterUtils.loadJMeterProperties(jmeterHome + "/bin/jmeter");
        JMeterUtils.initLocale();

        // Create a new hash tree to hold our test elements
        HashTree testPlanTree = new HashTree();

        // Create Header Manager
        HeaderManager manager = new HeaderManager();
        manager.add(new Header("Content-Type", "application/json"));
        manager.setName(JMeterUtils.getResString("header_manager_title")); // $NON-NLS-1$
        manager.setProperty(TestElement.TEST_CLASS, HeaderManager.class.getName());
        manager.setProperty(TestElement.GUI_CLASS, HeaderPanel.class.getName());

        String jsonData = "{ \"username\": \"your_username\", \"password\": \"your_password?\", \"twoFactorCode\": \"\", \"twoFactorRecoveryCode\" : \"\"}";
        HTTPArgument arg =new HTTPArgument("", jsonData, null, true);
        arg.setAlwaysEncoded(false);

        // Create a sampler
        HTTPSampler httpSamplerOne = new HTTPSampler();
        httpSamplerOne.setDomain("localhost");
        httpSamplerOne.setPort(7084);
        httpSamplerOne.setPath("/identity/login");
        httpSamplerOne.setMethod("POST");
        httpSamplerOne.setName("HTTP Request One");
        httpSamplerOne.setProtocol("https");
        httpSamplerOne.getArguments().addArgument(arg);
        httpSamplerOne.setProperty(TestElement.TEST_CLASS, HTTPSampler.class.getName());
        httpSamplerOne.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());

        // Create Header Manager
        HeaderManager managerTwo = new HeaderManager();
        managerTwo.add(new Header("Authorization", "Bearer ${auth_token}"));
        managerTwo.setName(JMeterUtils.getResString("header_manager_title")); // $NON-NLS-1$
        managerTwo.setProperty(TestElement.TEST_CLASS, HeaderManager.class.getName());
        managerTwo.setProperty(TestElement.GUI_CLASS, HeaderPanel.class.getName());

        // Create another sampler
        HTTPSampler httpSamplerTwo = new HTTPSampler();
        httpSamplerTwo.setDomain("localhost");
        httpSamplerTwo.setPort(7084);
        httpSamplerTwo.setPath("/weatherforecast");
        httpSamplerTwo.setMethod("GET");
        httpSamplerTwo.setName("HTTP Request");
        httpSamplerTwo.setProtocol("https");
        httpSamplerTwo.setProperty(TestElement.TEST_CLASS, HTTPSampler.class.getName());
        httpSamplerTwo.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());

        // JSON Post Processor
        JSONPostProcessor jsonExtractor = new JSONPostProcessor();
        jsonExtractor.setName("JSON Extractor");
        jsonExtractor.setRefNames("auth_token");
        jsonExtractor.setJsonPathExpressions("$.access_token");
        jsonExtractor.setProperty(TestElement.TEST_CLASS, JSONPostProcessor.class.getName());
        jsonExtractor.setProperty(TestElement.GUI_CLASS, JSONPostProcessorGui.class.getName());

        // Response Assertion
        ResponseAssertion ra = new ResponseAssertion();
        ra.setProperty(TestElement.GUI_CLASS, AssertionGui.class.getName());
        ra.setName(JMeterUtils.getResString("assertion_title"));
        ra.setTestFieldResponseCode();
        ra.setToEqualsType();
        ra.addTestString("200");

        // Create a loop controller
        LoopController loopController = new LoopController();
        loopController.setLoops(20);
        loopController.setFirst(true);
        loopController.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
        loopController.setProperty(TestElement.GUI_CLASS, LoopControlPanel.class.getName());
        loopController.initialize();

        // Create a thread group
        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setName("First Thread Group");
        threadGroup.setNumThreads(2);
        threadGroup.setRampUp(1);
        threadGroup.setSamplerController(loopController);
        threadGroup.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
        threadGroup.setProperty(TestElement.GUI_CLASS, ThreadGroupGui.class.getName());

        // Create a test plan
        TestPlan testPlan = new TestPlan("Test Plan");
        testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
        testPlan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());
        testPlan.setUserDefinedVariables((Arguments) new ArgumentsPanel().createTestElement());

        // Add the test plan to our hash tree, this is the top level of our test
        testPlanTree.add(testPlan);

        // HTTP Request Sampler and Header Manager
        HashTree requestHashTree = new HashTree();
        requestHashTree.add(httpSamplerOne, manager);
        requestHashTree.add(httpSamplerOne, jsonExtractor);
        requestHashTree.add(httpSamplerOne, ra);

        // HTTP Request Sampler and Header Manager
        HashTree requestHashTreeTwo = new HashTree();
        requestHashTreeTwo.add(httpSamplerTwo, managerTwo);
        requestHashTreeTwo.add(httpSamplerTwo, ra);

        // Construct Test Plan from previously initialized elements
        testPlanTree.add(testPlan);
        HashTree threadGroupHashTree = testPlanTree.add(testPlan, threadGroup);
        threadGroupHashTree.add(requestHashTree);

        // Add the http sampler to the hash tree that contains the thread group
        threadGroupHashTree.add(requestHashTreeTwo);

        // Build a Jmeter test after execution
        SaveService.saveTree(testPlanTree, new FileOutputStream(jmeterHome + "/bin/FirstTest.jmx"));

        // Summariser
        Summariser summariser = null;
        String summariserName = JMeterUtils.getPropDefault("summarise.names", "summary response");
        if (summariserName.length() > 0) {
            summariser = new Summariser(summariserName);
        }

        ResultCollector logger = new ResultCollector(summariser);
        testPlanTree.add(testPlanTree.getArray()[0], logger);

        // Write to a file
        ResultCollector rc = new ResultCollector();
        rc.setEnabled(true);
        rc.setErrorLogging(false);
        rc.isSampleWanted(true);
        SampleSaveConfiguration ssc = new SampleSaveConfiguration();
        ssc.setTime(true);
        ssc.setAssertionResultsFailureMessage(true);
        ssc.setThreadCounts(true);
        rc.setSaveConfig(ssc);
        rc.setFilename(jmeterHome + "/bin/FirstTest.jtl");
        testPlanTree.add(testPlanTree.getArray()[0], rc);

        // Configure
        jm.configure(testPlanTree);

        // Run
        jm.run();
    }
}