-- kanji=漢字
-- $Id: 1634bc04c0a4c5e77aa1e32660a6366045dd6519 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table CLASS_REQUIRED_ERR_DAT

create table CLASS_REQUIRED_ERR_DAT( \
     SCHREGNO       VARCHAR(8) NOT NULL, \
     CLASSCD        VARCHAR(2) NOT NULL, \
     SCHOOL_KIND    VARCHAR(2) NOT NULL, \
     REGISTERCD     VARCHAR(8), \
     UPDATED        TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) in usr1dms index in idx1dms

alter table CLASS_REQUIRED_ERR_DAT add constraint PK_SUBREQUIRE primary key (SCHREGNO, CLASSCD, SCHOOL_KIND)
