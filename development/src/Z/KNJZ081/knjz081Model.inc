<?php

require_once('for_php7.php');

//定数定義
class knjz081Model extends Model {
    var $field      = array();
    var $cmd;
    var $edboard_schoolcd;        //primary
    var $auth;
    var $prgid;

    //メンバ変数の初期化
    function knjz081Model()
    {
    }
    function init()
    {
        $this->field = array("EDBOARD_SCHOOLCD"     =>  VARS::post("EDBOARD_SCHOOLCD"),        //学校コード
                             "EDBOARD_SCHOOLNAME"   =>  VARS::post("EDBOARD_SCHOOLNAME"),      //学校名
                             "LINK_ADDR"            =>  VARS::post("LINK_ADDR"),               //アドレス
                             "DBNAME"               =>  VARS::post("DBNAME"),                  //DB名
                             "UPDATED"              =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");
        
        if (VARS::get("year_code")){
            unset($this->edboard_schoolcd);
        }
        if (VARS::get("PROGRAMID")) {
            $this->edboard_schoolcd = "";

            unset($this->sendAuth);
            unset($this->sendPrgid);
            unset($this->sendSubmit);
        }
        if (VARS::get("EDBOARD_SCHOOLCD")!="") {
            $this->edboard_schoolcd    = VARS::get("EDBOARD_SCHOOLCD");
        }

        if (VARS::get("SEND_PRGID")) {
            $this->sendAuth     = VARS::get("SEND_AUTH");
            $this->sendPrgid    = VARS::get("SEND_PRGID");
            $this->sendSubmit   = VARS::get("SEND_SUBMIT");
        }

        $this->auth = ($this->sendAuth == "") ? AUTHORITY : $this->sendAuth;
        $this->prgid = ($this->sendPrgid == "") ? PROGRAMID : $this->sendPrgid;
    }
    //入力項目チェック
    function validateInput($kind)
    {
        if (trim($this->field["EDBOARD_SCHOOLCD"]) == ""){
            $this->setWarning("MSG301","(学校コード)");
            return false;
        }

        if ($kind=="update" || $kind=="delete"){
            //リストより選択していない
            if ($this->edboard_schoolcd==""){
                $this->setWarning("MSG308");
                return false;
            }
            //キー値が変更されていますかをチェック
            if ($this->field["EDBOARD_SCHOOLCD"] != $this->edboard_schoolcd){
                $this->setWarning("MSG308","キー値は変更できません");
                return false;
            }

            //コードのゼロ埋め
            $this->field["EDBOARD_SCHOOLCD"] = sprintf("%04d",$this->field["EDBOARD_SCHOOLCD"]);
            //１レコード取得
            $row = knjz081Query::getRow($this->field["EDBOARD_SCHOOLCD"]);

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
        if ($kind=="update" || $kind=="insert"){

            //コードのゼロ埋め
            $this->field["EDBOARD_SCHOOLCD"] = sprintf("%04d",$this->field["EDBOARD_SCHOOLCD"]);
            //１レコード取得
            $row = knjz081Query::getRow($this->field["EDBOARD_SCHOOLCD"]);

            //重複チェック
            if ($this->cmd == "add" && is_array($row)){
                $this->setWarning("MSG918","(学校コード)");
                return false;
            }
            if (strlen($this->field["EDBOARD_SCHOOLCD"]) > 4 ){
                $this->setWarning("MSG915","(学校コードは4バイトまでです。)");
                return false;
            } elseif (strlen($this->field["EDBOARD_SCHOOLCD"])==0){ 
                $this->setWarning("MSG301","(学校コード)");
                return false;
            }
            if (strlen($this->field["EDBOARD_SCHOOLNAME"]) >60 ){
                $this->setWarning("MSG915","(学校名は60バイトまでです。)");
                return false;
            } elseif (strlen($this->field["EDBOARD_SCHOOLNAME"])==0){ 
                $this->setWarning("MSG301","(学校名)");
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
            $this->edboard_schoolcd = $this->field["EDBOARD_SCHOOLCD"];
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
        if (!isset($this->edboard_schoolcd)) {
            $this->setWarning("MSG303");
            return;
        }
        $result = knjz081Query::getDeleteQuery($this->field);
        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
            $this->field = array();
        }
    }
    function &insert()
    {
        $result = knjz081Query::getInsertQuery($this->field);
        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
        }
        return $result;
    }

    //アップデート時の内部メソッド
    function &update()
    {
        $result = knjz081Query::getUpdateQuery($this->field);
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
