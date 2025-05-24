import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.yaml.snakeyaml.Yaml;


public class GUI {
    private CreatureStorages creatureHolder = new CreatureStorages();
    private File currentSourceFile;

    public GUI() {
    }

    public static void main(String[] args) {
        showFrame();
    }

    public static void showFrame() {
        final GUI mainFrame = new GUI();
        JFrame frame = new JFrame();
        frame.setTitle("Creatures");
        JLabel label = new JLabel("Выберите файл с разрешением: .json / .xml / .yaml:");
        JButton chooseButton = new JButton("Выбрать файл");
        chooseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = null;

                try {
                    fileChooser = new JFileChooser((new File(GUI.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath())).getParentFile());
                } catch (URISyntaxException var8) {
                    URISyntaxException exx = var8;
                    throw new RuntimeException(exx);
                }

                FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON, XML, YAML Files", new String[]{"json", "xml", "yaml"});
                fileChooser.setFileFilter(filter);
                int returnValue = fileChooser.showOpenDialog((Component)null);
                if (returnValue == 0) {
                    File selectedFile = fileChooser.getSelectedFile();

                    try {
                        mainFrame.importFile(selectedFile);
                        mainFrame.displayCreatureTree();
                    } catch (IOException var7) {
                        IOException ex = var7;
                        ex.printStackTrace();
                    }
                }

            }
        });
        JPanel panel = new JPanel();
        panel.add(label);
        panel.add(chooseButton);
        frame.add(panel);
        frame.setSize(400, 100);
        frame.setDefaultCloseOperation(3);
        frame.setLocationRelativeTo((Component)null);
        frame.setVisible(true);
    }

    private void importFile(File file) throws IOException {
        this.currentSourceFile = file;
        JsonImporter importerChain = new JsonImporter();
        XmlImporter xmlImporter = new XmlImporter();
        YamlImporter yamlImporter = new YamlImporter();
        importerChain.setNext(xmlImporter);
        xmlImporter.setNext(yamlImporter);
        importerChain.importFile(file, this.creatureHolder);
    }

    private void displayCreatureTree() {
        JFrame treeFrame = new JFrame("Creature Tree");
        treeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        treeFrame.setPreferredSize(new Dimension(600, 400));
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Creatures");
        this.creatureHolder.getCreatures().stream()
                .sorted(Comparator.comparingInt(CreatureAndItsWeakness::getId))
                .forEach((creature) -> {
                    DefaultMutableTreeNode creatureNode = new DefaultMutableTreeNode(creature.getName());
                    DefaultMutableTreeNode idNode = new DefaultMutableTreeNode("ID: " + creature.getId());
                    creatureNode.add(idNode);
                    root.add(creatureNode);
                });

        final JTree tree = new JTree(root);
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
                if (selectedNode != null && selectedNode.getUserObject() instanceof String) {
                    String nodeText = (String)selectedNode.getUserObject();
                    GUI.this.creatureHolder.getCreatures().stream()
                            .filter((c) -> c.getName().equals(nodeText))
                            .findFirst()
                            .ifPresent((creature) -> {
                                GUI.this.showCreatureData(creature, GUI.this.currentSourceFile);
                            });
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tree);
        treeFrame.add(scrollPane, "Center");
        treeFrame.pack();
        treeFrame.setLocationRelativeTo(null);
        treeFrame.setVisible(true);
    }

    private void showCreatureData(CreatureAndItsWeakness creature, File sourceFile) {
        JFrame infoFrame = new JFrame("Creature Data: " + creature.getName());
        infoFrame.setPreferredSize(new Dimension(600, 550));
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JPanel namePanel = new JPanel(new BorderLayout(5, 5));
        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setFont(new Font("SansSerif", 1, 12));
        JTextField nameField = new JTextField(creature.getName());
        namePanel.add(nameLabel, "North");
        namePanel.add(nameField, "Center");
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, 1));
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.addCompactField(detailsPanel, "ID:", String.valueOf(creature.getId()));
        this.addCompactField(detailsPanel, "Description:", creature.getDescription());
        this.addCompactField(detailsPanel, "Recipe:", this.formatAsBulletPoints(creature.getRecipe()));
        this.addCompactField(detailsPanel, "Preparation Time:", creature.getPreparation_time());
        this.addCompactField(detailsPanel, "Effect:", creature.getEffect());
        this.addCompactField(detailsPanel, "Source:", creature.getSource());
        JButton saveButton = new JButton("Save Changes");
        saveButton.addActionListener((e) -> {
            String newName = nameField.getText().trim();
            if (newName.isEmpty()) {
                JOptionPane.showMessageDialog(infoFrame, "Name cannot be empty!", "Error", 2);
            } else if (newName.equals(creature.getName())) {
                JOptionPane.showMessageDialog(infoFrame, "No changes detected", "Info", 1);
            } else {
                try {
                    creature.setName(newName);
                    this.saveToSourceFile(creature, sourceFile);
                    infoFrame.dispose();
                    JFrame successFrame = new JFrame("Success");
                    successFrame.setSize(300, 100);
                    successFrame.setLocationRelativeTo((Component)null);
                    JLabel successLabel = new JLabel("Saved!", 0);
                    successLabel.setFont(new Font("SansSerif", 1, 12));
                    successFrame.add(successLabel);
                    successFrame.setVisible(true);
                    Timer timer = new Timer(1000, (ev) -> {
                        successFrame.dispose();
                        this.refreshCreatureTree();
                    });
                    timer.setRepeats(false);
                    timer.start();
                } catch (Exception var10) {
                    Exception ex = var10;
                    JOptionPane.showMessageDialog(infoFrame, "Save failed: " + ex.getMessage(), "Error", 0);
                }

            }
        });
        mainPanel.add(namePanel, "North");
        mainPanel.add(new JScrollPane(detailsPanel), "Center");
        mainPanel.add(saveButton, "South");
        infoFrame.add(mainPanel);
        infoFrame.pack();
        infoFrame.setLocationRelativeTo((Component)null);
        infoFrame.setVisible(true);
    }

    private void refreshCreatureTree() {
        Window[] var1 = Window.getWindows();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            Window window = var1[var3];
            if (window instanceof JFrame && "Creature Tree".equals(((JFrame)window).getTitle())) {
                window.dispose();
            }
        }

        this.displayCreatureTree();
    }

    private void saveToSourceFile(CreatureAndItsWeakness creature, File sourceFile) throws IOException {
        String filename = sourceFile.getName().toLowerCase();
        if (filename.endsWith(".json")) {
            this.saveJsonFile(creature, sourceFile);
        } else if (filename.endsWith(".xml")) {
            this.saveXmlFile(creature, sourceFile);
        } else if (filename.endsWith(".yaml") || filename.endsWith(".yml")) {
            this.saveYamlFile(creature, sourceFile);
        }

    }

    private void saveXmlFile(CreatureAndItsWeakness creature, File file) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);
            NodeList creatureNodes = doc.getElementsByTagName("creature");

            for(int i = 0; i < creatureNodes.getLength(); ++i) {
                Element creatureElement = (Element)creatureNodes.item(i);
                int id = Integer.parseInt(creatureElement.getElementsByTagName("id").item(0).getTextContent());
                if (id == creature.getId()) {
                    Node nameNode = creatureElement.getElementsByTagName("name").item(0);
                    if (nameNode != null) {
                        nameNode.setTextContent(creature.getName());
                    }
                    break;
                }
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(file);
            transformer.transform(source, result);
        } catch (TransformerException | SAXException | ParserConfigurationException var11) {
            Exception e = var11;
            throw new IOException("Failed to save XML file", e);
        }
    }

    private void saveJsonFile(CreatureAndItsWeakness creature, File file) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(file);
        JsonNode bestiary = rootNode.path("bestiary");
        if (bestiary.isArray()) {
            ArrayNode bestiaryArray = (ArrayNode)bestiary;
            Iterator var7 = bestiaryArray.iterator();

            while(var7.hasNext()) {
                JsonNode creatureNode = (JsonNode)var7.next();
                if (creatureNode.path("id").asInt() == creature.getId()) {
                    ((ObjectNode)creatureNode).put("name", creature.getName());
                    break;
                }
            }

            mapper.writeValue(file, rootNode);
        }

    }

    private void saveYamlFile(CreatureAndItsWeakness creature, File file) throws IOException {
        Yaml yaml = new Yaml();
        FileInputStream input = new FileInputStream(file);

        Map yamlData;
        try {
            yamlData = (Map)yaml.load(input);
        } catch (Throwable var12) {
            try {
                input.close();
            } catch (Throwable var9) {
                var12.addSuppressed(var9);
            }

            throw var12;
        }

        input.close();
        List bestiary = (List)yamlData.get("bestiary");
        if (bestiary != null) {
            Iterator var6 = bestiary.iterator();

            while(var6.hasNext()) {
                Map<String, Object> creatureData = (Map)var6.next();
                int id = (Integer)creatureData.get("id");
                if (id == creature.getId()) {
                    creatureData.put("name", creature.getName());
                    break;
                }
            }

            FileWriter writer = new FileWriter(file);

            try {
                yaml.dump(yamlData, writer);
            } catch (Throwable var11) {
                try {
                    writer.close();
                } catch (Throwable var10) {
                    var11.addSuppressed(var10);
                }

                throw var11;
            }

            writer.close();
        }

    }

    private void addCompactField(JPanel panel, String label, String value) {
        JPanel fieldPanel = new JPanel(new BorderLayout(3, 3));
        fieldPanel.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setFont(new Font("SansSerif", 1, 12));
        JTextArea fieldValue = new JTextArea(value);
        fieldValue.setEditable(false);
        fieldValue.setLineWrap(true);
        fieldValue.setWrapStyleWord(true);
        fieldValue.setBackground(panel.getBackground());
        fieldValue.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        fieldPanel.add(fieldLabel, "North");
        fieldPanel.add(fieldValue, "Center");
        panel.add(fieldPanel);
    }

    private String formatAsBulletPoints(String input) {
        if (input != null && !input.isEmpty()) {
            String[] ingredients = input.split(Pattern.quote("||INGREDIENT||"));
            StringBuilder formatted = new StringBuilder();
            String[] var4 = ingredients;
            int var5 = ingredients.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                String ingredient = var4[var6];
                if (!ingredient.trim().isEmpty()) {
                    formatted.append("• ").append(ingredient.trim()).append("\n");
                }
            }

            return formatted.toString().trim();
        } else {
            return "";
        }
    }
}