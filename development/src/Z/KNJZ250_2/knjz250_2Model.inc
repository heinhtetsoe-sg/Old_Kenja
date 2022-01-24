<?php

require_once('for_php7.php');

//定数定義

class knjz250_2Model extends Model {
    var $field      = array();
    var $cmd;
    var $certif_kindcd = "";        //primary
    var $year_code;

    //メンバ変数の初期化
    function knjz250_2Model()
    {
    }
    function init()
    {
        //プロパティファイル
        $this->getPropertiesAll();

        $this->field = array("CERTIF_KINDCD"  =>  VARS::post("CERTIF_KINDCD"),       //証明書種類コード
                            "KINDNAME"        =>  VARS::post("KINDNAME"),            //証明書種類名
                            "ISSUECD"         =>  VARS::post("ISSUECD"),             //事務発行区分
                            "STUDENTCD"       =>  VARS::post("STUDENTCD"),           //在学生
                            "GRADUATECD"      =>  VARS::post("GRADUATECD"),          //卒業生
                            "DROPOUTCD"       =>  VARS::post("DROPOUTCD"),           //退学者
                            "ELAPSED_YEARS"   =>  VARS::post("ELAPSED_YEARS"),       //卒業後の経過年数
                            "CERTIF_DIV"      =>  VARS::post("CERTIF_DIV"),          //証明書区分
                            "CURRENT_PRICE"   =>  VARS::post("CURRENT_PRICE"),       //在学生発行手数料
                            "GRADUATED_PRICE" =>  VARS::post("GRADUATED_PRICE"),     //卒業生発行手数料
                            "ISSUENO_AUTOFLG" =>  VARS::post("ISSUENO_AUTOFLG"),     //発行番号採番
                            "CERTIF_SCHOOL_KIND" => VARS::post("CERTIF_SCHOOL_KIND"),  //証明書種別校種
                            "UPDATED"         =>  VARS::post("UPDATED"),             //更新日付
                            "year_code"       =>  VARS::post("year_code")            //前画面の年度情保持
                            );

        $this->cmd              = VARS::request("cmd");
        if (VARS::request("year_code") !="") {
            $this->year_code   = VARS::request("year_code");
        }
        if (VARS::get("year_code")){
            unset($this->year_code);
            unset($this->certif_kindcd);
        }
        if (VARS::get("CERTIF_KINDCD")!=""){
            $this->certif_kindcd  = VARS::get("CERTIF_KINDCD");
        }
    }
    //入力項目チェック
    function validateInput($kind)
    {
        if (trim($this->field["CERTIF_KINDCD"]) == "") {
            $this->setWarning("MSG301","(証明書種類コード)");
            return false;
        }

        if ($kind=="update" || $kind=="delete") {
            //リストより選択していない
            if ($this->certif_kindcd==""){
                $this->setWarning("MSG308");
                return false;
            }
            //キー値が変更されていますかをチェック
            if ($this->certif_kindcd != $this->field["CERTIF_KINDCD"]){
                $this->setWarning("MSG308");
                return false;
            }

            //コードのゼロ埋め
            $this->field["CERTIF_KINDCD"] = sprintf("%03d",$this->field["CERTIF_KINDCD"]);
            //１レコード取得
            $row = knjz250_2Query::getRow($this->field["CERTIF_KINDCD"]);

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
            $this->field["CERTIF_KINDCD"] = sprintf("%03d",$this->field["CERTIF_KINDCD"]);
            //１レコード取得
            $row = knjz250_2Query::getRow($this->field["CERTIF_KINDCD"]);

            //重複チェック
            if ($this->cmd == "add" && is_array($row)){
                $this->setWarning("MSG918","(証明書種類コード)");
                return false;
            }
            if (strlen($this->field["KINDNAME"]) == 0){
                $this->setWarning("MSG301","(証明書種類名称)");
                return false;
            }
            if (strlen($this->field["KINDNAME"]) > 24) {
                $this->setWarning("MSG915","(証明書種類名称は24バイトまでです)");
                return false;
            }

            if ($this->Properties["certif_no_8keta"] == "1") {
                //桁数チェック
                if (strlen($this->field["CERTIF_DIV"]) > 1) {
                    $this->setWarning("MSG915","(証明書区分は1バイトまでです)");
                    return false;
                }

                if ($this->field["CERTIF_DIV"] == "0") {
                    $this->setWarning("MSG901","(証明書区分は1～9です。)");
                    return false;
                }
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
            $this->certif_kindcd = $this->field["CERTIF_KINDCD"];
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
        if (!isset($this->certif_kindcd)) {
            $this->setWarning("MSG303");
            return;
        }
        $result = knjz250_2Query::getDeleteQuery($this->field);
        if ($result==true) {
            if ( !isset($model->warning)){
                $this->setMessage("MSG201");
            }
            $this->certif_kindcd ="";
        } else {
            $this->setWarning("MSG919","年度データに登録されています。");
        }
        return $result;
    }
    function &insert()
    {
        $result = knjz250_2Query::getInsertQuery($this, $this->field);
        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
        }
        return $result;
    }

    //アップデート時の内部メソッド
    function &update()
    {
        $result = knjz250_2Query::getUpdateQuery($this, $this->field);
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
