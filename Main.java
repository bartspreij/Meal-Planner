package mealplanner;

import mealplanner.logic.MealPlannerDB;
import mealplanner.logic.MealPlannerLogic;
import mealplanner.ui.TextUI;

import java.sql.*;
import java.util.*;

public class Main {
  public static void main(String[] args) throws SQLException {
    Scanner scanner = new Scanner(System.in);

    MealPlannerDB db = new MealPlannerDB();
    MealPlannerLogic mealPlannerLogic = new MealPlannerLogic(db, scanner);

    if (db.tableExists("plan")) {
      mealPlannerLogic.setMealsPlanned(true);
    }

    if (!db.tableExists("meals")) {
      db.createTables();
    }

    TextUI ui = new TextUI(mealPlannerLogic, scanner, db);

    ui.start();

  }
}
