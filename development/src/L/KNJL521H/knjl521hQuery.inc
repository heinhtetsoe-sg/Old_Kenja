<?php

require_once('for_php7.php');
class knjl521hQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAME2, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試判別取得
    public function getDistinctId($model)
    {
        $query  = " SELECT ";
        $query .= "     DISTINCT_ID AS VALUE, ";
        $query .= "     DISTINCT_ID || ':' || DISTINCT_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_DISTINCTION_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    public function selectQuery($model, $div)
    {
        $query  = " WITH MAIN_T AS ( ";
        $query .= "     SELECT ";
        if ($div == "csv") {
            $query .= "         RCPT.ENTEXAMYEAR, ";
            $query .= "         RCPT.APPLICANTDIV, ";
            $query .= "         RCPT.DISTINCT_ID, ";
            $query .= "         D007.REMARK1 AS SHIGANSYA_SEQ, ";
            $query .= "         D007.REMARK2 AS MIRAI_TESTDIV, ";
            $query .= "         '".$model->lastColumn."' AS ".$model->lastColumn.", ";
        }
        $query .= "         RCPT.EXAMNO, ";
        $query .= "         BASE.NAME, ";
        $query .= "         BASE.NAME_KANA, ";
        $query .= "         Z002.NAME1 AS SEX, ";
        $query .= "         D007.REMARK8 AS TEST_NAME_ABBV, ";
        for ($i = 1; $i <= 12; $i++) {
            $num = sprintf("%02d", $i);
            $query .= "         CONF.CONFIDENTIAL_RPT{$num}, ";
            $query .= "         D001.REMARK{$i} AS DET001_{$i}, ";
        }
        $query .= "         CONF.TOTAL_ALL ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECEPT_DAT RCPT ";
        $query .= "         LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON RCPT.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "                                                 AND RCPT.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "                                                 AND RCPT.EXAMNO       = BASE.EXAMNO ";
        $query .= "         LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONF ";
        $query .= "              ON CONF.ENTEXAMYEAR  = RCPT.ENTEXAMYEAR ";
        $query .= "             AND CONF.APPLICANTDIV = RCPT.APPLICANTDIV ";
        $query .= "             AND CONF.EXAMNO       = RCPT.EXAMNO ";
        $query .= "         LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT D001 ";
        $query .= "              ON D001.ENTEXAMYEAR  = RCPT.ENTEXAMYEAR ";
        $query .= "             AND D001.APPLICANTDIV = RCPT.APPLICANTDIV ";
        $query .= "             AND D001.EXAMNO       = RCPT.EXAMNO ";
        $query .= "             AND D001.SEQ          = '001' ";
        $query .= "         LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ";
        $query .= "                                AND Z002.NAMECD2 = BASE.SEX ";
        $query .= "         LEFT JOIN NAME_MST L003 ON L003.NAMECD1 = 'L003' ";
        $query .= "                                AND L003.NAMECD2 = RCPT.APPLICANTDIV ";
        $query .= "         LEFT JOIN ENTEXAM_DISTINCTION_MST DIST ON DIST.ENTEXAMYEAR  = RCPT.ENTEXAMYEAR ";
        $query .= "                                               AND DIST.APPLICANTDIV = RCPT.APPLICANTDIV ";
        $query .= "                                               AND DIST.DISTINCT_ID  = RCPT.DISTINCT_ID ";
        $query .= "         LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT D007 ";
        $query .= "              ON D007.ENTEXAMYEAR  = RCPT.ENTEXAMYEAR ";
        $query .= "             AND D007.APPLICANTDIV = RCPT.APPLICANTDIV ";
        $query .= "             AND D007.TESTDIV      = RCPT.TESTDIV ";
        $query .= "             AND D007.EXAM_TYPE    = RCPT.EXAM_TYPE ";
        $query .= "             AND D007.RECEPTNO     = RCPT.RECEPTNO ";
        $query .= "             AND D007.SEQ          = '007' ";
        $query .= "     WHERE ";
        $query .= "             RCPT.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "         AND RCPT.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "         AND RCPT.DISTINCT_ID  = '{$model->distinctId}' ";
        if ($div == "list") {
            if ($model->cmd == "next") {
                $query .= "         AND RCPT.EXAMNO > '{$model->e_examno}' ";
            } elseif ($model->cmd == "back") {
                $query .= "         AND RCPT.EXAMNO < '{$model->s_examno}' ";
            } else {
                if (strlen($model->s_examno)) {
                    $query .= "         AND RCPT.EXAMNO >= '{$model->s_examno}' ";
                }
            }

            $sort = ($model->cmd == "back") ? "DESC" : "";
            $query .= "     ORDER BY ";
            $query .= "         RCPT.EXAMNO ".$sort;
            $query .= "     FETCH FIRST 40 ROWS ONLY ";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " ORDER BY ";
        $query .= "     EXAMNO ";

        return $query;
    }

    /************/
    /* 更新処理 */
    /************/
    //更新
    public function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //登録
        foreach ($model->confData as $examno => $confArr) {
            /** ENTEXAM_APPLICANTCONFRPT_DAT **/
            //登録済みチェック
            //未登録なら追加、登録済みなら更新
            $conf_cnt = $db->getOne(knjl521hQuery::cntEntexamApplicantconfrptDat($model, $examno));

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["EXAMNO"][TEXT]           = $examno;

            for ($i = 1; $i <= 12; $i++) {
                $num = sprintf("%02d", $i);
                $data["CONFIDENTIAL_RPT".$num][NUMBER]      = $confArr["CONFIDENTIAL_RPT".$num];
            }
            $data["TOTAL_ALL"][NUMBER]      = $confArr["TOTAL_ALL"];

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR  = '{$model->year}' ";
            $where .= "   AND APPLICANTDIV = '{$model->applicantdiv}' ";
            $where .= "   AND EXAMNO       = '{$examno}' ";

            if (0 == $conf_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT", $where);
            }
            $db->query($query);

            /** ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT **/
            $query  = " DELETE FROM ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT ";
            $query .= "       WHERE ENTEXAMYEAR     = '{$model->year}' ";
            $query .= "         AND APPLICANTDIV    = '{$model->applicantdiv}' ";
            $query .= "         AND EXAMNO          = '{$examno}' ";
            $query .= "         AND SEQ             = '001' ";
            $db->query($query);

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["EXAMNO"][TEXT]           = $examno;
            $data["SEQ"][TEXT]              = "001";

            for ($i = 1; $i <= 12; $i++) {
                $data["REMARK".$i][TEXT]      = $confArr["DET001_".$i];
            }

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //件数取得 -- ENTEXAM_APPLICANTCONFRPT_DAT
    public function cntEntexamApplicantconfrptDat($model, $examno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTCONFRPT_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND EXAMNO       = '{$examno}' ";

        return $query;
    }

    /**************/
    /* ＣＳＶ取込 */
    /**************/
    //データの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {

            /** ENTEXAM_APPLICANTCONFRPT_DAT **/
            $conf_cnt = $db->getOne(knjl521hQuery::cntEntexamApplicantconfrptDat($model, $data_arr[$i]["EXAMNO"]));

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];

            for ($k = 1; $k <= 12; $k++) {
                $num = sprintf("%02d", $k);
                $data["CONFIDENTIAL_RPT".$num][NUMBER]      = $data_arr[$i]["CONFIDENTIAL_RPT".$num];
            }
            $data["TOTAL_ALL"][NUMBER]      = $data_arr[$i]["TOTAL_ALL"];

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR  = '{$model->year}' ";
            $where .= "   AND APPLICANTDIV = '{$data_arr[$i]["APPLICANTDIV"]}' ";
            $where .= "   AND EXAMNO       = '{$data_arr[$i]["EXAMNO"]}' ";

            if (0 == $conf_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT", $where);
            }

            $db->query($query);

            /** ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT **/
            $query  = " DELETE FROM ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT ";
            $query .= "       WHERE ENTEXAMYEAR     = '{$model->year}' ";
            $query .= "         AND APPLICANTDIV    = '{$data_arr[$i]["APPLICANTDIV"]}' ";
            $query .= "         AND EXAMNO          = '{$data_arr[$i]["EXAMNO"]}' ";
            $query .= "         AND SEQ             = '001' ";
            $db->query($query);

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = "001";

            for ($k = 1; $k <= 12; $k++) {
                $data["REMARK".$k][TEXT]      = $data_arr[$i]["DET001_".$k];
            }

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    /**********************/
    /**  存在チェック用  **/
    /**********************/

    //名称マスタ登録コードチェック
    public function nameCntSql($model, $namecd, $setcd, $where = "")
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->year}' ";
        $query .= "     AND NAMECD1 = '{$namecd}' ";
        $query .= "     AND NAMECD2 = '{$setcd}' ";
        if ($where) {
            $query .= $where;
        }

        return $query;
    }

    //受験番号
    public function checkExamNo($model, $examNo)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND DISTINCT_ID  = '{$model->distinctId}' ";
        $query .= "     AND EXAMNO       = '{$examNo}' ";

        return $query;
    }
}
