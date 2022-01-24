<?php

require_once('for_php7.php');

class knjh410_hanteiModel extends Model {
    var $cmd;
    var $selectdata;    //新クラス（左リスト）
    var $selectdataLabel;    //新クラス（左リスト）
    var $control;

    function knjh410_hanteiModel()
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
            $schregQuery = knjh410_hanteiQuery::getSchreg($this->year, $this->GAKUSEKI);
            $schregRow = $db->getRow($schregQuery, DB_FETCHMODE_ASSOC);
            
            $grade = mb_convert_kana(mb_substr($schregRow["GRADE_NAME"], 0, 1), "n");
            
            $this->gakunen = number_format($grade);
            //DB切断
            Query::dbCheckIn($db);

        }

    }

    function validateInput() {
        return true;
    }

    function &getUpdateModel() {
        if ($this->validateInput()) {
            knjh410_hanteiQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
?>
