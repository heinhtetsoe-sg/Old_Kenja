<?php

require_once('for_php7.php');

class knjl015gModel extends model {
    var $cmd;
    var $year;
    var $applicantdiv;
    var $testdiv;
    var $shdiv;
    var $examcoursecd;
    var $hope_club_cd;
    var $field = array();
    var $isKeiai;
    var $isKasiwara;

    function knjl015gModel() {}

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->year = CTRL_YEAR+1;
        $this->applicantdiv = VARS::post("APPLICANTDIV");
        $this->testdiv      = VARS::post("TESTDIV");
        $this->shdiv        = VARS::post("SHDIV");
        $this->examcoursecd = VARS::post("EXAMCOURSECD");
        $this->hope_club_cd = VARS::post("HOPE_CLUB_CD");

        $this->hid_examno   = VARS::post("HID_EXAMNO");

        //学校コードを取得　敬愛：30270254001、柏原：30270247001
        $rtnRow = knjl015gQuery::getNameMstZ010();
        $this->isKeiai      = $rtnRow["NAME2"] == "30270254001" ? "1" : "";
        $this->isKasiwara   = $rtnRow["NAME2"] == "30270247001" ? "1" : "";
    }

    function &getMainModel() {}

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if (!strlen($this->hid_examno)) {
            $this->setWarning("MSG303");
            return false;
        }

        return true;
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        if ($this->validateInput()) {
            $examnoList = array();
            if (strlen($this->hid_examno)) $examnoList = explode(",", $this->hid_examno);

            $this->field = array();
            foreach ($examnoList as $examno) {
                $this->field["CLUB"][$examno] = VARS::post("CLUB"."-".$examno);
                $this->field["RANK"][$examno] = VARS::post("RANK"."-".$examno);
            }

            //更新処理
            $result = knjl015gQuery::getUpdateQuery($this);
            if ($result) {
                $this->setMessage("MSG201");
            }
        }
    }
}
?>
