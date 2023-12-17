### Substring Matching Design
"WHERE" +
"(SOUNDEX(UPPER(title)) = SOUNDEX(?) OR UPPER(title) LIKE %?%)\n" +
"AND year LIKE ?%\n" +
"AND (SOUNDEX(UPPER(director)) = SOUNDEX(?) OR UPPER(director) LIKE %?%)\n" +
"GROUP BY m.id , m.title , m.year , m.director , r.rating\n" +
"HAVING UPPER(stars) LIKE %?%" +

This substring matching occurs in the SEARCH_QUERY in the ListServlet.

### Demo video URL
- https://youtu.be/34alaFpLQOQ (Project 1)
- https://youtu.be/Ud1ASciy0-g (Project 2)
- https://youtu.be/L8IfwK66wAs (Project 3)
- https://youtu.be/FS-iLCJ5kPU (Project 4)

### Files with Prepared Statements
- DomParser.java
- ConfirmationServlet
- DashboardGenreServlet
- DashboardLoginServlet
- DashboardMovieServlet
- DashboardStarServlet
- ListServlet
- LoginServlet
- PlaceOrderServlet
- SingleMovieServlet
- SingleStarServlet

### Optimization Strategies
- We use batch statements instead of individually executing statements
- We made chunks of batch statements for multiple threads to work on.
