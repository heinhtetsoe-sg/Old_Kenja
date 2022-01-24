<?php

require_once('for_php7.php');

class knjh400_zyukoukamokuModel extends Model
{
    public $cmd;
    public $schregno;
    public $year;
    public $exp_year;
    public $semester;
    public $exp_semester;

    public function knjh400_zyukoukamokuModel()
    {
        common::GetControlMaster_Fnc($this->control_data);
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->schregno);
            unset($this->year);
            unset($this->exp_year);
            unset($this->semester);
            unset($this->exp_semester);
        }
        $this->cmd = VARS::request("cmd");

        $this->schregno = VARS::request("SCHREGNO") != "" ? VARS::request("SCHREGNO") : $this->schregno;

        $this->semester = VARS::request("SEMESTER") != "" ? VARS::request("SEMESTER") : $this->semester;
        $this->exp_semester = $this->semester;
        $this->yearGrade = VARS::GET("YEAR") != '' ? (VARS::GET("YEAR") . '-' . VARS::GET("GRADE")) : VARS::request("YEARGRADE");
        list($this->year, $this->grade) = explode('-', $this->yearGrade);
        $this->exp_year = $this->year;

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
}
