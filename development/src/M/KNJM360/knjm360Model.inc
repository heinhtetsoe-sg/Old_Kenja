<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjm360Model extends Model {
    var $field      = array();
    var $cmd;
    var $semester;      //学期
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード

    /**
     * メンバ変数の初期化
     */
    function knjm360Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init()
    {
        $this->field = array("LASTDAY"      =>  VARS::post("LASTDAY"),      //受付経過期間
                             "OUTPUT1"      =>  VARS::post("OUTPUT1"),      //件数一覧
                             "OUTPUT2"      =>  VARS::post("OUTPUT2"),      //詳細リスト
                             "SUBCLASS"     =>  VARS::post("SUBCLASS"),     //SUBCLASS
                             "DBNAME"       =>  VARS::post("DBNAME")        //DB変更に備えて
                     );
        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイルの読込み
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
