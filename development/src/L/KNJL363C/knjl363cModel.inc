<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl363cModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjl363cModel()
    {
        common::GetControlMaster_Fnc($this->control);
        $this->ObjYear      = CTRL_YEAR+1;
    }

    function init()
    {
        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試区分
                             "PRINT_TYPE"       =>  VARS::post("PRINT_TYPE"),       //出力選択
                             "GOUKAKUSHA"       =>  VARS::post("GOUKAKUSHA"),       //合格者チェックボックス用
                             "EXAMNO"           =>  VARS::post("EXAMNO"),           //受験番号
                             "LINE"             =>  VARS::post("LINE"),             //開始位置（行）
                             "ROW"              =>  VARS::post("ROW"),              //開始位置（列）
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
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
