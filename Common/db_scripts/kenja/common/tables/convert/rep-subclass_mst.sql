-- $Id: 8f8e5d25d41ecfd8861d76c0e250170da0a4f32d $

alter table SUBCLASS_MST alter column SUBCLASSORDERNAME2 set data type VARCHAR(150)
alter table SUBCLASS_MST alter column SUBCLASSORDERNAME3 set data type VARCHAR(150)

reorg table SUBCLASS_MST
