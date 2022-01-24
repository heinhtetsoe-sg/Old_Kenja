<?php

require_once('for_php7.php');

//定数定義
class knjh704Model extends Model
{
    public $field = array();
    public $cmd;
    public $testId;
    public $periodid;
    public $startHour;
    public $startMinute;
    public $endHour;
    public $endMinute;
    public $subclasscd;

    //メンバ変数の初期化
    public function knjh704Model()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->testId);
            unset($this->periodid);
            unset($this->startHour);
            unset($this->startMinute);
            unset($this->endHour);
            unset($this->endMinute);
            unset($this->subclasscd);
        }

        $this->field = array("TESTID"       => VARS::post("TESTID"),        //学力テスト(登録)
                             "PERIODID"     => VARS::post("PERIODID"),      //時限
                             "START_HOUR"   => VARS::post("START_HOUR"),    //開始時間
                             "START_MINUTE" => VARS::post("START_MINUTE"),  //開始分
                             "END_HOUR"     => VARS::post("END_HOUR"),      //終了時間
                             "END_MINUTE"   => VARS::post("END_MINUTE"),    //終了分
                             "SUBCLASSCD"   => VARS::post("SUBCLASSCD")     //科目
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
        //開始時間
        if (VARS::get("START_HOUR")) {
            $this->startHour = VARS::get("START_HOUR");
        }
        //開始分
        if (VARS::get("START_MINUTE")) {
            $this->startMinute = VARS::get("START_MINUTE");
        }
        //終了時間
        if (VARS::get("END_HOUR")) {
            $this->endHour = VARS::get("END_HOUR");
        }
        //終了分
        if (VARS::get("END_MINUTE")) {
            $this->endMinute = VARS::get("END_MINUTE");
        }
        //科目CD
        if (VARS::get("SUBCLASSCD")) {
            $this->subclasscd = VARS::get("SUBCLASSCD");
        }

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    public function validateInput($kind)
    {
        //追加・更新のとき
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
            if ($this->field["START_HOUR"] == "") {
                $this->setWarning("MSG301", "( 開始時間 )");
                return false;
            }
            if ($this->field["START_MINUTE"] == "") {
                $this->setWarning("MSG301", "( 開始分 )");
                return false;
            }
            if ($this->field["END_HOUR"] == "") {
                $this->setWarning("MSG301", "( 終了時間 )");
                return false;
            }
            if ($this->field["END_MINUTE"] == "") {
                $this->setWarning("MSG301", "( 終了分 )");
                return false;
            }
            if ($this->field["SUBCLASSCD"] == "") {
                $this->setWarning("MSG301", "( 科目名 )");
                return false;
            }

            $db = Query::dbCheckOut();
            //更新で、学力テスト・科目名を変更したとき
            if ($kind == "update" && ($this->testId != $this->field["TESTID"] || $this->subclasscd != $this->field["SUBCLASSCD"])) {
                //学力テスト・科目での重複チェック
                $rowCnt = $db->getOne(knjh704Query::duplicateCheck($this));
                if ($rowCnt != "0") {
                    $this->setWarning("MSG302");
                    return false;
                }
            }
            //追加のとき
            if ($kind == "insert") {
                //学力テスト・科目での重複チェック
                $rowCnt = $db->getOne(knjh704Query::duplicateCheck($this));
                if ($rowCnt != "0") {
                    $this->setWarning("MSG302");
                    return false;
                }
            }
            //時間の大小チェック
            $startH = $this->field["START_HOUR"];
            $startM = $this->field["START_MINUTE"];
            $endH   = $this->field["END_HOUR"];
            $endM   = $this->field["END_MINUTE"];

            $sTime = new DateTime($startH. ":" .$startM);
            $eTime = new DateTime($endH. ":" .$endM);

            if ($sTime >= $eTime) {
                $this->setWarning("日付の大小が不正です。");
                return false;
            }
            Query::dbCheckIn($db);
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
        knjh704Query::getInsertQuery($this);
        if (!isset($this->warning)) {
            $this->setMessage("MSG201");
            $this->testId      = $this->field["TESTID"];
            $this->periodid    = $this->field["PERIODID"];
            $this->startHour   = $this->field["START_HOUR"];
            $this->startMinute = $this->field["START_MINUTE"];
            $this->endHour     = $this->field["END_HOUR"];
            $this->endMinute   = $this->field["END_MINUTE"];
            $this->subclasscd  = $this->field["SUBCLASSCD"];
        }
    }

    //更新用のモデルを返します
    public function getUpdateModel()
    {
        if (!isset($this->testId)     ||
            !isset($this->periodid)   ||
            !isset($this->startHour)  ||
            !isset($this->startMinute)||
            !isset($this->endHour)    ||
            !isset($this->endMinute)  ||
            !isset($this->subclasscd)) {
            $this->setWarning("MSG308");
            return;
        }

        //入力チェック
        if ($this->validateInput("update")) {
            $this->update();
        }
    }

    //更新
    public function update()
    {
        knjh704Query::getUpdateQuery($this);
        if (!isset($this->warning)) {
            $this->setMessage("MSG201");
            $this->testId      = $this->field["TESTID"];
            $this->periodid    = $this->field["PERIODID"];
            $this->startHour   = $this->field["START_HOUR"];
            $this->startMinute = $this->field["START_MINUTE"];
            $this->endHour     = $this->field["END_HOUR"];
            $this->endMinute   = $this->field["END_MINUTE"];
            $this->subclasscd  = $this->field["SUBCLASSCD"];
        }
    }

    //削除用のモデルを返します
    public function getDeleteModel()
    {
        if (!isset($this->testId)     ||
            !isset($this->periodid)   ||
            !isset($this->startHour)  ||
            !isset($this->startMinute)||
            !isset($this->endHour)    ||
            !isset($this->endMinute)  ||
            !isset($this->subclasscd)) {
            $this->setWarning("MSG308");
            return;
        }

        //入力チェック
        if ($this->validateInput("delete")) {
            $this->delete();
        }
    }

    //削除
    public function delete()
    {
        knjh704Query::getDeleteQuery($this);
        if (!isset($this->warning)) {
            $this->setMessage("データを削除しました。");
        }
        //変数の初期化
        $this->field = array();
        unset($this->testId);
        unset($this->periodid);
        unset($this->startHour);
        unset($this->startMinute);
        unset($this->endHour);
        unset($this->endMinute);
        unset($this->subclasscd);
    }
}
