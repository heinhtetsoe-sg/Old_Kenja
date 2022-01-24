<?php
class knjl632fquery extends Query
{
    /******************/
    /**  画面で使用  **/
    /******************/

    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /************************/
    /**  ＣＳＶ取込で使用  **/
    /************************/

    //データカウント(ENTEXAM_APPLICANTCONFRPT_DAT)
    public function getCountConfrptdata($model, $examno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTCONFRPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' AND";
        $query .= "     APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' AND ";
        $query .= "     EXAMNO = '{$examno}' ";

        return $query;
    }

    //データカウント(ENTEXAM_APPLICANTBASE_DETAIL_DAT)
    public function getCountDetaildata($model, $examno, $seq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' AND";
        $query .= "     APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' AND ";
        $query .= "     EXAMNO = '{$examno}' AND ";
        $query .= "     SEQ = '{$seq}' ";

        return $query;
    }

    //願書の追加（ＣＳＶデータより読込）
    public function updateQueryCsv($model, &$data_arr)
    {
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < count($data_arr); $i++) {
            //データチェック
            $seq = '004';
            $getCount = $db->getOne(knjl632fQuery::getCountDetaildata($model, $data_arr[$i]["EXAMNO"], $seq));
            //ENTEXAM_APPLICANTBASE_DETAIL_DAT (SEQ=004)
            $data = array();
            if ($getCount == 0) {
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
                $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
                $data["SEQ"][TEXT]              = $seq;
            }
            $data["REMARK1"][TEXT]          = $data_arr[$i]["BUKATUDO"];
            $data["REMARK9"][TEXT]          = $data_arr[$i]["JIZENSODAN_CD"];
            $data["REMARK10"][TEXT]         = $data_arr[$i]["JIZENSODAN_TEXT"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            if ($getCount == 0) {
                   $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
                $where .= "       APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' AND ";
                $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
                $where .= "       SEQ           = '{$seq}' ";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            //データチェック
            $seq = '005';
            $getCount = $db->getOne(knjl632fQuery::getCountDetaildata($model, $data_arr[$i]["EXAMNO"], $seq));
            //ENTEXAM_APPLICANTBASE_DETAIL_DAT (SEQ=005)
            $data = array();
            if ($getCount == 0) {
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
                $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
                $data["SEQ"][TEXT]              = $seq;
            }
            $data["REMARK2"][TEXT]          = $data_arr[$i]["SHOUGAKU_CD"];
            $data["REMARK5"][TEXT]          = $data_arr[$i]["BIKOU"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            if ($getCount == 0) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
                $where .= "       APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' AND ";
                $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
                $where .= "       SEQ           = '{$seq}' ";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            $jituryokuGokei3  = $data_arr[$i]["JITURYOKU_3GOKEI"];
            $jituryokuHeikin3 = $data_arr[$i]["JITURYOKU_3HEIKIN"];
            $jituryokuGokei5  = $data_arr[$i]["JITURYOKU_5GOKEI"];
            $jituryokuHeikin5 = $data_arr[$i]["JITURYOKU_5HEIKIN"];

            //実力テスト3科得点率
            $tokutenritsu3 = 0;
            if ($jituryokuGokei3 > 0 && $jituryokuHeikin3 > 0) {
                $setTokutenRitsu = $jituryokuGokei3 / $jituryokuHeikin3 * 100;
                $tokutenritsu3 = round($setTokutenRitsu, 1);
            }
            //実力テスト5科得点率
            $tokutenritsu5 = 0;
            if ($jituryokuGokei5 > 0 && $jituryokuHeikin5 > 0) {
                $setTokutenRitsu = $jituryokuGokei5 / $jituryokuHeikin5 * 100;
                $tokutenritsu5 = round($setTokutenRitsu, 1);
            }

            //データチェック
            $seq = '020';
            $getCount = $db->getOne(knjl632fQuery::getCountDetaildata($model, $data_arr[$i]["EXAMNO"], $seq));
            //ENTEXAM_APPLICANTBASE_DETAIL_DAT (SEQ=020)
            $data = array();
            if ($getCount == 0) {
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
                $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
                $data["SEQ"][TEXT]              = $seq;
            }
            $data["REMARK1"][TEXT]          = $jituryokuGokei3;  //実力テスト3科合計
            $data["REMARK2"][TEXT]          = $jituryokuHeikin3; //実力テスト3科合計平均
            $data["REMARK3"][TEXT]          = $jituryokuGokei5;  //実力テスト5科合計
            $data["REMARK4"][TEXT]          = $jituryokuHeikin5; //実力テスト5科合計平均
            $data["REMARK5"][TEXT]          = $tokutenritsu3;    //実力テスト3科得点率
            $data["REMARK6"][TEXT]          = $tokutenritsu5;    //実力テスト5科得点率
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            if ($getCount == 0) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
                $where .= "       APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' AND ";
                $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
                $where .= "       SEQ           = '{$seq}' ";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            //全教科合計
            $totalAll = 0;
            for ($j = 0; $j < 9; $j++) {
                $num = sprintf("%02d", $j + 1);
                $totalAll += $data_arr[$i]["CONFIDENTIAL_RPT{$num}"];
            }
            //5教科合計
            $total5 = 0;
            $total5Kyoka = array("01", "02", "03", "04", "09");
            foreach ($total5Kyoka as $num) {
                $total5 += $data_arr[$i]["CONFIDENTIAL_RPT{$num}"];
            }

            //データチェック
            $getCount = $db->getOne(knjl632fQuery::getCountConfrptdata($model, $data_arr[$i]["EXAMNO"]));
            //ENTEXAM_APPLICANTCONFRPT_DAT
            $data = array();
            if ($getCount == 0) {
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
                $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            }
            $data["CONFIDENTIAL_RPT01"][NUMBER] = $data_arr[$i]["CONFIDENTIAL_RPT01"];
            $data["CONFIDENTIAL_RPT02"][NUMBER] = $data_arr[$i]["CONFIDENTIAL_RPT02"];
            $data["CONFIDENTIAL_RPT03"][NUMBER] = $data_arr[$i]["CONFIDENTIAL_RPT03"];
            $data["CONFIDENTIAL_RPT04"][NUMBER] = $data_arr[$i]["CONFIDENTIAL_RPT04"];
            $data["CONFIDENTIAL_RPT05"][NUMBER] = $data_arr[$i]["CONFIDENTIAL_RPT05"];
            $data["CONFIDENTIAL_RPT06"][NUMBER] = $data_arr[$i]["CONFIDENTIAL_RPT06"];
            $data["CONFIDENTIAL_RPT07"][NUMBER] = $data_arr[$i]["CONFIDENTIAL_RPT07"];
            $data["CONFIDENTIAL_RPT08"][NUMBER] = $data_arr[$i]["CONFIDENTIAL_RPT08"];
            $data["CONFIDENTIAL_RPT09"][NUMBER] = $data_arr[$i]["CONFIDENTIAL_RPT09"];
            $data["TOTAL5"][NUMBER]             = $total5;
            $data["TOTAL_ALL"][NUMBER]          = $totalAll;
            $data["REMARK1"][TEXT]              = $data_arr[$i]["TOKKI"]; //特記事項
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "SYSDATE()";

            if ($getCount == 0) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT");
            } else {
                $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
                $where .= "       APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' AND ";
                $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT", $where);
            }
            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    /**********************/
    /**  チェック用  **/
    /**********************/

    //受験番号
    public function checkExamno($model, $examno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR      = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND EXAMNO       = '{$examno}' ";

        return $query;
    }

    //かな氏名
    public function checkNameKana($model, $examno, $nameKana)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR      = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND EXAMNO       = '{$examno}' ";
        $query .= "     AND NAME_KANA    = '{$nameKana}' ";

        return $query;
    }

    //事前相談コード
    public function checkJizensodanCd($model, $code)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '{$model->ObjYear}' ";
        $query .= "     AND NAMECD1 = 'L032' ";
        $query .= "     AND NAMECD2 = '{$code}' ";

        return $query;
    }

    //奨学区分コード
    public function checkShougakuCd($model, $code)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '{$model->ObjYear}' ";
        $query .= "     AND NAMECD1 = 'L025' ";
        $query .= "     AND NAMESPARE1 = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND NAMECD2 = '{$code}' ";

        return $query;
    }

    /********************/
    /**  エラー処理用  **/
    /********************/

    //エラーデータの削除
    public function deleteQueryErr()
    {
        $query  = " DELETE FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ";
        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1 = array();
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $db->query($query);
    }

    //CVSエラー作成用のQUERY
    public function selectCsvErrQuery()
    {
        $query  = " SELECT ";
        $query .= "     MSGROW, ";
        $query .= "     MSGREMARK ";
        $query .= " FROM ";
        $query .= "     W_CSVMSG_PRG_DAT ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = '".PROGRAMID."' ";

        return $query;
    }

    //CVSエラー件数取得用のQUERY
    public function cntCsvErrQuery()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     W_CSVMSG_PRG_DAT ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = '".PROGRAMID."' ";

        return $query;
    }
}
?>
