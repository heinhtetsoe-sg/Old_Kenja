<?php

require_once('for_php7.php');


class knjz380qModel extends Model {
    var $field        = array();
    var $cmd;
    var $staffcd;               //ログインユーザーID保持
    var $sec_competence;        //権限保持
    var $control_data;          //処理年度保持
    var $semester;              //GETより取得
    var $testkindcd;            //GETより取得
    var $testitemcd;            //GETより取得
    var $score_div;             //GETより取得

    function knjz380qModel()
    {
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init()
    {
        //セキュリティーチェック
        $this->staffcd = STAFFCD;
        $this->sec_competence = common::SecurityCheck($this->staffcd, "KNJZ380Q");

        $this->leftCourseMajor = VARS::post("COURSE_MAJOR") ? VARS::post("COURSE_MAJOR") : $this->leftCourseMajor;
        $this->motoCourseMajor = VARS::post("MOTO_COURSE_MAJOR") ? VARS::post("MOTO_COURSE_MAJOR") : $this->motoCourseMajor;
        $this->field = array("RIGHT_COURSE_MAJOR"   =>  VARS::post("RIGHT_COURSE_MAJOR"),
                             "COUNTFLG"             =>  VARS::post("COUNTFLG"),
                             "SCHOOLCD"             =>  VARS::post("SCHOOLCD"),
                             "SCHOOL_KIND"          =>  VARS::post("SCHOOL_KIND"),
                             "SEMESTER"             =>  VARS::post("SEMESTER"),
                             "SEMESTER_DETAIL"      =>  VARS::post("SEMESTER_DETAIL"),
                             "TESTKINDCD"           =>  VARS::post("TESTKINDCD"),
                             "TESTITEMCD"           =>  VARS::post("TESTITEMCD"),
                             "SCORE_DIV"            =>  VARS::post("SCORE_DIV"),
                             "GRADE"                =>  VARS::post("GRADE"),
                             "RIGHT_COURSE_MAJOR"   =>  VARS::post("RIGHT_COURSE_MAJOR"),
                             "TESTITEMNAME"         =>  VARS::post("TESTITEMNAME"),
                             "TESTITEMABBV1"        =>  VARS::post("TESTITEMABBV1"),
                             "SIDOU_INPUT"          =>  VARS::post("SIDOU_INPUT"),
                             "SIDOU_INPUT_INF"      =>  VARS::post("SIDOU_INPUT_INF"),
                             "JYORETSU_FLG"         =>  VARS::post("JYORETSU_FLG"),
                             "NOT_USE_CSV_FLG"      =>  VARS::post("NOT_USE_CSV_FLG"),
                             "TEST_START_DATE"      =>  VARS::post("TEST_START_DATE"),
                             "TEST_END_DATE"        =>  VARS::post("TEST_END_DATE"),
                             "UPDATED"              =>  VARS::post("UPDATED")
                            );
        $this->cmd  = VARS::request("cmd");

        if(VARS::get("SCHOOLCD")){
            $this->schoolCd = VARS::get("SCHOOLCD");
            $this->field["SCHOOLCD"] = VARS::get("SCHOOLCD");
        }
        if(VARS::get("SCHOOL_KIND")){
            $this->schoolKind = VARS::get("SCHOOL_KIND");
            $this->field["SCHOOL_KIND"] = VARS::get("SCHOOL_KIND");
        }
        if(VARS::get("SEMESTER")){
            $this->semester = VARS::get("SEMESTER");
            $this->field["SEMESTER"] = $this->semester;
        }
        if(VARS::get("TESTKINDCD")){
            $this->testkindcd = VARS::get("TESTKINDCD");
            $this->field["TESTKINDCD"] = $this->testkindcd;
        }
        if(VARS::get("TESTITEMCD")){
            $this->testitemcd = VARS::get("TESTITEMCD");
        }
        if(VARS::get("SCORE_DIV")){
            $this->score_div = VARS::get("SCORE_DIV");
        }
        if(VARS::get("GRADE")){
            $this->grade = VARS::get("GRADE");
            $this->field["GRADE"] = VARS::get("GRADE");
        }
        if(VARS::get("COURSECD")){
            $this->courseCd = VARS::get("COURSECD");
            $this->field["COURSECD"] = VARS::get("COURSECD");
        }
        if(VARS::get("MAJORCD")){
            $this->majorCd = VARS::get("MAJORCD");
            $this->field["MAJORCD"] = VARS::get("MAJORCD");
        }
        if(VARS::get("PROGRAMID")){
            $this->clean();
        }

        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    //入力項目チェック
    function validateInput()
    {
        //入力チェック
        if (trim($this->field["TESTKINDCD"]) == "") {
            $this->setWarning('MSG301', "(テスト種別)");
            return false;
        } else if (trim($this->field["TESTITEMCD"]) == "") {
            $this->setWarning('MSG301', "(テスト項目コード)");
            return false;
        } else if (trim($this->field["SCORE_DIV"]) == "") {
            $this->setWarning('MSG301', "(成績種別コード)");
            return false;
        } else if (trim($this->field["TESTITEMNAME"]) == "") {
            $this->setWarning('MSG301', "(表示用テスト項目名)");
            return false;
        } else if (trim($this->field["TESTITEMABBV1"]) == "") {
            $this->setWarning('MSG301', "(表示用項目名1)");
            return false;
        } else if (trim($this->field["SCHOOLCD"]) == "") {
            $this->setWarning('MSG301', "(学校コード)");
            return false;
        } else if (trim($this->field["SCHOOL_KIND"]) == "") {
            $this->setWarning('MSG301', "(校種)");
            return false;
        } else if (trim($this->field["SEMESTER"]) == "") {
            $this->setWarning('MSG301', "(学期)");
            return false;
        } else if (trim($this->field["GRADE"]) == "") {
            $this->setWarning('MSG301', "(学年)");
            return false;
        } else if (trim($this->field["RIGHT_COURSE_MAJOR"]) == "") {
            $this->setWarning('MSG301', "(課程学科)");
            return false;
        } else if (strlen($this->field["TESTITEMNAME"]) > 30 && ($this->cmd == "add" || $this->cmd == "update" )) {
            $this->setWarning('MSG917', "(表示用テスト項目名は30バイトまでです。)");
            return false;
        } else if (strlen($this->field["TESTITEMABBV1"]) > 15 && ($this->cmd == "add" || $this->cmd == "update" )) {
            $this->setWarning('MSG917', "(表示用項目名1は15バイトまでです。)");
            return false;
        }
        //テスト項目コードの０埋め
        $this->field["TESTITEMCD"] = sprintf("%02d", $this->field["TESTITEMCD"]);
        //update,delete共通のチェック
        if ($this->cmd == "update" || $this->cmd == "delete") {
            if ($this->testkindcd != $this->field["TESTKINDCD"]) {
                $this->setWarning('MSG308',"(テスト種別)");
                return false;
            } elseif ($this->testitemcd != $this->field["TESTITEMCD"]) {
                $this->setWarning('MSG308',"(テスト項目コード)");
                return false;
            } elseif ($this->score_div != $this->field["SCORE_DIV"]) {
                $this->setWarning('MSG308',"(成績種別コード)");
                return false;
            } elseif ($this->schoolCd != $this->field["SCHOOLCD"]) {
                $this->setWarning('MSG308',"(学校コード)");
                return false;
            } elseif ($this->schoolKind != $this->field["SCHOOL_KIND"]) {
                $this->setWarning('MSG308',"(校種)");
                return false;
            } elseif ($this->semester != $this->field["SEMESTER"]) {
                $this->setWarning('MSG308',"(学期)");
                return false;
            } elseif ($this->grade != $this->field["GRADE"]) {
                $this->setWarning('MSG308',"(学年)");
                return false;
            } elseif ($this->courseCd."-".$this->majorCd != $this->field["RIGHT_COURSE_MAJOR"]) {
                $this->setWarning('MSG308',"(課程学科)");
                return false;
            }
        }

        //テスト期間チェック
        if ($this->Properties["Test_Period_Hyouji"] == "1") {
            if (($this->field["TEST_END_DATE"] != "" && $this->field["TEST_START_DATE"] == "") ||
                ($this->field["TEST_END_DATE"] == "" && $this->field["TEST_START_DATE"] != "")) {
                $this->setWarning('MSG913',"(テスト期間)");
                return false;
            }
            if ($this->field["TEST_END_DATE"] < $this->field["TEST_START_DATE"]) {
                $this->setWarning('MSG916',"(テスト期間)");
                return false;
            }
        }

        $ret_val = knjz380qQuery::getExists($this);
        if ($this->cmd == "add") {
            if ($ret_val) {
                $this->setWarning('MSG302',"(学期)(テスト種別)(テスト項目コード)(成績種別コード)");
                return false;
            }
        }
        if ($this->cmd == "update" || $this->cmd == "delete") {
            if (!$ret_val) {
                $this->setWarning('MSG303',"(学期)(テスト種別)(テスト項目コード)(成績種別コード)");
                return false;
            }
        }

        return true;
    }

    /**
     * 前年度からのコピーのモデルを返します
    */
    function &getCopyModel(){
        $result = knjz380qQuery::getCopyQuery($this);
    }

    /**
     * 指定課程学科からのコピー
    */
    function &getCopyModel2(){
        $result = knjz380qQuery::getCopyQuery2($this);
    }

    //更新用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
            $this->setMessage('MSG201');
        }
    }

    //追加用のモデルを返します
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->insert();
            $this->setMessage('MSG201');
        }
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->delete();
            $this->setMessage('MSG201');
        }
    }

    //インサート時の内部メソッド
    function &insert()
    {
        knjz380qQuery::getInsertUpdateQuery($this);
        $this->schoolCd = $this->field["SCHOOLCD"];
        $this->schoolKind = $this->field["SCHOOL_KIND"];
        $this->semester = $this->field["SEMESTER"];
        $this->testkindcd = $this->field["TESTKINDCD"];
        $this->testitemcd = $this->field["TESTITEMCD"];
        $this->score_div  = $this->field["SCORE_DIV"];
        $this->grade  = $this->field["GRADE"];
        list($coursecd, $majorcd) = preg_split("/-/", $this->field["RIGHT_COURSE_MAJOR"]);
        $this->courseCd  = $coursecd;
        $this->majorCd  = $majorcd;
    }

    //アップデート時の内部メソッド
    function &update()
    {
        knjz380qQuery::getInsertUpdateQuery($this);
        $this->schoolCd = $this->field["SCHOOLCD"];
        $this->schoolKind = $this->field["SCHOOL_KIND"];
        $this->semester = $this->field["SEMESTER"];
        $this->testkindcd = $this->field["TESTKINDCD"];
        $this->testitemcd = $this->field["TESTITEMCD"];
        $this->score_div  = $this->field["SCORE_DIV"];
        $this->grade  = $this->field["GRADE"];
        list($coursecd, $majorcd) = preg_split("/-/", $this->field["RIGHT_COURSE_MAJOR"]);
        $this->courseCd  = $coursecd;
        $this->majorCd  = $majorcd;
    }

    //デリート時の内部メソッド
    function &delete()
    {
        knjz380qQuery::getDeleteQuery($this);
        $this->clean();
    }
    
    //変数をクリアします
    function clean()
    {
        unset($this->schoolCd);
        unset($this->schoolKind);
        unset($this->semester);
        unset($this->testkindcd);
        unset($this->testitemcd);
        unset($this->score_div);
        unset($this->grade);
        unset($this->courseCd);
        unset($this->majorCd);
    }
}
?>
