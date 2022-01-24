<?php

require_once('for_php7.php');

class knjd627dModel extends Model
{
    public $programID;
    public $cmd;
    public $eventFrom;  //ポスト発行元コントロール名
    public $year;
    public $grade;
    public $target_grade;
    public $retry_test_year;    //再試験対象年度（year、grade、target_gradeを基に算出）
    public $term;
    public $borderscore;
    public $selectedClasses;

    public function init()
    {
        $this->year              = CTRL_YEAR;
        $this->cmd               = VARS::request("cmd");
        $this->eventFrom         = VARS::request("HID_EVENT_FROM");
        $this->grade             = VARS::post("GRADE");
        $this->target_grade      = VARS::post("TARGET_GRADE");
        $this->target_schoolkind = VARS::post("HID_TARGET_SCHOOL_KIND");
        $this->term              = VARS::post("TERM");
        $this->borderscore       = VARS::post("BORDER_SCORE");
        $this->selectedClasses   = VARS::post("CLASS_SELECTED");

        //プロパティファイル読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //学校種別指定有無
    public function isIndicateSchoolKind()
    {
        return ($this->Properties["useSchool_KindField"] == "1" && $this->selectSchoolKind != "");
    }
}
