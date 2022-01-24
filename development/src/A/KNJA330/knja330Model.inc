<?php

require_once('for_php7.php');


class knja330Model extends Model {

    var $cmd;       //コマンド
    var $year;      //今年度
    var $year_add;  //次年度
    var $output;    //ラジオ
    var $checkYear; //CONTROL_MSTの年度
    var $control;   //コントロール
    var $checkdiv;  //年度チェック区分

    function knja330Model()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->year     = CTRL_YEAR;
        $this->year_add = CTRL_YEAR + 1;

        $this->addchecked  = VARS::request("ADD_CHECKED");          //更新チェック
        $this->delchecked  = VARS::request("DEL_CHECKED");          //削除チェック
        $this->cmd         = VARS::request("cmd");                  //コマンド
        $this->output      = VARS::request("OUTPUT");               //ラジオ

        $db = Query::dbCheckOut();
        $this->checkYear   = $db->getOne(knja330Query::getCtrl());  //CONTROL_MSTの年度取得
        if ($this->checkYear >= $this->year) {
            $this->checkdiv = "1";
        } else {
            $this->checkdiv = "2";
        }
        Query::dbCheckIn($db);
    }

    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $regdField = array();
        $hdatField = array();
        if (!$this->addCheck()) {
            return false;
        }
        for ($i = 0; $i < get_count($this->addchecked); $i++) {
            $regdField = $this->getField($db, "SCHREG_REGD_DAT", $this->addchecked[$i]);
            $hdatField = $this->getField($db, "SCHREG_REGD_HDAT", $this->addchecked[$i]);
            $this->insert($db, $this->addchecked[$i], "SCHREG_REGD_DAT", $regdField);
            $this->delete($db, $this->year_add, sprintf("%02d",$this->addchecked[$i] + 1), "SCHREG_REGD_HDAT");
            $this->insert($db, $this->addchecked[$i], "SCHREG_REGD_HDAT", $hdatField);
        }
        $this->setMessage("MSG201");
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

    //チェック処理
    function addCheck()
    {
        if ($this->checkYear < $this->year) {
            $this->setWarning("MSG300","コントロールマスタ年度より大きい\\n年度は処理できません。");
            return false;
        }
        return true;
    }

    //INSERT SQL用のフィールド取得
    function getField($db, $table, $grade)
    {
        $Row = $db->getRow(knja330Query::getFieldQuery($table, $this->year), DB_FETCHMODE_ASSOC);
        $field = "";
        $sep = "";
        foreach ($Row as $key => $val) {
            if ($key == "YEAR") {
                $key = "'$this->year_add'";
            } else if ($key == "SEMESTER") {
                $key = "'1'";
            } else if ($key == "GRADE") {
                $key = "'".sprintf("%02d",(int)$grade+1)."'";
            } else if ($key == "REGISTERCD") {
                $key = "'".STAFFCD."'";
            } else if ($key == "UPDATED") {
                $key = "SYSDATE()";
            }
            $field .= $sep.$key;
            $sep = ",";
        }
        return $field;
    }

    //追加
    function &insert(&$db, $grade, $table, $field)
    {
        $query = knja330Query::getInsertQuery($this->year, $this->year_add, $grade, $table, $field, str_replace("/","-",$this->control["学期終了日付"][9]), $this->checkdiv);
        if ($query) {
            $db->query($query);
        }
    }

    //削除のモデルを返します
    function &getDeleteModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        if (!$this->delCheck()) {
            return false;
        }
        for ($i = 0; $i < get_count($this->delchecked); $i++) {
            $this->delete($db, $this->year, $this->delchecked[$i], "SCHREG_REGD_DAT");
            $this->delete($db, $this->year, $this->delchecked[$i], "SCHREG_REGD_HDAT");
        }
        $this->setMessage("MSG201");
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

    //チェック処理
    function delCheck()
    {
        if ($this->checkYear >= $this->year) {
            $this->setWarning("MSG919","コントロールマスタ年度以前の\\n年度は処理できません。");
            return false;
        }
        return true;
    }

    //削除
    function &delete(&$db, $year, $grade, $table)
    {
        $query = knja330Query::getDeleteQuery($year, $grade, $table);
        if ($query) {
            $db->query($query);
        }
    }
}
?>
