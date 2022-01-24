<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjm822dModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjm822dModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }
    function init()
    {               
        $this->field = array("SSCHREGNO"        =>  VARS::post("SSCHREGNO"),        //印刷範囲開始
                             "ESCHREGNO"        =>  VARS::post("ESCHREGNO"),        //印刷範囲終了
                             "YEAR"             =>  VARS::post("YEAR"),             //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "OUTPUT"           =>  VARS::post("OUTPUT")            //出力区分
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
