<?php

require_once('for_php7.php');

//定数定義
class knjz020jModel extends Model {
    var $field      = array();
    var $cmd;
    var $testdiv;           //primary
    var $coursecd;          //primary
    var $majorcd;           //primary
    var $examcoursecd;      //primary
    var $testsubclasscd;
    var $NextYear;
    
    //メンバ変数の初期化
    function knjz020jModel()
    {
    }
    
    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->testdiv);
            unset($this->coursecd);
            unset($this->majorcd);
            unset($this->examcoursecd);
            unset($this->totalcd);
            unset($this->testsubclasscd);
        }  

        $this->cmd  = VARS::request("cmd");

        $temp = VARS::request("year");
        if (isset($temp)) {
            $this->year = VARS::request("year");
        }

        $this->field = array("ENTEXAMYEAR"     =>  $this->year,                            //入試年度
                             "TESTDIV"         =>  VARS::post("TESTDIV"),                  //入試区分
                             "COURSECD"        =>  substr(VARS::post("TOTALCD"), 0, 1),    //課程コード
                             "MAJORCD"         =>  substr(VARS::post("TOTALCD"), 1, 3),    //学科コード
                             "EXAMCOURSECD"    =>  substr(VARS::post("TOTALCD"), 4, 4),    //コースコード
                             "TESTSUBCLASSCD"  =>  VARS::post("TESTSUBCLASSCD"),           //試験科目コード
                             "PERFECT"         =>  VARS::post("PERFECT"),                  //満点
                             "UPDATED"         =>  VARS::post("UPDATED")
                             );

        if (VARS::get("TESTDIV")) {
            $this->testdiv = VARS::get("TESTDIV");
        }
        if (VARS::get("COURSECD")) {
            $this->coursecd = VARS::get("COURSECD");
        }
        if (VARS::get("MAJORCD")) {
            $this->majorcd = VARS::get("MAJORCD");
        }
        if (VARS::get("EXAMCOURSECD")) {
            $this->examcoursecd = VARS::get("EXAMCOURSECD");
        }
        if (VARS::get("TESTSUBCLASSCD")) {
            $this->testsubclasscd = VARS::get("TESTSUBCLASSCD");
        }
        if(VARS::get("TOTALCD")){
            $this->totalcd      = VARS::get("TOTALCD");
            $this->coursecd     = substr($this->totalcd,0,1);
            $this->majorcd      = substr($this->totalcd,1,3);
            $this->examcoursecd = substr($this->totalcd,4,4);
        }

        if (VARS::post("TESTDIV")) {
            $this->testdiv          = VARS::post("TESTDIV");
            unset($this->totalcd);
        }

    }
    //入力項目チェック
    function validateInput($kind)
    {
        //空文字チェック
        if (strlen($this->field["ENTEXAMYEAR"]) == 0){
            $this->setWarning("MSG301","( 年度 )");
            return false;
        }
        if (strlen($this->field["TESTDIV"]) == 0){
            $this->setWarning("MSG301","( 入試区分 )");
            return false;
        }
        if (strlen($this->field["COURSECD"]) == 0){
            $this->setWarning("MSG301","( 課程コード )");
            return false;
        }
        if (strlen($this->field["MAJORCD"]) == 0){
            $this->setWarning("MSG301","( 学科コード )");
            return false;
        }
        if (strlen($this->field["EXAMCOURSECD"]) == 0){
            $this->setWarning("MSG301","( コースコード )");
            return false;
        }
        if (strlen($this->field["TESTSUBCLASSCD"]) == 0){
            $this->setWarning("MSG301","( 試験科目 )");
            return false;
        }
        if (strlen($this->field["PERFECT"]) == 0){
            $this->setWarning("MSG301","( 満点 )");
            return false;
        }        
        //キー値が変更されているかをチェック
        if (($this->field["TESTDIV"]        != $this->testdiv ||
             $this->field["TESTSUBCLASSCD"] != $this->testsubclasscd ||
             $this->field["COURSECD"]       != $this->coursecd ||
             $this->field["MAJORCD"]        != $this->majorcd  ||
             $this->field["EXAMCOURSECD"]   != $this->examcoursecd ) && 
            ($kind=="update" || $kind=="delete")){
            $this->setWarning("MSG308");
            return false;
        }

        //ENTEXAM_SCORE_DATのMAX(score)と更新する満点との比較
        if($kind=="update"){
            $maxscore = knjz020jQuery::ChScoreDat($this);
            if($maxscore > $this->field["PERFECT"]){
                $this->setWarning("MSG300","入力されている".$maxscore."点より低い満点は設定出来ません。");
                return false;
            }
        }
        //ENTEXAM_SCORE_DATで使用されてないかチェック
        if($kind=="delete"){
            $scoredat = knjz020jQuery::ChScoreDat($this);
            if($scoredat > 0){
                $this->setWarning("MSG300","得点が設定されています。");
                return false;
            }
        }

        //１レコード取得
        $row = knjz020jQuery::CheckData($this);
        
        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918");
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
        if (strlen($this->field["PRRFECT"]) > 3){
            $this->setWarning("MSG915","(満点は3バイトまでです。)");
            return false;
        }
        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")) {
            knjz020jQuery::getDeleteQuery($this);
            knjz020jQuery::getInsertQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }
    
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")) {
            knjz020jQuery::getInsertQuery($this);
            $this->setMessage("MSG201");
            $this->testdiv          = $this->field["TESTDIV"];
            $this->coursecd         = $this->field["COURSECD"];
            $this->majorcd          = $this->field["MAJORCD"];
            $this->examcoursecd     = $this->field["EXAMCOURSECD"];
            $this->testsubclasscd   = $this->field["TESTSUBCLASSCD"];
        }
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")) {
            knjz020jQuery::getDeleteQuery($this);
            $this->setMessage("MSG201");
            $this->testdiv = "";
            $this->coursecd = "";
            $this->majorcd = "";
            $this->examcoursecd = "";
            $this->testsubclasscd = "";
        }
        return;
    }  

    //次年度のデータを作成
    function &getCopyYearModel()
    {
        $this->NextYear = $this->year + 1;
        if(knjz020jQuery::getCopyYearQuery($this))
        {
            $this->year = $this->NextYear;
            $this->setMessage("MSG201");
        }else{
            $this->setMessage("MSG203", "\\n作成するデータが存在しませんでした。\\n既に作成されているか、" . "$this->NextYear" . "年度の受験コースマスタか\\n名称マスタが存在していません。");
        }
    }
       
    //変数をクリアします
    function clean()
    {
    }
}
?>
