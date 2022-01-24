<?php

require_once('for_php7.php');

//定数定義
class knjz020hModel extends Model {
    var $field      = array();
    var $cmd;
    var $examcoursecd;
    var $applicantdiv;
    var $testdiv;
    var $testsubclasscd;
    var $coursecd;
    var $majorcd;
    var $coursecode;
    var $NextYear;
    
    //メンバ変数の初期化
    function knjz020hModel()
    {
    }
    
    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->examcoursecd);
            unset($this->applicantdiv);
            unset($this->testdiv);
            unset($this->testsubclasscd);
            unset($this->coursecd);
            unset($this->majorcd);
            unset($this->coursecode);
        }  

        $this->cmd  = VARS::request("cmd");
        $temp = VARS::request("TOTALCD");
        if (isset($temp)) {
            $this->examcoursecd = VARS::request("TOTALCD");
        }

        $temp = VARS::request("year");
        if (isset($temp)) {
            $this->year = VARS::request("year");
        }

        $this->field = array("ENTEXAMYEAR"     =>  $this->year,                            //入試年度
                             "APPLICANTDIV"    =>  VARS::post("APPLICANTDIV"),             //入試制度
                             "TESTDIV"         =>  VARS::post("TESTDIV"),                  //入試区分
                             "COURSECD"        =>  VARS::post("COURSECD"),                 //課程コード
                             "MAJORCD"         =>  VARS::post("MAJORCD"),                  //学科コード
                             "EXAMCOURSECD"    =>  VARS::post("EXAMCOURSECD"),             //コースコード
                             "TESTSUBCLASSCD"  =>  VARS::post("TESTSUBCLASSCD"),           //試験科目コード
                             "PERFECT"         =>  VARS::post("PERFECT"),                  //満点
                             "UPDATED"         =>  VARS::post("UPDATED")
                             );

        if (VARS::get("TESTDIV")) {
            $this->testdiv = VARS::get("TESTDIV");
        }
        if (VARS::get("TESTSUBCLASSCD")) {
            $this->testsubclasscd = VARS::get("TESTSUBCLASSCD");
        }
        if (VARS::get("TOTALCD")) {
            $this->applicantdiv = substr(VARS::get("TOTALCD"), 0, 1);
            $this->coursecd = substr(VARS::get("TOTALCD"), 1, 1);
            $this->majorcd = substr(VARS::get("TOTALCD"), 2, 3);
            $this->coursecode = substr(VARS::get("TOTALCD"), 5, 4);
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
        if (strlen($this->field["TESTSUBCLASSCD"]) == 0){
            $this->setWarning("MSG301","( 試験科目 )");
            return false;
        }
        if (strlen($this->field["PERFECT"]) == 0){
            $this->setWarning("MSG301","( 満点 )");
            return false;
        }        
        //キー値が変更されているかをチェック
        if (($this->field["APPLICANTDIV"]   != $this->applicantdiv ||
             $this->field["TESTDIV"]        != $this->testdiv ||
             $this->field["TESTSUBCLASSCD"] != $this->testsubclasscd ||
             $this->field["COURSECD"]       != $this->coursecd ||
             $this->field["MAJORCD"]        != $this->majorcd ||
             $this->field["EXAMCOURSECD"]   != $this->coursecode ) &&
            ($kind=="update" || $kind=="delete")){
            $this->setWarning("MSG308");
            return false;
        }

        //ENTEXAM_SCORE_DATのMAX(score)と更新する満点との比較
        if($kind=="update"){
            $maxscore = knjz020hQuery::ChScoreDat($this);
            if($maxscore > $this->field["PERFECT"]){
                $this->setWarning("MSG300","入力されている".$maxscore."点より低い満点は設定出来ません。");
                return false;
            }
        }
        //ENTEXAM_SCORE_DATで使用されてないかチェック
        if($kind=="delete"){
            $scoredat = knjz020hQuery::ChScoreDat($this);
            if($scoredat > 0){
                $this->setWarning("MSG300","得点が設定されています。");
                return false;
            }
        }


        //１レコード取得
        $row = knjz020hQuery::CheckData($this);
        
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
            knjz020hQuery::getDeleteQuery($this);
            knjz020hQuery::getInsertQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }
    
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")) {
            knjz020hQuery::getInsertQuery($this);
            $this->setMessage("MSG201");
            $this->testdiv = $this->field["TESTDIV"];
            $this->testsubclasscd = $this->field["TESTSUBCLASSCD"];
        }
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")) {
            knjz020hQuery::getDeleteQuery($this);
            $this->setMessage("MSG201");
            $this->testdiv = "";
            $this->testsubclasscd = "";
        }
        return;
    }  

    //次年度のデータを作成
    function &getCopyYearModel()
    {
        $this->NextYear = $this->year + 1;
        if(knjz020hQuery::getCopyYearQuery($this))
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
