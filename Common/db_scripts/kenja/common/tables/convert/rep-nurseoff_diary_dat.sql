alter table NURSEOFF_DIARY_DAT alter column EVENT set data type varchar(1000)
alter table NURSEOFF_DIARY_DAT add column HUMIDITY varchar(3)
alter table NURSEOFF_DIARY_DAT add column CHECK_HOUR varchar(2)
alter table NURSEOFF_DIARY_DAT add column CHECK_MINUTE varchar(2)
alter table NURSEOFF_DIARY_DAT add column COLOR varchar(2)
alter table NURSEOFF_DIARY_DAT add column TURBIDITY varchar(2)
alter table NURSEOFF_DIARY_DAT add column SMELL varchar(2)
alter table NURSEOFF_DIARY_DAT add column TASTE varchar(2)
alter table NURSEOFF_DIARY_DAT add column RESIDUAL_CHLORINE varchar(4)
alter table NURSEOFF_DIARY_DAT add column WATER_REMARK varchar(15)
alter table NURSEOFF_DIARY_DAT add column AED varchar(2)

reorg table NURSEOFF_DIARY_DAT
