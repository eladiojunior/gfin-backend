/*==============================================================*/
/* Database: DB_GFIN                                            */
/*==============================================================*/
create
database DB_GFIN
go

use DB_GFIN
go

/*==============================================================*/
/* Table: TB_CONTA_CORRENTE                                     */
/*==============================================================*/
create table TB_CONTA_CORRENTE
(
    ID_CONTA_CORRENTE     int identity,
    NR_BANCO              int          not null,
    TX_NOME_BANCO         varchar(50)  not null,
    NR_AGENCIA            varchar(10)  not null,
    NR_CONTA_CORRENTE     varchar(20)  not null,
    TX_NOME_TITULAR_CONTA varchar(100) not null,
    VR_LIMITE_CONTA       decimal(15, 2) null,
    IN_CONTA_ATIVA        bit null,
    DH_REGISTRO_CONTA     datetime null,
    constraint PK_TB_CONTA_CORRENTE primary key nonclustered (ID_CONTA_CORRENTE)
)
    go

declare
@CurrentUser sysname
select @CurrentUser = user_name() execute sys.sp_addextendedproperty 'MS_Description',
   'Representa um registro de Conta Corrente da pessoa que estamos controlando as finaças;',
   'user', @CurrentUser, 'table', 'TB_CONTA_CORRENTE'
go

/*==============================================================*/
/* Table: TB_CARTAO_CREDITO                                     */
/*==============================================================*/
create table TB_CARTAO_CREDITO
(
    ID_CARTAO_CREDITO               int identity,
    ID_CONTA_CORRENTE               int null,
    CD_TIPO_SITUACAO_CARTAO_CREDITO int    not null,
    NR_CARTAO_CREDITO               varchar(35) not null,
    NM_CARTAO_CREDITO               varchar(80) not null,
    DT_VALIDADE_CARTAO_CREDITO      datetime    not null,
    VL_LIMITE_CARTAO_CREDITO        decimal(15, 2) null,
    IN_FUNCAO_CREDITO_ATIVO         bit null,
    IN_CARTAO_CREDITO_PREPAGO       bit null,
    IN_FUNCAO_DEBITO_ATIVO          bit null,
    DD_VENCIMENTO_CARTAO_CREDITO    int null,
    NM_PROPRIETARIO_CARTAO_CREDITO  varchar(80) not null,
    DH_REGISTRO_CARTAO_CREDITO      datetime null,
    constraint PK_TB_CARTAO_CREDITO primary key nonclustered (ID_CARTAO_CREDITO)
)
    go

declare
@CurrentUser sysname
select @CurrentUser = user_name() execute sys.sp_addextendedproperty 'MS_Description',
   'Armazena os cartoes de credito do usuario para registro de despesa de utilizaram como forma de liquidacao o cartao de credito, mantendo assim um controle de gastos por cartao de credito.',
   'user', @CurrentUser, 'table', 'TB_CARTAO_CREDITO'
go
create index IX_CONTA_CORRENTE_CARTAO_CREDITO on TB_CARTAO_CREDITO (ID_CONTA_CORRENTE ASC)
    go

/*==============================================================*/
/* Table: TB_DESPESA_FIXA                                       */
/*==============================================================*/
create table TB_DESPESA_FIXA
(
    ID_DESPESA_FIXA                  int identity,
    CD_TIPO_SITUACAO_DESPESA_FIXA    int       not null,
    CD_FORMA_LIQUIDACAO_DESPESA_FIXA int       not null,
    ID_NATUREZA_CONTA_DESPESA_FIXA   int            not null,
    TX_DESCRICAO_DESPESA_FIXA        varchar(150)   not null,
    DD_VENCIMENTO_DESPESA_FIXA       int       not null,
    VL_DESPESA_FIXA                  decimal(15, 2) not null,
    DH_REGISTRO_DESPESA_FIXA         datetime null,
    constraint PK_TB_DESPESA_FIXA primary key nonclustered (ID_DESPESA_FIXA)
)
    go

declare
@CurrentUser sysname
select @CurrentUser = user_name() execute sys.sp_addextendedproperty 'MS_Description',
   'Armazena as despesa fixa, que acontecem mensalmente, a atraves de um processo automatico essas sao lancadas como despesas mensais;',
   'user', @CurrentUser, 'table', 'TB_DESPESA_FIXA'
go
create index IX_NATUREZA_CONTA_DESPESA_FIXA on TB_DESPESA_FIXA (ID_NATUREZA_CONTA_DESPESA_FIXA ASC)
    go

/*==============================================================*/
/* Table: TB_DESPESA_MENSAL                                     */
/*==============================================================*/
create table TB_DESPESA_MENSAL
(
    ID_DESPESA_MENSAL                 int identity,
    ID_DESPESA_FIXA                   int null,
    ID_NATUREZA_DESPESA               int            not null,
    CD_FORMA_LIQUIDACAO_DESPESA       int       not null,
    IN_DESPESA_PARCELADA              bit            not null,
    CD_VINCULO_DESPESA_PARCELADA      int null,
    TX_DESCRICAO_DESPESA              varchar(150)   not null,
    DT_VENCIMENTO_DESPESA             datetime       not null,
    VL_DESPESA                        decimal(15, 2) not null,
    CD_VINCULO_FORMA_LIQUIDACAO       int null,
    IN_DESPESA_LIQUIDADA              bit null,
    TX_OBSERVACAO_DESPESA             varchar(500) null,
    DH_LIQUIDACAO_DESPESA             datetime null,
    VL_DESCONTO_LIQUIDACAO_DESPESA    decimal(15, 2) null,
    VL_MULTA_JUROS_LIQUIDACAO_DESPESA decimal(15, 2) null,
    VL_TOTAL_LIQUIDACAO_DESPESA       decimal(15, 2) null,
    DH_REGISTRO_DESPESA               datetime       not null default getdate(),
    constraint PK_TB_DESPESA_MENSAL primary key nonclustered (ID_DESPESA_MENSAL)
)
    go

declare
@CurrentUser sysname
select @CurrentUser = user_name() execute sys.sp_addextendedproperty 'MS_Description', 'Armazena as despesa mensais do usuario.', 'user', @CurrentUser, 'table', 'TB_DESPESA_MENSAL'
go
create index IX_DESPESA_MENSAL_DESPESA_FIXA on TB_DESPESA_MENSAL (ID_DESPESA_FIXA ASC)
    go
create index IX_NATURA_CONTA_DESPESA_MENSAL on TB_DESPESA_MENSAL (ID_NATUREZA_DESPESA ASC)
    go

/*==============================================================*/
/* Table: TB_HISTORICO_DESPESA_FIXA                             */
/*==============================================================*/
create table TB_HISTORICO_DESPESA_FIXA
(
    ID_HISTORICO_DESPESA_FIXA          int identity,
    ID_DESPESA_FIXA                    int            not null,
    VL_HISTORICO_DESPESA_FIXA          decimal(15, 2) not null,
    DH_REGISTRO_HISTORICO_DESPESA_FIXA datetime null,
    constraint PK_TB_HISTORICO_DESPESA_FIXA primary key nonclustered (ID_HISTORICO_DESPESA_FIXA)
)
    go

declare
@CurrentUser sysname
select @CurrentUser = user_name() execute sys.sp_addextendedproperty 'MS_Description',
   'Armazena o historico de alteracoes na despesa fixa de forma a manter atualizado o valor da despesa fixa. Exemplo: Contas de Luz - despesa fixa mas - variavel, sendo assim o sistema ira calcular as alteracoes dos valores para montar um valor medio.',
   'user', @CurrentUser, 'table', 'TB_HISTORICO_DESPESA_FIXA'
go
create index IX_HISTORICO_DESPESA_FIXA_DESPESA_FIXA on TB_HISTORICO_DESPESA_FIXA (ID_DESPESA_FIXA ASC)
    go

/*==============================================================*/
/* Table: TB_HISTORICO_SITUACAO_PROCESSO                        */
/*==============================================================*/
create table TB_HISTORICO_SITUACAO_PROCESSO
(
    ID_HISTORICO_SITUACAO_PROCESSO int identity,
    ID_PROCESSO_AUTOMATICO         int      not null,
    CD_TIPO_SITUACAO_PROCESSO      int not null,
    DH_SITUACAO_PROCESSO           datetime not null,
    TX_HISTORICO_SITUACAO_PROCESSO varchar(500) null,
    constraint PK_TB_HISTORICO_SITUACAO_PROCE primary key nonclustered (ID_HISTORICO_SITUACAO_PROCESSO)
)
    go

declare
@CurrentUser sysname
select @CurrentUser = user_name() execute sys.sp_addextendedproperty 'MS_Description',
   'Entidade que armazena o historico das situacoes de cada processo automatico (ciclo de vida do processo), e na entidade Processo Automatico tera a ultima situacao do processo, de forma a facilitar o acesso.',
   'user', @CurrentUser, 'table', 'TB_HISTORICO_SITUACAO_PROCESSO'
go
create index IX_PROCESSO_AUTOMATICO_HISTORICO_SITUACAO_PROCESSO on TB_HISTORICO_SITUACAO_PROCESSO (ID_PROCESSO_AUTOMATICO ASC)
    go

/*==============================================================*/
/* Table: TB_NATUREZA_CONTA                                     */
/*==============================================================*/
create table TB_NATUREZA_CONTA
(
    ID_NATUREZA_CONTA           int identity,
    CD_LANCAMENTO_CONTA         int     not null,
    CD_SITUACAO_NATUREZA_CONTA  int     not null,
    TX_DESCRICAO_NATUREZA_CONTA varchar(100) not null,
    DH_REGISTRO_NATUREZA_CONTA  datetime null,
    constraint PK_TB_NATUREZA_CONTA primary key nonclustered (ID_NATUREZA_CONTA)
)
    go

declare
@CurrentUser sysname
select @CurrentUser = user_name() execute sys.sp_addextendedproperty 'MS_Description',
   'Armazena natureza da conta, podendo ser de despesa ou receita. Exemplo: Educacao(Despesa), Aplicacao(Receita);',
   'user', @CurrentUser, 'table', 'TB_NATUREZA_CONTA'
go

/*==============================================================*/
/* Table: TB_PROCESSO_AUTOMATICO                                */
/*==============================================================*/
create table TB_PROCESSO_AUTOMATICO
(
    ID_PROCESSO_AUTOMATICO          int identity,
    CD_TIPO_PROCESSO_AUTOMATICO     int    not null,
    NM_PROCESSO_AUTOMATICO          varchar(80) not null,
    CD_TIPO_SITUACAO_ATUAL_PROCESSO int    not null,
    DH_REGISTRO_PROCESSO_AUTOMATICO datetime    not null,
    constraint PK_TB_PROCESSO_AUTOMATICO primary key nonclustered (ID_PROCESSO_AUTOMATICO)
)
    go

declare
@CurrentUser sysname
select @CurrentUser = user_name() execute sys.sp_addextendedproperty 'MS_Description',
   'Entidade para armazenar os processos automaticos executados na aplicacao.',
   'user', @CurrentUser, 'table', 'TB_PROCESSO_AUTOMATICO'
execute sp_addextendedproperty 'MS_Description',
   'Codigo do Tipo de Processamento Automatico: 1 - Verificar Contas Fixas Mensal;',
   'user', @CurrentUser, 'table', 'TB_PROCESSO_AUTOMATICO', 'column', 'CD_TIPO_PROCESSO_AUTOMATICO'
execute sp_addextendedproperty 'MS_Description',
   'Codigo do tipo de situacao do processo. Mantem o ultimo codigo tipo situacao do processo para facilitar o acesso a informacao.',
   'user', @CurrentUser, 'table', 'TB_PROCESSO_AUTOMATICO', 'column', 'CD_TIPO_SITUACAO_ATUAL_PROCESSO'
go

/*==============================================================*/
/* Table: TB_RECEITA_FIXA                                       */
/*==============================================================*/
create table TB_RECEITA_FIXA
(
    ID_RECEITA_FIXA             int identity,
    ID_NATUREZA_RECEITA_FIXA    int            not null,
    CD_SITUACAO_RECEITA_FIXA    int       not null,
    TX_DESCRICAO_RECEITA_FIXA   varchar(150)   not null,
    DD_RECEBIMENTO_RECEITA_FIXA int       not null,
    VL_RECEITA_FIXA             decimal(15, 2) not null,
    DH_REGISTRO_RECEITA_FIXA    datetime null,
    constraint PK_TB_RECEITA_FIXA primary key nonclustered (ID_RECEITA_FIXA)
)
    go

declare
@CurrentUser sysname
select @CurrentUser = user_name() execute sys.sp_addextendedproperty 'MS_Description',
   'Representa um receita fixa que ocorre a todo mes, todos os registro ativos desta entidade serao registrados na receita mensal de forma automatica todo mes,',
   'user', @CurrentUser, 'table', 'TB_RECEITA_FIXA'
go
create index IX_NATUREZA_CONTA_RECEITA_FIXA on TB_RECEITA_FIXA (ID_NATUREZA_RECEITA_FIXA ASC)
    go

/*==============================================================*/
/* Table: TB_RECEITA_MENSAL                                     */
/*==============================================================*/
create table TB_RECEITA_MENSAL
(
    ID_RECEITA_MENSAL            int identity,
    ID_RECEITA_FIXA              int null,
    ID_NATUREZA_CONTA            int            not null,
    CD_FORMA_RECEBIMENTO_RECEITA int            not null,
    TX_DESCRICAO_RECEITA         varchar(150)   not null,
    DT_RECEBIMENTO_RECEITA       datetime       not null,
    VL_RECEITA                   decimal(15, 2) not null,
    IN_RECEITA_LIQUIDACAO        bit null,
    DH_LIQUIDACAO_RECEITA        datetime null,
    VL_TOTAL_LIQUIDACAO_RECEITA  decimal(15, 2) null,
    DH_REGISTRO_RECEITA          datetime null,
    constraint PK_TB_RECEITA_MENSAL primary key nonclustered (ID_RECEITA_MENSAL)
)
    go

declare
@CurrentUser sysname
select @CurrentUser = user_name() execute sys.sp_addextendedproperty 'MS_Description',
   'Armazena as receitas mensais do usuario.', 'user', @CurrentUser, 'table', 'TB_RECEITA_MENSAL'
go
create index IX_RECEITA_FIXA_RECEITA_MENSAL on TB_RECEITA_MENSAL (ID_RECEITA_FIXA ASC)
go
create index IX_NATUREZA_CONTA_RECEITA_MENSAL on TB_RECEITA_MENSAL (ID_NATUREZA_CONTA ASC)
go

/*==============================================================*/
/* Criacao das Foreign Key das tabelas                          */
/*==============================================================*/
alter table TB_CARTAO_CREDITO
    add constraint FK_CARTA_CONTA foreign key (ID_CONTA_CORRENTE)
        references TB_CONTA_CORRENTE (ID_CONTA_CORRENTE)
go
alter table TB_DESPESA_FIXA
    add constraint FK_DESPESA_FIXA_NATUREZA foreign key (ID_NATUREZA_CONTA_DESPESA_FIXA)
        references TB_NATUREZA_CONTA (ID_NATUREZA_CONTA)
go
alter table TB_DESPESA_MENSAL
    add constraint FK_DESPESA_MENSAL_FIXA foreign key (ID_DESPESA_FIXA)
        references TB_DESPESA_FIXA (ID_DESPESA_FIXA)
    go
alter table TB_DESPESA_MENSAL
    add constraint FK_DESPESA_MENSAL_NATUREZA foreign key (ID_NATUREZA_DESPESA)
        references TB_NATUREZA_CONTA (ID_NATUREZA_CONTA)
    go
alter table TB_HISTORICO_DESPESA_FIXA
    add constraint FK_HISTORICO_DESPESA_FIXA foreign key (ID_DESPESA_FIXA)
        references TB_DESPESA_FIXA (ID_DESPESA_FIXA)
    go
alter table TB_HISTORICO_SITUACAO_PROCESSO
    add constraint FK_HISTORICO_PROCESSO_PROCESSO foreign key (ID_PROCESSO_AUTOMATICO)
        references TB_PROCESSO_AUTOMATICO (ID_PROCESSO_AUTOMATICO)
    go
alter table TB_RECEITA_FIXA
    add constraint FK_RECEITA_FIXA_NATUREZA foreign key (ID_NATUREZA_RECEITA_FIXA)
        references TB_NATUREZA_CONTA (ID_NATUREZA_CONTA)
    go
alter table TB_RECEITA_MENSAL
    add constraint FK_RECEITA_MENSAL_NATUREZA foreign key (ID_NATUREZA_CONTA)
        references TB_NATUREZA_CONTA (ID_NATUREZA_CONTA)
    go

alter table TB_RECEITA_MENSAL
    add constraint FK_RECEITA_MENSAL_FIXA foreign key (ID_RECEITA_FIXA)
        references TB_RECEITA_FIXA (ID_RECEITA_FIXA)
    go
