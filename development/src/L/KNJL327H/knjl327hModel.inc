<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl327hModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl327hModel()
    {
    }

    function init()
    {               
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear      = CTRL_YEAR+1;

        $this->field = array("APDIV"     		=>  VARS::post("APDIV"),     		//入試制度
                             "TESTDV"           =>  VARS::post("TESTDV"),     		//入試区分
                             "TESTSCR"        	=>  VARS::post("TESTSCR"),     		//指定範囲
                             "NOTICEDAY"        =>  VARS::post("NOTICEDAY"),     	//通知日付
							 "OUTPUT"   		=>  VARS::post("OUTPUT"),    		//帳票種類
							 "OUTPUTA"   		=>  VARS::post("OUTPUTA"),    		//出力範囲A
							 "OUTPUTB"   		=>  VARS::post("OUTPUTB"),    		//出力範囲B
							 "OUTPUTC"   		=>  VARS::post("OUTPUTC"),    		//出力範囲C
							 "EXAMNOA"   		=>  VARS::post("EXAMNOA"),    		//受験番号A
							 "EXAMNOB"   		=>  VARS::post("EXAMNOB"),    		//受験番号B
							 "EXAMNOC"   		=>  VARS::post("EXAMNOC"),    		//受験番号C
                             "CONTACTDATE"      =>  VARS::post("CONTACTDATE"),     	//連絡日付
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
