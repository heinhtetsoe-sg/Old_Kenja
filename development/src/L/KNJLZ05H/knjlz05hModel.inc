<?php
class knjlz05hModel extends Model
{
    var $cmd;
    var $examYear;
    var $leftYear = "";
    var $applicantdiv;
    var $kinddiv;
    var $field    = array();

    function knjlz05hModel()
    {
    }

    function init()
    {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->leftYear);
            unset($this->applicantdiv);
            unset($this->kinddiv);
            unset($this->field);
        }

        //プロパティファイル
        $this->getPropertiesAll();

        $this->cmd = VARS::request("cmd");

        $this->examYear  = CTRL_YEAR + 1;

        //左画面 年度コンボ
        if (VARS::request("ENTEXAMYEAR") != "") {
            $this->leftYear = VARS::request("ENTEXAMYEAR");
        }
        $this->leftYear = ($this->leftYear == "") ? $this->examYear : $this->leftYear;

        if (VARS::request("APPLICANTDIV") != "") {
            $this->applicantdiv = VARS::request("APPLICANTDIV");
        }

        if (VARS::get("KINDDIV") != "") {
            $this->kinddiv = VARS::get("KINDDIV");
        }

        $this->field = array(
            "KINDDIV"      => VARS::post("KINDDIV"),
            "KINDDIV_NAME" => VARS::post("KINDDIV_NAME"),
            "KINDDIV_ABBV" => VARS::post("KINDDIV_ABBV"),
        );
    }

    //コピーのモデルを返します
    function &getCopyModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $nextYear = $this->leftYear + 1;
        $message = '';
        $flg = true;

        //コピー元データの存在確認
        if ($this->checkKindDivMstYear($db, $this->leftYear) <= 0) {
            $message = '作成するデータが存在しません。';
            $flg = false;
        }
        //コピー先データの存在確認
        if ($this->checkKindDivMstYear($db, $nextYear) > 0) {
            $message = $message . '既に次年度のデータが作成されています。';
            $flg = false;
        }

        if ($flg) {
            $query = knjlz05hQuery::getCopyKindDivMstQuery($this);
            $db->query($query);
            $this->setMessage("MSG201");
        } else {
            $this->setMessage("MSG203", "\\n" . $message);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    function checkKindDivMstYear($db, $year)
    {
        $query = knjlz05hQuery::getCheckKindDivMstSql($year);
        $result = $db->getOne($query);
        return $result;
    }

    //入力項目チェック
    function validateInput()
    {

        //ゼロ埋め
        if ($this->field["KINDDIV"] != "") {
            $this->field["KINDDIV"] = sprintf("%02d", $this->field["KINDDIV"]);
        }

        //キー変更チェック
        if ($this->cmd == "update" || $this->cmd == "delete") {
            // $flg = false;
            if ($this->kinddiv != $this->field["KINDDIV"]) {
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
            if ($this->field["KINDDIV"] == "") {
                $this->setWarning("MSG301", "( 入試種別ＣＤ )");
                return false;
            }
        }
        if ($this->cmd == "add" || $this->cmd == "update") {
            if ($this->field["KINDDIV_NAME"] == "") {
                $this->setWarning("MSG301", "( 入試種別略称 )");
                return false;
            }
        }

        //１レコード取得
        $query = knjlz05hQuery::getRow($this, "chk");
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
        $checkStrLen["KINDDIV"]       = array("LEN" => 2,  "MESAGE" => "入試種別ＣＤ");
        $checkStrLen["KINDDIV_NAME"]  = array("LEN" => 30, "MESAGE" => "入試種別名称");
        $checkStrLen["KINDDIV_ABBV"]  = array("LEN" => 9,  "MESAGE" => "入試種別略称");
        foreach ($checkStrLen as $key => $val) {
            if (strlen($this->field[$key]) > $val["LEN"]) {
                $this->setWarning("MSG915", $val["MESAGE"] . " " . $val["LEN"] . "バイトまで");
                return false;
            }
        }

        //数値チェック
        $checkStrLen = array();
        $checkStrLen["KINDDIV"] = array("MESAGE" => "入試種別ＣＤ");
        foreach ($checkStrLen as $key => $val) {
            if ($this->field[$key] != "" && !preg_match('/^[0-9]+$/', $this->field[$key])) {
                $this->setWarning("MSG907", "( " . $val["MESAGE"] . " )");
                return false;
            }
        }

        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->kinddiv = $this->field["KINDDIV"];
            $this->setMessage("MSG201");
        }
    }
    //アップデート時の内部メソッド
    function &update()
    {
        $result = knjlz05hQuery::getUpdateQuery($this, $this->field);
        return $result;
    }

    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->insert();
            $this->kinddiv = $this->field["KINDDIV"];
            $this->setMessage("MSG201");
        }
        return;
    }
    //インサート
    function &insert()
    {
        $result = knjlz05hQuery::getInsertQuery($this, $this->field);
        return $result;
    }

    //DELETE
    function &getDeleteModel()
    {
        if ($this->validateInput()) {
            knjlz05hQuery::getDeleteQuery($this, $this->field);
            $this->kinddiv = "";
            $this->setMessage("MSG201");
            $this->field = array();
        }
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
