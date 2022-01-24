<?php

require_once('for_php7.php');

//定数定義
class knjz010mModel extends Model {
    var $field      = array();
    var $cmd;
    var $applicantdiv;      //primary
    var $coursecd;          //primary
    var $majorcd;           //primary
    var $examcoursecd;      //primary
    var $year;              //入試年度
    var $NextYear;
    
    //メンバ変数の初期化
    function knjz010mModel()
    {
    }

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->applicantdiv);
            unset($this->coursecd);
            unset($this->majorcd);
            unset($this->examcoursecd);
            unset($this->totalcd);
        }

        $temp = VARS::request("year");
        if (isset($temp)) {
            $this->year = VARS::request("year");
        }

        $this->field = array("ENTEXAMYEAR"     =>  $this->year,                            //入試年度
                             "APPLICANTDIV"    =>  VARS::post("APPLICANTDIV"),             //入試制度
                             "COURSECD"        =>  substr(VARS::post("TOTALCD"), 0, 1),    //課程コード
                             "MAJORCD"         =>  substr(VARS::post("TOTALCD"), 1, 3),    //学科コード
                             "EXAMCOURSECD"    =>  VARS::post("EXAMCOURSECD"),             //コースコード
                             "EXAMCOURSE_NAME" =>  VARS::post("EXAMCOURSE_NAME"),          //コース名
                             "EXAMCOURSE_MARK" =>  VARS::post("EXAMCOURSE_MARK"),          //コース記号
                             "CAPACITY"        =>  VARS::post("CAPACITY"),                 //コース記号
                             "UPDATED"         =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");


        $temp = VARS::get("APPLICANTDIV");
        if (isset($temp)){
            $this->applicantdiv = VARS::get("APPLICANTDIV");
        }
        $temp = VARS::get("COURSECD");
        if (isset($temp)){
            $this->coursecd = VARS::get("COURSECD");
        }
        $temp = VARS::get("MAJORCD");
        if (isset($temp)){
            $this->majorcd = VARS::get("MAJORCD");
        }
        $temp = VARS::get("EXAMCOURSECD");
        if (isset($temp)){
            $this->examcoursecd = VARS::get("EXAMCOURSECD");
        }

        if(VARS::get("TOTALCD")){
            $this->totalcd  = VARS::get("TOTALCD");
            $this->coursecd = substr($this->totalcd,0,1);
            $this->majorcd  = substr($this->totalcd,1,3);
        }
    }



    //入力項目チェック
    function validateInput($kind)
    {
        //空文字チェック
        if (strlen($this->field["APPLICANTDIV"]) == 0){
            $this->setWarning("MSG301","( 入試制度 )");
            return false;
        }
        //空文字チェック
        if (strlen($this->field["COURSECD"]) == 0){
            $this->setWarning("MSG301","( 課程コード )");
            return false;
        }
        //空文字チェック
        if (strlen($this->field["MAJORCD"]) == 0){
            $this->setWarning("MSG301","( 学科コード )");
            return false;
        }
       //空文字チェック
       if ((int)$this->field["EXAMCOURSECD"] < 1){
           $this->setWarning("MSG901","( コースコードには１以上を入力して下さい。 )");
           return false;
       }
        //空文字チェック
        if (strlen($this->field["EXAMCOURSECD"]) == 0){
            $this->setWarning("MSG301","( コースコード )");
            return false;
        }
        if (strlen($this->field["EXAMCOURSE_NAME"]) == 0){
            $this->setWarning("MSG301","( コース名 )");
            return false;
        }
        //キー値が変更されているかをチェック
        if (($this->field["APPLICANTDIV"] != $this->applicantdiv ||
             $this->field["COURSECD"]     != $this->coursecd ||
             $this->field["MAJORCD"]      != $this->majorcd  ||
             $this->field["EXAMCOURSECD"] != $this->examcoursecd ) && 
            ($kind=="update" || $kind=="delete")){
            $this->setWarning("MSG308");
            return false;
        }
        //コードのゼロ埋め
        $this->field["EXAMCOURSECD"] = sprintf("%04d",$this->field["EXAMCOURSECD"]);
        //１レコード取得
        $row = knjz010mQuery::getRow($this->field["ENTEXAMYEAR"],$this->field["EXAMCOURSECD"],$this->field["APPLICANTDIV"],$this->field["COURSECD"],$this->field["MAJORCD"]);
        
        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918","( コースコード )");
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
        if (strlen($this->field["EXAMCOURSE_NAME"]) > 30){
            $this->setWarning("MSG915","( コース名は30バイトまでです。)");
            return false;
        }
        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")){
            knjz010mQuery::getUpdateQuery($this->field);
            $this->setMessage("MSG201");
        }
        return;
    }
    
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")){
            knjz010mQuery::getInsertQuery($this->field);
            $this->setMessage("MSG201");
            $this->applicantdiv = $this->field["APPLICANTDIV"];
            $this->examcoursecd = $this->field["EXAMCOURSECD"];
            $this->coursecd     = $this->field["COURSECD"];
            $this->majorcd      = $this->field["MAJORCD"];
            $this->totalcd      = $this->field["COURSECD"].$this->field["MAJORCD"];
        }
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")){
            knjz010mQuery::getDeleteQuery($this->field);
            $this->setMessage("MSG201");
            $this->examcoursecd = "";
        }
        return;
    }  

    //次年度のデータを作成
    function &getCopyYearModel()
    {
        $this->NextYear = $this->year + 1;
        if(knjz010mQuery::getCopyYearQuery($this))
        {
            $this->year = $this->NextYear;
            $this->setMessage("MSG201");
        }else{
            $this->setMessage("MSG203", "\\n作成するデータが存在しませんでした。\\n既に作成されているか、" . "$this->NextYear" . "年度の名称、課程、学科マスタが存在していません。");
        }
    }
       
    //変数をクリアします
    function clean()
    {
    }
}
?>
