-- $Id: rep-schreg_send_address_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table SCHREG_SEND_ADDRESS_DAT_OLD
create table SCHREG_SEND_ADDRESS_DAT_OLD like SCHREG_SEND_ADDRESS_DAT
insert into SCHREG_SEND_ADDRESS_DAT_OLD select * from SCHREG_SEND_ADDRESS_DAT

drop table SCHREG_SEND_ADDRESS_DAT

CREATE TABLE SCHREG_SEND_ADDRESS_DAT( \
    SCHREGNO              VARCHAR(8)    NOT NULL, \
    DIV                   VARCHAR(1)    NOT NULL, \
    SEND_RELATIONSHIP     VARCHAR(2), \
    SEND_NAME             VARCHAR(60), \
    SEND_KANA             VARCHAR(120), \
    SEND_SEX              VARCHAR(1), \
    SEND_ZIPCD            VARCHAR(8), \
    SEND_AREACD           VARCHAR(2), \
    SEND_ADDR1            VARCHAR(150), \
    SEND_ADDR2            VARCHAR(75), \
    SEND_ADDR_FLG         VARCHAR(1), \
    SEND_TELNO            VARCHAR(14), \
    SEND_TELNO2           VARCHAR(14), \
    SEND_JOBCD            VARCHAR(2), \
    PUBLIC_OFFICE         VARCHAR(30), \
    REGISTERCD            VARCHAR(8), \
    UPDATED               TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SCHREG_SEND_ADDRESS_DAT ADD CONSTRAINT PK_SCHREG_SAD PRIMARY KEY (SCHREGNO, DIV)

insert into SCHREG_SEND_ADDRESS_DAT \
select \
    SCHREGNO, \
    DIV, \
    SEND_RELATIONSHIP, \
    SEND_NAME, \
    SEND_KANA, \
    SEND_SEX, \
    SEND_ZIPCD, \
    SEND_AREACD, \
    SEND_ADDR1, \
    SEND_ADDR2, \
    SEND_ADDR_FLG, \
    SEND_TELNO, \
    CAST(NULL AS VARCHAR(14)) AS SEND_TELNO2, \
    SEND_JOBCD, \
    PUBLIC_OFFICE, \
    REGISTERCD, \
    UPDATED \
from SCHREG_SEND_ADDRESS_DAT_OLD
