<?php

require_once('for_php7.php');

class knjp375Model extends Model {
    var $field = array();
    var $cmd;
    var $control;

    function knjp375Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $this->cmd = VARS::request("cmd");

        $this->field = array("SCHOOL_KIND"  =>  VARS::post("SCHOOL_KIND"),
                             "STAFF"        =>  VARS::post("STAFF"),
                             "PRINT_DATE"   =>  VARS::post("PRINT_DATE")
                            );

        $this->getPropertiesAll();  //プロパティファイルの読込み
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
