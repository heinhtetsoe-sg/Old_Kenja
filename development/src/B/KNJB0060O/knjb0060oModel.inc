<?php
// kanji=漢字
// $Id: knjb0060oModel.inc,v 1.2 2012/08/01 01:13:35 maesiro Exp $
class knjb0060oModel extends Model {
	var $field = array();
	var $cmd;
	var $control;   // コントロールマスタ
	var $staffcd;   // 職員コード
	var $kouji;     // 校時コード

    /**
     * コンストラクタ
     */
    function knjb0060oModel()
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
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
