<?php

require_once('for_php7.php');
class knjd132fModel extends Model {

    var $cmd;
    var $schregno = "";
    var $name = "";
    var $grade;
    var $field = array();

    function knjd132fModel() {
    }

    function init() {
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::get("PROGRAMID")!="") {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->target);
        }

        $this->field = array("COMMUNICATION"      =>  VARS::post("COMMUNICATION"),         //備考
                             "MORAL" => VARS::post("MORAL")  //道徳
                             );
        //特別活動の記録
        for ($cnt = 1; $cnt <= $this->semeCnt; $cnt++) {
            if ($cnt == CTRL_SEMESTER) {
                $this->field["SPECIALACTREMARK".$cnt] = VARS::post("SPECIALACTREMARK".$cnt);
            }
        }

        $this->cmd = VARS::request("cmd");

        if (VARS::get("SCHREGNO")!="") {
            $this->schregno   = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME")!="") {
            $this->name       = VARS::get("NAME");
        }
        if (VARS::get("GRADE")!="") {
            $this->grade       = VARS::get("GRADE");
        }

        if (VARS::get("target") != "") {
            $this->target   = VARS::get("target");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->schregno);
            unset($this->name);
            unset($this->target);
        }

        //校種取得
        $db = Query::dbCheckOut();
        $this->schoolkind = $db->getOne(knjd132fQuery::getSchoolkind($this->grade));
        Query::dbCheckIn($db);

        //入力文字初期値の設定
        //特別活動の記録
        if ($this->Properties["HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_{$this->schoolkind}"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_{$this->schoolkind}"]);
            $this->specialactremark_moji = (int)trim($moji);
            $this->specialactremark_gyou = (int)trim($gyou);
        } else {
            $this->specialactremark_moji = 37;
            $this->specialactremark_gyou = 5;
        }

        //道徳
        if ($this->Properties["HREPORTREMARK_DAT_MORAL_SIZE_{$this->schoolkind}"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_MORAL_SIZE_{$this->schoolkind}"]);
            $this->moral_moji = (int)trim($moji);
            $this->moral_gyou = (int)trim($gyou);
        } else {
            $this->moral_moji = 41;
            $this->moral_gyou = 3;
        }

        //備考
        if ($this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_{$this->schoolkind}"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_{$this->schoolkind}"]);
            $this->communication_moji = (int)trim($moji);
            $this->communication_gyou = (int)trim($gyou);
        } else {
            $this->communication_moji = 39;
            $this->communication_gyou = 5;
        }
    }

    //入力項目チェック
    function validateInput() {
        if ($this->schregno=="") {
            $this->setWarning("MSG304");
            return false;
        }
        for ($cnt = 1; $cnt <= $this->semeCnt; $cnt++) {
            if (strlen($this->field["SPECIALACTREMARK".$cnt]) > 780) {
                $this->setWarning("MSG915","( 特別活動の記録は780バイトまでです )");
                return false;
            }
        }
        if (strlen($this->field["MORAL"]) > 373) {
            $this->setWarning("MSG915","( 道徳は373バイトまでです )");
            return false;
        }
        if (strlen($this->field["COMMUNICATION"]) > 780) {
            $this->setWarning("MSG915","( 備考は780バイトまでです )");
            return false;
        }
        return true;
    }

    function &getUpdateModel() {
        if (!$this->validateInput()) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }

    function &update() {
        knjd132fQuery::getInsertQuery($this->field, $this->schregno, $this);
        return $result;
    }
}
?>
