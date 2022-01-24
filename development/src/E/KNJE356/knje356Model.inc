<?php

require_once('for_php7.php');

//定数定義
class knje356Model extends Model {
    var $field      = array();
    var $cmd;
    var $getyear;
    var $l_cd;
    //メンバ変数の初期化
    function knje356Model()
    {
    }
    function init()
    {
        $this->field = array("L_CD"     =>  VARS::post("L_CD"),
                             "S_CD"     =>  VARS::post("S_CD"),
                             "S_NAME"   =>  VARS::post("S_NAME"),
                             "S_ABBV"   =>  VARS::post("S_ABBV")
                             );

        $this->cmd  = VARS::request("cmd");

        if ($this->cmd == "") {
            $this->l_cd = "";
            $this->s_cd = "";
            $this->leftYear = "";
            $this->rightYear = "";
        }

        if (VARS::get("PROGRAMID")) {
            $this->l_cd = "";
            $this->s_cd = "";
            $this->leftYear = CTRL_YEAR;
        }
        if (VARS::get("L_CD") != "") {
            $this->l_cd   = VARS::get("L_CD");
        }
        if (VARS::get("S_CD") != "") {
            $this->s_cd   = VARS::get("S_CD");
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
        if (trim($this->field["L_CD"]) == ""){
            $this->setWarning("MSG301","(大分類CD)");
            return false;
        }
        if (trim($this->field["S_CD"]) == ""){
            $this->setWarning("MSG301","(小分類CD)");
            return false;
        }
        if (trim($this->field["S_NAME"]) == ""){
            $this->setWarning("MSG301","(小分類名称)");
            return false;
        }

        if ($kind=="update" || $kind=="delete"){
            //リストより選択していない
            if ($this->l_cd == ""){
                $this->setWarning("MSG308");
                return false;
            }
            if ($this->s_cd == ""){
                $this->setWarning("MSG308");
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

            //キー値が変更されていますかをチェック
            if ($this->field["L_CD"] != $this->l_cd){
                $this->setWarning("MSG308","キー値は変更できません");
                return false;
            }
            if ($this->field["S_CD"] != $this->s_cd){
                $this->setWarning("MSG308","キー値は変更できません");
                return false;
            }

            //大分類コードのゼロ埋め
            $this->field["S_CD"] = sprintf("%03d", $this->field["S_CD"]);

            $db = Query::dbCheckOut();

            //１レコード取得
            $query = knje356Query::getSmst($this, $this->field["L_CD"], $this->field["S_CD"]);
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
            //大分類コードのゼロ埋め
            $this->field["S_CD"] = sprintf("%03d", $this->field["S_CD"]);

            $db = Query::dbCheckOut();

            //１レコード取得
            $query = knje356Query::getSmst($this, $this->field["L_CD"], $this->field["S_CD"]);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

            Query::dbCheckIn($db);

            //重複チェック
            if ($this->cmd == "add" && is_array($row)){
                $this->setWarning("MSG918","(小分類コード)");
                return false;
            }
            if (strlen($this->field["S_CD"]) > 3){
                $this->setWarning("MSG915","(小分類コードは3バイトまでです。)");
                return false;
            } else if (strlen($this->field["S_NAME"]) > 75){
                $this->setWarning("MSG915","(小分類名称は75バイトまでです。)");
                return false;
            } else if (strlen($this->field["S_ABBV"]) > 75){
                $this->setWarning("MSG915","(小分類略称は75バイトまでです。)");
                return false;
            }
        }
        return true;
    }

    function &getCopyModel() {
        $result = knje356Query::getCopyQuery($this);
        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
        }
        return $result;
    }

    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput("insert")){
            $this->insert();
            $this->l_cd = $this->field["L_CD"];
            $this->s_cd = $this->field["S_CD"];
        }
        return;
    }

    function &insert() {
        $result = knje356Query::getInsertQuery($this, $this->field);
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
        $result = knje356Query::getUpdateQuery($this, $this->field);
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
        if (!isset($this->l_cd)) {
            $this->setWarning("MSG303");
            return;
        }
        if (!isset($this->s_cd)) {
            $this->setWarning("MSG303");
            return;
        }
        $result = knje356Query::getDeleteQuery($this, $this->field);
        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
            $this->field = array();
            $this->l_cd = "";
            $this->s_cd = "";
        }
    }

    //変数をクリアします
    function clean()
    {
    }
}
?>
