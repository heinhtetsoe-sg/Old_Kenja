<?php

require_once('for_php7.php');


class knjj080_2Model extends Model {
    var $field      = array();
    var $cmd;
    var $committeecd = "";        //primary
    var $committee_flg = "";

    function knjj080_2Model()
    {
    }

    function init()
    {
        //プロパティファイル
        $this->getPropertiesAll();

        $this->field = array("COMMITTEE_FLG"    =>  VARS::post("COMMITTEE_FLG"),
                             "COMMITTEECD"      =>  VARS::post("COMMITTEECD"),
                             "COMMITTEENAME"    =>  VARS::post("COMMITTEENAME"),
                             "UPDATED"          =>  VARS::post("UPDATED")
                             );

        $this->cmd         = VARS::request("cmd");
        if (VARS::get("mode") == "1"){
            unset($this->committee_flg);
            unset($this->committeecd);
            unset($this->selectSchoolKind);
            unset($this->schKind);
        }
        $temp = VARS::get("COMMITTEE_FLG");
        if (isset($temp)){
            $this->committee_flg    = VARS::get("COMMITTEE_FLG");
        }
        $temp = VARS::get("COMMITTEECD");
        if (isset($temp)){
            $this->committeecd      = VARS::get("COMMITTEECD");
        }

        if (VARS::get("SEND_selectSchoolKind")) {
            $this->selectSchoolKind = VARS::get("SEND_selectSchoolKind");
        }
        $this->schKind = VARS::post("SCHKIND") ? VARS::post("SCHKIND") : $this->schKind;
    }
    //入力項目チェック
    function validateInput()
    {
        $db = Query::dbCheckOut();
        //キーのチェック
        if ((!strlen($this->field["COMMITTEECD"]) || !strlen($this->field["COMMITTEE_FLG"])) && $this->cmd == "add") {
            $this->setWarning("MSG301", "(委員会区分及び委員会コード)");
            return false;
        }
        if (!strlen($this->field["COMMITTEENAME"]) && ($this->cmd == "add" || $this->cmd == "update")) {
            $this->setWarning("MSG301","(委員会名称)");
            return false;
        }
        if ((!strlen($this->committee_flg) || !strlen($this->committeecd)) && $this->cmd == "update") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->cmd == "update" && ($this->committeecd != $this->field["COMMITTEECD"] || $this->committee_flg != $this->field["COMMITTEE_FLG"])) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
        //コードのゼロ埋め
        if (strlen($this->field["COMMITTEECD"]) < 4) {	/* NO001 */
            $this->field["COMMITTEECD"] = sprintf("%02d", $this->field["COMMITTEECD"]);
        }
        //１レコード取得
        $query = knjj080_2Query::getRow($this, $this->field["COMMITTEECD"], $this->field["COMMITTEE_FLG"]);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918", "(委員会コード)");
            return false;
        }
        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]){
            $this->setWarning("MSG307");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["COMMITTEENAME"]) > 45 ) {
            $this->setWarning("MSG915", "(委員会名称) 45バイトまで");
            return false;
        }
        Query::dbCheckIn($db);
        return true;
    }
    /**
     * 編集完了用のモデルを返します
    */
    function &getUpdateModel()
    {
    //入力チェック
        if ($this->validateInput()){
            $this->update();
            $this->setMessage("MSG201");
        }
    }
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->insert();
            $this->committee_flg = $this->field["COMMITTEE_FLG"];
            $this->committeecd  = $this->field["COMMITTEECD"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        $db = Query::dbCheckOut();
        //キーの更新チェック
        if (!strlen($this->committee_flg) || !strlen($this->committeecd)) {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->committeecd != $this->field["COMMITTEECD"] || $this->committee_flg != $this->field["COMMITTEE_FLG"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
        $query = knjj080_2Query::getRow($this, $this->field["COMMITTEECD"], $this->field["COMMITTEE_FLG"]);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if (!is_array($row)){
            $this->setWarning("MSG303");
            return;    
        }
        if (knjj080_2Query::IsExisting($this, $this->field["COMMITTEECD"], $this->field["COMMITTEE_FLG"])){
            $this->setWarning("MSG919","年度データに登録されています。");
            return;
        }
        Query::dbCheckIn($db);

        knjj080_2Query::getDeleteQuery($this, $this->field);
        $this->committee_flg = "";
        $this->committeecd = "";
        $this->setMessage("MSG201");
    }

    function &insert()
    {
        $result = knjj080_2Query::getInsertQuery($this, $this->field);
    }
    //アップデート時の内部メソッド
    function &update()
    {
        $result = knjj080_2Query::getUpdateQuery($this, $this->field);
    }
    //変数をクリアします
    function clean()
    {
    }
}
?>
