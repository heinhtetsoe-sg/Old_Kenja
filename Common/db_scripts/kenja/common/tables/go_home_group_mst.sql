-- kanji=漢字
-- $Id: 135b6d9f28d3fed0b12afb56103d1f683302ce61 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table GO_HOME_GROUP_MST

create table GO_HOME_GROUP_MST \
(  \
    GO_HOME_GROUP_NO    varchar(2) not null, \
    GO_HOME_GROUP_NAME  varchar(60), \
    REMARK1             varchar(150), \
    MEETING_PLACE       varchar(45), \
    REGISTERCD          varchar(8), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table GO_HOME_GROUP_MST add constraint PK_GO_HOME_GM \
primary key (GO_HOME_GROUP_NO)
