<?php

require_once('for_php7.php');

//定数定義
class knjh702Model extends Model
{
    public $field = array();
    public $cmd;
    public $testdiv;
    public $bunridiv;
    public $classcd;
    public $subclasscd;

    //メンバ変数の初期化
    public function knjh702Model()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->testdiv);
            unset($this->bunridiv);
            unset($this->classcd);
            unset($this->subclasscd);
        }

        $this->field = array("TESTDIV"      => VARS::post("TESTDIV"),       //テスト区分
                             "BUNRIDIV"     => VARS::post("BUNRIDIV"),      //文理区分
                             "CLASSCD"      => VARS::post("CLASSCD"),       //教科コード
                             "SUBCLASSCD"   => VARS::post("SUBCLASSCD"),    //科目コード
                             "SUBCLASSNAME" => VARS::post("SUBCLASSNAME"),  //科目名称
                             "SUBCLASSABBV" => VARS::post("SUBCLASSABBV"),  //科目略称
                             "ELECTDIV"     => VARS::post("ELECTDIV")       //必修／選択区分
                            );

        $this->cmd = VARS::request("cmd");

        if (VARS::get("TESTDIV")) {
            $this->testdiv = VARS::get("TESTDIV");
        }
        if (VARS::get("BUNRIDIV")) {
            $this->bunridiv = VARS::get("BUNRIDIV");
        }
        if (VARS::get("CLASSCD")) {
            $this->classcd = VARS::get("CLASSCD");
        }
        if (VARS::get("SUBCLASSCD")) {
            $this->subclasscd = VARS::get("SUBCLASSCD");
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
        //追加・更新時
        if ($kind == "insert" || $kind == "update") {
            //必須入力チェック
            if ($this->field["TESTDIV"] == "") {
                $this->setWarning("MSG301", "( テスト区分 )");
                return false;
            }
            if ($this->field["BUNRIDIV"] == "") {
                $this->setWarning("MSG301", "( 文理区分 )");
                return false;
            }
            if ($this->field["CLASSCD"] == "") {
                $this->setWarning("MSG301", "( 教科 )");
                return false;
            }
            if ($this->field["SUBCLASSCD"] == "") {
                $this->setWarning("MSG301", "( 科目コード )");
                return false;
            }
            if ($this->field["SUBCLASSNAME"] == "") {
                $this->setWarning("MSG301", "( 科目名称 )");
                return false;
            }
        }

        //追加時
        if ($kind == "insert") {
            $db = Query::dbCheckOut();
            //重複チェック
            $query  = knjh702Query::duplicateCheck(CTRL_YEAR, $this);
            $rowCnt = $db->getOne($query);
            if ($rowCnt != "0") {
                $this->setWarning("MSG302");
                return false;
            }
            Query::dbCheckIn($db);
            //文字数チェック
            if ($this->lengthCheck() == false) {
                return false;
            }
        }

        //更新時
        if ($kind == "update") {
            if ($this->field["TESTDIV"]    == $this->testdiv  &&
                $this->field["BUNRIDIV"]   == $this->bunridiv &&
                $this->field["CLASSCD"]    == $this->classcd  &&
                $this->field["SUBCLASSCD"] == $this->subclasscd) {
                //主キーを変更していないとき
                $db = Query::dbCheckOut();
                $result = $db->query(knjh702Query::getAcademicTestSubclassData(CTRL_YEAR, $this->field["TESTDIV"], $this->field["BUNRIDIV"], $this->field["CLASSCD"], $this->field["SUBCLASSCD"]));
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
                //重複チェック
                $query = knjh702Query::duplicateCheck(CTRL_YEAR, $this);
                $rowCnt = $db->getOne($query);
                Query::dbCheckIn($db);
                if ($rowCnt != "0") {
                    $this->setWarning("MSG302");
                    return false;
                }
            }
            //文字数チェック
            if ($this->lengthCheck() == false) {
                return false;
            }
        }
        return true;
    }

    //文字数チェック
    public function lengthCheck()
    {
        //科目コード
        if (strlen($this->field["SUBCLASSCD"]) > 6) {
            $this->setWarning("MSG915", "(科目コードは2文字までです。)");
            return false;
        }
        //科目名称
        if (strlen($this->field["SUBCLASSNAME"]) > 60) {
            $this->setWarning("MSG915", "(科目名称は20文字までです。)");
            return false;
        }
        //科目略称
        if (strlen($this->field["SUBCLASSABBV"]) > 15) {
            $this->setWarning("MSG915", "(科目略称は5文字までです。)");
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
        $db = Query::dbCheckOut();
        $query = knjh702Query::getInsertQuery($this);
        $db->query($query);
        Query::dbCheckIn($db);
        if (!isset($this->warning)) {
            $this->setMessage("MSG201");
            //変数の値を画面上の値と一致させる
            $this->testdiv    = $this->field["TESTDIV"];
            $this->bunridiv   = $this->field["BUNRIDIV"];
            $this->classcd    = $this->field["CLASSCD"];
            $this->subclasscd = $this->field["SUBCLASSCD"];
        }
    }

    //更新用のモデルを返します
    public function getUpdateModel()
    {
        //データを選択していないとき
        if (!isset($this->testdiv) || !isset($this->bunridiv) || !isset($this->classcd) || !isset($this->subclasscd)) {
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
        $db = Query::dbCheckOut();
        $query = knjh702Query::getUpdateQuery($this);
        $db->query($query);
        Query::dbCheckIn($db);
        if (!isset($this->warning)) {
            $this->setMessage("MSG201");
            //変数の値を画面上の値と一致させる
            $this->testdiv    = $this->field["TESTDIV"];
            $this->bunridiv   = $this->field["BUNRIDIV"];
            $this->classcd    = $this->field["CLASSCD"];
            $this->subclasscd = $this->field["SUBCLASSCD"];
        }
    }

    //削除用のモデルを返します
    public function getDeleteModel()
    {
        //データを選択していないとき
        if (!isset($this->testdiv) || !isset($this->bunridiv) || !isset($this->classcd) || !isset($this->subclasscd)) {
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
        $db = Query::dbCheckOut();
        $query = knjh702Query::getDeleteQuery($this);
        $db->query($query);
        Query::dbCheckIn($db);
        if (!isset($this->warning)) {
            $this->setMessage("データを削除しました。");
        }
        $this->field = array();

        //変数を初期化
        unset($this->testdiv);
        unset($this->bunridiv);
        unset($this->classcd);
        unset($this->subclasscd);
    }

    //前年度からのコピーのモデルを返します
    public function getCopyModel()
    {
        $db = Query::dbCheckOut();

        //今年度のデータ取得
        $result = $db->query(knjh702Query::getAcademicTestSubclassData(CTRL_YEAR));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this_year_check = $row;
        }
        //前年度のデータ取得
        $result = $db->query(knjh702Query::getAcademicTestSubclassData(CTRL_YEAR - 1));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $last_year_check = $row;
        }

        //データの存在チェック
        if ($this_year_check) {
            //今年度のデータが存在するとき
            $this->setWarning("既にデータが存在します。");
            return false;
        } elseif (!$last_year_check) {
            //前年度のデータが存在しないとき
            $this->setWarning("MSG303");
            return false;
        } else {
            //前年度からのコピー
            $result = $db->query(knjh702Query::getAcademicTestSubclassData(CTRL_YEAR - 1));//前年度のデータ取得
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $db->query(knjh702Query::copyInsert($row));//前年度のデータを今年度のデータとしてINSERT
            }
        }
        Query::dbCheckIn($db);
    }
}
