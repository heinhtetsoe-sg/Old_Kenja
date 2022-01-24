<?php

require_once('for_php7.php');

class knje015Model extends Model
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
    public $exp_year;  //左フレームから送られてくる学期
    public $exp_semester;  //左フレームから送られてくる学期
    public $schArray = array();
    public $schoolKind;
    public $schoolName;
    public $schoolCode;

    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限

    public function knje015Model()
    {
        $this->year = CTRL_YEAR;
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->exp_yaer);
            unset($this->exp_semester);

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

        if (VARS::get("EXP_YEAR") != "") {
            $this->exp_year     = VARS::get("EXP_YEAR");
            $this->exp_semester = VARS::get("EXP_SEMESTER");
            $this->exp_grade = VARS::get("GRADE");
            $this->exp_hr_class = VARS::get("HR_CLASS");
        }

        //表示科目
        if (VARS::get("SCHREGNO")) {
            $this->schregno = VARS::get("SCHREGNO"); //学籍番号
        }
        if (VARS::get("NAME")) {
            $this->name = VARS::get("NAME");         //生徒氏名
        }
        if (VARS::request("GRADE_YEAR")) {
            $this->gradeYear = VARS::request("GRADE_YEAR");   //学年
        }
        if (VARS::request("YEAR")) {
            $this->year = VARS::request("YEAR");
        }

        //学年コンボの値で、年度も変更する
        $db = Query::dbCheckOut();
        Query::dbCheckIn($db);

        $this->field = array("SEQ001"       => VARS::post("SEQ001"),
                             "SEQ002"       => VARS::post("SEQ002"),
                             "SEQ003"       => VARS::post("SEQ003"),
                             "SEQ004"       => VARS::post("SEQ004"),
                             "SEQ005"       => VARS::post("SEQ005"),
                             "SEQ006"       => VARS::post("SEQ006"),
                             "TOTALREMARK"  => VARS::post("TOTALREMARK"),   //総合所見
                             "REMARK"       => VARS::post("REMARK")         //まなびの記録
                             );

        //左側から学籍番号を引き継いだタイミングで選択した年度学年をLogin年度でクリア
        if (VARS::get("SCHREGNO") != "") {
            $this->gradeYear = ($this->exp_year) ? $this->exp_year : CTRL_YEAR;
        }
        //生徒一覧を更新
        if (VARS::get("init")) {
            unset($this->schregno); //学籍番号
            unset($this->name);     //生徒氏名
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->moji = 15;
        $this->gyou = 66;

        //総合所見
        if ($this->Properties["seitoSidoYoroku_dat_TotalremarkSize"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["seitoSidoYoroku_dat_TotalremarkSize"]);
            $this->totalremark_moji = (int)trim($moji);
            $this->totalremark_gyou = (int)trim($gyou);
        } elseif ($this->Properties["seitoSidoYorokuSougouFieldSize"] == 1) {
            list($this->totalremark_moji, $this->totalremark_gyou) = array(66, 8);
        } elseif ($this->Properties["seitoSidoYorokuFieldSize"] == 1) {
            list($this->totalremark_moji, $this->totalremark_gyou) = array(66, 7);
        } else {
            list($this->totalremark_moji, $this->totalremark_gyou) = array(44, 6);
        }

        //まなびの記録
        $this->remark_moji = 50;
        $this->remark_gyou = 36;
    }

    //入力項目チェック
    public function validateInput()
    {
        if (!isset($this->schregno)) {
            return false;
        }

        if ($this->validate_row_cnt($this->field['SEQ001'], ((int)$this->moji * 2)) > $this->gyou) {
            $this->setWarning("MSG915", "(1)学習における特徴等は{$this->gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field['SEQ002'], ((int)$this->moji * 2)) > $this->gyou) {
            $this->setWarning("MSG915", "(2)行動の特徴，特技等は{$this->gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field['SEQ003'], ((int)$this->moji * 2)) > $this->gyou) {
            $this->setWarning("MSG915", "(3)部活動，ボランティア活動，留学・海外経験等は{$this->gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field['SEQ004'], ((int)$this->moji * 2)) > $this->gyou) {
            $this->setWarning("MSG915", "(4)取得資格，検定等は{$this->gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field['SEQ005'], ((int)$this->moji * 2)) > $this->gyou) {
            $this->setWarning("MSG915", "(5)表彰・顕彰等の記録は{$this->gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field['SEQ006'], ((int)$this->moji * 2)) > $this->gyou) {
            $this->setWarning("MSG915", "(6)その他は{$this->gyou}行までです)");
            return false;
        }
        if ($this->Properties["useSeitoSidoYorokuShomeiKinou"] != "1" && ($this->Properties["useSeitoSidoYorokuSougouShoken"] == "1" || $this->Properties["useSeitoSidoYorokuSougouShoken"] == "2")) {
            if ($this->validate_row_cnt($this->field['TOTALREMARK'], ((int)$this->totalremark_moji * 2)) > $this->totalremark_gyou) {
                $this->setWarning("MSG915", "総合所見は{$this->totalremark_gyou}行までです)");
                return false;
            }
        }
        if ($this->validate_row_cnt($this->field['REMARK'], ((int)$this->remark_moji * 2)) > $this->remark_gyou) {
            $this->setWarning("MSG915", "まなびの記録は{$this->remark_gyou}行までです)");
            return false;
        }

        return true;
    }

    //編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $db = Query::dbCheckOut();
            $db->autoCommit(false);
            knje015Query::update($db, $this);
            $db->commit();
            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
    }
}
