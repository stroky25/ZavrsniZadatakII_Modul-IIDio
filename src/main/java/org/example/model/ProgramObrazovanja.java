package org.example.model;
import jakarta.persistence.*;
@Entity @Table(name="ProgramObrazovanja")
public class ProgramObrazovanja {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="ProgramObrazovanjaID") private Integer id;
 @Column(nullable=false,length=100) private String naziv; @Column(nullable=false) private Integer csvet;
 public ProgramObrazovanja(){} public ProgramObrazovanja(String naziv,Integer csvet){this.naziv=naziv;this.csvet=csvet;}
 public Integer getId(){return id;} public String getNaziv(){return naziv;} public Integer getCsvet(){return csvet;}
}