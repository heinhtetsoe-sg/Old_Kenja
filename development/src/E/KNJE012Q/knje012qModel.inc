<?php

require_once('for_php7.php');

class knje012qModel extends Model {
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
    var $attendrec_remark_moji; //出欠の記録備考の文字数
    var $attendrec_remark_gyou; //出欠の記録備考の行数

    var $remark_moji;    //備考の文字数
    var $remark_gyou;    //備考の行数

    var $no_comments;           //チェック（特記事項なし）
    var $no_comments_label;     //文言（特記事項なし）

    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    function knje012qModel() {
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

        //表示項目
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

        $this->field = array("REMARK"         => VARS::post("REMARK")             //備考
                             );

        foreach ($this->schArray as $key => $val) {
            $year = $val["YEAR"];
            $this->field2[$year] = array("ATTENDREC_REMARK" => VARS::post("ATTENDREC_REMARK-".$year)    //出欠の記録備考
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

        //「特記事項なし」チェックボックス
        $this->no_comments_label = "特記事項なし";
        $this->no_comments = VARS::post("NO_COMMENTS");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //出欠の記録備考の文字数と行数の設定
        if ($this->Properties["tyousasyoAttendrecRemarkFieldSize_J"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["tyousasyoAttendrecRemarkFieldSize_J"]);
            $this->attendrec_remark_moji = (int)trim($moji);
            $this->attendrec_remark_gyou = (int)trim($gyou);
        } else {
            $this->attendrec_remark_moji = 8; //デフォルトの値
            $this->attendrec_remark_gyou = 5; //デフォルトの値
        }

        //備考の文字数と行数の設定
        if ($this->Properties["tyousasyoRemarkFieldSize_J"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["tyousasyoRemarkFieldSize_J"]);
            $this->remark_moji = (int)trim($moji);
            $this->remark_gyou = (int)trim($gyou);
        } else {
            $this->remark_moji = 45; //デフォルトの値
            $this->remark_gyou = 5;  //デフォルトの値
        }
    }

    //入力項目チェック
    function validateInput() {
        if (!isset($this->schregno)) return false;

        if ($this->validate_row_cnt($this->field["REMARK"], ($this->remark_moji * 2)) > $this->remark_gyou) {
            $this->setWarning("MSG915","(備考は{$this->remark_gyou}行までです)");
            return false;
        }

        if ($this->validateKinsoku($this->field["REMARK"])) {
            $this->setWarning("MSG916","(備考はスペースにつづいて長音（ー）を含んでいます。)");
            return false;
        }

        foreach ($this->schArray as $key => $val) {
            $year = $val["YEAR"];

            //出欠の記録備考
            if ($this->validate_row_cnt($this->field2[$year]["ATTENDREC_REMARK"], ($this->attendrec_remark_moji * 2)) > $this->attendrec_remark_gyou) {
                $this->setWarning("MSG915","(出欠の記録備考は{$this->attendrec_remark_gyou}行までです)");
                return false;
            }
            if ($this->validateKinsoku($this->field2[$year]["ATTENDREC_REMARK"])) {
                $this->setWarning("MSG916","(出欠の記録備考はスペースにつづいて長音（ー）を含んでいます。)");
                return false;
            }
        }
        return true;
    }

    function validateKinsoku($str) {
        if ($this->Properties["tyousasyoKinsokuForm"] == '1') {
            if (false !== strpos($str, '　ー') || false !== strpos($str, ' ー')) {
                return true;
            }
        }
        return false;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        if ($this->validateInput()) {
            $db = Query::dbCheckOut();
            $db->autoCommit(false);
            if ($this->cmd == "update") {
                //更新処理
                knje012qQuery::update($db, $this);

                //更新処理
                foreach ($this->schArray as $key => $val) {
                    knje012qQuery::update2($db, $this, $val);
                }
            }
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
    }
}
?>
