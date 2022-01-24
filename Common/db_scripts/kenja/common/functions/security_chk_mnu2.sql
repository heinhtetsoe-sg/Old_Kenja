-- $Id: 3d4a2b664d20a35103d3db27f58c71256a9953a3 $
-- スクリプトの使用方法: db2 -f <このファイル>

-- 念の為削除。なければ DB21034E と SQL0458N のメッセージが出る。
drop function SECURITY_CHK_MNU2

CREATE FUNCTION SECURITY_CHK_MNU2   \
(   \
    IN_STAFFCD  varchar(10),   \
    IN_MENUID   varchar(10),   \
    IN_YEAR     varchar(4),  \
    IN_SCHOOLKIND varchar(2),  \
    IN_SCHOOLCD varchar(12)   \
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
                                   and t2.school_kind = IN_SCHOOLKIND \
                                   and t2.schoolcd = IN_SCHOOLCD \
                                inner join groupauth_dat t3 \
                                    on t2.groupcd = t3.groupcd \
                                   and t3.school_kind = IN_SCHOOLKIND \
                                   and t3.schoolcd = IN_SCHOOLCD \
                                inner join (SELECT \
                                                  SCHOOLCD \
                                                , SCHOOL_KIND \
                                                , MENUID \
                                            FROM \
                                                MENU_MST  \
                                            where \
                                                menuid = in_menuid \
                                            UNION  \
                                            SELECT \
                                                  SCHOOLCD \
                                                , SCHOOL_KIND \
                                                , MENUID \
                                            FROM \
                                                MENU_STAFF_MST  \
                                            WHERE \
                                                STAFFCD = in_staffcd \
                                                and menuid = in_menuid \
                                                ) t4  \
                                    on t3.menuid = t4.menuid \
                                   and t4.school_kind = IN_SCHOOLKIND \
                                   and t4.schoolcd = IN_SCHOOLCD \
                            where \
                                t1.staffcd = in_staffcd \
                                and t1.school_kind = IN_SCHOOLKIND \
                                and t1.schoolcd = IN_SCHOOLCD \
                          ); \
 \
        SET wk_userauth = (SELECT MIN(userauth)   \
                             FROM user_mst     T1,   \
                                  userauth_dat T2,   \
                                   (SELECT   \
                                        SCHOOLCD,   \
                                        SCHOOL_KIND,   \
                                        MENUID   \
                                    FROM   \
                                        MENU_MST   \
                                    UNION   \
                                    SELECT   \
                                        SCHOOLCD,   \
                                        SCHOOL_KIND,   \
                                        MENUID   \
                                    FROM   \
                                        MENU_STAFF_MST   \
                                    WHERE   \
                                        STAFFCD = in_staffcd) T3   \
                            WHERE T1.staffcd = T2.staffcd   \
                              AND T1.staffcd = in_staffcd   \
                              AND T2.menuid  = T3.menuid   \
                              AND T3.menuid  = in_menuid   \
                              AND T1.SCHOOL_KIND = IN_SCHOOLKIND  \
                              AND T2.SCHOOL_KIND = IN_SCHOOLKIND  \
                              AND T3.SCHOOL_KIND = IN_SCHOOLKIND  \
                              AND T1.SCHOOLCD = IN_SCHOOLCD  \
                              AND T2.SCHOOLCD = IN_SCHOOLCD  \
                              AND T3.SCHOOLCD = IN_SCHOOLCD  \
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