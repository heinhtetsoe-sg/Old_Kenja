<?php

require_once('for_php7.php');

class knjz015Model extends Model {
    var $cmd;
    var $field;
    var $sort;                  //昇順,降順
    var $sec_competence;        //権限保持
    var $selectdata;

    function knjz015Model() {
        //ソート処理の初期化
        $this->sort["SRT_S"] = 1;
        $this->sort["SRT_P"] = "";
    }

    function init() {

        //セキュリティーチェック
        $this->staffcd = STAFFCD;
        $this->sec_competence = common::SecurityCheck($this->staffcd, "KNJZ015");

        if (VARS::get("PROGRAMID")) {
            $this->prgId    = "";
            $this->schkind  = "";
        }

        $this->field = array("SCHKIND"                 =>  VARS::post("SCHKIND"),       //校種
                             "PRGIDLIST"               =>  VARS::post("PRGIDLIST")      //プログラムID
                             );

        $this->cmd  = VARS::request("cmd");

        if (VARS::get("USEPROGRAMID")!="") {
            $this->prgId   = VARS::get("USEPROGRAMID");
        }
        if (VARS::get("SCHKIND")!="") {
            $this->schkind   = VARS::get("SCHKIND");
        }

        if ($this->cmd == "change") {
            $this->prgId    = "";
            $this->schkind  = "";
        }

        //左リスト
        $this->selectdata = VARS::post("selectdata");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    function &getMainModel() {
        //ソート用 番号、席次
        $sort = array("SRT_S","SRT_P");
        //ソートが押下
        if (trim(VARS::request("sort")) != ""){
            foreach($sort as $field){
                if (VARS::request("sort") == $field
                        && is_numeric($this->sort[VARS::get("sort")])
                        && $this->sort[VARS::get("sort")] != 0) {
                    //昇順⇔降順
                    $this->sort[$field] = $this->sort[$field] *(-1);
                }elseif (VARS::get("sort") != $field) {
                    unset($this->sort[$field]);
                }else{
                    //昇順
                    $this->sort[$field] = 1;
                }
            }
        }

        if (VARS::get("sort") == "SRT_S") {
            $this->sort["SRT_P"] = "";
        } elseif (VARS::get("sort") == "SRT_P") {
            $this->sort["SRT_S"] = "";
        }
    }

    //更新前チェック
    function validateInput() {
        if($this->field["SCHKIND"] == ""){
            $this->setWarning("MSG301","※校種");
            return false;
        }
        if($this->field["PRGIDLIST"] == ""){
            $this->setWarning("MSG301","※プログラムID");
            return false;
        }
        return true;
    }

    //更新用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->setMessage('MSG201');
        }
    }
    //アップデート時の内部メソッド
    function &update() {
        knjz015Query::getUpdateQuery($this);
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        //リストより選択してるか
        if ($this->prgId == "" || $this->schkind = "") {
            $this->setWarning("削除はリストよりデータを選択してから行ってください。");
            return false;
        }
        knjz015Query::getDeleteQuery($this);
        $this->prgId   = "";
        $this->schkind = "";
        $this->setMessage("MSG201");
    }

    //変数をクリアします
    function clean() {
    }
}
?>
