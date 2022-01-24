<?php

require_once('for_php7.php');
class knja121aModel extends Model {

    var $cmd;
    var $exp_year = "";
    var $exp_semester = "";
    var $schregno = "";
    var $name = "";
    var $field = array();
    var $record = array();
    var $control;

    function knja121aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        if (VARS::get("PROGRAMID")!="") {
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
                             "BEHAVEREC_REMARK"     =>  VARS::post("BEHAVEREC_REMARK"),
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

        if (VARS::get("EXP_YEAR")!="") {
            $this->exp_year     = VARS::get("EXP_YEAR");
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }
        if (VARS::get("SCHREGNO")!="") {
            $this->schregno   = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME")!="") {
            $this->name       = VARS::get("NAME");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
        }

        //年度コンボ（通知表所見）
        $this->year_cmb = VARS::request("YEAR_CMB");
        
        //ここから追加--
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if ($this->Properties["HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_J_OLD"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_J_OLD"]);
            $this->getPro["TOTALSTUDYACT"]["moji"] = (int)trim($moji);
            $this->getPro["TOTALSTUDYACT"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["TOTALSTUDYACT"]["moji"] = 20;
            $this->getPro["TOTALSTUDYACT"]["gyou"] = 4;
        }
        if ($this->Properties["HTRAINREMARK_DAT_VIEWREMARK_SIZE_J_OLD"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_VIEWREMARK_SIZE_J_OLD"]);
            $this->getPro["VIEWREMARK"]["moji"] = (int)trim($moji);
            $this->getPro["VIEWREMARK"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["VIEWREMARK"]["moji"] = 10;
            $this->getPro["VIEWREMARK"]["gyou"] = 4;
        }
        if ($this->Properties["HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_J_OLD"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_J_OLD"]);
            $this->getPro["TOTALSTUDYVAL"]["moji"] = (int)trim($moji);
            $this->getPro["TOTALSTUDYVAL"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["TOTALSTUDYVAL"]["moji"] = 20;
            $this->getPro["TOTALSTUDYVAL"]["gyou"] = 4;
        }
        if ($this->Properties["HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J_OLD"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J_OLD"]);
            $this->getPro["ATTENDREC_REMARK"]["moji"] = (int)trim($moji);
            $this->getPro["ATTENDREC_REMARK"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["ATTENDREC_REMARK"]["moji"] = 20;
            $this->getPro["ATTENDREC_REMARK"]["gyou"] = 1;
        }
        if ($this->Properties["HTRAINREMARK_DAT_TOTALREMARK_SIZE_J_OLD"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_DAT_TOTALREMARK_SIZE_J_OLD"]);
            $this->getPro["TOTALREMARK"]["moji"] = (int)trim($moji);
            $this->getPro["TOTALREMARK"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["TOTALREMARK"]["moji"] = 15;
            $this->getPro["TOTALREMARK"]["gyou"] = 29;
        }
    }

    //入力項目チェック
    function validateInput()
    {
        if ($this->schregno=="") {
            $this->setWarning("MSG304");
            return false;
        }
        if ($this->validate_row_cnt($this->field["TOTALSTUDYACT"], ($this->getPro["TOTALSTUDYACT"]["moji"] * 2)) > $this->getPro["TOTALSTUDYACT"]["gyou"]) {
            $this->setWarning("MSG915","(学習活動は{$this->getPro["TOTALSTUDYACT"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["VIEWREMARK"], ($this->getPro["VIEWREMARK"]["moji"] * 2)) > $this->getPro["VIEWREMARK"]["gyou"]) {
            $this->setWarning("MSG915","(観点は{$this->getPro["VIEWREMARK"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["TOTALSTUDYVAL"], ($this->getPro["TOTALSTUDYVAL"]["moji"] * 2)) > $this->getPro["TOTALSTUDYVAL"]["gyou"]) {
            $this->setWarning("MSG915","(評価は{$this->getPro["TOTALSTUDYVAL"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["ATTENDREC_REMARK"], ($this->getPro["ATTENDREC_REMARK"]["moji"] * 2)) > $this->getPro["ATTENDREC_REMARK"]["gyou"]) {
            $this->setWarning("MSG915","(評価は{$this->getPro["ATTENDREC_REMARK"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["TOTALREMARK"], ($this->getPro["TOTALREMARK"]["moji"] * 2)) > $this->getPro["TOTALREMARK"]["gyou"]) {
            $this->setWarning("MSG915","(評価は{$this->getPro["TOTALREMARK"]["gyou"]}行までです)");
            return false;
        }
        
/*
        if (strlen($this->field["TOTALSTUDYACT"]) > 246) {
            $this->setWarning("MSG915","(学習活動は246バイトまでです)");
            return false;
        }
        if (strlen($this->field["TOTALSTUDYVAL"]) > 246) {
            $this->setWarning("MSG915","(評価は246バイトまでです)");
            return false;
        }
        if (strlen($this->field["TOTALREMARK"]) > 1361) {
            $this->setWarning("MSG915","(総合所見は1361バイトまでです)");
            return false;
        }
        if (strlen($this->field["ATTENDREC_REMARK"]) > 60) {
            $this->setWarning("MSG915","(出欠の記録備考は60バイトまでです)");
            return false;
        }
        if (strlen($this->field["VIEWREMARK"]) > 126) {
            $this->setWarning("MSG915","(観点は126バイトまでです)");
            return false;
        }
*/
        
/***
        if (strlen($this->field["BEHAVEREC_REMARK"]) > 122) {
            $this->setWarning("MSG915","(行動の記録備考は122バイトまでです)");
            return false;
        }
***/
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
        knja121aQuery::getDeleteQuery($this->schregno, $this->exp_year);
        knja121aQuery::getInsertQuery($this->field,$this->schregno, $this->exp_year, $this->exp_semester);
        return $result;
    }

    function &update2()
    {
        knja121aQuery::getDeleteQuery2($this);
        knja121aQuery::getInsertQuery2($this);

        return $result;
    }
}
?>
