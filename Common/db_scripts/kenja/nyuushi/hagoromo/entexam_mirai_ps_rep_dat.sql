-- kanji=漢字
-- $Id: 0d8764d3a19c278a2118e0b0113fba57b31965f0 $

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
