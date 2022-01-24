<?php

require_once('for_php7.php');

class knjc161gModel extends Model
{
    public $field = array();
    public $cmd;
    public $selectdata;
    public $selectdataText;
    public $sDate;
    public $eDate;

    public function knjc161gModel()
    {
    }

    public function init()
    {
        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期コード
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "DISP"             =>  VARS::post("DISP"),             //クラス生徒区分
                             "SDATE"            =>  VARS::post("SDATE"),            //開始日付
                             "EDATE"            =>  VARS::post("EDATE"),            //終了日付
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd          = VARS::request("cmd");
        $this->selectdata       = VARS::post("selectdata");
        $this->selectdataText   = VARS::post("selectdataText");

        //プロパティファイルの読み込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if ($this->cmd == "change") {
            $this->selectdata = "";
        }
        if ($this->cmd == "semester") {
            $this->field["SDATE"]  = "";
            $this->field["EDATE"]  = "";
        }
    }
}
