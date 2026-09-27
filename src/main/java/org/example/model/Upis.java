package org.example.model;
import jakarta.persistence.*;
@Entity @Table(name="Upis")
public class Upis {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="UpisID") private Integer id;
 @ManyToOne(optional=false,fetch=FetchType.LAZY) @JoinColumn(name="IDPolaznik") private Polaznik polaznik;
 @ManyToOne(optional=false,fetch=FetchType.LAZY) @JoinColumn(name="IDProgramObrazovanja") private ProgramObrazovanja program;
 public Upis(){} public Upis(Polaznik p,ProgramObrazovanja pr){polaznik=p;program=pr;}
 public Integer getId(){return id;} public Polaznik getPolaznik(){return polaznik;} public ProgramObrazovanja getProgram(){return program;}
 public void setProgram(ProgramObrazovanja p){program=p;}
}