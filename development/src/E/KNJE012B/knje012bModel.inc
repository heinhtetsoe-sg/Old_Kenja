<?php

require_once('for_php7.php');
class knje012bModel extends Model
{
    public $cmd;
    public $schregno;      //学籍番号
    public $field;         //画面上のデータ
    public $grade;         //学年
    public $annual;        //学年(年次)
    public $control;
    public $form1_first;   //左フレームから最初の呼出ならば、on
    public $mode;          //在籍卒業モード
    public $grd_year;      //卒業年度
    public $grd_semester;  //卒業学期
    public $exp_year;      //左フレームから送られてくる年度
    public $exp_semester;  //左フレームから送られてくる学期

    public function knje012bModel()
    {
        $this->year = CTRL_YEAR;
        $this->mode = "ungrd";      //在籍モード
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

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

        //表示
        if (VARS::get("SCHREGNO")) {
            $this->schregno = VARS::get("SCHREGNO"); //学籍番号
        }
        if (VARS::get("NAME")) {
            $this->name = VARS::get("NAME");         //生徒氏名
        }
        if (VARS::get("GRADE")) {
            $this->grade = VARS::get("GRADE");       //学年
        }

        $this->form1_first = "off";
        //在籍卒業モード
        if (VARS::get("mode")) {
            $this->form1_first = "on";
            $this->mode = VARS::get("mode");         //在籍卒業モード
        }
        if (VARS::get("GRD_YEAR")) {
            $this->grd_year = VARS::get("GRD_YEAR"); //卒業年度
        }
        if (VARS::get("GRD_SEMESTER")) {
            $this->grd_semester = VARS::get("GRD_SEMESTER"); //卒業学期
        }

        $this->field = array("ATTENDREC_REMARK"    => VARS::post("ATTENDREC_REMARK"),  //出欠の記録（主な欠席理由）
                             "SPECIALACTREC"       => VARS::post("SPECIALACTREC"),     //総合的な学習の時間の記録
                             "CLUBACT"             => VARS::post("CLUBACT"),           //総合的な学習の時間の記録
                             "STUDENTACT"          => VARS::post("STUDENTACT"),        //生徒会活動
                             "CALSSACT"            => VARS::post("CALSSACT"),          //学級活動
                             "SCHOOLEVENT"         => VARS::post("SCHOOLEVENT"),       //学校行事
                             "SOUGOUSYOKEN"        => VARS::post("SOUGOUSYOKEN"),      //学校生活全般の総合所見
                             "ZITUYOUSIKEN"        => VARS::post("ZITUYOUSIKEN")       //文部科学省実用英語技能検定試験等
                             );

        //学年（年次）
        if (VARS::post("ANNUAL")) {
            list($tmp["YEAR"], $tmp["ANNUAL"]) = explode(",", VARS::post("ANNUAL"));
            $this->annual = $tmp;
        }

        //生徒一覧を更新
        if (VARS::get("init")) {
            unset($this->schregno); //学籍番号
            unset($this->name);     //生徒氏名
        }

        $this->fieldSize['ZITUYOUSIKEN']['moji'] = '15';
        $this->fieldSize['ZITUYOUSIKEN']['gyou'] = '10';
        if ($this->Properties["tyousasyoRemarkFieldSize_J"] != "") {
            $temp = explode('*', $this->Properties["tyousasyoRemarkFieldSize_J"]);
            $this->fieldSize['ZITUYOUSIKEN']['moji'] = trim($temp[0]);
            $this->fieldSize['ZITUYOUSIKEN']['gyou'] = trim($temp[1]);
        }
        $this->fieldSize['SOUGOUSYOKEN']['moji'] = '16';
        $this->fieldSize['SOUGOUSYOKEN']['gyou'] = '30';
        if ($this->Properties["tyousasyoRemark2FieldSize_J"] != "") {
            $temp = explode('*', $this->Properties["tyousasyoRemark2FieldSize_J"]);
            $this->fieldSize['SOUGOUSYOKEN']['moji'] = trim($temp[0]);
            $this->fieldSize['SOUGOUSYOKEN']['gyou'] = trim($temp[1]);
        }
        $this->fieldSize['ATTENDREC_REMARK']['moji'] = '4';
        $this->fieldSize['ATTENDREC_REMARK']['gyou'] = '4';
        if ($this->Properties["tyousasyoAttendrecRemarkFieldSize_J"] != "") {
            $temp = explode('*', $this->Properties["tyousasyoAttendrecRemarkFieldSize_J"]);
            $this->fieldSize['ATTENDREC_REMARK']['moji'] = trim($temp[0]);
            $this->fieldSize['ATTENDREC_REMARK']['gyou'] = trim($temp[1]);
        }
        $this->fieldSize['SPECIALACTREC']['moji'] = '18';
        $this->fieldSize['SPECIALACTREC']['gyou'] = '6';
        if ($this->Properties["tyousasyoSpecialactrecFieldSize_J"] != "") {
            $temp = explode('*', $this->Properties["tyousasyoSpecialactrecFieldSize_J"]);
            $this->fieldSize['SPECIALACTREC']['moji'] = trim($temp[0]);
            $this->fieldSize['SPECIALACTREC']['gyou'] = trim($temp[1]);
        }
        $this->fieldSize['CLUBACT']['moji'] = '16';
        $this->fieldSize['CLUBACT']['gyou'] = '6';
        if ($this->Properties["tyousasyoClubactFieldSize_J"] != "") {
            $temp = explode('*', $this->Properties["tyousasyoClubactFieldSize_J"]);
            $this->fieldSize['CLUBACT']['moji'] = trim($temp[0]);
            $this->fieldSize['CLUBACT']['gyou'] = trim($temp[1]);
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        if (!isset($this->schregno)) {
            return false;
        }

        if ($this->validate_row_cnt($this->field["ZITUYOUSIKEN"], ($this->fieldSize['ZITUYOUSIKEN']['moji'] * 2)) > $this->fieldSize['ZITUYOUSIKEN']['gyou']) {
            $this->setWarning("MSG915", "文部科学省実用英語検定試験等は、全角" . $this->fieldSize['ZITUYOUSIKEN']['moji'] ."文字" . $this->fieldSize['ZITUYOUSIKEN']['gyou'] . "行までです");
            return false;
        }
        if ($this->validate_row_cnt($this->field["SOUGOUSYOKEN"], ($this->fieldSize['SOUGOUSYOKEN']['moji'] * 2)) > $this->fieldSize['SOUGOUSYOKEN']['gyou']) {
            $this->setWarning("MSG915", "学校生活全般の総合所見は、全角" . $this->fieldSize['SOUGOUSYOKEN']['moji'] ."文字" . $this->fieldSize['SOUGOUSYOKEN']['gyou'] . "行までです");
            return false;
        }
        if ($this->validate_row_cnt($this->field["ATTENDREC_REMARK"], ($this->fieldSize['ATTENDREC_REMARK']['moji'] * 2)) > $this->fieldSize['ATTENDREC_REMARK']['gyou']) {
            $this->setWarning("MSG915", "出欠の記録は、全角" . $this->fieldSize['ATTENDREC_REMARK']['moji'] ."文字" . $this->fieldSize['ATTENDREC_REMARK']['gyou'] . "行までです");
            return false;
        }
        if ($this->validate_row_cnt($this->field["SPECIALACTREC"], ($this->fieldSize['SPECIALACTREC']['moji'] * 2)) > $this->fieldSize['SPECIALACTREC']['gyou']) {
            $this->setWarning("MSG915", "生徒会・学校行事・HR活動は、全角" . $this->fieldSize['SPECIALACTREC']['moji'] ."文字" . $this->fieldSize['SPECIALACTREC']['gyou'] . "行までです");
            return false;
        }
        if ($this->validate_row_cnt($this->field["CLUBACT"], ($this->fieldSize['CLUBACT']['moji'] * 2)) > $this->fieldSize['CLUBACT']['gyou']) {
            $this->setWarning("MSG915", "クラブ活動は、全角" . $this->fieldSize['CLUBACT']['moji'] ."文字" . $this->fieldSize['CLUBACT']['gyou'] . "行までです");
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
            if ($this->cmd == "update") {
                //更新処理
                knje012bQuery::update($db, $this);
            } elseif ($this->cmd == "update2") {
                //更新処理
                knje012bQuery::update2($db, $this);
            }
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
    }
}
