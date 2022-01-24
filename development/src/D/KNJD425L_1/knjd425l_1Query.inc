<?php

require_once('for_php7.php');

class knjd425l_1Query extends Query {

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

    //データ取得
    function getHreportGuidanceSchregSelfreliance($model, $selfTarget, $selfDiv, $selfSeq) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_SELFRELIANCE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        if ($selfTarget != "") {
            $query .= "     AND SELF_TARGET = '{$selfTarget}' ";
        }
        if ($selfDiv != "") {
            $query .= "     AND SELF_DIV = '{$selfDiv}' ";
        }
        if ($selfSeq != "") {
            $query .= "     AND SELF_SEQ = '{$selfSeq}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SELF_TARGET ";
        $query .= "   , SELF_DIV ";
        $query .= "   , SELF_SEQ ";

        return $query;
    }

    /************/
    /* 更新処理 */
    /************/
    //UPDATE -- HREPORT_GUIDANCE_SCHREG_REMARK_DAT
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $semester = '9';

        foreach ($model->remarkTextLimit as $seq => $textLimit) {
            $data = array();
            $data["REMARK"][TEXT]           = $model->field["REMARK_{$seq}"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";
            //存在チェック
            $query = knjd425l_1Query::getHreportGuidanceSchregRemark($model, $model->selKindNo, $seq);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if ($row) {
                //更新
                $where  = " WHERE ";
                $where .= "     YEAR         = '{$model->exp_year}' ";
                $where .= " AND SEMESTER     = '{$semester}' ";
                $where .= " AND RECORD_DATE  = '{$model->recordDate}' ";
                $where .= " AND SCHREGNO     = '{$model->schregno}' ";
                $where .= " AND DIV          = '01' ";
                $where .= " AND SEQ          = {$seq} ";

                $query = Query::updateSQL($data, "HREPORT_GUIDANCE_SCHREG_REMARK_DAT", $where);
                $db->query($query);
            } else {
                //登録
                $data["YEAR"][TEXT]             = $model->exp_year;
                $data["SEMESTER"][TEXT]         = $semester;
                $data["RECORD_DATE"][DATE]      = $model->recordDate;
                $data["SCHREGNO"][TEXT]         = $model->schregno;
                $data["DIV"][TEXT]              = "01";
                $data["SEQ"][NUMBER]            = $seq;

                $query = Query::insertSQL($data, "HREPORT_GUIDANCE_SCHREG_REMARK_DAT");
                $db->query($query);
            }

        }

        //自立項目削除
        $query  = " DELETE FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_SELFRELIANCE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR         = '{$model->exp_year}' ";
        $query .= " AND SCHREGNO     = '{$model->schregno}' ";
        $query .= " AND SELF_TARGET = '00' ";
        $db->query($query);

        //自立項目追加
        foreach ($model->selfrelianceList as $selfDiv => $selfreliance) {
            for ($i=0; $i < get_count($selfreliance); $i++) {
                $selfSeq = $selfreliance[$i];
                //チェックしたもののみ追加
                if ($model->field["SELF_".$selfDiv."_".$selfSeq] == "1") {
                    //登録
                    $data = array();
                    $data["YEAR"][TEXT]             = $model->exp_year;
                    $data["SCHREGNO"][TEXT]         = $model->schregno;
                    $data["SELF_TARGET"][TEXT]      = "00";
                    $data["SELF_DIV"][TEXT]         = $selfDiv;
                    $data["SELF_SEQ"][TEXT]         = $selfSeq;

                    $data["REGISTERCD"][TEXT]       = STAFFCD;
                    $data["UPDATED"][NUMBER]        = "sysdate()";

                    $query = Query::insertSQL($data, "HREPORT_GUIDANCE_SCHREG_SELFRELIANCE_DAT");
                    $db->query($query);
                }
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>
