-- kanji=漢字
-- $Id: rep-text_issuecompany_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table TEXT_ISSUECOMPANY_MST_OLD
create table TEXT_ISSUECOMPANY_MST_OLD like TEXT_ISSUECOMPANY_MST
insert into TEXT_ISSUECOMPANY_MST_OLD select * from TEXT_ISSUECOMPANY_MST

drop table TEXT_ISSUECOMPANY_MST
create table TEXT_ISSUECOMPANY_MST \
( \
     ISSUECOMPANY_CD      varchar(4) not null, \
     ISSUECOMPANY_NAME    varchar(60), \
     ISSUECOMPANY_ABBV    varchar(30), \
     ISSUECOMPANY_ZIPCD   varchar(8), \
     ISSUECOMPANY_PREF_CD varchar(2), \
     ISSUECOMPANY_ADDR1   varchar(75), \
     ISSUECOMPANY_ADDR2   varchar(75), \
     ISSUECOMPANY_ADDR3   varchar(75), \
     ISSUECOMPANY_TELNO   varchar(14), \
     REGISTERCD           varchar(8), \
     UPDATED              timestamp default current timestamp \
    ) in usr1dms index in idx1dms
alter table TEXT_ISSUECOMPANY_MST add constraint pk_issue_mst primary key(ISSUECOMPANY_CD)



insert into TEXT_ISSUECOMPANY_MST \
    select \
        ISSUECOMPANY_CD, \
        ISSUECOMPANY_NAME, \
        ISSUECOMPANY_ABBV, \
        ISSUECOMPANY_ZIPCD, \
        cast(null as varchar(2)), \
        ISSUECOMPANY_ADDR1, \
        ISSUECOMPANY_ADDR2, \
        ISSUECOMPANY_ADDR3, \
        ISSUECOMPANY_TELNO, \
        REGISTERCD, \
        UPDATED \
    from TEXT_ISSUECOMPANY_MST_OLD

