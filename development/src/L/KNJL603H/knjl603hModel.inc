<?php
class knjl603hModel extends Model
{
    public $field    = array();
    public $cmd;
    public $leftYear = "";        //primary
    public $testdiv  = "";        //primary

    public function knjl603hModel()
    {
    }

    public function init()
    {
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
        if (VARS::request("ENTEXAMYEAR") == "") {
            if ($this->leftYear == "") {
                $this->leftYear = $this->examyear;
            }
        } else {
            $this->leftYear = VARS::request("ENTEXAMYEAR");
        }

        $this->applicantdiv = "2";

        if (VARS::request("TESTDIV") != "") {
            $this->testdiv = VARS::request("TESTDIV");
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
        if (!$this->checkNextYearTestSubClassCdDat($db, $this->leftYear, $this->nextYear)) {
            $message = $message . '本年度と同様の入試科目が、次年度に一部、もしくは全て存在していません。入試日程マスタ登録にて、本年度と同様の入試科目を次年度に作成してください。';
            $flg = false;
        }
        if ($flg) {
            $query = knjl603hQuery::getCopyPerfectMstQuery($db, $this);
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
        $query = knjl603hQuery::getCheckPerfectMstSql($year);
        
        $result = $db->getOne($query);
        if ($result > 0) {
            return true;
        }
        return false;
    }
    
    public function checkNextYear($db, $nextYear)
    {
        $query = knjl603hQuery::getCheckPerfectMstSql($nextYear);

        $result = $db->getOne($query);
        if ($result == 0) {
            return true;
        }
        return false;
    }
    public function checkNextYearTestSubClassCdDat($db, $thisYear, $nextYear)
    {
        $query = knjl603hQuery::getCheckNextYearTestSubClassCdDatSql($thisYear, $nextYear);

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
                $this->setWarning("MSG301", "( 入試日程コード )");
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
        $query = knjl603hQuery::getRow($this, "chk");
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
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->testSubCd      = $this->field["TESTSUBCLASSCD"];
            $this->setMessage("MSG201");
        }
    }
    //アップデート時の内部メソッド
    public function &update()
    {
        $result = knjl603hQuery::getUpdateQuery($this, $this->field);
        return $result;
    }

    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->insert();
            $this->testSubCd      = $this->field["TESTSUBCLASSCD"];
            $this->setMessage("MSG201");
        }
        return;
    }
    //インサート
    public function &insert()
    {
        $result = knjl603hQuery::getInsertQuery($this, $this->field);
        return $result;
    }

    //DELETE
    public function &getDeleteModel()
    {
        if ($this->validateInput()) {
            knjl603hQuery::getDeleteQuery($this, $this->field);
            $this->testSubCd      = "";
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
