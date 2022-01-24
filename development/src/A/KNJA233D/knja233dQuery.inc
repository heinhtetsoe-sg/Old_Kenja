<?php
class knja233dQuery extends Query {

    //講座
    function getChairDat($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T3.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T5.SCHOOL_KIND, ";
            $query .= "         T5.CURRICULUM_CD, ";
        }
        $query .= "     T5.SUBCLASSCD, ";
        $query .= "     T5.CHAIRCD ATTENDCLASSCD, ";
        $query .= "     T3.CLASSNAME, ";
        $query .= "     T4.SUBCLASSNAME, ";
        $query .= "     (CASE WHEN T5.GROUPCD='0000' THEN (T7.HR_NAME) ELSE (T7.HR_NAME || '*') END )TARGETCLASS, ";
        $query .= "     T5.GROUPCD, ";
        $query .= "     T5.CHAIRNAME GROUPNAME, ";
        $query .= "     T6.STAFFNAME_SHOW, ";
        $query .= "     T6.STAFFCD, ";
        $query .= "     T7.GRADE || T7.HR_CLASS AS GRADE_HR_CLASS, ";
        $query .= "     T6.CHARGEDIV, ";
        $query .= "     T1.APPDATE, ";
        $query .= "     T1.APPENDDATE, ";
        $query .= "     T5.CHAIRNAME AS CLASSALIAS ";
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

        $query .= "     INNER JOIN CHAIR_DAT T5 ON T5.YEAR = T1.YEAR AND T5.CHAIRCD = T1.CHAIRCD ";
        $query .= "           AND T5.SEMESTER = '{$model->field["SEMESTER"]}', ";
        $query .= "     (SELECT DISTINCT ";
        $query .= "         TT3.CHAIRCD,TT4.STAFFCD,TT4.STAFFNAME_SHOW,TT3.CHARGEDIV ";
        $query .= "      FROM   ";
        $query .= "         CHAIR_STF_DAT TT3,   ";
        $query .= "         STAFF_MST TT4 ";
        $query .= "     WHERE ";
        $query .= "         TT3.YEAR = '{$model->year}' AND ";
        $query .= "         TT3.SEMESTER   = '{$model->field["SEMESTER"]}' AND ";
        //制限付き参照・更新可は自分が受け持つクラスのみ
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= "     TT3.STAFFCD  = '". STAFFCD ."' AND ";
        }elseif (AUTHORITY == DEF_NOAUTH){
            $query .= "     TT3.STAFFCD    IS NULL AND";
        }
        $query .= "         TT3.STAFFCD    = TT4.STAFFCD  ";
        $query .= "    ) T6, ";

        $query .= "     (SELECT ";
        $query .= "         WK1.chaircd, ";
        $query .= "         SUBSTR(MIN(WK1.trgtgrade || WK1.trgtclass),1,2) AS trgtgrade, ";
        $query .= "         SUBSTR(MIN(WK1.trgtgrade || WK1.trgtclass),3,3) AS trgtclass ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             k1.year, k1.semester, k2.chaircd, k1.trgtgrade, k1.trgtclass ";
        $query .= "         FROM ";
        $query .= "             chair_cls_dat k1, ";
        $query .= "             chair_dat k2 ";
        $query .= "         WHERE ";
        $query .= "             k1.year       = k2.year AND ";
        $query .= "             k1.semester   = k2.semester AND ";
        $query .= "             k1.chaircd    = '0000000' AND ";
        $query .= "             k1.year       = '{$model->year}' AND ";
        $query .= "             k1.semester   = '{$model->field["SEMESTER"]}' AND ";
        $query .= "             k1.groupcd    = k2.groupcd ";
        $query .= "         UNION   ";
        $query .= "         SELECT ";
        $query .= "             t1.year, t1.semester, t2.chaircd, t1.trgtgrade, t1.trgtclass ";
        $query .= "         FROM ";
        $query .= "             chair_cls_dat t1, ";
        $query .= "             chair_dat t2 ";
        $query .= "         WHERE ";
        $query .= "             t1.year       = t2.year AND ";
        $query .= "             t1.semester   = t2.semester AND ";
        $query .= "             t1.groupcd    = t2.groupcd AND ";
        $query .= "             t1.year       = '{$model->year}' AND ";
        $query .= "             t1.semester   = '{$model->field["SEMESTER"]}' AND ";
        $query .= "             t1.chaircd    = t2.chaircd ";
        $query .= "         ) WK1 ";
        $query .= "     GROUP BY ";
        $query .= "         WK1.chaircd ";
        $query .= "     ) T2, ";

        $query .= "     CLASS_MST T3, ";
        $query .= "     SUBCLASS_MST T4, ";
        $query .= "     SCHREG_REGD_HDAT T7 ";
        $query .= " WHERE ";
        $query .= "     T2.CHAIRCD    = T1.CHAIRCD AND ";
        $query .= "     T6.CHAIRCD    = T1.CHAIRCD AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T5.SCHOOL_KIND   = '".substr($model->field["SUBCLASSCD"], 3, 1)."' AND ";
            $query .= "    T5.CURRICULUM_CD = '".substr($model->field["SUBCLASSCD"], 5, 1)."' AND ";
            $query .= "    T5.SUBCLASSCD    = '".substr($model->field["SUBCLASSCD"], 7, 6)."' AND ";
            $query .= "    T3.CLASSCD    = T4.CLASSCD  AND ";
            $query .= "    T3.SCHOOL_KIND = T4.SCHOOL_KIND  AND ";
            $query .= "    T4.SCHOOL_KIND = T5.SCHOOL_KIND  AND ";
            $query .= "    T4.CURRICULUM_CD = T5.CURRICULUM_CD  AND ";
        } else {
            $query .= "    T5.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' AND ";
        }
        $query .= "     T3.CLASSCD    = SUBSTR(T5.SUBCLASSCD,1,2) AND ";
        $query .= "     T4.SUBCLASSCD = T5.SUBCLASSCD AND ";
        $query .= "     T7.YEAR       = '{$model->year}' AND ";
        $query .= "     T7.SEMESTER   = '{$model->field["SEMESTER"]}' AND ";
        $query .= "     T2.TRGTGRADE  = T7.GRADE AND ";
        $query .= "     T2.TRGTCLASS  = T7.HR_CLASS ";
        $query .= " ORDER BY ";
        $query .= "     ATTENDCLASSCD, ";
        $query .= "     T6.CHARGEDIV, ";
        $query .= "     T6.STAFFCD ";

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
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        //更新可能(制限付)
        if (AUTHORITY != DEF_UPDATABLE) {
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
        //更新可能(制限付)
        if (AUTHORITY != DEF_UPDATABLE) {
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

    function getSchoolCd() {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }
}
?>
