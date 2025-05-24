import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class JsonImporter extends FileImporter {
    private static final String INGREDIENT_DELIMITER = "||INGREDIENT||";

    public JsonImporter() {
    }

    public void importFile(File file, CreatureStorages creatureMap) throws IOException {
        if (file.getName().endsWith(".json")) {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(file);
            System.out.println("STARTING JSON IMPORT");
            JsonNode bestiaryNode = rootNode.path("bestiary");
            if (bestiaryNode.isArray()) {
                System.out.println("Found bestiary with " + bestiaryNode.size() + " creatures");
                Iterator var6 = bestiaryNode.iterator();

                while(var6.hasNext()) {
                    JsonNode creatureNode = (JsonNode)var6.next();
                    CreatureAndItsWeakness creature = this.parseCreature(creatureNode);
                    if (creature != null) {
                        String key = String.valueOf(creature.getId());
                        System.out.println("Adding creature: " + key);
                        creatureMap.addCreature(key, creature);
                    }
                }
            } else {
                System.out.println("No valid bestiary array found in JSON");
            }
        } else if (this.next != null) {
            this.next.importFile(file, creatureMap);
        } else {
            System.out.println("Unsupported file format");
        }

    }

    private CreatureAndItsWeakness parseCreature(JsonNode creatureNode) {
        try {
            int id = creatureNode.path("id").asInt();
            String name = creatureNode.path("name").asText();
            String description = creatureNode.path("description").asText();
            JsonNode recipeNode = creatureNode.path("recipe");
            String recipe = "";
            if (recipeNode.isArray()) {
                StringBuilder recipeBuilder = new StringBuilder();

                JsonNode item;
                for(Iterator var8 = recipeNode.iterator(); var8.hasNext(); recipeBuilder.append(item.asText().trim())) {
                    item = (JsonNode)var8.next();
                    if (recipeBuilder.length() > 0) {
                        recipeBuilder.append("||INGREDIENT||");
                    }
                }

                recipe = recipeBuilder.toString();
            } else {
                recipe = recipeNode.asText();
            }

            String preparationTime = creatureNode.path("preparation_time").asText();
            String effect = creatureNode.path("effect").asText();
            System.out.println("Parsed creature: " + name + " (ID: " + id + ")");
            return new CreatureAndItsWeakness(id, name, description, recipe, preparationTime, effect, "JSON");
        } catch (Exception var10) {
            Exception e = var10;
            System.err.println("Error parsing creature: " + String.valueOf(creatureNode));
            e.printStackTrace();
            return null;
        }
    }
}
