-- kanji=漢字
-- $Id: subclass_required_study_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table SUBCLASS_REQUIRED_STUDY_DAT

create table SUBCLASS_REQUIRED_STUDY_DAT \
    (CLASSCD        varchar(2) not null, \
     CURRICULUM_CD  varchar(1) not null, \
     SUBCLASSCD     varchar(6) not null, \
     SEQ            varchar(2) not null, \
     REGISTERCD     varchar(8), \
     UPDATED        timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table SUBCLASS_REQUIRED_STUDY_DAT add constraint PK_SUBREQUIRE primary key (CLASSCD, CURRICULUM_CD, SUBCLASSCD, SEQ)
