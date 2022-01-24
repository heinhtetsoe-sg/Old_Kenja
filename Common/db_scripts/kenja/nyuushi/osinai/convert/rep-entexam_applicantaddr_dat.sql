-- $Id: 46358710c8bf1dfcefeeccf5402f347d200f30c0 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table ENTEXAM_APPLICANTADDR_DAT_OLD
create table ENTEXAM_APPLICANTADDR_DAT_OLD like ENTEXAM_APPLICANTADDR_DAT
insert into ENTEXAM_APPLICANTADDR_DAT_OLD select * from ENTEXAM_APPLICANTADDR_DAT

drop table ENTEXAM_APPLICANTADDR_DAT
create table ENTEXAM_APPLICANTADDR_DAT( \
    ENTEXAMYEAR         varchar(4)  not null, \
    APPLICANTDIV        varchar(1)  not null, \
    EXAMNO              varchar(5)  not null, \
    FAMILY_REGISTER     varchar(2), \
    ZIPCD               varchar(8), \
    PREF_CD             varchar(2), \
    ADDRESS1            varchar(150), \
    ADDRESS2            varchar(150), \
    TELNO               varchar(14), \
    GNAME               varchar(60), \
    GKANA               varchar(120), \
    GZIPCD              varchar(8), \
    GPREF_CD            varchar(2), \
    GADDRESS1           varchar(150), \
    GADDRESS2           varchar(150), \
    GTELNO              varchar(14), \
    GFAXNO              varchar(14), \
    RELATIONSHIP        varchar(2), \
    EMERGENCYCALL       varchar(30), \
    EMERGENCYTELNO      varchar(14), \
    REGISTERCD          varchar(10),  \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_APPLICANTADDR_DAT add constraint PK_ENTEXAM_ADDR primary key (ENTEXAMYEAR, APPLICANTDIV, EXAMNO)

insert into ENTEXAM_APPLICANTADDR_DAT \
select \
    T1.ENTEXAMYEAR, \
    BASE.APPLICANTDIV, \
    T1.EXAMNO, \
    T1.FAMILY_REGISTER, \
    T1.ZIPCD, \
    T1.PREF_CD, \
    T1.ADDRESS1, \
    T1.ADDRESS2, \
    T1.TELNO, \
    T1.GNAME, \
    T1.GKANA, \
    T1.GZIPCD, \
    T1.GPREF_CD, \
    T1.GADDRESS1, \
    T1.GADDRESS2, \
    T1.GTELNO, \
    T1.GFAXNO, \
    T1.RELATIONSHIP, \
    T1.EMERGENCYCALL, \
    T1.EMERGENCYTELNO, \
    T1.REGISTERCD, \
    T1.UPDATED \
from \
    ENTEXAM_APPLICANTADDR_DAT_OLD T1 \
    INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE \
        ON  T1.ENTEXAMYEAR = BASE.ENTEXAMYEAR \
        AND T1.EXAMNO = BASE.EXAMNO
