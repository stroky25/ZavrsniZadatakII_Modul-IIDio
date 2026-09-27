package org.example.model;
import jakarta.persistence.*;
@Entity @Table(name="Polaznik")
public class Polaznik {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="PolaznikID") private Integer id;
 @Column(nullable=false,length=100) private String ime;
 @Column(nullable=false,length=100) private String prezime;
 public Polaznik(){} public Polaznik(String ime,String prezime){this.ime=ime;this.prezime=prezime;}
 public Integer getId(){return id;} public String getIme(){return ime;} public String getPrezime(){return prezime;}
}