-- kanji=漢字
-- $Id: 9feed4e82c2a241e70708f9bceec55547db51eee $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

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

alter table MEDEXAM_TOOTH_DAT add constraint pk_medexam_tooth_d primary key \
    (YEAR,SCHREGNO)

