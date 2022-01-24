-- $Id: subclass_std_pass_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table SUBCLASS_STD_PASS_DAT
create table SUBCLASS_STD_PASS_DAT ( \
     YEAR                   varchar(4)  not null, \
     SEMESTER               varchar(1)  not null, \
     CLASSCD                varchar(2)  not null, \
     SCHOOL_KIND            varchar(2)  not null, \
     CURRICULUM_CD          varchar(2)  not null, \
     SUBCLASSCD             varchar(6)  not null, \
     SCHREGNO               varchar(8)  not null, \
     REP_PASS_FLG           varchar(1), \
     SCHOOLING_PASS_FLG     varchar(1), \
     RECORD_PASS_FLG        varchar(1), \
     SEM_PASS_FLG           varchar(1), \
     GRAD_PASS_FLG          varchar(1), \
     REGISTERCD             varchar(8), \
     UPDATED                timestamp default current timestamp \
    ) in usr1dms index in idx1dms
alter table SUBCLASS_STD_PASS_DAT add constraint pk_subcls_s_p_dat primary key(YEAR,SEMESTER,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,SCHREGNO)
