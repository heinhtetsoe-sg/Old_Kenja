<?php

require_once('for_php7.php');

class knjb1230query extends Query {

    //処理年度
    function getExeYear($model)
    {
        $query  = " WITH EXE_YEAR (LABEL, VALUE) AS ( ";
        if ($model->search_div == "1") {
            $query .="     VALUES (".(CTRL_YEAR + 1).",".(CTRL_YEAR + 1).")";
        } else {
            $query .="     VALUES (".CTRL_YEAR.",".CTRL_YEAR."), (".(CTRL_YEAR + 1).",".(CTRL_YEAR + 1).")";
        }
        $query .="     ) ";
        $query .=" SELECT ";
        $query .="     * ";
        $query .=" FROM ";
        $query .="     EXE_YEAR ";
        $query .=" ORDER BY ";
        $query .="     VALUE DESC ";

        return $query;
    }

    //生徒情報取得
    function getStudentInfoData($model, $div = "")
    {
        $setYear = CTRL_YEAR;
        $seme = CTRL_SEMESTER;
        if ($div == "meisai") {
            $setYear = $model->exe_year;
            $seme = $model->exe_year == CTRL_YEAR ? CTRL_SEMESTER : 1 ;
        }

        $tableName = "V_SCHREG_BASE_MST";
        $tableBetsumei1 = "SC_R";
        $tableBetsumei2 = "SC_CTRL";
        if ($model->search_div == "1") {

            $tableName  = "(SELECT ";
            $tableName .= "     F1.*, ";
            $tableName .= "     V1.BASE_REMARK1 AS SATEI_TANNI, ";
            $tableName .= "     V1.BASE_REMARK5 AS JIKOUGAI_NYUURYOKU, ";
            $tableName .= "     V1.BASE_REMARK2 AS TOKKATU_JISU, ";
            $tableName .= "     V1.BASE_REMARK4 AS MUSYOU_KAISU ";
            $tableName .= " FROM ";
            $tableName .= "     FRESHMAN_DAT F1 ";
            $tableName .= "     LEFT JOIN SCHREG_BASE_DETAIL_MST V1 ON F1.SCHREGNO = V1.SCHREGNO ";
            $tableName .= "          AND V1.BASE_SEQ = '004' ";
            $tableName .= " ) ";
            $tableBetsumei1 = "MAIN";
            $tableBetsumei2 = "MAIN";
        }

        $query  =" SELECT ";
        $query .="     MAIN.SCHREGNO, ";
        if ($model->search_div == "1") {
            $query .="     '01' AS GRADE, ";
            $query .="     '01' AS CTRL_GRADE, ";
        } else {
            if ($model->exe_year == CTRL_YEAR) {
                $query .="     SC_R.GRADE, ";
            } else {
                $query .="     '0' || CAST(CAST(SC_R.GRADE AS SMALLINT) + 1 AS CHAR(1)) AS GRADE, ";
            }
            $query .="     SC_R.GRADE AS CTRL_GRADE, ";
            $query .="     SC_R.HR_CLASS AS CTRL_HR_CLASS, ";
        }
        $query .="     C2.COURSENAME AS COURSE, ";
        $query .="     C2.COURSENAME || M2.MAJORNAME AS MAJOR, ";
        $query .="     CC2.COURSECODENAME, ";
        $query .="     SM.STAFFNAME AS STAFFNAME, ";
        $query .="     SC_R.ANNUAL AS ANNUAL, ";
        $query .="     MAIN.NAME AS NAME, ";
        if ($model->search_div == "1") {
            $query .="     MAIN.CURRICULUM_YEAR AS CURRICULUM_YEAR, ";
            $query .="     MAIN.ENTERYEAR AS ENT_YEAR, ";
            $query .="     MAIN.ADDR1, ";
            $query .="     MAIN.ADDR2, ";
        } else {
            $query .="     SC_H.CURRICULUM_YEAR AS CURRICULUM_YEAR, ";
            $query .="     MAIN.ENT_DATE, ";
            $query .="     CASE WHEN MONTH(MAIN.ENT_DATE) < 4 ";
            $query .="          THEN YEAR(MAIN.ENT_DATE) - 1 ";
            $query .="          ELSE YEAR(MAIN.ENT_DATE) ";
            $query .="     END AS ENT_YEAR, ";
            $query .="     MAIN.GRD_DATE, ";
            $query .="     ADDR.ADDR1, ";
            $query .="     ADDR.ADDR2, ";
        }
        $query .="     SC_YD.BASE_REMARK1 AS GRD_YOTEI, ";
        $query .="     MAIN.SATEI_TANNI, ";
        $query .="     MAIN.JIKOUGAI_NYUURYOKU, ";
        $query .="     TOKKATU.NAME1 AS TOKKATU_JISU, ";
        $query .="     SCHOOLING.NAME1 AS SCHOOLING_DIV, ";
        $query .="     MAIN.MUSYOU_KAISU, ";
        $query .="     AREA.NAME1 AS AREA, ";
        $query .="     JOB.NAME1 AS JOB, ";
        $query .="     {$tableBetsumei1}.COURSECD, ";
        $query .="     {$tableBetsumei1}.MAJORCD, ";
        $query .="     {$tableBetsumei1}.COURSECODE, ";
        $query .="     {$tableBetsumei2}.COURSECD AS CTRL_COURSECD, ";
        $query .="     {$tableBetsumei2}.MAJORCD AS CTRL_MAJORCD, ";
        $query .="     {$tableBetsumei2}.COURSECODE AS CTRL_COURSECODE ";
        $query .=" FROM ";
        $query .="     {$tableName} MAIN ";
        $query .="     LEFT JOIN SCHREG_REGD_DAT SC_R ON SC_R.SCHREGNO = MAIN.SCHREGNO ";
        $query .="          AND SC_R.YEAR = '{$setYear}' ";
        $query .="          AND SC_R.SEMESTER = '{$seme}' ";
        $query .="     LEFT JOIN SCHREG_REGD_DAT SC_CTRL ON SC_CTRL.SCHREGNO = MAIN.SCHREGNO ";
        $query .="          AND SC_CTRL.YEAR = '".CTRL_YEAR."' ";
        $query .="          AND SC_CTRL.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .="     LEFT JOIN SCHREG_REGD_GDAT SC_G ON SC_R.YEAR = SC_G.YEAR ";
        $query .="          AND SC_R.GRADE = SC_G.GRADE ";
        $query .="     LEFT JOIN COURSE_MST C2 ON C2.COURSECD = {$tableBetsumei1}.COURSECD ";
        $query .="     LEFT JOIN MAJOR_MST M2 ON M2.COURSECD = {$tableBetsumei1}.COURSECD ";
        $query .="          AND M2.MAJORCD = {$tableBetsumei1}.MAJORCD ";
        $query .="     LEFT JOIN COURSECODE_MST CC2 ON CC2.COURSECODE = {$tableBetsumei1}.COURSECODE ";
        $query .="     LEFT JOIN SCHREG_REGD_HDAT SH ON SH.YEAR = SC_R.YEAR ";
        $query .="          AND SH.SEMESTER = SC_R.SEMESTER ";
        $query .="          AND SH.GRADE = SC_R.GRADE ";
        $query .="          AND SH.HR_CLASS = SC_R.HR_CLASS ";
        $query .="     LEFT JOIN STAFF_MST SM ON SM.STAFFCD = SH.TR_CD1 ";
        $query .="     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT SC_H ON MAIN.SCHREGNO = SC_H.SCHREGNO ";
        $query .="          AND SC_H.SCHOOL_KIND = SC_G.SCHOOL_KIND ";
        $query .="     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST SC_YD ON SC_YD.SCHREGNO = MAIN.SCHREGNO ";
        $query .="          AND SC_YD.YEAR = '{$model->exe_year}' ";
        $query .="          AND SC_YD.BASE_SEQ = '001' ";
        $query .="     LEFT JOIN SCHREG_SEND_ADDRESS_DAT SCH_SEND ON SCH_SEND.SCHREGNO = MAIN.SCHREGNO ";
        $query .="          AND SCH_SEND.DIV = '1' ";
        $query .= "    LEFT JOIN NAME_MST AREA ON AREA.NAMECD1 = 'A020' ";
        $query .= "         AND SCH_SEND.SEND_AREACD = AREA.NAMECD2 ";
        $query .= "    LEFT JOIN NAME_MST JOB ON JOB.NAMECD1 = 'H202' ";
        $query .= "         AND SCH_SEND.SEND_JOBCD = JOB.NAMECD2 ";
        $query .= "    LEFT JOIN ( ";
        $query .= "              SELECT ";
        $query .= "                  T1.* ";
        $query .= "              FROM ";
        $query .= "                  SCHREG_ADDRESS_DAT T1, ";
        $query .= "                  (SELECT ";
        $query .= "                       SCHREGNO, ";
        $query .= "                       MAX(ISSUEDATE) AS ISSUEDATE ";
        $query .= "                   FROM ";
        $query .= "                       SCHREG_ADDRESS_DAT ";
        $query .= "                   GROUP BY ";
        $query .= "                       SCHREGNO ";
        $query .= "                  ) T2 ";
        $query .= "              WHERE ";
        $query .= "                  T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "                  AND T1.ISSUEDATE = T2.ISSUEDATE) ADDR ";
        $query .= "         ON MAIN.SCHREGNO = ADDR.SCHREGNO ";
        $query .= "    LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST Y_DETAIL ON MAIN.SCHREGNO = Y_DETAIL.SCHREGNO ";
        $query .= "         AND Y_DETAIL.YEAR = '{$model->exe_year}' ";
        $query .= "         AND Y_DETAIL.BASE_SEQ = '002' ";
        $query .= "    LEFT JOIN NAME_MST TOKKATU ON TOKKATU.NAMECD1 = 'M013' ";
        $query .= "         AND MAIN.TOKKATU_JISU = TOKKATU.NAMECD2 ";
        $query .= "    LEFT JOIN NAME_MST SCHOOLING ON SCHOOLING.NAMECD1 = 'M014' ";
        $query .= "         AND Y_DETAIL.BASE_REMARK1 = SCHOOLING.NAMECD2 ";
        $query .=" WHERE ";
        $query .="     MAIN.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //履修カウント
    function getRisyuuCnt($model)
    {
        $query  = " WITH STD_YEAR AS ( ";
        $query .= " SELECT ";
        $query .= "     YEAR ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_STD_SELECT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR < '{$model->exe_year}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= " GROUP BY ";
        $query .= "     YEAR ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     STD_YEAR ";

        return $query;
    }

    //履修パターン
    function getPattern($model)
    {
        $query  =" SELECT ";
        $query .="     PATTERN_CD AS VALUE, ";
        $query .="     PATTERN_CD || ':' || PATTERN_NAME AS LABEL ";
        $query .=" FROM ";
        $query .="     COMP_CREDITS_PATTERN_MST ";
        $query .=" WHERE ";
        $query .="     YEAR = '{$model->exe_year}' ";
        $query .=" ORDER BY ";
        $query .="     VALUE ";

        return $query;
    }

    //卒業単位数
    function getGradCredits($model) {
        $query  = " SELECT ";
        $query .= "     GRAD_CREDITS ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exe_year}' ";

        return $query;
    }

    //履修期間
    function getTakesemes($model) {
        $query  = " WITH ADD_T (VALUE, LABEL) AS (VALUES('0', '0:通年')) ";
        $query .= " SELECT * FROM ADD_T ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTER || ':' || SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '{$model->exe_year}' AND ";
        $query .= "     SEMESTER   <> '9' ";
        $query .=" ORDER BY ";
        $query .="     VALUE ";

        return $query;
    }

    //履修コース
    function getRisyuuCourse($model) {
        $query  = " SELECT ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || VALUE(NAME1,'') AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '{$model->exe_year}' AND ";
        $query .= "     NAMECD1 = 'B022' ";
        $query .=" ORDER BY ";
        $query .="     VALUE ";

        return $query;
    }

    //複数講座チェック用
    function getSubclassChairCnt($model) {

        $query  = " WITH CHAIR_T AS ( ";
        $query .= " SELECT ";
        $query .= "     CHAIR.CHAIRCD, ";
        $query .= "     CHAIR.CLASSCD, ";
        $query .= "     CHAIR.SCHOOL_KIND, ";
        $query .= "     CHAIR.CURRICULUM_CD, ";
        $query .= "     CHAIR.SUBCLASSCD, ";
        $query .= "     MAX(CHAIR.CHAIRNAME) AS CHAIRNAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_CLS_DAT CHAIR_C ";
        $query .= "     INNER JOIN CHAIR_DAT CHAIR ON CHAIR_C.YEAR = CHAIR.YEAR ";
        $query .= "           AND CHAIR_C.SEMESTER = CHAIR.SEMESTER ";
        $query .= "           AND CHAIR_C.CHAIRCD = CHAIR.CHAIRCD ";
        $query .= " WHERE ";
        $query .= "     CHAIR_C.YEAR = '{$model->exe_year}' ";
        $query .= "     AND CHAIR_C.GROUPCD = '0000' ";
        $query .= "     AND CHAIR_C.TRGTGRADE || '-' || CHAIR_C.TRGTCLASS = '{$model->gradeHr}' ";
        $query .= " GROUP BY ";
        $query .= "     CHAIR.CHAIRCD, ";
        $query .= "     CHAIR.CLASSCD, ";
        $query .= "     CHAIR.SCHOOL_KIND, ";
        $query .= "     CHAIR.CURRICULUM_CD, ";
        $query .= "     CHAIR.SUBCLASSCD ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     CHAIR.CHAIRCD, ";
        $query .= "     CHAIR.CLASSCD, ";
        $query .= "     CHAIR.SCHOOL_KIND, ";
        $query .= "     CHAIR.CURRICULUM_CD, ";
        $query .= "     CHAIR.SUBCLASSCD, ";
        $query .= "     MAX(CHAIR.CHAIRNAME) AS CHAIRNAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_CLS_DAT CHAIR_C ";
        $query .= "     INNER JOIN CHAIR_DAT CHAIR ON CHAIR_C.YEAR = CHAIR.YEAR ";
        $query .= "           AND CHAIR_C.SEMESTER = CHAIR.SEMESTER ";
        $query .= "           AND CHAIR_C.GROUPCD = CHAIR.GROUPCD ";
        $query .= " WHERE ";
        $query .= "     CHAIR_C.YEAR = '{$model->exe_year}' ";
        $query .= "     AND CHAIR_C.GROUPCD != '0000' ";
        $query .= "     AND CHAIR_C.TRGTGRADE || '-' || CHAIR_C.TRGTCLASS = '{$model->gradeHr}' ";
        $query .= " GROUP BY ";
        $query .= "     CHAIR.CHAIRCD, ";
        $query .= "     CHAIR.CLASSCD, ";
        $query .= "     CHAIR.SCHOOL_KIND, ";
        $query .= "     CHAIR.CURRICULUM_CD, ";
        $query .= "     CHAIR.SUBCLASSCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     CHAIR_T ";
        $query .= " GROUP BY ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD ";
        $query .= " HAVING ";
        $query .= "     COUNT(*) > 1 ";

        return $query;
    }

    //複数講座チェック用
    function getChairStd($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     CHAIRCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exe_year}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //一覧
    function getSubclassName($model)
    {
        if ($model->Properties["useHyoutei"] == "1") {
            $testCd = "9-99-00-08";
        } else {
            $testCd = "9-99-00-09";
        }

        $seme = $model->exeNendoPatern == "1" ? CTRL_SEMESTER : 1 ;
        $query  = " WITH SCH_MAIN AS ( ";
        $query .= knjb1230query::getStudentInfoData($model);

        //教育課程
        $query .= " ), CURRICULUM_T AS ( ";
        $query .= " SELECT ";
        $query .= "    NAMECD2 ";
        $query .= " FROM ";
        $query .= "    NAME_MST ";
        $query .= " WHERE ";
        $query .= "    NAMECD1 = 'Z018' ";
        $query .= "    AND '{$model->exe_year}' BETWEEN NAMESPARE1 AND NAMESPARE2 ";
        $query .= " ), ";

        //継続履修
        if ($model->Properties["useKeizokuRisyuu"] == "1") {
            $query .= " KEIZOKU_T AS ( ";
            $query .= " SELECT ";
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     T1.COMP_CONTINUE ";
            $query .= " FROM ";
            $query .= "     V_RECORD_SCORE_HIST_DAT T1 ";
            $query .= " WHERE ";
            $query .= "    T1.YEAR = '".($model->exe_year - 1)."' ";
            $query .= "    AND T1.SEMESTER || '-' || T1.TESTKINDCD || '-' || T1.TESTITEMCD || '-' || T1.SCORE_DIV = '{$testCd}' ";
            $query .= "    AND T1.SCHREGNO = '{$model->schregno}' ";
            $query .= "    AND T1.SEQ = 1 ";
            $query .= " ), ";
        }

        //履修登録済み
        $query .= " COMP_SUBCLASS_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     '1' AS COMP_EXE_FLG ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST T1 ";
        $query .= "     INNER JOIN SUBCLASS_STD_SELECT_DAT S_STD ON S_STD.YEAR = '{$model->exe_year}' ";
        $query .= "           AND S_STD.SEMESTER = '{$seme}' ";
        $query .= "           AND S_STD.CLASSCD = T1.CLASSCD ";
        $query .= "           AND S_STD.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "           AND S_STD.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "           AND S_STD.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "           AND S_STD.SCHREGNO = '{$model->schregno}' ";
        $query .= " WHERE ";
        $query .= "    T1.CLASSCD < '91' ";
        $query .= " ), ";

        //履修登録済み (ログイン年度)
        $query .= " COMP_SUBCLASS_CTL_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     '1' AS COMP_EXE_FLG ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST T1 ";
        $query .= "     INNER JOIN SUBCLASS_STD_SELECT_DAT S_STD ON S_STD.YEAR = '".CTRL_YEAR."' ";
        $query .= "           AND S_STD.SEMESTER = '{$seme}' ";
        $query .= "           AND S_STD.CLASSCD = T1.CLASSCD ";
        $query .= "           AND S_STD.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "           AND S_STD.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "           AND S_STD.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "           AND S_STD.SCHREGNO = '{$model->schregno}' ";
        $query .= " WHERE ";
        $query .= "    T1.CLASSCD < '91' ";
        $query .= " ), ";

        //前籍校
        $query .= " ANOTHER_SUBCLASS_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND_SAKI AS SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD_SAKI AS CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD_SAKI AS SUBCLASSCD, ";
        $query .= "     L1.SUBCLASSNAME, ";
        $query .= "     SUM(VALUE(T1.GET_CREDIT, 0) + VALUE(T1.ADD_CREDIT, 0)) AS GET_CREDIT ";
        $query .= " FROM ";
        $query .= "    SCHREG_STUDYREC_REPLACE_DAT T1 ";
        $query .= "    LEFT JOIN SUBCLASS_MST L1 ON T1.CLASSCD_SAKI = L1.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND_SAKI = L1.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD_SAKI = L1.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD_SAKI = L1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD = '1' ";
        $query .= "     AND T1.SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND T1.ANNUAL = '00' ";
        $query .= "     AND T1.CLASSCD < '91' ";
        $query .= " GROUP BY ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND_SAKI, ";
        $query .= "     T1.CURRICULUM_CD_SAKI, ";
        $query .= "     T1.SUBCLASSCD_SAKI, ";
        $query .= "     L1.SUBCLASSNAME ";
        $query .= " ), ";

        //成績：今年度RECORD_DAT（STUDYREC除く）＋指定年度以下STUDYREC
        $query .= " RECORD AS ( ";
        $query .= " SELECT ";
        $query .= "     'REC' AS REC_DIV, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     '1' AS RISYUTYU_FLG, ";
        $query .= "     SUM(VALUE(T1.GET_CREDIT, 0) + VALUE(T1.ADD_CREDIT, 0)) AS GET_CREDIT ";
        $query .= " FROM ";
        $query .= "     RECORD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND T1.CLASSCD < '91' ";
        $query .= "     AND T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NOT EXISTS(SELECT ";
        $query .= "                        'x' ";
        $query .= "                    FROM ";
        $query .= "                       SCHREG_STUDYREC_DAT E1 ";
        $query .= "                    WHERE ";
        $query .= "                        E1.SCHOOLCD IN ('0', '2') ";
        $query .= "                        AND T1.YEAR = E1.YEAR ";
        $query .= "                        AND T1.SCHREGNO = E1.SCHREGNO ";
        $query .= "                        AND T1.CLASSCD = E1.CLASSCD ";
        $query .= "                        AND T1.SCHOOL_KIND = E1.SCHOOL_KIND ";
        $query .= "                        AND T1.CURRICULUM_CD = E1.CURRICULUM_CD ";
        $query .= "                        AND T1.SUBCLASSCD = E1.SUBCLASSCD ";
        $query .= "     ) ";
        $query .= " GROUP BY ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     'REC2' AS REC_DIV, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     '0' AS RISYUTYU_FLG, ";
        $query .= "     SUM(VALUE(T1.GET_CREDIT, 0) + VALUE(T1.ADD_CREDIT, 0)) AS GET_CREDIT ";
        $query .= " FROM ";
        $query .= "    SCHREG_STUDYREC_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD IN ('0', '2') ";
        $query .= "     AND YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND T1.CLASSCD < '91' ";
        $query .= " GROUP BY ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     'STUDY' AS REC_DIV, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     '0' AS RISYUTYU_FLG, ";
        $query .= "     SUM(VALUE(T1.GET_CREDIT, 0) + VALUE(T1.ADD_CREDIT, 0)) AS GET_CREDIT ";
        $query .= " FROM ";
        $query .= "    SCHREG_STUDYREC_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD IN ('0', '2') ";
        $query .= "     AND YEAR <= '".$model->exe_year."' ";
        $query .= "     AND T1.CLASSCD < '91' ";
        $query .= "     AND YEAR <> '".CTRL_YEAR."' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= " GROUP BY ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD ";
        $query .= " ), ";
        if ($model->exeNendoPatern != "1") {
            //成績：前年度STUDYREC
            $query .= " RECORD_OLD AS ( ";
            $query .= " SELECT ";
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     '1' AS OLD_FLG ";
            $query .= " FROM ";
            $query .= "    SCHREG_STUDYREC_DAT T1 ";
            $query .= " WHERE ";
            $query .= "     SCHOOLCD IN ('0', '2') ";
            $query .= "     AND YEAR = '".(CTRL_YEAR - 1)."' ";
            $query .= "     AND SCHREGNO = '{$model->schregno}' ";
            $query .= "     AND T1.CLASSCD < '91' ";
            $query .= " GROUP BY ";
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
            $query .= "     T1.SUBCLASSCD ";
            $query .= " ), ";
        }

        //出力対象科目（成績）
        $query .= " RECORD_SUB AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD ";
        $query .= " FROM ";
        $query .= "     RECORD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND T1.YEAR <= '".$model->exe_year."' ";
        $query .= "     AND T1.CLASSCD < '91' ";
        $query .= "     AND NOT EXISTS(SELECT ";
        $query .= "                        'x' ";
        $query .= "                    FROM ";
        $query .= "                       SCHREG_STUDYREC_DAT E1 ";
        $query .= "                    WHERE ";
        $query .= "                        E1.SCHOOLCD IN ('0', '2') ";
        $query .= "                        AND T1.YEAR = E1.YEAR ";
        $query .= "                        AND T1.SCHREGNO = E1.SCHREGNO ";
        $query .= "                        AND T1.CLASSCD = E1.CLASSCD ";
        $query .= "                        AND T1.SCHOOL_KIND = E1.SCHOOL_KIND ";
        $query .= "                        AND T1.CURRICULUM_CD = E1.CURRICULUM_CD ";
        $query .= "                        AND T1.SUBCLASSCD = E1.SUBCLASSCD ";
        $query .= "     ) ";
        $query .= " GROUP BY ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD ";
        $query .= " FROM ";
        $query .= "    SCHREG_STUDYREC_DAT T1 ";
        $query .= "    LEFT JOIN SUBCLASS_MST L1 ON T1.CLASSCD = L1.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD = L1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD IN ('0', '2') ";
        $query .= "     AND YEAR <= '".$model->exe_year."' ";
        $query .= "     AND T1.CLASSCD < '91' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= " GROUP BY ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD ";
        $query .= " ), ";

        //出力対象科目：開講科目＋成績（開講科目除く）＋自校外（開講科目と成績除く）
        $query .= " SUBCLASS_T AS ( ";
        $query .= " SELECT ";
        $query .= "    T1.CLASSCD, ";
        $query .= "    T1.SCHOOL_KIND, ";
        $query .= "    T1.CURRICULUM_CD, ";
        $query .= "    T1.SUBCLASSCD, ";
        $query .= "    T1.SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "    V_SUBCLASS_MST T1 ";
        $query .= " WHERE ";
        $query .= "    T1.YEAR = '".$model->exe_year."' ";
        $query .= "    AND T1.CLASSCD < '91' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "    T1.CLASSCD, ";
        $query .= "    T1.SCHOOL_KIND, ";
        $query .= "    T1.CURRICULUM_CD, ";
        $query .= "    T1.SUBCLASSCD, ";
        $query .= "    L1.SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "    RECORD_SUB T1 ";
        $query .= "    LEFT JOIN SUBCLASS_MST L1 ON T1.CLASSCD = L1.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD = L1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "    T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD ";
        $query .= "         NOT IN (SELECT ";
        $query .= "                     I1.CLASSCD || I1.SCHOOL_KIND || I1.CURRICULUM_CD || I1.SUBCLASSCD ";
        $query .= "                 FROM ";
        $query .= "                     V_SUBCLASS_MST I1 ";
        $query .= "                 WHERE ";
        $query .= "                     I1.YEAR = '".$model->exe_year."' ";
        $query .= "                ) ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "    T1.CLASSCD, ";
        $query .= "    T1.SCHOOL_KIND, ";
        $query .= "    T1.CURRICULUM_CD, ";
        $query .= "    T1.SUBCLASSCD, ";
        $query .= "    L1.SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "    COMP_SUBCLASS_CTL_T T1 ";
        $query .= "    LEFT JOIN SUBCLASS_MST L1 ON T1.CLASSCD = L1.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD = L1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "    T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD ";
        $query .= "         NOT IN (SELECT ";
        $query .= "                     I1.CLASSCD || I1.SCHOOL_KIND || I1.CURRICULUM_CD || I1.SUBCLASSCD ";
        $query .= "                 FROM ";
        $query .= "                     V_SUBCLASS_MST I1 ";
        $query .= "                 WHERE ";
        $query .= "                     I1.YEAR = '".$model->exe_year."' ";
        $query .= "                ) ";
        $query .= "    AND T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD ";
        $query .= "         NOT IN (SELECT ";
        $query .= "                     I2.CLASSCD || I2.SCHOOL_KIND || I2.CURRICULUM_CD || I2.SUBCLASSCD ";
        $query .= "                 FROM ";
        $query .= "                     RECORD I2 ";
        $query .= "                ) ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "    T1.CLASSCD, ";
        $query .= "    T1.SCHOOL_KIND, ";
        $query .= "    T1.CURRICULUM_CD, ";
        $query .= "    T1.SUBCLASSCD, ";
        $query .= "    L1.SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "    ANOTHER_SUBCLASS_T T1 ";
        $query .= "    LEFT JOIN SUBCLASS_MST L1 ON T1.CLASSCD = L1.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD = L1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "    T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD ";
        $query .= "         NOT IN (SELECT ";
        $query .= "                     I1.CLASSCD || I1.SCHOOL_KIND || I1.CURRICULUM_CD || I1.SUBCLASSCD ";
        $query .= "                 FROM ";
        $query .= "                     V_SUBCLASS_MST I1 ";
        $query .= "                 WHERE ";
        $query .= "                     I1.YEAR = '".$model->exe_year."' ";
        $query .= "                ) ";
        $query .= "    AND T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD ";
        $query .= "         NOT IN (SELECT ";
        $query .= "                     I2.CLASSCD || I2.SCHOOL_KIND || I2.CURRICULUM_CD || I2.SUBCLASSCD ";
        $query .= "                 FROM ";
        $query .= "                     RECORD I2 ";
        $query .= "                ) ";
        $query .= "    AND T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD ";
        $query .= "         NOT IN (SELECT ";
        $query .= "                     I3.CLASSCD || I3.SCHOOL_KIND || I3.CURRICULUM_CD || I3.SUBCLASSCD ";
        $query .= "                 FROM ";
        $query .= "                     COMP_SUBCLASS_CTL_T I3 ";
        $query .= "                ) ";
        $query .= " ), ";

        $query .= " CHAIR_T AS ( ";
        $query .= " SELECT ";
        $query .= "     CHAIR.CHAIRCD, ";
        $query .= "     CHAIR.CLASSCD, ";
        $query .= "     CHAIR.SCHOOL_KIND, ";
        $query .= "     CHAIR.CURRICULUM_CD, ";
        $query .= "     CHAIR.SUBCLASSCD, ";
        $query .= "     MAX(CHAIR.CHAIRNAME) AS CHAIRNAME, ";
        $query .= "     MAX(CHAIR.TAKESEMES) AS TAKESEMES ";
        $query .= " FROM ";
        $query .= "     CHAIR_CLS_DAT CHAIR_C ";
        $query .= "     INNER JOIN CHAIR_DAT CHAIR ON CHAIR_C.YEAR = CHAIR.YEAR ";
        $query .= "           AND CHAIR_C.SEMESTER = CHAIR.SEMESTER ";
        $query .= "           AND CHAIR_C.CHAIRCD = CHAIR.CHAIRCD ";
        $query .= " WHERE ";
        $query .= "     CHAIR_C.YEAR = '{$model->exe_year}' ";
        $query .= "     AND CHAIR_C.GROUPCD = '0000' ";
        $query .= "     AND CHAIR_C.TRGTGRADE || '-' || CHAIR_C.TRGTCLASS = '{$model->gradeHr}' ";
        $query .= " GROUP BY ";
        $query .= "     CHAIR.CHAIRCD, ";
        $query .= "     CHAIR.CLASSCD, ";
        $query .= "     CHAIR.SCHOOL_KIND, ";
        $query .= "     CHAIR.CURRICULUM_CD, ";
        $query .= "     CHAIR.SUBCLASSCD ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     CHAIR.CHAIRCD, ";
        $query .= "     CHAIR.CLASSCD, ";
        $query .= "     CHAIR.SCHOOL_KIND, ";
        $query .= "     CHAIR.CURRICULUM_CD, ";
        $query .= "     CHAIR.SUBCLASSCD, ";
        $query .= "     MAX(CHAIR.CHAIRNAME) AS CHAIRNAME, ";
        $query .= "     MAX(CHAIR.TAKESEMES) AS TAKESEMES ";
        $query .= " FROM ";
        $query .= "     CHAIR_CLS_DAT CHAIR_C ";
        $query .= "     INNER JOIN CHAIR_DAT CHAIR ON CHAIR_C.YEAR = CHAIR.YEAR ";
        $query .= "           AND CHAIR_C.SEMESTER = CHAIR.SEMESTER ";
        $query .= "           AND CHAIR_C.GROUPCD = CHAIR.GROUPCD ";
        $query .= " WHERE ";
        $query .= "     CHAIR_C.YEAR = '{$model->exe_year}' ";
        $query .= "     AND CHAIR_C.GROUPCD != '0000' ";
        $query .= "     AND CHAIR_C.TRGTGRADE || '-' || CHAIR_C.TRGTCLASS = '{$model->gradeHr}' ";
        $query .= " GROUP BY ";
        $query .= "     CHAIR.CHAIRCD, ";
        $query .= "     CHAIR.CLASSCD, ";
        $query .= "     CHAIR.SCHOOL_KIND, ";
        $query .= "     CHAIR.CURRICULUM_CD, ";
        $query .= "     CHAIR.SUBCLASSCD ";
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "    T1.CLASSCD, ";
        $query .= "    T1.SCHOOL_KIND, ";
        $query .= "    T1.CURRICULUM_CD, ";
        $query .= "    T1.SUBCLASSCD, ";
        $query .= "    T2.CLASSNAME, ";
        $query .= "    T1.SUBCLASSNAME, ";
        $query .= "    CHAIR_T.CHAIRCD, ";
        $query .= "    CHAIR_T.CHAIRNAME, ";
        $query .= "    CASE WHEN R3.SUBCLASSCD IS NOT NULL AND R3.GET_CREDIT > 0 ";
        $query .= "         THEN '1' ";
        $query .= "         ELSE '0' ";
        $query .= "    END AS NOT_CHANGE, ";
        if ($model->Properties["useKeizokuRisyuu"] == "1") {
            $query .= "    KEIZOKU.COMP_CONTINUE, ";
        }
        if ($model->exeNendoPatern == "1") {
            $query .= "    CASE WHEN COMP.SUBCLASSCD IS NOT NULL AND R1.RISYUTYU_FLG = '1' ";
            $query .= "         THEN R1.GET_CREDIT ";
            $query .= "         ELSE ";
            $query .= "         CASE WHEN COMP.SUBCLASSCD IS NOT NULL AND R1.SUBCLASSCD IS NULL AND R3.SUBCLASSCD IS NULL ";
            $query .= "              THEN CRECTL.CREDITS ";
            $query .= "              ELSE NULL ";
            $query .= "         END ";
            $query .= "    END AS RISYUTYU_CREDIT, ";
        } else {
            $query .= "    '' AS RISYUTYU_CREDIT, ";
            $query .= "    RECORD_OLD.OLD_FLG, ";
        }
        $query .= "    CASE WHEN COMP.SUBCLASSCD IS NOT NULL AND R3.SUBCLASSCD IS NOT NULL ";
        $query .= "         THEN R3.GET_CREDIT ";
        $query .= "         ELSE CRECTL.CREDITS ";
        $query .= "    END AS SET_CREDIT, ";
        $query .= "    CASE WHEN COMP.SUBCLASSCD IS NOT NULL AND R3.SUBCLASSCD IS NOT NULL ";
        $query .= "         THEN R3.GET_CREDIT ";
        $query .= "         ELSE CRECTL2.CREDITS ";
        $query .= "    END AS SET_CREDIT2, ";
        $query .= "    CASE WHEN COMP.SUBCLASSCD IS NOT NULL ";
        $query .= "         THEN CRECTL.CREDITS ";
        $query .= "    END AS SET_CREDIT3, ";
        $query .= "    CRECTL.CREDITS, ";
        $query .= "    VALUE(R2.GET_CREDIT, 0) + VALUE(R3.GET_CREDIT, 0) AS RECORD_CREDITS, ";
        $query .= "    ANOTHER.GET_CREDIT AS ANOTHER_CREDITS, ";
        $query .= "    CASE WHEN PATTERN.SUBCLASSCD IS NOT NULL ";
        $query .= "         THEN '1' ";
        $query .= "         ELSE '0' ";
        $query .= "    END AS PATTERN_FLG, ";
        $query .= "    CASE WHEN PATTERN.COMP_FLG = '1' ";
        $query .= "         THEN '1' ";
        $query .= "         ELSE ";
        $query .= "         CASE WHEN COMP.SUBCLASSCD IS NOT NULL ";
        $query .= "              THEN '1' ";
        $query .= "              ELSE '0' ";
        $query .= "         END ";
        $query .= "    END AS CHECKED, ";
        $query .= "    CASE WHEN COMPCTL.SUBCLASSCD IS NOT NULL ";
        $query .= "         THEN '1' ";
        $query .= "         ELSE '0' ";
        $query .= "    END AS CTLCHECKED, ";
        $query .= "    CASE WHEN V_SUB.SUBCLASSCD IS NOT NULL ";
        $query .= "         THEN '1' ";
        $query .= "         ELSE '0' ";
        $query .= "    END AS CHECKBOX, ";
        $query .= "    CASE WHEN VALUE(ANOTHER.GET_CREDIT, 0) = 0  ";
        $query .= "         THEN '1' ";
        $query .= "         ELSE '2' ";
        $query .= "    END AS SORT, ";
        $query .= "    CASE WHEN COMPCTL.SUBCLASSCD IS NOT NULL THEN 1 ";
        $query .= "         ELSE 2 ";
        $query .= "    END AS SORT2, ";
        $query .= "    COMP.COMP_EXE_FLG, ";
        $query .= "    SCH_COMP.KOUNIN, ";
        $query .= "    SCH_COMP.ADD_CREDIT AS ZOUTAN, ";
        $query .= "    N1.NAME1 AS REMARK, ";
        $query .= "    CASE WHEN VALUE(N1.NAMESPARE3, '0') = '1' ";
        $query .= "         THEN 'red' ";
        $query .= "         ELSE 'black' ";
        $query .= "    END AS RE_COLOR, ";
        $query .= "    CASE WHEN R3.SUBCLASSCD IS NOT NULL ";
        $query .= "         THEN '1' ";
        $query .= "         ELSE '0' ";
        $query .= "    END AS STUDYREC_CNT, ";
        $query .= "    N2.NAMESPARE1 AS HITSURISYU, ";
        $query .= "    CHAIR_T.TAKESEMES, ";
        $query .= "    CHAIR_DETAIL.RISYUU_COURSE ";
        $query .= " FROM ";
        $query .= "    SUBCLASS_T T1 ";
        $query .= "    LEFT JOIN CLASS_MST T2 ON T1.CLASSCD = T2.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "    LEFT JOIN RECORD R1 ON T1.CLASSCD = R1.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND   = R1.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD = R1.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD    = R1.SUBCLASSCD ";
        $query .= "         AND R1.REC_DIV       = 'REC' ";
        $query .= "    LEFT JOIN RECORD R2 ON T1.CLASSCD = R2.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND   = R2.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD = R2.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD    = R2.SUBCLASSCD ";
        $query .= "         AND R2.REC_DIV       = 'STUDY' ";
        $query .= "    LEFT JOIN RECORD R3 ON T1.CLASSCD = R3.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND   = R3.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD = R3.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD    = R3.SUBCLASSCD ";
        $query .= "         AND R3.REC_DIV       = 'REC2' ";
        $query .= "    LEFT JOIN ANOTHER_SUBCLASS_T ANOTHER ON T1.CLASSCD = ANOTHER.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND = ANOTHER.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD = ANOTHER.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD    = ANOTHER.SUBCLASSCD ";
        if ($model->Properties["useKeizokuRisyuu"] == "1") {
            $query .= "    LEFT JOIN KEIZOKU_T KEIZOKU ON T1.CLASSCD       = KEIZOKU.CLASSCD ";
            $query .= "         AND T1.SCHOOL_KIND   = KEIZOKU.SCHOOL_KIND ";
            $query .= "         AND T1.CURRICULUM_CD = KEIZOKU.CURRICULUM_CD ";
            $query .= "         AND T1.SUBCLASSCD    = KEIZOKU.SUBCLASSCD ";
        }
        $query .= "    LEFT JOIN COMP_SUBCLASS_T COMP ON T1.CLASSCD       = COMP.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND   = COMP.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD = COMP.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD    = COMP.SUBCLASSCD ";
        $query .= "    LEFT JOIN COMP_SUBCLASS_CTL_T COMPCTL ON T1.CLASSCD = COMPCTL.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND   = COMPCTL.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD = COMPCTL.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD    = COMPCTL.SUBCLASSCD ";
        $query .= "    LEFT JOIN COMP_CREDITS_PATTERN_DAT PATTERN ON PATTERN.YEAR = '{$model->exe_year}' ";
        $query .= "         AND T1.CLASSCD   = PATTERN.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND   = PATTERN.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD = PATTERN.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD    = PATTERN.SUBCLASSCD ";
        if ($model->cmd == "pattern") {
            $query .= "         AND PATTERN.PATTERN_CD    = '".$model->pattern_cd."' ";
        } else {
            $query .= "         AND PATTERN.PATTERN_CD    = '' ";
        }
        $query .= "    LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z018' ";
        $query .= "         AND T1.CURRICULUM_CD = N1.NAMECD2 ";
        $query .= "    LEFT JOIN V_SUBCLASS_MST V_SUB ON T1.CLASSCD = V_SUB.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND   = V_SUB.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD = V_SUB.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD    = V_SUB.SUBCLASSCD ";
        $query .= "         AND V_SUB.YEAR       = '".$model->exe_year."' ";
        $query .= "    LEFT JOIN SCH_MAIN I1 ON I1.SCHREGNO = '{$model->schregno}' ";
        $query .= "    LEFT JOIN CREDIT_MST CRECTL ON CRECTL.YEAR = '{$model->exe_year}' ";
        $query .= "         AND CRECTL.COURSECD || CRECTL.MAJORCD || CRECTL.GRADE || CRECTL.COURSECODE = I1.CTRL_COURSECD || I1.CTRL_MAJORCD || I1.CTRL_GRADE || I1.CTRL_COURSECODE ";
        $query .= "         AND T1.CLASSCD       = CRECTL.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND   = CRECTL.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD = CRECTL.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD    = CRECTL.SUBCLASSCD ";
        $query .= "    LEFT JOIN CREDIT_MST CRECTL2 ON CRECTL2.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND CRECTL2.COURSECD || CRECTL2.MAJORCD || CRECTL2.GRADE || CRECTL2.COURSECODE = I1.CTRL_COURSECD || I1.CTRL_MAJORCD || I1.CTRL_GRADE || I1.CTRL_COURSECODE ";
        $query .= "         AND T1.CLASSCD       = CRECTL2.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND   = CRECTL2.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD = CRECTL2.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD    = CRECTL2.SUBCLASSCD ";
        $query .= "    LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'Z011' ";
        $query .= "         AND CRECTL.REQUIRE_FLG = N2.NAMECD2 ";
        $query .= "    LEFT JOIN SCH_COMP_DETAIL_DAT SCH_COMP ON SCH_COMP.YEAR = '{$model->exe_year}' ";
        $query .= "         AND SCH_COMP.SCHREGNO = '{$model->schregno}' ";
        $query .= "         AND T1.CLASSCD       = SCH_COMP.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND   = SCH_COMP.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD = SCH_COMP.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD    = SCH_COMP.SUBCLASSCD ";
        if ($model->exeNendoPatern != "1") {
            $query .= "    LEFT JOIN RECORD_OLD RECORD_OLD ON T1.CLASSCD = RECORD_OLD.CLASSCD ";
            $query .= "         AND T1.SCHOOL_KIND   = RECORD_OLD.SCHOOL_KIND ";
            $query .= "         AND T1.CURRICULUM_CD = RECORD_OLD.CURRICULUM_CD ";
            $query .= "         AND T1.SUBCLASSCD    = RECORD_OLD.SUBCLASSCD ";
        }
        $query .= "    INNER JOIN CHAIR_T ON T1.CLASSCD = CHAIR_T.CLASSCD ";
        $query .= "          AND T1.SCHOOL_KIND   = CHAIR_T.SCHOOL_KIND ";
        $query .= "          AND T1.CURRICULUM_CD = CHAIR_T.CURRICULUM_CD ";
        $query .= "          AND T1.SUBCLASSCD    = CHAIR_T.SUBCLASSCD ";
        $query .= "     LEFT JOIN (SELECT ";
        $query .= "                     CHAIRCD, MAX(REMARK1) AS RISYUU_COURSE ";
        $query .= "                 FROM ";
        $query .= "                     CHAIR_DETAIL_DAT ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR = '{$model->exe_year}' AND SEQ = '002' ";
        $query .= "                 GROUP BY ";
        $query .= "                     CHAIRCD) CHAIR_DETAIL ON CHAIR_T.CHAIRCD = CHAIR_DETAIL.CHAIRCD ";
        $query .= " ORDER BY ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T2.CLASSNAME, ";
        $query .= "     CHECKBOX DESC, ";
        $query .= "     SORT, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.CURRICULUM_CD ";

        return $query;
    }

    //生徒教科書発注情報取得
    function getSchregTextbookDatChk($model)
    {
        $query  = " SELECT  ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_TEXTBOOK_SUBCLASS_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".$model->exe_year."' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //学期マスタ
    function getSeme($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$model->exe_year."' ";
        $query .= "     AND SEMESTER <> '9' ";

        return $query;
    }

    /**
     * 削除 SUBCLASS_STD_SELECT_DAT
     */
    function &deleteSubclassStd($model)
    {

        $query  = "DELETE FROM ";
        $query .= "    SUBCLASS_STD_SELECT_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->exe_year."' ";
        $query .= "    AND SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    /**
     * 新規 SUBCLASS_STD_SELECT_DAT
     */
    function &insertSubclassStd($db, $model, $semeData, $addvalue)
    {

        $data["YEAR"][TEXT]             = $model->exe_year;
        $data["SEMESTER"][TEXT]         = $semeData["SEMESTER"];
        $data["GROUPCD"][TEXT]          = "001";
        $data["CLASSCD"][TEXT]          = $addvalue[1];
        $data["SCHOOL_KIND"][TEXT]      = $addvalue[2];
        $data["CURRICULUM_CD"][TEXT]    = $addvalue[0];
        $data["SUBCLASSCD"][TEXT]       = $addvalue[3];
        $data["SCHREGNO"][TEXT]         = $model->schregno;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "SUBCLASS_STD_SELECT_DAT");

        return $query;
    }

    /**
     * 削除 SCH_COMP_DETAIL_DAT
     */
    function &deleteKounin($model)
    {

        $query  = "DELETE FROM ";
        $query .= "    SCH_COMP_DETAIL_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->exe_year."' ";
        $query .= "    AND SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    /**
     * 新規 SCH_COMP_DETAIL_DAT
     */
    function &insertKounin($db, $model, $addvalue)
    {

        $data["YEAR"][TEXT]             = $model->exe_year;
        $data["SCHREGNO"][TEXT]         = $model->schregno;
        $data["CLASSCD"][TEXT]          = $addvalue[1];
        $data["SCHOOL_KIND"][TEXT]      = $addvalue[2];
        $data["CURRICULUM_CD"][TEXT]    = $addvalue[0];
        $data["SUBCLASSCD"][TEXT]       = $addvalue[3];
        $data["KOUNIN"][TEXT]           = "1";
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "SCH_COMP_DETAIL_DAT");

        return $query;
    }

    /**
     * 新規 SCH_COMP_DETAIL_DAT
     */
    function &insertZoutan($db, $model, $addvalue, $val)
    {

        $data["YEAR"][TEXT]             = $model->exe_year;
        $data["SCHREGNO"][TEXT]         = $model->schregno;
        $data["CLASSCD"][TEXT]          = $addvalue[1];
        $data["SCHOOL_KIND"][TEXT]      = $addvalue[2];
        $data["CURRICULUM_CD"][TEXT]    = $addvalue[0];
        $data["SUBCLASSCD"][TEXT]       = $addvalue[3];
        $data["ADD_CREDIT"][NUMBER]     = $val;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "SCH_COMP_DETAIL_DAT");

        return $query;
    }

    /**
     * 新規/更新 SCHREG_BASE_YEAR_DETAIL_MST
     */
    function &insUpdSchregBaseYearDetail($db, $model)
    {
        $query = "SELECT COUNT(*) FROM SCHREG_BASE_YEAR_DETAIL_MST WHERE SCHREGNO = '{$model->schregno}' AND YEAR = '{$model->exe_year}' AND BASE_SEQ = '001' ";
        $dataCnt = $db->getOne($query);

        if ($dataCnt == 0) {
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["YEAR"][TEXT]             = $model->exe_year;
            $data["BASE_SEQ"][TEXT]         = "001";
        } else {
            $where .="    WHERE SCHREGNO = '{$model->schregno}' ";
            $where .="     AND YEAR = '{$model->exe_year}' ";
            $where .="     AND BASE_SEQ = '001' ";
        }
        $data["BASE_REMARK1"][TEXT]     = $model->grdYotei;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        if ($dataCnt == 0) {
            $query = Query::insertSQL($data, "SCHREG_BASE_YEAR_DETAIL_MST");
        } else {
            $query = Query::updateSQL($data, "SCHREG_BASE_YEAR_DETAIL_MST", $where);
        }

        return $query;
    }

    //学籍番号取得
    function getSchregNo($model) {
        $query  = " SELECT ";
        if ($this->cmd == "updateNext") {
            $query .= "     MIN(SCHREGNO) AS SCHREGNO ";
        } else if ($this->cmd == "updatePrev") {
            $query .= "     MAX(SCHREGNO) AS SCHREGNO ";
        } else {
            $query .= "     SCHREGNO ";
        }
        $query .= " FROM ";
        if ($model->search_div == "1") {
            $query .= "     FRESHMAN_DAT ";
        } else {
            $query .= "     SCHREG_BASE_MST ";
        }
        $query .= " WHERE ";
        if ($this->cmd == "updateNext") {
            $query .= "     SCHREGNO > '{$model->schregno}' ";
        } else if ($this->cmd == "updatePrev") {
            $query .= "     SCHREGNO < '{$model->schregno}' ";
        } else {
            $query .= "     SCHREGNO = '{$model->schregno}' ";
        }
        if ($model->search_div == "1") {
            $query .= "     AND ENTERYEAR = '{$model->exe_year}' ";
        }

        return $query;
    }

    /**
     * CHAIR_STD_DAT作成
     */
    function &delChairStd($model)
    {
        $query  = "DELETE FROM ";
        $query .= "    CHAIR_STD_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->exe_year."' ";
        $query .= "    AND SCHREGNO = '{$model->schregno}' ";
        return $query;
    }

    /**
     * CHAIR_STD_DAT作成
     */
    function &insChairStd($model, $semeData, $addvalue)
    {
        $data["YEAR"][TEXT]             = $model->exe_year;
        $data["SEMESTER"][TEXT]         = $semeData["SEMESTER"];
        $data["CHAIRCD"][TEXT]          = $addvalue[5];
        $data["SCHREGNO"][TEXT]         = $model->schregno;
        $data["APPDATE"][DATE]          = $semeData["SDATE"];
        $data["APPENDDATE"][DATE]       = $semeData["EDATE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "CHAIR_STD_DAT");
        return $query;
    }

}
?>
