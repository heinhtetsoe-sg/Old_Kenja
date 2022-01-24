<?php

require_once('for_php7.php');


class knjz430Model extends Model
{
    public $cmd;       //コマンド
    public $year;      //今年度
    public $year_add;  //次年度
    public $z010Name;  //学校判別名称

    public function knjz430Model()
    {
    }

    public function init()
    {
        $this->year     = CTRL_YEAR;
        $this->year_add = CTRL_YEAR + 1;

        $this->checked  = VARS::request("CHECKED");     //チェック
        $this->cmd      = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $this->z010Name = $db->getOne(knjz430Query::getZ010Name1());
        Query::dbCheckIn($db);
    }

    //編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //SCHOOL_MSTを実行したとき、併せてSCHOOL_DETAIL_DATも次年度コピーする
        if (in_array("SCHOOL_MST", $this->checked)) {
            $table_check = $db->getOne(knjz430Query::getkekka($this->year, $this->year_add, "SCHOOL_DETAIL_DAT"));
            if ($table_check == "") {
                $this->checked[] = "SCHOOL_DETAIL_DAT";
            }
            if ($this->Properties["use_school_detail_gcm_dat"] == "1") {
                $table_check = $db->getOne(knjz430Query::getkekka($this->year, $this->year_add, "SCHOOL_DETAIL_GCM_DAT"));
                if ($table_check == "") {
                    $this->checked[] = "SCHOOL_DETAIL_GCM_DAT";
                }
            }
        }
        if (in_array("CERTIF_KIND_YDAT", $this->checked)) {
            $table_check = $db->getOne(knjz430Query::getkekka($this->year, $this->year_add, "CERTIF_SCHOOL_DAT"));
            if ($table_check == "") {
                $this->checked[] = "CERTIF_SCHOOL_DAT";
            }
        }
        if (in_array("STAFF_YDAT", $this->checked)) {
            $table_check = $db->getOne(knjz430Query::getkekka($this->year, $this->year_add, "STAFF_DETAIL_MST"));
            if ($table_check == "") {
                $this->checked[] = "STAFF_DETAIL_MST";
            }
        }
        for ($i = 0; $i < get_count($this->checked); $i++) {
            //SEMESTER_MSTは、学期開始日、学期終了日に年度を+1するため、別で実行する
            if ($this->checked[$i] == 'SEMESTER_MST' || $this->checked[$i] == 'SEMESTER_DETAIL_MST') {
                $result = $db->query(knjz430Query::getFieldQuery($this->checked[$i], $this->year));
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($row["SDATE"]) {
                        list($syear, $smonth, $sday) = preg_split("/-/", $row["SDATE"]);
                        $row["SDATE"] = date("Y-m-d", mktime(0, 0, 0, $smonth, $sday, (int)$syear + 1));
                    }
                    if ($row["EDATE"]) {
                        list($eyear, $emonth, $eday) = preg_split("/-/", $row["EDATE"]);
                        $row["EDATE"] = date("Y-m-d", mktime(0, 0, 0, $emonth, $eday, (int)$eyear + 1));
                    }
                    $query = knjz430Query::getInsertSemesterMstQuery($this->checked[$i], $this->year_add, $row, $db);
                }
            } elseif ($this->checked[$i] == 'HOLIDAY_MST') {
                $result = $db->query(knjz430Query::getFieldQuery($this->checked[$i], $this->year, " FISCALYEAR(HOLIDAY) "));
                $query = "";
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    list($gryear, $grmonth, $grday) = preg_split("/-/", $row["HOLIDAY"]);
                    $holiday = date("Y-m-d", mktime(0, 0, 0, $grmonth, $grday, (int)$gryear + 1));
                    $key = "'$holiday'";
                    if ($query) {
                        $query .= ", ";
                    }
                    $query .= "('".$holiday."', '".$row["REMARK"]."', '".STAFFCD."', SYSDATE())";
                }
                $result->free();
                if ($query) {
                    $query = " INSERT INTO HOLIDAY_MST (HOLIDAY, REMARK, REGISTERCD, UPDATED) VALUES ".$query;
                }
            } else {
                $Row = $db->getRow(knjz430Query::getFieldQuery($this->checked[$i], $this->year), DB_FETCHMODE_ASSOC);
                $field = "";
                $sep = "";
                foreach ($Row as $key => $val) {
                    if ($key == "YEAR") {
                        $key = "'$this->year_add'";
                    } elseif ($key == "ENTRANCE_DATE") {
                        list($enyear, $enmonth, $enday) = preg_split("/-/", $val);
                        $entrance_date = date("Y-m-d", mktime(0, 0, 0, $enmonth, $enday, (int)$enyear + 1));
                        $key = "'$entrance_date'";
                    } elseif ($key == "GRADUATE_DATE") {
                        list($gryear, $grmonth, $grday) = preg_split("/-/", $val);
                        $graduate_date = date("Y-m-d", mktime(0, 0, 0, $grmonth, $grday, (int)$gryear + 1));
                        $key = "'$graduate_date'";
                    } elseif ($key == "REGISTERCD") {
                        $key = "'".STAFFCD."'";
                    } elseif ($key == "UPDATED") {
                        $key = "SYSDATE()";
                    }
                    $field .= $sep.$key;
                    $sep = ",";
                }
                $query = knjz430Query::getInsertQuery($this, $this->year, $this->year_add, $this->checked[$i], $field);
            }

            if ($query) {
                $db->query($query);
            }
        }

        if (in_array("SUBCLASS_YDAT", $this->checked)) {
            if ($this->Properties["useCurriculumcd"] == 1 && $this->Properties["useClassDetailDat"] == 1 && $this->Properties["hyoujiClassDetailDat"] == 1) {
                //次年度データ削除
                $query  = knjz430Query::getDeleteQuery('SUBCLASS_DETAIL_DAT', $this->year_add);
                $db->query($query);

                $query  = knjz430Query::getFieldQuery('SUBCLASS_DETAIL_DAT', $this->year);

                $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if (isset($Row)) {
                    $field = "";
                    $sep = "";
                    foreach ($Row as $key => $val) {
                        if ($key == "YEAR") {
                            $key = "'$this->year_add'";
                        } elseif ($key == "REGISTERCD") {
                            $key = "'".STAFFCD."'";
                        } elseif ($key == "UPDATED") {
                            $key = "SYSDATE()";
                        }
                        $field .= $sep.$key;
                        $sep = ",";
                    }
                    $query = knjz430Query::getInsertQuery($this, $this->year, $this->year_add, 'SUBCLASS_DETAIL_DAT', $field);
                    $db->query($query);
                }
            }
        }

        $this->setMessage("MSG201");
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }
}
