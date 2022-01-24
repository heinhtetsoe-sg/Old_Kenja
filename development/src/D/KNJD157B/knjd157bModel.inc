<?php

require_once('for_php7.php');

class knjd157bModel extends Model
{
    public $field = array();
    public $cmd;
    public $subclassGroup;
    public $control;//コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    public function knjd157bModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array("YEAR"              => VARS::post("YEAR"),             //年度
                             "SEMESTER"          => VARS::post("SEMESTER"),         //学期コンボ
                             "GRADE"             => VARS::post("GRADE"),            //学年
                             "HR_CLASS"          => VARS::post("HR_CLASS"),         //クラス
                             "CATEGORY_IS_CLASS" => VARS::post("CATEGORY_IS_CLASS"),//表示区分
                             "type_course"       => VARS::post("type_course"),      //順位・類型ラジオボタン
                             "DBNAME"            => VARS::post("DBNAME")            //DB変更に備えて
        );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        //初期値の設定
        $this->subclassGroup = (strlen($this->Properties["useSubclassGroup"])) ? $this->Properties["useSubclassGroup"] : 0;
    }

    public function clean()
    {
    }
}
