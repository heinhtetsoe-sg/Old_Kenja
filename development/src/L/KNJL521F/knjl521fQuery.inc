<?php

require_once('for_php7.php');

class knjl521fQuery extends Query 
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

    //確約区分のコンボ表示するためのデータ取得
    function getKakuyakuData($model)
    {
        $query  = " SELECT ";
        $query .= "     PROMISE_COURSE_NAME AS LABEL, ";
        $query .= "     VALUE(NORMAL_PASSCOURSECD, '0') || '-' || VALUE(NORMAL_PASSMAJORCD, '000') || '-' || VALUE(NORMAL_PASSEXAMCOURSECD, '0000') || '-' || JUDGMENT_DIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_JUDGMENT_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' ";
        $query .= " AND PROMISE_RECOMMEND_TEST_FLG = '1' ";
        $query .= " ORDER BY ";
        $query .= "     JUDGMENT_DIV ";

        return $query;
    }


    //メインデータ取得
    function get_edit_data($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.NAME, ";
        $query .= "     L1.*, ";
        $query .= "     SEQ020.REMARK1 AS SEQ020_R1, ";
        $query .= "     SEQ020.REMARK2 AS SEQ020_R2, ";
        $query .= "     SEQ020.REMARK3 AS SEQ020_R3, ";
        $query .= "     SEQ020.REMARK4 AS SEQ020_R4, ";
        $query .= "     SEQ004.REMARK1 AS SEQ004_R1, ";
        $query .= "     VALUE(SEQ004.REMARK5, '0') || '-' || VALUE(SEQ004.REMARK6, '000') || '-' || VALUE(SEQ004.REMARK7, '0000') || '-' || SEQ004.REMARK8 AS KAKUYAKU_SET ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT OUTER JOIN ENTEXAM_APPLICANTCONFRPT_DAT L1 ";
        $query .= "           ON T1.ENTEXAMYEAR = L1.ENTEXAMYEAR ";
        $query .= "          AND T1.EXAMNO = L1.EXAMNO ";
        $query .= "     LEFT OUTER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ004 ";
        $query .= "           ON T1.ENTEXAMYEAR = SEQ004.ENTEXAMYEAR ";
        $query .= "          AND T1.EXAMNO = SEQ004.EXAMNO ";
        $query .= "          AND SEQ004.SEQ = '004' ";
        $query .= "     LEFT OUTER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ020 ";
        $query .= "           ON T1.ENTEXAMYEAR = SEQ020.ENTEXAMYEAR ";
        $query .= "          AND T1.EXAMNO = SEQ020.EXAMNO ";
        $query .= "          AND SEQ020.SEQ = '020' ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->year."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->applicantdiv."' AND ";
        $query .= "     T1.EXAMNO = '".$model->examno."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //データカウント(ENTEXAM_APPLICANTCONFRPT_DAT)
    function getCountdata($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTCONFRPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "     APPLICANTDIV = '".$model->applicantdiv."' AND ";
        $query .= "     EXAMNO = '".$model->examno."'";

        return $query;
    }

    //データカウント(ENTEXAM_APPLICANTBASE_DETAIL_DAT)
    function getCountDetaildata($model, $seq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "     APPLICANTDIV = '".$model->applicantdiv."' AND ";
        $query .= "     EXAMNO = '".$model->examno."' AND ";
        $query .= "     SEQ = '{$seq}' ";
        return $query;
    }

    //--- UPDATE
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        
        //データチェック
        $getCount = $db->getOne(knjl521fQuery::getCountdata($model));
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
        $data["TOTAL_ALL"][NUMBER]          = $model->field["TOTAL_ALL"];
        $data["TOTAL5"][NUMBER]             = $model->field["TOTAL5"];
        $data["KASANTEN_ALL"][NUMBER]       = $model->field["KASANTEN_ALL"];
        $data["ABSENCE_DAYS"][NUMBER]       = $model->field["ABSENCE_DAYS"];
        $data["ABSENCE_DAYS2"][NUMBER]      = $model->field["ABSENCE_DAYS2"];
        $data["ABSENCE_DAYS3"][NUMBER]      = $model->field["ABSENCE_DAYS3"];
        $data["ABSENCE_REMARK"][TEXT]       = $model->field["ABSENCE_REMARK"];
        $data["ABSENCE_REMARK2"][TEXT]      = $model->field["ABSENCE_REMARK2"];
        $data["ABSENCE_REMARK3"][TEXT]      = $model->field["ABSENCE_REMARK3"];
        $data["REMARK1"][TEXT]              = $model->field["REMARK1"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount == 0) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR = '".$model->year."' AND";
            $where .= "       APPLICANTDIV = '".$model->applicantdiv."' AND ";
            $where .= "       EXAMNO = '".$model->examno."'";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT", $where);
        }
        $db->query($query);

        //データチェック
        $getCount = $db->getOne(knjl521fQuery::getCountDetaildata($model, "004"));
        //ENTEXAM_APPLICANTBASE_DETAIL_DAT ---UPDATE
        $data = array();
        if ($getCount == 0) {
            $data["ENTEXAMYEAR"][TEXT]  = $model->year;
            $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
            $data["EXAMNO"][TEXT]       = $model->examno;
            $data["SEQ"][TEXT]          = "004";
        }
        $data["REMARK1"][TEXT]      = $model->field["SEQ004_R1"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        if ($getCount == 0) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR = '".$model->year."' AND ";
            $where .= "       APPLICANTDIV = '".$model->applicantdiv."' AND ";
            $where .= "       EXAMNO = '".$model->examno."' AND ";
            $where .= "       SEQ = '004' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
        }
        $db->query($query);

        //データチェック
        $getCount = $db->getOne(knjl521fQuery::getCountDetaildata($model, "020"));
        //ENTEXAM_APPLICANTBASE_DETAIL_DAT ---UPDATE
        $data = array();
        if ($getCount == 0) {
            $data["ENTEXAMYEAR"][TEXT]  = $model->year;
            $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
            $data["EXAMNO"][TEXT]       = $model->examno;
            $data["SEQ"][TEXT]          = "020";
        }
        $data["REMARK1"][TEXT]      = $model->field["SEQ020_R1"];
        $data["REMARK2"][TEXT]      = $model->field["SEQ020_R2"];
        $data["REMARK3"][TEXT]      = $model->field["SEQ020_R3"];
        $data["REMARK4"][TEXT]      = $model->field["SEQ020_R4"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        if ($getCount == 0) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR = '".$model->year."' AND ";
            $where .= "       APPLICANTDIV = '".$model->applicantdiv."' AND ";
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