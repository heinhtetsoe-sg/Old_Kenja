<?php

require_once('for_php7.php');

class knjl503iModel extends Model
{
    public $field      = array();
    public $cmd;
    public $applicantdiv = "2";     //入試制度（固定値：2）
    public $testdiv = "0";          //入試区分（固定値：0）
    public $leftYear = "";          //primary
    public $testSubCd;              //試験科目
    public $nextYear;               //次の試験年度

    public function knjl503iModel()
    {
    }

    public function init()
    {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->leftYear);
            unset($this->testSubCd);
        }

        //プロパティファイル
        $this->getPropertiesAll();

        $this->cmd = VARS::request("cmd");

        $this->field = array("TESTSUBCLASSCD"   => VARS::post("TESTSUBCLASSCD"),
                             "PERFECT"          => VARS::post("PERFECT"),
                             "ENTEXAMYEAR"      => VARS::post("ENTEXAMYEAR")
                            );

        //リンク
        if (VARS::get("TESTSUBCLASSCD") != "") {
            $this->testSubCd = VARS::get("TESTSUBCLASSCD");
        }

        //左画面 抽出条件
        if (VARS::post("ENTEXAMYEAR")) {
            $this->leftYear = $this->field["ENTEXAMYEAR"];
        } elseif (is_null($this->leftYear)) {
            $this->leftYear  = CTRL_YEAR + 1;
        }
    }

    //モデルの内容をDBテーブルへ反映
    public function getCopyModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $this->nextYear = $this->leftYear + 1;
        $message = '';
        $flg = true;
        if (false == $this->isExistedYearInPerfectMst($db, $this->leftYear)) {
            $message = "作成するデータが存在しません。";
            $flg = false;
        }
        if (true == $this->isExistedYearInPerfectMst($db, $this->nextYear)) {
            $message = $message . '既に次年度のデータが作成されています。';
            $flg = false;
        }
        if ($flg) {
            $query = knjl503iQuery::getCopyPerfectMstQuery($db, $this);
            $db->query($query);
            $this->leftYear = $this->nextYear;
            $this->setMessage("MSG201");
        } else {
            $this->setMessage("MSG203", "\\n" . $message);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    //指定された年度（$year）に対して満点マスタに登録済みであるか否かを判定（該当するレコードが1件以上あればTRUEを返す）
    public function isExistedYearInPerfectMst($db, $year)
    {
        $query = knjl503iQuery::getCheckPerfectMstSql($this, $year);

        $result = $db->getOne($query);
        return ($result > 0);
    }

    //入力項目チェック
    public function validateInput()
    {
        //キー変更チェック
        if ($this->cmd == "update" || $this->cmd == "delete") {
            $flg = false;
            if ($this->testSubCd != $this->field["TESTSUBCLASSCD"]) {
                $this->setWarning("MSG308");
                return false;
            }
        }

        //必須チェック
        if ($this->cmd == "add" || $this->cmd == "update" || $this->cmd == "delete") {
            if ($this->field["TESTSUBCLASSCD"] == "") {
                $this->setWarning("MSG301", "( 試験科目 )");
                return false;
            }
        }
        if ($this->cmd == "add" || $this->cmd == "update") {
            if ($this->field["PERFECT"] == "") {
                $this->setWarning("MSG301", "( 満点 )");
                return false;
            }
        }

        //１レコード取得
        $query = knjl503iQuery::getRowQuery($this, "chk");
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
    public function getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->testSubCd      = $this->field["TESTSUBCLASSCD"];
            $this->setMessage("MSG201");
        }
    }
    //アップデート時の内部メソッド
    public function update()
    {
        $result = knjl503iQuery::updateExtmPrfctExtypMst($this, $this->field);
        return $result;
    }

    public function getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            knjl503iQuery::insertExtmPrfctExtypMst($this, $this->field);

            $this->testSubCd      = $this->field["TESTSUBCLASSCD"];
            $this->setMessage("MSG201");
        }
    }

    //DELETE
    public function getDeleteModel()
    {
        if ($this->validateInput()) {
            knjl503iQuery::deleteExtmPrfctExtypMst($this, $this->field);
            $this->testSubCd      = "";
            $this->setMessage("MSG201");
            $this->field = array();
        }
    }
}
?>
