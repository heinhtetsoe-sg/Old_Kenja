-- $Id: text_issuecompany_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table TEXT_ISSUECOMPANY_MST
create table TEXT_ISSUECOMPANY_MST ( \
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
