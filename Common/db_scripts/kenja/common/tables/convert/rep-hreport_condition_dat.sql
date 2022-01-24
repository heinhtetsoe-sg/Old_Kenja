-- $Id$

alter table HREPORT_CONDITION_DAT alter column REMARK10 set data type varchar(1500)

reorg table HREPORT_CONDITION_DAT
