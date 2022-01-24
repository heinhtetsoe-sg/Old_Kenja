<?php

require_once('for_php7.php');

class knjd666mModel extends Model
{
    public $field      = array();
    public $cmd;
    public $control;        //コントロールマスタ
    public $selectdata;
    public $selectdataSub;
    
    //美術は対象外
    public $art1 = "16-J-3-160100";
    public $art2 = "17-H-3-170400";
    public $art3 = "17-H-3-170500";
    public $art4 = "17-H-3-170600";

    public function knjd666mModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->semester = VARS::post("SEMESTER");
        $this->hrClass  = VARS::post("GRADE_HR_CLASS");
        $this->cmd      = VARS::request("cmd");

        if ($this->cmd == '') {
            unset($this->semester);
            unset($this->hrClass);
        }
    
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),              //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),          //学期コード
                             "SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND"),       //校種
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),    //年組
                             "TESTCD"           =>  VARS::post("TESTCD"),            //考査
                             "DBNAME"           =>  VARS::post("DBNAME")             //DB変更に備えて
                            );

        $this->cmd           = VARS::request("cmd");
        $this->selectdata    = VARS::post("selectdata");
        $this->selectdataSub = VARS::post("selectdataSub");

        if (!$this->field["SEMESTER"]) {
            $this->field["SEMESTER"] = CTRL_SEMESTER;
        }

        $this->getPropertiesAll(); //プロパティファイルの読込み
    }
}
