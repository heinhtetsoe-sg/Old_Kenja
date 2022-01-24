<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl250cModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $isGojo;

    /**
     * メンバ変数の初期化
     */
    function knjl250cModel()
    {
    }

    function init()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->ObjYear      = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "PRE_TESTDIV"  =>  VARS::post("PRE_TESTDIV"),      //プレテスト区分
                             "SORT"         =>  VARS::post("SORT"),             //印刷順序
                             "GOKEI"        =>  VARS::post("GOKEI"),            //成績順
                             "OUTPUT"       =>  VARS::post("OUTPUT")            //氏名を出力しない
                            );

        $this->cmd         = VARS::request("cmd");

        //学校コードを取得
        $schoolCode = knjl250cQuery::getNameMstZ010();
        $this->isGojo = $schoolCode == "30290053001" ? true : false;
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
