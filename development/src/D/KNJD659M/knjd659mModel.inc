<?php

require_once('for_php7.php');

class knjd659mModel extends Model {
    var $semester;
    var $hrClass;
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $selectdata;

    function knjd659mModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->semester = VARS::post("SEMESTER");
        $this->hrClass  = VARS::post("GRADE_HR_CLASS");
        $this->cmd      = VARS::request("cmd");

        if ($this->cmd == '') {
            unset($this->semester);
            unset($this->hrClass);
        }
    
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),              //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),          //学期コード
                             "GRADE"            =>  VARS::post("GRADE"),             //年
                             "DBNAME"           =>  VARS::post("DBNAME")             //DB変更に備えて
                            );

        $this->cmd          = VARS::request("cmd");
        $this->selectdata   = VARS::post("selectdata");

        if (!$this->field["SEMESTER"]) {
            $this->field["SEMESTER"] = CTRL_SEMESTER;
        }

        if (!$this->semester) {
            $this->semester = CTRL_SEMESTER;
        }

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>