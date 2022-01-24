<?php

require_once('for_php7.php');
class knja126mModel extends Model {

    var $cmd;
    var $exp_year = "";
    var $exp_semester = "";
    var $schregno = "";
    var $name = "";
    var $field = array();
    var $control;
    var $annual;        //学年(年次)

    function knja126mModel()
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
        }

        if (VARS::post("ANNUAL")){   //学年（年次）
            list($tmp["YEAR"], $tmp["ANNUAL"]) = explode(",", VARS::post("ANNUAL"));
            $this->annual = $tmp;
        }

        $this->field = array("TOTALSTUDYACT"        =>  VARS::post("TOTALSTUDYACT"),
                             "TOTALSTUDYVAL"        =>  VARS::post("TOTALSTUDYVAL"),
                             "SPECIALACTREMARK"     =>  VARS::post("SPECIALACTREMARK"),
                             "TOTALREMARK"          =>  VARS::post("TOTALREMARK"),
                             "ATTENDREC_REMARK"     =>  VARS::post("ATTENDREC_REMARK"),
                             "UPDATED"              =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::get("EXP_YEAR")!="") {
            $this->exp_year     = VARS::get("EXP_YEAR");
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }
        if (VARS::get("SCHREGNO")!="") {
            $this->schregno     = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME")!="") {
            $this->name         = VARS::get("NAME");
        }
        $this->r_year = VARS::request("R_YEAR");

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->annual);
            unset($this->r_year);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

    }

    //入力項目チェック
    function validateInput()
    {
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }
        if ($this->validate_row_cnt($this->field["TOTALSTUDYACT"], (44 * 2)) > 3) {
            $this->setWarning("MSG915","(活動内容は3行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["TOTALSTUDYVAL"], (44 * 2)) > 3) {
            $this->setWarning("MSG915","(評価は3行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["SPECIALACTREMARK"], (11 * 2)) > 6) {
            $this->setWarning("MSG915","(特別活動所見は6行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["TOTALREMARK"], (22 * 2)) > 6) {
            $this->setWarning("MSG915","(総合所見は6行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["ATTENDREC_REMARK"], (40 * 2)) > 1) {
            $this->setWarning("MSG915","(出欠の記録備考は80バイトまでです)");
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
        knja126mQuery::getDeleteQuery($this->schregno, $this->exp_year);
        knja126mQuery::getInsertQuery($this->field, $this->schregno, $this->exp_year, $this->exp_semester);
        return ;
    }

    //ハッシュ値を作る
    function makeHash($row, $study, $attend) {

        $data = $row["TOTALSTUDYACT"].$row["TOTALSTUDYVAL"].$row["SPECIALACTREMARK"].
                $row["TOTALREMARK"].$row["ATTENDREC_REMARK"].$study.$attend;

        return sha1($data);
    }
}
?>
