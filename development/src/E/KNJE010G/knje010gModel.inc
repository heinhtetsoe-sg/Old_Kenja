<?php

require_once("for_php7.php");

class knje010gModel extends Model
{
    public $cmd;
    public $schregno;      //学籍番号
    public $field;         //画面上のデータ
    public $field2;        //画面上のデータ
    public $grade;         //学年
    public $annual;        //学年(年次)
    public $control;
    public $mode;          //在籍卒業モード
    public $grd_year;      //卒業年度
    public $grd_semester;  //卒業学期
    public $exp_year;      //左フレームから送られてくる年度
    public $exp_semester;  //左フレームから送られてくる学期
    public $schArray = array();
    public $readYear;
    public $schoolKind;
    public $schoolName;
    public $schoolCode;

    public $mojigyou = array();
    public $itemname;

    public $no_comments;           //チェック（特記事項なし）
    public $no_comments_label;     //文言（特記事項なし）

    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限

    public function knje010gModel()
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

        $this->cmd     = VARS::request("cmd");
        //次の生徒
        $this->nextURL = VARS::post("nextURL");

        //左フレームから送られてくる年度
        if (VARS::get("EXP_YEAR")) {
            $this->exp_year = VARS::get("EXP_YEAR");
        }
        //左フレームから送られてくる学期
        if (VARS::get("EXP_SEMESTER")) {
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }

        //表示科目
        if (VARS::get("SCHREGNO")) {
            $this->schregno = VARS::get("SCHREGNO"); //学籍番号
            unset($this->readYear);
        } else {
            $this->readYear = VARS::post("READ_YEAR");
        }
        if (VARS::get("NAME")) {
            $this->name = VARS::get("NAME");         //生徒氏名
        }
        if (VARS::get("GRADE")) {
            $this->grade = VARS::get("GRADE");       //学年
        }
        if (VARS::get("HR_CLASS")) {
            $this->hrClass = VARS::get("HR_CLASS");
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
                             "COMMENTEX_A_CD"           => VARS::post("COMMENTEX_A_CD")             //学習成績概評
                             );
        foreach ($this->schArray as $key => $val) {
            $year = $val["YEAR"];
            $this->field2[$year] = array("SPECIALACTREC"            => VARS::post("SPECIALACTREC".$year),               //特別活動の記録
                                         "TRAIN_REF"                => VARS::post("TRAIN_REF1-".$year),                 //指導上参考となる諸事項1
                                         "TRAIN_REF1"               => VARS::post("TRAIN_REF1-".$year),                 //指導上参考となる諸事項1
                                         "TRAIN_REF2"               => VARS::post("TRAIN_REF2-".$year),                 //指導上参考となる諸事項2
                                         "TRAIN_REF3"               => VARS::post("TRAIN_REF3-".$year),                 //指導上参考となる諸事項3
                                         "TRAIN_REF4"               => VARS::post("TRAIN_REF4-".$year),                 //指導上参考となる諸事項4
                                         "TRAIN_REF5"               => VARS::post("TRAIN_REF5-".$year),                 //指導上参考となる諸事項5
                                         "TRAIN_REF6"               => VARS::post("TRAIN_REF6-".$year),                 //指導上参考となる諸事項6
                                         "TRAIN_REF1_NO_COMM"       => VARS::post("TRAIN_REF1-".$year."_NO_COMM"),      //指導上参考となる諸事項1 特記事項無し
                                         "TRAIN_REF2_NO_COMM"       => VARS::post("TRAIN_REF2-".$year."_NO_COMM"),      //指導上参考となる諸事項2 特記事項無し
                                         "TRAIN_REF3_NO_COMM"       => VARS::post("TRAIN_REF3-".$year."_NO_COMM"),      //指導上参考となる諸事項3 特記事項無し
                                         "TRAIN_REF4_NO_COMM"       => VARS::post("TRAIN_REF4-".$year."_NO_COMM"),      //指導上参考となる諸事項4 特記事項無し
                                         "TRAIN_REF5_NO_COMM"       => VARS::post("TRAIN_REF5-".$year."_NO_COMM"),      //指導上参考となる諸事項5 特記事項無し
                                         "TRAIN_REF6_NO_COMM"       => VARS::post("TRAIN_REF6-".$year."_NO_COMM"),      //指導上参考となる諸事項6 特記事項無し
                                         "TOTALSTUDYACT"            => VARS::post("TOTALSTUDYACT-".$year),              //活動内容
                                         "TOTALSTUDYACT_SLASH_FLG"  => VARS::post("TOTALSTUDYACT_SLASH_FLG-".$year),    //活動内容・斜線を入れる
                                         "TOTALSTUDYVAL"            => VARS::post("TOTALSTUDYVAL-".$year),              //評価
                                         "TOTALSTUDYVAL_SLASH_FLG"  => VARS::post("TOTALSTUDYVAL_SLASH_FLG-".$year),    //評価・斜線を入れる
                                         "ATTENDREC_REMARK"         => VARS::post("ATTENDREC_REMARK-".$year),           //出欠の記録備考
                                         "ATTENDREC_REMARK_SLASH_FLG" => VARS::post("ATTENDREC_REMARK_SLASH_FLG-".$year),  //出欠の記録備考・斜線を入れる
                                         "INS_COMMENTS"             => VARS::post("INS_COMMENTS-".$year),               //特記事項なし
                                         "INS_COMMENTS_LABEL"       => VARS::post("INS_COMMENTS_LABEL-".$year)          //特記事項なし
                                         );
        }

        //学年(年次)
        if (VARS::post("ANNUAL")) {
            list($tmp["YEAR"], $tmp["ANNUAL"]) = explode(",", VARS::post("ANNUAL"));
            $this->annual = $tmp;
        }

        //生徒一覧を更新
        if (VARS::get("init")) {
            unset($this->schregno); //学籍番号
            unset($this->name);     //生徒氏名
        }

        //学校名取得
        $query = knje010gQuery::getSchoolName();
        $db = Query::dbCheckOut();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $this->schoolName = $rowZ010["NAME1"];
        $this->schoolCode = $rowZ010["NAME2"];
        Query::dbCheckIn($db);

        //「特記事項なし」チェックボックス
        if ($this->schoolName == "naraken") {
            $this->no_comments_label = "特記事項なし。";
        } else {
            $this->no_comments_label = "特記事項なし";
        }
        $this->remark_no_comments = VARS::post("REMARK_NO_COMMENTS");

        // 取込みの対象フィールド
        $this->target = VARS::get("TARGET");
        $this->targetYear = VARS::get("TARGET_YEAR");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->Properties["tyousasyo2020"] = "1";

        $this->getFieldSize();

        if ($this->Properties["useTitleShukkou"] == "1") {
            $this->attendTitle = "出校";
        } else {
            $this->attendTitle = "出欠";
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        if (!isset($this->schregno)) {
            return false;
        }

        $chkarr = array();
        $chkarr[] = array("FIELD" => "ATTENDREC_REMARK", "NAME" => $this->attendTitle."の記録備考");
        $chkarr[] = array("FIELD" => "SPECIALACTREC", "NAME" => "特別活動の記録");
        for ($i = 1; $i <= 6; $i++) {
            $chkarr[] = array("FIELD" => "TRAIN_REF".$i, "NAME" => "指導上参考となる諸事項 ".$this->itemname["TRAIN_REF".$i]);
        }

        foreach ($this->schArray as $key => $val) {
            $year = $val["YEAR"];

            foreach ($chkarr as $chk) {
                $field = $chk["FIELD"];
                $name = $chk["NAME"]."(".$year."年度)";

                if ($this->validate_row_cnt($this->field2[$year][$field], ($this->mojigyou[$field]["moji"] * 2)) > $this->mojigyou[$field]["gyou"]) {
                    $this->setWarning("MSG915", "({$name}は{$this->mojigyou[$field]["gyou"]}行までです)");
                    return false;
                }
                if ($this->validateKinsoku($this->field2[$year][$field])) {
                    $this->setWarning("MSG916", "({$name}はスペースにつづいて長音（ー）を含んでいます。)");
                    return false;
                }
            }
        }

        $chkarr = array();
        $chkarr[] = array("FIELD" => "TOTALSTUDYACT", "NAME" => "総合学習 活動内容");
        $chkarr[] = array("FIELD" => "TOTALSTUDYVAL", "NAME" => "総合学習 評価");
        $chkarr[] = array("FIELD" => "REMARK",        "NAME" => "備考");

        foreach ($chkarr as $chk) {
            $field = $chk["FIELD"];
            $name = $chk["NAME"];
            if ($this->validate_row_cnt($this->field[$field], ($this->mojigyou[$field]["moji"] * 2)) > $this->mojigyou[$field]["gyou"]) {
                $this->setWarning("MSG915", "({$name}は{$this->mojigyou[$field]["gyou"]}行までです)");
                return false;
            }
            if ($this->validateKinsoku($this->field[$field])) {
                $this->setWarning("MSG916", "({$name}はスペースにつづいて長音（ー）を含んでいます。)");
                return false;
            }
        }

        return true;
    }

    public function validateKinsoku($str)
    {
        if ($this->Properties["tyousasyoKinsokuForm"] == '1') {
            if (false !== strpos($str, '　ー') || false !== strpos($str, ' ー')) {
                return true;
            }
        }
        return false;
    }

    //編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $db = Query::dbCheckOut();
            $db->autoCommit(false);
            if ($this->cmd == "update") {
                //更新処理
                knje010gQuery::update($db, $this);

                //更新処理
                foreach ($this->schArray as $key => $val) {
                    knje010gQuery::update2($db, $this, $val);
                }
            }
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
    }

    //HR一括諸事項取込みのモデルを返します
    public function &getUpdateHrShojikouModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        //更新処理
        knje010gQuery::updateHrShojikou($db, $this);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        $this->setMessage("MSG201");
    }

    public function getFieldSize()
    {
        $this->mojigyou = array();

        //活動内容（文字数・行数）
        if ($this->Properties["seitoSidoYoroku_dat_TotalstudyactSize"]) {
            list($moji, $gyou) = preg_split('/\*/', $this->Properties["seitoSidoYoroku_dat_TotalstudyactSize"]);
            $this->mojigyou["SIDO_TOTALSTUDYACT"]["moji"] = (int)trim($moji);
            $this->mojigyou["SIDO_TOTALSTUDYACT"]["gyou"] = (int)trim($gyou);
        } else {
            if ($this->Properties["seitoSidoYorokuFieldSize"] == 1) {
                $this->mojigyou["SIDO_TOTALSTUDYACT"]["moji"] = 22;
                $this->mojigyou["SIDO_TOTALSTUDYACT"]["gyou"] = 8;
            } else {
                $this->mojigyou["SIDO_TOTALSTUDYACT"]["moji"] = 11; //デフォルトの値
                $this->mojigyou["SIDO_TOTALSTUDYACT"]["gyou"] = 5;  //デフォルトの値
            }
        }
        //評価（文字数・行数）
        if ($this->Properties["seitoSidoYoroku_dat_TotalstudyvalSize"]) {
            list($moji, $gyou) = preg_split('/\*/', $this->Properties["seitoSidoYoroku_dat_TotalstudyvalSize"]);
            $this->mojigyou["SIDO_TOTALSTUDYVAL"]["moji"] = (int)trim($moji);
            $this->mojigyou["SIDO_TOTALSTUDYVAL"]["gyou"] = (int)trim($gyou);
        } else {
            if ($this->Properties["seitoSidoYorokuFieldSize"] == 1) {
                $this->mojigyou["SIDO_TOTALSTUDYVAL"]["moji"] = 22;
                $this->mojigyou["SIDO_TOTALSTUDYVAL"]["gyou"] = 8;
            } else {
                $this->mojigyou["SIDO_TOTALSTUDYVAL"]["moji"] = 11; //デフォルトの値
                $this->mojigyou["SIDO_TOTALSTUDYVAL"]["gyou"] = 6;  //デフォルトの値
            }
        }

        //出欠の記録備考の文字数と行数の設定
        $this->mojigyou["ATTENDREC_REMARK"]["moji"] = 8; //デフォルトの値
        $this->mojigyou["ATTENDREC_REMARK"]["gyou"] = 7; //デフォルトの値

        //活動内容の文字数と行数の設定
        $gyou = $this->propGyo($this->Properties["tyousasyo2020GTotalstudyactGyou"], 8);
        $this->Properties["tyousasyoTotalstudyactFieldSize"] = "48 * {$gyou}";
        list($moji, $gyou) = preg_split('/\*/', $this->Properties["tyousasyoTotalstudyactFieldSize"]);
        $this->mojigyou["TOTALSTUDYACT"]["moji"] = (int)trim($moji);
        $this->mojigyou["TOTALSTUDYACT"]["gyou"] = (int)trim($gyou);

        //評価の文字数と行数の設定
        $gyou = $this->propGyo($this->Properties["tyousasyo2020GTotalstudyvalGyou"], 8);
        $this->Properties["tyousasyoTotalstudyvalFieldSize"] = "48 * {$gyou}";
        list($moji, $gyou) = preg_split('/\*/', $this->Properties["tyousasyoTotalstudyvalFieldSize"]);
        $this->mojigyou["TOTALSTUDYVAL"]["moji"] = (int)trim($moji);
        $this->mojigyou["TOTALSTUDYVAL"]["gyou"] = (int)trim($gyou);

        //調査書〇年用
        $db = Query::dbCheckOut();
        $query = knje010gQuery::getSchoolDiv($this);
        $schooldiv = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        if ($schooldiv["NEN"] == '0') {
            if ($schooldiv["SCHOOLDIV"] == "1") {
                $schooldiv["NEN"] = "4";
            } else {
                $schooldiv["NEN"] = "3";
            }
        }

        // 指導上参考となる諸事項
        $gyou = $this->propGyo($this->Properties["tyousasyo2020GshojikouGyou".$schooldiv["NEN"]], 66);
        $fields = array("TRAIN_REF1","TRAIN_REF2","TRAIN_REF3","TRAIN_REF4","TRAIN_REF5", "TRAIN_REF6");
        foreach ($fields as $field) {
            $this->mojigyou[$field]["moji"] = 15;
            $this->mojigyou[$field]["gyou"] = $gyou;
        }

        // 特別活動の記録
        if ($schooldiv["NEN"] == 3) {
            $moji = 16; //デフォルトの値
            $gyou = 18;  //デフォルトの値
        } else {
            $moji = 10; //デフォルトの値
            $gyou = 27;  //デフォルトの値
        }
        $gyou = $this->propGyo($this->Properties["tyousasyo2020GspecialactrecGyou".$schooldiv["NEN"]], $gyou);
        $this->mojigyou["SPECIALACTREC"]["moji"] = $moji;
        $this->mojigyou["SPECIALACTREC"]["gyou"] = $gyou;

        //備考の文字数と行数の設定
        $gyou = $this->propGyo($this->Properties["tyousasyo2020GremarkGyou"], 50);
        $this->Properties["tyousasyoRemarkFieldSize"] = "45 * {$gyou}";
        list($moji, $gyou) = preg_split('/\*/', $this->Properties["tyousasyoRemarkFieldSize"]);
        $this->mojigyou["REMARK"]["moji"] = (int)trim($moji);
        $this->mojigyou["REMARK"]["gyou"] = (int)trim($gyou);

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

    public function propGyo($prop, $def)
    {
        return $this->Properties["tyousasyo2020GshojikouExtends"] != "" && $prop ? $prop : $def;
    }
}