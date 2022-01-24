<?php

require_once('for_php7.php');

//定数定義
class knjz032oModel extends Model {
    var $field      = array();
    var $cmd;
    var $testdiv;       //primary
    var $year;          //入試年度
    var $NextYear;
    
    //メンバ変数の初期化
    function knjz032oModel()
    {
    }

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->testdiv);
        }

        $temp = VARS::request("year");
        if (isset($temp)) {
            $this->year = VARS::request("year");
        }

        $this->field = array("ENTEXAMYEAR"      =>  $this->year,                            //入試年度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),                  //入試区分
                             "NAME"             =>  VARS::post("NAME"),                     //名称
                             "ABBV"             =>  VARS::post("ABBV"),                     //略称
                             "CLASSDIV"         =>  VARS::post("CLASSDIV"),                 //クラス区分
                             "SHOWDIV"          =>  VARS::post("SHOWDIV"),                  //表示区分
                             "CAPACITY"         =>  VARS::post("CAPACITY"),                 //定員
                             "TESTDAY"          =>  VARS::post("TESTDAY"),                  //入試日
                             "SHOWORDER"        =>  VARS::post("SHOWORDER"),                //表示順
                             "KATENDIV"         =>  VARS::post("KATENDIV"),                 //クラス区分
                             "UPDATED"          =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");

        $temp = VARS::get("TESTDIV");
        if (isset($temp)) {
            $this->testdiv = VARS::get("TESTDIV");
        }
    }



    //入力項目チェック
    function validateInput($kind)
    {
        //空文字チェック
        if (!strlen($this->field["TESTDIV"])) {
            $this->setWarning("MSG301","( 入試区分 )");
            return false;
        }
        if (!strlen($this->field["NAME"])) {
            $this->setWarning("MSG301","( 名称 )");
            return false;
        }

        //キー値が変更されているかをチェック
        if (($this->field["TESTDIV"] != $this->testdiv) && ($kind=="update" || $kind=="delete")) {
            $this->setWarning("MSG308");
            return false;
        }

        //１レコード取得
        $row = knjz032oQuery::getRow($this->field["ENTEXAMYEAR"],$this->field["TESTDIV"]);

        //重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning("MSG918","( 入試区分 )");
            return false;
        }
        //存在チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
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

        //文字数チェック
        if (strlen($this->field["NAME"]) > 60) {
            $this->setWarning("MSG915","( 名称は60バイトまでです。)");
            return false;
        }
        if (strlen($this->field["ABBV"]) > 30) {
            $this->setWarning("MSG915","( 略称は30バイトまでです。)");
            return false;
        }

        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")) {
            knjz032oQuery::getUpdateQuery($this->field);
            $this->setMessage("MSG201");
        }
        return;
    }
    
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")) {
            knjz032oQuery::getInsertQuery($this->field);
            $this->setMessage("MSG201");
            $this->testdiv = $this->field["TESTDIV"];
        }
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")) {
            knjz032oQuery::getDeleteQuery($this->field);
            $this->setMessage("MSG201");
            $this->testdiv = "";
        }
        return;
    }  

    //次年度のデータを作成
    function &getCopyYearModel()
    {
        $this->NextYear = $this->year + 1;
        if (knjz032oQuery::getCopyYearQuery($this)) {
            $this->year = $this->NextYear;
            $this->setMessage("MSG201");
        } else {
            $this->setMessage("MSG203", "\\n作成するデータが存在しませんでした。\\n既に作成されています。");
        }
    }

    //変数をクリアします
    function clean()
    {
    }
}
?>
