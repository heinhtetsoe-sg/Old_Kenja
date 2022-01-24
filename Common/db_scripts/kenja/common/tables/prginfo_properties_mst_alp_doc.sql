-- $Id: a8a5f9a2140360154f04a68f63eb1776e00bb7b8 $

drop table PRGINFO_PROPERTIES_MST_ALP_DOC

create table PRGINFO_PROPERTIES_MST_ALP_DOC( \
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

alter table PRGINFO_PROPERTIES_MST_ALP_DOC add constraint PK_PROPERTIE_PRGID_ALP primary key (NAME)
