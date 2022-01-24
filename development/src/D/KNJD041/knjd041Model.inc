<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd041Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード
    var $testTable;     //テスト種別テーブル

    /**
     * メンバ変数の初期化
     */
    function knjd041Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init()
    {
        $this->field = array("YEAR"       => VARS::post("YEAR"),       //年度
                             "GAKKI"      => VARS::post("GAKKI"),      //学期コード
                             "TEST"       => VARS::post("TEST"),       //テスト
                             "RECORD_DIV" => VARS::post("RECORD_DIV"), //素点・評価のラジオボタン
                             "OUTPUT"     => VARS::post("OUTPUT"),     //帳票出力指定 /* NO003 */
                             "DBNAME"     => VARS::post("DBNAME")      //DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
        //初期値の設定
        if (strlen($this->Properties["useTestCountflg"])) {
            $this->testTable = $this->Properties["useTestCountflg"];
        } else {
            $this->testTable = "TESTITEM_MST_COUNTFLG_NEW";
        }
    }
}
?>
