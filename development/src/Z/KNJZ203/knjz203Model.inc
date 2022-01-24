<?php

require_once('for_php7.php');

//定数定義
class knjz203Model extends Model {
    var $field      = array();
    var $cmd;
    var $course_pk;         //primary
    var $special_group_cd;
    var $coursename;
    var $coursecode;
    var $coursecd;
    var $majorcd;
    var $grade;

    function knjz203Model() {}

    function init() {
        common::GetControlMaster_Fnc($this->control);

        $this->cmd        = VARS::request("cmd");

        if(VARS::get("PROGRAMID")) {
           $this->coursecode="";
           $this->coursecd="";
           $this->majorcd="";
           $this->grade="";
           $this->special_group_cd="";
        }

        //コースキー
        if (VARS::post("COURSENAME")!="") {
            $this->coursename = VARS::post("COURSENAME");
            $this->course_pk  = explode(" ",VARS::post("COURSENAME"));
            $this->coursecode = $this->course_pk[0];
            $this->coursecd   = $this->course_pk[1];
            $this->majorcd    = $this->course_pk[2];
            $this->grade      = $this->course_pk[3];
        }

        $this->field = array("SPECIAL_GROUP_CD"     =>  VARS::post("SPECIAL_GROUP_CD"),
                             "ABSENCE_HIGH"         =>  VARS::post("ABSENCE_HIGH"),
                             "ABSENCE_WARN"         =>  VARS::post("ABSENCE_WARN"),
                             "ABSENCE_WARN2"        =>  VARS::post("ABSENCE_WARN2"),
                             "ABSENCE_WARN3"        =>  VARS::post("ABSENCE_WARN3"),
                             "UPDATED"              =>  VARS::post("UPDATED"),
                             "YEAR"                 =>  CTRL_YEAR
                             );

        if (VARS::get("COURSECD")) {
            $this->coursecd     = VARS::get("COURSECD");
        }
        if (VARS::get("MAJORCD")) {
            $this->majorcd      = VARS::get("MAJORCD");
        }
        if (VARS::get("COURSECODE")) {
            $this->coursecode   = VARS::get("COURSECODE");
        }
        if(VARS::get("SPECIAL_GROUP_CD")) {
            $this->special_group_cd   = VARS::get("SPECIAL_GROUP_CD");
        }
        if (VARS::get("GRADE")) {
            $this->grade        = VARS::get("GRADE");
        }
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput() {
        //１レコード取得
        $db = Query::dbCheckOut();
        $row = knjz203Query::chkCODE($this, $db);
        $Row = knjz203Query::getRow($this, $db);
        Query::dbCheckIn($db);
        if ($this->cmd=="update" || $this->cmd=="delete") {
            //リストより選択していない
            if ($this->special_group_cd=="") {
                $this->setWarning("MSG308");
                return false;
            }
            //キー項目を変更した場合
            if($this->special_group_cd != $this->field["SPECIAL_GROUP_CD"]) {
                if ($this->cmd == "update" && is_array($row)) {
                    $this->setWarning("MSG308");
                    return false;
                }
            }
            //キー変更をしての削除
            if($this->special_group_cd != $this->field["SPECIAL_GROUP_CD"]) {
                $this->setWarning("MSG308");
                return false;
            }

            //更新日付チェック1
             if ($this->cmd == "update" && $Row["UPDATED"] == "") {
                 $this->setWarning("MSG303");
                 return false;
             }

            //更新日付チェック2
            if ($this->cmd == "update" && $Row["UPDATED"] != $this->field["UPDATED"]) {
                $this->setWarning("MSG307");
                return false;
            }
        }
        if  ($this->cmd=="update" || $this->cmd=="add") {
            //重複チェック
            if ($this->cmd == "add" && is_array($row)) {
                $this->setWarning("MSG918");
                return false;
            }
            //上限値チェック
            if((int) $this->field["ABSENCE_HIGH"] >= 1000) {
                $this->setWarning("MSG914");
                return false;
            }
        }
        return true;
    }

    //前年度からのコピーのモデルを返します
    function &getCopyModel() {
        $db = Query::dbCheckOut();
        $result    = $db->query(knjz203Query::getOldYear($this));   //前年度のデータ取得
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $db->query(knjz203Query::copyInsert($row));             //前年度のデータを今年度のデータとしてINSERT
        }
        $this->setMessage("MSG201");
        Query::dbCheckIn($db);
    }

    //編集完了用のモデルを返します
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->insert();
        }
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->delete();
        }
        return;
    }

    //DELETE時の内部メソッド
    function &delete() {
        $result = knjz203Query::getDeleteQuery($this);
        if ( !isset($model->warning)) {
            $this->setMessage("MSG201");
            $this->field = array();
        }
    }

    //INSERT・UPDATE時の内部メソッド
    function &insert() {
        $db = Query::dbCheckOut();
        //授業時数のフラグ  欠課数上限値の入力可、付加の判定に使う
        $query = knjz203Query::getJugyouJisuFlg();
        $this->jugyou_jisu_flg = $db->getOne($query); //1:法定授業 2:実授業
        Query::dbCheckIn($db);

        $result = knjz203Query::getInsertQuery($this);
        if (!isset($model->warning)) {
            $this->special_group_cd = $this->field["SPECIAL_GROUP_CD"];
            $this->setMessage("MSG201");
        }
        return $result;
    }
}
?>
