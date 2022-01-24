<?php
class knjl601hModel extends Model
{
    public $field      = array();
    public $cmd;
    public $leftYear = "";        //primary
    public $sikakuCd = "";        //primary
    public $cntNotPrintColumn = "";

    public function knjl601hModel()
    {
    }

    public function init()
    {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->leftYear);
            unset($this->applicantdiv);
        }

        //プロパティファイル
        $this->getPropertiesAll();

        $this->cmd = VARS::request("cmd");

        $this->examyear  = CTRL_YEAR + 1;

        $this->field = array("TESTDIV"         => VARS::post("TESTDIV"),
                             "TESTDIV_NAME"    => VARS::post("TESTDIV_NAME"),
                             "TESTDIV_ABBV"    => VARS::post("TESTDIV_ABBV"),
                             "TEST_DATE"       => VARS::post("TEST_DATE"),
                             "TESTSUBCLASSCD"  => VARS::post("TESTSUBCLASSCD")
                            );

        //左画面 年度コンボ
        if (VARS::request("ENTEXAMYEAR") == "") {
            if ($this->leftYear == "") {
                $this->leftYear = $this->examyear;
            }
        } else {
            $this->leftYear = VARS::request("ENTEXAMYEAR");
        }

        $this->applicantdiv = "2";

        if (VARS::get("TESTDIV") != "") {
            $this->testdiv = VARS::get("TESTDIV");
        }
    }

    //コピーのモデルを返します
    public function &getCopyModel()
    {
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
            $query = knjl601hQuery::getCopyTestDivMstQuery($db, $this);
            $db->query($query);

            $query = knjl601hQuery::getCopyTestSubClassDatQuery($db, $this);
            $db->query($query);

            $this->setMessage("MSG201");
        } else {
            $this->setMessage("MSG203", "\\n" . $message);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    public function checkThisYear($db, $year)
    {
        $query = knjl601hQuery::getCheckTestDivMstSql($year);
        
        $result = $db->getOne($query);
        if ($result > 0) {
            return true;
        }
        return false;
    }
    
    public function checkNextYear($db, $nextYear)
    {
        $query = knjl601hQuery::getCheckTestDivMstSql($nextYear);

        $result = $db->getOne($query);
        if ($result == 0) {
            return true;
        }
        return false;
    }
    
    //入力項目チェック
    public function validateInput()
    {

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
            if ($this->field["TESTDIV"] == "") {
                $this->setWarning("MSG301", "( 入試日程コード )");
                return false;
            }
        }
        if ($this->cmd == "add" || $this->cmd == "update") {
            if ($this->field["TESTDIV_NAME"] == "") {
                $this->setWarning("MSG301", "( 入試日程 )");
                return false;
            }
        }

        //１レコード取得
        $query = knjl601hQuery::getRow($this, "chk");
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
        $checkStrLen["TESTDIV"]       = array("LEN" => 2,  "MESAGE" => "入試日程コード");
        $checkStrLen["TESTDIV_NAME"]  = array("LEN" => 60, "MESAGE" => "入試日程");
        $checkStrLen["TESTDIV_ABBV"]  = array("LEN" => 15, "MESAGE" => "略称");
        foreach ($checkStrLen as $key => $val) {
            if (strlen($this->field[$key]) > $val["LEN"]) {
                $this->setWarning("MSG915", $val["MESAGE"]." ".$val["LEN"]."バイトまで");
                return false;
            }
        }

        //入試日の日付型チェック
        if ($thisfield["TEST_DATE"] != "") {
            $matches = array();
            if (preg_match("/([0-9]{4})[\/]([0-9]{1,2})[\/]([0-9]{1,2})/", $thisfield["TEST_DATE"], $matches) != 1) {
                $this->setWarning("MSG902", "入試日");
                return false;
            } else {
                if (!checkdate($matches[2], $matches[3], $matches[1])) {
                    $this->setWarning("MSG902", "入試日");
                    return false;
                }
            }
        }

        return true;
    }

    //編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->testdiv      = $this->field["TESTDIV"];
            $this->setMessage("MSG201");
        }
    }

    //アップデート時の内部メソッド
    public function &update()
    {
        $result = knjl601hQuery::getUpdateQuery($this, $this->field);
        return $result;
    }

    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->insert();
            $this->testdiv      = $this->field["TESTDIV"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //インサート
    public function &insert()
    {
        $result = knjl601hQuery::getInsertQuery($this, $this->field);
        return $result;
    }

    //DELETE
    public function &getDeleteModel()
    {
        if ($this->validateInput()) {
            knjl601hQuery::getDeleteQuery($this, $this->field);
            $this->testdiv      = "";
            $this->setMessage("MSG201");
            $this->field = array();
        }
    }
    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
}
