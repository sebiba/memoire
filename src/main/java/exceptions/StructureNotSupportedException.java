package exceptions;

public class StructureNotSupportedException extends Exception{
    public StructureNotSupportedException(){
        super();
    }

    public StructureNotSupportedException(String message){
        super(message);
    }

    public StructureNotSupportedException(Exception e){
        super(e);
    }
}
