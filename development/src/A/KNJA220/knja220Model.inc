<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knja220Model extends Model {
    var $field      = array();
    var $cmd;
	var $control;		//コントロールマスタ
	var $staffcd;		//職員コード

    /**
     * メンバ変数の初期化
     */
    function knja220Model()
    {
		//コントロールマスタより情報を取得
		common::GetControlMaster_Fnc($this->control);
		$this->staffcd = STAFFCD;							//職員コード
    }

    function init()
    {
        $this->field = array("YEAR"				=>  VARS::post("YEAR"),		        //年度
							"OUTPUT"			=>	VARS::post("OUTPUT"),		    //名票
							"OUTPUT2"			=>	VARS::post("OUTPUT2"),		    //空行を詰めて印字 NO001
							"KENSUU"			=>	VARS::post("KENSUU"),		    //出力件数
							"GAKKI"				=>	VARS::post("GAKKI"),	        //学期コード
                            "DBNAME"            =>  VARS::post("DBNAME")            //DB変更に備えて
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
