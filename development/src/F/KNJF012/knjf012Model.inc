<?php

require_once('for_php7.php');
class knjf012Model extends Model {

    var $cmd;
    var $schregno;      //学籍番号
    var $name;          //生徒名
    var $field = array();
    var $control = array();

    function knjf012Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $this->cmd = VARS::request("cmd");

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::get("PROGRAMID")) {
            unset($this->schregno);
            unset($this->name);
        }
        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->schregno);
            unset($this->name);
        }

        if (VARS::get("SCHREGNO")){
            $this->schregno  = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME")) {
            $this->name = VARS::get("NAME");
        }

        $this->field = array("NO"                   =>  VARS::post("NO"),                   //回数
                             "DATE"                 =>  VARS::post("DATE"),                 //健康診断実施日付
                             "HEIGHT"               =>  VARS::post("HEIGHT"),               //身長
                             "WEIGHT"               =>  VARS::post("WEIGHT"),               //体重
                             "SITHEIGHT"            =>  VARS::post("SITHEIGHT"),            //座高
                             "R_BAREVISION"         =>  VARS::post("R_BAREVISION"),         //視力・右裸眼（数字）
                             "R_BAREVISION_MARK"    =>  VARS::post("R_BAREVISION_MARK"),    //視力・右裸眼（文字）
                             "R_VISION"             =>  VARS::post("R_VISION"),             //視力・右矯正（数字）
                             "R_VISION_MARK"        =>  VARS::post("R_VISION_MARK"),        //視力・右矯正（文字）
                             "L_BAREVISION"         =>  VARS::post("L_BAREVISION"),         //視力・左裸眼（数字）
                             "L_BAREVISION_MARK"    =>  VARS::post("L_BAREVISION_MARK"),    //視力・左裸眼（文字）
                             "L_VISION"             =>  VARS::post("L_VISION"),             //視力・左矯正（数字）
                             "L_VISION_MARK"        =>  VARS::post("L_VISION_MARK")         //視力・左矯正（文字）
                            );
    }

    //入力項目チェック
    function validateInput() {
        if ($this->schregno == "") {
            $this->setWarning("MSG308");
            return false;
        }

        //必須入力チェック
        if($this->field["NO"] == "") {
            $this->setWarning("MSG301","　（ 回数 ）");
            return false;
        }
        if($this->field["DATE"] == "") {
            $this->setWarning("MSG301","（健康診断実施日）");
            return false;
        }

        //値チェック
        $height = explode(".",$this->field["HEIGHT"]);
        $weight = explode(".",$this->field["WEIGHT"]);
        $sitheight = explode(".",$this->field["SITHEIGHT"]);
        if (strlen($height[0]) > 3){
            $this->setWarning("MSG901","(身長)");
            return false;
        } else if (strlen($height[1]) > 1) {
            $this->setWarning("MSG901","(身長) 小数点第一位まで");
            return false;
        }
        if (strlen($weight[0]) > 3) {
            $this->setWarning("MSG901","(体重)");
            return false;
        } else if (strlen($weight[1]) > 1) {
            $this->setWarning("MSG901","(体重) 小数点第一位まで");
            return false;
        }
        if (strlen($sitheight[0]) > 3) {
            $this->setWarning("MSG901","(座高)");
            return false;
        } else if (strlen($sitheight[1]) > 1) {
            $this->setWarning("MSG901","(座高) 小数点第一位まで");
            return false;
        }

        $r_barevision = explode(".",$this->field["R_BAREVISION"]);
        $r_vision = explode(".",$this->field["R_VISION"]);
        $l_barevision = explode(".",$this->field["L_BAREVISION"]);
        $l_vision = explode(".",$this->field["L_VISION"]);
        if (strlen($r_barevision[0]) > 1) {
            $this->setWarning("MSG901","(視力・右裸眼)");
            return false;
        } else if (strlen($r_barevision[1]) > 2) {
            $this->setWarning("MSG901","(視力・右裸眼) 小数点第２位まで");
            return false;
        }
        if (strlen($r_vision[0]) > 1) {
            $this->setWarning("MSG901","(視力・右矯正)");
            return false;
        } else if (strlen($r_vision[1]) > 2) {
            $this->setWarning("MSG901","(視力・右矯正) 小数点第２位まで");
            return false;
        }
        if (strlen($l_barevision[0]) > 1) {
            $this->setWarning("MSG901","(視力・左裸眼)");
            return false;
        } else if (strlen($l_barevision[1]) > 2) {
            $this->setWarning("MSG901","(視力・左裸眼) 小数点第２位まで");
            return false;
        }
        if (strlen($l_vision[0]) > 1) {
            $this->setWarning("MSG901","(視力・左矯正)");
            return false;
        } else if (strlen($l_vision[1]) > 2) {
            $this->setWarning("MSG901","(視力・左矯正) 小数点第２位まで");
            return false;
        }

        if (strlen($r_barevision[0]) > 0) 
            $this->field["R_BAREVISION"] = $r_barevision[0] . "." . sprintf("%-02s", $r_barevision[1]);
        if (strlen($r_vision[0]) > 0) 
            $this->field["R_VISION"] = $r_vision[0] . "." . sprintf("%-02s", $r_vision[1]);
        if (strlen($l_barevision[0]) > 0) 
            $this->field["L_BAREVISION"] = $l_barevision[0] . "." . sprintf("%-02s", $l_barevision[1]);
        if (strlen($l_vision[0]) > 0) 
            $this->field["L_VISION"] = $l_vision[0] . "." . sprintf("%-02s", $l_vision[1]);

        //置換
        $this->field["DATE"] = str_replace("/","-",$this->field["DATE"]);

        return true;
    }

    //更新処理
    function &getUpdateModel() {
        if (!$this->validateInput()) {
            return;
        }
        //視力の文字チェック
        $this->checkVisionMark($this->field);

        //更新
        $this->update();
        $this->setMessage("MSG201");
    }

    //視力の文字チェック
    function checkVisionMark($fieldArray) {
        $visions = array('R_BAREVISION', 'R_VISION', 'L_BAREVISION', 'L_VISION');
        foreach ($visions as $val) {
            $valMark = $val . "_MARK";
            if (!$fieldArray[$valMark] && $fieldArray[$val] != '') {
                $query = knjf012Query::getVisionMark();
                $db = Query::dbCheckOut();
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($fieldArray[$val] >= $row["NAMESPARE1"]){
                        $fieldArray[$valMark] = $row["NAME1"];
                    } else {
                        break 1;
                    }
                }
                Query::dbCheckIn($db);
            }
        }
    }

    //更新
    function &update() {

        //MEDEXAM_DET_NO_DAT更新
        knjf012Query::updateMedexamDetNoDat($this);

        //1回目のみMEDEXAM_HDAT、MEDEXAM_DET_DATを更新
        if ($this->field["NO"] == "1") {
            knjf012Query::updateMedexamHdat($this);
            knjf012Query::updateMedexamDetDat($this);
        }

        return;
    }

    //削除処理
    function &getDeleteModel() {
        if ($this->schregno == "") {
            $this->setWarning("MSG308");
            return false;
        }
        //必須入力チェック
        if($this->field["NO"] == "") {
            $this->setWarning("MSG301","　（ 回数 ）");
            return false;
        }

        //削除
        knjf012Query::deleteMedexamDetNoDat($this);

        $this->setMessage("MSG201");
        return;
    }
}
?>
