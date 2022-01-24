<?php

require_once('for_php7.php');

class knji120Query extends Query
{

    //起動チェック
    public function checktoStart($db, $model)
    {
        foreach (array("GRD_REGD_HDAT","v_course_mst","v_major_mst") as $val) {
            $query = "SELECT COUNT(*) FROM ".$val." WHERE year = '".$model->year."'";
            if ($val == "GRD_REGD_HDAT") {
                $query .= " AND semester = '".$model->seme."'";
            }

            if ($db->getOne($query) == "0") {
                return false;
            }
        }
        foreach (array("A001","A002","A003","Z002") as $val) {
            $query = "SELECT COUNT(*) FROM v_name_mst WHERE year = '".$model->year."' AND namecd1 = '".$val."'";
            if ($db->getOne($query) == "0") {
                return false;
            }
        }
        return true;
    }
    
    //年組,先生名
    public function getGrdClasQuery($model)
    {
        $query  = "";
        $query .= "  SELECT DISTINCT";
        $query .= "   T1.grade || ',' || T1.hr_class AS gc,";
        $query .= "   T1.hr_name,";
        $query .= "   T2.staffname_show AS name_show,";
        $query .= "   T2.staffcd";
        $query .= "  FROM";
        $query .= "   GRD_REGD_HDAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON T1.YEAR = REG_G.YEAR ";
                $query .= "           AND T1.GRADE = REG_G.GRADE ";
                $query .= "           AND REG_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON T1.YEAR = REG_G.YEAR ";
            $query .= "           AND T1.GRADE = REG_G.GRADE ";
            $query .= "           AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "   LEFT OUTER JOIN staff_mst T2 ON T1.tr_cd1 = T2.staffcd";
        $query .= "  WHERE";
        $query .= "   T1.year    = '".$model->year."' AND";
        $query .= "   T1.semester = '".$model->seme."'";
        if (AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "AND (T1.tr_cd1   = '".STAFFCD."' OR T1.tr_cd2    = '".STAFFCD."' OR T1.tr_cd3    = '".STAFFCD."' OR ";
            $query .= "  T1.subtr_cd1 = '".STAFFCD."' OR T1.subtr_cd2 = '".STAFFCD."' OR T1.subtr_cd3 = '".STAFFCD."')";
        }
        $query .= "  ORDER BY";
        $query .= "   gc";
        return $query;
    }

    //置換画面用の生徒一覧
    public function GetStudent($GradeClass, $model)
    {
        $arr = explode(",", $GradeClass);

        $query  = "";
        $query .= "  SELECT";
        $query .= "   T2.name_show ,";
        $query .= "   T1.schregno ,";
        $query .= "   T1.attendno";
        $query .= "  FROM";
        $query .= "   GRD_REGD_DAT T1";
        $query .= "   LEFT OUTER JOIN GRD_BASE_MST T2 ON T1.schregno = T2.schregno";
        $query .= "  WHERE";
        $query .= "   T1.year    = '".$model->year."' AND";
        $query .= "   T1.semester = '".$model->seme."' AND";
        $query .= "   T1.grade  = '".$arr[0]."' AND";
        $query .= "   T1.hr_class = '".$arr[1]."'";
        $query .= "  ORDER BY";
        $query .= "   T1.attendno";

        return $query;
    }

    //置換(在籍)
    public function replaceUpdateRegd($db, $model)
    {
        $db->autoCommit(false);
        $array = explode(",", $model->replace_data["selectdata"]);
        for ($i = 0; $i < get_count($array); $i++) {
            foreach ($this->replace_data["regd_field"] as $key => $val) {
                $data[$key][TEXT] = $val;
            }
            $data["REGISTERCD"][TEXT] = STAFFCD;
            $data["UPDATED"][FUNC]  = "sysdate()";
            $where  = "WHERE schregno='".$array[$i]."'";
            $where .=" and year='".$model->year."'";
            $where .=" and semester='".$model->seme."'";
            $db->query(Query::updateSQL($data, "GRD_REGD_DAT", $where));
        }
        $db->commit();
    }

    //置換(基礎）
    public function replaceUpdateBase($db, $model)
    {
        $db->autoCommit(false);
        $array = explode(",", $model->replace_data["selectdata"]);
        for ($i = 0; $i < get_count($array); $i++) {
            unset($data);
            foreach ($model->replace_data["base_field"] as $key => $val) {
                $data[$key][TEXT] = $val;
            }
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]     = "sysdate()";
            $where  = " WHERE schregno = '" .$array[$i] ."'";

            $db->query(Query::updateSQL($data, "GRD_BASE_MST", $where));
        }
        $db->commit();
    }


    //各生徒情報
    public function getStudent_data($schregno, $model)
    {
        $db = Query::dbCheckOut();

        $query  = "";
        $query .= "  SELECT";
        $query .= "   T1.schregno, T2.annual, T1.inoutcd,";
        $query .= "   T1.name, T1.name_show, T1.name_kana, T1.name_eng,";
        $query .= "   T1.birthday, T1.sex, T1.bloodtype, T1.blood_rh,";
        $query .= "   T1.HANDICAP, T1.NATIONALITY,";
        $query .= "   T1.finschoolcd, T1.finish_date,";
        $query .= "   T1.prischoolcd,";
        if ($model->Properties["useAddrField2"] == "1") {
            $query .= "   T1.ent_addr2, T1.grd_addr2, ";
        }
        $query .= "   T1.ent_date, T1.ent_div, T1.ent_school, T1.ent_addr, ";
        $query .= "   T1.grd_date, T1.grd_div, T1.grd_school, T1.grd_addr, T1.grd_no,";
        $query .= "   T1.remark1, T1.remark2, T1.remark3,";
        $query .= "   T2.coursecode,";
        $query .= "   T1.updated AS updated1,";
        $query .= "   T2.attendno,";
        $query .= "   T2.coursecd ||','|| T2.majorcd AS coursemajorcd,";
        $query .= "   T2.updated AS updated2,";
        $query .= "   T2.grade,";
        $query .= "   T2.grade || ',' || T2.hr_class AS grcl";
        $query .= "  FROM";
        $query .= "   GRD_BASE_MST T1 ";
        $query .= "   LEFT OUTER JOIN GRD_REGD_DAT T2 ON T1.schregno = T2.schregno";
        $query .= "  WHERE";
        $query .= "   T1.schregno = '". $schregno. "' AND";
        $query .= "   T2.year    = '". $model->year. "' AND";
        $query .= "   T2.semester = '". $model->seme. "'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //入学事由取得
    public function getEnt_reason($schregno)
    {
        return "SELECT ent_reason FROM GRD_BASE_MST WHERE schregno = '".$schregno."'";
    }

    //卒業事由取得
    public function getGrd_reason($schregno)
    {
        return "SELECT grd_reason FROM GRD_BASE_MST WHERE schregno = '".$schregno."'";
    }

    //課程学科コンボ
    public function getCourse_Subject($model)
    {
        $query  = "";
        $query .= "  SELECT DISTINCT";
        $query .= "   coursecd || ',' || majorcd AS coursemajorcd,";
        $query .= "   coursename || majorabbv AS course_subject";
        $query .= "  FROM";
        $query .= "   v_course_major_mst";
        $query .= "  WHERE";
        $query .= "   year = '".$model->year."'";
        $query .= "  ORDER BY";
        $query .= "   coursemajorcd";
        return $query;
    }

    //出身中学校コンボ
    public function getFinschoolName($model)
    {
        return "SELECT finschoolcd, finschool_name FROM v_finschool_mst WHERE year = '".$model->year. "' ORDER BY finschoolcd ";
    }

    //出身塾コンボ
    public function getPrischoolName($model)
    {
        $query  = " SELECT prischoolcd, prischool_name ";
        $query .= " FROM v_prischool_mst ";
        $query .= " WHERE year = '".$model->year. "'";
        $query .= " ORDER BY prischoolcd";
        return $query;
    }

    //GRD_BASE_MSTにデータの有無チェック
    public function getExistBaseMst($schregno)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT COUNT(*) FROM GRD_BASE_MST ";
        $query .= " WHERE schregno ='". $schregno. "' ";

        $row = $db->getOne($query);
        Query::dbCheckIn($db);
        return (($row == 0)? false : true);
    }

    //年組名取得
    public function getHR_Name($model)
    {
        $tmp = explode(",", $model->GradeClass);
        $query  = "SELECT hr_name FROM GRD_REGD_HDAT";
        $query .= " WHERE grade = '".$tmp[0]."'";
        $query .= "   AND hr_class = '".$tmp[1]."'";
        $query .= "   AND year   = '".$model->year."'";
        $query .= "   AND semester = '".$model->seme."'";
        return $query;
    }

    //出席番号重複チェック
    public function isExistAttendno($model)
    {
        $db = Query::dbCheckOut();
        $arr = explode(",", $model->field["GRCL"]);

        $query  = " SELECT COUNT(*) FROM GRD_REGD_DAT ";
        $query .= " WHERE grade = '" .$arr[0]. "'";
        $query .= "   AND hr_class = '" .$arr[1]. "'";
        $query .= "   AND year   = '" .$model->year. "'";
        $query .= "   AND semester = '" .$model->seme. "'";
        $query .= "   AND attendno = '" .sprintf("%03d", $model->field["ATTENDNO"]). "'";

        if ($model->cmd == "update" || $model->cmd == "pre_up" || $model->cmd == "next_up") {
            $query .= "   AND schregno <> '" . $model->schregno. "'";
        }

        $row = $db->getOne($query);
        Query::dbCheckIn($db);
        return (($row == 0)? false : true);
    }

    //GRD_REGD_DATにデータの有無チェック
    public function getExistRegdDat($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT COUNT(*) FROM GRD_REGD_DAT ";
        $query .= " WHERE schregno = '". $model->field["SCHREGNO"]. "' ";
        $query .= "   AND semester = '". $model->seme. "' ";
        $query .= "   AND    year = '". $model->year. "' ";
        $row = $db->getOne($query);

        Query::dbCheckIn($db);
        return (($row == 0)? false : true);
    }

    //コースコード取得
    public function getCourseCode($model)
    {
        $query  = " SELECT coursecode, coursecodename ";
        $query .= "   FROM v_coursecode_mst ";
        $query .= " WHERE year   = '". $model->year ."'";
        $query .= " ORDER BY coursecode";
        return $query;
    }

    //V_NAME_MSTから情報を取得
    public function getNameMstData($cd, $model)
    {
        $query  = " SELECT * FROM v_name_mst ";
        $query .= " WHERE namecd1 ='" .$cd. "' AND year='".$model->year."'";
        $query .= " ORDER BY namecd1, namecd2";
        return $query;
    }

    public function &getInsertQuery($model, $table)
    {
        if ($table == "") {
            return false;
        }
        if ($model->field["COURSEMAJORCD"] == "") {
            return false;
        }

        $db = Query::dbCheckOut();

        if ($table == "GRD_BASE_MST") {
            $arr = explode(",", $model->field["GRCL"]);
            $data["SCHREGNO"][TEXT]     = substr("00000000".$model->field["SCHREGNO"], strlen($model->field["SCHREGNO"]), 8);
            $data["INOUTCD"][TEXT]      = $model->field["INOUTCD"];
            $data["NAME"][TEXT]         = $model->field["NAME"];
            $data["NAME_SHOW"][TEXT]    = $model->field["NAME_SHOW"];
            $data["NAME_KANA"][TEXT]    = $model->field["NAME_KANA"];
            $data["NAME_ENG"][TEXT]     = $model->field["NAME_ENG"];
            $data["BIRTHDAY"][TEXT]     = str_replace("/", "-", $model->field["BIRTHDAY"]);
            $data["SEX"][TEXT]          = $model->field["SEX"];
            $data["BLOODTYPE"][TEXT]    = $model->field["BLOODTYPE"];
            $data["BLOOD_RH"][TEXT]     = $model->field["BLOOD_RH"];
            $data["HANDICAP"][TEXT]     = $model->field["HANDICAP"];
            $data["NATIONALITY"][TEXT]  = $model->field["NATIONALITY"];
            $data["FINSCHOOLCD"][TEXT]  = $model->field["FINSCHOOLCD"];
            $data["FINISH_DATE"][TEXT]  = str_replace("/", "-", $model->field["FINISH_DATE"]);
            $data["PRISCHOOLCD"][TEXT]  = $model->field["PRISCHOOLCD"];
            $data["ENT_DATE"][TEXT]     = str_replace("/", "-", $model->field["ENT_DATE"]);
            $data["ENT_DIV"][TEXT]      = $model->field["ENT_DIV"];
            $data["ENT_REASON"][TEXT]   = $model->field["ENT_REASON"];
            $data["ENT_SCHOOL"][TEXT]   = $model->field["ENT_SCHOOL"];
            $data["ENT_ADDR"][TEXT]     = $model->field["ENT_ADDR"];
            if ($model->Properties["useAddrField2"] == "1") {
                $data["ENT_ADDR2"][TEXT]    = $model->field["ENT_ADDR2"];
            }
            $data["GRD_DATE"][TEXT]     = str_replace("/", "-", $model->field["GRD_DATE"]);
            $data["GRD_DIV"][TEXT]      = $model->field["GRD_DIV"];
            $data["GRD_REASON"][TEXT]   = $model->field["GRD_REASON"];
            $data["GRD_SCHOOL"][TEXT]   = $model->field["GRD_SCHOOL"];
            $data["GRD_ADDR"][TEXT]     = $model->field["GRD_ADDR"];
            if ($model->Properties["useAddrField2"] == "1") {
                $data["GRD_ADDR2"][TEXT]    = $model->field["GRD_ADDR2"];
            }
            $data["GRD_TERM"][TEXT]     = (strlen($model->field["GRD_DIV"])) ? $model->seme : "";
            $data["GRD_SEMESTER"][TEXT] = $model->seme;
            $data["GRD_GRADE"][TEXT]    = $arr[0];
            $data["GRD_HR_CLASS"][TEXT] = $arr[1];
            $data["REMARK1"][TEXT]      = $model->field["REMARK1"];
            $data["REMARK2"][TEXT]      = $model->field["REMARK2"];
            $data["REMARK3"][TEXT]      = $model->field["REMARK3"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
        }

        if ($table == "GRD_REGD_DAT") {
            $ary = explode(",", $model->field["COURSEMAJORCD"]);
            $arr = explode(",", $model->field["GRCL"]);
#           $data["SCHREGNO"][TEXT]         = $model->field["SCHREGNO"];
            $data["SCHREGNO"][TEXT]  = substr("00000000".$model->field["SCHREGNO"], strlen($model->field["SCHREGNO"]), 8);
            $data["YEAR"][TEXT]      = $model->year;
            $data["SEMESTER"][TEXT]  = $model->seme;
            $data["GRADE"][TEXT]        = $arr[0];
            $data["HR_CLASS"][TEXT]  = $arr[1];
            $data["ATTENDNO"][TEXT]  = sprintf("%03d", $model->field["ATTENDNO"]);
            $data["ANNUAL"][TEXT]      = sprintf("%02d", $model->field["ANNUAL"]);
            $data["SEAT_COL"][FUNC]  = "conv_col_row('".sprintf("%03d", $model->field["ATTENDNO"])."',1)";
            $data["SEAT_ROW"][FUNC]  = "conv_col_row('".sprintf("%03d", $model->field["ATTENDNO"])."',2)";
            $data["COURSECD"][TEXT]  = $ary[0];
            $data["MAJORCD"][TEXT]    = $ary[1];
            $data["COURSECODE"][TEXT]   = $model->field["COURSECODE"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]    = "sysdate()";
        }

        $query = Query::insertSQL($data, $table);
        $db->query($query);
        Query::dbCheckIn($db);
        return true;
    }

    public function &getUpdateQuery($model, $table)
    {
        if ($table == "") {
            return false;
        }
        $db = Query::dbCheckOut();

        if ($table == "GRD_BASE_MST") {
            $data["INOUTCD"][TEXT]      = $model->field["INOUTCD"];
            $data["NAME"][TEXT]         = $model->field["NAME"];
            $data["NAME_SHOW"][TEXT]    = $model->field["NAME_SHOW"];
            $data["NAME_KANA"][TEXT]    = $model->field["NAME_KANA"];
            $data["NAME_ENG"][TEXT]     = $model->field["NAME_ENG"];
            $data["BIRTHDAY"][TEXT]     = str_replace("/", "-", $model->field["BIRTHDAY"]);
            $data["SEX"][TEXT]          = $model->field["SEX"];
            $data["BLOODTYPE"][TEXT]    = $model->field["BLOODTYPE"];
            $data["BLOOD_RH"][TEXT]     = $model->field["BLOOD_RH"];
            $data["HANDICAP"][TEXT]     = $model->field["HANDICAP"];
            $data["NATIONALITY"][TEXT]  = $model->field["NATIONALITY"];
            $data["FINSCHOOLCD"][TEXT]  = $model->field["FINSCHOOLCD"];
            $data["FINISH_DATE"][TEXT]  = str_replace("/", "-", $model->field["FINISH_DATE"]);
            $data["PRISCHOOLCD"][TEXT]  = $model->field["PRISCHOOLCD"];
            $data["ENT_DATE"][TEXT]     = str_replace("/", "-", $model->field["ENT_DATE"]);
            $data["ENT_DIV"][TEXT]      = $model->field["ENT_DIV"];
            $data["ENT_REASON"][TEXT]   = $model->field["ENT_REASON"];
            $data["ENT_SCHOOL"][TEXT]   = $model->field["ENT_SCHOOL"];
            $data["ENT_ADDR"][TEXT]     = $model->field["ENT_ADDR"];
            if ($model->Properties["useAddrField2"] == "1") {
                $data["ENT_ADDR2"][TEXT]    = $model->field["ENT_ADDR2"];
            }
            $data["GRD_DATE"][TEXT]     = str_replace("/", "-", $model->field["GRD_DATE"]);
            $data["GRD_DIV"][TEXT]      = $model->field["GRD_DIV"];
            $data["GRD_REASON"][TEXT]   = $model->field["GRD_REASON"];
            $data["GRD_SCHOOL"][TEXT]   = $model->field["GRD_SCHOOL"];
            $data["GRD_ADDR"][TEXT]     = $model->field["GRD_ADDR"];
            if ($model->Properties["useAddrField2"] == "1") {
                $data["GRD_ADDR2"][TEXT]    = $model->field["GRD_ADDR2"];
            }
            $data["GRD_TERM"][TEXT]     = (strlen($model->field["GRD_DIV"])) ? $model->seme : "";
            $data["REMARK1"][TEXT]      = $model->field["REMARK1"];
            $data["REMARK2"][TEXT]      = $model->field["REMARK2"];
            $data["REMARK3"][TEXT]      = $model->field["REMARK3"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $where  = "WHERE SCHREGNO ='".$model->field["SCHREGNO"]."'";
        }

        if ($table == "GRD_REGD_DAT") {
            $ary = explode(",", $model->field["COURSEMAJORCD"]);
            $arr = explode(",", $model->field["GRCL"]);
            
            $data["GRADE"][TEXT]        = $arr[0];
            $data["HR_CLASS"][TEXT]  = $arr[1];
            $data["ATTENDNO"][TEXT]  = sprintf("%03d", $model->field["ATTENDNO"]);
            $data["ANNUAL"][TEXT]      = sprintf("%02d", $model->field["ANNUAL"]);
            $data["SEAT_COL"][FUNC]  = "conv_col_row('".sprintf("%03d", $model->field["ATTENDNO"])."',1)";
            $data["SEAT_ROW"][FUNC]  = "conv_col_row('".sprintf("%03d", $model->field["ATTENDNO"])."',2)";
            $data["COURSECD"][TEXT]  = $ary[0];
            $data["MAJORCD"][TEXT]    = $ary[1];
            $data["COURSECODE"][TEXT]   = $model->field["COURSECODE"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]    = "sysdate()";

            $where   = "WHERE SCHREGNO  ='".$model->field["SCHREGNO"]."' ";
            $where  .= "  AND YEAR    ='".$model->year."' ";
            $where  .= "  AND SEMESTER  ='".$model->seme."' ";
        }

        $query = Query::updateSQL($data, $table, $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return true;
    }

    public function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $db->query("DELETE FROM GRD_BASE_MST            WHERE SCHREGNO = '".$model->field["SCHREGNO"]."'");
        $db->query("DELETE FROM GRD_REGD_DAT            WHERE SCHREGNO = '".$model->field["SCHREGNO"]."'");

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }




    //学年と組
    public function getHrClass($model)
    {
        $query = "";
        $query .= "SELECT ";
        $query .= "  T1.GRADE, ";
        $query .= "  T1.HR_CLASS, ";
        $query .= "  T1.HR_NAME, ";
        $query .= "  T1.HR_NAMEABBV, ";
        $query .= "  T2.STAFFCD, ";
        $query .= "  T2.STAFFNAME_SHOW ";
        $query .= "FROM ";
        $query .= "  GRD_REGD_HDAT T1,";
        $query .= "  STAFF_MST T2 ";
        $query .= "WHERE ";
#       if ($model->usr_auth == DEF_UPDATE_RESTRICT) {           //2005/05/31
        if ($model->usr_auth == DEF_UPDATE_RESTRICT || $model->usr_auth == DEF_REFER_RESTRICT) {
#           $query .= " T1.TR_CD1 = '". STAFFCD ."' AND";         //2005/06/29
            $query .= " (T1.TR_CD1 = '". STAFFCD ."' OR";          //2005/06/29
            $query .= "  T1.TR_CD2 = '". STAFFCD ."' OR";          //2005/06/29
            $query .= "  T1.TR_CD3 = '". STAFFCD ."' OR";          //2005/06/29
            $query .= "  T1.SUBTR_CD1 = '". STAFFCD ."' OR";        //2005/06/29
            $query .= "  T1.SUBTR_CD2 = '". STAFFCD ."' OR";        //2005/06/29
            $query .= "  T1.SUBTR_CD3 = '". STAFFCD ."') AND";    //2005/06/29
        }
        $query .= "  T1.TR_CD1 = T2.STAFFCD AND ";
        $query .= "  T1.YEAR = '" .$model->year ."' AND ";
        $query .= "  T1.SEMESTER = '" .$model->seme ."' ";
        $query .= " ORDER BY ";
        $query .= "  T1.GRADE,T1.HR_CLASS ";

//      echo $query;
        return $query;
    }
    //年度学期取得
    public function getYear()
    {
        $query  = "SELECT DISTINCT YEAR,SEMESTER FROM GRD_REGD_HDAT ORDER BY YEAR DESC,SEMESTER DESC ";

        return $query;
    }
    //卒業年度
    public function getGrdYear()
    {
        $query = " SELECT DISTINCT ";
        $query .= "   FISCALYEAR(GRD_DATE) AS GRD_YEAR ";
        $query .= " FROM ";
        $query .= "   GRD_BASE_MST ";

        return $query;
    }
    public function getStudents($model)
    {
        if ($model->cmd == "list" && $model->grade) {
            $grade = $model->grade;
        } elseif ($model->cmd == "list" && !$model->grade) {
            $grade = "";
        } elseif ($model->cmd == "search" && $model->search["GRADE"]) {
            $grade = $model->search["GRADE"];
        }

        $query = " SELECT DISTINCT ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T1.GRADE, ";
        $query .= "   T1.HR_CLASS, ";
        $query .= "   T3.HR_NAME, ";
        $query .= "   T3.HR_NAMEABBV, ";
        $query .= "   T1.ATTENDNO, ";
        $query .= "   T2.SEX, ";
        $query .= "   T2.NAME_SHOW, ";
        $query .= "   T2.NAME_KANA, ";
        $query .= "   T2.GRD_SEMESTER, ";
        $query .= "   T2.GRD_GRADE, ";
        $query .= "   T2.GRD_HR_CLASS, ";
        $query .= "   T2.GRD_ATTENDNO, ";
        $query .= "   FISCALYEAR(T2.GRD_DATE) AS GRD_YEAR ";
        $query .= " FROM ";
        $query .= "   GRD_REGD_DAT T1, ";
        $query .= "   GRD_BASE_MST T2, ";
        $query .= "   GRD_REGD_HDAT T3 ";
        $query .= " WHERE ";
        if ($model->search["HR_CLASS"]) {
            $query .= "   T2.GRD_HR_CLASS = '" .sprintf("%03s", $model->search["HR_CLASS"]). "' AND ";
        }
        if ($model->search["GRD_YEAR"]) {  //卒業生
            $query .= "   FISCALYEAR(T2.GRD_DATE) = '" .$model->search["GRD_YEAR"]. "' AND ";
        }
        if ($model->search["COURSECODE"]) {
            $query .= "   T1.COURSECODE = '" .$model->search["COURSECODE"]. "' AND ";
        }
        if ($model->search["NAME"]) {
            $query .= "   T2.NAME LIKE '%" .str_replace("'", "''", $model->search["NAME"]). "%' AND ";
        }
        if ($model->search["NAME_SHOW"]) {
            $query .= "   T2.NAME_SHOW LIKE '%" .str_replace("'", "''", $model->search["NAME_SHOW"]). "%' AND ";
        }
        if ($model->search["NAME_KANA"]) {
            $query .= "   T2.NAME_KANA LIKE '%" .str_replace("'", "''", $model->search["NAME_KANA"]). "%' AND ";
        }
        if ($model->search["NAME_ENG"]) {
            $query .= "   T2.NAME_ENG LIKE '%" .str_replace("'", "''", $model->search["NAME_ENG"]). "%' AND ";
        }
        if ($model->search["SEX"]) {
            $query .= "   T2.SEX = '" .$model->search["SEX"]. "' AND ";
        }
        $query .= "   T1.YEAR    = T3.YEAR AND ";
        $query .= "   T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "   T1.GRADE  = T3.GRADE AND ";
        $query .= "   T1.HR_CLASS = T3.HR_CLASS AND ";

//      $query .= "   T1.YEAR    = FISCALYEAR(T2.GRD_DATE) AND ";
//      $query .= "   T1.SEMESTER = T2.GRD_SEMESTER AND ";
//      $query .= "   T1.GRADE  = T2.GRD_GRADE AND ";
//      $query .= "   T1.HR_CLASS = T2.GRD_HR_CLASS AND ";

        $query .= "   T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "   T1.GRADE,";
        $query .= "   T1.HR_CLASS,";
        $query .= "   T1.ATTENDNO ";

//      echo $query;
        return $query;
    }
}
