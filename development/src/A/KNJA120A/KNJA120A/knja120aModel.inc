<?php

require_once('for_php7.php');

class knja120aModel extends Model {

    var $cmd;
    var $exp_year = "";
    var $exp_semester = "";
    var $schregno = "";
    var $name = "";
    var $field = array();
    var $control;
    var $annual;        //学年(年次)
    var $getPrgId;      //コールされたか
    var $sendAuth;      //親画面の権限
    var $auth;          //権限


    function knja120aModel()
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
                             "TOTALREMARK"          =>  VARS::post("TOTALREMARK"),
                             "ATTENDREC_REMARK"     =>  VARS::post("ATTENDREC_REMARK"),
                             "REMARK1_003"          =>  VARS::post("REMARK1_003"),
                             "REMARK2_003"          =>  VARS::post("REMARK2_003"),
                             "REMARK1_005"          =>  VARS::post("REMARK1_005"),
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
        if (VARS::get("GRADE")!="") {
            $this->grade      = VARS::get("GRADE");
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
        if ($this->Properties["seitoSidoYoroku_dat_TotalremarkSize"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["seitoSidoYoroku_dat_TotalremarkSize"]);
            $this->totalremark_moji = (int)trim($moji);
            $this->totalremark_gyou = (int)trim($gyou);
        } else {
            $this->totalremark_moji = 44; //デフォルトの値
            $this->totalremark_gyou = 6;  //デフォルトの値
        }
        if ($this->Properties["seitoSidoYoroku_dat_SpecialactremarkSize"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["seitoSidoYoroku_dat_SpecialactremarkSize"]);
            $this->specialactremark_moji = (int)trim($moji);
            $this->specialactremark_gyou = (int)trim($gyou);
        } else {
            $this->specialactremark_moji = 11; //デフォルトの値
            $this->specialactremark_gyou = 6;  //デフォルトの値
        }
        if ($this->Properties["seitoSidoYoroku_Totalstudyact2_val2_UseTextFlg"]) {
            if ($this->Properties["seitoSidoYoroku_dat_Totalstudyact2Size"]) {
                list($moji, $gyou) = preg_split("/\*/", $this->Properties["seitoSidoYoroku_dat_Totalstudyact2Size"]);
                $this->remark1_003_moji = (int)trim($moji);
                $this->remark1_003_gyou = (int)trim($gyou);
            } else {
                $this->remark1_003_moji = 44;   //デフォルトの値
                $this->remark1_003_gyou = 4;    //デフォルトの値
            }
            if ($this->Properties["seitoSidoYoroku_dat_Totalstudyval2Size"]) {
                list($moji, $gyou) = preg_split("/\*/", $this->Properties["seitoSidoYoroku_dat_Totalstudyval2Size"]);
                $this->remark2_003_moji = (int)trim($moji);
                $this->remark2_003_gyou = (int)trim($gyou);
            } else {
                $this->remark2_003_moji = 44;   //デフォルトの値
                $this->remark2_003_gyou = 6;    //デフォルトの値
            }
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

        if ($this->Properties["seitoSidoYoroku_dat_SpecialactremarkSize"]) {
            if ($this->validate_row_cnt($this->field["SPECIALACTREMARK"], ((int)$this->specialactremark_moji * 2)) > $this->specialactremark_gyou) {
                $this->setWarning("MSG915","(特別活動所見は{$this->specialactremark_gyou}行までです)");
                return false;
            }
        } else if ($this->Properties["seitoSidoYorokuSpecialactremarkFieldSize"] == 1) {
            if ($this->validate_row_cnt($this->field["SPECIALACTREMARK"], (22 * 2)) > 10) {
                $this->setWarning("MSG915","(特別活動所見は10行までです)");
                return false;
            }
        } else {
            if ($this->validate_row_cnt($this->field["SPECIALACTREMARK"], (11 * 2)) > 6) {
                $this->setWarning("MSG915","(特別活動所見は6行までです)");
                return false;
            }
        }

        if ($this->Properties["seitoSidoYoroku_dat_TotalremarkSize"]) {
            if ($this->validate_row_cnt($this->field["TOTALREMARK"], ((int)$this->totalremark_moji * 2)) > $this->totalremark_gyou) {
                $this->setWarning("MSG915","(総合所見は{$this->totalremark_gyou}行までです)");
                return false;
            }
        } else if ($this->Properties["seitoSidoYorokuSougouFieldSize"] == 1) {
            if ($this->validate_row_cnt($this->field["TOTALREMARK"], (66 * 2)) > 8) {
                $this->setWarning("MSG915","(総合所見は8行までです)");
                return false;
            }
        } elseif ($this->Properties["seitoSidoYorokuFieldSize"] == 1) {
            if ($this->validate_row_cnt($this->field["TOTALREMARK"], (66 * 2)) > 7) {
                $this->setWarning("MSG915","(総合所見は7行までです)");
                return false;
            }
        } else {
            if ($this->validate_row_cnt($this->field["TOTALREMARK"], (44 * 2)) > 6) {
                $this->setWarning("MSG915","(総合所見は6行までです)");
                return false;
            }
        }

        if ($this->Properties["seitoSidoYoroku_Totalstudyact2_val2_UseTextFlg"]) {
            $itemName = knja120aQuery::getItemName($model);
            $itemName = ($itemName == "") ? 'キャリアプラン' : $itemName;
            if ($this->validate_row_cnt($this->field["REMARK1_003"], ((int)$this->remark1_003_moji * 2)) > $this->remark1_003_gyou) {
                $this->setWarning("MSG915","({$itemName}・活動内容は{$this->remark1_003_gyou}行までです)");
                return false;
            }
            if ($this->validate_row_cnt($this->field["REMARK2_003"], ((int)$this->remark2_003_moji * 2)) > $this->remark2_003_gyou) {
                $this->setWarning("MSG915","({$itemName}・評価は{$this->remark2_003_gyou}行までです)");
                return false;
            }
        }

        if (knja120aQuery::getNaraHantei() > 0) {
            if ($this->validate_row_cnt($this->field["REMARK1_005"], (18 * 2)) > 6) {
                    $this->setWarning("MSG915","(奈良Timeは6行までです)");
                    return false;
            }
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
        knja120aQuery::getDeleteQuery($this->schregno, $this->exp_year);
        knja120aQuery::getInsertQuery($this->field,$this->schregno, $this->exp_year, $this->exp_semester);
        if ($this->Properties["seitoSidoYoroku_Totalstudyact2_val2_UseTextFlg"]) {
            knja120aQuery::getInsertQueryDetail($this->field, $this->schregno, $this->exp_year, $this);
        }
        if (knja120aQuery::getNaraHantei() > 0) {
            knja120aQuery::getInsertQueryDetail2($this->field, $this->schregno, $this->exp_year, $this);
        }

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
