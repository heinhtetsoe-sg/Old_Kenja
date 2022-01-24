<?php

require_once('for_php7.php');

class knjl221gQuery extends Query {

    //名称マスタよりデータを取得
    function get_name_cd($year, $cd, $sitei = "") {
        $query  = " SELECT ";
        if ($cd !== 'L008') {
            if ($cd == 'L051') {
                $query .= "     NAME1 AS LABEL, ";
            } else {
                $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
            }
            $query .= "     NAMECD2 AS VALUE, ";
            $query .= "     NAMESPARE1 ";
        } else {
            $query .= "     ABBV1, ";
            $query .= "     NAMESPARE1, ";
            $query .= "     NAMECD2 AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND NAMECD1 = '{$cd}' ";
        if ($sitei) {
            $query .= "     AND NAMECD2 = '{$sitei}' ";
        }
        $query .= " ORDER BY ";
        if ($cd == 'L051') {
            $query .= "     NAME1 ";
        } else {
            $query .= "     VALUE ";
        }

        return $query;
    }

    //メインデータ取得
    function get_edit_data($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     BASE.APPLICANTDIV, ";
        $query .= "     BASE.TESTDIV, ";
        $query .= "     BASE.NAME, ";
        $query .= "     S004.REMARK1 AS REMARK1_004, ";  //入試相談日
        $query .= "     S004.REMARK2 AS REMARK2_004, ";  //相談結果
        $query .= "     S004.REMARK3 AS REMARK3_004, ";  //体験ゼミ結果
        $query .= "     S031.REMARK10 AS REMARK10_031, ";//特記事項
        $query .= "     S031.REMARK6 AS REMARK6_031, ";  //面接日
        $query .= "     S031.REMARK7 AS REMARK7_031, ";  //開始時間(時)
        $query .= "     S031.REMARK8 AS REMARK8_031, ";  //開始時間(分)
        $query .= "     S031.REMARK9 AS REMARK9_031, ";  //面接会場
        $query .= "     CONF.* ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     LEFT JOIN  ENTEXAM_APPLICANTCONFRPT_DAT CONF ON CONF.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "                                                 AND CONF.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "                                                 AND CONF.EXAMNO       = BASE.EXAMNO ";
        $query .= "     LEFT JOIN  ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT S004 ON S004.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "                                                        AND S004.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "                                                        AND S004.EXAMNO       = BASE.EXAMNO ";
        $query .= "                                                        AND S004.SEQ          = '004' ";
        $query .= "     LEFT JOIN  ENTEXAM_APPLICANTBASE_DETAIL_DAT S031 ON S031.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "                                                     AND S031.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "                                                     AND S031.EXAMNO       = BASE.EXAMNO ";
        $query .= "                                                     AND S031.SEQ          = '031' ";
        $query .= " WHERE ";
        $query .= "         BASE.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND BASE.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND BASE.EXAMNO       = '{$model->examno}' ";

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
        $query .= "         ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND EXAMNO       = '{$model->examno}' ";

        return $query;
    }

    //データカウント(ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT)
    function getCountDetaildata($model, $seq) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND EXAMNO       = '{$model->examno}' ";
        $query .= "     AND SEQ          = '{$seq}' ";
        return $query;
    }

    //データカウント(ENTEXAM_APPLICANTBASE_DETAIL_DAT)
    function getCountBaseDetaildata($model, $seq) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND EXAMNO       = '{$model->examno}' ";
        $query .= "     AND SEQ          = '{$seq}' ";
        return $query;
    }

    //--- UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //データチェック
        $baseCnt = $db->getOne(knjl221gQuery::getCountdata($model));
        //ENTEXAM_APPLICANTCONFRPT_DAT ---UPDATE
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]          = $model->year;
        $data["APPLICANTDIV"][TEXT]         = $model->applicantdiv;
        $data["EXAMNO"][TEXT]               = $model->examno;
        $data["CONFIDENTIAL_RPT01"][NUMBER] = $model->field["CONFIDENTIAL_RPT01"];
        $data["CONFIDENTIAL_RPT02"][NUMBER] = $model->field["CONFIDENTIAL_RPT02"];
        $data["CONFIDENTIAL_RPT03"][NUMBER] = $model->field["CONFIDENTIAL_RPT03"];
        $data["CONFIDENTIAL_RPT04"][NUMBER] = $model->field["CONFIDENTIAL_RPT04"];
        $data["CONFIDENTIAL_RPT05"][NUMBER] = $model->field["CONFIDENTIAL_RPT05"];
        $data["CONFIDENTIAL_RPT06"][NUMBER] = $model->field["CONFIDENTIAL_RPT06"];
        $data["CONFIDENTIAL_RPT07"][NUMBER] = $model->field["CONFIDENTIAL_RPT07"];
        $data["CONFIDENTIAL_RPT08"][NUMBER] = $model->field["CONFIDENTIAL_RPT08"];
        $data["CONFIDENTIAL_RPT09"][NUMBER] = $model->field["CONFIDENTIAL_RPT09"];
        $data["ABSENCE_DAYS3"][NUMBER]      = $model->field["ABSENCE_DAYS3"];
        $data["TOTAL5"][NUMBER]             = $model->field["TOTAL5"];
        $data["TOTAL_ALL"][NUMBER]          = $model->field["TOTAL_ALL"];
        $data["TOTALSTUDYTIME"][TEXT]       = $model->field["TOTALSTUDYTIME"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($baseCnt == 0) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR  = '{$model->year}' ";
            $where .= "   AND APPLICANTDIV = '{$model->applicantdiv}' ";
            $where .= "   AND EXAMNO       = '{$model->examno}'";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT", $where);
        }
        $db->query($query);

        //ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT
        $cnt004 = $db->getOne(knjl221gQuery::getCountDetaildata($model, '004'));
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]  = $model->year;
        $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
        $data["EXAMNO"][TEXT]       = $model->examno;
        $data["SEQ"][TEXT]          = "004";
        $data["REMARK1"][TEXT]      = $model->field["REMARK1_004"];
        $data["REMARK2"][TEXT]      = $model->field["REMARK2_004"];
        $data["REMARK3"][TEXT]      = $model->field["REMARK3_004"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        if ($cnt004 == 0) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR  = '{$model->year}' ";
            $where .= "   AND APPLICANTDIV = '{$model->applicantdiv}' ";
            $where .= "   AND EXAMNO       = '{$model->examno}' ";
            $where .= "   AND SEQ          = '004' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
        }
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT
        $cnt031 = $db->getOne(knjl221gQuery::getCountBaseDetaildata($model, '031'));
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]  = $model->year;
        $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
        $data["EXAMNO"][TEXT]       = $model->examno;
        $data["SEQ"][TEXT]          = "031";
        $data["REMARK6"][TEXT]      = $model->field["REMARK6_031"];
        $data["REMARK7"][TEXT]      = $model->field["REMARK7_031"];
        $data["REMARK8"][TEXT]      = $model->field["REMARK8_031"];
        $data["REMARK9"][TEXT]      = $model->field["REMARK9_031"];
        $data["REMARK10"][TEXT]     = $model->field["REMARK10_031"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        if ($cnt031 == 0) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR  = '{$model->year}' ";
            $where .= "   AND APPLICANTDIV = '{$model->applicantdiv}' ";
            $where .= "   AND EXAMNO       = '{$model->examno}' ";
            $where .= "   AND SEQ          = '031' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
        }
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>