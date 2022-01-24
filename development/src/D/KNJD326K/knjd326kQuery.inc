<?php
class knjd326kQuery extends Query {

    //テスト結果を取得初回
    function get_testscore_data($model)
    {
        //NO009-->
        $query  = " WITH CHR_TEST_TABLE_SUB AS ( ";
        $query .= " SELECT ";
        $query .= "     CHAIRCD, ";
        $query .= "     TESTKINDCD, ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_TEST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->cntl_dt_year."' ";
        $query .= "     AND SEMESTER < '3' ";
        $query .= " GROUP BY ";
        $query .= "     CHAIRCD, ";
        $query .= "     TESTKINDCD, ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER ";
        $query .= " ), CHR_TEST_TABLE AS ( ";
        $query .= " SELECT ";
        $query .= "     CHAIRCD AS CHACD, ";
        $query .= "     SEMESTER AS SEMES, ";
        $query .= "     SUM(SMALLINT(TESTKINDCD)) AS TESTSUM ";
        $query .= " FROM ";
        $query .= "     CHR_TEST_TABLE_SUB ";
        $query .= " GROUP BY ";
        $query .= "     CHAIRCD, ";
        $query .= "     SEMESTER ";
        $query .= " ) ";
        //NO009<--
        $query .= " SELECT ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     SEM1_INTER_REC,SEM1_TERM_REC,SEM1_REC, ";
        $query .= "     SEM2_INTER_REC,SEM2_TERM_REC,SEM2_REC, ";
        $query .= "     SEM3_REC,SEM3_TERM_REC,GRADE_RECORD, ";
        $query .= "     CASE WHEN SEM1_REC IS NULL THEN 0 ELSE 1 END AS SEM1_REC_MARK, ";    //NO008
        $query .= "     CASE WHEN SEM2_REC IS NULL THEN 0 ELSE 1 END AS SEM2_REC_MARK, ";    //NO008
        $query .= "     CASE WHEN SEM3_REC IS NULL THEN 0 ELSE 1 END AS SEM3_REC_MARK, ";    //NO008
        $query .= "     SEM1_INTER_REC_DI,SEM1_TERM_REC_DI, ";
        $query .= "     SEM2_INTER_REC_DI,SEM2_TERM_REC_DI, ";
        $query .= "     SEM3_TERM_REC_DI, ";
        $query .= "     SEM1_INTER_REC_FLG,SEM1_TERM_REC_FLG,SEM1_REC_FLG, ";
        $query .= "     SEM2_INTER_REC_FLG,SEM2_TERM_REC_FLG,SEM2_REC_FLG, ";
        $query .= "     SEM3_TERM_REC_FLG,SEM3_REC_FLG, ";
        $query .= "     L1.TESTSUM AS TESTSUM1,L2.TESTSUM AS TESTSUM2 ";    //NO009
        $query .= " FROM ";
        $query .= "     KIN_RECORD_DAT ";
        //NO009-->
        $query .= "     LEFT JOIN CHR_TEST_TABLE L1 ON L1.CHACD = CHAIRCD ";
        $query .= "     AND L1.SEMES = '1' ";
        $query .= "     LEFT JOIN CHR_TEST_TABLE L2 ON L2.CHACD = CHAIRCD ";
        $query .= "     AND L2.SEMES = '2' ";
        //NO009<--
        $query .= " WHERE ";
        $query .= "     YEAR = '" .$model->cntl_dt_year. "' AND ";
        $query .= "     SCHREGNO = '" .$model->schregno. "' AND ";
        $query .= "     SUBSTR(SUBCLASSCD,1,2) in ('01','02','03','04','05','06','07','08','09') ";
        return $query;
    }
    //テスト結果を取得前年度
    function get_testscore_data_last($model)
    {
        //NO009-->
        $query  = " WITH CHR_TEST_TABLE_SUB AS ( ";
        $query .= " SELECT ";
        $query .= "     CHAIRCD, ";
        $query .= "     TESTKINDCD, ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_TEST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->cntl_last_year."' ";
        $query .= "     AND SEMESTER < '3' ";
        $query .= " GROUP BY ";
        $query .= "     CHAIRCD, ";
        $query .= "     TESTKINDCD, ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER ";
        $query .= " ), CHR_TEST_TABLE AS ( ";
        $query .= " SELECT ";
        $query .= "     CHAIRCD AS CHACD, ";
        $query .= "     SEMESTER AS SEMES, ";
        $query .= "     SUM(SMALLINT(TESTKINDCD)) AS TESTSUM ";
        $query .= " FROM ";
        $query .= "     CHR_TEST_TABLE_SUB ";
        $query .= " GROUP BY ";
        $query .= "     CHAIRCD, ";
        $query .= "     SEMESTER ";
        $query .= " ) ";
        //NO009<--
        $query .= " SELECT ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     SEM1_INTER_REC,SEM1_TERM_REC,SEM1_REC, ";
        $query .= "     SEM2_INTER_REC,SEM2_TERM_REC,SEM2_REC, ";
        $query .= "     SEM3_REC,SEM3_TERM_REC,GRADE_RECORD, ";
        $query .= "     CASE WHEN SEM1_REC IS NULL THEN 0 ELSE 1 END AS SEM1_REC_MARK, ";    //NO008
        $query .= "     CASE WHEN SEM2_REC IS NULL THEN 0 ELSE 1 END AS SEM2_REC_MARK, ";    //NO008
        $query .= "     CASE WHEN SEM3_REC IS NULL THEN 0 ELSE 1 END AS SEM3_REC_MARK, ";    //NO008
        $query .= "     SEM1_INTER_REC_DI,SEM1_TERM_REC_DI, ";
        $query .= "     SEM2_INTER_REC_DI,SEM2_TERM_REC_DI, ";
        $query .= "     SEM3_TERM_REC_DI, ";
        $query .= "     SEM1_INTER_REC_FLG,SEM1_TERM_REC_FLG,SEM1_REC_FLG, ";
        $query .= "     SEM2_INTER_REC_FLG,SEM2_TERM_REC_FLG,SEM2_REC_FLG, ";
        $query .= "     SEM3_TERM_REC_FLG,SEM3_REC_FLG, ";
        $query .= "     L1.TESTSUM AS TESTSUM1,L2.TESTSUM AS TESTSUM2 ";    //NO009
        $query .= " FROM ";
        $query .= "     KIN_RECORD_DAT ";
        //NO009-->
        $query .= "     LEFT JOIN CHR_TEST_TABLE L1 ON L1.CHACD = CHAIRCD ";
        $query .= "     AND L1.SEMES = '1' ";
        $query .= "     LEFT JOIN CHR_TEST_TABLE L2 ON L2.CHACD = CHAIRCD ";
        $query .= "     AND L2.SEMES = '2' ";
        //NO009<--
        $query .= " WHERE ";
        $query .= "     YEAR = '" .$model->cntl_last_year. "' AND ";
        $query .= "     SCHREGNO = '" .$model->schregno. "' AND ";
        $query .= "     SUBSTR(SUBCLASSCD,1,2) in ('01','02','03','04','05','06','07','08','09') ";
        return $query;
    }
    //テスト結果を取得前々年度
    function get_testscore_data_beforelast($model)
    {
        //NO009-->
        $query  = " WITH CHR_TEST_TABLE_SUB AS ( ";
        $query .= " SELECT ";
        $query .= "     CHAIRCD, ";
        $query .= "     TESTKINDCD, ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_TEST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->cntl_beforelast_year."' ";
        $query .= "     AND SEMESTER < '3' ";
        $query .= " GROUP BY ";
        $query .= "     CHAIRCD, ";
        $query .= "     TESTKINDCD, ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER ";
        $query .= " ), CHR_TEST_TABLE AS ( ";
        $query .= " SELECT ";
        $query .= "     CHAIRCD AS CHACD, ";
        $query .= "     SEMESTER AS SEMES, ";
        $query .= "     SUM(SMALLINT(TESTKINDCD)) AS TESTSUM ";
        $query .= " FROM ";
        $query .= "     CHR_TEST_TABLE_SUB ";
        $query .= " GROUP BY ";
        $query .= "     CHAIRCD, ";
        $query .= "     SEMESTER ";
        $query .= " ) ";
        //NO009<--
        $query .= " SELECT ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     SEM1_INTER_REC,SEM1_TERM_REC,SEM1_REC, ";
        $query .= "     SEM2_INTER_REC,SEM2_TERM_REC,SEM2_REC, ";
        $query .= "     SEM3_REC,SEM3_TERM_REC,GRADE_RECORD, ";
        $query .= "     CASE WHEN SEM1_REC IS NULL THEN 0 ELSE 1 END AS SEM1_REC_MARK, ";    //NO008
        $query .= "     CASE WHEN SEM2_REC IS NULL THEN 0 ELSE 1 END AS SEM2_REC_MARK, ";    //NO008
        $query .= "     CASE WHEN SEM3_REC IS NULL THEN 0 ELSE 1 END AS SEM3_REC_MARK, ";    //NO008
        $query .= "     SEM1_INTER_REC_DI,SEM1_TERM_REC_DI, ";
        $query .= "     SEM2_INTER_REC_DI,SEM2_TERM_REC_DI, ";
        $query .= "     SEM3_TERM_REC_DI, ";
        $query .= "     SEM1_INTER_REC_FLG,SEM1_TERM_REC_FLG,SEM1_REC_FLG, ";
        $query .= "     SEM2_INTER_REC_FLG,SEM2_TERM_REC_FLG,SEM2_REC_FLG, ";
        $query .= "     SEM3_TERM_REC_FLG,SEM3_REC_FLG, ";
        $query .= "     L1.TESTSUM AS TESTSUM1,L2.TESTSUM AS TESTSUM2 ";    //NO009
        $query .= " FROM ";
        $query .= "     KIN_RECORD_DAT ";
        //NO009-->
        $query .= "     LEFT JOIN CHR_TEST_TABLE L1 ON L1.CHACD = CHAIRCD ";
        $query .= "     AND L1.SEMES = '1' ";
        $query .= "     LEFT JOIN CHR_TEST_TABLE L2 ON L2.CHACD = CHAIRCD ";
        $query .= "     AND L2.SEMES = '2' ";
        //NO009<--
        $query .= " WHERE ";
        $query .= "     YEAR = '" .$model->cntl_beforelast_year. "' AND ";
        $query .= "     SCHREGNO = '" .$model->schregno. "' AND ";
        $query .= "     SUBSTR(SUBCLASSCD,1,2) in ('01','02','03','04','05','06','07','08','09') ";
        return $query;
    }
    //生徒名前取得（学籍基礎マスタ）
    function getSchreg_Base_Mst($model)
    {
        $query  = " SELECT * FROM SCHREG_BASE_MST ";
        $query .= " WHERE SCHREGNO = '" .$model->schregno."'";
        return $query;
    }
    //生徒学年クラス取得
    function getSchreg_Regd_Dat($model)
    {
        $query  = " SELECT t1.GRADE,t1.HR_CLASS,t1.ATTENDNO,t2.NAME FROM SCHREG_REGD_DAT t1 left join SCHREG_BASE_MST t2 on t1.SCHREGNO = t2.SCHREGNO ";
        $query .= " WHERE t1.SCHREGNO = '" .$model->schregno."' AND ";
        $query .= "       t1.YEAR = '".$model->cntl_dt_year."' AND ";
        $query .= "       t1.SEMESTER = '".$model->cntl_semester."' ";
        return $query;
    }
    //生徒学年クラス取得前年度
    function getSchreg_Regd_Dat_last($model)
    {
        $query  = " SELECT GRADE,HR_CLASS,ATTENDNO FROM SCHREG_REGD_DAT ";
        $query .= " WHERE SCHREGNO = '" .$model->schregno."' AND ";
        $query .= "       YEAR = '".$model->cntl_last_year."' ";
        return $query;
    }
    //生徒学年クラス取得前々年度
    function getSchreg_Regd_Dat_befor($model)
    {
        $query  = " SELECT GRADE,HR_CLASS,ATTENDNO FROM SCHREG_REGD_DAT ";
        $query .= " WHERE SCHREGNO = '" .$model->schregno."' AND ";
        $query .= "       YEAR = '".$model->cntl_beforelast_year."' ";
        return $query;
    }
    //異動情報取得
    function Get_transfer($model,$year)
    {

        $query  = " SELECT ";
        $query .= " T1.TRANSFERCD,T1.TRANSFER_SDATE ";
        $query .= " FROM ";
        $query .= " SCHREG_TRANSFER_DAT T1,";
        $query .= " (SELECT SDATE,EDATE ";
        $query .= "  FROM SEMESTER_MST T2";
        $query .= "  WHERE ";
        $query .= "  YEAR = '" .$year. "' AND ";
        $query .= "  SEMESTER = '9') T2 ";
        $query .= " WHERE ";
        $query .= " T1.SCHREGNO = '" .$model->schregno."' AND ";
        $query .= " T1.TRANSFERCD IN ('01','02') AND ";     //1:留学/2:休学
        $query .= " T1.TRANSFER_SDATE between T2.SDATE AND ";
        $query .= " T2.EDATE ";
        $query .= " ORDER BY T1.TRANSFER_SDATE DESC";

        return $query;
    }
    //対外模試データ取得
    function get_proficiency_data($model,$year)
    {
        $query  = " SELECT ";
        $query .= " SHAMEXAMCD,SUBCLASSCD,SCORE as SCORE1, ";
        $query .= " cast(SCORE as smallint) as SCORE, ";
        $query .= " case when DEVIATION is null then SCHOOL_DEVIATION else DEVIATION end as DEVIATION, ";
        $query .= " PRECEDENCE,SCHOOL_DEVIATION,SCHOOL_PRECEDENCE ";
        $query .= " FROM SHAMEXAMINATION_DAT ";
        $query .= " WHERE SCHREGNO = '" .$model->schregno."' AND ";
        $query .= "       YEAR = '".$year."' ";
        return $query;
    }
    //平均取得
    function get_groupavg_data($model,$subcd,$year,$seme,$testkind)    //NO003
    {
        //NO006 Add Start
        $query  = "";
        $query .= "SELECT INT(" .$testkind. " * (0.6)) AS VALUE ";
        $query .= "FROM   SCHREG_REGD_DAT W1, ";
        $query .= "       RECORD_CLASS_AVERAGE_DAT W2 ";
        $query .= "WHERE  W1.SCHREGNO = '" .$model->schregno. "' AND ";
        $query .= "       W1.YEAR = '" .$year. "' AND ";
        $query .= "       W1.SEMESTER = '" .$seme. "' AND ";
        $query .= "       W2.YEAR = W1.YEAR AND ";
        $query .= "       W2.GRADE = W1.GRADE AND ";
        $query .= "       W2.HR_CLASS = W1.HR_CLASS AND ";
        $query .= "       W2.CLASSCD = '" .substr($subcd, 0, 2). "' AND ";
        $query .= "       W2.CALC_DIV = '3' ";
        //NO006 End

        return $query;

    }

    //席次取得
    function get_Rank($model, $year, $div)
    {
        $query  = " SELECT * ";
        $query .= " FROM RECORD_RANK_DAT ";
        $query .= " WHERE SCHREGNO = '" .$model->schregno."' ";
        $query .= "       AND YEAR = '".$year."' ";
        $query .= "       AND RANK_DIV = '".$div."' ";
        return $query;
    }

    //席次取得
    function get_pdcm1($model,$subcd,$year,$seme,$grd,$kamoku)
    {
        $query  = " with courseget(COURSECODE,COURSE_SEQ,GRADE) as (SELECT ";
        $query .= "      t1.COURSECODE ,t2.COURSE_SEQ,t2.GRADE ";
        $query .= "  FROM ";
        $query .= "      SCHREG_REGD_DAT t1 left join COURSE_GROUP_DAT t2 on t1.COURSECODE = t2.COURSECODE ";
        $query .= "   AND t2.year = '" .$year. "' ";
        $query .= "   AND t1.GRADE = t2.GRADE ";
        $query .= "  WHERE ";
        $query .= "      t1.SCHREGNO = '" .$model->schregno. "' AND ";
        $query .= "      t1.year = '" .$year. "' AND ";
        $query .= "      t1.semester = '" .$seme. "' ";
        $query .= "  ), ";
        $query .= "  course(COURSECODE,GRADE) as (SELECT ";
        $query .= "      t1.COURSECODE,t1.GRADE ";
        $query .= "  FROM ";
        $query .= "      COURSE_GROUP_DAT t1,courseget t2 ";
        $query .= "  WHERE ";
        $query .= "      t1.YEAR = '" .$year. "' ";
        $query .= "      AND t1.GRADE = t2.GRADE ";
        $query .= "      AND t1.COURSE_SEQ = t2.COURSE_SEQ ";
        $query .= "  ), ";
        $query .= "  sctable(SCHREGNO) as (SELECT ";
        $query .= "                              SCHREGNO ";
        $query .= "                          FROM ";
        $query .= "                              SCHREG_REGD_DAT t1, ";
        $query .= "                              course t2 ";
        $query .= "                          WHERE ";
        $query .= "                              t1.year = '" .$year. "' AND ";
        $query .= "                              t1.semester = '" .$seme. "'AND ";
        $query .= "                              t1.COURSECODE = t2.COURSECODE AND ";
        $query .= "                              t1.GRADE = t2.GRADE ";
        $query .= "  ), ";
        $query .= " sem1test (SCHREGNO,CNTSUB) as ( ";
        $query .= " select ";
        $query .= "     schregno,count(SUBCLASSCD) ";
        $query .= " from ";
        $query .= "     kin_record_dat ";
        $query .= " where ";
        $query .= "     YEAR = '" .$year. "' AND ";
        if ($kamoku == 3) {
            $query .= "     SUBSTR(SUBCLASSCD,1,2) in ('01','03','05') AND ";
        } else if ($kamoku == 5) {
            $query .= "     SUBSTR(SUBCLASSCD,1,2) in ('01','02','03','04','05') AND ";
        } else {
            $query .= "     SUBSTR(SUBCLASSCD,1,2) in ('01','02','03','04','05','06','07','08','09') AND ";
        }
        $query .= "     SEM1_INTER_REC is not null ";
        $query .= " GROUP BY ";
        $query .= "     schregno ";
        $query .= " ), ";
        $query .= " sem1test2 (SCHREGNO) as ( ";
        $query .= " SELECT ";
        $query .= "     t1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     sctable t1 ";
        $query .= " WHERE ";
        if ($kamoku == 3) {
            $query .= "     not exists (select 'x' from sem1test t2 where t1.schregno = t2.schregno and t2.CNTSUB < 3 ) ";
        } else if ($kamoku == 5) {
            $query .= "     not exists (select 'x' from sem1test t2 where t1.schregno = t2.schregno and t2.CNTSUB < 5 ) ";
        } else {
            $query .= "     not exists (select 'x' from sem1test t2 where t1.schregno = t2.schregno and t2.CNTSUB < 9 ) ";
        }
        $query .= " GROUP BY ";
        $query .= "     t1.SCHREGNO ";
        $query .= " ), ";
        $query .= " sem1rnktable(SCHREGNO,S1INRNK) as ( ";
        $query .= "  SELECT ";
        $query .= "      t1.SCHREGNO, ";
        $query .= "      RANK() OVER(ORDER BY (value(round(avg(float(t1.SEM1_INTER_REC))*10,0)/10,-1)) desc) as S1INRNK ";
        $query .= "  FROM ";
        $query .= "      KIN_RECORD_DAT t1, ";
        $query .= "      sem1test2 t2 ";
        $query .= "  WHERE ";
        $query .= "      t1.YEAR = '" .$year. "' AND ";
        if ($kamoku == 3) {
            $query .= "      SUBSTR(t1.SUBCLASSCD,1,2) in ('01','03','05') AND ";
        } else if ($kamoku == 5) {
            $query .= "      SUBSTR(t1.SUBCLASSCD,1,2) in ('01','02','03','04','05') AND ";
        } else {
            $query .= "      SUBSTR(t1.SUBCLASSCD,1,2) in ('01','02','03','04','05','06','07','08','09') AND ";
        }
        $query .= "      t1.SCHREGNO = t2.SCHREGNO ";
        $query .= "  GROUP BY ";
        $query .= "      t1.SCHREGNO ";
        $query .= "  ) ";
        $query .= "  SELECT ";
        $query .= "      t1.S1INRNK ";
        $query .= "  FROM sem1rnktable t1";
        $query .= "  WHERE ";
        $query .= "      t1.SCHREGNO = '" .$model->schregno. "' ";

        return $query;

    }
    function get_pdcm2($model,$subcd,$year,$seme,$grd,$kamoku)
    {
        $query  = " with courseget(COURSECODE,COURSE_SEQ,GRADE) as (SELECT ";
        $query .= "      t1.COURSECODE ,t2.COURSE_SEQ,t2.GRADE ";
        $query .= "  FROM ";
        $query .= "      SCHREG_REGD_DAT t1 left join COURSE_GROUP_DAT t2 on t1.COURSECODE = t2.COURSECODE ";
        $query .= "   AND t2.year = '" .$year. "' ";
        $query .= "   AND t1.GRADE = t2.GRADE ";
        $query .= "  WHERE ";
        $query .= "      t1.SCHREGNO = '" .$model->schregno. "' AND ";
        $query .= "      t1.year = '" .$year. "' AND ";
        $query .= "      t1.semester = '" .$seme. "' ";
        $query .= "  ), ";
        $query .= "  course(COURSECODE,GRADE) as (SELECT ";
        $query .= "      t1.COURSECODE,t1.GRADE ";
        $query .= "  FROM ";
        $query .= "      COURSE_GROUP_DAT t1,courseget t2 ";
        $query .= "  WHERE ";
        $query .= "      t1.YEAR = '" .$year. "' ";
        $query .= "      AND t1.GRADE = t2.GRADE ";
        $query .= "      AND t1.COURSE_SEQ = t2.COURSE_SEQ ";
        $query .= "  ), ";
        $query .= "  sctable(SCHREGNO) as (SELECT ";
        $query .= "                              SCHREGNO ";
        $query .= "                          FROM ";
        $query .= "                              SCHREG_REGD_DAT t1, ";
        $query .= "                              course t2 ";
        $query .= "                          WHERE ";
        $query .= "                              t1.year = '" .$year. "' AND ";
        $query .= "                              t1.semester = '" .$seme. "'AND ";
        $query .= "                              t1.COURSECODE = t2.COURSECODE AND ";
        $query .= "                              t1.GRADE = t2.GRADE ";
        $query .= "  ), ";
        $query .= " ter1test (SCHREGNO,CNTSUB) as ( ";
        $query .= " select ";
        $query .= "     schregno,count(SUBCLASSCD) ";
        $query .= " from ";
        $query .= "     kin_record_dat ";
        $query .= " where ";
        $query .= "     YEAR = '" .$year. "' AND ";
        if ($kamoku == 3) {
            $query .= "     SUBSTR(SUBCLASSCD,1,2) in ('01','03','05') AND ";
        } else if ($kamoku == 5) {
            $query .= "     SUBSTR(SUBCLASSCD,1,2) in ('01','02','03','04','05') AND ";
        } else {
            $query .= "     SUBSTR(SUBCLASSCD,1,2) in ('01','02','03','04','05','06','07','08','09') AND ";
        }
        $query .= "     SEM1_TERM_REC is not null ";
        $query .= " GROUP BY ";
        $query .= "     schregno ";
        $query .= " ), ";
        $query .= " ter1test2 (SCHREGNO) as ( ";
        $query .= " SELECT ";
        $query .= "     t1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     sctable t1 ";
        $query .= " WHERE ";
        if ($kamoku == 3) {
            $query .= "     not exists (select 'x' from ter1test t2 where t1.schregno = t2.schregno and t2.CNTSUB < 3 ) ";
        } else if ($kamoku == 5) {
            $query .= "     not exists (select 'x' from ter1test t2 where t1.schregno = t2.schregno and t2.CNTSUB < 5 ) ";
        } else {
            $query .= "     not exists (select 'x' from ter1test t2 where t1.schregno = t2.schregno and t2.CNTSUB < 9 ) ";
        }
        $query .= " GROUP BY ";
        $query .= "     t1.SCHREGNO ";
        $query .= " ), ";
        $query .= " ter1rnktable(SCHREGNO,S1TMRNK) as ( ";
        $query .= "  SELECT ";
        $query .= "      t1.SCHREGNO, ";
        $query .= "      RANK() OVER(ORDER BY (value(round(avg(float(t1.SEM1_TERM_REC))*10,0 )/10,-1)) desc) as S1TMRNK ";
        $query .= "  FROM ";
        $query .= "      KIN_RECORD_DAT t1, ";
        $query .= "      ter1test2 t2 ";
        $query .= "  WHERE ";
        $query .= "      t1.YEAR = '" .$year. "' AND ";
        if ($kamoku == 3) {
            $query .= "      SUBSTR(t1.SUBCLASSCD,1,2) in ('01','03','05') AND ";
        } else if ($kamoku == 5) {
            $query .= "      SUBSTR(t1.SUBCLASSCD,1,2) in ('01','02','03','04','05') AND ";
        } else {
            $query .= "      SUBSTR(t1.SUBCLASSCD,1,2) in ('01','02','03','04','05','06','07','08','09') AND ";
        }
        $query .= "      t1.SCHREGNO = t2.SCHREGNO ";
        $query .= "  GROUP BY ";
        $query .= "      t1.SCHREGNO ";
        $query .= "  ) ";
        $query .= "  SELECT ";
        $query .= "      t1.S1TMRNK ";
        $query .= "  FROM ter1rnktable t1";
        $query .= "  WHERE ";
        $query .= "      t1.SCHREGNO = '" .$model->schregno. "' ";

        return $query;

    }
    function get_pdcm3($model,$subcd,$year,$seme,$grd,$kamoku)
    {
        $query  = " with courseget(COURSECODE,COURSE_SEQ,GRADE) as (SELECT ";
        $query .= "      t1.COURSECODE ,t2.COURSE_SEQ,t2.GRADE ";
        $query .= "  FROM ";
        $query .= "      SCHREG_REGD_DAT t1 left join COURSE_GROUP_DAT t2 on t1.COURSECODE = t2.COURSECODE ";
        $query .= "   AND t2.year = '" .$year. "' ";
        $query .= "   AND t1.GRADE = t2.GRADE ";
        $query .= "  WHERE ";
        $query .= "      t1.SCHREGNO = '" .$model->schregno. "' AND ";
        $query .= "      t1.year = '" .$year. "' AND ";
        $query .= "      t1.semester = '" .$seme. "' ";
        $query .= "  ), ";
        $query .= "  course(COURSECODE,GRADE) as (SELECT ";
        $query .= "      t1.COURSECODE,t1.GRADE ";
        $query .= "  FROM ";
        $query .= "      COURSE_GROUP_DAT t1,courseget t2 ";
        $query .= "  WHERE ";
        $query .= "      t1.YEAR = '" .$year. "' ";
        $query .= "      AND t1.GRADE = t2.GRADE ";
        $query .= "      AND t1.COURSE_SEQ = t2.COURSE_SEQ ";
        $query .= "  ), ";
        $query .= "  sctable(SCHREGNO) as (SELECT ";
        $query .= "                              SCHREGNO ";
        $query .= "                          FROM ";
        $query .= "                              SCHREG_REGD_DAT t1, ";
        $query .= "                              course t2 ";
        $query .= "                          WHERE ";
        $query .= "                              t1.year = '" .$year. "' AND ";
        $query .= "                              t1.semester = '" .$seme. "'AND ";
        $query .= "                              t1.COURSECODE = t2.COURSECODE AND ";
        $query .= "                              t1.GRADE = t2.GRADE ";
        $query .= "  ), ";
        $query .= " sem2test (SCHREGNO,CNTSUB) as ( ";
        $query .= " select ";
        $query .= "     schregno,count(SUBCLASSCD) ";
        $query .= " from ";
        $query .= "     kin_record_dat ";
        $query .= " where ";
        $query .= "     YEAR = '" .$year. "' AND ";
        if ($kamoku == 3) {
            $query .= "     SUBSTR(SUBCLASSCD,1,2) in ('01','03','05') AND ";
        } else if ($kamoku == 5) {
            $query .= "     SUBSTR(SUBCLASSCD,1,2) in ('01','02','03','04','05') AND ";
        } else {
            $query .= "     SUBSTR(SUBCLASSCD,1,2) in ('01','02','03','04','05','06','07','08','09') AND ";
        }
        $query .= "     SEM2_INTER_REC is not null ";
        $query .= " GROUP BY ";
        $query .= "     schregno ";
        $query .= " ), ";
        $query .= " sem2test2 (SCHREGNO) as ( ";
        $query .= " SELECT ";
        $query .= "     t1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     sctable t1 ";
        $query .= " WHERE ";
        if ($kamoku == 3) {
            $query .= "     not exists (select 'x' from sem2test t2 where t1.schregno = t2.schregno and t2.CNTSUB < 3 ) ";
        } else if ($kamoku == 5) {
            $query .= "     not exists (select 'x' from sem2test t2 where t1.schregno = t2.schregno and t2.CNTSUB < 5 ) ";
        } else {
            $query .= "     not exists (select 'x' from sem2test t2 where t1.schregno = t2.schregno and t2.CNTSUB < 9 ) ";
        }
        $query .= " GROUP BY ";
        $query .= "     t1.SCHREGNO ";
        $query .= " ), ";
        $query .= " sem2rnktable(SCHREGNO,S2INRNK) as ( ";
        $query .= "  SELECT ";
        $query .= "      t1.SCHREGNO, ";
        $query .= "      RANK() OVER(ORDER BY (value(round(avg(float(t1.SEM2_INTER_REC))*10,0)/10,-1)) desc) as S1INRNK ";
        $query .= "  FROM ";
        $query .= "      KIN_RECORD_DAT t1, ";
        $query .= "      sem2test2 t2 ";
        $query .= "  WHERE ";
        $query .= "      t1.YEAR = '" .$year. "' AND ";
        if ($kamoku == 3) {
            $query .= "      SUBSTR(t1.SUBCLASSCD,1,2) in ('01','03','05') AND ";
        } else if ($kamoku == 5) {
            $query .= "      SUBSTR(t1.SUBCLASSCD,1,2) in ('01','02','03','04','05') AND ";
        } else {
            $query .= "      SUBSTR(t1.SUBCLASSCD,1,2) in ('01','02','03','04','05','06','07','08','09') AND ";
        }
        $query .= "      t1.SCHREGNO = t2.SCHREGNO ";
        $query .= "  GROUP BY ";
        $query .= "      t1.SCHREGNO ";
        $query .= "  ) ";
        $query .= "  SELECT ";
        $query .= "      t1.S2INRNK ";
        $query .= "  FROM sem2rnktable t1";
        $query .= "  WHERE ";
        $query .= "      t1.SCHREGNO = '" .$model->schregno. "' ";

        return $query;

    }
    function get_pdcm4($model,$subcd,$year,$seme,$grd,$kamoku)
    {
        $query  = " with courseget(COURSECODE,COURSE_SEQ,GRADE) as (SELECT ";
        $query .= "      t1.COURSECODE ,t2.COURSE_SEQ,t2.GRADE ";
        $query .= "  FROM ";
        $query .= "      SCHREG_REGD_DAT t1 left join COURSE_GROUP_DAT t2 on t1.COURSECODE = t2.COURSECODE ";
        $query .= "   AND t2.year = '" .$year. "' ";
        $query .= "   AND t1.GRADE = t2.GRADE ";
        $query .= "  WHERE ";
        $query .= "      t1.SCHREGNO = '" .$model->schregno. "' AND ";
        $query .= "      t1.year = '" .$year. "' AND ";
        $query .= "      t1.semester = '" .$seme. "' ";
        $query .= "  ), ";
        $query .= "  course(COURSECODE,GRADE) as (SELECT ";
        $query .= "      t1.COURSECODE,t1.GRADE ";
        $query .= "  FROM ";
        $query .= "      COURSE_GROUP_DAT t1,courseget t2 ";
        $query .= "  WHERE ";
        $query .= "      t1.YEAR = '" .$year. "' ";
        $query .= "      AND t1.GRADE = t2.GRADE ";
        $query .= "      AND t1.COURSE_SEQ = t2.COURSE_SEQ ";
        $query .= "  ), ";
        $query .= "  sctable(SCHREGNO) as (SELECT ";
        $query .= "                              SCHREGNO ";
        $query .= "                          FROM ";
        $query .= "                              SCHREG_REGD_DAT t1, ";
        $query .= "                              course t2 ";
        $query .= "                          WHERE ";
        $query .= "                              t1.year = '" .$year. "' AND ";
        $query .= "                              t1.semester = '" .$seme. "'AND ";
        $query .= "                              t1.COURSECODE = t2.COURSECODE AND ";
        $query .= "                              t1.GRADE = t2.GRADE ";
        $query .= "  ), ";
        $query .= " ter2test (SCHREGNO,CNTSUB) as ( ";
        $query .= " select ";
        $query .= "     schregno,count(SUBCLASSCD) ";
        $query .= " from ";
        $query .= "     kin_record_dat ";
        $query .= " where ";
        $query .= "     YEAR = '" .$year. "' AND ";
        if ($kamoku == 3) {
            $query .= "     SUBSTR(SUBCLASSCD,1,2) in ('01','03','05') AND ";
        } else if ($kamoku == 5) {
            $query .= "     SUBSTR(SUBCLASSCD,1,2) in ('01','02','03','04','05') AND ";
        } else {
            $query .= "     SUBSTR(SUBCLASSCD,1,2) in ('01','02','03','04','05','06','07','08','09') AND ";
        }
        $query .= "     SEM2_TERM_REC is not null ";
        $query .= " GROUP BY ";
        $query .= "     schregno ";
        $query .= " ), ";
        $query .= " ter2test2 (SCHREGNO) as ( ";
        $query .= " SELECT ";
        $query .= "     t1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     sctable t1 ";
        $query .= " WHERE ";
        if ($kamoku == 3) {
            $query .= "     not exists (select 'x' from ter2test t2 where t1.schregno = t2.schregno and t2.CNTSUB < 3 ) ";
        } else if ($kamoku == 5) {
            $query .= "     not exists (select 'x' from ter2test t2 where t1.schregno = t2.schregno and t2.CNTSUB < 5 ) ";
        } else {
            $query .= "     not exists (select 'x' from ter2test t2 where t1.schregno = t2.schregno and t2.CNTSUB < 9 ) ";
        }
        $query .= " GROUP BY ";
        $query .= "     t1.SCHREGNO ";
        $query .= " ), ";
        $query .= " ter2rnktable(SCHREGNO,S2TMRNK) as ( ";
        $query .= "  SELECT ";
        $query .= "      t1.SCHREGNO, ";
        $query .= "      RANK() OVER(ORDER BY (value(round(avg(float(t1.SEM2_TERM_REC))*10,0 )/10,-1)) desc) as S1TMRNK ";
        $query .= "  FROM ";
        $query .= "      KIN_RECORD_DAT t1, ";
        $query .= "      ter2test2 t2 ";
        $query .= "  WHERE ";
        $query .= "      t1.YEAR = '" .$year. "' AND ";
        if ($kamoku == 3) {
            $query .= "      SUBSTR(t1.SUBCLASSCD,1,2) in ('01','03','05') AND ";
        } else if ($kamoku == 5) {
            $query .= "      SUBSTR(t1.SUBCLASSCD,1,2) in ('01','02','03','04','05') AND ";
        } else {
            $query .= "      SUBSTR(t1.SUBCLASSCD,1,2) in ('01','02','03','04','05','06','07','08','09') AND ";
        }
        $query .= "      t1.SCHREGNO = t2.SCHREGNO ";
        $query .= "  GROUP BY ";
        $query .= "      t1.SCHREGNO ";
        $query .= "  ) ";
        $query .= "  SELECT ";
        $query .= "      t1.S2TMRNK ";
        $query .= "  FROM ter2rnktable t1";
        $query .= "  WHERE ";
        $query .= "      t1.SCHREGNO = '" .$model->schregno. "' ";

        return $query;

    }
    function get_pdcm5($model,$subcd,$year,$seme,$grd,$kamoku)
    {
        $query  = " with courseget(COURSECODE,COURSE_SEQ,GRADE) as (SELECT ";
        $query .= "      t1.COURSECODE ,t2.COURSE_SEQ,t2.GRADE ";
        $query .= "  FROM ";
        $query .= "      SCHREG_REGD_DAT t1 left join COURSE_GROUP_DAT t2 on t1.COURSECODE = t2.COURSECODE ";
        $query .= "   AND t2.year = '" .$year. "' ";
        $query .= "   AND t1.GRADE = t2.GRADE ";
        $query .= "  WHERE ";
        $query .= "      t1.SCHREGNO = '" .$model->schregno. "' AND ";
        $query .= "      t1.year = '" .$year. "' AND ";
        $query .= "      t1.semester = '" .$seme. "' ";
        $query .= "  ), ";
        $query .= "  course(COURSECODE,GRADE) as (SELECT ";
        $query .= "      t1.COURSECODE,t1.GRADE ";
        $query .= "  FROM ";
        $query .= "      COURSE_GROUP_DAT t1,courseget t2 ";
        $query .= "  WHERE ";
        $query .= "      t1.YEAR = '" .$year. "' ";
        $query .= "      AND t1.GRADE = t2.GRADE ";
        $query .= "      AND t1.COURSE_SEQ = t2.COURSE_SEQ ";
        $query .= "  ), ";
        $query .= "  sctable(SCHREGNO) as (SELECT ";
        $query .= "                              SCHREGNO ";
        $query .= "                          FROM ";
        $query .= "                              SCHREG_REGD_DAT t1, ";
        $query .= "                              course t2 ";
        $query .= "                          WHERE ";
        $query .= "                              t1.year = '" .$year. "' AND ";
        $query .= "                              t1.semester = '" .$seme. "'AND ";
        $query .= "                              t1.COURSECODE = t2.COURSECODE AND ";
        $query .= "                              t1.GRADE = t2.GRADE ";
        $query .= "  ), ";
        $query .= " ter3test (SCHREGNO,CNTSUB) as ( ";
        $query .= " select ";
        $query .= "     schregno,count(SUBCLASSCD) ";
        $query .= " from ";
        $query .= "     kin_record_dat ";
        $query .= " where ";
        $query .= "     YEAR = '" .$year. "' AND ";
        if ($kamoku == 3) {
            $query .= "     SUBSTR(SUBCLASSCD,1,2) in ('01','03','05') AND ";
        } else if ($kamoku == 5) {
            $query .= "     SUBSTR(SUBCLASSCD,1,2) in ('01','02','03','04','05') AND ";
        } else {
            $query .= "     SUBSTR(SUBCLASSCD,1,2) in ('01','02','03','04','05','06','07','08','09') AND ";
        }
        $query .= "     SEM3_TERM_REC is not null ";
        $query .= " GROUP BY ";
        $query .= "     schregno ";
        $query .= " ), ";
        $query .= " ter3test2 (SCHREGNO) as ( ";
        $query .= " SELECT ";
        $query .= "     t1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     sctable t1 ";
        $query .= " WHERE ";
        if ($kamoku == 3) {
            $query .= "     not exists (select 'x' from ter3test t2 where t1.schregno = t2.schregno and t2.CNTSUB < 3 ) ";
        } else if ($kamoku == 5) {
            $query .= "     not exists (select 'x' from ter3test t2 where t1.schregno = t2.schregno and t2.CNTSUB < 5 ) ";
        } else {
            $query .= "     not exists (select 'x' from ter3test t2 where t1.schregno = t2.schregno and t2.CNTSUB < 9 ) ";
        }
        $query .= " GROUP BY ";
        $query .= "     t1.SCHREGNO ";
        $query .= " ), ";
        $query .= " ter3rnktable(SCHREGNO,S3TMRNK) as ( ";
        $query .= "  SELECT ";
        $query .= "      t1.SCHREGNO, ";
        $query .= "      RANK() OVER(ORDER BY (value(round(avg(float(t1.SEM3_TERM_REC))*10,0 )/10,-1)) desc) as S1TMRNK ";
        $query .= "  FROM ";
        $query .= "      KIN_RECORD_DAT t1, ";
        $query .= "      ter3test2 t2 ";
        $query .= "  WHERE ";
        $query .= "      t1.YEAR = '" .$year. "' AND ";
        if ($kamoku == 3) {
            $query .= "      SUBSTR(t1.SUBCLASSCD,1,2) in ('01','03','05') AND ";
        } else if ($kamoku == 5) {
            $query .= "      SUBSTR(t1.SUBCLASSCD,1,2) in ('01','02','03','04','05') AND ";
        } else {
            $query .= "      SUBSTR(t1.SUBCLASSCD,1,2) in ('01','02','03','04','05','06','07','08','09') AND ";
        }
        $query .= "      t1.SCHREGNO = t2.SCHREGNO ";
        $query .= "  GROUP BY ";
        $query .= "      t1.SCHREGNO ";
        $query .= "  ) ";
        $query .= "  SELECT ";
        $query .= "      t1.S3TMRNK ";
        $query .= "  FROM ter3rnktable t1";
        $query .= "  WHERE ";
        $query .= "      t1.SCHREGNO = '" .$model->schregno. "' ";

        return $query;

    }
    function get_pdcm6($model,$subcd,$year,$seme,$grd,$kamoku)
    {
        $query  = " with courseget(COURSECODE,COURSE_SEQ,GRADE) as (SELECT ";
        $query .= "      t1.COURSECODE ,t2.COURSE_SEQ,t2.GRADE ";
        $query .= "  FROM ";
        $query .= "      SCHREG_REGD_DAT t1 left join COURSE_GROUP_DAT t2 on t1.COURSECODE = t2.COURSECODE ";
        $query .= "   AND t2.year = '" .$year. "' ";
        $query .= "   AND t1.GRADE = t2.GRADE ";
        $query .= "  WHERE ";
        $query .= "      t1.SCHREGNO = '" .$model->schregno. "' AND ";
        $query .= "      t1.year = '" .$year. "' AND ";
        $query .= "      t1.semester = '" .$seme. "' ";
        $query .= "  ), ";
        $query .= "  course(COURSECODE,GRADE) as (SELECT ";
        $query .= "      t1.COURSECODE,t1.GRADE ";
        $query .= "  FROM ";
        $query .= "      COURSE_GROUP_DAT t1,courseget t2 ";
        $query .= "  WHERE ";
        $query .= "      t1.YEAR = '" .$year. "' ";
        $query .= "      AND t1.GRADE = t2.GRADE ";
        $query .= "      AND t1.COURSE_SEQ = t2.COURSE_SEQ ";
        $query .= "  ), ";
        $query .= "  sctable(SCHREGNO) as (SELECT ";
        $query .= "                              SCHREGNO ";
        $query .= "                          FROM ";
        $query .= "                              SCHREG_REGD_DAT t1, ";
        $query .= "                              course t2 ";
        $query .= "                          WHERE ";
        $query .= "                              t1.year = '" .$year. "' AND ";
        $query .= "                              t1.semester = '" .$seme. "'AND ";
        $query .= "                              t1.COURSECODE = t2.COURSECODE AND ";
        $query .= "                              t1.GRADE = t2.GRADE ";
        $query .= "  ), ";
        $query .= " etc3test (SCHREGNO,CNTSUB) as ( ";
        $query .= " select ";
        $query .= "     schregno,count(SUBCLASSCD) ";
        $query .= " from ";
        $query .= "     kin_record_dat ";
        $query .= " where ";
        $query .= "     YEAR = '" .$year. "' AND ";
        if ($kamoku == 3) {
            $query .= "     SUBSTR(SUBCLASSCD,1,2) in ('01','03','05') AND ";
        } else if ($kamoku == 5) {
            $query .= "     SUBSTR(SUBCLASSCD,1,2) in ('01','02','03','04','05') AND ";
        } else {
            $query .= "     SUBSTR(SUBCLASSCD,1,2) in ('01','02','03','04','05','06','07','08','09') AND ";
        }
        $query .= "     SEM1_REC is not null ";
        $query .= " GROUP BY ";
        $query .= "     schregno ";
        $query .= " ), ";
        $query .= " etc3test2 (SCHREGNO) as ( ";
        $query .= " SELECT ";
        $query .= "     t1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     sctable t1 ";
        $query .= " WHERE ";
        if ($kamoku == 3) {
            $query .= "     not exists (select 'x' from etc3test t2 where t1.schregno = t2.schregno and t2.CNTSUB < 3 ) ";
        } else if ($kamoku == 5) {
            $query .= "     not exists (select 'x' from etc3test t2 where t1.schregno = t2.schregno and t2.CNTSUB < 5 ) ";
        } else {
            $query .= "     not exists (select 'x' from etc3test t2 where t1.schregno = t2.schregno and t2.CNTSUB < 9 ) ";
        }
        $query .= " GROUP BY ";
        $query .= "     t1.SCHREGNO ";
        $query .= " ), ";
        $query .= "  rnktable(SCHREGNO,S1SMRNK) as ( ";
        $query .= "  SELECT ";
        $query .= "      t1.SCHREGNO, ";
        $query .= "      RANK() OVER(ORDER BY (value(round(avg(float(t1.SEM1_REC))*10,0      )/10,-1)) desc) as S1SMRNK ";
        $query .= "  FROM ";
        $query .= "      KIN_RECORD_DAT t1, ";
        $query .= "      etc3test2 t2 ";
        $query .= "  WHERE ";
        $query .= "      t1.YEAR = '" .$year. "' AND ";
        if ($kamoku == 3) {
            $query .= "      SUBSTR(t1.SUBCLASSCD,1,2) in ('01','03','05') AND ";
        } else if ($kamoku == 5) {
            $query .= "      SUBSTR(t1.SUBCLASSCD,1,2) in ('01','02','03','04','05') AND ";
        } else {
            $query .= "      SUBSTR(t1.SUBCLASSCD,1,2) in ('01','02','03','04','05','06','07','08','09') AND ";
        }
        $query .= "      t1.SCHREGNO = t2.SCHREGNO ";
        $query .= "  GROUP BY ";
        $query .= "      t1.SCHREGNO ";
        $query .= "  ) ";
        $query .= "  SELECT ";
        $query .= "      t1.S1SMRNK ";
        $query .= "  FROM rnktable t1";
        $query .= "  WHERE ";
        $query .= "      t1.SCHREGNO = '" .$model->schregno. "' ";

        return $query;

    }
    function get_pdcm7($model,$subcd,$year,$seme,$grd,$kamoku)
    {
        $query  = " with courseget(COURSECODE,COURSE_SEQ,GRADE) as (SELECT ";
        $query .= "      t1.COURSECODE ,t2.COURSE_SEQ,t2.GRADE ";
        $query .= "  FROM ";
        $query .= "      SCHREG_REGD_DAT t1 left join COURSE_GROUP_DAT t2 on t1.COURSECODE = t2.COURSECODE ";
        $query .= "   AND t2.year = '" .$year. "' ";
        $query .= "   AND t1.GRADE = t2.GRADE ";
        $query .= "  WHERE ";
        $query .= "      t1.SCHREGNO = '" .$model->schregno. "' AND ";
        $query .= "      t1.year = '" .$year. "' AND ";
        $query .= "      t1.semester = '" .$seme. "' ";
        $query .= "  ), ";
        $query .= "  course(COURSECODE,GRADE) as (SELECT ";
        $query .= "      t1.COURSECODE,t1.GRADE ";
        $query .= "  FROM ";
        $query .= "      COURSE_GROUP_DAT t1,courseget t2 ";
        $query .= "  WHERE ";
        $query .= "      t1.YEAR = '" .$year. "' ";
        $query .= "      AND t1.GRADE = t2.GRADE ";
        $query .= "      AND t1.COURSE_SEQ = t2.COURSE_SEQ ";
        $query .= "  ), ";
        $query .= "  sctable(SCHREGNO) as (SELECT ";
        $query .= "                              SCHREGNO ";
        $query .= "                          FROM ";
        $query .= "                              SCHREG_REGD_DAT t1, ";
        $query .= "                              course t2 ";
        $query .= "                          WHERE ";
        $query .= "                              t1.year = '" .$year. "' AND ";
        $query .= "                              t1.semester = '" .$seme. "'AND ";
        $query .= "                              t1.COURSECODE = t2.COURSECODE AND ";
        $query .= "                              t1.GRADE = t2.GRADE ";
        $query .= "  ), ";
        $query .= " etc3test (SCHREGNO,CNTSUB) as ( ";
        $query .= " select ";
        $query .= "     schregno,count(SUBCLASSCD) ";
        $query .= " from ";
        $query .= "     kin_record_dat ";
        $query .= " where ";
        $query .= "     YEAR = '" .$year. "' AND ";
        if ($kamoku == 3) {
            $query .= "     SUBSTR(SUBCLASSCD,1,2) in ('01','03','05') AND ";
        } else if ($kamoku == 5) {
            $query .= "     SUBSTR(SUBCLASSCD,1,2) in ('01','02','03','04','05') AND ";
        } else {
            $query .= "     SUBSTR(SUBCLASSCD,1,2) in ('01','02','03','04','05','06','07','08','09') AND ";
        }
        $query .= "     SEM2_REC is not null ";
        $query .= " GROUP BY ";
        $query .= "     schregno ";
        $query .= " ), ";
        $query .= " etc3test2 (SCHREGNO) as ( ";
        $query .= " SELECT ";
        $query .= "     t1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     sctable t1 ";
        $query .= " WHERE ";
        if ($kamoku == 3) {
            $query .= "     not exists (select 'x' from etc3test t2 where t1.schregno = t2.schregno and t2.CNTSUB < 3 ) ";
        } else if ($kamoku == 5) {
            $query .= "     not exists (select 'x' from etc3test t2 where t1.schregno = t2.schregno and t2.CNTSUB < 5 ) ";
        } else {
            $query .= "     not exists (select 'x' from etc3test t2 where t1.schregno = t2.schregno and t2.CNTSUB < 9 ) ";
        }
        $query .= " GROUP BY ";
        $query .= "     t1.SCHREGNO ";
        $query .= " ), ";
        $query .= "  rnktable(SCHREGNO,S2SMRNK) as ( ";
        $query .= "  SELECT ";
        $query .= "      t1.SCHREGNO, ";
        $query .= "      RANK() OVER(ORDER BY (value(round(avg(float(t1.SEM2_REC))*10,0      )/10,-1)) desc) as S2SMRNK ";
        $query .= "  FROM ";
        $query .= "      KIN_RECORD_DAT t1, ";
        $query .= "      etc3test2 t2 ";
        $query .= "  WHERE ";
        $query .= "      t1.YEAR = '" .$year. "' AND ";
        if ($kamoku == 3) {
            $query .= "      SUBSTR(t1.SUBCLASSCD,1,2) in ('01','03','05') AND ";
        } else if ($kamoku == 5) {
            $query .= "      SUBSTR(t1.SUBCLASSCD,1,2) in ('01','02','03','04','05') AND ";
        } else {
            $query .= "      SUBSTR(t1.SUBCLASSCD,1,2) in ('01','02','03','04','05','06','07','08','09') AND ";
        }
        $query .= "      t1.SCHREGNO = t2.SCHREGNO ";
        $query .= "  GROUP BY ";
        $query .= "      t1.SCHREGNO ";
        $query .= "  ) ";
        $query .= "  SELECT ";
        $query .= "      t1.S2SMRNK ";
        $query .= "  FROM rnktable t1";
        $query .= "  WHERE ";
        $query .= "      t1.SCHREGNO = '" .$model->schregno. "' ";

        return $query;

    }
    function get_pdcm8($model,$subcd,$year,$seme,$grd,$kamoku)
    {
        $query  = " with courseget(COURSECODE,COURSE_SEQ,GRADE) as (SELECT ";
        $query .= "      t1.COURSECODE ,t2.COURSE_SEQ,t2.GRADE ";
        $query .= "  FROM ";
        $query .= "      SCHREG_REGD_DAT t1 left join COURSE_GROUP_DAT t2 on t1.COURSECODE = t2.COURSECODE ";
        $query .= "   AND t2.year = '" .$year. "' ";
        $query .= "   AND t1.GRADE = t2.GRADE ";
        $query .= "  WHERE ";
        $query .= "      t1.SCHREGNO = '" .$model->schregno. "' AND ";
        $query .= "      t1.year = '" .$year. "' AND ";
        $query .= "      t1.semester = '" .$seme. "' ";
        $query .= "  ), ";
        $query .= "  course(COURSECODE,GRADE) as (SELECT ";
        $query .= "      t1.COURSECODE,t1.GRADE ";
        $query .= "  FROM ";
        $query .= "      COURSE_GROUP_DAT t1,courseget t2 ";
        $query .= "  WHERE ";
        $query .= "      t1.YEAR = '" .$year. "' ";
        $query .= "      AND t1.GRADE = t2.GRADE ";
        $query .= "      AND t1.COURSE_SEQ = t2.COURSE_SEQ ";
        $query .= "  ), ";
        $query .= "  sctable(SCHREGNO) as (SELECT ";
        $query .= "                              SCHREGNO ";
        $query .= "                          FROM ";
        $query .= "                              SCHREG_REGD_DAT t1, ";
        $query .= "                              course t2 ";
        $query .= "                          WHERE ";
        $query .= "                              t1.year = '" .$year. "' AND ";
        $query .= "                              t1.semester = '" .$seme. "'AND ";
        $query .= "                              t1.COURSECODE = t2.COURSECODE AND ";
        $query .= "                              t1.GRADE = t2.GRADE ";
        $query .= "  ), ";
        $query .= " etc3test (SCHREGNO,CNTSUB) as ( ";
        $query .= " select ";
        $query .= "     schregno,count(SUBCLASSCD) ";
        $query .= " from ";
        $query .= "     kin_record_dat ";
        $query .= " where ";
        $query .= "     YEAR = '" .$year. "' AND ";
        if ($kamoku == 3) {
            $query .= "     SUBSTR(SUBCLASSCD,1,2) in ('01','03','05') AND ";
        } else if ($kamoku == 5) {
            $query .= "     SUBSTR(SUBCLASSCD,1,2) in ('01','02','03','04','05') AND ";
        } else {
            $query .= "     SUBSTR(SUBCLASSCD,1,2) in ('01','02','03','04','05','06','07','08','09') AND ";
        }
        $query .= "     SEM3_REC is not null ";
        $query .= " GROUP BY ";
        $query .= "     schregno ";
        $query .= " ), ";
        $query .= " etc3test2 (SCHREGNO) as ( ";
        $query .= " SELECT ";
        $query .= "     t1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     sctable t1 ";
        $query .= " WHERE ";
        if ($kamoku == 3) {
            $query .= "     not exists (select 'x' from etc3test t2 where t1.schregno = t2.schregno and t2.CNTSUB < 3 ) ";
        } else if ($kamoku == 5) {
            $query .= "     not exists (select 'x' from etc3test t2 where t1.schregno = t2.schregno and t2.CNTSUB < 5 ) ";
        } else {
            $query .= "     not exists (select 'x' from etc3test t2 where t1.schregno = t2.schregno and t2.CNTSUB < 9 ) ";
        }
        $query .= " GROUP BY ";
        $query .= "     t1.SCHREGNO ";
        $query .= " ), ";
        $query .= "  rnktable(SCHREGNO,S3SMRNK) as ( ";
        $query .= "  SELECT ";
        $query .= "      t1.SCHREGNO, ";
        $query .= "      RANK() OVER(ORDER BY (value(round(avg(float(t1.SEM3_REC))*10,0      )/10,-1)) desc) as S3SMRNK ";
        $query .= "  FROM ";
        $query .= "      KIN_RECORD_DAT t1, ";
        $query .= "      etc3test2 t2 ";
        $query .= "  WHERE ";
        $query .= "      t1.YEAR = '" .$year. "' AND ";
        if ($kamoku == 3) {
            $query .= "      SUBSTR(t1.SUBCLASSCD,1,2) in ('01','03','05') AND ";
        } else if ($kamoku == 5) {
            $query .= "      SUBSTR(t1.SUBCLASSCD,1,2) in ('01','02','03','04','05') AND ";
        } else {
            $query .= "      SUBSTR(t1.SUBCLASSCD,1,2) in ('01','02','03','04','05','06','07','08','09') AND ";
        }
        $query .= "      t1.SCHREGNO = t2.SCHREGNO ";
        $query .= "  GROUP BY ";
        $query .= "      t1.SCHREGNO ";
        $query .= "  ) ";
        $query .= "  SELECT ";
        $query .= "      t1.S3SMRNK ";
        $query .= "  FROM rnktable t1";
        $query .= "  WHERE ";
        $query .= "      t1.SCHREGNO = '" .$model->schregno. "' ";

        return $query;

    }
    function get_pdcm9($model,$subcd,$year,$seme,$grd,$kamoku)
    {
        $query  = " with courseget(COURSECODE,COURSE_SEQ,GRADE) as (SELECT ";
        $query .= "      t1.COURSECODE ,t2.COURSE_SEQ,t2.GRADE ";
        $query .= "  FROM ";
        $query .= "      SCHREG_REGD_DAT t1 left join COURSE_GROUP_DAT t2 on t1.COURSECODE = t2.COURSECODE ";
        $query .= "   AND t2.year = '" .$year. "' ";
        $query .= "   AND t1.GRADE = t2.GRADE ";
        $query .= "  WHERE ";
        $query .= "      t1.SCHREGNO = '" .$model->schregno. "' AND ";
        $query .= "      t1.year = '" .$year. "' AND ";
        $query .= "      t1.semester = '" .$seme. "' ";
        $query .= "  ), ";
        $query .= "  course(COURSECODE,GRADE) as (SELECT ";
        $query .= "      t1.COURSECODE,t1.GRADE ";
        $query .= "  FROM ";
        $query .= "      COURSE_GROUP_DAT t1,courseget t2 ";
        $query .= "  WHERE ";
        $query .= "      t1.YEAR = '" .$year. "' ";
        $query .= "      AND t1.GRADE = t2.GRADE ";
        $query .= "      AND t1.COURSE_SEQ = t2.COURSE_SEQ ";
        $query .= "  ), ";
        $query .= "  sctable(SCHREGNO) as (SELECT ";
        $query .= "                              SCHREGNO ";
        $query .= "                          FROM ";
        $query .= "                              SCHREG_REGD_DAT t1, ";
        $query .= "                              course t2 ";
        $query .= "                          WHERE ";
        $query .= "                              t1.year = '" .$year. "' AND ";
        $query .= "                              t1.semester = '" .$seme. "'AND ";
        $query .= "                              t1.COURSECODE = t2.COURSECODE AND ";
        $query .= "                              t1.GRADE = t2.GRADE ";
        $query .= "  ), ";
        $query .= " etc3test (SCHREGNO,CNTSUB) as ( ";
        $query .= " select ";
        $query .= "     schregno,count(SUBCLASSCD) ";
        $query .= " from ";
        $query .= "     kin_record_dat ";
        $query .= " where ";
        $query .= "     YEAR = '" .$year. "' AND ";
        if ($kamoku == 3) {
            $query .= "     SUBSTR(SUBCLASSCD,1,2) in ('01','03','05') AND ";
        } else if ($kamoku == 5) {
            $query .= "     SUBSTR(SUBCLASSCD,1,2) in ('01','02','03','04','05') AND ";
        } else {
            $query .= "     SUBSTR(SUBCLASSCD,1,2) in ('01','02','03','04','05','06','07','08','09') AND ";
        }
        $query .= "     GRADE_RECORD is not null ";
        $query .= " GROUP BY ";
        $query .= "     schregno ";
        $query .= " ), ";
        $query .= " etc3test2 (SCHREGNO) as ( ";
        $query .= " SELECT ";
        $query .= "     t1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     sctable t1 ";
        $query .= " WHERE ";
        if ($kamoku == 3) {
            $query .= "     not exists (select 'x' from etc3test t2 where t1.schregno = t2.schregno and t2.CNTSUB < 3 ) ";
        } else if($kamoku == 5) {
            $query .= "     not exists (select 'x' from etc3test t2 where t1.schregno = t2.schregno and t2.CNTSUB < 5 ) ";
        } else {
            $query .= "     not exists (select 'x' from etc3test t2 where t1.schregno = t2.schregno and t2.CNTSUB < 9 ) ";
        }
        $query .= " GROUP BY ";
        $query .= "     t1.SCHREGNO ";
        $query .= " ), ";
        $query .= "  rnktable(SCHREGNO,S3GMRNK) as ( ";
        $query .= "  SELECT ";
        $query .= "      t1.SCHREGNO, ";
        $query .= "      RANK() OVER(ORDER BY (value(round(avg(float(t1.GRADE_RECORD))*10,0  )/10,-1)) desc) as S3GMRNK ";
        $query .= "  FROM ";
        $query .= "      KIN_RECORD_DAT t1, ";
        $query .= "      etc3test2 t2 ";
        $query .= "  WHERE ";
        $query .= "      t1.YEAR = '" .$year. "' AND ";
        if ($kamoku == 3) {
            $query .= "      SUBSTR(t1.SUBCLASSCD,1,2) in ('01','03','05') AND ";
        } else if ($kamoku == 5) {
            $query .= "      SUBSTR(t1.SUBCLASSCD,1,2) in ('01','02','03','04','05') AND ";
        } else {
            $query .= "      SUBSTR(t1.SUBCLASSCD,1,2) in ('01','02','03','04','05','06','07','08','09') AND ";
        }
        $query .= "      t1.SCHREGNO = t2.SCHREGNO ";
        $query .= "  GROUP BY ";
        $query .= "      t1.SCHREGNO ";
        $query .= "  ) ";
        $query .= "  SELECT ";
        $query .= "      t1.S3GMRNK";
        $query .= "  FROM rnktable t1";
        $query .= "  WHERE ";
        $query .= "      t1.SCHREGNO = '" .$model->schregno. "' ";

        return $query;

    }
}
?>