<?php

require_once('for_php7.php');

class knjd178Model extends Model {
    var $semester;
    var $hrClass;
    var $selectdata;
    var $selectdata2;
    var $cmd;
    var $control;        //コントロールマスタ
    function knjd178Model() {}

    function init() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);

        $this->semester    = VARS::post("SEMESTER");
        $this->hrClass     = VARS::post("GRADE_HRCLASS");

        $this->selectdata  = VARS::post("selectdata");    //対象者一覧
        $this->selectdata2 = VARS::post("selectdata2");   //生徒一覧

        $this->field["IDO_DATE"]      = VARS::post("IDO_DATE");
        $this->field["OUT_PUT_KEKKA"] = VARS::post("OUT_PUT_KEKKA");

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
    }
}
?>
