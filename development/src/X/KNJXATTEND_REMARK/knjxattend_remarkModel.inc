<?php

require_once('for_php7.php');

class knjxattend_remarkModel extends Model {
    var $cmd;
    var $schregno;  //学籍番号
    var $year;      //年度
    var $sdate;     //開始日付
    var $edate;     //終了日付
    var $semesflg;  //参照テーブル切替フラグ

    function knjxattend_remarkModel() {}

    function init() {
        $this->schregno  = VARS::request("SCHREGNO");
        $this->year      = VARS::request("YEAR");
        $this->sdate     = VARS::request("SDATE");
        $this->edate     = VARS::request("EDATE");
        $this->semesflg  = VARS::request("SEMESFLG");
        $this->warekiFlg = VARS::request("WAREKIFLG");
    }
}
?>
