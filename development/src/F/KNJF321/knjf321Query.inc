<?php

require_once('for_php7.php');

class knjf321Query extends Query {

    //年度取得
   function getYear()
   {
        $query  = " SELECT ";
        $query .= "     YEAR AS VALUE,";
        $query .= "     YEAR AS LABEL";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //リスト取得
    function getList($model)
    {
        $query  = " SELECT ";
        $query .= "     S1.STAFFNAME, ";
        $query .= "     T1.WORK_DATE, ";
        $query .= "     T1.STAFFCD, ";
        $query .= "     '".$model->year."' AS YEAR ";
        $query .= " FROM ";
        $query .= "     MEDICAL_WORK_RECORD_DAT T1 ";
        $query .= "     LEFT JOIN STAFF_MST S1 ON S1.STAFFCD = T1.STAFFCD ";
        $query .= " WHERE  ";
        $query .= "     FISCALYEAR(T1.WORK_DATE) = '".$model->year."' ";
        $query .= " AND T1.WORK_DIV = '2' ";
        $query .= " ORDER BY  ";
        $query .= "     T1.WORK_DATE ";

        return $query;
    }
    
    //１レコード取得
    function getRow($staffcd, $date)
    {
        $query  = " SELECT ";
        $query .= "     T1.WORK_DATE, ";
        $query .= "     T1.STAFFCD, ";
        $query .= "     L901.REMARK1 AS REMARK1_901, ";
        $query .= "     L901.REMARK2 AS REMARK2_901, ";
        $query .= "     L901.REMARK3 AS REMARK3_901, ";
        for ($i = 0; $i <= 9; $i++) {
            $setSeq = sprintf("%03d", $i);
            $query .= "     L$i.REMARK1 AS REMARK1_".$setSeq.", ";
            $query .= "     L$i.REMARK2 AS REMARK2_".$setSeq.", ";
            $query .= "     L$i.REMARK3 AS REMARK3_".$setSeq.", ";
            $query .= "     L$i.REMARK4 AS REMARK4_".$setSeq.", ";
            $query .= "     L$i.REMARK5 AS REMARK5_".$setSeq.", ";
            $query .= "     L$i.REMARK6 AS REMARK6_".$setSeq.", ";
            $query .= "     L$i.REMARK7 AS REMARK7_".$setSeq.", ";
        }
        $query .= "     T1.NEWS_STORY, ";
        $query .= "     T1.SPECIAL_REPORT ";
        $query .= " FROM ";
        $query .= "     MEDICAL_WORK_RECORD_DAT T1";
        $query .= "     LEFT JOIN MEDICAL_WORK_RECORD_DETAIL_DAT L901 ON T1.WORK_DATE = L901.WORK_DATE AND T1.STAFFCD = L901.STAFFCD AND T1.WORK_DIV = L901.WORK_DIV AND L901.SEQ = '901' ";
        for ($i = 0; $i <= 9; $i++) {
            $setSeq = sprintf("%03d", $i);
            $query .= "     LEFT JOIN MEDICAL_WORK_RECORD_DETAIL_DAT L$i ON T1.WORK_DATE = L$i.WORK_DATE AND T1.STAFFCD = L$i.STAFFCD AND T1.WORK_DIV = L$i.WORK_DIV AND L$i.SEQ = '".$setSeq."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.WORK_DATE = '".$date."' ";
        $query .= " AND T1.STAFFCD   = '".$staffcd."' ";
        $query .= " AND T1.WORK_DIV = '2' ";

        return $query;
    }

    //名称取得
    function getStaffMst($model)
    {
        $query  = " SELECT ";
        $query .= "     STAFFCD AS VALUE, ";
        $query .= "     STAFFCD || ' ' || STAFFNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($model)
    {
        
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        
        //MEDICAL_WORK_RECORD_DAT
        $data = array();
        $data["WORK_DATE"][TEXT]    = str_replace("/", "-", $model->field["WORK_DATE"]);
        $data["STAFFCD"][TEXT]      = $model->field["STAFFCD"];
        $data["WORK_DIV"][TEXT]     = '2';
        $data["STAFFCD"][TEXT]      = $model->field["STAFFCD"];
        $data["NEWS_STORY"][TEXT]   = $model->field["NEWS_STORY"];
        $data["SPECIAL_REPORT"][TEXT] = $model->field["SPECIAL_REPORT"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "MEDICAL_WORK_RECORD_DAT");
        $db->query($query);

        //MEDICAL_WORK_RECORD_DETAIL_DAT
        //時間帯
        $data = array();
        $data["WORK_DATE"][TEXT]  = str_replace("/", "-", $model->field["WORK_DATE"]);
        $data["STAFFCD"][TEXT]    = $model->field["STAFFCD"];
        $data["WORK_DIV"][TEXT]   = 2;
        $data["SEQ"][TEXT]        = 901;
        $data["REMARK1"][TEXT]    = $model->field["REMARK1_901"];
        $data["REMARK2"][TEXT]    = $model->field["REMARK2_901"];
        $data["REMARK3"][TEXT]    = $model->field["REMARK3_901"];
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC]    = "sysdate()";

        $query = Query::insertSQL($data, "MEDICAL_WORK_RECORD_DETAIL_DAT");
        $db->query($query);
        
        for ($i = 0; $i <= 9; $i++) {
            $setSeq = sprintf("%03d", $i);
            $data = array();
            $data["WORK_DATE"][TEXT]  = str_replace("/", "-", $model->field["WORK_DATE"]);
            $data["STAFFCD"][TEXT]    = $model->field["STAFFCD"];
            $data["WORK_DIV"][TEXT]   = 2;
            $data["SEQ"][TEXT]        = $setSeq;
            $data["REMARK1"][TEXT]    = $model->field["REMARK1_".$setSeq];
            $data["REMARK2"][TEXT]    = $model->field["REMARK2_".$setSeq];
            $data["REMARK3"][TEXT]    = $model->field["REMARK3_".$setSeq];
            $data["REMARK4"][TEXT]    = $model->field["REMARK4_".$setSeq];
            $data["REMARK5"][TEXT]    = $model->field["REMARK5_".$setSeq];
            $data["REMARK6"][TEXT]    = $model->field["REMARK6_".$setSeq];
            $data["REMARK7"][TEXT]    = $model->field["REMARK7_".$setSeq];
            $data["REGISTERCD"][TEXT] = STAFFCD;
            $data["UPDATED"][FUNC]    = "sysdate()";

            $query = Query::insertSQL($data, "MEDICAL_WORK_RECORD_DETAIL_DAT");
            $db->query($query);
        }

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //MEDICAL_WORK_RECORD_DAT
        $query  = " DELETE FROM ";
        $query .= "     MEDICAL_WORK_RECORD_DAT ";
        $query .= " WHERE ";
        $query .= "     WORK_DATE = '".str_replace("/", "-", $model->field["WORK_DATE"])."' ";
        $query .= " AND STAFFCD   = '".$model->field["STAFFCD"]."' ";
        $query .= " AND WORK_DIV  = '2' ";
        $db->query($query);

        //MEDICAL_WORK_RECORD_DETAIL_DAT
        $query  = " DELETE FROM ";
        $query .= "     MEDICAL_WORK_RECORD_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     WORK_DATE = '".str_replace("/", "-", $model->field["WORK_DATE"])."' ";
        $query .= " AND STAFFCD   = '".$model->field["STAFFCD"]."' ";
        $query .= " AND WORK_DIV  = '2' ";
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }
}
?>
