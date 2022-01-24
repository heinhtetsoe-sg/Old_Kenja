<?php

require_once('for_php7.php');

class knjd183eModel extends Model
{
    public $field = array();
    public $cmd;
    public $control;//コントロールマスタ

    public function knjd183eModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->year = CTRL_YEAR;
        $this->field = array("CATEGORY_IS_CLASS" =>  VARS::post("CATEGORY_IS_CLASS"),//対象一覧区分
                             "GRADE_HR_CLASS"    =>  VARS::post("GRADE_HR_CLASS"),   //
                             "SEMESTER"          =>  VARS::post("SEMESTER"),         //学期
                             "GRADE"             =>  VARS::post("GRADE"),            //学年
                             "DATE"              =>  VARS::post("DATE"),             //講座日付
                             "DBNAME"            =>  VARS::post("DBNAME")            //DB変更に備えて
        );
        $this->cmd = VARS::request("cmd");
    }

    public function clean()
    {
    }
}
