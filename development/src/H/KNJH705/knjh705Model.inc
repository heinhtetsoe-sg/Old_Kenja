<?php

require_once('for_php7.php');

//定数定義
class knjh705Model extends Model
{
    public $field = array();
    public $cmd;
    public $testId;
    public $periodid;
    public $faccd;
    public $staffcd1;

    //メンバ変数の初期化
    public function knjh705Model()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->testId);
            unset($this->periodid);
            unset($this->faccd);
            unset($this->staffcd1);
        }

        $this->field = array("TESTID"     => VARS::post("TESTID"),        //学力テスト(登録)
                             "PERIODID"   => VARS::post("PERIODID"),      //時限
                             "FACCD"      => VARS::post("FACILITYNAME"),  //教室
                             "STAFFCD1"   => VARS::post("STAFFCD1"),      //監督1
                             "STAFFCD2"   => VARS::post("STAFFCD2"),      //監督2
                             "STAFFCD3"   => VARS::post("STAFFCD3"),      //監督3
                             "STAFFNAME1" => VARS::post("STAFFNAME1"),    //監督名1
                             "STAFFNAME2" => VARS::post("STAFFNAME2"),    //監督名2
                             "STAFFNAME3" => VARS::post("STAFFNAME3")     //監督名3
                            );
        $this->cmd = VARS::request("cmd");

        //学力テスト
        if (VARS::get("TESTID")) {
            $this->testId = VARS::get("TESTID");
            $this->field["TESTID"] = $this->testId;
        }
        //時限
        if (VARS::get("PERIODID")) {
            $this->periodid = VARS::get("PERIODID");
        }
        //教室
        if (VARS::get("FACCD")) {
            $this->faccd = VARS::get("FACCD");
        }
        //監督1
        if (VARS::get("STAFFCD1")) {
            $this->staffcd1 = VARS::get("STAFFCD1");
        }

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    public function validateInput($kind)
    {
        //追加・更新時
        if ($kind == "insert" || $kind == "update") {
            //必須入力チェック
            if ($this->field["TESTID"] == "") {
                $this->setWarning("MSG301", "( 学力テスト )");
                return false;
            }
            if ($this->field["PERIODID"] == "") {
                $this->setWarning("MSG301", "( 時限 )");
                return false;
            }
            if ($this->field["FACCD"] == "") {
                $this->setWarning("MSG301", "( 教室 )");
                return false;
            }
            if ($this->field["STAFFCD1"] == "") {
                $this->setWarning("MSG301", "( 監督1 )");
                return false;
            }

            $db = Query::dbCheckOut();
            //更新で学力テスト・時限・教室を変更したとき
            if ($kind == "update" &&
                ($this->testId   != $this->field["TESTID"]   ||
                 $this->periodid != $this->field["PERIODID"] ||
                 $this->faccd    != $this->field["FACCD"])) {
                 $rowCnt = $db->getOne(knjh705Query::duplicateCheck($this));
                if ($rowCnt != "0") {
                    $this->setWarning("MSG302");
                    return false;
                }
            }
            //追加時
            if ($kind == "insert") {
                //登録データの重複チェック
                $rowCnt = $db->getOne(knjh705Query::duplicateCheck($this));
                if ($rowCnt != "0") {
                    $this->setWarning("MSG302");
                    return false;
                }
            }
            Query::dbCheckIn($db);

            //監督の重複チェック
            if (($this->field["STAFFCD1"] != "") &&
                ($this->field["STAFFCD1"] == $this->field["STAFFCD2"] ||
                 $this->field["STAFFCD1"] == $this->field["STAFFCD3"])) {
                $this->setWarning("MSG302");
                return false;
            }
            if (($this->field["STAFFCD2"] != "") &&
                ($this->field["STAFFCD2"] == $this->field["STAFFCD1"] ||
                 $this->field["STAFFCD2"] == $this->field["STAFFCD3"])) {
                $this->setWarning("MSG302");
                return false;
            }
            if (($this->field["STAFFCD3"] != "") &&
                ($this->field["STAFFCD3"] == $this->field["STAFFCD1"] ||
                 $this->field["STAFFCD3"] == $this->field["STAFFCD2"])) {
                $this->setWarning("MSG302");
                return false;
            }
        }
        return true;
    }

    //追加用のモデルを返します
    public function getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")) {
            $this->insert();
        }
    }

    //追加
    public function insert()
    {
        $seq = knjh705Query::getInsertQuery($this);
        if (!isset($this->warning)) {
            $this->setMessage("MSG201");
            $this->testId   = $this->field["TESTID"];
            $this->periodid = $this->field["PERIODID"];
            $this->faccd    = $this->field["FACCD"];
            $this->staffcd1 = $this->field["STAFFCD1"];
        }
    }

    //更新用のモデルを返します
    public function getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")) {
            $this->update();
        }
    }

    //更新
    public function update()
    {
        if (!isset($this->testId)   ||
            !isset($this->periodid) ||
            !isset($this->faccd)    ||
            !isset($this->staffcd1)) {
            $this->setWarning("MSG308");
            return;
        }

        knjh705Query::getUpdateQuery($this);
        if (!isset($this->warning)) {
            $this->setMessage("MSG201");
            $this->testId   = $this->field["TESTID"];
            $this->periodid = $this->field["PERIODID"];
            $this->faccd    = $this->field["FACCD"];
            $this->staffcd1 = $this->field["STAFFCD1"];
        }
    }

    //削除用のモデルを返します
    public function getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")) {
            $this->delete();
        }
    }

    //削除
    public function delete()
    {
        if (!isset($this->testId)   ||
            !isset($this->periodid) ||
            !isset($this->faccd)    ||
            !isset($this->staffcd1)) {
            $this->setWarning("MSG308");
            return;
        }

        knjh705Query::getDeleteQuery($this);
        if (!isset($this->warning)) {
            $this->setMessage("データを削除しました。");
        }
        //変数の初期化
        $this->field = array();
        unset($this->testId);
        unset($this->periodid);
        unset($this->faccd);
        unset($this->staffcd1);
    }
}
