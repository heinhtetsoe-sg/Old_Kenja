<?php

require_once('for_php7.php');

//定数定義
class knjl005aModel extends Model
{
    public $field = array();
    public $cmd;
    public $applicantdiv;      //primary
    public $testdiv;           //primary
    public $year;              //入試年度
    public $NextYear;

    public function init()
    {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->applicantdiv);
            unset($this->testdiv);
        }

        $temp = VARS::request("year");
        if (isset($temp)) {
            $this->year = VARS::request("year");
        }
        $temp = VARS::request("APPLICANTDIV");
        if (isset($temp)) {
            $this->applicantdiv = VARS::request("APPLICANTDIV");
        }
        $temp = VARS::request("TESTDIV");
        if (isset($temp)) {
            $this->testdiv = VARS::request("TESTDIV");
        }
        $temp = VARS::get("MAPPING_NO");
        if (isset($temp)) {
            $this->mappingNo = VARS::get("MAPPING_NO");
        }

        $postMappingNo = VARS::post("MAPPING_NO");
        $this->field = array("ENTEXAMYEAR"      =>  $this->year,                    //入試年度
                             "APPLICANTDIV"     =>  $this->applicantdiv,            //受験校種
                             "TESTDIV"          =>  $this->testdiv,                 //試験CD
                             "MAPPING_NO"       =>  ($postMappingNo != "") ? sprintf("%02d", $postMappingNo) : "",       //対応コード
                             "TESTDIV_NAME"     =>  VARS::post("TESTDIV_NAME"),     //試験名称
                             "SHDIV"            =>  VARS::post("SHDIV"),            //専願区分
                             "COURSEDIV"        =>  VARS::post("COURSEDIV"),        //志望コース
                             "EXAM_TYPE"        =>  VARS::post("EXAM_TYPE")         //受験型
                             );
        
        $this->cmd = VARS::request("cmd");
    }

    //入力項目チェック
    public function validateInput($kind)
    {
        //空文字チェック
        if (strlen($this->field["APPLICANTDIV"]) == 0) {
            $this->setWarning("MSG301", "( 受験校種 )");
            return false;
        }
        if (strlen($this->field["TESTDIV"]) == 0) {
            $this->setWarning("MSG301", "( 試験区分 )");
            return false;
        }
        if (strlen($this->field["MAPPING_NO"]) == 0) {
            $this->setWarning("MSG301", "( 対応コード )");
            return false;
        }
        if (strlen($this->field["TESTDIV_NAME"]) == 0) {
            $this->setWarning("MSG301", "( 認識用文字列 )");
            return false;
        }
        if (strlen($this->field["SHDIV"]) == 0) {
            $this->setWarning("MSG301", "( 専併区分 )");
            return false;
        }
        if (strlen($this->field["COURSEDIV"]) == 0) {
            $this->setWarning("MSG301", "( 志望コース )");
            return false;
        }
        if (strlen($this->field["EXAM_TYPE"]) == 0) {
            $this->setWarning("MSG301", "( 受験型 )");
            return false;
        }

        if ($this->field["MAPPING_NO"] < 1) {
            $this->setWarning("MSG901", "1以上の値を入力して下さい。( 対応コード )");
            return false;
        }

        //キー値が変更されているかをチェック
        if (($this->field["MAPPING_NO"] != $this->mappingNo) &&
            ($kind=="update" || $kind=="delete")) {
            $this->setWarning("MSG308");
            return false;
        }

        //１レコード取得
        $query = knjl005aQuery::getRow($this->field["ENTEXAMYEAR"], $this->field["APPLICANTDIV"], $this->field["TESTDIV"], $this->field["MAPPING_NO"]);
        $db = Query::dbCheckOut();
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        //重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning("MSG918");
            return false;
        }
        
        if ($this->cmd == "add" || $this->cmd == "update") {
            //認識用文字列の重複チェック
            $checkRow = knjl005aQuery::getExistsSameNameRow($this);
            if ($checkRow && $checkRow["MAPPING_NO"] != $this->field["MAPPING_NO"]) {
                $this->setWarning("MSG302", "\\n入力された「認識用文字列」は\\n「対応コード：{$checkRow["MAPPING_NO"]}」で既に登録されています。");
                return false;
            }
            //対応関係の重複チェック
            $checkRow = knjl005aQuery::getExistsSameMappingRow($this);
            if ($checkRow && $checkRow["MAPPING_NO"] != $this->field["MAPPING_NO"]) {
                $this->setWarning("MSG302", "\\n入力された「専併区分・志望コース・受験型」の組み合わせは\\n「対応コード：{$checkRow["MAPPING_NO"]}」で既に登録されています。");
                return false;
            }
        }
        //存在チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["TESTDIV_NAME"]) > 75) {
            $this->setWarning("MSG915", "( 認識用文字列は75バイトまでです。)");
            return false;
        }

        return true;
    }

    //編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")) {
            knjl005aQuery::getUpdateQuery($this->field);
            $this->setMessage("MSG201");
        }
        return;
    }

    //追加用のモデルを返します
    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")) {
            knjl005aQuery::getInsertQuery($this->field);
            $this->setMessage("MSG201");
        }
        $this->mappingNo = $this->field["MAPPING_NO"];
        return;
    }

    //削除用のモデルを返します
    public function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")) {
            knjl005aQuery::getDeleteQuery($this->field);
            $this->setMessage("MSG201");
            $this->field = "";
        }
        return;
    }

    //次年度のデータを作成
    public function &getCopyYearModel()
    {
        $this->nextYear = $this->year + 1;

        //今年度のデータが存在するか確認
        $t_year = knjl005aQuery::checkData($this->year);
        if (!isset($t_year)) {
            $this->setWarning('MSG203', "今年度のデータが存在しません。");
            return false;
        }
        //前年度のデータが存在するか確認
        $pre_year = knjl005aQuery::checkData($this->nextYear);
        if (isset($pre_year)) {
            $this->setWarning('MSG203', "既に次年度のデータが作成されています。");
            return false;
        }

        knjl005aQuery::getCopyQuery($this->year, $this->nextYear);
        $this->setMessage("MSG201");
        return;
    }

    //変数をクリアします
    public function clean()
    {
    }
}
