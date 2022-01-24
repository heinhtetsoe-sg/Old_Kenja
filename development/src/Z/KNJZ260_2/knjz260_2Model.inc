<?php

require_once('for_php7.php');

//定数定義

class knjz260_2Model extends Model {
    var $field      = array();
    var $cmd;
    var $dutysharecd;        //primary
    var $year_code;

    //メンバ変数の初期化
    function knjz260_2Model()
    {
    }
    function init()
    {              
        $this->field = array("DUTYSHARECD"     =>  VARS::post("DUTYSHARECD"),       //校務分掌コード
                            "SHARENAME"        =>  VARS::post("SHARENAME"),         //校務分掌名
                            "BASE_SHARENAME"   =>  VARS::post("BASE_SHARENAME"),    //学校基本調査名称
                            "UPDATED"          =>  VARS::post("UPDATED"),           //更新日付
                            "year_code"        =>  VARS::post("year_code")          //前画面の年度情保持
                            );

        $this->cmd              = VARS::request("cmd");
        if (VARS::get("year_code")){
            unset($this->dutysharecd);
        }
        if (VARS::get("mode") == "1"){
            unset($this->dutysharecd);
        }
        if (VARS::get("DUTYSHARECD")!=""){
            $this->dutysharecd  = VARS::get("DUTYSHARECD");
        }
        if (VARS::request("year_code")!="") {
            $this->year_code    = VARS::request("year_code");
        }
    }
    //入力項目チェック
    function validateInput($kind)
    {
        if (trim($this->field["DUTYSHARECD"]) == "") {
            $this->setWarning("MSG301","(校務分掌部コード)");
            return false;
        }

        if ($kind=="update" || $kind=="delete") {
            //リストより選択していない
            if ($this->dutysharecd==""){
                $this->setWarning("MSG308");
                return false;
            }
            //キー値が変更されていますかをチェック
            if ($this->field["DUTYSHARECD"] != $this->dutysharecd){
                $this->setWarning("MSG308","キー値は変更できません");
                return false;
            }

            //コードのゼロ埋め
            $this->field["DUTYSHARECD"] = sprintf("%04d",$this->field["DUTYSHARECD"]);
            //１レコード取得
            $row = knjz260_2Query::getRow($this->field["DUTYSHARECD"]);

            //更新日付チェック1
            if ($this->cmd == "update" && $row["UPDATED"] == ""){
                $this->setWarning("MSG303");
                return false;
            }
            //更新日付チェック2
            if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]){
                $this->setWarning("MSG307");
                return false;
            }
        }
        if ($kind=="update" || $kind=="insert") {

            //コードのゼロ埋め
            $this->field["DUTYSHARECD"] = sprintf("%04d",$this->field["DUTYSHARECD"]);
            //１レコード取得
            $row = knjz260_2Query::getRow($this->field["DUTYSHARECD"]);

            //重複チェック
            if ($this->cmd == "add" && is_array($row)){
                $this->setWarning("MSG918","(校務分掌部コード)");
                return false;
            }
            if (strlen($this->field["SHARENAME"]) == 0) {
                $this->setWarning("MSG301","(校務分掌部名称)");
                return false;
            }
            if (strlen($this->field["SHARENAME"]) >24 ) {
                $this->setWarning("MSG915","(分掌部名称は24バイトまでです)");
                return false;
            }
            if (strlen($this->field["BASE_SHARENAME"]) > 24 ) {
                $this->setWarning("MSG915", "(学校基本調査名称は24バイトまでです");
                return false;
            }
        }
        return true;
    }
    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")){
            $this->update();
        }
    }
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")){
            $this->insert();
            $this->dutysharecd = $this->field["DUTYSHARECD"];
        }
        return;
    }
    //削除用のモデルを返します
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")){
            $this->delete();
        }
        return;
    } 
    function &delete()
    {
        if (!isset($this->dutysharecd)) {
            $this->setWarning("MSG303");
            return;
        }

        $result = knjz260_2Query::getDeleteQuery($this->field);
        if ($result==true) {
            if ( !isset($model->warning)){
                $this->setMessage("MSG201");
            }
            $this->field = array();
        } else {
            $this->setWarning("MSG919","年度データに登録されています。");
        }
        return $result;
    }
    function &insert()
    {
        $result = knjz260_2Query::getInsertQuery($this->field);
        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
        }
        return $result;
    }

    //アップデート時の内部メソッド
    function &update()
    {
        $result = knjz260_2Query::getUpdateQuery($this->field);
        if ( !isset($model->warning)){
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
