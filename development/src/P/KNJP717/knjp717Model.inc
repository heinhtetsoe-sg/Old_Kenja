<?php

require_once('for_php7.php');

class knjp717Model extends Model {
    var $bankList = array();
    var $field = array();
    var $cmd;
    var $mode;
    var $schoolcd;
    var $schregno;
    var $row;
    var $control_data;
    var $search_div;
    var $select_year;
    var $yuucho;

    function knjp717Model()
    {
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init()
    {
        $this->yuucho = "9900";
        if (VARS::get("PROGRAMID")) {
            $this->bankList = array();
            $this->bankList = knjp717Query::getBankcd();
        }
        if (VARS::get("PROGRAMID") || VARS::get("init")) {
            unset($this->schoolcd);
            unset($this->schregno);
        }
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::get("SELECT_YEAR") != "") {
            $this->select_year = VARS::get("SELECT_YEAR");
        }
        if (VARS::get("SEARCH_DIV") != "") {
            $this->search_div = VARS::get("SEARCH_DIV");
        }
        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }

        $this->schoolcd = sprintf("%012d", SCHOOLCD);
        if (VARS::get("GRADE") != "") {
            $grade = VARS::get("GRADE");
        }

        $seqList = array("1", "2");
        $fieldList = array("BANKCD", "BRANCHCD", "DEPOSIT_ITEM", "ACCOUNTNO", "ACCOUNTNAME", "RELATIONSHIP");
        $this->field = array();
        $getBranchType = "_C";
        foreach ($seqList as $seq) {
            foreach ($fieldList as $fieldName) {
                if ($this->field["BANKCD".$seq] == $this->yuucho) {
                    $getBranchType = "_T";
                }
                if ($fieldName == "BRANCHCD") {
                    $this->field[$fieldName.$seq] = VARS::post($fieldName.$getBranchType.$seq);
                } else {
                    $this->field[$fieldName.$seq] = VARS::post($fieldName.$seq);
                }
            }
        }
        $this->cmd = VARS::request("cmd");
        $this->bankcd = VARS::post("BANKCD");
        $this->bankSearch = VARS::post("BANKCD_SEARCH");
    }

    function validateInput()
    {
        if (!strlen($this->schregno)) {
            $this->setWarning("MSG308");
            return false;
        }

        $seqList = array("1", "2");
        $fieldList  = array("BANKCD", "BRANCHCD", "DEPOSIT_ITEM", "ACCOUNTNO", "ACCOUNTNAME", "RELATIONSHIP");
        $fieldListH = array("BANKCD", "BRANCHCD", "DEPOSIT_ITEM", "ACCOUNTNO", "ACCOUNTNAME");

        $inputFlg = array();
        $inputFlgAll = false;
        foreach ($seqList as $seq) {
            $inputFlg[$seq] = false;
            foreach ($fieldList as $fieldName) {
                if (strlen($this->field[$fieldName.$seq])) {
                    $inputFlg[$seq] = true;
                    $inputFlgAll = true;
                }
            }
        }

        //1と2の全て未入力はエラー
        if (!$inputFlgAll) {
            $this->setWarning("MSG301");
            return false;
        }

        foreach ($seqList as $seq) {
            if ($inputFlg[$seq]) {
                //必須チェック
                foreach ($fieldListH as $fieldName) {
                    if (!strlen($this->field[$fieldName.$seq])) {
                        $this->setWarning("MSG301");
                        return false;
                    }
                }
                //文字数チェック
                if (strlen($this->field["ACCOUNTNAME".$seq]) > 48) {
                    $this->setWarning("MSG901","口座名義　48バイトまで");
                    return false;
                }
                //0埋め
                $this->field["ACCOUNTNO".$seq] = sprintf("%07d", $this->field["ACCOUNTNO".$seq]);
            }
        }

        return true;
    }

    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $result = knjp717Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }

    function &getDeleteModel()
    {
        if ($this->validateInput()){
            $result = knjp717Query::getDeleteQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }
    function &getSendModel()
    {
        $db = Query::dbCheckOut();
        if ($this->cmd == "sendBank") {
            $query = knjp717Query::getBankSearch($this->bankSearch);
        } else {
            $query = knjp717Query::getBranchcd($this->bankcd);
        }
        $result = $db->query($query);
        $data = "{";
        $sep = "";
        $setSort = 0;
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $data .= $sep .sprintf("'%s':'%s'", sprintf("%03d", $setSort), activewidgets_html($row["LABEL"]));
            $sep = ",";
            $setSort++;
        }
        if ($sep == ""){
            $data .= "";
            $sep = ",";
        }
        $data .= "}";
        if ($sep != ""){
            //URIエンコード
            $data = rawurlencode($data);
            //出力charsetをut-8に
            mb_http_output ( 'UTF-8' );
            //ヘッダ
            header ("Content-Type: text/html; charset=utf-8"); 
            echo($data);
        }
        exit;
    }
}
function activewidgets_html($msg){

    $msg = addslashes($msg);
    $msg = str_replace("\n", "\\n", $msg);
    $msg = str_replace("\r", "\\r", $msg);

    return $msg;
}

?>
