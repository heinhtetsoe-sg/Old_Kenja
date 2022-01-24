<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjm330Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;
    var $staffcd;

    /**
     * メンバ変数の初期化
     */
    function knjm330Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                            "OUTPUT"            =>  VARS::post("OUTPUT"),           //出力タイプ
                            "HYOUSI"            =>  VARS::post("HYOUSI"),           //表紙
                            "OUTPUT1"           =>  VARS::post("OUTPUT1"),          //出力タイプ
                            "OUTPUT2"           =>  VARS::post("OUTPUT2"),          //出力タイプ
                            "KAMOKU"            =>  VARS::post("KAMOKU"),           //科目コード
                            "RKAISU"            =>  VARS::post("RKAISU"),           //レポート回数
                            "TKAISU"            =>  VARS::post("TKAISU"),           //提出回数
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
