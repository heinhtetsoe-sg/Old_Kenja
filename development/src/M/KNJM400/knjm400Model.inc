<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjm400Model extends Model {
    var $field      = array();
    var $cmd;
	var $control;		//コントロールマスタ
	var $staffcd;		//職員コード

    /**
     * メンバ変数の初期化
     */
    function knjm400Model()
    {
		//コントロールマスタより情報を取得
		common::GetControlMaster_Fnc($this->control);
		$this->staffcd = STAFFCD;							//職員コード
    }

    function init()
    {
        $this->field = array("KINNAME"         => VARS::post("KINNAME"),        //スクーリング種別
                             "DATE"            => VARS::post("DATE"),           //日付
                             "CHRNAME"         => VARS::post("CHRNAME"),        //講座
                             "SCHLTIME"        => VARS::post("SCHLTIME"),       //校時コード
                             "DBNAME"          => VARS::post("DBNAME"),         //DB変更に備えて
                             "SELECT_SEMESTER" => VARS::post("SELECT_SEMESTER") //学期
                     );
                     
        $this->cmd         = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        
        $this->Properties["useTsushinSemesKonboHyoji"] = '1';
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
