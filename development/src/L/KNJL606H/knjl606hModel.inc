<?php
class knjl606hModel extends Model
{
    public $field        = array();
    public $cmd;
    public $leftYear     = "";        //primary
    public $applicantdiv = "";        //primary
    public $classifyCd   = "";        //primary

    public function knjl606hModel()
    {
    }

    public function init()
    {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->leftYear);
            unset($this->applicantdiv);
            unset($this->classifyCd);
        }

        //プロパティファイル
        $this->getPropertiesAll();

        $this->cmd = VARS::request("cmd");

        $this->examyear  = CTRL_YEAR + 1;

        $this->field = array("CLASSIFY_CD"     => VARS::post("CLASSIFY_CD"),
                             "CLASSIFY_NAME"   => VARS::post("CLASSIFY_NAME"),
                             "MARK"            => VARS::post("MARK"),
                             "PLUS_POINT"      => VARS::post("PLUS_POINT"),
                             "COMMITMENT_FLG"  => VARS::post("COMMITMENT_FLG"),
                             "CALC_FLG"        => VARS::post("CALC_FLG"),
                             "ORDER"           => VARS::post("ORDER")
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

        if (VARS::get("CLASSIFY_CD") != "") {
            $this->classifyCd = VARS::get("CLASSIFY_CD");
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
            $query = knjl606hQuery::getCopyClassIfyMstQuery($db, $this);
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
        $query = knjl606hQuery::getCheckClassIfyMstSql($year);
        
        $result = $db->getOne($query);
        if ($result > 0) {
            return true;
        }
        return false;
    }
    
    public function checkNextYear($db, $nextYear)
    {
        $query = knjl606hQuery::getCheckClassIfyMstSql($nextYear);

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
            if ($this->classifyCd != $this->field["CLASSIFY_CD"]) {
                $this->setWarning("MSG308");
                return false;
            }
        }

        //必須チェック
        if ($this->cmd == "add" || $this->cmd == "update" || $this->cmd == "delete") {
            if ($this->field["CLASSIFY_CD"] == "") {
                $this->setWarning("MSG301", "( 類別コード )");
                return false;
            }
        }
        if ($this->cmd == "add" || $this->cmd == "update") {
            if ($this->field["CLASSIFY_NAME"] == "") {
                $this->setWarning("MSG301", "( 類別  )");
                return false;
            }
        }

        //１レコード取得
        $query = knjl606hQuery::getRow($this, "chk");
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
        $checkStrLen["CLASSIFY_CD"]    = array("LEN" =>  2, "MESAGE" => "類別コード");
        $checkStrLen["CLASSIFY_NAME"]  = array("LEN" => 30, "MESAGE" => "類別");
        $checkStrLen["MARK"]           = array("LEN" =>  2, "MESAGE" => "記号");
        $checkStrLen["PLUS_POINT"]     = array("LEN" =>  3, "MESAGE" => "加点");
        foreach ($checkStrLen as $key => $val) {
            if (strlen($this->field[$key]) > $val["LEN"]) {
                $this->setWarning("MSG915", $val["MESAGE"]." ".$val["LEN"]."バイトまで");
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
            $this->classifyCd = $this->field["CLASSIFY_CD"];
            $this->setMessage("MSG201");
        }
    }

    //アップデート時の内部メソッド
    public function &update()
    {
        $result = knjl606hQuery::getUpdateQuery($this, $this->field);
        return $result;
    }

    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->insert();
            $this->classifyCd = $this->field["CLASSIFY_CD"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //インサート
    public function &insert()
    {
        $result = knjl606hQuery::getInsertQuery($this, $this->field);
        return $result;
    }

    //DELETE
    public function &getDeleteModel()
    {
        if ($this->validateInput()) {
            knjl606hQuery::getDeleteQuery($this, $this->field);
            $this->classifyCd = "";
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
