-- $Id: rep-grd_medexam_tooth_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table GRD_MEDEXAM_TOOTH_DAT_OLD

create table GRD_MEDEXAM_TOOTH_DAT_OLD like GRD_MEDEXAM_TOOTH_DAT

insert into GRD_MEDEXAM_TOOTH_DAT_OLD select * from GRD_MEDEXAM_TOOTH_DAT

drop table GRD_MEDEXAM_TOOTH_DAT

create table GRD_MEDEXAM_TOOTH_DAT \
    (YEAR               varchar(4)    not null, \
     SCHREGNO           varchar(8)    not null, \
     DATE               DATE, \
     JAWS_JOINTCD       varchar(2), \
     PLAQUECD           varchar(2), \
     GUMCD              varchar(2), \
     BABYTOOTH          smallint, \
     REMAINBABYTOOTH    smallint, \
     TREATEDBABYTOOTH   smallint, \
     BRACK_BABYTOOTH    smallint, \
     ADULTTOOTH         smallint, \
     REMAINADULTTOOTH   smallint, \
     TREATEDADULTTOOTH  smallint, \
     LOSTADULTTOOTH     smallint, \
     BRACK_ADULTTOOTH   smallint, \
     OTHERDISEASECD     varchar(2), \
     OTHERDISEASE       varchar(30), \
     DENTISTREMARKCD    varchar(2), \
     DENTISTREMARK      varchar(30), \
     DENTISTREMARKDATE  date, \
     DENTISTTREAT       varchar(30), \
     REGISTERCD         varchar(8), \
     UPDATED            timestamp default current timestamp \
    )

insert into GRD_MEDEXAM_TOOTH_DAT \
    select \
        T1.YEAR, \
        T1.SCHREGNO, \
        T2.DATE, \
        T1.JAWS_JOINTCD, \
        T1.PLAQUECD, \
        T1.GUMCD, \
        T1.BABYTOOTH, \
        T1.REMAINBABYTOOTH, \
        T1.TREATEDBABYTOOTH, \
        T1.BRACK_BABYTOOTH, \
        T1.ADULTTOOTH, \
        T1.REMAINADULTTOOTH, \
        T1.TREATEDADULTTOOTH, \
        T1.LOSTADULTTOOTH, \
        T1.BRACK_ADULTTOOTH, \
        T1.OTHERDISEASECD, \
        T1.OTHERDISEASE, \
        T1.DENTISTREMARKCD, \
        T1.DENTISTREMARK, \
        T1.DENTISTREMARKDATE, \
        T1.DENTISTTREAT, \
        T1.REGISTERCD, \
        T1.UPDATED \
    FROM \
        GRD_MEDEXAM_TOOTH_DAT_OLD T1 \
    LEFT JOIN GRD_MEDEXAM_HDAT T2 ON T1.YEAR = T2.YEAR \
         AND T1.SCHREGNO = T2.SCHREGNO

alter table GRD_MEDEXAM_TOOTH_DAT add constraint pk_grd_med_tooth primary key \
    (YEAR,SCHREGNO)

