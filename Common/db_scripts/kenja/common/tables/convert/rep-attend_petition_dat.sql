-- $Id: 26e08178ad9bba0cfb3e831d995ac246655e20ae $

DROP TABLE ATTEND_PETITION_DAT_OLD
RENAME TABLE ATTEND_PETITION_DAT TO ATTEND_PETITION_DAT_OLD
CREATE TABLE ATTEND_PETITION_DAT( \
    YEAR            varchar(4) not null, \
    SEQNO           integer not null, \
    SCHREGNO        varchar(8) not null, \
    ATTENDDATE      date not null, \
    PERIODCD        varchar(1) not null, \
    DI_CD           varchar(2), \
    DI_REMARK_CD    varchar(3), \
    DI_REMARK       varchar(30), \
    INPUT_FLG       varchar(1), \
    EXECUTED        varchar(1), \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) IN USR1DMS INDEX IN IDX1DMS

INSERT INTO ATTEND_PETITION_DAT \
    SELECT \
        YEAR, \
        SEQNO, \
        SCHREGNO, \
        ATTENDDATE, \
        PERIODCD, \
        DI_CD, \
        CAST(NULL AS VARCHAR(3)) AS DI_REMARK_CD, \
        DI_REMARK, \
        INPUT_FLG, \
        EXECUTED, \
        REGISTERCD, \
        UPDATED \
    FROM \
        ATTEND_PETITION_DAT_OLD

ALTER TABLE ATTEND_PETITION_DAT ADD CONSTRAINT PK_ATTEND_P_DAT PRIMARY KEY (YEAR, SEQNO, SCHREGNO, ATTENDDATE, PERIODCD)