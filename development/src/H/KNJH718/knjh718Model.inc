<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjh718Model extends Model
{
    public $field = array();
    public $cmd;

    /**
     * メンバ変数の初期化
     */
    public function knjh718Model()
    {
    }
    public function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->field["TESTID"] = VARS::post("TESTID");
    }
    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
}
