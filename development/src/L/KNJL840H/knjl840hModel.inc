<?php

require_once('for_php7.php');

class knjl840hModel extends Model
{
    public $field = array();
    public $cmd;
    public $examyear;

    public function knjl840hModel()
    {
    }

    public function init()
    {
        $this->examyear      = CTRL_YEAR + 1;
        $this->cmd           = VARS::request("cmd");

        $this->field = array("APPLICANTDIV"       =>  VARS::post("APPLICANTDIV"),      //学校種別
                             "TESTDIV"            =>  VARS::post("TESTDIV"),           //入試区分
                             "JUDGMENT_DIV"       =>  VARS::post("JUDGMENT_DIV"),      //合否区分
                             "SEX"                =>  VARS::post("SEX"),               //性別
                             "DECISION"           =>  VARS::post("DECISION"),          //重複出願
                             "TOTAL_DIV"          =>  VARS::post("TOTAL_DIV"),         //集計区分
                             "SCORE_S"            =>  VARS::post("SCORE_S"),           //出力点範囲開始
                             "SCORE_E"            =>  VARS::post("SCORE_E"),           //出力点範囲終了
                             "SCORE_KIZAMI"       =>  VARS::post("SCORE_KIZAMI"),      //出力点範囲終了 きざみ点
                             "MUSHIMEGANE_S"      =>  VARS::post("MUSHIMEGANE_S"),     //虫眼鏡開始
                             "MUSHIMEGANE_E"      =>  VARS::post("MUSHIMEGANE_E"),     //虫眼鏡終了
                             "MUSHIMEGANE_KIZAMI" =>  VARS::post("MUSHIMEGANE_KIZAMI") //虫眼鏡 きざみ点
                            );
    }
}
