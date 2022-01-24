<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl242gModel extends Model {
    var $cmd;
    var $year;
    var $applicantdiv;

    /**
     * メンバ変数の初期化
     */
    function knjl242gModel() {
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR + 1;
        $this->applicantdiv = VARS::post("APPLICANTDIV");
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
