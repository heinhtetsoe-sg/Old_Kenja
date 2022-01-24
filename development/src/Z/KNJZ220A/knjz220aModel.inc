<?php

require_once('for_php7.php');

class knjz220aModel extends Model {

    var $field = array();
    var $data  = array();
    var $cmd;

    /**
     * メンバ変数の初期化
     */
    function knjz220aModel()
    {
    }

    function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }
        
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array(
                            "SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND"),    //校種
                            "CLASSCD"          =>  VARS::post("CLASSCD"),        //教科
                            "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),     //科目
                            "DBNAME"           =>  VARS::post("DBNAME")          //DB変更に備えて
                            );
        //算出比率
        $this->rateList = array(); //データが入力されたテキストボックスのフォーム名
        if ($this->nameList) {
            foreach ($this->nameList as $name) {
                if (strlen(trim(VARS::post($name))) > 0) {
                    $this->rateList[$name] = VARS::post($name);
                }
            }
        }

        $this->cmd     = VARS::request("cmd");
    }

    //入力項目チェック
    function validateInput()
    {
        //必須入力チェック
        return true;
    }

    function &getUpdateModel()
    {
        if ($this->validateInput()) {
            knjz220aQuery::update($this);
            $this->setMessage("MSG201");
        }
    }
}
//var_dump($_REQUEST);
?>
