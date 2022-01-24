<?php

require_once('for_php7.php');

require_once("PrimarySchoolProcess.php");

class knjb3022Query extends Query {

    //校種取得
    function getSchoolKind($model) {
        $query .= " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        $query .= " ORDER BY ";
        $query .= "     ABBV3, NAMECD2 ";
        $query .= "  ";

        return $query;
    }
    //年度取得
    function getYear($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR || '年度' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "   WHERE YEAR IN ('".CTRL_YEAR."', '".(CTRL_YEAR + 1)."') ";
            if ($model->selectSchoolKind) {
                $query .= "     AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                $query .= "     AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "   WHERE YEAR IN ('".CTRL_YEAR."', '".(CTRL_YEAR + 1)."') ";
            $query .= "     AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= "     AND YEAR IN ('".CTRL_YEAR."', '".(CTRL_YEAR + 1)."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }
    //年度学期
    // TODO:不要になったはず
    function getYearSemester() {
        $query .= " SELECT ";
        $query .= "     SEME.YEAR || '-' || SEME.SEMESTER AS VALUE, ";
        $query .= "     SEME.YEAR || '年度 ' || SEME.SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST SEME ";
        $query .= " WHERE ";
        $query .= "     SEME.YEAR IN ('".CTRL_YEAR."', '".(CTRL_YEAR + 1)."') ";
        $query .= "     AND SEME.SEMESTER < '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //職員
    function getStaff($model) {
        $query .= " SELECT ";
        $query .= "     STF.STAFFCD AS VALUE, ";
        $query .= "     STF.STAFFCD || ':' || STF.STAFFNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_STAFF_MST STF ";
        $query .= " WHERE ";
        $query .= "     STF.YEAR = '{$model->year}' ";
        $query .= " ORDER  BY ";
        $query .= "     STF.STAFFCD ";

        return $query;
    }

    // 科目別基本時間割(ヘッダ)
    function getPtrnSubclassHdat($model) {
        $query  = " SELECT ";
        $query .= "     SEQ, ";
        $query .= "     TITLE, ";
        $query .= "     UPDATED ";
        $query .= " FROM ";
        $query .= "     SCH_PTRN_SUBCLASS_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= " ORDER BY ";
        $query .= "     SEQ ";

        return $query;
    }

    //学年取得
    function getSelectGrade($model) {
        $query  = "SELECT DISTINCT ";
        $query .= "    GRADE AS VALUE, ";
        $query .= "    GRADE_NAME1 AS LABEL ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_GDAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '{$model->year}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "ORDER BY ";
        $query .= "    VALUE";

        return $query;
    }

    //年組名取得
    function getHrName($model, $grade = "") {
        $query .= " SELECT ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     MAX(H1.HR_NAME) AS LABEL, ";
        $query .= "     H1.GRADE || '-' || H1.HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT H1 ";
        $query .= " WHERE ";
        $query .= "     H1.YEAR = '{$model->year}' ";
        if ($grade) {
            $query .= "     AND H1.GRADE = '{$grade}' ";
        }
        $query .= " GROUP BY ";
        $query .= "     GRADE ";
        $query .= "   , HR_CLASS ";

        return $query;
    }

    // 単位マスタを参照しHRクラス毎の科目を取得
    function getCreditSubclass($model) {
        $query  = " SELECT ";
        $query .= "     C.YEAR ";
        $query .= "     , C.COURSECD ";
        $query .= "     , C.MAJORCD ";
        $query .= "     , C.GRADE ";
        $query .= "     , R.HR_CLASS ";
        $query .= "     , C.COURSECODE ";
        $query .= "     , C.CLASSCD ";
        $query .= "     , C.SCHOOL_KIND ";
        $query .= "     , C.CURRICULUM_CD ";
        $query .= "     , C.SUBCLASSCD ";
        $query .= " FROM ";
        $query .= "     CREDIT_MST C ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT R ";
        $query .= "         ON C.YEAR = R.YEAR ";
        $query .= "         AND C.COURSECD = R.COURSECD ";
        $query .= "         AND C.MAJORCD = R.MAJORCD ";
        $query .= "         AND C.GRADE = R.GRADE ";
        $query .= "         AND C.COURSECODE = R.COURSECODE ";
        $query .= " WHERE ";
        $query .= "     C.YEAR = '{$model->year}' ";

        if ($model->ajaxParam['HR_CLASS']) {
            $query .= "     AND C.GRADE || '-' || R.HR_CLASS IN (   ";
            $query .= " '".implode($model->ajaxParam['HR_CLASS'],"','")."' ";
            $query .= "       ) ";
        }
        if ($model->ajaxParam["SUBCLASSCD"]) {
            $query .= "     AND C.CLASSCD = '{$model->ajaxParam["CLASSCD"]}' ";
            $query .= "     AND C.SCHOOL_KIND = '{$model->ajaxParam["SCHOOL_KIND"]}' ";
            $query .= "     AND C.CURRICULUM_CD = '{$model->ajaxParam["CURRICULUM_CD"]}' ";
            $query .= "     AND C.SUBCLASSCD = '{$model->ajaxParam["SUBCLASSCD"]}' ";
        }

        $query .= " GROUP BY ";
        $query .= "     C.YEAR ";
        $query .= "     , C.COURSECD ";
        $query .= "     , C.MAJORCD ";
        $query .= "     , C.GRADE ";
        $query .= "     , R.HR_CLASS ";
        $query .= "     , C.COURSECODE ";
        $query .= "     , C.CLASSCD ";
        $query .= "     , C.SCHOOL_KIND ";
        $query .= "     , C.CURRICULUM_CD ";
        $query .= "     , C.SUBCLASSCD ";

        return $query;
    }

    // HRクラスの担当or副担当
    function getHrClassTr($model) {
        $query  = "";
        $query .= " SELECT DISTINCT ";
        $query .= "     HDAT.GRADE, ";
        $query .= "     HDAT.HR_CLASS ";
        $query .= " FROM SCHREG_REGD_HDAT HDAT ";

        $query .= " WHERE HDAT.YEAR = '{$model->year}' ";

        $query .= "   AND HDAT.GRADE || '-' || HDAT.HR_CLASS IN ( ";
        $query .= " '".implode($model->ajaxParam['HR_CLASS'],"','")."' ";
        $query .= "     ) ";
        
        $query .= "   AND '{$model->staffCd}' IN ( ";
        $query .= "       HDAT.TR_CD1, HDAT.TR_CD2, HDAT.TR_CD3, ";
        $query .= "       HDAT.SUBTR_CD1, HDAT.SUBTR_CD2, HDAT.SUBTR_CD3 ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     HDAT.GRADE ";
        $query .= "   , HDAT.HR_CLASS ";
        
        return $query;
    }

    // 教科
    function getClass($model) {

        $query  = "";
        if ($model->ajaxParam['HR_CLASS']) {
            $query .= " WITH CREDIT_SUBCLASS AS ( ";
            $query .= knjb3022Query::getCreditSubclass($model);
            $query .= " ) ";
        }

        $query .= " SELECT DISTINCT ";
        $query .= "     CLASS.CLASSNAME, ";
        $query .= "     CLASS.CLASSCD || '-' || CLASS.SCHOOL_KIND AS VALUE, ";
        $query .= "     CLASS.CLASSCD || '-' || CLASS.SCHOOL_KIND || ':' || CLASS.CLASSNAME AS LABEL ";
        $query .= " FROM V_CLASS_MST CLASS ";
        $query .= " LEFT JOIN STAFF_CLASS_MST STFCLS  ";
        $query .= "    ON CLASS.CLASSCD = STFCLS.CLASSCD ";
        $query .= "   AND CLASS.SCHOOL_KIND = STFCLS.SCHOOL_KIND ";
        

        $query .= " WHERE CLASS.CLASSCD <= '90' ";
        $query .= "   AND CLASS.YEAR = '{$model->year}' ";
        if ($model->staffClass) {
            $query .= " AND STFCLS.STAFFCD = '{$model->staffCd}' ";
        }
        if ($model->ajaxParam['HR_CLASS']) {
            $query .= " AND EXISTS ( ";
            $query .= "     SELECT * ";
            $query .= "     FROM CREDIT_SUBCLASS ";
            $query .= "     WHERE YEAR = '{$model->year}' ";
            $query .= "       AND CREDIT_SUBCLASS.CLASSCD = CLASS.CLASSCD  ";
            $query .= "       AND CREDIT_SUBCLASS.SCHOOL_KIND = CLASS.SCHOOL_KIND  ";
            $query .= " ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    // 教科(HR教科)
    function getClassHr($model) {
        
        $query .= " SELECT DISTINCT ";
        $query .= "     CLASS.CLASSNAME, ";
        $query .= "     CLASS.CLASSCD || '-' || CLASS.SCHOOL_KIND AS VALUE, ";
        $query .= "     CLASS.CLASSCD || '-' || CLASS.SCHOOL_KIND || ':' || CLASS.CLASSNAME AS LABEL ";
        $query .= " FROM V_CLASS_MST CLASS ";
        $query .= " WHERE CLASS.CLASSCD > '90' ";
        $query .= "   AND CLASS.YEAR = '{$model->year}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        
        return $query;
    }

    // 科目
    function getSubclass($model) {
        $query  = "";
        if ($model->ajaxParam["HR_CLASS"] && $model->ajaxParam["CLASSCD"] <= '90') {
            $query .= " WITH CREDIT_SUBCLASS AS ( ";
            $query .= knjb3022Query::getCreditSubclass($model);
            $query .= " ) ";
        }

        $query .= " SELECT DISTINCT ";
        $query .= "     SUBCLASS.SUBCLASSNAME, ";
        $query .= "     SUBCLASS.CLASSCD || '-' || SUBCLASS.SCHOOL_KIND || '-' || SUBCLASS.CURRICULUM_CD || '-' || SUBCLASS.SUBCLASSCD AS VALUE, ";
        $query .= "     SUBCLASS.CLASSCD || '-' || SUBCLASS.SCHOOL_KIND || '-' || SUBCLASS.CURRICULUM_CD || '-' || SUBCLASS.SUBCLASSCD || ':' || SUBCLASS.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST SUBCLASS ";
        $query .= " WHERE ";
        $query .= "       SUBCLASS.YEAR = '{$model->year}' ";
        $query .= "   AND SUBCLASS.CLASSCD = '{$model->ajaxParam["CLASSCD"]}' ";
        $query .= "   AND SUBCLASS.SCHOOL_KIND = '{$model->ajaxParam["SCHOOL_KIND"]}' ";

        if ($model->ajaxParam["HR_CLASS"] && $model->ajaxParam["CLASSCD"] <= '90') {
            $query .= " AND EXISTS ( ";
            $query .= "     SELECT * ";
            $query .= "     FROM CREDIT_SUBCLASS ";
            $query .= "     WHERE YEAR = '{$model->year}' ";
            $query .= "       AND CREDIT_SUBCLASS.CLASSCD = SUBCLASS.CLASSCD  ";
            $query .= "       AND CREDIT_SUBCLASS.SCHOOL_KIND = SUBCLASS.SCHOOL_KIND  ";
            $query .= "       AND CREDIT_SUBCLASS.CURRICULUM_CD = SUBCLASS.CURRICULUM_CD  ";
            $query .= "       AND CREDIT_SUBCLASS.SUBCLASSCD = SUBCLASS.SUBCLASSCD  ";

            $query .= " ) ";
        }

        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //校時
    function getPeriod($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND NAMECD1 = 'B001' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        
        return $query;
    }
    

    // 科目別基本時間割
    function getPtrnSubclassDat($model) {

        $query  = " WITH HRCLASS AS ( ";
        $query .= knjb3022Query::getHrName($model);
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEQ, ";
        $query .= "     T1.WEEK_CD, ";
        $query .= "     T1.PERIODCD, ";
        $query .= "     T1.STAFFCD, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T2.LABEL HR_NAME, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T3.SUBCLASSNAME, ";
        $query .= "     T3.SUBCLASSABBV ";

        $query .= " FROM ";
        $query .= "     SCH_PTRN_SUBCLASS_DAT T1 ";
        $query .= "     INNER JOIN HRCLASS T2 ON T1.GRADE = T2.GRADE ";
        $query .= "       AND T1.HR_CLASS = T2.HR_CLASS ";
        $query .= "     INNER JOIN SUBCLASS_MST T3 ON T1.CLASSCD = T3.CLASSCD ";
        $query .= "       AND T1.CLASSCD = T3.CLASSCD ";
        $query .= "       AND T1.SCHOOL_KIND = T3.SCHOOL_KIND ";
        $query .= "       AND T1.CURRICULUM_CD = T3.CURRICULUM_CD ";
        $query .= "       AND T1.SUBCLASSCD = T3.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "       T1.YEAR = '{$model->year}' ";
        $query .= "   AND T1.SEQ = {$model->bscSeq} ";
        $query .= "   AND T1.STAFFCD = '{$model->staffCd}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.WEEK_CD, ";
        $query .= "     T1.PERIODCD, ";
        $query .= "     T1.STAFFCD, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD ";

        return $query;
    }


    function update($db, $model){
        // データの削除
        // 変更のあったセルの曜日・校時のリスト
        for ($i = 0; $i < get_count($model->updateDelData); $i++) {
            $delVal = $model->updateDelData[$i];
            //曜日の読替
            $weekCd = $model->weekPtrn[$delVal['WEEK_CD']]['CD'];
            $periodCd = $model->periYomikae[$delVal['PERIODCD']];

            $query  = " DELETE FROM ";
            $query .= "     SCH_PTRN_SUBCLASS_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$model->year}' ";
            $query .= "     AND SEQ = {$model->bscSeq} ";
            $query .= "     AND WEEK_CD = '".$weekCd."' ";
            $query .= "     AND PERIODCD = '".$periodCd."' ";
            $query .= "     AND STAFFCD = '{$model->staffCd}' ";
            $db->query($query);
        }

        // 変更のあったセルのあったレコード
        for ($i = 0; $i < get_count($model->updateAddData); $i++) {
            $updVal = $model->updateAddData[$i];
            //曜日の読替
            $weekCd = $model->weekPtrn[$updVal['WEEK_CD']]['CD'];
            $periodCd = $model->periYomikae[$updVal['PERIODCD']];

            $data = array();
            $data["YEAR"][TEXT]          = $model->year;
            $data["SEQ"][NUMBER]         = $model->bscSeq;
            $data["WEEK_CD"][TEXT]       = $weekCd;
            $data["PERIODCD"][TEXT]      = $periodCd;
            $data["STAFFCD"][TEXT]       = $model->staffCd;
            $data["GRADE"][TEXT]         = $updVal["GRADE"];
            $data["HR_CLASS"][TEXT]      = $updVal["HR_CLASS"];
            $data["CLASSCD"][TEXT]       = $updVal["CLASSCD"];
            $data["SCHOOL_KIND"][TEXT]   = $updVal["SCHOOL_KIND"];
            $data["CURRICULUM_CD"][TEXT] = $updVal["CURRICULUM_CD"];
            $data["SUBCLASSCD"][TEXT]    = $updVal["SUBCLASSCD"];

            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][NUMBER]     = "sysdate()";

            $query = Query::insertSQL($data, "SCH_PTRN_SUBCLASS_DAT");
            $db->query($query);
        }
    }

    function updatePtrnTemplate($db, $model){
        if($model->field['PTRN_UPDATE_RADIO'] == 1){
            $data = array();
            $data["TITLE"][TEXT]        = $model->field['PTRN_UPDATE_TITLE'];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";

            $where  = " WHERE YEAR      = '{$model->year}'";
            $where .= "   AND SEQ       = {$this->bscSeq}";

            $query = Query::updateSQL($data, "SCH_PTRN_SUBCLASS_HDAT", $where);
            $db->query($query);
            $bscSeq = $this->bscSeq;
        }
        if($model->field['PTRN_UPDATE_RADIO'] == 2){
            $query .= " SELECT ";
            $query .= "     MAX(SEQ) ";
            $query .= " FROM ";
            $query .= "     SCH_PTRN_SUBCLASS_HDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$model->year}' ";

            $bscSeq = $db->getOne($query);
            if(!isset($bscSeq)){
                $bscSeq = 1;
            } else {
                $bscSeq ++;
            }

            $data = array();
            $data["YEAR"][TEXT]         = $model->year;
            $data["SEQ"][NUMBER]        = $bscSeq;
            $data["TITLE"][TEXT]        = $model->field['PTRN_UPDATE_TITLE'];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";

            $query = Query::insertSQL($data, "SCH_PTRN_SUBCLASS_HDAT");
            $db->query($query);
        }
        return $bscSeq;
    }

}
?>
