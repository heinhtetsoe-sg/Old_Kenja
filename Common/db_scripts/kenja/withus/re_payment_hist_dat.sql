-- kanji=漢字
-- $Id: re_payment_hist_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop table RE_PAYMENT_HIST_DAT

create table RE_PAYMENT_HIST_DAT \
	(APPLICANTNO varchar(7) not null, \
     RE_PAY_DATE date not null, \
     RE_PAY_DIV  varchar(2) not null, \
     INQUIRY_NO  varchar(6) not null, \
     RE_PAYMENT  integer, \
	 REGISTERCD  varchar(8)  , \
	 UPDATED     timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table RE_PAYMENT_HIST_DAT add constraint PK_RE_PAYMENT_DAT primary key \
      (APPLICANTNO, RE_PAY_DATE, RE_PAY_DIV, INQUIRY_NO)
