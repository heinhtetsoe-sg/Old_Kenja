-- kanji=漢字
-- $Id: 709b01a878edf3fc7700c981bcaf07f1010a40d9 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
drop table MEDEXAM_TOOTH_DAT_OLD

create table MEDEXAM_TOOTH_DAT_OLD like MEDEXAM_TOOTH_DAT

insert into MEDEXAM_TOOTH_DAT_OLD select * from MEDEXAM_TOOTH_DAT

drop table MEDEXAM_TOOTH_DAT

create table MEDEXAM_TOOTH_DAT \
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


insert into MEDEXAM_TOOTH_DAT \
    select \
        T1.YEAR, \
        T1.SCHREGNO, \
        T1.JAWS_JOINTCD, \
        T1.JAWS_JOINTCD2, \
        '' AS JAWS_JOINTCD3, \
        T1.PLAQUECD, \
        T1.GUMCD, \
        T1.CALCULUS, \
        T1.ORTHODONTICS, \
        T1.UP_R_BABY5, \
        T1.UP_R_BABY4, \
        T1.UP_R_BABY3, \
        T1.UP_R_BABY2, \
        T1.UP_R_BABY1, \
        T1.UP_L_BABY1, \
        T1.UP_L_BABY2, \
        T1.UP_L_BABY3, \
        T1.UP_L_BABY4, \
        T1.UP_L_BABY5, \
        T1.LW_R_BABY5, \
        T1.LW_R_BABY4, \
        T1.LW_R_BABY3, \
        T1.LW_R_BABY2, \
        T1.LW_R_BABY1, \
        T1.LW_L_BABY1, \
        T1.LW_L_BABY2, \
        T1.LW_L_BABY3, \
        T1.LW_L_BABY4, \
        T1.LW_L_BABY5, \
        T1.BABYTOOTH, \
        T1.REMAINBABYTOOTH, \
        T1.TREATEDBABYTOOTH, \
        T1.BRACK_BABYTOOTH, \
        T1.UP_R_ADULT8, \
        T1.UP_R_ADULT7, \
        T1.UP_R_ADULT6, \
        T1.UP_R_ADULT5, \
        T1.UP_R_ADULT4, \
        T1.UP_R_ADULT3, \
        T1.UP_R_ADULT2, \
        T1.UP_R_ADULT1, \
        T1.UP_L_ADULT1, \
        T1.UP_L_ADULT2, \
        T1.UP_L_ADULT3, \
        T1.UP_L_ADULT4, \
        T1.UP_L_ADULT5, \
        T1.UP_L_ADULT6, \
        T1.UP_L_ADULT7, \
        T1.UP_L_ADULT8, \
        T1.LW_R_ADULT8, \
        T1.LW_R_ADULT7, \
        T1.LW_R_ADULT6, \
        T1.LW_R_ADULT5, \
        T1.LW_R_ADULT4, \
        T1.LW_R_ADULT3, \
        T1.LW_R_ADULT2, \
        T1.LW_R_ADULT1, \
        T1.LW_L_ADULT1, \
        T1.LW_L_ADULT2, \
        T1.LW_L_ADULT3, \
        T1.LW_L_ADULT4, \
        T1.LW_L_ADULT5, \
        T1.LW_L_ADULT6, \
        T1.LW_L_ADULT7, \
        T1.LW_L_ADULT8, \
        T1.ADULTTOOTH, \
        T1.REMAINADULTTOOTH, \
        T1.TREATEDADULTTOOTH, \
        T1.LOSTADULTTOOTH, \
        T1.BRACK_ADULTTOOTH, \
        T1.CHECKADULTTOOTH, \
        T1.OTHERDISEASECD, \
        T1.OTHERDISEASE, \
        T1.DENTISTREMARKCD, \
        T1.DENTISTREMARK, \
        T1.DENTISTREMARKDATE, \
        T1.DENTISTREMARK_CO, \
        T1.DENTISTREMARK_GO, \
        T1.DENTISTREMARK_G, \
        T1.DENTISTTREATCD, \
        T1.DENTISTTREAT, \
        T1.REGISTERCD, \
        T1.UPDATED \
    from \
        MEDEXAM_TOOTH_DAT_OLD T1

alter table MEDEXAM_TOOTH_DAT add constraint pk_medexam_tooth_d primary key \
    (YEAR, SCHREGNO)

