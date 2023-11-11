import jakarta.servlet.ServletConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DomMains243Parser {
    private DataSource dataSource;
    List<Movie> movies = new ArrayList<>();
    static Map<String, String> categoryCodes;
    static {
        categoryCodes = new HashMap<>();
        categoryCodes.put("SUSP", "Thriller");
        categoryCodes.put("CNR", "Crime");
        categoryCodes.put("DRAM", "Drama");
        categoryCodes.put("WEST", "Western");
        categoryCodes.put("MYST", "Mystery");
        categoryCodes.put("S.F.", "Sci-Fi");
        categoryCodes.put("ADVT", "Adventure");
        categoryCodes.put("HORR", "Horror");
        categoryCodes.put("ROMT", "Romance");
        categoryCodes.put("COMD", "Comedy");
        categoryCodes.put("MUSC", "Musical");
        categoryCodes.put("DOCU", "Documentary");
        categoryCodes.put("PORN", "Pornography");
        categoryCodes.put("NOIR", "Black");
        categoryCodes.put("BIOP", "Biographical Picture");
        categoryCodes.put("TV", "TV Show");
        categoryCodes.put("TVS", "TV Series");
        categoryCodes.put("TVM", "TV Miniseries");
        // Manually Created Codes
        categoryCodes.put("HIST", "History");
        categoryCodes.put("ACTN", "Action");
        categoryCodes.put("FAML", "Family");
        categoryCodes.put("CART", "Animation");
        categoryCodes.put("TVMINI", "TV Miniseries");
        categoryCodes.put("FANT", "Fantasy");
        categoryCodes.put("CNRB", "Crime");
        categoryCodes.put("ROMT COMD", "Romance Comedy");
        categoryCodes.put("SCFI", "Sci-Fi");
        categoryCodes.put("EPIC", "Epic");
    }
    Document dom;
    public void runParse() throws FileNotFoundException {
        parseXmlFile();
        parseDocument();
        printData();
    }

    private void parseXmlFile() throws FileNotFoundException {
        InputStream inputStream = new FileInputStream("mains243.xml");
        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.ISO_8859_1);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            dom = documentBuilder.parse(new InputSource(reader));
        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
    }

    private void parseDocument() {
        Element documentElement = dom.getDocumentElement();
        NodeList directorFilmsNodeList = documentElement.getElementsByTagName("directorfilms");
        for (int i = 0; i < directorFilmsNodeList.getLength(); i++) {
            Element directorFilms = (Element) directorFilmsNodeList.item(i);
            NodeList filmsNodeList = directorFilms.getElementsByTagName("films");
            for (int j = 0; j < filmsNodeList.getLength(); j++) {
                Element film = (Element) filmsNodeList.item(j);
                Movie movie = parseMovie(film);
                if (movie != null) {
                    movies.add(movie);
                }
            }
        }
    }

    private Movie parseMovie(Element element) {
        String id = getTextValue(element, "fid");
        String title = getTextValue(element, "t");
        int year = getIntValue(element, "year");
        String director = getTextValue(element,"dirn");
        List<String> genres = extractGenres(element);
        if (id != null && title != null && year != 0 && director != null && !genres.isEmpty()) {
            return new Movie(id, title, year, director, genres);
        }
        return null;
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
            e.printStackTrace();
            return 0;
        }
    }

    private ArrayList<String> extractGenres(Element element) {
        ArrayList<String> genres = new ArrayList<>();
        Element catsElement = (Element) element.getElementsByTagName("cats").item(0);
        if (catsElement == null) {
            return genres;
        }
        NodeList catElements = catsElement.getElementsByTagName("cat");
        for (int i = 0; i < catElements.getLength(); i++) {
            String genre = catElements.item(i).getTextContent().strip().toUpperCase();
            if (categoryCodes.containsKey(genre)) {
                if (genre.equals("ROMT COMD")) {
                    genres.add("Romance");
                    genres.add("Comedy");
                } else {
                    genres.add(categoryCodes.get(genre));
                }
            }
        }
        return genres;
    }

    private void printData() {
        System.out.println("Total parsed " + movies.size() + " movies");
        for (Movie movie : movies) {
            System.out.println("\t" + movie.toString());
        }
    }

    public void insertMoviesIntoDatabase() {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
            
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws FileNotFoundException {
        DomMains243Parser domMains243Parser = new DomMains243Parser();
        domMains243Parser.runParse();
    }

}
