<?php
class knjd428mModel extends Model
{
    public $cmd;
    public $exp_year;
    public $schregno;
    public $name;
    public $field = array();
    public $getPrgId;
    public $auth;
    public $sendAuth;
    public $grade;
    public $hrClass;
    public $getPro;
    public $useJisshu;

    public function knjd428mModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->exp_year);
            unset($this->semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->hrClass);
        }
        $this->semester9  = "9";  //固定
        $this->detailDiv  = "06"; //固定
        $this->detailCode = "01"; //固定

        $this->field = array("ATTENDREC_REMARK"     =>  VARS::post("ATTENDREC_REMARK"), //出欠の備考
                             "COMMUNICATION"        =>  VARS::post("COMMUNICATION"),    //学校より
                             "D02_01_REMARK1"       =>  VARS::post("D02_01_REMARK1"),   //記録１ 期日・実習先・実習内容1
                             "D02_01_REMARK2"       =>  VARS::post("D02_01_REMARK2"),   //記録１ 所見1
                             "D02_01_REMARK3"       =>  VARS::post("D02_01_REMARK3"),   //記録１ 斜線を入れるチェックボックス
                             "D02_02_REMARK1"       =>  VARS::post("D02_02_REMARK1"),   //記録２ 期日・実習先・実習内容2
                             "D02_02_REMARK2"       =>  VARS::post("D02_02_REMARK2"),   //記録２ 所見2
                             "D02_02_REMARK3"       =>  VARS::post("D02_02_REMARK3"),   //記録２ 斜線を入れるチェックボックス
                             "D01_01_REMARK1"       =>  VARS::post("D01_01_REMARK1"),   //ホームルーム活動
                             "D01_02_REMARK1"       =>  VARS::post("D01_02_REMARK1"),   //生徒会活動
                             "D01_03_REMARK1"       =>  VARS::post("D01_03_REMARK1"),   //部活動・その他
                             "UPDATED"              =>  VARS::post("UPDATED"),
                             "REMARK_CNT"           =>  VARS::post("REMARK_CNT")        //年間目標 目標数
                             );


        for ($idx=1; $idx <= $this->field["REMARK_CNT"]; $idx++) {
            $this->field["REMARK".$idx]      =  VARS::post("REMARK".$idx);
            $this->field["REMARK_NAME".$idx] =  VARS::post("REMARK_NAME".$idx);
        }

        $this->cmd = VARS::request("cmd");

        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        //権限
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        //左フレームから取得
        if (VARS::get("EXP_YEAR") != "") {
            $this->exp_year     = VARS::get("EXP_YEAR");
        }
        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME") != "") {
            $this->name = VARS::get("NAME");
        }

        if (VARS::post("SEMESTER") != "") {
            $this->semester = VARS::post("SEMESTER");
        }
        
        if (VARS::get("GRADE") != "") {
            $this->grade = VARS::get("GRADE");
        }
        if (VARS::get("HR_CLASS") != "") {
            $this->hrClass = VARS::get("HR_CLASS");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->exp_year);
            unset($this->semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->hrClass);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $db = Query::dbCheckOut();
        //帳票パターン取得 GRADE_KIND_SCHREG_GROUP_DAT
        $rowG = $db->getRow(knjd428mQuery::getGradeKindSchregGroupDat($this), DB_FETCHMODE_ASSOC);
        $query = knjd428mQuery::getPrintPattern($this, $rowG);
        $this->printPattern = $db->getOne($query);
        $this->printPattern = substr($this->printPattern, 1, 1);

        //校種
        $query = knjd428mQuery::getSchoolKind($this);
        $schoolKind = $db->getOne($query);

        //印刷デフォルト値設定「主に使用する様式」が「文言評価(3枠)B」か判定
        $this->useJisshu = false;
        $query = knjd428mQuery::getReportCondition($this, $schoolKind, "102");
        $remark1 = $db->getOne($query);
        if ($remark1 == "104") {
            $this->useJisshu = true;
        }
        Query::dbCheckIn($db);

        //入力文字初期値の設定
        //年間目標
        $this->remark_moji = 40;
        $this->remark_gyou = 10;

        //出欠の備考
        $this->attendrec_remark_moji = 10;
        $this->attendrec_remark_gyou = 2;

        //学校より
        $this->communication_moji = 45;
        $this->communication_gyou = 6;

        //記録１ 期日・実習先・実習内容1
        $this->getPro["D02_01_REMARK1"]["moji"] = 9;
        $this->getPro["D02_01_REMARK1"]["gyou"] = 6;

        //記録１ 所見1
        $this->getPro["D02_01_REMARK2"]["moji"] = 36;
        $this->getPro["D02_01_REMARK2"]["gyou"] = 6;

        //記録２ 期日・実習先・実習内容2
        $this->getPro["D02_02_REMARK1"]["moji"] = 9;
        $this->getPro["D02_02_REMARK1"]["gyou"] = 6;

        //記録２ 所見2
        $this->getPro["D02_02_REMARK2"]["moji"] = 36;
        $this->getPro["D02_02_REMARK2"]["gyou"] = 6;

        //特別活動の記録 ホームルーム活動
        $this->getPro["D01_01_REMARK1"]["moji"] = 13;
        $this->getPro["D01_01_REMARK1"]["gyou"] = 4;

        //特別活動の記録 生徒会活動
        $this->getPro["D01_02_REMARK1"]["moji"] = 13;
        $this->getPro["D01_02_REMARK1"]["gyou"] = 4;

        //特別活動の記録 部活動・その他
        $this->getPro["D01_03_REMARK1"]["moji"] = 19;
        $this->getPro["D01_03_REMARK1"]["gyou"] = 4;
    }

    //入力項目チェック
    public function validateInput()
    {
        //必須チェック
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }
        //年間目標 文字数チェック
        for ($i = 1; $i <= $this->field["REMARK_CNT"]; $i++) {
            if (strlen($this->field["REMARK".$i]) > 0) {
                if ($this->validate_row_cnt($this->field["REMARK".$i], ($this->remark_moji * 2)) > $this->remark_gyou) {
                    $this->setWarning("MSG915", "({$this->field["REMARK_NAME".$i]}は{$this->remark_gyou}行までです)");
                    return false;
                }
            }
        }
        //文字数チェック
        if (strlen($this->field["ATTENDREC_REMARK"]) > 0) {
            if ($this->validate_row_cnt($this->field["ATTENDREC_REMARK"], ($this->attendrec_remark_moji * 2)) > $this->attendrec_remark_gyou) {
                $this->setWarning("MSG915", "(出欠の備考は{$this->attendrec_remark_gyou}行までです)");
                return false;
            }
        }
        if (strlen($this->field["COMMUNICATION"]) > 0) {
            if ($this->validate_row_cnt($this->field["COMMUNICATION"], ($this->communication_moji * 2)) > $this->communication_gyou) {
                $this->setWarning("MSG915", "(学校よりは{$this->communication_gyou}行までです)");
                return false;
            }
        }
        if (strlen($this->field["D02_01_REMARK1"]) > 0) {
            if ($this->validate_row_cnt($this->field["D02_01_REMARK1"], ($this->getPro["D02_01_REMARK1"]["moji"] * 2)) > $this->getPro["D02_01_REMARK1"]["gyou"]) {
                $this->setWarning("MSG915", "(期日・実習先・実習内容1は{$this->getPro["D02_01_REMARK1"]["gyou"]}行までです)");
                return false;
            }
        }
        if (strlen($this->field["D02_01_REMARK2"]) > 0) {
            if ($this->validate_row_cnt($this->field["D02_01_REMARK2"], ($this->getPro["D02_01_REMARK2"]["moji"] * 2)) > $this->getPro["D02_01_REMARK2"]["gyou"]) {
                $this->setWarning("MSG915", "(所見1は{$this->getPro["D02_01_REMARK2"]["gyou"]}行までです)");
                return false;
            }
        }
        if (strlen($this->field["D02_02_REMARK1"]) > 0) {
            if ($this->validate_row_cnt($this->field["D02_02_REMARK1"], ($this->getPro["D02_02_REMARK1"]["moji"] * 2)) > $this->getPro["D02_02_REMARK1"]["gyou"]) {
                $this->setWarning("MSG915", "(期日・実習先・実習内容2は{$this->getPro["D02_02_REMARK1"]["gyou"]}行までです)");
                return false;
            }
        }
        if (strlen($this->field["D02_02_REMARK2"]) > 0) {
            if ($this->validate_row_cnt($this->field["D02_02_REMARK2"], ($this->getPro["D02_02_REMARK2"]["moji"] * 2)) > $this->getPro["D02_02_REMARK2"]["gyou"]) {
                $this->setWarning("MSG915", "(所見2は{$this->getPro["D02_02_REMARK2"]["gyou"]}行までです)");
                return false;
            }
        }
        if (strlen($this->field["D01_01_REMARK1"]) > 0) {
            if ($this->validate_row_cnt($this->field["D01_01_REMARK1"], ($this->getPro["D01_01_REMARK1"]["moji"] * 2)) > $this->getPro["D01_01_REMARK1"]["gyou"]) {
                $this->setWarning("MSG915", "(ホームルーム活動は{$this->getPro["D01_01_REMARK1"]["gyou"]}行までです)");
                return false;
            }
        }
        if (strlen($this->field["D01_02_REMARK1"]) > 0) {
            if ($this->validate_row_cnt($this->field["D01_02_REMARK1"], ($this->getPro["D01_02_REMARK1"]["moji"] * 2)) > $this->getPro["D01_02_REMARK1"]["gyou"]) {
                $this->setWarning("MSG915", "(生徒会活動は{$this->getPro["D01_02_REMARK1"]["gyou"]}行までです)");
                return false;
            }
        }
        if (strlen($this->field["D01_03_REMARK1"]) > 0) {
            if ($this->validate_row_cnt($this->field["D01_03_REMARK1"], ($this->getPro["D01_03_REMARK1"]["moji"] * 2)) > $this->getPro["D01_03_REMARK1"]["gyou"]) {
                $this->setWarning("MSG915", "(部活動・その他は{$this->getPro["D01_03_REMARK1"]["gyou"]}行までです)");
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
        //HREPORTREMARK_DAT
        knjd428mQuery::getInsertQuery($this);

        return ;
    }

    //更新(年間目標)
    public function &getUpdateRemarkModel()
    {
        if (!$this->validateInput()) {
            return;
        }
        $this->remarkupdate();
        $this->setMessage("MSG201");
    }

    public function &remarkupdate()
    {
        //HREPORT_SCHREG_REMARK_DAT
        knjd428mQuery::getRemarkInsertQuery($this);

        return ;
    }
}
