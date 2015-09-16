package org.jboss.hal.testsuite.test.configuration.undertow;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.hal.testsuite.dmr.AddressTemplate;
import org.jboss.hal.testsuite.dmr.Dispatcher;
import org.jboss.hal.testsuite.dmr.Operation;
import org.jboss.hal.testsuite.dmr.ResourceAddress;
import org.jboss.hal.testsuite.fragment.ConfigFragment;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.fragment.shared.modal.ConfirmationWindow;
import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindow;
import org.jboss.hal.testsuite.page.config.UndertowHTTPPage;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Jan Kasik <jkasik@redhat.com>
 *         Created on 15.9.15.
 */
public class HostTestCase extends UndertowTestCaseAbstract {

    @Page
    UndertowHTTPPage page;

    private final String ALIAS = "alias_i";
    private final String DEFAULT_RESPONSE_CODE = "default-response-code";
    private final String DEFAULT_WEB_MODULE = "default-web-module";
    private final String DISABLE_CONSOLE_REDIRECT = "disable-console-redirect";

    private final String ALIAS_ATTR = "alias_i";
    private final String DEFAULT_RESPONSE_CODE_ATTR = "default-response-code";
    private final String DEFAULT_WEB_MODULE_ATTR = "default-web-module";
    private final String DISABLE_CONSOLE_REDIRECT_ATTR = "disable-console-redirect";

    //values
    private final String[] ALIAS_VALUES = new String[]{"localhost", "test", "example"};
    private final String DEFAULT_RESPONSE_CODE_VALUE = "500";

    private static String httpServer;
    private AddressTemplate hostTemplate = undertowAddressTemplate.append("/host=*");
    private String httpServerHost;
    private ResourceAddress address;

    @BeforeClass
    public static void setUp() {
        httpServer = createHTTPServer(dispatcher);
    }

    @Before
    public void before() {
        httpServerHost = createHTTPServerHostHost(dispatcher);
        address = hostTemplate.resolve(context, httpServer, httpServerHost);
        page.navigate();
        page.selectHTTPServer(httpServer).switchToHosts();
    }

    @After
    public void after() {
        removeHTTPServerHost(httpServerHost);
    }

    @AfterClass
    public static void tearDown() {
        removeHTTPServer(dispatcher, httpServer);
    }

    @Test
    public void editAliases() throws IOException, InterruptedException {
        editTextAreaAndVerify(address, ALIAS, ALIAS_ATTR, ALIAS_VALUES);
    }

    @Test
    public void editDefaultResponseCode() throws IOException, InterruptedException {
        editTextAndVerify(address, DEFAULT_RESPONSE_CODE, DEFAULT_RESPONSE_CODE_ATTR, DEFAULT_RESPONSE_CODE_VALUE);
    }

    @Test
    public void editDefaultWebModule() throws IOException, InterruptedException {
        editTextAndVerify(address, DEFAULT_WEB_MODULE, DEFAULT_WEB_MODULE_ATTR);
    }

    @Test
    public void setDisableConsoleRedirectToTrue() throws IOException, InterruptedException {
        editCheckboxAndVerify(address, DISABLE_CONSOLE_REDIRECT, DISABLE_CONSOLE_REDIRECT_ATTR, true);
    }

    @Test
    public void setDisableConsoleRedirectToFalse() throws IOException, InterruptedException {
        editCheckboxAndVerify(address, DISABLE_CONSOLE_REDIRECT, DISABLE_CONSOLE_REDIRECT_ATTR, false);
    }

    @Test
    public void addHTTPServerHostInGUI() {
        String name = RandomStringUtils.randomAlphanumeric(6);
        String webModule = RandomStringUtils.randomAlphanumeric(6);
        ConfigFragment config = page.getConfigFragment();
        WizardWindow wizard = config.getResourceManager().addResource();

        Editor editor = wizard.getEditor();
        editor.text("name", name);
        editor.text(ALIAS, String.join("\n", ALIAS_VALUES));
        editor.text(DEFAULT_RESPONSE_CODE, DEFAULT_RESPONSE_CODE_VALUE);
        editor.text(DEFAULT_WEB_MODULE, webModule);
        editor.checkbox(DISABLE_CONSOLE_REDIRECT, true);
        boolean result = wizard.finish();

        Assert.assertTrue("Window should be closed", result);
        Assert.assertTrue("HTTP server host should be present in table", config.resourceIsPresent(name));
        ResourceAddress address = hostTemplate.resolve(context, httpServer, name);
        verifier.verifyResource(address, true);
        verifier.verifyAttribute(address, ALIAS_ATTR, ALIAS_VALUES);
        verifier.verifyAttribute(address, DEFAULT_RESPONSE_CODE_ATTR, DEFAULT_RESPONSE_CODE_VALUE);
        verifier.verifyAttribute(address, DEFAULT_WEB_MODULE_ATTR, webModule);
        verifier.verifyAttribute(address, DISABLE_CONSOLE_REDIRECT, true);
    }

    @Test
    public void removeHTTPServerHostInGUI(String name) {
        ConfigFragment config = page.getConfigFragment();
        config.getResourceManager()
                .removeResource(name)
                .confirm();

        Assert.assertFalse("HTTP server host should not be present in table", config.resourceIsPresent(name));
        ResourceAddress address = hostTemplate.resolve(context, httpServer, name);
        verifier.verifyResource(address, false);//HTTP server host should not be present on server
    }

    private String createHTTPServerHostHost(Dispatcher dispatcher) {
        String name = RandomStringUtils.randomAlphanumeric(6);
        ResourceAddress address = hostTemplate.resolve(context, httpServer, name);
        dispatcher.execute(new Operation.Builder("add", address).build());
        return name;
    }

    private boolean removeHTTPServerHost(String ajpListener) {
        ResourceAddress address = hostTemplate.resolve(context, httpServer, ajpListener);
        return dispatcher.execute(new Operation.Builder("remove", address).build()).isSuccessful();
    }
}
