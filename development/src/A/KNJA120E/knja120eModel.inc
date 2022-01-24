<?php

require_once('for_php7.php');

class knja120eModel extends Model
{
    public $cmd;
    public $exp_year = "";
    public $exp_semester = "";
    public $schregno = "";
    public $name = "";
    public $grade;
    public $field = array();
    public $control;
    public $annual;        //学年(年次)
    public $getPrgId;      //コールされたか
    public $sendAuth;      //親画面の権限
    public $auth;          //権限
    public $mojigyo = array();

    public function knja120eModel()
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

        if (VARS::post("ANNUAL")) {   //学年（年次）
            list($tmp["YEAR"], $tmp["ANNUAL"]) = explode(",", VARS::post("ANNUAL"));
            $this->annual = $tmp;
        }

        $this->field = array("TOTALSTUDYACT"        =>  VARS::post("TOTALSTUDYACT"),
                             "TOTALSTUDYVAL"        =>  VARS::post("TOTALSTUDYVAL"),
                             "SPECIALACTREMARK"     =>  VARS::post("SPECIALACTREMARK"),
                             "TRAIN_REF1"           =>  VARS::post("TRAIN_REF1"),
                             "TRAIN_REF2"           =>  VARS::post("TRAIN_REF2"),
                             "TRAIN_REF3"           =>  VARS::post("TRAIN_REF3"),
                             "TRAIN_REF4"           =>  VARS::post("TRAIN_REF4"),
                             "TRAIN_REF5"           =>  VARS::post("TRAIN_REF5"),
                             "TRAIN_REF6"           =>  VARS::post("TRAIN_REF6"),
                             "TRAIN_REF1_NO_COMM"   =>  VARS::post("TRAIN_REF1_NO_COMM"),
                             "TRAIN_REF2_NO_COMM"   =>  VARS::post("TRAIN_REF2_NO_COMM"),
                             "TRAIN_REF3_NO_COMM"   =>  VARS::post("TRAIN_REF3_NO_COMM"),
                             "TRAIN_REF4_NO_COMM"   =>  VARS::post("TRAIN_REF4_NO_COMM"),
                             "TRAIN_REF5_NO_COMM"   =>  VARS::post("TRAIN_REF5_NO_COMM"),
                             "TRAIN_REF6_NO_COMM"   =>  VARS::post("TRAIN_REF6_NO_COMM"),
                             "ATTENDREC_REMARK"     =>  VARS::post("ATTENDREC_REMARK"),
                             "INS_COMMENTS"         =>  VARS::post("INS_COMMENTS"),      //特記事項なし
                             "NO_COMMENTS_LABEL"    =>  VARS::post("NO_COMMENTS_LABEL"), //特記事項なし
                             "TOTALSTUDYACT_BG_COLOR_FLG"       =>  VARS::post("TOTALSTUDYACT_BG_COLOR_FLG"),
                             "TOTALSTUDYVAL_BG_COLOR_FLG"       =>  VARS::post("TOTALSTUDYVAL_BG_COLOR_FLG"),
                             "SPECIALACTREMARK_BG_COLOR_FLG"    =>  VARS::post("SPECIALACTREMARK_BG_COLOR_FLG"),
                             "TRAIN_REF1_BG_COLOR_FLG"          =>  VARS::post("TRAIN_REF1_BG_COLOR_FLG"),
                             "TRAIN_REF2_BG_COLOR_FLG"          =>  VARS::post("TRAIN_REF2_BG_COLOR_FLG"),
                             "TRAIN_REF3_BG_COLOR_FLG"          =>  VARS::post("TRAIN_REF3_BG_COLOR_FLG"),
                             "ATTENDREC_REMARK_BG_COLOR_FLG"    =>  VARS::post("ATTENDREC_REMARK_BG_COLOR_FLG"),
                             "UPDATED"              =>  VARS::post("UPDATED"),
                             "PATTERN"              =>  VARS::post("PATTERN"),
                             "CATEGORY_SELECTED"    =>  VARS::post("CATEGORY_SELECTED"),
                             "ZIRITUKATUDOU"        =>  VARS::post("ZIRITUKATUDOU")
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
            list($moji, $gyou) = preg_split('/\*/', $this->Properties["seitoSidoYoroku_dat_TotalstudyactSize"]);
            $this->mojigyo["TOTALSTUDYACT"]["moji"] = (int)trim($moji);
            $this->mojigyo["TOTALSTUDYACT"]["gyo"] = (int)trim($gyou);
        } elseif ($this->Properties["seitoSidoYorokuFieldSize"] == 1) {
            $this->mojigyo["TOTALSTUDYACT"]["moji"] = 22;
            $this->mojigyo["TOTALSTUDYACT"]["gyo"] = 8;
        } else {
            $this->mojigyo["TOTALSTUDYACT"]["moji"] = 11;
            $this->mojigyo["TOTALSTUDYACT"]["gyo"] = 4;
        }

        if ($this->Properties["seitoSidoYoroku_dat_TotalstudyvalSize"]) {
            list($moji, $gyou) = preg_split('/\*/', $this->Properties["seitoSidoYoroku_dat_TotalstudyvalSize"]);
            $this->mojigyo["TOTALSTUDYVAL"]["moji"] = (int)trim($moji);
            $this->mojigyo["TOTALSTUDYVAL"]["gyo"] = (int)trim($gyou);
        } elseif ($this->Properties["seitoSidoYorokuFieldSize"] == 1) {
            $this->mojigyo["TOTALSTUDYVAL"]["moji"] = 22;
            $this->mojigyo["TOTALSTUDYVAL"]["gyo"] = 8;
        } else {
            $this->mojigyo["TOTALSTUDYVAL"]["moji"] = 11;
            $this->mojigyo["TOTALSTUDYVAL"]["gyo"] = 6;
        }

        if ($this->Properties["seitoSidoYoroku_dat_Attendrec_RemarkSize"]) {
            list($moji, $gyou) = preg_split('/\*/', $this->Properties["seitoSidoYoroku_dat_Attendrec_RemarkSize"]);
            $this->mojigyo["ATTENDREC_REMARK"]["moji"] = (int)trim($moji);
            $this->mojigyo["ATTENDREC_REMARK"]["gyo"]  = (int)trim($gyou);
        } else {
            $this->mojigyo["ATTENDREC_REMARK"]["moji"] = 20; //デフォルトの値
            $this->mojigyo["ATTENDREC_REMARK"]["gyo"]  = 2;  //デフォルトの値
        }
        if ($this->Properties["seitoSidoYoroku_dat_SpecialactremarkSize"]) {
            list($moji, $gyou) = preg_split('/\*/', $this->Properties["seitoSidoYoroku_dat_SpecialactremarkSize"]);
            $this->mojigyo["SPECIALACTREMARK"]["moji"] = (int)trim($moji);
            $this->mojigyo["SPECIALACTREMARK"]["gyo"]  = (int)trim($gyou);
        } else {
            $this->mojigyo["SPECIALACTREMARK"]["moji"] = 11; //デフォルトの値
            $this->mojigyo["SPECIALACTREMARK"]["gyo"]  = 6;  //デフォルトの値
        }

        for ($i = 1; $i <= 6; $i++) {
            $this->mojigyo["TRAIN_REF".$i]["moji"] = 27;
            $this->mojigyo["TRAIN_REF".$i]["gyo"]  = 8;
        }

        $db = Query::dbCheckOut();
        $z010 = $db->getOne(knja120eQuery::getSchoolName("NAME1"));
        Query::dbCheckIn($db);
        if ($z010 == "shimaneken") {
            // 項目名
            $this->itemname = array(
                "TRAIN_REF1" => "(1)各教科・科目等の学習に関する所見",
                "TRAIN_REF2" => "(2)行動に関する所見・特技等",
                "TRAIN_REF3" => "(3)進路指導に関する事項",
                "TRAIN_REF4" => "(4)取得資格、検定等",
                "TRAIN_REF5" => "(5)部活動、ボランティア活動等",
                "TRAIN_REF6" => "(6)その他"
            );
        } else {
            // 項目名
            $this->itemname = array(
                "TRAIN_REF1" => "(1)学習における特徴等",
                "TRAIN_REF2" => "(2)行動の特徴，特技等",
                "TRAIN_REF3" => "(3)部活動，ボランティア活動，留学・海外経験等",
                "TRAIN_REF4" => "(4)取得資格，検定等",
                "TRAIN_REF5" => "(5)表彰・顕彰等の記録",
                "TRAIN_REF6" => "(6)その他"
            );
        }
        $this->mojigyo["ZIRITUKATUDOU"]["moji"] = 12;
        $this->mojigyo["ZIRITUKATUDOU"]["gyo"]  = 8;
    }

    //入力項目チェック
    public function validateInput()
    {
        if ($this->schregno=="") {
            $this->setWarning("MSG304");
            return false;
        }

        $arr = array("TOTALSTUDYACT" => "活動内容",
                     "TOTALSTUDYVAL" => "評価",
                     "ATTENDREC_REMARK" => "出欠の記録備考",
                     "SPECIALACTREMARK" => "特別活動所見",
                     "ZIRITUKATUDOU" => "自立活動の記録",
                 );
        for ($i = 1; $i <= 6; $i++) {
            $arr["TRAIN_REF".$i] = "指導上参考になる諸事項".$i;
        }
        foreach ($arr as $field => $title) {
            $moji = $this->mojigyo[$field]["moji"];
            $gyo = $this->mojigyo[$field]["gyo"];
            if ($this->validate_row_cnt($this->field[$field], ($moji * 2)) > $gyo) {
                $this->setWarning("MSG915", "(".$title."は{$gyo}行までです)");
                return false;
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

    public function &getUpdateTyousasyoModel()
    {
        if ($this->schregno=="") {
            $this->setWarning("MSG304");
            return;
        }
        if (get_count($this->field['CATEGORY_SELECTED']) != 0) {
            $db = Query::dbCheckOut();
            $db->autoCommit(false);
            $db->query(knja120eQuery::getDeleteTyousasyoQuery($this));
            $db->query(knja120eQuery::getInsertTyousasyoQuery($this));
            $db->commit();
            Query::dbCheckIn($db);
        }
        $this->setMessage("MSG201");
    }

    public function &update()
    {
        knja120eQuery::getInsertQuery($this, $this->field, $this->schregno, $this->exp_year, $this->exp_semester);
        return $result;
    }

    /**
     * 実行モデルを返します
    */
    public function &getExecModel()
    {
        $setFileName = explode(".", $this->file['name']);
        $setFileName[0] = mb_convert_encoding($setFileName[0], "SJIS-win", "UTF-8");
        $extension = $setFileName[count($setFileName) - 1];
        //echo "cnt = " . count($setFileName) . "<BR>";
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
        } elseif ($this->schregno == "") {
            $this->warning = "学籍番号が不正です。";
        } else {
            //ディレクトリ（学籍番号）か・・・なければ作成
            if (!is_dir($dirname)) {
                if (!mkdir($dirname)) {
                    //作成失敗
                }
            }
            if (move_uploaded_file($this->file['tmp_name'], $filename) == false) {
                $this->warning = "取込失敗です。";
            } else {
                $this->message = "取込正常です。";
            }
        }
    }
}
