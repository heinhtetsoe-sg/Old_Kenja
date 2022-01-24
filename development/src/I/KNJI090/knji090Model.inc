<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knji090Model extends Model {
    var $field      = array();
    var $cmd;
	var $control;		//コントロールマスタ
	var $staffcd;		//職員コード

    /**
     * メンバ変数の初期化
     */
    function knji090Model()
    {
		//コントロールマスタより情報を取得
		common::GetControlMaster_Fnc($this->control);
		$this->staffcd = STAFFCD;							//職員コード
    }

    function init()
    {
        $this->field = array("YEAR"			   	=>  VARS::post("YEAR"),				//卒業年度
							"GRADE_HR_CLASS"   	=>  VARS::post("GRADE_HR_CLASS"),   //年組
							"PAGE"			   =>  VARS::post("PAGE"),				//ページ番号初期値
                            "DBNAME"          	=>  VARS::post("DBNAME")          	//DB変更に備えて
                     );


        $this->cmd         = VARS::request("cmd");

        //プログラムID
        $programid = VARS::get("PROGRAMID");
        if (isset($programid)){
            $this->programid = VARS::get("PROGRAMID");
        }

        //権限が変更された場合
        if ($this->staffcd != STAFFCD){
            $this->staffcd      = STAFFCD;                  //職員コード
        }

        //権限チェック
        $this->auth = common::SecurityCheck($this->staffcd, $this->programid);

    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
