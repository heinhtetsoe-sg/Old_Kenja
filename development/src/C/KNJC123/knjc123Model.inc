<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjc123Model extends Model {
    var $field    = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード

    /**
     * メンバ変数の初期化
     */
    function knjc123Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init()
    {
        $this->field = array("YEAR"                         =>  VARS::post("YEAR"),                         //年度
                            "GRADE"                         =>  VARS::post("GRADE"),                        //学年
                            "SYUKKETU_SHUBETU"              =>  VARS::post("SHUBETU"),                      //生徒リストラジオ
                            "SYUKKETU_SYUKKETU_TIKOKU"      =>  VARS::post("SYUKKETU_SYUKKETU_TIKOKU"),     //出欠状況/出欠状況/遅刻
                            "SYUKKETU_SYUKKETU_SOUTAI"      =>  VARS::post("SYUKKETU_SYUKKETU_SOUTAI"),     //出欠状況/出欠状況/早退
                            "SDATE"                         =>  VARS::post("SDATE"),                        //対象日(開始)
                            "EDATE"                         =>  VARS::post("EDATE"),                        //対象日(終了)
                     );


        $this->cmd       = VARS::request("cmd");
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
