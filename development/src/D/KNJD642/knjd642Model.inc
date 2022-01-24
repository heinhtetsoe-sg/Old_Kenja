<?php

require_once('for_php7.php');

//定数定義
class knjd642Model extends Model {
    var $field      = array();
    var $cmd;
    var $bu_cd;          //primary
    var $ka_cd;          //primary
    var $year;           //年度
    var $NextYear;
    
    //メンバ変数の初期化
    function knjd642Model()
    {

    }


    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->bu_cd);
            unset($this->ka_cd);
        }

        $temp = VARS::request("year");
        if (isset($temp)) {
            $this->year = VARS::request("year");
        }

        $this->field = array("YEAR"         =>  $this->year,                //年度
                             "BU_CD"        =>  VARS::post("BU_CD"),        //学部コード
                             "BU_NAME"      =>  VARS::post("BU_NAME"),      //学部名
                             "BU_ABBV"      =>  VARS::post("BU_ABBV"),      //学部略称
                             "KA_CD"        =>  VARS::post("KA_CD"),        //学科コード
                             "KA_NAME"      =>  VARS::post("KA_NAME"),      //学科名
                             "KA_ABBV"      =>  VARS::post("KA_ABBV"),      //学科略称
                             "KA_MARK"      =>  VARS::post("KA_MARK"),      //学科記号
                             "DIV"          =>  VARS::post("DIV"),          //文理区分
                             "FRAME"        =>  VARS::post("FRAME"),        //推薦枠
                             "UPDATED"      =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");

        $temp = VARS::get("BU_CD");
        if (isset($temp)){
            $this->bu_cd = VARS::get("BU_CD");
        }
        $temp = VARS::get("KA_CD");
        if (isset($temp)){
            $this->ka_cd = VARS::get("KA_CD");
        }

    }

    //入力項目チェック
    function validateInput($kind)
    {
        //空文字チェック
        if (strlen($this->field["BU_CD"]) == 0){
            $this->setWarning("MSG301","( 学部コード )");
            return false;
        }
        if (strlen($this->field["KA_CD"]) == 0){
            $this->setWarning("MSG301","( 学科コード )");
            return false;
        }
        if (strlen($this->field["BU_NAME"]) == 0){
            $this->setWarning("MSG301","( 学部名 )");
            return false;
        }
        if (strlen($this->field["KA_NAME"]) == 0){
            $this->setWarning("MSG301","( 学科名 )");
            return false;
        }
        //リストより選択していない
        //キー値が変更されているかをチェック
        if (($this->field["BU_CD"] != $this->bu_cd ||
             $this->field["KA_CD"]     != $this->ka_cd ) && 
            ($kind=="update" || $kind=="delete")){
            $this->setWarning("MSG308");
            return false;
        }
        //１レコード取得
        $row = knjd642Query::getRow($this->field["YEAR"],$this->field["BU_CD"],$this->field["KA_CD"]);
        
        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918","( 学科コード )");
            return false;
        }
        //存在チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
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
        //文字数チェック
        if (strlen($this->field["BU_NAME"]) > 80){
            $this->setWarning("MSG915","( 学部名は80バイトまでです。)");
            return false;
        }
        if (strlen($this->field["KA_NAME"]) > 80){
            $this->setWarning("MSG915","( 学科名は80バイトまでです。)");
            return false;
        }
        if (strlen($this->field["BU_ABBV"]) > 20){
            $this->setWarning("MSG915","( 学部略称は20バイトまでです。)");
            return false;
        }
        if (strlen($this->field["KA_ABBV"]) > 20){
            $this->setWarning("MSG915","( 学科略称は20バイトまでです。)");
            return false;
        }

        //データチェック
        $db = Query::dbCheckOut();
        $school_cd = $db->getOne(knjd642Query::getSchoolcdQuery());
        if ($this->cmd == "add" && !isset($school_cd)){
            $this->setWarning("MSG301","( 名称マスタ )");
            return false;
        }
        Query::dbCheckIn($db);

        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")){
            knjd642Query::getUpdateQuery($this->field);
            $this->setMessage("MSG201");
        }
        return;
    }
    
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")){
            knjd642Query::getInsertQuery($this->field);
            $this->setMessage("MSG201");
            $this->bu_cd = $this->field["BU_CD"];
            $this->ka_cd = $this->field["KA_CD"];
        }
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")){
            knjd642Query::getDeleteQuery($this->field);
            $this->setMessage("MSG201");
            $this->bu_cd = "";
            $this->ka_cd = "";
        }
        return;
    }  

    //次年度のデータを作成
    function &getCopyYearModel()
    {
        $this->NextYear = $this->year + 1;
        if(knjd642Query::getCopyYearQuery($this))
        {
            $this->year = $this->NextYear;
            $this->setMessage("MSG201");
        }else{
            $this->setMessage("MSG203", "\\n作成するデータが存在しませんでした。\\nデータは既に作成されています。");
        }
    }
       
    //変数をクリアします
    function clean()
    {
    }
}
?>
