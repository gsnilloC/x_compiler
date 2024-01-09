package codegen;

public class StringOpcode extends Code {
    
    String str;
    
    public StringOpcode(Codes.ByteCodes code, String s) {
        super(code);
        str = s;
    }
    
    String getString() {
        return str;
    }
    
    public String toString() {
        return String.format("%s \"%s\"", super.toString(), str);
    }
    
    public void print() {
        System.out.println(toString());
    }
}
