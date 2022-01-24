<?php

require_once('for_php7.php');

class knja126pModel extends Model {

    var $cmd;
    var $exp_year;
    var $exp_semester;
    var $schregno;
    var $name;
    var $field = array();
    var $record = array();
    var $control;

    function knja126pModel()
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

        $this->field = array("TOTALSTUDYACT"        =>  VARS::post("TOTALSTUDYACT"),
                             "TOTALSTUDYVAL"        =>  VARS::post("TOTALSTUDYVAL"),
                             "TOTALREMARK"          =>  VARS::post("TOTALREMARK"),
                             "ATTENDREC_REMARK"     =>  VARS::post("ATTENDREC_REMARK"),
                             "VIEWREMARK"           =>  VARS::post("VIEWREMARK"),
                             "FOREIGNLANGACT1"      =>  VARS::post("FOREIGNLANGACT1"),
                             "FOREIGNLANGACT2"      =>  VARS::post("FOREIGNLANGACT2"),
                             "FOREIGNLANGACT3"      =>  VARS::post("FOREIGNLANGACT3"),
                             "SPECIALACTREMARK"     =>  VARS::post("SPECIALACTREMARK"),
                             "UPDATED"              =>  VARS::post("UPDATED")
                             );

        //行動の記録
        for($i=1; $i<11; $i++)
        {
            $ival = "3" . sprintf("%02d", $i);
            $this->record["RECORD"][$ival] = VARS::post("RECORD".$ival);
        }

        //特別活動の記録
        for($i=1; $i < get_count($this->itemArray) + 1; $i++)
        {
            $ival = "4" . sprintf("%02d", $i);
            $this->record["RECORD"][$ival] = VARS::post("RECORD".$ival);
        }

        $this->cmd = VARS::request("cmd");

        if (VARS::get("EXP_YEAR") != "") {
            $this->exp_year     = VARS::get("EXP_YEAR");
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }
        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME") != "") {
            $this->name = VARS::get("NAME");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();

        if ($this->Properties["HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_P"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_P"]);
            $this->attendrec_remark_moji = (int)trim($moji);
            $this->attendrec_remark_gyou = (int)trim($gyou);
        } else {
            $this->attendrec_remark_moji = 35;
            $this->attendrec_remark_gyou = 1;
        }

        if ($this->Properties["HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_P"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_P"]);
            $this->specialactremark_moji = (int)trim($moji);
            $this->specialactremark_gyou = (int)trim($gyou);
        } else {
            $this->specialactremark_moji = 16;
            $this->specialactremark_gyou = 4;
        }
    }

    //入力項目チェック
    function validateInput()
    {
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }

        $db = Query::dbCheckOut();
        //署名チェック
        $check = $db->getRow(knja126pQuery::getOpinionsWk($this), DB_FETCHMODE_ASSOC);
        if ($check["CHAGE_OPI_SEQ"] || $check["LAST_OPI_SEQ"]) {
            $this->setWarning("MSG917","（署名済み）");
            return false;
        }
        Query::dbCheckIn($db);

        if ($this->validate_row_cnt($this->field["TOTALSTUDYACT"], (8 * 2)) > 8) {
            $this->setWarning("MSG915","(学習活動は8行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["TOTALSTUDYVAL"], (15 * 2)) > 8) {
            $this->setWarning("MSG915","(評価は8行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["VIEWREMARK"], (7 * 2)) > 8) {
            $this->setWarning("MSG915","(観点は8行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["TOTALREMARK"], (22 * 2)) > 15) {
            $this->setWarning("MSG915","(総合所見は15行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["FOREIGNLANGACT1"], (10 * 2)) > 4) {
            $this->setWarning("MSG915","(コミュニケーションへの関心・意欲・態度は4行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["FOREIGNLANGACT2"], (10 * 2)) > 4) {
            $this->setWarning("MSG915","(外国語への慣れ親しみは4行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["FOREIGNLANGACT3"], (10 * 2)) > 4) {
            $this->setWarning("MSG915","(言語や文化に関する気付きは4行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["ATTENDREC_REMARK"], ((int)$this->attendrec_remark_moji * 2)) > $this->attendrec_remark_gyou) {
            $this->setWarning("MSG915","(出欠の記録備考は{$this->attendrec_remark_gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["SPECIALACTREMARK"], (17 * 2)) > 10) {
            $this->setWarning("MSG915","(特別活動の記録の観点は10行までです)");
            return false;
        }

        return true;
    }

    function &getUpdateModel()
    {
        if (!$this->validateInput()) {
            return;
        }
        if ($this->cmd == "update") $this->update();
        if ($this->cmd == "update2") $this->update2();
        $this->setMessage("MSG201");
    }

    function &update()
    {
        knja126pQuery::getInsertQuery($this);

        return ;
    }

    function &update2()
    {
        knja126pQuery::getInsertQuery2($this);

        return ;
    }

    //ハッシュ値を作る
    function makeHash($row, $behavior, $study, $attend) {

        $data = $row["FOREIGNLANGACT1"].$row["FOREIGNLANGACT2"].$row["FOREIGNLANGACT3"].$row["TOTALREMARK"].
                $row["TOTALSTUDYACT"].$row["VIEWREMARK"].$row["TOTALSTUDYVAL"].$row["ATTENDREC_REMARK"].$behavior.
                $row["SPECIALACTREMARK"].$study.$attend["CLASSDAYS"].$attend["OFFDAYS"].$attend["ABSENT"].
                $attend["SUSPEND"].$attend["MOURNING"].$attend["ABROAD"].$attend["REQUIREPRESENT"].$attend["SICK"].
                $attend["ACCIDENTNOTICE"].$attend["NOACCIDENTNOTICE"].$attend["PRESENT"];

        return sha1($data);
    }
}
?>
