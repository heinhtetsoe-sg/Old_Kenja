<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl255cModel extends Model {
    var $field      = array();
    var $cmd;
	var $control;		//コントロールマスタ
	var $staffcd;		//職員コード

    /**
     * メンバ変数の初期化
     */
    function knjl255cModel()
    {
		//コントロールマスタより情報を取得
		common::GetControlMaster_Fnc($this->control);
		$this->staffcd = STAFFCD;							//職員コード
    }

    function init()
    {
        $this->field = array("YEAR"				=>  VARS::post("YEAR"),		//年度
							 "POROW"   			=>  VARS::post("POROW"),    ////行
							 "POCOL"   			=>  VARS::post("POCOL"),    //列
							 "GAKKI"			=>	VARS::post("GAKKI"),	//学期コード
                             "FINSCHOOLDIV"     =>  VARS::post("FINSCHOOLDIV"), //区分
                             "DBNAME"           =>  VARS::post("DBNAME")    //DB変更に備えて
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
