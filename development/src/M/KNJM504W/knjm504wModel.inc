<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjm504wModel extends Model {
    var $field = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $select_data = array();
    var $select_opt  = array();    //セレクトデータ
    var $selectleft;

    /**
     * メンバ変数の初期化
     */
    function knjm504wModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("GAKKI"             =>  VARS::post("GAKKI"),            //学期
                             "KIJUN"             =>  VARS::post("KIJUN"),           //集計基準日
                             "DBNAME"            =>  VARS::post("DBNAME"));         //DB変更に備えて

        $this->select_data["selectdata"] = explode(",", VARS::post("selectdata"));
        $this->selectleft = VARS::post("selectleft");

        $this->cmd         = VARS::request("cmd");

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
