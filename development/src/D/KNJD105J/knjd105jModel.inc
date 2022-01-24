<?php

require_once('for_php7.php');

class knjd105jModel extends Model {
    var $semester;
    var $grade;
    var $hrClass;
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $selectdata;

    function knjd105jModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->semester = VARS::post("SEMESTER");
        $this->grade    = VARS::post("GRADE");
        $this->hrClass  = VARS::post("HR_CLASS");
        $this->cmd      = VARS::request("cmd");

        if ($this->cmd == '') {
            unset($this->semester);
            unset($this->grade);
            unset($this->hrClass);
        }
    
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),           //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),       //学期コード
                             "GRADE"            =>  VARS::post("GRADE"),          //年組
                             "HR_CLASS"         =>  VARS::post("HR_CLASS"),       //学級
                             "DATE"             =>  VARS::post("DATE"),           //異動対象日付
                             "PRINT_TUISHI"     =>  VARS::post("PRINT_TUISHI"),   //追試出力
                             "DBNAME"           =>  VARS::post("DBNAME")          //DB変更に備えて
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
