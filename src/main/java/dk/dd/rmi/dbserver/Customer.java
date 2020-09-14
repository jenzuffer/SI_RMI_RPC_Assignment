package dk.dd.rmi.dbserver;


import java.io.Serializable;
//attributes of http sessions need to be serializeable
public class Customer implements Serializable {
    private Integer accnum;
    private String name;
    private Double amount;

    public Customer() {
    }

    public Customer(Integer accnum, String name, double amount) {
        this.accnum = accnum;
        this.amount = amount;
        this.name = name;
    }

    public void setAccnum(Integer accnum) {
        this.accnum = accnum;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Integer get_accnum() {
        return this.accnum;
    }

    public double get_amount() {
        return this.amount;
    }

    public String get_name() {
        return this.name;
    }
}
