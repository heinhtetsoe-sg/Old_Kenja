<?php
class knjl730hModel extends Model
{
    public $cmd;
    public $field = array();
    public $ObjYear;
    public $selectLeft;
    public $selectLeftText;
    public $selectRight;
    public $selectRightText;
    public $applicantdiv;
    public $testdiv;

    public function knjl730hModel()
    {
    }

    public function init()
    {
        $this->ObjYear = CTRL_YEAR + 1;

        $this->cmd = VARS::request("cmd");

        $this->field = array("APPLICANTDIV"     => VARS::post("APPLICANTDIV"),
                             "TESTDIV"          => VARS::post("TESTDIV"),
                             "EXAM_TYPE"        => VARS::post("EXAM_TYPE"),
                             "EXAMHALLCD"       => VARS::post("EXAMHALLCD"),
                             "DEL_HALL"         => VARS::post("DEL_HALL"),
                             "EXAMHALL_NAME"    => VARS::post("EXAMHALL_NAME")
                            );

        $this->selectLeft      = VARS::post("selectLeft");
        $this->selectLeftText  = VARS::post("selectLeftText");
        $this->selectRight     = VARS::post("selectRight");
        $this->selectRightText = VARS::post("selectRightText");

        if ($this->cmd == "halladd") {
            $this->applicantdiv = VARS::request("APPLICANTDIV");
            $this->testdiv = VARS::request("TESTDIV");
        }
    }

    //入力項目チェック
    public function validateInput($db)
    {
        if ($this->cmd == "hallupdate") {
            if (strlen($this->field["EXAMHALL_NAME"]) == 0) {
                $this->setWarning("MSG301", "( 会場名 )");
                return false;
            }
            if (strlen($this->field["EXAMHALL_NAME"]) > 30) {
                $this->setWarning("MSG914", "( 会場名 )");
                return false;
            }
        } else {
            //データ指定なし
            if (!$this->selectLeft && !$this->selectRight) {
                $this->setWarning("MSG304");
                return false;
            }
        }
        return ture;
    }

    //更新処理
    public function &getUpdateModel()
    {
        //DB接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        //入力チェック
        if ($this->validateInput($db)) {
            knjl730hQuery::getDeleteQuery($this, $db);
            if ($this->selectLeft) {
                //一覧の更新
                knjl730hQuery::getUpdateQuery($this, $db);
            } elseif ($this->field["DEL_HALL"]) {
                //会場コードの削除
                knjl730hQuery::getDeleteEntexamHallYdat($this, $db);
            }

            $this->setMessage("MSG201");
        }
        $db->commit();
        //DB切断
        Query::dbCheckIn($db);
        return;
    }

    //会場追加処理
    public function &getHallUpdateModel()
    {
        //入力チェック
        if ($this->validateInput($db)) {
            knjl730hQuery::getUpdateEntexamHallYdat($this);
            $this->setMessage("MSG201");
        }
        return;
    }
}
