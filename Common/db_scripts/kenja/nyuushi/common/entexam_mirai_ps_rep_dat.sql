-- $Id: 9d95d6de8e7acac6a6b29019ae2553fd2b95b5ef $
drop table ENTEXAM_MIRAI_PS_REP_DAT

create table ENTEXAM_MIRAI_PS_REP_DAT \
( \
    MIRAI_PS_CD         varchar(10) not null, \
    PS_CD               varchar(7), \
    REGISTERCD          varchar(10),  \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_MIRAI_PS_REP_DAT add constraint \
PK_EEXAM_MR_PSRP primary key (MIRAI_PS_CD)
