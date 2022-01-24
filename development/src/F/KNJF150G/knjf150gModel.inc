<?php

require_once('for_php7.php');

class knjf150gModel extends Model
{
    public $cmd;
    public $year;
    public $semester;
    public $startDate;
    public $endDate;
    public $chairCd;

    public function knjf150gModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")!="") {
            unset($this->grade);
            unset($this->hrClass);
            unset($this->startDate);
            unset($this->data);
        }

        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR;
        $this->semester = CTRL_SEMESTER;

        if (VARS::request("GRADE") != '') {
            $this->grade = VARS::request("GRADE");
        }
        if (VARS::request("HR_CLASS") != '') {
            $this->hrClass = VARS::request("HR_CLASS");
        }
        if (VARS::request("START_DATE") != '') {
            $this->startDate = VARS::request("START_DATE");
        }
        if (VARS::get("PROGRAMID")!="") {
            $this->endDate = CTRL_DATE;
        } else {
            if (VARS::request("END_DATE") != '') {
                $this->endDate = VARS::request("END_DATE");
            }
        }
        $this->startDate = str_replace('-', '/', $this->startDate);
        $this->endDate = str_replace('-', '/', $this->endDate);

        if ($this->startDate != '') {
            $list = explode('/', $this->startDate);
            if (get_count($list) != 3) {
                $this->setWarning("開始日付が不正です");
            } elseif (strlen($list[0]) != 4 || strlen($list[1]) != 2 || strlen($list[2]) != 2) {
                $this->setWarning("開始日付が不正です2");
            }
        }
        if ($this->endDate != '') {
            $list = explode('/', $this->endDate);
            if (get_count($list) != 3) {
                $this->setWarning("終了日付が不正です");
            } elseif (strlen($list[0]) != 4 || strlen($list[1]) != 2 || strlen($list[2]) != 2) {
                $this->setWarning("終了日付が不正です2");
            }
        }
        if ($this->startDate != '' && $this->endDate != '' && $this->endDate<$this->startDate) {
            $this->setWarning("終了日付より開始日付の方が大きいです");
        }
    }
}
