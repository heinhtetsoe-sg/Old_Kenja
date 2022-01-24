<?php

require_once('for_php7.php');

class knjd417mQuery extends Query {

    //名称マスタ取得
    function getNameMst($nameCd1, $nameCd2) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= "   , NAMECD2 AS VALUE ";
        $query .= "   , NAME1 ";
        $query .= "   , NAMESPARE1 ";
        $query .= "   , NAMESPARE3 ";
        $query .= "   , ABBV3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '".$nameCd1."' ";
        if ($nameCd2) {
            $query .= "     AND NAMECD2 = '".$nameCd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE(NAMECD2, '99') ";
        return $query;
    }

    //構成種類
    function getCompositionTypeList() {
        $query  = " SELECT ";
        $query .= "     VD090.NAMECD2 || ':' || VD090.NAME1 AS LABEL ";
        $query .= "   , VD090.NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "   HREPORT_GUIDANCE_KIND_DAT T1 ";
        $query .= "   LEFT JOIN V_NAME_MST VD090 ";
        $query .= "        ON VD090.YEAR = T1.YEAR ";
        $query .= "       AND VD090.NAMECD1 = 'D090' ";
        $query .= "       AND VD090.NAMECD2 = T1.KIND_NO ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND T1.GRADE || T1.HR_CLASS = '00000' ";
        $query .= "   AND T1.SCHREGNO = '00000000' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SHOWORDER ";
        return $query;
    }

    //項目名称取得(名称M取得)
    function getCompositionTypeSmplList($ncd1, $cutcode = false, $ncd2 = "") {
        $query  = " SELECT ";
        if ($cutcode) {
            $query .= "     NAME1 AS LABEL ";
            $query .= "   , NAMECD2 AS VALUE ";
        } else {
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
            $query .= "   , NAMECD2 || '-' || VALUE(NAMESPARE1, '') || '-' || VALUE(NAMESPARE3, '') AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '".$ncd1."' ";
        if ($ncd2 != "") {
            $query .= "     AND NAMECD2 = '".$ncd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE(NAMECD2, '99') ";

        return $query;
    }

    //ヘッダ取得
    function getHDKindNameHDat($kindNo) {
        $query  = " SELECT ";
        $query .= "     HDAT.YEAR ";
        $query .= "   , HDAT.KIND_NO ";
        $query .= "   , HDAT.KIND_NAME ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_KIND_NAME_HDAT HDAT ";
        $query .= " WHERE ";
        $query .= "         HDAT.YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND HDAT.KIND_NO = '".$kindNo."' ";

        return $query;
    }

    //項目取得
    function getHDKindNameDat($model, $cd) {
        $query  = " SELECT ";
        $query .= "     HDAT.YEAR ";
        $query .= "   , HDAT.KIND_NO ";
        $query .= "   , HDAT.KIND_NAME ";
        $query .= "   , KIND.KIND_SEQ ";
        $query .= "   , KIND.KIND_REMARK ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_KIND_NAME_HDAT HDAT ";
        $query .= "     LEFT JOIN HREPORT_GUIDANCE_KIND_NAME_DAT KIND ";
        $query .= "         ON KIND.YEAR     = HDAT.YEAR ";
        $query .= "         AND KIND.KIND_NO = HDAT.KIND_NO ";
        $query .= " WHERE ";
        $query .= "         HDAT.YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND HDAT.KIND_NO = '".$cd."' ";
        $query .= " ORDER BY ";
        $query .= "     KIND.KIND_SEQ ";

        return $query;
    }

    //学期取得
    function getSemester($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE ";
        $query .= "   , SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR      = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学部取得
    function getSchoolKind() {
        $query  = " SELECT ";
        $query .= "     NAME1 || ':' || ABBV1 AS LABEL ";
        $query .= "   , NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //状態区分取得
    function getCondition($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= "   , NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A033' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //帳票パターン取得
    function getGuidancePattern($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= "   , NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A035' ";
        $query .= "     AND NAMESPARE1 IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //パターン毎の項目取得
    function getPattern($nameCd2) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 ";
        $query .= "   , NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 in ( ";
        $query .= "                     SELECT ";
        $query .= "                         NAMESPARE1 ";
        $query .= "                     FROM ";
        $query .= "                         V_NAME_MST ";
        $query .= "                     WHERE ";
        $query .= "                             YEAR    = '".CTRL_YEAR."' ";
        $query .= "                         AND NAMECD1 = 'A035' ";
        $query .= "                         AND NAMECD2 = '{$nameCd2}' ";
        $query .= "                     ) ";

        return $query;
    }

    //HREPORT_GUIDANCE_ITEM_NAME_DAT取得
    function getHreportGuidanceItemNameDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_ITEM_NAME_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR               = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER           = '".$model->semester."' ";
        $query .= "     AND GAKUBU_SCHOOL_KIND = '".$model->gakubu_school_kind."' ";
        $query .= "     AND CONDITION          = '".$model->condition."' ";
        $query .= "     AND GUIDANCE_PATTERN   = '".$model->guidance_pattern."' ";

        return $query;
    }

    function getGradeKindCompGroupYmst($model) {
        $query  = " SELECT ";
        $query .= "   COUNT(*) ";
        $query .= " FROM ";
        $query .= "   GRADE_KIND_COMP_GROUP_YMST ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND GAKUBU_SCHOOL_KIND = '".$model->gakubu_school_kind."' ";
        $query .= "   AND CONDITION = '".$model->condition."' ";
        return $query;
    }

    function getDefaultGuidancePattern($model) {
        $query  = " SELECT DISTINCT GUIDANCE_PATTERN FROM GRADE_KIND_COMP_GROUP_YMST ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND GAKUBU_SCHOOL_KIND = '".$model->gakubu_school_kind."' ";
        $query .= "   AND CONDITION = '".$model->condition."' ";
        $query .= "   AND GUIDANCE_PATTERN IS NOT NULL ";

        return $query;
    }

    //更新
    function &getInsertQuery($model) {
        //削除
        knjd417mQuery::getDeleteQuery($model);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //更新
        //HREPORT_GUIDANCE_KIND_NAME_HDAT
        $data = array();
        $data["YEAR"][TEXT]         = CTRL_YEAR;
        $data["KIND_NO"][TEXT]      = $model->KindNo;
        $data["KIND_NAME"][TEXT]    = $model->kindName;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        $query = Query::insertSQL($data, "HREPORT_GUIDANCE_KIND_NAME_HDAT");
        $db->query($query);

        //HREPORT_GUIDANCE_KIND_NAME_DAT
        if ($model->kindCnt > 0) {
            for ($i = 1; $i <= $model->kindCnt; $i++) {
                $data = array();
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                $data["KIND_NO"][TEXT]      = $model->KindNo;
                $data["KIND_SEQ"][TEXT]     = sprintf("%03d", $i);
                $data["KIND_REMARK"][TEXT]  = $model->field["KIND_REMARK_".$i];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";
                $query = Query::insertSQL($data, "HREPORT_GUIDANCE_KIND_NAME_DAT");
                $db->query($query);
            }
        }

        // HREPORT_GUIDANCE_ITEM_NAME_DAT
        if ($model->kindType == '3') {
            $query  = " DELETE FROM ";
            $query .= "     HREPORT_GUIDANCE_ITEM_NAME_DAT ";
            $query .= " WHERE ";
            $query .= "         YEAR               = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER           = '".$model->semester."' ";
            $query .= "     AND GAKUBU_SCHOOL_KIND = '".$model->gakubu_school_kind."' ";
            $query .= "     AND CONDITION          = '".$model->condition."' ";
            $query .= "     AND GUIDANCE_PATTERN   = '".$model->guidance_pattern."' ";
            $db->query($query);

            //更新
            $data = array();
            $data["YEAR"][TEXT]                 = CTRL_YEAR;
            $data["SEMESTER"][TEXT]             = $model->semester;
            $data["GAKUBU_SCHOOL_KIND"][TEXT]   = $model->gakubu_school_kind;
            $data["CONDITION"][TEXT]            = $model->condition;
            $data["GUIDANCE_PATTERN"][TEXT]     = $model->guidance_pattern;
            //項目
            foreach ($model->pattern as $key => $val) {
                $data["ITEM_REMARK".$key][TEXT] = $model->field["ITEM_REMARK".$key];
            }
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $query = Query::insertSQL($data, "HREPORT_GUIDANCE_ITEM_NAME_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //削除
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        // HREPORT_GUIDANCE_KIND_NAME_DAT
        $query  = " DELETE FROM ";
        $query .= "     HREPORT_GUIDANCE_KIND_NAME_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' ";
        $query .= " AND KIND_NO = '".$model->KindNo."' ";

        $db->query($query);

        // HREPORT_GUIDANCE_KIND_NAME_HDAT
        $query  = " DELETE FROM ";
        $query .= "     HREPORT_GUIDANCE_KIND_NAME_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' ";
        $query .= " AND KIND_NO = '".$model->KindNo."' ";

        $db->query($query);

        // HREPORT_GUIDANCE_ITEM_NAME_DAT
        if ($model->kindType == '3') {
            $query  = " DELETE FROM ";
            $query .= "     HREPORT_GUIDANCE_ITEM_NAME_DAT ";
            $query .= " WHERE ";
            $query .= "         YEAR               = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER           = '".$model->semester."' ";
            $query .= "     AND GAKUBU_SCHOOL_KIND = '".$model->gakubu_school_kind."' ";
            $query .= "     AND CONDITION          = '".$model->condition."' ";
            $query .= "     AND GUIDANCE_PATTERN   = '".$model->guidance_pattern."' ";
            $db->query($query);
        }

        Query::dbCheckIn($db);
        return;
    }

    //データの存在チェック（コピー用）
    function chkCopyData($year, $flg, $table) {
        $query  = " SELECT ";
        if ($flg == "check") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     {$table} ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";

        return $query;
    }

    //コピー
    function &getCopyQuery($Row, $table) {
        $data = array();
        $data["YEAR"][TEXT]         = CTRL_YEAR;
        $data["KIND_NO"][TEXT]      = $Row["KIND_NO"];
        if ($table == "HREPORT_GUIDANCE_KIND_NAME_HDAT") {
            $data["KIND_NAME"][TEXT]    = $Row["KIND_NAME"];
        } else {
            $data["KIND_SEQ"][TEXT]     = $Row["KIND_SEQ"];
            $data["KIND_REMARK"][TEXT]  = $Row["KIND_REMARK"];
        }
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, $table);

        return $query;
    }

    //コピー
    function getDeleteItemNameQuery($table) {

        $query  = " DELETE FROM ";
        $query .= "     {$table} ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //コピー
    function getCopyItemNameQuery($Row, $table) {

        //更新
        $data = array();
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["SEMESTER"][TEXT]             = $Row["SEMESTER"];
        $data["GAKUBU_SCHOOL_KIND"][TEXT]   = $Row["GAKUBU_SCHOOL_KIND"];
        $data["CONDITION"][TEXT]            = $Row["CONDITION"];
        $data["GUIDANCE_PATTERN"][TEXT]     = $Row["GUIDANCE_PATTERN"];
        $data["ITEM_REMARK1"][TEXT]         = $Row["ITEM_REMARK1"];
        $data["ITEM_REMARK2"][TEXT]         = $Row["ITEM_REMARK2"];
        $data["ITEM_REMARK3"][TEXT]         = $Row["ITEM_REMARK3"];
        $data["ITEM_REMARK4"][TEXT]         = $Row["ITEM_REMARK4"];
        $data["ITEM_REMARK5"][TEXT]         = $Row["ITEM_REMARK5"];
        $data["ITEM_REMARK6"][TEXT]         = $Row["ITEM_REMARK6"];
        $data["ITEM_REMARK7"][TEXT]         = $Row["ITEM_REMARK7"];
        $data["ITEM_REMARK8"][TEXT]         = $Row["ITEM_REMARK8"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, $table);

        return $query;
    }
}
?>
