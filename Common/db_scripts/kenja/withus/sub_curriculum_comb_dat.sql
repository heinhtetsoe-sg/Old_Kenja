-- kanji=漢字
-- $Id: sub_curriculum_comb_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table SUB_CURRICULUM_COMB_DAT

create table SUB_CURRICULUM_COMB_DAT \
(  \
    MOTO_CLASSCD         varchar(2) not null, \
    MOTO_CURRICULUM_CD   varchar(1) not null, \
    MOTO_SUBCLASSCD      varchar(6) not null, \
    SAKI_CLASSCD         varchar(2) not null, \
    SAKI_CURRICULUM_CD   varchar(1) not null, \
    SAKI_SUBCLASSCD      varchar(6) not null, \
    REGISTERCD      varchar(8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SUB_CURRICULUM_COMB_DAT  \
add constraint PK_SUB_CURRICULUM \
primary key  \
(MOTO_CLASSCD,MOTO_CURRICULUM_CD,MOTO_SUBCLASSCD,\
SAKI_CLASSCD,SAKI_CURRICULUM_CD,SAKI_SUBCLASSCD)
