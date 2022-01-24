<?php

require_once('for_php7.php');

class knjl721hQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '" . $year . "' AND ";
        $query .= "     NAMECD1 = '" . $namecd1 . "' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '" . $namecd2 . "' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //設定マスタより取得
    public function getEntexamSettingMst($model, $settingCd, $seq = "")
    {
        $query  = " SELECT ";
        $query .= "     SEQ, ";
        $query .= "     NAME1, ";
        $query .= "     ABBV1, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE3 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '" . $model->examYear . "' ";
        $query .= "     AND APPLICANTDIV = '" . $model->applicantdiv . "' ";
        $query .= "     AND SETTING_CD   = '" . $settingCd . "' ";
        if ($seq) {
            $query .= "     AND SEQ          = '" . $seq . "' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SEQ ";

        return $query;
    }

    //入試区分
    public function getTestdiv($model)
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->examYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    public function selectQuery($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     B1.EXAMNO, ";
        $query .= "     B1.NAME, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT01, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT02, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT03, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT04, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT05, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT06, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT07, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT08, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT09, ";
        $query .= "     CONF1.TOTAL5, ";
        $query .= "     CONF1.TOTAL3, ";
        $query .= "     CONF1.TOTAL_ALL, ";
        $query .= "     CONF1.SPECIALACTREC AS KODO, ";
        $query .= "     CONF1.ABSENCE_DAYS3 AS KESSEKI, ";
        $query .= "     B_D1.REMARK1 AS REMARK ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT B_D1 ";
        $query .= "       ON B_D1.ENTEXAMYEAR = B1.ENTEXAMYEAR ";
        $query .= "      AND B_D1.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "      AND B_D1.EXAMNO = B1.EXAMNO ";
        $query .= "      AND B_D1.SEQ = '033' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONF1 ";
        $query .= "       ON CONF1.ENTEXAMYEAR = B1.ENTEXAMYEAR ";
        $query .= "      AND CONF1.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "      AND CONF1.EXAMNO = B1.EXAMNO ";
        $query .= " WHERE ";
        $query .= "     B1.ENTEXAMYEAR = '" . $model->examYear . "' ";
        $query .= "     AND B1.APPLICANTDIV = '" . $model->applicantdiv . "' ";
        $query .= "     AND B1.TESTDIV = '" . $model->testdiv . "' ";
        if ($model->cmd == "read") {
            if ($model->s_examno != "") {
                $query .= "  AND B1.EXAMNO >= '".$model->s_examno."' ";
            }
            if ($model->e_examno != "") {
                $query .= "  AND B1.EXAMNO <= '".$model->e_examno."' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     B1.EXAMNO ";
        return $query;
    }

    public function getConfRptCount($model, $examno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTCONFRPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->examYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND EXAMNO = '{$examno}' ";
        return $query;
    }

    public function getBaseDetailCount($model, $examno, $seq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->examYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND EXAMNO = '{$examno}' ";
        $query .= "     AND SEQ = '{$seq}' ";

        return $query;
    }

    //更新
    public function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $tmp = explode(",", $model->hid_examno);
        $kyoka_array = (!$model->hid_kyokaseq) ? array() : explode(",", $model->hid_kyokaseq);
        $examno_array = array();
        foreach ($tmp as $key => $examno_count) {
            $examno_array = explode("-", $examno_count);
            $examno     = $examno_array[0];
            $recexam = $model->recordarry[$examno_array[1]];

            //ENTEXAM_APPLICANTCONFRPT_DAT(UPD or INS)
            $reccnt = $db->getOne(knjl721hQuery::getConfRptCount($model, $examno));
            $data = array();

            foreach ($kyoka_array as $kyokaseq) {
                $data["CONFIDENTIAL_RPT{$kyokaseq}"][NUMBER] = $recexam["kyoka_{$kyokaseq}"];
            }
            $data["TOTAL5"][NUMBER]         = $recexam["total5"];
            $data["TOTAL3"][NUMBER]         = $recexam["total3"];
            $data["TOTAL_ALL"][NUMBER]      = $recexam["total_all"];
            $data["SPECIALACTREC"][NUMBER]  = $recexam["kodo"];
            $data["ABSENCE_DAYS3"][NUMBER]  = $recexam["kesseki"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            if ($reccnt > 0) {
                $where  = "     WHERE ENTEXAMYEAR  = '" . $model->examYear . "' ";
                $where .= "       AND APPLICANTDIV = '" . $model->applicantdiv . "' ";
                $where .= "       AND EXAMNO       = '" . $examno . "' ";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT", $where);
            } else {
                $data["ENTEXAMYEAR"][TEXT]  = $model->examYear;
                $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
                $data["EXAMNO"][TEXT]       = $examno;
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT");
            }
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT(UPD or INS)
            $seq = '033';
            $reccnt = $db->getOne(knjl721hQuery::getBaseDetailCount($model, $examno, $seq));
            $data = array();
            $data["REMARK1"][TEXT]          = $recexam["remark"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            if ($reccnt > 0) {
                $where  = "     WHERE ENTEXAMYEAR  = '" . $model->examYear . "' ";
                $where .= "       AND APPLICANTDIV = '" . $model->applicantdiv . "' ";
                $where .= "       AND EXAMNO       = '" . $examno . "' ";
                $where .= "       AND SEQ          = '" . $seq . "' ";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            } else {
                $data["ENTEXAMYEAR"][TEXT]  = $model->examYear;
                $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
                $data["EXAMNO"][TEXT]       = $examno;
                $data["SEQ"][TEXT]          = $seq;
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            }
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
