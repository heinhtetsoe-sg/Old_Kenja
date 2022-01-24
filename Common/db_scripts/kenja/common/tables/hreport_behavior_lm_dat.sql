-- $Id: d6116265040f0c0a271c911915ec7a8c25216181 $

drop table HREPORT_BEHAVIOR_LM_DAT
create table HREPORT_BEHAVIOR_LM_DAT( \
    YEAR        varchar(4)  not null, \
    SEMESTER    varchar(1)  not null, \
    SCHREGNO    varchar(8)  not null, \
    L_CD        varchar(2)  not null, \
    M_CD        varchar(2)  not null, \
    RECORD      varchar(1)  not null, \
    REGISTERCD  varchar(10), \
    UPDATED     timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table HREPORT_BEHAVIOR_LM_DAT add constraint PK_HREPBEH_LM_D primary key (YEAR, SEMESTER, SCHREGNO, L_CD, M_CD)
