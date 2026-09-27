package org.example;

import org.example.model.Polaznik;
import org.example.model.ProgramObrazovanja;
import org.example.model.Upis;
import org.example.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=== JAVA ADV - EVIDENCIJA POLAZNIKA ===");

        try {
            boolean radi = true;
            while (radi) {
                ispisiIzbornik();
                String izbor = scanner.nextLine().trim();

                try {
                    switch (izbor) {
                        case "1" -> unesiPolaznika();
                        case "2" -> unesiProgram();
                        case "3" -> upisiPolaznika();
                        case "4" -> prebaciPolaznika();
                        case "5" -> prikaziPolaznikePrograma();
                        case "0" -> radi = false;
                        default -> System.out.println("Nepoznata opcija.");
                    }
                } catch (Exception e) {
                    System.out.println("Greška: " + e.getMessage());
                }
            }
        } finally {
            HibernateUtil.shutdown();
            scanner.close();
        }

        System.out.println("Program završen.");
    }

    private static void ispisiIzbornik() {
        System.out.println();
        System.out.println("1. Unesi novog polaznika");
        System.out.println("2. Unesi novi program obrazovanja");
        System.out.println("3. Upiši polaznika na program obrazovanja");
        System.out.println("4. Prebaci polaznika iz jednog u drugi program obrazovanja");
        System.out.println("5. Prikaži polaznike određenog programa");
        System.out.println("0. Izlaz");
        System.out.print("Odabir: ");
    }

    private static void unesiPolaznika() {
        System.out.print("Ime: ");
        String ime = scanner.nextLine().trim();
        System.out.print("Prezime: ");
        String prezime = scanner.nextLine().trim();

        if (ime.isBlank() || prezime.isBlank()) {
            System.out.println("Ime i prezime su obavezni.");
            return;
        }

        Transaction transaction = null;
        try (Session session = HibernateUtil.getFactory().openSession()) {
            transaction = session.beginTransaction();
            Polaznik polaznik = new Polaznik(ime, prezime);
            session.persist(polaznik);
            transaction.commit();
            System.out.println("Polaznik je spremljen. ID: " + polaznik.getId());
        } catch (Exception e) {
            rollback(transaction);
            throw e;
        }
    }

    private static void unesiProgram() {
        System.out.print("Naziv programa obrazovanja: ");
        String naziv = scanner.nextLine().trim();
        Integer csvet = ucitajInteger("CSVET bodovi: ");

        if (naziv.isBlank()) {
            System.out.println("Naziv programa je obavezan.");
            return;
        }
        if (csvet == null || csvet < 0) {
            System.out.println("CSVET mora biti cijeli broj veći ili jednak nuli.");
            return;
        }

        Transaction transaction = null;
        try (Session session = HibernateUtil.getFactory().openSession()) {
            transaction = session.beginTransaction();
            ProgramObrazovanja program = new ProgramObrazovanja(naziv, csvet);
            session.persist(program);
            transaction.commit();
            System.out.println("Program je spremljen. ID: " + program.getId());
        } catch (Exception e) {
            rollback(transaction);
            throw e;
        }
    }

    private static void upisiPolaznika() {
        Integer polaznikId = ucitajInteger("ID polaznika: ");
        Integer programId = ucitajInteger("ID programa obrazovanja: ");
        if (polaznikId == null || programId == null) return;

        Transaction transaction = null;
        try (Session session = HibernateUtil.getFactory().openSession()) {
            Polaznik polaznik = session.find(Polaznik.class, polaznikId);
            ProgramObrazovanja program = session.find(ProgramObrazovanja.class, programId);

            if (polaznik == null) {
                System.out.println("Polaznik ne postoji.");
                return;
            }
            if (program == null) {
                System.out.println("Program obrazovanja ne postoji.");
                return;
            }

            Long brojUpisa = session.createQuery(
                    "select count(u) from Upis u where u.polaznik.id = :polaznikId and u.program.id = :programId",
                    Long.class)
                    .setParameter("polaznikId", polaznikId)
                    .setParameter("programId", programId)
                    .getSingleResult();

            if (brojUpisa > 0) {
                System.out.println("Polaznik je već upisan na odabrani program.");
                return;
            }

            transaction = session.beginTransaction();
            session.persist(new Upis(polaznik, program));
            transaction.commit();
            System.out.println("Polaznik je uspješno upisan na program.");
        } catch (Exception e) {
            rollback(transaction);
            throw e;
        }
    }

    private static void prebaciPolaznika() {
        Integer polaznikId = ucitajInteger("ID polaznika: ");
        Integer stariProgramId = ucitajInteger("ID trenutnog programa: ");
        Integer noviProgramId = ucitajInteger("ID novog programa: ");
        if (polaznikId == null || stariProgramId == null || noviProgramId == null) return;

        Transaction transaction = null;
        try (Session session = HibernateUtil.getFactory().openSession()) {
            transaction = session.beginTransaction();

            Upis upis = session.createQuery(
                    "from Upis u where u.polaznik.id = :polaznikId and u.program.id = :programId",
                    Upis.class)
                    .setParameter("polaznikId", polaznikId)
                    .setParameter("programId", stariProgramId)
                    .setMaxResults(1)
                    .uniqueResult();

            ProgramObrazovanja noviProgram = session.find(ProgramObrazovanja.class, noviProgramId);

            if (upis == null) {
                throw new IllegalArgumentException("Postojeći upis polaznika na stari program nije pronađen.");
            }
            if (noviProgram == null) {
                throw new IllegalArgumentException("Novi program obrazovanja ne postoji.");
            }
            if (stariProgramId.equals(noviProgramId)) {
                throw new IllegalArgumentException("Stari i novi program ne mogu biti isti.");
            }

            Long vecUpisan = session.createQuery(
                    "select count(u) from Upis u where u.polaznik.id = :polaznikId and u.program.id = :programId",
                    Long.class)
                    .setParameter("polaznikId", polaznikId)
                    .setParameter("programId", noviProgramId)
                    .getSingleResult();

            if (vecUpisan > 0) {
                throw new IllegalArgumentException("Polaznik je već upisan na novi program.");
            }

            // Promjena upisa unutar jedne Hibernate transakcije.
            upis.setProgram(noviProgram);
            session.merge(upis);

            transaction.commit();
            System.out.println("Polaznik je uspješno prebačen na novi program.");
        } catch (Exception e) {
            rollback(transaction);
            throw e;
        }
    }

    private static void prikaziPolaznikePrograma() {
        Integer programId = ucitajInteger("ID programa obrazovanja: ");
        if (programId == null) return;

        try (Session session = HibernateUtil.getFactory().openSession()) {
            ProgramObrazovanja program = session.find(ProgramObrazovanja.class, programId);
            if (program == null) {
                System.out.println("Program obrazovanja ne postoji.");
                return;
            }

            List<Upis> upisi = session.createQuery(
                    "from Upis u join fetch u.polaznik where u.program.id = :programId order by u.polaznik.prezime, u.polaznik.ime",
                    Upis.class)
                    .setParameter("programId", programId)
                    .getResultList();

            System.out.println();
            System.out.println("Program: " + program.getNaziv());
            System.out.println("CSVET: " + program.getCsvet());

            if (upisi.isEmpty()) {
                System.out.println("Na programu nema upisanih polaznika.");
                return;
            }

            System.out.println("--------------------------------------------");
            for (Upis upis : upisi) {
                System.out.printf("%s %s | %s | %d CSVET%n",
                        upis.getPolaznik().getIme(),
                        upis.getPolaznik().getPrezime(),
                        program.getNaziv(),
                        program.getCsvet());
            }
        }
    }

    private static Integer ucitajInteger(String poruka) {
        System.out.print(poruka);
        String unos = scanner.nextLine().trim();
        try {
            return Integer.parseInt(unos);
        } catch (NumberFormatException e) {
            System.out.println("Unesite cijeli broj.");
            return null;
        }
    }

    private static void rollback(Transaction transaction) {
        if (transaction != null && transaction.isActive()) {
            transaction.rollback();
        }
    }
}
