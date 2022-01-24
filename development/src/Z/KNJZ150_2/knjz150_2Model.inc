<?php

require_once('for_php7.php');
// kanji=漢字
// $Id: knjz150_2Model.inc 56580 2017-10-22 12:35:29Z maeshiro $
class knjz150_2Model extends Model {
    var $field      = array();
    var $cmd;
    var $textbookcd;        //primary

    function knjz150_2Model()
    {
    }

    function init()
    {              
        $this->field = array("TEXTBOOKCD"           => VARS::post("TEXTBOOKCD"),
                             "TEXTBOOKDIV"          => VARS::post("TEXTBOOKDIV"),
                             "TEXTBOOKNAME"         => VARS::post("TEXTBOOKNAME"),
                             "TEXTBOOKABBV"         => VARS::post("TEXTBOOKABBV"),
                             "TEXTBOOKWRITINGNAME"  => VARS::post("TEXTBOOKWRITINGNAME"),
                             "TEXTBOOKPRICE"        => VARS::post("TEXTBOOKPRICE"),
                             "TEXTBOOKUNITPRICE"    => VARS::post("TEXTBOOKUNITPRICE"),
                             "ISSUECOMPANY"         => VARS::post("ISSUECOMPANY"),
                             "CONTRACTORNAME"       => VARS::post("CONTRACTORNAME"),
                             "REMARK"               => VARS::post("REMARK"),
                             "UPDATED"              => VARS::post("UPDATED")
                             );

        $this->cmd          = VARS::request("cmd");
        if (VARS::get("mode") == "1"){
            unset($this->textbookcd);
        }
        $temp = VARS::get("TEXTBOOKCD");
        if (isset($temp)){
            $this->textbookcd      = VARS::get("TEXTBOOKCD");
        }
    }

   //入力項目チェック
    function validateInput()
    {
        //キーのチェック
        if (trim($this->field["TEXTBOOKCD"]) == "" && $this->cmd == "add") {
            $this->setWarning(MSG301,"(教科書コード)");
            return false;
        }
        if ($this->textbookcd == "" && $this->cmd == "update") {
           $this->setWarning(MSG308);
           return false;
        }
        if ($this->cmd == "update" && $this->textbookcd != $this->field["TEXTBOOKCD"]) {
            $this->setWarning(MSG308);
            return;
        }
        //１レコード取得
        $row = knjz150_2Query::getRow($this->field["TEXTBOOKCD"]);

        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning(MSG303);
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning(MSG918, "(教科書コード)");
            return false;
        }
        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]){
            $this->setWarning(MSG307);
            return false;
        }
        //文字数チェック
        if (strlen($this->field["TEXTBOOKNAME"]) == 0) {
            $this->setWarning(MSG301,"(教科書名称)");
            return false;
        } elseif (strlen($this->field["TEXTBOOKDIV"]) == 0 ) {
            $this->setWarning(MSG301,"(教科書区分)");
            return false;
        } elseif (strlen($this->field["TEXTBOOKDIV"]) > 1 ) {
            $this->setWarning(MSG915, "(教科書区分) 1バイトまで");
            return false;
        } elseif (strlen($this->field["TEXTBOOKNAME"]) > 90 ) {
            $this->setWarning(MSG915, "(教科書名称) 90バイトまで");
            return false;
        } elseif (strlen($this->field["TEXTBOOKABBV"]) > 15 ) {
            $this->setWarning(MSG915, "(教科書略称) 15バイトまで");
            return false;
        } elseif (strlen($this->field["TEXTBOOKWRITINGNAME"]) > 60 ) {
            $this->setWarning(MSG915, "(著作名) 60バイトまで");
            return false;
        } elseif(strlen($this->field["TEXTBOOKPRICE"]) > 5) {
            $this->setWarning(MSG915, "(定価) 最大値 32767まで");
            return false;
        } elseif($this->field["TEXTBOOKPRICE"] > 32767) {
            $this->setWarning(MSG915, "(定価)最大値 32767まで");
            return false;
        } elseif(strlen($this->field["TEXTBOOKUNITPRICE"]) > 5) {
            $this->setWarning(MSG915, "(単価) 最大値 32767まで");
            return false;
        } elseif($this->field["TEXTBOOKUNITPRICE"] > 32767) {
            $this->setWarning(MSG915, "(単価)最大値 32767まで");
            return false;
        } elseif (strlen($this->field["ISSUECOMPANY"]) > 45 ) {
            $this->setWarning(MSG915, "(発行社) 45バイトまで");
            return false;
        } elseif (strlen($this->field["CONTRACTORNAME"]) > 45 ) {
            $this->setWarning(MSG915, "(業者名) 45バイトまで");
            return false;
        } elseif (strlen($this->field["REMARK"]) > 60 ) {
            $this->setWarning(MSG915, "(備考) 60バイトまで");
            return false;
        }
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
            $this->setMessage(MSG201);
       }    
   }
   function &getInsertModel()
   {
       //入力チェック
       if ($this->validateInput()){
           $this->insert();
           $this->textbookcd = $this->field["TEXTBOOKCD"];
           $this->setMessage(MSG201);
       }
       return;
   }

    //DELETE
   function &getDeleteModel()
   {
        //キーの更新チェック
        if ($this->textbookcd == "") {
           $this->setWarning(MSG308);
           return false;
       }
        if ($this->textbookcd != $this->field["TEXTBOOKCD"]) {
            $this->setWarning(MSG308);
            return;
        }
        $row = knjz150_2Query::getRow($this->textbookcd);
        if (!is_array($row)){
            $this->setWarning(MSG303);
            return;
        }
       if (knjz150_2Query::IsExisting($this->textbookcd)){
            $this->setWarning(MSG919, "年度データに登録されています。");
            return;
        }
        knjz150_2Query::getDeleteQuery($this->field);
        unset($this->textbookcd);
        $this->setMessage(MSG201);
   }

    function &insert()
    {
        $result = knjz150_2Query::getInsertQuery($this->field);
        return $result;
    }

    /**
     * アップデート時の内部メソッド
     */
    function &update()
    {
        $result = knjz150_2Query::getUpdateQuery($this->field);
        return $result;
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
