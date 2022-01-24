<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl324cModel extends Model {
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
    function knjl324cModel()
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
                             "JUDGEMENT"	=>  VARS::post("JUDGEMENT")     	//合格者区分
                            );

        $this->cmd         = VARS::request("cmd");

        //学校コードを取得：五条・和歌山を判断するため
        $rtnRow = knjl324cQuery::getNameMstZ010();
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
