package mealplanner.logic;

import mealplanner.domain.Meal;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

public class MealPlannerLogic {
    private final ArrayList<Meal> meals;
    private final MealPlannerDB db;
    private final Scanner scanner;
    private boolean mealPlannedStatus;


    public MealPlannerLogic(MealPlannerDB db, Scanner scanner) {
        this.meals = new ArrayList<>();
        this.db = db;
        this.scanner = scanner;
        this.mealPlannedStatus = false;

    }
    public void setMealsPlanned(boolean flag) {
        this.mealPlannedStatus = flag;
    }

    public boolean getMealsPlannedOrNot() {
        return this.mealPlannedStatus;
    }

    public Collection<Meal> getMeals() {
        return this.meals;
    }

    public void addMeal(String category, String name, String[] ingredientList) {
        Meal newMeal = new Meal (name, ingredientList, category);
        meals.add(newMeal);
    }

    public void addMeal() {
        String category = promptForValidCategoryInput();
        String name = promptForValidMealName();
        String ingredientList = promptForValidIngredientList();

        String[] ingredients = ingredientList.split(",");
        for (int i = 0; i < ingredients.length; i++) {
            ingredients[i] = ingredients[i].trim();
        }

        Meal newMeal = new Meal(name, ingredients, category);
        this.meals.add(newMeal);
        System.out.println("The meal has been added!");

        db.addMeal(category, name);
        db.addIngredients(ingredients);
    }

    public String promptForValidCategoryInput() {
        String category;

        do {
            System.out.println("Which meal do you want to add (breakfast, lunch, dinner)?");
            category = scanner.nextLine();

            if (!isValidCategoryInput(category)) {
                System.out.println("Wrong meal category! Choose from: breakfast, lunch, dinner.");
            }
        } while (!isValidCategoryInput(category));

        return category;
    }

    public String promptForValidMealName() {
        String name;

        do {
            System.out.println("Input the meal's name:"); // ask for meal name
            name = scanner.nextLine().trim();

            if (!name.matches("[a-zA-Z ]+")) {
                System.out.println("Wrong format. Use letters only!");
            }
        } while (!name.matches("[a-zA-Z ]+"));

        return name;
    }

    public String promptForValidIngredientList() {
        String ingredientList;

        do {
            System.out.println("Input the ingredients:");
            ingredientList = scanner.nextLine();
        } while (!isValidIngredientList(ingredientList));

        return ingredientList;
    }

    public boolean isValidCategoryInput(String input) {
        String[] allowedWords = {"breakfast", "lunch", "dinner"};

        for (String word : allowedWords) { // if match return true
            if (input.matches(word)) {
                return true;
            }
        }
        return false; // nothing matched
    }

    public boolean isValidIngredientList(String ingredientList) {
        // Check for valid characters in the input string
        String[] subStrings = ingredientList.split(",");

        for (String subString : subStrings) {
            subString = subString.trim();

            if (!subString.matches("[a-zA-Z ]+")) {
                System.out.println("Wrong format. Use letters only!");
                return false;
            }
        }
        return true;
    }

    public void printMeals(Scanner scanner) {
        String category;
        System.out.println("Which category do you want to print (breakfast, lunch, dinner)?");

        do { // prompt until valid category
            category = scanner.nextLine();

            if (!isValidCategoryInput(category)) {
                System.out.println("Wrong meal category! Choose from: breakfast, lunch, dinner.");
            }

        } while (!isValidCategoryInput(category));

        printCategory(category, 1);
    }

    public void printCategory(String category, int caseNum) {

        boolean noMealsInCategory = meals.stream()
                .noneMatch(meal -> meal.getCategory().equals(category));

        switch (caseNum) {
            case 1 -> {
                if (noMealsInCategory) {
                    System.out.println("No meals found.");
                } else {
                    System.out.println("Category: " + category);
                    for (Meal meal : meals) {
                        if (meal.getCategory().equals(category)) {
                            System.out.println(meal);
                        }
                    }
                }
            }
            case 2 -> {
                if (noMealsInCategory) {
                    System.out.println("No meals found.");
                } else {
                    for (Meal meal : meals) {
                        if (meal.getCategory().equals(category)) {
                            System.out.println(meal.getName());
                        }
                    }
                }
            }
            default -> System.out.println("Invalid case number.");
        }
        }

    public void planWeekMeals() throws SQLException {
        if (meals.isEmpty()) {
            System.out.println("No meals found.");
            return;
        }

        if (db.tableExists("plan")) {
            db.clearTable("plan");
        }

        Collections.sort(meals);

        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        String[] categories = {"Breakfast", "Lunch", "Dinner"};

        for (String day : days) {
            System.out.println(day);
            for (String category : categories) {
                printCategory(category.toLowerCase(), 2);
                System.out.printf("Choose the %s for %s from the list above:\n", category, day);

                while (true) {
                    String answer = scanner.nextLine();
                    ResultSet rs = db.queryDBForMeal(answer);

                    if (rs.next()) {
                        String meal = rs.getString(1);
                        int mealID = rs.getInt(2);

                        db.addToPlan(meal, category, mealID, day);
                        break;
                    } else {
                        System.out.println("This meal doesn’t exist. Choose a meal from the list above.");
                    }
                }
            }
            System.out.printf("Yeah! We planned the meals for %s.\n\n", day);
        }

        mealPlannedStatus = true;
        printWeekMeals();
    }

    public void printWeekMeals() {
        // TODO: refactor
        try (ResultSet rs = db.queryDBForWeekPlan()) {

            ResultSetMetaData rsmd = rs.getMetaData();
            int columnsNumber = rsmd.getColumnCount();

            while (rs.next()) {

                for (int i = 0; i < columnsNumber - 1; i++) {
                    if (i % 3 == 0) { // only print day info after every three meals
                        System.out.println(rs.getString("day"));
                    }
                    System.out.println(rs.getString("meal_category") + ": " + rs.getString("meal_option"));
                    rs.next();
                }
                System.out.println();
                rs.previous();

            }
        } catch (SQLException e) {
            System.out.println("Error fetching week plan meals " + e.getMessage());
        }

    }

    public void saveIngredientsOfMealPlanToFile(Scanner scanner) {
        HashMap<String, Integer> ingredientsForWeekPlan = WeekPlanIngredientsToHashMap();

        System.out.println("Input a filename:");
        String fileName = scanner.nextLine();

        File file = new File(fileName);

        try (PrintWriter pW = new PrintWriter(file)) {
            for (Map.Entry<String, Integer> entry : ingredientsForWeekPlan.entrySet()) {
                if (entry.getValue() > 1) {
                    pW.write(entry.getKey() + " x" + entry.getValue() + "\n");
                } else {
                    pW.write(entry.getKey() + "\n");
                }

            }
        } catch (IOException e) {
            System.out.printf("An exception occurred %s", e.getMessage());
        }
    }

    public HashMap<String, Integer> WeekPlanIngredientsToHashMap() {
        HashMap<String, Integer> ingredientsForWeekPlan = new HashMap<>();
        try (ResultSet rs = db.queryDBForWeekPlanMealIds()) {
            while (rs.next()) {
                int meal_id = rs.getInt("meal_id");
                ResultSet rsIngredients = db.queryDBForIngredientsOfMealId(meal_id);

                while (rsIngredients.next()) {
                    String ingredient = rsIngredients.getString("ingredient");

                    ingredientsForWeekPlan.merge(ingredient, 1, Integer::sum);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error trying to fetch ingredients: " + e.getMessage());
        }
        return ingredientsForWeekPlan;
    }
}