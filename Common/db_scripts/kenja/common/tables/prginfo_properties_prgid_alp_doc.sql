-- $Id: 5539dc22062d794eeb6d240989041ba828be0d1b $

drop table PRGINFO_PROPERTIES_PRGID_ALP_DOC

create table PRGINFO_PROPERTIES_PRGID_ALP_DOC( \
    PROGRAMID  varchar(50)   not null, \
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
) in usr1dms index in idx1dms

alter table PRGINFO_PROPERTIES_PRGID_ALP_DOC add constraint PK_PROPERTIE_PRGID_ALP primary key (PROGRAMID, NAME)

INSERT INTO PRGINFO_PROPERTIES_PRGID_ALP_DOC \
    SELECT \
        PROGRAMID, \
        NAME       , \
        VALUE      , \
        SORT       , \
        REMARK     , \
        COMMENT_1  , \
        COMMENT_2  , \
        COMMENT_3  , \
        COMMENT_4  , \
        COMMENT_5  , \
        COMMENT_6  , \
        COMMENT_7  , \
        COMMENT_8  , \
        COMMENT_9  , \
        COMMENT_10 , \
        REGISTERCD , \
        UPDATED      \
    FROM \
        PRGINFO_PROPERTIES_ALP_DOC
