<?php

require_once('for_php7.php');

class knjd185Model extends Model {
    var $semester;
    var $field = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $selectdata;

    function knjd185Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->semester = VARS::post("SEMESTER");
        $this->cmd      = VARS::request("cmd");

        if ($this->cmd == '') {
            unset($this->semester);
        }
    
        $this->field = array("DATE"             =>  VARS::post("DATE"),            //異動対象日付
                             "YEAR"             =>  VARS::post("YEAR"),            //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),        //学期コード
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),  //年組
                             "ADDR_PRINT"       =>  VARS::post("ADDR_PRINT"),      //住所印字
                             "ADDR_DIV"         =>  VARS::post("ADDR_DIV"),        //住所区分
                             "PRINT_KATEI"      =>  VARS::post("PRINT_KATEI"),     //家庭からの欄
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
