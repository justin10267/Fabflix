import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class DomActors63AndCasts124Parser {
    List<Star> stars = new ArrayList<>();
    Set<String> processedActors = new HashSet<>();
    Document dom;
    public void runParse() throws FileNotFoundException {
        parseActors63XmlFile();
        parseActors63Document();
        printData();
        parseCast124XmlFile();
        parseCast124Document();
        printData();
    }

    private void parseActors63XmlFile() throws FileNotFoundException {
        InputStream inputStream = new FileInputStream("actors63.xml");
        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.ISO_8859_1);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            dom = documentBuilder.parse(new InputSource(reader));
        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
    }

    private void parseActors63Document() {
        Element documentElement = dom.getDocumentElement();
        NodeList nodeList = documentElement.getElementsByTagName("actor");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element element = (Element) nodeList.item(i);
            Star star = parseStar(element);
            if (star != null) {
                stars.add(star);
            }
        }
    }

    private Star parseStar(Element element) {
        String name = getTextValue(element, "stagename");
        int birthYear = getIntValue(element, "dob");
        String actorIdentifier = name + birthYear;
        if (name != null && birthYear != 0 && !processedActors.contains(actorIdentifier)) {
            processedActors.add(actorIdentifier);
            return new Star(name, birthYear);
        }
        return null;
    }

    private void parseCast124XmlFile() throws FileNotFoundException {
        InputStream inputStream = new FileInputStream("casts124.xml");
        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.ISO_8859_1);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            dom = documentBuilder.parse(new InputSource(reader));
        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
    }

    private void parseCast124Document() {
        Element documentElement = dom.getDocumentElement();
        NodeList nodeList = documentElement.getElementsByTagName("actor");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element element = (Element) nodeList.item(i);
            Star star = parseStar(element);
            if (star != null) {
                stars.add(star);
            }
        }
    }

    private String getTextValue(Element element, String tagName) {
        String textVal = null;
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            if (nodeList.item(0) != null) {
                textVal = nodeList.item(0).getTextContent();
            }
        }
        return textVal;
    }

    private int getIntValue(Element ele, String tagName) {
        try {
            return Integer.parseInt(getTextValue(ele, tagName));
        } catch (NumberFormatException e) {
            //e.printStackTrace();
            return 0;
        }
    }

    private void printData() {
        System.out.println("Total parsed " + stars.size() + " stars");
        for (Star star : stars) {
            System.out.println("\t" + star.toString());
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        DomActors63AndCasts124Parser domActors63AndCasts124Parser = new DomActors63AndCasts124Parser();
        domActors63AndCasts124Parser.runParse();
    }
}