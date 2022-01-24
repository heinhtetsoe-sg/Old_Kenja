-- kanji=漢字
-- $Id: 91d1b78a8aa99ba6d795b0366cb3d6c10fe3333a $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table UPLOAD_FILE_PATH_DAT

create table UPLOAD_FILE_PATH_DAT \
        (SEQ                smallint not null, \
         FILE_PATH          varchar(1500), \
         MEMO               varchar(300), \
         FILE_NAME1         varchar(150), \
         FILE_NAME2         varchar(150), \
         FILE_NAME3         varchar(150), \
         REMARK1            varchar(150), \
         REMARK2            varchar(150), \
         REMARK3            varchar(150), \
         REGISTERCD         varchar(10), \
         UPDATED            timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table UPLOAD_FILE_PATH_DAT add constraint PK_UPLOAD_FILE_PATH_DAT primary key \
        (SEQ)

insert into UPLOAD_FILE_PATH_DAT \
(SEQ, FILE_PATH, MEMO) VALUES(1, '/tmp/テスト', '県へ報告する資料として、KNJC165BでPDFを保存する。File Serverにマウントしてある')
