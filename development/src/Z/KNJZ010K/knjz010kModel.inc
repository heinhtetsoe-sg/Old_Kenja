<?php

require_once('for_php7.php');

//定数定義
class knjz010kModel extends Model {
    var $field      = array();
    var $cmd;
    var $examcoursecd;        //primary
    var $year;             //入試年度
    var $NextYear;
    
    //メンバ変数の初期化
    function knjz010kModel()
    {
    }
    
    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->examcoursecd);
            //$this->year = CTRL_YEAR+1;
        }

        $temp = VARS::request("year");
        if (isset($temp)) {
            $this->year = VARS::request("year");
        }

        $this->field = array("ENTEXAMYEAR"     =>  $this->year,                            //入試年度
                             "COURSECD"        =>  substr(VARS::post("TOTALCD"), 0, 1),    //課程コード
                             "MAJORCD"         =>  substr(VARS::post("TOTALCD"), 1, 3),    //学科コード
                             "EXAMCOURSECD"    =>  VARS::post("EXAMCOURSECD"),             //コースコード
                             "EXAMCOURSE_NAME" =>  VARS::post("EXAMCOURSE_NAME"),          //コース名
                             "EXAMCOURSE_ABBV" =>  VARS::post("EXAMCOURSE_ABBV"),          //コース略称
                             "EXAMCOURSE_MARK" =>  VARS::post("EXAMCOURSE_MARK"),          //コース記号
                             "CAPACITY"        =>  VARS::post("CAPACITY"),                 //コース記号
                             "UPDATED"         =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");
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
        if (strlen($this->field["COURSECD"]) == 0 || strlen($this->field["MAJORCD"]) == 0){
            $this->setWarning("MSG301","(課程学科)");
            return false;
        }
        if (strlen($this->field["EXAMCOURSECD"]) == 0){
            $this->setWarning("MSG301","(コースコード)");
            return false;
        }
        if (strlen($this->field["EXAMCOURSE_NAME"]) == 0){
            $this->setWarning("MSG301","(コース名)");
            return false;
        }
        if ($this->field["EXAMCOURSECD"] == '0'){
            $this->setWarning("MSG901","コースコードは\"0\"以外の数字を入力してください。");
            return false;
        }
        //リストより選択していない
        if ($this->examcoursecd == "" && ($kind=="update" || $kind=="delete")){
            $this->setWarning("MSG308");
            return false;
        }
        //キー値が変更されていますかをチェック
        if ($this->field["EXAMCOURSECD"] != $this->examcoursecd && ($kind=="update" || $kind=="delete")){
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        //コードのゼロ埋め
        $this->field["EXAMCOURSECD"] = sprintf("%04d",$this->field["EXAMCOURSECD"]);
        //１レコード取得
        $row = knjz010kQuery::getRow($this->field["ENTEXAMYEAR"],$this->field["EXAMCOURSECD"],$this->field["COURSECD"],$this->field["MAJORCD"]);

        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918","(コースコード)");
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
        if (strlen($this->field["EXAMCOURSE_NAME"]) > 33){
            $this->setWarning("MSG915","(コース名は33バイトまでです。)");
            return false;
        }
        if (strlen($this->field["EXAMCOURSE_ABBV"]) > 3){
            $this->setWarning("MSG915","(コース略称は3バイトまでです。)");
            return false;
        }
        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")){
            knjz010kQuery::getUpdateQuery($this->field);
            $this->setMessage("MSG201");
        }
        return;
    }
    
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")){
            knjz010kQuery::getInsertQuery($this->field);
            $this->setMessage("MSG201");
            $this->examcoursecd = $this->field["EXAMCOURSECD"];
        }
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")){
            knjz010kQuery::getDeleteQuery($this->field);
            $this->setMessage("MSG201");
            $this->examcoursecd = "";
        }
        return;
    }  

    //次年度のデータを作成
    function &getCopyYearModel()
    {
        if ($this->year == (CTRL_YEAR+1)) {
            //今年度のデータが存在するかチェック
            $t_year = knjz010kQuery::CheckData($this->year);
            if(!isset($t_year)){
               $this->setWarning('MSG203', "今年度のデータが存在しません。");
               return;
            }
        }
        $this->NextYear = $this->year + 1;
        //学科マスタに次年度のデータが存在するか確認
        if($result_create = knjz010kQuery::NextYearExist($this))
        {
            knjz010kQuery::getCopyYearQuery($this);
            $this->year = $this->NextYear;
            $this->setMessage("MSG201");
        } else {
            $this->setMessage("MSG203", "学科マスタの" . "$this->NextYear" . "年度のデータに今年度の課程コードと\\n学科コードが存在しません。");
        }
    }
       
    //変数をクリアします
    function clean()
    {
    }
}
?>
