<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjm809Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $select_opt = array();  //セレクトデータ
    var $selectleft;
    var $selectleftval;

    /**
     * メンバ変数の初期化
     */
    function knjm809Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
    }

    function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),            //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),        //学期
                             "OUTPUT"           =>  VARS::post("OUTPUT")           //出力
                             );

        $this->cmd         = VARS::request("cmd");
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
