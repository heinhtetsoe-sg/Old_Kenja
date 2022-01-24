<?php

require_once('for_php7.php');
class knje012yModel extends Model {
    var $cmd;
    var $schregno;      //学籍番号
    var $field;         //画面上のデータ
    var $grade;         //学年
    var $annual;        //学年(年次)
    var $control;
    var $form1_first;   //左フレームから最初の呼出ならば、on
    var $mode;          //在籍卒業モード
    var $grd_year;      //卒業年度
    var $grd_semester;  //卒業学期
    var $exp_year;      //左フレームから送られてくる年度
    var $exp_semester;  //左フレームから送られてくる学期

    function knje012yModel() {
        $this->year = CTRL_YEAR;
        $this->mode = "ungrd";      //在籍モード
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
    
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

        $this->field = array("TOTALSTUDYVAL"    => VARS::post("TOTALSTUDYVAL"),     //総合的な学習の時間の記録
                             "CALSSACT"         => VARS::post("CALSSACT"),          //学級活動
                             "STUDENTACT"       => VARS::post("STUDENTACT"),        //生徒会活動
                             "SCHOOLEVENT"      => VARS::post("SCHOOLEVENT"),       //学校行事
                             "CLUBACT"          => VARS::post("CLUBACT"),           //その他の活動
                             "ATTENDREC_REMARK" => VARS::post("ATTENDREC_REMARK"),  //出欠の記録（主な欠席理由）
                             "REMARK"           => VARS::post("REMARK")             //参考となる諸事項等の記録
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

        //年度コンボ（通知表所見）
        $this->year_cmb = VARS::request("YEAR_CMB");
    }

    //入力項目チェック
    function validateInput() {
        if (!isset($this->schregno)) return false;

        if ($this->validate_row_cnt($this->field["TOTALSTUDYVAL"], (31 * 2)) > 4) {
            $this->setWarning("MSG915","総合的な学習の時間の記録は、4行までです");
            return false;
        }
        if ($this->validate_row_cnt($this->field["CALSSACT"], (8 * 2)) > 4) {
            $this->setWarning("MSG915","学級活動は、4行までです");
            return false;
        }
        if ($this->validate_row_cnt($this->field["STUDENTACT"], (8 * 2)) > 4) {
            $this->setWarning("MSG915","生徒会活動は、4行までです");
            return false;
        }
        if ($this->validate_row_cnt($this->field["SCHOOLEVENT"], (8 * 2)) > 4) {
            $this->setWarning("MSG915","学校行事は、4行までです");
            return false;
        }
        if ($this->validate_row_cnt($this->field["CLUBACT"], (8 * 2)) > 4) {
            $this->setWarning("MSG915","その他の活動は、4行までです");
            return false;
        }
        if ($this->validate_row_cnt($this->field["ATTENDREC_REMARK"], (24 * 2)) > 1) {
            $this->setWarning("MSG915","出欠の記録（主な欠席理由）は、1行までです");
            return false;
        }
        if (CTRL_YEAR < "2014") {
            if ($this->validate_row_cnt($this->field["REMARK"], (31 * 2)) > 12) {
                $this->setWarning("MSG915","参考となる諸事項等の記録は、12行までです");
                return false;
            }
        } else {
            if ($this->validate_row_cnt($this->field["REMARK"], (58 * 2)) > 8) {
                $this->setWarning("MSG915","参考となる諸事項等の記録は、8行までです");
                return false;
            }
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
                knje012yQuery::update($db, $this);
            }else if ($this->cmd == "update2") {
                //更新処理
                knje012yQuery::update2($db, $this);
            }
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
    }
}
?>
