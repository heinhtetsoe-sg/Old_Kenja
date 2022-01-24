-- $Id: bcb5ff28e1fbb5c5b7623d11b7b7e6dedb2bc3a9 $

drop table tmp_CREDIT_MST

create table tmp_CREDIT_MST \
	(YEAR                 varchar(4) not null, \
	 COURSECD             varchar(1) not null, \
	 MAJORCD              varchar(3) not null, \
	 GRADE                varchar(2) not null, \
	 COURSECODE           varchar(4) not null, \
	 CLASSCD              varchar(2) not null, \
	 SUBCLASSCD           varchar(6) not null, \
	 CREDITS              smallint    , \
	 ABSENCE_HIGH         smallint    , \
	 ABSENCE_WARN         smallint    , \
	 REQUIRE_FLG          varchar(1)  , \
	 AUTHORIZE_FLG        varchar(1)  , \
	 COMP_UNCONDITION_FLG varchar(1)  , \
	 REGISTERCD           varchar(8)  , \
	 UPDATED              timestamp default current timestamp \
	) in usr1dms index in idx1dms

insert into tmp_CREDIT_MST \
select \
YEAR        , \
COURSECD    , \
MAJORCD     , \
GRADE       , \
COURSECODE  , \
CLASSCD     , \
SUBCLASSCD  , \
CREDITS     , \
ABSENCE_HIGH, \
cast(null as smallint), \
REQUIRE_FLG , \
cast(null as varchar(1)), \
cast(null as varchar(1)), \
REGISTERCD  , \
UPDATED      \
from \
CREDIT_MST 

drop table CREDIT_MST_OLD

rename table CREDIT_MST to CREDIT_MST_old

rename table tmp_CREDIT_MST to CREDIT_MST

alter table CREDIT_MST add constraint pk_CREDIT_MST primary key \
	(YEAR,COURSECD,MAJORCD,GRADE,COURSECODE,CLASSCD,SUBCLASSCD)


