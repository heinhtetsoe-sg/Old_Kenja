<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl322cModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $isGojou;
    var $isWakayama;
    var $isCollege;

    /**
     * メンバ変数の初期化
     */
    function knjl322cModel()
    {
    }

    function init()
    {               
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear      = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV"),          //入試区分
                             "SHDIV"        =>  VARS::post("SHDIV"),            //入試区分
                             "SORT"         =>  VARS::post("SORT"),             //印刷順序
                             "PRINT_PASS1"  =>  VARS::post("PRINT_PASS1"),      //帳票選択
                             "PRINT_PASS2"  =>  VARS::post("PRINT_PASS2"),      //帳票選択
                             "PRINT_PASS3"  =>  VARS::post("PRINT_PASS3")       //帳票選択
                            );

        $this->cmd         = VARS::request("cmd");

        //学校コードを取得：五条・和歌山を判断するため
        $rtnRow = knjl322cQuery::getNameMstZ010();
        $this->isGojou = $rtnRow["NAME2"] == "30290053001" || $rtnRow["NAME2"] == "30290086001" ? true : false;
        $this->isWakayama = $rtnRow["NAME2"] == "30300049001" ? true : false;
        $this->isCollege = $rtnRow["NAME2"] == "30290086001" ? true : false;
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
