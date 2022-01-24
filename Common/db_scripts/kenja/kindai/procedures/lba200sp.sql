drop procedure lba200sp

CREATE PROCEDURE LBA200SP \
( \
     IN IN_userID        varchar(6) \
    ,IN IN_term_start        VARCHAR(10) \
    ,IN IN_term_end        VARCHAR(10) \
    ,IN IN_yymm              VARCHAR(10) \
    ,IN IN_year               VARCHAR(4) \
    ,IN IN_term              VARCHAR(1) \
    ,OUT OUT_RESULT          VARCHAR(2)        -- 処理結果 \
    ,OUT OUT_COUNT           integer          -- 処理件数 \
    ,OUT OUT_MESSAGE         VARCHAR(40)     -- エラーメッセージ \
) LANGUAGE SQL \
  BEGIN \
------------------------------------------------------------------------------ \
-- 内部変数宣言・定義 \
------------------------------------------------------------------------------ \
-- エラーステータス定義 \
        DECLARE STS_SUCCESS VARCHAR(1) default '0'; \
        DECLARE STS_LOCKED  VARCHAR(1) default '1'; \
        DECLARE STS_INVALID VARCHAR(1) default '9'; \
        DECLARE SQLSTATE char(5) default '00000'; \
        DECLARE at_error smallint default 0; \
-- 内部変数宣言 \
        DECLARE WK_OUT_COUNT          smallint; \
        DECLARE WK_rtimesum           smallint; \
        DECLARE WK_rtimepresent       smallint; \
        DECLARE WK_rtimeabsent        smallint; \
        DECLARE WK_rtimesuspend       smallint; \
        DECLARE WK_rtimemourning      smallint; \
        DECLARE WK_rtimesick          smallint; \
        DECLARE WK_rtimelate          smallint; \
        DECLARE WK_rtimenotice        smallint; \
        DECLARE WK_rtimenonotice      smallint; \
        DECLARE WK_timesum            smallint; \
        DECLARE WK_timepresent        smallint; \
        DECLARE WK_timeabsent         smallint; \
        DECLARE WK_timesuspend        smallint; \
        DECLARE WK_timemourning       smallint; \
        DECLARE WK_timesick           smallint; \
        DECLARE WK_timelate           smallint; \
        DECLARE WK_timenotice         smallint; \
        DECLARE WK_timenonotice       smallint; \
------------------------------------------------------------------------------ \
-- variables for each cursor \
------------------------------------------------------------------------------ \
        DECLARE v_schregno1     varchar(6); \
        DECLARE v_classcd1      varchar(2); \
        DECLARE v_subclasscd1   varchar(4); \
        DECLARE v_classhour1    integer; \
        DECLARE v_schregno2     varchar(6); \
        DECLARE v_classcd2      varchar(2); \
        DECLARE v_subclasscd2   varchar(4); \
        DECLARE v_classhour2    integer; \
        DECLARE v_schregno3     varchar(6); \
        DECLARE v_classcd3      varchar(2); \
        DECLARE v_subclasscd3   varchar(4); \
        DECLARE v_sum3          integer; \
        DECLARE v_absent3               integer; \
        DECLARE v_suspend3              integer; \
        DECLARE v_mourning3             integer; \
        DECLARE v_sick3                 integer; \
        DECLARE v_accidentnotice3       integer; \
        DECLARE v_noaccidentnotice3     integer; \
        DECLARE v_late3                 integer; \ 
        DECLARE v_schregno4     varchar(6); \
        DECLARE v_classcd4      varchar(2); \
        DECLARE v_subclasscd4   varchar(4); \
        DECLARE v_sum4          integer; \
        DECLARE v_absent4               integer; \
        DECLARE v_suspend4              integer; \
        DECLARE v_mourning4             integer; \
        DECLARE v_sick4                 integer; \
        DECLARE v_accidentnotice4       integer; \
        DECLARE v_noaccidentnotice4     integer; \
        DECLARE v_late4                 integer; \
------------------------------------------------------------------------------ \
-- カーソル定義 \
------------------------------------------------------------------------------ \
    DECLARE CUR_classhour_dat CURSOR FOR \
        SELECT \
            T1.schregno                         AS  schregno, \
            T3.classcd		                AS  classcd, \
            T3.subclasscd                       AS  subclasscd, \
            COALESCE(T3.credit,0) * COALESCE(T4.classweeks,0) AS  classhour \
        FROM \
            schreg_regd_dat      T1, \
            schreg_base_mst      T2, \
            credityear_dat      T3, \
            classweek_dat      T4 \
        WHERE T1.year               =   IN_year \
          AND T2.schregno           =   T1.schregno \
          AND T3.credityear         =   T1.year \
          AND T3.coursecd           =   T2.coursecd \
          AND T3.majorcd            =   T2.majorcd \
          AND T3.grade              =   T1.grade \
          AND T3.course             =   T1.coursecode \
          AND T4.year               =   T1.year \
          AND T4.semester           =   IN_term \
          AND T4.grade              =   T1.grade \
          AND T4.hr_class           =   T1.hr_class \
    	ORDER BY T1.schregno, T3.classcd, T3.subclasscd; \
------------------------------------------------------------------------------ \
    DECLARE CUR_schedule_dat2 CURSOR FOR \
        SELECT \
            T2.schregno                  AS  schregno, \
            T1.classcd                   AS  classcd, \
            T1.subclasscd                AS  subclasscd, \
            SUM(1)                       AS  classhour \
        FROM schedule_dat        T1, \
             attendclass_dat     T2 \
        WHERE T1.executedate           >=  DATE(IN_term_start) \
          AND T1.executedate           <=  DATE(IN_term_end) \
          AND T1.periodcd         >=  '2' \
          AND T2.year               =   T1.year \
          AND T2.attendclasscd   =   T1.attendclasscd \
		  AND NOT EXISTS \
          ( \
            SELECT * FROM attend_dat T3 \
            WHERE T3.schregno      =    T2.schregno \
              AND T3.attenddate      =    T1.executedate \
              AND T3.periodcd    =    '1' \
              AND T3.di_cd    >=   '1' \
              AND T3.di_cd    <=   '6' \
          ) \
        GROUP BY T2.schregno, T1.classcd, T1.subclasscd \
        ORDER BY T2.schregno, T1.classcd, T1.subclasscd; \
------------------------------------------------------------------------------ \
    DECLARE CUR_attend_dat1 CURSOR FOR \ 
        SELECT \
            T1.schregno, \
            T1.classcd, \
            T1.subclasscd, \
            SUM(CASE T1.di_cd WHEN '0' THEN 0 ELSE 1 END)          AS  sum, \
            SUM(CASE T1.di_cd WHEN '1' THEN 1 ELSE 0 END)          AS  absent, \
            SUM(CASE T1.di_cd WHEN '2' THEN 1 ELSE 0 END)          AS  suspend, \
            SUM(CASE T1.di_cd WHEN '3' THEN 1 ELSE 0 END)          AS  mourning, \ 
            SUM(CASE T1.di_cd WHEN '4' THEN 1 ELSE 0 END)          AS  sick, \
            SUM(CASE T1.di_cd WHEN '5' THEN 1 ELSE 0 END)          AS  accidentnotice, \
            SUM(CASE T1.di_cd WHEN '6' THEN 1 ELSE 0 END)          AS  noaccidentnotice, \
            SUM(CASE T1.di_cd WHEN '7' THEN 1 ELSE 0 END)          AS  late \
        FROM attend_dat T1 \
        WHERE T1.attenddate       >=  DATE(IN_term_start) \
          AND T1.attenddate       <=  DATE(IN_term_end) \
          AND T1.periodcd     >=  '2' \
		  AND NOT EXISTS \
          ( \
            SELECT * FROM attend_dat T2 \
            WHERE T2.schregno      =    T1.schregno \
              AND T2.attenddate      =    T1.attenddate \
              AND T2.periodcd    =    '1' \
              AND T2.di_cd    >=   '1' \
              AND T2.di_cd    <=   '6' \
          ) \
        GROUP BY T1.schregno, T1.classcd, T1.subclasscd \
        ORDER BY schregno, classcd, subclasscd; \
------------------------------------------------------------------------------ \
    DECLARE CUR_attend_dat2 CURSOR FOR \
        SELECT \
            schregno, \
            classcd, \
            subclasscd, \
            SUM(CASE di_cd WHEN '0' THEN 0 ELSE 1 END)          AS  sum, \
            SUM(CASE di_cd WHEN '1' THEN 1 ELSE 0 END)          AS  absent, \
            SUM(CASE di_cd WHEN '2' THEN 1 ELSE 0 END)          AS  suspend, \
            SUM(CASE di_cd WHEN '3' THEN 1 ELSE 0 END)          AS  mourning, \
            SUM(CASE di_cd WHEN '4' THEN 1 ELSE 0 END)          AS  sick, \
            SUM(CASE di_cd WHEN '5' THEN 1 ELSE 0 END)          AS  accidentnotice, \
            SUM(CASE di_cd WHEN '6' THEN 1 ELSE 0 END)          AS  noaccidentnotice, \
            SUM(CASE di_cd WHEN '7' THEN 1 ELSE 0 END)          AS  late \
        FROM attend_dat \
        WHERE attenddate       >=  DATE(IN_term_start) \
          AND attenddate       <=  DATE(IN_term_end) \
          AND periodcd     >=  '2' \
        GROUP BY schregno, classcd, subclasscd \
        ORDER BY schregno, classcd, subclasscd; \
------------------------------------------------------------------------------ \
-- エラー処理 \
------------------------------------------------------------------------------ \
        BEGIN \
-- 例外定義 \
        DECLARE not_found CONDITION FOR SQLSTATE '02000'; \
        DECLARE CONTINUE HANDLER FOR not_found SET at_error = 2; \
        DECLARE EXIT HANDLER FOR SQLEXCEPTION \
        IF at_error = 1 THEN \
                IF SQLSTATE = '40001' THEN \
                        SET OUT_RESULT  = STS_LOCKED; \
                        SET OUT_COUNT   = 0; \
                -- その他 \
                ELSE \
                        SET OUT_RESULT  = STS_INVALID; \
                        SET OUT_COUNT   = 0; \
                        SET OUT_MESSAGE = 'other error'; \
                END IF; \
        END IF; \
        END; \
------------------------------------------------------------------------------ \
-- 処理開始 \
------------------------------------------------------------------------------ \
    -- 初期処理 \
    SET OUT_RESULT      = STS_INVALID; \
    SET OUT_COUNT       = 0; \
    SEt WK_OUT_COUNT    = 0; \
  --既存データの削除 \
  DELETE FROM attend_subclass_dat \
    WHERE year = IN_year \
      AND semester = IN_term; \
  -- カーソルのオープン \
  OPEN CUR_schedule_dat2; \
  FETCH CUR_schedule_dat2   INTO    v_schregno2, v_classcd2, v_subclasscd2, v_classhour2; \
  OPEN CUR_classhour_dat; \
  FETCH CUR_classhour_dat  INTO    v_schregno1, v_classcd1, v_subclasscd1, v_classhour1; \
  OPEN CUR_attend_dat1; \
  FETCH CUR_attend_dat1     INTO   v_schregno3, v_classcd3, v_subclasscd3, v_sum3, v_absent3, \
				   v_suspend3, v_mourning3, v_sick3, v_accidentnotice3, \
				　 v_noaccidentnotice3, v_late3; \
  OPEN CUR_attend_dat2; \
  FETCH CUR_attend_dat2     INTO   v_schregno4, v_classcd4, v_subclasscd4, v_sum4, v_absent4, \
                                   v_suspend4, v_mourning4, v_sick4, v_accidentnotice4, \
                                　 v_noaccidentnotice4, v_late4; \
-- 更新処理 \
  main_loop: FOR for1 AS \
        SELECT \
            T2.schregno                  AS  schregno, \
            T1.classcd                   AS  classcd, \
            T1.subclasscd                AS  subclasscd, \
            SUM(1)                       AS  classhour \
        FROM schedule_dat        T1, \
             attendclass_dat     T2 \
        WHERE T1.executedate           >=  DATE(IN_term_start) \
          AND T1.executedate           <=  DATE(IN_term_end) \
          AND T1.periodcd         >=  '2' \
          AND T2.year               =   T1.year \
          AND T2.attendclasscd   =   T1.attendclasscd \
        GROUP BY T2.schregno, T1.classcd, T1.subclasscd \
        ORDER BY T2.schregno, T1.classcd, T1.subclasscd \
	DO \
        SET WK_rtimesum           =  0; \
        SET WK_rtimepresent       =  0; \
        SET WK_rtimeabsent        =  0; \
        SET WK_rtimesuspend       =  0; \
        SET WK_rtimemourning      =  0; \
        SET WK_rtimesick          =  0; \
        SET WK_rtimelate          =  0; \
        SET WK_rtimenotice        =  0; \
        SET WK_rtimenonotice      =  0; \
        SET WK_rtimesum           =  0; \
        SET WK_rtimepresent       =  0; \
        SET WK_rtimeabsent        =  0; \
        SET WK_rtimesuspend       =  0; \
        SET WK_rtimemourning      =  0; \
        SET WK_rtimesick          =  0; \
        SET WK_rtimelate          =  0; \
        SET WK_rtimenotice        =  0; \
        SET WK_rtimenonotice      =  0; \  
  -- 出欠データから出欠科目別累積データを作成 \
        ---------------------------- \
        --時間割データ2の検索 \
        ---------------------------- \
        loop1: LOOP \
            IF at_error = 2 THEN \
                SET WK_rtimepresent = 0; \
                LEAVE loop1; \
            ELSEIF for1.schregno || for1.subclasscd < v_schregno2 || v_subclasscd2 THEN \
                SET WK_rtimepresent =  0; \
                LEAVE loop1; \
            ELSEIF for1.schregno || for1.subclasscd = v_schregno2 || v_subclasscd2 THEN \
                SET  WK_rtimepresent = v_classhour2; \
                LEAVE loop1; \
            ELSEIF for1.schregno || for1.subclasscd > v_schregno2 || v_subclasscd2 THEN \
              FETCH CUR_schedule_dat2   INTO v_schregno2, v_classcd2, \
					     v_subclasscd2, v_classhour2; \
            END IF; \
        END LOOP loop1; \
        ---------------------------- \
        --授業時数データの検索 \
        ---------------------------- \
        loop2: LOOP \
            IF at_error = 2 THEN \
                SET WK_timepresent = v_classhour1; \
                LEAVE loop2; \
            ELSEIF for1.schregno || for1.subclasscd < v_schregno1 || v_subclasscd1 THEN \
                SET WK_timepresent = v_classhour1; \
                LEAVE loop2; \
            ELSEIF for1.schregno || for1.subclasscd = v_schregno1 || v_subclasscd1 THEN \
                SET WK_timepresent = v_classhour1; \
                LEAVE loop2; \
            ELSEIF for1.schregno || for1.subclasscd > v_schregno1 || v_subclasscd1 THEN \
              FETCH CUR_classhour_dat  INTO v_schregno1, v_classcd1, \
					     v_subclasscd1, v_classhour1; \
            END IF; \
        END LOOP loop2; \
        ---------------------------- \
        --出欠データ1の検索 \
        ---------------------------- \
        loop3: LOOP \
            IF at_error = 2 THEN \
                SET WK_rtimesum      =  0; \
                SET WK_rtimeabsent   =  0; \
                SET WK_rtimesuspend  =  0; \
                SET WK_rtimemourning =  0; \
                SET WK_rtimesick     =  0; \
                SET WK_rtimelate     =  0; \
                SET WK_rtimenotice   =  0; \
                SET WK_rtimenonotice =  0; \
                LEAVE loop3; \
            ELSEIF for1.schregno || for1.subclasscd < v_schregno3 || v_subclasscd3 THEN \
                SET WK_rtimesum    = 0; \
                SET WK_rtimeabsent = 0; \
                SET WK_rtimesuspend = 0; \
                SET WK_rtimemourning = 0; \
                SET WK_rtimesick     =  0; \
                SET WK_rtimelate =  0; \
                SET WK_rtimenotice   =  0; \
                SET WK_rtimenonotice   =  0; \                
                LEAVE loop3; \
            ELSEIF for1.schregno || for1.subclasscd = v_schregno3 || v_subclasscd3 THEN \
                SET WK_rtimesum       =  v_sum3; \
                SET WK_rtimeabsent       =  v_absent3; \
                SET WK_rtimesuspend       =  v_suspend3; \
                SET WK_rtimemourning       =  v_mourning3; \
                SET WK_rtimesick       =  v_sick3; \
                SET WK_rtimelate       =  v_late3; \
                SET WK_rtimenotice     =  v_accidentnotice3; \
                SET WK_rtimenonotice   =  v_noaccidentnotice3; \
                LEAVE loop3; \
            ELSEIF for1.schregno || for1.subclasscd > v_schregno3 || v_subclasscd3 THEN \
   	    FETCH CUR_attend_dat1   INTO v_schregno3, v_classcd3, v_subclasscd3, v_sum3, \
                                         v_absent3,v_suspend3, v_mourning3, v_sick3, \
                                         v_accidentnotice3, v_noaccidentnotice3, v_late3; \
            END IF; \
        END LOOP loop3; \
        ---------------------------- \
        --出欠データ2の検索 \
        ---------------------------- \
        loop4: LOOP \
            IF at_error = 2 THEN \
                SET WK_timesum      =  0; \
                SET WK_timeabsent   =  0; \
                SET WK_timesuspend  =  0; \
                SET WK_timemourning =  0; \
                SET WK_timesick     =  0; \
                SET WK_timelate     =  0; \
                SET WK_timenotice   =  0; \
                SET WK_timenonotice   =  0; \
                LEAVE loop4; \
            ELSEIF for1.schregno || for1.subclasscd < v_schregno4 || v_subclasscd4 THEN \
                SET WK_timesum      =  0; \
                SET WK_timeabsent   =  0; \
                SET WK_timesuspend  =  0; \
                SET WK_timemourning =  0; \
                SET WK_timesick     =  0; \
                SET WK_timelate     =  0; \
                SET WK_timenotice   =  0; \
                SET WK_timenonotice   =  0; \
                LEAVE loop4; \
            ELSEIF for1.schregno || for1.subclasscd = v_schregno4 || v_subclasscd4 THEN \
                SET WK_timesum       =  v_sum4; \
                SET WK_timeabsent       =  v_absent4; \
                SET WK_timesuspend       =  v_suspend4; \
                SET WK_timemourning       =  v_mourning4; \
                SET WK_timesick       =  v_sick4; \
                SET WK_timelate       =  v_late4; \
                SET WK_timenotice     =  v_accidentnotice4; \
                SET WK_timenonotice   =  v_noaccidentnotice4; \
                LEAVE loop4; \
            ELSEIF for1.schregno || for1.subclasscd > v_schregno4 || v_subclasscd4 THEN \
  	FETCH CUR_attend_dat2   INTO v_schregno4, v_classcd4, v_subclasscd4, v_sum4, \
                                 v_absent4,v_suspend4, v_mourning4, v_sick4, \
                                 v_accidentnotice4, v_noaccidentnotice4, v_late4; \
            END IF; \
        END LOOP loop4; \
        ---------------------------- \
        --更新主処理 \
        ---------------------------- \
        INSERT INTO attend_subclass_dat( \
		YEAR, SEMESTER, classcd, SUBCLASSCD, SCHREGNO, \
		ATTENDMONTH, A_PRESENT, A_ABSENT, A_SUSPEND, \
		A_MOURNING, A_SICK, A_LATE, A_NOTICE, A_NONOTICE, \
		PRESENT, ABSENT, SUSPEND, MOURNING, SICK, LATE, NOTICE, NONOTICE, UPDATED) \
        VALUES ( \
            IN_year, \
            IN_term, \
            for1.classcd, \
            for1.subclasscd, \
            for1.schregno, \
            IN_yymm, \
            WK_rtimepresent  -  WK_rtimesum, \
            WK_rtimeabsent, \
            WK_rtimesuspend, \
            WK_rtimemourning, \
            WK_rtimesick, \
            WK_rtimelate, \
            WK_rtimenotice, \
            WK_rtimenonotice, \
            WK_timepresent     -   WK_timesum, \
            WK_timeabsent, \
            WK_timesuspend, \
            WK_timemourning, \
            WK_timesick, \
            WK_timelate, \
            WK_timenotice, \
            WK_timenonotice, \
            SYSDATE()); \
    -- 学籍番号，出力件数のカウントアップ \
        SET WK_OUT_COUNT = WK_OUT_COUNT + 1; \
    END FOR main_loop; \
    CLOSE CUR_attend_dat1; \
    CLOSE CUR_attend_dat2; \
    CLOSE CUR_schedule_dat2; \
    -- 正常終了 \
    SET OUT_RESULT      = STS_SUCCESS; \
    SET OUT_COUNT       = WK_OUT_COUNT; \
END 

