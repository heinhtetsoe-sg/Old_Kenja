<?php

require_once('for_php7.php');

class knjd418Query extends Query {
    //年度一覧取得
    function getYear() {
        $query  = " WITH THIS_YEAR(LABEL, VALUE) AS (VALUES('".CTRL_YEAR."', '".CTRL_YEAR."')) ";
        $query .= " SELECT ";
        $query .= "     LABEL, ";
        $query .= "     VALUE ";
        $query .= " FROM ";
        $query .= "     THIS_YEAR ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_ITEM_NAME_DAT ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //学期取得
    function getSemester($model) {
        $query  = "  SELECT ";
        $query .= "      SEMESTER AS VALUE, ";
        $query .= "      SEMESTERNAME AS LABEL ";
        $query .= "  FROM ";
        $query .= "      SEMESTER_MST ";
        $query .= "  WHERE ";
        $query .= "      YEAR = '".$model->year."' AND ";
        $query .= "      SEMESTER <> '9' ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    //学部取得
    function getSchoolKind() {
        $query  = " SELECT ";
        $query .= "     NAME1 || ':' || ABBV1 AS LABEL, ";
        $query .= "     NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //リスト取得（左フレーム）
    function getList($model) {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     L1.NAMECD2 || ':' || L1.NAME1 AS CONDITION_SHOW, ";
        $query .= "     L2.NAMECD2 || ':' || L2.NAME1 AS GUIDANCE_PATTERN_SHOW ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_ITEM_NAME_DAT T1 ";
        $query .= "     LEFT JOIN V_NAME_MST L1 ON L1.YEAR      = T1.YEAR ";
        $query .= "                            AND L1.NAMECD1   = 'A033' ";
        $query .= "                            AND L1.NAMECD2   = T1.CONDITION ";
        $query .= "     LEFT JOIN V_NAME_MST L2 ON L2.YEAR      = T1.YEAR ";
        $query .= "                            AND L2.NAMECD1   = 'A035' ";
        $query .= "                            AND L2.NAMECD2   = T1.GUIDANCE_PATTERN ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".$model->year."' AND ";
        $query .= "     T1.SEMESTER = '".$model->semester."' AND ";
        $query .= "     T1.GAKUBU_SCHOOL_KIND = '".$model->gakubu_school_kind."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.CONDITION, ";
        $query .= "     T1.GUIDANCE_PATTERN ";

        return $query;
    }

    //状態区分取得
    function getCondition($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->year."' AND ";
        $query .= "     NAMECD1 = 'A033' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //帳票パターン取得
    function getGuidancePattern($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->year."' AND ";
        $query .= "     NAMECD1 = 'A035' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //HREPORT_GUIDANCE_ITEM_NAME_DAT取得
    function getHreportGuidanceItemNameDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_ITEM_NAME_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->year."' AND ";
        $query .= "     SEMESTER    = '".$model->semester."' AND ";
        $query .= "     GAKUBU_SCHOOL_KIND  = '".$model->gakubu_school_kind."' AND ";
        $query .= "     CONDITION   = '".$model->condition."' AND ";
        $query .= "     GUIDANCE_PATTERN    = '".$model->guidance_pattern."' ";

        return $query;
    }

    //更新処理
    function &getInsertQuery($model) {
        //削除
        knjd418Query::getDeleteQuery($model);

        //更新
        $db = Query::dbCheckOut();
        $data = array();
        $data["YEAR"][TEXT]                 = $model->year;
        $data["SEMESTER"][TEXT]             = $model->semester;
        $data["GAKUBU_SCHOOL_KIND"][TEXT]   = $model->gakubu_school_kind;
        $data["CONDITION"][TEXT]            = $model->condition;
        $data["GUIDANCE_PATTERN"][TEXT]     = $model->guidance_pattern;
        //項目
        foreach ($model->pattern[$model->guidance_pattern] as $key => $val) {
            $data["ITEM_REMARK".$key][TEXT] = $model->field["ITEM_REMARK".$key];
        }
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "HREPORT_GUIDANCE_ITEM_NAME_DAT");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //削除
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     HREPORT_GUIDANCE_ITEM_NAME_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->year."' AND ";
        $query .= "     SEMESTER    = '".$model->semester."' AND ";
        $query .= "     GAKUBU_SCHOOL_KIND  = '".$model->gakubu_school_kind."' AND ";
        $query .= "     CONDITION   = '".$model->condition."' AND ";
        $query .= "     GUIDANCE_PATTERN    = '".$model->guidance_pattern."' ";

        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //MAX学期取得
    function getMaxSemester() {
        $query  = " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     MAX(SEMESTER) AS MAX_SEMESTER ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " GROUP BY ";
        $query .= "      YEAR ";

        return $query;
    }

    //存在チェック（コピー） -- HREPORT_GUIDANCE_ITEM_NAME_DAT
    function checkHreportGuidanceItemNameDat($year, $semester) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_ITEM_NAME_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$year."' AND ";
        $query .= "     SEMESTER = '".$semester."' ";

        return $query;
    }

    //コピー用データ取得
    function copyHreportGuidanceItemNameDat($model, $pre_year, $pre_semester) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_ITEM_NAME_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$pre_year."' AND ";
        $query .= "     SEMESTER    = '".$pre_semester."' ";

        return $query;
    }

    //コピー
    function &copyInsertQuery($model, $Row) {
        $data = array();
        $data["YEAR"][TEXT]                 = $model->year;
        $data["SEMESTER"][TEXT]             = $model->semester;
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
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "HREPORT_GUIDANCE_ITEM_NAME_DAT");
        return $query;
    }
}
?>
