<?php

require_once('for_php7.php');

class knjl213yModel extends Model {
    var $recept_div;
    var $cmd;
    var $ObjYear;           //入試年度
    var $field      = array();

    function knjl213yModel() {
    }

    function init() {
        $this->field = array("APPLICANTDIV"	    =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"	        =>  VARS::post("TESTDIV"),          //入試区分
                             "YOBI"             =>  VARS::post("YOBI"),             //予備枚数
                             "APP_HOLD"	        =>  VARS::post("APP_HOLD")          //入試区分
                            );

        $this->recept_div   = "1"; //1:受験番号をコピー 2:連番(XXXX順)
        $this->ObjYear      = CTRL_YEAR+1;
        $this->cmd          = VARS::request("cmd");
    }

    function &getMainModel() {
    }

    function validateInput() {
        if (!strlen($this->field["APPLICANTDIV"])) {
            $this->setWarning("MSG301", "( 入試制度 )");
            return false;
        }
        if (!strlen($this->field["TESTDIV"])) {
            $this->setWarning("MSG301", "( 入試区分 )");
            return false;
        }
        //存在チェック
        $db = Query::dbCheckOut();
        $query = knjl213yQuery::getExamno($this, "CNT");
        $cnt = $db->getOne($query);
        if ($cnt == 0) {
            $this->setWarning("MSG303", "( 志願者基礎データ )");
            Query::dbCheckIn($db);
            return false;
        }
        Query::dbCheckIn($db);

        return true;
    }

    /**
     * 実行モデルを返します
    */
    function &getExecModel() {
        if($this->validateInput()){
            knjl213yQuery::ExecuteQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
?>
