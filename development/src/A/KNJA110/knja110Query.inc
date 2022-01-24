<?php

require_once('for_php7.php');

class knja110Query extends Query {

    //起動チェック
    function ChecktoStart($db)
    {
        foreach (array("schreg_regd_hdat","v_course_mst","v_major_mst","v_finschool_mst") as $val)
        {
            $query = "SELECT COUNT(*) FROM ".$val." WHERE year = '".CTRL_YEAR."'";
            if ($val == "schreg_regd_hdat") $query .= " AND semester = '".CTRL_SEMESTER."'";

            if ($db->getOne($query) == "0") return false;
        }
        foreach (array("A001","A002","A003","Z002") as $val)
        {
            $query = "SELECT COUNT(*) FROM v_name_mst WHERE year = '".CTRL_YEAR."' AND namecd1 = '".$val."'";
            if ($db->getOne($query) == "0") return false;
        }
        return true;
    }

    //年組,先生名
    function getGrd_ClasQuery($model)
    {
        $query  = "";
        $query .= "  SELECT DISTINCT";
        $query .= "      T1.grade || ',' || T1.hr_class AS gc,";
        $query .= "      T1.hr_name,";
        $query .= "      T2.staffname_show AS name_show,";
        $query .= "      T2.staffcd";
        $query .= "  FROM";
        $query .= "      schreg_regd_hdat T1 ";
        $query .= "      LEFT OUTER JOIN staff_mst T2 ON T1.tr_cd1 = T2.staffcd";
        $query .= "  WHERE";
        $query .= "      T1.year     = '".CTRL_YEAR."' AND";
        $query .= "      T1.semester = '".CTRL_SEMESTER."'";
        if (AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "AND (T1.tr_cd1    = '".STAFFCD."' OR T1.tr_cd2    = '".STAFFCD."' OR T1.tr_cd3    = '".STAFFCD."' OR ";
            $query .= "     T1.subtr_cd1 = '".STAFFCD."' OR T1.subtr_cd2 = '".STAFFCD."' OR T1.subtr_cd3 = '".STAFFCD."')";
        }
        $query .= "  ORDER BY";
        $query .= "      gc";
        return $query;
    }

    //置換画面用の生徒一覧
    function GetStudent($GradeClass)
    {
        $arr = explode(",",$GradeClass);

        $query  = "";
        $query .= "  SELECT";
        $query .= "      T2.name_show ,";
        $query .= "      T1.schregno ,";
        $query .= "      T1.attendno";
        $query .= "  FROM";
        $query .= "      schreg_regd_dat T1";
        $query .= "      LEFT OUTER JOIN schreg_base_mst T2 ON T1.schregno = T2.schregno";
        $query .= "  WHERE";
        $query .= "      T1.year     = '".CTRL_YEAR."' AND";
        $query .= "      T1.semester = '".CTRL_SEMESTER."' AND";
        $query .= "      T1.grade    = '".$arr[0]."' AND";
        $query .= "      T1.hr_class = '".$arr[1]."'";
        $query .= "  ORDER BY";
        $query .= "      T1.attendno";

        return $query;
    }

    //置換(在籍)
    function ReplaceUpdate_Regd($db,$model)
    {
        $db->autoCommit(false);
        $array = explode(",", $model->replace_data["selectdata"]);
        for ($i = 0; $i < get_count($array); $i++)
        {
            foreach ($this->replace_data["regd_field"] as $key => $val) {
                $data[$key][TEXT] = $val;
            }
            $data["REGISTERCD"][TEXT] = STAFFCD;
            $data["UPDATED"][FUNC]    = "sysdate()";
            $where  = "WHERE schregno='".$array[$i]."'";
            $where .=" and year='".CTRL_YEAR."'";
            $where .=" and semester='".CTRL_SEMESTER."'";
            $db->query(Query::updateSQL($data, "schreg_regd_dat", $where));
        }
        $db->commit();
    }

    //置換(基礎）
    function ReplaceUpdate_Base($db,$model)
    {
        $db->autoCommit(false);
        $array = explode(",", $model->replace_data["selectdata"]);
        for ($i = 0; $i < get_count($array); $i++)
        {
            unset($data);
            foreach ($this->replace_data["base_field"] as $key => $val) {
                $data[$key][TEXT] = $val;
            }
            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][FUNC]       = "sysdate()";
            $where  = " WHERE schregno = '" .$array[$i] ."'";

            $db->query(Query::updateSQL($data, "schreg_base_mst", $where));
        }
        $db->commit();
    }


    //各生徒情報
    function getStudent_data($model, $schregno)
    {
        $db = Query::dbCheckOut();

        $query  = "";
        $query .= "  SELECT";
        $query .= "      T1.schregno, T2.annual, T1.inoutcd,";
        $query .= "      T1.name, T1.name_show, T1.name_kana, T1.name_eng,";
        $query .= "      T1.birthday, T1.sex, T1.bloodtype, T1.blood_rh,";
        $query .= "      T1.handicap, T1.nationality,";
        $query .= "      T1.finschoolcd, T1.finish_date,";
        $query .= "      T1.prischoolcd,";
        $query .= "      T1.ent_date, T1.ent_div, T1.ent_school, T1.ent_addr, ";
        if ($model->Properties["useAddrField2"] == "1") {
            $query .= "      T1.ent_addr2,";
            $query .= "      T1.grd_addr2,";
        }
        $query .= "      T1.grd_date, T1.grd_div, T1.grd_school, T1.grd_addr, T1.grd_no,";
        $query .= "      T1.remark1, T1.remark2, T1.remark3,";
        $query .= "      T2.coursecode,";
        $query .= "      T1.updated AS updated1,";
        $query .= "      T2.attendno,";
        $query .= "      T2.coursecd ||','|| T2.majorcd AS coursemajorcd,";
        $query .= "      T2.updated AS updated2,";
        $query .= "      T2.grade,";
        $query .= "      T2.grade || ',' || T2.hr_class AS grcl";
        $query .= "  FROM";
        $query .= "      schreg_base_mst T1 ";
        $query .= "      LEFT OUTER JOIN schreg_regd_dat T2 ON T1.schregno = T2.schregno";
        $query .= "  WHERE";
        $query .= "      T1.schregno = '". $schregno. "' AND";
        $query .= "      T2.year     = '". CTRL_YEAR. "' AND";
        $query .= "      T2.semester = '". CTRL_SEMESTER. "'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

/*    //卒業台帳番号取得
    function getGrd_no($schregno)
    {
        return "SELECT grd_no FROM schreg_base_mst WHERE schregno = '".$schregno."'";
    }
*/
    //入学事由取得
    function getEnt_reason($schregno)
    {
        return "SELECT ent_reason FROM schreg_base_mst WHERE schregno = '".$schregno."'";
    }

    //卒業事由取得
    function getGrd_reason($schregno)
    {
        return "SELECT grd_reason FROM schreg_base_mst WHERE schregno = '".$schregno."'";
    }

    //課程学科コンボ
    function getCourse_Subject()
    {
        $query  = "";
        $query .= "  SELECT DISTINCT";
        $query .= "      coursecd || ',' || majorcd AS coursemajorcd,";
        $query .= "      coursename || majorabbv AS course_subject";
        $query .= "  FROM";
        $query .= "      v_course_major_mst";
        $query .= "  WHERE";
        $query .= "      year = '".CTRL_YEAR."'";
        $query .= "  ORDER BY";
        $query .= "      coursemajorcd";
        return $query;
    }

    //出身中学校名取得
    function getFinschoolName($cd)
    {
        $query .= " SELECT ";
        $query .= "     FINSCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     FINSCHOOLCD = '{$cd}' ";

        return $query;
    }

    //出身塾コンボ
    function getPrischoolName()
    {
        $query  = " SELECT prischoolcd, prischool_name ";
        $query .= " FROM v_prischool_mst ";
        $query .= " WHERE year = '".CTRL_YEAR. "'";
        $query .= " ORDER BY prischoolcd";
        return $query;
    }

    //SCHREG_BASE_MSTにデータの有無チェック
    function getExist_base_mst($schregno)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT COUNT(*) FROM schreg_base_mst ";
        $query .= " WHERE schregno ='". $schregno. "' ";

        $row = $db->getOne($query);
        Query::dbCheckIn($db);
        return (($row == 0)? false : true);
    }

    //年組名取得
    function getHR_Name($model)
    {
        $tmp = explode(",",$model->GradeClass);
        $query  = "SELECT hr_name FROM schreg_regd_hdat";
        $query .= " WHERE grade    = '".$tmp[0]."'";
        $query .= "   AND hr_class = '".$tmp[1]."'";
        $query .= "   AND year     = '".CTRL_YEAR."'";
        $query .= "   AND semester = '".CTRL_SEMESTER."'";
        return $query;
    }

    //出席番号重複チェック
    function IsExistAttendno($model)
    {
        $db = Query::dbCheckOut();
        $arr = explode(",",$model->field["GRCL"]);

        $query  = " SELECT COUNT(*) FROM schreg_regd_dat ";
        $query .= " WHERE grade    = '" .$arr[0]. "'";
        $query .= "   AND hr_class = '" .$arr[1]. "'";
        $query .= "   AND year     = '" .CTRL_YEAR. "'";
        $query .= "   AND semester = '" .CTRL_SEMESTER. "'";
        $query .= "   AND attendno = '" .sprintf("%03d",$model->field["ATTENDNO"]). "'";

        if ($model->cmd == "update" || $model->cmd == "pre_up" || $model->cmd == "next_up")
            $query .= "   AND schregno <> '" . $model->schregno. "'";

        $row = $db->getOne($query);
        Query::dbCheckIn($db);
        return (($row == 0)? false : true);
    }

    //SCHREG_REGD_DATにデータの有無チェック
    function getExist_regd_dat($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT COUNT(*) FROM schreg_regd_dat ";
        $query .= " WHERE schregno = '". $model->field["SCHREGNO"]. "' ";
        $query .= "   AND semester = '". CTRL_SEMESTER. "' ";
        $query .= "   AND     year = '". CTRL_YEAR. "' ";
        $row = $db->getOne($query);

        Query::dbCheckIn($db);
        return (($row == 0)? false : true);
    }

    //コースコード取得
    function getCourseCode()
    {
        $query  = " SELECT coursecode, coursecodename ";
        $query .= "   FROM v_coursecode_mst ";
        $query .= " WHERE year     = '". CTRL_YEAR ."'";
        $query .= " ORDER BY coursecode";
        return $query;
    }

    //V_NAME_MSTから情報を取得
    function getNameMst_data($cd)
    {
        $query  = " SELECT * FROM v_name_mst ";
        $query .= " WHERE namecd1 ='" .$cd. "' AND year='".CTRL_YEAR."'";
        $query .= " ORDER BY namecd1, namecd2";
        return $query;
    }

    function &getInsertQuery($model, $table)
    {
        if($table == "") return false;
        if($model->field["COURSEMAJORCD"] == "") return false;

        $db = Query::dbCheckOut();

        if($table == "schreg_base_mst")
        {
#            $data["SCHREGNO"][TEXT]            = $model->field["SCHREGNO"];
            $data["SCHREGNO"][TEXT]     = substr("00000000".$model->field["SCHREGNO"],strlen($model->field["SCHREGNO"]),8);
            $data["INOUTCD"][TEXT]      = $model->field["INOUTCD"];
            $data["NAME"][TEXT]         = $model->field["NAME"];
            $data["NAME_SHOW"][TEXT]    = $model->field["NAME_SHOW"];
            $data["NAME_KANA"][TEXT]    = $model->field["NAME_KANA"];
            $data["NAME_ENG"][TEXT]     = $model->field["NAME_ENG"];
            $data["BIRTHDAY"][TEXT]     = str_replace("/","-",$model->field["BIRTHDAY"]);
            $data["SEX"][TEXT]          = $model->field["SEX"];
            $data["BLOODTYPE"][TEXT]    = $model->field["BLOODTYPE"];
            $data["BLOOD_RH"][TEXT]     = $model->field["BLOOD_RH"];
            $data["HANDICAP"][TEXT]     = $model->field["HANDICAP"];
            $data["NATIONALITY"][TEXT]  = $model->field["NATIONALITY"];
            $data["FINSCHOOLCD"][TEXT]  = $model->field["FINSCHOOLCD"];
            $data["FINISH_DATE"][TEXT]  = str_replace("/","-",$model->field["FINISH_DATE"]);
            $data["PRISCHOOLCD"][TEXT]  = $model->field["PRISCHOOLCD"];
            $data["ENT_DATE"][TEXT]     = str_replace("/","-",$model->field["ENT_DATE"]);
            $data["ENT_DIV"][TEXT]      = $model->field["ENT_DIV"];
            $data["ENT_REASON"][TEXT]   = $model->field["ENT_REASON"];
            $data["ENT_SCHOOL"][TEXT]   = $model->field["ENT_SCHOOL"];
            $data["ENT_ADDR"][TEXT]     = $model->field["ENT_ADDR"];
            if ($model->Properties["useAddrField2"] == "1") {
                $data["ENT_ADDR2"][TEXT]    = $model->field["ENT_ADDR2"];
            }
            $data["GRD_DATE"][TEXT]     = str_replace("/","-",$model->field["GRD_DATE"]);
            $data["GRD_DIV"][TEXT]      = $model->field["GRD_DIV"];
            $data["GRD_REASON"][TEXT]   = $model->field["GRD_REASON"];
            $data["GRD_SCHOOL"][TEXT]   = $model->field["GRD_SCHOOL"];
            $data["GRD_ADDR"][TEXT]     = $model->field["GRD_ADDR"];
            if ($model->Properties["useAddrField2"] == "1") {
                $data["GRD_ADDR2"][TEXT]    = $model->field["GRD_ADDR2"];
            }
            $data["GRD_TERM"][TEXT]     = (strlen($model->field["GRD_DIV"])) ? CTRL_SEMESTER : "";
            $data["REMARK1"][TEXT]      = $model->field["REMARK1"];
            $data["REMARK2"][TEXT]      = $model->field["REMARK2"];
            $data["REMARK3"][TEXT]      = $model->field["REMARK3"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
        }

        if($table == "schreg_regd_dat")
        {
            $ary = explode(",",$model->field["COURSEMAJORCD"]);
            $arr = explode(",",$model->field["GRCL"]);
#            $data["SCHREGNO"][TEXT]            = $model->field["SCHREGNO"];
            $data["SCHREGNO"][TEXT]     = substr("00000000".$model->field["SCHREGNO"],strlen($model->field["SCHREGNO"]),8);
            $data["YEAR"][TEXT]         = CTRL_YEAR;
            $data["SEMESTER"][TEXT]     = CTRL_SEMESTER;
            $data["GRADE"][TEXT]        = $arr[0];
            $data["HR_CLASS"][TEXT]     = $arr[1];
            $data["ATTENDNO"][TEXT]     = sprintf("%03d",$model->field["ATTENDNO"]);
            $data["ANNUAL"][TEXT]       = sprintf("%02d",$model->field["ANNUAL"]);
            $data["SEAT_COL"][FUNC]     = "conv_col_row('".sprintf("%03d",$model->field["ATTENDNO"])."',1)";
            $data["SEAT_ROW"][FUNC]     = "conv_col_row('".sprintf("%03d",$model->field["ATTENDNO"])."',2)";
            $data["COURSECD"][TEXT]     = $ary[0];
            $data["MAJORCD"][TEXT]      = $ary[1];
            $data["COURSECODE"][TEXT]   = $model->field["COURSECODE"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
        }

        $query = Query::insertSQL($data, $table);

        $db->query($query);
        Query::dbCheckIn($db);
        return true;
    }

    function &getUpdateQuery($model,$table)
    {
        if($table == "") return false;
        $db = Query::dbCheckOut();

        if($table == "schreg_base_mst")
        {
            $data["INOUTCD"][TEXT]      = $model->field["INOUTCD"];
            $data["NAME"][TEXT]         = $model->field["NAME"];
            $data["NAME_SHOW"][TEXT]    = $model->field["NAME_SHOW"];
            $data["NAME_KANA"][TEXT]    = $model->field["NAME_KANA"];
            $data["NAME_ENG"][TEXT]     = $model->field["NAME_ENG"];
            $data["BIRTHDAY"][TEXT]     = str_replace("/","-",$model->field["BIRTHDAY"]);
            $data["SEX"][TEXT]          = $model->field["SEX"];
            $data["BLOODTYPE"][TEXT]    = $model->field["BLOODTYPE"];
            $data["BLOOD_RH"][TEXT]     = $model->field["BLOOD_RH"];
            $data["HANDICAP"][TEXT]     = $model->field["HANDICAP"];
            $data["NATIONALITY"][TEXT]  = $model->field["NATIONALITY"];
            $data["FINSCHOOLCD"][TEXT]  = $model->field["FINSCHOOLCD"];
            $data["FINISH_DATE"][TEXT]  = str_replace("/","-",$model->field["FINISH_DATE"]);
            $data["PRISCHOOLCD"][TEXT]  = $model->field["PRISCHOOLCD"];
            $data["ENT_DATE"][TEXT]     = str_replace("/","-",$model->field["ENT_DATE"]);
            $data["ENT_DIV"][TEXT]      = $model->field["ENT_DIV"];
            $data["ENT_REASON"][TEXT]   = $model->field["ENT_REASON"];
            $data["ENT_SCHOOL"][TEXT]   = $model->field["ENT_SCHOOL"];
            $data["ENT_ADDR"][TEXT]     = $model->field["ENT_ADDR"];
            if ($model->Properties["useAddrField2"] == "1") {
                $data["ENT_ADDR2"][TEXT]    = $model->field["ENT_ADDR2"];
            }
            $data["GRD_DATE"][TEXT]     = str_replace("/","-",$model->field["GRD_DATE"]);
            $data["GRD_DIV"][TEXT]      = $model->field["GRD_DIV"];
            $data["GRD_REASON"][TEXT]   = $model->field["GRD_REASON"];
            $data["GRD_SCHOOL"][TEXT]   = $model->field["GRD_SCHOOL"];
            $data["GRD_ADDR"][TEXT]     = $model->field["GRD_ADDR"];
            if ($model->Properties["useAddrField2"] == "1") {
                $data["GRD_ADDR2"][TEXT]    = $model->field["GRD_ADDR2"];
            }
            $data["GRD_TERM"][TEXT]     = (strlen($model->field["GRD_DIV"])) ? CTRL_SEMESTER : "";
            $data["REMARK1"][TEXT]      = $model->field["REMARK1"];
            $data["REMARK2"][TEXT]      = $model->field["REMARK2"];
            $data["REMARK3"][TEXT]      = $model->field["REMARK3"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $where  = "WHERE SCHREGNO ='".$model->field["SCHREGNO"]."'";
        }

        if($table == "schreg_regd_dat")
        {
            $ary = explode(",",$model->field["COURSEMAJORCD"]);
            $arr = explode(",",$model->field["GRCL"]);

            $data["GRADE"][TEXT]        = $arr[0];
            $data["HR_CLASS"][TEXT]     = $arr[1];
            $data["ATTENDNO"][TEXT]     = sprintf("%03d",$model->field["ATTENDNO"]);
            $data["ANNUAL"][TEXT]       = sprintf("%02d",$model->field["ANNUAL"]);
            $data["SEAT_COL"][FUNC]     = "conv_col_row('".sprintf("%03d",$model->field["ATTENDNO"])."',1)";
            $data["SEAT_ROW"][FUNC]     = "conv_col_row('".sprintf("%03d",$model->field["ATTENDNO"])."',2)";
            $data["COURSECD"][TEXT]     = $ary[0];
            $data["MAJORCD"][TEXT]      = $ary[1];
            $data["COURSECODE"][TEXT]   = $model->field["COURSECODE"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $where   = "WHERE SCHREGNO  ='".$model->field["SCHREGNO"]."' ";
            $where  .= "  AND YEAR      ='".CTRL_YEAR."' ";
            $where  .= "  AND SEMESTER  ='".CTRL_SEMESTER."' ";
        }

        $query = Query::updateSQL($data, $table, $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return true;
    }

    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $db->query("DELETE FROM SCHREG_BASE_MST         WHERE SCHREGNO = '".$model->field["SCHREGNO"]."'");
        $db->query("DELETE FROM SCHREG_REGD_DAT         WHERE SCHREGNO = '".$model->field["SCHREGNO"]."'");
        $db->query("DELETE FROM SCHREG_TRANSFER_DAT     WHERE SCHREGNO = '".$model->field["SCHREGNO"]."'");
        $db->query("DELETE FROM SCHREG_ADDRESS_DAT      WHERE SCHREGNO = '".$model->field["SCHREGNO"]."'");
        $db->query("DELETE FROM ATTEND_DAT              WHERE SCHREGNO = '".$model->field["SCHREGNO"]."'");
        $db->query("DELETE FROM ATTEND_SEMES_DAT        WHERE SCHREGNO = '".$model->field["SCHREGNO"]."'");
        $db->query("DELETE FROM ATTEND_SUBCLASS_DAT     WHERE SCHREGNO = '".$model->field["SCHREGNO"]."'");
        $db->query("DELETE FROM CERTIF_ISSUE_DAT        WHERE SCHREGNO = '".$model->field["SCHREGNO"]."'");
        $db->query("DELETE FROM CHAIR_STD_DAT           WHERE SCHREGNO = '".$model->field["SCHREGNO"]."'");
        $db->query("DELETE FROM CLASS_FORMATION_DAT     WHERE SCHREGNO = '".$model->field["SCHREGNO"]."'");
        $db->query("DELETE FROM FRESHMAN_DAT            WHERE SCHREGNO = '".$model->field["SCHREGNO"]."'");
        $db->query("DELETE FROM GUARDIAN_DAT            WHERE SCHREGNO = '".$model->field["SCHREGNO"]."'");
        $db->query("DELETE FROM HEXAM_EMPREMARK_DAT     WHERE SCHREGNO = '".$model->field["SCHREGNO"]."'");
        $db->query("DELETE FROM HEXAM_ENTREMARK_DAT     WHERE SCHREGNO = '".$model->field["SCHREGNO"]."'");
        $db->query("DELETE FROM HEXAM_ENTREMARK_HDAT    WHERE SCHREGNO = '".$model->field["SCHREGNO"]."'");
        $db->query("DELETE FROM HREPORTREMARK_DAT       WHERE SCHREGNO = '".$model->field["SCHREGNO"]."'");
        $db->query("DELETE FROM HTRAINREMARK_DAT        WHERE SCHREGNO = '".$model->field["SCHREGNO"]."'");
        $db->query("DELETE FROM MEDEXAM_DET_DAT         WHERE SCHREGNO = '".$model->field["SCHREGNO"]."'");
        $db->query("DELETE FROM MEDEXAM_HDAT            WHERE SCHREGNO = '".$model->field["SCHREGNO"]."'");
        $db->query("DELETE FROM MEDEXAM_TOOTH_DAT       WHERE SCHREGNO = '".$model->field["SCHREGNO"]."'");
        $db->query("DELETE FROM SCHREG_ATTENDREC_DAT    WHERE SCHREGNO = '".$model->field["SCHREGNO"]."'");
//        $db->query("DELETE FROM SCHREG_AWARD_DAT        WHERE SCHREGNO = '".$model->field["SCHREGNO"]."'");
        $db->query("DELETE FROM SCHREG_DETAILHIST_DAT   WHERE SCHREGNO = '".$model->field["SCHREGNO"]."'");	//2006.02.06 alp m-yama
        $db->query("DELETE FROM SCHREG_ENVIR_DAT        WHERE SCHREGNO = '".$model->field["SCHREGNO"]."'");
        $db->query("DELETE FROM SCHREG_QUALIFIED_DAT    WHERE SCHREGNO = '".$model->field["SCHREGNO"]."'");
        $db->query("DELETE FROM SCHREG_RELA_DAT         WHERE SCHREGNO = '".$model->field["SCHREGNO"]."'");
        $db->query("DELETE FROM SCHREG_STUDYREC_DAT     WHERE SCHREGNO = '".$model->field["SCHREGNO"]."'");
        $db->query("DELETE FROM SCHREG_TRAINHIST_DAT    WHERE SCHREGNO = '".$model->field["SCHREGNO"]."'");
        $db->query("DELETE FROM SCHREG_TRAIN_DAT        WHERE SCHREGNO = '".$model->field["SCHREGNO"]."'");
        $db->query("DELETE FROM STUDYCLASSREMARK_DAT    WHERE SCHREGNO = '".$model->field["SCHREGNO"]."'");
        $db->query("DELETE FROM STUDYRECREMARK_DAT      WHERE SCHREGNO = '".$model->field["SCHREGNO"]."'");

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
?>
