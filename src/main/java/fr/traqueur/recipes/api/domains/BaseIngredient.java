package fr.traqueur.recipes.api.domains;

/**
 * Base class for ingredients.
 */
public abstract class BaseIngredient implements Ingredient {

    /**
     * The sign of the ingredient.
     */
    private final Character sign;

    /**
     * Constructor.
     * @param sign The sign of the ingredient.
     */
    public BaseIngredient(Character sign) {
        this.sign = sign;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Character sign() {
        return this.sign;
    }
}
