<?php

require_once('for_php7.php');

class knja125j_shokenModel extends Model
{

    public $cmd;
    public $schregno;
    public $name;
    public $field = array();
    public $record = array();
    public $control;

    public function knja125j_shokenModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->schregno);
            $this->schregno     = VARS::get("SCHREGNO");
        }

        $this->field = array("TOTALSTUDYACT"        =>  VARS::post("TOTALSTUDYACT"),
                             "VIEWREMARK"           =>  VARS::post("VIEWREMARK"),
                             "TOTALSTUDYVAL"        =>  VARS::post("TOTALSTUDYVAL"),
                             "TOTALREMARK"          =>  VARS::post("TOTALREMARK"),
                             "ATTENDREC_REMARK"     =>  VARS::post("ATTENDREC_REMARK"),
                             "SPECIALACTREMARK"     =>  VARS::post("SPECIALACTREMARK"),
                             "CLASSACT"             =>  VARS::post("CLASSACT"),
                             "STUDENTACT"           =>  VARS::post("STUDENTACT"),
                             "SCHOOLEVENT"          =>  VARS::post("SCHOOLEVENT"),
                             "YEAR_ANNUAL"          =>  VARS::post("YEAR_ANNUAL"),
                             "UPDATED"              =>  VARS::post("UPDATED")
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

        if ($this->Properties["HTRAINREMARK_DAT_TOTALREMARK_SIZE_J"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_TOTALREMARK_SIZE_J"]);
            $this->totalremark_moji = (int)trim($moji);
            $this->totalremark_gyou = (int)trim($gyou);
        } else {
            $this->totalremark_moji = 44;
            $this->totalremark_gyou = 10;
        }

        if ($this->Properties["HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J"]);
            $this->attendrec_remark_moji = (int)trim($moji);
            $this->attendrec_remark_gyou = (int)trim($gyou);
        } else {
            $this->attendrec_remark_moji = 35;
            $this->attendrec_remark_gyou = 1;
        }
    }
}
