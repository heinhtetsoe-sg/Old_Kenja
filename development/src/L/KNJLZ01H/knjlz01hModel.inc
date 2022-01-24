<?php
class knjlz01hModel extends Model {
    var $cmd;
    var $examYear;
    var $leftYear = "";
    var $applicantdiv;
    var $testdiv;
    var $field    = array();

    function knjlz01hModel() {
    }

    function init() {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->leftYear);
            unset($this->applicantdiv);
            unset($this->testdiv);
            unset($this->field);
        }

        //プロパティファイル
        $this->getPropertiesAll();

        $this->cmd = VARS::request("cmd");

        $this->examYear  = CTRL_YEAR + 1;

        $this->field = array("TESTDIV"                => VARS::post("TESTDIV"),
                             "TESTDIV_NAME"           => VARS::post("TESTDIV_NAME"),
                             "TESTDIV_ABBV"           => VARS::post("TESTDIV_ABBV"),
                             "CONDITION_HYOTEI"       => VARS::post("CONDITION_HYOTEI"),
                             "CONDITION_GOKYOKA"      => VARS::post("CONDITION_GOKYOKA"),
                             "CAPACITY_MALE"          => VARS::post("CAPACITY_MALE"),
                             "CAPACITY_FEMALE"        => VARS::post("CAPACITY_FEMALE"),
                             "TEST_DATE"              => VARS::post("TEST_DATE"),
                            );

        //左画面 年度コンボ
        if (VARS::request("ENTEXAMYEAR") != "") {
            $this->leftYear = VARS::request("ENTEXAMYEAR");
        }
        $this->leftYear = ($this->leftYear == "") ? $this->examYear : $this->leftYear;

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

        $nextYear = $this->leftYear + 1;
        $message = '';
        $flg = true;

        //コピー元データの存在確認
        if ($this->checkTestDivMstYear($db, $this->leftYear) <= 0) {
            $message = '作成するデータが存在しません。';
            $flg = false;
        }
        //コピー先データの存在確認
        if ($this->checkTestDivMstYear($db, $nextYear) > 0) {
            $message = $message . '既に次年度のデータが作成されています。';
            $flg = false;
        }

        if ($flg) {
            $result = knjlz01hQuery::getCopyTestDivMstQuery($this);
    
            $this->setMessage("MSG201");
        } else {
            $this->setMessage("MSG203", "\\n" . $message);
        }

        Query::dbCheckIn($db);
    }

    function checkTestDivMstYear($db, $year) {
        $query = knjlz01hQuery::getCheckTestDivMstSql($year);
        $result = $db->getOne($query);
        return $result;
    }

    //入力項目チェック
    function validateInput() {

        //ゼロ埋め
        if ($this->field["TESTDIV"] != "") {
            $this->field["TESTDIV"] = sprintf("%02d", $this->field["TESTDIV"]);
        }

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
                $this->setWarning("MSG301", "( 学校制度 )");
                return false;
            }
            if ($this->field["TESTDIV"] == "") {
                $this->setWarning("MSG301", "( 入試区分ＣＤ )");
                return false;
            }
        }
        if ($this->cmd == "add" || $this->cmd == "update") {
            if ($this->field["TESTDIV_NAME"] == "") {
                $this->setWarning("MSG301", "( 入試区分名 )");
                return false;
            }
            if ($this->field["TEST_DATE"] == "") {
                $this->setWarning("MSG301","( 試験日 )");
                return false;
            }
        }

        //１レコード取得
        $query = knjlz01hQuery::getRow($this, "chk");
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
        if ($this->cmd == "add" || $this->cmd == "update") {
            $checkStrLen = array();
            $checkStrLen["TESTDIV"]           = array("LEN" => 2,  "MESAGE" => "入試区分ＣＤ");
            $checkStrLen["TESTDIV_NAME"]      = array("LEN" => 30, "MESAGE" => "入試区分名称");
            $checkStrLen["TESTDIV_ABBV"]      = array("LEN" => 9,  "MESAGE" => "入試区分略称");
            $checkStrLen["CONDITION_HYOTEI"]  = array("LEN" => 3,  "MESAGE" => "出願条件評定合計");
            $checkStrLen["CONDITION_GOKYOKA"] = array("LEN" => 3,  "MESAGE" => "出願条件５教科合計");
            $checkStrLen["CAPACITY_MALE"]     = array("LEN" => 3,  "MESAGE" => "募集人数　男子");
            $checkStrLen["CAPACITY_FEMALE"]   = array("LEN" => 3,  "MESAGE" => "募集人数　女子");
            foreach ($checkStrLen as $key => $val) {
                if (strlen($this->field[$key]) > $val["LEN"] ) {
                    $this->setWarning("MSG915", $val["MESAGE"]." ".$val["LEN"]."バイトまで");
                    return false;
                }
            }

            //数値チェック
            $checkStrLen = array();
            $checkStrLen["TESTDIV"]         = array("MESAGE" => "入試区分ＣＤ");
            $checkStrLen["CAPACITY_MALE"]   = array("MESAGE" => "募集人数　男子");
            $checkStrLen["CAPACITY_FEMALE"] = array("MESAGE" => "募集人数　女子");
            foreach ($checkStrLen as $key => $val) {
                if ($this->field[$key] != "" && !preg_match('/^[0-9]+$/', $this->field[$key])) {
                    $this->setWarning("MSG907", "( ".$val["MESAGE"]." )");
                    return false;
                }
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
        $result = knjlz01hQuery::getUpdateQuery($this, $this->field);
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
        $result = knjlz01hQuery::getInsertQuery($this, $this->field);
        return $result;
    }

    //DELETE
    function &getDeleteModel() {
        if ($this->validateInput()) {
            knjlz01hQuery::getDeleteQuery($this, $this->field);
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
