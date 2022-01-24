<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knje090Model extends Model {
    var $field      = array();
    var $cmd;
	var $control;		//コントロールマスタ
	var $staffcd;		//職員コード

    /**
     * メンバ変数の初期化
     */
    function knje090Model()
    {
		//コントロールマスタより情報を取得
		common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
		$this->staffcd = STAFFCD;							//職員コード
    }

    function init()
    {
        $this->field = array("YEAR"				=>  VARS::post("YEAR"),				//年度
							 "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
							 "GAKKI"			=>	VARS::post("GAKKI"),			//学期コード
                             "DBNAME"           =>  VARS::post("DBNAME")          	//DB変更に備えて
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
