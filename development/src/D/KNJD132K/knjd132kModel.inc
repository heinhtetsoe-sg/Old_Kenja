<?php

require_once('for_php7.php');

class knjd132kModel extends Model
{
    public $cmd;
    public $schregno = "";
    public $name = "";
    public $grade = "";
    public $hr_class;
    public $field = array();
    public $gakki;

    public function knjd132kModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->exp_year);
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

        if (VARS::get("EXP_YEAR") != "") {
            $this->exp_year = VARS::get("EXP_YEAR");
        }
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
            $this->school_kind = knjd132kQuery::getSchoolKind($this->schregno);
            Query::dbCheckIn($db);
        } else {
            if ($this->Properties["use_prg_schoolkind"] == "1") {
                if ($this->selectSchoolKind) {
                    if (get_count(explode(':', $this->selectSchoolKind)) == 1) {
                        $this->school_kind = $this->selectSchoolKind;
                    } else {
                        $this->school_kind = "H";
                    }
                } else {
                    $this->school_kind = "H";
                }
            } elseif ($this->Properties["useSchool_KindField"] == "1") {
                $this->school_kind = SCHOOLKIND;
            }
        }
        
        //項目一覧
        $this->textList = array();
        // テキストエリア設定
        $this->textList["0602_REMARK1"]   = array("moji" => 30, "gyou" => 2,  "flg" => "",  "label" => "");
        $this->textList["0602_REMARK2"]   = array("moji" => 30, "gyou" => 2,  "flg" => "",  "label" => "");
        $this->textList["0602_REMARK3"]   = array("moji" => 30, "gyou" => 2,  "flg" => "",  "label" => "");

        $this->textList["0702_REMARK1"]   = array("moji" => 30, "gyou" => 2,  "flg" => "",  "label" => "");
        $this->textList["0702_REMARK2"]   = array("moji" => 30, "gyou" => 2,  "flg" => "",  "label" => "");
        $this->textList["0702_REMARK3"]   = array("moji" => 30, "gyou" => 2,  "flg" => "",  "label" => "");

        $this->textList["0801_REMARK1"]   = array("moji" => 30, "gyou" => 2,  "flg" => "",  "label" => "　　道　　　徳");

        // 定型文設定
        $this->textList["0602_REMARK1"] += array("dataDiv" => "21", "title" => "総合表現-");
        $this->textList["0602_REMARK2"] += array("dataDiv" => "22", "title" => "総合表現-");
        $this->textList["0602_REMARK3"] += array("dataDiv" => "23", "title" => "総合表現-");
        $this->textList["0702_REMARK1"] += array("dataDiv" => "24", "title" => "総合探究-");
        $this->textList["0702_REMARK2"] += array("dataDiv" => "25", "title" => "総合探究-");
        $this->textList["0702_REMARK3"] += array("dataDiv" => "26", "title" => "総合探究-");

        foreach ($this->textList as $field => $val) {
            //サイズ初期値
            $this->getPro[$field]["moji"] = $val["moji"];
            $this->getPro[$field]["gyou"] = $val["gyou"];

            //値の保持
            $this->field[$field] = VARS::post($field);
        }
    }

    //入力項目チェック
    public function validateInput()
    {
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
                    $this->setWarning("MSG915", "( {$val["label"]}は全角{$this->getPro[$field]["gyou"]}文字までです )");
                } else {
                    $this->setWarning("MSG915", "( {$val["label"]}は{$this->getPro[$field]["gyou"]}行までです )");
                }
                return false;
            }
        }

        return true;
    }

    //更新処理
    public function &getUpdateModel()
    {
        if (!$this->validateInput()) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }

    public function &update()
    {
        knjd132kQuery::getUpdateQuery($this, $this->schregno);
        return;
    }
}
