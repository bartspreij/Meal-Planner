package mealplanner.logic;

import java.sql.*;
import java.util.ArrayList;


public class MealPlannerDB {

    private final String DB_URL = "jdbc:postgresql:meals_db";
    private final String USER = "postgres";
    private final String PASS = "1111";
    private int meal_id = 1;
    private int ingredient_id = 1;
    private Connection connection;
    private Statement statement;
    private PreparedStatement pstmt;

    public MealPlannerDB() {
        try {
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            connection.setAutoCommit(true);
            statement = connection.createStatement();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void connect() {
        try {
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            connection.setAutoCommit(true);
            statement = connection.createStatement();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void disconnect() {
        try {
            pstmt.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            System.out.println("Error disconnecting" + e.getMessage());
        }
    }

    public void createTables() {
        try {
            // create tables
            statement.executeUpdate("create table meals (" +
                    "category varchar(1024)," +
                    "meal varchar(1024)," +
                    "meal_id integer" +
                    ")");

            statement.executeUpdate("create table ingredients (" +
                    "ingredient varchar(1024)," +
                    "ingredient_id integer," +
                    "meal_id integer" +
                    ")");
            statement.executeUpdate("create table plan (" +
                    "meal_option varchar(1024)," +
                    "meal_category varchar(1024)," +
                    "meal_id integer," +
                    "day varchar(1024)" +
                    ")");
        } catch (SQLException e) {
            System.out.println("Error creating tables " + e.getMessage());
        }
    }

    public boolean tableExists(String table) throws SQLException {
        DatabaseMetaData dbm = connection.getMetaData();

        ResultSet tables = dbm.getTables(null, null, table, null);

        return tables.next();
    }

    public void addMeal(String category, String name) {
        try {
            pstmt = connection.prepareStatement("INSERT INTO meals (category, meal, meal_id) VALUES (?, ?, ?) RETURNING meal_id", Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, category);
            pstmt.setString(2, name);
            pstmt.setInt(3, meal_id);
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            rs.next();
            meal_id = rs.getInt(1); // store meal_id to use for ingredients


        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void addIngredients(String[] ingredients) {
        try {
            pstmt = connection.prepareStatement("INSERT INTO ingredients (ingredient, meal_id, ingredient_id) VALUES (?, ?, ?)");
            for (String ingredient : ingredients) {
                pstmt.setString(1, ingredient);
                pstmt.setInt(2, meal_id);
                pstmt.setInt(3, ingredient_id++);
                pstmt.executeUpdate();
            }
            meal_id++;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void getMeals(MealPlannerLogic mealLogic) {
        // TODO: refactor
        try (ResultSet rs = statement.executeQuery("SELECT * FROM meals;")) {

            while (rs.next()) { // for every meal

                // fetch ingredients first
                int getIdToFetchIngredients = rs.getInt("meal_id");
                pstmt = connection.prepareStatement("SELECT * FROM ingredients WHERE meal_id = ?;");
                pstmt.setInt(1, getIdToFetchIngredients);
                ResultSet ingredients = pstmt.executeQuery();

                ArrayList<String> arrIngredients = new ArrayList<>();
                while (ingredients.next()) { // store ingredients in array
                    arrIngredients.add(ingredients.getString("ingredient"));
                }

                String name = rs.getString("meal");
                String category = rs.getString("category");
                String[] ingredientList = arrIngredients.toArray(new String[0]);

                mealLogic.addMeal(category, name, ingredientList);
            }
        } catch (SQLException e) {
            System.out.println("Error getting meals: " + e.getMessage());
        }
    }

    public void clearTable(String table) {
        try {
            statement = connection.createStatement();
            String sql = "DELETE FROM " + table;
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            System.out.println("Error clearing table " + e.getMessage());
        }
    }

    public void dropTable(String table) {
        try {
            statement = connection.createStatement();
            String sql = "DROP TABLE " + table;
            statement.executeUpdate(sql);

        } catch (SQLException e) {
            System.out.println("Error deleting table: " + e.getMessage());
        }
    }

    public ResultSet queryDBForMeal(String query) {
        try {
            pstmt = connection.prepareStatement("SELECT meal, meal_id FROM meals WHERE meal = ?");
            pstmt.setString(1, query);
            pstmt.executeQuery();
            return pstmt.getResultSet();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public ResultSet queryDBForWeekPlan() {
        try {
            statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            statement.executeQuery("SELECT * FROM plan;");
            return statement.getResultSet();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public ResultSet queryDBForWeekPlanMealIds() {
        try {
            statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            statement.executeQuery("SELECT meal_id FROM plan;");
            return statement.getResultSet();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public ResultSet queryDBForIngredientsOfMealId(int id) {
        try {
            pstmt = connection.prepareStatement("SELECT * FROM ingredients WHERE meal_id = ?");
            pstmt.setInt(1, id);

            return pstmt.executeQuery();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public void addToPlan(String meal, String category, int mealID, String day) {
        try {
            pstmt = connection.prepareStatement("INSERT INTO plan (meal_option, meal_category, meal_id, day)" +
                    "VALUES (?, ?, ?, ?)");
            pstmt.setString(1, meal);
            pstmt.setString(2, category);
            pstmt.setInt(3, mealID);
            pstmt.setString(4, day);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error adding meal to plan " + e.getMessage());
        }
    }
}

