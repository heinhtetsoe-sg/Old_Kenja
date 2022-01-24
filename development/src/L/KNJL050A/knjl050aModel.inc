<?php

require_once('for_php7.php');

class knjl050aModel extends model {
    var $cmd;
    var $applicantdiv;
    var $testdiv;
    var $testsubclasscd;
    var $s_receptno;
    var $e_receptno;
    var $ObjYear;

    function knjl050aModel() {}

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->applicantdiv);
            unset($this->testdiv);
            unset($this->testsubclasscd);
            unset($this->s_receptno);
            unset($this->e_receptno);
        }

        $this->ObjYear      = CTRL_YEAR+1;
        $this->applicantdiv = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->exam_type    = "1";  //固定値

        $this->cmd = VARS::request("cmd");

        $this->testdiv          = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->testsubclasscd   = strlen(VARS::post("TESTSUBCLASSCD")) ? VARS::post("TESTSUBCLASSCD") : VARS::post("HID_TESTSUBCLASSCD");

        $this->s_receptno         = VARS::post("S_RECEPTNO");
        $this->e_receptno         = VARS::post("E_RECEPTNO");

        $this->hid_receptno       = VARS::post("HID_RECEPTNO");

        $this->score = array();
        $this->updFlg = array();
        $array = explode(",", $this->hid_receptno);
        foreach ($array as $key => $receptno) {
            $this->score[$receptno]     = VARS::post("SCORE_".$receptno);
            $this->updFlg[$receptno]    = VARS::post("UPD_FLG_".$receptno);
        }
        $this->examscore = array();
    }

    function &getMainModel() {}

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        //更新
        if ($this->testsubclasscd != "6") {
            knjl050aQuery::getUpdateScoreQuery($this);
        } else {
            $db = Query::dbCheckOut();
            $db->autoCommit(false);
            $query = knjl050aQuery::getInterviewDatQuery($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->updFlg[$row["RECEPTNO"]] !== "1") continue;
                if ($row["RCPT_EXAMNO"] != "") {
                    $this->examscore[$row["RCPT_EXAMNO"]] = $this->score[$row["RECEPTNO"]];
                }
            }
            Query::dbCheckIn($db);
            knjl050aQuery::getUpdateInterviewQuery($this);
        }
        $this->setMessage("MSG201","\\n全ての得点入力が完了したら\\n成績確定処理を実行して下さい。");
    }

    //CSV出力
    function getCsvModel() {
        //DB接続
        $db = Query::dbCheckOut();

        //初期化
        $contents = "";

        //ヘッダ
        $header = array();
        $header["ENTEXAMYEAR"]      = "入試年度";
        $header["TESTDIV"]          = "試験区分";
        $header["TESTSUBCLASSCD"]   = "入力教科";
        $header["RECEPTNO"]         = "受験番号";
        $header["SCORE"]            = "得点";
        $header["DUMMY"]            = "DUMMY";

        //ヘッダ出力
        $sep = "";
        foreach ($header as $field => $label) {
            $contents .= $sep.$label;
            $sep = ",";
        }
        $contents .= "\n";

        //データ出力
        $data = false;
        $query = knjl050aQuery::SelectQuery($this, "csv");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $sep = "";
            foreach ($header as $field => $label) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";
            $data = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        //CSV出力
        if ($data) {
            $title = $this->ObjYear."年度"."得点入力";
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>
