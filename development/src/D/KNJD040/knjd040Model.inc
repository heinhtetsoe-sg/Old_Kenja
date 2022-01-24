<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd040Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード

    /**
     * メンバ変数の初期化
     */
    function knjd040Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init()
    {
        $this->field = array("YEAR"         =>  VARS::post("YEAR"),     //年度
                             "GAKKI"        =>  VARS::post("GAKKI"),    //学期コード
                             "TEST"         =>  VARS::post("TEST"),     //テスト
                             "OUTPUT"       =>  VARS::post("OUTPUT"),   //帳票出力指定 /* NO003 */
                             "DBNAME"       =>  VARS::post("DBNAME")    //DB変更に備えて
                     );


        $this->cmd         = VARS::request("cmd");
        //プロパティファイル
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