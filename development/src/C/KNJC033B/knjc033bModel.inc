<?php

require_once('for_php7.php');

class knjc033bModel extends Model {
    var $cmd;
    var $field;
    var $appointed_day;

    function knjc033bModel() {
        $this->field = array();
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"]) {
            $this->getSelectSchoolKind();
        }

        //月コード
        $month = preg_split("/-/", VARS::request("MONTHCD"));
        $this->field["MONTHCD"]  = VARS::request("MONTHCD");
        $this->field["MONTH"]    = $month[0];
        $this->field["SEMESTER"] = $month[1];

        //学級コード
        $this->field["GRADE_HR_CLASS"] = VARS::request("GRADE_HR_CLASS");

        //出欠項目一覧
        $this->field["UPD_FIELD"] = VARS::request("UPD_FIELD");

        //生徒一覧
        $this->field["SCH_ARRAY"] = VARS::request("SCH_ARRAY");

        //科目一覧
        $this->field["SUBCLASS"] = VARS::request("SUBCLASS");

        //校種取得
        $this->school_kind = knjc033bQuery::getSchoolKind($this);

        if ($this->field["SUBCLASS"]) {
            foreach (explode(',', $this->field["SUBCLASS"]) as $subclasscd) {
                $this->field["SICK"][$subclasscd]        = VARS::post("SICK:".$subclasscd);
                $this->field["NOTICE"][$subclasscd]      = VARS::post("NOTICE:".$subclasscd);
                $this->field["NONOTICE"][$subclasscd]    = VARS::post("NONOTICE:".$subclasscd);
            }
        }

        //生徒一覧
        $this->field["SCHREGNO"] = VARS::post("SCHREGNO");

        //生徒ごとの出欠入力対象科目
        $this->field["ATTEND_FIELD"] = VARS::post("ATTEND_FIELD");

        $this->getPropertiesAll();  //プロパティファイルの読込み
    }

    //入力項目チェック
    function validateInput() {
        //更新対象データがない場合
        if (!$this->field["MONTHCD"] || !$this->field["SUBCLASS"]) {
            $this->setWarning("MSG303");
            return false;
        }
        if($this->field["SCHREGNO"] == NULL || $this->field["SCHREGNO"] == "") {
            $this->setWarning("MSG303");
            return false;
        }

        //締め日チェック
        if (!$this->appointed_day) {
            $this->setWarning("MSG305", "締め日を設定してください。");
            return false;
        }

        return true;
    }

    //更新処理
    function &getUpdateModel() {
        if ($this->validateInput()) {
            knjc033bQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }
}
?>
