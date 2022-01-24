<?php

require_once('for_php7.php');


class knjl515aModel extends model
{
    var $cmd;
    var $applicantdiv;
    var $testdiv;
    var $hid_examno;
    var $examyear;
    var $hope_coursecode;

    var $CONST_SELALL;

    function knjl515aModel() {
    }

    function init() {
        $this->CONST_SELALL = "99999";

        $this->examyear     = CTRL_YEAR+1;
        $this->applicantdiv = "1";
        $this->field = array("S_EXAMNO"     =>  VARS::post("S_EXAMNO"),         //受験番号開始
                             "E_EXAMNO"     =>  VARS::post("E_EXAMNO"),         //受験番号終わり
                             "OUTPUT"       =>  VARS::post("OUTPUT"),           //本人、保護者切替
                             "DBNAME"       =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd    = VARS::request("cmd");

        if ($this->cmd == "") {
            unset($this->hope_coursecode);
        }

        $this->testdiv         = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->hope_coursecode = strlen(VARS::post("HOPE_COURSECODE")) ? VARS::post("HOPE_COURSECODE") : VARS::post("HID_HOPE_COURSECODE");

        $this->hid_examno = VARS::post("HID_EXAMNO");
        if ($this->hid_examno != "") {
            $this->data = explode(",", $this->hid_examno);
            $this->fields = array();
            for ($i = 0; $i < get_count($this->data); $i++) {
                for ($colcnt = 1;$colcnt <= 9;$colcnt++) {
                    $baseidx = $this->data[$i]."_".$colcnt;
                    $this->fields[$baseidx] = VARS::post($baseidx);
                }
                $baseidx = $this->data[$i]."_TOTAL5";
                if (VARS::post("HID_".$baseidx)) {
                    $this->fields[$baseidx] = VARS::post("HID_".$baseidx);
                } else {
                    $this->fields[$baseidx] = 0;
                }
                $baseidx = $this->data[$i]."_TOTAL_ALL";
                if (VARS::post("HID_".$baseidx)) {
                    $this->fields[$baseidx] = VARS::post("HID_".$baseidx);
                } else {
                    $this->fields[$baseidx] = 0;
                }
            }
        }
        //ラジオボタン切替時直前のコマンド保持(読込み、前へ、後ろへ)
        if ($this->cmd == "main" || $this->cmd == "") {
            $this->hoji_cmd = "";
        } else if ($this->cmd == "read" || $this->cmd == "back" || $this->cmd == "next") {
            $this->hoji_cmd = $this->cmd;
        }
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        if ($this->hid_examno == "") {
            $this->setMessage("MSG303");
            return;
        }
        foreach ($this->fields as $key => $val) {
            if($val != ""){
                if (!preg_match('/^[0-9]+$/', $val)) {
                    $this->setWarning("MSG907","(内申点：".$val.")");
                    return;
                }
            }
        }

        $result = knjl515aQuery::getUpdateQuery($this);
        if ($result) {
            $this->setMessage("MSG201");
            $this->cmd = $this->hoji_cmd;
        }
    }
}
?>
