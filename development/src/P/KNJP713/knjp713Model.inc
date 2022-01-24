<?php

require_once('for_php7.php');

class knjp713Model extends Model {

    var $cmd;
    var $exp_grpcd;
    var $selectExp_grpcd;
    var $field = array();
    var $taisyouYear;

    function knjp713Model()
    {
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");

        if (VARS::get("PROGRAMID") || $this->cmd == "changeYear") {
            unset($this->taisyouYear);
            unset($this->exp_grpcd);
            unset($this->selectExp_grpcd);
            unset($this->schoolKind);
        }

        //年度
        $this->taisyouYear = (VARS::request("TAISYOU_YEAR")) ? VARS::request("TAISYOU_YEAR") : $this->taisyouYear;
        $this->taisyouYear = ($this->taisyouYear) ? $this->taisyouYear : CTRL_YEAR;
        $this->schoolKind = VARS::post("SCHOOL_KIND") ? VARS::post("SCHOOL_KIND") : $this->schoolKind;

        //左画面から
        if (VARS::get("COLLECT_GRP_CD")) {
            $this->exp_grpcd        = sprintf("%04d",VARS::get("COLLECT_GRP_CD"));
            $this->selectExp_grpcd  = sprintf("%04d",VARS::get("COLLECT_GRP_CD"));
        }
        //右画面から
        if ($this->cmd == "grpChange") {
            $this->exp_grpcd = (strlen(VARS::post("COLLECT_GRP_CD"))) ? sprintf("%04d",VARS::post("COLLECT_GRP_CD")) : "";
        }
        $this->field = array("COLLECT_GRP_CD"       => strlen(VARS::post("COLLECT_GRP_CD")) ? sprintf("%04d",VARS::post("COLLECT_GRP_CD")) : "",
                             "COLLECT_GRP_NAME"     => VARS::post("COLLECT_GRP_NAME"),
                             "COLLECT_M_CD"         => VARS::post("selectdata2"));
    }

    //入力項目チェック
    function validateInput($kind)
    {
        //空文字チェック
        if(strlen($this->field["COLLECT_GRP_CD"]) == 0){
            $this->setWarning("MSG301","(入金グループコード)");
            return false;
        }
        if(strlen($this->field["COLLECT_GRP_NAME"]) == 0){
            $this->setWarning("MSG301","(入金グループ)");
            return false;
        }
        if(($kind=="update" || $kind=="delete") &&
            $this->selectExp_grpcd != $this->field["COLLECT_GRP_CD"]
        ){
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }

        //値チェック
        if (sprintf("%04d",$this->field["COLLECT_GRP_CD"]) == '0000') {
            $this->setWarning("MSG901","0000は対象外です。\\n(入金グループコード)");
            return false;
        }

        //追加時の重複チェック
        $db = Query::dbCheckOut();
        $query = knjp713Query::getRow($this, 2);
        $row = $db->getRow($query);
        if ($kind == "add" && is_array($row)) {
            $this->setWarning("MSG918");
            return false;
        }
        Query::dbCheckIn($db);

        //文字数チェック
        if (strlen($this->field["COLLECT_GRP_NAME"]) > 60){
             $this->setWarning("MSG915","(入金グループは60バイトまでです。)");
             return false;        
        }
        return true;
    }

    //更新処理
    function &getUpdateModel()
    {
        //入力チェック
        if($this->validateInput($this->cmd)){
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            knjp713Query::getDeleteQuery($this, $db);
            knjp713Query::getUpdateQuery($this, $db);

            $db->commit();
            Query::dbCheckIn($db);

            $this->exp_grpcd = sprintf("%04d", $this->field["COLLECT_GRP_CD"]);
            $this->selectExp_grpcd = sprintf("%04d", $this->field["COLLECT_GRP_CD"]);

            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    function &getDeleteModel()
    {
        //入力チェック
        if($this->validateInput($this->cmd)){
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            knjp713Query::getDeleteQuery($this, $db);

            $db->commit();
            Query::dbCheckIn($db);

            $this->exp_grpcd = "";
            $this->selectExp_grpcd = "";
            $this->setMessage("MSG201");
        }
        return;
    }

    //前年度のデータをコピー
    function &getCopyModel()
    {
        $db = Query::dbCheckOut();
        //前年度データ存在チェック
        $exist_data  = $db->getOne(knjp713Query::selectYearQuery($this, $this->taisyouYear - 1, "COLLECT_GRP_MST"));
        Query::dbCheckIn($db);

        if($exist_data == 0){
            $this->setWarning('MSG203', "前年度のデータは存在しません。");
            return false;
        } else{
            knjp713Query::getCopyYearQuery($this);
            $this->setMessage("MSG201");
            $this->exp_grpcd = "";
            $this->selectExp_grpcd = "";
        }

        return;
    }
}
?>
