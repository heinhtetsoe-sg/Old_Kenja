-- kanji=漢字
-- $Id: 11be5bd56c9c42759fba4e1ff7d198a46cab1c0f $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table CLASS_REQUIRED_DAT

create table CLASS_REQUIRED_DAT( \
     SCHREGNO       VARCHAR(8) NOT NULL, \
     CLASSCD        VARCHAR(2) NOT NULL, \
     SCHOOL_KIND    VARCHAR(2) NOT NULL, \
     ERR_FLG        VARCHAR(1), \
     CURRICULUM_CD  VARCHAR(1), \
     COURSECD       VARCHAR(1), \
     MAJORCD        VARCHAR(3), \
     SEQ            VARCHAR(2), \
     REGISTERCD     VARCHAR(8), \
     UPDATED        TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) in usr1dms index in idx1dms

alter table CLASS_REQUIRED_DAT add constraint PK_CLASSREQUIRE primary key (SCHREGNO, CLASSCD, SCHOOL_KIND)
