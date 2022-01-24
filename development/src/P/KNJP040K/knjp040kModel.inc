<?php

require_once('for_php7.php');

class knjp040kModel extends Model {
    var $field      = array();
    var $cmd;
    var $mode;
    var $SCHREGNO;
    var $row;
    var $control_data;

    function knjp040kModel()
    {
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init()
    {
        if (VARS::get("PROGRAMID") || VARS::get("init")) {
            unset($this->schregno);
        }

        if(VARS::get("SCHREGNO") != ""){
            $this->schregno = VARS::get("SCHREGNO");
        }
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->relation = VARS::get("relation");
        $this->name = VARS::get("name");
        $this->cmd = VARS::request("cmd");

        $this->field = array("BANKCD"        => VARS::post("BANKCD"),
                             "BRANCHCD"      => VARS::post("BRANCHCD"),
                             "DEPOSIT_ITEM"  => VARS::post("DEPOSIT_ITEM"), 
                             "ACCOUNTNO"     => VARS::post("ACCOUNTNO"),
                             "ACCOUNTNAME"   => VARS::post("ACCOUNTNAME"),  
                             "RELATIONSHIP"  => VARS::post("RELATIONSHIP"),
                             "BANKCD"        => VARS::post("BANKCD"));
    }

    function validateInput()
    {
        if (!strlen($this->schregno)) {
            $this->setWarning("MSG308");
            return false;
        }
        if (!strlen($this->field["BANKCD"])) {
            $this->setWarning("MSG301");
            return false;
        }
        if (!strlen($this->field["BRANCHCD"])) {
            $this->setWarning("MSG301");
            return false;
        }
        if (strlen($this->field["ACCOUNTNAME"]) > 48) {
            $this->setWarning("MSG901","口座名義　48バイトまで");
            return false;
        }
        
        //全角スペースを正規表現で検索できないため一度半角に置き換える
//        $this->field["ACCOUNTNAME"] = str_replace("　"," ",$this->field["ACCOUNTNAME"]);
//        if(!mbereg("^[ァ-ヶ\s]*$",$this->field["ACCOUNTNAME"])){
//            $this->setWarning("MSG901","口座名義 すべて全角カナで入力してください");
//            return false;
//        }
        $this->field["ACCOUNTNAME"] = str_replace(" ","　",$this->field["ACCOUNTNAME"]);

        if ($this->field["BANKCD"]=="" || $this->field["DEPOSIT_ITEM"]=="" || 
            $this->field["ACCOUNTNO"]=="" || $this->field["ACCOUNTNAME"]=="" ) {
#            $this->field["ACCOUNTNO"]=="" || $this->field["ACCOUNTNAME"]=="" || $this->field["RELATIONSHIP"]=="" ) {
            $this->setWarning("MSG301", "必修項目を入力してください");
            return false;
        }
        
        $this->field["ACCOUNTNO"] = sprintf("%07d", $this->field["ACCOUNTNO"]);
        return true;
    }

    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $result = knjp040kQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }

    function &getDeleteModel()
    {
        if ($this->validateInput()){
            $result = knjp040kQuery::getDeleteQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }
    function &getSendModel()
    {
        $db = Query::dbCheckOut();
        $result = $db->query(knjp040kQuery::getBranchcd($this->field["BANKCD"]));
        $data = "{";
        $sep = "";
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $data .= $sep .sprintf("'%s':'%s'", $row["BRANCHCD"], activewidgets_html($row["BRANCHCD"]."：".$row["BRANCHNAME"]));
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
//    $msg = htmlspecialchars($msg);

    return $msg;
}

?>
