-- kanji=漢字
-- $Id: 9565e23c622b5777af7b73fc07b77ffda97e1635 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table HEXAM_RECORD_DOCUMENT_DAT

create table HEXAM_RECORD_DOCUMENT_DAT \
    (YEAR           varchar(4)    not null, \
     SEMESTER       varchar(1)    not null, \
     GRADE          varchar(2)    not null, \
     TYPE_GROUP_CD  varchar(6)    not null, \
     REMARK_DIV     varchar(1)    not null, \
     REMARK1        varchar(1050), \
     REGISTERCD     varchar(8), \
     UPDATED        timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table HEXAM_RECORD_DOCUMENT_DAT add constraint PK_HEX_REC_DOC primary key (YEAR, SEMESTER, GRADE, TYPE_GROUP_CD, REMARK_DIV)
