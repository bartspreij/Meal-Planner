package mealplanner.ui;

import mealplanner.logic.MealPlannerLogic;
import mealplanner.logic.MealPlannerDB;

import java.sql.SQLException;
import java.util.Scanner;

public class TextUI {
    private Scanner scanner;
    private MealPlannerLogic mealLogic;
    private MealPlannerDB DB;

    public TextUI(MealPlannerLogic mealPlannerLogic, Scanner scanner, MealPlannerDB DB) {
        this.mealLogic = mealPlannerLogic;
        this.scanner = scanner;
        this.DB = DB;
    }

    public void start() {
        readDataInTheTables();
        startMealControl();
    }

    private void startMealControl() {

        while (true) {
            System.out.println("What would you like to do (add, show, plan, save, exit)?");
            String answer = scanner.nextLine();

            switch (answer) {

                case "add":
                    mealLogic.addMeal();
                    break;

                case "show":
                    if (mealLogic.getMeals().isEmpty()) {
                        System.out.println("No meals saved. Add a meal first.");
                    } else {
                        mealLogic.printMeals(scanner);
                    }
                    break;

                case "plan":

                    try {
                        mealLogic.planWeekMeals();
                        System.out.println("Bye!");
                        break;
                    } catch (SQLException e) {
                        System.out.println("Error planning week meals menu" + e.getMessage());
                    }

                case "save":
                    if (!mealLogic.getMealsPlannedOrNot()) {
                        System.out.println("Unable to save. Plan your meals first.");
                        break;
                    }
                    mealLogic.saveIngredientsOfMealPlanToFile(scanner);
                    System.out.println("Saved!");
                    break;

                case "exit":
                    System.out.print("Bye!");
                    return;

                default:
                    System.out.println("Invalid input. Please enter 'add', 'show', or 'exit'.");
            }
        }
    }

    private void readDataInTheTables() {
        DB.getMeals(mealLogic);
    }
}