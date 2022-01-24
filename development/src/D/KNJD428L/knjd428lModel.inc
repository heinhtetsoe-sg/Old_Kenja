<?php

require_once('for_php7.php');

class knjd428lModel extends Model {
    var $cmd;
    var $schregno;
    var $name;
    var $semester;
    var $field = array();
    var $control;
    var $exp_year;      //左フレームから送られてくる年度
    var $sendPrgId;      //コールされたか
    var $sendAuth;      //親画面の権限
    var $auth;          //権限
    var $grade;
    var $hrClass;

    function knjd428lModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->sendPrgId);
            unset($this->sendAuth);
            unset($this->auth);
            unset($this->exp_year);
            unset($this->schregno);
            unset($this->name);
            unset($this->semester);
            unset($this->grade);
            unset($this->hrClass);
        }

        if (VARS::get("SEND_PRGID")) {
            $this->sendPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth  = VARS::get("SEND_AUTH");
        }
        //権限
        $this->auth = $this->sendPrgId ? $this->sendAuth : AUTHORITY;

        $this->field = array(
                              "SEMESTER"        => VARS::post("SEMESTER")
                            , "REMARK1_01"        => VARS::post("REMARK1_01")
                            , "FOREIGNLANGACT"        => VARS::post("FOREIGNLANGACT")
                            , "TOTALSTUDYTIME"        => VARS::post("TOTALSTUDYTIME")
                            , "SPECIALACTREMARK"        => VARS::post("SPECIALACTREMARK")
                            , "REMARK1_02"        => VARS::post("REMARK1_02")
                            , "ATTENDREC_REMARK"        => VARS::post("ATTENDREC_REMARK")
                            , "COMMUNICATION"        => VARS::post("COMMUNICATION")
                            );

        $this->cmd = VARS::request("cmd");

        //左フレームから送られてくる
        //年度
        if (VARS::get("EXP_YEAR")) {
            $this->exp_year = VARS::get("EXP_YEAR");
        }
        //学期
        if (VARS::get("EXP_SEMESTER")) {
            $this->semester = VARS::get("EXP_SEMESTER");
        }
        //学籍番号
        if (VARS::get("SCHREGNO")) {
            $this->schregno = VARS::get("SCHREGNO");
        }
        //氏名
        if (VARS::get("NAME")) {
            $this->name = VARS::get("NAME");
        }
        //年
        if (VARS::get("GRADE") != "") {
            $this->grade = VARS::get("GRADE");
        }
        //組
        if (VARS::get("HR_CLASS") != "") {
            $this->hrClass = VARS::get("HR_CLASS");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->exp_year);
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->hrClass);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }


        //特別な教科の入力欄表示有無
        //  [校種(SCHOOL_KIND)][学年(GRADE_CD)]
        //  ※学年は「SCHREG_REGD_GDAT」の「GRADE_CD」を使用
        //    GRADE_CD="00"は全学年を表示
        //    学年(GRADE_CD)で取得出来た場合は取得した入力欄表示有無を使用
        //    取得できなかった場合は"00"を使用する
        //  「1:特別の教科　道徳」「2:外国語活動」「3:総合的な学習の時間」「4:特別活動」「5:自立活動」
        $this->useSection = array();
        //P:小学
        $this->useSection["P"]["00"] = array("1", "2", "3", "4", "5");
        $this->useSection["P"]["01"] = array("1", "", "", "4", "5");
        $this->useSection["P"]["02"] = array("1", "", "", "4", "5");
        $this->useSection["P"]["03"] = array("1", "2", "3", "4", "5");
        $this->useSection["P"]["04"] = array("1", "2", "3", "4", "5");
        $this->useSection["P"]["05"] = array("1", "", "3", "4", "5");
        $this->useSection["P"]["06"] = array("1", "", "3", "4", "5");
        //J:中学
        $this->useSection["J"]["00"] = array("1", "", "3", "4", "5");
        //H:高校
        $this->useSection["H"]["00"] = array("", "", "3", "4", "5");
        //H:専攻
        $this->useSection["A"]["00"] = array("", "", "", "", "");

        //各textareaの最大文字数
        $this->textLimit = array(
              "REMARK1_01"       => array("title" => "特別の教科 道徳",   "moji" => 35, "gyou" => 6)
            , "FOREIGNLANGACT"   => array("title" => "外国語活動",        "moji" => 35, "gyou" => 6)
            , "TOTALSTUDYTIME"   => array("title" => "総合的な学習の時間", "moji" => 35, "gyou" => 6)
            , "SPECIALACTREMARK" => array("title" => "特別活動",          "moji" => 35, "gyou" => 6)
            , "REMARK1_02"       => array("title" => "自立活動",          "moji" => 35, "gyou" => 6)
            , "ATTENDREC_REMARK" => array("title" => "出欠の備考",        "moji" => 10, "gyou" => 2)
            , "COMMUNICATION"    => array("title" => "学校より",          "moji" => 45, "gyou" => 6)
        );

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
            foreach ($this->textLimit as $key => $textLimit) {
                if ($this->validate_row_cnt($this->field[$key], ($textLimit["moji"] * 2)) > $textLimit["gyou"]) {
                    $this->setWarning("MSG915","({$textLimit["title"]}は全角{$textLimit["moji"]}文字X{$textLimit["gyou"]}行までです)");
                    return false;
                }
            }
        }

        $db = Query::dbCheckOut();
        //生徒の校種・学年を取得
        $query = knjd428lQuery::getSchoolKindGrade($this);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if (!$row) {
            $this->setWarning("MSG303", '生徒に対応する校種・学年が取得できませんでした。');
            return false;
        }
        Query::dbCheckIn($db);

        return true;
    }

    //更新
    function &getUpdateModel() {
        if (!$this->validateInput()) {
            $this->setCmd("check");
            return;
        }
        knjd428lQuery::getUpdateQuery($this->field, $this);
        $this->setCmd("edit");
        $this->setMessage("MSG201");
    }

}
?>
