-- kanji=漢字
-- $Id: 4da374b62ca184426f42e74a48cf6ef962b61e04 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table ATTEND_SUBCLASS_SPECIAL_MST

create table ATTEND_SUBCLASS_SPECIAL_MST \
(  \
    SPECIAL_GROUP_CD    varchar(3)  not null, \
    SPECIAL_GROUP_NAME  varchar(60), \
    SPECIAL_GROUP_ABBV  varchar(9), \
    REGISTERCD          varchar(8), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ATTEND_SUBCLASS_SPECIAL_MST add constraint PK_ATTND_SS_MST \
primary key (SPECIAL_GROUP_CD)


