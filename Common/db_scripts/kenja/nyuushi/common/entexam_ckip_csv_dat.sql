-- kanji=漢字
-- $Id: 0a6592a38725a1c4bbf8a3c71fe93870ac208e72 $

-- 支部マスタ
-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table ENTEXAM_CKIP_CSV_DAT

create table ENTEXAM_CKIP_CSV_DAT ( \
    LOGIN_ID                    varchar(20) not null, \
    ENTEXAMYEAR                 varchar(4) not null, \
    APPLICANTDIV                varchar(1) not null, \
    TESTDIV                     varchar(2) not null, \
    TESTDIV1                    varchar(2) not null, \
    DESIREDIV                   varchar(2), \
    SHDIV                       varchar(1), \
    EXAMNO                      varchar(10) not null, \
    EXAMHALL_NAME               varchar(90), \
    EXAMHALL_CLASSNAME          varchar(90), \
    NAME                        varchar(120), \
    NAME_KANA                   varchar(120), \
    SEX                         varchar(1), \
    BIRTHDAY                    date , \
    ZIPCD                       varchar(8), \
    ADDRESS1                    varchar(150), \
    ADDRESS2                    varchar(150), \
    ADDRESS3                    varchar(150), \
    TELNO                       varchar(14), \
    GTELNO                      varchar(14), \
    GTELNO2                     varchar(14), \
    FS_CD                       varchar(20), \
    FS_NAME                     varchar(120), \
    FS_TELNO                    varchar(14), \
    FS_ZIPCD                    varchar(8), \
    FS_ADDR1                    varchar(150), \
    FS_DAY                      date , \
    FS_GRDNAME                  varchar(3), \
    GNAME                       varchar(120), \
    GKANA                       varchar(120), \
    GZIPCD                      varchar(8), \
    GADDRESS1                   varchar(150), \
    GADDRESS2                   varchar(150), \
    GADDRESS3                   varchar(150), \
    RELATIONSHIP_NAME           varchar(15), \
    ENT_MONEY_STATUS            varchar(2), \
    PRI_NAME                    varchar(60), \
    PRI_CLASSNAME               varchar(60), \
    REMARK1                     varchar(150), \
    REMARK2                     varchar(150), \
    REMARK3                     varchar(150), \
    REMARK4                     varchar(150), \
    REMARK5                     varchar(150), \
    REMARK6                     varchar(150), \
    REMARK7                     varchar(150), \
    REMARK8                     varchar(150), \
    REMARK9                     varchar(150), \
    REMARK10                    varchar(150), \
    DUMMY                       varchar(5), \
    REGISTERCD                  varchar(10), \
    UPDATED                     timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_CKIP_CSV_DAT add constraint PK_EXAM_CKIP_CSV primary key (LOGIN_ID, ENTEXAMYEAR, APPLICANTDIV, TESTDIV, TESTDIV1, EXAMNO)
