<?php

require_once('for_php7.php');

class knjl021qQuery extends Query 
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
        //実践模試
        $query .= "     S2.PLACEAREA AS SAT_PLACE, ";
        $query .= "     S1.SAT_NO, ";
        $query .= "     S3.SCORE_TOTAL AS SAT_TOTAL, ";
        $query .= "     S4.NAME1 AS SAT_JUDGE, ";
        //リンデン模試
        $query .= "     S1.MOCK_AUG_NO, ";
        $query .= "     S1.MOCK_AUG_DEV, ";
        $query .= "     '' AS MOCK_AUG_SCORE, ";//仕様未確定
        $query .= "     S1.MOCK_SEP_NO, ";
        $query .= "     S1.MOCK_SEP_DEV, ";
        $query .= "     '' AS MOCK_SEP_SCORE, ";//仕様未確定
        //公的試験
        $query .= "     PUB.SCORE1 AS PUBLIC_SCORE1, ";
        $query .= "     PUB.SCORE2 AS PUBLIC_SCORE2, ";
        $query .= "     CASE WHEN PUB.SCORE1 IS NOT NULL AND PUB.SCORE2 IS NOT NULL THEN SMALLINT(ROUND(FLOAT(PUB.SCORE1 + PUB.SCORE2) / 2, 0)) ";
        $query .= "          WHEN PUB.SCORE1 IS NOT NULL AND PUB.SCORE2 IS     NULL THEN PUB.SCORE1 ";
        $query .= "          WHEN PUB.SCORE1 IS     NULL AND PUB.SCORE2 IS NOT NULL THEN PUB.SCORE2 ";
        $query .= "     END AS PUBLIC_AVG, ";
        $query .= "     PUB.KAKUYAKU_FLG AS PUBLIC_KAKUYAKU_FLG, ";
        //教科(1,2年)
        for ($i = 1; $i <= 9; $i++) {
            $num = sprintf("%02d", $i);
            $query .= "     L1.REMARK{$i} AS CONF1_RPT{$num}, ";
            $query .= "     L2.REMARK{$i} AS CONF2_RPT{$num}, ";
        }
        //5科9科平均(1,2年)
        $query .= "     L1.REMARK11 AS CONF1_AVG5, ";
        $query .= "     L1.REMARK12 AS CONF1_AVG9, ";
        $query .= "     L2.REMARK11 AS CONF2_AVG5, ";
        $query .= "     L2.REMARK12 AS CONF2_AVG9, ";
        $query .= "     L3.* ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN  ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT L1 ";
        $query .= "          ON L1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND L1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND L1.EXAMNO = T1.EXAMNO ";
        $query .= "         AND L1.SEQ = '001' ";
        $query .= "     LEFT JOIN  ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT L2 ";
        $query .= "          ON L2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND L2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND L2.EXAMNO = T1.EXAMNO ";
        $query .= "         AND L2.SEQ = '002' ";
        $query .= "     LEFT JOIN  ENTEXAM_APPLICANTCONFRPT_DAT L3 ";
        $query .= "          ON L3.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND L3.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND L3.EXAMNO = T1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_PUBLIC_TEST_DAT PUB ";
        $query .= "          ON PUB.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND PUB.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND PUB.EXAMNO = T1.EXAMNO ";
        $query .= "     LEFT JOIN SAT_APP_FORM_MST S1 ON S1.YEAR = '".CTRL_YEAR."' AND S1.SAT_NO = T1.JIZEN_BANGOU ";
        $query .= "     LEFT JOIN SAT_EXAM_PLACE_DAT S2 ON S2.YEAR = S1.YEAR AND S2.PLACECD = S1.PLACECD ";
        $query .= "     LEFT JOIN SAT_EXAM_DAT S3 ON S3.YEAR = '".CTRL_YEAR."' AND S3.SAT_NO = T1.JIZEN_BANGOU ";
        $query .= "     LEFT JOIN V_NAME_MST S4 ON S4.YEAR = S3.YEAR AND S4.NAMECD1 = 'L200' AND S4.NAMECD2 = S3.JUDGE_SAT ";
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

    //データカウント(ENTEXAM_PUBLIC_TEST_DAT)
    function getCountPublic($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PUBLIC_TEST_DAT ";
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
        $getCount = $db->getOne(knjl021qQuery::getCountdata($model));
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
            $where  = " WHERE ENTEXAMYEAR = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV = '".$model->applicantdiv."'";
            $where .= "   AND EXAMNO = '".$model->examno."'";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT", $where);
        }
        $db->query($query);

        //ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT
        //教科(1年)
        $getCount = $db->getOne(knjl021qQuery::getCountDetaildata($model, "001"));
        $data = array();
        if ($getCount == 0) {
            $data["ENTEXAMYEAR"][TEXT]  = $model->year;
            $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
            $data["EXAMNO"][TEXT]       = $model->examno;
            $data["SEQ"][TEXT]          = "001";
        }
        for ($i = 1; $i <= 9; $i++) {
            $num = sprintf("%02d", $i);
            $data["REMARK{$i}"][TEXT]   = $model->field["CONF1_RPT{$num}"];
        }
        //5科9科平均(1年)
        $data["REMARK11"][TEXT]   = $model->field["CONF1_AVG5"];
        $data["REMARK12"][TEXT]   = $model->field["CONF1_AVG9"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        if ($getCount == 0) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV = '".$model->applicantdiv."'";
            $where .= "   AND EXAMNO = '".$model->examno."'";
            $where .= "   AND SEQ = '001' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
        }
        $db->query($query);
        //教科(2年)
        $getCount = $db->getOne(knjl021qQuery::getCountDetaildata($model, "002"));
        $data = array();
        if ($getCount == 0) {
            $data["ENTEXAMYEAR"][TEXT]  = $model->year;
            $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
            $data["EXAMNO"][TEXT]       = $model->examno;
            $data["SEQ"][TEXT]          = "002";
        }
        for ($i = 1; $i <= 9; $i++) {
            $num = sprintf("%02d", $i);
            $data["REMARK{$i}"][TEXT]   = $model->field["CONF2_RPT{$num}"];
        }
        //5科9科平均(2年)
        $data["REMARK11"][TEXT]   = $model->field["CONF2_AVG5"];
        $data["REMARK12"][TEXT]   = $model->field["CONF2_AVG9"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        if ($getCount == 0) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV = '".$model->applicantdiv."'";
            $where .= "   AND EXAMNO = '".$model->examno."'";
            $where .= "   AND SEQ = '002' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
        }
        $db->query($query);

        //ENTEXAM_PUBLIC_TEST_DAT
        //データチェック
        $getCount = $db->getOne(knjl021qQuery::getCountPublic($model));
        $data = array();
        if ($getCount == 0) {
            $data["ENTEXAMYEAR"][TEXT]          = $model->year;
            $data["APPLICANTDIV"][TEXT]         = $model->applicantdiv;
            $data["EXAMNO"][TEXT]               = $model->examno;
        }
        $data["KAKUYAKU_FLG"][TEXT]         = $model->field["PUBLIC_KAKUYAKU_FLG"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount == 0) {
            $query = Query::insertSQL($data, "ENTEXAM_PUBLIC_TEST_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."'";
            $where .= "   AND EXAMNO        = '".$model->examno."'";
            $query = Query::updateSQL($data, "ENTEXAM_PUBLIC_TEST_DAT", $where);
        }
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>