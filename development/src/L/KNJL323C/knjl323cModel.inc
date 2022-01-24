<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl323cModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $isGojou;
    var $isCollege;
    var $isWakayama;

    /**
     * メンバ変数の初期化
     */
    function knjl323cModel()
    {
    }

    function init()
    {               
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear      = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV"	    =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"	        =>  VARS::post("TESTDIV"),     		//入試区分
                             "SHDIV"	        =>  VARS::post("SHDIV"),     		//専併区分
                             "PRINT_TYPE1"	    =>  VARS::post("PRINT_TYPE1"),      //帳票種類
                             "PRINT_TYPE2"	    =>  VARS::post("PRINT_TYPE2"),      //帳票種類
                             "PRINT_TYPE3"	    =>  VARS::post("PRINT_TYPE3"),      //帳票種類
                             "PRINT_TYPE4"	    =>  VARS::post("PRINT_TYPE4"),      //帳票種類
                             "SPECIAL_MEASURES"	=>  VARS::post("SPECIAL_MEASURES")  //追加合格者名簿区分
                            );

        $this->cmd         = VARS::request("cmd");

        //学校コードを取得：五条・和歌山を判断するため
        $rtnRow = knjl323cQuery::getNameMstZ010();
        $this->isGojou = $rtnRow["NAME2"] == "30290053001" || $rtnRow["NAME2"] == "30290086001" ? true : false;
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
