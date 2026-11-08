package br.com.devd2.models;

public class LoginResponse {

    public String accessToken;
    public String tokenType;
    public long expiresIn;
    public String nomeUsuario;
    public String emailUsuario;

    public LoginResponse(String accessToken, long expiresIn, String nomeUsuario, String emailUsuario) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
        this.expiresIn = expiresIn;
        this.nomeUsuario = nomeUsuario;
        this.emailUsuario = emailUsuario;
    }
}
