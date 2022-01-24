<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd230tModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード
    var $testTable;

    /**
     * メンバ変数の初期化
     */
    function knjd230tModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;       //職員コード
    }

    function init()
    {               
        $this->field = array("GRADE"            =>  VARS::post("GRADE"),            //学年
                             "DATE"             =>  VARS::post("DATE"),             //出欠集計日付
                             "DBNAME"           =>  VARS::post("DBNAME"),           //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        //初期値の設定
        $this->testTable = (strlen($this->Properties["useTestCountflg"])) ? $this->Properties["useTestCountflg"] : "TESTITEM_MST_COUNTFLG_NEW";
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
