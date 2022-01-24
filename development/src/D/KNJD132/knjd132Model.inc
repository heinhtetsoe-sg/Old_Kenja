<?php

require_once('for_php7.php');
class knjd132Model extends Model {
    var $cmd;
    var $schregno;      //学籍番号
    var $field;         //画面上のデータ
    var $field2;        //画面上のデータ
    var $grade;         //学年
    var $control;
    var $mode;          //在籍卒業モード

    function knjd132Model() {
        $this->year = CTRL_YEAR;
        $this->mode = "ungrd";      //在籍モード
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->cmd     = VARS::request("cmd");
        //次の生徒
        $this->nextURL = VARS::post("nextURL");

        if (VARS::get("SCHREGNO")) {
            $this->schregno = VARS::get("SCHREGNO"); //学籍番号
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

        $this->field = array("SEMESTER"  => VARS::post("SEMESTER"),
                             "REMARK1"   => VARS::post("REMARK1"),
                             "REMARK2"   => VARS::post("REMARK2")
                             );

        $this->field["SEMESTER"] = $this->field["SEMESTER"] ? $this->field["SEMESTER"] : CTRL_SEMESTER;

        //生徒一覧を更新
        if (VARS::get("init")) {
            unset($this->schregno); //学籍番号
            unset($this->name);     //生徒氏名
        }
    }

    //入力項目チェック
    function validateInput() {
        if (!isset($this->schregno)) return false;

        if ($this->validate_row_cnt($this->fields["REMARK1"][$i], (40 * 2)) > 1) {
            $this->setWarning("MSG915","( 備考1は1行までです )");
            return false;
        }

        if ($this->validate_row_cnt($this->fields["REMARK2"][$i], (40 * 2)) > 1) {
            $this->setWarning("MSG915","( 備考2は1行までです )");
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
                knjd132Query::update($db, $this);
            }
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
    }
}
?>
