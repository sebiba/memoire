package exceptions;

public class RequirementException extends Exception{
    public RequirementException(){
        super();
    }

    public RequirementException(String message){
        super(message);
    }

    public RequirementException(Exception e){
        super(e);
    }
}
