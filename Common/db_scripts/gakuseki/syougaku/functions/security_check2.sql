drop function db2inst1.security_check2

CREATE FUNCTION db2inst1.SECURITY_CHECK2 \ 
( \
	in_staffcd		varchar(6), \
	in_menuid		varchar(10) \
 ) RETURNS varchar(1) \
 READS SQL DATA \
 SPECIFIC SECURITY_CHECK2 \
 LANGUAGE SQL \
 NO EXTERNAL ACTION \
 DETERMINISTIC \
 BEGIN ATOMIC \
 DECLARE	wk_userauth		varchar(1); \
 DECLARE 	wk_groupauth		varchar(1); \
 DECLARE	wk_programid		VARCHAR(6); \
 DECLARE	wk_nonauth		VARCHAR(1) default '9'; \
 DECLARE	ret_val			varchar(1); \
 \
--���������ɵڤӼ¹Է���̾�˳������륰�롼�פθ��¼���-----------------------��
	SET wk_groupauth = (SELECT MIN(groupauth) FROM user_mst T1, usergroup_dat T2, groupauth_dat T3, menu_mst T4 \
			    WHERE T1.staffcd  = T2.staffcd \
	 		    AND  T1.staffcd   = in_staffcd \
	 		    AND  T2.groupcode = T3.groupcode \
	 		    AND  T3.menuid    = T4.menuid \
	 		    AND  T4.menuid    = in_menuid); \
--���������ɵڤӼ¹Է���̾�˳�������Ŀͤθ��¼���-----------------------��
	set wk_userauth = (SELECT MIN(userauth) \
			   FROM	db2inst1.user_mst T1, \
				db2inst1.userauth_dat T2, \
				db2inst1.menu_mst T3 \
			   WHERE T1.staffcd = T2.staffcd \
	 		   AND  T1.staffcd = in_staffcd \
	 		   AND  T2.menuid	= T3.menuid \
	 		   AND  T3.menuid	= in_menuid); \
--���롼�׵ڤӸĿͤθ��¤����------------------------------------------- \
	IF  (wk_userauth IS NOT NULL)	THEN \
		set ret_val = wk_userauth; \
	ELSEIF (wk_groupauth IS NOT NULL) THEN \
		set ret_val = wk_groupauth; \
	ELSE \
		set ret_val = wk_nonauth; \
	END IF; \
	Return ret_val; \
END \ 

