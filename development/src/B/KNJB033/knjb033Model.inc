<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjb033Model extends Model {
	var $field = array();
	var $cmd;
	var $control;   // コントロールマスタ
	var $staffcd;   // 職員コード
	var $kouji;     // 校時コード

    /**
     * コンストラクタ
     */
    function KNJB033Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;	// 職員コード
    }

    /**
     * 初期化
     */
    function init()
    {
        //プログラムID
        $programid = VARS::get("PROGRAMID");
        if (isset($programid)){
            $this->programid = VARS::get("PROGRAMID");
        }

        //講座コード
        $chaircd = VARS::get("chaircd");
        if (isset($chaircd)){
            $this->chaircd = VARS::get("chaircd");
        }

        //選択群コード
        $groupcd = VARS::get("groupcd");
        if (isset($groupcd)){
            $this->groupcd = VARS::get("groupcd");
        }

        //選択群名称
        $groupname = VARS::get("groupname");
        if (isset($groupname)){
            $this->groupname = VARS::get("groupname");
        }

        //権限が変更された場合
        if ($this->staffcd != STAFFCD){
            $this->staffcd      = STAFFCD;                  //職員コード
        }

        $this->cmd   = VARS::request("cmd");

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
