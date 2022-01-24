<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjm020Model extends Model {
    var $field      = array();
    var $cmd;
	var $control;		//コントロールマスタ
	var $staffcd;		//職員コード

    /**
     * メンバ変数の初期化
     */
    function knjm020Model()
    {
		//コントロールマスタより情報を取得
		common::GetControlMaster_Fnc($this->control);
		$this->staffcd = STAFFCD;							//職員コード
    }

    function init()
    {
        $this->field = array("GRADE"		=>  VARS::post("GRADE"),			//学年
                             "BUSU"			=>  VARS::post("BUSU"),				//部数
                             "DBNAME"		=>  VARS::post("DBNAME"));			//DB変更に備えて



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
