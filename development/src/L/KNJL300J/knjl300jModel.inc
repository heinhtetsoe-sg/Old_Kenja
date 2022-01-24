<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl300jModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl300jModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear      = CTRL_YEAR+1;
    }
    function init()
    {               
        $this->field = array("YEAR"         =>  VARS::post("YEAR"),      //年度
                             "GAKKI"        =>  VARS::post("GAKKI"),     //学期
                             "TESTDV"       =>  VARS::post("TESTDV"),    //入試区分
                             "EXAM_TYPE"    =>  "1",                     //入試区分
                             "NOINF_ST"     =>  VARS::post("NOINF_ST"),  //受付番号開始
                             "NOINF_ED"     =>  VARS::post("NOINF_ED"),  //受付番号終了
                             "POROW"        =>  VARS::post("POROW"),     //行
                             "POCOL"        =>  VARS::post("POCOL")      //列
                     );

        $this->cmd         = VARS::request("cmd");
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
