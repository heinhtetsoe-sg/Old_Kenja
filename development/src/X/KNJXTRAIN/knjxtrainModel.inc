<?php

require_once('for_php7.php');

class knjxtrainModel extends Model {
    var $cmd;
    var $schregno;  //学籍番号
    var $year;      //年度
    var $buttonFlg;

    function knjxtrainModel() {}

    function init() {
        //プロパティファイルの読込み
        $this->getPropertiesAll();

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
