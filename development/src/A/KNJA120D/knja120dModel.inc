<?php

require_once('for_php7.php');

class knja120dModel extends Model {

    var $cmd;
    var $exp_year = "";
    var $exp_semester = "";
    var $schregno = "";
    var $name = "";
    var $grade;
    var $field = array();
    var $control;
    var $annual;        //学年(年次)
    var $getPrgId;      //コールされたか
    var $sendAuth;      //親画面の権限
    var $auth;          //権限


    function knja120dModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if(VARS::get("SEND_PRGID")){
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

        if (VARS::post("ANNUAL")){   //学年（年次）
            list($tmp["YEAR"], $tmp["ANNUAL"]) = explode(",", VARS::post("ANNUAL"));
            $this->annual = $tmp;
        }

        $this->field = array("TOTALSTUDYACT"        =>  VARS::post("TOTALSTUDYACT"),
                             "TOTALSTUDYVAL"        =>  VARS::post("TOTALSTUDYVAL"),
                             "SPECIALACTREMARK"     =>  VARS::post("SPECIALACTREMARK"),
                             "TRAIN_REF1"           =>  VARS::post("TRAIN_REF1"),
                             "TRAIN_REF2"           =>  VARS::post("TRAIN_REF2"),
                             "TRAIN_REF3"           =>  VARS::post("TRAIN_REF3"),
                             "ATTENDREC_REMARK"     =>  VARS::post("ATTENDREC_REMARK"),
                             "INS_COMMENTS"         => VARS::post("INS_COMMENTS"),      //特記事項なし
                             "INS_COMMENTS_LABEL"   => VARS::post("INS_COMMENTS_LABEL"), //特記事項なし
                             "TOTALSTUDYACT_BG_COLOR_FLG"       =>  VARS::post("TOTALSTUDYACT_BG_COLOR_FLG"),
                             "TOTALSTUDYVAL_BG_COLOR_FLG"       =>  VARS::post("TOTALSTUDYVAL_BG_COLOR_FLG"),
                             "SPECIALACTREMARK_BG_COLOR_FLG"    =>  VARS::post("SPECIALACTREMARK_BG_COLOR_FLG"),
                             "TRAIN_REF1_BG_COLOR_FLG"          =>  VARS::post("TRAIN_REF1_BG_COLOR_FLG"),
                             "TRAIN_REF2_BG_COLOR_FLG"          =>  VARS::post("TRAIN_REF2_BG_COLOR_FLG"),
                             "TRAIN_REF3_BG_COLOR_FLG"          =>  VARS::post("TRAIN_REF3_BG_COLOR_FLG"),
                             "ATTENDREC_REMARK_BG_COLOR_FLG"    =>  VARS::post("ATTENDREC_REMARK_BG_COLOR_FLG"),
                             "UPDATED"              =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");

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
        if (VARS::get("GRADE") != "") {
            $this->grade = VARS::get("GRADE");
        }

        if (VARS::get("target") != "") {
            $this->target   = VARS::get("target");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->annual);
            unset($this->grade);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->file = VARS::file("FILE");

        //年度コンボ（通知表所見）
        if ($this->Properties["useTuutisyoSyokenNendo"] == "1") {
            $this->year_cmb = VARS::request("YEAR_CMB");
        } else {
            $this->year_cmb = $this->exp_year;
        }

        if ($this->Properties["seitoSidoYoroku_dat_TotalstudyactSize"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["seitoSidoYoroku_dat_TotalstudyactSize"]);
            $this->totalstudyact_moji = (int)trim($moji);
            $this->totalstudyact_gyou = (int)trim($gyou);
        } else {
            $this->totalstudyact_moji = 23; //デフォルトの値
            $this->totalstudyact_gyou = 5;  //デフォルトの値
        }
        if ($this->Properties["seitoSidoYoroku_dat_TotalstudyvalSize"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["seitoSidoYoroku_dat_TotalstudyvalSize"]);
            $this->totalstudyval_moji = (int)trim($moji);
            $this->totalstudyval_gyou = (int)trim($gyou);
        } else {
            $this->totalstudyval_moji = 23; //デフォルトの値
            $this->totalstudyval_gyou = 6;  //デフォルトの値
        }
        if ($this->Properties["seitoSidoYoroku_dat_Attendrec_RemarkSize"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["seitoSidoYoroku_dat_Attendrec_RemarkSize"]);
            $this->attendrec_remark_moji = (int)trim($moji);
            $this->attendrec_remark_gyou = (int)trim($gyou);
        } else {
            $this->attendrec_remark_moji = 20; //デフォルトの値
            $this->attendrec_remark_gyou = 2;  //デフォルトの値
        }
        if ($this->Properties["seitoSidoYoroku_dat_SpecialactremarkSize"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["seitoSidoYoroku_dat_SpecialactremarkSize"]);
            $this->specialactremark_moji = (int)trim($moji);
            $this->specialactremark_gyou = (int)trim($gyou);
        } else {
            $this->specialactremark_moji = 11; //デフォルトの値
            $this->specialactremark_gyou = 6;  //デフォルトの値
        }

        if ($this->Properties["seitoSidoYoroku_train_ref_1_2_3_field_size"] == 1) {
            $this->train_ref1_moji = 14;
            $this->train_ref2_moji = 21;
            $this->train_ref3_moji = 7;
        } else if ($this->Properties["seitoSidoYoroku_train_ref_1_2_3_field_size"] == 2) {
            $this->train_ref1_moji = 21;
            $this->train_ref2_moji = 21;
            $this->train_ref3_moji = 7;
        } else if (substr_count($this->Properties["seitoSidoYoroku_train_ref_1_2_3_field_size"], '-') == "2") {
            $train_ref_array = array();
            $train_ref_array = explode('-', $this->Properties["seitoSidoYoroku_train_ref_1_2_3_field_size"]);
            $this->train_ref1_moji = $train_ref_array[0];
            $this->train_ref2_moji = $train_ref_array[1];
            $this->train_ref3_moji = $train_ref_array[2];
        }
        if ($this->train_ref1_moji == '' || $this->train_ref2_moji == '' || $this->train_ref3_moji == '') {
            if ($this->Properties["train_ref_1_2_3_field_size"] == 1) {
                $this->train_ref1_moji = 14;
                $this->train_ref2_moji = 21;
                $this->train_ref3_moji = 7;
            } else if ($this->Properties["train_ref_1_2_3_field_size"] == 2) {
                $this->train_ref1_moji = 21;
                $this->train_ref2_moji = 21;
                $this->train_ref3_moji = 7;
            } else if (substr_count($this->Properties["train_ref_1_2_3_field_size"], '-') == "2") {
                $train_ref_array = array();
                $train_ref_array = explode('-', $this->Properties["train_ref_1_2_3_field_size"]);
                $this->train_ref1_moji = $train_ref_array[0];
                $this->train_ref2_moji = $train_ref_array[1];
                $this->train_ref3_moji = $train_ref_array[2];
            } else {
                $this->train_ref1_moji = 14;
                $this->train_ref2_moji = 14;
                $this->train_ref3_moji = 14;
            }
        }

        if ($this->Properties["seitoSidoYoroku_train_ref_1_2_3_gyo_size"]) {
            $this->train_ref1_gyou = $this->Properties["seitoSidoYoroku_train_ref_1_2_3_gyo_size"];
            $this->train_ref2_gyou = $this->Properties["seitoSidoYoroku_train_ref_1_2_3_gyo_size"];
            $this->train_ref3_gyou = $this->Properties["seitoSidoYoroku_train_ref_1_2_3_gyo_size"];
        } else if ($this->Properties["train_ref_1_2_3_gyo_size"]) {
            $this->train_ref1_gyou = $this->Properties["train_ref_1_2_3_gyo_size"];
            $this->train_ref2_gyou = $this->Properties["train_ref_1_2_3_gyo_size"];
            $this->train_ref3_gyou = $this->Properties["train_ref_1_2_3_gyo_size"];
        } else {
            $this->train_ref1_gyou = 5;
            $this->train_ref2_gyou = 5;
            $this->train_ref3_gyou = 5;
        }
    }

    //入力項目チェック
    function validateInput()
    {
        if ($this->schregno=="") {
            $this->setWarning("MSG304");
            return false;
        }
        if ($this->Properties["seitoSidoYoroku_dat_TotalstudyactSize"]) {
            if ($this->validate_row_cnt($this->field["TOTALSTUDYACT"], ((int)$this->totalstudyact_moji * 2)) > $this->totalstudyact_gyou) {
                $this->setWarning("MSG915","(活動内容は{$this->totalstudyact_gyou}行までです)");
                return false;
            }
        } else if ($this->Properties["seitoSidoYorokuFieldSize"] == 1) {
            if ($this->validate_row_cnt($this->field["TOTALSTUDYACT"], (22 * 2)) > 8) {
                $this->setWarning("MSG915","(活動内容は8行までです)");
                return false;
            }
        } else {
            if ($this->validate_row_cnt($this->field["TOTALSTUDYACT"], (11 * 2)) > 4) {
                $this->setWarning("MSG915","(活動内容は4行までです)");
                return false;
            }
        }

        if ($this->Properties["seitoSidoYoroku_dat_TotalstudyvalSize"]) {
            if ($this->validate_row_cnt($this->field["TOTALSTUDYVAL"], ((int)$this->totalstudyval_moji * 2)) > $this->totalstudyval_gyou) {
                $this->setWarning("MSG915","(評価は{$this->totalstudyval_gyou}行までです)");
                return false;
            }
        } else if ($this->Properties["seitoSidoYorokuFieldSize"] == 1) {
            if ($this->validate_row_cnt($this->field["TOTALSTUDYVAL"], (22 * 2)) > 8) {
                $this->setWarning("MSG915","(評価は8行までです)");
                return false;
            }
        } else {
            if ($this->validate_row_cnt($this->field["TOTALSTUDYVAL"], (11 * 2)) > 6) {
                $this->setWarning("MSG915","(評価は6行までです)");
                return false;
            }
        }

        if ($this->Properties["seitoSidoYoroku_dat_Attendrec_RemarkSize"]) {
            if ($this->validate_row_cnt($this->field["ATTENDREC_REMARK"], ((int)$this->attendrec_remark_moji * 2)) > $this->attendrec_remark_gyou) {
                $this->setWarning("MSG915","(出欠の記録備考は{$this->attendrec_remark_gyou}行までです)");
                return false;
            }
        } else if ($this->Properties["seitoSidoYorokuFieldSize"] == 1) {
            if ($this->validate_row_cnt($this->field["ATTENDREC_REMARK"], (40 * 2)) > 2) {
                $this->setWarning("MSG915","(出欠の記録備考は2行までです)");
                return false;
            }
        } else {
            if ($this->validate_row_cnt($this->field["ATTENDREC_REMARK"], (20 * 2)) > 2) {
                $this->setWarning("MSG915","(出欠の記録備考は2行までです)");
                return false;
            }
        }

        if ($this->Properties["seitoSidoYorokuSpecialactremarkFieldSize"] == 1) {
            if ($this->validate_row_cnt($this->field["SPECIALACTREMARK"], (22 * 2)) > 10) {
                $this->setWarning("MSG915","(特別活動所見は10行までです)");
                return false;
            }
        } else if ($this->Properties["seitoSidoYoroku_dat_SpecialactremarkSize"]) {
            if ($this->validate_row_cnt($this->field["SPECIALACTREMARK"], ((int)$this->specialactremark_moji * 2)) > $this->specialactremark_gyou) {
                $this->setWarning("MSG915","(特別活動所見は".$this->specialactremark_gyou."行までです)");
                return false;
            }
        } else {
            if ($this->validate_row_cnt($this->field["SPECIALACTREMARK"], (11 * 2)) > 6) {
                $this->setWarning("MSG915","(特別活動所見は6行までです)");
                return false;
            }
        }

        if ($this->validate_row_cnt($this->field["TRAIN_REF1"], ((int)$this->train_ref1_moji * 2)) > $this->train_ref1_gyou) {
            $this->setWarning("MSG915","(指導上参考になる諸事項1は{$this->train_ref1_gyou}行までです)");
            return false;
        } elseif ($this->validate_row_cnt($this->field["TRAIN_REF2"], ((int)$this->train_ref2_moji * 2)) > $this->train_ref2_gyou) {
            $this->setWarning("MSG915","(指導上参考になる諸事項2は{$this->train_ref2_gyou}行までです)");
            return false;
        } elseif ($this->validate_row_cnt($this->field["TRAIN_REF3"], ((int)$this->train_ref3_moji * 2)) > $this->train_ref3_gyou) {
            $this->setWarning("MSG915","(指導上参考になる諸事項3は{$this->train_ref3_gyou}行までです)");
            return false;
        }
        return true;
    }

    function &getUpdateModel()
    {
        if (!$this->validateInput()) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }

    function &update()
    {
        knja120dQuery::getInsertQuery($this->field,$this->schregno, $this->exp_year, $this->exp_semester);
        return $result;
    }

    /**
     * 実行モデルを返します
    */
    function &getExecModel()
    {
        $setFileName = explode(".", $this->file['name']);
        $setFileName[0] = mb_convert_encoding($setFileName[0], "SJIS-win", "UTF-8");
        $extension = $setFileName[get_count($setFileName) - 1];
//echo "cnt = " . get_count($setFileName) . "<BR>";
//echo "extension = " . $extension . "<BR>";
        //拡張子チェック
        if ($extension != "pdf" && $extension != "PDF") {
            $this->warning = "PDFファイルを指定して下さい。\\n(".$extension."ファイルの取込は出来ません。)";
            return false;
        }
        //アップロード(PDF)
        $dirname  = DOCUMENTROOT . "/pdf/" . $this->schregno;
        $filename = $dirname . "/" . $setFileName[0] . "_" . date("Ymj_His") . '.' . $extension;
        if (!is_uploaded_file($this->file['tmp_name'])) {
            $this->warning = "ファイル名が不正です。";
        } else if ($this->schregno == "") {
            $this->warning = "学籍番号が不正です。";
        } else {
            //ディレクトリ（学籍番号）か・・・なければ作成
            if (!is_dir($dirname)) {
                if (!mkdir($dirname)) {
                    //作成失敗
                }
            }
            if (move_uploaded_file($this->file['tmp_name'], $filename) == FALSE){
                $this->warning = "取込失敗です。";
            } else {
                $this->message = "取込正常です。";
            }
        }
    }
}
?>
