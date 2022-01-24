<?php

require_once('for_php7.php');

class knjz452Model extends Model {
    var $field      = array();
    var $cmd;
    var $leftYear = "";           //primary
    var $sikakuCd = "";        //primary
    var $cntNotPrintColumn = "";

    function knjz452Model() {
    }

    function init() {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->leftYear);
            unset($this->sikakuCd);
            unset($this->div);
            unset($this->resultCd);
        }

        //プロパティファイル
        $this->getPropertiesAll();

        $this->cmd = VARS::request("cmd");

        if (VARS::request("YEAR") != "") {
            $this->leftYear = VARS::request("YEAR");
        }
        $this->leftYear = ($this->leftYear == "") ? CTRL_YEAR: $this->leftYear;

        if (VARS::request("SIKAKUCD") != "") {
            $this->sikakuCd = VARS::request("SIKAKUCD");
        }
        $db = Query::dbCheckOut(); //DB接続
        $setSikakuCd = $db->getOne(knjz452Query::getQualifiedMst());
        $this->sikakuCd = ($this->sikakuCd == "") ? $setSikakuCd: $this->sikakuCd;

        if (VARS::request("DIV") != "") {
            $this->div = VARS::request("DIV");
        }
        $this->div = ($this->div == "") ? "1": $this->div;

        // NOT_PRINTがテーブルにあるかどうか取得
        $this->cntNotPrintColumn = $db->getOne(knjz452Query::getSyscatColumns($this->div));

        Query::dbCheckIn($db);     //DB切断

        if ($this->div == "1") {
            $this->field = array("KYUU_CD1"            =>  VARS::post("KYUU_CD1"),          //級CD
                                 "JOUKYOUCD1"          =>  VARS::post("JOUKYOUCD1"),        //状況CD
                                 "RESULT_NAME"         =>  VARS::post("RESULT_NAME"),       //結果名
                                 "RESULT_NAME_ABBV"    =>  VARS::post("RESULT_NAME_ABBV"),  //結果略称
                                 "CERT_FLG"            =>  VARS::post("CERT_FLG"),          //正式資格
                                 "LIMITED_PERIOD"      =>  VARS::post("LIMITED_PERIOD"),    //有効期間
                                 "RESULT_LEVEL"        =>  VARS::post("RESULT_LEVEL")       //レベル
                                 );
        } else {
            $this->field = array("KYUU_CD2"            =>  VARS::post("KYUU_CD2"),          //級CD
                                 "JOUKYOUCD2"          =>  VARS::post("JOUKYOUCD2"),        //状況CD
                                 "TEST_NAME"           =>  VARS::post("TEST_NAME"),         //受験級名称
                                 "TEST_NAME_ABBV"      =>  VARS::post("TEST_NAME_ABBV"),    //受験級略称
                                 "TEST_FEE"            =>  VARS::post("TEST_FEE"),          //受験料
                                 "PREREQ_RESALT_CD"    =>  VARS::post("PREREQ_RESALT_CD"),  //前提結果CD
                                 "TEST_LEVEL"          =>  VARS::post("TEST_LEVEL")         //レベル
                                 );
        }

        if ($this->div == "1") {
            $temp = VARS::get("RESULT_CD");
            if (isset($temp)) {
                $this->resultCd = VARS::get("RESULT_CD");
            }
        } else {
            $temp = VARS::get("TEST_CD");
            if (isset($temp)) {
                $this->testCd = VARS::get("TEST_CD");
            }
        }

        if ($this->cntNotPrintColumn > 0) {
            $this->field["NOT_PRINT"] = VARS::post("NOT_PRINT");      //出力設定
            $this->field["SCORE"]     = VARS::post("SCORE");          //得点
        }
    }

    //コピーのモデルを返します
    function &getCopyModel() {
        knjz452Query::getCopyDelResaltMstQuery($this);
        knjz452Query::getCopyResaltMstQuery($this);
        knjz452Query::getCopyDelTestMstQuery($this);
        knjz452Query::getCopyTestMstQuery($this);
        $this->setMessage('MSG201');
    }

    //入力項目チェック
    function validateInput() {
        //コードのゼロ埋め
        $this->field["RESULT_CD"]  = sprintf("%03d", $this->field["KYUU_CD1"]);
        $this->field["RESULT_CD"] .= $this->field["JOUKYOUCD1"];
        $this->field["TEST_CD"]    = sprintf("%03d", $this->field["KYUU_CD2"]);
        $this->field["TEST_CD"]   .= $this->field["JOUKYOUCD2"];

        //キーのチェック
        if ($this->div == "1") {
            if (strlen($this->field["KYUU_CD1"]) == 0 && $this->cmd == "add") {
                $this->setWarning("MSG301","(結果CD)");
                return false;
            }
            if (strlen($this->field["RESULT_NAME"]) == 0 && $this->cmd == "add") {
                $this->setWarning("MSG301","(結果名)");
                return false;
            }
            if (strlen($this->field["RESULT_NAME_ABBV"]) == 0 && $this->cmd == "add") {
                $this->setWarning("MSG301","(結果略称)");
                return false;
            }
            if (strlen($this->field["CERT_FLG"]) == 0) {
                $this->setWarning("MSG301","(正式資格)");
                return false;
            }
            if (strlen($this->field["RESULT_LEVEL"]) == 0) {
                $this->setWarning("MSG301","(レベル)");
                return false;
            }

            if ($this->cmd == "update" && ($this->resultCd != $this->field["RESULT_CD"] || $this->resultCd == "")) {
                $this->setWarning("MSG308","キー値は変更できません");
                return;
            }
            $setMsseage = "結果CD";
        } else {
            if (strlen($this->field["KYUU_CD2"]) == "" && $this->cmd == "add") {
                $this->setWarning("MSG301","(テストCD)");
                return false;
            }
            if (strlen($this->field["TEST_NAME"]) == "" && $this->cmd == "add") {
                $this->setWarning("MSG301","(受験級名称)");
                return false;
            }
            if (strlen($this->field["TEST_FEE"]) == 0) {
                $this->setWarning("MSG301","(受験料)");
                return false;
            }
            if (strlen($this->field["TEST_LEVEL"]) == 0) {
                $this->setWarning("MSG301","(レベル)");
                return false;
            }

            if ($this->cmd == "update" && ($this->testCd != $this->field["TEST_CD"] || $this->testCd == "")) {
                $this->setWarning("MSG308","キー値は変更できません");
                return;
            }
            $setMsseage = "テストCD";
        }

        //１レコード取得
        $setRScd = ($this->div == "1") ? $this->field["RESULT_CD"]: $this->field["TEST_CD"];
        $query = knjz452Query::getRow($this, $setRScd);
        $db = Query::dbCheckOut(); //DB接続
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);     //DB切断
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning("MSG918", "{$setMsseage}");
            return false;
        }
        //文字数チェック
        $checkStrLen["RESULT_NAME"]      = array("LEN" => 60, "MESAGE" => "結果名名称");
        $checkStrLen["RESULT_NAME_ABBV"] = array("LEN" => 50, "MESAGE" => "結果略称");
        $checkStrLen["TEST_NAME"]        = array("LEN" => 60, "MESAGE" => "受験級名称");
        $checkStrLen["TEST_NAME_ABBV"]   = array("LEN" => 50, "MESAGE" => "受験級称");
        if ($this->cntNotPrintColumn > 0) {
            $checkStrLen["SCORE"]   = array("LEN" => 4, "SCORE" => "得点");
        }
        foreach ($checkStrLen as $key => $val) {
            if (strlen($this->field[$key]) > $val["LEN"] ) {
                $this->setWarning("MSG915", $val["MESAGE"]." ".$val["LEN"]."バイトまで");
                return false;
            }
        }
        //数値チェック
        if ($this->cntNotPrintColumn > 0) {
            if ($this->field["SCORE"] != "" && !is_numeric($this->field["SCORE"])) {
                $this->setWarning("MSG905","(得点)");
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
            $this->setMessage("MSG201");
        }
    }
    //アップデート時の内部メソッド
    function &update() {
        $result = knjz452Query::getUpdateQuery($this, $this->field);
        return $result;
    }

    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->insert();
            $this->resultCd = $this->field["RESULT_CD"];
            $this->testCd   = $this->field["TEST_CD"];
            $this->setMessage("MSG201");
        }
        return;
    }
    //インサート
    function &insert() {
        $result = knjz452Query::getInsertQuery($this, $this->field);
        return $result;
    }

    //DELETE
    function &getDeleteModel() {
        //コードのゼロ埋め
        $this->field["RESULT_CD"]  = sprintf("%03d", $this->field["KYUU_CD1"]);
        $this->field["RESULT_CD"] .= $this->field["JOUKYOUCD1"];
        $this->field["TEST_CD"]    = sprintf("%03d", $this->field["KYUU_CD2"]);
        $this->field["TEST_CD"]   .= $this->field["JOUKYOUCD2"];

        //キーの更新チェック
        if ($this->div == "1") {
            if ($this->resultCd == "") {
                $this->setWarning("MSG308","キー値は変更できません");
                return false;
            }
            if ($this->resultCd != $this->field["RESULT_CD"]) {
                $this->setWarning("MSG308","キー値は変更できません");
                return;
            }
        } else {
            if ($this->testCd == "") {
                $this->setWarning("MSG308","キー値は変更できません");
                return false;
            }
            if ($this->testCd != $this->field["TEST_CD"]) {
                $this->setWarning("MSG308","キー値は変更できません");
                return;
            }
        }

        $setRScd = ($this->div == "1") ? $this->field["RESULT_CD"]: $this->field["TEST_CD"];
        $query = knjz452Query::getRow($this, $setRScd);
        $db = Query::dbCheckOut(); //DB接続
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);     //DB切断
        if (!is_array($row)) {
            $this->setWarning("MSG303");
            return;
        }
        knjz452Query::getDeleteQuery($this, $this->field);
        $this->resultCd = "";
        $this->testCd   = "";
        $this->setMessage("MSG201");
        
        $this->field = array();
    }
    /**
     * 変数をクリアします
     */
    function clean() {
    }
}
?>
