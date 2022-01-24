<?php

require_once('for_php7.php');

class knjd425l_2Query extends Query {


    //タイトル取得
    function getHreportGuidanceKindNameHdat($model) {
        $query  = " SELECT ";
        $query .= "     KIND_NAME ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_KIND_NAME_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND KIND_NO = '{$model->selKindNo}' ";

        return $query;
    }

    //項目タイトル取得
    function getHreportGuidanceKindNamedat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_KIND_NAME_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND KIND_NO = '{$model->selKindNo}' ";

        return $query;
    }

    //データ取得
    function getHreportGuidanceSchregRemark($model, $div, $seq) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_REMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER = '9' ";
        $query .= "     AND RECORD_DATE = '{$model->recordDate}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        if ($div != "") {
            $query .= "     AND DIV = '{$div}' ";
        }
        if ($seq != "") {
            $query .= "     AND SEQ = {$seq} ";
        }
        $query .= " ORDER BY ";
        $query .= "     DIV ";
        $query .= "   , SEQ ";

        return $query;
    }

    /************/
    /* 更新処理 */
    /************/
    //UPDATE -- HREPORT_GUIDANCE_SCHREG_REMARK_DAT
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        // $db->autoCommit(false);

        $semester = '9';

        foreach ($model->remarkTitle as $seq => $remarkTitle) {
            $data = array();
            $data["REMARK"][TEXT]           = $model->field["REMARK_{$seq}"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";
            //存在チェック
            $query = knjd425l_2Query::getHreportGuidanceSchregRemark($model, $model->selKindNo, $seq);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if ($row) {
                //更新
                $where  = " WHERE ";
                $where .= "     YEAR         = '{$model->exp_year}' ";
                $where .= " AND SEMESTER     = '{$semester}' ";
                $where .= " AND RECORD_DATE  = '{$model->recordDate}' ";
                $where .= " AND SCHREGNO     = '{$model->schregno}' ";
                $where .= " AND DIV          = '{$model->selKindNo}' ";
                $where .= " AND SEQ          = {$seq} ";

                $query = Query::updateSQL($data, "HREPORT_GUIDANCE_SCHREG_REMARK_DAT", $where);
                $db->query($query);
            } else {
                //登録
                $data["YEAR"][TEXT]             = $model->exp_year;
                $data["SEMESTER"][TEXT]         = $semester;
                $data["RECORD_DATE"][DATE]      = $model->recordDate;
                $data["SCHREGNO"][TEXT]         = $model->schregno;
                $data["DIV"][TEXT]              = $model->selKindNo;
                $data["SEQ"][NUMBER]            = $seq;

                $query = Query::insertSQL($data, "HREPORT_GUIDANCE_SCHREG_REMARK_DAT");
                $db->query($query);
            }

        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>
