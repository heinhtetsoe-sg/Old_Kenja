<?php

require_once('for_php7.php');

class knjl415mModel extends Model
{
    public $cmd;
    public $examyear;
    public $printdate;
    public $examSchoolKind;

    public function init()
    {
        $this->examyear         = CTRL_YEAR + 1;
        $this->cmd              = VARS::request("cmd");
        $this->examSchoolKind   = VARS::post("EXAM_SCHOOL_KIND");
        $this->printdate        = VARS::post("PRINT_DATE");

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
}
