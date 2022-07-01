package Interfaces;

import org.jdom2.Element;

import java.util.Map;

public interface interpreter {
    String getName();
    boolean checConstruct(Element node);
    void getChildren();
    void getAttributs();
    void prettyPrint();
    void insert();

    void checImport(Element node, Map<String, String> importer);
}
