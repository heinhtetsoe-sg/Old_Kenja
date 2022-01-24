<?php

require_once('for_php7.php');

class knjl021fQuery extends Query 
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
        $query .= "     L1.* ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN  ENTEXAM_APPLICANTCONFRPT_DAT L1 ";
        $query .= "          ON T1.ENTEXAMYEAR = L1.ENTEXAMYEAR ";
        $query .= "         AND T1.EXAMNO = L1.EXAMNO ";
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

    //--- UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //データチェック
        $getCount = $db->getOne(knjl021fQuery::getCountdata($model));
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
        $data["TOTAL3"][NUMBER]             = $model->field["TOTAL3"];
        $data["TOTAL5"][NUMBER]             = $model->field["TOTAL5"];
        $data["TOTAL_ALL"][NUMBER]          = $model->field["TOTAL_ALL"];
        $data["KASANTEN_ALL"][NUMBER]       = $model->field["KASANTEN_ALL"];
        $data["REMARK1"][TEXT]              = $model->field["REMARK1"];
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

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>