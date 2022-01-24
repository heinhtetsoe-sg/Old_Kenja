<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd090Model extends Model {
    var $field      = array();
    var $cmd;
	var $control;		//コントロールマスタ
	var $staffcd;		//職員コード

    /**
     * メンバ変数の初期化
     */
    function knjd090Model()
    {
		//コントロールマスタより情報を取得
		common::GetControlMaster_Fnc($this->control);
		$this->staffcd = STAFFCD;							//職員コード
    }

    function init()
    {
        $this->field = array("YEAR"				=>  VARS::post("YEAR"),		        //年度
							 "GAKKI"			=>	VARS::post("GAKKI"),	        //学期コード
							 "OUT2"				=>	VARS::post("OUT2"),		        //出力形式
							 "STUD"				=>	VARS::post("STUD"),		        //出力生徒区分
							 "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "NUMBER"           =>  VARS::post("NUMBER"),           //席次
							 "POINT"			=>  VARS::post("POINT"),		    //評定点数
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
