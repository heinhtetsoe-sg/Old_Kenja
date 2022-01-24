<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjc150Model extends Model {
    var $field      = array();
    var $cmd;
	var $control;		//コントロールマスタ
	var $staffcd;		//職員コード

    /**
     * メンバ変数の初期化
     */
    function knjc150Model()
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
							"NENGETSU_FROM"		=>	VARS::post("NENGETSU_FROM"),	//印刷範囲（開始）
							"NENGETSU_TO"		=>	VARS::post("NENGETSU_TO"),	    //印刷範囲（終了）
							"OUTPUT1"			=>	VARS::post("OUTPUT1"),	    	//帳票選択（学年別）
							"OUTPUT2"			=>	VARS::post("OUTPUT2"),	    	//帳票選択（科目別）
							"DAYS"				=>	VARS::post("DAYS"),	    		//指定日数
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
