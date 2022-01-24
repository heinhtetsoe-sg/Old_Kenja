<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjm130Model extends Model {
    var $field      = array();
    var $cmd;
    var $semester;		//学期
	var $control;		//コントロールマスタ
	var $staffcd;		//職員コード

    /**
     * メンバ変数の初期化
     */
    function knjm130Model()
    {
		//コントロールマスタより情報を取得
		common::GetControlMaster_Fnc($this->control);
		$this->staffcd = STAFFCD;							//職員コード
    }

    function init()
    {
        $this->field = array("YEAR"			=>	VARS::post("YEAR"),		//日付
							 "CHAIR"		=>	VARS::post("CHAIR"),	//CHAIR
                             "DBNAME"		=>  VARS::post("DBNAME")	//DB変更に備えて
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