<?php

require_once('for_php7.php');

//定数定義
class knjh701Model extends Model
{
    public $field = array();
    public $cmd;
    public $year;    //現在処理年度
    public $testdiv; //テスト区分
    public $testid;  //テストID
    public $date;    //実施日付

    //メンバ変数の初期化
    public function knjh701Model()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->testdiv);
            unset($this->testid);
            unset($this->date);
        }

        $this->field = array("TESTDIV"      => VARS::post("TESTDIV"),     //テスト区分
                             "TESTID"       => VARS::post("TESTID"),      //テストID
                             "EXAM_DATE"    => VARS::post("EXAM_DATE"),   //実施日付
                             "TESTNAME"     => VARS::post("TESTNAME"),    //テスト名称
                             "TESTNAMEABBV" => VARS::post("TESTNAMEABBV") //テスト略称
                            );

        $this->year = CTRL_YEAR;

        $this->cmd = VARS::request("cmd");

        //リンクをクリックしたとき
        if (VARS::get("TESTID")) {
            $this->testdiv = substr(VARS::get("TESTID"), 0, 1);
            $this->testid  = substr(VARS::get("TESTID"), 1, 2);
        }
        if (VARS::get("EXAM_DATE")) {
            $this->date = str_replace("/", "-", VARS::get("EXAM_DATE"));
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    public function validateInput($kind)
    {
        if ($kind == "insert" || $kind == "update") {
            //必須チェック
            if ($this->field["TESTDIV"] == "") {
                $this->setWarning("MSG301", "( テスト区分 )");
                return false;
            }
            if ($this->field["TESTID"] == "") {
                $this->setWarning("MSG301", "( テストID )");
                return false;
            }
            if ($this->field["EXAM_DATE"] == "") {
                $this->setWarning("MSG301", "( 実施日付 )");
                return false;
            }
        }

        //更新時
        if ($kind == "update") {
            if ($this->field["TESTDIV"] == $this->testdiv &&
                $this->field["TESTID"]  == $this->testid  &&
                str_replace("/", "-", $this->field["EXAM_DATE"]) == $this->date) {
                //主キーを変更していないとき
                $db = Query::dbCheckOut();
                $result = $db->query(knjh701Query::getAcademicTestMst(CTRL_YEAR, $this->field["TESTDIV"], sprintf("%02d", $this->field["TESTID"]), $this->field["EXAM_DATE"]));
                while ($rowTemp = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $row = $rowTemp;
                }
                Query::dbCheckIn($db);

                if (isset($row) == false) {
                    //存在しないとき
                    $this->setWarning("MSG308");
                    return false;
                }
            } else {
                //主キーを変更したとき
                $db = Query::dbCheckOut();
                //年度・テスト区分・テストIDでの重複チェック
                $rowCnt = $db->getOne(knjh701Query::duplicateCheck(CTRL_YEAR, "1", $this));
                if ($rowCnt != "0" && ($this->field["TESTDIV"] != $this->testdiv || $this->field["TESTID"] != $this->testid)) {
                    $this->setWarning("MSG302");
                    return false;
                }
                //年度・実施日付での重複チェック
                $rowCnt = $db->getOne(knjh701Query::duplicateCheck(CTRL_YEAR, "2", $this));
                if ($rowCnt != "0" && str_replace("/", "-", $this->field["EXAM_DATE"]) != $this->date) {
                    $this->setWarning("MSG302");
                    return false;
                }
                Query::dbCheckIn($db);
            }
            //文字数チェック
            if ($this->lengthCheck() == false) {
                return false;
            }
            return true;
        }

        //追加時
        if ($kind == "insert") {
            //文字数チェック
            if ($this->lengthCheck() == false) {
                return false;
            }

            $db = Query::dbCheckOut();
            //年度・テスト区分・テストIDでの重複チェック
            $rowCnt = $db->getOne(knjh701Query::duplicateCheck(CTRL_YEAR, "1", $this));
            if ($rowCnt != "0") {
                $this->setWarning("MSG302");
                return false;
            }
            //年度・実施日付での重複チェック
            $rowCnt = $db->getOne(knjh701Query::duplicateCheck(CTRL_YEAR, "2", $this));
            if ($rowCnt != "0") {
                $this->setWarning("MSG302");
                return false;
            }
            Query::dbCheckIn($db);
        }
        return true;
    }

    //文字数チェック
    public function lengthCheck()
    {
        //テストID
        if (strlen($this->field["TESTID"]) > 2) {
            $this->setWarning("MSG915", "(テストIDは2文字までです。)");
            return false;
        }
        //テスト名称
        if (strlen($this->field["TESTNAME"]) > 60) {
            $this->setWarning("MSG915", "(テスト名称は20文字までです。)");
            return false;
        }
        //テスト略称
        if (strlen($this->field["TESTNAMEABBV"]) > 30) {
            $this->setWarning("MSG915", "(テスト略称は10文字までです。)");
            return false;
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
        knjh701Query::getInsertQuery($this->field, $this);
        if (!isset($this->warning)) {
            $this->setMessage("MSG201");
            $this->testdiv = $this->field["TESTDIV"];
            $this->testid  = $this->field["TESTID"];
            $this->date    = str_replace("/", "-", $this->field["EXAM_DATE"]);
        }
    }

    //更新用のモデルを返します
    public function getUpdateModel()
    {
        //データを選択していないとき
        if (!isset($this->testdiv) || !isset($this->testid) || !isset($this->date)) {
            $this->setWarning("MSG308");
            return false;
        }

        //入力チェック
        if ($this->validateInput("update")) {
            $this->update();
        }
    }

    //更新
    public function update()
    {
        knjh701Query::getUpdateQuery($this);
        if (!isset($this->warning)) {
            $this->setMessage("MSG201");
            $this->setMessage("MSG201");
            $this->testdiv = $this->field["TESTDIV"];
            $this->testid  = $this->field["TESTID"];
            $this->date    = str_replace("/", "-", $this->field["EXAM_DATE"]);
        }
    }

    //削除用のモデルを返します
    public function getDeleteModel()
    {
        //データを選択していないとき
        if (!isset($this->testdiv) || !isset($this->testid) || !isset($this->date)) {
            $this->setWarning("MSG308");
            return false;
        }
        //入力チェック
        if ($this->validateInput("delete")) {
            $this->delete();
        }
    }

    //削除
    public function delete()
    {
        knjh701Query::getDeleteQuery($this);
        if (!isset($this->warning)) {
            $this->setMessage("データを削除しました。");
        }
        $this->field = array();

        //変数を初期化
        unset($this->testdiv);
        unset($this->testid);
        unset($this->date);

    }

    //前年度からのコピーのモデルを返します
    public function getCopyModel()
    {
        $db = Query::dbCheckOut();

        //今年度のデータ取得
        $result = $db->query(knjh701Query::getAcademicTestMst(CTRL_YEAR));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this_year_check = $row;
        }
        //前年度のデータ取得
        $result = $db->query(knjh701Query::getAcademicTestMst(CTRL_YEAR - 1));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $last_year_check = $row;
        }

        //データの存在チェック
        if ($this_year_check) {
            //今年度のデータが存在するとき
            $this->setWarning("既にデータが存在します。");
            return false;
        } elseif (!$last_year_check) {
            //前年度のデータが存在するとき
            $this->setWarning("MSG303");
            return false;
        } else {
            //前年度からのコピー
            $result = $db->query(knjh701Query::getAcademicTestMst(CTRL_YEAR - 1));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $db->query(knjh701Query::copyInsert($row));//前年度のデータを今年度のデータとしてINSERT
            }
        }
        Query::dbCheckIn($db);
    }
}
