<?php

require_once('for_php7.php');


class knjz237Model extends Model {
    var $field        = array();
    var $cmd;
    var $staffcd;               //ログインユーザーID保持
    var $sec_competence;        //権限保持
    var $control_data;          //処理年度保持
    var $semester;            	//GETより取得
    var $testkindcd;            //GETより取得
    var $testitemcd;            //GETより取得

    function knjz237Model() {
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init() {
        //セキュリティーチェック
        $this->sec_competence = common::SecurityCheck(STAFFCD, "KNJZ237");
        if (VARS::request("cmd") == '') {
            unset($this->semester);
            unset($this->test);
        }
        if (VARS::post("cmd") == "list") {
            unset($this->test);
        }
        
        //プロパティファイル
        $this->getPropertiesAll();
        
        $this->cmd  = VARS::request("cmd");
        $this->field = array("TEST"         =>  VARS::post("TEST"),
                             "SEMESTER"     =>  VARS::post("SEMESTER"),
                             "DIV"          =>  VARS::post("DIV"),
                             "GRADE"        =>  VARS::post("GRADE"),
                             "SUBCLASSCD"   =>  VARS::post("SUBCLASSCD"),
                             "PASS_SCORE"   =>  VARS::post("PASS_SCORE"),
                             "COURSECD"     =>  VARS::post("COURSECD"),
                             "MAJORCD"      =>  VARS::post("MAJORCD"),
                             "COURSECODE"   =>  VARS::post("COURSECODE"),
                             "PERFECT"      =>  VARS::post("PERFECT")
                             );

        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            $this->field["CLASSCD"] = substr($this->field["SUBCLASSCD"],0,2);
            $this->field["SCHOOL_KIND"] = substr($this->field["SUBCLASSCD"],3,1);
            $this->field["CURRICULUM_CD"] = substr($this->field["SUBCLASSCD"],5,1);
        }
        
        if (VARS::get("cmd") == "edit"){
            $this->test       = VARS::get("TEST");
            $this->div        = VARS::get("DIV");
            $this->grade      = VARS::get("GRADE");
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                $this->classcd       = VARS::get("CLASSCD");
                $this->school_kind   = VARS::get("SCHOOL_KIND");
                $this->curriculum_cd = VARS::get("CURRICULUM_CD");
            }
            $this->subclasscd = VARS::get("SUBCLASSCD");
            $this->pass_score = VARS::get("PASS_SCORE");
            $this->coursecd   = VARS::get("COURSECD");
            $this->majorcd    = VARS::get("MAJORCD");
            $this->coursecode = VARS::get("COURSECODE");
            $this->perfect    = VARS::get("PERFECT");
        }

        if (VARS::get("cmd") == "edit" || $this->cmd == "reset"){
            $this->field["TEST"]       = $this->test;
            $this->field["DIV"]        = $this->div;
            $this->field["GRADE"]      = $this->grade;
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                $this->field["CLASSCD"]         = $this->classcd;
                $this->field["SCHOOL_KIND"]     = $this->school_kind;
                $this->field["CURRICULUM_CD"]   = $this->curriculum_cd;
            }
            $this->field["SUBCLASSCD"] = $this->subclasscd;
            $this->field["PASS_SCORE"] = $this->pass_score;
            $this->field["COURSECD"]   = $this->coursecd;
            $this->field["MAJORCD"]    = $this->majorcd;
            $this->field["COURSECODE"] = $this->coursecode;
            $this->field["PERFECT"]    = $this->perfect;
        }
        if (VARS::get("cmd") == "list_update") {
            $this->field["TEST"] = $this->test;
        }

        if(VARS::request("SEMESTER")){
            $this->semester = VARS::request("SEMESTER");
        }

        if(VARS::request("TEST")){
            $this->test = VARS::request("TEST");
        }

        if(VARS::get("PROGRAMID")){
            $this->clean();
        }

        $this->semester = $this->semester ? $this->semester : CTRL_SEMESTER;

        //初期値の設定
        $this->testTable = (strlen($this->Properties["useTestCountflg"])) ? $this->Properties["useTestCountflg"] : "TESTITEM_MST_COUNTFLG_NEW";
    }

    //入力項目チェック
    function validateInput() {
        //入力チェック
        if (trim($this->field["SEMESTER"]) == "") {
            $this->setWarning('MSG301', "(学期)");
            return false;
        }
        if(trim($this->field["TEST"]) == "") {
            $this->setWarning('MSG301', "(テスト名)");
            return false;
        }
        if(trim($this->field["SUBCLASSCD"]) == "") {
            $this->setWarning('MSG301', "(科目)");
            return false;
        }
        if(trim($this->field["DIV"]) == "") {
            $this->setWarning('MSG301', "(区分)");
            return false;
        }
        if ($this->field["DIV"] >= "2") {
            if(trim($this->field["GRADE"]) == "") {
                $this->setWarning('MSG301', "(学年)");
                return false;
            }
            if ($this->field["DIV"] == "3") {
                if(trim($this->field["COURSECD"]) == "") {
                    $this->setWarning('MSG301', "(課程)");
                    return false;
                }
                if(trim($this->field["MAJORCD"]) == "") {
                    $this->setWarning('MSG301', "(学科)");
                    return false;
                }
                if(trim($this->field["COURSECODE"]) == "") {
                    $this->setWarning('MSG301', "(コース)");
                    return false;
                }
            } else {
                $this->field["COURSECD"]   = "0";
                $this->field["MAJORCD"]    = "0:000";
                $this->field["COURSECODE"] = "0000";
            }
        } else {
            $this->field["GRADE"]      = "00";
            $this->field["COURSECD"]   = "0";
            $this->field["MAJORCD"]    = "0:000";
            $this->field["COURSECODE"] = "0000";
        }
        if((strlen($this->field["PERFECT"]) > 3 || strlen($this->field["PASS_SCORE"]) > 3 )&& ($this->cmd == "add" || $this->cmd == "update" )) {
            $this->setWarning('MSG917', "(文字数が不正です。)");
            return false;
        }
        //update,delete共通のチェック
        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            if($this->cmd == "update" || $this->cmd == "delete"){
                if($this->semester != $this->field["SEMESTER"]){
                    $this->setWarning('MSG308',"(学期)");
                    return false;
                }elseif($this->test != $this->field["TEST"]){
                    $this->setWarning('MSG308',"(テスト名)");
                    return false;
                }elseif($this->subclasscd != substr($this->field["SUBCLASSCD"],7,6)){
                    if($this->subclasscd != $this->field["SUBCLASSCD"]) {
                        $this->setWarning('MSG308',"(科目)");
                        return false;
                    }
                }elseif($this->div != $this->field["DIV"]){
                    $this->setWarning('MSG308',"(区分)");
                    return false;
                }elseif($this->grade != $this->field["GRADE"]){
                    $this->setWarning('MSG308',"(学年)");
                    return false;
                }elseif($this->coursecd != $this->field["COURSECD"]){
                    $this->setWarning('MSG308',"(課程)");
                    return false;
                }elseif($this->majorcd != $this->field["MAJORCD"]){
                    $this->setWarning('MSG308',"(学科)");
                    return false;
                }elseif($this->coursecode != $this->field["COURSECODE"]){
                    $this->setWarning('MSG308',"(コース)");
                    return false;
                }
            }
        } else {
            if($this->cmd == "update" || $this->cmd == "delete"){
                if($this->semester != $this->field["SEMESTER"]){
                    $this->setWarning('MSG308',"(学期)");
                    return false;
                }elseif($this->test != $this->field["TEST"]){
                    $this->setWarning('MSG308',"(テスト名)");
                    return false;
                }elseif($this->subclasscd != $this->field["SUBCLASSCD"]){
                    $this->setWarning('MSG308',"(科目)");
                    return false;
                }elseif($this->div != $this->field["DIV"]){
                    $this->setWarning('MSG308',"(区分)");
                    return false;
                }elseif($this->grade != $this->field["GRADE"]){
                    $this->setWarning('MSG308',"(学年)");
                    return false;
                }elseif($this->coursecd != $this->field["COURSECD"]){
                    $this->setWarning('MSG308',"(課程)");
                    return false;
                }elseif($this->majorcd != $this->field["MAJORCD"]){
                    $this->setWarning('MSG308',"(学科)");
                    return false;
                }elseif($this->coursecode != $this->field["COURSECODE"]){
                    $this->setWarning('MSG308',"(コース)");
                    return false;
                }
            }
        }

        if ($this->cmd == "add") {
            $db = Query::dbCheckOut();
            if ($this->field["DIV"] == '1') {
                $query = knjz237Query::getExistsDiv1($this);
                $rowCnt = $db->getOne($query);
                if ($rowCnt > 0) {
                    $this->setWarning('MSG302');
                    return false;
                }
            } elseif ($this->field["DIV"] == '2') {
                $query = knjz237Query::getExistsDiv2($this);
                $rowCnt = $db->getOne($query);
                if ($rowCnt > 0) {
                    $this->setWarning('MSG302');
                    return false;
                }
            } else {
                $query = knjz237Query::getExistsDiv3($this);
                $rowCnt = $db->getOne($query);
                if ($rowCnt > 0) {
                    $this->setWarning('MSG302');
                    return false;
                }
            }
            Query::dbCheckIn($db);
        }
        if ($this->cmd == "update" || $this->cmd == "delete") {
            $ret_val = knjz237Query::getExists($this);
            if (!$ret_val) {
                $this->setWarning('MSG303');
                return false;
            }
        }

        return true;
    }

    /**
     * 前年度からのコピーのモデルを返します
    */
    function &getCopyModel(){
        $cnt = knjz237Query::getCopyCountQuery($this);
        if ($cnt == '0') {
            $this->setMessage('更新対象がありませんでした。');
        } else {
            $result = knjz237Query::getCopyQuery($this);
            $this->setMessage('MSG201','コピー件数：'.$cnt.'件');
        }
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
    function &insert() {
        knjz237Query::getInsertUpdateQuery($this);
        $this->semester   = $this->field["SEMESTER"];
        $this->test       = $this->field["TEST"];
        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            $this->classcd = substr($this->field["SUBCLASSCD"],0,2);
            $this->school_kind = $this->field["SCHOOL_KIND"];
            $this->curriculum_cd = $this->field["CURRICULUM_CD"];
        }
        $this->subclasscd = $this->field["SUBCLASSCD"];
        $this->div        = $this->field["DIV"];
        $this->grade      = $this->field["GRADE"];
        $this->coursecd   = $this->field["COURSECD"];
        $this->majorcd    = $this->field["MAJORCD"];
        $this->coursecode = $this->field["COURSECODE"];
        $this->perfect    = $this->field["PERFECT"];
        $this->pass_score = $this->field["PASS_SCORE"];
    }

    //アップデート時の内部メソッド
    function &update() {
        knjz237Query::getInsertUpdateQuery($this);
        $this->semester   = $this->field["SEMESTER"];
        $this->test       = $this->field["TEST"];
        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            $this->classcd = substr($this->field["SUBCLASSCD"],0,2);
            $this->school_kind = $this->field["SCHOOL_KIND"];
            $this->curriculum_cd = $this->field["CURRICULUM_CD"];
        }
        $this->subclasscd = $this->field["SUBCLASSCD"];
        $this->div        = $this->field["DIV"];
        $this->grade      = $this->field["GRADE"];
        $this->coursecd   = $this->field["COURSECD"];
        $this->majorcd    = $this->field["MAJORCD"];
        $this->coursecode = $this->field["COURSECODE"];
        $this->perfect    = $this->field["PERFECT"];
        $this->pass_score = $this->field["PASS_SCORE"];
    }

    //デリート時の内部メソッド
    function &delete() {
        knjz237Query::getDeleteQuery($this);
        $this->clean();
    }
    
    //変数をクリアします
    function clean() {
        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            unset($this->classcd);
            unset($this->school_kind);
            unset($this->curriculum_cd);
        }
        unset($this->subclasscd);
        unset($this->div);
        unset($this->grade);
        unset($this->coursecd);
        unset($this->majorcd);
        unset($this->coursecode);
        unset($this->perfect);
        unset($this->pass_score);
    }
}
?>
