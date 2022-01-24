<?php

require_once('for_php7.php');

class knjc035kQuery extends Query {

    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJC035K' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    //科目取得
    function getSubclasscd($model) {
        $query  = " SELECT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' ||  T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS VALUE, ";
            $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' ||  T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD || VALUE(T3.SUBCLASSABBV, '') AS LABEL ";
        } else {
            $query .= "     T3.SUBCLASSCD AS VALUE, ";
            $query .= "     T3.SUBCLASSCD || VALUE(T3.SUBCLASSABBV, '') AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_STF_DAT T2, ";
        $query .= "     SUBCLASS_MST T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.CHAIRCD  = T2.CHAIRCD AND ";
        //権限（制限付）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     T2.STAFFCD  = '".STAFFCD."' AND ";
        }
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T1.CLASSCD          = T3.CLASSCD AND ";
            $query .= "     T1.SCHOOL_KIND      = T3.SCHOOL_KIND AND ";
            $query .= "     T1.CURRICULUM_CD    = T3.CURRICULUM_CD AND ";
        }
        $query .= "     T1.SUBCLASSCD = T3.SUBCLASSCD ";
        //合併先科目は対象外
        $query .= "     AND NOT EXISTS (SELECT 'X' FROM ";
        $query .= "                         SUBCLASS_REPLACE_COMBINED_DAT COMB ";
        $query .= "                     WHERE ";
        $query .= "                         COMB.REPLACECD              = '1' ";
        $query .= "                     AND COMB.YEAR                   = T1.YEAR ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                     AND COMB.COMBINED_CLASSCD       = T1.CLASSCD ";
            $query .= "                     AND COMB.COMBINED_SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "                     AND COMB.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "                     AND COMB.COMBINED_SUBCLASSCD    = T1.SUBCLASSCD) ";
        $query .= " GROUP BY ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T3.CLASSCD, ";
            $query .= "     T3.SCHOOL_KIND, ";
            $query .= "     T3.CURRICULUM_CD, ";
        }
        $query .= "     T3.SUBCLASSCD, ";
        $query .= "     T3.SUBCLASSABBV ";
        $query .= " ORDER BY ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T3.CLASSCD, ";
            $query .= "     T3.SCHOOL_KIND, ";
            $query .= "     T3.CURRICULUM_CD, ";
        }
        $query .= "     T3.SUBCLASSCD ";

        return $query;
    }

    //講座取得
    function getChaircd(&$model) {
        $query  = " SELECT DISTINCT";
        $query .= "     T1.CHAIRCD AS VALUE, ";
        $query .= "     T1.CHAIRCD || VALUE(T1.CHAIRNAME, '') AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_STF_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.CHAIRCD  = T2.CHAIRCD AND ";
        //権限（制限付）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     T2.STAFFCD  = '".STAFFCD."' AND ";
        }
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' ||  T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "     T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.CHAIRCD ";

        return $query;
    }

    //メインデータ作成
    function selectSemesAll($seme="") {
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

    //対象月データの取得
    function selectMonthQuery($month, $model) {
        $setNameCd = "Z005";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."05";
        }
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = '{$setNameCd}' AND ";
        $query .= "     NAMECD2 IN (SELECT ";
        $query .= "                     CONTROL_CODE ";
        $query .= "                 FROM ";
        $query .= "                     ADMIN_CONTROL_DAT ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR = '".CTRL_YEAR."' AND ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "                     CONTROL_FLG = '2') AND ";
        $query .= "     NAMECD2 = '".sprintf('%02d', $month)."' ";
        $query .= " ORDER BY ";
        $query .= "     NAMESPARE1 ";

        return $query;
    }

    //MAX(LESSON)取得
    function getInputMaxLesson($model) {
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

    //学期の最終月判定
    function getMaxSemeMonthCnt($model) {
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

    //学期の週数取得
    function getSyusu($seme) {
        $query  = " SELECT ";
        $query .= "     HOUTEI_SYUSU_SEMESTER".$seme." ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     JUGYOU_JISU_FLG = '1' ";

        return $query;
    }

    //単位数取得
    function getCredit($model) {
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

    //MAX(SUM(LESSON))取得
    function getMaxSumLesson($model) {
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

    //締め日の取得
    function getAppointedDay($model) {
        $query  = " SELECT ";
        $query .= "     APPOINTED_DAY ";
        $query .= " FROM ";
        $query .= "     APPOINTED_DAY_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '". CTRL_YEAR ."' AND ";
        $query .= "     MONTH       = '".$model->field["MONTH"]."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' ";

        return $query;
    }

    //欠課種別取得
    function getSickDiv() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'C001' AND ";
        $query .= "     NAMECD2 IN ('4', '5', '6') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //明細
    function selectMeisaiQuery($model) {
        $query  = " WITH SCH_INFO AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T3.HR_NAMEABBV || '-' || T1.ATTENDNO AS HR_ATTENDNO, ";
        $query .= "         T2.NAME_SHOW AS NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1, ";
        $query .= "         SCHREG_BASE_MST T2, ";
        $query .= "         SCHREG_REGD_HDAT T3, ";
        $query .= "         CHAIR_STD_DAT T4 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = T3.YEAR AND ";
        $query .= "         T1.YEAR     = T4.YEAR AND ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "         T1.SEMESTER = T4.SEMESTER AND ";
        $query .= "         T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "         T1.SCHREGNO = T4.SCHREGNO AND ";
        $query .= "         T1.GRADE    = T3.GRADE AND ";
        $query .= "         T1.HR_CLASS = T3.HR_CLASS AND ";
        $query .= "         T4.CHAIRCD  = '".$model->field["CHAIRCD"]."' ";
        if ($model->field["MONTH"]) {
            $query .= "         AND MONTH(T4.APPDATE) <= ".intval($model->field["MONTH"])." + CASE WHEN ".intval($model->field["MONTH"])." < 4 THEN 12 ELSE 0 END AND ";
            $query .= "         ".intval($model->field["MONTH"])." <= MONTH(T4.APPENDDATE) + CASE WHEN MONTH(T4.APPENDDATE) < 4 THEN 12 ELSE 0 END ";
            $query .= "         AND CASE WHEN ".intval($model->field["MONTH"])." < 4 THEN ".intval($model->field["MONTH"])."+12 ELSE ".intval($model->field["MONTH"])." END ";
            $query .= "         BETWEEN ";
            $query .= "             CASE WHEN MONTH(T4.APPDATE) < 4 THEN MONTH(T4.APPDATE)+12 ELSE MONTH(T4.APPDATE) END ";
            $query .= "         AND ";
            $query .= "             CASE WHEN MONTH(T4.APPENDDATE) < 4 THEN MONTH(T4.APPENDDATE)+12 ELSE MONTH(T4.APPENDDATE) END ";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T2.APPOINTED_DAY, ";
        $query .= "     T2.LESSON, ";
        $query .= "     T2.OFFDAYS, ";
        $query .= "     T2.ABSENT, ";
        $query .= "     T2.SUSPEND, ";
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "     T2.KOUDOME, ";
        }
        if ($model->Properties["useVirus"] == "true") {
            $query .= "     T2.VIRUS, ";
        }
        $query .= "     T2.MOURNING, ";
        $query .= "     T2.ABROAD, ";
        $query .= "     T2.SICK, ";
        $query .= "     T2.NOTICE, ";
        $query .= "     T2.NONOTICE, ";
        $query .= "     T2.NURSEOFF, ";
        $query .= "     T2.LATE, ";
        $query .= "     T2.EARLY, ";
        $query .= "     T2.SCHREGNO AS SUB_SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCH_INFO T1 ";
        $query .= "     LEFT JOIN ATTEND_SUBCLASS_DAT T2 ON T1.SCHREGNO     = T2.SCHREGNO AND ";
        $query .= "                                         T2.YEAR         = '".CTRL_YEAR."' AND  ";
        $query .= "                                         T2.SEMESTER     = '".$model->field["SEMESTER"]."' AND ";
        $query .= "                                         T2.MONTH        = '".$model->field["MONTH"]."' AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                                         T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' ||  T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD   = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "                                         T2.CLASSCD      = '".substr($model->field["SUBCLASSCD"], 0, 2)."' AND ";
            $query .= "                                         T2.SUBCLASSCD   = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T1.SCHREGNO ";

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
    function getTransferData($schregno, $date) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_TRANSFER_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     TRANSFERCD IN ('1', '2') AND ";
        $query .= "     '".$date."' BETWEEN TRANSFER_SDATE AND CASE WHEN TRANSFER_EDATE IS NULL THEN '".(CTRL_YEAR+1)."' || '-03-31' ELSE TRANSFER_EDATE END ";

        return $query;
    }

    //フィールド一覧取得
    function getFieldList($table) {
        $query  = " WITH MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         COLNO, ";
        $query .= "         NAME ";
        $query .= "     FROM ";
        $query .= "         SYSIBM.SYSCOLUMNS ";
        $query .= "     WHERE ";
        $query .= "         TBNAME  = '".$table."' AND ";
        $query .= "         NULLS   = 'Y' AND ";
        $query .= "         NAME NOT IN ('APPOINTED_DAY', 'LESSON', 'REGISTERCD', 'UPDATED') ";
        $query .= "     ORDER BY ";
        $query .= "         COLNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     NAME ";
        $query .= " FROM ";
        $query .= "     MAIN ";
        $query .= " ORDER BY ";
        $query .= "     COLNO ";

        return $query;
    }

    //出欠月別累積データの更新
    function getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $subclass_field = $semes_field = array();
        //フィールド一覧取得（ATTEND_SUBCLASS_DAT）
        $query = knjc035kQuery::getFieldList("ATTEND_SUBCLASS_DAT");
        $subclass_field = $db->getCol($query);
        //フィールド一覧取得（ATTEND_SEMES_DAT）
        $query = knjc035kQuery::getFieldList("ATTEND_SEMES_DAT");
        $semes_field = $db->getCol($query);

        foreach($model->field["SCHREGNO"] as $key => $schregno) {
            //更新対象データが存在するかをチェック
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ATTEND_SUBCLASS_DAT ";
            $query .= " WHERE ";
            $query .= "     COPYCD      = '0' AND ";
            $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
            $query .= "     MONTH       = '".$model->field["MONTH"]."' AND ";
            $query .= "     SCHREGNO    = '".$schregno."' AND ";
            if ($model->Properties["useCurriculumcd"] == "1") {
                $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
            } else {
                $query .= "     CLASSCD     = '".substr($model->field["SUBCLASSCD"], 0, 2)."' AND ";
                $query .= "     SUBCLASSCD  = '".$model->field["SUBCLASSCD"]."' ";
            }

            $flag = $db->getOne($query);
            $data = array();

            if ($flag) {
                $where  = " WHERE ";
                $where .= "     COPYCD      = '0' AND ";
                $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
                $where .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
                $where .= "     MONTH       = '".$model->field["MONTH"]."' AND ";
                $where .= "     SCHREGNO    = '".$schregno."' AND ";
                if ($model->Properties["useCurriculumcd"] == "1") {
                    $where .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
                } else {
                    $where .= "     CLASSCD     = '".substr($model->field["SUBCLASSCD"], 0, 2)."' AND ";
                    $where .= "     SUBCLASSCD  = '".$model->field["SUBCLASSCD"]."' ";
                }
            } else {
                //更新対象データがない時 - INSERT
                $data["COPYCD"][TEXT]     = "0";
                $data["YEAR"][TEXT]       = CTRL_YEAR;
                $data["MONTH"][TEXT]      = $model->field["MONTH"];
                $data["SEMESTER"][TEXT]   = $model->field["SEMESTER"];
                $data["SCHREGNO"][TEXT]   = $schregno;
                if ($model->Properties["useCurriculumcd"] == "1") {
                    list($classCd, $schoolKind, $curriculumCd, $subclassCD) = preg_split("/-/", $model->field["SUBCLASSCD"]);
                    $data["CLASSCD"][TEXT]       = $classCd;
                    $data["SCHOOL_KIND"][TEXT]   = $schoolKind;
                    $data["CURRICULUM_CD"][TEXT] = $curriculumCd;
                    $data["SUBCLASSCD"][TEXT]    = $subclassCD;
                } else {
                    $data["CLASSCD"][TEXT]    = substr($model->field["SUBCLASSCD"], 0, 2);
                    $data["SUBCLASSCD"][TEXT] = $model->field["SUBCLASSCD"];
                }
            }
            //更新データをセット
            $data["APPOINTED_DAY"][TEXT] = $model->field["APPOINTED_DAY"];      //締め日
            $data["LESSON"][NUMBER]      = $model->field["LESSON"][$key];       //授業日数
            foreach ($subclass_field as $subfld) {
                if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
                    $data[$subfld][NUMBER]   = $model->field[$subfld][$key];
                } else {
                    $data[$subfld][NUMBER]   = ($model->field[$subfld][$key]) ?    $model->field[$subfld][$key] : 0;
                }
            }
            $data["REGISTERCD"][TEXT]    = STAFFCD ;                                 //登録者コード
            $data["UPDATED"][FUNC]       = "sysdate()";                              //更新日付

            //ATTEND_SEMES_DATチェック
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ATTEND_SEMES_DAT ";
            $query .= " WHERE ";
            $query .= "     COPYCD      = '0' AND ";
            $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "     MONTH       = '".$model->field["MONTH"]."' AND ";
            $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
            $query .= "     SCHREGNO    = '".$schregno."' ";

            $flg2 = $db->getOne($query);
            $data2 = array();

            //ATTEND_SEMES_DATセット
            $data2["COPYCD"][TEXT]          = "0";
            $data2["YEAR"][TEXT]            = CTRL_YEAR;
            $data2["MONTH"][TEXT]           = $model->field["MONTH"];
            $data2["SEMESTER"][TEXT]        = $model->field["SEMESTER"];
            $data2["SCHREGNO"][TEXT]        = $schregno;
            $data2["APPOINTED_DAY"][TEXT]   = $model->field["APPOINTED_DAY"];
            $data2["LESSON"][NUMBER]        = 0;
            foreach ($semes_field as $semfld) {
                if ($model->Properties["use_Attend_zero_hyoji"] != "1") {
                    $data2[$semfld][NUMBER]     = 0;
                }
            }
            $data2["REGISTERCD"][TEXT]      = STAFFCD;
            $data2["UPDATED"][FUNC]         = "SYSDATE()";

            if ("" != $model->field["APPOINTED_DAY"]) {
                //更新対象データがある時 - UPDATE
                if($flag) {
                    $query = Query::updateSQL($data, "ATTEND_SUBCLASS_DAT", $where);
                }
                //更新対象データがない時 - INSERT
                else {
                    $query = Query::insertSQL($data, "ATTEND_SUBCLASS_DAT");
                }

                //ATTEND_SEMES_DAT追加
                if(!$flg2) {
                    $query2 = Query::insertSQL($data2, "ATTEND_SEMES_DAT");
                    $db->query($query2);
                }
            } else {
                if ($flag) {
                    $query = "DELETE FROM ATTEND_SUBCLASS_DAT ".$where;
                }
            }
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>
