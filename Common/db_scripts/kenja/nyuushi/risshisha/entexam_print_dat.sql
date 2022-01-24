-- $Id: 82bf9af0d085012312b3f2aa27cff098032453fa $

drop table ENTEXAM_PRINT_DAT
create table ENTEXAM_PRINT_DAT( \
    ENTEXAMYEAR          varchar(4)    not null, \
    EXAMNO               varchar(10)    not null, \
    PRINTFLG             varchar(1), \
    GET_YOUROKU          varchar(1), \
    GET_MEDEXAM          varchar(1), \
    GET_SPORTS           varchar(1), \
    REGISTERCD           varchar(10), \
    UPDATED              timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_PRINT_DAT add constraint PK_ENTEXAM_PRINT primary key (ENTEXAMYEAR, EXAMNO)
