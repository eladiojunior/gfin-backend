package br.com.devd2;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeIn;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;

@OpenAPIDefinition(
        info = @Info(
                title = "API do GFin (Gerenciador Financeiro) em Quarkus",
                version = "1.0.0",
                description = "API do gerenciador financeiro GFin. A maioria dos endpoints requer autenticação via Bearer Token JWT. " +
                        "Use o endpoint POST /auth/login para obter o token e inclua-o no header Authorization como: Bearer {token}",
                contact = @Contact(name = "GFin Support")
        )
)
@SecurityScheme(
        securitySchemeName = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER,
        description = "Token JWT obtido através do endpoint POST /auth/login. Informe o token no formato: Bearer {seu_token}"
)
@ApplicationPath("/api/v1")
public class GfinAplication extends Application {   
}