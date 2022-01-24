<?php

require_once('for_php7.php');

//定数定義
class knjz010tModel extends Model {
    var $field      = array();
    var $cmd;
    var $applicantdiv;      //primary
    var $majorcd;           //primary
    var $year;              //入試年度
    var $NextYear;

    //メンバ変数の初期化
    function knjz010tModel() {}

    function init() {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->applicantdiv);
            unset($this->majorcd);
        }

        $temp = VARS::request("year");
        if (isset($temp)) {
            $this->year = VARS::request("year");
        }

        $this->field = array("ENTEXAMYEAR"     =>  $this->year,                 //入試年度
                             "APPLICANTDIV"    =>  VARS::post("APPLICANTDIV"),  //入試制度
                             "MAJORCD"         =>  VARS::post("MAJORCD"),       //学科コード
                             "CAPACITY"        =>  VARS::post("CAPACITY"),      //コース記号
                             "UPDATED"         =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");

        $temp = VARS::get("APPLICANTDIV");
        if (isset($temp)){
            $this->applicantdiv = VARS::get("APPLICANTDIV");
        }
        $temp = VARS::get("MAJORCD");
        if (isset($temp)){
            $this->majorcd = VARS::get("MAJORCD");
        }
    }

    //入力項目チェック
    function validateInput($kind) {
        //空文字チェック
        if (strlen($this->field["APPLICANTDIV"]) == 0){
            $this->setWarning("MSG301","( 入試制度 )");
            return false;
        }
        //空文字チェック
        if (strlen($this->field["MAJORCD"]) == 0){
            $this->setWarning("MSG301","( 学科コード )");
            return false;
        }
        //キー値が変更されているかをチェック
        if (($this->field["APPLICANTDIV"] != $this->applicantdiv ||
             $this->field["MAJORCD"]      != $this->majorcd ) &&
            ($kind=="update" || $kind=="delete")){
            $this->setWarning("MSG308");
            return false;
        }
        //１レコード取得
        $row = knjz010tQuery::getRow($this->field["ENTEXAMYEAR"],$this->field["APPLICANTDIV"],$this->field["MAJORCD"]);

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
        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput("update")){
            knjz010tQuery::getUpdateQuery($this->field);
            $this->setMessage("MSG201");
            $this->applicantdiv = $this->field["APPLICANTDIV"];
            $this->majorcd = $this->field["MAJORCD"];
        }
        return;
    }

    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput("insert")){
            knjz010tQuery::getInsertQuery($this->field);
            $this->setMessage("MSG201");
            $this->applicantdiv = $this->field["APPLICANTDIV"];
            $this->majorcd = $this->field["MAJORCD"];
        }
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput("delete")){
            knjz010tQuery::getDeleteQuery($this->field);
            $this->setMessage("MSG201");
            unset($this->applicantdiv);
            unset($this->majorcd);
        }
        return;
    }

    //次年度のデータを作成
    function &getCopyYearModel() {
        $this->NextYear = $this->year + 1;
        if(knjz010tQuery::getCopyYearQuery($this))
        {
            $this->year = $this->NextYear;
            $this->setMessage("MSG201");
        }else{
            $this->setMessage("MSG203", "\\n作成するデータが存在しませんでした。\\n既に作成されているか、" . "$this->NextYear" . "年度の名称、課程、学科マスタが存在していません。");
        }
    }

    //変数をクリアします
    function clean() {}
}
?>
