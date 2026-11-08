/*==============================================================*/
/* Database name:  DB_GFIN                                      */
/* DBMS name:      Microsoft SQL Server 2008                    */
/* Created on:     25/12/2016 16:22:39                          */
/*==============================================================*/


drop database DB_GFIN
go

/*==============================================================*/
/* Database: DB_GFIN                                            */
/*==============================================================*/
create database DB_GFIN
go

use DB_GFIN
go

/*==============================================================*/
/* Table: TB_AGENDA_CORRENTISTA                                 */
/*==============================================================*/
create table TB_AGENDA_CORRENTISTA (
   ID_AGENDA_CORRENTISTA int                  identity,
   ID_ENTIDADE_CONTROLE int                  null,
   NM_BANCO             varchar(80)          not null,
   NR_AGENCIA           varchar(20)          not null,
   NR_CONTA_CORRENTE    varchar(30)          not null,
   NM_CORRENTISTA       varchar(80)          not null,
   TX_OBSERVACAO_CORRENTISTA varchar(250)         null,
   DH_REGISTRO_CORRENTISTA datetime             null,
   constraint PK_TB_AGENDA_CORRENTISTA primary key nonclustered (ID_AGENDA_CORRENTISTA)
)
go

declare @CurrentUser sysname
select @CurrentUser = user_name()
execute sys.sp_addextendedproperty 'MS_Description', 
   'Armazena as informa��es de correntista que realizamos transa��es banc�rias.',
   'user', @CurrentUser, 'table', 'TB_AGENDA_CORRENTISTA'
go

/*==============================================================*/
/* Index: RL_ENTIDADE_CONTROLE_AGENDA_CORRENTISTA_FK            */
/*==============================================================*/
create index RL_ENTIDADE_CONTROLE_AGENDA_CORRENTISTA_FK on TB_AGENDA_CORRENTISTA (
ID_ENTIDADE_CONTROLE ASC
)
go

/*==============================================================*/
/* Table: TB_CARTAO_CREDITO                                     */
/*==============================================================*/
create table TB_CARTAO_CREDITO (
   ID_CARTAO_CREDITO    int                  identity,
   ID_CONTA_CORRENTE    int                  null,
   CD_TIPO_SITUACAO_CARTAO_CREDITO smallint             not null,
   ID_ENTIDADE_CONTROLE int                  not null,
   NR_CARTAO_CREDITO    varchar(35)          not null,
   NM_CARTAO_CREDITO    varchar(80)          not null,
   DT_VALIDADE_CARTAO_CREDITO datetime             not null,
   VL_LIMITE_CARTAO_CREDITO decimal(15,2)        null,
   IN_FUNCAO_CREDITO_ATIVO bit                  null,
   IN_CARTAO_CREDITO_PREPAGO bit                  null,
   IN_FUNCAO_DEBITO_ATIVO bit                  null,
   DD_VENCIMENTO_CARTAO_CREDITO smallint             null,
   NM_PROPRIETARIO_CARTAO_CREDITO varchar(80)          not null,
   DH_REGISTRO_CARTAO_CREDITO datetime             null,
   constraint PK_TB_CARTAO_CREDITO primary key nonclustered (ID_CARTAO_CREDITO)
)
go

declare @CurrentUser sysname
select @CurrentUser = user_name()
execute sys.sp_addextendedproperty 'MS_Description', 
   'Armazena os cart�es de cr�dito do usu�rio para registro de despesa de utilizaram como forma de liquida��o o cart�o de cr�dito, mantendo assim um controle de gastos por cart�o de cr�dito.',
   'user', @CurrentUser, 'table', 'TB_CARTAO_CREDITO'
go

/*==============================================================*/
/* Index: RL_CONTA_CORRENTE_CARTAO_CREDITO_FK                   */
/*==============================================================*/
create index RL_CONTA_CORRENTE_CARTAO_CREDITO_FK on TB_CARTAO_CREDITO (
ID_CONTA_CORRENTE ASC
)
go

/*==============================================================*/
/* Index: RL_ENTIDADE_CONTROLE_CARTAO_CREDITO_FK                */
/*==============================================================*/
create index RL_ENTIDADE_CONTROLE_CARTAO_CREDITO_FK on TB_CARTAO_CREDITO (
ID_ENTIDADE_CONTROLE ASC
)
go

/*==============================================================*/
/* Table: TB_CHEQUE_CONTA                                       */
/*==============================================================*/
create table TB_CHEQUE_CONTA (
   ID_CHEQUE_CONTA      int                  identity,
   ID_CONTA_CORRENTE    int                  not null,
   CD_TIPO_SITUACAO_CHEQUE smallint             not null,
   NR_CHEQUE_CONTA      int                  not null,
   DH_REGISTRO_CHEQUE_CONTA datetime             null,
   constraint PK_TB_CHEQUE_CONTA primary key nonclustered (ID_CHEQUE_CONTA)
)
go

declare @CurrentUser sysname
select @CurrentUser = user_name()
execute sys.sp_addextendedproperty 'MS_Description', 
   'Armazenas os registros de cheque de uma conta corrente.',
   'user', @CurrentUser, 'table', 'TB_CHEQUE_CONTA'
go

/*==============================================================*/
/* Index: RL_CONTA_CORRENTE_CHEQUE_FK                           */
/*==============================================================*/
create index RL_CONTA_CORRENTE_CHEQUE_FK on TB_CHEQUE_CONTA (
ID_CONTA_CORRENTE ASC
)
go

/*==============================================================*/
/* Table: TB_CHEQUE_CONTA_CANCELADO                             */
/*==============================================================*/
create table TB_CHEQUE_CONTA_CANCELADO (
   ID_CHEQUE_CANCELADO  int                  identity,
   ID_CHEQUE_CONTA      int                  not null,
   DT_CANCELAMENTO_CHEQUE datetime             not null,
   IN_CANCELAMENTO_BANCO bit                  not null,
   TX_OBSERVACAO_CANCELAMENTO_CHEQUE varchar(500)         null,
   constraint PK_TB_CHEQUE_CONTA_CANCELADO primary key nonclustered (ID_CHEQUE_CANCELADO)
)
go

declare @CurrentUser sysname
select @CurrentUser = user_name()
execute sys.sp_addextendedproperty 'MS_Description', 
   'Armazena cheque registrados e cancelados.',
   'user', @CurrentUser, 'table', 'TB_CHEQUE_CONTA_CANCELADO'
go

/*==============================================================*/
/* Index: RL_CHEQUE_CONTA_CHEQUE_CANCELADO_FK                   */
/*==============================================================*/
create index RL_CHEQUE_CONTA_CHEQUE_CANCELADO_FK on TB_CHEQUE_CONTA_CANCELADO (
ID_CHEQUE_CONTA ASC
)
go

/*==============================================================*/
/* Table: TB_CHEQUE_CONTA_COMPENSADO                            */
/*==============================================================*/
create table TB_CHEQUE_CONTA_COMPENSADO (
   ID_CHEQUE_CONTA_COMPENSADO int                  identity,
   ID_CHEQUE_CONTA      int                  not null,
   DT_COMPENSACAO_CHEQUE_CONTA datetime             not null,
   TX_OBSERVACAO_CHEQUE_COMPENSADO varchar(500)         null,
   constraint PK_TB_CHEQUE_CONTA_COMPENSADO primary key nonclustered (ID_CHEQUE_CONTA_COMPENSADO)
)
go

declare @CurrentUser sysname
select @CurrentUser = user_name()
execute sys.sp_addextendedproperty 'MS_Description', 
   'Armazena cheque compensado ap�s sua emiss�o.',
   'user', @CurrentUser, 'table', 'TB_CHEQUE_CONTA_COMPENSADO'
go

/*==============================================================*/
/* Index: RL_CHEQUE_CONTA_CHEQUE_COMPENSADO_FK                  */
/*==============================================================*/
create index RL_CHEQUE_CONTA_CHEQUE_COMPENSADO_FK on TB_CHEQUE_CONTA_COMPENSADO (
ID_CHEQUE_CONTA ASC
)
go

/*==============================================================*/
/* Table: TB_CHEQUE_CONTA_DEVOLVIDO                             */
/*==============================================================*/
create table TB_CHEQUE_CONTA_DEVOLVIDO (
   ID_CHEQUE_DEVOLVIDO  int                  identity,
   ID_CHEQUE_CONTA      int                  not null,
   DT_DEVOLUCAO_CHEQUE  datetime             not null,
   TX_OBSERVACAO_DEVOLUCAO_CHEQUE varchar(500)         not null,
   constraint PK_TB_CHEQUE_CONTA_DEVOLVIDO primary key nonclustered (ID_CHEQUE_DEVOLVIDO)
)
go

declare @CurrentUser sysname
select @CurrentUser = user_name()
execute sys.sp_addextendedproperty 'MS_Description', 
   'Armazena cheque devolvido na conta corrente, por ter sido cancelado ou por falta de saldo no momento da compensa��o.',
   'user', @CurrentUser, 'table', 'TB_CHEQUE_CONTA_DEVOLVIDO'
go

/*==============================================================*/
/* Index: RL_CHEQUE_CONTA_CHEQUE_DEVOLVIDO_FK                   */
/*==============================================================*/
create index RL_CHEQUE_CONTA_CHEQUE_DEVOLVIDO_FK on TB_CHEQUE_CONTA_DEVOLVIDO (
ID_CHEQUE_CONTA ASC
)
go

/*==============================================================*/
/* Table: TB_CHEQUE_CONTA_EMITIDO                               */
/*==============================================================*/
create table TB_CHEQUE_CONTA_EMITIDO (
   ID_CHEQUE_EMITIDO    int                  identity,
   ID_CHEQUE_CONTA      int                  not null,
   DT_EMISSAO_CHEQUE_CONTA datetime             not null,
   DT_VENCIMENTO_CHEQUE_CONTA datetime             not null,
   TX_HISTORICO_EMISSAO_CHEQUE_CONTA varchar(150)         not null,
   VL_CHEQUE_EMITIDO    decimal(15,2)        not null,
   constraint PK_TB_CHEQUE_CONTA_EMITIDO primary key nonclustered (ID_CHEQUE_EMITIDO)
)
go

declare @CurrentUser sysname
select @CurrentUser = user_name()
execute sys.sp_addextendedproperty 'MS_Description', 
   'Armazena o cheque emitido de uma conta corrente.',
   'user', @CurrentUser, 'table', 'TB_CHEQUE_CONTA_EMITIDO'
go

/*==============================================================*/
/* Index: RL_CHEQUE_CONTA_CHEQUE_EMITIDO_FK                     */
/*==============================================================*/
create index RL_CHEQUE_CONTA_CHEQUE_EMITIDO_FK on TB_CHEQUE_CONTA_EMITIDO (
ID_CHEQUE_CONTA ASC
)
go

/*==============================================================*/
/* Table: TB_CHEQUE_CONTA_RESGATADO                             */
/*==============================================================*/
create table TB_CHEQUE_CONTA_RESGATADO (
   ID_CHEQUE_RESGATADO  int                  identity,
   ID_CHEQUE_CONTA      int                  not null,
   DT_RESGATE_CHEQUE    datetime             not null,
   IN_BAIXA_CHEQUE_CCF  bit                  not null,
   DT_BAIXA_CHEQUE_CCF  datetime             null,
   VL_BAIXA_CHEQUE_CCF  decimal(15,2)        not null,
   VL_RESGATE_CHEQUE    decimal(15,2)        not null,
   TX_OBSERVACAO_RESGATE_CHEQUE varchar(500)         null,
   constraint PK_TB_CHEQUE_CONTA_RESGATADO primary key nonclustered (ID_CHEQUE_RESGATADO)
)
go

declare @CurrentUser sysname
select @CurrentUser = user_name()
execute sys.sp_addextendedproperty 'MS_Description', 
   'Armazena cheque resgatado ap�s emiss�o e devolu��o do mesmo.',
   'user', @CurrentUser, 'table', 'TB_CHEQUE_CONTA_RESGATADO'
go

/*==============================================================*/
/* Index: RL_CHEQUE_CONTA_CHEQU_RESGATADO_FK                    */
/*==============================================================*/
create index RL_CHEQUE_CONTA_CHEQU_RESGATADO_FK on TB_CHEQUE_CONTA_RESGATADO (
ID_CHEQUE_CONTA ASC
)
go

/*==============================================================*/
/* Table: TB_CONTA_CORRENTE                                     */
/*==============================================================*/
create table TB_CONTA_CORRENTE (
   ID_CONTA_CORRENTE    int                  identity,
   ID_ENTIDADE_CONTROLE int                  not null,
   NR_BANCO             int                  not null,
   TX_NOME_BANCO        varchar(50)          not null,
   NR_AGENCIA           varchar(10)          not null,
   NR_CONTA_CORRENTE    varchar(20)          not null,
   TX_NOME_TITULAR_CONTA varchar(100)         not null,
   VR_LIMITE_CONTA      decimal(15,2)        null,
   IN_CONTA_ATIVA       bit                  null,
   DH_REGISTRO_CONTA    datetime             null,
   constraint PK_TB_CONTA_CORRENTE primary key nonclustered (ID_CONTA_CORRENTE)
)
go

declare @CurrentUser sysname
select @CurrentUser = user_name()
execute sys.sp_addextendedproperty 'MS_Description', 
   'Representa um registro de Conta Corrente da empresa que estamos controlando as fina�as;',
   'user', @CurrentUser, 'table', 'TB_CONTA_CORRENTE'
go

/*==============================================================*/
/* Index: RL_ENTIDADE_CONTROLE_CONTA_CORRENTE_FK                */
/*==============================================================*/
create index RL_ENTIDADE_CONTROLE_CONTA_CORRENTE_FK on TB_CONTA_CORRENTE (
ID_ENTIDADE_CONTROLE ASC
)
go

/*==============================================================*/
/* Table: TB_DESPESA_FIXA                                       */
/*==============================================================*/
create table TB_DESPESA_FIXA (
   ID_DESPESA_FIXA      int                  identity,
   CD_TIPO_SITUACAO_DESPESA_FIXA smallint             not null,
   CD_FORMA_LIQUIDACAO_DESPESA_FIXA smallint             not null,
   ID_NATUREZA_CONTA_DESPESA_FIXA int                  not null,
   ID_ENTIDADE_CONTROLE int                  not null,
   TX_DESCRICAO_DESPESA_FIXA varchar(150)         not null,
   DD_VENCIMENTO_DESPESA_FIXA smallint             not null,
   VL_DESPESA_FIXA      decimal(15,2)        not null,
   DH_REGISTRO_DESPESA_FIXA datetime             null,
   constraint PK_TB_DESPESA_FIXA primary key nonclustered (ID_DESPESA_FIXA)
)
go

declare @CurrentUser sysname
select @CurrentUser = user_name()
execute sys.sp_addextendedproperty 'MS_Description', 
   'Armazena as despesa fixa, que acontecem mensalmente, a atrav�s de um processo autom�tico essas s�o lan�adas como despesas mensais;',
   'user', @CurrentUser, 'table', 'TB_DESPESA_FIXA'
go

/*==============================================================*/
/* Index: RL_NATUREZA_CONTA_DESPESA_FIXA_FK                     */
/*==============================================================*/
create index RL_NATUREZA_CONTA_DESPESA_FIXA_FK on TB_DESPESA_FIXA (
ID_NATUREZA_CONTA_DESPESA_FIXA ASC
)
go

/*==============================================================*/
/* Index: RL_ENTIDADE_DESPESA_FIXA_FK                           */
/*==============================================================*/
create index RL_ENTIDADE_DESPESA_FIXA_FK on TB_DESPESA_FIXA (
ID_ENTIDADE_CONTROLE ASC
)
go

/*==============================================================*/
/* Table: TB_DESPESA_MENSAL                                     */
/*==============================================================*/
create table TB_DESPESA_MENSAL (
   ID_DESPESA_MENSAL    int                  identity,
   ID_DESPESA_FIXA      int                  null,
   ID_NATUREZA_DESPESA  int                  not null,
   CD_FORMA_LIQUIDACAO_DESPESA smallint             not null,
   ID_ENTIDADE_CONTROLE int                  not null,
   IN_DESPESA_PARCELADA bit                  not null,
   CD_VINCULO_DESPESA_PARCELADA int                  null,
   TX_DESCRICAO_DESPESA varchar(150)         not null,
   DT_VENCIMENTO_DESPESA datetime             not null,
   VL_DESPESA           decimal(15,2)        not null,
   CD_VINCULO_FORMA_LIQUIDACAO int                  null,
   IN_DESPESA_LIQUIDADA bit                  null,
   TX_OBSERVACAO_DESPESA varchar(500)         null,
   DH_LIQUIDACAO_DESPESA datetime             null,
   VL_DESCONTO_LIQUIDACAO_DESPESA decimal(15,2)        null,
   VL_MULTA_JUROS_LIQUIDACAO_DESPESA decimal(15,2)        null,
   VL_TOTAL_LIQUIDACAO_DESPESA decimal(15,2)        null,
   DH_REGISTRO_DESPESA  datetime             not null default getdate(),
   constraint PK_TB_DESPESA_MENSAL primary key nonclustered (ID_DESPESA_MENSAL)
)
go

declare @CurrentUser sysname
select @CurrentUser = user_name()
execute sys.sp_addextendedproperty 'MS_Description', 
   'Armazena as despesa mensais do usu�rio.',
   'user', @CurrentUser, 'table', 'TB_DESPESA_MENSAL'
go

declare @CurrentUser sysname
select @CurrentUser = user_name()
execute sp_addextendedproperty 'MS_Description', 
   'C�digo gerado pelo sistema quanto uma despesa � parcelada, esse c�digo mantem um v�nculo f�sico entre elas, de forma a possibilitar a identifica��o do parcelamento.',
   'user', @CurrentUser, 'table', 'TB_DESPESA_MENSAL', 'column', 'CD_VINCULO_DESPESA_PARCELADA'
go

declare @CurrentUser sysname
select @CurrentUser = user_name()
execute sp_addextendedproperty 'MS_Description', 
   'C�digo que relaciona a forma de liquisa��o com o registro de origem.
Ex.: Para a forma de liquida��o seja "Cheque" aqui ter� o Id do Cheque utilizado,
caso seja "Cart�o de Cr�dito" aqui ter� o Id do cart�o utilizado.
Esse atributo pode ser NULL, caso o usu�rio n�o queira vincular nada;',
   'user', @CurrentUser, 'table', 'TB_DESPESA_MENSAL', 'column', 'CD_VINCULO_FORMA_LIQUIDACAO'
go

declare @CurrentUser sysname
select @CurrentUser = user_name()
execute sp_addextendedproperty 'MS_Description', 
   'Data e hora do registro da despesa mensal.',
   'user', @CurrentUser, 'table', 'TB_DESPESA_MENSAL', 'column', 'DH_REGISTRO_DESPESA'
go

/*==============================================================*/
/* Index: RL_DESPESA_MENSAL_DESPESA_FIXA_FK                     */
/*==============================================================*/
create index RL_DESPESA_MENSAL_DESPESA_FIXA_FK on TB_DESPESA_MENSAL (
ID_DESPESA_FIXA ASC
)
go

/*==============================================================*/
/* Index: RL_NATURA_CONTA_DESPESA_MENSAL_FK                     */
/*==============================================================*/
create index RL_NATURA_CONTA_DESPESA_MENSAL_FK on TB_DESPESA_MENSAL (
ID_NATUREZA_DESPESA ASC
)
go

/*==============================================================*/
/* Index: RL_ENTIDADE_DESPESA_MENSAL_FK                         */
/*==============================================================*/
create index RL_ENTIDADE_DESPESA_MENSAL_FK on TB_DESPESA_MENSAL (
ID_ENTIDADE_CONTROLE ASC
)
go

/*==============================================================*/
/* Table: TB_ENTIDADE_CONTROLE_FINANCEIRO                       */
/*==============================================================*/
create table TB_ENTIDADE_CONTROLE_FINANCEIRO (
   ID_ENTIDADE_CONTROLE int                  identity,
   CD_TIPO_SITUACAO_ENTIDADE smallint             not null,
   CD_TIPO_ENTIDADE_CONTROLE smallint             not null,
   NM_ENTIDADE_CONTROLE varchar(100)         not null,
   NR_CPF_CNPJ_ENTIDADE_CONTROLE varchar(20)          null,
   DH_REGISTRO_ENTIDADE_CONTROLE datetime             not null,
   constraint PK_TB_ENTIDADE_CONTROLE_FINANC primary key nonclustered (ID_ENTIDADE_CONTROLE)
)
go

declare @CurrentUser sysname
select @CurrentUser = user_name()
execute sys.sp_addextendedproperty 'MS_Description', 
   'Armazena a entidade que estamos realizando o controle financeiro.
Exemplo: Minha Casa ou uma Empresa.',
   'user', @CurrentUser, 'table', 'TB_ENTIDADE_CONTROLE_FINANCEIRO'
go

/*==============================================================*/
/* Table: TB_EXTRATO_BANCARIO_CONTA                             */
/*==============================================================*/
create table TB_EXTRATO_BANCARIO_CONTA (
   ID_EXTRATO_BANCARIO  int                  identity,
   ID_CONTA_CORRENTE    int                  not null,
   DH_IMPORTACAO_EXTRATO datetime             not null,
   TX_LOCAL_ARQUIVO_IMPORTACAO_EXTRATO char(150)            not null,
   IN_BAIXA_AUTOMATICA_DESPESA bit                  not null,
   IN_BAIXA_AUTOMATICA_RECEITA bit                  not null,
   IN_BAIXA_AUTOMATICA_CHEQUE bit                  not null,
   TX_LOG_IMPORTACAO_EXTRATO varchar(500)         null,
   constraint PK_TB_EXTRATO_BANCARIO_CONTA primary key nonclustered (ID_EXTRATO_BANCARIO)
)
go

declare @CurrentUser sysname
select @CurrentUser = user_name()
execute sys.sp_addextendedproperty 'MS_Description', 
   'Armazena extrato banc�rio de conta corrente importados para o sistema.',
   'user', @CurrentUser, 'table', 'TB_EXTRATO_BANCARIO_CONTA'
go

/*==============================================================*/
/* Index: RL_CONTA_CORRENTE_EXTRATO_BANCARIO_FK                 */
/*==============================================================*/
create index RL_CONTA_CORRENTE_EXTRATO_BANCARIO_FK on TB_EXTRATO_BANCARIO_CONTA (
ID_CONTA_CORRENTE ASC
)
go

/*==============================================================*/
/* Table: TB_HISTORICO_DESPESA_FIXA                             */
/*==============================================================*/
create table TB_HISTORICO_DESPESA_FIXA (
   ID_HISTORICO_DESPESA_FIXA int                  identity,
   ID_DESPESA_FIXA      int                  not null,
   VL_HISTORICO_DESPESA_FIXA decimal(15,2)        not null,
   DH_REGISTRO_HISTORICO_DESPESA_FIXA datetime             null,
   constraint PK_TB_HISTORICO_DESPESA_FIXA primary key nonclustered (ID_HISTORICO_DESPESA_FIXA)
)
go

declare @CurrentUser sysname
select @CurrentUser = user_name()
execute sys.sp_addextendedproperty 'MS_Description', 
   'Armazena o hist�rico de altera��es na despesa fixa de forma a manter atualizado o valor da despesa fixa.
Exemplo: Contas de Luz � despesa fixa mas � vari�vel, sendo assim o sistema ir� calcular as altera��es dos valores para montar um valor m�dio.',
   'user', @CurrentUser, 'table', 'TB_HISTORICO_DESPESA_FIXA'
go

/*==============================================================*/
/* Index: RL_HISTORICO_DESPESA_FIXA_DESPESA_FIXA_FK             */
/*==============================================================*/
create index RL_HISTORICO_DESPESA_FIXA_DESPESA_FIXA_FK on TB_HISTORICO_DESPESA_FIXA (
ID_DESPESA_FIXA ASC
)
go

/*==============================================================*/
/* Table: TB_HISTORICO_SITUACAO_PROCESSO                        */
/*==============================================================*/
create table TB_HISTORICO_SITUACAO_PROCESSO (
   ID_HISTORICO_SITUACAO_PROCESSO int                  identity,
   ID_PROCESSO_AUTOMATICO int                  not null,
   CD_TIPO_SITUACAO_PROCESSO smallint             not null,
   DH_SITUACAO_PROCESSO datetime             not null,
   TX_HISTORICO_SITUACAO_PROCESSO varchar(500)         null,
   constraint PK_TB_HISTORICO_SITUACAO_PROCE primary key nonclustered (ID_HISTORICO_SITUACAO_PROCESSO)
)
go

declare @CurrentUser sysname
select @CurrentUser = user_name()
execute sys.sp_addextendedproperty 'MS_Description', 
   'Entidade que armazena o hist�rico das situa��es de cada processo autom�tico (ciclo de vida do processo), e na entidade Processo Automatico ter� a �ltima situa��o do processo, de forma a facilitar o acesso.',
   'user', @CurrentUser, 'table', 'TB_HISTORICO_SITUACAO_PROCESSO'
go

declare @CurrentUser sysname
select @CurrentUser = user_name()
execute sp_addextendedproperty 'MS_Description', 
   'C�digo do tipo de situa��o do processo.',
   'user', @CurrentUser, 'table', 'TB_HISTORICO_SITUACAO_PROCESSO', 'column', 'CD_TIPO_SITUACAO_PROCESSO'
go

/*==============================================================*/
/* Index: RL_PROCESSO_AUTOMATICO_HISTORICO_SITUACAO_PROCESSO_FK */
/*==============================================================*/
create index RL_PROCESSO_AUTOMATICO_HISTORICO_SITUACAO_PROCESSO_FK on TB_HISTORICO_SITUACAO_PROCESSO (
ID_PROCESSO_AUTOMATICO ASC
)
go

/*==============================================================*/
/* Table: TB_HISTORICO_USUARIO                                  */
/*==============================================================*/
create table TB_HISTORICO_USUARIO (
   ID_HISTORICO_USUARIO int                  identity,
   CD_TIPO_OPERACAO     smallint             not null,
   ID_USUARIO           int                  not null,
   CD_IDENTIFICACAO_MAQUINA_USUARIO varchar(30)          not null,
   IN_DISPOSITIVO_MOBILE_USUARIO bit                  not null,
   TX_DISPOSITIVO_ACESSO_USUARIO varchar(250)         null,
   TX_HISTORICO_USUARIO varchar(500)         null,
   DH_REGISTRO_HISTORICO_USUARIO datetime             not null,
   constraint PK_TB_HISTORICO_USUARIO primary key nonclustered (ID_HISTORICO_USUARIO)
)
go

declare @CurrentUser sysname
select @CurrentUser = user_name()
execute sys.sp_addextendedproperty 'MS_Description', 
   'Armazena o hist�rico de atividades do usu�rio, conforme o tipo de opera��o realizada pelo usu�rio.
Ser�o mantidas informa��es de querm realizou, ip da m�quina, etc.',
   'user', @CurrentUser, 'table', 'TB_HISTORICO_USUARIO'
go

declare @CurrentUser sysname
select @CurrentUser = user_name()
execute sp_addextendedproperty 'MS_Description', 
   'Identificador �nico do registro de hist�rico do usu�rio.',
   'user', @CurrentUser, 'table', 'TB_HISTORICO_USUARIO', 'column', 'ID_HISTORICO_USUARIO'
go

declare @CurrentUser sysname
select @CurrentUser = user_name()
execute sp_addextendedproperty 'MS_Description', 
   'C�digo dos tipos de opera��es realizadas pelo usu�rio.',
   'user', @CurrentUser, 'table', 'TB_HISTORICO_USUARIO', 'column', 'CD_TIPO_OPERACAO'
go

declare @CurrentUser sysname
select @CurrentUser = user_name()
execute sp_addextendedproperty 'MS_Description', 
   'Identificador �nico do usu�rio.',
   'user', @CurrentUser, 'table', 'TB_HISTORICO_USUARIO', 'column', 'ID_USUARIO'
go

declare @CurrentUser sysname
select @CurrentUser = user_name()
execute sp_addextendedproperty 'MS_Description', 
   'C�digo que identifique a m�quina do usu�rio(ip, mac ou nome da m�quina) que realizou alguma opera��o para armazenar no hist�rico.',
   'user', @CurrentUser, 'table', 'TB_HISTORICO_USUARIO', 'column', 'CD_IDENTIFICACAO_MAQUINA_USUARIO'
go

declare @CurrentUser sysname
select @CurrentUser = user_name()
execute sp_addextendedproperty 'MS_Description', 
   'Indicador de dispositivo mobile, caso o usu�rio esteja acessando de um dispositivo mobile (celular, table ou afins) ser� registrado como Verdadeiro, caso contr�rio Falso;',
   'user', @CurrentUser, 'table', 'TB_HISTORICO_USUARIO', 'column', 'IN_DISPOSITIVO_MOBILE_USUARIO'
go

declare @CurrentUser sysname
select @CurrentUser = user_name()
execute sp_addextendedproperty 'MS_Description', 
   'Armazena informa��es sobre o dispositivo de acesso do usu�rio quanto o hist�rico foi registrado.
Exemplo: Identificado que o usu�rio est� acessando de um navegador em um desktop, ser� recuperado: navegador, vers�o e sistema operacional; Quando identificado que � de um dispositivo m�vel: nome do disponitivo, navegador, vers�o e sistema operacional utilizado.',
   'user', @CurrentUser, 'table', 'TB_HISTORICO_USUARIO', 'column', 'TX_DISPOSITIVO_ACESSO_USUARIO'
go

declare @CurrentUser sysname
select @CurrentUser = user_name()
execute sp_addextendedproperty 'MS_Description', 
   'Descri��o de informa��es complementares da opera��o realizada. Por exemplo: se uma usu�rio realizar a altera��o de sua senha, a senha antiga pode ser armazenada nesse campo para uma consulta futura.',
   'user', @CurrentUser, 'table', 'TB_HISTORICO_USUARIO', 'column', 'TX_HISTORICO_USUARIO'
go

declare @CurrentUser sysname
select @CurrentUser = user_name()
execute sp_addextendedproperty 'MS_Description', 
   'Data e hora em que uma opera��o foi realizada e registrada no hist�rico.',
   'user', @CurrentUser, 'table', 'TB_HISTORICO_USUARIO', 'column', 'DH_REGISTRO_HISTORICO_USUARIO'
go

/*==============================================================*/
/* Index: RL_USUARIO_SISTEMA_HISTORICO_USUARIO_FK               */
/*==============================================================*/
create index RL_USUARIO_SISTEMA_HISTORICO_USUARIO_FK on TB_HISTORICO_USUARIO (
ID_USUARIO ASC
)
go

/*==============================================================*/
/* Table: TB_LANCAMENTO_EXTRATO_BANCARIO                        */
/*==============================================================*/
create table TB_LANCAMENTO_EXTRATO_BANCARIO (
   ID_LANCAMENTO_EXTRATO int                  identity,
   ID_CONTA_CORRENTE    int                  not null,
   CD_LANCAMENTO_CONTA  smallint             not null,
   DH_LANCAMENTO_EXTRATO datetime             not null,
   TX_HISTORICO_LANCAMENTO_EXTRATO varchar(100)         not null,
   NR_LANCAMENTO_EXTRATO varchar(30)          not null,
   VL_LANCAMENTO_EXTRATO decimal(15,2)        not null,
   constraint PK_TB_LANCAMENTO_EXTRATO_BANCA primary key nonclustered (ID_LANCAMENTO_EXTRATO)
)
go

declare @CurrentUser sysname
select @CurrentUser = user_name()
execute sys.sp_addextendedproperty 'MS_Description', 
   'Armazena registro de lan�amento do extrato banc�rio de uma conta corrente.',
   'user', @CurrentUser, 'table', 'TB_LANCAMENTO_EXTRATO_BANCARIO'
go

/*==============================================================*/
/* Index: RL_CONTA_CORRENTE_LANCAMENTO_EXTRATO_BANCARIO_FK      */
/*==============================================================*/
create index RL_CONTA_CORRENTE_LANCAMENTO_EXTRATO_BANCARIO_FK on TB_LANCAMENTO_EXTRATO_BANCARIO (
ID_CONTA_CORRENTE ASC
)
go

/*==============================================================*/
/* Table: TB_NATUREZA_CONTA                                     */
/*==============================================================*/
create table TB_NATUREZA_CONTA (
   ID_NATUREZA_CONTA    int                  identity,
   CD_LANCAMENTO_CONTA  smallint             not null,
   CD_SITUACAO_NATUREZA_CONTA smallint             not null,
   ID_ENTIDADE_CONTROLE int                  not null,
   TX_DESCRICAO_NATUREZA_CONTA varchar(100)         not null,
   DH_REGISTRO_NATUREZA_CONTA datetime             null,
   constraint PK_TB_NATUREZA_CONTA primary key nonclustered (ID_NATUREZA_CONTA)
)
go

declare @CurrentUser sysname
select @CurrentUser = user_name()
execute sys.sp_addextendedproperty 'MS_Description', 
   'Armazena natureza da conta, podendo ser de despesa ou receita.
Exemplo: Educa��o(Despesa), Aplica��o(Receita);',
   'user', @CurrentUser, 'table', 'TB_NATUREZA_CONTA'
go

/*==============================================================*/
/* Index: RL_ENTIDADE_NATUREZA_CONTA_FK                         */
/*==============================================================*/
create index RL_ENTIDADE_NATUREZA_CONTA_FK on TB_NATUREZA_CONTA (
ID_ENTIDADE_CONTROLE ASC
)
go

/*==============================================================*/
/* Table: TB_PROCESSO_AUTOMATICO                                */
/*==============================================================*/
create table TB_PROCESSO_AUTOMATICO (
   ID_PROCESSO_AUTOMATICO int                  identity,
   ID_ENTIDADE_CONTROLE int                  not null,
   CD_TIPO_PROCESSO_AUTOMATICO smallint             not null,
   NM_PROCESSO_AUTOMATICO varchar(80)          not null,
   CD_TIPO_SITUACAO_ATUAL_PROCESSO smallint             not null,
   DH_REGISTRO_PROCESSO_AUTOMATICO datetime             not null,
   constraint PK_TB_PROCESSO_AUTOMATICO primary key nonclustered (ID_PROCESSO_AUTOMATICO)
)
go

declare @CurrentUser sysname
select @CurrentUser = user_name()
execute sys.sp_addextendedproperty 'MS_Description', 
   'Entidade para armazenar os processo autom�tico executados na aplica��o.',
   'user', @CurrentUser, 'table', 'TB_PROCESSO_AUTOMATICO'
go

declare @CurrentUser sysname
select @CurrentUser = user_name()
execute sp_addextendedproperty 'MS_Description', 
   'C�digo do Tipo de Processamento Autom�tico:
1 - Verificar Contas Fixas Mensal;
2 - Verificar Importa��o de Arquivo de Conta Corrente;',
   'user', @CurrentUser, 'table', 'TB_PROCESSO_AUTOMATICO', 'column', 'CD_TIPO_PROCESSO_AUTOMATICO'
go

declare @CurrentUser sysname
select @CurrentUser = user_name()
execute sp_addextendedproperty 'MS_Description', 
   'C�digo do tipo de situa��o do processo.
Mantem o �ltimo c�digo tipo situa��o do processo para facilitar o acesso a informa��o.',
   'user', @CurrentUser, 'table', 'TB_PROCESSO_AUTOMATICO', 'column', 'CD_TIPO_SITUACAO_ATUAL_PROCESSO'
go

/*==============================================================*/
/* Index: RL_ENTIDADE_CONTROLE_PROCESSO_AUTOMATICO_FK           */
/*==============================================================*/
create index RL_ENTIDADE_CONTROLE_PROCESSO_AUTOMATICO_FK on TB_PROCESSO_AUTOMATICO (
ID_ENTIDADE_CONTROLE ASC
)
go

/*==============================================================*/
/* Table: TB_RECEITA_FIXA                                       */
/*==============================================================*/
create table TB_RECEITA_FIXA (
   ID_RECEITA_FIXA      int                  identity,
   ID_NATUREZA_RECEITA_FIXA int                  not null,
   CD_SITUACAO_RECEITA_FIXA smallint             not null,
   ID_ENTIDADE_CONTROLE int                  not null,
   TX_DESCRICAO_RECEITA_FIXA varchar(150)         not null,
   DD_RECEBIMENTO_RECEITA_FIXA smallint             not null,
   VL_RECEITA_FIXA      decimal(15,2)        not null,
   DH_REGISTRO_RECEITA_FIXA datetime             null,
   constraint PK_TB_RECEITA_FIXA primary key nonclustered (ID_RECEITA_FIXA)
)
go

declare @CurrentUser sysname
select @CurrentUser = user_name()
execute sys.sp_addextendedproperty 'MS_Description', 
   'Representa um receita fixa que ocorre a todo m�s, todos os registro ativos desta entidade ser�o registrados na receita mensal de forma autom�tica todo m�s,',
   'user', @CurrentUser, 'table', 'TB_RECEITA_FIXA'
go

/*==============================================================*/
/* Index: RL_NATUREZA_CONTA_RECEITA_FIXA_FK                     */
/*==============================================================*/
create index RL_NATUREZA_CONTA_RECEITA_FIXA_FK on TB_RECEITA_FIXA (
ID_NATUREZA_RECEITA_FIXA ASC
)
go

/*==============================================================*/
/* Index: RL_ENTIDADE_RECEITA_FIXA_FK                           */
/*==============================================================*/
create index RL_ENTIDADE_RECEITA_FIXA_FK on TB_RECEITA_FIXA (
ID_ENTIDADE_CONTROLE ASC
)
go

/*==============================================================*/
/* Table: TB_RECEITA_MENSAL                                     */
/*==============================================================*/
create table TB_RECEITA_MENSAL (
   ID_RECEITA_MENSAL    int                  identity,
   ID_RECEITA_FIXA      int                  null,
   ID_NATUREZA_CONTA    int                  not null,
   CD_FORMA_RECEBIMENTO_RECEITA smallint             not null,
   ID_ENTIDADE_CONTROLE int                  not null,
   TX_DESCRICAO_RECEITA varchar(150)         not null,
   DT_RECEBIMENTO_RECEITA datetime             not null,
   VL_RECEITA           decimal(15,2)        not null,
   IN_RECEITA_LIQUIDACAO bit                  null,
   DH_LIQUIDACAO_RECEITA datetime             null,
   VL_TOTAL_LIQUIDACAO_RECEITA decimal(15,2)        null,
   DH_REGISTRO_RECEITA  datetime             null,
   constraint PK_TB_RECEITA_MENSAL primary key nonclustered (ID_RECEITA_MENSAL)
)
go

declare @CurrentUser sysname
select @CurrentUser = user_name()
execute sys.sp_addextendedproperty 'MS_Description', 
   'Armazena as receitas mensais do usu�rio.',
   'user', @CurrentUser, 'table', 'TB_RECEITA_MENSAL'
go

/*==============================================================*/
/* Index: RL_RECEITA_FIXA_RECEITA_MENSAL_FK                     */
/*==============================================================*/
create index RL_RECEITA_FIXA_RECEITA_MENSAL_FK on TB_RECEITA_MENSAL (
ID_RECEITA_FIXA ASC
)
go

/*==============================================================*/
/* Index: RL_NATUREZA_CONTA_RECEITA_MENSAL_FK                   */
/*==============================================================*/
create index RL_NATUREZA_CONTA_RECEITA_MENSAL_FK on TB_RECEITA_MENSAL (
ID_NATUREZA_CONTA ASC
)
go

/*==============================================================*/
/* Index: RL_ENTIDADE_RECEITA_MENSAL_FK                         */
/*==============================================================*/
create index RL_ENTIDADE_RECEITA_MENSAL_FK on TB_RECEITA_MENSAL (
ID_ENTIDADE_CONTROLE ASC
)
go

/*==============================================================*/
/* Table: TB_USUARIO_ACESSO_ENTIDADE_CONTROLE                   */
/*==============================================================*/
create table TB_USUARIO_ACESSO_ENTIDADE_CONTROLE (
   ID_USUARIO_ACESSO_ENTIDADE int                  identity,
   ID_ENTIDADE_CONTROLE int                  not null,
   ID_USUARIO           int                  not null,
   ID_USUARIO_RESPONSACEL_ACESSO int                  not null,
   CD_TIPO_PERFIL_ACESSO_USUARIO smallint             not null,
   DT_INICIAL_VIGENCIA_ACESSO_USUARIO datetime             not null,
   DT_FINAL_VIGENCIA_ACESSO_USUARIO datetime             null,
   DH_REGISTRO_USUARIO_ACESSO datetime             not null,
   constraint PK_TB_USUARIO_ACESSO_ENTIDADE_ primary key nonclustered (ID_USUARIO_ACESSO_ENTIDADE)
)
go

declare @CurrentUser sysname
select @CurrentUser = user_name()
execute sys.sp_addextendedproperty 'MS_Description', 
   'Armazena usu�rios com acesso as entidade de controle financeiro, com uma vig�ncia e perfil espec�fico: respons�veis e convidados.',
   'user', @CurrentUser, 'table', 'TB_USUARIO_ACESSO_ENTIDADE_CONTROLE'
go

declare @CurrentUser sysname
select @CurrentUser = user_name()
execute sp_addextendedproperty 'MS_Description', 
   'Identificador �nico do usu�rio.',
   'user', @CurrentUser, 'table', 'TB_USUARIO_ACESSO_ENTIDADE_CONTROLE', 'column', 'ID_USUARIO'
go

declare @CurrentUser sysname
select @CurrentUser = user_name()
execute sp_addextendedproperty 'MS_Description', 
   'Identificador �nico do usu�rio.',
   'user', @CurrentUser, 'table', 'TB_USUARIO_ACESSO_ENTIDADE_CONTROLE', 'column', 'ID_USUARIO_RESPONSACEL_ACESSO'
go

/*==============================================================*/
/* Index: RL_ENTIDADE_CONTROLE_USUARIO_FK                       */
/*==============================================================*/
create index RL_ENTIDADE_CONTROLE_USUARIO_FK on TB_USUARIO_ACESSO_ENTIDADE_CONTROLE (
ID_ENTIDADE_CONTROLE ASC
)
go

/*==============================================================*/
/* Index: RL_USUARIO_ENTIDADE_CONTROLE_FK                       */
/*==============================================================*/
create index RL_USUARIO_ENTIDADE_CONTROLE_FK on TB_USUARIO_ACESSO_ENTIDADE_CONTROLE (
ID_USUARIO ASC
)
go

/*==============================================================*/
/* Index: RL_USUARIO_RESPONSAVEL_REGISTRO_ACESSO_USUARIO_FK     */
/*==============================================================*/
create index RL_USUARIO_RESPONSAVEL_REGISTRO_ACESSO_USUARIO_FK on TB_USUARIO_ACESSO_ENTIDADE_CONTROLE (
ID_USUARIO_RESPONSACEL_ACESSO ASC
)
go

/*==============================================================*/
/* Table: TB_USUARIO_SISTEMA                                    */
/*==============================================================*/
create table TB_USUARIO_SISTEMA (
   ID_USUARIO           int                  identity,
   CD_TIPO_SITUACAO_USUARIO smallint             not null,
   NM_USUARIO           varchar(100)         not null,
   BY_FOTO_PERFIL_USUARIO image                null,
   TX_EMAIL_USUARIO     varchar(80)          not null,
   TX_SENHA_USUARIO     varchar(100)         not null,
   TX_SALT_SENHA_USUARIO varchar(100)         not null,
   IN_CONFIRMACAO_EMAIL_USUARIO bit                  not null,
   IN_ALTERACAO_SENHA_USUARIO bit                  not null,
   DH_ULTIMO_ACESSO_USUARIO datetime             null,
   DH_REGISTRO_USUARIO  datetime             not null,
   constraint PK_TB_USUARIO_SISTEMA primary key nonclustered (ID_USUARIO)
)
go

declare @CurrentUser sysname
select @CurrentUser = user_name()
execute sys.sp_addextendedproperty 'MS_Description', 
   'Armazena usu�rio do sistema ligado a entidade de controle financeiro.',
   'user', @CurrentUser, 'table', 'TB_USUARIO_SISTEMA'
go

declare @CurrentUser sysname
select @CurrentUser = user_name()
execute sp_addextendedproperty 'MS_Description', 
   'Identificador �nico do usu�rio.',
   'user', @CurrentUser, 'table', 'TB_USUARIO_SISTEMA', 'column', 'ID_USUARIO'
go

alter table TB_AGENDA_CORRENTISTA
   add constraint FK_TB_AGEND_RL_ENTIDA_TB_ENTID foreign key (ID_ENTIDADE_CONTROLE)
      references TB_ENTIDADE_CONTROLE_FINANCEIRO (ID_ENTIDADE_CONTROLE)
go

alter table TB_CARTAO_CREDITO
   add constraint FK_TB_CARTA_RL_CONTA__TB_CONTA foreign key (ID_CONTA_CORRENTE)
      references TB_CONTA_CORRENTE (ID_CONTA_CORRENTE)
go

alter table TB_CARTAO_CREDITO
   add constraint FK_TB_CARTA_RL_ENTIDA_TB_ENTID foreign key (ID_ENTIDADE_CONTROLE)
      references TB_ENTIDADE_CONTROLE_FINANCEIRO (ID_ENTIDADE_CONTROLE)
go

alter table TB_CHEQUE_CONTA
   add constraint FK_TB_CHEQU_RL_CONTA__TB_CONTA foreign key (ID_CONTA_CORRENTE)
      references TB_CONTA_CORRENTE (ID_CONTA_CORRENTE)
go

alter table TB_CHEQUE_CONTA_CANCELADO
   add constraint FK_CHEQUE_CONTA_CHEQUE_CANCELADO foreign key (ID_CHEQUE_CONTA)
      references TB_CHEQUE_CONTA (ID_CHEQUE_CONTA)
go

alter table TB_CHEQUE_CONTA_COMPENSADO
   add constraint FK_CHEQUE_CONTA_CHEQUE_COMPENSADO foreign key (ID_CHEQUE_CONTA)
      references TB_CHEQUE_CONTA (ID_CHEQUE_CONTA)
go

alter table TB_CHEQUE_CONTA_DEVOLVIDO
   add constraint FK_CHEQUE_CONTA_CHEQUE_DEVOLVIDO foreign key (ID_CHEQUE_CONTA)
      references TB_CHEQUE_CONTA (ID_CHEQUE_CONTA)
go

alter table TB_CHEQUE_CONTA_EMITIDO
   add constraint FK_CHEQUE_CONTA_CHEQUE_EMITIDO foreign key (ID_CHEQUE_CONTA)
      references TB_CHEQUE_CONTA (ID_CHEQUE_CONTA)
go

alter table TB_CHEQUE_CONTA_RESGATADO
   add constraint FK_CHEQUE_CONTA_CHEQUE_RESGATADO foreign key (ID_CHEQUE_CONTA)
      references TB_CHEQUE_CONTA (ID_CHEQUE_CONTA)
go

alter table TB_CONTA_CORRENTE
   add constraint FK_TB_CONTA_RL_ENTIDA_TB_ENTID foreign key (ID_ENTIDADE_CONTROLE)
      references TB_ENTIDADE_CONTROLE_FINANCEIRO (ID_ENTIDADE_CONTROLE)
go

alter table TB_DESPESA_FIXA
   add constraint FK_ENTIDADE_DESPESA_FIXA foreign key (ID_ENTIDADE_CONTROLE)
      references TB_ENTIDADE_CONTROLE_FINANCEIRO (ID_ENTIDADE_CONTROLE)
go

alter table TB_DESPESA_FIXA
   add constraint FK_TB_DESPE_RL_NATURE_TB_NATUR foreign key (ID_NATUREZA_CONTA_DESPESA_FIXA)
      references TB_NATUREZA_CONTA (ID_NATUREZA_CONTA)
go

alter table TB_DESPESA_MENSAL
   add constraint FK_TB_DESPE_RL_DESPES_TB_DESPE foreign key (ID_DESPESA_FIXA)
      references TB_DESPESA_FIXA (ID_DESPESA_FIXA)
go

alter table TB_DESPESA_MENSAL
   add constraint FK_ENTIDADE_DESPESA_MENSAL foreign key (ID_ENTIDADE_CONTROLE)
      references TB_ENTIDADE_CONTROLE_FINANCEIRO (ID_ENTIDADE_CONTROLE)
go

alter table TB_DESPESA_MENSAL
   add constraint FK_TB_DESPE_RL_NATURA_TB_NATUR foreign key (ID_NATUREZA_DESPESA)
      references TB_NATUREZA_CONTA (ID_NATUREZA_CONTA)
go

alter table TB_EXTRATO_BANCARIO_CONTA
   add constraint FK_TB_EXTRA_RL_CONTA__TB_CONTA foreign key (ID_CONTA_CORRENTE)
      references TB_CONTA_CORRENTE (ID_CONTA_CORRENTE)
go

alter table TB_HISTORICO_DESPESA_FIXA
   add constraint FK_TB_HISTO_RL_HISTOR_TB_DESPE foreign key (ID_DESPESA_FIXA)
      references TB_DESPESA_FIXA (ID_DESPESA_FIXA)
go

alter table TB_HISTORICO_SITUACAO_PROCESSO
   add constraint FK_PROCESSO_HISTORICO_SITUACAO foreign key (ID_PROCESSO_AUTOMATICO)
      references TB_PROCESSO_AUTOMATICO (ID_PROCESSO_AUTOMATICO)
go

alter table TB_HISTORICO_USUARIO
   add constraint FK_TB_HISTO_RL_USUARI_TB_USUAR foreign key (ID_USUARIO)
      references TB_USUARIO_SISTEMA (ID_USUARIO)
go

alter table TB_LANCAMENTO_EXTRATO_BANCARIO
   add constraint FK_TB_LANCA_RL_CONTA__TB_CONTA foreign key (ID_CONTA_CORRENTE)
      references TB_CONTA_CORRENTE (ID_CONTA_CORRENTE)
go

alter table TB_NATUREZA_CONTA
   add constraint FK_TB_NATUR_RL_ENTIDA_TB_ENTID foreign key (ID_ENTIDADE_CONTROLE)
      references TB_ENTIDADE_CONTROLE_FINANCEIRO (ID_ENTIDADE_CONTROLE)
go

alter table TB_PROCESSO_AUTOMATICO
   add constraint FK_ENTIDADE_CONTROLE_PROCESSO foreign key (ID_ENTIDADE_CONTROLE)
      references TB_ENTIDADE_CONTROLE_FINANCEIRO (ID_ENTIDADE_CONTROLE)
go

alter table TB_RECEITA_FIXA
   add constraint FK_ENTIDADE_RECEITA_FIXA foreign key (ID_ENTIDADE_CONTROLE)
      references TB_ENTIDADE_CONTROLE_FINANCEIRO (ID_ENTIDADE_CONTROLE)
go

alter table TB_RECEITA_FIXA
   add constraint FK_NATUREZA_RECEITA_FIXA foreign key (ID_NATUREZA_RECEITA_FIXA)
      references TB_NATUREZA_CONTA (ID_NATUREZA_CONTA)
go

alter table TB_RECEITA_MENSAL
   add constraint FK_ENTIDADE_RECEITA_MENSAL foreign key (ID_ENTIDADE_CONTROLE)
      references TB_ENTIDADE_CONTROLE_FINANCEIRO (ID_ENTIDADE_CONTROLE)
go

alter table TB_RECEITA_MENSAL
   add constraint FK_NATUREZA_RECEITA_MENSAL foreign key (ID_NATUREZA_CONTA)
      references TB_NATUREZA_CONTA (ID_NATUREZA_CONTA)
go

alter table TB_RECEITA_MENSAL
   add constraint FK_TB_RECEI_RL_RECEIT_TB_RECEI foreign key (ID_RECEITA_FIXA)
      references TB_RECEITA_FIXA (ID_RECEITA_FIXA)
go

alter table TB_USUARIO_ACESSO_ENTIDADE_CONTROLE
   add constraint FK_TB_USUAR_RL_ENTIDA_TB_ENTID foreign key (ID_ENTIDADE_CONTROLE)
      references TB_ENTIDADE_CONTROLE_FINANCEIRO (ID_ENTIDADE_CONTROLE)
go

alter table TB_USUARIO_ACESSO_ENTIDADE_CONTROLE
   add constraint FK_USUARIO_SISTEMA_ENTIDADE_CONTROLE foreign key (ID_USUARIO)
      references TB_USUARIO_SISTEMA (ID_USUARIO)
go

alter table TB_USUARIO_ACESSO_ENTIDADE_CONTROLE
   add constraint FK_USUARIO_RESPONSAVEL_ENTIDADE_CONTROLE foreign key (ID_USUARIO_RESPONSACEL_ACESSO)
      references TB_USUARIO_SISTEMA (ID_USUARIO)
go

