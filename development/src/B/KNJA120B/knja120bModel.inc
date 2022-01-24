<?php
class knja120bModel extends Model {

    var $cmd;
    var $exp_year = "";
    var $exp_semester = "";
    var $schregno = "";
    var $name = "";
    var $field = array();

    function knja120bModel() {}

    function init() {

        if (VARS::get("PROGRAMID") != "") {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
        }

        $this->field = array("TOTALSTUDYACT"        =>  VARS::post("TOTALSTUDYACT"),
                             "TOTALSTUDYVAL"        =>  VARS::post("TOTALSTUDYVAL"),
                             "SPECIALACTREMARK"     =>  VARS::post("SPECIALACTREMARK"),
                             "TOTALREMARK"          =>  VARS::post("TOTALREMARK"),
                             "ATTENDREC_REMARK"     =>  VARS::post("ATTENDREC_REMARK"),
                             "UPDATED"              =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");

        if (!$this->cmd) {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
        }

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
            $this->name = VARS::get("NAME");
        }

        $this->year     = VARS::request("YEAR");
        $this->semester = VARS::request("SEMESTER");

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->year);
            unset($this->semester);
            unset($this->schregno);
            unset($this->name);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["seitoSidoYoroku_dat_TotalstudyactSize"]) {
            list($moji, $gyou) = split('\*', $this->Properties["seitoSidoYoroku_dat_TotalstudyactSize"]);
            $this->totalstudyact_moji = (int)trim($moji);
            $this->totalstudyact_gyou = (int)trim($gyou);
        } else {
            $this->totalstudyact_moji = 23; //デフォルトの値
            $this->totalstudyact_gyou = 5;  //デフォルトの値
        }
        if ($this->Properties["seitoSidoYoroku_dat_TotalstudyvalSize"]) {
            list($moji, $gyou) = split('\*', $this->Properties["seitoSidoYoroku_dat_TotalstudyvalSize"]);
            $this->totalstudyval_moji = (int)trim($moji);
            $this->totalstudyval_gyou = (int)trim($gyou);
        } else {
            $this->totalstudyval_moji = 23; //デフォルトの値
            $this->totalstudyval_gyou = 6;  //デフォルトの値
        }
    }

    //入力項目チェック
    function validateInput($db)
    {
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }

        //署名チェック
        $check = $db->getRow(knja120bQuery::getOpinionsWk($this), DB_FETCHMODE_ASSOC);
        if ($check["CHAGE_OPI_SEQ"] || $check["LAST_OPI_SEQ"]) {
            $this->setWarning("MSG917","（署名済み）");
            return false;
        }

        //学校取得
        $schoolname = $db->getOne(knja120bQuery::getNameMst('Z010', '00'));

        if ($schoolname == "kumamoto") {
            if ($this->Properties["seitoSidoYoroku_dat_TotalstudyactSize"]) {
                $totalstudyact_length = ($this->totalstudyact_moji * $this->totalstudyact_gyou * 3) + ($this->totalstudyact_gyou - 1) * 2;
                if (strlen($this->field["TOTALSTUDYACT"]) > $totalstudyact_length) {
                    $this->setWarning("MSG915","(活動内容は{$totalstudyact_length}バイトまでです)");
                    return false;
                }
            } else if ($this->Properties["seitoSidoYorokuFieldSize"] == 1) {
                if (strlen($this->field["TOTALSTUDYACT"]) > 542) {
                    $this->setWarning("MSG915","(活動内容は542バイトまでです)");
                    return false;
                }
            } else {
                if (strlen($this->field["TOTALSTUDYACT"]) > 138) {
                    $this->setWarning("MSG915","(活動内容は138バイトまでです)");
                    return false;
                }
            }

            if ($this->Properties["seitoSidoYoroku_dat_TotalstudyvalSize"]) {
                $totalstudyval_length = ($this->totalstudyval_moji * $this->totalstudyval_gyou * 3) + ($this->totalstudyval_gyou - 1) * 2;
                if (strlen($this->field["TOTALSTUDYVAL"]) > $totalstudyval_length) {
                    $this->setWarning("MSG915","(評価は{$totalstudyval_length}バイトまでです)");
                    return false;
                }
            } else if ($this->Properties["seitoSidoYorokuFieldSize"] == 1) {
                if (strlen($this->field["TOTALSTUDYVAL"]) > 542) {
                    $this->setWarning("MSG915","(評価は542バイトまでです)");
                    return false;
                }
            } else {
                if (strlen($this->field["TOTALSTUDYVAL"]) > 208) {
                    $this->setWarning("MSG915","(評価は208バイトまでです)");
                    return false;
                }
            }

            if ($this->Properties["seitoSidoYorokuFieldSize"] == 1) {
                if (strlen($this->field["ATTENDREC_REMARK"]) > 242) {
                    $this->setWarning("MSG915","(出欠の記録備考は242バイトまでです)");
                    return false;
                }
            } else {
                if (strlen($this->field["ATTENDREC_REMARK"]) > 122) {
                    $this->setWarning("MSG915","(出欠の記録備考は122バイトまでです)");
                    return false;
                }
            }

            if ($this->Properties["seitoSidoYorokuSpecialactremarkFieldSize"] == 1) {
                if (strlen($this->field["SPECIALACTREMARK"]) > 678) {
                    $this->setWarning("MSG915","(特別活動所見は678バイトまでです)");
                    return false;
                }
            } else {
                if (strlen($this->field["SPECIALACTREMARK"]) > 208) {
                    $this->setWarning("MSG915","(特別活動所見は208バイトまでです)");
                    return false;
                }
            }

            if ($this->Properties["seitoSidoYorokuSougouFieldSize"] == 1) {
                if (strlen($this->field["TOTALREMARK"]) > 1598) {
                    $this->setWarning("MSG915","(総合所見は1598バイトまでです)");
                    return false;
                }
            } elseif ($this->Properties["seitoSidoYorokuFieldSize"] == 1) {
                if (strlen($this->field["TOTALREMARK"]) > 1398) {
                    $this->setWarning("MSG915","(総合所見は1398バイトまでです)");
                    return false;
                }
            } else {
                if (strlen($this->field["TOTALREMARK"]) > 802) {
                    $this->setWarning("MSG915","(総合所見は802バイトまでです)");
                    return false;
                }
            }
        } else {
            if ($this->Properties["seitoSidoYoroku_dat_TotalstudyactSize"]) {
                if ($this->validate_row_cnt($this->field["TOTALSTUDYACT"], ($this->totalstudyact_moji * 2)) > $this->totalstudyact_gyou) {
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
                if ($this->validate_row_cnt($this->field["TOTALSTUDYVAL"], ($this->totalstudyval_moji * 2)) > $this->totalstudyval_gyou) {
                    $this->setWarning("MSG915","(評価は{$this->totalstudyval_gyou}行までです)");
                    return false;
                }
            } else if ($this->Properties["seitoSidoYorokuFieldSize"] == 1) {
                if ($this->validate_row_cnt($this->field["TOTALSTUDYVAL"], (22 * 2)) > 8) {
                    $this->setWarning("MSG915","(評価は8行トまでです)");
                    return false;
                }
            } else {
                if ($this->validate_row_cnt($this->field["TOTALSTUDYVAL"], (11 * 2)) > 6) {
                    $this->setWarning("MSG915","(評価は6行までです)");
                    return false;
                }
            }

            if ($this->Properties["seitoSidoYorokuFieldSize"] == 1) {
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
            } else {
                if ($this->validate_row_cnt($this->field["SPECIALACTREMARK"], (11 * 2)) > 6) {
                    $this->setWarning("MSG915","(特別活動所見は6行までです)");
                    return false;
                }
            }

            if ($this->Properties["seitoSidoYorokuSougouFieldSize"] == 1) {
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
        }




        return true;
    }

    function &getUpdateModel()
    {
        $db = Query::dbCheckOut();
        if (!$this->validateInput($db)) {
            return;
        }

        $this->update($db);
        $this->setMessage("MSG201");
        Query::dbCheckIn($db);
    }

    //アップデート
    function &update($db) {

        knja120bQuery::getDeleteSql($db, $this);
        knja120bQuery::getInsertSql($db, $this, $this->field);

        return;
    }

    //ハッシュ値を作る
    function makeHash($Row, $study, $attend) {

        $data = $Row["TOTALSTUDYACT"].$Row["TOTALSTUDYVAL"].$Row["SPECIALACTREMARK"].$Row["TOTALREMARK"].$Row["ATTENDREC_REMARK"].
                $study.$attend["CLASSDAYS"].$attend["OFFDAYS"].$attend["ABSENT"].$attend["SUSPEND"].$attend["MOURNING"].
                $attend["ABROAD"].$attend["REQUIREPRESENT"].$attend["SICK"].$attend["ACCIDENTNOTICE"].$attend["NOACCIDENTNOTICE"].$attend["PRESENT"];

        return sha1($data);
    }
}
?>
