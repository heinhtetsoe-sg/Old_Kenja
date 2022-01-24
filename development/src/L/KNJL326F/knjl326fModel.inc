<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl326fModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード

    /**
     * メンバ変数の初期化
     */
    function knjl326fModel()
    {
    }

    function init()
    {               
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear      = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV"	=>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"	    =>  VARS::post("TESTDIV"),     		//入試区分
                             "PRINT_DATE"	=>  VARS::post("PRINT_DATE"),       //通知日付
                             "TEISHUTSU_DATE"	=>  VARS::post("TEISHUTSU_DATE"),       //提出日付
                             "GOUKAKUTUUCHI_TELNO"	=>  VARS::post("GOUKAKUTUUCHI_TELNO"),       //連絡先
                             "GOUKAKUTUUCHI_NYUSHITANTOU"	=>  VARS::post("GOUKAKUTUUCHI_NYUSHITANTOU"),       //入試担当
                             "OUTPUT"	    =>  VARS::post("OUTPUT"),           //帳票種類
                             "OUTPUTA"	    =>  VARS::post("OUTPUTA"),          //出力範囲
                             "OUTPUTB"	    =>  VARS::post("OUTPUTB"),          //出力範囲
                             "OUTPUTC"	    =>  VARS::post("OUTPUTC"),          //出力範囲
                             "OUTPUTD"	    =>  VARS::post("OUTPUTD"),          //出力範囲
                             "OUTPUTE"	    =>  VARS::post("OUTPUTE"),          //出力範囲
                             "OUTPUTF"	    =>  VARS::post("OUTPUTF"),          //出力範囲
                             "OUTPUTG"	    =>  VARS::post("OUTPUTG"),          //出力範囲
                             "OUTPUTH"	    =>  VARS::post("OUTPUTH"),          //出力範囲
                             "EXAMNOA"	    =>  VARS::post("EXAMNOA"),          //受験番号
                             "EXAMNOB"	    =>  VARS::post("EXAMNOB"),          //受験番号
                             "EXAMNOC"	    =>  VARS::post("EXAMNOC"),          //受験番号
                             "EXAMNOD"	    =>  VARS::post("EXAMNOD"),          //受験番号
                             "EXAMNOE"	    =>  VARS::post("EXAMNOE"),          //受験番号
                             "EXAMNOF"	    =>  VARS::post("EXAMNOF"),          //受験番号
                             "EXAMNOG"	    =>  VARS::post("EXAMNOG"),          //受験番号
                             "EXAMNOH"	    =>  VARS::post("EXAMNOH")           //受験番号
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
