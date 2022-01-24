<?php

require_once('for_php7.php');

class knjc033kquery extends Query {

    /**
     * メイン
     */

    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJC033K' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    /* 学期名取得 */
    function getSemesterName($year, $semester) {
        $query .= " SELECT ";
        $query .= "     SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= " AND SEMESTER = '{$semester}' ";

        return $query;
    }

    //欠課種別取得
    function getSickDiv()
    {
        $query  = " SELECT ";
        $query .= "     T1.NAMECD2 AS VALUE, ";
        $query .= "     T1.NAMECD2 || ' ' || L1.NAME1 AS LABEL, ";
        $query .= "     T1.NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= " INNER JOIN ";
        $query .= "     V_NAME_MST L1 ON  L1.YEAR    = T1.YEAR ";
        $query .= "                   AND L1.NAMECD1 = 'C001' ";
        $query .= "                   AND L1.NAMECD2 = T1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.NAMECD1 = 'C041' AND ";
        $query .= "     T1.NAMECD2 IN ('1', '2', '3', '4', '5', '6', '15', '16', '19', '25') ";
        $query .= " ORDER BY ";
        $query .= "     T1.NAMESPARE2, ";
        $query .= "     INT(VALUE) ";

        return $query;
    }

    function getSickName($model) {
        $query .= " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'C001' AND ";
        $query .= "     NAMECD2 = '{$model->field["SICK"]}' ";

        return $query;
    }

    //メインデータ作成
    function selectSemesAll($seme = "")
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     SEMESTERNAME, ";
        if (!$seme) {
            $query .= "     CASE WHEN MONTH(SDATE) < 4 ";
            $query .= "          THEN MONTH(SDATE) + 12 ";
            $query .= "          ELSE MONTH(SDATE) END AS S_MONTH, ";
            $query .= "     CASE WHEN MONTH(EDATE) < 4 ";
            $query .= "          THEN MONTH(EDATE) + 12 ";
            $query .= "          ELSE MONTH(EDATE) END AS E_MONTH ";
        } else {
            $query .= "     SEMESTERNAME, ";
            $query .= "     MONTH(SDATE) AS S_MONTH, ";
            $query .= "     DAY(SDATE) AS S_DAY, ";
            $query .= "     MONTH(EDATE) AS E_MONTH, ";
            $query .= "     DAY(EDATE) AS E_DAY ";
        }
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if (!$seme) {
            $query .= "     AND SEMESTER <> '9' ";
        } else {
            $query .= "     AND SEMESTER = '".$seme."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //メインデータ作成
    function selectSemesGradeAll($seme, $grade)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     SEMESTERNAME, ";
        $query .= "     SEMESTERNAME, ";
        $query .= "     MONTH(SDATE) AS S_MONTH, ";
        $query .= "     DAY(SDATE) AS S_DAY, ";
        $query .= "     MONTH(EDATE) AS E_MONTH, ";
        $query .= "     DAY(EDATE) AS E_DAY ";
        $query .= " FROM ";
        $query .= "     V_SEMESTER_GRADE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$seme."' ";
        $query .= "     AND GRADE = '".$grade."' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    /* 対象月データの取得 */
    function selectMonthQuery($month, $data, $model)
    {
        $setNameCd = "Z005";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."05";
        }
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2, NAME1, NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR  = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '{$setNameCd}' ";
        if ($data == "DATA") {
            $query .= "     AND NAMECD2 IN (SELECT CONTROL_CODE FROM ADMIN_CONTROL_DAT WHERE YEAR='".CTRL_YEAR."' AND ";
            if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
            }
            $query .= "     CONTROL_FLG ='2') ";
        }
        $query .= "     AND NAMECD2 = '".sprintf('%02d',$month)."' ";
        $query .= " ORDER BY ";
        $query .= "     NAMESPARE1 ";

        return $query;
    }

    function getMaxSemeMonthCnt($month, $seme) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '{$seme}' ";
        if($month) {
            $query .= "     AND MONTH(EDATE) = ".$month." ";
        }

        return $query;
    }

    function getSyusu($model, $seme) {
        $query  = " SELECT ";
        $query .= "     HOUTEI_SYUSU_SEMESTER".$seme." ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND JUGYOU_JISU_FLG = '1' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    function getCredit($row, $model) {
        $query  = " SELECT ";
        $query .= "     CREDITS ";
        $query .= " FROM ";
        $query .= "     CREDIT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND COURSECD = '{$row["COURSECD"]}' ";
        $query .= "     AND MAJORCD = '{$row["MAJORCD"]}' ";
        $query .= "     AND GRADE = '{$row["GRADE"]}' ";
        $query .= "     AND COURSECODE = '{$row["COURSECODE"]}' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "       AND CLASSCD || '-' || SCHOOL_KIND || '-' ||  CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "       AND SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }

        return $query;
    }

    //科目データの取得
    function selectSubclassQuery($model){

        $query  = "   SELECT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "       t3.CLASSCD || '-' || t3.SCHOOL_KIND || '-' ||  t3.CURRICULUM_CD || '-' || t3.SUBCLASSCD AS VALUE, ";
            $query .= "       t3.CLASSCD || '-' || t3.SCHOOL_KIND || '-' ||  t3.CURRICULUM_CD || '-' || t3.SUBCLASSCD || VALUE(t3.SUBCLASSABBV, '') AS LABEL ";
        } else {
            $query .= "       t3.SUBCLASSCD AS VALUE, ";
            $query .= "       t3.SUBCLASSCD || VALUE(t3.SUBCLASSABBV, '') AS LABEL ";
        }
        $query .= "   FROM ";
        $query .= "       CHAIR_DAT t1, ";
        $query .= "       CHAIR_STF_DAT t2, ";
        $query .= "       SUBCLASS_MST t3 ";
        if ('' != $model->field["COURSE_MAJOR"]) {
            $query .= "       , CHAIR_STD_DAT t4 ";
            $query .= "       , SCHREG_REGD_DAT t5 ";
        }
        $query .= "   WHERE ";
        $query .= "       t1.YEAR = '".CTRL_YEAR."' ";
        $query .= "       AND t1.YEAR = t2.YEAR ";
        $query .= "       AND t1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "       AND t1.SEMESTER = t2.SEMESTER ";
        $query .= "       AND t1.CHAIRCD = t2.CHAIRCD ";
        if ('' != $model->field["COURSE_MAJOR"]) {
            $query .= "       AND t1.YEAR = t4.YEAR ";
            $query .= "       AND t1.SEMESTER = t4.SEMESTER ";
            $query .= "       AND t1.CHAIRCD = t4.CHAIRCD ";
            $query .= "       AND t1.YEAR = t5.YEAR ";
            $query .= "       AND t1.SEMESTER = t5.SEMESTER ";
            $query .= "       AND t4.SCHREGNO = t5.SCHREGNO ";
            $query .= "       AND t5.COURSECD || t5.MAJORCD = '".$model->field["COURSE_MAJOR"]."' ";
        }
        //更新可能(制限付)
        if($model->auth != DEF_UPDATABLE){
            $query .= "         AND t2.STAFFCD = '".STAFFCD."' ";
        }
        if ($model->Properties["useCurriculumcd"] == "1") {
            if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND t1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= "       AND t1.CLASSCD = t3.CLASSCD ";
            $query .= "       AND t1.SCHOOL_KIND = t3.SCHOOL_KIND ";
            $query .= "       AND t1.CURRICULUM_CD = t3.CURRICULUM_CD ";
        }
        $query .= "       AND t1.SUBCLASSCD = t3.SUBCLASSCD ";
        //合併先科目は対象外
        $query .= "     AND NOT EXISTS (SELECT 'X' FROM ";
        $query .= "                         SUBCLASS_REPLACE_COMBINED_DAT COMB ";
        $query .= "                     WHERE ";
        $query .= "                         COMB.REPLACECD              = '1' ";
        $query .= "                     AND COMB.YEAR                   = t1.YEAR ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                     AND COMB.COMBINED_CLASSCD       = t1.CLASSCD ";
            $query .= "                     AND COMB.COMBINED_SCHOOL_KIND   = t1.SCHOOL_KIND ";
            $query .= "                     AND COMB.COMBINED_CURRICULUM_CD = t1.CURRICULUM_CD ";
        }
        $query .= "                     AND COMB.COMBINED_SUBCLASSCD    = t1.SUBCLASSCD) ";
        $query .= "   GROUP BY ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "      t3.CLASSCD,";
            $query .= "      t3.SCHOOL_KIND,";
            $query .= "      t3.CURRICULUM_CD,";
        }
        $query .= "       t3.SUBCLASSCD, ";
        $query .= "       t3.SUBCLASSABBV ";
        $query .= "   ORDER BY ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "      t3.CLASSCD,";
            $query .= "      t3.SCHOOL_KIND,";
            $query .= "      t3.CURRICULUM_CD,";
        }
        $query .= "       t3.SUBCLASSCD ";
        return $query;
    }

    //講座データの取得
    function selectChairQuery(&$model)
    {
        $query  = "   SELECT DISTINCT";
        $query .= "       t1.CHAIRCD AS VALUE, ";
        $query .= "       t1.CHAIRCD || VALUE(t1.CHAIRNAME, '') AS LABEL ";
        $query .= "   FROM ";
        $query .= "       CHAIR_DAT t1, ";
        $query .= "       CHAIR_STF_DAT t2 ";
        if ('' != $model->field["COURSE_MAJOR"]) {
            $query .= "       , CHAIR_STD_DAT t4 ";
            $query .= "       , SCHREG_REGD_DAT t5 ";
        }
        $query .= "   WHERE ";
        $query .= "       t1.YEAR = '".CTRL_YEAR."' ";
        $query .= "       AND t1.YEAR = t2.YEAR ";
        $query .= "       AND t1.SEMESTER = t2.SEMESTER ";
        $query .= "       AND t1.CHAIRCD = t2.CHAIRCD ";
        if ('' != $model->field["COURSE_MAJOR"]) {
            $query .= "       AND t1.YEAR = t4.YEAR ";
            $query .= "       AND t1.SEMESTER = t4.SEMESTER ";
            $query .= "       AND t1.CHAIRCD = t4.CHAIRCD ";
            $query .= "       AND t1.YEAR = t5.YEAR ";
            $query .= "       AND t1.SEMESTER = t5.SEMESTER ";
            $query .= "       AND t4.SCHREGNO = t5.SCHREGNO ";
            $query .= "       AND t5.COURSECD || t5.MAJORCD = '".$model->field["COURSE_MAJOR"]."' ";
        }
        //更新可能(制限付)
        if ($model->auth != DEF_UPDATABLE) {
            $query .= "         AND t2.STAFFCD = '".STAFFCD."' ";
        }
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "       AND t1.CLASSCD || '-' || t1.SCHOOL_KIND || '-' ||  t1.CURRICULUM_CD || '-' || t1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "       AND t1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= "   ORDER BY ";
        $query .= "       t1.CHAIRCD ";
        return $query;
    }

    // 生徒情報リスト取得
    function selectQuery(&$model, $monthdata, $target_month)
    {
        $query  = " SELECT ";
        $query .= "     t1.name_show, t1.hr_nameabbv, t1.attendno, t1.schregno, ";
        $query .= "     t1.GRADE, t1.COURSECD, t1.MAJORCD, t1.COURSECODE, t1.SCHOOL_KIND ";
        for($i = 0; $i < get_count($monthdata); $i++)
        {
            $tmp = $i + 1;
            $query .= " ,w".$tmp.".".$model->sickdiv[$model->field["SICK"]]." AS month".$tmp." ";
            $query .= " ,w".$tmp.".SEMESTER AS SEMESTER".$tmp." ";
            $query .= " ,w".$tmp.".SCHREGNO AS SUB_SCHREGNO".$tmp." ";
        }
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "        t1.schregno, t1.name_show, t3.hr_nameabbv, t3.grade, t3.hr_class, t4.attendno, ";
        $query .= "        t4.COURSECD, t4.MAJORCD, t4.COURSECODE, t5.SCHOOL_KIND ";
        $query .= "     FROM ";
        $query .= "         schreg_base_mst t1, chair_std_dat t2, schreg_regd_hdat t3, schreg_regd_dat t4, schreg_regd_gdat t5 ";
        $query .= "     WHERE ";
        $query .= "             t1.schregno = t2.schregno ";
        $query .= "         AND t1.schregno = t4.schregno ";
        $query .= "         AND t2.year     = '".CTRL_YEAR."' ";
        $query .= "         AND t2.year     = t3.year ";
        $query .= "         AND t2.year     = t4.year ";
        $query .= "         AND t2.year     = t5.year ";
        $query .= "         AND t2.semester = '".CTRL_SEMESTER."' ";
        $query .= "         AND t2.semester = t3.semester ";
        $query .= "         AND t2.semester = t4.semester ";
        $query .= "         AND t2.chaircd  = '".$model->field["CHAIRCD"]."' ";
        $query .= "         AND t3.grade    = t4.grade ";
        $query .= "         AND t3.grade    = t5.grade ";
        $query .= "         AND t3.hr_class = t4.hr_class ";
        if (strlen($target_month) == 6) {
            $query .= "     AND '{$target_month}' BETWEEN rtrim(char(year(t2.appdate))) || SUBSTR(CHAR(DECIMAL(month(t2.appdate),2,0)),1,2) ";
            $query .= "     AND rtrim(char(year(t2.appenddate))) || SUBSTR(CHAR(DECIMAL(month(t2.appenddate),2,0)),1,2) ";
        }
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND t5.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     GROUP BY ";
        $query .= "         t1.schregno, t1.name_show, t3.hr_nameabbv, t3.grade, t3.hr_class, t4.attendno, ";
        $query .= "         t4.COURSECD, t4.MAJORCD, t4.COURSECODE, t5.SCHOOL_KIND ";
        $query .= "     ORDER BY ";
        $query .= "         t3.grade, t3.hr_class, t4.attendno ";
        $query .= "     )t1 ";

        for($i = 0; $i < get_count($monthdata); $i++)
        {
            $tmp = $i + 1;

            $query .= " LEFT OUTER JOIN ";
            $query .= "     (SELECT ";
            $query .= "         year, month, SEMESTER, schregno, classcd, subclasscd, ".$model->sickdiv[$model->field["SICK"]]." ";
            if ($model->Properties["useCurriculumcd"] == "1") {
                $query .= "       ,SCHOOL_KIND, CURRICULUM_CD ";
            }
            $query .= "     FROM ";
            $query .= "         attend_subclass_dat ";
            $query .= "     WHERE ";
            $query .= "         copycd = '0' ";
            $query .= "         AND year = '".CTRL_YEAR."' ";
            $query .= "         AND month = '".$monthdata[$i]["MONTH"]."' ";
            $query .= "         AND SEMESTER = '".$monthdata[$i]["SEMESTER"]."' ";
            if ($model->Properties["useCurriculumcd"] == "1") {
                $query .= "         AND CLASSCD || '-' || SCHOOL_KIND || '-' ||  CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
            } else {
                $query .= "         AND classcd = '".substr($model->field["SUBCLASSCD"], 0, 2)."' ";
                $query .= "         AND subclasscd = '".$model->field["SUBCLASSCD"]."' ";
            }
            $query .= "     )w".$tmp." ";
            $query .= "     ON ";
            $query .= "         t1.schregno = w".$tmp.".schregno ";
        }
        $query .= " ORDER BY ";
        $query .= " t1.grade, t1.hr_class, t1.attendno";

        return $query;
    }

    // MAX(APPOINTED_DAY), MAX(LESSON)取得
    function selectInputQuery($model)
    {
        $query  = "WITH SCH_T AS (SELECT ";
        $query .= "        T1.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_BASE_MST T1, CHAIR_STD_DAT T2, SCHREG_REGD_HDAT T3, SCHREG_REGD_DAT T4 ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
            $query .= "          ON G1.YEAR         = T4.YEAR ";
            $query .= "         AND G1.GRADE        = T4.GRADE ";
            $query .= "         AND G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= "     WHERE ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "         AND T1.SCHREGNO = T4.SCHREGNO ";
        $query .= "         AND T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND T2.YEAR = T3.YEAR ";
        $query .= "         AND T2.YEAR = T4.YEAR ";
        $query .= "         AND T2.CHAIRCD = '".$model->field["CHAIRCD"]."' ";
        $query .= "         AND T3.GRADE = T4.GRADE ";
        $query .= "         AND T3.HR_CLASS = T4.HR_CLASS ";
        $query .= "         AND T3.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "         AND T3.SEMESTER = T4.SEMESTER ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO ";
        $query .= "     ) ";
        $query .= "SELECT ";
        $query .= "    MAX(APPOINTED_DAY) AS APPOINTED_DAY, ";
        $query .= "    MAX(LESSON) AS LESSON ";
        $query .= "FROM ";
        $query .= "    ATTEND_SUBCLASS_DAT ";
        $query .= "WHERE ";
        $query .= "    COPYCD = '0' ";
        $query .= "    AND YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND MONTH = '".$model->field["MONTH"]."' ";
        $query .= "    AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "    AND CLASSCD || '-' || SCHOOL_KIND || '-' ||  CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "    AND CLASSCD = '".substr($model->field["SUBCLASSCD"], 0, 2)."' ";
            $query .= "    AND SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= "    AND SCHREGNO IN (SELECT SCHREGNO FROM SCH_T) ";
        return $query;
    }

    //MAX(SUM(LESSON))取得
    function getMaxSumLesson($model) {
        $query  = " WITH SCH_T AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
            $query .= "          ON G1.YEAR         = T1.YEAR ";
            $query .= "         AND G1.GRADE        = T1.GRADE ";
            $query .= "         AND G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= "        ,CHAIR_STD_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "         T1.YEAR     = T2.YEAR AND ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "         T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "         T2.CHAIRCD  = '".$model->field["CHAIRCD"]."' ";
        $query .= " ), SUM_LESSON AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         SUM(LESSON) AS LESSON ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUBCLASS_DAT ";
        $query .= "     WHERE ";
        $query .= "         COPYCD      = '0' AND ";
        $query .= "         YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "         SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         CLASSCD || '-' || SCHOOL_KIND || '-' ||  CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        } else {
            $query .= "         CLASSCD = '".substr($model->field["SUBCLASSCD"], 0, 2)."' AND ";
            $query .= "         SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        }
        $query .= "         SCHREGNO IN (SELECT SCHREGNO FROM SCH_T) ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     MAX(LESSON) AS LESSON ";
        $query .= " FROM ";
        $query .= "     SUM_LESSON ";

        return $query;
    }

    function getSchregRegdGdatGradeCd($grade) {
        $query  = " SELECT ";
        $query .= "     GRADE_CD ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";
        $query .= "     AND GRADE = '{$grade}' ";

        return $query;
    }

    function getAppointedGradeDay($month, $semester, $grade) {
        $query  = " SELECT ";
        $query .= "     APPOINTED_DAY ";
        $query .= " FROM ";
        $query .= "     V_APPOINTED_DAY_GRADE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";
        $query .= "     AND MONTH = '{$month}' ";
        $query .= "     AND SEMESTER = '{$semester}' ";
        $query .= "     AND GRADE = '{$grade}' ";

        return $query;
    }

    //異動データ（退学・転学・卒業）取得
    function getIdouData($schregno, $date) {

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     GRD_DIV IN ('1', '2', '3', '6') AND ";
        $query .= "     GRD_DATE < '".$date."' ";

        return $query;
    }

    //異動データ（留学・休学）取得
    function getTransferData1($schregno, $date) {

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_TRANSFER_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     TRANSFERCD IN ('1', '2') AND ";
        $query .= "     '".$date."' BETWEEN TRANSFER_SDATE AND CASE WHEN TRANSFER_EDATE IS NULL THEN '".(CTRL_YEAR+1)."-03-31' ELSE TRANSFER_EDATE END ";

        return $query;
    }

    //異動日付（留学・休学）取得
    function getTransferData2($schregno, $month) {

        $query  = " SELECT ";
        if($month == "s") {
            $query .= "     MONTH(TRANSFER_SDATE) AS S_MONTH ";
        } else {
            $query .= "     MONTH(TRANSFER_EDATE) AS E_MONTH ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_TRANSFER_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     TRANSFERCD IN ('1', '2') AND ";
        $query .= "     TRANSFER_SDATE BETWEEN '".CTRL_YEAR."-04-01' AND '".(CTRL_YEAR+1)."-03-31' ";

        return $query;
    }

    /* データ更新処理 */
    function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();

        for($i = 1; $i <= $model->field["std_num"]; $i++)
        {
            $tmp  = "SCHREGNO".sprintf("%02d", $i);
            $temp = $model->sickdiv[$model->field["SICK"]]."-".sprintf("%02d", $i);

            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     attend_subclass_dat ";
            $query .= " WHERE COPYCD ='0' ";
            $query .= "   AND year = '".CTRL_YEAR."' ";
            $query .= "   AND month = '".$model->field["MONTH"]."' ";
            $query .= "   AND SEMESTER = '".$model->field["SEMESTER"]."' ";
            $query .= "   AND schregno = '".$model->field[$tmp]."' ";
            if ($model->Properties["useCurriculumcd"] == "1") {
                $query .= "   AND CLASSCD || '-' || SCHOOL_KIND || '-' ||  CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
            } else {
                $query .= "   AND classcd = '".substr($model->field["SUBCLASSCD"], 0, 2)."' ";
                $query .= "   AND subclasscd = '".$model->field["SUBCLASSCD"]."' ";
            }

            $flag = $db->getOne($query);
            $data = array();
            $setAppDay = $model->gradeAppDay[$model->schGradeArray[$model->field[$tmp]]];

            if ($flag) {
                if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
                    $data[$model->sickdiv[$model->field["SICK"]]][NUMBER] = $model->field[$temp];
                } else {
                    $data[$model->sickdiv[$model->field["SICK"]]][NUMBER] = ($model->field[$temp]) ? $model->field[$temp] : 0;
                }
                $data["APPOINTED_DAY"][TEXT] = $setAppDay;
                $data["LESSON"][NUMBER]      = $model->lesson;
                $data["REGISTERCD"][TEXT]    = STAFFCD ;                                 //登録者コード
                $data["UPDATED"][FUNC]       = "sysdate()";                              //更新日付

                $where  = " WHERE COPYCD ='0' ";
                $where .= "   AND year = '".CTRL_YEAR."' ";
                $where .= "   AND month = '".$model->field["MONTH"]."' ";
                $where .= "   AND SEMESTER = '".$model->field["SEMESTER"]."' ";
                $where .= "   AND schregno = '".$model->field[$tmp]."' ";
                if ($model->Properties["useCurriculumcd"] == "1") {
                    $where .= "   AND CLASSCD || '-' || SCHOOL_KIND || '-' ||  CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
                } else {
                    $where .= "   AND classcd = '".substr($model->field["SUBCLASSCD"], 0, 2)."' ";
                    $where .= "   AND subclasscd = '".$model->field["SUBCLASSCD"]."' ";
                }

                $query = Query::updateSQL($data, ATTEND_SUBCLASS_DAT, $where);
            } else {
                $data["COPYCD"][TEXT]        = "0";
                $data["YEAR"][TEXT]          = CTRL_YEAR;
                $data["MONTH"][TEXT]         = $model->field["MONTH"];
                $data["SCHREGNO"][TEXT]      = $model->field[$tmp];
                $data["SEMESTER"][TEXT]      = $model->field["SEMESTER"];
                if ($model->Properties["useCurriculumcd"] == "1") {
                    list($classCd, $schoolKind, $curriculumCd, $subclassCD) = preg_split("/-/", $model->field["SUBCLASSCD"]);
                    $data["CLASSCD"][TEXT]       = $classCd;
                    $data["SCHOOL_KIND"][TEXT]   = $schoolKind;
                    $data["CURRICULUM_CD"][TEXT] = $curriculumCd;
                    $data["SUBCLASSCD"][TEXT]    = $subclassCD;
                } else {
                    $data["CLASSCD"][TEXT]       = substr($model->field["SUBCLASSCD"], 0, 2);
                    $data["SUBCLASSCD"][TEXT]    = $model->field["SUBCLASSCD"];
                }
                $data["APPOINTED_DAY"][TEXT] = $setAppDay;
                $data["LESSON"][NUMBER]      = $model->lesson;
                if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
                    $data[$model->sickdiv[$model->field["SICK"]]][NUMBER]    = $model->field[$temp];
                } else {
                    $data[$model->sickdiv[$model->field["SICK"]]][NUMBER]    = ($model->field[$temp]) ? $model->field[$temp] : 0;
                    if ($model->sickdiv[$model->field["SICK"]] != "OFFDAYS") {
                        $data["OFFDAYS"][NUMBER]     = 0;
                    }
                    if ($model->sickdiv[$model->field["SICK"]] != "ABSENT") {
                        $data["ABSENT"][NUMBER]      = 0;
                    }
                    if ($model->sickdiv[$model->field["SICK"]] != "SUSPEND") {
                        $data["SUSPEND"][NUMBER]     = 0;
                    }
                    if ($model->Properties["useKoudome"] == "true") {
                        if ($model->sickdiv[$model->field["SICK"]] != "KOUDOME") {
                            $data["KOUDOME"][NUMBER]     = 0;
                        }
                    }
                    if ($model->Properties["useVirus"] == "true") {
                        if ($model->sickdiv[$model->field["SICK"]] != "VIRUS") {
                            $data["VIRUS"][NUMBER]       = 0;
                        }
                    }
                    if ($model->sickdiv[$model->field["SICK"]] != "MOURNING") {
                        $data["MOURNING"][NUMBER]    = 0;
                    }
                    if ($model->sickdiv[$model->field["SICK"]] != "ABROAD") {
                        $data["ABROAD"][NUMBER]      = 0;
                    }
                    $sick = explode(",",str_replace($model->field["SICK"],"","4,5,6"));
                    foreach($sick as $code ){
                        if($code){
                            $data[$model->sickdiv[$code]][NUMBER]    = 0;
                        }
                    }
                    $data["NURSEOFF"][NUMBER]    = 0;
                    if ($model->sickdiv[$model->field["SICK"]] != "LATE") {
                        $data["LATE"][NUMBER]        = 0;
                    }
                    if ($model->sickdiv[$model->field["SICK"]] != "EARLY") {
                        $data["EARLY"][NUMBER]       = 0;
                    }
                }
                $data["REGISTERCD"][TEXT]    = STAFFCD ;                                 //登録者コード
                $data["UPDATED"][FUNC]       = "sysdate()";                              //更新日付

                $query = Query::insertSQL($data, ATTEND_SUBCLASS_DAT);
            }
            $db->query($query);

            //ATTEND_SEMES_DATチェック
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ATTEND_SEMES_DAT ";
            $query .= " WHERE COPYCD ='0' ";
            $query .= "   AND YEAR = '".CTRL_YEAR."' ";
            $query .= "   AND MONTH = '".$model->field["MONTH"]."' ";
            $query .= "   AND SEMESTER = '".$model->field["SEMESTER"]."' ";
            $query .= "   AND SCHREGNO = '".$model->field[$tmp]."' ";

            $flg2 = $db->getOne($query);
            $data = array();

            //ATTEND_SEMES_DAT追加
            if(!$flg2) {
                $data["COPYCD"][TEXT]           = "0";
                $data["YEAR"][TEXT]             = CTRL_YEAR;
                $data["MONTH"][TEXT]            = $model->field["MONTH"];
                $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
                $data["SCHREGNO"][TEXT]         = $model->field[$tmp];
                $data["APPOINTED_DAY"][TEXT]    = $setAppDay;
                $data["LESSON"][NUMBER]         = 0;
                if ($model->Properties["use_Attend_zero_hyoji"] != "1") {
                    $data["OFFDAYS"][NUMBER]        = 0;
                    $data["ABSENT"][NUMBER]         = 0;
                    $data["SUSPEND"][NUMBER]        = 0;
                    if ($model->Properties["useKoudome"] == "true") {
                        $data["KOUDOME"][NUMBER]          = 0;
                    }
                    if ($model->Properties["useVirus"] == "true") {
                        $data["VIRUS"][NUMBER]          = 0;
                    }
                    $data["MOURNING"][NUMBER]       = 0;
                    $data["ABROAD"][NUMBER]         = 0;
                    $data["SICK"][NUMBER]           = 0;
                    $data["NOTICE"][NUMBER]         = 0;
                    $data["NONOTICE"][NUMBER]       = 0;
                    $data["LATE"][NUMBER]           = 0;
                    $data["EARLY"][NUMBER]          = 0;
                }
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "SYSDATE()";

                $query = Query::insertSQL($data, ATTEND_SEMES_DAT);
                $db->query($query);
            }
        }
        Query::dbCheckIn($db);
    }

    /**
     * 生徒情報表示
     */

    // 生徒の名前取得
    function getStudentName($no)
    {
        $query  = " SELECT ";
        $query .= "     name_show ";
        $query .= " FROM ";
        $query .= "     schreg_base_mst";
        $query .= " WHERE ";
        $query .= "     schregno = '$no' ";

        $db = Query::dbCheckOut();
        $row = $db->getOne($query);
        Query::dbCheckIn($db);

        return $row;
    }

    // 生徒の詳細欠時取得
    function selectStudent($no, $cd, $grade)
    {
        $query  = " SELECT AD.ATTENDDATE,AD.PERIODCD,VNM.NAME1,ATT_DI.DI_NAME1 AS DI_CD ";
        $query .= "   FROM ATTEND_DAT AS AD ";
        $query .= "       LEFT OUTER JOIN ( SELECT NAMECD2,NAME1 ";
        $query .= "                           FROM V_NAME_MST ";
        $query .= "                          WHERE YEAR = '".CTRL_YEAR."'  ";
        $query .= "                            AND NAMECD1='B001') VNM ON VNM.NAMECD2=AD.PERIODCD  ";
        $query .= "       LEFT JOIN ( SELECT DI_CD,DI_NAME1 ";
        $query .= "                           FROM ATTEND_DI_CD_DAT ";
        $query .= "                          WHERE YEAR = '".CTRL_YEAR."' ) ATT_DI ON ATT_DI.DI_CD = AD.DI_CD, ";
        $query .= "       (SELECT SDATE,EDATE FROM V_SEMESTER_GRADE_MST WHERE YEAR ='".CTRL_YEAR."' AND SEMESTER = '9' AND GRADE = '{$grade}') AS SM ";
        $query .= " WHERE AD.SCHREGNO='".$no."'  ";
        $query .= "   AND AD.ATTENDDATE BETWEEN SM.SDATE AND SM.EDATE ";
        $query .= "   AND AD.CHAIRCD = '".$cd."' ";
        $query .= "ORDER BY AD.ATTENDDATE ";

        return $query;
    }

    /**
     * 一括更新画面
     */

    //講座名称取得
    function getChairName($model)
    {
        $query  = " SELECT ";
        $query .= "     CHAIRNAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND CHAIRCD = '".$model->field["CHAIRCD"]."' ";

        return $query;
    }

    //生徒リスト取得
    function GetStudent($model)
    {
        $query  = " WITH MAIN_T AS (SELECT ";
        $query .= "    T1.SCHREGNO, T1.NAME_SHOW, T3.HR_NAMEABBV, T3.GRADE, T3.HR_CLASS, T4.ATTENDNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1, CHAIR_STD_DAT T2, SCHREG_REGD_HDAT T3, SCHREG_REGD_DAT T4 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     AND T1.SCHREGNO = T4.SCHREGNO ";
        $query .= "     AND T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T2.YEAR = T3.YEAR ";
        $query .= "     AND T2.YEAR = T4.YEAR ";
        $query .= "     AND T2.CHAIRCD = '".$model->field["CHAIRCD"]."' ";
        $query .= "     AND T3.GRADE = T4.GRADE ";
        $query .= "     AND T3.HR_CLASS = T4.HR_CLASS ";
        $query .= "     AND T3.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T3.SEMESTER = T4.SEMESTER ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO, T1.NAME_SHOW, T3.HR_NAMEABBV, T3.GRADE, T3.HR_CLASS, T4.ATTENDNO) ";
        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     W.LESSON, ";
        $query .= "     W.APPOINTED_DAY ";
        $query .= " FROM ";
        $query .= "     MAIN_T T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     (SELECT ";
        $query .= "         SCHREGNO, LESSON, APPOINTED_DAY ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUBCLASS_DAT ";
        $query .= "     WHERE ";
        $query .= "         COPYCD = '0' ";
        $query .= "         AND YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND MONTH = '".$model->field["MONTH"]."' ";
        $query .= "         AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "         AND CLASSCD = '".substr($model->field["SUBCLASSCD"], 0, 2)."' ";
            $query .= "         AND SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= "     ) W ";
        $query .= "     ON ";
        $query .= "         T1.SCHREGNO = W.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ";

        return $query;
    }

    function getUpdateSubQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $schArray = explode(",", $model->subfield["SELECTDATA"]);
        for ($i = 0; $i < get_count($schArray); $i++)
        {
            $tmp  = "SCHREGNO".sprintf("%02d", $i);
            $temp = $model->sickdiv[$model->field["SICK"]]."-".sprintf("%02d", $i);

            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     attend_subclass_dat ";
            $query .= " WHERE COPYCD = '0' ";
            $query .= "   AND YEAR = '" . CTRL_YEAR . "' ";
            $query .= "   AND MONTH = '" . $model->field["MONTH"] . "' ";
            $query .= "   AND SCHREGNO = '" . $schArray[$i] . "' ";
            $query .= "   AND SEMESTER = '" . $model->field["SEMESTER"] . "' ";
            if ($model->Properties["useCurriculumcd"] == "1") {
                $query .= "   AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
            } else {
                $query .= "   AND CLASSCD = '" . substr($model->field["SUBCLASSCD"], 0, 2) . "' ";
                $query .= "   AND SUBCLASSCD = '" . $model->field["SUBCLASSCD"] . "' ";
            }

            $flag = $db->getOne($query);
            $data = array();

            if ($flag) {
                $data["APPOINTED_DAY"][TEXT] = $model->subfield["APPOINTED_DAY"];
                $data["LESSON"][NUMBER]      = $model->subfield["LESSON"];

                $where  = " WHERE COPYCD ='0' ";
                $where .= "   AND YEAR = '" . CTRL_YEAR . "' ";
                $where .= "   AND MONTH = '" . $model->field["MONTH"] . "' ";
                $where .= "   AND SCHREGNO = '" . $schArray[$i] . "' ";
                $where .= "   AND SEMESTER = '" . $model->field["SEMESTER"] . "' ";
                if ($model->Properties["useCurriculumcd"] == "1") {
                    $where .= "   AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
                } else {
                    $where .= "   AND CLASSCD = '" . substr($model->field["SUBCLASSCD"], 0, 2) . "' ";
                    $where .= "   AND SUBCLASSCD = '" . $model->field["SUBCLASSCD"] . "' ";
                }

                $query = Query::updateSQL($data, ATTEND_SUBCLASS_DAT, $where);
            } else {
                $data["COPYCD"][TEXT]        = "0";
                $data["YEAR"][TEXT]          = CTRL_YEAR;
                $data["MONTH"][TEXT]         = $model->field["MONTH"];
                $data["SCHREGNO"][TEXT]      = $schArray[$i];
                $data["SEMESTER"][TEXT]      = $model->field["SEMESTER"];
                if ($model->Properties["useCurriculumcd"] == "1") {
                    list($classCd, $schoolKind, $curriculumCd, $subclassCD) = preg_split("/-/", $model->field["SUBCLASSCD"]);
                    $data["CLASSCD"][TEXT]       = $classCd;
                    $data["SCHOOL_KIND"][TEXT]   = $schoolKind;
                    $data["CURRICULUM_CD"][TEXT] = $curriculumCd;
                    $data["SUBCLASSCD"][TEXT]    = $subclassCD;
                } else {
                    $data["CLASSCD"][TEXT]       = substr($model->field["SUBCLASSCD"], 0, 2);
                    $data["SUBCLASSCD"][TEXT]    = $model->field["SUBCLASSCD"];
                }
                $data["APPOINTED_DAY"][TEXT] = $model->subfield["APPOINTED_DAY"];
                $data["LESSON"][NUMBER]      = $model->subfield["LESSON"];

                $query = Query::insertSQL($data, ATTEND_SUBCLASS_DAT);
            }
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
    }

    //遅刻何回で欠課とするかの指数取得
    function getScAbsentCov($model) {
        $query  = " SELECT ";
        $query .= "     absent_cov,absent_cov_late ";
        $query .= " FROM ";
        $query .= "     school_mst ";
        $query .= " WHERE ";
        $query .= "     year = '".CTRL_YEAR."' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //休学時の欠課をカウントするかどうかのフラグ(1 or NULL)を取得。1:欠課をカウントする
    function getOffdaysFlg($model, $year) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     SUB_OFFDAYS,SUB_ABSENT,SUB_SUSPEND,SUB_MOURNING,SUB_VIRUS,SUB_KOUDOME ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR='{$year}' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    function GetAttendData($chaircd,$subclasscd, $absent_cov, $absent_cov_late, $offdaysFlg, $schregno, $model) {
        $lesson = "SUM(value(LESSON,0)) - SUM(value(ABROAD,0))";
        //学校マスタの各フラグを参照し「休学・出停・忌引・出停（伝染病）」を授業時数から除く処理。
        if ($offdaysFlg["SUB_OFFDAYS"]  != "1") $lesson .= " - SUM(value(OFFDAYS,0))";
        if ($offdaysFlg["SUB_SUSPEND"]  != "1") $lesson .= " - SUM(value(SUSPEND,0))";
        if ($model->Properties["useKoudome"] == "true") {
            if ($offdaysFlg["SUB_KOUDOME"]  != "1") $lesson .= " - SUM(value(KOUDOME,0))";
        }
        if ($model->Properties["useVirus"] == "true") {
            if ($offdaysFlg["SUB_VIRUS"]    != "1") $lesson .= " - SUM(value(VIRUS,0))";
        }
        if ($offdaysFlg["SUB_MOURNING"] != "1") $lesson .= " - SUM(value(MOURNING,0))";
        $notice = "SUM(value(notice,0)) + SUM(value(nonotice,0)) + SUM(value(sick,0)) + SUM(value(nurseoff,0))";
        //学校マスタの各フラグを参照し「休学・公欠・出停・忌引・出停（伝染病）」を欠課に含める処理。
        if ($offdaysFlg["SUB_OFFDAYS"]  == "1") $notice .= " + SUM(value(OFFDAYS,0))";
        if ($offdaysFlg["SUB_ABSENT"]   == "1") $notice .= " + SUM(value(ABSENT,0))";
        if ($offdaysFlg["SUB_SUSPEND"]  == "1") $notice .= " + SUM(value(SUSPEND,0))";
        if ($model->Properties["useKoudome"] == "true") {
            if ($offdaysFlg["SUB_KOUDOME"]  == "1") $notice .= " + SUM(value(KOUDOME,0))";
        }
        if ($model->Properties["useVirus"] == "true") {
            if ($offdaysFlg["SUB_VIRUS"]    == "1") $notice .= " + SUM(value(VIRUS,0))";
        }
        if ($offdaysFlg["SUB_MOURNING"] == "1") $notice .= " + SUM(value(MOURNING,0))";
        $late_early = "SUM(value(late,0)) + SUM(value(early,0))";
        /* 出欠累積情報 */
        $query  = "SELECT schregno ";
        $query .= "       ,".$lesson."  AS LESSON ";
        $query .= "       ,".$notice."  AS T_NOTICE ";
        if($model->Properties["chikokuHyoujiFlg"] == "1"){
            $query .= "   ,".$late_early." AS T_LATEEARLY ";
        } else {
            if ($absent_cov == "4" && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                $query .= "   ,MOD(".$late_early.",".$absent_cov_late.") AS T_LATEEARLY ";
            } elseif ($absent_cov == "2" && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                $query .= "   ,MOD(".$late_early.",".$absent_cov_late.") AS T_LATEEARLY ";
            } else {
                $query .= "   ,".$late_early." AS T_LATEEARLY ";
            }
        }
        //小数点ありAdd
        if ($absent_cov == "4" && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "   ,DECIMAL((FLOAT(".$late_early.") / ".$absent_cov_late.") + (".$notice."),4,1) AS NOTICE_LATE ";
        } elseif ($absent_cov == "2" && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "   ,((".$late_early.") / ".$absent_cov_late.") + (".$notice.") AS NOTICE_LATE ";
        } else {
            $query .= "   ,".$notice."  AS NOTICE_LATE ";
        }
        $query .= "  FROM attend_subclass_dat  ";
        $query .= " WHERE COPYCD     = '0' ";
        $query .= "   AND SCHREGNO   = '{$schregno}' ";
        $query .= "   AND year       = '".CTRL_YEAR."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "   AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$subclasscd."' ";
        } else {
            $query .= "   AND subclasscd = '".$subclasscd."' ";
        }
        $query .= "   AND EXISTS (SELECT 'X' FROM chair_std_dat ";
        $query .= "                WHERE year    = '".CTRL_YEAR."'";
        $query .= "                  AND chaircd = '".$chaircd."')";
        $query .= " GROUP BY schregno";

        return $query;
    }

    //(学期ごとに集計）する場合
    function GetAttendData2($chaircd,$subclasscd, $absent_cov, $absent_cov_late, $offdaysFlg, $schregno, $model) {
        $lesson = "SUM(value(LESSON,0)) - SUM(value(ABROAD,0))";
        //学校マスタの各フラグを参照し「休学・出停・忌引・出停（伝染病）」を授業時数から除く処理。
        if ($offdaysFlg["SUB_OFFDAYS"]  != "1") $lesson .= " - SUM(value(OFFDAYS,0))";
        if ($offdaysFlg["SUB_SUSPEND"]  != "1") $lesson .= " - SUM(value(SUSPEND,0))";
        if ($model->Properties["useKoudome"] == "true") {
            if ($offdaysFlg["SUB_KOUDOME"]  != "1") $lesson .= " - SUM(value(KOUDOME,0))";
        }
        if ($model->Properties["useVirus"] == "true") {
            if ($offdaysFlg["SUB_VIRUS"]    != "1") $lesson .= " - SUM(value(VIRUS,0))";
        }
        if ($offdaysFlg["SUB_MOURNING"] != "1") $lesson .= " - SUM(value(MOURNING,0))";
        $notice = "SUM(value(notice,0)) + SUM(value(nonotice,0)) + SUM(value(sick,0)) + SUM(value(nurseoff,0))";
        //学校マスタの各フラグを参照し「休学・公欠・出停・忌引・出停（伝染病）」を欠課に含める処理。
        if ($offdaysFlg["SUB_OFFDAYS"]  == "1") $notice .= " + SUM(value(OFFDAYS,0))";
        if ($offdaysFlg["SUB_ABSENT"]   == "1") $notice .= " + SUM(value(ABSENT,0))";
        if ($offdaysFlg["SUB_SUSPEND"]  == "1") $notice .= " + SUM(value(SUSPEND,0))";
        if ($model->Properties["useKoudome"] == "true") {
            if ($offdaysFlg["SUB_KOUDOME"]  == "1") $notice .= " + SUM(value(KOUDOME,0))";
        }
        if ($model->Properties["useVirus"] == "true") {
            if ($offdaysFlg["SUB_VIRUS"]    == "1") $notice .= " + SUM(value(VIRUS,0))";
        }
        if ($offdaysFlg["SUB_MOURNING"] == "1") $notice .= " + SUM(value(MOURNING,0))";
        $late_early = "SUM(value(late,0)) + SUM(value(early,0))";
        $query  = "SELECT schregno ";
        $query .= "       ,SUM(LESSON)      AS LESSON";
        $query .= "       ,SUM(T_NOTICE)    AS T_NOTICE";
        $query .= "       ,SUM(T_LATEEARLY) AS T_LATEEARLY";
        $query .= "       ,SUM(NOTICE_LATE) AS NOTICE_LATE";
        $query .= "  FROM ";
        $query .= "       (SELECT  T1.schregno";
        $query .= "               ,T1.semester ";
        $query .= "               ,".$lesson." AS LESSON";
        $query .= "               ,".$notice." AS T_NOTICE ";
        if($model->Properties["chikokuHyoujiFlg"] == "1"){
            $query .= "           ,".$late_early." AS T_LATEEARLY ";
        } else {
            if ($absent_cov == "3" && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                $query .= "           ,MOD(".$late_early.",".$absent_cov_late.") AS T_LATEEARLY ";
            } elseif ($absent_cov == "1" && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                $query .= "           ,MOD(".$late_early.",".$absent_cov_late.") AS T_LATEEARLY ";
            } else {
                $query .= "           ,".$late_early." AS T_LATEEARLY ";
            }
        }
        //小数点ありAdd
        if ($absent_cov == "3" && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "           ,DECIMAL((FLOAT(".$late_early.") / ".$absent_cov_late.") + (".$notice."),4,1) AS NOTICE_LATE ";
        } elseif ($absent_cov == "1" && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "           ,((".$late_early.") / ".$absent_cov_late.") + (".$notice.") AS NOTICE_LATE ";
        } else {
            $query .= "           ,".$notice." AS NOTICE_LATE ";
        }
        $query .= "          FROM attend_subclass_dat T1   ";
        $query .= "         WHERE COPYCD        = '0' ";
        $query .= "           AND T1.year       = '".CTRL_YEAR."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "           AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$subclasscd."' ";
        } else {
            $query .= "           AND T1.subclasscd = '".$subclasscd."' ";
        }
        $query .= "           AND EXISTS (SELECT 'X' FROM chair_std_dat T2 ";
        $query .= "                        WHERE T1.year     = T2.year ";
        $query .= "                          AND T1.semester = T2.semester ";
        $query .= "                          AND T1.schregno = T2.schregno "; 
        $query .= "                          AND T2.chaircd  = '".$chaircd."')";
        $query .= "         GROUP BY T1.schregno, T1.semester) T1 ";
        $query .= " WHERE SCHREGNO = '{$schregno}' ";
        $query .= " GROUP BY schregno ";

        return $query;
    }

    //欠課の上限値取得
    function getJogenchi($row, $subclasscd, $model) {
        $sem1 = (CTRL_SEMESTER == 1) ? "" : CTRL_SEMESTER;
        $sem2 = CTRL_SEMESTER;

        $query  = " WITH CREDIT_M AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.ABSENCE_HIGH, ";
        $query .= "         T1.GET_ABSENCE_HIGH, ";
        $query .= "         T1.ABSENCE_WARN_SHUTOKU_SEM1, ";
        $query .= "         T1.ABSENCE_WARN_RISHU_SEM1, ";
        $query .= "         T1.ABSENCE_WARN_SHUTOKU_SEM2, ";
        $query .= "         T1.ABSENCE_WARN_RISHU_SEM2, ";
        $query .= "         T1.ABSENCE_WARN_SHUTOKU_SEM3, ";
        $query .= "         T1.ABSENCE_WARN_RISHU_SEM3, ";
        $query .= "         T1.ABSENCE_WARN, ";
        $query .= "         T1.ABSENCE_WARN2, ";
        $query .= "         T1.ABSENCE_WARN3, ";
        $query .= "         T2.JUGYOU_JISU_FLG, ";
        $query .= "         T3.NAMESPARE1 ";
        $query .= "     FROM ";
        $query .= "         V_CREDIT_MST T1 ";
        $query .= "         LEFT JOIN V_SCHOOL_MST T2 ON T1.YEAR = T2.YEAR ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                              AND T2.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "                              AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "         LEFT JOIN V_NAME_MST T3 ON T1.YEAR = T3.YEAR AND T3.NAMECD1 = 'C042' AND T3.NAMECD2 = '01' ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "         T1.COURSECD     = '".$row["COURSECD"]."' AND ";
        $query .= "         T1.MAJORCD      = '".$row["MAJORCD"]."' AND ";
        $query .= "         T1.COURSECODE   = '".$row["COURSECODE"]."' AND ";
        $query .= "         T1.GRADE        = '".$row["GRADE"]."' AND ";
        $query .= "         T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$subclasscd."' ";
        $query .= " ), ABSENCE_HIGH AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR, ";
        $query .= "         COMP_ABSENCE_HIGH, ";
        $query .= "         GET_ABSENCE_HIGH ";
        $query .= "     FROM ";
        $query .= "         SCHREG_ABSENCE_HIGH_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "         DIV         = '2' AND ";
        $query .= "         SCHREGNO    = '".$row["SCHREGNO"]."' AND ";
        $query .= "         CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$subclasscd."' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     (CASE WHEN T1.JUGYOU_JISU_FLG = '2' THEN T2.COMP_ABSENCE_HIGH ";
        $query .= "           ELSE T1.ABSENCE_HIGH END ";
        $query .= "      - VALUE(CASE WHEN T1.NAMESPARE1 = '1' THEN T1.ABSENCE_WARN{$sem1} ";
        $query .= "                   ELSE T1.ABSENCE_WARN_RISHU_SEM{$sem2} END, 0)) AS RISHU_JOGENCHI, ";
        $query .= "     (CASE WHEN T1.JUGYOU_JISU_FLG = '2' THEN T2.GET_ABSENCE_HIGH ";
        $query .= "           ELSE T1.GET_ABSENCE_HIGH END ";
        $query .= "      - VALUE(CASE WHEN T1.NAMESPARE1 = '1' THEN T1.ABSENCE_WARN{$sem1} ";
        $query .= "                   ELSE T1.ABSENCE_WARN_RISHU_SEM{$sem2} END, 0)) AS SHUTOKU_JOGENCHI ";
        $query .= " FROM ";
        $query .= "     CREDIT_M T1 ";
        $query .= "     LEFT JOIN ABSENCE_HIGH T2 ON T1.YEAR = T2.YEAR ";

        return $query;
    }

    //課程学科コンボ取得
    function c035fgetCourseMajor($model, $sk) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.COURSECD || T1.MAJORCD AS VALUE, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || ':' || S1.COURSENAME || S1.MAJORNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN V_COURSE_MAJOR_MST S1 ";
        $query .= "          ON T1.YEAR         = S1.YEAR ";
        $query .= "         AND T1.COURSECD     = S1.COURSECD ";
        $query .= "         AND T1.MAJORCD      = S1.MAJORCD ";
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT S2 ";
            $query .= "          ON T1.YEAR         = S2.YEAR ";
            $query .= "         AND T1.GRADE        = S2.GRADE ";
            $query .= "         AND S2.SCHOOL_KIND  = '".$sk."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //MAX(LESSON)取得
    function c035fgetInputMaxLesson($model) {
        $query  = " WITH SCH_T AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT T1, ";
        $query .= "         SCHREG_REGD_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = T2.YEAR AND ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "         T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "         T1.CHAIRCD  = '".$model->field["CHAIRCD"]."' ";
        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            $query .= "     AND T2.COURSECD || T2.MAJORCD = '".$model->field["COURSE_MAJOR"]."' ";
        }
        if ($model->field["MONTH"]) {
            $query .= "         AND MONTH(T1.APPDATE) <= ".intval($model->field["MONTH"])." + CASE WHEN ".intval($model->field["MONTH"])." < 4 THEN 12 ELSE 0 END AND ";
            $query .= "         ".intval($model->field["MONTH"])." <= MONTH(T1.APPENDDATE) + CASE WHEN MONTH(T1.APPENDDATE) < 4 THEN 12 ELSE 0 END ";
            $query .= "         AND CASE WHEN ".intval($model->field["MONTH"])." < 4 THEN ".intval($model->field["MONTH"])."+12 ELSE ".intval($model->field["MONTH"])." END ";
            $query .= "         BETWEEN ";
            $query .= "             CASE WHEN MONTH(T1.APPDATE) < 4 THEN MONTH(T1.APPDATE)+12 ELSE MONTH(T1.APPDATE) END ";
            $query .= "         AND ";
            $query .= "             CASE WHEN MONTH(T1.APPENDDATE) < 4 THEN MONTH(T1.APPENDDATE)+12 ELSE MONTH(T1.APPENDDATE) END ";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     MAX(LESSON) AS LESSON ";
        $query .= " FROM ";
        $query .= "     ATTEND_SUBCLASS_DAT ";
        $query .= " WHERE ";
        $query .= "     COPYCD      = '0' AND ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     MONTH       = '".$model->field["MONTH"]."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' ||  CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        } else {
            $query .= "     CLASSCD = '".substr($model->field["SUBCLASSCD"], 0, 2)."' AND ";
            $query .= "     SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        }
        $query .= "     SCHREGNO IN (SELECT SCHREGNO FROM SCH_T) ";

        return $query;
    }

    //授業時数区分取得
    function c035fgetJugyouJisuFlg($model, $cm="") {
        $query  = " SELECT ";
        $query .= "     JUGYOU_JISU_FLG ";
        $query .= " FROM ";
        if ($cm) {
            $query .= "     V_SCHOOL_GCM_MST ";
        } else {
            $query .= "     V_SCHOOL_MST ";
        }
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        } else if ($cm) {
            $query .= " AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        }
        if ($cm) {
            $query .= " AND COURSECD || MAJORCD = '".$cm."' ";
        }

        return $query;
    }

    //学期の最終月判定
    function c035fgetMaxSemeMonthCnt($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     MONTH(EDATE) = ".intval($model->field["MONTH"])." ";

        return $query;
    }

    //単位数取得
    function c035fgetCredit($model) {
        $query  = " WITH MIN_SCH AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         MIN(T2.GRADE || T2.HR_CLASS || T2.ATTENDNO) AS SCH ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT T1, ";
        $query .= "         SCHREG_REGD_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = T2.YEAR AND ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "         T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "         T1.CHAIRCD  = '".$model->field["CHAIRCD"]."' ";
        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            $query .= "     AND T2.COURSECD || T2.MAJORCD = '".$model->field["COURSE_MAJOR"]."' ";
        }
        if ($model->field["MONTH"]) {
            $query .= "         AND MONTH(T1.APPDATE) <= ".intval($model->field["MONTH"])." + CASE WHEN ".intval($model->field["MONTH"])." < 4 THEN 12 ELSE 0 END AND ";
            $query .= "         ".intval($model->field["MONTH"])." <= MONTH(T1.APPENDDATE) + CASE WHEN MONTH(T1.APPENDDATE) < 4 THEN 12 ELSE 0 END ";
            $query .= "         AND CASE WHEN ".intval($model->field["MONTH"])." < 4 THEN ".intval($model->field["MONTH"])."+12 ELSE ".intval($model->field["MONTH"])." END ";
            $query .= "         BETWEEN ";
            $query .= "             CASE WHEN MONTH(T1.APPDATE) < 4 THEN MONTH(T1.APPDATE)+12 ELSE MONTH(T1.APPDATE) END ";
            $query .= "         AND ";
            $query .= "             CASE WHEN MONTH(T1.APPENDDATE) < 4 THEN MONTH(T1.APPENDDATE)+12 ELSE MONTH(T1.APPENDDATE) END ";
        }
        $query .= " ), SCH_INFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR, ";
        $query .= "         COURSECD, ";
        $query .= "         MAJORCD, ";
        $query .= "         GRADE, ";
        $query .= "         COURSECODE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "         GRADE || HR_CLASS || ATTENDNO IN (SELECT SCH FROM MIN_SCH) ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     CREDITS ";
        $query .= " FROM ";
        $query .= "     CREDIT_MST T1, ";
        $query .= "     SCH_INFO T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = T2.YEAR AND ";
        $query .= "     T1.COURSECD     = T2.COURSECD AND ";
        $query .= "     T1.MAJORCD      = T2.MAJORCD AND ";
        $query .= "     T1.GRADE        = T2.GRADE AND ";
        $query .= "     T1.COURSECODE   = T2.COURSECODE AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' ||  CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "     SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }

        return $query;
    }

    //月毎の週数取得
    function c035fgetMonthSyusu($model, $cm="") {
        $query  = " SELECT ";
        $query .= "     SYUSU ";
        $query .= " FROM ";
        $query .= "     ATTEND_SYUSU_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     MONTH       = '".$model->field["MONTH"]."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     GRADE       = '00' AND ";
        if ($cm) {
            $query .= "     COURSECD || MAJORCD = '".$cm."' ";
        } else {
            $query .= "     COURSECD    = '0' AND ";
            $query .= "     MAJORCD     = '000' ";
        }

        return $query;
    }

    //学期の週数取得
    function c035fgetSyusu($model, $seme, $cm="") {
        $query  = " SELECT ";
        $query .= "     HOUTEI_SYUSU_SEMESTER".$seme." ";
        $query .= " FROM ";
        if ($cm) {
            $query .= "     V_SCHOOL_GCM_MST ";
        } else {
            $query .= "     V_SCHOOL_MST ";
        }
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     JUGYOU_JISU_FLG = '1' ";
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        } else if ($cm) {
            $query .= " AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        }
        if ($cm) {
            $query .= " AND COURSECD || MAJORCD = '".$cm."' ";
        }

        return $query;
    }

    //MAX(SUM(LESSON))取得
    function c035fgetMaxSumLesson($model) {
        $query  = " WITH SCH_T AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT T1, ";
        $query .= "         SCHREG_REGD_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = T2.YEAR AND ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "         T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "         T1.CHAIRCD  = '".$model->field["CHAIRCD"]."' ";
        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            $query .= "     AND T2.COURSECD || T2.MAJORCD = '".$model->field["COURSE_MAJOR"]."' ";
        }
        if ($model->field["MONTH"]) {
            $query .= "         AND MONTH(T1.APPDATE) <= ".intval($model->field["MONTH"])." + CASE WHEN ".intval($model->field["MONTH"])." < 4 THEN 12 ELSE 0 END AND ";
            $query .= "         ".intval($model->field["MONTH"])." <= MONTH(T1.APPENDDATE) + CASE WHEN MONTH(T1.APPENDDATE) < 4 THEN 12 ELSE 0 END ";
            $query .= "         AND CASE WHEN ".intval($model->field["MONTH"])." < 4 THEN ".intval($model->field["MONTH"])."+12 ELSE ".intval($model->field["MONTH"])." END ";
            $query .= "         BETWEEN ";
            $query .= "             CASE WHEN MONTH(T1.APPDATE) < 4 THEN MONTH(T1.APPDATE)+12 ELSE MONTH(T1.APPDATE) END ";
            $query .= "         AND ";
            $query .= "             CASE WHEN MONTH(T1.APPENDDATE) < 4 THEN MONTH(T1.APPENDDATE)+12 ELSE MONTH(T1.APPENDDATE) END ";
        }
        $query .= " ), SUM_LESSON AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         SUM(LESSON) AS LESSON ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUBCLASS_DAT ";
        $query .= "     WHERE ";
        $query .= "         COPYCD      = '0' AND ";
        $query .= "         YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "         SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         CLASSCD || '-' || SCHOOL_KIND || '-' ||  CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        } else {
            $query .= "         CLASSCD = '".substr($model->field["SUBCLASSCD"], 0, 2)."' AND ";
            $query .= "         SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        }
        $query .= "         SCHREGNO IN (SELECT SCHREGNO FROM SCH_T) ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     MAX(LESSON) AS LESSON ";
        $query .= " FROM ";
        $query .= "     SUM_LESSON ";

        return $query;
    }

}
?>
