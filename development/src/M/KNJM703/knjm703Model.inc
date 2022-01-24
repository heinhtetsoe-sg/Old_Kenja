<?php

require_once('for_php7.php');

class knjm703Model extends Model {
    var $cmd;
    var $control;        //コントロールマスタ
    var $field = array();

    function knjm703Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        if ($this->cmd == '') {
            unset($this->field);
        }

        $this->field = array("CLASSCD"      =>  VARS::post("CLASSCD"),      //教科
                             "SUBCLASSCD"   =>  VARS::post("SUBCLASSCD"),   //科目
                             "CHAIRCD"      =>  VARS::post("CHAIRCD"),      //講座
                             "ATTENDDATE"   =>  VARS::post("ATTENDDATE"),   //出席日付
                             "PERIODCD"     =>  VARS::post("PERIODCD")      //校時
                             );
        $this->getPropertiesAll(); //プロパティファイルの読込み

    }
}
?>
