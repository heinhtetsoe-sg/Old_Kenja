<?php

require_once('for_php7.php');

//定数定義
class knje357Model extends Model {
    var $field      = array();
    var $cmd;
    var $getyear;
    var $advertise_div;
    //メンバ変数の初期化
    function knje357Model()
    {
    }
    function init()
    {
        $this->field = array("ADVERTISE_DIV"    =>  VARS::post("ADVERTISE_DIV"),
                             "PROGRAM_CD"       =>  VARS::post("PROGRAM_CD"),
                             "PROGRAM_NAME"     =>  VARS::post("PROGRAM_NAME"),
                             "PROGRAM_ABBV"     =>  VARS::post("PROGRAM_ABBV")
                             );

        $this->cmd  = VARS::request("cmd");

        if ($this->cmd == "") {
            $this->advertise_div = "";
            $this->program_cd = "";
            $this->leftYear = "";
            $this->rightYear = "";
        }

        if (VARS::get("PROGRAMID")) {
            $this->advertise_div = "";
            $this->program_cd = "";
            $this->leftYear = CTRL_YEAR;
        }
        if (VARS::get("ADVERTISE_DIV") != "") {
            $this->advertise_div   = VARS::get("ADVERTISE_DIV");
        }
        if (VARS::get("PROGRAM_CD") != "") {
            $this->program_cd   = VARS::get("PROGRAM_CD");
        }
        if (VARS::get("YEAR") != "") {
            $this->rightYear   = VARS::get("YEAR");
        }
        if (VARS::post("LEFT_YEAR") != "") {
            $this->leftYear   = VARS::post("LEFT_YEAR");
        }
    }

    //入力項目チェック
    function validateInput($kind) {
        if (trim($this->field["ADVERTISE_DIV"]) == ""){
            $this->setWarning("MSG301","(募集区分)");
            return false;
        }
        if (trim($this->field["PROGRAM_CD"]) == ""){
            $this->setWarning("MSG301","(日程CD)");
            return false;
        }
        if (trim($this->field["PROGRAM_NAME"]) == ""){
            $this->setWarning("MSG301","(日程名称)");
            return false;
        }

        if ($kind=="update" || $kind=="delete"){
            //リストより選択していない
            if ($this->advertise_div == ""){
                $this->setWarning("MSG308");
                return false;
            }
            if ($this->program_cd == ""){
                $this->setWarning("MSG308");
                return false;
            }
            //キー値が変更されていますかをチェック
            if ($this->field["ADVERTISE_DIV"] != $this->advertise_div){
                $this->setWarning("MSG308","キー値は変更できません");
                return false;
            }
            if ($this->field["PROGRAM_CD"] != $this->program_cd){
                $this->setWarning("MSG308","キー値は変更できません");
                return false;
            }
            //リストより選択していない
            if ($this->rightYear == ""){
                $this->setWarning("MSG308");
                return false;
            }
            //キー値が変更されていますかをチェック
            if ($this->rightYear != $this->leftYear){
                $this->setWarning("MSG308","キー値は変更できません");
                return false;
            }

            //日程コードのゼロ埋め
            $this->field["PROGRAM_CD"] = sprintf("%02d", $this->field["PROGRAM_CD"]);
            $db = Query::dbCheckOut();

            //１レコード取得
            $query = knje357Query::getPrgDat($this, $this->field["ADVERTISE_DIV"], $this->field["PROGRAM_CD"]);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

            Query::dbCheckIn($db);

            //存在チェック
            if (!is_array($row)){
                $this->setWarning("MSG303");
                return false;
            }
        }
        if ($kind=="update" || $kind=="insert"){
            $db = Query::dbCheckOut();
            //日程コードのゼロ埋め
            $this->field["PROGRAM_CD"] = sprintf("%02d", $this->field["PROGRAM_CD"]);

            $db = Query::dbCheckOut();

            //１レコード取得
            $query = knje357Query::getPrgDat($this, $this->field["ADVERTISE_DIV"], $this->field["PROGRAM_CD"]);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

            Query::dbCheckIn($db);

            //重複チェック
            if ($this->cmd == "add" && is_array($row)){
                $this->setWarning("MSG918","(日程コード)");
                return false;
            }
            if (strlen($this->field["PROGRAM_CD"]) > 2){
                $this->setWarning("MSG915","(日程コードは2バイトまでです。)");
                return false;
            } else if (strlen($this->field["PROGRAM_NAME"]) > 75){
                $this->setWarning("MSG915","(日程名称は75バイトまでです。)");
                return false;
            } else if (strlen($this->field["PROGRAM_ABBV"]) > 75){
                $this->setWarning("MSG915","(日程略称は75バイトまでです。)");
                return false;
            }
        }
        return true;
    }

    function &getCopyModel() {
        $result = knje357Query::getCopyQuery($this);
        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
        }
        return $result;
    }

    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput("insert")){
            $this->insert();
            $this->advertise_div = $this->field["ADVERTISE_DIV"];
            $this->program_cd = $this->field["PROGRAM_CD"];
        }
        return;
    }

    function &insert() {
        $result = knje357Query::getInsertQuery($this, $this->field);
        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
        }
        return $result;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput("update")){
            $this->update();
        }
    }

    //アップデート時の内部メソッド
    function &update()
    {
        $result = knje357Query::getUpdateQuery($this, $this->field);
        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
        }
        return $result;
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput("delete")){
            $this->delete();
        }
        return;
    }

    function &delete() {
        if (!isset($this->advertise_div)) {
            $this->setWarning("MSG303");
            return;
        }
        if (!isset($this->program_cd)) {
            $this->setWarning("MSG303");
            return;
        }
        $result = knje357Query::getDeleteQuery($this, $this->field);
        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
            $this->field = array();
            $this->advertise_div = "";
            $this->program_cd = "";
        }
    }

    //変数をクリアします
    function clean()
    {
    }
}
?>
