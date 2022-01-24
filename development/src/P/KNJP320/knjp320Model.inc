<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjp320Model extends Model {
    var $field      = array();
    var $cmd;
	var $control;		//コントロールマスタ
	var $staffcd;		//職員コード

    /**
     * メンバ変数の初期化
     */
    function knjp320Model()
    {
		//コントロールマスタより情報を取得
		common::GetControlMaster_Fnc($this->control);
		$this->staffcd = STAFFCD;							//職員コード
    }

    function init()
    {
        $this->field = array("YEAR"				=>  VARS::post("YEAR"),		//年度
                            "GAKKI"         	=>  VARS::post("GAKKI"),   	//学期
                            "OUTPUTA"			=>  VARS::post("OUTPUTA"),  //出力種別Ａ
                            "OUTPUTB"			=>  VARS::post("OUTPUTB"),  //出力種別Ｂ
                            "HYOUSI"			=>  VARS::post("HYOUSI"),	//ヘッダ出力
                            "MEISAI"			=>  VARS::post("MEISAI"),	//明細出力
                            "OUTPUTC"			=>  VARS::post("OUTPUTC"),  //出力種別Ｃ
                            "DBNAME"        	=>  VARS::post("DBNAME")	//DB変更に備えて
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
