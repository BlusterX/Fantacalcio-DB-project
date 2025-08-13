package fantacalcio.model;

import java.time.LocalDate;
import java.util.Objects;

public class Utente {
    
    private int idUtente;
    private String nome;
    private String cognome;
    private String nickname;
    private String email;
    private String numero;
    private LocalDate dataDiNascita;
    private String password;
    private boolean isAdmin;
    
    // Costruttori
    public Utente() {
        this.isAdmin = false;
    }
    
    public Utente(String nome, String cognome, String nickname, String email, 
                  String numero, LocalDate dataDiNascita, String password) {
        this.nome = nome;
        this.cognome = cognome;
        this.nickname = nickname;
        this.email = email;
        this.numero = numero;
        this.dataDiNascita = dataDiNascita;
        this.password = password;
        this.isAdmin = false;
    }
    
    // Costruttore completo (con ID per lettura da database)
    public Utente(int idUtente, String nome, String cognome, String nickname, 
                  String email, String numero, LocalDate dataDiNascita, String password, boolean isAdmin) {
        this.idUtente = idUtente;
        this.nome = nome;
        this.cognome = cognome;
        this.nickname = nickname;
        this.email = email;
        this.numero = numero;
        this.dataDiNascita = dataDiNascita;
        this.password = password;
        this.isAdmin = isAdmin;
    }
    
    // Getters e Setters
    public int getIdUtente() {
        return idUtente;
    }
    
    public void setIdUtente(int idUtente) {
        this.idUtente = idUtente;
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public String getCognome() {
        return cognome;
    }
    
    public void setCognome(String cognome) {
        this.cognome = cognome;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getNumero() {
        return numero;
    }
    
    public void setNumero(String numero) {
        this.numero = numero;
    }
    
    public LocalDate getDataDiNascita() {
        return dataDiNascita;
    }
    
    public void setDataDiNascita(LocalDate dataDiNascita) {
        this.dataDiNascita = dataDiNascita;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public boolean isAdmin() {
        return isAdmin;
    }
    
    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
    
    // Metodi di utilità
    public String getNomeCompleto() {
        return nome + " " + cognome;
    }
    
    public int getEta() {
        return LocalDate.now().getYear() - dataDiNascita.getYear();
    }
    
    // equals, hashCode e toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Utente utente = (Utente) o;
        return idUtente == utente.idUtente &&
               Objects.equals(email, utente.email) &&
               Objects.equals(nickname, utente.nickname);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(idUtente, email, nickname);
    }
    
    @Override
    public String toString() {
        return String.format("Utente{id=%d, nickname='%s', nome='%s', email='%s', admin=%s}", 
                           idUtente, nickname, getNomeCompleto(), email, isAdmin);
    }
}