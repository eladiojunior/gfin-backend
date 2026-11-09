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
import br.com.devd2.models.DespesaFixaEdicaoModel;
import br.com.devd2.models.DespesaMensalEdicaoModel;
import br.com.devd2.models.LiquidacaoDespesaModel;
import br.com.devd2.services.DespesaService;
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

@Path("/despesas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("usuario")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Despesas", description = "Operações de despesas fixas e mensais do gerenciador financeiro.")
public class DespesaResource {

    @Inject br.com.devd2.services.EstornoService estornoService;

    private final Logger log = LoggerFactory.getLogger(DespesaResource.class);

    @Inject
    DespesaService despesaService;

    @Inject
    JsonWebToken jwt;

    private String idUsuarioLogado() {
        return jwt.getSubject();
    }

    // -------------------------------------------------------------------------
    // Despesa Fixa
    // -------------------------------------------------------------------------

    @GET
    @Path("/fixas")
    @APIResponse(responseCode = "200", description = "Lista as despesas fixas ativas.", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.ARRAY, implementation = DespesaFixaModel.class)))
    @APIResponse(responseCode = "500", description = "Erro ao listar as despesas fixas.")
    @Operation(summary = "Listar despesas fixas", description = "Lista as despesas fixas ativas do usuário autenticado.")
    public Response listar_despesas_fixas() {
        try {
            return Response.ok(despesaService.listarDespesasFixas(idUsuarioLogado())).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }

    @GET
    @Path("/fixas/{id}")
    @APIResponse(responseCode = "200", description = "Despesa fixa recuperada com sucesso.", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = DespesaFixaModel.class)))
    @APIResponse(responseCode = "400", description = "Despesa fixa não encontrada.")
    @APIResponse(responseCode = "500", description = "Erro ao recuperar despesa fixa.")
    @Operation(summary = "Obter despesa fixa", description = "Recupera uma despesa fixa pelo seu ID.")
    public Response obter_despesa_fixa(@PathParam("id") String id) {
        try {
            return Response.ok(despesaService.obterDespesaFixa(id, idUsuarioLogado())).build();
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
    @APIResponse(responseCode = "200", description = "Despesa fixa registrada com sucesso.", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = DespesaFixaModel.class)))
    @APIResponse(responseCode = "400", description = "Regra de negócio não atendida.")
    @APIResponse(responseCode = "500", description = "Erro ao registrar despesa fixa.")
    @Operation(summary = "Registrar despesa fixa", description = "Cria uma nova despesa fixa. Um histórico de valor é criado automaticamente.")
    public Response nova_despesa_fixa(DespesaFixaEdicaoModel despesaFixa) {
        try {
            return Response.ok(despesaService.registrarDespesaFixa(despesaFixa, idUsuarioLogado())).build();
        } catch (ServiceException regra) {
            return Response.status(Status.BAD_REQUEST).entity(ResultApiHelper.toResponseRegra(regra)).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            log.error("Erro ao registrar despesa fixa.", erro);
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }

    @PUT
    @Path("/fixas/{id}")
    @APIResponse(responseCode = "200", description = "Despesa fixa alterada com sucesso.", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = DespesaFixaModel.class)))
    @APIResponse(responseCode = "400", description = "Regra de negócio não atendida.")
    @APIResponse(responseCode = "500", description = "Erro ao alterar despesa fixa.")
    @Operation(summary = "Alterar despesa fixa", description = "Edita uma despesa fixa pelo seu ID. Passe hasGravarHistorico=true para registrar histórico de valor.")
    public Response editar_despesa_fixa(@PathParam("id") String id, DespesaFixaEdicaoModel despesaFixa,
            @QueryParam("hasGravarHistorico") boolean hasGravarHistorico) {
        try {
            return Response.ok(despesaService.alterarDespesaFixa(id, despesaFixa, hasGravarHistorico, idUsuarioLogado())).build();
        } catch (ServiceException regra) {
            return Response.status(Status.BAD_REQUEST).entity(ResultApiHelper.toResponseRegra(regra)).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            log.error("Erro ao editar despesa fixa.", erro);
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }

    @DELETE
    @Path("/fixas/{id}")
    @APIResponse(responseCode = "200", description = "Despesa fixa excluída (lógica ou física).")
    @APIResponse(responseCode = "400", description = "Regra de negócio não atendida.")
    @APIResponse(responseCode = "500", description = "Erro ao excluir despesa fixa.")
    @Operation(summary = "Excluir despesa fixa", description = "Remove logicamente (se houver despesas mensais vinculadas) ou fisicamente uma despesa fixa.")
    public Response excluir_despesa_fixa(@PathParam("id") String id) {
        try {
            despesaService.excluirDespesaFixa(id, idUsuarioLogado());
            return Response.ok().build();
        } catch (ServiceException regra) {
            return Response.status(Status.BAD_REQUEST).entity(ResultApiHelper.toResponseRegra(regra)).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            log.error("Erro ao excluir despesa fixa.", erro);
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }

    // -------------------------------------------------------------------------
    // Despesa Mensal
    // -------------------------------------------------------------------------

    @GET
    @Path("/mensais")
    @APIResponse(responseCode = "200", description = "Lista as despesas mensais do período.", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.ARRAY, implementation = DespesaMensalModel.class)))
    @APIResponse(responseCode = "500", description = "Erro ao listar despesas mensais.")
    @Operation(summary = "Listar despesas mensais", description = "Lista as despesas do mês/ano informados do usuário autenticado.")
    public Response listar_despesas_mensais(
            @QueryParam("mes") int mes,
            @QueryParam("ano") int ano) {
        try {
            return Response.ok(despesaService.listarDespesasMensais(mes, ano, idUsuarioLogado())).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }

    @GET
    @Path("/mensais/{id}")
    @APIResponse(responseCode = "200", description = "Despesa mensal recuperada com sucesso.", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = DespesaMensalModel.class)))
    @APIResponse(responseCode = "400", description = "Despesa mensal não encontrada.")
    @Operation(summary = "Obter despesa mensal", description = "Recupera uma despesa mensal pelo seu ID.")
    public Response obter_despesa_mensal(@PathParam("id") String id) {
        try {
            return Response.ok(despesaService.obterDespesaMensal(id, idUsuarioLogado())).build();
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
    @APIResponse(responseCode = "200", description = "Despesa(s) mensal(is) registrada(s) com sucesso.", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.ARRAY, implementation = DespesaMensalModel.class)))
    @APIResponse(responseCode = "400", description = "Regra de negócio não atendida.")
    @APIResponse(responseCode = "500", description = "Erro ao registrar despesa mensal.")
    @Operation(summary = "Registrar despesa mensal", description = "Cria uma despesa mensal ou parcelas mensais; valorDespesa é o total, dividido em centavos, com o restante na última parcela.")
    public Response nova_despesa_mensal(DespesaMensalEdicaoModel model) {
        try {
            return Response.ok(despesaService.registrarDespesaMensal(model, idUsuarioLogado())).build();
        } catch (ServiceException regra) {
            return Response.status(Status.BAD_REQUEST).entity(ResultApiHelper.toResponseRegra(regra)).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            log.error("Erro ao registrar despesa mensal.", erro);
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }

    @PUT
    @Path("/mensais/{id}")
    @APIResponse(responseCode = "200", description = "Despesa mensal alterada com sucesso.", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = DespesaMensalModel.class)))
    @APIResponse(responseCode = "400", description = "Regra de negócio não atendida.")
    @APIResponse(responseCode = "500", description = "Erro ao alterar despesa mensal.")
    @Operation(summary = "Alterar despesa mensal", description = "Atualiza dados de uma despesa mensal não liquidada.")
    public Response editar_despesa_mensal(@PathParam("id") String id, DespesaMensalEdicaoModel model) {
        try {
            return Response.ok(despesaService.alterarDespesaMensal(id, model, idUsuarioLogado())).build();
        } catch (ServiceException regra) {
            return Response.status(Status.BAD_REQUEST).entity(ResultApiHelper.toResponseRegra(regra)).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            log.error("Erro ao alterar despesa mensal.", erro);
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }

    @POST
    @Path("/mensais/{id}/liquidar")
    @APIResponse(responseCode = "200", description = "Despesa mensal liquidada com sucesso.", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = DespesaMensalModel.class)))
    @APIResponse(responseCode = "400", description = "Despesa já liquidada ou dados inválidos.")
    @APIResponse(responseCode = "500", description = "Erro ao liquidar despesa mensal.")
    @Operation(summary = "Liquidar despesa mensal", description = "Registra o pagamento de uma despesa mensal com desconto, multa/juros e forma de pagamento.")
    public Response liquidar_despesa_mensal(@PathParam("id") String id, LiquidacaoDespesaModel liquidacao) {
        try {
            return Response.ok(despesaService.liquidarDespesaMensal(id, liquidacao, idUsuarioLogado())).build();
        } catch (ServiceException regra) {
            return Response.status(Status.BAD_REQUEST).entity(ResultApiHelper.toResponseRegra(regra)).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            log.error("Erro ao liquidar despesa mensal.", erro);
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }

    @DELETE
    @Path("/mensais/{id}")
    @APIResponse(responseCode = "200", description = "Despesa mensal excluída com sucesso.")
    @APIResponse(responseCode = "400", description = "Não foi possível excluir a despesa mensal.")
    @APIResponse(responseCode = "500", description = "Erro ao excluir despesa mensal.")
    @Operation(summary = "Excluir despesa mensal", description = "Remove uma despesa mensal não liquidada.")
    public Response excluir_despesa_mensal(@PathParam("id") String id) {
        try {
            despesaService.excluirDespesaMensal(id, idUsuarioLogado());
            return Response.ok().build();
        } catch (ServiceException regra) {
            return Response.status(Status.BAD_REQUEST).entity(ResultApiHelper.toResponseRegra(regra)).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            log.error("Erro ao excluir despesa mensal.", erro);
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }
    @POST
    @Path("/mensais/{id}/estornar")
    @Consumes(MediaType.WILDCARD)
    @APIResponse(responseCode = "200", description = "Liquidação estornada; lançamento pendente.", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = DespesaMensalModel.class)))
    @APIResponse(responseCode = "400", description = "Lançamento inexistente, pendente ou alterado simultaneamente.")
    @Operation(summary = "Estornar liquidação", description = "Retorna o lançamento ao estado pendente e preserva a liquidação no histórico de estornos.")
    public Response estornarMensal(@PathParam("id") String id) {
        try {
            estornoService.estornar(id, idUsuarioLogado(), true);
            return Response.ok(despesaService.obterDespesaMensal(id, idUsuarioLogado())).build();
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
            return Response.ok(estornoService.historico(id, idUsuarioLogado(), true)).build();
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
