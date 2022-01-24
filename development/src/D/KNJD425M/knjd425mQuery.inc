<?php

require_once('for_php7.php');

class knjd425mQuery extends Query {

    //校種取得
    function getSchoolKind() {
        $query  = " SELECT DISTINCT ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= "     , NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期取得
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //科目取得
    function getSubclass($model) {
        $query  = " SELECT ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE ";
        $query .= "     , SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";

        return $query;
    }

    //講座一覧取得
    function getChairCd($model) {
        $query   = " SELECT ";
        $query  .= "    CHAIRCD || ':' || CHAIRNAME AS LABEL, ";
        $query  .= "    CHAIRCD AS VALUE ";
        $query  .= " FROM ";
        $query  .= "     CHAIR_DAT ";
        $query  .= " WHERE ";
        $query  .= "     YEAR = '".CTRL_YEAR."' ";
        $query  .= "     AND SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query  .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        $query  .= " ORDER BY ";
        $query  .= "      VALUE ";

        return $query;
    }

    //講座一覧取得
    function getItemName() {
        $query  = " SELECT ";
        $query .= "     KIND_REMARK ";
        $query .= "     , LTRIM(KIND_SEQ, '0') AS KIND_SEQ ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_KIND_NAME_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND KIND_NO = '03' ";
        $query .= "     AND KIND_SEQ IN ('001', '002', '003') ";

        return $query;
    }

    //指定講座の生徒一覧取得
    function getChairSchreg($model) {
        $query   = " WITH CHAIR_STD AS ( ";
        $query  .= "     SELECT ";
        $query  .= "         YEAR ";
        $query  .= "         , SEMESTER ";
        $query  .= "         , CHAIRCD ";
        $query  .= "         , SCHREGNO ";
        $query  .= "     FROM ";
        $query  .= "         CHAIR_STD_DAT ";
        $query  .= "     GROUP BY ";
        $query  .= "         YEAR ";
        $query  .= "         , SEMESTER ";
        $query  .= "         , CHAIRCD ";
        $query  .= "         , SCHREGNO ";
        $query  .= " ) ";
        $query  .= " SELECT ";
        $query  .= "     CHSTD.SCHREGNO ";
        $query  .= "     , BASE.NAME ";
        if (is_array($model->itemNameArr)) {
            foreach ($model->itemNameArr as $itemNo => $item_remark){
                $query .= "     , GUIDANCE_SEQ{$itemNo}.REMARK AS REMARK_{$itemNo} ";
            }
        }
        $query  .= " FROM ";
        $query  .= "     CHAIR_STD CHSTD ";
        $query  .= "     LEFT JOIN SCHREG_BASE_MST BASE ";
        $query  .= "         ON BASE.SCHREGNO = CHSTD.SCHREGNO ";
        if (is_array($model->itemNameArr)) {
            foreach ($model->itemNameArr as $itemNo => $item_remark){
                $query  .= "     LEFT JOIN HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT GUIDANCE_SEQ{$itemNo} ";
                $query  .= "         ON GUIDANCE_SEQ{$itemNo}.YEAR = CHSTD.YEAR ";
                $query  .= "         AND GUIDANCE_SEQ{$itemNo}.SEMESTER = CHSTD.SEMESTER ";
                $query  .= "         AND GUIDANCE_SEQ{$itemNo}.SCHREGNO = CHSTD.SCHREGNO ";
                $query  .= "         AND GUIDANCE_SEQ{$itemNo}.CLASSCD || '-' || GUIDANCE_SEQ{$itemNo}.SCHOOL_KIND || '-' || GUIDANCE_SEQ{$itemNo}.CURRICULUM_CD || '-' || GUIDANCE_SEQ{$itemNo}.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
                $query  .= "         AND GUIDANCE_SEQ{$itemNo}.UNITCD = '00' ";
                $query  .= "         AND GUIDANCE_SEQ{$itemNo}.GUIDANCE_PATTERN = '2' ";
                $query  .= "         AND GUIDANCE_SEQ{$itemNo}.SEQ = '{$itemNo}' ";
            }
        }
        $query  .= "     WHERE ";
        $query  .= "         CHSTD.YEAR = '".CTRL_YEAR."' ";
        $query  .= "         AND CHSTD.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query  .= "         AND CHSTD.CHAIRCD = '{$model->field["CHAIRCD"]}' ";
        $query  .= "     ORDER BY ";
        $query  .= "         SCHREGNO ";

        return $query;
    }

    /************/
    /* 更新処理 */
    /************/
    //INSERT -- HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        list($classCd, $schoolKind, $curriculumCd, $subclassCd) = preg_split("/-/", $model->field["SUBCLASSCD"]);
        foreach ($model->schField as $schregno => $val) {
            foreach ($model->itemNameArr as $itemNo => $item_remark) {
                //存在チェック
                $query = knjd425mQuery::getHreportGuidanceSchregSubclassDatCnt($model, $schregno, $itemNo);
                $check = $db->getOne($query);
                $data = array();
                $data["REMARK"][TEXT]           = $val["REMARK_{$itemNo}"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "sysdate()";
                if ($check == 0) {
                    $data["YEAR"][TEXT]             = CTRL_YEAR;
                    $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
                    $data["SCHREGNO"][TEXT]         = $schregno;
                    $data["CLASSCD"][TEXT]          = $classCd;
                    $data["SCHOOL_KIND"][TEXT]      = $schoolKind;
                    $data["CURRICULUM_CD"][TEXT]    = $curriculumCd;
                    $data["SUBCLASSCD"][TEXT]       = $subclassCd;
                    $data["UNITCD"][TEXT]           = '00';
                    $data["GUIDANCE_PATTERN"][TEXT] = '2';
                    $data["SEQ"][NUMBER]            = $itemNo;
                    //追加
                    $query = Query::insertSQL($data, "HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT");
                    $db->query($query);
                } else {
                    $where  = " WHERE ";
                    $where .= "         YEAR             = '".CTRL_YEAR."' ";
                    $where .= "     AND SEMESTER         = '{$model->field["SEMESTER"]}'";
                    $where .= "     AND SCHREGNO         = '{$schregno}' ";
                    $where .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
                    $where .= "     AND UNITCD           = '00' ";
                    $where .= "     AND GUIDANCE_PATTERN = '2' ";
                    $where .= "     AND SEQ              = {$itemNo} ";
                    //更新
                    $query = Query::updateSQL($data, "HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT", $where);
                    $db->query($query);
                }
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //存在チェック -- HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT
    function getHreportGuidanceSchregSubclassDatCnt($model, $schregno, $seq) {
        $query  = " SELECT ";
        $query .= "     COUNT ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND SCHREGNO = '{$schregno}' ";
        $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        $query .= "     AND SEQ = '{$seq}' ";

        return $query;
    }
}
?>
