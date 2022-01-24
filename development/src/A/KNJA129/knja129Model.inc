<?php

require_once('for_php7.php');

class knja129Model extends Model
{
    public $cmd;
    public $exp_year = "";
    public $exp_semester = "";
    public $schregno = "";
    public $name = "";
    public $field = array();
    public $control;
    public $annual;        //学年(年次)
    public $getPrgId;      //コールされたか
    public $sendAuth;      //親画面の権限
    public $auth;          //権限
    public $moji = array();
    public $gyo = array();


    public function knja129Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        if (VARS::get("PROGRAMID")!="") {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->annual);
            unset($this->grade);
            unset($this->moji);
            unset($this->gyo);
        }

        if (VARS::get("EXP_YEAR")!="") {
            $this->exp_year     = VARS::get("EXP_YEAR");
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }
        if (VARS::get("SCHREGNO")!="") {
            $this->schregno   = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME")!="") {
            $this->name       = VARS::get("NAME");
        }
        if (VARS::get("GRADE")!="") {
            $this->grade      = VARS::get("GRADE");
        }

        if (VARS::get("target") != "") {
            $this->target   = VARS::get("target");
        }

        if (VARS::post("ANNUAL")) {   //学年（年次）
            list($tmp["YEAR"], $tmp["ANNUAL"]) = explode(",", VARS::post("ANNUAL"));
            $this->annual = $tmp;
        }

        $this->cmd = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $schSchoolKind = "";
        if ($this->schregno) {
            $schSchoolKind = knja129Query::getSchoolKind($this->schregno, $this->exp_year);
        } else {
            if ($this->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $schSchoolKind = SCHOOLKIND;
            }
        }

        $this->jidoOrSeito = in_array($schSchoolKind, array("K", "P")) ? "児童" : "生徒";

        $this->inputs = array();
        $this->inputs["ABSENCE_REASON"]     = array("TYPE" => "TEXTAREA", "MOJI" => 45, "GYOU" => 3, "TITLE" => "{$this->jidoOrSeito}が登校できない事由");
        $this->inputs["DAYS"]               = array("TYPE" => "TEXTINT",  "MOJI" => 3, "TITLE" => "実施日数");
        $this->inputs["PARTICIPATION_DAYS"] = array("TYPE" => "TEXTINT",  "MOJI" => 3, "TITLE" => "参加日数");
        $this->inputs["METHOD"]             = array("TYPE" => "TEXTAREA", "MOJI" => 35, "GYOU" => 3, "TITLE" => "実施方法等");
        $this->inputs["OTHER_LEARNING"]     = array("TYPE" => "TEXTAREA", "MOJI" => 45, "GYOU" => 3, "TITLE" => "その他の学習等");

        foreach (array(
              "ABSENCE_REASON"     => "seitoSidoYorokuOnlineJiyuSize",
              "METHOD"             => "seitoSidoYorokuOnlineJisshiHouhouSize",
              "OTHER_LEARNING"     => "seitoSidoYorokuOnlineSonotaGakushuSize"
        ) as $fieldname => $propname) {
            if ($this->Properties[$propname]) {
                list($moji, $gyou) = preg_split("/\*/", $this->Properties[$propname]);
                if ($moji && $gyou) {
                    $this->inputs[$fieldname]["MOJI"] = (int)trim($moji);
                    $this->inputs[$fieldname]["GYOU"]  = (int)trim($gyou);
                }
            }
        }

        $this->field = array();
        foreach ($this->inputs as $fieldname => $input) {
            $this->field[$fieldname] = VARS::post($fieldname);
        }
        $this->field["DBNAME"] = VARS::post("DBNAME");
    }

    //入力項目チェック
    public function validateInput()
    {
        if ($this->schregno=="") {
            $this->setWarning("MSG304");
            return false;
        }

        foreach ($this->inputs as $fieldname => $input) {
            if ($input["TYPE"] == "TEXTAREA") {
                if ($this->validate_row_cnt($this->field[$fieldname], $input["MOJI"] * 2) > $input["GYOU"]) {
                    $this->setWarning("MSG915", "({$input["TITLE"]}は{$input["GYOU"]}行までです)");
                    return false;
                }
            }
        }

        return true;
    }

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
        knja129Query::getDeleteQuery($this->schregno, $this->exp_year);
        knja129Query::getInsertQuery($this->field, $this->schregno, $this->exp_year, $this->exp_semester);

        return true;
    }
}
