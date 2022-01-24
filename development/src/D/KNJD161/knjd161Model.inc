<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd161Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $testTable;

    /**
     * メンバ変数の初期化
     */
    function knjd161Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                  //職員コード
    }
    function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "TEST"             =>  VARS::post("TEST"),             //テスト名
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "DATE"             =>  VARS::post("DATE"),             //異動対象日付
                             "DBNAME"           =>  VARS::post("DBNAME"),           //DB変更に備えて
                            );

        $this->cmd   = VARS::request("cmd");

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
