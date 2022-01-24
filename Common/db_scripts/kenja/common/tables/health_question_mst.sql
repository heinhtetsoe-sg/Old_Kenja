-- kanji=漢字
-- $Id: 549bbd5b4d422aeb603fb53713f4bef531ed50fe $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table HEALTH_QUESTION_MST

create table HEALTH_QUESTION_MST \
        (QUESTIONCD             varchar(2)      not null, \
         CONTENTS               varchar(120), \
         SORT                   varchar(2), \
         REGISTERCD             varchar(10), \
         UPDATED                timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table HEALTH_QUESTION_MST add constraint pk_hea_que_mst primary key \
        (QUESTIONCD)
