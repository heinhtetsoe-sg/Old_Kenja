-- kanji=漢字
-- $Id: 75661b85d1066a79566859fc8ef27b85d1cc6202 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table HEXAM_MOCK_REMARK_DAT

create table HEXAM_MOCK_REMARK_DAT \
(  \
        YEAR        varchar(4) not null, \
        MOCKCD      varchar(9) not null, \
        SCHREGNO    varchar(8) not null, \
        REMARK_DIV  varchar(1) not null, \
        REMARK1     varchar(1050) , \
        REMARK2     varchar(1050) , \
        REMARK3     varchar(1050) , \
        REMARK4     varchar(1050) , \
        REGISTERCD  varchar(8), \
        UPDATED     timestamp default current timestamp  \
) in usr1dms index in idx1dms

alter table HEXAM_MOCK_REMARK_DAT  \
add constraint PK_HEXAM_MOCK_REM  \
primary key  \
(YEAR, MOCKCD, SCHREGNO, REMARK_DIV)
