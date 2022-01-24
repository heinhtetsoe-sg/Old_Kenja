<?php

require_once('for_php7.php');
//定数定義

class knjz400j_2Model extends Model {
    var $field      = array();
    var $cmd;
    var $viewcd="";        //primary
    var $year_code = "";

    //メンバ変数の初期化
    function knjz400j_2Model()
    {
    }

    function init()
    {
        $mid ="" ;
        //コードのゼロ埋め
        if (VARS::post("VIEWCD") != "") {
             $mid = sprintf("%02d",VARS::post("VIEWCD"));
        }

        $this->field = array("VIEWCD"         =>  VARS::post("CLASSCD").$mid,           //観点コード
                            "VIEWNAME"        =>  VARS::post("VIEWNAME"),               //観点名
                            "SHOWORDER"       =>  VARS::post("SHOWORDER"),              //表示順
                            "UPDATED"         =>  VARS::post("UPDATED"),                //更新日付
                            "year_code"       =>  VARS::post("year_code")               //前画面の年度情保持
                            );

        $this->cmd             = VARS::request("cmd");
        if (VARS::get("year_code")){
            unset($this->viewcd);
        }
        if (VARS::get("mode") == "1"){
            unset($this->viewcd);
        }
        if (VARS::get("VIEWCD")!=""){
            $this->viewcd = VARS::get("VIEWCD");
        }
    }

    //入力項目チェック
    function validateInput($kind)
    {
        //１レコード取得
        $row = knjz400j_2Query::getRow($this->field["VIEWCD"]);
        if ($kind=="update" || $kind=="delete") {                 //リストより選択していない
            if ($this->viewcd==""){
                $this->setWarning("MSG308","キー値は変更できません");
                return false;
            }
            //キー値が変更されていますかをチェック
            if ($this->field["VIEWCD"] != $this->viewcd){
                $this->setWarning("MSG308","キー値は変更できません");
                return false;
            }
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
            if (trim($this->field["VIEWCD"]) < 100) {
                $this->setWarning("MSG301","(観点コード)");
                return false;
            }
            if (substr($this->field["VIEWCD"] ,2) == 99) {
                $this->setWarning("MSG901","(観点コード：99は使用不可です。99以外を入力して下さい。)");
                return false;
            }
            //重複チェック
            if ($this->cmd == "add" && is_array($row)){
                $this->setWarning("MSG918","(観点コード)");
                return false;
            }
            if (strlen($this->field["VIEWNAME"]) >75 ) {
                $this->setWarning("MSG915","(観点名は75バイトまでです)");
                return false;
            } elseif (strlen($this->field["VIEWNAME"])==0){
                $this->setWarning("MSG301","(観点名称)");
                return false;
            } elseif (strlen($this->field["SHOWORDER"]) >2){ 
                $this->setWarning("MSG915","(表示順)");
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
            $this->viewcd = $this->field["VIEWCD"];
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
        if (!isset($this->viewcd)) {
            $this->setWarning("MSG303");
            return;
        }
        $result = knjz400j_2Query::getDeleteQuery($this->field);
        if ($result==true) {
            if ( !isset($model->warning)){
                $this->setMessage("MSG201");
            }
            $this->viewcd ="";
        } else {
            $this->setWarning("MSG919","年度データに登録されています。");
        }
        return $result;
    }
    function &insert()
    {
        $result = knjz400j_2Query::getInsertQuery($this->field);
        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
        }
        return $result;
    }

    //アップデート時の内部メソッド
    function &update()
    {
        $result = knjz400j_2Query::getUpdateQuery($this->field);
        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
        }
/*        if ($result==true){
            //更新は成功すると、新しい日付を保留
            $row = knjz400j_2Query::getRow($this->field["VIEWCD"]);
//            $this->field["UPDATED"] = $row["UPDATED"];
        } */
        return $result;
    }
    //変数をクリアします
    function clean()
    {
    }
}
?>
