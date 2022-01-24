<?php

require_once('for_php7.php');

class knjl021gQuery extends Query 
{

    //名称マスタよりデータを取得
    function get_name_cd($year, $cd, $sitei = "")
    {
        $query  = " SELECT ";
        if ($cd !== 'L008') {
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        } else {
            $query .= "     ABBV1, ";
            $query .= "     NAMECD2 AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$cd."' ";
        if ($sitei) {
            $query .= "     AND NAMECD2 = '".$sitei."' ";
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
        $query .= "     L2.REMARK1 AS JITURYOKU_KOKUGO, ";
        $query .= "     L2.REMARK2 AS JITURYOKU_EIGO, ";
        $query .= "     L2.REMARK3 AS JITURYOKU_SUUGAKU, ";
        $query .= "     L2.REMARK4 AS JITURYOKU_TOTAL, ";
        $query .= "     L2.REMARK5 AS MOGI_HENSATI, ";
        $query .= "     L1.* ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN  ENTEXAM_APPLICANTCONFRPT_DAT L1 ";
        $query .= "          ON L1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND L1.EXAMNO = T1.EXAMNO ";
        $query .= "     LEFT JOIN  ENTEXAM_APPLICANTBASE_DETAIL_DAT L2 ";
        $query .= "          ON L2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND L2.EXAMNO = T1.EXAMNO ";
        $query .= "         AND L2.SEQ = '020' ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND T1.EXAMNO = '".$model->examno."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //データカウント(ENTEXAM_APPLICANTCONFRPT_DAT)
    function getCountdata($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTCONFRPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "     EXAMNO = '".$model->examno."'";

        return $query;
    }

    //データカウント(ENTEXAM_APPLICANTBASE_DETAIL_DAT)
    function getCountDetaildata($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "     EXAMNO = '".$model->examno."' AND ";
        $query .= "     SEQ = '020' ";
        return $query;
    }

    //--- UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //データチェック
        $getCount = $db->getOne(knjl021gQuery::getCountdata($model));
        //ENTEXAM_APPLICANTCONFRPT_DAT ---UPDATE
        $data = array();
        if ($getCount == 0) {
            $data["ENTEXAMYEAR"][TEXT]          = $model->year;
            $data["EXAMNO"][TEXT]               = $model->examno;
        }
        $data["CONFIDENTIAL_RPT01"][NUMBER] = $model->field["CONFIDENTIAL_RPT01"];
        $data["CONFIDENTIAL_RPT02"][NUMBER] = $model->field["CONFIDENTIAL_RPT02"];
        $data["CONFIDENTIAL_RPT03"][NUMBER] = $model->field["CONFIDENTIAL_RPT03"];
        $data["CONFIDENTIAL_RPT04"][NUMBER] = $model->field["CONFIDENTIAL_RPT04"];
        $data["CONFIDENTIAL_RPT05"][NUMBER] = $model->field["CONFIDENTIAL_RPT05"];
        $data["CONFIDENTIAL_RPT06"][NUMBER] = $model->field["CONFIDENTIAL_RPT06"];
        $data["CONFIDENTIAL_RPT07"][NUMBER] = $model->field["CONFIDENTIAL_RPT07"];
        $data["CONFIDENTIAL_RPT08"][NUMBER] = $model->field["CONFIDENTIAL_RPT08"];
        $data["CONFIDENTIAL_RPT09"][NUMBER] = $model->field["CONFIDENTIAL_RPT09"];
        $data["ABSENCE_DAYS"][NUMBER]       = $model->field["ABSENCE_DAYS"];
        $data["ABSENCE_DAYS2"][NUMBER]      = $model->field["ABSENCE_DAYS2"];
        $data["ABSENCE_DAYS3"][NUMBER]      = $model->field["ABSENCE_DAYS3"];
        $data["TOTAL_ALL"][NUMBER]          = $model->field["TOTAL_ALL"];
        $data["AVERAGE_ALL"][NUMBER]        = $model->field["AVERAGE_ALL"];
//        $data["AVERAGE_ALL"][NUMBER]        = round($model->field["AVERAGE_ALL"], 2);
        $data["ABSENCE_REMARK"][TEXT]       = $model->field["ABSENCE_REMARK"];
        $data["ABSENCE_REMARK2"][TEXT]      = $model->field["ABSENCE_REMARK2"];
        $data["ABSENCE_REMARK3"][TEXT]      = $model->field["ABSENCE_REMARK3"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount == 0) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR = '".$model->year."' AND";
            $where .= "       EXAMNO = '".$model->examno."'";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT", $where);
        }
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT
        $getCount = $db->getOne(knjl021gQuery::getCountDetaildata($model));
        $data = array();
        if ($getCount == 0) {
            $data["ENTEXAMYEAR"][TEXT]  = $model->year;
            $data["EXAMNO"][TEXT]       = $model->examno;
            $data["SEQ"][TEXT]          = "020";
        }
        $data["REMARK1"][TEXT]      = $model->field["JITURYOKU_KOKUGO"];
        $data["REMARK2"][TEXT]      = $model->field["JITURYOKU_EIGO"];
        $data["REMARK3"][TEXT]      = $model->field["JITURYOKU_SUUGAKU"];
        $data["REMARK4"][TEXT]      = $model->field["JITURYOKU_TOTAL"];
        $data["REMARK5"][TEXT]      = $model->field["MOGI_HENSATI"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        if ($getCount == 0) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR = '".$model->year."' AND ";
            $where .= "       EXAMNO = '".$model->examno."' AND ";
            $where .= "       SEQ = '020' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
        }
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>