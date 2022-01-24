-- kanji=漢字
-- $Id: medexam_tooth_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table MEDEXAM_TOOTH_DAT

create table MEDEXAM_TOOTH_DAT \
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

alter table MEDEXAM_TOOTH_DAT add constraint pk_medexam_tooth primary key \
    (YEAR,SCHREGNO)

