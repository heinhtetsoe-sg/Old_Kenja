<?php

require_once('for_php7.php');

class knjd425l_1Model extends Model {

    var $cmd;
    var $schregno;
    var $name;
    var $grade;
    var $exp_year;
    var $exp_semester;
    var $selKindNo;
    var $upddate;
    var $control;           //コントロールマスタ
    var $sendPrgId;
    var $sendAuth;
    var $auth;

    var $field = array();

    function knjd425l_1Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->sendPrgId);
            unset($this->sendAuth);
            unset($this->auth);

            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->selKindNo);
            unset($this->upddate);
            unset($this->field);
        }

        if (VARS::get("SEND_PRGID")) {
            $this->sendPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        //権限
        $this->auth = $this->sendPrgId ? $this->sendAuth : AUTHORITY;

        $this->cmd = VARS::request("cmd");

        if (VARS::get("EXP_YEAR") != "") {
            $this->exp_year = VARS::get("EXP_YEAR");
        }
        if (VARS::get("EXP_SEMESTER") != "") {
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }
        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME") != "") {
            $this->name     = VARS::get("NAME");
        }
        if (VARS::get("GRADE") != "") {
            $this->grade    = VARS::get("GRADE");
        }
        if (VARS::get("KINDNO")) {
            $this->selKindNo = VARS::get("KINDNO");
        }
        if (VARS::get("UPDDATE")) {
            $this->upddate = VARS::get("UPDDATE");
            $this->recordDate = VARS::get("UPDDATE");
        }

        if (VARS::post("RECORD_DATE")) {
            $this->recordDate = VARS::post("RECORD_DATE");
        }
        if ($this->recordDate) {
            $this->recordDate = str_replace("/", "-", $this->recordDate);
        }

        $this->field = array();

        //各textareaのタイトル
        $this->remarkTitle = array(
               "1"  =>  "児童・生徒の実態"
            ,  "2"  =>  "健康の保持"
            ,  "3"  =>  "心理的な安定"
            ,  "4"  =>  "人間関係の形成"
            ,  "5"  =>  "環境の把握"
            ,  "6"  =>  "身体の動き"
            ,  "7"  =>  "コミュニケーション"
            ,  "8"  =>  "指導目標"
            ,  "9"  =>  "具体的な指導内容①"
            , "10"  =>  "具体的な指導内容②"
            , "11"  =>  "具体的な指導内容③"
        );

        //各textareaの最大文字数
        $this->remarkTextLimit = array(
            "1"  =>  array("moji" => 45, "gyou" =>  7)
            ,  "2"  =>  array("moji" =>  7, "gyou" =>  7)
            ,  "3"  =>  array("moji" =>  7, "gyou" =>  7)
            ,  "4"  =>  array("moji" =>  7, "gyou" =>  7)
            ,  "5"  =>  array("moji" =>  7, "gyou" =>  7)
            ,  "6"  =>  array("moji" =>  7, "gyou" =>  7)
            ,  "7"  =>  array("moji" =>  7, "gyou" =>  7)
            ,  "8"  =>  array("moji" => 45, "gyou" =>  3)
            ,  "9"  =>  array("moji" => 15, "gyou" =>  5)
            , "10"  =>  array("moji" => 15, "gyou" =>  5)
            , "11"  =>  array("moji" => 15, "gyou" =>  5)
        );
        //テキストエリアの値取得
        foreach ($this->remarkTextLimit as $key => $textLimit) {
            $this->field["REMARK_".$key] = VARS::post("REMARK_".$key);
        }

        //自立活動のタイトル
        $this->selfrelianceTitle = array(
            "1"  =>  "健康の保持"
            ,  "2"  =>  "心理的な安定"
            ,  "3"  =>  "人間関係の形成"
            ,  "4"  =>  "環境の把握"
            ,  "5"  =>  "身体の動き"
            ,  "6"  =>  "コミュニケーション"
        );
        $this->selfrelianceSubTitle = array();
        $this->selfrelianceSubTitle["1"]["1"] = "①";
        $this->selfrelianceSubTitle["1"]["2"] = "②";
        $this->selfrelianceSubTitle["1"]["3"] = "③";
        $this->selfrelianceSubTitle["1"]["4"] = "④";
        $this->selfrelianceSubTitle["1"]["5"] = "⑤";

        $this->selfrelianceSubTitle["2"]["1"] = "①";
        $this->selfrelianceSubTitle["2"]["2"] = "②";
        $this->selfrelianceSubTitle["2"]["3"] = "③";

        $this->selfrelianceSubTitle["3"]["1"] = "①";
        $this->selfrelianceSubTitle["3"]["2"] = "②";
        $this->selfrelianceSubTitle["3"]["3"] = "③";
        $this->selfrelianceSubTitle["3"]["4"] = "④";

        $this->selfrelianceSubTitle["4"]["1"] = "①";
        $this->selfrelianceSubTitle["4"]["2"] = "②";
        $this->selfrelianceSubTitle["4"]["3"] = "③";
        $this->selfrelianceSubTitle["4"]["4"] = "④";
        $this->selfrelianceSubTitle["4"]["5"] = "⑤";

        $this->selfrelianceSubTitle["5"]["1"] = "①";
        $this->selfrelianceSubTitle["5"]["2"] = "②";
        $this->selfrelianceSubTitle["5"]["3"] = "③";
        $this->selfrelianceSubTitle["5"]["4"] = "④";
        $this->selfrelianceSubTitle["5"]["5"] = "⑤";

        $this->selfrelianceSubTitle["6"]["1"] = "①";
        $this->selfrelianceSubTitle["6"]["2"] = "②";
        $this->selfrelianceSubTitle["6"]["3"] = "③";
        $this->selfrelianceSubTitle["6"]["4"] = "④";
        $this->selfrelianceSubTitle["6"]["5"] = "⑤";

        //自立活動の取得項目
        $this->selfrelianceList = array(
            "1"  =>  array("1", "2", "3", "4", "5")
            ,  "2"  =>  array("1", "2", "3")
            ,  "3"  =>  array("1", "2", "3", "4")
            ,  "4"  =>  array("1", "2", "3", "4", "5")
            ,  "5"  =>  array("1", "2", "3", "4", "5")
            ,  "6"  =>  array("1", "2", "3", "4", "5")
        );

        foreach ($this->selfrelianceList as $selfDiv => $selfreliance) {
            for ($i=0; $i < get_count($selfreliance); $i++) {
                $seq = $selfreliance[$i];
                $this->field["SELF_".$selfDiv."_".$seq] = VARS::post("SELF_".$selfDiv."_".$seq);
            }
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
        if ($this->cmd == "update") {
            foreach ($this->remarkTextLimit as $seq => $textLimit) {
                $moji = $textLimit["moji"];
                $gyou = $textLimit["gyou"];

                if ($this->validate_row_cnt($this->field["REMARK_{$seq}"], ($moji * 2)) > $gyou) {
                    $this->setWarning("MSG915","({$this->remarkTitle[$seq]}は{$moji}文字X{$gyou}行までです。)");
                    return false;
                }

            }
        }

        return true;
    }

    //更新用のモデルを返します
    function &getUpdateModel() {
        if (!$this->validateInput()) {
            $this->setCmd("check");
            return;
        }

        knjd425l_1Query::getUpdateQuery($this);
        $this->setCmd("edit");
        $this->setMessage("MSG201");
    }

}
?>
