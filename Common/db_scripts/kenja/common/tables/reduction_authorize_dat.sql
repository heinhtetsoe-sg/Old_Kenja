-- $Id: 3c432f9da598edd842517fa0361080bf28265a6e $

drop table REDUCTION_AUTHORIZE_DAT
create table REDUCTION_AUTHORIZE_DAT( \
    SCHOOLCD                varchar(12) not null, \
    SCHOOL_KIND             varchar(2)  not null, \
    SCHREGNO                varchar(8)  not null, \
    RENBAN                  varchar(4), \
    PASSNO                  varchar(25), \
    STATUS                  varchar(2), \
    REASON                  varchar(450), \
    DIS_APPLY_MONTH         varchar(2), \
    RECEIVE_MONEY           integer, \
    SUP_LIMIT_MONTH         varchar(2), \
    LIMIT_CREDIT            varchar(3), \
    REMARK                  varchar(450), \
    APPLY_DATE              date, \
    DECIDE_DATE             date, \
    BEGIN_YEARMONTH         varchar(6), \
    REMAIN_SUP_LIMIT_MONTH  varchar(2), \
    REGISTERCD              varchar(10), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REDUCTION_AUTHORIZE_DAT add constraint PK_REDUC_AUTH_DAT primary key (SCHOOLCD, SCHOOL_KIND, SCHREGNO)
