<?php

require_once('for_php7.php');

class knjd627eModel extends Model
{
    public $cmd;
    public $eventFrom;  //ポスト発行元コントロール名
    public $examyear;
    public $grade;
    public $printdate;
    public $term;
    public $selectedClasses;

    public function init()
    {
        $this->year             = CTRL_YEAR;
        $this->cmd              = VARS::request("cmd");
        $this->eventFrom        = VARS::request("HID_EVENT_FROM");
        $this->grade            = VARS::post("GRADE");
        $this->printdate        = VARS::post("PRINT_DATE");
        $this->term             = VARS::post("TERM");
        $this->selectedClasses  = VARS::post("CLASS_SELECTED");

        if (isset($this->printdate) == false) {
            $this->printdate = CTRL_DATE;
        }

        //プロパティファイル読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            //機能別に指定された校種を取得
            $this->getSelectSchoolKind();
        }
    }

    //学校種別指定有無
    public function isIndicateSchoolKind()
    {
        return ($this->Properties["useSchool_KindField"] == "1" && $this->selectSchoolKind != "");
    }
}
