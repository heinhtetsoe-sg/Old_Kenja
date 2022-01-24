-- kanji=漢字
-- $Id: 5d2831c24c8ffb93e1867a3620179b28416e0e36 $
-- テスト項目マスタ集計フラグ

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop   table W_CSVMSG_PRG_DAT

create table W_CSVMSG_PRG_DAT ( \
    PROGRAMID           varchar(10) not null, \
    MSGROW              integer not null, \
    MSGREMARK           varchar(120) \
) in usr1dms index in idx1dms

alter table W_CSVMSG_PRG_DAT add constraint PK_W_CSVMSG_PRG \
      primary key (PROGRAMID, MSGROW)
