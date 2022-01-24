<?php

require_once('for_php7.php');

//定数定義
class knjd236aModel extends Model {

    var $field      = array();
    var $cmd;
    var $coursename;
    var $grade;
    var $semester;
    var $type_group_cd;


    //メンバ変数の初期化
    function knjd236aModel()
    {
    }

    function init()
    {

        $this->cmd        = VARS::request("cmd");

        if (VARS::get("PROGRAMID") || $this->cmd == "coursename"){
            $this->type_group_cd = "";
            $this->grade = "";
            $this->semester = "";
        }

        $this->field = array("TYPE_GROUP_CD"    =>  VARS::post("TYPE_GROUP_CD"),
                             "REMARK1"          =>  VARS::post("REMARK1"),
                             "SEMESTER"         =>  VARS::post("SEMESTER"),
                             "GRADE"            =>  VARS::post("GRADE") ? VARS::post("GRADE") : $this->field["GRADE"],
                             "UPDATED"          =>  VARS::post("UPDATED"),
                             "YEAR"             =>  CTRL_YEAR
                             );

        if (VARS::request("SEMESTER")){
            $this->semester = VARS::request("SEMESTER");
        }
        if (VARS::request("GRADE")){
            $this->grade = VARS::request("GRADE");
        }
        if (VARS::get("TYPE_GROUP_CD")){
            $this->type_group_cd = VARS::get("TYPE_GROUP_CD");
        }
    }

    //入力項目チェック
    function validateInput()
    {
        //１レコード取得
        $row = knjd236aQuery::chkCode($this);

        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918");
            return false;
        }
        //類型グループの存在チェック
        if ($this->cmd == "update" && !is_array($row)){
            $this->setWarning("対象データがありません。");
            return false;
        }

        //必須項目チェック
        if (!$this->field["SEMESTER"]) {
            $this->setWarning("MSG304","（学期）");
            return false;
        }
        if (!$this->field["GRADE"] || !$this->grade) {
            $this->setWarning("MSG304","（学年）");
            return false;
        }
        if (!$this->field["TYPE_GROUP_CD"]) {
            $this->setWarning("MSG304","（類型グループ）");
            return false;
        }

        //桁数チェック
        if (strlen($this->field["REMARK1"]) > 720) {
            $this->setWarning("MSG915","(全体評は720バイトまでです)");
            return false;
        }
        return true;
    }

    //前年度からのコピーのモデルを返します
    function &getCopyModel(){
        $db = Query::dbCheckOut();
        $result = $db->query(knjd236aQuery::getOldYear($this));   //前年度のデータ取得
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $db->query(knjd236aQuery::copyInsert($row));          //前年度のデータを今年度のデータとしてINSERT
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

    //INSERT・UPDATE時の内部メソッド
    function &insert()
    {
        knjd236aQuery::getInsertQuery($this);
        if (!isset($model->warning)){
            $this->type_group_cd = $this->field["TYPE_GROUP_CD"];
            $this->setMessage("MSG201");
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
        $result = knjd236aQuery::getDeleteQuery($this);
        if ( !isset($model->warning)){
            $this->setMessage("MSG201");
            $this->field = array();
            $this->type_group_cd = "";
        }
    }

}
?>
