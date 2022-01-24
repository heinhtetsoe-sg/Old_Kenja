-- kanji=漢字
-- $Id: 75dd49eeddad840de61536552b0cab88f5b82227 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table SCHREG_OTHER_SYSTEM_USER_DAT

create table SCHREG_OTHER_SYSTEM_USER_DAT \
(  \
    SYSTEMID            VARCHAR(8)      not null, \
    SCHREGNO            VARCHAR(8)      not null, \
    LOGINID             VARCHAR(26), \
    PASSWORD            VARCHAR(32), \
    REGISTERCD          VARCHAR(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SCHREG_OTHER_SYSTEM_USER_DAT add constraint PK_SCH_OTHER_SY_D \
primary key (SYSTEMID, SCHREGNO)
