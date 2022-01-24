<?php

require_once('for_php7.php');

class knjz417Model extends Model {
    var $field      = array();
    var $cmd;
    var $jobtype_lcd;
    var $jobtype_mcd;

    function knjz417Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("JOBTYPE_LCD"        =>  VARS::post("JOBTYPE_LCD"),
                             "JOBTYPE_MCD"        =>  VARS::post("JOBTYPE_MCD"),
                             "JOBTYPE_LNAME"      =>  VARS::post("JOBTYPE_LNAME"),
                             "JOBTYPE_MNAME"      =>  VARS::post("JOBTYPE_MNAME"),
                             "JOBTYPE_LNAME_KANA" =>  VARS::post("JOBTYPE_LNAME_KANA"),
                             "JOBTYPE_MNAME_KANA" =>  VARS::post("JOBTYPE_MNAME_KANA")
                             );

        $this->cmd = VARS::request("cmd");
        if (VARS::get("SEND_FLG") == "1") {
            $this->jobtype_lcd = VARS::get("JOBTYPE_LCD");
            $this->jobtype_mcd = VARS::get("JOBTYPE_MCD");
        }
        if (VARS::request("cmd") == '') {
            unset($this->jobtype_lcd);
            unset($this->jobtype_mcd);
        }
    }

    //入力項目チェック
    function validateInput() {
        if (trim($this->field["JOBTYPE_LCD"]) == "" ||
            trim($this->field["JOBTYPE_MCD"]) == ""
            ) {
            $this->setWarning("MSG301");
            return false;
        }

        //頭ゼロ埋め
        if (strlen($this->field["JOBTYPE_MCD"]) <= 2) {
            $this->field["JOBTYPE_MCD"] = sprintf("%02d", $this->field["JOBTYPE_MCD"]);
        }
        //文字数チェック
        $checkItem = array();
        $checkItem["JOBTYPE_LNAME"]         = array(50,     "大分類名称");
        $checkItem["JOBTYPE_LNAME_KANA"]    = array(100,    "大分類名称かな");
        $checkItem["JOBTYPE_MNAME"]         = array(50,     "中分類名称");
        $checkItem["JOBTYPE_MNAME_KANA"]    = array(100,    "中分類名称かな");
        foreach ($checkItem as $key => $val) {
            list($moji, $label) = $val;
            if ($this->validate_row_cnt($this->field[$key], ((int)$moji * 2)) > 1) {
                $this->setWarning("MSG915","({$label}) 全角{$moji}文字までです。");
                return false;
            }
        }

        return true;
    }

    /**********/
    /* UPDATE */
    /**********/
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()){
            knjz417Query::getUpdateLQuery($this);
            knjz417Query::getUpdateMQuery($this);
            $this->jobtype_lcd = $this->field["JOBTYPE_LCD"];
            $this->jobtype_mcd = $this->field["JOBTYPE_MCD"];
            $this->setMessage("MSG201");
        }
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteModel() {
        knjz417Query::getDeleteQuery($this);
        unset($this->jobtype_lcd);
        unset($this->jobtype_mcd);
        $this->setMessage("MSG201");
    }
}
?>
