<?php

require_once('for_php7.php');

class knjl326bModel extends Model
{
    public $field      = array();
    public $ObjYear;
    public $cmd;
    public $control;   //コントロールマスタ
        
    public function knjl326bModel()
    {
    }

    public function init()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->ObjYear      = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV"),          //入試区分
                             "TSUCHI_DATE"  =>  VARS::post("TSUCHI_DATE"),      //通知日付
                             "SPECIAL_REASON_DIV"  =>  VARS::post("SPECIAL_REASON_DIV"),      //特別措置者のみ
                             "OUTPUT"       =>  VARS::post("OUTPUT"),           //出力範囲
                             "SCHOOLCD"     =>  VARS::post("SCHOOLCD"),         //出身学校コード
                             "DBNAME"       =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
    }
}
