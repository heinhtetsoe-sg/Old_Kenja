<?php

require_once('for_php7.php');

class knjz418Model extends Model {
    var $field      = array();
    var $cmd;
    var $jobtype_lcd;
    var $jobtype_mcd;

    function knjz418Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("JOBTYPE_LCD"        =>  VARS::post("JOBTYPE_LCD"),
                             "JOBTYPE_MCD"        =>  VARS::post("JOBTYPE_MCD"),
                             "JOBTYPE_SCD"        =>  VARS::post("JOBTYPE_SCD"),
                             "JOBTYPE_L_MCD"      =>  VARS::post("JOBTYPE_L_MCD"),
                             "JOBTYPE_SNAME"      =>  VARS::post("JOBTYPE_SNAME"),
                             "JOBTYPE_SNAME_KANA" =>  VARS::post("JOBTYPE_SNAME_KANA")
                             );

        $this->cmd = VARS::request("cmd");
        if (VARS::get("SEND_FLG") == "1") {
            $this->jobtype_lcd = VARS::get("JOBTYPE_LCD");
            $this->jobtype_mcd = VARS::get("JOBTYPE_MCD");
            $this->jobtype_scd = VARS::get("JOBTYPE_SCD");
        }
        if (VARS::request("cmd") == '') {
            unset($this->jobtype_lcd);
            unset($this->jobtype_mcd);
            unset($this->jobtype_scd);
        }
    }

    //入力項目チェック
    function validateInput() {
        if (trim($this->field["JOBTYPE_L_MCD"]) == "" ||
            trim($this->field["JOBTYPE_SCD"]) == ""
            ) {
            $this->setWarning("MSG301");
            return false;
        }
        //頭ゼロ埋め
        if (strlen($this->field["JOBTYPE_SCD"]) <= 2) {
            $this->field["JOBTYPE_SCD"] = sprintf("%03d", $this->field["JOBTYPE_SCD"]);
        }
        //文字数チェック
        $checkItem = array();
        $checkItem["JOBTYPE_SNAME"]         = array(50,     "小分類名称");
        $checkItem["JOBTYPE_SNAME_KANA"]    = array(100,    "小分類名称かな");
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
            knjz418Query::getUpdateSQuery($this);
            list($this->jobtype_lcd, $this->jobtype_mcd) = explode("_", $this->field["JOBTYPE_L_MCD"]);
            $this->jobtype_scd = $this->field["JOBTYPE_SCD"];
            $this->setMessage("MSG201");
        }
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteModel() {
        $this->field["JOBTYPE_SCD"] = sprintf("%03d", $this->field["JOBTYPE_SCD"]);
        if ($this->jobtype_scd != $this->field["JOBTYPE_SCD"]) {
            $this->setWarning("削除はリストよりデータを選択してから行ってください。");
            return false;
        }
        knjz418Query::getDeleteQuery($this);
        list($this->jobtype_lcd, $this->jobtype_mcd) = explode("_", $this->field["JOBTYPE_L_MCD"]);
        $this->jobtype_scd = $this->field["JOBTYPE_SCD"];
        $this->setMessage("MSG201");
    }
}
?>
