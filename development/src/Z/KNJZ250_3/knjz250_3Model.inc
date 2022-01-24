<?php

require_once('for_php7.php');

//定数定義

class knjz250_3Model extends Model {
    var $field      = array();
    var $cmd;
    var $certif_kindcd = "";        //primary
    var $year_code;
    var $selectdata;
    var $certif_div;

    //メンバ変数の初期化
    function knjz250_3Model()
    {
            unset($this->certif_div);
    }
    function init()
    {
        //プロパティファイル
        $this->getPropertiesAll();

        $this->field = array("CERTIF_DIV"      =>  VARS::post("CERTIF_DIV"),       //証明書区分コード
                             "CERTIF_DIV_NAME" =>  VARS::post("CERTIF_DIV_NAME"),  //証明書区分名
                             "KINDNAME"        =>  VARS::post("KINDNAME"),         //証明書名
                             "SEND_FLG"        =>  VARS::get("SEND_FLG")           //更新時フラグ
                            );

        $this->cmd              = VARS::request("cmd");
        if (VARS::request("year_code") !="") {
            $this->year_code   = VARS::request("year_code");
        }
        if (VARS::get("year_code")){
            unset($this->year_code);
            unset($this->certif_div);
        }
        if (VARS::get("CERTIF_DIV")!=""){
            $this->certif_div  = VARS::get("CERTIF_DIV");
            $this->field["CERTIF_DIV"] = $this->certif_div;
        }

        unset($this->selectdata);

        if (VARS::post("selectdata") != NULL) {
            $this->selectdata = explode(',', VARS::post("selectdata"));
        }

    }
    //入力項目チェック
    function validateInput($kind)
    {
        if (trim($this->field["CERTIF_DIV"]) == "") {
            $this->setWarning("MSG301","(証明書区分コード)");
            return false;
        }
        if (trim($this->field["CERTIF_DIV_NAME"]) == "") {
            $this->setWarning("MSG301","(証明書区分名称)");
            return false;
        }

        if ($kind == "update" || $kind == "delete") {
            //リストより選択していない
            if ($this->certif_div==""){
                $this->setWarning("MSG308");
                return false;
            }
            //キー値が変更されているかをチェック
            if ($this->certif_div != $this->field["CERTIF_DIV"]) {
                $this->setWarning("MSG308");
                return false;
            }
        }
        if ($kind == "update" || $kind == "insert") {
            //１レコード取得
            $row = knjz250_3Query::getRow($this->field["CERTIF_DIV"]);
            //重複チェック
            if ($kind == "insert" && is_array($row)) {
                $this->setWarning("MSG918","(証明書区分コード)");
                return false;
            }

            //証明書区分コードのチェック
            //桁数チェック
            if (strlen($this->field["CERTIF_DIV"]) > 1) {
                $this->setWarning("MSG915","(証明書区分は1バイトまでです)");
                return false;
            }

            //証明書区分入力チェック
            if (strlen($this->field["CERTIF_DIV"]) == 0) {
                $this->setWarning("MSG304","(証明書区分)");
                return false;
            }

            if ($this->field["CERTIF_DIV"] == "0") {
                $this->setWarning("MSG901","(証明書区分は1～9です。)");
                return false;
            }

            //証明書区分名称のチェック
            if (strlen($this->field["CERTIF_DIV_NAME"]) > 0){
                if (strlen($this->field["CERTIF_DIV_NAME"]) > 60) {
                    $this->setWarning("MSG915","(証明書種類名称は60バイトまでです)");
                    return false;
                }
            }
        }
        return true;
    }

    //追加ボタン
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput("insert")){
            $this->insert();
            $this->updateCertifDiv();
            $this->setMessage("MSG201");
            $this->certif_div = $this->field["CERTIF_DIV"];
        }
        return;
    }

    //更新ボタン
    //編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput("update")){
            $this->update();
            $this->clearCertifDiv();
            $this->updateCertifDiv();
            $this->setMessage("MSG201");
        }
    }

    //削除ボタン
    //削除用のモデルを返します
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput("delete")){
            $this->delete();
            $this->clearCertifDiv();
            $this->setMessage("MSG201");
            unset($this->certif_div);
            unset($this->field);
        }
    }

    function &insert() {
        $result = knjz250_3Query::getInsertQuery($this, $this->field);
        return $result;
    }

    function &update() {
        $result = knjz250_3Query::getUpdateQuery($this, $this->field);
        return $result;
    }

    function &delete() {
        $result = knjz250_3Query::getDeleteQuery($this, $this->field);
        return $result;
    }

    function &clearCertifDiv() {
        $result = knjz250_3Query::clearCertifDivQuery($this->field);
        return $result;
    }

    function &updateCertifDiv() {
        if (is_array($this->selectdata)) {
            for ($i = 0; $i < get_count($this->selectdata); $i++){
                $result = knjz250_3Query::updateCertifDivQuery($this->field, $this->selectdata[$i]);
            }
        }
        return $result;
    }

    //変数をクリアします
    function clean()
    {
    }
}
?>
