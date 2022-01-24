-- $Id: reduction_auto_create_hist_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $
drop table REDUCTION_AUTO_CREATE_HIST_DAT
create table REDUCTION_AUTO_CREATE_HIST_DAT( \
    YEAR        varchar(4) not null, \
    EXE_TIME    timestamp  not null, \
    REGISTERCD  varchar(8), \
    UPDATED     timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REDUCTION_AUTO_CREATE_HIST_DAT add constraint PK_REDUCTION_HIST primary key(YEAR, EXE_TIME)
