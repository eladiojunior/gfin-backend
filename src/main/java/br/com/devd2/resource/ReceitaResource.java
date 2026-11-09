package br.com.devd2.resource;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import br.com.devd2.models.*;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.devd2.helper.ResultApiHelper;
import br.com.devd2.infra.ServiceException;
import br.com.devd2.models.LiquidacaoReceitaModel;
import br.com.devd2.models.ReceitaFixaEdicaoModel;
import br.com.devd2.models.ReceitaMensalEdicaoModel;
import br.com.devd2.services.ReceitaService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/receitas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("usuario")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Receitas", description = "Operações de receitas fixas e mensais do gerenciador financeiro.")
public class ReceitaResource {

    @Inject br.com.devd2.services.EstornoService estornoService;

    private final Logger log = LoggerFactory.getLogger(ReceitaResource.class);

    @Inject
    ReceitaService receitaService;

    @Inject
    JsonWebToken jwt;

    private String idUsuarioLogado() {
        return jwt.getSubject();
    }

    // -------------------------------------------------------------------------
    // Receita Fixa
    // -------------------------------------------------------------------------

    @GET
    @Path("/fixas")
    @APIResponse(responseCode = "200", description = "Lista as receitas fixas ativas.", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.ARRAY, implementation = ReceitaFixaModel.class)))
    @Operation(summary = "Listar receitas fixas", description = "Lista as receitas fixas ativas do usuário autenticado.")
    public Response listar_receitas_fixas() {
        try {
            return Response.ok(receitaService.listarReceitasFixas(idUsuarioLogado())).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }

    @GET
    @Path("/fixas/{id}")
    @APIResponse(responseCode = "200", description = "Receita fixa recuperada com sucesso.", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ReceitaFixaModel.class)))
    @APIResponse(responseCode = "400", description = "Receita fixa não encontrada.")
    @Operation(summary = "Obter receita fixa", description = "Recupera uma receita fixa pelo seu ID.")
    public Response obter_receita_fixa(@PathParam("id") String id) {
        try {
            return Response.ok(receitaService.obterReceitaFixa(id, idUsuarioLogado())).build();
        } catch (ServiceException regra) {
            return Response.status(Status.BAD_REQUEST).entity(ResultApiHelper.toResponseRegra(regra)).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }

    @POST
    @Path("/fixas")
    @APIResponse(responseCode = "200", description = "Receita fixa registrada com sucesso.", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ReceitaFixaModel.class)))
    @APIResponse(responseCode = "400", description = "Regra de negócio não atendida.")
    @APIResponse(responseCode = "500", description = "Erro ao registrar receita fixa.")
    @Operation(summary = "Registrar receita fixa", description = "Cria uma nova receita fixa. Um histórico de valor é criado automaticamente.")
    public Response nova_receita_fixa(ReceitaFixaEdicaoModel model) {
        try {
            return Response.ok(receitaService.registrarReceitaFixa(model, idUsuarioLogado())).build();
        } catch (ServiceException regra) {
            return Response.status(Status.BAD_REQUEST).entity(ResultApiHelper.toResponseRegra(regra)).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            log.error("Erro ao registrar receita fixa.", erro);
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }

    @PUT
    @Path("/fixas/{id}")
    @APIResponse(responseCode = "200", description = "Receita fixa alterada com sucesso.", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ReceitaFixaModel.class)))
    @APIResponse(responseCode = "400", description = "Regra de negócio não atendida.")
    @APIResponse(responseCode = "500", description = "Erro ao alterar receita fixa.")
    @Operation(summary = "Alterar receita fixa", description = "Edita uma receita fixa pelo seu ID. Passe hasGravarHistorico=true para registrar histórico de valor.")
    public Response editar_receita_fixa(@PathParam("id") String id, ReceitaFixaEdicaoModel model,
            @QueryParam("hasGravarHistorico") boolean hasGravarHistorico) {
        try {
            return Response.ok(receitaService.alterarReceitaFixa(id, model, hasGravarHistorico, idUsuarioLogado())).build();
        } catch (ServiceException regra) {
            return Response.status(Status.BAD_REQUEST).entity(ResultApiHelper.toResponseRegra(regra)).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            log.error("Erro ao alterar receita fixa.", erro);
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }

    @DELETE
    @Path("/fixas/{id}")
    @APIResponse(responseCode = "200", description = "Receita fixa excluída (lógica ou física).")
    @APIResponse(responseCode = "400", description = "Regra de negócio não atendida.")
    @APIResponse(responseCode = "500", description = "Erro ao excluir receita fixa.")
    @Operation(summary = "Excluir receita fixa", description = "Remove logicamente (se houver receitas mensais vinculadas) ou fisicamente uma receita fixa.")
    public Response excluir_receita_fixa(@PathParam("id") String id) {
        try {
            receitaService.excluirReceitaFixa(id, idUsuarioLogado());
            return Response.ok().build();
        } catch (ServiceException regra) {
            return Response.status(Status.BAD_REQUEST).entity(ResultApiHelper.toResponseRegra(regra)).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            log.error("Erro ao excluir receita fixa.", erro);
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }

    // -------------------------------------------------------------------------
    // Receita Mensal
    // -------------------------------------------------------------------------

    @GET
    @Path("/mensais")
    @APIResponse(responseCode = "200", description = "Lista as receitas mensais do período.", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.ARRAY, implementation = ReceitaMensalModel.class)))
    @Operation(summary = "Listar receitas mensais", description = "Lista as receitas do mês/ano informados do usuário autenticado.")
    public Response listar_receitas_mensais(
            @QueryParam("mes") int mes,
            @QueryParam("ano") int ano) {
        try {
            return Response.ok(receitaService.listarReceitasMensais(mes, ano, idUsuarioLogado())).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }

    @GET
    @Path("/mensais/{id}")
    @APIResponse(responseCode = "200", description = "Receita mensal recuperada com sucesso.", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ReceitaMensalModel.class)))
    @APIResponse(responseCode = "400", description = "Receita mensal não encontrada.")
    @Operation(summary = "Obter receita mensal", description = "Recupera uma receita mensal pelo seu ID.")
    public Response obter_receita_mensal(@PathParam("id") String id) {
        try {
            return Response.ok(receitaService.obterReceitaMensal(id, idUsuarioLogado())).build();
        } catch (ServiceException regra) {
            return Response.status(Status.BAD_REQUEST).entity(ResultApiHelper.toResponseRegra(regra)).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }

    @POST
    @Path("/mensais")
    @APIResponse(responseCode = "200", description = "Receita mensal registrada com sucesso.", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ReceitaMensalModel.class)))
    @APIResponse(responseCode = "400", description = "Regra de negócio não atendida.")
    @APIResponse(responseCode = "500", description = "Erro ao registrar receita mensal.")
    @Operation(summary = "Registrar receita mensal", description = "Cria uma nova receita mensal.")
    public Response nova_receita_mensal(ReceitaMensalEdicaoModel model) {
        try {
            return Response.ok(receitaService.registrarReceitaMensal(model, idUsuarioLogado())).build();
        } catch (ServiceException regra) {
            return Response.status(Status.BAD_REQUEST).entity(ResultApiHelper.toResponseRegra(regra)).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            log.error("Erro ao registrar receita mensal.", erro);
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }

    @PUT
    @Path("/mensais/{id}")
    @APIResponse(responseCode = "200", description = "Receita mensal alterada com sucesso.", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ReceitaMensalModel.class)))
    @APIResponse(responseCode = "400", description = "Regra de negócio não atendida.")
    @APIResponse(responseCode = "500", description = "Erro ao alterar receita mensal.")
    @Operation(summary = "Alterar receita mensal", description = "Atualiza dados de uma receita mensal não liquidada.")
    public Response editar_receita_mensal(@PathParam("id") String id, ReceitaMensalEdicaoModel model) {
        try {
            return Response.ok(receitaService.alterarReceitaMensal(id, model, idUsuarioLogado())).build();
        } catch (ServiceException regra) {
            return Response.status(Status.BAD_REQUEST).entity(ResultApiHelper.toResponseRegra(regra)).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            log.error("Erro ao alterar receita mensal.", erro);
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }

    @POST
    @Path("/mensais/{id}/liquidar")
    @APIResponse(responseCode = "200", description = "Receita mensal liquidada com sucesso.", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ReceitaMensalModel.class)))
    @APIResponse(responseCode = "400", description = "Receita já liquidada ou dados inválidos.")
    @APIResponse(responseCode = "500", description = "Erro ao liquidar receita mensal.")
    @Operation(summary = "Liquidar receita mensal", description = "Registra a liquidação (recebimento) de uma receita mensal.")
    public Response liquidar_receita_mensal(@PathParam("id") String id, LiquidacaoReceitaModel liquidacao) {
        try {
            return Response.ok(receitaService.liquidarReceitaMensal(id, liquidacao, idUsuarioLogado())).build();
        } catch (ServiceException regra) {
            return Response.status(Status.BAD_REQUEST).entity(ResultApiHelper.toResponseRegra(regra)).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            log.error("Erro ao liquidar receita mensal.", erro);
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }

    @DELETE
    @Path("/mensais/{id}")
    @APIResponse(responseCode = "200", description = "Receita mensal excluída com sucesso.")
    @APIResponse(responseCode = "400", description = "Não foi possível excluir a receita mensal.")
    @APIResponse(responseCode = "500", description = "Erro ao excluir receita mensal.")
    @Operation(summary = "Excluir receita mensal", description = "Remove uma receita mensal não liquidada.")
    public Response excluir_receita_mensal(@PathParam("id") String id) {
        try {
            receitaService.excluirReceitaMensal(id, idUsuarioLogado());
            return Response.ok().build();
        } catch (ServiceException regra) {
            return Response.status(Status.BAD_REQUEST).entity(ResultApiHelper.toResponseRegra(regra)).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            log.error("Erro ao excluir receita mensal.", erro);
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }
    @POST
    @Path("/mensais/{id}/estornar")
    @Consumes(MediaType.WILDCARD)
    @APIResponse(responseCode = "200", description = "Liquidação estornada; lançamento pendente.", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ReceitaMensalModel.class)))
    @APIResponse(responseCode = "400", description = "Lançamento inexistente, pendente ou alterado simultaneamente.")
    @Operation(summary = "Estornar liquidação", description = "Retorna o lançamento ao estado pendente e preserva a liquidação no histórico de estornos.")
    public Response estornarMensal(@PathParam("id") String id) {
        try {
            estornoService.estornar(id, idUsuarioLogado(), false);
            return Response.ok(receitaService.obterReceitaMensal(id, idUsuarioLogado())).build();
        } catch (ServiceException regra) {
            return Response.status(Status.BAD_REQUEST).entity(ResultApiHelper.toResponseRegra(regra)).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            log.error("Erro ao estornar lançamento.", erro);
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }

    @GET
    @Path("/mensais/{id}/historico-estornos")
    @APIResponse(responseCode = "200", description = "Histórico de liquidações estornadas, em ordem de registro.", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.ARRAY, implementation = br.com.devd2.models.HistoricoEstornoModel.class)))
    @APIResponse(responseCode = "400", description = "Lançamento não encontrado.")
    @Operation(summary = "Consultar histórico de estornos", description = "Retorna os dados das liquidações estornadas do lançamento do usuário autenticado.")
    public Response historicoEstornos(@PathParam("id") String id) {
        try {
            return Response.ok(estornoService.historico(id, idUsuarioLogado(), false)).build();
        } catch (ServiceException regra) {
            return Response.status(Status.BAD_REQUEST).entity(ResultApiHelper.toResponseRegra(regra)).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            log.error("Erro ao consultar histórico de estornos.", erro);
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }
}
