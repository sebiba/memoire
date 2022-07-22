package Interfaces;

import org.jdom2.Element;

import java.util.Map;

public interface interpreter {
    String getName();
    boolean checConstruct(Element node);

    void checImport(String localDirect, Map<String, String> importer, String file);

    void getChildren();
    void getAttributs();

    void prettyPrint();

    void insert() throws Exception;

    void construct(Element node, Map<String, String> importer);
}
