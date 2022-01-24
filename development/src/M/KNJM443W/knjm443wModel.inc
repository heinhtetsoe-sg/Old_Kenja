<?php

require_once('for_php7.php');


/**
 * 定数定義
*/

class knjm443wModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード

    /**
     * メンバ変数の初期化
     */
    function knjm443wModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //職員コード
    }

    function init()
    {
        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),     //学期
                             "TESTCD"           =>  VARS::post("TESTCD"),       //テスト種別
                             "TEST_COUNT"       =>  VARS::post("TEST_COUNT"),   //回数
                             "SUBCLASS"         =>  VARS::post("SUBCLASS"),     //科目
                             "DBNAME"           =>  VARS::post("DBNAME")        //DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");

        $this->getPropertiesAll();  //プロパティファイルの読込み
    }
    
}
?>

