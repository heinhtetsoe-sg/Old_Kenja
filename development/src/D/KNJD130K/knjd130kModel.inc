<?php

require_once('for_php7.php');

class knjd130kModel extends Model {
    var $cmd;
    var $schregno = "";
    var $name = "";
    var $grade = "";
    var $hr_class;
    var $field = array();
    var $gakki;

    function knjd130kModel() {}

    function init() {

        if (VARS::get("PROGRAMID") != "") {
            unset($this->schregno);
            unset($this->name);
            unset($this->gakki);
            unset($this->grade);
            unset($this->hr_class);
            unset($this->allwidth);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->cmd = VARS::request("cmd");

        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME") != "") {
            $this->name = VARS::get("NAME");
        }
        if (VARS::get("GRADE") != "") {
            $this->grade = VARS::get("GRADE");
        }
        if (VARS::get("HR_CLASS") != "") {
            $this->hr_class = VARS::get("HR_CLASS");
        }

        //学期
        $this->gakki = VARS::post("GAKKI");

        //画面サイズ
        if ($this->allwidth == "") {
            $this->allwidth = VARS::post("allwidth");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->schregno);
            unset($this->name);
            unset($this->gakki);
            unset($this->grade);
        }

        //校種
        if ($this->schregno) {
            $db = Query::dbCheckOut();
            $this->school_kind = knjd130kQuery::getSchoolKind($this->schregno);
            Query::dbCheckIn($db);
        } else {
            if ($this->Properties["use_prg_schoolkind"] == "1") {
                if ($this->selectSchoolKind) {
                    if (get_count(explode(':', $model->selectSchoolKind)) == 1) {
                        $this->school_kind = $model->selectSchoolKind;
                    } else {
                        $this->school_kind = "H";
                    }
                } else {
                    $this->school_kind = "H";
                }
            } else if ($this->Properties["useSchool_KindField"] == "1") {
                $this->school_kind = SCHOOLKIND;
            }
        }
        
        //項目一覧
        $this->textList = array();
        $this->textList["REMARK1"]   = array("moji" => 30, "gyou" => 2,  "flg" => "",  "label" => "道徳");

        foreach ($this->textList as $field => $val) {
            //サイズ初期値
            $this->getPro[$field]["moji"] = $val["moji"];
            $this->getPro[$field]["gyou"] = $val["gyou"];

            //値の保持
            $this->field[$field] = VARS::post($field);
        }
    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }

        //文字数チェック
        foreach ($this->textList as $field => $val) {
            $value = $this->field[$field];
            if ($this->validate_row_cnt($value, ($this->getPro[$field]["moji"] * 2)) > $this->getPro[$field]["gyou"]) {
                if ($this->getPro[$field]["gyou"] == 1) {
                    $this->setWarning("MSG915","( {$val["label"]}は全角{$this->getPro[$field]["gyou"]}文字までです )");
                } else {
                    $this->setWarning("MSG915","( {$val["label"]}は{$this->getPro[$field]["gyou"]}行までです )");
                }
                return false;
            }
        }

        return true;
    }

    //更新処理
    function &getUpdateModel() {
        if (!$this->validateInput()) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }

    function &update() {
        knjd130kQuery::getUpdateQuery($this, $this->schregno);
        return;
    }
}
?>
