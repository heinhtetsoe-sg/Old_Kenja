<?php

require_once('for_php7.php');

//定数定義
class knje355Model extends Model {
    var $field      = array();
    var $cmd;
    var $getyear;
    var $l_cd;
    //メンバ変数の初期化
    function knje355Model()
    {
    }
    function init()
    {
        $this->field = array("L_CD"     =>  VARS::post("L_CD"),
                             "L_NAME"   =>  VARS::post("L_NAME"),
                             "L_ABBV"   =>  VARS::post("L_ABBV")
                             );

        $this->cmd  = VARS::request("cmd");

        if ($this->cmd == "") {
            $this->l_cd = "";
            $this->leftYear = "";
            $this->rightYear = "";
        }

        if (VARS::get("PROGRAMID")) {
            $this->l_cd = "";
        }
        if (VARS::get("L_CD") != "") {
            $this->l_cd   = VARS::get("L_CD");
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
        if (trim($this->field["L_NAME"]) == ""){
            $this->setWarning("MSG301","(大分類名称)");
            return false;
        }

        if ($kind=="update" || $kind=="delete"){
            //リストより選択していない
            if ($this->field["L_CD"] == ""){
                $this->setWarning("MSG308");
                return false;
            }
            //キー値が変更されていますかをチェック
            if ($this->field["L_CD"] != $this->l_cd){
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

            //大分類コードのゼロ埋め
            $this->field["L_CD"] = sprintf("%02d", $this->field["L_CD"]);

            $db = Query::dbCheckOut();

            //１レコード取得
            $query = knje355Query::getLmst($this, $this->field["L_CD"]);
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
            $this->field["L_CD"] = sprintf("%02d", $this->field["L_CD"]);

            $db = Query::dbCheckOut();

            //１レコード取得
            $query = knje355Query::getLmst($this, $this->field["L_CD"]);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

            Query::dbCheckIn($db);

            //重複チェック
            if ($this->cmd == "add" && is_array($row)){
                $this->setWarning("MSG918","(大分類コード)");
                return false;
            }
            if (strlen($this->field["L_CD"]) > 2){
                $this->setWarning("MSG915","(大分類コードは2バイトまでです。)");
                return false;
            } else if (strlen($this->field["L_NAME"]) > 75){
                $this->setWarning("MSG915","(大分類名称は75バイトまでです。)");
                return false;
            } else if (strlen($this->field["L_ABBV"]) > 75){
                $this->setWarning("MSG915","(大分類略称は75バイトまでです。)");
                return false;
            }
        }
        return true;
    }

    function &getCopyModel() {
        $result = knje355Query::getCopyQuery($this);
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
        }
        return;
    }

    function &insert() {
        $result = knje355Query::getInsertQuery($this, $this->field);
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
        $result = knje355Query::getUpdateQuery($this, $this->field);
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
        $result = knje355Query::getDeleteQuery($this, $this->field);
        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
            $this->field = array();
            $this->l_cd = "";
        }
    }

    //変数をクリアします
    function clean()
    {
    }
}
?>
