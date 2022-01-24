<?php

require_once('for_php7.php');

//定数定義

class knjz070Model extends Model
{
    public $cmd;
    public $year;  //年度
    public $selectdata;
    public $temp_year;

    //メンバ変数の初期化
    public function knjz070Model()
    {
    }
    public function init()
    {
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $temp = VARS::post("year");
        if (isset($temp)) {
            $this->year          = VARS::post("year");
        } else {
            $this->year          = CTRL_YEAR;           //現在処理年度を初期値にする
        }
        $this->selectdata        = VARS::post("selectdata");
        $this->rightMoveData     = VARS::post("rightMoveData");
        $this->cmd               = VARS::request("cmd");
    }

    //入力項目チェック
    public function validateInput()
    {
        if (!is_numeric($this->year)) {
            $this->warning = "対象年度を選択してください。";
            return false;
        }

        if ($this->cmd == "update" && strlen($this->rightMoveData) > 0) {
            $subclasscdArray = explode(",", $this->rightMoveData);
            $chkRsult = $this->checkUpdateSubclass($subclasscdArray);// $chkRsult => 0:削除対象の科目が他のテーブルで参照されているかの真偽値, 1:エラーメッセージ
            if ($chkRsult[0]) {
                $this->setWarning("MSG917", $chkRsult[1]);
                return false;
            }
        }

        return true;
    }

    //科目が別テーブルで参照されているかのチェック
    public function checkUpdateSubclass($subclasscdArray)
    {
        $db = Query::dbCheckOut();

        $recordTables   = array("RECORD_DAT", "RECORD_SCORE_DAT", "RECORD_SCORE_HIST_DAT");
        $rankTables     = array("RECORD_RANK_DAT", "RECORD_RANK_SDIV_DAT", "RECORD_RANK_FI_SDIV_DAT");
        $studyRecTables = array("SCHREG_STUDYREC_DAT");
        $chairTables    = array("CHAIR_DAT");
        $attendTables   = array("ATTEND_SUBCLASS_DAT");
        $allTables      = array_merge($recordTables, $rankTables, $studyRecTables, $chairTables, $attendTables);

        $query          = knjz070Query::getTargetTable($this, $allTables);

        $existFlg = false;
        $checkArray = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $tableName = $row["TABLE_NAME"];
            $typeSort =
            $typeName = "";
            if (in_array($tableName, $recordTables)) {
                $typeSort = 1;
                $typeName = "成績";
            }
            if (in_array($tableName, $rankTables)) {
                $typeSort = 2;
                $typeName = "序列後の成績";
            }
            if (in_array($tableName, $studyRecTables)) {
                $typeSort = 3;
                $typeName = "指導要録用成績";
            }
            if (in_array($tableName, $chairTables)) {
                $typeSort = 4;
                $typeName = "講座";
            }
            if (in_array($tableName, $attendTables)) {
                $typeSort = 5;
                $typeName = "科目別出欠";
            }

            $existsCnt = $db->getOne(knjz070Query::checkSubclass($this, $tableName, $subclasscdArray));
            $existFlg = ($existsCnt > 0) ? true : $existFlg;
            if ($existsCnt > 0) {
                $checkArray[$typeSort] =  $typeName;
            }
        }
        $result->free();

        $msg = "\\r\\n";
        for ($i = 1; $i <= 5; $i++) {
            if (!isset($checkArray[$i])) {
                continue;
            }

            $typeName = $checkArray[$i];
            if ($typeName) {
                $msg .= "{$typeName}データに登録されています。";
                $msg .= "\\r\\n";
            }
        }
        Query::dbCheckIn($db);

        return array($existFlg, $msg);
    }

    //編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
        }
    }
    //アップデート時の内部メソッド
    public function &update()
    {
        $result = knjz070Query::getUpdateQuery($this);
        if (!isset($this->warning)) {
            $this->setMessage("MSG201");
        }
        if ($result == true) {
            $this->year = "";
        }
        return $result;
    }
}