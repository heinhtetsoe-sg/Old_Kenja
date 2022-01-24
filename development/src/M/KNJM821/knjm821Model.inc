<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjm821Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjm821Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }
    function init()
    {               
        $this->field = array("OUTPUTSDATE"      =>  VARS::post("OUTPUTSDATE"),      //印刷範囲開始
                             "OUTPUTEDATE"      =>  VARS::post("OUTPUTEDATE"),      //印刷範囲終了
                             "SHUUKEI"          =>  VARS::post("SHUUKEI"),          //集計票
                             "MEISAI"           =>  VARS::post("MEISAI")            //明細票
                     );
        $this->cmd = VARS::request("cmd");
        
        if ($this->cmd == "") {
            $this->field["SHUUKEI"] = '1';
            $this->field["MEISAI"] = '1';
        }
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
