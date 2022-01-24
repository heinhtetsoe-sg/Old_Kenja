<?php

require_once('for_php7.php');

class knjl014cModel extends Model {
    var $field      = array();
    var $ObjYear;
    var $selectdata;
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $isGojou;
    var $isCollege;
    var $examno1Keta;
        
    /**
     * メンバ変数の初期化
     */
    function knjl014cModel()
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
                             "GRADE_HRCLASS"    =>  VARS::post("GRADE_HRCLASS"),    //年組
                             "DBNAME"	        =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->selectdata   = VARS::post("selectdata");
        $this->cmd          = VARS::request("cmd");
        //学校コードを取得：五条・和歌山を判断するため
        $rtnRow = knjl014cQuery::getNameMstZ010();
        $this->isGojou = $rtnRow["NAME2"] == "30290053001" ? true : false;
        $this->isCollege = $rtnRow["NAME2"] == "30290086001" ? true : false;
        $this->examno1Keta = ($this->isGojou) ? "3" : "6";
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }

    //入力項目チェック
    function validateInput()
    {
        if (!($this->field["APPLICANTDIV"])) {
            $this->setWarning("MSG301", "( 入試制度 )");
            return false;
        }
        if (!($this->field["TESTDIV"])) {
            $this->setWarning("MSG301", "( 入試区分 )");
            return false;
        }

        return true;
    }

    //実行処理
    function &getUpdateModel()
    {
        if ($this->validateInput()) {
            knjl014cQuery::getUpdateQuery($this);
            $this->setMessage("MSG202");
        }
        return;
    }
}
?>
