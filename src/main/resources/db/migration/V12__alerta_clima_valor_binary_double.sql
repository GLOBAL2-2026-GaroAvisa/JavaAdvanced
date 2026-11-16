-- O Hibernate mapeia Double para BINARY_DOUBLE no Oracle, e a validacao do
-- schema reprova NUMBER. A V11 criou a coluna como NUMBER(6,2).
-- A tabela acabou de nascer e esta vazia, entao a conversao e direta.
ALTER TABLE T_GA_ALERTA_CLIMA MODIFY (vl_atingido BINARY_DOUBLE);