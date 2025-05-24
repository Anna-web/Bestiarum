public class CreatureAndItsWeakness {
    private int id;
    private String name;
    private String description;
    private String recipe;
    private String preparation_time;
    private String effect;
    private String source;

    public CreatureAndItsWeakness(int id, String name, String description, String recipe, String preparation_time, String effect, String source) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.recipe = recipe;
        this.preparation_time = preparation_time;
        this.effect = effect;
        this.source = source;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRecipe() {
        return this.recipe;
    }

    public void setRecipe(String recipe) {
        this.recipe = recipe;
    }

    public String getPreparation_time() {
        return this.preparation_time;
    }

    public void setPreparation_time(String preparation_time) {
        this.preparation_time = preparation_time;
    }

    public String getEffect() {
        return this.effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }

    public String getSource() {
        return this.source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String toString() {
        return "CreatureAndItsWeakness{id=" + this.id + ", name='" + this.name + "', description='" + this.description + "', recipe='" + this.recipe + "', preparation_time='" + this.preparation_time + "', effect='" + this.effect + "', source='" + this.source + "'}";
    }
}
