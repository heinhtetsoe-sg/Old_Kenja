<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl327Model extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl327Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear = CTRL_YEAR+1;
    }
    function init()
    {               
        $this->field = array("APDIV"     		=>  VARS::post("APDIV"),     		//入試制度
                             "TESTDV"           =>  VARS::post("TESTDV"),     		//入試区分
                             "TESTSCR"        	=>  VARS::post("TESTSCR"),     		//指定範囲
                             "NOTICEDAY"        =>  VARS::post("NOTICEDAY"),     	//通知日付
							 "OUTPUT"   		=>  VARS::post("OUTPUT"),    		//帳票種類
							 "OUTPUTA"   		=>  VARS::post("OUTPUTA"),    		//出力範囲A
							 "OUTPUTB"   		=>  VARS::post("OUTPUTB"),    		//出力範囲B
							 "OUTPUTC"   		=>  VARS::post("OUTPUTC"),    		//出力範囲C
							 "OUTPUTD"   		=>  VARS::post("OUTPUTD"),    		//出力範囲D
							 "OUTPUTE"   		=>  VARS::post("OUTPUTE"),    		//出力範囲E
							 "EXAMNOA"   		=>  VARS::post("EXAMNOA"),    		//受験番号A
							 "EXAMNOB"   		=>  VARS::post("EXAMNOB"),    		//受験番号B
							 "EXAMNOC"   		=>  VARS::post("EXAMNOC"),    		//受験番号C
							 "EXAMNOD"   		=>  VARS::post("EXAMNOD"),    		//受験番号D
							 "EXAMNOE"   		=>  VARS::post("EXAMNOE"),    		//受験番号E
                             "DEADLINE"        	=>  VARS::post("DEADLINE"),     	//提出期限
                             "TIMEUPH"        	=>  VARS::post("TIMEUPH"),	     	//期限時
                             "TIMEUPM"        	=>  VARS::post("TIMEUPM"),  	   	//期限分
							 "CHKBOXA"   		=>  VARS::post("CHKBOXA"),    		//合格者のみA /* NO002 */
							 "CHKBOXB"   		=>  VARS::post("CHKBOXB"),    		//合格者のみB /* NO002 */
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
