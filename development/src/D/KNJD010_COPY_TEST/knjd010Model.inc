<?php

require_once('for_php7.php');


class knjd010Model extends Model {
    var $field        = array();
    var $cmd;
    var $classcd;               //primary
    var $staffcd;               //ログインユーザーID保持
    var $sec_competence;        //権限保持
    var $control_data;          //処理年度保持
    var $subclass_sort;         //科目名ソートフラグ
    var $testkind_sort;         //テスト種別名ソートフラグ
    var $subclasscd;            //GETよりsubclasscdを取得
    var $testkindcd;            //GETより取得
    var $testitemcd;            //GETより取得
    var $selectdata;
    var $flg1;
    function knjd010Model()
    {
        common::GetControlMaster_Fnc($this->control_data);
        //ソートフラグ初期化
        $this->subclass_sort = "ASC";
        $this->testkind_sort = "ASC";
    }

    function init()
    {
        //セキュリティーチェック
        $this->staffcd = STAFFCD;
        $this->sec_competence = common::SecurityCheck($this->staffcd, "KNJD010");
        $db = Query::dbCheckOut();
        //教科マスタ
        $query = knjd010Query::getClassName($this);
        $result = $db->query($query);
        $row = $result->fetchRow(DB_FETCHMODE_ASSOC);
        // echo get_count($row);
        $this->flg1=get_count($row);
        Query::dbCheckIn($db);

        $this->field = array("SUBCLASSCD"   =>  VARS::post("SUBCLASSCD"),
                            "TESTKINDCD"    =>  VARS::post("TESTKINDCD"),
                            "TESTITEMCD"    =>  VARS::post("TESTITEMCD"),
                            "TESTITEMNAME"  =>  VARS::post("TESTITEMNAME"),
                            "UPDATED"       =>  VARS::post("UPDATED")
                            );
        $this->cmd  = VARS::request("cmd");

        if(VARS::post("CLASSCD")){
            $this->classcd = VARS::post("CLASSCD");
        }
        if(VARS::get("SUBCLASSCD")){
            $this->subclasscd = VARS::get("SUBCLASSCD");
        }
        if(VARS::get("TESTKINDCD")){
            $this->testkindcd = VARS::get("TESTKINDCD");
        }
        if(VARS::get("TESTITEMCD")){
            $this->testitemcd = VARS::get("TESTITEMCD");
        }

        //SUBCLASSソート値の取得
        if(VARS::get("SUBCLASS_SORT")){
            $this->subclass_sort = VARS::get("SUBCLASS_SORT") ;
        }
        //TESTKIND_SORTソート値の取得
        if(VARS::get("TESTKIND_SORT")){
            $this->testkind_sort = VARS::get("TESTKIND_SORT") ;
        }
        //対象科目
        $this->selectdata = VARS::post("selectdata");

        if ($this->cmd == "change"){
            $this->clean();
        }
        if(VARS::get("PROGRAMID")){
            $this->subclass_sort = "ASC" ;
            $this->testkind_sort = "ASC" ;
            if($this->sec_competence == DEF_UPDATABLE){
                $this->classcd = "01";
            } else {
            	unset($this->classcd);
            }
            unset($this->subclasscd);
            unset($this->testkindcd);
            unset($this->testitemcd);
        }
    }

    //入力項目チェック
    function validateInput()
    {
        //リスト選択チェック
        if(($this->cmd =="update" || $this->cmd =="delete") && ($this->subclasscd == "")){
           $this->setWarning('MSG308');
            return false;
        }
        //値のチェック
        if (trim($this->selectdata) == ""){
            $this->setWarning('MSG301', "(科目コード)");
            return false;
        }else if($this->subclasscd == "" && trim($this->field["TESTKINDCD"]) == "") {
            $this->setWarning('MSG301', "(テスト項目種別コード)");
            return false;
        }else if(trim($this->field["TESTITEMNAME"]) == "") {
            $this->setWarning('MSG301', "(テスト項目名)");
            return false;
        }else if($this->subclasscd == "" && (trim($this->field["TESTITEMCD"]) == "" || strlen($this->field["TESTITEMCD"]) > 2 )) {
            $this->setWarning('MSG917', "(テスト項目コードが不正です。)");
            return false;
        }else if(strlen($this->field["TESTITEMNAME"]) > 30 && ($this->cmd == "add" || $this->cmd == "update" )) {
            $this->setWarning('MSG917', "(文字数が不正です。)");
            return false;
        }
        //insert,update共通チェック
        if($this->cmd == "add" || $this->cmd == "update"){
/*            //テスト項目追加区分チェック
            if($this->cmd == "add" && knjd010Query::getTestItemAddCd($this)){
                $this->warning = "このテスト項目はこれ以上追加できません。";
                return false;
            }
            //テスト項目追加区分チェック
            if($this->cmd == "add" && knjd010Query::CheckTestItemAdd($this)){
                $this->warning = "このテスト項目はこれ以上追加できません。";
                return false;
            } */
            //行数チェック
            if(knjd010Query::getCountTestItem($this)){
                // $this->setWarning(MB3010);
                $this->warning = "項目追加は30以下にしてください。";
                return false;
            }
        }
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
        //delete時のチェック
        if($this->cmd == "delete"){
            //更新前１レコード取得
            if(knjd010Query::chkTestScoreData($this)){
                $this->setWarning('MSG919',"テスト実施予定で使用されています。");
                return false;
            }
        }

        return true;
    }

    /**
     * 前年度からのコピーのモデルを返します
    */
    function &getCopyModel(){
        $result = knjd010Query::getCopyQuery($this);
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
        knjd010Query::getInsertUpdateQuery($this);
        $this->testkindcd = $this->field["TESTKINDCD"];
        $this->testitemcd = $this->field["TESTITEMCD"];
    }

    //アップデート時の内部メソッド
    function &update()
    {
        knjd010Query::getInsertUpdateQuery($this);
        $this->testkindcd = $this->field["TESTKINDCD"];
        $this->testitemcd = $this->field["TESTITEMCD"];
    }

    //デリート時の内部メソッド
    function &delete()
    {
        knjd010Query::getDeleteQuery($this);
    }
    
    //変数をクリアします
    function clean()
    {
        unset($this->subclasscd);
    }
}
?>
