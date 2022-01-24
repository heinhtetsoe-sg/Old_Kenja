<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl341Model extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl341Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear = CTRL_YEAR+1;
    }
    function init()
    {               
        $this->field = array("APDIV"            =>  VARS::post("APDIV"),            //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試区分
                             "NOTICEDAY"        =>  VARS::post("NOTICEDAY"),        //通知日付
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //帳票種類
                             "OUTPUTA"          =>  VARS::post("OUTPUTA"),          //出力範囲A
                             "OUTPUTB"          =>  VARS::post("OUTPUTB"),          //出力範囲B
                             "OUTPUTC"          =>  VARS::post("OUTPUTC"),          //出力範囲C
                             "OUTPUTD"          =>  VARS::post("OUTPUTD"),          //出力範囲D
                             "EXAMNOA"          =>  VARS::post("EXAMNOA"),          //受験番号A
                             "EXAMNOB"          =>  VARS::post("EXAMNOB"),          //受験番号B
                             "EXAMNOC"          =>  VARS::post("EXAMNOC"),          //受験番号C
                             "EXAMNOD"          =>  VARS::post("EXAMNOD")           //受験番号D
                     );

        $this->cmd         = VARS::request("cmd");
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
