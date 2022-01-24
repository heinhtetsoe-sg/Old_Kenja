<?php

require_once('for_php7.php');

class knjl410_1Model extends Model {
    var $auth;
    var $recruit_no;
    var $field = array();
    var $cmd;
    var $control;        //コントロールマスタ

    function knjl410_1Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->auth);
            unset($this->recruit_no);
        }
        if (VARS::get("SEND_PRGRID")){
            $this->getPrgId     = VARS::get("SEND_PRGRID");
            $this->sendAuth     = VARS::get("SEND_AUTH");
            $this->recruit_no   = VARS::get("SEND_RECRUIT_NO");
//echo "model=" .$this->recruit_no ."<BR>";
        }
        $this->auth = ($this->getPrgId) ? $this->sendAuth : AUTHORITY;

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();  //プロパティファイルの読込み

        //以下、入力項目

        //1.RECRUIT_VISIT_DAT
        $this->field = array("TOUROKU_DATE"         =>  VARS::post("TOUROKU_DATE"),
                             "KAKUTEI_DATE"         =>  VARS::post("KAKUTEI_DATE"),
                             "HOPE_COURSE"          =>  VARS::post("HOPE_COURSE"),
                             "TESTDIV"              =>  VARS::post("TESTDIV"),
                             "STAFFCD"              =>  VARS::post("STAFFCD"),
                             "JUDGE_KIND"           =>  VARS::post("JUDGE_KIND"),
                             "SCHOOL_DIV"           =>  VARS::post("SCHOOL_DIV"),
                             "SCHOOL_NAME"          =>  VARS::post("SCHOOL_NAME"),
                             "SCORE_CHK"            =>  VARS::post("SCORE_CHK"),
                             "MOCK_CHK"             =>  VARS::post("MOCK_CHK"),
                             "REMARK1"              =>  VARS::post("REMARK1")
                            );
        //2.RECRUIT_VISIT_SCORE_DAT
        //SEMESTER='1'-'2'
        for ($sem = 1; $sem <= 2; $sem++) {
            for ($s = 1; $s <= 9; $s++) {
                $sub = sprintf("%02d", $s);
                $this->field["SUBCLASSCD{$sub}"."_S_{$sem}"] = VARS::post("SUBCLASSCD{$sub}"."_S_{$sem}");
            }
            $this->field["TOTAL3"."_S_{$sem}"] = VARS::post("TOTAL3"."_S_{$sem}");
            $this->field["TOTAL5"."_S_{$sem}"] = VARS::post("TOTAL5"."_S_{$sem}");
            $this->field["TOTAL9"."_S_{$sem}"] = VARS::post("TOTAL9"."_S_{$sem}");
            $this->field["SELECT_DIV"."_S_{$sem}"] = VARS::post("SELECT_DIV"."_S_{$sem}");
        }
        //3.RECRUIT_VISIT_MOCK_DAT
        //INSERT(MONTH='04'-'12')
        for ($i = 4; $i <= 12; $i++) {
            $mon = sprintf("%02d", $i);
            for ($s = 1; $s <= 5; $s++) {
                $sub = sprintf("%02d", $s);
                $this->field["SUBCLASSCD{$sub}"."_M_{$mon}"] = VARS::post("SUBCLASSCD{$sub}"."_M_{$mon}");
            }
            $this->field["AVG3"."_M_{$mon}"] = VARS::post("AVG3"."_M_{$mon}");
            $this->field["AVG5"."_M_{$mon}"] = VARS::post("AVG5"."_M_{$mon}");
            $this->field["COMPANYCD"."_M_{$mon}"] = VARS::post("COMPANYCD"."_M_{$mon}");
            $this->field["COMPANY_TEXT"."_M_{$mon}"] = VARS::post("COMPANY_TEXT"."_M_{$mon}");
        }
        //INSERT(MONTH='99')
        for ($i = 99; $i <= 99; $i++) {
            $mon = sprintf("%02d", $i);
            $this->field["TOP1_AVG3"."_M_{$mon}"] = VARS::post("TOP1_AVG3"."_M_{$mon}");
            $this->field["TOP1_AVG5"."_M_{$mon}"] = VARS::post("TOP1_AVG5"."_M_{$mon}");
            $this->field["TOP1_COMPANYCD"."_M_{$mon}"] = VARS::post("TOP1_COMPANYCD"."_M_{$mon}");
            $this->field["TOP1_COMPANY_TEXT"."_M_{$mon}"] = VARS::post("TOP1_COMPANY_TEXT"."_M_{$mon}");
            $this->field["TOP2_AVG3"."_M_{$mon}"] = VARS::post("TOP2_AVG3"."_M_{$mon}");
            $this->field["TOP2_AVG5"."_M_{$mon}"] = VARS::post("TOP2_AVG5"."_M_{$mon}");
            $this->field["TOP2_COMPANYCD"."_M_{$mon}"] = VARS::post("TOP2_COMPANYCD"."_M_{$mon}");
            $this->field["TOP2_COMPANY_TEXT"."_M_{$mon}"] = VARS::post("TOP2_COMPANY_TEXT"."_M_{$mon}");
            $this->field["TOP_AVG"."_M_{$mon}"] = VARS::post("TOP_AVG"."_M_{$mon}");
        }
        //4.RECRUIT_VISIT_ACTIVE_DAT
        $db = Query::dbCheckOut();
        $this->actArray = array();
        $query = knjl410_1Query::getNameMstL408();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->actArray[] = array('LABEL' => $row["LABEL"], 'VALUE' => $row["VALUE"], 'POINT' => $row["POINT"]);
        }
        $result->free();
        Query::dbCheckIn($db);
        //INSERT(SEQ_DIV='1')
        foreach ($this->actArray as $actKey => $actRow) {
            $i = $actRow["VALUE"];
            $div = "1";
            $seq = sprintf("%03d", $i);
            $this->field["REMARK1"."_A_{$div}_{$seq}"] = VARS::post("REMARK1"."_A_{$div}_{$seq}");
        }
        //INSERT(SEQ_DIV='2')
        for ($i = 1; $i <= 2; $i++) {
            $div = "2";
            $seq = sprintf("%03d", $i);
            $this->field["REMARK1"."_A_{$div}_{$seq}"] = VARS::post("REMARK1"."_A_{$div}_{$seq}");
        }
    }

    //入力項目チェック
    function validateInput() {
        if ($this->recruit_no == "") {
            $this->setWarning("MSG309");
            return false;
        }

        if ($this->field["TOUROKU_DATE"] == "") {
            $this->setWarning("MSG301", "　　　　　（ 登録日付 ）");
            return false;
        }

        //文字数チェック
        if ($this->validate_row_cnt($this->field["SCHOOL_NAME"], (25 * 2)) > 1) {
            $this->setWarning("MSG915","(志望校名は1行までです)");
            return false;
        }

        return true;
    }

    //更新用のモデルを返します
    function &getUpdateModel() {

        if (!$this->validateInput()) {
            return;
        }

        //更新
        knjl410_1Query::getUpdateQuery($this, $this->field);
        $this->setMessage("MSG201");

        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel() {

        if ($this->recruit_no == "") {
            $this->setWarning("MSG309");
            return;
        }
        if ($this->field["TOUROKU_DATE"] == "") {
            $this->setWarning("MSG301", "　　　　　（ 登録日付 ）");
            return;
        }

        //削除
        knjl410_1Query::getDeleteQuery($this);

        $this->field = array();

        $this->setMessage("MSG201");

        return;
    }
}
?>
