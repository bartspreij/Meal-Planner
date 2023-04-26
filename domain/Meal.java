package mealplanner.domain;

public class Meal implements Comparable<Meal> {
    private final String NAME;
    private final String[] INGREDIENTS;
    private final String CATEGORY;


    public Meal(String NAME, String[] ingredients, String category)  {
        this.NAME = NAME;
        this.INGREDIENTS = ingredients;
        this.CATEGORY = category;
    }

    public String getCategory() {
        return CATEGORY;
    }

    public String getName() {
        return NAME;
    }

    @Override
    public String toString() {
        StringBuilder printIngredients = new StringBuilder();
        for (String str : this.INGREDIENTS) {
            printIngredients.append(str).append("\n");
        }

        return "Name: " + this.NAME + "\nIngredients:\n" + printIngredients;
    }

    public int compareTo(Meal meal) { // compares the NAME of meals for sorting
        return (this.getName().compareToIgnoreCase(meal.getName()));
    }
}
