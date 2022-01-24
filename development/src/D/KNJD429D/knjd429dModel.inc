<?php

require_once('for_php7.php');

class knjd429dModel extends Model {
    var $cmd;
    var $control;        //コントロールマスタ
    var $optright;

    function knjd429dModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
    }

    function init() {
        $this->cmd      = VARS::request("cmd");

        $this->field = array("SEMESTER"               =>  VARS::post("SEMESTER"),               //学期
                             "SCHOOL_KIND"            =>  VARS::post("SCHOOL_KIND"),            //学校種別
                             "HUKUSIKI_RADIO"         =>  VARS::post("HUKUSIKI_RADIO"),         //コンボ切替
                             "MOVE_DATE"              =>  VARS::post("MOVE_DATE"),              //異動対象日付
                             "GHR_CD"                 =>  VARS::post("GHR_CD"),                 //年組
                             "PRINT_PATTERN"          =>  VARS::post("PRINT_PATTERN"),          //帳票パターン
                             "PRINT_SIDE1"            =>  VARS::post("PRINT_SIDE1"),            //表示・裏表紙
                             "PRINT_SIDE2"            =>  VARS::post("PRINT_SIDE2"),            //学習の記録
                             "PRINT_SIDE3"            =>  VARS::post("PRINT_SIDE3"),            //出欠の記録
                             "OUTPUT1"                =>  VARS::post("OUTPUT1"),                //修了証書
                             );

        $this->getPropertiesAll(); //プロパティファイルの読込み

        $this->optright = array();
        $this->selectdata   = VARS::post("selectdata");

    }
}
?>
