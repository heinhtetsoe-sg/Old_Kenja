<?php

require_once('for_php7.php');

//定数定義
class knjj180Model extends Model {
    var $field      = array();
    var $cmd;
    var $branchcd;        //primary
    //メンバ変数の初期化
    function knjj180Model() {
    }
    function init() {
        $this->field = array("BRANCHCD"     =>  VARS::post("BRANCHCD"),     //支部コード
                             "BRANCHNAME"   =>  VARS::post("BRANCHNAME"),   //支部名称
                             "ABBV"         =>  VARS::post("ABBV"),         //支部略称
                             "UPDATED"      =>  VARS::post("UPDATED")
                             );

        $this->cmd             = VARS::request("cmd");
        if (VARS::get("year_code")) {
            unset($this->branchcd);
        }
        if (VARS::get("PROGRAMID")) {
           $this->branchcd = "";
        }
        if (VARS::get("BRANCHCD")!="") {
            $this->branchcd    = VARS::get("BRANCHCD");
        }
    }
    //入力項目チェック
    function validateInput($kind) {
        if (trim($this->field["BRANCHCD"]) == "") {
            $this->setWarning("MSG301","(支部コード)");
            return false;
        }

        if ($kind=="update" || $kind=="delete") {
            //リストより選択していない
            if ($this->branchcd=="") {
                $this->setWarning("MSG308");
                return false;
            }
            //キー値が変更されていますかをチェック
            if ($this->field["BRANCHCD"] !=$this->branchcd) {
                $this->setWarning("MSG308","キー値は変更できません");
                return false;
            }

            //コードのゼロ埋め
            $this->field["BRANCHCD"] = sprintf("%02d",$this->field["BRANCHCD"]);
            //１レコード取得
            $row = knjj180Query::getRow($this->field["BRANCHCD"]);

            //更新日付チェック1
            if ($this->cmd == "update" && $row["UPDATED"] == "") {
                $this->setWarning("MSG303");
                return false;
            }
            //更新日付チェック2
            if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]) {
                $this->setWarning("MSG307");
                return false;
            }
        }
        if ($kind=="update" || $kind=="insert") {

            //コードのゼロ埋め
            $this->field["BRANCHCD"] = sprintf("%02d",$this->field["BRANCHCD"]);
            //１レコード取得
            $row = knjj180Query::getRow($this->field["BRANCHCD"]);

            //重複チェック
            if ($this->cmd == "add" && is_array($row)) {
                $this->setWarning("MSG918","(支部コード)");
                return false;
            }
            if (strlen($this->field["BRANCHNAME"]) >75 ) {
                $this->setWarning("MSG915","(支部名称は75バイトまでです。)");
                return false;
            } elseif (strlen($this->field["BRANCHNAME"])==0) { 
                $this->setWarning("MSG301","(支部名称)");
                return false;
            } elseif (strlen($this->field["ABBV"]) >75) { 
                $this->setWarning("MSG915","(支部略称は75バイトまでです。)");
                return false;
            }
        }
        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput("update")) {
            $this->update();
        }
    }
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput("insert")) {
            $this->insert();
            $this->branchcd = $this->field["BRANCHCD"];
        }
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput("delete")) {
            $this->delete();
        }
        return;
    }
    function &delete() {
        if (!isset($this->branchcd)) {
            $this->setWarning("MSG303");
            return;
        }
        $result = knjj180Query::getDeleteQuery($this->field);
        if ( !isset($model->warning)) {
            $this->setMessage("MSG201");
            $this->field = array();
        }
    }

    function &insert() {
        $result = knjj180Query::getInsertQuery($this->field);
        if ( !isset($model->warning)) {
            $this->setMessage("MSG201");
        }
        return $result;
    }

    //アップデート時の内部メソッド
    function &update() {
        $result = knjj180Query::getUpdateQuery($this->field);
        if ( !isset($model->warning)) {
            $this->setMessage("MSG201");
        }
        return $result;
    }
    //変数をクリアします
    function clean()
    {
    }
}
?>
