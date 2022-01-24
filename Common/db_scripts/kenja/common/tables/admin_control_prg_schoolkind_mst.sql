-- kanji=漢字
-- $Id: 858271c098dcb89baa2b08a86042fed4aff19156 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop   table ADMIN_CONTROL_PRG_SCHOOLKIND_MST

create table ADMIN_CONTROL_PRG_SCHOOLKIND_MST ( \
    SCHOOL_KIND         varchar(2) not null, \
    PROGRAMID           varchar(20) not null, \
    SELECT_SCHOOL_KIND  varchar(2) not null, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ADMIN_CONTROL_PRG_SCHOOLKIND_MST add constraint PK_CTRL_SCHKIND_M primary key (SCHOOL_KIND, PROGRAMID, SELECT_SCHOOL_KIND)
