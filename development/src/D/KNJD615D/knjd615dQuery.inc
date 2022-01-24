<?php

require_once('for_php7.php');

class knjd615dQuery extends Query {
    //中学なのか高校なのか
    function getGdat($grade) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' AND ";
        $query .= "     GRADE = '$grade' ";

        return $query;
    }

    //学校名
    function getSchoolname() {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";
        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model) {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR='" .$model->control["年度"] ."'";
            if ($model->field["SEMESTER"]=="9") {
                $query .= "AND SEMESTER='".CTRL_SEMESTER ."' ";
            } else {
                $query .= "AND SEMESTER='".$model->field["SEMESTER"] ."' ";
            }
            $query .= "AND GRADE='".$model->field["GRADE"] ."'";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR='" .$model->control["年度"] ."'";
            if ($model->field["SEMESTER"]=="9") {
                $query .= "AND SEMESTER='".CTRL_SEMESTER ."' ";
            } else {
                $query .= "AND SEMESTER='".$model->field["SEMESTER"] ."' ";
            }
            $query .= "AND GRADE='".$model->field["GRADE"] ."'";
            $query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= "    OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= "    OR TR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }

    //学年取得
    function getSelectGrade($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND IN ('J','H') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期取得
    function getSemester()
    {
        $query  = "SELECT ";
        $query .= "    SEMESTERNAME AS LABEL, ";
        $query .= "    SEMESTER AS VALUE ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "ORDER BY ";
        $query .= "    SEMESTER ";

        return $query;
    }

    //テスト種別取得
    function getTestItem($semester)
    {
        $query = "WITH MAIN_T(LABEL, VALUE, KINDCD, ITEMCD) AS ( ";
        $query.= "SELECT ";
        $query.= "    TESTKINDCD || TESTITEMCD || '　' || TESTITEMNAME, ";
        $query.= "    TESTKINDCD || TESTITEMCD, ";
        $query.= "    TESTKINDCD, ";
        $query.= "    TESTITEMCD ";
        $query.= "FROM ";
        $query.= "    TESTITEM_MST_COUNTFLG_NEW ";
        $query.= "WHERE ";
        $query.= "    YEAR = '".CTRL_YEAR."' AND ";
        $query.= "    SEMESTER = '".$semester."' ";
        $query.= ") ";
        $query.= "SELECT ";
        $query.= "    LABEL, ";
        $query.= "    VALUE ";
        $query.= "FROM ";
        $query.= "    MAIN_T ";
        $query.= "ORDER BY ";
        $query.= "    KINDCD, ";
        $query.= "    ITEMCD ";

        return $query;
    }

    function getSubClass($model) {
        //変数
        $year = CTRL_YEAR;
        $selectSeme = $model->field["SEMESTER"];
        $schregSeme = ($selectSeme == "9") ? CTRL_SEMESTER : $selectSeme;
        $arrData = explode(",", $model->selectdata);
        $gradeHrClassIn = implode($arrData, "','");
        $sogoCd = ($model->field["OUTPUT_SOUGOU"] != "") ? "89" : "90"; //総合的な学習の時間を表示しない
        //SQL
        $query  = "";
        $query .= " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD AS VALUE, ";
        } else {
            $query .= "     T2.SUBCLASSCD AS VALUE, ";
        }
        $query .= "     value(T4.ELECTDIV,'0') as ELECTDIV, ";
        $query .= "     T4.SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1 ";
        $query .= "     INNER JOIN CHAIR_DAT T2 ON T2.YEAR = '{$year}' ";
        $query .= "                            AND T2.SEMESTER = '{$schregSeme}' ";
        $query .= "                            AND T2.CHAIRCD = T1.CHAIRCD ";
        $query .= "                            AND SUBSTR(T2.SUBCLASSCD,1,2) <= '{$sogoCd}' ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T3 ON T3.YEAR = '{$year}' ";
        $query .= "                                  AND T3.SEMESTER = '{$schregSeme}' ";
        $query .= "                                  AND T3.GRADE || T3.HR_CLASS IN ('{$gradeHrClassIn}') ";
        $query .= "                                  AND T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN SUBCLASS_MST T4 ON T4.SUBCLASSCD = T2.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                              AND T4.CLASSCD         = T2.CLASSCD ";
            $query .= "                              AND T4.SCHOOL_KIND     = T2.SCHOOL_KIND ";
            $query .= "                              AND T4.CURRICULUM_CD   = T2.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' AND ";
        $query .= "     T1.SEMESTER = '{$schregSeme}' ";
        $query .= " GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD, ";
            $query .= "     T2.SCHOOL_KIND, ";
            $query .= "     T2.CURRICULUM_CD, ";
        }
        $query .= "     T2.SUBCLASSCD, ";
        $query .= "     value(T4.ELECTDIV,'0'), ";
        $query .= "     T4.SUBCLASSNAME ";
        $query .= " ORDER BY ";
        $query .= "     value(T4.ELECTDIV,'0'), ";
        $query .= "     VALUE ";
        return $query;
    }

    //出欠集計開始日付などを取得
    function getAttendDate($model) {
        //変数
        $year = CTRL_YEAR;
        $selectSeme = $model->field["SEMESTER"];
        //SQL
        $query  = "";
        $query .= "SELECT ";
        $query .= "    SEMESTER, ";
        $query .= "    MAX(CASE WHEN MONTH BETWEEN '01' AND '03' THEN RTRIM(CHAR(INT(YEAR)+1)) ELSE YEAR END) AS MAX_YEAR, ";
        $query .= "    MONTH, ";
        $query .= "    MAX(APPOINTED_DAY) AS MAX_APP ";
        $query .= "FROM ";
        $query .= "    ATTEND_SEMES_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR='{$year}' ";
        $query .= "    AND SEMESTER <= '{$selectSeme}' ";
        $query .= "GROUP BY ";
        $query .= "    SEMESTER, ";
        $query .= "    MONTH ";
        $query .= "ORDER BY ";
        $query .= "    2, ";
        $query .= "    1, ";
        $query .= "    3 ";
        return $query;
    }

    //学校マスタの取得
    function getSchoolMst() {
        //変数
        $year = CTRL_YEAR;
        //SQL
        $query  = "";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        return $query;
    }

    //CSV出力
    function getCsvQuery($model, $semester, $month, $attend_sdate, $knjSchoolMst)
    {
        //変数
        $year = CTRL_YEAR;
        $selectSeme = $model->field["SEMESTER"];
        $selectTest = ($model->field["TESTKINDCD"] == "9909") ? "9900" : $model->field["TESTKINDCD"];
        $scoreDiv = ($selectTest == "9900") ? "00" : "01";
        $schregSeme = ($selectSeme == "9") ? CTRL_SEMESTER : $selectSeme;
        $arrData = explode(",", $model->selectdata);
        $gradeHrClassIn = implode($arrData, "','");

        //対象生徒
        $query  = "WITH SCH_T AS ( ";
        $query .= "SELECT ";
        $query .= "    schregno ,grade ,hr_class ,attendno ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '{$year}' AND ";
        $query .= "    SEMESTER = '{$schregSeme}' AND ";
        $query .= "    GRADE || HR_CLASS IN ('{$gradeHrClassIn}') ";
        $query .= ") ";

        //講座名簿データ
        $query .= " , T_CHAIR AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD, ";
            $query .= "     T2.SCHOOL_KIND, ";
            $query .= "     T2.CURRICULUM_CD, ";
        }
        $query .= "         T2.SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT T1 ";
        $query .= "         INNER JOIN CHAIR_DAT T2 ON T2.YEAR = '{$year}' ";
        $query .= "                                AND T2.SEMESTER = '{$schregSeme}' ";
        $query .= "                                AND T2.CHAIRCD = T1.CHAIRCD ";
        $query .= "         INNER JOIN SCH_T T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '{$year}' AND ";
        $query .= "         T1.SEMESTER = '{$schregSeme}' ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD, ";
            $query .= "     T2.SCHOOL_KIND, ";
            $query .= "     T2.CURRICULUM_CD, ";
        }
        $query .= "         T2.SUBCLASSCD ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         T3.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     '-' AS CLASSCD, ";
            $query .= "     '-'  AS SCHOOL_KIND, ";
            $query .= "     '-'  AS CURRICULUM_CD, ";
        }
        $query .= "         '999999' AS SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "         SCH_T T3 ";
        $query .= "     ) ";

        //出欠データ
        $query .= knjd615dQuery::getAttendHasuu($model, $semester, $month, $attend_sdate, $knjSchoolMst);
        $query .= ", ATTEND_T AS ( ";
        $query .= knjd615dQuery::getAttendMain($model, $knjSchoolMst);
        $query .= ") ";

        //成績席次データ
        $query .= " , RECORD_RANK AS ( ";
        $query .= "     SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         SCHREGNO, ";
        $query .= "         SCORE, ";
        $query .= "         DECIMAL(ROUND(FLOAT(AVG) * 10, 0) /10, 5, 1)  AS AVG, ";
        //順位の基準点 1:総合点 2:平均点
        if ($model->field["OUTPUT_KIJUN"] == "1") {
            $query .= "     CLASS_RANK, ";
            //総合順位出力 2:学年 3:コース
            if ($model->field["OUTPUT_RANK"] == "2") {
                $query .= " GRADE_RANK AS RANK ";
            } else if ($model->field["OUTPUT_RANK"] == "5") {
                $query .= " MAJOR_RANK AS RANK ";
            } else {
                $query .= " COURSE_RANK AS RANK ";
            }
        } else {
            $query .= "     CLASS_AVG_RANK AS CLASS_RANK, ";
            //総合順位出力 2:学年 3:コース
            if ($model->field["OUTPUT_RANK"] == "2") {
                $query .= " GRADE_AVG_RANK AS RANK ";
            } else if ($model->field["OUTPUT_RANK"] == "5") {
                $query .= " MAJOR_AVG_RANK AS RANK ";
            } else {
                $query .= " COURSE_AVG_RANK AS RANK ";
            }
        }
        $query .= "     FROM ";
        $query .= "         RECORD_RANK_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' AND ";
        $query .= "         SEMESTER = '{$selectSeme}' AND ";
        $query .= "         TESTKINDCD || TESTITEMCD = '{$selectTest}' ";
        $query .= " ) ";

        //成績データ
        if ($model->field["TESTKINDCD"] == "9909") {
            $query .= " , RECORD_SCORE AS ( ";
            $query .= "     SELECT ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     CLASSCD, ";
                $query .= "     SCHOOL_KIND, ";
                $query .= "     CURRICULUM_CD, ";
            }
            $query .= "         SUBCLASSCD, ";
            $query .= "         SCHREGNO, ";
            $query .= "         VALUE ";
            $query .= "     FROM ";
            $query .= "         RECORD_SCORE_DAT ";
            $query .= "     WHERE ";
            $query .= "         YEAR = '{$year}' AND ";
            $query .= "         SEMESTER = '{$selectSeme}' AND ";
            $query .= "         TESTKINDCD || TESTITEMCD = '{$selectTest}' AND ";
            $query .= "         SCORE_DIV = '{$scoreDiv}' ";
            $query .= " ) ";
        }

        //メインデータ
        $query .= "SELECT ";
        $query .= "    '' as YEAR ,'' as SEMESTER ";
        $query .= "    ,T1.schregno ";
        $query .= "    ,T1.grade ,T1.hr_class ,T1.attendno ";
        $query .= "    ,T2.name ";
        $query .= "    ,'' as TESTNAME ";
        $scoreName = ($model->field["TESTKINDCD"] == "9909") ? "R2.VALUE" : "R1.SCORE";
        for ($i = 0; $i < get_count($model->arrSubclass); $i++) {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    ,SUM( CASE T3.SUBCLASSCD WHEN '".substr($model->arrSubclass[$i], 7, 6)."' THEN ".$scoreName." ELSE NULL END ) as SCORE" .$i ." ";
                $query .= "    ,SUM( CASE T3.SUBCLASSCD WHEN '".substr($model->arrSubclass[$i], 7, 6)."' THEN L1.NOTICE_LATE ELSE NULL END) as DI" .$i ." ";
            } else {
                $query .= "    ,SUM( CASE T3.SUBCLASSCD WHEN '".$model->arrSubclass[$i]."' THEN ".$scoreName." ELSE NULL END ) as SCORE" .$i ." ";
                $query .= "    ,SUM( CASE T3.SUBCLASSCD WHEN '".$model->arrSubclass[$i]."' THEN L1.NOTICE_LATE ELSE NULL END) as DI" .$i ." ";
            }
        }
        $query .= "    ,SUM( CASE T3.SUBCLASSCD WHEN '999999' THEN R1.SCORE ELSE NULL END ) as TOTAL ";
        $query .= "    ,SUM( CASE T3.SUBCLASSCD WHEN '999999' THEN R1.AVG ELSE NULL END ) as AVG ";
        $query .= "    ,SUM( CASE T3.SUBCLASSCD WHEN '999999' THEN R1.CLASS_RANK ELSE NULL END ) as CLASS_RANK ";
        $query .= "    ,SUM( CASE T3.SUBCLASSCD WHEN '999999' THEN R1.RANK ELSE NULL END ) as RANK ";
        $query .= "    ,'' as KETTEN ";
        $query .= "FROM ";
        $query .= "    SCH_T T1 ";
        $query .= "    inner join schreg_base_mst T2 on T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "    LEFT JOIN T_CHAIR T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "    LEFT JOIN ATTEND_T L1 ON L1.SCHREGNO = T3.SCHREGNO ";
        $query .= "                         AND L1.SUBCLASSCD = T3.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                         AND L1.CLASSCD          = T3.CLASSCD ";
            $query .= "                         AND L1.SCHOOL_KIND      = T3.SCHOOL_KIND ";
            $query .= "                         AND L1.CURRICULUM_CD    = T3.CURRICULUM_CD ";
        }
        $query .= "    LEFT JOIN RECORD_RANK R1 ON R1.SCHREGNO = T3.SCHREGNO ";
        $query .= "                            AND R1.SUBCLASSCD = T3.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                         AND (R1.SUBCLASSCD = '999999' OR  ";
            $query .= "                             R1.CLASSCD          = T3.CLASSCD ";
            $query .= "                         AND R1.SCHOOL_KIND      = T3.SCHOOL_KIND ";
            $query .= "                         AND R1.CURRICULUM_CD    = T3.CURRICULUM_CD ";
            $query .= "                             ) ";
        }
        if ($model->field["TESTKINDCD"] == "9909") {
            $query .= "    LEFT JOIN RECORD_SCORE R2 ON R2.SCHREGNO = T3.SCHREGNO ";
            $query .= "                             AND R2.SUBCLASSCD = T3.SUBCLASSCD ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                         AND R2.CLASSCD          = T3.CLASSCD ";
                $query .= "                         AND R2.SCHOOL_KIND      = T3.SCHOOL_KIND ";
                $query .= "                         AND R2.CURRICULUM_CD    = T3.CURRICULUM_CD ";
            }
        }
        $query .= "GROUP BY ";
        $query .= "    T1.schregno,T1.grade,T1.hr_class,T1.attendno,T2.name ";
        $query .= "ORDER BY ";
        $query .= "    T1.grade,T1.hr_class,T1.attendno ";

        return $query;
    }

    //端数処理
    function getAttendHasuu($model, $semester, $month, $attend_sdate, $knjSchoolMst)
    {
        //変数
        $year = CTRL_YEAR;
        $attend_edate = str_replace("/", "-", $model->field["DATE"]);
        //SQL
        $query  = ",ATTEND_SUBCLASS AS ( ";
        $query .= "    SELECT * ";
        $query .= "    FROM   ATTEND_SUBCLASS_DAT ";
        $query .= "    WHERE  YEAR='{$year}' ";
        $query .= "      AND  SEMESTER <= '{$semester}' ";
        $query .= "      AND  MONTH IN ('" .implode($month, "','") ."') ";
        $query .= "    ) ";
        $query .= ",SCHEDULE AS( ";
        $query .= "    SELECT EXECUTEDATE, PERIODCD, CHAIRCD, DATADIV ";
        $query .= "    FROM   SCH_CHR_DAT  ";
        $query .= "    WHERE  EXECUTEDATE BETWEEN DATE('{$attend_sdate}') AND DATE('{$attend_edate}')  ";
        $query .= "      AND  PERIODCD != '0'  ";
        $query .= "    ) ";
        $query .= ",T_attend_dat AS( ";
        $query .= "     SELECT W1.SCHREGNO,W1.ATTENDDATE,W1.PERIODCD, ";
        $query .= "            CASE WHEN L1.ATSUB_REPL_DI_CD IS NOT NULL THEN L1.ATSUB_REPL_DI_CD ELSE L1.REP_DI_CD END AS DI_CD, ";
        $query .= "            L1.MULTIPLY  ";
        $query .= "     FROM   ATTEND_DAT W1  ";
        $query .= "            LEFT JOIN ATTEND_DI_CD_DAT L1 ON W1.YEAR = L1.YEAR AND L1.DI_CD = W1.DI_CD ";
        $query .= "     WHERE  W1.ATTENDDATE BETWEEN DATE('{$attend_sdate}') AND DATE('{$attend_edate}')  ";
        $query .= "            AND NOT EXISTS(SELECT 'X' FROM SCHREG_TRANSFER_DAT T7  ";
        $query .= "                           WHERE  T7.SCHREGNO = W1.SCHREGNO  ";
        $query .= "                             AND  T7.TRANSFERCD IN('1','2')  ";
        $query .= "                             AND  W1.ATTENDDATE BETWEEN T7.TRANSFER_SDATE AND T7.TRANSFER_EDATE )  ";
        $query .= "    ) ";
        //テスト項目マスタの集計フラグの表
        $query .= " , TEST_COUNTFLG AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.EXECUTEDATE, ";
        $query .= "         T1.PERIODCD, ";
        $query .= "         T1.CHAIRCD, ";
        $query .= "         '2' AS DATADIV ";
        $query .= "     FROM ";
        $query .= "         SCH_CHR_TEST T1, ";
        $query .= "         TESTITEM_MST_COUNTFLG_NEW T2 ";
        $query .= "     WHERE ";
        $query .= "             T2.YEAR       = T1.YEAR ";
        $query .= "         AND T2.SEMESTER   = T1.SEMESTER ";
        $query .= "         AND T2.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "         AND T2.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "         AND T2.COUNTFLG   = '0' "; //0：集計しない 0以外：集計する
        $query .= "     ) ";
        $query .= ",T_attend AS( ";
        $query .= "SELECT S1.SCHREGNO  ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        ,S1.CLASSCD ";
            $query .= "        ,S1.SCHOOL_KIND ";
            $query .= "        ,S1.CURRICULUM_CD ";
        }
        $query .= "      ,S1.SUBCLASSCD  ";
        $query .= "      ,S1.SEMESTER  ";
        $query .= "      ,COUNT(*)  AS LESSON  ";
        $query .= "      ,SUM(CASE WHEN S3.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END)  AS  OFFDAYS  ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN  '1' THEN 1 WHEN '8' THEN 1 ELSE 0 END)  AS  ABSENT  ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN  '2' THEN 1 WHEN '9' THEN 1 ELSE 0 END)  AS  SUSPEND  ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN  '3' THEN 1 WHEN '10' THEN 1 ELSE 0 END)  AS  MOURNING  ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN  '4' THEN 1 WHEN '11' THEN 1 ELSE 0 END)  AS  SICK  ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN '19' THEN 1 WHEN '20' THEN 1 ELSE 0 END)  AS  VIRUS  ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN '25' THEN 1 WHEN '26' THEN 1 ELSE 0 END)  AS  KOUDOME  ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN  '5' THEN 1 WHEN '12' THEN 1 ELSE 0 END)  AS  NOTICE  ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN  '6' THEN 1 WHEN '13' THEN 1 ELSE 0 END)  AS  NONOTICE  ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN '14' THEN 1 ELSE 0 END)  AS  NURSEOFF  ";
        $query .= "      ,SUM(CASE WHEN S2.DI_CD IN('15','23','24') THEN SMALLINT(VALUE(S2.MULTIPLY, '1')) ELSE 0 END)  AS  LATE  ";
        $query .= "      ,SUM(CASE S2.DI_CD WHEN '16' THEN 1 ELSE 0 END)  AS  EARLY  ";
        $query .= "FROM  ";
        $query .= "   ( ";
        $query .= "    SELECT ";
        $query .= "         T2.SCHREGNO, ";
        $query .= "         T1.EXECUTEDATE, ";
        $query .= "         T1.PERIODCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        } 
        $query .= "         SUBCLASSCD, ";
        $query .= "         T2.SEMESTER ";
        $query .= "  FROM   SCHEDULE T1  ";
        $query .= "        ,CHAIR_STD_DAT T2  ";
        $query .= "        ,CHAIR_DAT T3  ";
        $query .= "        ,SCH_T T4  ";
        $query .= "  WHERE  T1.CHAIRCD  = T3.CHAIRCD  ";
        $query .= "    AND  T3.YEAR     = '{$year}'  ";
        $query .= "    AND  T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE  ";
        $query .= "    AND  T2.YEAR     = '{$year}'  ";
        $query .= "    AND  T2.SEMESTER = T3.SEMESTER  ";
        $query .= "    AND  T2.CHAIRCD  = T1.CHAIRCD  ";
        $query .= "    AND  T4.SCHREGNO = T2.SCHREGNO  ";
        $query .= "    AND     NOT EXISTS(SELECT  'X' FROM SCH_CHR_COUNTFLG T5  ";
        $query .= "                    WHERE   T5.EXECUTEDATE = T1.EXECUTEDATE AND  ";
        $query .= "                            T5.PERIODCD = T1.PERIODCD AND  ";
        $query .= "                            T5.CHAIRCD = T1.CHAIRCD AND  ";
        $query .= "                            T5.GRADE = T4.GRADE AND  ";
        $query .= "                            T5.HR_CLASS = T4.HR_CLASS AND  ";
        $query .= "                            T1.DATADIV IN ('0','1') AND "; //テスト(DATADIV=2)以外
        $query .= "                            T5.COUNTFLG = '0') ";
        $query .= "    AND     NOT EXISTS(SELECT 'X' FROM TEST_COUNTFLG TEST ";
        $query .= "                        WHERE TEST.EXECUTEDATE = T1.EXECUTEDATE ";
        $query .= "                          AND TEST.PERIODCD    = T1.PERIODCD ";
        $query .= "                          AND TEST.CHAIRCD     = T1.CHAIRCD ";
        $query .= "                          AND TEST.DATADIV     = T1.DATADIV) "; //テスト(DATADIV=2)
        $query .= "    AND     NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T6  ";
        $query .= "                    WHERE   T6.SCHREGNO = T4.SCHREGNO AND  ";
        $query .= "                          ((T6.GRD_DIV IN('1','2','3') AND T6.GRD_DATE < T1.EXECUTEDATE) OR  ";
        $query .= "                           (T6.ENT_DIV IN('4','5') AND T6.ENT_DATE > T1.EXECUTEDATE)) )  ";
        $query .= "    AND NOT EXISTS(SELECT ";
        $query .= "                       'X' ";
        $query .= "                   FROM ";
        $query .= "                       ATTEND_DAT L4 ";
        $query .= "                       LEFT JOIN ATTEND_DI_CD_DAT ADD ON L4.YEAR  = ADD.YEAR ";
        $query .= "                                                     AND L4.DI_CD = ADD.DI_CD ";
        $query .= "                   WHERE ";
        $query .= "                           L4.SCHREGNO   = T2.SCHREGNO ";
        $query .= "                       AND L4.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "                       AND L4.PERIODCD   = T1.PERIODCD ";
        $query .= "                       AND ADD.REP_DI_CD = '27' "; // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
        $query .= "                  ) ";
        $query .= "    AND NOT EXISTS(SELECT ";
        $query .= "                       'X' ";
        $query .= "                   FROM ";
        $query .= "                       ATTEND_DAT L4 ";
        $query .= "                       LEFT JOIN ATTEND_DI_CD_DAT ADD ON L4.YEAR  = ADD.YEAR ";
        $query .= "                                                     AND L4.DI_CD = ADD.DI_CD ";
        $query .= "                   WHERE ";
        $query .= "                           L4.SCHREGNO   = T2.SCHREGNO ";
        $query .= "                       AND L4.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "                       AND L4.PERIODCD   = T1.PERIODCD ";
        $query .= "                       AND ADD.REP_DI_CD = '28' "; // 勤怠コード'28'は時間割にカウントしない
        $query .= "                  ) ";
        
        $query .= "  GROUP BY  ";
        $query .= "           T2.SCHREGNO, ";
        $query .= "           T1.EXECUTEDATE, ";
        $query .= "           T1.PERIODCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             CLASSCD, ";
            $query .= "             SCHOOL_KIND, ";
            $query .= "             CURRICULUM_CD, ";
        }
        $query .= "           SUBCLASSCD, ";
        $query .= "           T2.SEMESTER ";
        $query .= "     )S1  ";
        $query .= " LEFT JOIN T_attend_dat S2 ON S2.SCHREGNO = S1.SCHREGNO AND S2.ATTENDDATE = S1.EXECUTEDATE AND S2.PERIODCD = S1.PERIODCD  ";
        $query .= " LEFT JOIN SCHREG_TRANSFER_DAT S3 ON S3.SCHREGNO = S1.SCHREGNO AND S3.TRANSFERCD = '2' AND S1.EXECUTEDATE BETWEEN S3.TRANSFER_SDATE AND S3.TRANSFER_EDATE  ";
        $query .= " LEFT JOIN SCHREG_TRANSFER_DAT S4 ON S4.SCHREGNO = S1.SCHREGNO AND S4.TRANSFERCD = '1' AND S1.EXECUTEDATE BETWEEN S4.TRANSFER_SDATE AND S4.TRANSFER_EDATE  ";
        $query .= " GROUP BY  ";
        $query .= "           S1.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             S1.CLASSCD, ";
            $query .= "             S1.SCHOOL_KIND, ";
            $query .= "             S1.CURRICULUM_CD, ";
        }
        $query .= "           S1.SUBCLASSCD, ";
        $query .= "           S1.SEMESTER ";
        $query .= "    ) ";
        $query .= ",ATTEND_SUM AS( ";
        $query .= "    SELECT ";
        $query .= "           W1.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "           W1.CLASSCD, ";
            $query .= "           W1.SCHOOL_KIND, ";
            $query .= "           W1.CURRICULUM_CD, ";
        } 
        $query .= "           W1.SUBCLASSCD, ";
        $query .= "           W1.SEMESTER, ";
        $query .= "           SUM(value(W1.LESSON,0)) LESSON, ";
        $query .= "           SUM(value(W1.OFFDAYS,0)) OFFDAYS, ";
        $query .= "           SUM(value(W1.ABSENT,0)) ABSENT, ";
        $query .= "           SUM(value(W1.SUSPEND,0)) SUSPEND, ";
        $query .= "           SUM(value(W1.MOURNING,0)) MOURNING, ";
        $query .= "           SUM(value(W1.SICK,0)) SICK, ";
        if ($model->virus) {
            $query .= "           SUM(value(W1.VIRUS,0)) VIRUS, ";
        }
        if ($model->koudome) {
            $query .= "           SUM(value(W1.KOUDOME,0)) KOUDOME, ";
        }
        $query .= "           SUM(value(W1.NOTICE,0)) NOTICE, ";
        $query .= "           SUM(value(W1.NONOTICE,0)) NONOTICE, ";
        $query .= "           SUM(value(W1.NURSEOFF,0)) NURSEOFF, ";
        $query .= "           SUM(value(W1.LATE,0)) LATE, ";
        $query .= "           SUM(value(W1.EARLY,0)) EARLY ";
        $query .= "    FROM   ATTEND_SUBCLASS W1, SCH_T W0 ";
        $query .= "    WHERE  W1.SCHREGNO = W0.SCHREGNO ";
        $query .= "    GROUP BY  ";
        $query .= "           W1.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             W1.CLASSCD, ";
            $query .= "             W1.SCHOOL_KIND, ";
            $query .= "             W1.CURRICULUM_CD, ";
        }
        $query .= "           W1.SUBCLASSCD, ";
        $query .= "           W1.SEMESTER ";
        $query .= "    UNION ALL ";
        $query .= "    SELECT ";
        $query .= "           W2.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "           W2.CLASSCD, ";
            $query .= "           W2.SCHOOL_KIND, ";
            $query .= "           W2.CURRICULUM_CD, ";
        } 
        $query .= "           W2.SUBCLASSCD, ";
        $query .= "           W2.SEMESTER, ";
        $query .= "           value(W2.LESSON,0) as LESSON, ";
        $query .= "           value(W2.OFFDAYS,0) as OFFDAYS, ";
        $query .= "           value(W2.ABSENT,0) as ABSENT, ";
        $query .= "           value(W2.SUSPEND,0) as SUSPEND, ";
        $query .= "           value(W2.MOURNING,0) as MOURNING, ";
        $query .= "           value(W2.SICK,0) as SICK, ";
        if ($model->virus) {
            $query .= "           value(W2.VIRUS,0) as VIRUS, ";
        }
        if ($model->koudome) {
            $query .= "           value(W2.KOUDOME,0) as KOUDOME, ";
        }
        $query .= "           value(W2.NOTICE,0) as NOTICE, ";
        $query .= "           value(W2.NONOTICE,0) as NONOTICE, ";
        $query .= "           value(W2.NURSEOFF,0) as NURSEOFF, ";
        $query .= "           value(W2.LATE,0) as LATE, ";
        $query .= "           value(W2.EARLY,0) as EARLY ";
        $query .= "     FROM   T_attend W2 ";
        $query .= "    ) ";
        //学期毎に清算
        $query .= ",ATTEND_SUM2 AS ( ";
        $query .= "    SELECT ";
        $query .= "            SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        CLASSCD, ";
            $query .= "        SCHOOL_KIND, ";
            $query .= "        CURRICULUM_CD, ";
        } 
        $query .= "           SUBCLASSCD, ";
        $query .= "           SEMESTER, ";
        $query .= "           SUM(LESSON) LESSON, ";
        $query .= "           SUM(OFFDAYS) OFFDAYS, ";
        $query .= "           SUM(ABSENT) ABSENT, ";
        $query .= "           SUM(SUSPEND) SUSPEND, ";
        $query .= "           SUM(MOURNING) MOURNING, ";
        $query .= "           SUM(SICK) SICK, ";
        if ($model->virus) {
            $query .= "           SUM(VIRUS) VIRUS, ";
        }
        if ($model->koudome) {
            $query .= "           SUM(KOUDOME) KOUDOME, ";
        }
        $query .= "           SUM(NOTICE) NOTICE, ";
        $query .= "           SUM(NONOTICE) NONOTICE, ";
        $query .= "           SUM(NURSEOFF) NURSEOFF, ";
        $query .= "           SUM(LATE) LATE, ";
        $query .= "           SUM(EARLY) EARLY ";
        if (($knjSchoolMst["ABSENT_COV"] == "3") && (is_numeric($knjSchoolMst["ABSENT_COV_LATE"]) && (int)$knjSchoolMst["ABSENT_COV_LATE"] != 0)) {
            $query .= "      ,decimal((float(sum(LATE) + sum(EARLY)) / ".$knjSchoolMst["ABSENT_COV_LATE"].") + (sum(NOTICE) + sum(NONOTICE) + sum(NURSEOFF) + sum(SICK) ";
            if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
                $query .= "            + sum(OFFDAYS) ";
            }
            if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
                $query .= "            + sum(SUSPEND) ";
            }
            if ($knjSchoolMst["SUB_VIRUS"] == "1") {
                $query .= "            + sum(VIRUS) ";
            }
            if ($knjSchoolMst["SUB_KOUDOME"] == "1") {
                $query .= "            + sum(KOUDOME) ";
            }
            if ($knjSchoolMst["SUB_MOURNING"] == "1") {
                $query .= "            + sum(MOURNING) ";
            }
            if ($knjSchoolMst["SUB_ABSENT"] == "1") {
                $query .= "            + sum(ABSENT) ";
            }
            $query .= "      ), 4, 1) as NOTICE_LATE ";
        } elseif (($knjSchoolMst["ABSENT_COV"] == "1") && (is_numeric($knjSchoolMst["ABSENT_COV_LATE"]) && (int)$knjSchoolMst["ABSENT_COV_LATE"] != 0)) {
            $query .= "      ,((sum(LATE) + sum(EARLY)) / ".$knjSchoolMst["ABSENT_COV_LATE"].") + (sum(NOTICE) + sum(NONOTICE) + sum(NURSEOFF) + sum(SICK) ";
            if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
                $query .= "            + sum(OFFDAYS) ";
            }
            if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
                $query .= "            + sum(SUSPEND) ";
            }
            if ($knjSchoolMst["SUB_VIRUS"] == "1") {
                $query .= "            + sum(VIRUS) ";
            }
            if ($knjSchoolMst["SUB_KOUDOME"] == "1") {
                $query .= "            + sum(KOUDOME) ";
            }
            if ($knjSchoolMst["SUB_MOURNING"] == "1") {
                $query .= "            + sum(MOURNING) ";
            }
            if ($knjSchoolMst["SUB_ABSENT"] == "1") {
                $query .= "            + sum(ABSENT) ";
            }
            $query .= "      ) as NOTICE_LATE ";
        } else {
            $query .= "      ,sum(NOTICE) + sum(NONOTICE) + sum(NURSEOFF) + sum(SICK) ";
            if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
                $query .= "            + sum(OFFDAYS) ";
            }
            if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
                $query .= "            + sum(SUSPEND) ";
            }
            if ($knjSchoolMst["SUB_VIRUS"] == "1") {
                $query .= "            + sum(VIRUS) ";
            }
            if ($knjSchoolMst["SUB_KOUDOME"] == "1") {
                $query .= "            + sum(KOUDOME) ";
            }
            if ($knjSchoolMst["SUB_MOURNING"] == "1") {
                $query .= "            + sum(MOURNING) ";
            }
            if ($knjSchoolMst["SUB_ABSENT"] == "1") {
                $query .= "            + sum(ABSENT) ";
            }
            $query .= "      as NOTICE_LATE ";
        }
        $query .= "    FROM   ATTEND_SUM ";
        $query .= "    GROUP BY ";
        $query .= "        SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        CLASSCD, ";
            $query .= "        SCHOOL_KIND, ";
            $query .= "        CURRICULUM_CD, ";
        } 
        $query .= "        SUBCLASSCD, ";
        $query .= "        SEMESTER ";
        $query .= "    ) ";
        //年間で清算
        $query .= ",ATTEND_SUM3 AS ( ";
        $query .= "    SELECT ";
        $query .= "        SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        CLASSCD, ";
            $query .= "        SCHOOL_KIND, ";
            $query .= "        CURRICULUM_CD, ";
        } 
        $query .= "        SUBCLASSCD, ";
        $query .= "        SUM(NOTICE_LATE) NOTICE_LATE1 ";
        if (($knjSchoolMst["ABSENT_COV"] == "4") && (is_numeric($knjSchoolMst["ABSENT_COV_LATE"]) && (int)$knjSchoolMst["ABSENT_COV_LATE"] != 0)) {
            $query .= "      ,decimal((float(sum(LATE) + sum(EARLY)) / ".$knjSchoolMst["ABSENT_COV_LATE"].") + (sum(NOTICE) + sum(NONOTICE) + sum(NURSEOFF) + sum(SICK) ";
            if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
                $query .= "            + sum(OFFDAYS) ";
            }
            if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
                $query .= "            + sum(SUSPEND) ";
            }
            if ($knjSchoolMst["SUB_VIRUS"] == "1") {
                $query .= "            + sum(VIRUS) ";
            }
            if ($knjSchoolMst["SUB_KOUDOME"] == "1") {
                $query .= "            + sum(KOUDOME) ";
            }
            if ($knjSchoolMst["SUB_MOURNING"] == "1") {
                $query .= "            + sum(MOURNING) ";
            }
            if ($knjSchoolMst["SUB_ABSENT"] == "1") {
                $query .= "            + sum(ABSENT) ";
            }
            $query .= "      ), 4, 1) as NOTICE_LATE2 ";
        } elseif (($knjSchoolMst["ABSENT_COV"] == "2") && (is_numeric($knjSchoolMst["ABSENT_COV_LATE"]) && (int)$knjSchoolMst["ABSENT_COV_LATE"] != 0)) {
            $query .= "      ,((sum(LATE) + sum(EARLY)) / ".$knjSchoolMst["ABSENT_COV_LATE"].") + (sum(NOTICE) + sum(NONOTICE) + sum(NURSEOFF) + sum(SICK) ";
            if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
                $query .= "            + sum(OFFDAYS) ";
            }
            if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
                $query .= "            + sum(SUSPEND) ";
            }
            if ($knjSchoolMst["SUB_VIRUS"] == "1") {
                $query .= "            + sum(VIRUS) ";
            }
            if ($knjSchoolMst["SUB_KOUDOME"] == "1") {
                $query .= "            + sum(KOUDOME) ";
            }
            if ($knjSchoolMst["SUB_MOURNING"] == "1") {
                $query .= "            + sum(MOURNING) ";
            }
            if ($knjSchoolMst["SUB_ABSENT"] == "1") {
                $query .= "            + sum(ABSENT) ";
            }
            $query .= "      ) as NOTICE_LATE2 ";
        } else {
            $query .= "      ,sum(NOTICE) + sum(NONOTICE) + sum(NURSEOFF) + sum(SICK) ";
            if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
                $query .= "            + sum(OFFDAYS) ";
            }
            if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
                $query .= "            + sum(SUSPEND) ";
            }
            if ($knjSchoolMst["SUB_VIRUS"] == "1") {
                $query .= "            + sum(VIRUS) ";
            }
            if ($knjSchoolMst["SUB_KOUDOME"] == "1") {
                $query .= "            + sum(KOUDOME) ";
            }
            if ($knjSchoolMst["SUB_MOURNING"] == "1") {
                $query .= "            + sum(MOURNING) ";
            }
            if ($knjSchoolMst["SUB_ABSENT"] == "1") {
                $query .= "            + sum(ABSENT) ";
            }
            $query .= "      as NOTICE_LATE2 ";
        }
        $query .= "    FROM ";
        $query .= "        ATTEND_SUM2 ";
        $query .= "    GROUP BY ";
        $query .= "        SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        CLASSCD, ";
            $query .= "        SCHOOL_KIND, ";
            $query .= "        CURRICULUM_CD, ";
        } 
        $query .= "        SUBCLASSCD ";
        $query .= "    ) ";

        return $query;
    }

    //出欠メイン
    function getAttendMain($model, $knjSchoolMst)
    {
        //変数
        $year = CTRL_YEAR;

        $query .= "SELECT ";
        $query .= "    T1.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        T2.CLASSCD, ";
            $query .= "        T2.SCHOOL_KIND, ";
            $query .= "        T2.CURRICULUM_CD, ";
        } 
        $query .= "    T2.SUBCLASSCD, ";
        if (($knjSchoolMst["ABSENT_COV"] == "1" || $knjSchoolMst["ABSENT_COV"] == "3") && (is_numeric($knjSchoolMst["ABSENT_COV_LATE"]) && (int)$knjSchoolMst["ABSENT_COV_LATE"] != 0)) {
            $query .= "    NOTICE_LATE1 AS NOTICE_LATE ";
        } else {
            $query .= "    NOTICE_LATE2 AS NOTICE_LATE ";
        }
        $query .= "FROM ";
        $query .= "    SCH_T T1, ";
        $query .= "    ATTEND_SUM3 T2 ";
        $query .= "WHERE ";
        $query .= "    T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "    AND NOT EXISTS(SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT W1 ";
        $query .= "                   WHERE  W1.YEAR = '{$year}' AND W1.COMBINED_SUBCLASSCD = T2.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                          AND W1.COMBINED_CLASSCD        = T2.CLASSCD ";
            $query .= "                                          AND W1.COMBINED_SCHOOL_KIND    = T2.SCHOOL_KIND ";
            $query .= "                                          AND W1.COMBINED_CURRICULUM_CD  = T2.CURRICULUM_CD ";
        } 
        $query .= "                   ) ";
        $query .= "UNION ALL ";
        $query .= "SELECT ";
        $query .= "    T1.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        W1.COMBINED_CLASSCD AS CLASSCD, ";
            $query .= "        W1.COMBINED_SCHOOL_KIND AS SCHOOL_KIND, ";
            $query .= "        W1.COMBINED_CURRICULUM_CD AS CURRICULUM_CD, ";
        } 
        $query .= "    W1.COMBINED_SUBCLASSCD AS SUBCLASSCD, ";
        if (($knjSchoolMst["ABSENT_COV"] == "1" || $knjSchoolMst["ABSENT_COV"] == "3") && (is_numeric($knjSchoolMst["ABSENT_COV_LATE"]) && (int)$knjSchoolMst["ABSENT_COV_LATE"] != 0)) {
            $query .= "    SUM(NOTICE_LATE1) AS NOTICE_LATE ";
        } else {
            $query .= "    SUM(NOTICE_LATE2) AS NOTICE_LATE ";
        }
        $query .= "FROM ";
        $query .= "    SCH_T T1, ";
        $query .= "    ATTEND_SUM3 T2 ";
        $query .= "    INNER JOIN SUBCLASS_REPLACE_COMBINED_DAT W1 ON W1.YEAR = '{$year}' ";
        $query .= "                                               AND W1.ATTEND_SUBCLASSCD = T2.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                   AND    W1.ATTEND_CLASSCD        = T2.CLASSCD ";
            $query .= "                   AND    W1.ATTEND_SCHOOL_KIND    = T2.SCHOOL_KIND ";
            $query .= "                   AND    W1.ATTEND_CURRICULUM_CD  = T2.CURRICULUM_CD ";
        } 
        $query .= "WHERE ";
        $query .= "    T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "GROUP BY ";
        $query .= "    T1.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        W1.COMBINED_CLASSCD, ";
            $query .= "        W1.COMBINED_SCHOOL_KIND, ";
            $query .= "        W1.COMBINED_CURRICULUM_CD, ";
        } 
        $query .= "    W1.COMBINED_SUBCLASSCD ";

        return $query;
    }

    function get_count_record_slump_dat($model, $row, $subclasscd) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd       = substr($subclasscd, 0, 2);
            $substr_school_kind   = substr($subclasscd, 3, 1);
            $substr_curriculum_cd = substr($subclasscd, 5, 1);
            $substr_subclasscd    = substr($subclasscd, 7, 6);
        }
        $selectTest = ($model->field["TESTKINDCD"] == "9909") ? "9900" : $model->field["TESTKINDCD"];

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     RECORD_SLUMP_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '". CTRL_YEAR ."' ";
        $query .= "     AND SEMESTER    = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND TESTKINDCD || TESTITEMCD = '{$selectTest}' ";
         //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND CLASSCD         = '{$substr_classcd}' ";
            $query .= "     AND SCHOOL_KIND     = '{$substr_school_kind}' ";
            $query .= "     AND CURRICULUM_CD   = '{$substr_curriculum_cd}' ";
            $query .= "     AND SUBCLASSCD      = '{$substr_subclasscd}' ";
        } else {
            $query .= "     AND SUBCLASSCD  = '{$subclasscd}' ";
        }
        $query .= "     AND SCHREGNO    = '{$row["SCHREGNO"]}' ";

        return $query;
    }

    function get_perfect_record_dat($model, $row, $subclasscd) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_school_kind   = substr($subclasscd, 3, 1);
            $substr_curriculum_cd = substr($subclasscd, 5, 1);
            $substr_subclasscd    = substr($subclasscd, 7, 6);
        }
    
        $selectTest = ($model->field["TESTKINDCD"] == "9909") ? "9900" : $model->field["TESTKINDCD"];

        $query  = " SELECT ";
        $query .= "     VALUE(T1.PASS_SCORE, 0) ";
        $query .= " FROM ";
        $query .= "     PERFECT_RECORD_DAT T1 ";
        $query .= " INNER JOIN ";
        $query .= "     SCHREG_REGD_DAT L1 ON  L1.YEAR       = T1.YEAR ";
        $query .= "                        AND L1.SEMESTER   = T1.SEMESTER ";
        $query .= "                        AND L1.GRADE      = T1.GRADE ";
        $query .= "                        AND L1.COURSECD   = T1.COURSECD ";
        $query .= "                        AND L1.MAJORCD    = T1.MAJORCD ";
        $query .= "                        AND L1.COURSECODE = T1.COURSECODE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR       = '". CTRL_YEAR ."'";
        $query .= " AND T1.SEMESTER   = '{$model->field["SEMESTER"]}' ";
        $query .= " AND T1.TESTKINDCD || TESTITEMCD = '{$selectTest}' ";
        $query .= " AND T1.CLASSCD    = '". substr($subclasscd, 0, 2) ."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND T1.SCHOOL_KIND     = '{$substr_school_kind}' ";
            $query .= " AND T1.CURRICULUM_CD   = '{$substr_curriculum_cd}' ";
            $query .= " AND T1.SUBCLASSCD      = '{$substr_subclasscd}' ";
        } else {
            $query .= " AND T1.SUBCLASSCD = '{$subclasscd}' ";
        }
        $query .= " AND L1.SCHREGNO   = '{$row["SCHREGNO"]}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.DIV ";

        return $query;
    }
}
?>
