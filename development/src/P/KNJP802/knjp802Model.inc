<?php

require_once('for_php7.php');

class knjp802Model extends Model
{
    public $field      = array();
    public $cmd;
    public $control;        //コントロールマスタ
    public $selectStudent;
    public $selectStudentLabel;

    /**
     * メンバ変数の初期化
     */
    public function knjp802Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),              //年度
                             "GAKKI"            =>  VARS::post("GAKKI"),             //学期
                             "GRADE"            =>  VARS::post("GRADE"),             //学年
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),    //年組
                             "OUTPUT"           =>  VARS::post("OUTPUT"),            //発行日
                             "DBNAME"           =>  VARS::post("DBNAME")             //DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
}
?>

