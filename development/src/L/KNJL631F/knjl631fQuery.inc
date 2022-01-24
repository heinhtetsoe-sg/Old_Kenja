<?php
class knjl631fQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     NAME2, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$year}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '{$namecd2}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    // 受験番号取得
    public function selectBaseDat($model, $nameKana)
    {
        $query  = " SELECT ";
        $query .= "     EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND NAME_KANA    = '{$nameKana}' ";
    
        return $query;
    }

    // かな氏名存在チェック、重複チェック
    public function checkNameKanaExists($model, $nameKana)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(EXAMNO) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND NAME_KANA    = '{$nameKana}' ";
    
        return $query;
    }

    /**************/
    /* ＣＳＶ取込 */
    /**************/
    //データの追加（ＣＳＶデータより読込）
    public function updateQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < count($data_arr); $i++) {
            $seq = $data_arr[$i]["PRE_CNT"] == "1" ? "038" : "039";
            $flg2ka = $flg3ka = false;
            $total2ka = $total3ka = 0;
            $cnt3ka = 0;
            if ($data_arr[$i]["PRE_KOKUGO"]) {
                $total2ka += $data_arr[$i]["PRE_KOKUGO"];
                $total3ka += $data_arr[$i]["PRE_KOKUGO"];
                $flg2ka = true;
                $flg3ka = true;
                $cnt3ka++;
            }
            if ($data_arr[$i]["PRE_SANSUU"]) {
                $total2ka += $data_arr[$i]["PRE_SANSUU"];
                $total3ka += $data_arr[$i]["PRE_SANSUU"];
                $flg2ka = true;
                $flg3ka = true;
                $cnt3ka++;
            }
            if ($data_arr[$i]["PRE_EIGO"]) {
                $total3ka += $data_arr[$i]["PRE_EIGO"];
                $flg3ka = true;
                $cnt3ka++;
            }

            //１件取得
            $query = knjl631fQuery::selectBaseDat($model, $data_arr[$i]["NAME_KANA"]);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);

            //プレテスト得点
            //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=038 or 039
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]  = $model->year;
            $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
            $data["EXAMNO"][TEXT]       = $Row["EXAMNO"];
            $data["SEQ"][TEXT]          = $seq;
            $data["REMARK1"][TEXT]      = $data_arr[$i]["PRE_KOKUGO"];
            $data["REMARK2"][TEXT]      = $data_arr[$i]["PRE_SANSUU"];
            $data["REMARK3"][TEXT]      = $data_arr[$i]["PRE_EIGO"];
            $data["REMARK4"][TEXT]      = ($flg2ka) ? $total2ka : "";
            $data["REMARK5"][TEXT]      = ($cnt3ka == 3) ? round($total3ka * 2 / 2.5) : "";
            $data["REMARK6"][TEXT]      = ($flg3ka) ? $total3ka : "";
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $where  = " WHERE ";
            $where .= "       ENTEXAMYEAR  = '{$model->year}' ";
            $where .= "   AND APPLICANTDIV = '{$model->applicantdiv}' ";
            $where .= "   AND EXAMNO       = '{$Row["EXAMNO"]}'";
            $where .= "   AND SEQ          = '{$seq}'";
            $dataCnt = $db->getOne(" SELECT COUNT(*) FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ".$where);
            if ($dataCnt == 0) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            $cnt++;
        }

        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //エラーデータの削除
    public function deleteQueryErr()
    {
        $query  = "DELETE FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = 'KNJL631F' ";
        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr($db, $record_no, $errMessage)
    {
        $data1["PROGRAMID"][TEXT]  = "KNJL631F";
        $data1["MSGROW"][NUMBER]   = $record_no;
        $data1["MSGREMARK"][TEXT]  = $errMessage;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //CVS作成用のQUERY
    public function selectCsvQuery()
    {
        $query  = "SELECT MSGROW, MSGREMARK FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = 'KNJL631F'";

        return $query;
    }
}
