-- kanji=漢字
-- $Id: f3446d746df0755bf730d8712ab8080e3e00081b $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table MEDEXAM_TOOTH_DETAIL_DAT

create table MEDEXAM_TOOTH_DETAIL_DAT \
    (YEAR           varchar(4)    not null, \
     SCHREGNO       varchar(8)    not null, \
     TOOTH_SEQ      varchar(3)    not null, \
     TOOTH_REMARK1  varchar(300), \
     TOOTH_REMARK2  varchar(300), \
     TOOTH_REMARK3  varchar(300), \
     TOOTH_REMARK4  varchar(300), \
     TOOTH_REMARK5  varchar(300), \
     TOOTH_REMARK6  varchar(300), \
     TOOTH_REMARK7  varchar(300), \
     TOOTH_REMARK8  varchar(300), \
     TOOTH_REMARK9  varchar(300), \
     TOOTH_REMARK10 varchar(300), \
     REGISTERCD     varchar(8), \
     UPDATED        timestamp default current timestamp \
    )

alter table MEDEXAM_TOOTH_DETAIL_DAT add constraint pk_med_tooth_de primary key \
    (YEAR, SCHREGNO, TOOTH_SEQ)

