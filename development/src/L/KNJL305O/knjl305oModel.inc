<?php

require_once('for_php7.php');


class knjl305oModel extends Model {

    var $cmd;
    var $year;              //入試年度
    var $examno;            //受験番号
    var $testdiv;           //入試区分
    var $checkexam;    
    var $hallcd;    
    var $examtype;    
    var $field = array();           //フィールドデータ
    var $cflg;                      //変更フラグ（同じ受験番号を編集中チェック）

    function knjl305oModel()
    {
    }

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->examno);
            unset($this->checkexam);
            unset($this->hallcd);
            unset($this->examtype);
            unset($this->cflg);
        }

        $this->cmd              = VARS::request("cmd");
        $this->year             = CTRL_YEAR + 1;
        if (strlen(VARS::post("EXAMNO"))) {
            if ($this->examno != VARS::post("EXAMNO")) {
                $this->cflg = "false";
            } elseif ($this->cmd == "change" || $this->cmd == "change_testdiv2") { //編集中フラグ
                $this->cflg = "true";
            } else {
                $this->cflg = "false";
            }
            $this->examno  = VARS::post("EXAMNO");
            $this->examno = sprintf("%05d",$this->examno);
        }
        $this->hallcd        = VARS::post("CHANGE_HALL");
        $this->examtype      = VARS::post("CHANGE_EXAM_TYPE");

        //フィールド
        $this->field = array("APPLICANTDIV"		 		=> VARS::post("APPLICANTDIV"),			//入試制度
                             "TESTDIV" 					=> VARS::post("TESTDIV"),				//出願区分
                             "RECEPTNO"             	=> VARS::post("RECEPTNO"),				//受付番号
                             "CHANGE_RECEPTNO"    		=> VARS::post("CHANGE_RECEPTNO"),		//変更後受付番号
                             "EXAM_TYPE"            	=> VARS::post("EXAM_TYPE"),				//受験型
                             "CHANGE_EXAM_TYPE"    		=> VARS::post("CHANGE_EXAM_TYPE"),		//変更後受験型
                            );
    }


    //入力項目チェック
    function validateInput()
    {
        //空文字チェック
        if (!($this->examno)) {
            $this->setWarning("MSG301", "( 受験番号 )");
            return false;
        }
        if (!($this->field["APPLICANTDIV"])) {
            $this->setWarning("MSG301", "( 入試制度 )");
            return false;
        }
        if (!($this->field["TESTDIV"])) {
            $this->setWarning("MSG301", "( 出願区分 )");
            return false;
        }
        if (!($this->field["RECEPTNO"])) {
            $this->setWarning("MSG301", "( 座席番号 )");
            return false;
        }
        if (!($this->field["CHANGE_RECEPTNO"])) {
            $this->setWarning("MSG301", "( 変更後座席番号 )");
            return false;
        }
        if (!($this->field["EXAM_TYPE"])) {
            $this->setWarning("MSG301", "( 受験型 )");
            return false;
        }
        if (!($this->field["CHANGE_EXAM_TYPE"])) {
            $this->setWarning("MSG301", "( 変更後受験型 )");
            return false;
        }
        //キー値変更チェック
        if ($this->examno != $this->checkexam) {
            $this->setWarning("MSG300", "更新は検索を実行してから行ってください。");
            return false;
        }        
        //存在チェック
        $row = knjl305oQuery::Existdata($this);
        if (isset($row["RECEPTNO"])) {
            $this->setWarning("MSG302");
            return false;
        }
        return true;
    }

    //更新処理
    function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $result = knjl305oQuery::getUpdateQuery($this);
            if ($this->cmd == "update") {
                $this->setMessage("MSG201");
            }
        } else {
            $this->cmd = "main";
        }
        return;
    } 
}
?>