-- kanji=漢字
-- $Id: 6556003e1122473cb254104466a46c619c759be1 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table schreg_transfer_dat

create table schreg_transfer_dat \
    (schregno         varchar(8)    not null, \
     transfercd       varchar(2)    not null, \
     transfer_sdate   date          not null, \
     transfer_edate   date, \
     transferreason   varchar(75), \
     transferplace    varchar(60), \
     transferaddr     varchar(150), \
     transferaddr2    varchar(150), \
     abroad_classdays smallint, \
     abroad_credits   smallint, \
     abroad_print_drop_regd varchar(1), \
     registercd       varchar(10), \
     REMARK1          VARCHAR(90), \
     REMARK2          VARCHAR(90), \
     updated          timestamp default current timestamp \
    )in usr1dms index in idx1dms

alter table schreg_transfer_dat add constraint pk_srt_dat primary key \
    (schregno, transfercd, transfer_sdate)
