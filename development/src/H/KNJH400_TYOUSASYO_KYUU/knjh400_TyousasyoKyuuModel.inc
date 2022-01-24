<?php

require_once('for_php7.php');
class knjh400_TyousasyoKyuuModel extends Model
{
    public $cmd;
    public $schregno;      //学籍番号
    public $field;         //画面上のデータ
    public $field2;        //画面上のデータ
    public $grade;         //学年
    public $hrClass;       //クラス
    public $annual;        //学年(年次)
    public $control;
    public $mode;          //在籍卒業モード
    public $grd_year;      //卒業年度
    public $grd_semester;  //卒業学期
    public $exp_year;      //左フレームから送られてくる年度
    public $exp_semester;  //左フレームから送られてくる学期
    public $schoolName;
    public $schoolCode;
    public $attendrec_remark_moji; //出校の記録備考の文字数
    public $attendrec_remark_gyou; //出校の記録備考の行数

    public $totalstudyact_moji;    //活動内容の文字数
    public $totalstudyact_gyou;    //活動内容の行数

    public $totalstudyval_moji;    //評価の文字数
    public $totalstudyval_gyou;    //評価の行数

    public $sido_totalstudyact_moji;    //（指導要録）活動内容の文字数
    public $sido_totalstudyact_gyou;    //（指導要録）活動内容の行数
    public $sido_totalstudyval_moji;    //（指導要録）評価の文字数
    public $sido_totalstudyval_gyou;    //（指導要録）評価の行数

    public $remark_moji;    //備考の文字数
    public $remark_gyou;    //備考の行数
    public $remark2_gyou;   //備考2の行数

    public $no_comments;           //チェック（特記事項なし）
    public $no_comments_label;     //文言（特記事項なし）

    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限

    public $school_kind;
    public $entDiv;
    public $entYear;
    public $addYear;
    public $addYearGrade;
    public $selectdata;
    public $selectdataText;

    public $gradeHrClass;

    public function knjh400_TyousasyoKyuuModel()
    {
        $this->year = CTRL_YEAR;
        $this->mode = "ungrd";      //在籍モード
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

        $this->cmd = VARS::request("cmd");

        $this->schregno = VARS::request("SCHREGNO") != "" ? VARS::request("SCHREGNO") : $this->schregno;

        $this->year = VARS::request("YEAR") != "" ? VARS::request("YEAR") : $this->year;
        $this->exp_year = $this->year;

        $this->semester = VARS::request("SEMESTER") != "" ? VARS::request("SEMESTER") : $this->semester;
        $this->exp_semester = $this->semester;

        if (VARS::get("NAME")) {
            $this->name = VARS::get("NAME");         //生徒氏名
        }
        if (VARS::get("GRADE")) {
            $this->grade = VARS::get("GRADE");       //学年
            $this->grade = substr($this->grade, 0, 2);
        }
        if (VARS::get("HR_CLASS")) {
            $this->hrClass = VARS::get("HR_CLASS");    //クラス
        }

        $this->gradeHrClass = $this->grade."-".$this->hrClass;

        //在籍卒業モード
        if (VARS::get("mode")) {
            $this->mode = VARS::get("mode");         //在籍卒業モード
        }
        if (VARS::get("GRD_YEAR")) {
            $this->grd_year = VARS::get("GRD_YEAR"); //卒業年度
        }
        if (VARS::get("GRD_SEMESTER")) {
            $this->grd_semester = VARS::get("GRD_SEMESTER"); //卒業学期
        }

        $this->field = array("DISEASE"                  => VARS::post("DISEASE"),                   //疾患および異常
                             "DOC_REMARK"               => VARS::post("DOC_REMARK"),                //担当学校医所見
                             "TR_REMARK"                => VARS::post("TR_REMARK"),                 //担任所見
                             "TOTALSTUDYACT"            => VARS::post("TOTALSTUDYACT"),             //活動内容
                             "TOTALSTUDYACT_SLASH_FLG"  => VARS::post("TOTALSTUDYACT_SLASH_FLG"),   //活動内容・斜線を入れる
                             "TOTALSTUDYVAL"            => VARS::post("TOTALSTUDYVAL"),             //評価
                             "TOTALSTUDYVAL_SLASH_FLG"  => VARS::post("TOTALSTUDYVAL_SLASH_FLG"),   //評価・斜線を入れる
                             "REMARK"                   => VARS::post("REMARK"),                    //備考
                             "REMARK2"                  => VARS::post("REMARK2"),                   //備考2
                             "COMMENTEX_A_CD"           => VARS::post("COMMENTEX_A_CD")             //学習成績概評
                             );

        $this->field2 = array("SPECIALACTREC"           => VARS::post("SPECIALACTREC"),             //特別活動の記録
                              "TOTALREMARK"             => VARS::post("TOTALREMARK"),               //指導要録
                              "TRAIN_REF"               => VARS::post("TRAIN_REF"),                 //指導上参考になる諸事項
                              "TRAIN_REF1"              => VARS::post("TRAIN_REF1"),                //指導上参考になる諸事項1
                              "TRAIN_REF2"              => VARS::post("TRAIN_REF2"),                //指導上参考になる諸事項2
                              "TRAIN_REF3"              => VARS::post("TRAIN_REF3"),                //指導上参考になる諸事項3
                              "TOTALSTUDYACT"           => VARS::post("TOTALSTUDYACT"),             //活動内容
                              "TOTALSTUDYACT_SLASH_FLG" => VARS::post("TOTALSTUDYACT_SLASH_FLG"),   //活動内容・斜線を入れる
                              "TOTALSTUDYVAL"           => VARS::post("TOTALSTUDYVAL"),             //評価
                              "TOTALSTUDYVAL_SLASH_FLG" => VARS::post("TOTALSTUDYVAL_SLASH_FLG"),   //評価・斜線を入れる
                              "ATTENDREC_REMARK"        => VARS::post("ATTENDREC_REMARK"),          //出校の記録備考
                              "ATTENDREC_REMARK_SLASH_FLG" => VARS::post("ATTENDREC_REMARK_SLASH_FLG"),  //出校の記録備考・斜線を入れる
                              "INS_COMMENTS"            => VARS::post("INS_COMMENTS"),              //特記事項なし
                              "INS_COMMENTS_LABEL"      => VARS::post("INS_COMMENTS_LABEL")         //特記事項なし
                              );

        //学年（年次）
        if (VARS::post("ANNUAL")) {
            list($tmp["YEAR"], $tmp["ANNUAL"]) = explode(",", VARS::post("ANNUAL"));
            $this->annual = $tmp;
        }

        $this->addYear          = VARS::post("ADD_YEAR");
        $this->addYearGrade     = VARS::post("ADD_YEAR_GRADE");
        $this->selectdata       = VARS::post("selectdata");
        $this->selectdataText   = VARS::post("selectdataText");

        //生徒一覧を更新
        if (VARS::get("init")) {
            unset($this->schregno); //学籍番号
            unset($this->name);     //生徒氏名
        }

        //学校名取得
        $query = knjh400_TyousasyoKyuuQuery::getSchoolName();
        $db = Query::dbCheckOut();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $this->schoolName = $rowZ010["NAME1"];
        $this->schoolCode = $rowZ010["NAME2"];
        //校種取得
        $this->school_kind  = $db->getOne(knjh400_TyousasyoKyuuQuery::getSchoolKind($this));
        //入学区分取得
        $this->entDiv       = $db->getOne(knjh400_TyousasyoKyuuQuery::getSchregBase($this, "ENT_DIV"));
        //入学年度取得
        $this->entYear      = $db->getOne(knjh400_TyousasyoKyuuQuery::getSchregBase($this, "ENT_DATE", "year"));
        Query::dbCheckIn($db);

        //「特記事項なし」チェックボックス
        $this->no_comments_label = "特記事項なし";
        $this->no_comments = VARS::post("NO_COMMENTS");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        //固定
        $this->Properties["useSyojikou3"] = "1";        //調査書：指導上参考データ3分割
        $this->Properties["sidouyourokuSansyou"] = ""; //1 ⇒ E011 それ以外は E010

        //活動内容（文字数・行数）
        if ($this->Properties["seitoSidoYoroku_dat_TotalstudyactSize"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["seitoSidoYoroku_dat_TotalstudyactSize"]);
            $this->sido_totalstudyact_moji = (int)trim($moji);
            $this->sido_totalstudyact_gyou = (int)trim($gyou);
        } else {
            if ($this->Properties["seitoSidoYorokuFieldSize"] == 1) {
                $this->sido_totalstudyact_moji = 22;
                $this->sido_totalstudyact_gyou = 8;
            } else {
                $this->sido_totalstudyact_moji = 11; //デフォルトの値
                $this->sido_totalstudyact_gyou = 5;  //デフォルトの値
            }
        }
        //評価（文字数・行数）
        if ($this->Properties["seitoSidoYoroku_dat_TotalstudyvalSize"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["seitoSidoYoroku_dat_TotalstudyvalSize"]);
            $this->sido_totalstudyval_moji = (int)trim($moji);
            $this->sido_totalstudyval_gyou = (int)trim($gyou);
        } else {
            if ($this->Properties["seitoSidoYorokuFieldSize"] == 1) {
                $this->sido_totalstudyval_moji = 22;
                $this->sido_totalstudyval_gyou = 8;
            } else {
                $this->sido_totalstudyval_moji = 11; //デフォルトの値
                $this->sido_totalstudyval_gyou = 6;  //デフォルトの値
            }
        }

        $this->getFieldSize();

        if ($this->Properties["useTitleShukkou"] == "1") {
            $this->attendTitle = "出校";
        } else {
            $this->attendTitle = "出欠";
        }
    }

    public function &getProgrampathModel()
    {
        $db = Query::dbCheckOut();
        $programpath = $db->getOne(knjh400_TyousasyoKyuuQuery::getProgrampath());
        Query::dbCheckIn($db);

        return $programpath;
    }

    //入力項目チェック
    public function validateInput()
    {
        if (!isset($this->schregno)) {
            return false;
        }


        if ($this->validate_row_cnt($this->field["REMARK"], ($this->remark_moji * 2)) > $this->remark_gyou) {
            $this->setWarning("MSG915", "(備考は{$this->remark_gyou}行までです)");
            return false;
        }

        if ($this->validate_row_cnt($this->field["REMARK2"], ($this->remark_moji * 2)) > $this->remark2_gyou) {
            $this->setWarning("MSG915", "(備考2は{$this->remark2_gyou}行までです)");
            return false;
        }

        if ($this->validate_row_cnt($this->field2["SPECIALACTREC"], ($this->specialactrec_moji * 2)) > $this->specialactrec_gyou) {
            $this->setWarning("MSG915", "(特別活動の記録は{$this->specialactrec_gyou}行までです)");
            return false;
        }

        if ($this->Properties["useSyojikou3"] != "1") {
            if ($this->Properties["tyousasyoTokuBetuFieldSize"] == 1) {
                if ($this->validate_row_cnt($this->field2["TRAIN_REF"], (58 * 2)) > 7) {
                    $this->setWarning("MSG915", "(指導上参考になる諸事項は7行までです)");
                    return false;
                }
            } else {
                if ($this->validate_row_cnt($this->field2["TRAIN_REF"], (41 * 2)) > 5) {
                    $this->setWarning("MSG915", "(指導上参考になる諸事項は5行までです)");
                    return false;
                }
            }
        } else {
            if ($this->validate_row_cnt($this->field2["TRAIN_REF1"], ($this->train_ref1_moji * 2)) > $this->train_ref1_gyou) {
                $this->setWarning("MSG915", "(指導上参考になる諸事項1は{$this->train_ref1_gyou}行までです)");
                return false;
            } elseif ($this->validate_row_cnt($this->field2["TRAIN_REF2"], ($this->train_ref2_moji * 2)) > $this->train_ref2_gyou) {
                $this->setWarning("MSG915", "(指導上参考になる諸事項2は{$this->train_ref2_gyou}行までです)");
                return false;
            } elseif ($this->validate_row_cnt($this->field2["TRAIN_REF3"], ($this->train_ref3_moji * 2)) > $this->train_ref3_gyou) {
                $this->setWarning("MSG915", "(指導上参考になる諸事項3は{$this->train_ref3_gyou}行までです)");
                return false;
            }
        }

        //出校の記録備考
        if ($this->validate_row_cnt($this->field2["ATTENDREC_REMARK"], ($this->attendrec_remark_moji * 2)) > $this->attendrec_remark_gyou) {
            $this->setWarning("MSG915", "(".$this->attendTitle."の記録備考は{$this->attendrec_remark_gyou}行までです)");
            return false;
        }

        //活動内容
        if ($this->validate_row_cnt($this->field2["TOTALSTUDYACT"], ($this->totalstudyact_moji * 2)) > $this->totalstudyact_gyou) {
            $this->setWarning("MSG915", "(総合学習...活動内容は{$this->totalstudyact_gyou}行までです)");
            return false;
        }

        //評価
        if ($this->validate_row_cnt($this->field2["TOTALSTUDYVAL"], ($this->totalstudyval_moji * 2)) > $this->totalstudyval_gyou) {
            $this->setWarning("MSG915", "(総合学習...評価は{$this->totalstudyval_gyou}行までです)");
            return false;
        }

        return true;
    }

    //保健より読込みのモデルを返します
    public function &getReloadHealthModel()
    {
        $db = Query::dbCheckOut();

        $query = knjh400_TyousasyoKyuuQuery::selectQueryHealth($this);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if (is_array($row)) {
            echo "<script language=\"javascript\">\n";
            if ($row["OTHERDISEASECD"] != "") {
                echo "top.main_frame.right_frame.document.forms[0].DISEASE.value = '" .addslashes($row["OTHERDISEASECD"]) ."';\n";
            }
            if ($row["DOC_REMARK"] != "") {
                echo "top.main_frame.right_frame.document.forms[0].DOC_REMARK.value = '" .addslashes($row["DOC_REMARK"]) ."';\n";
            }
            echo "</script>\n";
        }

        Query::dbCheckIn($db);
    }

    public function getFieldSize()
    {
        //出校の記録備考の文字数と行数の設定
        if ($this->Properties["tyousasyoAttendrecRemarkFieldSize"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["tyousasyoAttendrecRemarkFieldSize"]);
            $this->attendrec_remark_moji = (int)trim($moji);
            $this->attendrec_remark_gyou = (int)trim($gyou);
        } else {
            $this->attendrec_remark_moji = 40; //デフォルトの値
            $this->attendrec_remark_gyou = 1; //デフォルトの値
        }
        //活動内容の文字数と行数の設定
        if ($this->Properties["tyousasyoTotalstudyactFieldSize"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["tyousasyoTotalstudyactFieldSize"]);
            $this->totalstudyact_moji = (int)trim($moji);
            $this->totalstudyact_gyou = (int)trim($gyou);
        } else {
            if ($this->Properties["tyousasyoSougouHyoukaNentani"] == 1) {
                $this->totalstudyact_moji = 44; //デフォルトの値
                $this->totalstudyact_gyou = 1; //デフォルトの値
            } else {
                $this->totalstudyact_moji = 41; //デフォルトの値
                $this->totalstudyact_gyou = 2; //デフォルトの値
            }
        }
        //評価の文字数と行数の設定
        if ($this->Properties["tyousasyoTotalstudyvalFieldSize"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["tyousasyoTotalstudyvalFieldSize"]);
            $this->totalstudyval_moji = (int)trim($moji);
            $this->totalstudyval_gyou = (int)trim($gyou);
        } else {
            if ($this->Properties["tyousasyoSougouHyoukaNentani"] == 1) {
                $this->totalstudyval_moji = 44; //デフォルトの値
                $this->totalstudyval_gyou = 2; //デフォルトの値
            } else {
                $this->totalstudyval_moji = 41; //デフォルトの値
                $this->totalstudyval_gyou = 3; //デフォルトの値
            }
        }

        if ($this->Properties["train_ref_1_2_3_field_size"] == 1) {
            $this->train_ref1_moji = 14;
            $this->train_ref2_moji = 21;
            $this->train_ref3_moji = 7;
        } elseif ($this->Properties["train_ref_1_2_3_field_size"] == 2) {
            $this->train_ref1_moji = 21;
            $this->train_ref2_moji = 21;
            $this->train_ref3_moji = 7;
        } elseif (substr_count($this->Properties["train_ref_1_2_3_field_size"], '-') == "2") {
            $train_ref_array = array();
            $train_ref_array = explode('-', $this->Properties["train_ref_1_2_3_field_size"]);
            $this->train_ref1_moji = $train_ref_array[0];
            $this->train_ref2_moji = $train_ref_array[1];
            $this->train_ref3_moji = $train_ref_array[2];
        } else {
            $this->train_ref1_moji = 24;
            $this->train_ref2_moji = 18;
            $this->train_ref3_moji = 18;
        }

        if ($this->Properties["train_ref_1_2_3_gyo_size"]) {
            $this->train_ref1_gyou = $this->Properties["train_ref_1_2_3_gyo_size"];
            $this->train_ref2_gyou = $this->Properties["train_ref_1_2_3_gyo_size"];
            $this->train_ref3_gyou = $this->Properties["train_ref_1_2_3_gyo_size"];
        } else {
            $this->train_ref1_gyou = 6;
            $this->train_ref2_gyou = 6;
            $this->train_ref3_gyou = 6;
        }

        //特別活動の記録の文字数と行数の設定
        if ($this->Properties["tyousasyoSpecialactrecFieldSize"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["tyousasyoSpecialactrecFieldSize"]);
            $this->specialactrec_moji = (int)trim($moji);
            $this->specialactrec_gyou = (int)trim($gyou);
        } else {
            if ($this->Properties["tyousasyoTokuBetuFieldSize"] == 1) {
                $this->specialactrec_moji = 15; //デフォルトの値
                $this->specialactrec_gyou = 10; //デフォルトの値
            } else {
                $this->specialactrec_moji = 11; //デフォルトの値
                $this->specialactrec_gyou = 6;  //デフォルトの値
            }
        }

        //備考の文字数と行数の設定
        if ($this->Properties["tyousasyoRemarkFieldSize"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["tyousasyoRemarkFieldSize"]);
            $this->remark_moji = (int)trim($moji);
            $this->remark_gyou = (int)trim($gyou);
        } else {
            $this->remark_moji = 41; //デフォルトの値
            $this->remark_gyou = 5;  //デフォルトの値
        }
        //備考2の行数の設定
        $this->remark2_gyou = 3;  //3行固定
    }
}
