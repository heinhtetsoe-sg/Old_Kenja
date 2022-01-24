drop procedure lbd030sp \

CREATE PROCEDURE LBD030SP \
( \
     IN IN_userID	       varchar(6) \ 	
    ,IN IN_term_start          VARCHAR(10) \
    ,IN IN_term_end            varchar(10) \
    ,IN IN_yymm                varchar(4) \
    ,IN IN_classcd             varchar(2) \
    ,IN IN_subclasscd          varchar(4) \
    ,IN IN_year                varchar(4) \
    ,IN IN_term                varchar(1) \
    ,IN IN_attendclasscd       varchar(4) \
    ,OUT OUT_RESULT            varchar(5)         -- 処理結果 \
    ,OUT OUT_COUNT             integer          -- 処理件数 \
    ,OUT OUT_MESSAGE           varchar(40)         -- エラーメッセージ \
) LANGUAGE SQL \
  BEGIN \
------------------------------------------------------------------------------ \
-- 内部変数宣言・定義 \
------------------------------------------------------------------------------ \
-- variables for cursor \
--    REC_時間割データ2       CUR_時間割データ2%ROWTYPE; \
        DECLARE v_schregno2     varchar(6); \
        DECLARE v_classhour2    integer; \
--    REC_出欠データ1     CUR_出欠データ1%ROWTYPE; \
        DECLARE v_schregno3             varchar(6); \
        DECLARE v_sum3                  smallint; \
        DECLARE v_absent3               smallint; \
        DECLARE v_suspend3              smallint; \
        DECLARE v_mourning3             smallint; \
        DECLARE v_sick3                 smallint; \
        DECLARE v_accidentnotice3       smallint; \
        DECLARE v_noaccidentnotice3     smallint; \
        DECLARE v_late3                 smallint; \
--    REC_出欠データ2     CUR_出欠データ2%ROWTYPE; \
        DECLARE v_schregno4             varchar(6); \
        DECLARE v_sum4                  smallint; \
        DECLARE v_absent4               smallint; \
        DECLARE v_suspend4              smallint; \
        DECLARE v_mourning4             smallint; \
        DECLARE v_sick4                 smallint; \
        DECLARE v_accidentnotice4       smallint; \
        DECLARE v_noaccidentnotice4     smallint; \
        DECLARE v_late4                 smallint; \
-- エラーステータス定義 \
        DECLARE at_error    smallint   default 0; \
        DECLARE SQLSTATE    char(5)    default '00000'; \
        DECLARE STS_SUCCESS VARCHAR(1) default '0'; \
        DECLARE STS_LOCKED  VARCHAR(1) default '1'; \
        DECLARE STS_INVALID VARCHAR(1) default '9'; \
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
-- カーソル定義 \
------------------------------------------------------------------------------ \
   DECLARE CUR_schedule_dat2 CURSOR FOR \
        SELECT  \
            T2.schregno                     AS  schregno, \
            SUM(1)                          AS  classhour \
        FROM \
            schedule_dat        T1, \
            attendclass_dat     T2 \
        WHERE T1.executedate           >=  DATE(IN_term_start) \
            AND T1.executedate         <=  DATE(IN_term_end) \
            AND T1.periodcd      >=  '2' \
            AND T1.classcd        =   IN_classcd \
            AND T1.subclasscd     =   IN_subclasscd \
            AND T2.year           =   T1.year \
            AND T2.attendclasscd  =   T1.attendclasscd \
            AND T2.schregno         IN \
            ( \
             SELECT schregno FROM attendclass_dat \
             WHERE year            =   IN_year \
               AND attendclasscd   =   IN_attendclasscd \
            ) \
            AND char(DATE(T1.executedate)) || T2.schregno NOT IN \
            ( \
                SELECT \
                    char(DATE(T3.attenddate)) || T3.schregno \
                FROM attend_dat T3 \
                WHERE T3.schregno    =    T2.schregno \
                  AND T3.attenddate >=   DATE(IN_term_start) \ 
                  AND T3.attenddate <=   DATE(IN_term_end) \
                  AND T3.periodcd    =    '1' \
                  AND T3.di_cd      >=   '1' \
                  AND T3.di_cd      <=   '6' \
            ) \
        GROUP BY IN_yymm, T2.schregno \
        ORDER BY IN_yymm, T2.schregno; \
------------------------------------------------------------------------------ \
    DECLARE CUR_attend_dat1 CURSOR FOR \
        SELECT \
            T1.schregno, \
            SUM(CASE T1.di_cd WHEN '0' THEN 0 ELSE 1 END)          AS  sum, \
            SUM(CASE T1.di_cd WHEN '1' THEN 1 ELSE 1 END)          AS  absent, \
            SUM(CASE T1.di_cd WHEN '2' THEN 1 ELSE 1 END)          AS  suspend, \
            SUM(CASE T1.di_cd WHEN '3' THEN 1 ELSE 1 END)          AS  mourning, \
            SUM(CASE T1.di_cd WHEN '4' THEN 1 ELSE 1 END)          AS  sick, \
            SUM(CASE T1.di_cd WHEN '5' THEN 1 ELSE 1 END)          AS  accidentnotice, \
            SUM(CASE T1.di_cd WHEN '6' THEN 1 ELSE 1 END)          AS  noaccidentnotice, \
            SUM(CASE T1.di_cd WHEN '7' THEN 1 ELSE 1 END)          AS  late \
        FROM attend_dat         T1, \
             attendclass_dat    T2 \
        WHERE T1.attenddate         >=  DATE(IN_term_start) \
          AND T1.attenddate         <=  DATE(IN_term_end) \ 
          AND T1.periodcd           >=  '2' \
          AND T1.classcd             =   IN_classcd \
          AND T1.subclasscd          =   IN_subclasscd \
          AND T2.year                =   IN_year \
          AND T2.attendclasscd       =   IN_attendclasscd \
          AND T2.schregno            =   T1.schregno \
          AND char(DATE(T1.attenddate)) || T1.schregno NOT IN \
          ( \
            SELECT \
                char(DATE(T3.attenddate)) || T3.schregno \
            FROM attend_dat T3 \
            WHERE T3.schregno         =    T1.schregno \
              AND T3.attenddate      >=   DATE(IN_term_start) \
              AND T3.attenddate      <=   DATE(IN_term_end) \
              AND T3.periodcd         =    '1' \
              AND T3.di_cd           >=   '1' \
              AND T3.di_cd           <=   '6' \
          ) \
        GROUP BY T1.schregno \
        ORDER BY T1.schregno; \
------------------------------------------------------------------------------ \
    DECLARE CUR_attend_dat2 CURSOR FOR \
        SELECT \
            T1.schregno, \
            SUM(CASE T1.di_cd WHEN '0' THEN 0 ELSE 1 END)          AS  sum, \
            SUM(CASE T1.di_cd WHEN '1' THEN 1 ELSE 1 END)          AS  absent, \
            SUM(CASE T1.di_cd WHEN '2' THEN 1 ELSE 1 END)          AS  suspend, \
            SUM(CASE T1.di_cd WHEN '3' THEN 1 ELSE 1 END)          AS  mourning, \
            SUM(CASE T1.di_cd WHEN '4' THEN 1 ELSE 1 END)          AS  sick, \
            SUM(CASE T1.di_cd WHEN '5' THEN 1 ELSE 1 END)          AS  accidentnotice, \
            SUM(CASE T1.di_cd WHEN '6' THEN 1 ELSE 1 END)          AS  noaccidentnotice, \
            SUM(CASE T1.di_cd WHEN '7' THEN 1 ELSE 1 END)          AS  late \
        FROM attend_dat         T1, \
             attendclass_dat    T2 \
        WHERE T1.attenddate         >=  DATE(IN_term_start) \
          AND T1.attenddate         <=  DATE(IN_term_end) \
          AND T1.periodcd           >=  '2' \
          AND T1.subclasscd          =   IN_subclasscd \
          AND T2.year                =   IN_year \
          AND T2.attendclasscd       =   IN_attendclasscd \
          AND T2.schregno            =   T1.schregno \
        GROUP BY T1.schregno \
        ORDER BY T1.schregno; \
------------------------------------------------------------------------------ \
-- エラー処理 \
------------------------------------------------------------------------------ \
    -- ロック失敗
        BEGIN \
        DECLARE CONTINUE HANDLER FOR NOT FOUND set at_error = 1; \
        DECLARE EXIT HANDLER FOR SQLEXCEPTION \
        IF SQLSTATE = '40001' THEN \
                SET OUT_RESULT  = STS_LOCKED; \
                SET OUT_COUNT   = 0; \
                ROLLBACK; \
        ELSE \
        -- その他
	        SET OUT_RESULT  = STS_INVALID; \
        	SET OUT_COUNT   = 0; \
        ROLLBACK; \
        END IF; \
        END; \
------------------------------------------------------------------------------ \
-- 処理開始 \
------------------------------------------------------------------------------ \
    -- 初期処理 \
    ---------------------------- \
    --既存データの削除 \
    ---------------------------- \
    DELETE FROM attend_subclass_dat \
    WHERE year           =   IN_year \
      AND semester       =   IN_term \
      AND classcd        =   IN_classcd \
      AND subclasscd     =   IN_subclasscd \
      AND schregno          IN \
          ( \
           SELECT schregno FROM attendclass_dat \
           WHERE year            =   IN_year \
             AND attendclasscd   =   IN_attendclasscd \
          ); \
    SET OUT_RESULT     = STS_INVALID; \
    SET OUT_COUNT      = 0; \
    SET WK_OUT_COUNT   = 0; \
  -- 更新処理 \
  OPEN CUR_attend_dat1; \
  FETCH CUR_attend_dat1     INTO  v_schregno3, v_sum3, v_absent3, v_suspend3, v_mourning3, v_sick3, \
				  v_accidentnotice3, v_noaccidentnotice3, v_late3; \
  OPEN CUR_attend_dat2; \
  FETCH CUR_attend_dat2     INTO  v_schregno4, v_sum4, v_absent4, v_suspend4, v_mourning4, v_sick4, \
                                  v_accidentnotice4, v_noaccidentnotice4, v_late4; \
  \
  OPEN CUR_schedule_dat2; \
  FETCH CUR_schedule_dat2   INTO  v_schregno2, v_classhour2; \
  FOR for1 AS \
        SELECT \
            T1.schregno                                 AS  schregno, \
            COALESCE(T5.credit,0) * T4.classweeks       AS  classhour \
        FROM \
            attendclass_dat    T1, \
            schreg_regd_dat     T2, \
            schreg_base_mst      T3, \
            classweek_dat      T4, \
            credityear_dat      T5 \
        WHERE T1.year               =   IN_year \
          AND T1.attendclasscd      =   IN_attendclasscd \
          AND T2.year               =   T1.year \
          AND T2.schregno           =   T1.schregno \
          AND T3.schregno           =   T1.schregno \
          AND T4.year               =   T1.year \
          AND T4.semester           =   IN_term \
          AND T4.grade              =   T2.grade \
          AND T4.hr_class           =   T2.hr_class \
          AND T5.credityear         =   T1.year \
          AND T5.coursecd           =   T3.coursecd \
          AND T5.majorcd            =   T3.majorcd \
          AND T5.grade              =   T2.grade \
          AND T5.course             =   T2.coursecode \
          AND T5.classcd            =   IN_classcd \
          AND T5.subclasscd         =   IN_subclasscd \
        ORDER BY T1.schregno \
  DO \
    -- 出欠データから出欠科目別累積データを作成 \
        ---------------------------- \
        --出欠データ1の検索 \
        ---------------------------- \
        loop1: LOOP \
            IF at_error = 1 THEN \
                SET WK_rtimeabsent     =  0; \
                SET WK_rtimesum        =  0; \
                SET WK_rtimesuspend    =  0; \
                SET WK_rtimemourning   =  0; \
                SET WK_rtimesick       =  0; \
                SET WK_rtimelate       =  0; \
                SET WK_rtimenotice     =  0; \
                SET WK_rtimenonotice   =  0; \ 
                LEAVE loop1; \
            ELSEIF  for1.schregno    =    v_schregno3  THEN \
                SET WK_rtimeabsent     =  v_sum3; \
                SET WK_rtimesum        =  v_absent3; \
                SET WK_rtimesuspend    =  v_suspend3; \
                SET WK_rtimemourning   =  v_mourning3; \
                SET WK_rtimesick       =  v_sick3; \
                SET WK_rtimelate       =  v_late3; \
                SET WK_rtimenotice     =  v_accidentnotice3; \
                SET WK_rtimenonotice   =  v_noaccidentnotice3; \
                LEAVE loop1; \
            ELSEIF  for1.schregno    <    v_schregno3  THEN \
                SET WK_rtimeabsent     =  0; \
                SET WK_rtimesum        =  0; \
                SET WK_rtimesuspend    =  0; \
                SET WK_rtimemourning   =  0; \
                SET WK_rtimesick       =  0; \
                SET WK_rtimelate       =  0; \
                SET WK_rtimenotice     =  0; \
                SET WK_rtimenonotice   =  0; \
                LEAVE loop1; \
            ELSEIF  for1.schregno    >    v_schregno3  THEN \
		FETCH CUR_attend_dat1     INTO  v_schregno3, v_sum3, v_absent3, v_suspend3, v_mourning3, v_sick3, \
                                	        v_accidentnotice3, v_noaccidentnotice3, v_late3; \
            END IF; \
        END LOOP loop1; \
        ---------------------------- \
        --出欠データ2の検索 \
        ---------------------------- \
        loop2: LOOP \
            IF at_error = 1 THEN \
                SET WK_timeabsent     =  0; \
                SET WK_timesum        =  0; \
                SET WK_timesuspend    =  0; \
                SET WK_timemourning   =  0; \
                SET WK_timesick       =  0; \
                SET WK_timelate       =  0; \
                SET WK_timenotice     =  0; \
                SET WK_timenonotice   =  0; \
                LEAVE loop2; \
            ELSEIF  for1.schregno    =   v_schregno4  THEN \
                SET WK_timeabsent     =  v_sum4; \
                SET WK_timesum        =  v_absent4; \
                SET WK_timesuspend    =  v_suspend4; \
                SET WK_timemourning   =  v_mourning4; \
                SET WK_timesick       =  v_sick4; \
                SET WK_timelate       =  v_late4; \
                SET WK_timenotice     =  v_accidentnotice4; \
                SET WK_timenonotice   =  v_noaccidentnotice4; \
                LEAVE loop2; \
            ELSEIF  for1.schregno    <   v_schregno4  THEN \
                SET WK_timeabsent     =  0; \
                SET WK_timesum        =  0; \
                SET WK_timesuspend    =  0; \
                SET WK_timemourning   =  0; \
                SET WK_timesick       =  0; \
                SET WK_timelate       =  0; \
                SET WK_timenotice     =  0; \
                SET WK_timenonotice   =  0; \
                LEAVE loop2; \
            ELSEIF  for1.schregno    >   v_schregno4  THEN \
	    FETCH CUR_attend_dat2     INTO  v_schregno4, v_sum4, v_absent4, v_suspend4, v_mourning4, v_sick4, \
            	                            v_accidentnotice4, v_noaccidentnotice4, v_late4; \
            END IF; \
        END LOOP loop2; \
        ---------------------------- \
        --時間割データ2の検索 \
        ---------------------------- \ 
        loop3: LOOP \
            IF at_error = 1 THEN \
                SET WK_rtimepresent           =  0; \
                LEAVE loop3; \
            ELSEIF  for1.schregno    =    v_schregno2  THEN \
                SET WK_rtimepresent           =  v_classhour2; \
                LEAVE loop3; \
            ELSEIF  for1.schregno    <    v_schregno2  THEN \
              	SET WK_rtimepresent           =  0; \
                LEAVE loop3; \
            ELSEIF  for1.schregno    >    v_schregno2  THEN \
              FETCH CUR_schedule_dat2   INTO    v_schregno2, v_classhour2; \
            END IF; \
        END LOOP loop3; \
        ---------------------------- \
        --時間割データ1の検索 \
        ---------------------------- \
        SET WK_timepresent      =  for1.classhour; \
        INSERT INTO  attend_subclass_dat( \
            	year, \
            	semester, \
            	schregno, \
            	attendmonth, \
		classcd, \
		subclasscd, \
		a_present, \
		a_absent, \ 
		a_suspend, \ 
		a_mourning, \
		a_sick, \
		a_late, \
		a_notice, \
		a_nonotice, \
		present, \
		absent, \
		suspend, \
		mourning, \
		sick, \
		late, \
		notice, \
		nonotice, \
		updated) \
        VALUES \
            ( \
            IN_year, \
            IN_term, \
            for1.schregno, \
            IN_yymm, \
            IN_classcd, \
            IN_subclasscd, \
            WK_rtimepresent   -   WK_rtimesum, \
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
    END FOR; \
    CLOSE CUR_attend_dat1; \
    CLOSE CUR_attend_dat2; \
    CLOSE CUR_schedule_dat2; \
    -- 正常終了 \
    SET OUT_RESULT      = STS_SUCCESS; \
    SET OUT_COUNT       = WK_OUT_COUNT; \
END
