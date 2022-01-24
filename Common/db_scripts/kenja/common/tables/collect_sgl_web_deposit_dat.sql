-- kanji=漢字
-- $Id: b433b595e6f7741860b031ce585a3e634a89426e $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

--Web出願入金情報

drop table COLLECT_SGL_WEB_DEPOSIT_DAT

create table COLLECT_SGL_WEB_DEPOSIT_DAT \
( \
        "YEAR"                  varchar(4)  not null, \
        "SCHREGNO"              varchar(8)  not null, \
        "SEQ"                   int         not null, \
        "ACCOUNTNAME"           varchar(120), \
        "ENT_MONEY"             varchar(10) , \
        "RESERVE_MONEY"         varchar(10) , \
        "REGISTERCD"            varchar(10) , \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_SGL_WEB_DEPOSIT_DAT \
add constraint PK_CO_WEB_DEP_DAT \
primary key \
(YEAR, SCHREGNO)
