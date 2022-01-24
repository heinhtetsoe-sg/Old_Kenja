<?php

require_once('for_php7.php');

class knjm150Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;
    var $staffcd;

    /**
     * メンバ変数の初期化
     */
    function knjm150Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                            "OUTPUT"            =>  VARS::post("OUTPUT"),           //出力タイプ
                            "KAMOKU"            =>  VARS::post("KAMOKU"),           //科目コード
                            "DBNAME"            =>  VARS::post("DBNAME")            //DB変更に備えて
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
