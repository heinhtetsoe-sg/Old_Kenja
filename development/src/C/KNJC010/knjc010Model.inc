<?php

require_once('for_php7.php');
// kanji=漢字
// $Id: knjc010Model.inc 56585 2017-10-22 12:47:53Z maeshiro $
class knjc010Model extends Model {
	var $field = array();
	var $cmd;
	var $control;   // コントロールマスタ
	var $staffcd;   // 職員コード
	var $kouji;     // 校時コード
	var $syoribi;	// 処理日
	var $chaircd;	// 講座コード '03.11.25 add
	var $grade;		// 学年 '04.08.22 add
	var $hrclass;	// 組 	'04.08.22 add

    /**
     * コンストラクタ
     */
    function knjc010Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    /**
     * 初期化
     */
    function init()
    {
		$this->cmd   = VARS::request("cmd");

		// 職員コード
		if( VARS::request("STAFFCD")=="" ){
			$this->staffcd = STAFFCD;
		} else{
			$this->staffcd = VARS::request("STAFFCD");
		}

		// 校時コード
		if( VARS::request("periodcd")=="" ){
			$this->kouji = -1;
		} else{
			$this->kouji = VARS::request("periodcd");
		}

        $this->syoribi = VARS::request("syoribi");
	    $this->chaircd = VARS::request("chaircd");
	    $this->grade = VARS::request("grade");
	    $this->hrclass = VARS::request("hrclass");
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
