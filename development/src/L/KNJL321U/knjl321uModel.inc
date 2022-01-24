<?php

require_once('for_php7.php');

class knjl321uModel extends Model {
    var $field = array();
    var $cmd;
    var $ObjYear;

    function knjl321uModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試区分
                             "NOTICEDATE"       =>  VARS::post("NOTICEDATE"),       //通知日付
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //帳票種類
                             "OUTPUTA"          =>  VARS::post("OUTPUTA"),          //出力範囲
                             "OUTPUTB"          =>  VARS::post("OUTPUTB"),          //出力範囲
                             "OUTPUTC"          =>  VARS::post("OUTPUTC"),          //出力範囲
                             "EXAMNOA"          =>  VARS::post("EXAMNOA"),          //受験番号
                             "EXAMNOB"          =>  VARS::post("EXAMNOB"),          //受験番号
                             "EXAMNOC"          =>  VARS::post("EXAMNOC")           //受験番号
                     );

        $this->cmd = VARS::request("cmd");
    }
}
?>
