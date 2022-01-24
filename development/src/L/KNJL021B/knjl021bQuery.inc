<?php

require_once('for_php7.php');

class knjl021bQuery extends Query 
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
        $query .= "     L2.REMARK1 AS DETAIL_REMARK1, ";
        $query .= "     L2.REMARK2 AS DETAIL_REMARK2, ";
        $query .= "     L2.REMARK3 AS DETAIL_REMARK3, ";
        $query .= "     L2.REMARK4 AS DETAIL_REMARK4, ";
        $query .= "     VALUE(L2.REMARK5, '0') || '-' || VALUE(L2.REMARK6, '000') || '-' || VALUE(L2.REMARK7, '0000') || '-' || L2.REMARK8 AS KAKUYAKU_SET ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     ENTEXAM_APPLICANTCONFRPT_DAT L1 ";
        $query .= " ON ";
        $query .= "     T1.ENTEXAMYEAR = L1.ENTEXAMYEAR AND ";
        $query .= "     T1.EXAMNO = L1.EXAMNO ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT L2 ";
        $query .= " ON ";
        $query .= "     T1.ENTEXAMYEAR = L2.ENTEXAMYEAR AND ";
        $query .= "     T1.EXAMNO = L2.EXAMNO AND ";
        $query .= "     L2.SEQ = '004' ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->year."' AND ";
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
        $query .= "     SEQ = '004' ";
        return $query;
    }

    //--- UPDATE
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        
        //データチェック
        $getCount = $db->getOne(knjl021bQuery::getCountdata($model));
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
        $data["AVERAGE_ALL"][NUMBER]        = round($model->field["AVERAGE_ALL"], 2);
        $data["ABSENCE_REMARK"][TEXT]       = $model->field["ABSENCE_REMARK"];
        $data["ABSENCE_REMARK2"][TEXT]      = $model->field["ABSENCE_REMARK2"];
        $data["ABSENCE_REMARK3"][TEXT]      = $model->field["ABSENCE_REMARK3"];
        
        if (!$model->field["BASE_FLG"]) $model->field["BASE_FLG"] = "0";
        $data["BASE_FLG"][TEXT]             = $model->field["BASE_FLG"];
        
        if (!$model->field["HEALTH_FLG"]) $model->field["HEALTH_FLG"] = "0";
        $data["HEALTH_FLG"][TEXT]           = $model->field["HEALTH_FLG"];
        
        if (!$model->field["ACTIVE_FLG"]) $model->field["ACTIVE_FLG"] = "0";
        $data["ACTIVE_FLG"][TEXT]           = $model->field["ACTIVE_FLG"];
        
        if (!$model->field["RESPONSIBLE_FLG"]) $model->field["RESPONSIBLE_FLG"] = "0";
        $data["RESPONSIBLE_FLG"][TEXT]      = $model->field["RESPONSIBLE_FLG"];
        
        if (!$model->field["ORIGINAL_FLG"]) $model->field["ORIGINAL_FLG"] = "0";
        $data["ORIGINAL_FLG"][TEXT]         = $model->field["ORIGINAL_FLG"];
        
        if (!$model->field["MIND_FLG"]) $model->field["MIND_FLG"] = "0";
        $data["MIND_FLG"][TEXT]             = $model->field["MIND_FLG"];
        
        if (!$model->field["NATURE_FLG"]) $model->field["NATURE_FLG"] = "0";
        $data["NATURE_FLG"][TEXT]           = $model->field["NATURE_FLG"];
        
        if (!$model->field["WORK_FLG"]) $model->field["WORK_FLG"] = "0";
        $data["WORK_FLG"][TEXT]             = $model->field["WORK_FLG"];
        
        if (!$model->field["JUSTICE_FLG"]) $model->field["JUSTICE_FLG"] = "0";
        $data["JUSTICE_FLG"][TEXT]          = $model->field["JUSTICE_FLG"];
        
        if (!$model->field["PUBLIC_FLG"]) $model->field["PUBLIC_FLG"] = "0";
        $data["PUBLIC_FLG"][TEXT]           = $model->field["PUBLIC_FLG"];
        
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

        //データチェック
        $getCount = $db->getOne(knjl021bQuery::getCountDetaildata($model));
        //ENTEXAM_APPLICANTBASE_DETAIL_DAT ---UPDATE
        $data = array();
        if ($getCount == 0) {
            $data["ENTEXAMYEAR"][TEXT]  = $model->year;
            $data["EXAMNO"][TEXT]       = $model->examno;
            $data["SEQ"][TEXT]          = "004";
        }
        $data["REMARK1"][TEXT]      = $model->field["DETAIL_REMARK1"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        if ($getCount == 0) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR = '".$model->year."' AND ";
            $where .= "       EXAMNO = '".$model->examno."' AND ";
            $where .= "       SEQ = '004' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
        }
        $db->query($query);


        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>