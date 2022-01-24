<?php

require_once("for_php7.php");

class knje011eModel extends Model
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
    public $mojigyou = array();

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

    public $target;

    public $gradeHrClass;

    public $itemname;

    public function knje011eModel()
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
        }
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

        $this->field = array(
                             "TOTALSTUDYACT"            => VARS::post("TOTALSTUDYACT"),             //活動内容
                             "TOTALSTUDYACT_SLASH_FLG"  => VARS::post("TOTALSTUDYACT_SLASH_FLG"),   //活動内容・斜線を入れる
                             "TOTALSTUDYVAL"            => VARS::post("TOTALSTUDYVAL"),             //評価
                             "TOTALSTUDYVAL_SLASH_FLG"  => VARS::post("TOTALSTUDYVAL_SLASH_FLG"),   //評価・斜線を入れる
                             "REMARK"                   => VARS::post("REMARK"),                    //備考
                             "COMMENTEX_A_CD"           => VARS::post("COMMENTEX_A_CD")             //学習成績概評
                             );

        $this->field2 = array("SPECIALACTREC"           => VARS::post("SPECIALACTREC"),             //特別活動の記録
                              "TOTALREMARK"             => VARS::post("TOTALREMARK"),               //指導要録
                              "TRAIN_REF"               => VARS::post("TRAIN_REF"),                 //指導上参考になる諸事項
                              "TRAIN_REF1"              => VARS::post("TRAIN_REF1"),                //指導上参考になる諸事項1
                              "TRAIN_REF2"              => VARS::post("TRAIN_REF2"),                //指導上参考になる諸事項2
                              "TRAIN_REF3"              => VARS::post("TRAIN_REF3"),                //指導上参考になる諸事項3
                              "TRAIN_REF4"              => VARS::post("TRAIN_REF4"),                //指導上参考になる諸事項4
                              "TRAIN_REF5"              => VARS::post("TRAIN_REF5"),                //指導上参考になる諸事項5
                              "TRAIN_REF6"              => VARS::post("TRAIN_REF6"),                //指導上参考になる諸事項6
                              "TRAIN_REF1_NO_COMM"      => VARS::post("TRAIN_REF1_NO_COMM"),        //指導上参考になる諸事項1 特記事項なし
                              "TRAIN_REF2_NO_COMM"      => VARS::post("TRAIN_REF2_NO_COMM"),        //指導上参考になる諸事項2 特記事項なし
                              "TRAIN_REF3_NO_COMM"      => VARS::post("TRAIN_REF3_NO_COMM"),        //指導上参考になる諸事項3 特記事項なし
                              "TRAIN_REF4_NO_COMM"      => VARS::post("TRAIN_REF4_NO_COMM"),        //指導上参考になる諸事項4 特記事項なし
                              "TRAIN_REF5_NO_COMM"      => VARS::post("TRAIN_REF5_NO_COMM"),        //指導上参考になる諸事項5 特記事項なし
                              "TRAIN_REF6_NO_COMM"      => VARS::post("TRAIN_REF6_NO_COMM"),        //指導上参考になる諸事項6 特記事項なし
                              "TOTALSTUDYACT"           => VARS::post("TOTALSTUDYACT"),             //活動内容
                              "TOTALSTUDYACT_SLASH_FLG" => VARS::post("TOTALSTUDYACT_SLASH_FLG"),   //活動内容・斜線を入れる
                              "TOTALSTUDYVAL"           => VARS::post("TOTALSTUDYVAL"),             //評価
                              "TOTALSTUDYVAL_SLASH_FLG" => VARS::post("TOTALSTUDYVAL_SLASH_FLG"),   //評価・斜線を入れる
                              "ATTENDREC_REMARK"        => VARS::post("ATTENDREC_REMARK"),          //出欠の記録備考
                              "ATTENDREC_REMARK_SLASH_FLG" => VARS::post("ATTENDREC_REMARK_SLASH_FLG"),  //出欠の記録備考・斜線を入れる
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

        $this->target = VARS::get("TARGET");

        //生徒一覧を更新
        if (VARS::get("init")) {
            unset($this->schregno); //学籍番号
            unset($this->name);     //生徒氏名
        }

        //学校名取得
        $query = knje011eQuery::getSchoolName();
        $db = Query::dbCheckOut();
        $rowZ011 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $this->schoolName = $rowZ011["NAME1"];
        $this->schoolCode = $rowZ011["NAME2"];
        //校種取得
        $this->school_kind  = $db->getOne(knje011eQuery::getSchoolKind($this));
        //入学区分取得
        $this->entDiv       = $db->getOne(knje011eQuery::getSchregBase($this, "ENT_DIV"));
        //入学年度取得
        $this->entYear      = $db->getOne(knje011eQuery::getSchregBase($this, "ENT_DATE", "year"));
        // Z010
        $this->getSchoolName = $db->getOne(knje011eQuery::getNameMst("Z010"));
        Query::dbCheckIn($db);

        //「特記事項なし」チェックボックス
        if ($this->getSchoolName == "naraken") {
            $this->no_comments_label = "特記事項なし。";
        } else {
            $this->no_comments_label = "特記事項なし";
        }
        $this->remark_no_comments = VARS::post("REMARK_NO_COMMENTS");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        //固定
        $this->Properties["useSyojikou3"] = "1";        //調査書：指導上参考データ3分割
        //$this->Properties["sidouyourokuSansyou"] = "1"; //1 ⇒ E011A それ以外は E011

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

        $this->Properties["tyousasyo2020"] = "1";
        $this->Properties["tyousasyoSougouHyoukaNentani"] = "";
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

        $f2 = array(array("SPECIALACTREC", "特別活動の記録"),
                    array("TRAIN_REF1", $this->itemname["TRAIN_REF1"]),
                    array("TRAIN_REF2", $this->itemname["TRAIN_REF2"]),
                    array("TRAIN_REF3", $this->itemname["TRAIN_REF3"]),
                    array("TRAIN_REF4", $this->itemname["TRAIN_REF4"]),
                    array("TRAIN_REF5", $this->itemname["TRAIN_REF5"]),
                    array("TRAIN_REF6", $this->itemname["TRAIN_REF6"]),
                    array("ATTENDREC_REMARK", $this->attendTitle."の記録備考")
              );

        foreach ($f2 as $arr) {
            $fieldname = $arr[0];
            $title = $arr[1];

            if ($this->validate_row_cnt($this->field2[$fieldname], ($this->mojigyou[$fieldname]["moji"] * 2)) > $this->mojigyou[$fieldname]["gyou"]) {
                $this->setWarning("MSG915", "(".$title."は{$this->mojigyou[$fieldname]["gyou"]}行までです)");
                return false;
            }
            if ($this->validateKinsoku($this->field2[$fieldname])) {
                $this->setWarning("MSG916", "(".$title."はスペースにつづいて長音（ー）を含んでいます。)");
                return false;
            }
        }

        $f1 = array(array("TOTALSTUDYACT", "総合学習 活動内容"),
                    array("TOTALSTUDYVAL", "総合学習 評価"),
                    array("REMARK", "備考")
              );

        foreach ($f1 as $arr) {
            $fieldname = $arr[0];
            $title = $arr[1];

            if ($this->validate_row_cnt($this->field[$fieldname], ($this->mojigyou[$fieldname]["moji"] * 2)) > $this->mojigyou[$fieldname]["gyou"]) {
                $this->setWarning("MSG915", "(".$title."は{$this->mojigyou[$fieldname]["gyou"]}行までです)");
                return false;
            }
            if ($this->validateKinsoku($this->field[$fieldname])) {
                $this->setWarning("MSG916", "(".$title."はスペースにつづいて長音（ー）を含んでいます。)");
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
                knje011eQuery::update($db, $this);
            } elseif ($this->cmd == "update2") {
                //更新処理
                knje011eQuery::update2($db, $this);
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
        knje011eQuery::updateHrShojikou($db, $this);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        $this->setMessage("MSG201");
    }

    public function getFieldSize()
    {
        //出欠の記録備考の文字数と行数の設定
        $this->mojigyou["ATTENDREC_REMARK"]["moji"] = 8;
        $this->mojigyou["ATTENDREC_REMARK"]["gyou"] = 7;
        $this->Properties["tyousasyoAttendrecRemarkFieldSize"] = "8 * 7";

        //活動内容の文字数と行数の設定
        $this->mojigyou["TOTALSTUDYACT"]["moji"] = 48;
        $this->mojigyou["TOTALSTUDYACT"]["gyou"] = $this->propGyou($this->Properties["tyousasyo2020ETotalstudyactGyou"], 8);
        $this->Properties["tyousasyoTotalstudyactFieldSize"] = "48 * {$this->mojigyou["TOTALSTUDYACT"]["gyou"]}";

        //評価の文字数と行数の設定
        $this->mojigyou["TOTALSTUDYVAL"]["moji"] = 48;
        $this->mojigyou["TOTALSTUDYVAL"]["gyou"] = $this->propGyou($this->Properties["tyousasyo2020ETotalstudyvalGyou"], 8);
        $this->Properties["tyousasyoTotalstudyvalFieldSize"] = "48 * {$this->mojigyou["TOTALSTUDYVAL"]["gyou"]}";

        //調査書〇年用
        $db = Query::dbCheckOut();
        $query = knje011eQuery::getSchoolDiv($this);
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
        $gyou = $this->propGyou($this->Properties["tyousasyo2020EshojikouGyou".$schooldiv["NEN"]], 66);
        $fields = array("TRAIN_REF1","TRAIN_REF2","TRAIN_REF3","TRAIN_REF4","TRAIN_REF5", "TRAIN_REF6");
        foreach ($fields as $field) {
            $this->mojigyou[$field]["moji"] = 15;
            $this->mojigyou[$field]["gyou"] = $gyou;
        }

        //特別活動の記録
        if ($schooldiv["NEN"] == 3) {
            $moji = 16; //デフォルトの値
            $gyou = 18;  //デフォルトの値
        } else {
            $moji = 10; //デフォルトの値
            $gyou = 27;  //デフォルトの値
        }
        $gyou = $this->propGyou($this->Properties["tyousasyo2020EspecialactrecGyou".$schooldiv["NEN"]], $gyou);
        $this->mojigyou["SPECIALACTREC"]["moji"] = $moji;
        $this->mojigyou["SPECIALACTREC"]["gyou"] = $gyou;

        //備考の文字数と行数の設定
        $this->mojigyou["REMARK"]["moji"] = 45;
        $this->mojigyou["REMARK"]["gyou"] = $this->propGyou($this->Properties["tyousasyo2020EremarkGyou"], 50);

        // 項目名
        $this->itemname = array(
            "TRAIN_REF1" => "指導上参考となる諸事項 (1)学習における特徴等",
            "TRAIN_REF2" => "指導上参考となる諸事項 (2)行動の特徴，特技等",
            "TRAIN_REF3" => "指導上参考となる諸事項 (3)部活動，ボランティア活動，留学・海外経験等",
            "TRAIN_REF4" => "指導上参考となる諸事項 (4)取得資格，検定等",
            "TRAIN_REF5" => "指導上参考となる諸事項 (5)表彰・顕彰等の記録",
            "TRAIN_REF6" => "指導上参考となる諸事項 (6)その他"
        );
    }

    public function propGyou($prop, $def)
    {
        return $this->Properties["tyousasyo2020EshojikouExtends"] != "" && $prop ? $prop : $def;
    }
}
