<?php

require_once('for_php7.php');

//定数定義
class knjl003vModel extends Model
{
    public $field = array();
    public $cmd;
    public $examyear;
    public $leftYear = "";
    public $placeId = "";

    public function init()
    {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->leftYear);
            unset($this->placeId);
        }

        $this->cmd = VARS::request("cmd");

        $this->examyear  = CTRL_YEAR + 1;

        //左画面 年度コンボ
        if (VARS::request("YEAR") != "") {
            $this->leftYear = VARS::request("YEAR");
        }
        $this->leftYear = ($this->leftYear == "") ? $this->examyear : $this->leftYear;

        $this->field = array("PLACE_ID"           =>  VARS::post("PLACE_ID"),           //会場ID
                             "PLACE_NAME"         =>  VARS::post("PLACE_NAME"),         //会場名
                             "SEATS"              =>  VARS::post("SEATS")               //収容人数
                             );

        if (VARS::get("PLACE_ID") != "") {
            $this->placeId = VARS::get("PLACE_ID");
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        //空文字チェック
        if (strlen($this->field["PLACE_ID"]) == 0) {
            $this->setWarning("MSG301", "( 会場ID )");
            return false;
        }

        //キー変更チェック
        if ($this->cmd == "update" || $this->cmd == "delete") {
            if ($this->placeId != $this->field["PLACE_ID"]) {
                $this->setWarning("MSG308");
                return false;
            }
        }

        //必須チェック
        if ($this->cmd == "add" || $this->cmd == "update" || $this->cmd == "delete") {
            if (strlen($this->field["PLACE_ID"]) == 0) {
                $this->setWarning("MSG301", "( 会場ID )");
                return false;
            }
        }
        if ($this->cmd == "add" || $this->cmd == "update") {
        }

        //コードのゼロ埋め
        $this->field["PLACE_ID"] = sprintf("%04d", $this->field["PLACE_ID"]);

        //１レコード取得
        $query = knjl003vQuery::getRow($this, "chk");
        $db = Query::dbCheckOut(); //DB接続
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);     //DB切断

        //存在チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }

        //重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning("MSG918");
            return false;
        }

        //文字数チェック
        $checkStrLen["PLACE_ID"]     = array("LEN" => 4,   "MESAGE" => "会場ID");
        $checkStrLen["PLACE_NAME"]   = array("LEN" => 20,  "MESAGE" => "会場名");
        $checkStrLen["SEATS"]        = array("LEN" => 2,   "MESAGE" => "収容人数");
        foreach ($checkStrLen as $key => $val) {
            if (mb_strlen($this->field[$key]) > $val["LEN"]) {
                $this->setWarning("MSG915", $val["MESAGE"]." ".$val["LEN"]."文字まで");
                return false;
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
            $this->placeId = $this->field["PLACE_ID"];
            $this->setMessage("MSG201");
        }
        return;
    }
    //アップデート時の内部メソッド
    public function &update()
    {
        $result = knjl003vQuery::getUpdateQuery($this, $this->field);
        return $result;
    }

    //追加用のモデルを返します
    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->insert();
            $this->placeId = $this->field["PLACE_ID"];
            $this->setMessage("MSG201");
        }
        return;
    }
    //インサート
    public function &insert()
    {
        $result = knjl003vQuery::getInsertQuery($this, $this->field);
        return $result;
    }

    //削除用のモデルを返します
    public function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")) {
            knjl003vQuery::getDeleteQuery($this, $this->field);
            $this->setMessage("MSG201");
            unset($this->field);
        }
        return;
    }

    //コピーのモデルを返します
    public function &getCopyYearModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $message = '';
        $flg = true;

        $query = knjl003vQuery::getCheckHallMstSql($this->leftYear);
        $result = $db->getOne($query);
        if ($result > 0) {
            $message = '今年度のデータが存在します。';
            $flg = false;
        }

        $prevYear = $this->leftYear - 1;
        $query = knjl003vQuery::getCheckHallMstSql($prevYear);
        $result = $db->getOne($query);
        if ($result <= 0) {
            $message = '前年データが存在しません。';
            $flg = false;
        }

        if ($flg) {
            $query = knjl003vQuery::getCopyQuery($this, $prevYear);
            $db->query($query);
            $this->setMessage("MSG201");
        } else {
            $this->setMessage("MSG203", "\\n" . $message);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    //変数をクリアします
    public function clean()
    {
    }
}
