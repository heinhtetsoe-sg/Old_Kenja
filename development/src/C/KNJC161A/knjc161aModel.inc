<?php

require_once('for_php7.php');

class knjc161aModel extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjc161aModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("OUTPUT_SELECT"    =>  VARS::post("OUTPUT_SELECT"),
                             "SETSEMESTER"      =>  VARS::post("SETSEMESTER"),
                             "SMONTH"           =>  VARS::post("SMONTH"),
                             "EMONTH"           =>  VARS::post("EMONTH"),
                             "SICK"             =>  VARS::post("SICK"),
                             "CHOREIKETSU"      =>  VARS::post("CHOREIKETSU"),
                             "DBNAME"           =>  VARS::post("DBNAME")
                             );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
    /**
     * 変数をクリアします
     */
    function clean() {}
}
?>
