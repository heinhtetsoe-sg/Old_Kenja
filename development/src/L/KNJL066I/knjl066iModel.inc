<?php
class knjl066iModel extends Model {
    var $field      = array();
    var $cmd;
    var $leftYear = "";           //primary
    var $sikakuCd = "";        //primary
    var $cntNotPrintColumn = "";

    function knjl066iModel() {
    }

    function init() {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->leftYear);
            unset($this->applicantdiv);
            unset($this->testdiv);
            unset($this->groupdiv);
        }

        //プロパティファイル
        $this->getPropertiesAll();

        $this->cmd = VARS::request("cmd");

        $this->examyear  = CTRL_YEAR + 1;

        $this->field = array("APPLICANTDIV"     => VARS::post("APPLICANTDIV"),
                             "TESTDIV"          => VARS::post("TESTDIV"),
                             "GROUPCD"          => (VARS::post("GROUPCD") != "") ? sprintf("%02d", VARS::post("GROUPCD")) : "",
                             "GROUPNAME"        => VARS::post("GROUPNAME"),
                             "GROUPNAME_ABBV"   => VARS::post("GROUPNAME_ABBV"),
                             "GROUPPEOPLE"      => VARS::post("GROUPPEOPLE"),
                            );

        if (VARS::get("APPLICANTDIV") != "") {
            $this->applicantdiv = VARS::get("APPLICANTDIV");
        }
        if (VARS::get("TESTDIV") != "") {
            $this->testdiv = VARS::get("TESTDIV");
        }
        if (VARS::get("GROUPCD") != "") {
            $this->groupcd = (VARS::get("GROUPCD") != "") ? sprintf("%02d", VARS::get("GROUPCD")) : "";
        }

        if (VARS::request("GROUPDIV") != "") {
            $this->groupdiv = VARS::request("GROUPDIV");
        }
        //左画面 年度コンボ
        if (VARS::request("ENTEXAMYEAR") != "") {
            $this->leftYear = VARS::request("ENTEXAMYEAR");
        }
        $this->leftYear = ($this->leftYear == "") ? $this->examyear : $this->leftYear;

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
            $query = knjl066iQuery::getCopyHallYdatQuery($db, $this);
            $db->query($query);
            $this->setMessage("MSG201");
        } else {
            $this->setMessage("MSG203", "\\n" . $message);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    function checkThisYear($db, $year) {
        $query = knjl066iQuery::getCheckHallYdatSql($year);
        
        $result = $db->getOne($query);
        if ($result > 0) {
            return true;
        }
        return false;
    }
    
    function checkNextYear($db, $nextYear) {
        $query = knjl066iQuery::getCheckHallYdatSql($nextYear);

        $result = $db->getOne($query);
        if ($result == 0) {
            return true;
        }
        return false;
    }

    //入力項目チェック
    function validateInput() {

        $divName = ($this->groupdiv == "1") ? "受験" : "面接";
        //キー変更チェック
        if ($this->cmd == "update" || $this->cmd == "delete") {
            $flg = false;
            if ($this->applicantdiv != $this->field["APPLICANTDIV"]
                || $this->testdiv != $this->field["TESTDIV"]
                || $this->groupcd != $this->field["GROUPCD"]) {
                $this->setWarning("MSG308");
                return false;
            }
        }

        //必須チェック
        if ($this->cmd == "add" || $this->cmd == "update" || $this->cmd == "delete") {
            if ($this->field["APPLICANTDIV"] == "") {
                $this->setWarning("MSG301", "( 入試制度 )");
                return false;
            }
            if ($this->field["TESTDIV"] == "") {
                $this->setWarning("MSG301", "( 入試区分 )");
                return false;
            }
            if ($this->field["GROUPNAME"] == "") {
                $this->setWarning("MSG301", "( {$divName}班コード )");
                return false;
            }
        }
        if ($this->cmd == "add" || $this->cmd == "update") {
            if ($this->field["GROUPNAME"] == "") {
                $this->setWarning("MSG301", "( {$divName}班名 )");
                return false;
            }
            if ($this->field["GROUPPEOPLE"] == "") {
                $this->setWarning("MSG301", "( 人数 )");
                return false;
            }
        }

        //１レコード取得
        $query = knjl066iQuery::getRow($this, "chk");
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
        $checkStrLen["GROUPCD"]         = array("LEN" => 2, "MESAGE"  => "{$divName}班コード");
        $checkStrLen["GROUPNAME"]       = array("LEN" => 30, "MESAGE" => "{$divName}班名");
        $checkStrLen["GROUPNAME_ABBV"]  = array("LEN" => 15, "MESAGE" => "略称");
        $checkStrLen["GROUPPEOPLE"]     = array("LEN" => 2,  "MESAGE" => "人数");
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
            $this->applicantdiv = $this->field["APPLICANTDIV"];
            $this->testdiv      = $this->field["TESTDIV"];
            $this->groupcd      = $this->field["GROUPCD"];
            $this->setMessage("MSG201");
        }
    }
    //アップデート時の内部メソッド
    function &update() {
        $result = knjl066iQuery::getUpdateQuery($this, $this->field);
        return $result;
    }

    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->insert();
            $this->applicantdiv = $this->field["APPLICANTDIV"];
            $this->testdiv      = $this->field["TESTDIV"];
            $this->groupcd      = $this->field["GROUPCD"];
            $this->setMessage("MSG201");
        }
        return;
    }
    //インサート
    function &insert() {
        $result = knjl066iQuery::getInsertQuery($this, $this->field);
        return $result;
    }

    //DELETE
    function &getDeleteModel() {
        if ($this->validateInput()) {
            knjl066iQuery::getDeleteQuery($this, $this->field);
            $this->applicantdiv = "";
            $this->testdiv      = "";
            $this->groupcd      = "";
            $this->setMessage("MSG201");
        }
        
        $this->field = array();
    }
    /**
     * 変数をクリアします
     */
    function clean() {
    }
}
?>
