<?php

require_once('for_php7.php');

//定数定義
class knjd105aModel extends Model {

    var $field      = array();
    var $cmd;
    var $course_pk;         //primary
    var $subclasscd;
    var $coursename;
    var $coursecode;
    var $coursecd;
    var $majorcd;
    var $grade;
    var $semester;
    var $testname;
    var $testcd;

    //メンバ変数の初期化
    function knjd105aModel()
    {
    }

    function init()
    {
        //プロパティファイル
        $this->getPropertiesAll();
        $this->cmd        = VARS::request("cmd");

        $this->subclasscd = "999999";

        if (VARS::get("PROGRAMID") || $this->cmd == "coursename"){
            $this->coursecode = "";
            $this->coursecd = "";
            $this->majorcd = "";
            $this->grade = "";
            $this->testcd = "";
            $this->semester = "";
        }

        $this->field = array("COURSE"       =>  VARS::post("COURSE"),
                             "FOOTNOTE"     =>  VARS::post("FOOTNOTE"),
                             "SEMESTER"     =>  VARS::post("SEMESTER"),
                             "TESTCD"       =>  VARS::post("TESTCD"),
                             "GRADE"        =>  VARS::post("GRADE"),
                             "UPDATED"      =>  VARS::post("UPDATED"),
                             "YEAR"         =>  CTRL_YEAR
                             );

        if (VARS::request("SEMESTER")){
            $this->semester        = VARS::request("SEMESTER");
        }
        if (VARS::request("TESTCD")){
            $this->testcd            = VARS::request("TESTCD");
        }
        if (VARS::request("GRADE")){
            $this->grade        = VARS::request("GRADE");
        }
        
        if (VARS::get("COURSECODE")){
            $this->coursecode = VARS::get("COURSECODE");
        }
        
        if (VARS::get("COURSECD")){
            $this->coursecd = VARS::get("COURSECD");
        }
        
        if (VARS::get("MAJORCD")){
            $this->majorcd = VARS::get("MAJORCD");
        }

    }

    //入力項目チェック
    function validateInput()
    {
        //１レコード取得
        $row = knjd105aQuery::chkCODE($this);
        $Row = knjd105aQuery::getRow($this);
        if ($this->cmd=="update" || $this->cmd=="delete"){
            //リストより選択していない
            if ($this->coursecode=="" &&
                $this->coursecd=="" &&
                $this->majorcd=="" 
                ){
                $this->setWarning("MSG308");
                return false;
            }
            //キー項目を変更した場合
            if($this->coursecd."-".$this->majorcd."-".$this->coursecode != $this->field["COURSE"])
            {
                if ($this->cmd == "update" && is_array($row)){
                    $this->setWarning("MSG308");
                    return false;
                }
            } 
           //キー変更をしての削除
           if($this->coursecd."-".$this->majorcd."-".$this->coursecode != $this->field["COURSE"])
           {
               $this->setWarning("MSG308");
               return false;
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
        
        if (strlen($this->field["FOOTNOTE"]) > 957) {
            $this->setWarning("MSG915","(全体評は957バイトまでです)");
            return false;
        }
        
        return true;
    }
    
    //前年度からのコピーのモデルを返します
    function &getCopyModel(){
        $db = Query::dbCheckOut();
        $result    = $db->query(knjd105aQuery::getOldYear($this));   //前年度のデータ取得
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $db->query(knjd105aQuery::copyInsert($this, $row));             //前年度のデータを今年度のデータとしてINSERT
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
        $result = knjd105aQuery::getDeleteQuery($this);
        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
            $this->field = array();
            $this->coursecode = "";
            $this->coursecd   = "";
            $this->majorcd    = "";
        }
    }

    //INSERT・UPDATE時の内部メソッド
    function &insert()
    {
        $result = knjd105aQuery::getInsertQuery($this);
        if (!isset($model->warning)){
            //$this->subclasscd = $this->field["SUBCLASSCD"];
            list($this->coursecd, $this->majorcd, $this->coursecode) = preg_split("/-/", $this->field["COURSE"]);
            $this->setMessage("MSG201");
            
        }
        return $result;
    }

}
?>
