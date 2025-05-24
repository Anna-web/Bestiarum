import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.yaml.snakeyaml.Yaml;

public class YamlImporter extends FileImporter {
    private static final String INGREDIENT_DELIMITER = "||INGREDIENT||";

    public YamlImporter() {
    }

    public void importFile(File file, CreatureStorages creatureMap) throws IOException {
        if (!file.getName().endsWith(".yaml") && !file.getName().endsWith(".yml")) {
            if (this.next != null) {
                this.next.importFile(file, creatureMap);
            } else {
                System.out.println("Unsupported file format");
            }
        } else {
            Yaml yaml = new Yaml();
            FileInputStream inputStream = new FileInputStream(file);

            try {
                Map<String, Object> yamlData = (Map)yaml.load(inputStream);
                List<Map<String, Object>> bestiary = (List)yamlData.get("bestiary");
                if (bestiary != null) {
                    Iterator var7 = bestiary.iterator();

                    while(var7.hasNext()) {
                        Map<String, Object> creatureData = (Map)var7.next();
                        CreatureAndItsWeakness creature = this.parseCreature(creatureData);
                        if (creature != null) {
                            String key = String.valueOf(creature.getId());
                            creatureMap.addCreature(key, creature);
                        }
                    }
                }
            } catch (Throwable var12) {
                try {
                    inputStream.close();
                } catch (Throwable var11) {
                    var12.addSuppressed(var11);
                }

                throw var12;
            }

            inputStream.close();
        }

    }

    private CreatureAndItsWeakness parseCreature(Map<String, Object> creatureData) {
        try {
            int id = (Integer)creatureData.get("id");
            String name = (String)creatureData.get("name");
            String description = (String)creatureData.get("description");
            Object recipeObj = creatureData.get("recipe");
            String recipe;
            if (recipeObj instanceof List<?> recipeList) {
                recipe = recipeList.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining("||INGREDIENT||"));
            } else {
                recipe = String.valueOf(recipeObj);  // Безопасное приведение
            }

            String preparationTime = (String)creatureData.get("preparation_time");
            String effect = (String)creatureData.get("effect");
            return new CreatureAndItsWeakness(id, name, description, recipe, preparationTime, effect, "YAML");
        } catch (Exception var9) {
            Exception e = var9;
            System.err.println("Error parsing creature: " + String.valueOf(creatureData));
            e.printStackTrace();
            return null;
        }
    }
}