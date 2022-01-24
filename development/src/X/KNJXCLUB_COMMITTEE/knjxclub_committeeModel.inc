<?php

require_once('for_php7.php');

class knjxclub_committeeModel extends Model {
    var $cmd;
    var $schregno;  //学籍番号
    var $year;      //年度
    var $hyoujiFlg;  //参照テーブル切替フラグ
    var $buttonFlg;

    function knjxclub_committeeModel() {}

    function init() {
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::get("SCHREGNO")!="") {
            unset($this->schregno);
            unset($this->year);
            unset($this->hyoujiFlg);
            unset($this->buttonFlg);
        }
        $this->schregno = VARS::request("SCHREGNO");
        $this->year     = VARS::request("YEAR");
        $this->hyoujiFlg = VARS::request("HYOUJI_FLG");
        $this->buttonFlg = VARS::request("BUTTON_FLG");
    }
}
?>
