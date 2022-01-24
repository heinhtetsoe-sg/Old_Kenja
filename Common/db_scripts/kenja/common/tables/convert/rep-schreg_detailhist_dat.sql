-- $Id: 566c0fca0f3e85d77cb773a2ebc238c8e14c93e5 $
alter table SCHREG_DETAILHIST_DAT alter column REGISTERCD set data type varchar(10)
alter table SCHREG_DETAILHIST_DAT alter column CONTENT set data type varchar(1200)
alter table SCHREG_DETAILHIST_DAT alter column REMARK set data type varchar(210)

alter table SCHREG_DETAILHIST_DAT add column OCCURRENCE_DATE date
alter table SCHREG_DETAILHIST_DAT add column INVESTIGATION_DATE date
alter table SCHREG_DETAILHIST_DAT add column STD_GUID_MTG_DATE date
alter table SCHREG_DETAILHIST_DAT add column ORIGINAL_PLAN_CD varchar(2)
alter table SCHREG_DETAILHIST_DAT add column STAFF_MTG_DATE date
alter table SCHREG_DETAILHIST_DAT add column PUNISH_CD varchar(2)
alter table SCHREG_DETAILHIST_DAT add column OCCURRENCE_PLACE varchar(90)
alter table SCHREG_DETAILHIST_DAT add column DIARY_FLG varchar(1)
alter table SCHREG_DETAILHIST_DAT add column WRITTEN_OATH_FLG varchar(1)
alter table SCHREG_DETAILHIST_DAT add column REPORT_FLG varchar(1)
alter table SCHREG_DETAILHIST_DAT add column WRITTEN_STAFFCD varchar(10)
alter table SCHREG_DETAILHIST_DAT add column INVESTIGATION_STAFFCD1 varchar(10)
alter table SCHREG_DETAILHIST_DAT add column INVESTIGATION_STAFFCD2 varchar(10)
alter table SCHREG_DETAILHIST_DAT add column INVESTIGATION_STAFFCD3 varchar(10)
alter table SCHREG_DETAILHIST_DAT add column INVESTIGATION_STAFFCD4 varchar(10)

reorg table SCHREG_DETAILHIST_DAT
