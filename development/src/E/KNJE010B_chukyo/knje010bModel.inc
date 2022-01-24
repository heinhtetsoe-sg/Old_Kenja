<?php

require_once('for_php7.php');

class knje010bModel extends Model {
    var $cmd;
    var $schregno;      //学籍番号
    var $field;         //画面上のデータ
    var $field2;        //画面上のデータ
    var $grade;         //学年
    var $annual;        //学年(年次)
    var $control;
    var $mode;          //在籍卒業モード
    var $grd_year;      //卒業年度
    var $grd_semester;  //卒業学期
    var $exp_year;      //左フレームから送られてくる年度
    var $exp_semester;  //左フレームから送られてくる学期
    var $schArray = array();
    var $readYear;
    var $schoolKind;
    var $schoolName;
    var $schoolCode;
    var $attendrec_remark_moji; //出欠の記録備考の文字数
    var $attendrec_remark_gyou; //出欠の記録備考の行数

    var $sido_totalstudyact_moji;    //（指導要録）活動内容の文字数
    var $sido_totalstudyact_gyou;    //（指導要録）活動内容の行数
    var $sido_totalstudyval_moji;    //（指導要録）評価の文字数
    var $sido_totalstudyval_gyou;    //（指導要録）評価の行数

    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    function knje010bModel() {
        $this->year = CTRL_YEAR;
        $this->mode = "ungrd";      //在籍モード
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
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

        $this->field = array("DISEASE"        => VARS::post("DISEASE"),            //疾患および異常
                             "DOC_REMARK"     => VARS::post("DOC_REMARK"),         //担当学校医所見
                             "TR_REMARK"      => VARS::post("TR_REMARK"),          //担任所見
                             "TOTALSTUDYACT"  => VARS::post("TOTALSTUDYACT"),      //活動内容
                             "TOTALSTUDYVAL"  => VARS::post("TOTALSTUDYVAL"),      //評価
                             "REMARK"         => VARS::post("REMARK"),             //備考
                             "COMMENTEX_A_CD" => VARS::post("COMMENTEX_A_CD")      //学習成績概評
                             );
        foreach ($this->schArray as $key => $val) {
            $year = $val["YEAR"];
            $this->field2[$year] = array("SPECIALACTREC"    => VARS::post("SPECIALACTREC-".$year),      //特別活動の記録
                                         "TRAIN_REF"        => VARS::post("TRAIN_REF1-".$year),         //指導上参考となる諸事項1
                                         "TRAIN_REF2"       => VARS::post("TRAIN_REF2-".$year),         //指導上参考となる諸事項2
                                         "TRAIN_REF3"       => VARS::post("TRAIN_REF3-".$year),         //指導上参考となる諸事項3
                                         "ATTENDREC_REMARK" => VARS::post("ATTENDREC_REMARK-".$year)    //出欠の記録備考
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
        $query = knje010bQuery::getSchoolName();
        $db = Query::dbCheckOut();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $this->schoolName = $rowZ010["NAME1"];
        $this->schoolCode = $rowZ010["NAME2"];
        Query::dbCheckIn($db);

        //プロパティファイルの読込み
        $this->getPropertiesAll();

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
    }

    //入力項目チェック
    function validateInput() {
        if (!isset($this->schregno)) return false;

        if ($this->schoolName == 'tottori') {
            if ($this->validate_row_cnt($this->field["REMARK"], (41 * 2)) > 4) {
                $this->setWarning("MSG915","(備考は4行までです)");
                return false;
            }
        } else {
            if ($this->validate_row_cnt($this->field["REMARK"], (41 * 2)) > 5) {
                $this->setWarning("MSG915","(備考は5行までです)");
                return false;
            }
        }

        foreach ($this->schArray as $key => $val) {
            $year = $val["YEAR"];
            if ($this->validate_row_cnt($this->field2[$year]["SPECIALACTREC"], ($this->specialactrec_moji * 2)) > $this->specialactrec_gyou) {
                $this->setWarning("MSG915","(特別活動の記録は{$this->specialactrec_gyou}行までです)");
                return false;
            }

            if ($this->Properties["useSyojikou3"] != "1") {
                if ($this->Properties["tyousasyoTokuBetuFieldSize"] == 1) {
                    if ($this->validate_row_cnt($this->field2[$year]["TRAIN_REF"], (58 * 2)) > 7) {
                        $this->setWarning("MSG915","(指導上参考となる諸事項は7行までです)");
                        return false;
                    }
                } else {
                    if ($this->validate_row_cnt($this->field2[$year]["TRAIN_REF"], (41 * 2)) > 5) {
                        $this->setWarning("MSG915","(指導上参考となる諸事項は5行までです)");
                        return false;
                    }
                }
            } else {
                if ($this->validate_row_cnt($this->field2[$year]["TRAIN_REF"], ($this->train_ref1_moji * 2)) > $this->train_ref1_gyou) {
                    $this->setWarning("MSG915","(指導上参考となる諸事項1は{$this->train_ref1_gyou}行までです)");
                    return false;
                }
                if ($this->validate_row_cnt($this->field2[$year]["TRAIN_REF2"], ($this->train_ref2_moji * 2)) > $this->train_ref2_gyou) {
                    $this->setWarning("MSG915","(指導上参考となる諸事項2は{$this->train_ref2_gyou}行までです)");
                    return false;
                }
                if ($this->validate_row_cnt($this->field2[$year]["TRAIN_REF3"], ($this->train_ref3_moji * 2)) > $this->train_ref3_gyou) {
                    $this->setWarning("MSG915","(指導上参考となる諸事項3は{$this->train_ref3_gyou}行までです)");
                    return false;
                }
            }

            //出欠の記録備考
            if ($this->validate_row_cnt($this->field2[$year]["ATTENDREC_REMARK"], ($this->attendrec_remark_moji * 2)) > $this->attendrec_remark_gyou) {
                $this->setWarning("MSG915","(出欠の記録備考は{$this->attendrec_remark_gyou}行までです)");
                return false;
            }
        }

        //活動内容
        if ($this->validate_row_cnt($this->field["TOTALSTUDYACT"], ($this->totalstudyact_moji * 2)) > $this->totalstudyact_gyou) {
            $this->setWarning("MSG915","(総合学習...活動内容は{$this->totalstudyact_gyou}行までです)");
            return false;
        }

        //評価
        if ($this->validate_row_cnt($this->field["TOTALSTUDYVAL"], ($this->totalstudyval_moji * 2)) > $this->totalstudyval_gyou) {
            $this->setWarning("MSG915","(総合学習...評価は{$this->totalstudyval_gyou}行までです)");
            return false;
        }

        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        if ($this->validateInput()) {
            $db = Query::dbCheckOut();
            $db->autoCommit(false);
            if ($this->cmd == "update") {
                //更新処理
                knje010bQuery::update($db, $this);

                //更新処理
                foreach ($this->schArray as $key => $val) {
                    knje010bQuery::update2($db, $this, $val);
                }
            }
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
    }

    //保健より読込みのモデルを返します
    function &getReloadHealthModel() {
        $db = Query::dbCheckOut();

        $query = knje010bQuery::selectQueryHealth($this);
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

    function getFieldSize() {
        //出欠の記録備考の文字数と行数の設定
        if ($this->Properties["tyousasyoAttendrecRemarkFieldSize"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["tyousasyoAttendrecRemarkFieldSize"]);
            $this->attendrec_remark_moji = (int)trim($moji);
            $this->attendrec_remark_gyou = (int)trim($gyou);
        } else {
            $this->attendrec_remark_moji = 5; //デフォルトの値
            $this->attendrec_remark_gyou = 3; //デフォルトの値
        }
        //活動内容の文字数と行数の設定
        if ($this->Properties["tyousasyoTotalstudyactFieldSize"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["tyousasyoTotalstudyactFieldSize"]);
            $this->totalstudyact_moji = (int)trim($moji);
            $this->totalstudyact_gyou = (int)trim($gyou);
        } else {
            if ($this->Properties["tyousasyoSougouHyoukaNentani"] == 1) {
                $this->totalstudyact_moji = 19; //デフォルトの値
                $this->totalstudyact_gyou = 5; //デフォルトの値
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
                $this->totalstudyval_moji = 19; //デフォルトの値
                $this->totalstudyval_gyou = 6; //デフォルトの値
            } else {
                $this->totalstudyval_moji = 41; //デフォルトの値
                $this->totalstudyval_gyou = 3; //デフォルトの値
            }
        }

        if ($this->Properties["train_ref_1_2_3_field_size"] == 1) {
            $this->train_ref1_moji = 14;
            $this->train_ref2_moji = 21;
            $this->train_ref3_moji = 7;
        } else {
            $this->train_ref1_moji = 14;
            $this->train_ref2_moji = 14;
            $this->train_ref3_moji = 14;
        }

        if ($this->Properties["train_ref_1_2_3_gyo_size"]) {
            $this->train_ref1_gyou = $this->Properties["train_ref_1_2_3_gyo_size"];
            $this->train_ref2_gyou = $this->Properties["train_ref_1_2_3_gyo_size"];
            $this->train_ref3_gyou = $this->Properties["train_ref_1_2_3_gyo_size"];
        } else {
            $this->train_ref1_gyou = 5;
            $this->train_ref2_gyou = 5;
            $this->train_ref3_gyou = 5;
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
                $this->specialactrec_gyou = 8;  //デフォルトの値
            }
        }
    }
}
?>
