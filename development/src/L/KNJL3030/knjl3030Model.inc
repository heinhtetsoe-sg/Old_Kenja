<?php
class knjl3030Model extends Model {
    var $field      = array();
    var $cmd;
    var $leftYear = "";        //primary
    var $sikakuCd = "";        //primary
    var $cntNotPrintColumn = "";

    function knjl3030Model() {
    }

    function init() {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->leftYear);
            unset($this->applicantdiv);
            unset($this->testSubCd);
            unset($this->testdiv);
        }

        //プロパティファイル
        $this->getPropertiesAll();

        $this->cmd = VARS::request("cmd");

        $this->examyear  = CTRL_YEAR + 1;

        $this->field = array("TESTSUBCLASSCD"   => VARS::post("TESTSUBCLASSCD"),
                             "PERFECT"          => VARS::post("PERFECT"),
                            );

        //リンク
        if (VARS::get("TESTSUBCLASSCD") != "") {
            $this->testSubCd = VARS::get("TESTSUBCLASSCD");
        }

        //左画面 抽出条件
        if (VARS::request("ENTEXAMYEAR") != "") {
            $this->leftYear = VARS::request("ENTEXAMYEAR");
        }

        if (VARS::request("APPLICANTDIV") != "") {
            $this->applicantdiv = VARS::request("APPLICANTDIV");
        }
        $this->leftYear = ($this->leftYear == "") ? $this->examyear : $this->leftYear;
        if (VARS::request("TESTDIV") != "") {
            $this->testdiv = VARS::request("TESTDIV");
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
            $query = knjl3030Query::getCopyPerfectMstQuery($db, $this);
            $db->query($query);
            $this->setMessage("MSG201");
        } else {
            $this->setMessage("MSG203", "\\n" . $message);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    function checkThisYear($db, $year) {
        $query = knjl3030Query::getCheckPerfectMstSql($year);
        
        $result = $db->getOne($query);
        if ($result > 0) {
            return true;
        }
        return false;
    }
    
    function checkNextYear($db, $nextYear) {
        $query = knjl3030Query::getCheckPerfectMstSql($nextYear);

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
            if ($this->testSubCd != $this->field["TESTSUBCLASSCD"]) {
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
            if ($this->testdiv == "") {
                $this->setWarning("MSG301", "( 入試区分コード )");
                return false;
            }
            if ($this->field["TESTSUBCLASSCD"] == "") {
                $this->setWarning("MSG301", "( 試験科目 )");
                return false;
            }
        }
        if ($this->cmd == "add" || $this->cmd == "update") {
            if ($this->field["PERFECT"] == "") {
                $this->setWarning("MSG301", "( 満点 )");
                return false;
            }
        }

        //１レコード取得
        $query = knjl3030Query::getRow($this, "chk");
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

        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->testSubCd      = $this->field["TESTSUBCLASSCD"];
            $this->setMessage("MSG201");
        }
    }
    //アップデート時の内部メソッド
    function &update() {
        $result = knjl3030Query::getUpdateQuery($this, $this->field);
        return $result;
    }

    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->insert();
            $this->testSubCd      = $this->field["TESTSUBCLASSCD"];
            $this->setMessage("MSG201");
        }
        return;
    }
    //インサート
    function &insert() {
        $result = knjl3030Query::getInsertQuery($this, $this->field);
        return $result;
    }

    //DELETE
    function &getDeleteModel() {
        if ($this->validateInput()) {
            knjl3030Query::getDeleteQuery($this, $this->field);
            $this->testSubCd      = "";
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
