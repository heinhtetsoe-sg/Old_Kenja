<?php

require_once('for_php7.php');

class knjd420lQuery extends Query {

    //学期取得
    function getSemester($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER as VALUE, ";
        $query .= "     SEMESTERNAME as LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //履修教科・科目グループ取得
    function getGradeKindSchregGroupDat($model) {
        $query  = " SELECT ";
        $query .= "     T1.YEAR ";
        $query .= "   , T1.SEMESTER ";
        $query .= "   , T1.GAKUBU_SCHOOL_KIND ";
        $query .= "   , T1.GHR_CD ";
        $query .= "   , T1.GRADE ";
        $query .= "   , T1.HR_CLASS ";
        $query .= "   , T1.CONDITION ";
        $query .= "   , T3.NAME1 CONDITION_NAME ";
        $query .= "   , T1.GROUPCD ";
        $query .= "   , T2.GROUPNAME ";
        $query .= "   , T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_SCHREG_GROUP_DAT T1 ";
        $query .= " LEFT JOIN GRADE_KIND_COMP_GROUP_YMST T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "     AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "     AND T2.GAKUBU_SCHOOL_KIND = T1.GAKUBU_SCHOOL_KIND ";
        $query .= "     AND T2.GHR_CD = T1.GHR_CD ";
        $query .= "     AND T2.GRADE = T1.GRADE ";
        $query .= "     AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= "     AND T2.CONDITION = T1.CONDITION ";
        $query .= "     AND T2.GROUPCD = T1.GROUPCD ";
        $query .= " LEFT JOIN V_NAME_MST T3 ";
        $query .= "     ON T3.YEAR = T1.YEAR ";
        $query .= "     AND T3.NAMECD1 = 'A033' ";
        $query .= "     AND T3.NAMECD2 = T1.CONDITION ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->exp_year."' ";
        $query .= "     AND T1.SEMESTER = '9' ";
        $query .= "     AND T1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //科目取得
    function getSubclass($model) {
        $query  = " SELECT ";
        $query .= "     L3.CLASSCD || '-' || L3.SCHOOL_KIND || '-' || L3.CURRICULUM_CD || '-' || L3.SUBCLASSCD AS VALUE, ";
        $query .= "     L3.CLASSCD || '-' || L3.SCHOOL_KIND || '-' || L3.CURRICULUM_CD || '-' || L3.SUBCLASSCD || ':' || L1.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_SCHREG_GROUP_DAT T1 ";
        $query .= "     INNER JOIN GRADE_KIND_COMP_GROUP_DAT L3 ";
        $query .= "         ON T1.YEAR = L3.YEAR ";
        $query .= "        AND T1.SEMESTER    = L3.SEMESTER ";
        $query .= "        AND T1.GAKUBU_SCHOOL_KIND = L3.GAKUBU_SCHOOL_KIND ";
        $query .= "        AND T1.GHR_CD      = L3.GHR_CD ";
        $query .= "        AND T1.GRADE       = L3.GRADE ";
        $query .= "        AND T1.HR_CLASS    = L3.HR_CLASS ";
        $query .= "        AND T1.CONDITION   = L3.CONDITION ";
        $query .= "        AND T1.GROUPCD     = L3.GROUPCD ";
        $query .= "     INNER JOIN SUBCLASS_MST L1 ";
        $query .= "         ON L3.CLASSCD = L1.CLASSCD ";
        $query .= "        AND L3.SCHOOL_KIND = L1.SCHOOL_KIND ";
        $query .= "        AND L3.CURRICULUM_CD = L1.CURRICULUM_CD ";
        $query .= "        AND L3.SUBCLASSCD  = L1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '".$model->exp_year."' ";
        $query .= "     AND T1.SEMESTER = '9' ";
        $query .= "     AND T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //単元取得
    function getUnit($model, $subclassCd, $unitCd) {
        $query  = " SELECT ";
        $query .= "     T1.UNITCD AS VALUE ";
        $query .= "   , T1.UNITNAME AS LABEL ";
        $query .= "   , T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD SUBCLASSCD ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_UNIT_GROUP_YMST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".$model->exp_year."' ";
        $query .= " AND T1.SEMESTER     = '9' ";
        $query .= " AND T1.GAKUBU_SCHOOL_KIND = '".$model->schregInfo["GAKUBU_SCHOOL_KIND"]."' ";
        $query .= " AND T1.GHR_CD       = '".$model->schregInfo["GHR_CD"]."' ";
        $query .= " AND T1.GRADE        = '".$model->schregInfo["GRADE"]."' ";
        $query .= " AND T1.HR_CLASS     = '".$model->schregInfo["HR_CLASS"]."' ";
        $query .= " AND T1.CONDITION    = '".$model->schregInfo["CONDITION"]."' ";
        $query .= " AND T1.GROUPCD      = '".$model->schregInfo["GROUPCD"]."' ";
        if ($subclassCd != "") {
            $query .= " AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '{$subclassCd}' ";
        }
        if ($unitCd != "") {
            $query .= " AND T1.UNITCD = '{$unitCd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //枠数取得
    function getHreportConditionDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORT_CONDITION_DAT ";
        $query .= " WHERE YEAR        = '".$model->exp_year."' ";
        $query .= "   AND SCHOOL_KIND = '".$model->schregInfo["GAKUBU_SCHOOL_KIND"]."' ";
        $query .= "   AND SEQ         = '102' ";

        return $query;
    }

    //項目名取得
    function getGuidanceItemName($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_ITEM_NAME_DAT ";
        $query .= " WHERE YEAR                = '".$model->exp_year."' ";
        $query .= "   AND SEMESTER            = '9' ";
        $query .= "   AND GAKUBU_SCHOOL_KIND  = '".$model->schregInfo["GAKUBU_SCHOOL_KIND"]."' ";
        $query .= "   AND CONDITION           = '".$model->schregInfo["CONDITION"]."' ";
        $query .= "   AND GUIDANCE_PATTERN    = '3' ";

        return $query;
    }

   //履歴情報の表示
    function getList($model) {
        $query  = " SELECT ";
        $query .= "     T1.YEAR ";
        $query .= "   , T1.SEMESTER ";
        $query .= "   , L2.SEMESTERNAME ";
        $query .= "   , T1.SCHREGNO ";
        $query .= "   , T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD SUBCLASSCD ";
        $query .= "   , T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ':' || L1.SUBCLASSNAME SUBCLASSNAME ";
        $query .= "   , T1.UNITCD ";
        $query .= "   , T1.SEQ ";
        $query .= "   , T1.REMARK ";
        $query .= " FROM ";
        $query .= "     HREPORT_SCHREG_SUBCLASS_REMARK_DAT T1 ";
        $query .= "     LEFT JOIN SUBCLASS_MST L1 ";
        $query .= "         ON L1.CLASSCD = T1.CLASSCD ";
        $query .= "        AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "        AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "        AND L1.SUBCLASSCD  = T1.SUBCLASSCD ";

        $query .= "     LEFT JOIN SEMESTER_MST L2 ";
        $query .= "         ON L2.YEAR = T1.YEAR ";
        $query .= "        AND L2.SEMESTER = T1.SEMESTER ";

        $query .= " WHERE T1.YEAR        = '{$model->exp_year}' ";
        $query .= "   AND T1.SCHREGNO    = '{$model->schregno}' ";

        $query .= " ORDER BY ";
        $query .= "     YEAR ";
        $query .= "   , SEMESTER ";
        $query .= "   , SCHREGNO ";
        $query .= "   , SUBCLASSCD ";

        return $query;
    }

    //１レコード取得 -- HREPORT_TOKUSHI_SCHREG_SUBCLASS_DAT
    function getHreportTokushiSchregSubclassDat($model) {

        $query  = " SELECT ";
        $query .= "     T1.YEAR ";
        $query .= "   , T1.SEMESTER ";
        $query .= "   , T1.SCHREGNO ";
        $query .= "   , T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD SUBCLASSCD ";
        $query .= "   , T1.SEQ ";
        $query .= "   , T1.REMARK ";
        $query .= " FROM ";
        $query .= "     HREPORT_SCHREG_SUBCLASS_REMARK_DAT T1 ";
        $query .= " WHERE T1.YEAR        = '{$model->exp_year}' ";
        $query .= "   AND T1.SEMESTER    = '{$model->semester}' ";
        $query .= "   AND T1.SCHREGNO    = '{$model->schregno}' ";
        $query .= "   AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '{$model->subclasscd}' ";
        $query .= "   AND T1.UNITCD = '{$model->unitcd}' ";

        return $query;
    }

    /************/
    /* 更新処理 */
    /************/
    //COPY -- HREPORT_SCHREG_SUBCLASS_REMARK_DAT
    function &getCopyQuery($model) {

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        // 削除してからコピーする
        $query  = " DELETE FROM ";
        $query .= "     HREPORT_SCHREG_SUBCLASS_REMARK_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER    = '{$model->semester}' ";
        $query .= "     AND SCHREGNO    = '{$model->schregno}' ";
        // 指定科目でコピー
        if ($model->cmd == "copy") {
            $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->subclasscd}' ";
        }
        $db->query($query);

        $query  = " INSERT INTO HREPORT_SCHREG_SUBCLASS_REMARK_DAT ( ";
        $query .= "       YEAR ";
        $query .= "     , SEMESTER ";
        $query .= "     , SCHREGNO ";
        $query .= "     , CLASSCD ";
        $query .= "     , SCHOOL_KIND ";
        $query .= "     , CURRICULUM_CD ";
        $query .= "     , SUBCLASSCD ";
        $query .= "     , UNITCD ";
        $query .= "     , SEQ ";
        $query .= "     , REMARK ";
        $query .= "     , REGISTERCD ";
        $query .= "     , UPDATED ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "       YEAR ";
        $query .= "     , SEMESTER ";
        $query .= "     , SCHREGNO ";
        $query .= "     , CLASSCD ";
        $query .= "     , SCHOOL_KIND ";
        $query .= "     , CURRICULUM_CD ";
        $query .= "     , SUBCLASSCD ";
        $query .= "     , UNITCD ";
        $query .= "     , SEQ ";
        $query .= "     , REMARK ";
        $query .= "     , '".STAFFCD."' ";
        $query .= "     , sysdate() ";
        $query .= " FROM HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER    = '{$model->semester}' ";
        $query .= "     AND SCHREGNO    = '{$model->schregno}' ";
        $query .= "     AND GUIDANCE_PATTERN = '3' ";

        // 指定科目でコピー
        if ($model->cmd == "copy") {
            $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->subclasscd}' ";
        }
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //INSERT -- HREPORT_SCHREG_SUBCLASS_REMARK_DAT
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query  = " DELETE FROM ";
        $query .= "     HREPORT_SCHREG_SUBCLASS_REMARK_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER    = '{$model->semester}' ";
        $query .= "     AND SCHREGNO    = '{$model->schregno}' ";
        $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->subclasscd}' ";
        $query .= "     AND UNITCD      = '{$model->unitcd}' ";
        $db->query($query);

        list($classCd, $schoolKind, $curriculumCd, $subclassCd) = preg_split("/-/", $model->subclasscd);
        foreach($model->itemNameArr as $key => $value) {
            $data = array();
            $data["REMARK"][TEXT]           = $model->field["REMARK_{$key}"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $data["YEAR"][TEXT]             = $model->exp_year;
            $data["SEMESTER"][TEXT]         = $model->semester;
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["CLASSCD"][TEXT]          = $classCd;
            $data["SCHOOL_KIND"][TEXT]      = $schoolKind;
            $data["CURRICULUM_CD"][TEXT]    = $curriculumCd;
            $data["SUBCLASSCD"][TEXT]       = $subclassCd;
            $data["UNITCD"][TEXT]           = $model->unitcd;
            $data["SEQ"][NUMBER]            = $key;
            //追加
            $query = Query::insertSQL($data, "HREPORT_SCHREG_SUBCLASS_REMARK_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //DELETE -- HREPORT_SCHREG_SUBCLASS_REMARK_DAT
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     HREPORT_SCHREG_SUBCLASS_REMARK_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER    = '{$model->semester}' ";
        $query .= "     AND SCHREGNO    = '{$model->schregno}' ";
        $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->subclasscd}' ";
        $query .= "     AND UNITCD      = '{$model->unitcd}' ";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //DELETE -- HREPORT_SCHREG_SUBCLASS_REMARK_DAT
    function &getListDeleteQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);


        $delList = array();
        if ($model->deleteList) {
            $delList = explode(",", $model->deleteList);
        }

        for ($i=0; $i < get_count($delList); $i++) { 
            list($semester, $classCd, $schoolKind, $curriculumCd, $subclassCd, $unitCd) = preg_split("/-/", $delList[$i]);

            $query  = " DELETE FROM ";
            $query .= "     HREPORT_SCHREG_SUBCLASS_REMARK_DAT ";
            $query .= " WHERE ";
            $query .= "         YEAR          = '{$model->exp_year}' ";
            $query .= "     AND SEMESTER      = '{$semester}' ";
            $query .= "     AND SCHREGNO      = '{$model->schregno}' ";
            $query .= "     AND CLASSCD       = '{$classCd}' ";
            $query .= "     AND SCHOOL_KIND   = '{$schoolKind}' ";
            $query .= "     AND CURRICULUM_CD = '{$curriculumCd}' ";
            $query .= "     AND SUBCLASSCD    = '{$subclassCd}' ";
            $query .= "     AND UNITCD        = '{$unitCd}' ";

            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>
