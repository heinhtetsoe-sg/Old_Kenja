<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjh131Model extends Model {
    var $field = array();
    var $cmd;
    var $control;        //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjh131Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("YEAR"     => VARS::post("YEAR"),      //年度
                             "SEMESTER" => VARS::post("SEMESTER"),  //学期
                             "OUTPUTA"  => VARS::post("OUTPUTA"),   //出力対象
                             "OUTPUTB"  => VARS::post("OUTPUTB")    //出力帳票
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
