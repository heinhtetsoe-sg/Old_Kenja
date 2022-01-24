<?php

require_once('for_php7.php');
class knjp908Model extends Model {
    var $bankList = array();
    var $field = array();
    var $cmd;
    var $mode;
    var $schoolcd;
    var $schoolKind;
    var $schregno;
    var $row;
    var $control_data;

    function knjp908Model()
    {
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init()
    {
        if (VARS::get("PROGRAMID")) {
            $this->bankList = array();
        }
        if (VARS::get("PROGRAMID") || VARS::get("init")) {
            unset($this->schoolcd);
            unset($this->schoolKind);
            unset($this->schregno);
        }

        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("GRADE") != "") {
            $grade = VARS::get("GRADE");
            $this->schoolcd = sprintf("%012d", SCHOOLCD);
            $this->schoolKind = knjp908Query::getSchoolKind($grade);
        }
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->cmd = VARS::request("cmd");
        $this->month = VARS::post("MONTH_CMB");
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
            $result = knjp908Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }

    function &getDeleteModel()
    {
        if ($this->validateInput()){
            $result = knjp908Query::getDeleteQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }
    function &getSendModel()
    {
        $db = Query::dbCheckOut();
        if ($this->cmd == "sendBank") {
            $query = knjp908Query::getBankSearch($this->bankSearch);
        } else {
            $query = knjp908Query::getBranchcd($this->bankcd);
        }
        $result = $db->query($query);
        $data = "{";
        $sep = "";
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $data .= $sep .sprintf("'%s':'%s'", $row["VALUE"], activewidgets_html($row["LABEL"]));
            $sep = ",";
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
