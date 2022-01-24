<?php
// kanji=漢字
// $Id: knjb3040oModel.inc,v 1.5 2012/07/13 06:07:24 maesiro Exp $
class knjb3040oModel extends Model {
	var $field = array();
	var $cmd;
	var $control;   // コントロールマスタ
	var $staffcd;   // 職員コード
	var $kouji;     // 校時コード
	var $PROFICIENCY_COUNTFLG_DATADIV;     // 実力テストのカウントフラグデータ区分
	var $useCurriculumcd;                  // 教育課程コードを使用するか
    

    /**
     * コンストラクタ
     */
    function knjb3040oModel()
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
        $useCurriculumcd = $this->Properties["useCurriculumcd"];
        if ('1' == $useCurriculumcd) {
            $this->useCurriculumcd = "true";
        } else {
            $this->useCurriculumcd = "false";
        }
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
