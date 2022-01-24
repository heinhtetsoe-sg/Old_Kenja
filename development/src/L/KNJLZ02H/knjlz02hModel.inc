<?php
class knjlz02hModel extends Model {
    var $cmd;
    var $examYear;
    var $leftYear = "";
    var $applicantdiv;
    var $testdiv;
    var $testSubCd;
    var $field    = array();

    function knjlz02hModel() {
    }

    function init() {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->leftYear);
            unset($this->applicantdiv);
            unset($this->testdiv);
            unset($this->testSubCd);
        }

        //プロパティファイル
        $this->getPropertiesAll();

        $this->cmd = VARS::request("cmd");

        $this->examYear  = CTRL_YEAR + 1;

        $this->field = array(
                        "TESTSUBCLASSCD"    => VARS::post("TESTSUBCLASSCD"),    // 科目コード
                        "TESTSUBCLASS_NAME" => VARS::post("TESTSUBCLASS_NAME"), // 科目名称
                        "PERFECT"           => VARS::post("PERFECT"),           // 満点
                        "NAMESEX_HIDE_FLAG" => VARS::post("NAMESEX_HIDE_FLAG"), // 氏名・性別非表示
                        "TESTSUBCLASS_KIND" => VARS::post("TESTSUBCLASS_KIND"), // 科目種別
                        );

        //左画面 抽出条件
        if (VARS::request("ENTEXAMYEAR") != "") {
            $this->leftYear = VARS::request("ENTEXAMYEAR");
        }
        $this->leftYear = ($this->leftYear == "") ? $this->examYear : $this->leftYear;

        if (VARS::request("APPLICANTDIV") != "") {
            $this->applicantdiv = VARS::request("APPLICANTDIV");
        }

        if (VARS::request("TESTDIV") != "") {
            $this->testdiv = VARS::request("TESTDIV");
        }

        //リンク
        if (VARS::get("TESTSUBCLASSCD") != "") {
            $this->testSubCd = VARS::get("TESTSUBCLASSCD");
        }
    }

    //コピーのモデルを返します
    function &getCopyModel() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $nextYear = $this->leftYear + 1;
        $message = '';
        $flg = true;

        //コピー元データの存在確認
        if ($this->checkTestSubClassCdDatYear($db, $this->leftYear) <= 0) {
            $message = '作成するデータが存在しません。';
            $flg = false;
        }
        //コピー先データの存在確認
        if ($this->checkTestSubClassCdDatYear($db, $nextYear) > 0) {
            $message = $message . '既に次年度のデータが作成されています。';
            $flg = false;
        }

        if ($flg) {
            $query = knjlz02hQuery::getCopyTestSubClassCdDatQuery($this);
            $db->query($query);
            $this->setMessage("MSG201");
        } else {
            $this->setMessage("MSG203", "\\n" . $message);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    function checkTestSubClassCdDatYear($db, $year) {
        $query = knjlz02hQuery::getCheckTestSubClassCdDatSql($year);
        $result = $db->getOne($query);
        return $result;
    }

    //入力項目チェック
    function validateInput() {

        //ゼロ埋め
        if ($this->field["TESTSUBCLASSCD"] != "") {
            $this->field["TESTSUBCLASSCD"] = sprintf("%02d", $this->field["TESTSUBCLASSCD"]);
        }

        //キー変更チェック
        if ($this->cmd == "update" || $this->cmd == "delete") {
            // $flg = false;
            if ($this->testSubCd != $this->field["TESTSUBCLASSCD"]) {
                $this->setWarning("MSG308");
                return false;
            }
        }

        //必須チェック
        if ($this->cmd == "add" || $this->cmd == "update" || $this->cmd == "delete") {
            if ($this->applicantdiv == "") {
                $this->setWarning("MSG301", "( 学校制度 )");
                return false;
            }
            if ($this->testdiv == "") {
                $this->setWarning("MSG301", "( 入試区分コード )");
                return false;
            }
            if ($this->field["TESTSUBCLASSCD"] == "") {
                $this->setWarning("MSG301", "( 科目コード )");
                return false;
            }
        }
        if ($this->cmd == "add" || $this->cmd == "update") {
            if ($this->field["TESTSUBCLASS_NAME"] == "") {
                $this->setWarning("MSG301", "( 科目名称 )");
                return false;
            }
            if ($this->field["PERFECT"] == "") {
                $this->setWarning("MSG301", "( 満点 )");
                return false;
            }
        }

        //１レコード取得
        $query = knjlz02hQuery::getRow($this, "chk");
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
        $checkStrLen = array();
        $checkStrLen["TESTSUBCLASSCD"]    = array("LEN" => 2,  "MESAGE" => "科目コード");
        $checkStrLen["TESTSUBCLASS_NAME"] = array("LEN" => 15, "MESAGE" => "科目名称");
        foreach ($checkStrLen as $key => $val) {
            if (strlen($this->field[$key]) > $val["LEN"] ) {
                $this->setWarning("MSG915", $val["MESAGE"]." ".$val["LEN"]."バイトまで");
                return false;
            }
        }

        //数値チェック
        $checkStrLen = array();
        foreach ($checkStrLen as $key => $val) {
            if ($this->field[$key] != "" && !preg_match('/^[0-9]+$/', $this->field[$key])) {
                $this->setWarning("MSG907", "( ".$val["MESAGE"]." )");
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
            $this->testSubCd = $this->field["TESTSUBCLASSCD"];
            $this->setMessage("MSG201");
        }
    }
    //アップデート時の内部メソッド
    function &update() {
        $result = knjlz02hQuery::getUpdateQuery($this, $this->field);
        return $result;
    }

    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->insert();
            $this->testSubCd = $this->field["TESTSUBCLASSCD"];
            $this->setMessage("MSG201");
        }
        return;
    }
    //インサート
    function &insert() {
        $result = knjlz02hQuery::getInsertQuery($this, $this->field);
        return $result;
    }

    //DELETE
    function &getDeleteModel() {
        if ($this->validateInput()) {
            knjlz02hQuery::getDeleteQuery($this, $this->field);
            $this->testSubCd = "";
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
