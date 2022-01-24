<?php

require_once('for_php7.php');

class knjd132tModel extends Model {
    var $field = array();
    var $data  = array();
    var $cmd;
    var $schregno = "";

    /**
     * メンバ変数の初期化
     */
    function knjd132tModel() {
    }

    function init() {
        if (VARS::request("PROGRAMID")) {
            unset($this->schregno);
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }
        
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "DATADIV"          =>  VARS::post("DATADIV"),          //radio
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd     = VARS::request("cmd");
        $this->windowWidth  = VARS::post("windowWidth");
        $this->windowHeight = VARS::post("windowHeight");

        //年組より校種を取得
        $db = Query::dbCheckOut();
        $grade = !strlen($this->field["GRADE_HR_CLASS"]) ? "" : substr($this->field["GRADE_HR_CLASS"],0,2);
        $query = knjd132tQuery::getSchoolKind($grade);
        $school_kind = $db->getOne($query);
        $school_kind = !strlen($school_kind) ? "H" : $school_kind;
        $this->schoolKind = $school_kind;
        Query::dbCheckIn($db);

        /**総合的な学習の時間**/
        //学習活動
        if ($this->Properties["HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_".$school_kind]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_".$school_kind]);
            $this->getPro["TOTALSTUDYTIME"] = $moji * $gyou;
        } else {
            $this->getPro["TOTALSTUDYTIME"] = 15;
        }
        //観点
        if ($this->Properties["HREPORTREMARK_DAT_REMARK1_SIZE_".$school_kind]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_REMARK1_SIZE_".$school_kind]);
            $this->getPro["VIEWPOINT"] = $moji * $gyou;
        } else {
            $this->getPro["VIEWPOINT"] = 15;
        }
        //学習活動の様子
        if ($this->Properties["HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_".$school_kind]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_".$school_kind]);
            $this->getPro["SPECIALACTREMARK"] = $moji * $gyou;
        } else {
            $this->getPro["SPECIALACTREMARK"] = 110;
        }

        /**特別活動の記録**/
        //学級活動
        if ($this->Properties["HREPORTREMARK_DETAIL_DAT_01_01_REMARK1_SIZE_".$school_kind]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DETAIL_DAT_01_01_REMARK1_SIZE_".$school_kind]);
            $this->getPro["CLASS_ACT"] = $moji * $gyou;
        } else {
            $this->getPro["CLASS_ACT"] = 22;
        }
        //生徒会活動
        if ($this->Properties["HREPORTREMARK_DETAIL_DAT_01_01_REMARK1_SIZE_".$school_kind]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DETAIL_DAT_01_01_REMARK1_SIZE_".$school_kind]);
            $this->getPro["SCH_COUNCIL"] = $moji * $gyou;
        } else {
            $this->getPro["SCH_COUNCIL"] = 22;
        }
        //学校行事
        if ($this->Properties["HREPORTREMARK_DETAIL_DAT_01_01_REMARK1_SIZE_".$school_kind]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DETAIL_DAT_01_01_REMARK1_SIZE_".$school_kind]);
            $this->getPro["SCHOOL_EVENT"] = $moji * $gyou;
        } else {
            $this->getPro["SCHOOL_EVENT"] = 66;
        }

        //部活動
        if ($this->Properties["HREPORTREMARK_DETAIL_DAT_01_01_REMARK1_SIZE_".$school_kind]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DETAIL_DAT_01_01_REMARK1_SIZE_".$school_kind]);
            $this->getPro["CLUB_NAME"] = $moji * $gyou;
        } else {
            $this->getPro["CLUB_NAME"] = 15;
        }
        //部活動の記録
        if ($this->Properties["HREPORTREMARK_DETAIL_DAT_01_01_REMARK1_SIZE_".$school_kind]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DETAIL_DAT_01_01_REMARK1_SIZE_".$school_kind]);
            $this->getPro["CLUB_MEMO"] = $moji * $gyou;
        } else {
            $this->getPro["CLUB_MEMO"] = 30;
        }

        //道徳欄
        if ($this->Properties["HREPORTREMARK_DETAIL_DAT_04_01_REMARK1_SIZE_".$school_kind]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DETAIL_DAT_04_01_REMARK1_SIZE_".$school_kind]);
            $this->getPro["OTHER_MORAL"] = $moji * $gyou;
        } else {
            $this->getPro["OTHER_MORAL"] = 50;
        }
        //その他の活動
        if ($this->Properties["HREPORTREMARK_DETAIL_DAT_03_01_REMARK1_SIZE_".$school_kind]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DETAIL_DAT_03_01_REMARK1_SIZE_".$school_kind]);
            $this->getPro["OTHER_ACT"] = $moji * $gyou;
        } else {
            $this->getPro["OTHER_ACT"] = 78;
        }
        //通信欄
        if ($this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_".$school_kind]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_".$school_kind]);
            $this->getPro["COMMUNICATION"] = $moji * $gyou;
        } else {
            $this->getPro["COMMUNICATION"] = 176;
        }

        if (VARS::get("SCHREGNO")!="") {
            $this->schregno = VARS::get("SCHREGNO");
        }
    }

    //入力項目チェック
    function validateInput() {
        //必須入力チェック
        if ($this->field["SEMESTER"] == "") {
            $this->setWarning("MSG304", "( 学期 )");
            return false;
        }
        //必須入力チェック
        if ($this->field["GRADE_HR_CLASS"] == "") {
            $this->setWarning("MSG304", "( 年組 )");
            return false;
        }

        if (get_count($this->data["SCHREGNO"]) == 0) {
            $this->setWarning("MSG303");
            return false;
        }

        //桁数チェック
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            $schregno = $this->data["SCHREGNO"][$i];
            foreach ($this->setFieldName as $key => $fieldName) {
                if (strlen($this->fields[$fieldName][$schregno]) > $this->getPro[$fieldName] * 3) {
                    $this->setWarning("MSG915","({$key}は".($this->getPro[$fieldName] * 3)."バイトまでです。No:{$this->data["ATTENDNO"][$schregno]})");
                    return false;
                }
            }
        }
        return true;
    }

    function &getUpdateModel() {
        //初期化
        $this->fields = array();
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            $schregno = $this->data["SCHREGNO"][$i];
            foreach ($this->setFieldName as $key => $fieldName) {
                $term_name = $fieldName."-".$schregno;
                $this->fields[$fieldName][$schregno] = VARS::post($term_name);
            }
        }

        if ($this->validateInput()) {
            knjd132tQuery::update($this);
            $this->setMessage("MSG201");
        }
    }
}
//var_dump($_REQUEST);
?>
