<?php

require_once('for_php7.php');


class knjz380vModel extends Model {
    var $field        = array();
    var $cmd;
    var $staffcd;               //ログインユーザーID保持
    var $sec_competence;        //権限保持
    var $control_data;          //処理年度保持
    var $semester;              //GETより取得
    var $testkindcd;            //GETより取得
    var $testitemcd;            //GETより取得
    var $score_div;             //GETより取得

    function knjz380vModel()
    {
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init()
    {
        //セキュリティーチェック
        $this->staffcd = STAFFCD;
        $this->sec_competence = common::SecurityCheck($this->staffcd, "KNJZ380V");

        $this->field = array("COUNTFLG"         =>  VARS::post("COUNTFLG"),
                             "SEMESTER"         =>  VARS::post("SEMESTER"),
                             "SEMESTER_DETAIL"  =>  VARS::post("SEMESTER_DETAIL"),
                             "TESTKINDCD"       =>  VARS::post("TESTKINDCD"),
                             "TESTITEMCD"       =>  VARS::post("TESTITEMCD"),
                             "SCORE_DIV"        =>  VARS::post("SCORE_DIV"),
                             "TESTITEMNAME"     =>  VARS::post("TESTITEMNAME"),
                             "TESTITEMABBV1"    =>  VARS::post("TESTITEMABBV1"),
                             "SIDOU_INPUT"      =>  VARS::post("SIDOU_INPUT"),
                             "SIDOU_INPUT_INF"  =>  VARS::post("SIDOU_INPUT_INF"),
                             "MIKOMI_FLG"       =>  VARS::post("MIKOMI_FLG"),
                             "SANKOU_FLG"       =>  VARS::post("SANKOU_FLG"),
                             "REMARK_FLG"       =>  VARS::post("REMARK_FLG"),
                             "JYORETSU_FLG"     =>  VARS::post("JYORETSU_FLG"),
                             "NOT_USE_CSV_FLG"  =>  VARS::post("NOT_USE_CSV_FLG"),
                             "UPDATED"          =>  VARS::post("UPDATED")
                            );
        $this->cmd  = VARS::request("cmd");

        if(VARS::get("SEMESTER")){
            $this->semester = VARS::get("SEMESTER");
            $this->field["SEMESTER"] = $this->semester;
        }
        if(VARS::get("TESTKINDCD")){
            $this->testkindcd = VARS::get("TESTKINDCD");
            $this->field["TESTKINDCD"] = $this->testkindcd;
        }
        if(VARS::get("TESTITEMCD")){
            $this->testitemcd = VARS::get("TESTITEMCD");
        }
        if(VARS::get("SCORE_DIV")){
            $this->score_div = VARS::get("SCORE_DIV");
        }
        
        if(VARS::get("PROGRAMID")){
            $this->clean();
        }

        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    //入力項目チェック
    function validateInput()
    {
        //入力チェック
        if (trim($this->field["TESTKINDCD"]) == "") {
            $this->setWarning('MSG301', "(テスト種別)");
            return false;
        }else if(trim($this->field["TESTITEMCD"]) == "") {
            $this->setWarning('MSG301', "(テスト項目コード)");
            return false;
        }else if(trim($this->field["SCORE_DIV"]) == "") {
            $this->setWarning('MSG301', "(成績種別コード)");
            return false;
        }else if(trim($this->field["TESTITEMNAME"]) == "") {
            $this->setWarning('MSG301', "(表示用テスト項目名)");
            return false;
        }else if(trim($this->field["TESTITEMABBV1"]) == "") {
            $this->setWarning('MSG301', "(表示用項目名1)");
            return false;
        }else if(trim($this->field["SEMESTER"]) == "") {
            $this->setWarning('MSG301', "(学期)");
            return false;
        }else if(strlen($this->field["TESTITEMNAME"]) > 30 && ($this->cmd == "add" || $this->cmd == "update" )) {
            $this->setWarning('MSG917', "(表示用テスト項目名は30バイトまでです。)");
            return false;
        }else if(strlen($this->field["TESTITEMABBV1"]) > 15 && ($this->cmd == "add" || $this->cmd == "update" )) {
            $this->setWarning('MSG917', "(表示用項目名1は15バイトまでです。)");
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
            }elseif($this->score_div != $this->field["SCORE_DIV"]){
                $this->setWarning('MSG308',"(成績種別コード)");
                return false;
            }elseif($this->semester != $this->field["SEMESTER"]){
                $this->setWarning('MSG308',"(学期)");
                return false;
            }
        }
        $ret_val = knjz380vQuery::getExists($this);
        if ($this->cmd == "add") {
            if ($ret_val) {
                $this->setWarning('MSG302',"(学期)(テスト種別)(テスト項目コード)(成績種別コード)");
                return false;
            }
        }
        if ($this->cmd == "update" || $this->cmd == "delete") {
            if (!$ret_val) {
                $this->setWarning('MSG303',"(学期)(テスト種別)(テスト項目コード)(成績種別コード)");
                return false;
            }
        }

        return true;
    }

    /**
     * 前年度からのコピーのモデルを返します
    */
    function &getCopyModel(){
        $result = knjz380vQuery::getCopyQuery($this);
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
        knjz380vQuery::getInsertUpdateQuery($this);
        $this->semester = $this->field["SEMESTER"];
        $this->testkindcd = $this->field["TESTKINDCD"];
        $this->testitemcd = $this->field["TESTITEMCD"];
        $this->score_div  = $this->field["SCORE_DIV"];
    }

    //アップデート時の内部メソッド
    function &update()
    {
        knjz380vQuery::getInsertUpdateQuery($this);
        $this->semester = $this->field["SEMESTER"];
        $this->testkindcd = $this->field["TESTKINDCD"];
        $this->testitemcd = $this->field["TESTITEMCD"];
        $this->score_div  = $this->field["SCORE_DIV"];
    }

    //デリート時の内部メソッド
    function &delete()
    {
        knjz380vQuery::getDeleteQuery($this);
        $this->clean();
    }
    
    //変数をクリアします
    function clean()
    {
        unset($this->semester);
        unset($this->testkindcd);
        unset($this->testitemcd);
        unset($this->score_div);
    }
}
?>
