<?php

require_once('for_php7.php');

class knjb1220query extends Query {

    //フィールド有無チェック
    function checkTableColumn($tableName, $columnName) {
        $query  = " WITH TMP AS (";
        $query .= " SELECT ";
        $query .= "     NAME ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSCOLUMNS ";
        $query .= " WHERE ";
        $query .= "     TBNAME  = '{$tableName}' ";
        $query .= " AND NAME  = '{$columnName}' ";

        $query .= " ) SELECT COUNT(*) FROM TMP ";

        return $query;
    }

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

    //履修履歴
    function getRirekiCode($model)
    {
        $query  = " SELECT ";
        $query .= "     RIREKI_CODE AS VALUE, ";
        $query .= "     SELECT_NAME || ' ' || CHAR(REPLACE(CHAR(SELECT_DATE), '-', '/')) AS LABEL ";
        $query .= " FROM ";
        $query .= "     STUDY_SELECT_DATE_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exe_year}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    /**
     * 在籍有無
     */
    function getSchregRegdCnt($model)
    {
        if ($model->search_div == "1") {
            $query  = " SELECT ";
            $query .= "     COUNT(*) AS CNT ";
            $query .= " FROM ";
            $query .= "     FRESHMAN_DAT FRESHMAN ";
            $query .= " WHERE ";
            $query .= "     FRESHMAN.ENTERYEAR = '{$model->exe_year}' ";
            $query .= "     AND FRESHMAN.SCHREGNO = '{$model->schregno}' ";
            if ($model->isFreshmanGrade == "1") {
                $query .= "     AND FRESHMAN.GRADE IS NOT NULL ";
            }
        } else {
            $query  = " SELECT ";
            $query .= "     COUNT(*) AS CNT ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT REGD ";
            $query .= " WHERE ";
            $query .= "     REGD.SCHREGNO = '{$model->schregno}' ";
            $query .= "     AND REGD.YEAR = '{$model->exe_year}' ";
        }

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
            if ($model->isFreshmanGrade == "1") {
                $query .="     MAIN.GRADE, ";
                $query .="     MAIN.GRADE AS CTRL_GRADE, ";
            } else {
                $query .="     '01' AS GRADE, ";
                $query .="     '01' AS CTRL_GRADE, ";
            }
        } else {
            $query .="     SC_R.GRADE, ";
            $query .="     SC_R.GRADE AS CTRL_GRADE, ";
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
        $query .= "     SUBCLASS_STD_SELECT_RIREKI_DAT ";
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

    //学年・コース情報
    function getGradeCourse($model)
    {
        $semester = $model->exeNendoPatern == "1" ? CTRL_SEMESTER : "1";
        if ($model->search_div == "1") {
            $query  = " SELECT ";
            if ($model->isFreshmanGrade == "1") {
                $query .= "     GRADE, ";
            } else {
                $query .= "     '01' GRADE, ";
            }
            $query .= "     HR_CLASS, ";
            $query .= "     ATTENDNO, ";
            $query .= "     '01' AS ANNUAL, ";
            $query .= "     COURSECD, ";
            $query .= "     MAJORCD, ";
            $query .= "     COURSECODE ";
            $query .= " FROM ";
            $query .= "     FRESHMAN_DAT FRESHMAN ";
            $query .= " WHERE ";
            $query .= "     FRESHMAN.ENTERYEAR = '{$model->exe_year}' ";
            $query .= "     AND FRESHMAN.SCHREGNO = '{$model->schregno}' ";
        } else {
            $query  = " SELECT ";
            $query .= "     GRADE, ";
            $query .= "     HR_CLASS, ";
            $query .= "     ATTENDNO, ";
            $query .= "     ANNUAL, ";
            $query .= "     COURSECD, ";
            $query .= "     MAJORCD, ";
            $query .= "     COURSECODE ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHREGNO = '{$model->schregno}' ";
            $query .= "     AND YEAR = '{$model->exe_year}' ";
            $query .= "     AND SEMESTER = '{$semester}' ";
        }

        return $query;
    }

    //履修パターン
    function getPattern($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.PATTERN_CD AS VALUE, ";
        $query .= "     T1.PATTERN_CD || ':' || T1.PATTERN_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COMP_CREDITS_PATTERN_COURSE_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->exe_year}' ";
        $query .= "     AND T1.GRADE = '{$model->gradeCourse["GRADE"]}' ";
        $query .= "     AND T1.COURSECD = '{$model->gradeCourse["COURSECD"]}' ";
        $query .= "     AND T1.MAJORCD = '{$model->gradeCourse["MAJORCD"]}' ";
        $query .= "     AND T1.COURSECODE = '{$model->gradeCourse["COURSECODE"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //履修パターン初期値
    function getPatternCd($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.PATTERN_CD ";
        $query .= " FROM ";
        $query .= "     COMP_CREDITS_PATTERN_STD_COURSE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->exe_year}' ";
        $query .= "     AND T1.RIREKI_CODE = '{$model->rirekiCode}' ";
        $query .= "     AND T1.GRADE = '{$model->gradeCourse["GRADE"]}' ";
        $query .= "     AND T1.COURSECD = '{$model->gradeCourse["COURSECD"]}' ";
        $query .= "     AND T1.MAJORCD = '{$model->gradeCourse["MAJORCD"]}' ";
        $query .= "     AND T1.COURSECODE = '{$model->gradeCourse["COURSECODE"]}' ";
        $query .= "     AND T1.SCHREGNO = '{$model->schregno}' ";

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
    //一覧
    function getSubclassName($model)
    {
        $seme = $model->exeNendoPatern == "1" ? CTRL_SEMESTER : 1 ;
        $query  = " WITH SCH_MAIN AS ( ";
        $query .= knjb1220query::getStudentInfoData($model, "meisai");

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
        $query .= "     INNER JOIN SUBCLASS_STD_SELECT_RIREKI_DAT S_STD ON S_STD.YEAR = '{$model->exe_year}' ";
        $query .= "           AND S_STD.SEMESTER = '{$seme}' ";
        $query .= "           AND S_STD.RIREKI_CODE = '{$model->rirekiCode}' ";
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
        $query .= "     INNER JOIN SUBCLASS_STD_SELECT_RIREKI_DAT S_STD ON S_STD.YEAR = '".CTRL_YEAR."' ";
        $query .= "           AND S_STD.SEMESTER = '{$seme}' ";
        $query .= "           AND S_STD.RIREKI_CODE = '{$model->rirekiCode}' ";
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
        $query .= "     CASE WHEN T1.CLASSNAME IS NOT NULL ";
        $query .= "          THEN T1.CLASSNAME ";
        $query .= "          ELSE STUDYREC_CLASS.CLASSNAME ";
        $query .= "     END AS CLASSNAME, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     CASE WHEN T1.SUBCLASSNAME IS NOT NULL ";
        $query .= "          THEN T1.SUBCLASSNAME ";
        $query .= "          ELSE L1.SUBCLASSNAME ";
        $query .= "     END AS SUBCLASSNAME, ";
        $query .= "     SUM(VALUE(T1.GET_CREDIT, 0) + VALUE(T1.ADD_CREDIT, 0)) AS GET_CREDIT ";
        $query .= " FROM ";
        $query .= "    SCHREG_STUDYREC_DAT T1 ";
        $query .= "    LEFT JOIN CLASS_MST STUDYREC_CLASS ON T1.CLASSCD = STUDYREC_CLASS.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND = STUDYREC_CLASS.SCHOOL_KIND ";
        $query .= "    LEFT JOIN SUBCLASS_MST L1 ON T1.CLASSCD = L1.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD = L1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD = '1' ";
        $query .= "     AND T1.SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND T1.ANNUAL = '00' ";
        $query .= "     AND T1.CLASSCD < '91' ";
        $query .= " GROUP BY ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     CASE WHEN T1.CLASSNAME IS NOT NULL ";
        $query .= "          THEN T1.CLASSNAME ";
        $query .= "          ELSE STUDYREC_CLASS.CLASSNAME ";
        $query .= "     END, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     CASE WHEN T1.SUBCLASSNAME IS NOT NULL ";
        $query .= "          THEN T1.SUBCLASSNAME ";
        $query .= "          ELSE L1.SUBCLASSNAME ";
        $query .= "     END ";
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
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "    T1.CLASSCD, ";
        $query .= "    T1.SCHOOL_KIND, ";
        $query .= "    T1.CURRICULUM_CD, ";
        $query .= "    T1.SUBCLASSCD, ";
        $query .= "    CASE WHEN ANOTHER.CLASSNAME IS NOT NULL ";
        $query .= "         THEN ANOTHER.CLASSNAME ";
        $query .= "         ELSE T2.CLASSNAME ";
        $query .= "    END AS CLASSNAME, ";
        $query .= "    CASE WHEN ANOTHER.SUBCLASSNAME IS NOT NULL ";
        $query .= "         THEN ANOTHER.SUBCLASSNAME ";
        $query .= "         ELSE T1.SUBCLASSNAME ";
        $query .= "    END AS SUBCLASSNAME, ";
        $query .= "    CASE WHEN R3.SUBCLASSCD IS NOT NULL AND R3.GET_CREDIT > 0 AND N3.YEAR IS NULL ";
        $query .= "         THEN '1' ";
        $query .= "         ELSE '0' ";
        $query .= "    END AS NOT_CHANGE, ";
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
        $query .= "    N2.NAMESPARE1 AS HITSURISYU ";
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
        $query .= "    LEFT JOIN COMP_SUBCLASS_T COMP ON T1.CLASSCD       = COMP.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND   = COMP.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD = COMP.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD    = COMP.SUBCLASSCD ";
        $query .= "    LEFT JOIN COMP_SUBCLASS_CTL_T COMPCTL ON T1.CLASSCD = COMPCTL.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND   = COMPCTL.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD = COMPCTL.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD    = COMPCTL.SUBCLASSCD ";
        $query .= "    LEFT JOIN COMP_CREDITS_PATTERN_COURSE_DAT PATTERN ON PATTERN.YEAR = '{$model->exe_year}' ";
        $query .= "         AND PATTERN.GRADE      = '{$model->gradeCourse["GRADE"]}' ";
        $query .= "         AND PATTERN.COURSECD   = '{$model->gradeCourse["COURSECD"]}' ";
        $query .= "         AND PATTERN.MAJORCD    = '{$model->gradeCourse["MAJORCD"]}' ";
        $query .= "         AND PATTERN.COURSECODE = '{$model->gradeCourse["COURSECODE"]}' ";
        $query .= "         AND T1.CLASSCD         = PATTERN.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND     = PATTERN.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD   = PATTERN.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD      = PATTERN.SUBCLASSCD ";
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
        $query .= "         AND CRECTL.COURSECD || CRECTL.MAJORCD || CRECTL.GRADE || CRECTL.COURSECODE = I1.COURSECD || I1.MAJORCD || I1.GRADE || I1.COURSECODE ";
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
        $query .= "    LEFT JOIN V_NAME_MST N3 ON N3.YEAR = '".CTRL_YEAR."' AND N3.NAMECD1 = 'D084' ";
        $query .= "         AND N3.NAME1 = T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ";

        $query .= " ORDER BY ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     CLASSNAME, ";
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
     * 削除 SUBCLASS_STD_SELECT_RIREKI_DAT
     */
    function &deleteSubclassStd($model, $notDelInState)
    {

        $query  = "DELETE FROM ";
        $query .= "    SUBCLASS_STD_SELECT_RIREKI_DAT ";
        $query .= "WHERE ";
        $query .= "     YEAR = '{$model->exe_year}' ";
        $query .= "     AND RIREKI_CODE = '{$model->rirekiCode}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND SEMESTER || CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD NOT IN {$notDelInState} ";

        return $query;
    }

    /**
     * 新規 SUBCLASS_STD_SELECT_RIREKI_DAT
     */
    function &insertSubclassStd($db, $model, $semeData, $addvalue)
    {
        $schSql = knjb1220query::getSchregInfo($model, $semeData);
        $schRow = $db->getRow($schSql, DB_FETCHMODE_ASSOC);

        $cmpCntSql = knjb1220query::getStdSelectCnt($model, $semeData, $addvalue);
        $cmpCnt = $db->getOne($cmpCntSql);

        $sentakuSql = knjb1220query::getSentakuKamoku($model, $schRow, $addvalue);
        $sentakuCnt = $db->getOne($sentakuSql);

        if ($cmpCnt > 0) {
            $where  = " WHERE ";
            $where .= "     YEAR = '{$model->exe_year}' ";
            $where .= "     AND SEMESTER = '{$semeData["SEMESTER"]}' ";
            $where .= "     AND RIREKI_CODE = '{$model->rirekiCode}' ";
            $where .= "     AND CLASSCD = '{$addvalue[1]}' ";
            $where .= "     AND SCHOOL_KIND = '{$addvalue[2]}' ";
            $where .= "     AND CURRICULUM_CD = '{$addvalue[0]}' ";
            $where .= "     AND SUBCLASSCD = '{$addvalue[3]}' ";
            $where .= "     AND SCHREGNO = '{$model->schregno}' ";

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::updateSQL($data, "SUBCLASS_STD_SELECT_RIREKI_DAT", $where);
        } else {

            $groupSql = knjb1220query::getGroupCd($model, $semeData, $addvalue, $sentakuCnt, $schRow);
            $setGroup = $db->getOne($groupSql);
            //最大値
            if (!$setGroup && is_array($schRow)) {
                $groupSql = knjb1220query::getGroupCdMax($model, $semeData, $addvalue, $sentakuCnt, $schRow);
                $setGroup = $db->getOne($groupSql);
                if ($setGroup) {
                    $setGroup = (int)$setGroup + 1;
                } else if ($sentakuCnt == "1") {
                    $setGroup = "001";
                } else {
                    $setGroup = "600";
                }
                //SUBCLASS_COMP_SELECT_DAT追加
                $query = knjb1220query::insertAutoSubclassCompSelectDat($model, $semeData, $addvalue, $setGroup, $schRow);
                $db->query($query);
                //SUBCLASS_COMP_SELECT_MST追加
                $query = knjb1220query::insertAutoSubclassCompSelectMst($model, $semeData, $addvalue, $setGroup, $schRow);
                $db->query($query);
            }

            if ($setGroup) {
                $data["YEAR"][TEXT]             = $model->exe_year;
                $data["SEMESTER"][TEXT]         = $semeData["SEMESTER"];
                $data["RIREKI_CODE"][TEXT]      = $model->rirekiCode;
                $data["GROUPCD"][TEXT]          = $setGroup;
                $data["CLASSCD"][TEXT]          = $addvalue[1];
                $data["SCHOOL_KIND"][TEXT]      = $addvalue[2];
                $data["CURRICULUM_CD"][TEXT]    = $addvalue[0];
                $data["SUBCLASSCD"][TEXT]       = $addvalue[3];
                $data["SCHREGNO"][TEXT]         = $model->schregno;
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $query = Query::insertSQL($data, "SUBCLASS_STD_SELECT_RIREKI_DAT");
            }
        }

        return $query;
    }

    /**
     * 生徒情報取得
     */
    function getSchregInfo($model, $semeData)
    {
        if ($model->search_div == "1") {
            $query  = " SELECT ";
            if ($model->isFreshmanGrade == "1") {
                $query .= "     GRADE, ";
            } else {
                $query .= "     '01' GRADE, ";
            }
            $query .= "     COURSECD, ";
            $query .= "     MAJORCD, ";
            $query .= "     COURSECODE ";
            $query .= " FROM ";
            $query .= "     FRESHMAN_DAT FRESHMAN ";
            $query .= " WHERE ";
            $query .= "     FRESHMAN.ENTERYEAR = '{$model->exe_year}' ";
            $query .= "     AND FRESHMAN.SCHREGNO = '{$model->schregno}' ";
        } else {
            $query  = " SELECT ";
            $query .= "     GRADE, ";
            $query .= "     COURSECD, ";
            $query .= "     MAJORCD, ";
            $query .= "     COURSECODE ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT REGD ";
            $query .= " WHERE ";
            $query .= "     REGD.SCHREGNO = '{$model->schregno}' ";
            $query .= "     AND REGD.YEAR = '{$model->exe_year}' ";
            $query .= "     AND REGD.SEMESTER = '{$semeData["SEMESTER"]}' ";
        }

        return $query;
    }

    /**
     * SUBCLASS_STD_SELECT_RIREKI_DATのカウント
     */
    function getStdSelectCnt($model, $semeData, $addvalue)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_STD_SELECT_RIREKI_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->exe_year}' ";
        $query .= "     AND T1.SEMESTER = '{$semeData["SEMESTER"]}' ";
        $query .= "     AND T1.RIREKI_CODE = '{$model->rirekiCode}' ";
        $query .= "     AND T1.CLASSCD = '{$addvalue[1]}' ";
        $query .= "     AND T1.SCHOOL_KIND = '{$addvalue[2]}' ";
        $query .= "     AND T1.CURRICULUM_CD = '{$addvalue[0]}' ";
        $query .= "     AND T1.SUBCLASSCD = '{$addvalue[3]}' ";
        $query .= "     AND T1.SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    /**
     * 選択科目CREDIT_MST
     */
    function getSentakuKamoku($model, $schRow, $addvalue)
    {
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     CREDIT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exe_year}' ";
        $query .= "     AND COURSECD = '{$schRow["COURSECD"]}' ";
        $query .= "     AND MAJORCD = '{$schRow["MAJORCD"]}' ";
        $query .= "     AND GRADE = '{$schRow["GRADE"]}' ";
        $query .= "     AND COURSECODE = '{$schRow["COURSECODE"]}' ";
        $query .= "     AND CLASSCD = '{$addvalue[1]}' ";
        $query .= "     AND SCHOOL_KIND = '{$addvalue[2]}' ";
        $query .= "     AND CURRICULUM_CD = '{$addvalue[0]}' ";
        $query .= "     AND SUBCLASSCD = '{$addvalue[3]}' ";
        $query .= "     AND REQUIRE_FLG = '3' ";

        return $query;
    }

    /**
     * SUBCLASS_COMP_SELECT_DAT.MAX(GROUPCD)取得
     */
    function getGroupCdMax($model, $semeData, $addvalue, $sentakuCnt, $schRow)
    {
        $query  = " SELECT ";
        $query .= "     MAX(T1.GROUPCD) AS GROUPCD ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_COMP_SELECT_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->exe_year}' ";
        $query .= "     AND T1.GRADE = '{$schRow["GRADE"]}' ";
        $query .= "     AND T1.COURSECD = '{$schRow["COURSECD"]}' ";
        $query .= "     AND T1.MAJORCD = '{$schRow["MAJORCD"]}' ";
        $query .= "     AND T1.COURSECODE = '{$schRow["COURSECODE"]}' ";
        if ($sentakuCnt == "1") {
            $query .= "     AND INT(T1.GROUPCD) < 600 ";
        } else {
            $query .= "     AND INT(T1.GROUPCD) >= 600  ";
        }

        return $query;
    }

    /**
     * SUBCLASS_COMP_SELECT_DAT.GROUPCD取得
     */
    function getGroupCd($model, $semeData, $addvalue, $sentakuCnt, $schRow)
    {
        $query  = " SELECT ";
        $query .= "     MIN(T1.GROUPCD) AS GROUPCD ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_COMP_SELECT_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->exe_year}' ";
        $query .= "     AND T1.GRADE = '{$schRow["GRADE"]}' ";
        $query .= "     AND T1.COURSECD = '{$schRow["COURSECD"]}' ";
        $query .= "     AND T1.MAJORCD = '{$schRow["MAJORCD"]}' ";
        $query .= "     AND T1.COURSECODE = '{$schRow["COURSECODE"]}' ";
        $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$addvalue[1]."-".$addvalue[2]."-".$addvalue[0]."-".$addvalue[3]."' ";
        if ($sentakuCnt == "1") {
            $query .= "     AND INT(T1.GROUPCD) < 600 ";
        } else {
            $query .= "     AND INT(T1.GROUPCD) >= 600  ";
        }

        return $query;
    }

    //SUBCLASS_COMP_SELECT_DAT追加（600以上のGROUPCDを新規追加）
    function insertAutoSubclassCompSelectDat($model, $semeData, $addvalue, $setGroup, $schRow) {

        $auto_data = array();
        $auto_data["YEAR"][TEXT]            = $model->exe_year;
        $auto_data["GRADE"][TEXT]           = $schRow["GRADE"];
        $auto_data["COURSECD"][TEXT]        = $schRow["COURSECD"];
        $auto_data["MAJORCD"][TEXT]         = $schRow["MAJORCD"];
        $auto_data["COURSECODE"][TEXT]      = $schRow["COURSECODE"];
        $auto_data["GROUPCD"][TEXT]         = $setGroup;
        $auto_data["CLASSCD"][TEXT]         = $addvalue[1];
        $auto_data["SCHOOL_KIND"][TEXT]     = $addvalue[2];
        $auto_data["CURRICULUM_CD"][TEXT]   = $addvalue[0];
        $auto_data["SUBCLASSCD"][TEXT]      = $addvalue[3];
        $auto_data["REGISTERCD"][TEXT]      = STAFFCD;
        $auto_data["UPDATED"][NUMBER]       = "sysdate()";

        $query = Query::insertSQL($auto_data, "SUBCLASS_COMP_SELECT_DAT");
        return $query;
    }

    //SUBCLASS_COMP_SELECT_DAT追加（600以上のGROUPCDを新規追加）
    function insertAutoSubclassCompSelectMst($model, $semeData, $addvalue, $setGroup, $schRow) {

        $auto_data = array();
        $auto_data["YEAR"][TEXT]            = $model->exe_year;
        $auto_data["GRADE"][TEXT]           = $schRow["GRADE"];
        $auto_data["COURSECD"][TEXT]        = $schRow["COURSECD"];
        $auto_data["MAJORCD"][TEXT]         = $schRow["MAJORCD"];
        $auto_data["COURSECODE"][TEXT]      = $schRow["COURSECODE"];
        $auto_data["GROUPCD"][TEXT]         = $setGroup;
        $auto_data["NAME"][TEXT]            = $setGroup;
        $auto_data["ABBV"][TEXT]            = $setGroup;
        $auto_data["JOUGEN"][NUMBER]        = 1;
        $auto_data["KAGEN"][NUMBER]         = 1;
        $auto_data["REGISTERCD"][TEXT]      = STAFFCD;
        $auto_data["UPDATED"][NUMBER]       = "sysdate()";

        $query = Query::insertSQL($auto_data, "SUBCLASS_COMP_SELECT_MST");
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
        if ($model->search_div == "2" && $model->hrClassHyoujiFlg == "1") {
            $query .= " SELECT ";
            $query .= "     MIN(SCHREGNO) AS SCHREGNO ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT T1 ";
            $query .= " WHERE ";
            $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "     AND GRADE || HR_CLASS = (SELECT GRADE || HR_CLASS FROM SCHREG_REGD_DAT WHERE SCHREGNO = '".$model->schregno."' AND YEAR = '".CTRL_YEAR."' AND SEMESTER = '".CTRL_SEMESTER."') ";
            $query .= "     AND ATTENDNO = ( ";
            $query .= "         SELECT ";
            if ($model->cmd == "updateNext") {
                $query .= "     MIN(ATTENDNO) ";
            } else if ($model->cmd == "updatePrev") {
                $query .= "     MAX(ATTENDNO) ";
            } else {
                $query .= "        (ATTENDNO) ";
            }
            $query .= "       FROM ";
            $query .= "           SCHREG_REGD_DAT I1 ";
            $query .= "       WHERE ";
            $query .= "               I1.YEAR = '".CTRL_YEAR."' ";
            $query .= "           AND I1.SEMESTER = '".CTRL_SEMESTER."' ";
            if ($model->cmd == "updateNext") {
                $query .= "       AND ATTENDNO > (SELECT ATTENDNO FROM SCHREG_REGD_DAT WHERE SCHREGNO = '".$model->schregno."' AND YEAR = '".CTRL_YEAR."' AND SEMESTER = '".CTRL_SEMESTER."')  ";
            } else if ($model->cmd == "updatePrev") {
                $query .= "       AND ATTENDNO < (SELECT ATTENDNO FROM SCHREG_REGD_DAT WHERE SCHREGNO = '".$model->schregno."' AND YEAR = '".CTRL_YEAR."' AND SEMESTER = '".CTRL_SEMESTER."')  ";
            } else {
                $query .= "       AND I1.SCHREGNO = '".$model->schregno."' ";
            }
            $query .= "         ) ";
        } else {
            $query  = " SELECT ";
            if ($model->cmd == "updateNext") {
                $query .= "     MIN(SCHREGNO) AS SCHREGNO ";
            } else if ($model->cmd == "updatePrev") {
                $query .= "     MAX(SCHREGNO) AS SCHREGNO ";
            } else {
                $query .= "     SCHREGNO ";
            }
            $query .= " FROM ";
            if ($model->search_div == "1") {
                $query .= "     FRESHMAN_DAT ";
            } else {
                $query .= "     SCHREG_BASE_MST T1 ";
            }
            $query .= " WHERE ";
            if ($model->cmd == "updateNext") {
                $query .= "     SCHREGNO > '{$model->schregno}' ";
            } else if ($model->cmd == "updatePrev") {
                $query .= "     SCHREGNO < '{$model->schregno}' ";
            } else {
                $query .= "     SCHREGNO = '{$model->schregno}' ";
            }
            if ($model->search_div == "1") {
                $query .= "     AND ENTERYEAR = '{$model->exe_year}' ";
            } else {
                $query .= "     AND EXISTS ( ";
                $query .= "         SELECT 'X' ";
                $query .= "         FROM SCHREG_REGD_DAT REGD ";
                $query .= "         WHERE ";
                $query .= "             REGD.SCHREGNO = T1.SCHREGNO ";
                $query .= "         AND REGD.YEAR = '{$model->exe_year}' ";
                if ($model->exe_year == CTRL_YEAR) {
                    $query .= "         AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
                } else {
                    $query .= "         AND REGD.SEMESTER = '1' ";
                }
                $query .= "     ) ";
            }
        }

        return $query;
    }

    /**
     * パターン登録
     */
    function &delInsPaternStd($db, $model)
    {
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     COMP_CREDITS_PATTERN_STD_COURSE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->exe_year}' ";
        $query .= "     AND T1.RIREKI_CODE = '{$model->rirekiCode}' ";
        $query .= "     AND T1.GRADE = '{$model->gradeCourse["GRADE"]}' ";
        $query .= "     AND T1.COURSECD = '{$model->gradeCourse["COURSECD"]}' ";
        $query .= "     AND T1.MAJORCD = '{$model->gradeCourse["MAJORCD"]}' ";
        $query .= "     AND T1.COURSECODE = '{$model->gradeCourse["COURSECODE"]}' ";
        $query .= "     AND T1.SCHREGNO = '{$model->schregno}' ";
        $db->query($query);

        $data["YEAR"][TEXT]         = $model->exe_year;
        $data["RIREKI_CODE"][TEXT]  = $model->rirekiCode;
        $data["PATTERN_CD"][TEXT]   = $model->pattern_cd;
        $data["GRADE"][TEXT]        = $model->gradeCourse["GRADE"];
        $data["COURSECD"][TEXT]     = $model->gradeCourse["COURSECD"];
        $data["MAJORCD"][TEXT]      = $model->gradeCourse["MAJORCD"];
        $data["COURSECODE"][TEXT]   = $model->gradeCourse["COURSECODE"];
        $data["SCHREGNO"][TEXT]     = $model->schregno;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "COMP_CREDITS_PATTERN_STD_COURSE_DAT");

        return $query;
    }

}
?>
