<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knjg052Model.inc 58564 2018-02-14 06:05:25Z maeshiro $

class knjg052Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;
    var $output;

    function knjg052Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                            "CMBCLASS"          =>  VARS::post("CMBCLASS"),         //学年・クラス
                            "NOTICEDAY"         =>  VARS::post("NOTICEDAY"),        //記載日付
                            "GRADUATE_DATE"     =>  VARS::post("GRADUATE_DATE"),    //卒業年月
                            "GAKKI"             =>  VARS::post("GAKKI"),            //学期コード
                            "KIND"              =>  VARS::post("KIND"),             //印刷種別
                            "POINT"             =>  VARS::post("POINT"),            //句点あり
                            "PRINT_STAMP"       =>  VARS::post("PRINT_STAMP"),      //印影出力する
                            "DBNAME"            =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");
        $this->output      = VARS::post("OUTPUT");

        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    function clean()
    {
    }
}
?>
