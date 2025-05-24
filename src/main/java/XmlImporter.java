import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XmlImporter extends FileImporter {
    private static final String INGREDIENT_DELIMITER = "||INGREDIENT||";

    public XmlImporter() {
    }

    public void importFile(File file, CreatureStorages creatureMap) throws IOException {
        if (file.getName().endsWith(".xml")) {
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                FileInputStream fileInputStream = new FileInputStream(file);

                try {
                    Document doc = builder.parse(fileInputStream);
                    doc.getDocumentElement().normalize();
                    NodeList nodeList = doc.getDocumentElement().getChildNodes();

                    for(int i = 0; i < nodeList.getLength(); ++i) {
                        if (nodeList.item(i).getNodeType() == 1) {
                            Element element = (Element)nodeList.item(i);
                            CreatureAndItsWeakness creatureAndItsWeakness = this.parseCreature(element);
                            creatureMap.addCreature(Integer.toString(i), creatureAndItsWeakness);
                        }
                    }
                } catch (Throwable var12) {
                    try {
                        fileInputStream.close();
                    } catch (Throwable var11) {
                        var12.addSuppressed(var11);
                    }

                    throw var12;
                }

                fileInputStream.close();
            } catch (ParserConfigurationException | SAXException | IOException var13) {
            }
        } else if (this.next != null) {
            this.next.importFile(file, creatureMap);
        } else {
            System.out.println("Unsupported file format");
        }

    }

    private CreatureAndItsWeakness parseCreature(Element element) {
        int id = Integer.parseInt(element.getElementsByTagName("id").item(0).getTextContent());
        String name = element.getElementsByTagName("name").item(0).getTextContent();
        String description = element.getElementsByTagName("description").item(0).getTextContent();
        NodeList ingredients = element.getElementsByTagName("ingredient");
        StringBuilder recipeBuilder = new StringBuilder();

        for(int i = 0; i < ingredients.getLength(); ++i) {
            if (i > 0) {
                recipeBuilder.append("||INGREDIENT||");
            }

            recipeBuilder.append(ingredients.item(i).getTextContent().trim());
        }

        String recipe = recipeBuilder.toString();
        String preparationTime = element.getElementsByTagName("preparation_time").item(0).getTextContent();
        String effect = element.getElementsByTagName("effect").item(0).getTextContent();
        return new CreatureAndItsWeakness(id, name, description, recipe, preparationTime, effect, "XML");
    }

    private String getElementTextContent(Element element, String tagName) {
        NodeList nodeList = element.getElementsByTagName(tagName);
        return nodeList.getLength() > 0 ? nodeList.item(0).getTextContent() : "";
    }

    private Double getDoubleElementTextContent(Element element, String tagName) {
        String textContent = this.getElementTextContent(element, tagName);
        return !textContent.isEmpty() ? Double.valueOf(textContent) : null;
    }

    private Integer getIntElementTextContent(Element element, String tagName) {
        String textContent = this.getElementTextContent(element, tagName);
        return !textContent.isEmpty() ? Integer.valueOf(textContent) : null;
    }
}
