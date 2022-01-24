-- $Id: guardian_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $


drop   table GUARDIAN_DAT
create table GUARDIAN_DAT \
        (SCHREGNO               varchar(8) not null, \
         RELATIONSHIP           varchar(2) not null, \
         GUARD_NAME             varchar(60), \
         GUARD_KANA             varchar(120), \
         GUARD_BIRTHDAY         date , \
         GUARD_SEX              varchar(1), \
         GUARD_ZIPCD            varchar(8), \
         GUARD_PREF_CD          varchar(2), \
         GUARD_ADDR1            varchar(75), \
         GUARD_ADDR2            varchar(75), \
         GUARD_ADDR3            varchar(75), \
         GUARD_TELNO            varchar(14), \
         GUARD_TELNO_ABB        varchar(14), \
         GUARD_FAXNO            varchar(14), \
         GUARD_E_MAIL           varchar(20), \
         GUARD_JOBCD            varchar(2), \
         GUARD_WORK_NAME        varchar(60), \
         GUARD_WORK_TELNO       varchar(14), \
         GUARANTOR_RELATIONSHIP varchar(2), \
         GUARANTOR_NAME         varchar(60), \
         GUARANTOR_KANA         varchar(120), \
         GUARANTOR_SEX          varchar(1), \
         GUARANTOR_ZIPCD        varchar(8), \
         GUARANTOR_PREF_CD      varchar(2), \
         GUARANTOR_ADDR1        varchar(75), \
         GUARANTOR_ADDR2        varchar(75), \
         GUARANTOR_ADDR3        varchar(75), \
         GUARANTOR_TELNO        varchar(14), \
         GUARANTOR_TELNO_ABB    varchar(14), \
         GUARANTOR_JOBCD        varchar(2), \
         PUBLIC_OFFICE          varchar(30), \
         REGISTERCD             varchar(8), \
         UPDATED                timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table GUARDIAN_DAT add constraint PK_GUARDIAN_DAT primary key (SCHREGNO)


