<?php

require_once('for_php7.php');
// kanji=漢字
// $Id: knjb0050Model.inc 56585 2017-10-22 12:47:53Z maeshiro $
/**
 * 定数定義
*/

class knjb0050Model extends Model {
	var $field = array();
	var $cmd;
	var $control;   // コントロールマスタ
	var $staffcd;   // 職員コード
	var $kouji;     // 校時コード

    /**
     * コンストラクタ
     */
    function KNJB0050Model()
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
		/* ----------↓---------2005.02.23 add by nakamoto----------↓--------- */
        //年度
        $year = VARS::get("year");
        if (isset($year)){
            $this->year = VARS::get("year");
        }
        //学期
        $semester = VARS::get("semester");
        if (isset($semester)){
            $this->semester = VARS::get("semester");
        }
        //職員コード
        $staffcd = VARS::get("staffcd");
        if (isset($staffcd)){
            $this->staffcd = VARS::get("staffcd");
        }
		/* ----------↑---------2005.02.23 add by nakamoto----------↑--------- */

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
