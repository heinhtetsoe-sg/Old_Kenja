-- kanji=漢字
-- $Id: ccf89d24654664a6a19309d8d99ceac62ba7b94d $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
-- 2018/11/01現在、ALPが裏からデータをセットする。

drop table SEASON_GREETINGS_MST

create table SEASON_GREETINGS_MST \
(  \
    MONTH           varchar(2)  not null, \
    SEQ             varchar(3)  not null, \
    GREETING        varchar(90), \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SEASON_GREETINGS_MST add constraint PK_SEASON_GREET_M \
primary key (MONTH, SEQ)
