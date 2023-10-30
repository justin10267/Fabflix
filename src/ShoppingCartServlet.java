import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

@WebServlet(name = "ShoppingCartServlet", urlPatterns = "/api/cart")
public class ShoppingCartServlet extends HttpServlet {

    public class MovieItem {
        private String title;
        private int quantity;
        private double price;

        public MovieItem(String title, double price) {
            this.title = title;
            this.price = price;
            this.quantity = 1; // default
        }

        public double getTotalPrice() {
            return this.price * this.quantity;
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        JsonObject responseObject = new JsonObject();
        HttpSession session = request.getSession();
        User loggedInUser = (User) session.getAttribute("user");
        if (loggedInUser == null) {
            responseObject.addProperty("status", "failure");
            responseObject.addProperty("message", "User not logged in");
            response.getWriter().write(responseObject.toString());
            return;
        }

        String userId = loggedInUser.getUsername();
        String cartKey = "cart_" + userId;

        HashMap<String, MovieItem> cart;
        if (session.getAttribute(cartKey) instanceof HashMap) {
            cart = (HashMap<String, MovieItem>) session.getAttribute(cartKey);
        } else {
            cart = new HashMap<>();
        }

        double totalPrice = cart.values().stream().mapToDouble(MovieItem::getTotalPrice).sum();
        JsonArray itemsArray = new JsonArray();

        for (MovieItem item : cart.values()) {
            JsonObject itemObject = new JsonObject();
            itemObject.addProperty("title", item.title);
            itemObject.addProperty("price", item.price);
            itemObject.addProperty("quantity", item.quantity);
            itemsArray.add(itemObject);
        }

        responseObject.add("items", itemsArray);
        responseObject.addProperty("totalPrice", totalPrice);

        response.getWriter().write(responseObject.toString());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        JsonObject responseObject = new JsonObject();
        HttpSession session = request.getSession();
        User loggedInUser = (User) session.getAttribute("user");
        if (loggedInUser == null) {
            responseObject.addProperty("status", "failure");
            responseObject.addProperty("message", "User not logged in");
            response.getWriter().write(responseObject.toString());
            return;
        }
        String userId = loggedInUser.getUsername();
        String cartKey = "cart_" + userId;

        try {
            String action = request.getParameter("action");
            String title = request.getParameter("title");

            if(action == null || title == null) {
                throw new IllegalArgumentException("Missing required parameters.");
            }

            HashMap<String, MovieItem> cart;
            if (session.getAttribute(cartKey) instanceof HashMap) {
                cart = (HashMap<String, MovieItem>) session.getAttribute(cartKey);
            } else {
                cart = new HashMap<>();
            }

            switch (action) {
                case "add":
                    if (!cart.containsKey(title)) {
                        Random rand = new Random();
                        double randomPrice = 10 + (50 - 10) * rand.nextDouble(); // random price between 10 and 50
                        cart.put(title, new MovieItem(title, randomPrice));
                    } else {
                        MovieItem existingItem = cart.get(title);
                        existingItem.quantity += 1;
                    }
                    break;
                case "increase":
                    if (cart.containsKey(title)) {
                        MovieItem item = cart.get(title);
                        item.quantity += 1;
                    }
                    break;
                case "decrease":
                    if (cart.containsKey(title)) {
                        MovieItem item = cart.get(title);
                        item.quantity -= 1;
                        if (item.quantity <= 0) {
                            cart.remove(title);
                        }
                    }
                    break;
                case "delete":
                    cart.remove(title);
                    break;
            }

            session.setAttribute(cartKey, cart);
            responseObject.addProperty("status", "success");
        } catch (Exception e) {
            responseObject.addProperty("status", "failure");
            responseObject.addProperty("message", e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        response.getWriter().write(responseObject.toString());
    }
}