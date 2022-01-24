<?php

require_once('for_php7.php');

class knjz350_nenkan_testitemModel extends Model {
    var $leftScoolkind;
    var $field = array();
    var $sendField = array();
    var $selectdata = array();
    var $cmd;

    function knjz350_nenkan_testitemModel()
    {
    }

    function init() {

        $this->cmd = VARS::request("cmd");
        $this->sendFlg = VARS::get("SEND_FLG");
        //プロパティファイル
        $this->getPropertiesAll();
        if (VARS::get("SEND_selectSchoolKind")) {
            $this->selectSchoolKind = VARS::get("SEND_selectSchoolKind");
            $this->sendSchoolKind = VARS::get("SEND_schoolKind");
        }

        if ($this->sendFlg) {
            $this->sendField = array("GRADE"        =>  VARS::get("GRADE"),
                                     "SEMESTER"     =>  VARS::get("SEMESTER"),
                                     "TESTKINDCD"   =>  VARS::get("TESTKINDCD"),
                                     "TESTITEMCD"   =>  VARS::get("TESTITEMCD"),
                                     "COURSE_MAJOR_COURSECODE"  =>  VARS::get("SEND_COURSE_MAJOR_COURSECODE"),
                                     "SUBCLASSCD"               =>  VARS::get("SEND_SUBCLASSCD")
                                     );
        }
        if ($this->cmd == "leftChange") {
            $this->leftScoolkind = VARS::post("LEFT_SCHOOL_KIND");
            $this->clean();
        }

        $this->field = array("GRADE"        =>  VARS::post("GRADE"),
                             "SEMESTER"     =>  VARS::post("SEMESTER"),
                             "TESTKINDCD"   =>  VARS::post("TESTKINDCD"),
                             "TESTITEMCD"   =>  VARS::post("TESTITEMCD"),
                             "COURSE_MAJOR_COURSECODE"  =>  VARS::post("COURSE_MAJOR_COURSECODE"),
                             "SUBCLASSCD"               =>  VARS::post("SUBCLASSCD")
                             );

        $this->selectdata = array();
        $this->selectdata = explode(",", VARS::post("selectdata"));

        if (VARS::get("SEND_PRGID")) {
            $this->clean();
            unset($this->leftScoolkind);
            unset($this->sendFlg);
            unset($this->sendField);
            unset($this->field);
        }
        $this->setSchoolKind = $this->sendSchoolKind;
        if ($this->leftScoolkind) {
            $this->setSchoolKind = $this->leftScoolkind;
        }
    }

    //前年度からのコピーのモデルを返します
    function &getCopyModel() {
        $cnt     = knjz350_nenkan_testitemQuery::getCopyCountQuery($this);
        $cnt2    = knjz350_nenkan_testitemQuery::getCopyCount2($this);
        $cntTest = knjz350_nenkan_testitemQuery::getCopyCountTsetitem($this, $this->Properties["useTestCountflg"]);
        if ($cnt2 != '0') {
            $this->setMessage('すでにデータがあります。');
        } else if ($cntTest == '0') {
            $this->setMessage('今年度の考査種別が未登録です。');
        } else if ($cnt == '0') {
            $this->setMessage('更新対象がありませんでした。');
        } else {
            $result = knjz350_nenkan_testitemQuery::getCopyDelTestQuery($this);
            $result = knjz350_nenkan_testitemQuery::getCopyTestQuery($this, $this->Properties["useTestCountflg"]);
            $cntCopyList = knjz350_nenkan_testitemQuery::getCntKekka($this, $this->Properties["useTestCountflg"]);

            $this->setMessage('MSG201','コピー件数：'.$cntCopyList.'件');
        }
    }

    //入力項目チェック
    function validateInput() {
        $check = array("GRADE"                     =>  array("NULL" => "ON", "NAME" => "学年"),
                       "COURSE_MAJOR_COURSECODE"   =>  array("NULL" => "ON", "NAME" => "コース"),
                       "SUBCLASSCD"                =>  array("NULL" => "ON", "NAME" => "科目")
                       );
        if ($this->cmd == "update") {
            foreach ($check as $key => $val) {
                //必須チェック
                if ($val["NULL"] == "ON") {
                    //必須
                    if (strlen($this->field[$key]) == 0) {
                        $this->setWarning("MSG301","(".$val["NAME"].")");
                        return false;
                    }
                }
            }
        } else {
            foreach ($check as $key => $val) {
                //必須チェック
                if (in_array($key, array("TESTKINDCD", "TESTITEMCD")) && $val["NULL"] == "ON") {
                    //必須
                    if (strlen($this->field[$key]) == 0) {
                        $this->setWarning("MSG301","(".$val["NAME"].")");
                        return false;
                    }
                }
            }
        }

        return true;
    }

    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            knjz350_nenkan_testitemQuery::getDeleteQuery($this->field);
            foreach ($this->selectdata as $key => $val) {
                $setVal = explode(":", $val);
                $this->field["SEMESTER"]   = $setVal[0];
                $this->field["TESTKINDCD"] = $setVal[1];
                $this->field["TESTITEMCD"] = $setVal[2];

                knjz350_nenkan_testitemQuery::getInsertQuery($this->field);
            }
            $this->sendField["GRADE"] = $this->field["GRADE"];
            $this->sendField["COURSE_MAJOR_COURSECODE"] = $this->field["COURSE_MAJOR_COURSECODE"];
            $this->sendField["SUBCLASSCD"] = $this->field["SUBCLASSCD"];
            $this->setMessage("MSG201");
        }
        return;
    }

    function &getDeleteModel()
    {
        if ($this->validateInput()){
            foreach ($this->selectdata as $key => $val) {
                $setVal = explode(":", $val);
                $this->field["SEMESTER"]  = $setVal[0];
                $this->field["TESTKINDCD"] = $setVal[1];
                $this->field["TESTITEMCD"] = $setVal[2];

                knjz350_nenkan_testitemQuery::getDeleteQuery($this->field);
            }
        }
        $this->clean();
        $this->setMessage("MSG201");
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
        $this->sendField = array();
    }
}
?>
