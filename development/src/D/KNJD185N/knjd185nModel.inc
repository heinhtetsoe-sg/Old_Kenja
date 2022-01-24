<?php

require_once('for_php7.php');

class knjd185nModel extends Model {
    var $semester;
    var $field = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $selectdata;

    function knjd185nModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->semester = VARS::post("SEMESTER");
        $this->cmd      = VARS::request("cmd");

        if ($this->cmd == '') {
            unset($this->semester);
        }
    
        $this->field = array("DATE"             =>  VARS::post("DATE"),            //異動対象日付
                             "DESC_DATE"        =>  VARS::post("DESC_DATE"),       //記載日付
                             "YEAR"             =>  VARS::post("YEAR"),            //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),        //学期コード
                             "TESTKINDCD"       =>  VARS::post("TESTKINDCD"),      //テスト種別コード
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),  //年組
                             "GROUP_DIV"        =>  VARS::post("GROUP_DIV"),       //グループ区分
                             "RANK_DIV"         =>  VARS::post("RANK_DIV"),        //順位の基準点
                             "DBNAME"           =>  VARS::post("DBNAME")           //DB変更に備えて
                            );

        $this->cmd          = VARS::request("cmd");
        $this->selectdata   = VARS::post("selectdata");

        if (!$this->field["SEMESTER"]) {
            $this->field["SEMESTER"] = CTRL_SEMESTER;
        }

        if (!$this->semester) {
            $this->semester = CTRL_SEMESTER;
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }
}
?>
