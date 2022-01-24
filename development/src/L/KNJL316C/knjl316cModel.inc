<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl316cModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $isGojou;
    var $isCollege;

    /**
     * メンバ変数の初期化
     */
    function knjl316cModel()
    {
    }

    function init()
    {               
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear      = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV"	=>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"	    =>  VARS::post("TESTDIV"),     		//入試区分
                             "SHDIV"	    =>  VARS::post("SHDIV"),     		//専併区分
                             "SORT"	        =>  VARS::post("SORT"),             //印刷順序
                             "OUTPUT"       =>  VARS::post("OUTPUT")            //出力フラグ
                            );

        $this->cmd         = VARS::request("cmd");

        //学校コードを取得：五条・和歌山を判断するため
        $rtnRow = knjl316cQuery::getNameMstZ010();
        $this->isGojou = $rtnRow["NAME2"] == "30290053001" || $rtnRow["NAME2"] == "30290086001" ? true : false;
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
