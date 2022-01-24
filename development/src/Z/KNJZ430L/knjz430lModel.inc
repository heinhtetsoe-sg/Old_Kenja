<?php

require_once('for_php7.php');

class knjz430lModel extends Model {
    var $cmd;
    var $year;
    var $year_add;

    function knjz430lModel() {}

    function init() {
        $this->year     = CTRL_YEAR;
        $this->year_add = CTRL_YEAR + 1;

        $this->checked  = VARS::request("CHECKED");     //チェック選択
        $this->cmd      = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //SCHOOL_MSTを実行したとき、併せてSCHOOL_DETAIL_DATも次年度コピーする
        if(in_array("SCHOOL_MST", $this->checked)) {
            $table_check = $db->getOne(knjz430lQuery::getkekka($this->year, $this->year_add, "SCHOOL_DETAIL_DAT"));
            if($table_check == "") $this->checked[] = "SCHOOL_DETAIL_DAT";
            if ($this->Properties["use_school_detail_gcm_dat"] == "1") {
                $table_check = $db->getOne(knjz430lQuery::getkekka($this->year, $this->year_add, "SCHOOL_DETAIL_GCM_DAT"));
                if($table_check == "") $this->checked[] = "SCHOOL_DETAIL_GCM_DAT";
            }
        }
        if (in_array("CERTIF_KIND_YDAT", $this->checked)) {
            $table_check = $db->getOne(knjz430lQuery::getkekka($this->year, $this->year_add, "CERTIF_SCHOOL_DAT"));
            if($table_check == "") $this->checked[] = "CERTIF_SCHOOL_DAT";
        }
        for ($i = 0; $i < get_count($this->checked); $i++) {
            $Row = $db->getRow(knjz430lQuery::getFieldQuery($this->checked[$i], $this->year), DB_FETCHMODE_ASSOC);
            $field = "";
            $sep = "";
            foreach ($Row as $key => $val) {
                if ($key == "YEAR") {
                    $key = "'$this->year_add'";
                } else if ($key == "ENTRANCE_DATE") {
                    list($enyear, $enmonth, $enday) = preg_split("/-/", $val);
                    $entrance_date = date("Y-m-d", mktime(0, 0, 0, $enmonth, $enday, $enyear + 1));
                    $key = "'$entrance_date'";
                } else if ($key == "GRADUATE_DATE") {
                    list($gryear, $grmonth, $grday) = preg_split("/-/", $val);
                    $graduate_date = date("Y-m-d", mktime(0, 0, 0, $grmonth, $grday, $gryear + 1));
                    $key = "'$graduate_date'";
                } else if ($key == "REGISTERCD") {
                    $key = "'".STAFFCD."'";
                } else if ($key == "UPDATED") {
                    $key = "SYSDATE()";
                }
                $field .= $sep.$key;
                $sep = ",";
            }
            $query = knjz430lQuery::getInsertQuery($this, $this->year, $this->year_add, $this->checked[$i], $field);
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
