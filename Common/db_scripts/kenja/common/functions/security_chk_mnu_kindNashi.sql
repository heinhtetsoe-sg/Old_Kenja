-- $Id: security_chk_mnu.sql 65564 2019-02-07 08:09:11Z yamashiro $
-- スクリプトの使用方法: db2 -f <このファイル>

-- 念の為削除。なければ DB21034E と SQL0458N のメッセージが出る。
drop function SECURITY_CHK_MNU

CREATE FUNCTION SECURITY_CHK_MNU  \
(  \
    IN_STAFFCD  varchar(10),  \
    IN_MENUID   varchar(10),  \
    IN_YEAR     varchar(4)  \
 ) RETURNS varchar(1)  \
 READS SQL DATA  \
 SPECIFIC SECURITY_CHK_MNU  \
 LANGUAGE SQL  \
 NO EXTERNAL ACTION  \
 DETERMINISTIC  \
 BEGIN ATOMIC  \
 DECLARE wk_userauth    varchar(1); \
 DECLARE wk_groupauth   varchar(1); \
 DECLARE wk_programid   varchar(6); \
 DECLARE wk_nonauth     varchar(1) default '9'; \
 DECLARE ret_val        varchar(1); \
 \
 \
        SET wk_groupauth = (SELECT \
                                  MIN(GROUPAUTH)  \
                            FROM \
                                USER_MST T1  \
                                INNER JOIN USERGROUP_DAT T2  \
                                    ON T1.STAFFCD = T2.STAFFCD  \
                                    AND T2.YEAR = in_year \
                                INNER JOIN GROUPAUTH_DAT T3  \
                                    ON T2.GROUPCD = T3.GROUPCD  \
                                INNER JOIN MENU_MST T4  \
                                    ON T3.MENUID = T4.MENUID  \
                                    AND T4.menuid =  in_menuid  \
                            WHERE \
                                T1.STAFFCD = in_staffcd  \
                                  \
                           ); \
                            \
        SET wk_userauth = (SELECT MIN(userauth)  \
                             FROM user_mst     T1,  \
                                  userauth_dat T2,  \
                                  menu_mst     T3  \
                            WHERE T1.staffcd = T2.staffcd  \
                              AND T1.staffcd = in_staffcd  \
                              AND T2.menuid  = T3.menuid  \
                              AND T3.menuid  = in_menuid  \
                          ); \
  \
        IF (wk_userauth IS NOT NULL)    THEN  \
           SET ret_val = wk_userauth; \
        ELSEIF (wk_groupauth IS NOT NULL) THEN  \
           SET ret_val = wk_groupauth; \
        ELSE  \
           SET ret_val = wk_nonauth; \
        END IF; \
        Return ret_val; \
END
