<?php

require_once('for_php7.php');

class knjl021uQuery extends Query 
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

        return $query;
    }


    //メインデータ取得
    function get_edit_data($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.NAME, ";
        $query .= "     L3.* ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN  ENTEXAM_APPLICANTCONFRPT_DAT L3 ";
        $query .= "          ON L3.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND L3.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND L3.EXAMNO = T1.EXAMNO ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->applicantdiv."'";
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
        $query .= "     ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."'";
        $query .= "     AND EXAMNO = '".$model->examno."'";

        return $query;
    }

    //--- UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //データチェック
        $getCount = $db->getOne(knjl021uQuery::getCountdata($model));
        //ENTEXAM_APPLICANTCONFRPT_DAT ---UPDATE
        $data = array();
        if ($getCount == 0) {
            $data["ENTEXAMYEAR"][TEXT]          = $model->year;
            $data["APPLICANTDIV"][TEXT]         = $model->applicantdiv;
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
        $data["AVERAGE5"][NUMBER]           = $model->field["AVERAGE5"];
        $data["AVERAGE_ALL"][NUMBER]        = $model->field["AVERAGE_ALL"];
        $data["ABSENCE_DAYS3"][NUMBER]      = $model->field["ABSENCE_DAYS3"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount == 0) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV = '".$model->applicantdiv."'";
            $where .= "   AND EXAMNO = '".$model->examno."'";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT", $where);
        }
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>