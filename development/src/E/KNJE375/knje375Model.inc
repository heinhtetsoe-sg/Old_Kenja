<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knje375Model extends Model {
    var $field = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード

    /**
     * メンバ変数の初期化
     */
    function knje375Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;   //職員コード
    }

    function init() {
        $this->field = array("YEAR"         =>  VARS::post("YEAR"),         //年度
                             "SEMESTER"     =>  VARS::post("SEMESTER"),     //学期
                             "SCHOOL_KIND"  =>  VARS::post("SCHOOL_KIND"),  //校種
                             "MIN_YEAR"     =>  VARS::post("MIN_YEAR"),     //MIN年度
                             "MAX_YEAR"     =>  VARS::post("MAX_YEAR"),     //MAX年度
                             "DBNAME"       =>  VARS::post("DBNAME")        //DB変更に備えて
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
    function clean() {
    }
}
?>
