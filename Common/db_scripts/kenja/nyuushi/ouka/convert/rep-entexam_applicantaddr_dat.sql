-- $Id: feaaad40e6121294f438fd07d983ff227b375836 $

DROP TABLE ENTEXAM_APPLICANTADDR_DAT_OLD
RENAME TABLE ENTEXAM_APPLICANTADDR_DAT TO ENTEXAM_APPLICANTADDR_DAT_OLD
CREATE TABLE ENTEXAM_APPLICANTADDR_DAT( \
    ENTEXAMYEAR         varchar(4)  not null, \
    APPLICANTDIV        varchar(1)  not null, \
    EXAMNO              varchar(10) not null, \
    FAMILY_REGISTER     varchar(2), \
    ZIPCD               varchar(8), \
    PREF_CD             varchar(2), \
    ADDRESS1            varchar(150), \
    ADDRESS2            varchar(150), \
    TELNO               varchar(14), \
    GNAME               varchar(120), \
    GKANA               varchar(240), \
    GZIPCD              varchar(8), \
    GPREF_CD            varchar(2), \
    GADDRESS1           varchar(150), \
    GADDRESS2           varchar(150), \
    GTELNO              varchar(14), \
    GTELNO2             varchar(14), \
    GFAXNO              varchar(14), \
    RELATIONSHIP        varchar(2), \
    EMERGENCYCALL       varchar(30), \
    EMERGENCYTELNO      varchar(14), \
    REGISTERCD          varchar(10),  \
    UPDATED             timestamp default current timestamp \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ENTEXAM_APPLICANTADDR_DAT ADD CONSTRAINT PK_ENTEXAM_ADDR PRIMARY KEY (ENTEXAMYEAR, APPLICANTDIV, EXAMNO)

INSERT INTO ENTEXAM_APPLICANTADDR_DAT \
    SELECT \
        * \
    FROM \
        ENTEXAM_APPLICANTADDR_DAT_OLD

