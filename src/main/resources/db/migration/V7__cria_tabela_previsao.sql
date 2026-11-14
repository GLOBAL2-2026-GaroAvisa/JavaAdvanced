CREATE TABLE T_GA_PREVISAO (
    fk_id_endereco   NUMBER(10)                  NOT NULL,
    dados            CLOB                        NOT NULL,
    dt_atualizacao   TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT Previsao_PK          PRIMARY KEY (fk_id_endereco),
    CONSTRAINT Previsao_Endereco_FK FOREIGN KEY (fk_id_endereco)
        REFERENCES T_GA_ENDERECO (id_endereco) ON DELETE CASCADE,
    CONSTRAINT Previsao_Dados_CK    CHECK (dados IS JSON)
);