<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjc166iModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjc166iModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("YEAR"                =>  VARS::post("YEAR"),             //年度
                            "GAKKI"                =>  VARS::post("GAKKI"),            //学期コード
                            "SCHOOL_KIND"          =>  VARS::post("SCHOOL_KIND"),      //校種
                            "GRADE"                =>  VARS::post("GRADE"),            //学年
                            "DATE"                 =>  VARS::post("DATE"),             //異動対象日付
//                            "OUTPUT_KAIKIN"        =>  VARS::post("OUTPUT_KAIKIN"),    //皆勤者/皆勤者/遅刻・早退
//                            "OUTPUT"               =>  VARS::post("OUTPUT"),           //皆勤者/精勤者/遅刻・早退
//                            "OUTPUT1_RUIKEI"       =>  VARS::post("OUTPUT1_RUIKEI"),   //皆勤者欠席
//                            "KESSEKI"              =>  VARS::post("KESSEKI"),          //欠席
//                            "CHIKOKU_SOUTAI"       =>  VARS::post("CHIKOKU_SOUTAI"),   //遅刻早退
//                            "CHIKOKU"              =>  VARS::post("CHIKOKU"),          //遅刻
//                            "SOUTAI"               =>  VARS::post("SOUTAI"),           //早退
//                            "KEKKA"                =>  VARS::post("KEKKA"),            //欠課
                            "DBNAME"               =>  VARS::post("DBNAME")            //DB変更に備えて
                     );


        $this->cmd         = VARS::request("cmd");
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
