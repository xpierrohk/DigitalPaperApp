package net.sony.dpt.command.firmware;

import net.sony.dpt.command.sync.LocalSyncProgressBar;
import net.sony.util.ProgressBar;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.junit.Assert.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class CheckFirmwareUpdatesCommandTest {

    @Rule
    public MockServerRule mockServerRule = new MockServerRule(this);

    public MockServerClient mockServerClient;

    @Test
    public void shouldParseSonyXml() throws IOException, ParserConfigurationException, SAXException {
        String xml = IOUtils.toString(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("firmwareUpgrade.xml")), StandardCharsets.UTF_8);

        FirmwareUpdatesCommand checkFirmwareUpdatesCommand = new FirmwareUpdatesCommand(null, null, null, null);
        Document document = checkFirmwareUpdatesCommand.parseXml(checkFirmwareUpdatesCommand.filterValidXml(xml));

        assertNotNull(document);
    }

    @Test
    public void shouldFindLatestVersion() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        String expected = "1.6.50.14130";

        String xml = IOUtils.toString(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("firmwareUpgrade.xml")), StandardCharsets.UTF_8);

        FirmwareUpdatesCommand checkFirmwareUpdatesCommand = new FirmwareUpdatesCommand(null, null, null, null);
        Document document = checkFirmwareUpdatesCommand.parseXml(checkFirmwareUpdatesCommand.filterValidXml(xml));

        String result = checkFirmwareUpdatesCommand.lastestVersion(document, "DPT-RP1");

        assertEquals(expected, result);
    }

    @Test
    public void shouldFindDownloadUrl() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        String expected = "https://www.sony.net/Products/DigitalPaperSystem/Software/DP/FwUpdater.pkg";

        String xml = IOUtils.toString(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("firmwareUpgrade.xml")), StandardCharsets.UTF_8);

        FirmwareUpdatesCommand checkFirmwareUpdatesCommand = new FirmwareUpdatesCommand(null, null, null, null);
        Document document = checkFirmwareUpdatesCommand.parseXml(checkFirmwareUpdatesCommand.filterValidXml(xml));

        FirmwareUpdatesCommand.Firmware result = checkFirmwareUpdatesCommand.firmware(document, "DPT-RP1");

        assertEquals(expected, result.downloadUrl);
    }

    @Test
    public void shouldDownload() throws IOException {
        byte[] firmwarePkg = IOUtils.toByteArray(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("firmware.pkg")));

        FirmwareUpdatesCommand checkFirmwareUpdatesCommand = new FirmwareUpdatesCommand(
                new LocalSyncProgressBar(
                        System.err,
                        ProgressBar.ProgressStyle.CAMEMBERT
                ),
                null,
                null,
                null
        );

        FirmwareUpdatesCommand.Firmware result = new FirmwareUpdatesCommand.Firmware();
        result.version = "JUNIT";
        result.downloadUrl = "http://localhost:" + mockServerRule.getPort() + "/firmware.pkg";
        result.size = firmwarePkg.length;

        mockServerClient
                .when(request()
                        .withMethod("GET")
                        .withPath("/firmware.pkg")
                ).respond(response().withBody(firmwarePkg)
        );

        byte[] downloaded = checkFirmwareUpdatesCommand.downloadAndVerify(result);

        assertArrayEquals(firmwarePkg, downloaded);
    }

}
