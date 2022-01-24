<?php

require_once('for_php7.php');

//定数定義
class knjd104bModel extends Model {

    var $field      = array();
    var $cmd;
    var $course_pk;         //primary
    //教育課程用
    var $classcd;        
    var $school_kind;
    var $curriculum_cd;
    
    var $subclasscd;
    var $coursename;
    var $coursecode;
    var $coursecd;
    var $majorcd;
    var $grade;
    var $semester;
    var $testname;
    var $testkindcd;
    var $testitemcd;
    //メンバ変数の初期化
    function knjd104bModel()
    {
    }

    function init()
    {
        $this->getPropertiesAll(); //プロパティファイル
        $this->cmd        = VARS::request("cmd");

        if (VARS::get("PROGRAMID") || $this->cmd == "coursename"){
            $this->coursecode = "";
            $this->coursecd = "";
            $this->majorcd = "";
            $this->grade = "";
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                $this->classcd = "";
                $this->school_kind = "";
                $this->curriculum_cd = "";
            }
            $this->subclasscd = "";
            $this->testkindcd = "";
            $this->testitemcd = "";
            $this->semester = "";
        }

        //コースキー
        if (VARS::post("COURSENAME")!=""){
            $this->coursename = VARS::post("COURSENAME");
            $this->course_pk  = explode(" ",VARS::post("COURSENAME"));
            $this->coursecode = $this->course_pk[0];
            $this->coursecd   = $this->course_pk[1];
            $this->majorcd    = $this->course_pk[2];
            $this->grade      = $this->course_pk[3];
        }
        
        //テストキー
        if (VARS::post("TESTNAME")!=""){
            $this->coursename = VARS::post("TESTNAME");
            $this->test_pk    = explode(" ",VARS::post("TESTNAME"));
            $this->testkindcd = $this->test_pk[0];
            $this->testitemcd = $this->test_pk[1];
        }

        $this->field = array("SUBCLASSCD"   =>  VARS::post("SUBCLASSCD"),
                             "FOOTNOTE"     =>  VARS::post("FOOTNOTE"),
                             "UPDATED"      =>  VARS::post("UPDATED"),
                             "YEAR"         =>  CTRL_YEAR
                             );

        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            $this->field["CLASSCD"] = substr($this->field["SUBCLASSCD"],0,2);
            $this->field["SCHOOL_KIND"] = substr($this->field["SUBCLASSCD"],3,1);
            $this->field["CURRICULUM_CD"] = substr($this->field["SUBCLASSCD"],5,1);
        }

        if (VARS::request("SEMESTER") != "")
            $this->semester  = VARS::request("SEMESTER");

        if ((VARS::request("PROGRAMID") != ""))
            $this->semester  = CTRL_SEMESTER;

        if (VARS::request("COURSENAME") != "")
            $this->coursename  = VARS::request("COURSENAME");

        if ((VARS::request("PROGRAMID") != ""))
            unset($this->coursename);

        if (VARS::request("TESTNAME") != "")
            $this->testname  = VARS::request("TESTNAME");

        if ((VARS::request("PROGRAMID") != ""))
            unset($this->testname);

        if (VARS::get("COURSECD")){
            $this->coursecd     = VARS::get("COURSECD");
        }
        if (VARS::get("MAJORCD")){
            $this->majorcd      = VARS::get("MAJORCD");
        }
        if (VARS::get("COURSECODE")){
            $this->coursecode   = VARS::get("COURSECODE");
        }
        if (VARS::get("SUBCLASSCD")){
            $this->subclasscd   = VARS::get("SUBCLASSCD");
        }
        if (VARS::get("GRADE")){
            $this->grade        = VARS::get("GRADE");
        }
        if (VARS::get("TESTKINDCD")){
            $this->testkindcd        = VARS::get("TESTKINDCD");
        }
        if (VARS::get("TESTITEMCD")){
            $this->testitemcd        = VARS::get("TESTITEMCD");
        }
        if (VARS::get("SEMESTER")){
            $this->semester        = VARS::get("SEMESTER");
        }
        //教育課程用
        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            if (VARS::get("CLASSCD")) {
                $this->classcd        = VARS::get("CLASSCD");
            }
            if (VARS::get("SCHOOL_KIND")) {
                $this->school_kind        = VARS::get("SCHOOL_KIND");
            }
            if (VARS::get("CURRICULUM_CD")) {
                $this->curriculum_cd        = VARS::get("CURRICULUM_CD");
            }
        }
        if (VARS::get("cmd") == "edit" || $this->cmd == "reset") {
            //教育課程対応
            $this->field["SUBCLASSCD"] = $this->subclasscd;
            if ($this->Properties["useCurriculumcd"] == '1') {
                $this->field["CLASSCD"]         = $this->classcd;
                $this->field["SCHOOL_KIND"]     = $this->school_kind;
                $this->field["CURRICULUM_CD"]   = $this->curriculum_cd;
            }
        }

    }

    //入力項目チェック
    function validateInput()
    {
        //１レコード取得
        $row = knjd104bQuery::chkCODE($this);
        $Row = knjd104bQuery::getRow($this);
        if ($this->cmd=="update" || $this->cmd=="delete"){
            //リストより選択していない
            if ($this->subclasscd==""){
                $this->setWarning("MSG308");
                return false;
            }
            //キー項目を変更した場合
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                if($this->subclasscd != substr($this->field["SUBCLASSCD"],7,6))
                {
                    if ($this->cmd == "update" && is_array($row)){
                        $this->setWarning("MSG308");
                        return false;
                    }
                }
               //キー変更をしての削除
               if($this->subclasscd != substr($this->field["SUBCLASSCD"],7,6))
               {
                   $this->setWarning("MSG308");
                   return false;
               }
            } else {
                if($this->subclasscd != $this->field["SUBCLASSCD"])
                {
                    if ($this->cmd == "update" && is_array($row)){
                        $this->setWarning("MSG308");
                        return false;
                    }
                }
               //キー変更をしての削除
               if($this->subclasscd != $this->field["SUBCLASSCD"])
               {
                   $this->setWarning("MSG308");
                   return false;
               }
            }
           //更新日付チェック1
            if ($this->cmd == "update" && $Row["UPDATED"] == ""){
                $this->setWarning("MSG303");
                return false;
            }
        }
        if ($this->cmd=="update" || $this->cmd=="add"){
           //重複チェック
           if ($this->cmd == "add" && is_array($row)){
               $this->setWarning("MSG918");
               return false;
           }
        }
        if ($this->grade < '04'){
            if (strlen($this->field["FOOTNOTE"]) > 618) {
                $this->setWarning("MSG915","(総評は618バイトまでです)");
                return false;
            }
        }else{
            if (strlen($this->field["FOOTNOTE"]) > 432) {
                $this->setWarning("MSG915","(総評は432バイトまでです)");
                return false;
            }
        }
        return true;
    }
    
    //前年度からのコピーのモデルを返します
    function &getCopyModel(){
        $db = Query::dbCheckOut();
        $result    = $db->query(knjd104bQuery::getOldYear($this));   //前年度のデータ取得
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $db->query(knjd104bQuery::copyInsert($this, $row));             //前年度のデータを今年度のデータとしてINSERT
        }
        Query::dbCheckIn($db);
    }

    //編集完了用のモデルを返します
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->insert();
        }
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->delete();
        }
        return;
    }

    //DELETE時の内部メソッド
    function &delete()
    {
        $result = knjd104bQuery::getDeleteQuery($this);
        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
            $this->field = array();
        }
    }

    //INSERT・UPDATE時の内部メソッド
    function &insert()
    {
        $result = knjd104bQuery::getInsertQuery($this);
        if (!isset($model->warning)){
            $this->subclasscd = $this->field["SUBCLASSCD"];
            $this->setMessage("MSG201");
        }
        return $result;
    }

}
?>
