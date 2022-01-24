<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd322Model extends Model {
    var $field      = array();
    var $cmd;
	var $control;		//コントロールマスタ
	var $staffcd;		//職員コード

    /**
     * メンバ変数の初期化
     */
    function knjd322Model()
    {
		//コントロールマスタより情報を取得
		common::GetControlMaster_Fnc($this->control);
		$this->staffcd = STAFFCD;							//職員コード
    }

    function init()
    {
        $this->field = array("YEAR"				=>  VARS::post("YEAR"),		        //年度
							"GAKKI"				=>	VARS::post("GAKKI"),	        //学期コード
							"GRADE"				=>	VARS::post("GRADE"),	        //学年
//							"SUBCLASS"			=>	VARS::post("SUBCLASS"),	    	//科目数
//							"OUTPUT"			=>	VARS::post("OUTPUT"),	    	//帳票選択（1:以上,2:未満）
                            "DBNAME"            =>  VARS::post("DBNAME")            //DB変更に備えて
                     );


        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
