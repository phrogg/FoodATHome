package eu.roggstar.FoodATHome;

public class Product {
    public String bar,name,image,company,expiring;
    public Integer id;

    public Product(Integer id,String bar,String name,String image,String company,String expiring){ // TODO switch to setter
        this.id = id;
        this.bar = bar;
        this.name = name;
        this.image = image;
        this.company = company;
        this.expiring = expiring;
    }
}
