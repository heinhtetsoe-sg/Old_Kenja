<?php

require_once('for_php7.php');

class knja233fQuery extends Query {

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

        $query .= "     INNER JOIN CHAIR_DAT T5 ON T5.YEAR = T1.YEAR AND T5.CHAIRCD = T1.CHAIRCD ";     //講座データ
        $query .= "           AND T5.SEMESTER = '{$model->field["SEMESTER"]}', ";     //講座データ
        $query .= "     (SELECT DISTINCT ";
        $query .= "         TT3.CHAIRCD,TT4.STAFFCD,TT4.STAFFNAME_SHOW,TT3.CHARGEDIV ";  //2004/06/30 nakamoto
        $query .= "      FROM   ";
        $query .= "         CHAIR_STF_DAT TT3,   ";  //講座担当職員データ
        $query .= "         STAFF_MST TT4 ";   //職員マスタ
        $query .= "     WHERE ";
        $query .= "         TT3.YEAR = '{$model->year}' AND ";
        $query .= "         TT3.SEMESTER   = '{$model->field["SEMESTER"]}' AND ";
        //制限付き参照・更新可は自分が受け持つクラスのみ
        if ($model->auth == DEF_REFER_RESTRICT || $model->auth == DEF_UPDATE_RESTRICT){
            $query .= "     TT3.STAFFCD  = '". STAFFCD ."' AND ";
        }elseif ($model->auth == DEF_NOAUTH){
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

        $query .= "     CLASS_MST T3, ";  //教科マスタ
        $query .= "     SUBCLASS_MST T4, ";    //科目マスタ
        $query .= "     SCHREG_REGD_HDAT T7 "; //学籍在籍ヘッダデータ
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

    //学校名取得
    function getSchoolName() {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //高セキュリティー取得
    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJA233A' AND ";
        $query .= "     INVALID_FLG = '0' ";

        return $query;
    }

/************************************/
/********* 以下CSV関係 **************/
/************************************/

    //講座名称取得
    function getChairName($chair, $model){
        $query  = "SELECT ";
        $query .= "    CHAIRNAME ";
        $query .= "FROM ";
        $query .= "    CHAIR_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR     = '".$model->year."' AND ";
        $query .= "    SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "    CHAIRCD  = '".$chair."' ";

        return $query;
    }

    //職員名取得
    function getStaff($staff){
        $query  = "SELECT ";
        $query .= "    STAFFNAME ";
        $query .= "FROM ";
        $query .= "    STAFF_MST ";
        $query .= "WHERE ";
        $query .= "    STAFFCD = '".$staff."' ";

        return $query;
    }

    //CSVデータ取得
    function Csvdata_get($opt, $chair, $app, $model) {
        $query  = " SELECT ";
        $query .= "     value(T2.HR_CLASS,'') AS HR_CLASS, ";
        $query .= "     value(T2.ATTENDNO,'') AS ATTENDNO, ";
        $query .= "     CASE WHEN T1.SEX = '2' THEN '*' ELSE '' END AS SEX, ";
        $query .= "     value(T1.NAME,'') AS NAME, ";
        $query .= "     value(T1.NAME_KANA,'') AS NAME_KANA ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T7 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T1 ";
        $query .= "        ON T1.SCHREGNO = T7.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T2 ";
        $query .= "        ON T2.SCHREGNO = T7.SCHREGNO ";
        $query .= "       AND T2.YEAR     = T7.YEAR";
        $query .= "       AND T2.SEMESTER = T7.SEMESTER ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ";
        $query .= "       ON GDAT.YEAR  = T2.YEAR ";
        $query .= "      AND GDAT.GRADE = T2.GRADE ";
        $query .= "     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT ENTGRD ";
        $query .= "       ON ENTGRD.SCHREGNO    = T7.SCHREGNO ";
        $query .= "      AND ENTGRD.SCHOOL_KIND = GDAT.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "     T7.YEAR     = '".$model->year."' ";
        $query .= "     AND T7.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND T7.CHAIRCD  = '".$chair."' ";
        $query .= "     AND T7.APPDATE  = '".$app."' ";
        if($model->field["GRD_DIV"] == "1"){
            $query .= " AND VALUE(ENTGRD.GRD_DIV, '') NOT IN ('2', '3') ";
        }
        $query .= " ORDER BY ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO ";

        return $query;
    }
}
?>
