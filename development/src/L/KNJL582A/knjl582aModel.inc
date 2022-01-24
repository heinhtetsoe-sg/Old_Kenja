<?php

require_once('for_php7.php');

class knjl582aModel extends model {
    var $field = array();
    var $data  = array();
    var $cmd;
    var $applicantdiv;
    var $ObjYear;
    var $hid_examno;

    function knjl582aModel() {}

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->applicantdiv);
        }

        $this->ObjYear      = CTRL_YEAR+1;
        $this->applicantdiv = "1";  //固定値

        $this->cmd = VARS::request("cmd");

        $this->field = array("TESTDIV"      =>  VARS::post("TESTDIV"),      //受験区分
                             "DESIREDIV"    =>  VARS::post("DESIREDIV"),    //志望区分
                             "S_EXAMNO"     =>  VARS::post("S_EXAMNO"),     //受験番号 FROM
                             "E_EXAMNO"     =>  VARS::post("E_EXAMNO"),     //受験番号 TO
                             "SORT"         =>  VARS::post("SORT"),         //ソート順
                             "DIV"          =>  VARS::post("DIV")           //入力項目
                             );

        $this->hid_examno       = VARS::post("HID_EXAMNO");

        $array       = explode(",", $this->hid_examno);
        foreach ($array as $key => $examno) {
            $this->data["REMARK1"][$examno]         = strlen(VARS::post("HID_REMARK1_".$examno))      ? VARS::post("HID_REMARK1_".$examno)      : VARS::post("REMARK1_".$examno);
            $this->data["PROCEDUREDIV"][$examno]    = strlen(VARS::post("HID_PROCEDUREDIV_".$examno)) ? VARS::post("HID_PROCEDUREDIV_".$examno) : VARS::post("PROCEDUREDIV_".$examno);
            $this->data["ENTDIV"][$examno]          = strlen(VARS::post("HID_ENTDIV_".$examno))       ? VARS::post("HID_ENTDIV_".$examno)       : VARS::post("ENTDIV_".$examno);
        }

        //ラジオボタン切替時直前のコマンド保持(読込み、前へ、後ろへ)
        if ($this->cmd == "main" || $this->cmd == "") {
            $this->hoji_cmd = "";
        } else if ($this->cmd == "search" || $this->cmd == "back" || $this->cmd == "next") {
            $this->hoji_cmd = $this->cmd;
        }
    }

    function &getMainModel() {}

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        //更新
        knjl582aQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }
}
?>
