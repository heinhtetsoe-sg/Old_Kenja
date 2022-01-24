<?php

require_once('for_php7.php');

class knjz431Model extends Model {
    var $cmd;       //コマンド
    var $year;      //今年度
    var $year_add;  //次年度

    function knjz431Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->year     = CTRL_YEAR;
        $this->year_add = CTRL_YEAR + 1;

        $this->checked  = VARS::request("CHECKED");     //チェック
        $this->cmd      = VARS::request("cmd");
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //SCHOOL_MSTを実行したとき、併せてSCHOOL_DETAIL_DAT、SEMESTER_MSTも次年度コピーする
        if(in_array("SCHOOL_MST", $this->checked)){
            //SCHOOL_DETAIL_DAT
            $table_check = $db->getOne(knjz431Query::getkekka($this->year, $this->year_add, "SCHOOL_DETAIL_DAT"));
            if($table_check == "") $this->checked[] = "SCHOOL_DETAIL_DAT";
            //SEMESTER_MST
            $table_check = $db->getOne(knjz431Query::getkekka($this->year, $this->year_add, "SEMESTER_MST"));
            if($table_check == "") $this->checked[] = "SEMESTER_MST";
        }

        for ($i = 0; $i < get_count($this->checked); $i++) {
            //SEMESTER_MSTは別で実行する
            if ($this->checked[$i] == 'SEMESTER_MST') {
                $semester = array('1', '9');
                foreach ($semester as $sem) {
                    //更新（SEMESTER_MST）
                    $query = knjz431Query::getInsertSemesterMstQuery($this->year_add, $sem, $db);
                }
            } else {
                //フィールド取得
                $Row = $db->getRow(knjz431Query::getFieldQuery($this->checked[$i], $this->year), DB_FETCHMODE_ASSOC);
                $field = "";
                $sep = "";
                foreach ($Row as $key => $val) {
                    if ($key == "YEAR") {
                        $key = "'$this->year_add'";
                    } else if ($key == "SEMESTERDIV") {
                        $key = "'1'";
                    } else if ($key == "REGISTERCD") {
                        $key = "'".STAFFCD."'";
                    } else if ($key == "UPDATED") {
                        $key = "SYSDATE()";
                    }
                    $field .= $sep.$key;
                    $sep = ",";
                }
                //更新
                $query = knjz431Query::getInsertQuery($this->year, $this->year_add, $this->checked[$i], $field);
            }

            if ($query) {
                $db->query($query);
            }
        }
        $this->setMessage("MSG201");
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }
}
?>
