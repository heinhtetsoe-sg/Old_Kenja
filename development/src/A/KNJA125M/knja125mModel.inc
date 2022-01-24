<?php

require_once('for_php7.php');

class knja125mModel extends Model {

    var $cmd;
    var $exp_year = "";
    var $exp_semester = "";
    var $schregno = "";
    var $name = "";
    var $field = array();
    var $control;
    var $annual;                    //学年(年次)

    function knja125mModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        if (VARS::get("PROGRAMID") != "") {
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
                             "TOTALSTUDYACT2"       =>  VARS::post("TOTALSTUDYACT2"),//東京仕様
                             "TOTALSTUDYVAL2"       =>  VARS::post("TOTALSTUDYVAL2"),//東京仕様
                             "SPECIALJISUU"         =>  VARS::post("SPECIALJISUU"),//佐賀通信仕様
                             "SPECIALACTREMARK"     =>  VARS::post("SPECIALACTREMARK"),
                             "TOTALREMARK"          =>  VARS::post("TOTALREMARK"),
                             "ATTENDREC_REMARK"     =>  VARS::post("ATTENDREC_REMARK"),
                             "CREDITREMARK"         =>  VARS::post("CREDITREMARK"),//東京仕様
                             "NARA_TIME"            =>  VARS::post("NARA_TIME"),//奈良仕様
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
            $this->grade       = VARS::get("GRADE");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->annual);
        }

        //DB接続
        $db = Query::dbCheckOut();
        //東京仕様かを確認
        $this->getname = "";
        $this->getname = $db->getOne(knja125mQuery::getNamecd("Z010", "00"));
        //DB切断
        Query::dbCheckIn($db);

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }


        // 総合的な学習の時間活動内容
        if ($this->Properties["seitoSidoYoroku_dat_TotalstudyactSize"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["seitoSidoYoroku_dat_TotalstudyactSize"]);
            $this->totalstudyact_moji = (int)trim($moji);
            $this->totalstudyact_gyou = (int)trim($gyou);
        } else {
            $this->totalstudyact_moji = 44; //デフォルトの値
            $this->totalstudyact_gyou = 1;  //デフォルトの値
        }

        // 総合的な学習の時間評価
        if ($this->Properties["seitoSidoYoroku_dat_TotalstudyvalSize"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["seitoSidoYoroku_dat_TotalstudyvalSize"]);
            $this->totalstudyval_moji = (int)trim($moji);
            $this->totalstudyval_gyou = (int)trim($gyou);
        } else {
            $this->totalstudyval_moji = 44; //デフォルトの値
            $this->totalstudyval_gyou = 2;  //デフォルトの値
        }

        // 特別活動の記録
        if ($this->Properties["seitoSidoYoroku_dat_SpecialactremarkSize"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["seitoSidoYoroku_dat_SpecialactremarkSize"]);
            $this->specialact_moji = (int)trim($moji);
            $this->specialact_gyou = (int)trim($gyou);
        } else {
            $this->specialact_moji = 11; //デフォルトの値
            $this->specialact_gyou = 6;  //デフォルトの値
        }

        // 総合所見
        if ($this->Properties["seitoSidoYoroku_dat_TotalremarkSize"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["seitoSidoYoroku_dat_TotalremarkSize"]);
            $this->totalremark_moji = (int)trim($moji);
            $this->totalremark_gyou = (int)trim($gyou);
        } else {
            $this->totalremark_moji = 22; //デフォルトの値
            $this->totalremark_gyou = 6;  //デフォルトの値
        }

        // 奈良Time
        $this->nara_time_moji = 18; //デフォルトの値
        $this->nara_time_gyou = 6;  //デフォルトの値
        
        // 特別活動出席時数
        $this->specialjisuu_moji = 6;  //デフォルトの値
        $this->specialjisuu_gyou = 1;  //デフォルトの値
    }

    //入力項目チェック
    function validateInput()
    {
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }
        if ($this->validate_row_cnt($this->field["TOTALSTUDYACT"], ((int)$this->totalstudyact_moji * 2)) > $this->totalstudyact_gyou) {
            $this->setWarning("MSG915","(活動内容は{$this->totalstudyact_gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["TOTALSTUDYVAL"], ((int)$this->totalstudyval_moji * 2)) > $this->totalstudyval_gyou) {
            $this->setWarning("MSG915","(評価は{$this->totalstudyval_gyou}行までです)");
            return false;
        }
        if ($this->getname === 'tokyoto') {
            if ($this->validate_row_cnt($this->field["TOTALSTUDYACT2"], (44 * 2)) > 2) {
                $this->setWarning("MSG915","(学習活動は2行までです)");
                return false;
            }
            if ($this->validate_row_cnt($this->field["TOTALSTUDYVAL2"], (44 * 2)) > 2) {
                $this->setWarning("MSG915","(評価は2行までです)");
                return false;
            }
            if ($this->validate_row_cnt($this->field["CREDITREMARK"], (44 * 2)) > 5) {
                $this->setWarning("MSG915","(備考は5行までです)");
                return false;
            }
        }
        if ($this->getname === 'sagaken') {
            if($this->field["SPECIALJISUU"] != ''){
                if (!is_numeric($this->field["SPECIALJISUU"])){
                    $this->setWarning("MSG907","");
                    return false;
                }else if ($this->validate_row_cnt($this->field["SPECIALJISUU"], ($this->specialjisuu_moji)) > $this->specialjisuu_gyou) {
                    $this->setWarning("MSG915","(特別活動出席時数は{$this->specialjisuu_moji}桁までです)");
                    return false;
                }
            }
        }
        if ($this->validate_row_cnt($this->field["SPECIALACTREMARK"], ((int)$this->specialact_moji * 2)) > $this->specialact_gyou) {
            $this->setWarning("MSG915","(特別活動所見は{$this->specialact_gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["TOTALREMARK"], ((int)$this->totalremark_moji * 2)) > $this->totalremark_gyou) {
            $this->setWarning("MSG915","(総合所見は{$this->totalremark_gyou}行までです)");
            return false;
        }
        if ($this->getname === 'naraken') {
            if ($this->validate_row_cnt($this->field["NARA_TIME"], ((int)$this->nara_time_moji * 2)) > $this->nara_time_gyou) {
                $this->setWarning("MSG915","(奈良Timeは{$this->nara_time_gyou}行までです)");
                return false;
            }
        }
        if ($this->validate_row_cnt($this->field["ATTENDREC_REMARK"], (40 * 2)) > 1) {
            $this->setWarning("MSG915","(出校の記録備考は80バイトまでです)");
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
        knja125mQuery::getDeleteQuery($this->schregno, $this->exp_year, $this);
        knja125mQuery::getInsertQuery($this->field, $this->schregno, $this->exp_year, $this->exp_semester, $this);
        return ;
    }
}
?>
