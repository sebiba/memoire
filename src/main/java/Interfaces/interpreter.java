package Interfaces;

import org.jdom2.Element;

import java.util.Map;

public interface interpreter {
    String getName();
    boolean checConstruct(Element node);

    void checImport(String localDirect, Map<String, String> importer, String file);

    void construct(Element node, Map<String, String> importer);
}
