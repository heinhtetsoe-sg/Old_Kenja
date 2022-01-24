<?php

require_once('for_php7.php');

class knjh400_hanteiModel extends Model {
    var $cmd;
    var $selectdata;    //新クラス（左リスト）
    var $selectdataLabel;    //新クラス（左リスト）
    var $control;

    function knjh400_hanteiModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->schregno = array();
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");

        $this->GAKUSEKI = VARS::request("SCHREGNO") != "" ? VARS::request("SCHREGNO") : $this->GAKUSEKI;
        
        $this->year = VARS::request("YEAR") != "" ? VARS::request("YEAR") : $this->year;
        
        
        $this->top["GYOUSYA"] = VARS::post("GYOUSYA") != "" ? VARS::post("GYOUSYA") : "";
        
        $this->field = array("GYOUSYA"               =>  VARS::request("GYOUSYA") != "" ? VARS::request("GYOUSYA") : "",
                            );

        if($this->cmd == ""){
            //データベース接続
            $db = Query::dbCheckOut();

            //SCHREG_REGD_BASE_MSTから取得
            $schregQuery = knjh400_hanteiQuery::getSchreg($this->year, $this->GAKUSEKI);
            $schregRow = $db->getRow($schregQuery, DB_FETCHMODE_ASSOC);

            $this->gakunen = number_format($schregRow["GRADE_CD"]);
            //DB切断
            Query::dbCheckIn($db);

        }

    }

    function validateInput() {
        return true;
    }

    function &getUpdateModel() {
        if ($this->validateInput()) {
            knjh400_hanteiQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
?>
