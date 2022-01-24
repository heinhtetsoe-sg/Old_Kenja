<?php

require_once('for_php7.php');

class knjz418bModel extends Model
{
    public $field      = array();
    public $cmd;
    public $jobtype_lcd;
    public $jobtype_mcd;

    public function knjz418bModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array("JOBTYPE_LCD"        => VARS::post("JOBTYPE_LCD"),
                             "JOBTYPE_MCD"        => VARS::post("JOBTYPE_MCD"),
                             "JOBTYPE_SCD"        => VARS::post("JOBTYPE_SCD"),
                             "JOBTYPE_SSCD"       => VARS::post("JOBTYPE_SSCD"),
                             "JOBTYPE_L_MCD"      => VARS::post("JOBTYPE_L_MCD"),
                             "JOBTYPE_SNAME"      => VARS::post("JOBTYPE_SNAME"),
                             "JOBTYPE_SNAME_KANA" => VARS::post("JOBTYPE_SNAME_KANA")
        );

        $this->cmd = VARS::request("cmd");
        if (VARS::get("SEND_FLG") == "1") {
            $this->jobtype_lcd  = VARS::get("JOBTYPE_LCD");
            $this->jobtype_mcd  = VARS::get("JOBTYPE_MCD");
            $this->jobtype_scd  = VARS::get("JOBTYPE_SCD");
            $this->jobtype_sscd = VARS::get("JOBTYPE_SSCD");
        }
        if (VARS::request("cmd") == '') {
            unset($this->jobtype_lcd);
            unset($this->jobtype_mcd);
            unset($this->jobtype_scd);
            unset($this->jobtype_sscd);
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        if (trim($this->field["JOBTYPE_L_MCD"]) == "" ||
            trim($this->field["JOBTYPE_SCD"])   == "" ||
            trim($this->field["JOBTYPE_SSCD"])  == "") {
            $this->setWarning("MSG301");
            return false;
        }

        //頭ゼロ埋め
        $this->field["JOBTYPE_SCD"]  = $this->getZeroPaddingScd($this->field["JOBTYPE_SCD"], "S");
        $this->field["JOBTYPE_SSCD"] = $this->getZeroPaddingScd($this->field["JOBTYPE_SSCD"], "SS");

        //文字数チェック
        $checkItem = array();
        $checkItem["JOBTYPE_SNAME"]      = array(90,  "小分類名称");
        $checkItem["JOBTYPE_SNAME_KANA"] = array(120, "小分類名称かな");
        foreach ($checkItem as $key => $val) {
            list($moji, $label) = $val;
            if ($this->validate_row_cnt($this->field[$key], ((int)$moji * 2)) > 1) {
                $this->setWarning("MSG915", "({$label}) 全角{$moji}文字までです。");
                return false;
            }
        }

        return true;
    }

    /**********/
    /* UPDATE */
    /**********/
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            knjz418bQuery::getUpdateSQuery($this);
            list($this->jobtype_lcd, $this->jobtype_mcd) = explode("_", $this->field["JOBTYPE_L_MCD"]);
            $this->jobtype_scd = $this->field["JOBTYPE_SCD"];
            $this->jobtype_sscd = $this->field["JOBTYPE_SSCD"];
            $this->setMessage("MSG201");
        }
    }

    /**********/
    /* DELETE */
    /**********/
    public function &getDeleteModel()
    {
        $this->field["JOBTYPE_SCD"]  = $this->getZeroPaddingScd($this->field["JOBTYPE_SCD"], 'S');
        $this->field["JOBTYPE_SSCD"] = $this->getZeroPaddingScd($this->field["JOBTYPE_SSCD"], 'SS');
        if ($this->jobtype_scd != $this->field["JOBTYPE_SCD"]) {
            $this->setWarning("削除はリストよりデータを選択してから行ってください。");
            return false;
        }
        knjz418bQuery::getDeleteQuery($this);
        list($this->jobtype_lcd, $this->jobtype_mcd) = explode("_", $this->field["JOBTYPE_L_MCD"]);
        $this->jobtype_scd = $this->field["JOBTYPE_SCD"];
        $this->setMessage("MSG201");
    }

    //小・細分類コードのゼロ埋め処理
    public function getZeroPaddingScd($jobtypescd, $cmd)
    {
        if (strlen($jobtypescd) == 0) {
            return '';
        }

        //頭ゼロ埋め
        if (strlen($jobtypescd) <= 2 && $cmd == 'S') {
            $jobtypescd = sprintf("%03d", $jobtypescd);
        }
        //頭ゼロ埋め
        if (strlen($jobtypescd) <= 1 && $cmd == 'SS') {
            $jobtypescd = sprintf("%02d", $jobtypescd);
        }

        return $jobtypescd;
    }
}
