<?php

require_once('for_php7.php');

class knjxschregModel extends Model {
    var $cmd;
    var $schregno;  //学籍番号
    var $year;      //年度
    var $buttonFlg;

    function knjxschregModel() {}

    function init() {
        common::GetControlMaster_Fnc($this->control_data);
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        $this->finschoolcdKeta = ($this->Properties["useFinschoolcdFieldSize"] == '12' ? 12 : 7);

        if (VARS::get("SCHREGNO")!="") {
            unset($this->schregno);
            unset($this->year);
            unset($this->buttonFlg);
        }
        $this->schregno = VARS::request("SCHREGNO");
        $this->year     = VARS::request("YEAR");
        $this->buttonFlg = VARS::request("BUTTON_FLG");
    }
}
?>
