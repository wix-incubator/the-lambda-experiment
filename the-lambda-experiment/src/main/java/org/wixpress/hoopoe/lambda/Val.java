package org.wixpress.hoopoe.lambda;

/**
 * @author Yoav
 * @since 10/3/11
 */
public class Val extends FuncVariable{

    private String name = "";
    private Object value;

    public Val(String name, Object value) {
        super(value.getClass());
        assert !name.equals("");
        this.name = name;
        this.value = value;

    }

    public Val(Object value) {
        super(value.getClass());
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    void setDefaultName(char c) {
        if ("".equals(name))
            name = String.valueOf(c);
    }

}
