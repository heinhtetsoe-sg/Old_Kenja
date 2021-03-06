-- kanji=漢字
-- $Id: attest_usbkey_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table ATTEST_USBKEY_DAT

create table ATTEST_USBKEY_DAT \
(  \
        "SIGNATURE"        varchar(350) not null, \
        "RESULT"           integer not null, \
        "REGISTERCD"       varchar(8), \
        "UPDATED"          timestamp default current timestamp  \
) in usr1dms index in idx1dms

