package lib;
import org.jdom2.Attribute;
import org.jdom2.Element;
import java.util.List;

public class xmlParser {
    private static xmlParser instance = null;
    public static xmlParser getInstance(){
        if (instance == null){
            instance = new xmlParser();
        }
        return instance;
    }
    public List<Element> getChildOf(Element node) {
        return node.getChildren();
    }
    public Element getChildOf(Element node, int index) {
        return node.getChildren().get(index);
    }
    public List<Attribute> getParametersOf(Element node){
        return node.getAttributes();
    }
}
