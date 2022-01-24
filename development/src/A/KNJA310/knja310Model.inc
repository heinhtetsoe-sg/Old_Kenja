<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knja310Model extends Model {
    var $field      = array();
    var $cmd;
	var $control;		//コントロールマスタ
	var $staffcd;		//職員コード

    /**
     * メンバ変数の初期化
     */
    function knja310Model()
    {
		//コントロールマスタより情報を取得
		common::GetControlMaster_Fnc($this->control);
		$this->staffcd = STAFFCD;							//職員コード
    }

    function init()
    {
        $this->field = array("YEAR"				=>  VARS::post("YEAR"),		        //年度
							 "GAKKI"			=>	VARS::post("GAKKI"),	        //学期
							 "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
							 "BUSUU"			=>	VARS::post("BUSUU"),	        //各生徒の出力枚数
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
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
