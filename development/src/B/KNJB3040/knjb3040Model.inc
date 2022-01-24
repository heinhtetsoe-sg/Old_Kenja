<?php

require_once('for_php7.php');
// kanji=漢字
// $Id: knjb3040Model.inc 56585 2017-10-22 12:47:53Z maeshiro $
class knjb3040Model extends Model {
	var $field = array();
	var $cmd;
	var $control;   // コントロールマスタ
	var $staffcd;   // 職員コード
	var $kouji;     // 校時コード
	var $PROFICIENCY_COUNTFLG_DATADIV;     // 実力テストのカウントフラグデータ区分
    

    /**
     * コンストラクタ
     */
    function knjb3040Model()
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

        //権限が変更された場合
        if ($this->staffcd != STAFFCD){
            $this->staffcd      = STAFFCD;                  //職員コード
        }
        $this->cmd   = VARS::request("cmd");

        //権限チェック
        $this->auth = common::SecurityCheck($this->staffcd, $this->programid);

        $this->getPropertiesAll();
        $div = $this->Properties["PROFICIENCY_COUNTFLG_DATADIV"];
        if ('3' != $div && '4' != $div) {
            $div = '3';
        }
        $this->PROFICIENCY_COUNTFLG_DATADIV = $div;
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
