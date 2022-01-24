<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjm503Model extends Model {
    var $field = array();
    var $cmd;
    var $control;        //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjm503Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("SEMESTER"          =>  VARS::post("SEMESTER"),        //学期
                             "GRADE_HR_CLASS"    =>  VARS::post("GRADE_HR_CLASS"),  //クラス
                             "ADDR_PRINT"        =>  VARS::post("ADDR_PRINT"),      //住所印字
                             "ADDR_DIV"          =>  VARS::post("ADDR_DIV"),        //住所区分
                             "SKIJUN"            =>  VARS::post("SKIJUN"),          //スクーリング提出基準日
                             "TKIJUN"            =>  VARS::post("TKIJUN"),          //特別活動集計基準日
                             "DBNAME"            =>  VARS::post("DBNAME"));         //DB変更に備えて

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
