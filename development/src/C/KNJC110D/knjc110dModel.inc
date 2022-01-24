<?php

require_once('for_php7.php');

class knjc110dModel extends Model
{
    public $field = array();
    public $cmd;
    public $control;   //コントロールマスタ
    public $testTable;

    public function knjc110dModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE_HRCLASS"    =>  VARS::post("GRADE_HRCLASS"),    //学年
                             "OUTPUT_DIV"       =>  VARS::post("OUTPUT_DIV"),       //帳票種別
                             "SMONTH"           =>  VARS::post("SMONTH"),           //対象月(開始)
                             "EMONTH"           =>  VARS::post("EMONTH"),           //対象月(終了)
                             "DATE"             =>  VARS::post("DATE"),             //学籍処理日
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        //初期値の設定
        if (strlen($this->Properties["useTestCountflg"])) {
            $this->testTable = $this->Properties["useTestCountflg"];
        } else {
            $this->testTable = "TESTITEM_MST_COUNTFLG_NEW";
        }
    }

    public function clean()
    {
    }
}
