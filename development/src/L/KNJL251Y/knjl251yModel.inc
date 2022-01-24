<?php

require_once('for_php7.php');


class knjl251yModel extends model
{
    var $cmd;
    var $applicantdiv;
    var $appHold;
    var $testdiv;
    var $exam_type;
    var $examno;
    var $ObjYear;
    var $examhallcd;

    function knjl251yModel() {
    }

    function init() {
        $this->ObjYear     = CTRL_YEAR+1;
        
        $this->field = array("S_EXAMNO"     =>  VARS::post("S_EXAMNO"),         //受験番号開始
                             "E_EXAMNO"     =>  VARS::post("E_EXAMNO"),         //受験番号終わり
                             "OUTPUT"       =>  VARS::post("OUTPUT"),           //本人、保護者切替
                             "DBNAME"       =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd    = VARS::request("cmd");

        $this->applicantdiv   = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->appHold        = VARS::post("APP_HOLD");
        $this->testdiv        = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->exam_type      = "1";
        
        $this->hid_receptno = VARS::post("HID_RECEPTNO");
        
        //ラジオボタン切替時直前のコマンド保持(読込み、前へ、後ろへ)
        if ($this->cmd == "main" || $this->cmd == "") {
            $this->hoji_cmd = "";
        } else if ($this->cmd == "read" || $this->cmd == "back" || $this->cmd == "next") {
            $this->hoji_cmd = $this->cmd;
        }
    }

    function &getMainModel() {
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        //初期化
        $this->fields = array();
        for ($i = 0; $i < get_count($this->data["EXAMNO"]); $i++) {
            $remark = "INTERVIEW_REMARK"."-".$i;
            $this->fields["INTERVIEW_REMARK"][] = VARS::post($remark);
            
            //入力チェック
            if (strlen($this->fields["INTERVIEW_REMARK"][$i]) > 150 ) {
                $this->setWarning("MSG914","(受験番号：".$this->data["EXAMNO"][$i].")");
                return false;
            }
            //本人用
            if ($this->field["OUTPUT"] == "1") {
                $value  = "INTERVIEW_VALUE"."-".$i;
                $this->fields["INTERVIEW_VALUE"][] = VARS::post($value);
                //入力チェック
                if (strlen($this->fields["INTERVIEW_VALUE"][$i]) > 1 ) {
                    $this->setWarning("MSG914","評価(受験番号：".$this->data["EXAMNO"][$i].")");
                    return false;
                }
            }
        }
        
        $result = knjl251yQuery::getUpdateQuery($this);
        if ($result) {
            $this->setMessage("MSG201");
            $this->cmd = $this->hoji_cmd;
        }
    }
}
?>
