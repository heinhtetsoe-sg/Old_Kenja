<?php

require_once('for_php7.php');
class knje370kModel extends Model {

    var $cmd;
    var $exp_grpcd;
    var $selectExp_grpcd;
    var $field = array();

    function knje370kModel()
    {
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");

        if (VARS::get("PROGRAMID")) {
            unset($this->exp_grpcd);
            unset($this->selectExp_grpcd);
            unset($this->schoolKind);
        }

        //校種
        $this->schoolKind = VARS::post("SCHOOL_KIND") ? VARS::post("SCHOOL_KIND") : $this->schoolKind;

        //左画面から
        if (VARS::get("COLLEGE_GRP_CD")) {
            $this->exp_grpcd        = sprintf("%02d",VARS::get("COLLEGE_GRP_CD"));
            $this->selectExp_grpcd  = sprintf("%02d",VARS::get("COLLEGE_GRP_CD"));
        }
        //右画面から
        if ($this->cmd == "grpChange") {
            $this->exp_grpcd = (strlen(VARS::post("COLLEGE_GRP_CD"))) ? sprintf("%02d",VARS::post("COLLEGE_GRP_CD")) : "";
        }
        $this->field = array("COLLEGE_GRP_CD"       => strlen(VARS::post("COLLEGE_GRP_CD")) ? sprintf("%02d",VARS::post("COLLEGE_GRP_CD")) : "",
                             "COLLEGE_GRP_NAME"     => VARS::post("COLLEGE_GRP_NAME"),
                             "SCHOOL_GROUP"         => VARS::post("SCHOOL_GROUP"));

        if ($this->field["SCHOOL_GROUP"] == "") $this->field["SCHOOL_GROUP"] = "01";

        $this->selectLeft     = VARS::post("selectLeft");
        $this->selectLeftText = VARS::post("selectLeftText");
    }

    //入力項目チェック
    function validateInput($kind)
    {
    
        if(($kind=="update" || $kind=="delete") &&
            $this->selectExp_grpcd != $this->field["COLLEGE_GRP_CD"]
        ){
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }

        //追加時の重複チェック
        $db = Query::dbCheckOut();
        $query = knje370kQuery::getSelectData($this, 2);
        $row = $db->getRow($query);
        if ($kind == "add" && is_array($row)) {
            $this->setWarning("MSG918", "(グループコード)");
            return false;
        }

        //他GRPに選択したDATのデータがあればエラー
        $query = knje370kQuery::getAftCollegeGroupDatCnt($this);
        $dataCnt = $db->getOne($query);
        if (($kind == "add" || $kind == "update") && $dataCnt > 0) {
            $this->setWarning("MSG302", "複数のグループに同じ学校は登録できません。");
            return false;
        }

        Query::dbCheckIn($db);

        //文字数チェック
        if (mb_strlen($this->field["COLLEGE_GRP_NAME"]) > 60){
             $this->setWarning("MSG915","(グループ名は60バイトまでです。)");
             return false;
        }

        //学校追加チェック

        return ture;
    }

    //更新処理
    function &getUpdateModel()
    {
        //入力チェック
        if($this->validateInput($this->cmd)){
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            knje370kQuery::getDeleteQuery($this, $db);
            knje370kQuery::getUpdateQuery($this, $db);

            $db->commit();
            Query::dbCheckIn($db);

            $this->exp_grpcd        = $this->field["COLLEGE_GRP_CD"];
            $this->selectExp_grpcd  = $this->field["COLLEGE_GRP_CD"];
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

            knje370kQuery::getDeleteQuery($this, $db);

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

        knje370kQuery::getCopyYearQuery($this);
        $this->setMessage("MSG201");
        $this->exp_grpcd = "";
        $this->selectExp_grpcd = "";

        return;
    }
}
?>
