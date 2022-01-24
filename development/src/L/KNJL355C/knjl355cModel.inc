<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl355cModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl355cModel()
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
                             "TITLE"        =>  VARS::post("TITLE"),            //タイトル
                             "SORT"         =>  VARS::post("SORT")              //印刷順序
                            );

        $this->cmd         = VARS::request("cmd");

        $rtnRow = knjl355cQuery::getNameMstZ010();
        $this->isGojou   = $rtnRow["NAME2"] == "30290053001" ? true : false;
        $this->isCollege = $rtnRow["NAME2"] == "30290086001" ? true : false;
        $this->isWakayama = $rtnRow["NAME2"] == "30300049001" ? true : false;
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
