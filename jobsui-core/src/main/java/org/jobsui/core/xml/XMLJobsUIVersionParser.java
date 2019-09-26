package org.jobsui.core.xml;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.net.URL;

public class XMLJobsUIVersionParser extends DefaultHandler {
    private final String rootName;
    private String version;

    static String getInProject(URL url) throws Exception {
        return getJobsUIVersion(url, "Project");
    }

    static String getInJob(URL url) throws Exception {
        return getJobsUIVersion(url, "Job");
    }

    private static String getJobsUIVersion(URL url, String rootName) throws Exception {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxParserFactory.newSAXParser();

        String version;

        try (InputStream inputStream = url.openStream()) {
            XMLJobsUIVersionParser parser =  new XMLJobsUIVersionParser(rootName);
            saxParser.parse(inputStream, parser);

            version = parser.version;
        }

        if (version == null) {
            return "0.0.0";
        }
        return version;
    }

    private XMLJobsUIVersionParser(String rootName) {
        this.rootName = rootName;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if (qName.equalsIgnoreCase(rootName)) {
            version = attributes.getValue("jobsUIVersion");
        }
    }

}
