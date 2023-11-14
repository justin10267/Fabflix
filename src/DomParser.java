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
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.*;
import java.util.concurrent.*;

public class DomParser {
    private DataSource dataSource;
//    String user = "root";
//    String password = "mangobanana109";
    String user = "mytestuser";
    String password = "My6$Password";

    Set<String> processedActors = new HashSet<>();
    Map<String, Movie> movieByFid = new HashMap<>();
    Map<String, List<Star>> starsByName = new HashMap<>();
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
    Document moviesDom;
    Document starsDom;
    Document castsDom;
    public void runParse() throws FileNotFoundException {
        parseMains243XmlFile();
        parseMoviesDocument();
        parseActors63XmlFile();
        parseStarsDocument();
        parseCast124XmlFile();
        parseCastDocument();
//        printMovieData();
//        printStarData();
//        insertMoviesIntoDatabase();
//        insertStarsIntoDatabase();
//        System.out.println((chunkMovies(new ArrayList<>(movieByFid.values()), 1000)).get(1));
//        System.out.println(chunkStars(new ArrayList<>(starsByName.values()), 500).size());
        runInsertWithThreads();
    }

    public void runInsertWithThreads() throws FileNotFoundException {
        List<List<Movie>> movieChunks = chunkMovies(new ArrayList<>(movieByFid.values()), 100);
        List<List<Star>> starChunks = chunkStars(new ArrayList<>(starsByName.values()), 100);

        List<Thread> movieThreads = new ArrayList<>();
        List<Thread> starThreads = new ArrayList<>();

        // Threads for movies
        for (List<Movie> chunk : movieChunks) {
            Thread thread = new Thread(() -> insertMoviesChunkIntoDatabase(chunk));
            movieThreads.add(thread);
            thread.start();
        }

        // Threads for stars
        for (List<Star> chunk : starChunks) {
            Thread thread = new Thread(() -> insertStarsChunkIntoDatabase(chunk));
            starThreads.add(thread);
            thread.start();
        }

        // Wait for movie threads to finish
        for (Thread thread : movieThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Wait for star threads to finish
        for (Thread thread : starThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    private List<List<Movie>> chunkMovies(List<Movie> movies, int chunkSize) {
        List<List<Movie>> chunks = new ArrayList<>();
        for (int i = 0; i < movies.size(); i += chunkSize) {
            chunks.add(movies.subList(i, Math.min(i + chunkSize, movies.size())));
        }
        return chunks;
    }

    private List<List<Star>> chunkStars(List<List<Star>> stars, int chunkSize) {
        List<Star> flattenedList = new ArrayList<>();
        for (List<Star> starList : stars) {
            flattenedList.addAll(starList);
        }
        List<List<Star>> chunks = new ArrayList<>();
        for (int i = 0; i < flattenedList.size(); i += chunkSize) {
            chunks.add(flattenedList.subList(i, Math.min(i + chunkSize, flattenedList.size())));
        }
        return chunks;
    }

    private void insertMoviesChunkIntoDatabase(List<Movie> movies) {
        try {
            String url = "jdbc:mysql://localhost:3306/moviedb";
            String user = "root";
            String password = "mangobanana109";

            Connection connection = DriverManager.getConnection(url, user, password);

            String addMovieCall = "{call add_movie_parser(?, ?, ?)}";
            String linkGenreCall = "{call link_genre_to_movie_parser(?, ?, ?, ?)}";
            String linkStarCall = "{call link_star_to_movie_parser(?, ?, ?, ?, ?)}";

            try (CallableStatement addMovieStmt = connection.prepareCall(addMovieCall);
                 CallableStatement linkGenreStmt = connection.prepareCall(linkGenreCall);
                 CallableStatement linkStarStmt = connection.prepareCall(linkStarCall)) {

                for (Movie movie : movies) {
                    // Add the movie
                    addMovieStmt.setString(1, movie.getTitle());
                    addMovieStmt.setInt(2, movie.getYear());
                    addMovieStmt.setString(3, movie.getDirector());
                    addMovieStmt.addBatch();

                    // Link genres to the movie
                    for (String genre : movie.getGenres()) {
                        linkGenreStmt.setString(1, genre);
                        linkGenreStmt.setString(2, movie.getTitle());
                        linkGenreStmt.setInt(3, movie.getYear());
                        linkGenreStmt.setString(4, movie.getDirector());
                        linkGenreStmt.addBatch();
                    }

                    // Link stars to the movie
                    for (Star star : movie.getStars()) {
                        linkStarStmt.setString(1, star.getName());
                        if (star.getBirthYear() != 0) {
                            linkStarStmt.setInt(2, star.getBirthYear());
                        } else {
                            linkStarStmt.setNull(2, java.sql.Types.INTEGER);
                        }
                        linkStarStmt.setString(3, movie.getTitle());
                        linkStarStmt.setInt(4, movie.getYear());
                        linkStarStmt.setString(5, movie.getDirector());
                        linkStarStmt.addBatch();
                    }
                }

                // Execute all batches
                System.out.println(addMovieStmt);
                System.out.println(linkGenreStmt);
                System.out.println(linkStarStmt);
                addMovieStmt.executeBatch();
                linkGenreStmt.executeBatch();
                linkStarStmt.executeBatch();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertStarsChunkIntoDatabase(List<Star> stars) {
        try {
            String url = "jdbc:mysql://localhost:3306/moviedb";

            Connection connection = DriverManager.getConnection(url, user, password);

            String addStarCall = "{call add_star_parser(?, ?)}";

            try (CallableStatement addStarStmt = connection.prepareCall(addStarCall)) {

                for (Star star : stars) {
                    addStarStmt.setString(1, star.getName());
                    if (star.getBirthYear() != 0) {
                        addStarStmt.setInt(2, star.getBirthYear());
                    } else {
                        addStarStmt.setNull(2, java.sql.Types.INTEGER);
                    }
                    addStarStmt.addBatch();
                }

                // Execute the batch statement
                System.out.println(addStarStmt);
                addStarStmt.executeBatch();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseMains243XmlFile() throws FileNotFoundException {
        InputStream inputStream = new FileInputStream("./XMLDataAndParser/mains243.xml");
        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.ISO_8859_1);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            moviesDom = documentBuilder.parse(new InputSource(reader));
        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
    }

    private void parseMoviesDocument() {
        Element documentElement = moviesDom.getDocumentElement();
        NodeList directorFilmsNodeList = documentElement.getElementsByTagName("directorfilms");
        for (int i = 0; i < directorFilmsNodeList.getLength(); i++) {
            Element directorFilms = (Element) directorFilmsNodeList.item(i);
            NodeList filmsNodeList = directorFilms.getElementsByTagName("film");
            for (int j = 0; j < filmsNodeList.getLength(); j++) {
                Element film = (Element) filmsNodeList.item(j);
                Movie movie = parseMovie(film);
                if (movie != null) {
                    movieByFid.put(movie.getId(), movie);
                } else {
                    logInconsistency("Movie", film);
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

    private void parseCast124XmlFile() throws FileNotFoundException {
        InputStream inputStream = new FileInputStream("./XMLDataAndParser/casts124.xml");
        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.ISO_8859_1);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            castsDom = documentBuilder.parse(new InputSource(reader));
        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
    }

    private void parseCastDocument() {
        Element documentElement = castsDom.getDocumentElement();
        NodeList dirfilmsNodeList = documentElement.getElementsByTagName("dirfilms");
        for (int i = 0; i < dirfilmsNodeList.getLength(); i++) {
            Element dirfilmsElement = (Element) dirfilmsNodeList.item(i);
            NodeList filmcNodeList = dirfilmsElement.getElementsByTagName("filmc");

            for (int j = 0; j < filmcNodeList.getLength(); j++) {
                Element filmcElement = (Element) filmcNodeList.item(j);
                NodeList mNodeList = filmcElement.getElementsByTagName("m");

                for (int k = 0; k < mNodeList.getLength(); k++) {
                    Element mElement = (Element) mNodeList.item(k);
                    String movieId = getTextValue(mElement, "f");
                    String starName = getTextValue(mElement, "a");
                    if (movieByFid.containsKey(movieId)) {
                        Movie movie = movieByFid.get(movieId);
                        List<Star> starsWithName = starsByName.get(starName);
                        Star star;
                        if (starsWithName != null && !starsWithName.isEmpty()) {
                            star = starsWithName.get(0);
                            starsWithName.remove(star);
                            if (starsWithName.isEmpty()) {
                                starsByName.remove(starName);
                            }
                        } else {
                            star = new Star(starName, 0);
                        }
                        movie.addStar(star);
                    }
                }
            }
        }
    }

    private void parseActors63XmlFile() throws FileNotFoundException {
        InputStream inputStream = new FileInputStream("./XMLDataAndParser/actors63.xml");
        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.ISO_8859_1);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            starsDom = documentBuilder.parse(new InputSource(reader));
        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
    }

    private void parseStarsDocument() {
        Element documentElement = starsDom.getDocumentElement();
        NodeList nodeList = documentElement.getElementsByTagName("actor");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element element = (Element) nodeList.item(i);
            Star star = parseStar(element);
            if (star != null) {
                String name = star.getName();
                if (!starsByName.containsKey(name)) {
                    starsByName.put(name, new ArrayList<>());
                }
                List<Star> starList = starsByName.get(name);
                starList.add(star);
            }
        }
    }

    private Star parseStar(Element element) {
        String name = getTextValue(element, "stagename");
        int birthYear = getIntValue(element, "dob");
        String actorIdentifier = name + birthYear;
        if (name != null && !processedActors.contains(actorIdentifier)) {
            processedActors.add(actorIdentifier);
            return new Star(name, birthYear);
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
            //e.printStackTrace();
            return 0;
        }
    }

    private void printMovieData() {
        System.out.println("Total parsed " + movieByFid.size() + " movies");
        for (Map.Entry<String, Movie> entry : movieByFid.entrySet()) {
            System.out.println("\tFID: " + entry.getKey() + ", Movie: " + entry.getValue().toString());
        }
    }

    private void printStarData() {
        System.out.println("Total parsed " + starsByName.size() + " stars");
        for (Map.Entry<String, List<Star>> entry : starsByName.entrySet()) {
            System.out.println("Star Name: " + entry.getKey());
            for (Star star : entry.getValue()) {
                System.out.println("\t" + star.toString());
            }
        }
    }

    private void logInconsistency(String elementType, Element element) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("error.log", true)))) {
            out.println("Inconsistent " + elementType + " element - Tag: " + element.getTagName() + ", Value: " + element.getTextContent());
            out.flush(); // This ensures the content is immediately written
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logTime(String message) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("time.log", true)))) {
            out.println(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void insertMoviesIntoDatabase() {
        try {
            String url = "jdbc:mysql://localhost:3306/moviedb";

            Connection connection = DriverManager.getConnection(url, user, password);

            String addMovieCall = "{call add_movie_parser(?, ?, ?)}";
            String linkGenreCall = "{call link_genre_to_movie_parser(?, ?, ?, ?)}";
            String linkStarCall = "{call link_star_to_movie_parser(?, ?, ?, ?, ?)}";

            try (CallableStatement addMovieStmt = connection.prepareCall(addMovieCall);
                 CallableStatement linkGenreStmt = connection.prepareCall(linkGenreCall);
                 CallableStatement linkStarStmt = connection.prepareCall(linkStarCall)) {

                for (Movie movie : movieByFid.values()) {
                    // Add the movie
                    addMovieStmt.setString(1, movie.getTitle());
                    addMovieStmt.setInt(2, movie.getYear());
                    addMovieStmt.setString(3, movie.getDirector());
                    addMovieStmt.addBatch();

                    // Link genres to the movie
                    for (String genre : movie.getGenres()) {
                        linkGenreStmt.setString(1, genre);
                        linkGenreStmt.setString(2, movie.getTitle());
                        linkGenreStmt.setInt(3, movie.getYear());
                        linkGenreStmt.setString(4, movie.getDirector());
                        linkGenreStmt.addBatch();
                    }

                    // Link stars to the movie
                    for (Star star : movie.getStars()) {
                        linkStarStmt.setString(1, star.getName());
                        if (star.getBirthYear() != 0) {
                            linkStarStmt.setInt(2, star.getBirthYear());
                        } else {
                            linkStarStmt.setNull(2, java.sql.Types.INTEGER);
                        }
                        linkStarStmt.setString(3, movie.getTitle());
                        linkStarStmt.setInt(4, movie.getYear());
                        linkStarStmt.setString(5, movie.getDirector());
                        linkStarStmt.addBatch();
                    }
                }

                // Execute all batches
                System.out.println(addMovieStmt);
                System.out.println(linkGenreStmt);
                System.out.println(linkStarStmt);
                addMovieStmt.executeBatch();
                linkGenreStmt.executeBatch();
                linkStarStmt.executeBatch();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertStarsIntoDatabase() {
        try {
            String url = "jdbc:mysql://localhost:3306/moviedb";

            Connection connection = DriverManager.getConnection(url, user, password);

            String addStarCall = "{call add_star(?, ?)}";
            connection.setAutoCommit(false);

            for (List<Star> stars : starsByName.values()) {
                try (CallableStatement addStarStmt = connection.prepareCall(addStarCall)) {
                    for (Star star : stars) {
                        addStarStmt.setString(1, star.getName());
                        if (star.getBirthYear() != 0) {
                            addStarStmt.setInt(2, star.getBirthYear());
                        } else {
                            addStarStmt.setNull(2, java.sql.Types.INTEGER);
                        }
                        addStarStmt.addBatch();
                    }
                    System.out.println(addStarStmt);
                    addStarStmt.executeBatch();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            connection.commit();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void TESTinsertMoviesIntoDatabase() {
        try {
            String url = "jdbc:mysql://localhost:3306/moviedb";

            Connection connection = DriverManager.getConnection(url, user, password);

            String addMovieCall = "{call add_movie_parser(?, ?, ?)}";
            String linkGenreCall = "{call link_genre_to_movie_parser(?, ?, ?, ?)}";
            String linkStarCall = "{call link_star_to_movie_parser(?, ?, ?, ?, ?)}";

            try (CallableStatement addMovieStmt = connection.prepareCall(addMovieCall);
                 CallableStatement linkGenreStmt = connection.prepareCall(linkGenreCall);
                 CallableStatement linkStarStmt = connection.prepareCall(linkStarCall)) {

                addMovieStmt.setString(1, "MOVIE10");
                addMovieStmt.setInt(2, 2002);
                addMovieStmt.setString(3, "DIRECTOR1");
                addMovieStmt.addBatch();

                linkGenreStmt.setString(1, "Action");
                linkGenreStmt.setString(2, "MOVIE10");
                linkGenreStmt.setInt(3, 2002);
                linkGenreStmt.setString(4, "DIRECTOR1");
                linkGenreStmt.addBatch();

                linkStarStmt.setString(1, "STAR1");
                linkStarStmt.setInt(2, 2002);
                linkStarStmt.setString(3, "MOVIE10");
                linkStarStmt.setInt(4, 2002);
                linkStarStmt.setString(5, "DIRECTOR1");
                linkStarStmt.addBatch();

                addMovieStmt.setString(1, "MOVIE11");
                addMovieStmt.setInt(2, 2002);
                addMovieStmt.setString(3, "DIRECTOR1");
                addMovieStmt.addBatch();

                linkGenreStmt.setString(1, "Action");
                linkGenreStmt.setString(2, "MOVIE11");
                linkGenreStmt.setInt(3, 2002);
                linkGenreStmt.setString(4, "DIRECTOR1");
                linkGenreStmt.addBatch();

                linkStarStmt.setString(1, "STAR1");
                linkStarStmt.setInt(2, 2002);
                linkStarStmt.setString(3, "MOVIE11");
                linkStarStmt.setInt(4, 2002);
                linkStarStmt.setString(5, "DIRECTOR1");
                linkStarStmt.addBatch();

                // Execute all batches
                System.out.println(addMovieStmt);
                System.out.println(linkGenreStmt);
                System.out.println(linkStarStmt);
                addMovieStmt.executeBatch();
                linkGenreStmt.executeBatch();
                linkStarStmt.executeBatch();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        DomParser domParser = new DomParser();

//        long startTime = System.nanoTime();
//        domParser.runParseWithThreads();
//        long endTime = System.nanoTime();
//        long timeElapsedWithThreads = endTime - startTime;
//        domParser.logTime("Time with threads: " + (timeElapsedWithThreads / 1e6) + " milliseconds");

        long startTime = System.nanoTime();
        domParser.runParse();
        long endTime = System.nanoTime();
        long timeElapsedWithoutThreads = endTime - startTime;
        domParser.logTime("Time without threads: " + (timeElapsedWithoutThreads / 1e6) + " milliseconds");
    }

}
