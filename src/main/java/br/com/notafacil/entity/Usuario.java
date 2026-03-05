package br.com.notafacil.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, length = 14)
    private String cnpj;

    @Column(nullable = false)
    private String role; // ADMIN, USER

    public Usuario() {}

    public Usuario(String username, String password, String nome, String cnpj, String role) {
        this.username = username;
        this.password = password;
        this.nome = nome;
        this.cnpj = cnpj;
        this.role = role;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
