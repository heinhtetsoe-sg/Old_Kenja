<?php
class knjlz01iModel extends Model {
    var $field      = array();
    var $cmd;
    var $leftYear = "";        //primary
    var $applicantdiv = "";
    var $testdiv = "";
    var $cntNotPrintColumn = "";

    function knjlz01iModel() {
    }

    function init() {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->leftYear);
            unset($this->applicantdiv);
            unset($this->testdiv);
        }

        //プロパティファイル
        $this->getPropertiesAll();

        $this->cmd = VARS::request("cmd");

        $this->examyear  = CTRL_YEAR + 1;

        $this->field = array("TESTDIV"         => VARS::post("TESTDIV"),
                             "TESTDIV_NAME"    => VARS::post("TESTDIV_NAME"),
                             "TESTDIV_ABBV"    => VARS::post("TESTDIV_ABBV"),
                             "CAPACITY"        => VARS::post("CAPACITY"),
                            );

        //左画面 年度コンボ
        if (VARS::request("ENTEXAMYEAR") != "") {
            $this->leftYear = VARS::request("ENTEXAMYEAR");
        }
        $this->leftYear = ($this->leftYear == "") ? $this->examyear : $this->leftYear;

        if (VARS::request("APPLICANTDIV") != "") {
            $this->applicantdiv = VARS::request("APPLICANTDIV");
        }

        if (VARS::get("TESTDIV") != "") {
            $this->testdiv = VARS::get("TESTDIV");
        }
    }

    //コピーのモデルを返します
    function &getCopyModel() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $this->nextYear = $this->leftYear + 1;
        $message = '';
        $flg = true;
        if (!$this->checkThisYear($db, $this->leftYear)) {
            $message = '作成するデータが存在しません。';
            $flg = false;
        }
        if (!$this->checkNextYear($db, $this->nextYear)) {
            $message = $message . '既に次年度のデータが作成されています。';
            $flg = false;
        }
        if ($flg) {
            $query = knjlz01iQuery::getCopyTestDivMstQuery($db, $this);
            $db->query($query);
            $this->setMessage("MSG201");
        } else {
            $this->setMessage("MSG203", "\\n" . $message);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    function checkThisYear($db, $year) {
        $query = knjlz01iQuery::getCheckTestDivMstSql($year);
        
        $result = $db->getOne($query);
        if ($result > 0) {
            return true;
        }
        return false;
    }
    
    function checkNextYear($db, $nextYear) {
        $query = knjlz01iQuery::getCheckTestDivMstSql($nextYear);

        $result = $db->getOne($query);
        if ($result == 0) {
            return true;
        }
        return false;
    }
    
    //入力項目チェック
    function validateInput() {

        //キー変更チェック
        if ($this->cmd == "update" || $this->cmd == "delete") {
            $flg = false;
            if ($this->testdiv != $this->field["TESTDIV"]) {
                $this->setWarning("MSG308");
                return false;
            }
        }

        //必須チェック
        if ($this->cmd == "add" || $this->cmd == "update" || $this->cmd == "delete") {
            if ($this->applicantdiv == "") {
                $this->setWarning("MSG301", "( 入試制度 )");
                return false;
            }
            if ($this->field["TESTDIV"] == "") {
                $this->setWarning("MSG301", "( 入試区分コード )");
                return false;
            }
        }
        if ($this->cmd == "add" || $this->cmd == "update") {
            if ($this->field["TESTDIV_NAME"] == "") {
                $this->setWarning("MSG301", "( 入試区分名 )");
                return false;
            }
        }

        //１レコード取得
        $query = knjlz01iQuery::getRow($this, "chk");
        $db = Query::dbCheckOut(); //DB接続
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);     //DB切断
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning("MSG918", "{$setMsseage}");
            return false;
        }

        //文字数チェック
        $checkStrLen["TESTDIV"]       = array("LEN" => 2,  "MESAGE" => "入試区分コード");
        $checkStrLen["TESTDIV_NAME"]  = array("LEN" => 60, "MESAGE" => "入試区分名");
        $checkStrLen["TESTDIV_ABBV"]  = array("LEN" => 30, "MESAGE" => "略称");
        $checkStrLen["CAPACITY"]      = array("LEN" => 3,  "MESAGE" => "募集定員数");
        foreach ($checkStrLen as $key => $val) {
            if (strlen($this->field[$key]) > $val["LEN"] ) {
                $this->setWarning("MSG915", $val["MESAGE"]." ".$val["LEN"]."バイトまで");
                return false;
            }
        }

        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->testdiv      = $this->field["TESTDIV"];
            $this->setMessage("MSG201");
        }
    }
    //アップデート時の内部メソッド
    function &update() {
        $result = knjlz01iQuery::getUpdateQuery($this, $this->field);
        return $result;
    }

    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->insert();
            $this->testdiv      = $this->field["TESTDIV"];
            $this->setMessage("MSG201");
        }
        return;
    }
    //インサート
    function &insert() {
        $result = knjlz01iQuery::getInsertQuery($this, $this->field);
        return $result;
    }

    //DELETE
    function &getDeleteModel() {
        if ($this->validateInput()) {
            knjlz01iQuery::getDeleteQuery($this, $this->field);
            $this->testdiv      = "";
            $this->setMessage("MSG201");        
            $this->field = array();
        }
    }
    /**
     * 変数をクリアします
     */
    function clean() {
    }
}
?>
