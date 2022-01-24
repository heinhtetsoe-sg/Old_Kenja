<?php

require_once('for_php7.php');

class knjd616gQuery extends Query {

    //学期取得
    function getSemester($model, $seme = "") {
        $query  = "SELECT ";
        $query .= "    SEMESTERNAME AS LABEL, ";
        $query .= "    SEMESTER AS VALUE, ";
        $query .= "    SDATE, EDATE ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '{$model->year}' AND ";
        if ($seme == "9") {
            $query .= "    SEMESTER = '9' ";
        } else {
            $query .= "    SEMESTER <> '9' ";
        }
        $query .= "ORDER BY ";
        $query .= "    SEMESTER ";

        return $query;
    }

    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "   NAME1 || ':' || ABBV1 AS LABEL, ";
        $query .= "   NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "   NAME_MST ";
        $query .= " WHERE ";
        $query .= "   NAMECD1 = 'A023' ";
        $query .= "   AND NAME1 = 'H' ";
        $query .= " ORDER BY ";
        $query .= "   VALUE ";
        return $query;
    }


    //科目取得
    function getSubclassMst($model, $selectClass = "") {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $class_array = array();
            $class_array = explode("-", $model->field["CLASSCD"]);
        }
        $query  = " SELECT DISTINCT ";
        if ($selectClass) {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     L1.CLASSCD || '-' || L1.SCHOOL_KIND || ':' || L2.CLASSNAME AS LABEL, ";
                $query .= "     L1.CLASSCD || '-' || L1.SCHOOL_KIND AS VALUE ";
            } else {
                $query .= "     L1.CLASSCD || ':' || L2.CLASSNAME AS LABEL, ";
                $query .= "     L1.CLASSCD AS VALUE ";
            }
        } else {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || L1.SUBCLASSCD || ':' || L1.SUBCLASSNAME AS LABEL, ";
                $query .= "     L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || L1.SUBCLASSCD AS VALUE ";
            } else {
                $query .= "     L1.SUBCLASSCD || ':' || L1.SUBCLASSNAME AS LABEL, ";
                $query .= "     L1.SUBCLASSCD AS VALUE ";
            }
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
        $query .= " INNER JOIN CLASS_MST L2 ON L2.CLASSCD = SUBSTR(T1.SUBCLASSCD, 1, 2) ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                 AND L2.SCHOOL_KIND  = T1.SCHOOL_KIND ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' AND ";
        $query .= "     T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "    AND T1.SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
        if ($selectClass == '') {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     AND T1.CLASSCD          = '".$class_array[0]."' ";
                $query .= "     AND T1.SCHOOL_KIND      = '".$class_array[1]."' ";
            } else {
                $query .= "     AND SUBSTR(T1.SUBCLASSCD, 1, 2) = '{$model->field["CLASSCD"]}' ";
            }
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
        $query .= "     CHAIR_DAT T5 ";     //講座データ
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
        $query .= "    ) T6 ON T6.CHAIRCD    = T5.CHAIRCD ";

        $query .= " WHERE ";
        $query .= "        T5.YEAR     = '{$model->year}' AND ";
        $query .= "        T5.SEMESTER = '{$model->field["SEMESTER"]}' AND ";
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

}
?>
