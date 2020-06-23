package org.iish.slideshow.service;

import org.iish.slideshow.configuration.Blacklist;
import org.marc4j.MarcReader;
import org.marc4j.MarcXmlReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class RandomRecord {
    private static final int MAX_RECORDS = 20;
    private static final int MIN_SIZE    = 400;

    private final String          apiUrl;
    private final String          accessToken;
    private final List<String>    formats;
    private final Blacklist       blacklist;
    private final DocumentBuilder documentBuilder;

    private static final Logger LOGGER = Logger.getLogger(RandomRecord.class.getName());

    public RandomRecord(String apiUrl, String accessToken, List<String> formats, Blacklist blacklist) {
        this.apiUrl = apiUrl;
        this.accessToken = accessToken;
        this.formats = formats;
        this.blacklist = blacklist;

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setIgnoringComments(true);
            dbf.setNamespaceAware(true);
            dbf.setValidating(false);
            this.documentBuilder = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public Record getRandomRecord() throws Exception {
        Random random = new Random();

        int numberOfRecords = 0;
        String format = null;
        while (numberOfRecords == 0) {
            int formatIdx = random.nextInt(formats.size());
            format = fixFormat(formats.get(formatIdx));
            numberOfRecords = getNumberOfRecords(format);
        }

        LOGGER.info("Searching for image with format " + format);

        Record record = null;
        while (record == null) {
            int recordIdx = random.nextInt(numberOfRecords) + 1;
            record = getRecord(format, recordIdx);
        }

        LOGGER.info("Record found with format " + format);

        return record;
    }

    private int getNumberOfRecords(String format) throws Exception {
        Document doc = getApiResult(format, 1, 0);
        NodeList nodeList = doc.getElementsByTagName("numberOfRecords");
        if (nodeList.getLength() > 0) {
            return Integer.parseInt(nodeList.item(0).getTextContent());
        }
        return 0;
    }

    private Record getRecord(String format, int index) throws Exception {
        Document doc = getApiResult(format, index, MAX_RECORDS);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        NodeList nodeList = doc.getElementsByTagName("marc:record");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            Transformer xform = transformerFactory.newTransformer();
            xform.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            xform.transform(new DOMSource(node), new StreamResult(os));

            MarcReader reader = new MarcXmlReader(new ByteArrayInputStream(os.toByteArray()));
            Record record = reader.next();

            DataField barcodeDataField = (DataField) record.getVariableField("852");
            Subfield barcodeField = barcodeDataField.getSubfield('p');
            String barcode = barcodeField.getData();

            if (!isBlacklisted(record, barcode) && isImgInSor(barcode)) {
                return record;
            }
            LOGGER.info("No image found in the SOR (or invalid size) for barcode "
                    + barcode + " for format " + format);
        }

        return null;
    }

    private boolean isBlacklisted(Record record, String barcode) {
        DataField dataField = (DataField) record.getVariableField("710");
        if (dataField != null) {
            Subfield subfield = dataField.getSubfield('a');
            if (subfield != null) {
                String organization = subfield.getData();
                for (String blacklistOrganization : blacklist.getOrganizations()) {
                    if (organization.toLowerCase().contains(blacklistOrganization.toLowerCase())) {
                        LOGGER.info("Skipped record from organization " + blacklistOrganization);
                        return true;
                    }
                }
            }
        }

        if (blacklist.getBarcodes().contains(barcode)) {
            LOGGER.info("Skipped record that was blacklisted");
            return true;
        }

        return false;
    }

    private boolean isImgInSor(String barcode) throws Exception {
        final URL url = new URL("https://hdl.handle.net/10622/" +
                URLEncoder.encode(barcode, "UTF-8")
                + "?locatt=view:level1");

        HttpURLConnection testConn = (HttpURLConnection) url.openConnection();
        testConn.setInstanceFollowRedirects(true);
        testConn.setRequestMethod("HEAD");

        if ((testConn.getResponseCode() == HttpURLConnection.HTTP_OK)
                && testConn.getHeaderField("Content-Type").equals("image/jpeg")) {
            BufferedImage image = ImageIO.read(url);
            if ((image.getWidth() >= MIN_SIZE) || (image.getHeight() >= MIN_SIZE)) {
                return true;
            }
            LOGGER.info("Invalid size: " + image.getWidth() + ", " + image.getHeight());
        }

        return false;
    }

    private Document getApiResult(String format, int startRecord, int maxRecords) throws Exception {
        InputStream in = new URL(apiUrl + "?query=dc.type+%3D+%22" + URLEncoder.encode(format, "UTF-8") + "%22" +
                "&version=1.1" +
                "&operation=searchRetrieve" +
                "&recordSchema=info%3Asrw%2Fschema%2F1%2Fmarcxml-v1.1" +
                "&maximumRecords=" + maxRecords +
                "&startRecord=" + startRecord +
                "&resultSetTTL=300" +
                "&recordPacking=xml").openStream();
        return documentBuilder.parse(in);
    }

    private static String fixFormat(String format) {
        format = StringUtils.capitalize(format);
        if (!format.endsWith(".")) {
            format += ".";
        }
        return format;
    }
}
