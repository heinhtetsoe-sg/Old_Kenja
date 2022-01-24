-- $Id: a6a4f6998f2df4d4d3965f9db9f9231f0cf4eaa8 $

DROP TABLE PRGINFO_PROPERTIES_ALP_DOC_OLD
RENAME TABLE PRGINFO_PROPERTIES_ALP_DOC TO PRGINFO_PROPERTIES_ALP_DOC_OLD
CREATE TABLE PRGINFO_PROPERTIES_ALP_DOC( \
    PROGRAMID      VARCHAR(20) not null, \
    NAME       varchar(300)   not null, \
    VALUE      varchar(300), \
    SORT       smallint, \
    REMARK     varchar(300), \
    COMMENT_1  varchar(300), \
    COMMENT_2  varchar(300), \
    COMMENT_3  varchar(300), \
    COMMENT_4  varchar(300), \
    COMMENT_5  varchar(300), \
    COMMENT_6  varchar(300), \
    COMMENT_7  varchar(300), \
    COMMENT_8  varchar(300), \
    COMMENT_9  varchar(300), \
    COMMENT_10 varchar(300), \
    REGISTERCD varchar(10), \
    UPDATED    timestamp default current timestamp \
) IN USR1DMS INDEX IN IDX1DMS

alter table PRGINFO_PROPERTIES_ALP_DOC add constraint PK_PROPERTIE_ALP primary key (PROGRAMID, NAME)