package org.wixpress.hoopoe.lambda;

/**
 * @author Yoav
 * @since 10/3/11
 */
public class Var<T> extends FuncVariable {

    private String name = "";

    public Var(Class<T> type, String name) {
        super(type);
        assert !name.equals("");
        this.name = name;
    }

    public Var(Class<T> type) {
        super(type);
    }

    public String getName() {
        return name;
    }

    void setDefaultName(char c) {
        if ("".equals(name))
            name = String.valueOf(c);
    }
}
