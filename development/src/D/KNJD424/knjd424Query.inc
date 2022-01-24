<?php

require_once('for_php7.php');

class knjd424Query extends Query {

    //１レコード取得 -- HREPORTREMARK_DAT
    function getRow($model) {
        $query  = " SELECT ";
        $query .= "     T1.SEQ, ";
        $query .= "     T1.REMARK ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_SEMESTER_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR  = '".$model->exp_year."' ";
        $query .= " AND T1.SEMESTER = '9' ";
        $query .= " AND T1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //データチェック
    function getGuidanceSemesCount($model, $seq="")
    {
        //テーブル取得
        $table = "";
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_SEMESTER_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$model->exp_year."' ";
        $query .= " AND SEMESTER = '9' ";
        $query .= " AND SCHREGNO = '".$model->schregno."' ";
        $query .= " AND SEQ = {$seq} ";

        return $query;
    }


    //UPDATE処理
    function &getUpdateQuery($fields, $model) {
        $db = Query::dbCheckOut();

        $seqs = array("1", "2");
        foreach ($seqs as $seq) {
            //データチェック
            $dataCheck1 = $db->getOne(knjd424Query::getGuidanceSemesCount($model, $seq));
            //UPDATE -- HTRAINREMARK_DAT
            $data = array();
            $data["SEQ"][NUMBER]          = $seq;
            $data["REMARK"][TEXT]         = $fields["REMARK".$seq];
            $data["REGISTERCD"][TEXT]     = STAFFCD;
            $data["UPDATED"][FUNC]        = "sysdate()";
            if ($dataCheck1 == 0) {
                $data["YEAR"][TEXT]       = $model->exp_year;
                $data["SEMESTER"][TEXT]   = "9";
                $data["SCHREGNO"][TEXT]   = $model->schregno;
                
                $db->query(Query::insertSQL($data, "HREPORT_GUIDANCE_SCHREG_SEMESTER_DAT"));
            } else {
                $where  = " WHERE ";
                $where .= "     YEAR     = '".$model->exp_year."' ";
                $where .= " AND SEMESTER = '9' ";
                $where .= " AND SCHREGNO = '".$model->schregno."' ";
                $where .= " AND SEQ = ".$seq." ";

                $db->query(Query::updateSQL($data, "HREPORT_GUIDANCE_SCHREG_SEMESTER_DAT", $where));
            }
        }
        
        Query::dbCheckIn($db);
        return;
    }
}
?>
