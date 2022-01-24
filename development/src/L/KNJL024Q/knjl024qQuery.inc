<?php

require_once('for_php7.php');

class knjl024qQuery extends Query 
{

    //名称マスタよりデータを取得
    function get_name_cd($year, $namecd1, $sitei = "") {
        $query  = " SELECT ";
        if ($namecd1 !== 'L008') {
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        } else {
            $query .= "     ABBV1, ";
            $query .= "     NAMECD2 AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($sitei) {
            $query .= "     AND NAMECD2 = '".$sitei."' ";
        }
        if ($namecd1 == "L003") {
            $query .= " AND NAMESPARE3 = '".SCHOOLKIND."' ";
        }

        return $query;
    }


    //メインデータ取得
    function get_edit_data($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.NAME, ";
        $query .= "     L1.REMARK1, ";
        $query .= "     L1.REMARK2, ";
        $query .= "     L1.REMARK3, ";
        $query .= "     L1.REMARK9 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN  ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT L1 ";
        $query .= "          ON L1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND L1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND L1.EXAMNO = T1.EXAMNO ";
        $query .= "         AND L1.SEQ = '003' ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->applicantdiv."'";
        $query .= "     AND T1.EXAMNO = '".$model->examno."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //データカウント(ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT)
    function getCountDetaildata($model, $seq) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."'";
        $query .= "     AND EXAMNO = '".$model->examno."'";
        $query .= "     AND SEQ = '{$seq}' ";
        return $query;
    }

    //--- UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $getCount = $db->getOne(knjl024qQuery::getCountDetaildata($model, "003"));
        $data = array();
        if ($getCount == 0) {
            $data["ENTEXAMYEAR"][TEXT]  = $model->year;
            $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
            $data["EXAMNO"][TEXT]       = $model->examno;
            $data["SEQ"][TEXT]          = "003";
        }
        $data["REMARK1"][TEXT]      = $model->field["REMARK1"];
        $data["REMARK2"][TEXT]      = $model->field["REMARK2"];
        $data["REMARK3"][TEXT]      = $model->field["REMARK3"];
        $data["REMARK9"][TEXT]      = $model->field["REMARK9"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        if ($getCount == 0) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV = '".$model->applicantdiv."'";
            $where .= "   AND EXAMNO = '".$model->examno."'";
            $where .= "   AND SEQ = '003' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
        }
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>