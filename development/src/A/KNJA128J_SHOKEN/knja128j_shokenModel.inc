<?php

require_once('for_php7.php');

class knja128j_shokenModel extends Model {
    var $cmd;
    var $schregno;
    var $name;
    var $field = array();
    var $record = array();
    var $control;

    function knja128j_shokenModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()  {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->schregno);
            $this->schregno     = VARS::get("SCHREGNO");
        }

        $this->field = array("YEAR_ANNUAL"          =>  VARS::post("YEAR_ANNUAL"),
                             "SPECIALACTREMARK"     =>  VARS::post("SPECIALACTREMARK")
                             );

        //行動の記録
        for ($i=1; $i<11; $i++) {
            $ival = "1" . sprintf("%02d", $i);
            $this->record["RECORD"][$ival] = VARS::post("RECORD".$ival);
        }

        //特別活動の記録
        for ($i=1; $i<5; $i++) {
            $ival = "2" . sprintf("%02d", $i);
            $this->record["RECORD"][$ival] = VARS::post("RECORD".$ival);
        }

        $this->cmd = VARS::request("cmd");

        if (VARS::get("EXP_YEAR") != "") {
            $this->exp_year     = VARS::get("EXP_YEAR");
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }
        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME") != "") {
            $this->name = VARS::get("NAME");
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();

        //特別活動の記録の観点
        if ($this->Properties["HTRAINREMARK_DAT_SPECIALACTREMARK_SIZE_J"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_SPECIALACTREMARK_SIZE_J"]);
            $this->specialactremark_moji = (int)trim($moji);
            $this->specialactremark_gyou = (int)trim($gyou);
        } else { 
            $this->specialactremark_moji = 14;
            $this->specialactremark_gyou = 7;
        }
    }
}
?>
