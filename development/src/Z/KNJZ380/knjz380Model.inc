<?php

require_once('for_php7.php');


class knjz380Model extends Model {
    var $field        = array();
    var $cmd;
    var $staffcd;               //ログインユーザーID保持
    var $sec_competence;        //権限保持
    var $control_data;          //処理年度保持
    var $testkindcd;            //GETより取得
    var $testitemcd;            //GETより取得

    function knjz380Model()
    {
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init()
    {
        //セキュリティーチェック
        $this->staffcd = STAFFCD;
        $this->sec_competence = common::SecurityCheck($this->staffcd, "KNJZ380");

        $this->field = array("COUNTFLG"     =>  VARS::post("COUNTFLG"),
                            "TESTKINDCD"    =>  VARS::post("TESTKINDCD"),
                            "TESTITEMCD"    =>  VARS::post("TESTITEMCD"),
                            "TESTITEMNAME"  =>  VARS::post("TESTITEMNAME"),
                            "UPDATED"       =>  VARS::post("UPDATED")
                            );
        $this->cmd  = VARS::request("cmd");

        if(VARS::get("TESTKINDCD")){
            $this->testkindcd = VARS::get("TESTKINDCD");
        }
        if(VARS::get("TESTITEMCD")){
            $this->testitemcd = VARS::get("TESTITEMCD");
        }

        if(VARS::get("PROGRAMID")){
            $this->clean();
        }
    }

    //入力項目チェック
    function validateInput()
    {
/***NO001-->
        //値のチェック
        if (trim($this->field["TESTKINDCD"]) == "01" || trim($this->field["TESTKINDCD"]) == "02") {
            $this->field["TESTITEMCD"] = "01";
        }
<--NO001***/
        //入力チェック
        if (trim($this->field["TESTKINDCD"]) == "") {
            $this->setWarning('MSG301', "(テスト種別)");
            return false;
        }else if(trim($this->field["TESTITEMCD"]) == "") {
            $this->setWarning('MSG301', "(テスト項目コード)");
            return false;
        }else if(trim($this->field["TESTITEMNAME"]) == "") {
            $this->setWarning('MSG301', "(テスト項目名)");
            return false;
/***NO001-->
        }else if((trim($this->field["TESTITEMCD"]) == "" || strlen($this->field["TESTITEMCD"]) > 2 )) {
            $this->setWarning('MSG917', "(テスト項目コードが不正です。)");
            return false;
<--NO001***/
        }else if(strlen($this->field["TESTITEMNAME"]) > 30 && ($this->cmd == "add" || $this->cmd == "update" )) {
            $this->setWarning('MSG917', "(テスト項目名は30バイトまでです。)");
            return false;
        }
        //テスト項目コードの０埋め
        $this->field["TESTITEMCD"] = sprintf("%02d", $this->field["TESTITEMCD"]);
        //update,delete共通のチェック
        if($this->cmd == "update" || $this->cmd == "delete"){
            if($this->testkindcd != $this->field["TESTKINDCD"]){
                $this->setWarning('MSG308',"(テスト種別)");
                return false;
            }elseif($this->testitemcd != $this->field["TESTITEMCD"]){
                $this->setWarning('MSG308',"(テスト項目コード)");
                return false;
            }
        }
        $ret_val = knjz380Query::getExists($this);
        if ($this->cmd == "add") {
            if ($ret_val) {
                $this->setWarning('MSG302',"(テスト種別)(テスト項目コード)");
                return false;
            }
        }
        if ($this->cmd == "update" || $this->cmd == "delete") {
            if (!$ret_val) {
                $this->setWarning('MSG303',"(テスト種別)(テスト項目コード)");
                return false;
            }
        }

        return true;
    }

    /**
     * 前年度からのコピーのモデルを返します
    */
    function &getCopyModel(){
        $result = knjz380Query::getCopyQuery($this);
    }

    //更新用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
            $this->setMessage('MSG201');
        }
    }

    //追加用のモデルを返します
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->insert();
            $this->setMessage('MSG201');
        }
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->delete();
            $this->setMessage('MSG201');
        }
    }

    //インサート時の内部メソッド
    function &insert()
    {
        knjz380Query::getInsertUpdateQuery($this);
        $this->testkindcd = $this->field["TESTKINDCD"];
        $this->testitemcd = $this->field["TESTITEMCD"];
    }

    //アップデート時の内部メソッド
    function &update()
    {
        knjz380Query::getInsertUpdateQuery($this);
        $this->testkindcd = $this->field["TESTKINDCD"];
        $this->testitemcd = $this->field["TESTITEMCD"];
    }

    //デリート時の内部メソッド
    function &delete()
    {
        knjz380Query::getDeleteQuery($this);
        $this->clean();
    }
    
    //変数をクリアします
    function clean()
    {
        unset($this->testkindcd);
        unset($this->testitemcd);
    }
}
?>
