<?php
class knjl3040Model extends Model {
    var $field      = array();
    var $cmd;
    var $leftYear = "";        //primary
    var $sikakuCd = "";        //primary
    var $cntNotPrintColumn = "";

    function knjl3040Model() {
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

        $this->field = array("TESTDIV"          => VARS::post("TESTDIV"),
                             "EXAMNO_FROM"      => VARS::post("EXAMNO_FROM"),
                             "EXAMNO_TO"        => VARS::post("EXAMNO_TO"),
                            );
        //ゼロ埋め
        $this->field["EXAMNO_FROM"] = ($this->field["EXAMNO_FROM"] != "") ? sprintf("%04d", $this->field["EXAMNO_FROM"]) : "" ;  
        $this->field["EXAMNO_TO"]   = ($this->field["EXAMNO_TO"] != "") ? sprintf("%04d", $this->field["EXAMNO_TO"]) : "" ;  

        if (VARS::get("TESTDIV") != "") {
            $this->testdiv = VARS::get("TESTDIV");
        }

        //左画面 抽出条件
        if (VARS::request("ENTEXAMYEAR") != "") {
            $this->leftYear = VARS::request("ENTEXAMYEAR");
        }
        $this->leftYear = ($this->leftYear == "") ? $this->examyear : $this->leftYear;

        if (VARS::request("APPLICANTDIV") != "") {
            $this->applicantdiv = VARS::request("APPLICANTDIV");
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
            $query = knjl3040Query::getCopyExamnoDatQuery($db, $this);
            $db->query($query);
            $this->setMessage("MSG201");
        } else {
            $this->setMessage("MSG203", "\\n" . $message);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    function checkThisYear($db, $year) {
        $query = knjl3040Query::getCheckExamnoDatSql($year);
        
        $result = $db->getOne($query);
        if ($result > 0) {
            return true;
        }
        return false;
    }
    
    function checkNextYear($db, $nextYear) {
        $query = knjl3040Query::getCheckExamnoDatSql($nextYear);

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
                $this->setWarning("MSG301", "( 入試区分 )");
                return false;
            }
        }
        if ($this->cmd == "add" || $this->cmd == "update") {
            if ($this->field["EXAMNO_FROM"] == "") {
                $this->setWarning("MSG301", "( 受験番号帯(開始番号) )");
                return false;
            }
            if ($this->field["EXAMNO_TO"] == "") {
                $this->setWarning("MSG301", "( 受験番号帯(終了番号) )");
                return false;
            }
            //大小チェック
            if (intval($this->field["EXAMNO_TO"]) < intval($this->field["EXAMNO_FROM"])) {
                $this->setWarning("MSG901", "( 開始番号と終了番号の大小が逆です。 )");
                return false;
            }
            //他の入試区分の受験番号帯と範囲が重なっているかチェック
            $db = Query::dbCheckOut();
            $query = knjl3040Query::getList($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $errFlg1 = $row["EXAMNO_FROM"] <= $this->field["EXAMNO_FROM"] && $this->field["EXAMNO_FROM"] <=$row["EXAMNO_TO"];
                $errFlg2 = $row["EXAMNO_FROM"] <= $this->field["EXAMNO_TO"] && $this->field["EXAMNO_TO"] <=$row["EXAMNO_TO"];
                if ($this->field["TESTDIV"] != $row["TESTDIV"] && ($errFlg1 || $errFlg2)) {
                    $this->setWarning("MSG901", "( 他入試区分と受験番号の範囲が重なっています。 )");
                    return false;
                }
            }
            Query::dbCheckIn($db);
        }

        //１レコード取得
        $query = knjl3040Query::getRow($this, "chk");
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
            $this->testdiv      = $this->field["TESTDIV"];
            $this->setMessage("MSG201");
        }
    }
    //アップデート時の内部メソッド
    function &update() {
        $result = knjl3040Query::getUpdateQuery($this, $this->field);
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
        $result = knjl3040Query::getInsertQuery($this, $this->field);
        return $result;
    }

    //DELETE
    function &getDeleteModel() {
        if ($this->validateInput()) {
            knjl3040Query::getDeleteQuery($this, $this->field);
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
