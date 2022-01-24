<?php
class knjl107iquery extends Query
{

    //名称マスタよりデータを取得
    public function get_name_cd($year, $namecd1, $namecd2="")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1, ";
        $query .= "     ABBV1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        if ($namecd2) {
            $query .= "     AND NAMECD2 = '{$namecd2}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試設定マスタ取得
    public function getEntSettingMst($year, $applicantDiv, $settingCd, $seq = "")
    {
        $query  = " SELECT ";
        $query .= "     SEQ VALUE, ";
        $query .= "     SEQ || ':' || NAME1 LABEL, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR         = '{$year}' ";
        $query .= "     AND APPLICANTDIV    = '{$applicantDiv}' ";
        $query .= "     AND SETTING_CD      = '{$settingCd}' ";
        if ($seq != "") {
            $query .= "     AND SEQ             = '{$seq}' ";
        }

        return $query;
    }

    //合格者チェック
    public function checkJudge($model, $data)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$data["ENTEXAMYEAR"]}' ";
        $query .= "     AND APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND EXAMNO          = '{$data["EXAMNO"]}' ";
        $query .= "     AND JUDGEMENT       = '1' "; // 1:合格

        return $query;
    }

    //データ取込
    public function insertQueryCsv($model, &$data_arr)
    {
        $data = array();
        
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        
        $cnt = 0;   //処理件数
        for ($i = 0; $i < count($data_arr); $i++) {
            //一次手続き　ENTEXAM_APPLICANTBASE_DETAIL_DAT(SEQ=022)
            if ($data_arr[$i]["PROCEDUREDIV1"] == "1") {
                //データ存在
                $query  = " SELECT ";
                $query .= "     COUNT(*) AS CNT ";
                $query .= " FROM ";
                $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
                $query .= " WHERE ";
                $query .= "         ENTEXAMYEAR     = '{$data_arr[$i]["ENTEXAMYEAR"]}' ";
                $query .= "     AND APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' ";
                $query .= "     AND EXAMNO          = '{$data_arr[$i]["EXAMNO"]}' ";
                $query .= "     AND SEQ             = '022' ";
                $dataCnt = $db->getOne($query);
                //データセット
                $data = array();
                $data["REMARK1"][TEXT]          = $data_arr[$i]["PROCEDUREDIV1"];
                $data["REMARK2"][TEXT]          = str_replace("/", "-", $data_arr[$i]["PROCEDUREDATE"]);
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "SYSDATE()";
                //データ更新
                if ($dataCnt > 0) {
                    $where  = " WHERE ";
                    $where .= "         ENTEXAMYEAR     = '{$data_arr[$i]["ENTEXAMYEAR"]}' ";
                    $where .= "     AND APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' ";
                    $where .= "     AND EXAMNO          = '{$data_arr[$i]["EXAMNO"]}' ";
                    $where .= "     AND SEQ             = '022' ";
                    $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
                } else {
                    $data["ENTEXAMYEAR"][TEXT]      = $data_arr[$i]["ENTEXAMYEAR"];
                    $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
                    $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
                    $data["SEQ"][TEXT]              = "022";
                    $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
                }
                $db->query($query);
                $cnt++;
            //二次手続き　ENTEXAM_APPLICANTBASE_DAT
            } elseif ($data_arr[$i]["PROCEDUREDIV2"] == "1") {
                //データセット
                $data = array();
                $data["PROCEDUREDIV"][TEXT]     = $data_arr[$i]["PROCEDUREDIV2"];
                $data["PROCEDUREDATE"][TEXT]    = str_replace("/", "-", $data_arr[$i]["PROCEDUREDATE"]);
                $data["ENTDIV"][TEXT]           = "1"; // 「1:入学」
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "SYSDATE()";
                //データ更新
                $where  = " WHERE ";
                $where .= "         ENTEXAMYEAR     = '{$data_arr[$i]["ENTEXAMYEAR"]}' ";
                $where .= "     AND APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' ";
                $where .= "     AND EXAMNO          = '{$data_arr[$i]["EXAMNO"]}' ";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
                $db->query($query);
                $cnt++;
            }
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

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
        $data1["PROGRAMID"][TEXT]       = PROGRAMID;
        $data1["MSGROW"][NUMBER]        = $record_no;
        $data1["MSGREMARK"][TEXT]       = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //CVSエラー作成用のQUERY
    public function selectCsvErrQuery()
    {
        $query  = " SELECT MSGROW, MSGREMARK FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ";

        return $query;
    }
}
