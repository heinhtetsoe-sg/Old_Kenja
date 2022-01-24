<?php

require_once('for_php7.php');
class knja121bModel extends Model {

    var $cmd;
    var $exp_year = "";
    var $exp_semester = "";
    var $schregno = "";
    var $name = "";
    var $field = array();
    var $record = array();

    function knja121bModel() {}

    function init() {

        if (VARS::get("PROGRAMID") != "") {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
        }

        $this->field = array("TOTALSTUDYACT"        =>  VARS::post("TOTALSTUDYACT"),
                             "TOTALSTUDYVAL"        =>  VARS::post("TOTALSTUDYVAL"),
                             "VIEWREMARK"           =>  VARS::post("VIEWREMARK"),
                             "TOTALREMARK"          =>  VARS::post("TOTALREMARK"),
                             "ATTENDREC_REMARK"     =>  VARS::post("ATTENDREC_REMARK"),
                             "UPDATED"              =>  VARS::post("UPDATED")
                             );

        //行動の記録
        for($i=1; $i<11; $i++)
        {
            $ival = "1" . sprintf("%02d", $i);
            $this->record["RECORD"][$ival] = VARS::post("RECORD".$ival);
        }
        //特別活動の記録
        for($i=1; $i<4; $i++)
        {
            $ival = "2" . sprintf("%02d", $i);
            $this->record["RECORD"][$ival] = VARS::post("RECORD".$ival);
        }

        $this->cmd = VARS::request("cmd");

        if (!$this->cmd) {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
        }

        if (VARS::get("EXP_YEAR") != "") {
            $this->exp_year = VARS::get("EXP_YEAR");
        }
        if (VARS::get("EXP_SEMESTER") != "") {
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }
        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME") != "") {
            $this->name = VARS::get("NAME");
        }

        $this->year     = VARS::request("YEAR");
        $this->semester = VARS::request("SEMESTER");

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->year);
            unset($this->semester);
            unset($this->schregno);
            unset($this->name);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput($db)
    {
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }

        //署名チェック
        $check = $db->getRow(knja121bQuery::getOpinionsWk($this), DB_FETCHMODE_ASSOC);
        if ($check["CHAGE_OPI_SEQ"] || $check["LAST_OPI_SEQ"]) {
            $this->setWarning("MSG917","（署名済み）");
            return false;
        }

        if ($this->validate_row_cnt($this->field["TOTALSTUDYACT"], (20 * 2)) > 4) {
            $this->setWarning("MSG915","(学習活動は4行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["TOTALSTUDYVAL"], (20 * 2)) > 4) {
            $this->setWarning("MSG915","(評価は4行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["VIEWREMARK"], (10 * 2)) > 4) {
            $this->setWarning("MSG915","(観点は4行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["TOTALREMARK"], (15 * 2)) > 29) {
            $this->setWarning("MSG915","(総合所見は29行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["ATTENDREC_REMARK"], (20 * 2)) > 1) {
            $this->setWarning("MSG915","(出欠の記録備は1行までです)");
            return false;
        }

        return true;
    }

    function &getUpdateModel()
    {
        $db = Query::dbCheckOut();
        if (!$this->validateInput($db)) {
            return;
        }

        $this->update($db);
        $this->setMessage("MSG201");
        Query::dbCheckIn($db);
    }

    //アップデート
    function &update($db) {

        knja121bQuery::getDeleteSql($db, $this);
        knja121bQuery::getInsertSql($db, $this, $this->field);

        return;
    }

    //ハッシュ値を作る
    function makeHash($Row, $koudou) {

        return sha1($Row["TOTALSTUDYACT"]. $Row["TOTALSTUDYVAL"]. $Row["VIEWREMARK"]. $Row["TOTALREMARK"]. $Row["ATTENDREC_REMARK"]. $koudou);
    }

    //行動の記録備考更新
    function &getUpdateModel_koudou()
    {
        knja121bQuery::getDeleteQuery_koudou($this);
        knja121bQuery::getInsertQuery_koudou($this);
        $this->setMessage("MSG201");
    }

}
?>
