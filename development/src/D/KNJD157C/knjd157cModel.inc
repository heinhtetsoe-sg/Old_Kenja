<?php

require_once('for_php7.php');

class knjd157cModel extends Model
{
    public $field = array();
    public $cmd;
    public $subclassGroup;
    public $control;//コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    public function knjd157cModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array("YEAR"             => VARS::post("YEAR"),            //年度
                             "SEMESTER"         => VARS::post("SEMESTER"),        //学期コンボ
                             "GRADE"            => VARS::post("GRADE"),           //学年
                             "TESTCD"           => VARS::post("TESTCD"),          //テスト
                             "EXCELLENT_PERSON" => VARS::post("EXCELLENT_PERSON"),//優秀者
                             "UNSKILFUL_PERSON" => VARS::post("UNSKILFUL_PERSON"),//欠点数多数保持者
                             "DBNAME"           => VARS::post("DBNAME")           //DB変更に備えて
        );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        //初期値の設定
        $this->subclassGroup = (strlen($this->Properties["useSubclassGroup"])) ? $this->Properties["useSubclassGroup"] : 0;
    }
}
