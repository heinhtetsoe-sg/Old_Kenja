drop function db2inst1.security_check

CREATE FUNCTION db2inst1.SECURITY_CHECK \ 
( \
	in_staffcd		varchar(6), \
	in_exename		varchar(20) \
 ) RETURNS varchar(1) \
 READS SQL DATA \
 SPECIFIC SECURITY_CHECK \
 LANGUAGE SQL \
 NO EXTERNAL ACTION \
 DETERMINISTIC \
 BEGIN ATOMIC \
 DECLARE	wk_userauth		varchar(1); \
 DECLARE 	wk_groupauth		varchar(1); \
 DECLARE	wk_exename		varchar(20); \
 DECLARE	wk_programid		VARCHAR(6); \
 DECLARE	wk_nonauth		VARCHAR(1) default '9'; \
 DECLARE	ret_val			varchar(1); \
 \
	set wk_exename  = replace(UPPER(in_exename),'.EXE',''); \
--職員コード及び実行形式名に該当するグループの権限取得-----------------------↓
	SET wk_groupauth = (SELECT MIN(groupauth) FROM user_mst T1, usergroup_dat T2, groupauth_dat T3, menu_mst T4 \
			    WHERE T1.staffcd  = T2.staffcd \
	 		    AND  T1.staffcd   = in_staffcd \
	 		    AND  T2.groupcode = T3.groupcode \
	 		    AND  T3.menuid    = T4.menuid \
	 		    AND  T4.exename   = wk_exename); \
--職員コード及び実行形式名に該当する個人の権限取得-----------------------↓
	set wk_userauth = (SELECT MIN(userauth) \
			   FROM	db2inst1.user_mst T1, \
				db2inst1.userauth_dat T2, \
				db2inst1.menu_mst T3 \
			   WHERE T1.staffcd = T2.staffcd \
	 		   AND  T1.staffcd = in_staffcd \
	 		   AND  T2.menuid	= T3.menuid \
	 		   AND  T3.exename	= wk_exename); \
--グループ及び個人の権限を比較------------------------------------------- \
	IF  (wk_userauth IS NOT NULL)	THEN \
--利用者権\
		set ret_val = wk_userauth; \
	ELSEIF (wk_groupauth IS NOT NULL) THEN \
--グループ権限 \
		set ret_val = wk_groupauth; \
	ELSE \
--権限無し \
		set ret_val = wk_nonauth; \
	END IF; \
	Return ret_val; \
END \ 

