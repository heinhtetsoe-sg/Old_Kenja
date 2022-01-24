drop function db2inst1.meisyou_get

CREATE FUNCTION db2inst1.Meisyou_Get \
( \
	in_namecode	varchar(4), \
	in_namecd	varchar(4), \
	in_pos		integer \
) RETURNS varchar(40) \
 READS SQL DATA \
 SPECIFIC Meisyou_get \
 LANGUAGE SQL \
 NO EXTERNAL ACTION \
 DETERMINISTIC \
 BEGIN ATOMIC \
--プロシジャ内にて変数を利用する場合に定義---------------------------------- \
	/* 作業用変数定義 */ \
 DECLARE	wk_name1	varchar(40); \
 DECLARE	wk_name2	varchar(40); \
 DECLARE	wk_name3	varchar(40); \
 DECLARE	wk_abbv1	varchar(20); \
 DECLARE	wk_abbv2	varchar(20); \
 DECLARE	wk_abbv3	varchar(20); \
 DECLARE	wk_namespare1	varchar(20); \
 DECLARE	wk_namespare2	varchar(20); \
 DECLARE	wk_namespare3	varchar(20); \
 DECLARE	ret_val		varchar(40); \ 
\
--EXEPTION_INITプラグマを利用する場合に定義--------------------------------- \
--	OTHER_USER_LOCKED EXCEPTION; \
--	PRAGMA EXCEPTION_INIT(OTHER_USER_LOCKED,-00054); \
--READのみの指定------------------------------------------------------------- \
--	SET TRANSACTION READ ONLY; \
--データの読み込み------------------------------------------------------------- \
	set wk_name1 = (select name1 from name_mst where namecd1 = in_namecd and namecd2 = in_namecode); \ 
	set wk_name2 = (SELECT name2 FROM name_mst WHERE namecd1 = in_namecd AND namecd2 = in_namecode); \
	set wk_name3 = (SELECT name3 FROM name_mst WHERE namecd1 = in_namecd AND namecd2 = in_namecode); \
	set wk_abbv1 = (SELECT abbv1 FROM name_mst WHERE namecd1 = in_namecd AND namecd2 = in_namecode); \
	set wk_abbv2 = (SELECT abbv2 FROM name_mst WHERE namecd1 = in_namecd AND namecd2 = in_namecode); \
	set wk_abbv3 = (SELECT abbv3 FROM name_mst WHERE namecd1 = in_namecd AND namecd2 = in_namecode); \
	set wk_namespare1 = (SELECT namespare1 FROM db2inst1.name_mst WHERE namecd1 = in_namecd AND namecd2 = in_namecode); \
	set wk_namespare2 = (SELECT namespare2 FROM db2inst1.name_mst WHERE namecd1 = in_namecd AND namecd2 = in_namecode); \
	set wk_namespare3 = (SELECT namespare3 FROM db2inst1.name_mst WHERE namecd1 = in_namecd AND namecd2 = in_namecode); \
--戻す内容------------------------------------------------------------- \
	IF in_pos = 1 THEN \
		set ret_val = wk_name1; \
	ELSEIF in_pos =	2 THEN \
		set ret_val = wk_name2; \
	ELSEIF in_pos = 3 THEN \
		set ret_val = wk_name3; \
	ELSEIF in_pos = 4 THEN \
		set ret_val = wk_abbv1; \
	ELSEIF in_pos = 5 THEN \
		set ret_val = wk_abbv2; \
	ELSEIF in_pos =  6 THEN \
		set ret_val = wk_abbv3; \
	ELSEIF in_pos = 7 THEN \
		set ret_val = wk_namespare1; \
	ELSEIF in_pos = 8 THEN \
		set ret_val = wk_namespare2; \
	ELSEIF in_pos = 9 THEN \
		set ret_val = wk_namespare3; \
	END IF; \
	RETURN ret_val; \
--EXCEPTION
--	WHEN NO_DATA_FOUND THEN
--		RETURN	'';
END

