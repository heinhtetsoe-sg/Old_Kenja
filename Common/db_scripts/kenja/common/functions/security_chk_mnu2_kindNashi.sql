-- $Id: security_chk_mnu2.sql 65564 2019-02-07 08:09:11Z yamashiro $
-- スクリプトの使用方法: db2 -f <このファイル>

-- 念の為削除。なければ DB21034E と SQL0458N のメッセージが出る。
drop function SECURITY_CHK_MNU2

CREATE FUNCTION SECURITY_CHK_MNU2   \
(   \
    IN_STAFFCD  varchar(10),   \
    IN_MENUID   varchar(10),   \
    IN_YEAR     varchar(4)  \
 ) RETURNS varchar(1)   \
 READS SQL DATA   \
 SPECIFIC SECURITY_CHK_MNU2   \
 LANGUAGE SQL   \
 NO EXTERNAL ACTION   \
 DETERMINISTIC   \
 BEGIN ATOMIC   \
 DECLARE wk_userauth    varchar(1); \
 DECLARE wk_groupauth   varchar(1); \
 DECLARE wk_programid   varchar(6); \
 DECLARE wk_nonauth     varchar(1) default '9'; \
 DECLARE ret_val        varchar(1); \
   \
        SET wk_groupauth = (select \
                                min(groupauth) \
                            from \
                                user_mst t1 \
                                inner join usergroup_dat t2  \
                                    on t1.staffcd = t2.staffcd \
                                   and t2.year = in_year \
                                inner join groupauth_dat t3 \
                                    on t2.groupcd = t3.groupcd \
                                inner join (SELECT \
                                                MENUID \
                                            FROM \
                                                MENU_MST  \
                                            where \
                                                menuid = in_menuid \
                                            UNION  \
                                            SELECT \
                                                MENUID \
                                            FROM \
                                                MENU_STAFF_MST  \
                                            WHERE \
                                                STAFFCD = in_staffcd \
                                                and menuid = in_menuid \
                                                ) t4  \
                                    on t3.menuid = t4.menuid \
                            where \
                                t1.staffcd = in_staffcd \
                          ); \
 \
        SET wk_userauth = (SELECT MIN(userauth)   \
                             FROM user_mst     T1,   \
                                  userauth_dat T2,   \
                                   (SELECT   \
                                        MENUID   \
                                    FROM   \
                                        MENU_MST   \
                                    UNION   \
                                    SELECT   \
                                        MENUID   \
                                    FROM   \
                                        MENU_STAFF_MST   \
                                    WHERE   \
                                        STAFFCD = in_staffcd) T3   \
                            WHERE T1.staffcd = T2.staffcd   \
                              AND T1.staffcd = in_staffcd   \
                              AND T2.menuid  = T3.menuid   \
                              AND T3.menuid  = in_menuid   \
                          ); \
                           \
        IF (wk_userauth IS NOT NULL)    THEN   \
           SET ret_val = wk_userauth; \
        ELSEIF (wk_groupauth IS NOT NULL) THEN   \
           SET ret_val = wk_groupauth; \
        ELSE   \
           SET ret_val = wk_nonauth; \
        END IF; \
        Return ret_val; \
END