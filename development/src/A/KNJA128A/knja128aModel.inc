<?php

require_once('for_php7.php');
class knja128aModel extends Model
{
    public $cmd;
    public $schregno = "";
    public $name = "";
    public $grade = "";
    public $field = array();
    public $control;
    public $annual;        //学年(年次)
    public $exp_year;      //左フレームから送られてくる年度
    public $exp_semester;  //左フレームから送られてくる学期
    public $getPrgId;      //コールされたか
    public $sendAuth;      //親画面の権限
    public $auth;          //権限

    public function knja128aModel()
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
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
        }

        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        //権限
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        //学年（年次）
        if (VARS::post("ANNUAL")) {
            list($tmp["YEAR"], $tmp["ANNUAL"]) = explode(",", VARS::post("ANNUAL"));
            $this->annual = $tmp;
        }

        $this->field = array("TOTALSTUDYACT"            =>  VARS::post("TOTALSTUDYACT"),
                             "TOTALSTUDYVAL"            =>  VARS::post("TOTALSTUDYVAL"),
                             "SPECIALACTREMARK"         =>  VARS::post("SPECIALACTREMARK"),
                             "INDEPENDENT_REMARK"       =>  VARS::post("INDEPENDENT_REMARK"),
                             "ENT_DISABILITY_REMARK"    =>  VARS::post("ENT_DISABILITY_REMARK"),
                             "TOTALREMARK"              =>  VARS::post("TOTALREMARK"),
                             "ATTENDREC_REMARK"         =>  VARS::post("ATTENDREC_REMARK")
                             );

        $this->cmd = VARS::request("cmd");

        //左フレームから送られてくる
        //年度
        if (VARS::get("EXP_YEAR")) {
            $this->exp_year = VARS::get("EXP_YEAR");
        }
        //学期
        if (VARS::get("EXP_SEMESTER")) {
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }
        //学籍番号
        if (VARS::get("SCHREGNO")) {
            $this->schregno = VARS::get("SCHREGNO");
        }
        //氏名
        if (VARS::get("NAME")) {
            $this->name = VARS::get("NAME");
        }
        //学年
        if (VARS::get("GRADE")) {
            $this->grade = VARS::get("GRADE");
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

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //文字数・行数設定
        //活動内容
        if ($this->Properties["seitoSidoYoroku_dat_TotalstudyactSize"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["seitoSidoYoroku_dat_TotalstudyactSize"]);
            $this->totalstudyact_moji = (int)trim($moji);
            $this->totalstudyact_gyou = (int)trim($gyou);
        } else {
            $this->totalstudyact_moji = 60;
            $this->totalstudyact_gyou = 6;
        }
        //評価
        if ($this->Properties["seitoSidoYoroku_dat_TotalstudyvalSize"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["seitoSidoYoroku_dat_TotalstudyvalSize"]);
            $this->totalstudyval_moji = (int)trim($moji);
            $this->totalstudyval_gyou = (int)trim($gyou);
        } else {
            $this->totalstudyval_moji = 60;
            $this->totalstudyval_gyou = 6;
        }
        //特別活動所見
        if ($this->Properties["seitoSidoYoroku_dat_SpecialactremarkSize"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["seitoSidoYoroku_dat_SpecialactremarkSize"]);
            $this->specialactremark_moji = (int)trim($moji);
            $this->specialactremark_gyou = (int)trim($gyou);
        } else {
            $this->specialactremark_moji = 15;
            $this->specialactremark_gyou = 6;
        }
        //自立活動の記録
        if ($this->Properties["HTRAINREMARK_DAT_INDEPENDENT_REMARK_SIZE_H"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_INDEPENDENT_REMARK_SIZE_H"]);
            $this->indep_remark_moji = (int)trim($moji);
            $this->indep_remark_gyou = (int)trim($gyou);
        } else {
            $this->indep_remark_moji = 64;
            $this->indep_remark_gyou = 7;
        }
        //入学時の障害の状態
        if ($this->Properties["HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_H"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_H"]);
            $this->disability_moji = (int)trim($moji);
            $this->disability_gyou = (int)trim($gyou);
        } else {
            $this->disability_moji = 11;
            $this->disability_gyou = 6;
        }
        //総合所見
        if ($this->Properties["seitoSidoYoroku_dat_TotalremarkSize"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["seitoSidoYoroku_dat_TotalremarkSize"]);
            $this->totalremark_moji = (int)trim($moji);
            $this->totalremark_gyou = (int)trim($gyou);
        } else {
            $this->totalremark_moji = 66;
            $this->totalremark_gyou = 8;
        }
        //出欠の記録備考
        if ($this->Properties["seitoSidoYoroku_dat_Attendrec_RemarkSize"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["seitoSidoYoroku_dat_Attendrec_RemarkSize"]);
            $this->attendrec_remark_moji = (int)trim($moji);
            $this->attendrec_remark_gyou = (int)trim($gyou);
        } else {
            $this->attendrec_remark_moji = 35;
            $this->attendrec_remark_gyou = 2;
        }
    }

    //PROGRAMPATH取得
    public function &getProgrampathModel()
    {
        $db = Query::dbCheckOut();
        $programpath = $db->getOne(knja128aQuery::getProgrampath());
        Query::dbCheckIn($db);

        return $programpath;
    }

    //入力項目チェック
    public function validateInput()
    {
        //必須チェック
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }

        //文字数・行数チェック
        if ($this->validate_row_cnt($this->field["TOTALSTUDYACT"], ($this->totalstudyact_moji * 2)) > $this->totalstudyact_gyou) {
            $this->setWarning("MSG915", "(活動内容は{$this->totalstudyact_gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["TOTALSTUDYVAL"], ($this->totalstudyval_moji * 2)) > $this->totalstudyval_gyou) {
            $this->setWarning("MSG915", "(評価は{$this->totalstudyval_gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["SPECIALACTREMARK"], ($this->specialactremark_moji * 2)) > $this->specialactremark_gyou) {
            $this->setWarning("MSG915", "(特別活動所見は{$this->specialactremark_gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["INDEPENDENT_REMARK"], ($this->indep_remark_moji * 2)) > $this->indep_remark_gyou) {
            $this->setWarning("MSG915", "(自立活動の記録は{$this->indep_remark_gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["ENT_DISABILITY_REMARK"], ($this->disability_moji * 2)) > $this->disability_gyou) {
            $this->setWarning("MSG915", "(入学時の障害の状態は{$this->disability_gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["TOTALREMARK"], ($this->totalremark_moji * 2)) > $this->totalremark_gyou) {
            $this->setWarning("MSG915", "(総合所見は{$this->totalremark_gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["ATTENDREC_REMARK"], ($this->attendrec_remark_moji * 2)) > $this->attendrec_remark_gyou) {
            $this->setWarning("MSG915", "(出欠の記録備考は{$this->attendrec_remark_gyou}行までです)");
            return false;
        }

        return true;
    }

    //更新
    public function &getUpdateModel()
    {
        if (!$this->validateInput()) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }

    public function &update()
    {
        knja128aQuery::getDeleteQuery($this);
        knja128aQuery::getInsertQuery($this->field, $this);
        return $result;
    }
}
