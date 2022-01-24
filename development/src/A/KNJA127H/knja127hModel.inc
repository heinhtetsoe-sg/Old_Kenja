<?php

require_once('for_php7.php');

class knja127hModel extends Model
{
    public $cmd;
    public $schregno = "";
    public $name = "";
    public $grade = "";
    public $field = array();
    public $control;
    public $exp_year;      //左フレームから送られてくる年度
    public $exp_semester;  //左フレームから送られてくる学期
    public $getPrgId;      //コールされたか
    public $sendAuth;      //親画面の権限
    public $auth;          //権限

    public function knja127hModel()
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

        $this->field = array("TOTALSTUDYACT"            =>  VARS::post("TOTALSTUDYACT"),
                             "TOTALSTUDYVAL"            =>  VARS::post("TOTALSTUDYVAL"),
                             "ENT_DISABILITY_REMARK"    =>  VARS::post("ENT_DISABILITY_REMARK"),
                             "TOTALREMARK"              =>  VARS::post("TOTALREMARK"),
                             "ATTENDREC_REMARK"         =>  VARS::post("ATTENDREC_REMARK"),
                             "JUGYO_JISU"               =>  VARS::post("JUGYO_JISU")
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
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }


        //文字数・行数設定
        //活動内容
        if ($this->Properties["seitoSidoYoroku_dat_TotalstudyactSize_disability"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["seitoSidoYoroku_dat_TotalstudyactSize_disability"]);
            $this->totalstudyact_moji = (int)trim($moji);
            $this->totalstudyact_gyou = (int)trim($gyou);
        } else {
            $this->totalstudyact_moji = 48;
            $this->totalstudyact_gyou = 9;
        }
        //評価
        if ($this->Properties["seitoSidoYoroku_dat_TotalstudyvalSize_disability"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["seitoSidoYoroku_dat_TotalstudyvalSize_disability"]);
            $this->totalstudyval_moji = (int)trim($moji);
            $this->totalstudyval_gyou = (int)trim($gyou);
        } else {
            $this->totalstudyval_moji = 48;
            $this->totalstudyval_gyou = 12;
        }
        //入学時の障害の状態
        if ($this->Properties["HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_H_disability"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_H_disability"]);
            $this->disability_moji = (int)trim($moji);
            $this->disability_gyou = (int)trim($gyou);
        } else {
            $this->disability_moji = 11;
            $this->disability_gyou = 22;
        }
        //総合所見
        if ($this->Properties["seitoSidoYoroku_dat_TotalremarkSize_disability"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["seitoSidoYoroku_dat_TotalremarkSize_disability"]);
            $this->totalremark_moji = (int)trim($moji);
            $this->totalremark_gyou = (int)trim($gyou);
        } else {
            $this->totalremark_moji = 60;
            $this->totalremark_gyou = 12;
        }
        //出欠の記録備考
        if ($this->Properties["seitoSidoYoroku_dat_Attendrec_RemarkSize_disability"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["seitoSidoYoroku_dat_Attendrec_RemarkSize_disability"]);
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
        $programpath = $db->getOne(knja127hQuery::getProgrampath());
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
        if ($this->validate_row_cnt($this->field["TOTALSTUDYACT"], ((int)$this->totalstudyact_moji * 2)) > $this->totalstudyact_gyou) {
            $this->setWarning("MSG915", "(活動内容は{$this->totalstudyact_gyou}行までです)");
            // Add by PP for textarea_cursor start 2020/01/20
            $this->message915="TOTALSTUDYACT";
            // Add by PP for textarea_cursor end 2020/01/31
            return false;
        }
        if ($this->validate_row_cnt($this->field["TOTALSTUDYVAL"], ((int)$this->totalstudyval_moji * 2)) > $this->totalstudyval_gyou) {
            $this->setWarning("MSG915", "(評価は{$this->totalstudyval_gyou}行までです)");
            // Add by PP for textarea_cursor start 2020/01/20
            $this->message915="TOTALSTUDYVAL";
            // Add by PP for textarea_cursor end 2020/01/31
            return false;
        }
        if ($this->validate_row_cnt($this->field["ENT_DISABILITY_REMARK"], ((int)$this->disability_moji * 2)) > $this->disability_gyou) {
            $this->setWarning("MSG915", "(入学時の障害の状態は{$this->disability_gyou}行までです)");
            // Add by PP for textarea_cursor start 2020/01/20
            $this->message915="ENT_DISABILITY_REMARK";
            // Add by PP for textarea_cursor end 2020/01/31
            return false;
        }
        if ($this->validate_row_cnt($this->field["TOTALREMARK"], ((int)$this->totalremark_moji * 2)) > $this->totalremark_gyou) {
            $this->setWarning("MSG915", "(総合所見は{$this->totalremark_gyou}行までです)");
            // Add by PP for textarea_cursor start 2020/01/20
            $this->message915="TOTALREMARK";
            // Add by PP for textarea_cursor end 2020/01/31
            return false;
        }
        if ($this->validate_row_cnt($this->field["ATTENDREC_REMARK"], ((int)$this->attendrec_remark_moji * 2)) > $this->attendrec_remark_gyou) {
            $this->setWarning("MSG915", "(出欠の記録備考は{$this->attendrec_remark_gyou}行までです)");
            // Add by PP for textarea_cursor start 2020/01/20
            $this->message915="ATTENDREC_REMARK";
            // Add by PP for textarea_cursor end 2020/01/31
            return false;
        }
        if (strlen($this->field["JUGYO_JISU"]) > 0) {
            if (!preg_match('/^[0-9]+$/', $this->field["JUGYO_JISU"])) {
                $this->setWarning("MSG909", "(総授業時数)");
                // Add by PP for textarea_cursor start 2020/01/20
                $this->message915="JUGYO_JISU";
            // Add by PP for textarea_cursor end 2020/01/31
                return false;
            }
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
        knja127hQuery::getDeleteQuery($this);
        knja127hQuery::getInsertQuery($this->field, $this);
        return $result;
    }
}
