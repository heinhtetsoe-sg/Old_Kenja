<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl212cModel extends Model {
    var $cmd;
    var $ObjYear;
    var $field = array();

    /**
     * メンバ変数の初期化
     */
    function knjl212cModel()
    {
        $this->ObjYear = CTRL_YEAR+1;
        $this->cmd = VARS::request("cmd");

        $this->field["PRE_TESTDIV"] = VARS::post("PRE_TESTDIV");
        $this->field["RINKAN_HOUR"] = VARS::post("RINKAN_HOUR");
        $this->field["RINKAN_MINUTE"] = VARS::post("RINKAN_MINUTE");
        $this->field["FUKUJIN_HOUR"] = VARS::post("FUKUJIN_HOUR");
        $this->field["FUKUJIN_MINUTE"] = VARS::post("FUKUJIN_MINUTE");
        $this->field["GOJOU_HOUR"] = VARS::post("GOJOU_HOUR");
        $this->field["GOJOU_MINUTE"] = VARS::post("GOJOU_MINUTE");
        $this->field["GAKUEN_HOUR"] = VARS::post("GAKUEN_HOUR");
        $this->field["GAKUEN_MINUTE"] = VARS::post("GAKUEN_MINUTE");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
