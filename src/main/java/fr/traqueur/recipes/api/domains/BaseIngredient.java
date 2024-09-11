package fr.traqueur.recipes.api.domains;

public abstract class BaseIngredient implements Ingredient {

    private final Character sign;

    public BaseIngredient(Character sign) {
        this.sign = sign;
    }

    @Override
    public Character sign() {
        return this.sign;
    }
}
