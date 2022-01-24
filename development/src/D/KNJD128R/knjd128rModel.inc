<?php

require_once('for_php7.php');

class knjd128rModel extends Model {
    var $cmd;
    var $field;

    function knjd128rModel() {
        $this->field = array();
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        /*** ヘッダー部 ***/
        //学期コード
        $this->field["SEMESTER"] = VARS::post("SEMESTER");
        $this->field["SCH_SEMESTER"] = $this->field["SEMESTER"] == "9" ? CTRL_SEMESTER : $this->field["SEMESTER"];

        //学級コード
        $this->field["GRADE_HR_CLASS"] = VARS::post("GRADE_HR_CLASS");

        //テスト種別コード
        $this->field["TESTKINDCD"] = VARS::post("TESTKINDCD");

        /*** データ部 ***/
        //生徒一覧
        $this->field["SCH_ARRAY"] = VARS::request("SCH_ARRAY");

        //科目一覧
        $this->field["SUBCLASS"] = VARS::request("SUBCLASS");

        //成績
        if ($this->field["SUBCLASS"]) {
            foreach (explode(',', $this->field["SUBCLASS"]) as $subclasscd) {
                $this->field["SCORE"][$subclasscd] = VARS::post("SCORE:".$subclasscd);
            }
        }

        //生徒一覧
        $this->field["SCHREGNO"] = VARS::post("SCHREGNO");

        //生徒ごとの出欠入力対象科目
        $this->field["RECORD_FIELD"] = VARS::post("RECORD_FIELD");

        $this->getPropertiesAll();  //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput() {
        //更新対象データがない場合
        if (!$this->field["TESTKINDCD"] || !$this->field["SUBCLASS"]) {
            $this->setWarning("MSG303");
            return false;
        }
        if($this->field["SCHREGNO"] == NULL || $this->field["SCHREGNO"] == "") {
            $this->setWarning("MSG303");
            return false;
        }

        return true;
    }

    //更新処理
    function &getUpdateModel() {
        if ($this->validateInput()) {
            knjd128rQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }
}
?>
