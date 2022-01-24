<?php

require_once('for_php7.php');

class knjd184iModel extends Model
{
    public $field = array();
    public $control;
    public $cmd;

    public function knjd184iModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "DISP"             =>  VARS::post("DISP"),             //表示選択
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "PRINT_DATE"       =>  VARS::post("PRINT_DATE"),       //記載日付
                             "SDATE"            =>  VARS::post("SDATE"),            //集計開始日付
                             "DATE"             =>  VARS::post("DATE"),             //異動対象日付
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");
    }
}
