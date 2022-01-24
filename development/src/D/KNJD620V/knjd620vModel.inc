<?php

require_once('for_php7.php');


class knjd620vModel extends Model
{
    public $field      = array();
    public $cmd;
    public $testTable;

    public function knjd620vModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array("YEAR"         =>  VARS::post("YEAR"),
                             "SEMESTER"     =>    VARS::post("SEMESTER"),
                             "SUBCLASSCD"   =>  VARS::post("SUBCLASSCD"),
                             "PRINT_DIV"    =>  VARS::post("PRINT_DIV"),
                             "PRINT_KEKKA0" =>  VARS::post("PRINT_KEKKA0"),
                             "PRINT_TANNIN" =>  VARS::post("PRINT_TANNIN"),
                             "DBNAME"       =>  VARS::post("DBNAME"),
                             "SDATE"        =>  VARS::post("SDATE"),
                             "DATE"         =>  VARS::post("DATE")
                     );

        $this->cmd         = VARS::request("cmd");
        $this->field["COURSE_MAJOR"] = VARS::request("COURSE_MAJOR"); //課程学科コンボ

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    public function clean()
    {
    }
}
