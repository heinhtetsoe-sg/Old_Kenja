-- kanji=漢字
-- $Id: 4af65fbcb63443e7cc514d7fdd23abc9a56cedf0 $

-- 駅年度マスタ
-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop   table AFT_RECOMMENDATION_ASSESS_AVG_SUBCLASS_DAT

create table AFT_RECOMMENDATION_ASSESS_AVG_SUBCLASS_DAT ( \
    YEAR                   varchar(4) not null, \
    RECOMMENDATION_CD      varchar(4) not null, \
    SEQ                    smallint not null, \
    CLASSCD                varchar(2) not null, \
    SCHOOL_KIND            varchar(2) not null, \
    CURRICULUM_CD          varchar(2) not null, \
    SUBCLASSCD             varchar(6) not null, \
    REGISTERCD             varchar(10), \
    UPDATED                timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table AFT_RECOMMENDATION_ASSESS_AVG_SUBCLASS_DAT add constraint PK_AFT_RECOMMENDATION_ASSESS_AVG_SUBCLASS_DAT \
primary key (YEAR, RECOMMENDATION_CD, SEQ, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD)
