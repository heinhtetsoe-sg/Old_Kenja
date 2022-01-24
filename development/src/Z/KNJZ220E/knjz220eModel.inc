<?php

require_once('for_php7.php');

//定数定義
class knjz220eModel extends Model {
    var $field = array();
    var $cmd;
    var $schoolKind;
    var $grade;

    //メンバ変数の初期化
    function knjz220eModel() {
    }

    function init() {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->schoolKind);
            unset($this->grade);
        }

        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR;

        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //校種の初期値を設定
        if (!$this->schoolKind) {
            $db = Query::dbCheckOut();

            $row = $db->getRow(knjz220eQuery::getVNameMstA023($this), DB_FETCHMODE_ASSOC);
            $this->schoolKind = $row["VALUE"];

            Query::dbCheckIn($db);
        }

        if (VARS::post("SCHOOL_KIND")) {
            $this->schoolKind = VARS::post("SCHOOL_KIND");
        }

        $getParamGrade = VARS::get("GRADE");
        if (isset($getParamGrade)) {
            $this->grade      = VARS::get("GRADE");
        }

        $this->field = array("GRADE"        => VARS::post("GRADE"),           //学年
                             "MIKOMI_RATE"  => VARS::post("MIKOMI_RATE"),     //算出比率（％）
                             );
    }

    //入力項目チェック
    function validateInput($kind) {
        if (strlen($this->field["GRADE"]) == 0) {
            $this->setWarning("MSG301","(学年)");
            return false;
        }

        if (($kind=="update" || $kind=="delete") && $this->grade != $this->field["GRADE"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }

        $db = Query::dbCheckOut();

        //入力項目の１レコード取得
        $row = $db->getRow(knjz220eQuery::getRow($this, $this->field["GRADE"]), DB_FETCHMODE_ASSOC);
        //重複チェック
        if ($kind == "insert" && is_array($row)) {
            $this->setWarning("MSG918");
            return false;
        }
        //存在チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }

        Query::dbCheckIn($db);

        return true;
    }

    //追加処理
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput("insert")) {
            knjz220eQuery::getInsertQuery($this);
            $this->grade = $this->field["GRADE"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput("update")) {
            knjz220eQuery::getUpdateQuery($this);
            $this->grade = $this->field["GRADE"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput("delete")) {
            knjz220eQuery::getDeleteQuery($this);
            $this->grade = "";
            $this->field["GRADE"] = "";
            $this->setMessage("MSG201");
        }
        return;
    }

    //前年度のデータをコピー
    function &getCopyModel() {
        //今年度のデータが存在するか確認
        $thisYear = knjz220eQuery::CheckData(CTRL_YEAR);
        if (isset($thisYear)) {
           $this->setWarning('MSG203', "今年度のデータが存在します。");
           return false;
        }
        //前年度のデータが存在するか確認
        $preYear = knjz220eQuery::CheckData(CTRL_YEAR - 1);
        if (!isset($preYear)) {
           $this->setWarning('MSG203', "前年データが存在しません。");
           return false;
        }

        knjz220eQuery::getCopyQuery($this);
        $this->setMessage("MSG201");
        return;
    }
}
?>
