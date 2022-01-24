<?php

require_once('for_php7.php');

class knjc161Model extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード

    /**
     * メンバ変数の初期化
     */
    function knjc161Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;  //職員コード
    }

    function init() {
        $this->field = array("SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND"),
                             "STRT_DATE"        =>  VARS::post("STRT_DATE"),
                             "DATE"             =>  VARS::post("DATE"),
                             "OUTPUT_SELECT"    =>  VARS::post("OUTPUT_SELECT"),
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
