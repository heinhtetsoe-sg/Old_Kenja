-- $Id: f30931a055772557f27ed309f0bb65deae978a78 $

drop table GRD_MEDEXAM_TOOTH_DAT_OLD

create table GRD_MEDEXAM_TOOTH_DAT_OLD like GRD_MEDEXAM_TOOTH_DAT

insert into GRD_MEDEXAM_TOOTH_DAT_OLD select * from GRD_MEDEXAM_TOOTH_DAT

drop table GRD_MEDEXAM_TOOTH_DAT

create table GRD_MEDEXAM_TOOTH_DAT \
    (YEAR               varchar(4)    not null, \
     SCHREGNO           varchar(8)    not null, \
     JAWS_JOINTCD       varchar(2), \
     JAWS_JOINTCD2      varchar(2), \
     JAWS_JOINTCD3      varchar(2), \
     PLAQUECD           varchar(2), \
     GUMCD              varchar(2), \
     CALCULUS           varchar(2), \
     ORTHODONTICS       varchar(2), \
     UP_R_BABY5         varchar(2), \
     UP_R_BABY4         varchar(2), \
     UP_R_BABY3         varchar(2), \
     UP_R_BABY2         varchar(2), \
     UP_R_BABY1         varchar(2), \
     UP_L_BABY1         varchar(2), \
     UP_L_BABY2         varchar(2), \
     UP_L_BABY3         varchar(2), \
     UP_L_BABY4         varchar(2), \
     UP_L_BABY5         varchar(2), \
     LW_R_BABY5         varchar(2), \
     LW_R_BABY4         varchar(2), \
     LW_R_BABY3         varchar(2), \
     LW_R_BABY2         varchar(2), \
     LW_R_BABY1         varchar(2), \
     LW_L_BABY1         varchar(2), \
     LW_L_BABY2         varchar(2), \
     LW_L_BABY3         varchar(2), \
     LW_L_BABY4         varchar(2), \
     LW_L_BABY5         varchar(2), \
     BABYTOOTH          smallint, \
     REMAINBABYTOOTH    smallint, \
     TREATEDBABYTOOTH   smallint, \
     BRACK_BABYTOOTH    smallint, \
     UP_R_ADULT8        varchar(2), \
     UP_R_ADULT7        varchar(2), \
     UP_R_ADULT6        varchar(2), \
     UP_R_ADULT5        varchar(2), \
     UP_R_ADULT4        varchar(2), \
     UP_R_ADULT3        varchar(2), \
     UP_R_ADULT2        varchar(2), \
     UP_R_ADULT1        varchar(2), \
     UP_L_ADULT1        varchar(2), \
     UP_L_ADULT2        varchar(2), \
     UP_L_ADULT3        varchar(2), \
     UP_L_ADULT4        varchar(2), \
     UP_L_ADULT5        varchar(2), \
     UP_L_ADULT6        varchar(2), \
     UP_L_ADULT7        varchar(2), \
     UP_L_ADULT8        varchar(2), \
     LW_R_ADULT8        varchar(2), \
     LW_R_ADULT7        varchar(2), \
     LW_R_ADULT6        varchar(2), \
     LW_R_ADULT5        varchar(2), \
     LW_R_ADULT4        varchar(2), \
     LW_R_ADULT3        varchar(2), \
     LW_R_ADULT2        varchar(2), \
     LW_R_ADULT1        varchar(2), \
     LW_L_ADULT1        varchar(2), \
     LW_L_ADULT2        varchar(2), \
     LW_L_ADULT3        varchar(2), \
     LW_L_ADULT4        varchar(2), \
     LW_L_ADULT5        varchar(2), \
     LW_L_ADULT6        varchar(2), \
     LW_L_ADULT7        varchar(2), \
     LW_L_ADULT8        varchar(2), \
     ADULTTOOTH         smallint, \
     REMAINADULTTOOTH   smallint, \
     TREATEDADULTTOOTH  smallint, \
     LOSTADULTTOOTH     smallint, \
     BRACK_ADULTTOOTH   smallint, \
     CHECKADULTTOOTH    smallint, \
     OTHERDISEASECD     varchar(2), \
     OTHERDISEASE       varchar(60), \
     DENTISTREMARKCD    varchar(2), \
     DENTISTREMARK      varchar(60), \
     DENTISTREMARKDATE  date, \
     DENTISTREMARK_CO   smallint, \
     DENTISTREMARK_GO   varchar(1), \
     DENTISTREMARK_G    varchar(1), \
     DENTISTTREATCD     varchar(2), \
     DENTISTTREAT       varchar(60), \
     REGISTERCD         varchar(10), \
     UPDATED            timestamp default current timestamp \
    )

alter table GRD_MEDEXAM_TOOTH_DAT add constraint pk_grd_med_tooth primary key \
    (YEAR, SCHREGNO)


insert into GRD_MEDEXAM_TOOTH_DAT \
    select \
        YEAR, \
        SCHREGNO, \
        JAWS_JOINTCD, \
        JAWS_JOINTCD2, \
        CAST(NULL AS VARCHAR(2)) AS JAWS_JOINTCD3, \
        PLAQUECD, \
        GUMCD, \
        CALCULUS, \
        ORTHODONTICS, \
        UP_R_BABY5, \
        UP_R_BABY4, \
        UP_R_BABY3, \
        UP_R_BABY2, \
        UP_R_BABY1, \
        UP_L_BABY1, \
        UP_L_BABY2, \
        UP_L_BABY3, \
        UP_L_BABY4, \
        UP_L_BABY5, \
        LW_R_BABY5, \
        LW_R_BABY4, \
        LW_R_BABY3, \
        LW_R_BABY2, \
        LW_R_BABY1, \
        LW_L_BABY1, \
        LW_L_BABY2, \
        LW_L_BABY3, \
        LW_L_BABY4, \
        LW_L_BABY5, \
        BABYTOOTH, \
        REMAINBABYTOOTH, \
        TREATEDBABYTOOTH, \
        BRACK_BABYTOOTH, \
        UP_R_ADULT8, \
        UP_R_ADULT7, \
        UP_R_ADULT6, \
        UP_R_ADULT5, \
        UP_R_ADULT4, \
        UP_R_ADULT3, \
        UP_R_ADULT2, \
        UP_R_ADULT1, \
        UP_L_ADULT1, \
        UP_L_ADULT2, \
        UP_L_ADULT3, \
        UP_L_ADULT4, \
        UP_L_ADULT5, \
        UP_L_ADULT6, \
        UP_L_ADULT7, \
        UP_L_ADULT8, \
        LW_R_ADULT8, \
        LW_R_ADULT7, \
        LW_R_ADULT6, \
        LW_R_ADULT5, \
        LW_R_ADULT4, \
        LW_R_ADULT3, \
        LW_R_ADULT2, \
        LW_R_ADULT1, \
        LW_L_ADULT1, \
        LW_L_ADULT2, \
        LW_L_ADULT3, \
        LW_L_ADULT4, \
        LW_L_ADULT5, \
        LW_L_ADULT6, \
        LW_L_ADULT7, \
        LW_L_ADULT8, \
        ADULTTOOTH, \
        REMAINADULTTOOTH, \
        TREATEDADULTTOOTH, \
        LOSTADULTTOOTH, \
        BRACK_ADULTTOOTH, \
        CHECKADULTTOOTH, \
        OTHERDISEASECD, \
        OTHERDISEASE, \
        DENTISTREMARKCD, \
        DENTISTREMARK, \
        DENTISTREMARKDATE, \
        CAST(NULL AS SMALLINT)   AS DENTISTREMARK_CO, \
        CAST(NULL AS VARCHAR(1)) AS DENTISTREMARK_GOTCD3, \
        CAST(NULL AS VARCHAR(1)) AS DENTISTREMARK_G, \
        DENTISTTREATCD, \
        DENTISTTREAT, \
        REGISTERCD, \
        UPDATED \
    from \
        GRD_MEDEXAM_TOOTH_DAT_OLD 

