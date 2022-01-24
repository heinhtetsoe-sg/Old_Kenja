<?php

require_once('for_php7.php');

class knjp738Model extends Model {
    var $cmd;
    var $exp_grpcd;
    var $selectExp_grpcd;
    var $field = array();
    var $year;

    function knjp738Model() {}

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->schoolkind);
            unset($this->grp_cd);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->cmd = VARS::request("cmd");

        //左画面から
        if (VARS::get("GRP_CD")) {
            $this->grp_cd = VARS::get("GRP_CD");
        }
        if (VARS::request("YEAR") != "") {
            $this->year = VARS::request("YEAR");
        }
        if (VARS::request("SCHOOL_KIND") != "") {
            $this->schoolkind = VARS::request("SCHOOL_KIND");
        }

        if ($this->cmd == "editNew") {
            unset($this->grp_cd);
        }

        $this->field = array("GRP_CD"       => strlen(VARS::post("GRP_CD")) ? sprintf("%03d",VARS::post("GRP_CD")) : "",
                             "GRP_NAME"     => VARS::post("GRP_NAME")
                             );

        $this->selectdata = VARS::post("selectdata");
    }

    //入力チェック
    function validateInput($kind) {
        //空文字チェック
        if(strlen($this->field["GRP_CD"]) == 0){
            $this->setWarning("MSG301","(グループコード)");
            return false;
        }
        if(strlen($this->field["GRP_NAME"]) == 0){
            $this->setWarning("MSG301","(グループ名)");
            return false;
        }

        //文字数チェック
        if (strlen($this->field["GRP_NAME"]) > 60){
             $this->setWarning("MSG915","(グループ名は60バイトまでです。)");
             return false;        
        }

        //追加時の重複チェック
        $db = Query::dbCheckOut();
        $query = knjp738Query::getRow($this, 2);
        $row = $db->getRow($query);
        if ($kind == "add" && is_array($row)) {
            $this->setWarning("MSG918");
            return false;
        }
        Query::dbCheckIn($db);

        if(($kind=="update" || $kind=="delete") &&
            $this->grp_cd != $this->field["GRP_CD"]
        ){
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }

        return true;
    }

    //更新処理
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput($this->cmd)) {
            knjp738Query::getUpdateQuery($this);

            $this->grp_cd = sprintf("%03d", $this->field["GRP_CD"]);
            $this->setMessage("MSG201");
        }

        return;
    }

    //削除処理
    function &getDeleteModel() {
        //入力チェック
        if($this->validateInput($this->cmd)) {
            knjp738Query::getDeleteQuery($this);

            $this->grp_cd = "";
            $this->setMessage("MSG201");
        }
        return;
    }

    //前年度のデータをコピー
    function &getCopyModel() {
        //今年度のデータが存在するか確認
        $t_year = knjp738Query::CheckData($this, "COLLECT_CSV_GRP_MST", $this->year);
        if (isset($t_year)) {
           $this->setWarning('MSG203', "今年度のデータが存在します。");
           return false;
        }
        $t_year = knjp738Query::CheckData($this, "COLLECT_CSV_GRP_DAT", $this->year);
        if (isset($t_year)) {
           $this->setWarning('MSG203', "今年度のデータが存在します。");
           return false;
        }
        //前年度のデータが存在するか確認
        $pre_year = knjp738Query::CheckData($this, "COLLECT_CSV_GRP_MST", ($this->year - 1));
        if (!isset($pre_year)) {
           $this->setWarning('MSG203', "前年データが存在しません。");
           return false;
        }
        $pre_year = knjp738Query::CheckData($this, "COLLECT_CSV_GRP_DAT", ($this->year - 1));
        if (!isset($pre_year)) {
           $this->setWarning('MSG203', "前年データが存在しません。");
           return false;
        }

        knjp738Query::getCopyQuery($this);
        $this->setMessage("MSG201");
        return;
    }
}
?>
