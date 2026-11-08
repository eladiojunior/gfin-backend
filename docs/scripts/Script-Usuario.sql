-- Criar um usuário de login no SQL Server
CREATE LOGIN usergfin WITH PASSWORD = 'gfin@2024';

-- Criar um usuário no banco de dados e atribuir o usuário de login
USE DB_GFIN;
CREATE USER usergfin FOR LOGIN usergfin;

-- Conceder a role db_datareader ao usuário (SELECT)
ALTER ROLE db_datareader ADD MEMBER usergfin;

-- Conceder a role db_datawriter ao usuário (INSERT, UPDATE e DELETE)
ALTER ROLE db_datawriter ADD MEMBER usergfin;

-- Conceder acesso ao usuário 'db_owner' para acesso total ao banco de dados.
-- ALTER ROLE db_owner ADD MEMBER usergfin;