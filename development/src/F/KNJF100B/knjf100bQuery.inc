<?php

require_once('for_php7.php');

class knjf100bQuery extends Query {
    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }

    //学校名称2取得
    function getSchoolName2($model) {
        $query  = " SELECT ";
        $query .= "     SCHOOLNAME2 ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SCHOOLCD = '".sprintf("%012d", SCHOOLCD)."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     AND SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } else {
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //学期取得
    function getSemester($model) {
        $query  = "SELECT ";
        $query .= "    SEMESTERNAME AS LABEL, ";
        $query .= "    SEMESTER AS VALUE ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '{$model->year}' AND ";
        $query .= "    SEMESTER <> '9' ";
        $query .= "ORDER BY ";
        $query .= "    SEMESTER ";

        return $query;
    }

    //教科取得
    function getClassMst($model) {
        $query  = " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     L1.CLASSCD || '-' || L1.SCHOOL_KIND || ':' || L1.CLASSNAME AS LABEL, ";
            $query .= "     L1.CLASSCD || '-' || L1.SCHOOL_KIND AS VALUE ";
        } else {
            $query .= "     L1.CLASSCD || ':' || L1.CLASSNAME AS LABEL, ";
            $query .= "     L1.CLASSCD AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= " LEFT JOIN ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASS_MST L1 ON L1.CLASSCD = SUBSTR(T1.SUBCLASSCD, 1, 2) ";
            $query .= "                 AND L1.SCHOOL_KIND  = T1.SCHOOL_KIND ";
        } else {
            $query .= "     CLASS_MST L1 ON L1.CLASSCD = SUBSTR(SUBCLASSCD, 1, 2) ";
        }
        $query .= " INNER JOIN ";
        $query .= "     SUBCLASS_MST L2 ON L2.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                 AND L2.CLASSCD      = T1.CLASSCD ";
            $query .= "                 AND L2.SCHOOL_KIND  = T1.SCHOOL_KIND ";
            $query .= "                 AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' AND ";
        $query .= "     T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "    AND T1.CHAIRCD IN (SELECT ";
            $query .= "                          S1.CHAIRCD ";
            $query .= "                      FROM ";
            $query .= "                          CHAIR_STF_DAT S1 ";
            $query .= "                      WHERE ";
            $query .= "                          S1.YEAR = T1.YEAR ";
            $query .= "                      AND S1.SEMESTER = T1.SEMESTER ";
            $query .= "                      AND S1.CHAIRCD  = T1.CHAIRCD ";
            $query .= "                      AND S1.STAFFCD = '".STAFFCD."' ";
            $query .= "                     ) ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T1.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目取得
    function getSubclassMst($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $class_array = array();
            $class_array = explode("-", $model->field["CLASSCD"]);
        }
        $query  = " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || L1.SUBCLASSCD || ':' || L1.SUBCLASSNAME AS LABEL, ";
            $query .= "     L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || L1.SUBCLASSCD AS VALUE ";
        } else {
            $query .= "     L1.SUBCLASSCD || ':' || L1.SUBCLASSNAME AS LABEL, ";
            $query .= "     L1.SUBCLASSCD AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= " INNER JOIN ";
        $query .= "     SUBCLASS_MST L1 ON L1.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                 AND L1.CLASSCD      = T1.CLASSCD ";
            $query .= "                 AND L1.SCHOOL_KIND  = T1.SCHOOL_KIND ";
            $query .= "                 AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' AND ";
        $query .= "     T1.SEMESTER = '{$model->field["SEMESTER"]}' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD          = '".$class_array[0]."' AND ";
            $query .= "     T1.SCHOOL_KIND      = '".$class_array[1]."' ";
        } else {
            $query .= "     SUBSTR(T1.SUBCLASSCD, 1, 2) = '{$model->field["CLASSCD"]}' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "    AND T1.CHAIRCD IN (SELECT ";
            $query .= "                          S1.CHAIRCD ";
            $query .= "                      FROM ";
            $query .= "                          CHAIR_STF_DAT S1 ";
            $query .= "                      WHERE ";
            $query .= "                          S1.YEAR = T1.YEAR ";
            $query .= "                      AND S1.SEMESTER = T1.SEMESTER ";
            $query .= "                      AND S1.CHAIRCD  = T1.CHAIRCD ";
            $query .= "                      AND S1.STAFFCD = '".STAFFCD."' ";
            $query .= "                     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //講座
    function getChairDat($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T5.CHAIRCD AS VALUE, ";
        $query .= "     T5.CHAIRCD || ':' || T5.CHAIRNAME AS LABEL ";
        $query .= " FROM ";

        $query .= "     (SELECT ";
        $query .= "         YEAR, ";
        $query .= "         CHAIRCD, ";
        $query .= "         APPDATE, ";
        $query .= "         MAX(APPENDDATE) AS APPENDDATE ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR     = '{$model->year}' AND ";
        $query .= "         SEMESTER = '{$model->field["SEMESTER"]}' AND ";
        $query .= "         '". str_replace("/", "-", $model->field["DATE"]) ."' BETWEEN APPDATE AND APPENDDATE  ";
        $query .= "     GROUP BY ";
        $query .= "         YEAR, ";
        $query .= "         CHAIRCD, ";
        $query .= "         APPDATE ";
        $query .= "     ) T1 ";

        $query .= "     INNER JOIN CHAIR_DAT T5 ON T5.YEAR = T1.YEAR AND T5.CHAIRCD = T1.CHAIRCD ";     //講座データ
        $query .= "           AND T5.SEMESTER = '{$model->field["SEMESTER"]}' ";     //講座データ
        $query .= "     INNER JOIN (SELECT DISTINCT ";
        $query .= "         TT3.CHAIRCD, TT4.STAFFCD, TT4.STAFFNAME_SHOW, TT3.CHARGEDIV ";
        $query .= "      FROM   ";
        $query .= "         CHAIR_STF_DAT TT3,   ";  //講座担当職員データ
        $query .= "         STAFF_MST TT4 ";   //職員マスタ
        $query .= "     WHERE ";
        $query .= "         TT3.YEAR = '{$model->year}' AND ";
        $query .= "         TT3.SEMESTER   = '{$model->field["SEMESTER"]}' AND ";
        //制限付き参照・更新可は自分が受け持つクラスのみ
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     TT3.STAFFCD  = '". STAFFCD ."' AND ";
        } else if (AUTHORITY == DEF_NOAUTH) {
            $query .= "     TT3.STAFFCD    IS NULL AND";
        }
        $query .= "         TT3.STAFFCD    = TT4.STAFFCD  ";
        $query .= "    ) T6 ON T6.CHAIRCD    = T1.CHAIRCD ";

        $query .= " WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T5.SCHOOL_KIND   = '".substr($model->field["SUBCLASSCD"], 3, 1)."' AND ";
            $query .= "    T5.CURRICULUM_CD = '".substr($model->field["SUBCLASSCD"], 5, 1)."' AND ";
            $query .= "    T5.SUBCLASSCD    = '".substr($model->field["SUBCLASSCD"], 7, 6)."' ";
        } else {
            $query .= "    T5.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T5.CHAIRCD ";

        return $query;
    }


    //講座名簿
    function getChairStdDat($model) {
        $query = "";
        $query .= " WITH SCH_INFO AS( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T3.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1  ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT T3 ON ";
        $query .= "         T1.YEAR     = T3.YEAR AND ";
        $query .= "         T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "         T1.GRADE    = T3.GRADE AND ";
        $query .= "         T1.HR_CLASS = T3.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "     T2.GRADE || T2.HR_CLASS || T2.ATTENDNO || '-' || T2.SCHREGNO AS VALUE, ";
        $query .= "     T2.HR_NAME || ' ' || T2.ATTENDNO || '番 ' || T2.NAME_SHOW AS LABEL, ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1, ";
        $query .= "     SCH_INFO T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND T1.CHAIRCD = '".$model->field["CHAIRCD"]."' ";
        $query .= "     AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     AND '".str_replace("/", "-", $model->field["DATE"])."' BETWEEN T1.APPDATE AND T1.APPENDDATE ";
        $query .= " ORDER BY ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO ";
        return $query;
    }

    //テーブル存在チェック
    function checkTableExist() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSTABLES ";
        $query .= " WHERE ";
        $query .= "     NAME = 'SETTING_DAT' ";

        return $query;
    }

    //生徒項目名取得
    function getSchName($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     REMARK1, ";
        $query .= "     SCHOOLCD ";
        $query .= " FROM ";
        $query .= "     SETTING_DAT ";
        $query .= " WHERE ";
        $query .= "     SEQ = '001' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else {
            list ($classcd, $school_kind) = preg_split("/-/", $model->field["CLASSCD"]);
            $query .= " AND SCHOOL_KIND = '".$school_kind."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SCHOOLCD ";

        return $query;
    }
}
?>
