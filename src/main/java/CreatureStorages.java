import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CreatureStorages {
    private Map<String, CreatureAndItsWeakness> creatureMap = new HashMap();

    public CreatureStorages() {
    }

    public void addCreature(String key, CreatureAndItsWeakness creatureAndItsWeakness) {
        this.creatureMap.put(key, creatureAndItsWeakness);
    }

    public Collection<CreatureAndItsWeakness> getCreatures() {
        return this.creatureMap.values();
    }
}
