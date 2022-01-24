<?php

require_once('for_php7.php');

class knjp190qModel extends Model {

    var $field        = array();
    var $cmd;
    var $schregno;
    var $control_data = array();    //コントロールマスタデータを保持
    var $sec_competence;            //セキュリティーチェック

    function knjp190qModel()
    {
        common::GetControlMaster_Fnc(&$this->control_data);
    }

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->schregno);
            unset($this->grantcd);
        }

        $this->cmd       = VARS::request("cmd");

        $this->field = array("GRANTCD"      =>  VARS::post("GRANTCD"),      //交付コード
                             "GRANTYEAR"    =>  VARS::post("GRANTYEAR"),    //交付年度
                             "GRANTSDATE"   =>  VARS::post("GRANTSDATE"),   //交付開始日
                             "GRANTEDATE"   =>  VARS::post("GRANTEDATE"),   //交付終了日
                             "GRANT_MONEY"  =>  VARS::post("GRANT_MONEY"),  //交付金額
                             "temp_cd"      =>  VARS::post("temp_cd"),
                             "REMARK"       =>  VARS::post("REMARK"),       //備考
                             "UPDATED"      =>  VARS::post("UPDATED")
                             );

        $this->schregno = VARS::request("SCHREGNO");
        $this->grantcd  = substr(VARS::request("GRANTCD"),0,2);

        if($this->cmd == "clear"){
            $this->grantcd = $model->temp_cd;
        }
    }

    //入力項目チェック
    function validateInput($kind)
    {
       if ($this->schregno=="") {
           $this->setWarning("MSG304","(左より生徒を選択してから行ってください。)");
           return false;
       }

        if($kind == "insert" || $kind == "update"){
            //空文字チェック
            if(strlen($this->field["GRANTCD"]) == 0){
                $this->setWarning("MSG301","(交付コード)");
                return false;
            }
            if(strlen($this->field["GRANTYEAR"]) == 0){
                $this->setWarning("MSG301","(交付年度)");
                return false;
            }
            if(strlen($this->field["GRANTSDATE"]) == 0){
                $this->setWarning("MSG301","(交付開始日)");
                return false;
            }
            //文字数チェック
            if (strlen($this->field["RANTYEAR"]) > 4) {
                $this->setWarning("MSG915","(交付年度は4バイトまでです。)");
                return false;
            }
            if (strlen($this->field["GRANT_MONEY"]) > 9) {
                $this->setWarning("MSG915","(交付金額は9バイトまでです。)");
                return false;
            }
            if (strlen($this->field["REMARK"]) > 75) {
                $this->setWarning("MSG915","(備考は75バイトまでです。)");
                return false;
            }

            //開始日と終了日の大小
            if(($this->field["GRANTSDATE"] && $this->field["GRANTEDATE"]) && ($this->field["GRANTSDATE"] > $this->field["GRANTEDATE"])){
                $this->setWarning("MSG913","(交付開始日,交付終了日)");
                return false;
            }

            //入学日付、卒業日付を取得
            $eg_year = array();
            $eg_year = knjp190qQuery::getEgYear($this->schregno);

            $grantsdate = str_replace("/", "", $this->field["GRANTSDATE"]);					# 交付開始日
            $grantedate = str_replace("/", "", $this->field["GRANTEDATE"]);					# 交付終了日
            $ent_date   = str_replace("-", "", substr($eg_year["ENT_DATE"],0,4)."-04-01");	# 入学日付 NO001
            $grd_date   = str_replace("-", "", $eg_year["GRD_DATE"]);						# 除籍(卒業)日付

            //入力チェック交付年度
            if($this->field["GRANTYEAR"]){
                if(($this->field["GRANTYEAR"] < $eg_year["EYEAR"]) || ($eg_year["GYEAR"] < $this->field["GRANTYEAR"])){
                    $this->setWarning("MSG913","(交付年度)");
                    return false;
                }
            }
            //入力チェック交付開始日
            if($grantsdate){
                if(($grantsdate < $ent_date) || ($grd_date < $grantsdate)){
                    $this->setWarning("MSG913","(交付開始日)");
                    return false;
                }
            }
            //入力チェック交付終了日
            if($grantedate){
                if(($grantedate < $ent_date) || ($grd_date < $grantedate)){
                    $this->setWarning("MSG913","(交付終了日)");
                    return false;
                }
            }
        }

        //１レコード取得
        $row = knjp190qQuery::getRow($this->field["GRANTCD"],$this->schregno);

        //重複チェック
        if($kind=="insert" && is_array($row)){
            $this->setWarning("MSG918");
            return false;
        }

        if($kind=="update" || $kind=="delete"){
            //リストより選択していない
            if($this->field["GRANTCD"]==""){
                $this->setWarning("MSG308");
                return false;
            }
            //キー値が変更されていますかをチェック
            if($this->field["GRANTCD"] != $this->field["temp_cd"]){
                $this->setWarning("MSG308","キー値が変更されています。");
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
        return true;
    }

    //更新
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")){
            $result = knjp190qQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }

    //追加
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")){
            $result = knjp190qQuery::getInsertQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")){
            $result = knjp190qQuery::getDeleteQuery($this);
            $this->field = array();
            $this->setMessage("MSG201");
        }
        return;
    }
}
?>
