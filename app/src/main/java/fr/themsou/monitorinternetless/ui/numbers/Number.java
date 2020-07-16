package fr.themsou.monitorinternetless.ui.numbers;

import java.util.ArrayList;

public class Number {

    private String owner;
    private String number;

    public Number(String owner, String number) {
        this.owner = owner;
        this.number = number;
    }

    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
    }
    public String getOwner() {
        return owner;
    }
    public void setOwner(String owner) {
        this.owner = owner;
    }

    public static ArrayList<Number> getNumbers(){
        ArrayList<Number> numbers = new ArrayList<>();

        numbers.add(new Number("test1", "0764985479"));
        numbers.add(new Number("test2", "+33658957568"));
        numbers.add(new Number("test3", "0658965421"));
        numbers.add(new Number("test4", "0457896356"));

        return numbers;
    }

}
