<?php

require_once('for_php7.php');

class knje461Model extends Model {
    var $cmd;
    var $control;        //コントロールマスタ

    function knje461Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
    }

    function init() {
        $this->cmd      = VARS::request("cmd");

        $this->field = array("SCHOOL_KIND"            =>  VARS::post("SCHOOL_KIND"),            //学校種別
                             "HUKUSIKI_RADIO"         =>  VARS::post("HUKUSIKI_RADIO"),         //コンボ切替
                             "GHR_CD"                 =>  VARS::post("GHR_CD")                  //年組
                             );

        $this->getPropertiesAll(); //プロパティファイルの読込み

        $this->selectdata   = VARS::post("selectdata");

    }
}
?>
