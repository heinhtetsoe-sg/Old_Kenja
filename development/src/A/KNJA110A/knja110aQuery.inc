<?php

require_once('for_php7.php');

class knja110aQuery extends Query
{

    //復学あり
    public function getComeBackT()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SYSCAT.COLUMNS ";
        $query .= " WHERE ";
        $query .= "     TABNAME = 'SCHREG_ENT_GRD_HIST_COMEBACK_DAT' ";

        return $query;
    }

    //復学日
    public function getCBEntDate($model)
    {
        $query  = " WITH MIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     MIN(COMEBACK_DATE) AS COMEBACK_DATE ";
        $query .= " FROM ";
        $query .= "     SCHREG_ENT_GRD_HIST_COMEBACK_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND SCHOOL_KIND = '". $model->schoolKind. "' ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     ENT_DATE ";
        $query .= " FROM ";
        $query .= "     SCHREG_ENT_GRD_HIST_COMEBACK_DAT T1, ";
        $query .= "     MIN_T ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = MIN_T.SCHREGNO ";
        $query .= "     AND T1.SCHOOL_KIND = '". $model->schoolKind. "' ";
        $query .= "     AND T1.COMEBACK_DATE = MIN_T.COMEBACK_DATE ";

        return $query;
    }

    //学年種別取得
    public function getSchoolKind($model)
    {
        if ($model->field["GRCL"]) {
            $arr = explode(",", $model->field["GRCL"]);
        } else {
            $arr = explode(",", $model->GradeClass);
        }

        $query  = "";
        $query .= "  SELECT";
        $query .= "      T1.SCHOOL_KIND ";
        $query .= "  FROM";
        $query .= "      SCHREG_REGD_GDAT T1 ";
        $query .= "  WHERE";
        $query .= "      T1.YEAR     = '".CTRL_YEAR."' AND";
        $query .= "      T1.GRADE    = '".$arr[0]."' ";

        return $query;
    }

    //起動チェック
    public function checktoStart($db)
    {
        foreach (array("schreg_regd_hdat","v_course_mst","v_major_mst","v_finschool_mst") as $val) {
            $query = "SELECT COUNT(*) FROM ".$val." WHERE year = '".CTRL_YEAR."'";
            if ($val == "schreg_regd_hdat") {
                $query .= " AND semester = '".CTRL_SEMESTER."'";
            }

            if ($db->getOne($query) == "0") {
                return false;
            }
        }
        foreach (array("A001","A002","A003","Z002") as $val) {
            $query = "SELECT COUNT(*) FROM v_name_mst WHERE year = '".CTRL_YEAR."' AND namecd1 = '".$val."'";
            if ($db->getOne($query) == "0") {
                return false;
            }
        }
        return true;
    }
    //複式年組コンボ
    public function getGhrCd($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.GHR_CD, ";
        $query .= "     T1.GHR_NAME, ";
        $query .= "     T1.GHR_NAMEABBV, ";
        $query .= "     T2.STAFFCD, ";
        $query .= "     T2.STAFFNAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GHR_HDAT T1 ";
        $query .= "     INNER JOIN STAFF_MST T2 ON T2.STAFFCD = T1.TR_CD1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        if (AUTHORITY == DEF_UPDATE_RESTRICT || AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= "     AND (T1.TR_CD1    = '". STAFFCD ."' OR ";
            $query .= "          T1.TR_CD2    = '". STAFFCD ."' OR ";
            $query .= "          T1.TR_CD3    = '". STAFFCD ."' OR ";
            $query .= "          T1.SUBTR_CD1 = '". STAFFCD ."' OR ";
            $query .= "          T1.SUBTR_CD2 = '". STAFFCD ."' OR ";
            $query .= "          T1.SUBTR_CD3 = '". STAFFCD ."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GHR_CD ";
        return $query;
    }

    //FI複式年組コンボ
    public function getFiGradeHrclass($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || ',' || T1.HR_CLASS AS FI_GRADE_HR_CLASS, ";
        $query .= "     T1.HR_NAME AS FI_HR_CLASS_NAME, ";
        $query .= "     T1.HR_NAMEABBV AS FI_HR_NAMEABBV, ";
        $query .= "     T2.STAFFCD, ";
        $query .= "     T2.STAFFNAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_FI_HDAT T1 ";
        $query .= "     INNER JOIN STAFF_MST T2 ON T2.STAFFCD = T1.TR_CD1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        if (AUTHORITY == DEF_UPDATE_RESTRICT || AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= "     AND (T1.TR_CD1    = '". STAFFCD ."' OR ";
            $query .= "          T1.TR_CD2    = '". STAFFCD ."' OR ";
            $query .= "          T1.TR_CD3    = '". STAFFCD ."' OR ";
            $query .= "          T1.SUBTR_CD1 = '". STAFFCD ."' OR ";
            $query .= "          T1.SUBTR_CD2 = '". STAFFCD ."' OR ";
            $query .= "          T1.SUBTR_CD3 = '". STAFFCD ."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     FI_GRADE_HR_CLASS ";

        return $query;
    }
    //年組,先生名
    public function getGrdClasQuery($model)
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
    public function getStudent($model)
    {
        if ($model->field["GRCL"]) {
            $arr = explode(",", $model->field["GRCL"]);
        } else {
            $arr = explode(",", $model->GradeClass);
        }

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
    public function replaceUpdateRegd1($db, $model)
    {
        $db->autoCommit(false);
        $array = explode(",", $model->replace_data["selectdata"]);
        for ($i = 0; $i < get_count($array); $i++) {
            foreach ($model->replace_data["regd_field"] as $key => $val) {
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

    //置換(在籍)
    public function replaceUpdateRegd($db, $data, $schregno)
    {
        $year = CTRL_YEAR;
        $semester = CTRL_SEMESTER;
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC]    = "sysdate()";
        $where  = "WHERE schregno='{$schregno}'";
        $where .=" and year='{$year}'";
        $where .=" and semester='{$semester}'";

        $db->query(Query::updateSQL($data, "schreg_regd_dat", $where));
    }

    //置換(基礎）
    public function replaceUpdateBase1($db, $model)
    {
        $db->autoCommit(false);
        $array = explode(",", $model->replace_data["selectdata"]);
        for ($i = 0; $i < get_count($array); $i++) {
            unset($data);
            foreach ($model->replace_data["base_field"] as $key => $val) {
                if ($key == "NYUGAKUMAE_SYUSSIN_JOUHOU") {
                    $query = "DELETE FROM SCHREG_BASE_DETAIL_MST WHERE SCHREGNO ='".$array[$i]."' AND BASE_SEQ = '002' ";
                    $db->query($query);
                    $dataDetail = array();
                    $dataDetail["SCHREGNO"][TEXT]     = $array[$i];
                    $dataDetail["BASE_SEQ"][TEXT]     = "002";
                    $dataDetail["BASE_REMARK1"][TEXT] = $val;
                    $dataDetail["REGISTERCD"][TEXT]   = STAFFCD;
                    $dataDetail["UPDATED"][FUNC]      = "sysdate()";
                    $query = Query::insertSQL($dataDetail, "SCHREG_BASE_DETAIL_MST");
                    $db->query($query);
                } else {
                    $data[$key][TEXT] = $val;
                }
            }
            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][FUNC]       = "sysdate()";
            $where  = " WHERE schregno = '" .$array[$i] ."'";

            $db->query(Query::updateSQL($data, "schreg_base_mst", $where));

            if ($model->replace_data["base_field"]["GRD_DIV"] == "1" ||
                $model->replace_data["base_field"]["GRD_DIV"] == "2" ||
                $model->replace_data["base_field"]["GRD_DIV"] == "3"
            ) {
                $query = "DELETE FROM CLASS_FORMATION_DAT WHERE SCHREGNO = '{$array[$i]}' AND YEAR = '".(CTRL_YEAR + 1)."'";
                $db->query($query);
            }
        }
        $db->commit();
    }

    //置換(基礎）
    public function replaceUpdateEntGrd1($db, $model)
    {
        $db->autoCommit(false);
        $array = explode(",", $model->replace_data["selectdata"]);
        for ($i = 0; $i < get_count($array); $i++) {
            unset($data);
            foreach ($model->replace_data["entGrd_field"] as $key => $val) {
                $data[$key][TEXT] = $val;
            }
            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][FUNC]       = "sysdate()";
            $where  = " WHERE ";
            $where .= "     SCHREGNO = '" .$array[$i] ."'";
            $where .= "     AND SCHOOL_KIND = '".$model->schoolKind."'";

            $db->query(Query::updateSQL($data, "SCHREG_ENT_GRD_HIST_DAT", $where));

            $entGrdData = $db->getRow(knja110aQuery::getEntGrdData($model, $array[$i], "MAX"), DB_FETCHMODE_ASSOC);

            $updEntGrdData = array();
            $updEntGrdData["FINSCHOOLCD"][TEXT]     = ($entGrdData["FINSCHOOLCD"] ? sprintf("%0".($model->finschoolcdKeta)."s", $entGrdData["FINSCHOOLCD"]) : "");
            $updEntGrdData["FINISH_DATE"][TEXT]     = str_replace("/", "-", $entGrdData["FINISH_DATE"]);
            $updEntGrdData["ENT_DATE"][TEXT]        = str_replace("/", "-", $entGrdData["ENT_DATE"]);
            $updEntGrdData["ENT_DIV"][TEXT]         = $entGrdData["ENT_DIV"];
            $updEntGrdData["ENT_REASON"][TEXT]      = $entGrdData["ENT_REASON"];
            $updEntGrdData["ENT_SCHOOL"][TEXT]      = $entGrdData["ENT_SCHOOL"];
            $updEntGrdData["ENT_ADDR"][TEXT]        = $entGrdData["ENT_ADDR"];
            if ($model->Properties["useAddrField2"] == "1" || $model->schoolKind == "H") {
                $updEntGrdData["ENT_ADDR2"][TEXT]       = $entGrdData["ENT_ADDR2"];
            }
            $updEntGrdData["GRD_DATE"][TEXT]        = str_replace("/", "-", $entGrdData["GRD_DATE"]);
            $updEntGrdData["GRD_DIV"][TEXT]         = $entGrdData["GRD_DIV"];
            $updEntGrdData["GRD_REASON"][TEXT]      = $entGrdData["GRD_REASON"];
            $updEntGrdData["GRD_SCHOOL"][TEXT]      = $entGrdData["GRD_SCHOOL"];
            $updEntGrdData["GRD_ADDR"][TEXT]        = $entGrdData["GRD_ADDR"];
            if ($model->Properties["useAddrField2"] == "1" || $model->schoolKind == "H") {
                $updEntGrdData["GRD_ADDR2"][TEXT]       = $entGrdData["GRD_ADDR2"];
            }
            $updEntGrdData["REGISTERCD"][TEXT]      = STAFFCD;
            $updEntGrdData["UPDATED"][FUNC]         = "sysdate()";

            $where  = " WHERE ";
            $where .= "     SCHREGNO = '".$array[$i]."' ";
            $query = Query::updateSQL($updEntGrdData, "SCHREG_BASE_MST", $where);
            $db->query($query);
            if ($entGrdData["GRD_DIV"] == "1" ||
                $entGrdData["GRD_DIV"] == "2" ||
                $entGrdData["GRD_DIV"] == "3"
            ) {
                $query = "DELETE FROM CLASS_FORMATION_DAT WHERE SCHREGNO = '{$array[$i]}' AND YEAR = '".(CTRL_YEAR + 1)."'";
                $db->query($query);
            }
        }
        $db->commit();
    }

    //置換(基礎）
    public function replaceUpdateBase($db, $model, $data, $schregno)
    {
        if ($model->Properties["useAddrField2"] == "1" || $model->schoolKind == "H") {
            $checkArray = array("FINSCHOOLCD", "FINISH_DATE", "ENT_DATE", "CURRICULUM_YEAR", "ENT_DIV", "ENT_REASON", "ENT_SCHOOL", "ENT_ADDR", "ENT_ADDR2", "GRD_DATE", "GRD_DIV", "GRD_REASON", "GRD_SCHOOL", "GRD_ADDR", "GRD_ADDR2");
        } else {
            $checkArray = array("FINSCHOOLCD", "FINISH_DATE", "ENT_DATE", "CURRICULUM_YEAR", "ENT_DIV", "ENT_REASON", "ENT_SCHOOL", "ENT_ADDR", "GRD_DATE", "GRD_DIV", "GRD_REASON", "GRD_SCHOOL", "GRD_ADDR");
        }
        $setEntGrdFlg = false;
        $setEntGrdData = array();
        foreach ($checkArray as $key => $val) {
            if (array_key_exists($val, $data)) {
                $setEntGrdFlg = true;
                $setEntGrdData[$val] = $data[$val];
            }
        }
        if ($setEntGrdFlg) {
            $schregno = substr("00000000".$schregno, strlen($schregno), 8);
            $entGrdData = $db->getRow(knja110aQuery::getEntGrdData($model, $schregno, ""), DB_FETCHMODE_ASSOC);
            if (!is_array($entGrdData)) {
                $setEntGrdData["SCHREGNO"][TEXT]    = $schregno;
                $setEntGrdData["SCHOOL_KIND"][TEXT] = $model->schoolKind;
            }
            $setEntGrdData["REGISTERCD"][TEXT]  = STAFFCD;
            $setEntGrdData["UPDATED"][FUNC]     = "sysdate()";

            if (!is_array($entGrdData)) {
                $query = Query::insertSQL($setEntGrdData, "SCHREG_ENT_GRD_HIST_DAT");
            } else {
                $where .= " WHERE ";
                $where .= "     SCHREGNO = '".$schregno."' ";
                $where .= "     AND SCHOOL_KIND = '". $model->schoolKind. "' ";
                $query = Query::updateSQL($setEntGrdData, "SCHREG_ENT_GRD_HIST_DAT", $where);
            }
            $db->query($query);
        }

        unset($data["CURRICULUM_YEAR"]);

        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";
        $where  = " WHERE schregno = '{$schregno}'";

        $query = Query::updateSQL($data, "schreg_base_mst", $where);
        $db->query($query);

        $entGrdData = $db->getRow(knja110aQuery::getEntGrdData($model, $schregno, "MAX"), DB_FETCHMODE_ASSOC);

        $updEntGrdData = array();
        $updEntGrdData["FINSCHOOLCD"][TEXT]     = ($entGrdData["FINSCHOOLCD"] ? sprintf("%0".($model->finschoolcdKeta)."s", $entGrdData["FINSCHOOLCD"]) : "");
        $updEntGrdData["FINISH_DATE"][TEXT]     = str_replace("/", "-", $entGrdData["FINISH_DATE"]);
        $updEntGrdData["ENT_DATE"][TEXT]        = str_replace("/", "-", $entGrdData["ENT_DATE"]);
        $updEntGrdData["ENT_DIV"][TEXT]         = $entGrdData["ENT_DIV"];
        $updEntGrdData["ENT_REASON"][TEXT]      = $entGrdData["ENT_REASON"];
        $updEntGrdData["ENT_SCHOOL"][TEXT]      = $entGrdData["ENT_SCHOOL"];
        $updEntGrdData["ENT_ADDR"][TEXT]        = $entGrdData["ENT_ADDR"];
        if ($model->Properties["useAddrField2"] == "1" || $model->schoolKind == "H") {
            $updEntGrdData["ENT_ADDR2"][TEXT]       = $entGrdData["ENT_ADDR2"];
        }
        $updEntGrdData["GRD_DATE"][TEXT]        = str_replace("/", "-", $entGrdData["GRD_DATE"]);
        $updEntGrdData["GRD_DIV"][TEXT]         = $entGrdData["GRD_DIV"];
        $updEntGrdData["GRD_REASON"][TEXT]      = $entGrdData["GRD_REASON"];
        $updEntGrdData["GRD_SCHOOL"][TEXT]      = $entGrdData["GRD_SCHOOL"];
        $updEntGrdData["GRD_ADDR"][TEXT]        = $entGrdData["GRD_ADDR"];
        if ($model->Properties["useAddrField2"] == "1" || $model->schoolKind == "H") {
            $updEntGrdData["GRD_ADDR2"][TEXT]       = $entGrdData["GRD_ADDR2"];
        }
        $updEntGrdData["REGISTERCD"][TEXT]      = STAFFCD;
        $updEntGrdData["UPDATED"][FUNC]         = "sysdate()";

        $where  = " WHERE ";
        $where .= "     SCHREGNO = '".$schregno."' ";
        $query = Query::updateSQL($updEntGrdData, "SCHREG_BASE_MST", $where);

        if ($entGrdData["GRD_DIV"] == "1" ||
            $entGrdData["GRD_DIV"] == "2" ||
            $entGrdData["GRD_DIV"] == "3"
        ) {
            $query = "DELETE FROM CLASS_FORMATION_DAT WHERE SCHREGNO = '{$schregno}' AND YEAR = '".(CTRL_YEAR + 1)."'";
            $db->query($query);
        }

        $db->query($query);
    }

    //更新対象の人のschreg_base_mstに変更があるかのチェックに使う
    public function getBaseField($schregno)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' ";

        return $query;
    }

    public function getRegdField($schregno)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "        SCHREGNO = '{$schregno}' ";
        $query .= "    AND YEAR     = 'CTRL_YEAR' ";
        $query .= "    AND SEMESTER = 'CTRL_SEMESTER' ";

        return $query;
    }

    //最終履歴の終了日取得
    public function getEappDate($schregno)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ENT_DATE, ";
        $query .= "     L1.EXPIREDATE ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1 ";
        $query .= " LEFT JOIN SCHREG_BASE_HIST_DAT L1 ON  T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "                                   AND L1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "                                   AND L1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " WHERE ";
        $query .= "         T1.SCHREGNO = '{$schregno}' ";
        $query .= " ORDER BY ISSUEDATE DESC ";
        $query .= " FETCH FIRST ROW ONLY ";

        return $query;
    }

    /****************************** この関数は不要だと思うから最後にチェック *******************************************/
    //生徒履歴情報
    public function getStudentHistData($schregno)
    {
        $db = Query::dbCheckOut();

        $query  = "  SELECT ";
        $query .= "      * ";
        $query .= "  FROM ";
        $query .= "      SCHREG_BASE_HIST_DAT ";
        $query .= "  WHERE ";
        $query .= "      SCHREGNO = '{$schregno}' AND ";
        $query .= "      YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "      SEMESTER = '". CTRL_SEMESTER. "' ";
        $query .= "  ORDER BY ";
        $query .= "      ISSUEDATE DESC ";
        $query .= "  FETCH FIRST 1 ROWS ONLY ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //各生徒情報
    public function getStudentData($schregno, $model)
    {
        $db = Query::dbCheckOut();

        $setApplicantDiv = ($model->schoolKind == "J") ? "1": "2";//(1:中学 2:高校)

        $query  = "  SELECT";
        $query .= "      T1.SCHREGNO,";
        if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
            $query .= "     CASE WHEN T5.BASE_REMARK2 = '1' THEN T5.BASE_REMARK2 ELSE '' END AS VISITOR,";
        }
        $query .= "      T2.ANNUAL,";
        $query .= "      T1.INOUTCD,";
        $query .= "      T1.NAME,";
        $query .= "      T1.NAME_SHOW,";
        $query .= "      T1.NAME_KANA,";
        $query .= "      T1.NAME_ENG,";
        $query .= "      T1.REAL_NAME,";
        $query .= "      T1.REAL_NAME_KANA,";
        $query .= "      T1.BIRTHDAY,";
        $query .= "      T1.SEX,";
        $query .= "      T1.BLOODTYPE,";
        $query .= "      T1.BLOOD_RH,";
        $query .= "      CASE WHEN T5.BASE_REMARK1 IS NULL ";
        $query .= "           THEN T1.HANDICAP ";
        $query .= "           ELSE T5.BASE_REMARK1 ";
        $query .= "      END AS HANDICAP,";
        $query .= "      T1.NATIONALITY,";
        $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "           THEN T3.FINSCHOOLCD ";
        $query .= "           ELSE T1.FINSCHOOLCD ";
        $query .= "      END AS FINSCHOOLCD, ";
        $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "           THEN T3.FINISH_DATE ";
        $query .= "           ELSE T1.FINISH_DATE ";
        $query .= "      END AS FINISH_DATE, ";
        $query .= "      T1.PRISCHOOLCD,";
        $query .= "      T6.BASE_REMARK1 AS PRISCHOOL_CLASS_CD,";
        $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "           THEN T3.ENT_DATE ";
        $query .= "           ELSE T1.ENT_DATE ";
        $query .= "      END AS ENT_DATE, ";
        $query .= "      T3.CURRICULUM_YEAR, ";
        $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "           THEN T3.ENT_DIV ";
        $query .= "           ELSE T1.ENT_DIV ";
        $query .= "      END AS ENT_DIV, ";
        $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "           THEN T3.ENT_REASON ";
        $query .= "           ELSE T1.ENT_REASON ";
        $query .= "      END AS ENT_REASON, ";
        $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "           THEN T3.ENT_SCHOOL ";
        $query .= "           ELSE T1.ENT_SCHOOL ";
        $query .= "      END AS ENT_SCHOOL, ";
        $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "           THEN T3.ENT_ADDR ";
        $query .= "           ELSE T1.ENT_ADDR ";
        $query .= "      END AS ENT_ADDR, ";
        if ($model->Properties["useAddrField2"] == "1" || $model->schoolKind == "H") {
            $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
            $query .= "           THEN T3.ENT_ADDR2 ";
            $query .= "           ELSE T1.ENT_ADDR2 ";
            $query .= "      END AS ENT_ADDR2, ";
        }
        $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "           THEN T3.GRD_DATE ";
        $query .= "           ELSE T1.GRD_DATE ";
        $query .= "      END AS GRD_DATE, ";
        $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "           THEN T3.GRD_DIV ";
        $query .= "           ELSE T1.GRD_DIV ";
        $query .= "      END AS GRD_DIV, ";
        $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "           THEN T3.GRD_REASON ";
        $query .= "           ELSE T1.GRD_REASON ";
        $query .= "      END AS GRD_REASON, ";
        $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "           THEN T3.GRD_SCHOOL ";
        $query .= "           ELSE T1.GRD_SCHOOL ";
        $query .= "      END AS GRD_SCHOOL, ";
        $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "           THEN T3.GRD_ADDR ";
        $query .= "           ELSE T1.GRD_ADDR ";
        $query .= "      END AS GRD_ADDR, ";
        if ($model->Properties["useAddrField2"] == "1" || $model->schoolKind == "H") {
            $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
            $query .= "           THEN T3.GRD_ADDR2 ";
            $query .= "           ELSE T1.GRD_ADDR2 ";
            $query .= "      END AS GRD_ADDR2, ";
        }
        $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "           THEN T3.GRD_NO ";
        $query .= "           ELSE T1.GRD_NO ";
        $query .= "      END AS GRD_NO, ";
        $query .= "      T1.REMARK1,";
        $query .= "      T1.REMARK2,";
        $query .= "      T1.REMARK3,";
        $query .= "      T1.TENGAKU_SAKI_ZENJITU,";
        $query .= "      T1.TENGAKU_SAKI_GRADE, ";
        //出身
        if ($model->Properties["Origin_hyouji"] == '1') {
            $query .= "      D016.BASE_REMARK1 AS ORIGIN, ";
        }
        $query .= "      T1.NYUGAKUMAE_SYUSSIN_JOUHOU,";
        $query .= "      T1.EXAMNO, ";
        if ($model->school_name == "koma") {
            $query .= "      value(C1.CLASSIFY_CD,'') || '  ' || value(C1.CLASSIFY_NAME,'') AS CLASSIFY_NAME, ";
        } elseif ($model->school_name == "meikei") {
            $query .= "      D014.BASE_REMARK1 AS TESTDIV, ";
            $query .= "      D014.BASE_REMARK2 AS EXAM_TYPE, ";
        } else {
            $query .= "      D003.BASE_REMARK2 || '  ' || VALUE(N1.NAME1,'') AS TESTDIV, ";
        }
        $query .= "      T2.COURSECODE,";
        $query .= "      T1.UPDATED AS UPDATED1,";
        $query .= "      T2.ATTENDNO,";
        $query .= "      T2.COURSECD ||','|| T2.MAJORCD AS COURSEMAJORCD,";
        $query .= "      T2.UPDATED AS UPDATED2,";
        //特別支援学校対応
        if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
            //複式年組
            $query .= "     L2.GHR_CD, ";
            $query .= "     L2.GHR_ATTENDNO, ";
        } elseif ($model->Properties["useFi_Hrclass"] == '1') {
            //FI複式年組
            $query .= "     L2.GRADE || ',' || L2.HR_CLASS AS FI_GRADE_HR_CLASS, ";
            $query .= "     L2.ATTENDNO AS FI_ATTENDNO, ";
        }
        $query .= "      T2.GRADE,";
        $query .= "      T2.GRADE || ',' || T2.HR_CLASS AS GRCL";
        $query .= "  FROM";
        $query .= "      V_SCHREG_BASE_MST T1 ";
        $query .= "      LEFT JOIN SCHREG_REGD_DAT T2 ON T1.SCHREGNO = T2.SCHREGNO";
        $query .= "      LEFT JOIN SCHREG_ENT_GRD_HIST_DAT T3 ON T1.SCHREGNO = T3.SCHREGNO";
        $query .= "           AND T3.SCHOOL_KIND = '". $model->schoolKind. "' ";
        //特別支援学校対応
        if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
            //複式年組
            $query .= "     LEFT JOIN SCHREG_REGD_GHR_DAT L2 ON L2.SCHREGNO = T2.SCHREGNO AND L2.YEAR = T2.YEAR AND L2.SEMESTER = T2.SEMESTER ";
        //FI複式クラスを使うためのプロパティ
        } elseif ($model->Properties["useFi_Hrclass"] == '1') {
            $query .= "     LEFT JOIN SCHREG_REGD_FI_DAT L2 ON L2.SCHREGNO = T2.SCHREGNO AND L2.YEAR = T2.YEAR AND L2.SEMESTER = T2.SEMESTER ";
        }
        $query .= "      LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST T5 ON T5.SCHREGNO = T1.SCHREGNO AND T5.YEAR = '". CTRL_YEAR. "' AND T5.BASE_SEQ = '004' ";
        $query .= "      LEFT JOIN SCHREG_BASE_DETAIL_MST T6 ON T6.SCHREGNO = T1.SCHREGNO AND T6.BASE_SEQ = '010' ";
        if ($model->school_name == "koma") {
            $query .= "      LEFT JOIN ENTEXAM_APPLICANTBASE_DAT A1  ON A1.ENTEXAMYEAR  = FISCALYEAR(T1.ENT_DATE) ";
            $query .= "                                             AND A1.APPLICANTDIV = '{$setApplicantDiv}' ";
            $query .= "                                             AND A1.EXAMNO       = T1.EXAMNO ";
            $query .= "      LEFT JOIN ENTEXAM_CLASSIFY_MST C1  ON C1.ENTEXAMYEAR  = A1.ENTEXAMYEAR ";
            $query .= "                                        AND C1.APPLICANTDIV = A1.APPLICANTDIV ";
            $query .= "                                        AND C1.CLASSIFY_CD  = A1.TESTDIV1";
        } elseif ($model->school_name == "meikei") {
            $query .= "      LEFT JOIN SCHREG_BASE_DETAIL_MST D014 ON D014.SCHREGNO = T1.SCHREGNO AND D014.BASE_SEQ = '014' ";
        } else {
            $query .= "      LEFT JOIN SCHREG_BASE_DETAIL_MST D003 ON D003.SCHREGNO = T1.SCHREGNO AND D003.BASE_SEQ = '003' ";
            if (knja110aQuery::getEntSettingExistsCnt($db) > 0) {
                $query .= "      LEFT JOIN ENTEXAM_SETTING_MST N1 ON N1.ENTEXAMYEAR = T2.YEAR AND N1.SETTING_CD = 'L004' AND N1.SEQ = D003.BASE_REMARK2 ";
            } else {
                $query .= "      LEFT JOIN V_NAME_MST N1 ON N1.YEAR = T2.YEAR AND N1.NAMECD1 = 'L004' AND N1.NAMECD2 = D003.BASE_REMARK2 ";
            }
        }
        //出身
        if ($model->Properties["Origin_hyouji"] == '1') {
            $query .= "      LEFT JOIN SCHREG_BASE_DETAIL_MST D016 ON D016.SCHREGNO = T1.SCHREGNO AND D016.BASE_SEQ = '016' ";
        }
        $query .= "  WHERE";
        $query .= "      T1.SCHREGNO = '". $schregno. "' AND";
        $query .= "      T2.YEAR     = '". CTRL_YEAR. "' AND";
        $query .= "      T2.SEMESTER = '". CTRL_SEMESTER. "'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //更新するときに履歴に入れるデータを取ってくる
    public function getStudentDataBefore($model, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     T2.YEAR, ";
        $query .= "     T2.SEMESTER, ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T2.ANNUAL, ";
        $query .= "     T2.COURSECD, ";
        $query .= "     T2.MAJORCD, ";
        $query .= "     T2.COURSECODE, ";
        $query .= "     T2.SCHREGNO, ";
        $query .= "     T1.INOUTCD, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_SHOW, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.NAME_ENG, ";
        $query .= "     T1.REAL_NAME, ";
        $query .= "     T1.REAL_NAME_KANA, ";
        $query .= "     T1.BIRTHDAY, ";
        $query .= "     T1.SEX, ";
        $query .= "     T1.BLOODTYPE, ";
        $query .= "     T1.BLOOD_RH, ";
        $query .= "     T1.HANDICAP, ";
        $query .= "     T1.NATIONALITY, ";
        $query .= "     T1.FINSCHOOLCD, ";
        $query .= "     T1.FINISH_DATE, ";
        $query .= "     T1.ENT_DATE, ";
        $query .= "     T1.ENT_DIV, ";
        $query .= "     T1.ENT_REASON, ";
        $query .= "     T1.ENT_SCHOOL, ";
        $query .= "     T1.ENT_ADDR, ";
        if ($model->Properties["useAddrField2"] == "1" || $model->schoolKind == "H") {
            $query .= "     T1.ENT_ADDR2, ";
        }
        $query .= "     T1.GRD_DATE, ";
        $query .= "     T1.GRD_DIV, ";
        $query .= "     T1.GRD_REASON, ";
        $query .= "     T1.GRD_SCHOOL, ";
        $query .= "     T1.GRD_ADDR, ";
        if ($model->Properties["useAddrField2"] == "1" || $model->schoolKind == "H") {
            $query .= "     T1.GRD_ADDR2, ";
        }
        $query .= "     T1.GRD_NO, ";
        $query .= "     T1.GRD_TERM, ";
        $query .= "     T1.PRISCHOOLCD, ";
        $query .= "     T1.REMARK1, ";
        $query .= "     T1.REMARK2, ";
        $query .= "     T1.REMARK3 ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     SCHREG_REGD_DAT T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$schregno}' AND ";
        $query .= "     T2.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T2.SEMESTER = '".CTRL_SEMESTER."' ";

        return $query;
    }

    //履歴の最終終了日付取得
    public function getLastHistExpiredate($schregno)
    {
        $query  = " SELECT ";
        $query .= "     EXPIREDATE ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     ISSUEDATE DESC ";
        $query .= " FETCH FIRST ROW ONLY ";

        return $query;
    }

    //最終履歴がなかったら入学日付を取得する
    public function getEntDate($schregno)
    {
        $query  = " SELECT ";
        $query .= "     ENT_DATE ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' ";

        return $query;
    }


    //入学事由取得
    public function getEntReason($schregno)
    {
        return "SELECT ent_reason FROM schreg_base_mst WHERE schregno = '".$schregno."'";
    }

    //卒業事由取得
    public function getGrdReason($schregno)
    {
        return "SELECT grd_reason FROM schreg_base_mst WHERE schregno = '".$schregno."'";
    }

    //課程学科コンボ
    public function getCourseSubject()
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

    //出身中学校コンボ
    public function getFinschoolName($cd)
    {
        $query .= " SELECT ";
        $query .= "     FINSCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     FINSCHOOLCD = '{$cd}' ";

        return $query;
    }

    //塾名
    public function getPriSchoolName($priSchoolCd)
    {
        $query  = " SELECT ";
        $query .= "     PRISCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     PRISCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     PRISCHOOLCD = '{$priSchoolCd}' ";

        return $query;
    }
    //教室名
    public function getPriSchoolClassName($priSchoolCd, $priSchoolClassCd)
    {
        $query  = " SELECT ";
        $query .= "     PRISCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     PRISCHOOL_CLASS_MST ";
        $query .= " WHERE ";
        $query .= "     PRISCHOOLCD = '{$priSchoolCd}' ";
        $query .= "     AND PRISCHOOL_CLASS_CD = '{$priSchoolClassCd}' ";

        return $query;
    }

    //SCHREG_BASE_MSTにデータの有無チェック
    public function getExistBaseMst($schregno)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT COUNT(*) FROM schreg_base_mst ";
        $query .= " WHERE schregno ='". $schregno. "' ";

        $row = $db->getOne($query);
        Query::dbCheckIn($db);
        return (($row == 0)? false : true);
    }

    //年組名取得
    public function getHRName($model)
    {
        if ($model->field["GRCL"]) {
            $tmp = explode(",", $model->field["GRCL"]);
        } else {
            $tmp = explode(",", $model->GradeClass);
        }
        $query  = "SELECT hr_name FROM schreg_regd_hdat";
        $query .= " WHERE grade    = '".$tmp[0]."'";
        $query .= "   AND hr_class = '".$tmp[1]."'";
        $query .= "   AND year     = '".CTRL_YEAR."'";
        $query .= "   AND semester = '".CTRL_SEMESTER."'";
        return $query;
    }

    //複式出席番号重複チェック
    public function isExistGhrAttendno($model)
    {
        $db = Query::dbCheckOut();
        $arr = explode(",", $model->field["GRCL"]);

        $query  = " SELECT COUNT(*) FROM SCHREG_REGD_GHR_DAT ";
        $query .= " WHERE YEAR     = '" .CTRL_YEAR. "'";
        $query .= "   AND SEMESTER = '" .CTRL_SEMESTER. "'";
        $query .= "   AND GHR_CD        = '" .$model->field["GHR_CD"]. "'";
        $query .= "   AND GHR_ATTENDNO  = '" .sprintf("%03d", $model->field["GHR_ATTENDNO"]). "'";

        if ($model->cmd == "update" || $model->cmd == "pre_up" || $model->cmd == "next_up") {
            $query .= "   AND SCHREGNO <> '" . $model->schregno. "'";
        }

        $row = $db->getOne($query);
        Query::dbCheckIn($db);
        return (($row == 0)? false : true);
    }

    //複式SCHREG_REGD_GHR_DATにデータの有無チェック
    public function getExistRegdGhrDat($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT COUNT(*) FROM SCHREG_REGD_GHR_DAT ";
        $query .= " WHERE SCHREGNO = '". $model->field["SCHREGNO"]. "' ";
        $query .= "   AND SEMESTER = '". CTRL_SEMESTER. "' ";
        $query .= "   AND     YEAR = '". CTRL_YEAR. "' ";
        $row = $db->getOne($query);

        Query::dbCheckIn($db);
        return (($row == 0)? false : true);
    }

    //FI複式出席番号重複チェック
    public function isExistFiAttendno($model)
    {
        $db = Query::dbCheckOut();
        $arr = explode(",", $model->field["GRCL"]);

        $query  = " SELECT COUNT(*) FROM SCHREG_REGD_FI_DAT ";
        $query .= " WHERE YEAR      = '" .CTRL_YEAR. "'";
        $query .= "   AND SEMESTER  = '" .CTRL_SEMESTER. "'";
        $query .= "   AND GRADE || ',' || HR_CLASS     = '" .$model->field["FI_GRADE_HR_CLASS"]. "'";
        $query .= "   AND ATTENDNO  = '" .sprintf("%03d", $model->field["FI_ATTENDNO"]). "'";

        if ($model->cmd == "update" || $model->cmd == "pre_up" || $model->cmd == "next_up") {
            $query .= "   AND SCHREGNO <> '" . $model->schregno. "'";
        }

        $row = $db->getOne($query);
        Query::dbCheckIn($db);
        return (($row == 0)? false : true);
    }

    //FI複式SCHREG_REGD_FI_DATにデータの有無チェック
    public function getExistRegdFiDat($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT COUNT(*) FROM SCHREG_REGD_FI_DAT ";
        $query .= " WHERE SCHREGNO  = '". $model->field["SCHREGNO"]. "' ";
        $query .= "   AND SEMESTER  = '". CTRL_SEMESTER. "' ";
        $query .= "   AND     YEAR  = '". CTRL_YEAR. "' ";
        $row = $db->getOne($query);

        Query::dbCheckIn($db);
        return (($row == 0)? false : true);
    }

    //出席番号重複チェック
    public function isExistAttendno($model)
    {
        $db = Query::dbCheckOut();
        $arr = explode(",", $model->field["GRCL"]);

        $query  = " SELECT COUNT(*) FROM schreg_regd_dat ";
        $query .= " WHERE grade    = '" .$arr[0]. "'";
        $query .= "   AND hr_class = '" .$arr[1]. "'";
        $query .= "   AND year     = '" .CTRL_YEAR. "'";
        $query .= "   AND semester = '" .CTRL_SEMESTER. "'";
        $query .= "   AND attendno = '" .sprintf("%03d", $model->field["ATTENDNO"]). "'";

        if ($model->cmd == "update" || $model->cmd == "pre_up" || $model->cmd == "next_up") {
            $query .= "   AND schregno <> '" . $model->schregno. "'";
        }

        $row = $db->getOne($query);
        Query::dbCheckIn($db);
        return (($row == 0)? false : true);
    }

    //SCHREG_REGD_DATにデータの有無チェック
    public function getExistRegdDat($model)
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
    public function getCourseCode()
    {
        $query  = " SELECT coursecode, coursecodename ";
        $query .= "   FROM v_coursecode_mst ";
        $query .= " WHERE year     = '". CTRL_YEAR ."'";
        $query .= " ORDER BY coursecode";
        return $query;
    }

    //MAX学籍番号取得
    public function getMaxSchregno()
    {
        $query  = " WITH MAX_SCHNO AS ( ";
        $query .= "     SELECT ";
        $query .= "         INT(MAX(SCHREGNO))+1 AS SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_BASE_MST ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO LIKE '".CTRL_YEAR."%' ";
        $query .= " ), CHECK_SCHNO AS ( ";
        $query .= "     SELECT ";
        $query .= "         COUNT(T1.SCHREGNO) AS CNT ";
        $query .= "     FROM ";
        $query .= "         SCHREG_BASE_MST T1, ";
        $query .= "         MAX_SCHNO T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHREGNO = RTRIM(CHAR(T2.SCHREGNO)) ";
        $query .= " ), MIN_SCHNO AS ( ";
        $query .= "     SELECT ";
        $query .= "         INT(MIN(T1.SCHREGNO))+1 AS SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_BASE_MST T1, ";
        $query .= "         MAX_SCHNO T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHREGNO > RTRIM(CHAR(T2.SCHREGNO)) AND ";
        $query .= "         INT(T1.SCHREGNO) NOT IN (SELECT ";
        $query .= "                                     INT(S1.SCHREGNO)-1 ";
        $query .= "                                 FROM ";
        $query .= "                                     SCHREG_BASE_MST S1, ";
        $query .= "                                     MAX_SCHNO S2 ";
        $query .= "                                 WHERE ";
        $query .= "                                     S1.SCHREGNO > RTRIM(CHAR(S2.SCHREGNO)) ";
        $query .= "                                 ) ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     RTRIM(CHAR(CASE WHEN T2.CNT > 0 THEN T3.SCHREGNO ELSE T1.SCHREGNO END)) AS SCHREGNO ";
        $query .= " FROM ";
        $query .= "     MAX_SCHNO T1, ";
        $query .= "     CHECK_SCHNO T2, ";
        $query .= "     MIN_SCHNO T3 ";

        return $query;
    }

    //V_NAME_MSTから情報を取得
    public function getNameMstData($cd)
    {
        $query  = " SELECT * FROM v_name_mst ";
        $query .= " WHERE namecd1 ='" .$cd. "' AND year='".CTRL_YEAR."'";
        $query .= " ORDER BY namecd1, namecd2";
        return $query;
    }

    //第二国籍
    public function getNationality2($model)
    {
        $query  = " SELECT ";
        $query .= "     BASE_REMARK1, ";
        $query .= "     BASE_REMARK2 AS NAME, ";
        $query .= "     BASE_REMARK3 AS NAME_KANA, ";
        $query .= "     BASE_REMARK4 AS NAME_ENG, ";
        $query .= "     BASE_REMARK5 AS REAL_NAME, ";
        $query .= "     BASE_REMARK6 AS REAL_NAME_KANA ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND BASE_SEQ = '011' ";

        return $query;
    }

    //V_NAME_MSTから情報を取得
    public function getNameMst($namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || '  ' || NAME1 AS LABEL, ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '{$namecd1}' ";
        $query .= "     AND YEAR = '".CTRL_YEAR."'";
        if ($namecd2) {
            $query .= "     AND NAMECD2 = '".$namecd2."'";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //ENTEXAM_SETTING_MSTから情報を取得
    public function getEntexamSettingMst($model, $settingCd, $seq = "")
    {
        $setApplicantDiv = ($model->schoolKind == "J") ? "1": "2";//(1:中学 2:高校)

        $query  = " SELECT ";
        $query .= "     SEQ AS VALUE, ";
        $query .= "     SEQ || '  ' || NAME1 AS LABEL, ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR      = '".CTRL_YEAR."' ";
        $query .= "     AND APPLICANTDIV = '{$setApplicantDiv}' ";
        $query .= "     AND SETTING_CD   = '{$settingCd}' ";
        if ($seq) {
            $query .= "     AND SEQ          = '".$seq."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //学校種別取得
    public function getSubSchoolKind($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.NAME1 AS VALUE, ";
        $query .= "     VALUE(T1.NAME1, '') || ':' || VALUE(T1.ABBV1, '') AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.NAMECD1 = 'A023' ";
        $query .= "     AND NAME1 = '{$model->schoolKind}' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //ENTEXAM_SETTING_MSTテーブルが定義済みかチェック
    public function getEntSettingExistsCnt($db)
    {
        $query  = "";
        $query .= "  SELECT ";
        $query .= "      COUNT(*) AS CNT ";
        $query .= "  FROM ";
        $query .= "      SYSCAT.TABLES ";
        $query .= "  WHERE ";
        $query .= "      TABNAME = 'ENTEXAM_SETTING_MST' ";

        return $db->getOne($query);
    }

    //内容が変更されているかのチェック(一括更新)履歴にとるか取らないかの判断
    public function changeCheckIkkatu()
    {
    }

    //更新の際内容が変更されていれば、
    //履歴テーブルを更新するので、日付を取得する。
    public function getEdate($schregno)
    {
        $query  = " SELECT ";
        $query .= "     EXPIREDATE ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     ISSUEDATE DESC";
        $query .= " FETCH FIRST 1 ROWS ONLY ";

        return $query;
    }

    //履歴テーブルの更新
    public function insertHist($histArray, $edate, $start_day, $setFlg)
    {

        //$edate ⇒ 前回の履歴の終了日
        //$start_day ⇒ 今回入力された終了
        //$seq ⇒ このシーケンス番号でインサートする
        if ($edate) { //$edateには前回の終了日が入っている
            $edateArray = explode('-', $edate);
            $edateYear  = $edateArray[0];
            $edateMonth = $edateArray[1];
            $edateDay   = $edateArray[2];

            $time = mktime(0, 0, 0, $edateMonth, $edateDay + 1, $edateYear); //一日後を取得
            $sdate = date("Y-m-d", $time); //それを今回の(履歴の)開始日とする
        } else {
            $sdate = $histArray["ENT_DATE"] ? $histArray["ENT_DATE"] : CTRL_YEAR . '-04-01';
        }
        $start_dayArray = explode('/', $start_day);
        $start_dayYear  = $start_dayArray[0];
        $start_dayMonth = $start_dayArray[1];
        $start_dayDay   = $start_dayArray[2];
        $time = mktime(0, 0, 0, $start_dayMonth, $start_dayDay, $start_dayYear); //一日前を取得
        $konkaiEdate = date("Y-m-d", $time); //今日から見た前日(一日前)

        if ($konkaiEdate <= $sdate) {
            $start_dayArray = explode('-', $sdate);
            $start_dayYear  = $start_dayArray[0];
            $start_dayMonth = $start_dayArray[1];
            $start_dayDay   = $start_dayArray[2];
            $time = mktime(0, 0, 0, $start_dayMonth, $start_dayDay + 1, $start_dayYear); //一日前を取得
            $konkaiEdate = date("Y-m-d", $time); //今日から見た前日(一日前)
        }

        $data["SCHREGNO"][TEXT]            = $histArray["SCHREGNO"];
        $data["ISSUEDATE"][DATE]           = $sdate;
        $data["EXPIREDATE"][DATE]          = $konkaiEdate;
        $data["YEAR"][TEXT]                = $histArray["YEAR"];
        $data["SEMESTER"][TEXT]            = $histArray["SEMESTER"];
        $data["GRADE"][TEXT]               = $histArray["GRADE"];
        $data["HR_CLASS"][TEXT]            = $histArray["HR_CLASS"];
        $data["ATTENDNO"][TEXT]            = $histArray["ATTENDNO"];
        $data["ANNUAL"][TEXT]              = $histArray["ANNUAL"];
        $data["COURSECD"][TEXT]            = $histArray["COURSECD"];
        $data["MAJORCD"][TEXT]             = $histArray["MAJORCD"];
        $data["COURSECODE"][TEXT]          = $histArray["COURSECODE"];
        $data["NAME"][TEXT]                = $histArray["NAME"];
        $data["NAME_SHOW"][TEXT]           = $histArray["NAME_SHOW"];
        $data["NAME_KANA"][TEXT]           = $histArray["NAME_KANA"];
        $data["NAME_ENG"][TEXT]            = $histArray["NAME_ENG"];
        $data["REAL_NAME"][TEXT]           = $histArray["REAL_NAME"];
        $data["REAL_NAME_KANA"][TEXT]      = $histArray["REAL_NAME_KANA"];

        $data["GRADE_FLG"][TEXT]           = $setFlg["GRADE_FLG"];
        $data["HR_CLASS_FLG"][TEXT]        = $setFlg["HR_CLASS_FLG"];
        $data["ATTENDNO_FLG"][TEXT]        = $setFlg["ATTENDNO_FLG"];
        $data["ANNUAL_FLG"][TEXT]          = $setFlg["ANNUAL_FLG"];
        $data["COURSECD_FLG"][TEXT]        = $setFlg["COURSECD_FLG"];
        $data["MAJORCD_FLG"][TEXT]         = $setFlg["MAJORCD_FLG"];
        $data["COURSECODE_FLG"][TEXT]      = $setFlg["COURSECODE_FLG"];
        $data["NAME_FLG"][TEXT]            = $setFlg["NAME_FLG"];
        $data["NAME_SHOW_FLG"][TEXT]       = $setFlg["NAME_SHOW_FLG"];
        $data["NAME_KANA_FLG"][TEXT]       = $setFlg["NAME_KANA_FLG"];
        $data["NAME_ENG_FLG"][TEXT]        = $setFlg["NAME_ENG_FLG"];
        $data["REAL_NAME_FLG"][TEXT]       = $setFlg["REAL_NAME_FLG"];
        $data["REAL_NAME_KANA_FLG"][TEXT]  = $setFlg["REAL_NAME_KANA_FLG"];

        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][FUNC]             = "sysdate()";

        $query = Query::insertSQL($data, "SCHREG_BASE_HIST_DAT");

        return $query;
    }

    //履歴テーブルの更新
    public function insertHistAndYoyaku($db, $histArray, $model, $edate, $start_day, $setFlg)
    {

        //$edate ⇒ 前回の履歴の終了日
        //$start_day ⇒ 今回入力された終了
        //$seq ⇒ このシーケンス番号でインサートする
        if ($edate) { //$edateには前回の終了日が入っている
            $edateArray = explode('-', $edate);
            $edateYear  = $edateArray[0];
            $edateMonth = $edateArray[1];
            $edateDay   = $edateArray[2];

            $time = mktime(0, 0, 0, $edateMonth, $edateDay + 1, $edateYear); //一日後を取得
            $sdate = date("Y-m-d", $time); //それを今回の(履歴の)開始日とする
        } else {
            $sdate = $histArray["ENT_DATE"] ? $histArray["ENT_DATE"] : CTRL_YEAR . '-04-01';
        }
        $start_dayArray = explode('/', $start_day);
        $start_dayYear  = $start_dayArray[0];
        $start_dayMonth = $start_dayArray[1];
        $start_dayDay   = $start_dayArray[2];
        $time = mktime(0, 0, 0, $start_dayMonth, $start_dayDay, $start_dayYear); //一日前を取得
        $konkaiEdate = date("Y-m-d", $time); //今日から見た前日(一日前)

        if ($konkaiEdate <= $sdate) {
            $start_dayArray = explode('-', $sdate);
            $start_dayYear  = $start_dayArray[0];
            $start_dayMonth = $start_dayArray[1];
            $start_dayDay   = $start_dayArray[2];
            $time = mktime(0, 0, 0, $start_dayMonth, $start_dayDay + 1, $start_dayYear); //一日前を取得
            $konkaiEdate = date("Y-m-d", $time); //今日から見た前日(一日前)
        }

        $data["SCHREGNO"][TEXT]                        = $histArray["SCHREGNO"];
        $data["ISSUEDATE"][DATE]                       = $sdate;
        $data["EXPIREDATE"][DATE]                      = $konkaiEdate;
        $data["YEAR"][TEXT]                            = $histArray["YEAR"];
        $data["SEMESTER"][TEXT]                        = $histArray["SEMESTER"];
        $data["GRADE"][TEXT]                           = $histArray["GRADE"];
        $data["HR_CLASS"][TEXT]                        = $histArray["HR_CLASS"];
        $data["ATTENDNO"][TEXT]                        = $histArray["ATTENDNO"];
        $data["ANNUAL"][TEXT]                          = $histArray["ANNUAL"];
        $data["COURSECD"][TEXT]                        = $histArray["COURSECD"];
        $data["MAJORCD"][TEXT]                         = $histArray["MAJORCD"];
        $data["COURSECODE"][TEXT]                      = $histArray["COURSECODE"];
        $data["NAME"][TEXT]                            = $histArray["NAME"];
        $data["NAME_SHOW"][TEXT]                       = $histArray["NAME_SHOW"];
        $data["NAME_KANA"][TEXT]                       = $histArray["NAME_KANA"];
        $data["NAME_ENG"][TEXT]                        = $histArray["NAME_ENG"];
        $data["REAL_NAME"][TEXT]                       = $histArray["REAL_NAME"];
        $data["REAL_NAME_KANA"][TEXT]                  = $histArray["REAL_NAME_KANA"];
        $data["HANDICAP"][TEXT]                        = $histArray["HANDICAP"];
        $data["NATIONALITY2"][TEXT]                    = $model->detail011Back["NATIONALITY2"];
        $data["NATIONALITY_NAME"][TEXT]                = $model->detail011Back["NATIONALITY_NAME"];
        $data["NATIONALITY_NAME_KANA"][TEXT]           = $model->detail011Back["NATIONALITY_NAME_KANA"];
        $data["NATIONALITY_NAME_ENG"][TEXT]            = $model->detail011Back["NATIONALITY_NAME_ENG"];
        $data["NATIONALITY_REAL_NAME"][TEXT]           = $model->detail011Back["NATIONALITY_REAL_NAME"];
        $data["NATIONALITY_REAL_NAME_KANA"][TEXT]      = $model->detail011Back["NATIONALITY_REAL_NAME_KANA"];

        $data["GRADE_FLG"][TEXT]                       = $setFlg["GRADE_FLG"];
        $data["HR_CLASS_FLG"][TEXT]                    = $setFlg["HR_CLASS_FLG"];
        $data["ATTENDNO_FLG"][TEXT]                    = $setFlg["ATTENDNO_FLG"];
        $data["ANNUAL_FLG"][TEXT]                      = $setFlg["ANNUAL_FLG"];
        $data["COURSECD_FLG"][TEXT]                    = $setFlg["COURSECD_FLG"];
        $data["MAJORCD_FLG"][TEXT]                     = $setFlg["MAJORCD_FLG"];
        $data["COURSECODE_FLG"][TEXT]                  = $setFlg["COURSECODE_FLG"];
        $data["NAME_FLG"][TEXT]                        = $setFlg["NAME_FLG"];
        $data["NAME_SHOW_FLG"][TEXT]                   = $setFlg["NAME_SHOW_FLG"];
        $data["NAME_KANA_FLG"][TEXT]                   = $setFlg["NAME_KANA_FLG"];
        $data["NAME_ENG_FLG"][TEXT]                    = $setFlg["NAME_ENG_FLG"];
        $data["REAL_NAME_FLG"][TEXT]                   = $setFlg["REAL_NAME_FLG"];
        $data["REAL_NAME_KANA_FLG"][TEXT]              = $setFlg["REAL_NAME_KANA_FLG"];
        $data["HANDICAP_FLG"][TEXT]                    = $setFlg["HANDICAP_FLG"];
        $data["NATIONALITY2_FLG"][TEXT]                = $setFlg["NATIONALITY2_FLG"];
        $data["NATIONALITY_NAME_FLG"][TEXT]            = $setFlg["NATIONALITY_NAME_FLG"];
        $data["NATIONALITY_NAME_KANA_FLG"][TEXT]       = $setFlg["NATIONALITY_NAME_KANA_FLG"];
        $data["NATIONALITY_NAME_ENG_FLG"][TEXT]        = $setFlg["NATIONALITY_NAME_ENG_FLG"];
        $data["NATIONALITY_REAL_NAME_FLG"][TEXT]       = $setFlg["NATIONALITY_REAL_NAME_FLG"];
        $data["NATIONALITY_REAL_NAME_KANA_FLG"][TEXT]  = $setFlg["NATIONALITY_REAL_NAME_KANA_FLG"];

        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][FUNC]             = "sysdate()";

        $histQuery = Query::insertSQL($data, "SCHREG_BASE_HIST_DAT");
        $query = "VALUES(DATE(sysdate()))";
        $sysDate = $db->getOne($query);

        if ($sysDate < $konkaiEdate) {
            $saisyuArray = explode('-', $konkaiEdate);
            $saisyuYear  = $saisyuArray[0];
            $saisyuMonth = $saisyuArray[1];
            $saisyuDay   = $saisyuArray[2];

            $time = mktime(0, 0, 0, $saisyuMonth, $saisyuDay + 1, $saisyuYear); //一日後を取得
            $sdate = date("Y-m-d", $time); //それを今回の(履歴の)開始日とする
            $edate = "9999-12-31";

            $data["NAME"][TEXT]             = $model->field["NAME"];
            $data["NAME_SHOW"][TEXT]        = $model->field["NAME_SHOW"];
            $data["NAME_KANA"][TEXT]        = $model->field["NAME_KANA"];
            $data["NAME_ENG"][TEXT]         = $model->field["NAME_ENG"];
            $data["REAL_NAME"][TEXT]        = $model->field["REAL_NAME"];
            $data["REAL_NAME_KANA"][TEXT]   = $model->field["REAL_NAME_KANA"];
            $data["HANDICAP"][TEXT]         = $model->field["HANDICAP"];

            $ary = explode(",", $model->field["COURSEMAJORCD"]);
            $arr = explode(",", $model->field["GRCL"]);
            $data["GRADE"][TEXT]        = $arr[0];
            $data["HR_CLASS"][TEXT]     = $arr[1];
            $data["ATTENDNO"][TEXT]     = sprintf("%03d", $model->field["ATTENDNO"]);
            $data["ANNUAL"][TEXT]       = sprintf("%02d", $model->field["ANNUAL"]);
            $data["COURSECD"][TEXT]     = $ary[0];
            $data["MAJORCD"][TEXT]      = $ary[1];
            $data["COURSECODE"][TEXT]   = $model->field["COURSECODE"];

            $data["ISSUEDATE"][DATE]  = $sdate;
            $data["EXPIREDATE"][DATE] = $edate;

            $yoyakuQuery = Query::insertSQL($data, "SCHREG_BASE_HIST_DAT");
        }

        return array($histQuery, $yoyakuQuery);
    }

    //インサート
    public function &getInsertQuery($model, $table)
    {
        if ($table == "") {
            return false;
        }
        if ($model->field["COURSEMAJORCD"] == "") {
            return false;
        }

        $db = Query::dbCheckOut();
        $schregno = substr("00000000".$model->field["SCHREGNO"], strlen($model->field["SCHREGNO"]), 8);
        $entGrdData = $db->getRow(knja110aQuery::getEntGrdData($model, $schregno, "MAX"), DB_FETCHMODE_ASSOC);
        $HandicapMaxData = $db->getRow(knja110aQuery::getHandicapYearData($schregno, "MAX"), DB_FETCHMODE_ASSOC);

        if ($table == "schreg_base_mst") {
            $data = array();
            $data["SCHREGNO"][TEXT]    = $schregno;
            $data["INOUTCD"][TEXT]     = $model->field["INOUTCD"];
            $data["NAME"][TEXT]        = $model->field["NAME"];
            $data["NAME_SHOW"][TEXT]   = $model->field["NAME_SHOW"];
            $data["NAME_KANA"][TEXT]   = $model->field["NAME_KANA"];
            $data["NAME_ENG"][TEXT]    = $model->field["NAME_ENG"];
            $data["REAL_NAME"][TEXT]   = $model->field["REAL_NAME"];
            $data["REAL_NAME_KANA"][TEXT] = $model->field["REAL_NAME_KANA"];
            $data["BIRTHDAY"][TEXT]    = str_replace("/", "-", $model->field["BIRTHDAY"]);
            $data["SEX"][TEXT]         = $model->field["SEX"];
            $data["BLOODTYPE"][TEXT]   = $model->field["BLOODTYPE"];
            $data["BLOOD_RH"][TEXT]    = $model->field["BLOOD_RH"];
            if (is_array($HandicapMaxData) && CTRL_YEAR < intval($HandicapMaxData["YEAR"])) {
                $data["HANDICAP"][TEXT]     = $HandicapMaxData["BASE_REMARK1"];
            } else {
                $data["HANDICAP"][TEXT]     = $model->field["HANDICAP"];
            }
            $data["NATIONALITY"][TEXT] = $model->field["NATIONALITY"];
            $data["FINSCHOOLCD"][TEXT] = ($entGrdData["FINSCHOOLCD"] ? sprintf("%0".($model->finschoolcdKeta)."s", $entGrdData["FINSCHOOLCD"]) : "");
            $data["FINISH_DATE"][TEXT] = str_replace("/", "-", $entGrdData["FINISH_DATE"]);
            $data["PRISCHOOLCD"][TEXT] = $model->field["PRISCHOOLCD"];
            $data["ENT_DATE"][TEXT]    = str_replace("/", "-", $entGrdData["ENT_DATE"]);
            $data["ENT_DIV"][TEXT]     = $entGrdData["ENT_DIV"];
            $data["ENT_REASON"][TEXT]  = $entGrdData["ENT_REASON"];
            $data["ENT_SCHOOL"][TEXT]  = $entGrdData["ENT_SCHOOL"];
            $data["ENT_ADDR"][TEXT]    = $entGrdData["ENT_ADDR"];
            if ($model->Properties["useAddrField2"] == "1" || $model->schoolKind == "H") {
                $data["ENT_ADDR2"][TEXT]   = $entGrdData["ENT_ADDR2"];
            }
            $data["GRD_DATE"][TEXT]    = str_replace("/", "-", $entGrdData["GRD_DATE"]);
            $data["GRD_DIV"][TEXT]     = $entGrdData["GRD_DIV"];
            $data["GRD_REASON"][TEXT]  = $entGrdData["GRD_REASON"];
            $data["GRD_SCHOOL"][TEXT]  = $entGrdData["GRD_SCHOOL"];
            $data["GRD_ADDR"][TEXT]    = $entGrdData["GRD_ADDR"];
            if ($model->Properties["useAddrField2"] == "1" || $model->schoolKind == "H") {
                $data["GRD_ADDR2"][TEXT]   = $entGrdData["GRD_ADDR2"];
            }
            $data["REMARK1"][TEXT]     = $model->field["REMARK1"];
            $data["REMARK2"][TEXT]     = $model->field["REMARK2"];
            $data["REMARK3"][TEXT]     = $model->field["REMARK3"];
            $data["REGISTERCD"][TEXT]  = STAFFCD;
            $data["UPDATED"][FUNC]     = "sysdate()";
            $query = Query::insertSQL($data, $table);
            $db->query($query);

            if ($entGrdData["GRD_DIV"] == "1" ||
                $entGrdData["GRD_DIV"] == "2" ||
                $entGrdData["GRD_DIV"] == "3"
            ) {
                $query = "DELETE FROM CLASS_FORMATION_DAT WHERE SCHREGNO = '{$schregno}' AND YEAR = '".(CTRL_YEAR + 1)."'";
                $db->query($query);
            }

            $data = array();
            $data["SCHREGNO"][TEXT]     = $schregno;
            $data["BASE_SEQ"][TEXT]     = "001";
            $data["BASE_REMARK1"][TEXT] = str_replace("/", "-", $model->field["TENGAKU_SAKI_ZENJITU"]);
            $data["BASE_REMARK2"][TEXT] = $model->field["TENGAKU_SAKI_GRADE"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $query = Query::insertSQL($data, "SCHREG_BASE_DETAIL_MST");
            $db->query($query);

            $data = array();
            $data["SCHREGNO"][TEXT]     = $schregno;
            $data["BASE_SEQ"][TEXT]     = "002";
            $data["BASE_REMARK1"][TEXT] = $model->field["NYUGAKUMAE_SYUSSIN_JOUHOU"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $query = Query::insertSQL($data, "SCHREG_BASE_DETAIL_MST");
            $db->query($query);

            $data = array();
            $data["SCHREGNO"][TEXT]     = $schregno;
            $data["BASE_SEQ"][TEXT]     = "003";
            $data["BASE_REMARK1"][TEXT] = $model->field["EXAMNO"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $query = Query::insertSQL($data, "SCHREG_BASE_DETAIL_MST");
            $db->query($query);

            $data = array();
            $data["SCHREGNO"][TEXT]     = $schregno;
            $data["BASE_SEQ"][TEXT]     = "010";
            $data["BASE_REMARK1"][TEXT] = $model->field["PRISCHOOL_CLASS_CD"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $query = Query::insertSQL($data, "SCHREG_BASE_DETAIL_MST");
            $db->query($query);

            if ($model->school_name == "meikei") {
                $data = array();
                $data["SCHREGNO"][TEXT]     = $schregno;
                $data["BASE_SEQ"][TEXT]     = "014";
                $data["BASE_REMARK1"][TEXT] = $model->field["TESTDIV"];
                $data["BASE_REMARK2"][TEXT] = $model->field["EXAM_TYPE"];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";
                $query = Query::insertSQL($data, "SCHREG_BASE_DETAIL_MST");
                $db->query($query);
            }

            //出身
            if ($model->Properties["Origin_hyouji"] == '1') {
                $data = array();
                $data["SCHREGNO"][TEXT]     = $schregno;
                $data["BASE_SEQ"][TEXT]     = "016";
                $data["BASE_REMARK1"][TEXT] = $model->field["ORIGIN"];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";
                $query = Query::insertSQL($data, "SCHREG_BASE_DETAIL_MST");
                $db->query($query);
            }
        }

        if ($table == "schreg_regd_dat") {
            $ary = explode(",", $model->field["COURSEMAJORCD"]);
            $arr = explode(",", $model->field["GRCL"]);
            $data = array();
            $data["SCHREGNO"][TEXT]     = $schregno;
            $data["YEAR"][TEXT]         = CTRL_YEAR;
            $data["SEMESTER"][TEXT]     = CTRL_SEMESTER;
            $data["GRADE"][TEXT]        = $arr[0];
            $data["HR_CLASS"][TEXT]     = $arr[1];
            $data["ATTENDNO"][TEXT]     = sprintf("%03d", $model->field["ATTENDNO"]);
            $data["ANNUAL"][TEXT]       = sprintf("%02d", $model->field["ANNUAL"]);
            $data["SEAT_COL"][FUNC]     = "conv_col_row('".sprintf("%03d", $model->field["ATTENDNO"])."',1)";
            $data["SEAT_ROW"][FUNC]     = "conv_col_row('".sprintf("%03d", $model->field["ATTENDNO"])."',2)";
            $data["COURSECD"][TEXT]     = $ary[0];
            $data["MAJORCD"][TEXT]      = $ary[1];
            $data["COURSECODE"][TEXT]   = $model->field["COURSECODE"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $query = Query::insertSQL($data, $table);
            $db->query($query);
        }

        $HandicapLoginData = $db->getRow(knja110aQuery::getHandicapYearData($schregno), DB_FETCHMODE_ASSOC);
        $data = array();
        $data["BASE_REMARK1"][TEXT] = $model->field["HANDICAP"];
        if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
            $data["BASE_REMARK2"][TEXT] = $model->field["VISITOR"];
        }
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        if (!is_array($HandicapLoginData)) {
            $data["SCHREGNO"][TEXT]     = $schregno;
            $data["YEAR"][TEXT]         = CTRL_YEAR;
            $data["BASE_SEQ"][TEXT]     = "004";
            $query = Query::insertSQL($data, SCHREG_BASE_YEAR_DETAIL_MST);
            $db->query($query);
        } else {
            $where  = " WHERE SCHREGNO  ='".$schregno."' ";
            $where .= "   AND YEAR      ='".CTRL_YEAR."' ";
            $where .= "   AND BASE_SEQ  ='004' ";
            $query = Query::updateSQL($data, SCHREG_BASE_YEAR_DETAIL_MST, $where);
            $db->query($query);
        }

        //特別支援学校対応
        if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
            if ($table == "schreg_regd_ghr_dat") {
                $data = array();
                $data["SCHREGNO"][TEXT]     = $schregno;
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                $data["SEMESTER"][TEXT]     = CTRL_SEMESTER;
                $data["GHR_CD"][TEXT]       = $model->field["GHR_CD"];
                $data["GHR_ATTENDNO"][TEXT] = sprintf("%03d", $model->field["GHR_ATTENDNO"]);
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $where  = " WHERE SCHREGNO  ='".$schregno."' ";
                $where .= "   AND YEAR      ='".CTRL_YEAR."' ";
                $where .= "   AND SEMESTER  ='".CTRL_SEMESTER."' ";

                //追加ボタンを押した時、
                //レコードあり、画面のGHR_CDとGHR_ATTENDNOあり => レコード更新・・・通常ありえない
                //レコードあり、画面のGHR_CDとGHR_ATTENDNOなし => レコード削除・・・通常ありえない
                //レコードなし、画面のGHR_CDとGHR_ATTENDNOあり => レコード追加・・・通常
                //レコードなし、画面のGHR_CDとGHR_ATTENDNOなし => 何もしない
                $query = "SELECT COUNT(*) FROM " . $table . $where;
                $cnt = $db->getOne($query);
                if (0 < $cnt) {
                    if (strlen($model->field["GHR_CD"]) && strlen($model->field["GHR_ATTENDNO"])) {
                        $query = Query::updateSQL($data, $table, $where);
                        $db->query($query);
                    } else {
                        $query = "DELETE FROM " . $table . $where;
                        $db->query($query);
                    }
                } else {
                    if (strlen($model->field["GHR_CD"]) && strlen($model->field["GHR_ATTENDNO"])) {
                        $query = Query::insertSQL($data, $table);
                        $db->query($query);
                    }
                }
            }
            //FI複式クラスを使うためのプロパティ
        } elseif ($model->Properties["useFi_Hrclass"] == '1') {
            if ($table == "schreg_regd_fi_dat") {
                $ary = explode(",", $model->field["COURSEMAJORCD"]);
                $arr = explode(",", $model->field["FI_GRADE_HR_CLASS"]);
                $data = array();
                $data["SCHREGNO"][TEXT]     = $schregno;
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                $data["SEMESTER"][TEXT]     = CTRL_SEMESTER;
                $data["GRADE"][TEXT]        = $arr[0];
                $data["HR_CLASS"][TEXT]     = $arr[1];
                $data["ATTENDNO"][TEXT]     = sprintf("%03d", $model->field["FI_ATTENDNO"]);
                $data["ANNUAL"][TEXT]       = sprintf("%02d", $model->field["ANNUAL"]);
                $data["SEAT_COL"][FUNC]     = "conv_col_row('".sprintf("%03d", $model->field["FI_ATTENDNO"])."',1)";
                $data["SEAT_ROW"][FUNC]     = "conv_col_row('".sprintf("%03d", $model->field["FI_ATTENDNO"])."',2)";
                $data["COURSECD"][TEXT]     = $ary[0];
                $data["MAJORCD"][TEXT]      = $ary[1];
                $data["COURSECODE"][TEXT]   = $model->field["COURSECODE"];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $where  = " WHERE SCHREGNO  ='".$schregno."' ";
                $where .= "   AND YEAR      ='".CTRL_YEAR."' ";
                $where .= "   AND SEMESTER  ='".CTRL_SEMESTER."' ";

                //追加ボタンを押した時、
                //レコードあり、画面のGHR_CDとFI_ATTENDNOあり => レコード更新・・・通常ありえない
                //レコードあり、画面のGHR_CDとFI_ATTENDNOなし => レコード削除・・・通常ありえない
                //レコードなし、画面のGHR_CDとFI_ATTENDNOあり => レコード追加・・・通常
                //レコードなし、画面のGHR_CDとFI_ATTENDNOなし => 何もしない
                $query = "SELECT COUNT(*) FROM " . $table . $where;
                $cnt = $db->getOne($query);
                if (0 < $cnt) {
                    if (strlen($model->field["FI_GRADE_HR_CLASS"]) && strlen($model->field["FI_ATTENDNO"])) {
                        $query = Query::updateSQL($data, $table, $where);
                        $db->query($query);
                    } else {
                        $query = "DELETE FROM " . $table . $where;
                        $db->query($query);
                    }
                } else {
                    if (strlen($model->field["FI_GRADE_HR_CLASS"]) && strlen($model->field["FI_ATTENDNO"])) {
                        $query = Query::insertSQL($data, $table);
                        $db->query($query);
                    }
                }
            }
        }

        Query::dbCheckIn($db);
        return true;
    }

    //アップデート
    public function &getUpdateQuery($model, $table)
    {
        $db = Query::dbCheckOut();
        $schregno = $model->field["SCHREGNO"];
        $entGrdData = $db->getRow(knja110aQuery::getEntGrdData($model, $schregno, "MAX"), DB_FETCHMODE_ASSOC);
        $HandicapMaxData = $db->getRow(knja110aQuery::getHandicapYearData($schregno, "MAX"), DB_FETCHMODE_ASSOC);

        if ($table == "schreg_base_mst") {
            $data = array();
            $data["INOUTCD"][TEXT]      = $model->field["INOUTCD"];
            $data["NAME"][TEXT]         = $model->field["NAME"];
            $data["NAME_SHOW"][TEXT]    = $model->field["NAME_SHOW"];
            $data["NAME_KANA"][TEXT]    = $model->field["NAME_KANA"];
            $data["NAME_ENG"][TEXT]     = $model->field["NAME_ENG"];
            $data["REAL_NAME"][TEXT]    = $model->field["REAL_NAME"];
            $data["REAL_NAME_KANA"][TEXT] = $model->field["REAL_NAME_KANA"];
            $data["BIRTHDAY"][TEXT]     = str_replace("/", "-", $model->field["BIRTHDAY"]);
            $data["SEX"][TEXT]          = $model->field["SEX"];
            $data["BLOODTYPE"][TEXT]    = $model->field["BLOODTYPE"];
            $data["BLOOD_RH"][TEXT]     = $model->field["BLOOD_RH"];
            if (is_array($HandicapMaxData) && CTRL_YEAR < intval($HandicapMaxData["YEAR"])) {
                $data["HANDICAP"][TEXT]     = $HandicapMaxData["BASE_REMARK1"];
            } else {
                $data["HANDICAP"][TEXT]     = $model->field["HANDICAP"];
            }
            $data["NATIONALITY"][TEXT]  = $model->field["NATIONALITY"];
            $data["FINSCHOOLCD"][TEXT]  = ($entGrdData["FINSCHOOLCD"] ? sprintf("%0".($model->finschoolcdKeta)."s", $entGrdData["FINSCHOOLCD"]) : "");
            $data["FINISH_DATE"][TEXT]  = str_replace("/", "-", $entGrdData["FINISH_DATE"]);
            $data["PRISCHOOLCD"][TEXT]  = $model->field["PRISCHOOLCD"];
            $data["ENT_DATE"][TEXT]     = str_replace("/", "-", $entGrdData["ENT_DATE"]);
            $data["ENT_DIV"][TEXT]      = $entGrdData["ENT_DIV"];
            $data["ENT_REASON"][TEXT]   = $entGrdData["ENT_REASON"];
            $data["ENT_SCHOOL"][TEXT]   = $entGrdData["ENT_SCHOOL"];
            $data["ENT_ADDR"][TEXT]     = $entGrdData["ENT_ADDR"];
            if ($model->Properties["useAddrField2"] == "1" || $model->schoolKind == "H") {
                $data["ENT_ADDR2"][TEXT]    = $entGrdData["ENT_ADDR2"];
            }
            $data["GRD_DATE"][TEXT]     = str_replace("/", "-", $entGrdData["GRD_DATE"]);
            $data["GRD_DIV"][TEXT]      = $entGrdData["GRD_DIV"];
            $data["GRD_REASON"][TEXT]   = $entGrdData["GRD_REASON"];
            $data["GRD_SCHOOL"][TEXT]   = $entGrdData["GRD_SCHOOL"];
            $data["GRD_ADDR"][TEXT]     = $entGrdData["GRD_ADDR"];
            if ($model->Properties["useAddrField2"] == "1" || $model->schoolKind == "H") {
                $data["GRD_ADDR2"][TEXT]    = $entGrdData["GRD_ADDR2"];
            }
            $data["REMARK1"][TEXT]      = $model->field["REMARK1"];
            $data["REMARK2"][TEXT]      = $model->field["REMARK2"];
            $data["REMARK3"][TEXT]      = $model->field["REMARK3"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $where  = "WHERE SCHREGNO ='".$schregno."'";

            $query = Query::updateSQL($data, $table, $where);
            $db->query($query);

            $HandicapLoginData = $db->getRow(knja110aQuery::getHandicapYearData($schregno), DB_FETCHMODE_ASSOC);
            $data = array();
            $data["BASE_REMARK1"][TEXT] = $model->field["HANDICAP"];
            if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
                $data["BASE_REMARK2"][TEXT] = $model->field["VISITOR"];
            }
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            if (!is_array($HandicapLoginData)) {
                $data["SCHREGNO"][TEXT]     = $schregno;
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                $data["BASE_SEQ"][TEXT]     = "004";
                $query = Query::insertSQL($data, SCHREG_BASE_YEAR_DETAIL_MST);
                $db->query($query);
            } else {
                $where  = " WHERE SCHREGNO  ='".$schregno."' ";
                $where .= "   AND YEAR      ='".CTRL_YEAR."' ";
                $where .= "   AND BASE_SEQ  ='004' ";
                $query = Query::updateSQL($data, SCHREG_BASE_YEAR_DETAIL_MST, $where);
                $db->query($query);
            }

            if ($entGrdData["GRD_DIV"] == "1" ||
                $entGrdData["GRD_DIV"] == "2" ||
                $entGrdData["GRD_DIV"] == "3"
            ) {
                $query = "DELETE FROM CLASS_FORMATION_DAT WHERE SCHREGNO = '{$schregno}' AND YEAR = '".(CTRL_YEAR + 1)."'";
                $db->query($query);
            }

            $query = "DELETE FROM SCHREG_BASE_DETAIL_MST WHERE SCHREGNO ='".$schregno."' AND BASE_SEQ = '001' ";
            $db->query($query);

            $data = array();
            $data["SCHREGNO"][TEXT]     = $schregno;
            $data["BASE_SEQ"][TEXT]     = "001";
            $data["BASE_REMARK1"][TEXT] = str_replace("/", "-", $model->field["TENGAKU_SAKI_ZENJITU"]);
            $data["BASE_REMARK2"][TEXT] = $model->field["TENGAKU_SAKI_GRADE"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $query = Query::insertSQL($data, "SCHREG_BASE_DETAIL_MST");
            $db->query($query);

            $query = "DELETE FROM SCHREG_BASE_DETAIL_MST WHERE SCHREGNO ='".$schregno."' AND BASE_SEQ = '002' ";
            $db->query($query);

            $data = array();
            $data["SCHREGNO"][TEXT]     = $schregno;
            $data["BASE_SEQ"][TEXT]     = "002";
            $data["BASE_REMARK1"][TEXT] = $model->field["NYUGAKUMAE_SYUSSIN_JOUHOU"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $query = Query::insertSQL($data, "SCHREG_BASE_DETAIL_MST");
            $db->query($query);

            $query = "DELETE FROM SCHREG_BASE_DETAIL_MST WHERE SCHREGNO ='".$schregno."' AND BASE_SEQ = '003' ";
            $db->query($query);

            $data = array();
            $data["SCHREGNO"][TEXT]     = $schregno;
            $data["BASE_SEQ"][TEXT]     = "003";
            $data["BASE_REMARK1"][TEXT] = $model->field["EXAMNO"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $query = Query::insertSQL($data, "SCHREG_BASE_DETAIL_MST");
            $db->query($query);

            $query = "DELETE FROM SCHREG_BASE_DETAIL_MST WHERE SCHREGNO ='".$schregno."' AND BASE_SEQ = '010' ";
            $db->query($query);

            $data = array();
            $data["SCHREGNO"][TEXT]     = $schregno;
            $data["BASE_SEQ"][TEXT]     = "010";
            $data["BASE_REMARK1"][TEXT] = $model->field["PRISCHOOL_CLASS_CD"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $query = Query::insertSQL($data, "SCHREG_BASE_DETAIL_MST");
            $db->query($query);

            if ($model->school_name == "meikei") {
                $query = " DELETE FROM SCHREG_BASE_DETAIL_MST WHERE SCHREGNO ='".$schregno."' AND BASE_SEQ = '014' ";
                $db->query($query);

                $data = array();
                $data["SCHREGNO"][TEXT]   = $schregno;
                $data["BASE_SEQ"][TEXT]   = '014';
                $data["BASE_REMARK1"][TEXT]    = $model->field["TESTDIV"];
                $data["BASE_REMARK2"][TEXT]    = $model->field["EXAM_TYPE"];
                $data["REGISTERCD"][TEXT] = STAFFCD;
                $data["UPDATED"][FUNC]    = "sysdate()";

                $query = Query::insertSQL($data, "SCHREG_BASE_DETAIL_MST");
                $db->query($query);
            }
            //出身
            if ($model->Properties["Origin_hyouji"] == '1') {
                $query = "DELETE FROM SCHREG_BASE_DETAIL_MST WHERE SCHREGNO ='".$schregno."' AND BASE_SEQ = '016' ";
                $db->query($query);

                $data = array();
                $data["SCHREGNO"][TEXT]     = $schregno;
                $data["BASE_SEQ"][TEXT]     = "016";
                $data["BASE_REMARK1"][TEXT] = $model->field["ORIGIN"];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";
                $query = Query::insertSQL($data, "SCHREG_BASE_DETAIL_MST");
                $db->query($query);
            }
        }

        if ($table == "schreg_regd_dat") {
            $ary = explode(",", $model->field["COURSEMAJORCD"]);
            $arr = explode(",", $model->field["GRCL"]);
            $data = array();
            $data["GRADE"][TEXT]        = $arr[0];
            $data["HR_CLASS"][TEXT]     = $arr[1];
            $data["ATTENDNO"][TEXT]     = sprintf("%03d", $model->field["ATTENDNO"]);
            $data["ANNUAL"][TEXT]       = sprintf("%02d", $model->field["ANNUAL"]);
            $data["SEAT_COL"][FUNC]     = "conv_col_row('".sprintf("%03d", $model->field["ATTENDNO"])."',1)";
            $data["SEAT_ROW"][FUNC]     = "conv_col_row('".sprintf("%03d", $model->field["ATTENDNO"])."',2)";
            $data["COURSECD"][TEXT]     = $ary[0];
            $data["MAJORCD"][TEXT]      = $ary[1];
            $data["COURSECODE"][TEXT]   = $model->field["COURSECODE"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $where   = "WHERE SCHREGNO  ='".$schregno."' ";
            $where  .= "  AND YEAR      ='".CTRL_YEAR."' ";
            $where  .= "  AND SEMESTER  ='".CTRL_SEMESTER."' ";

            $query = Query::updateSQL($data, $table, $where);
            $db->query($query);
        }

        //特別支援学校対応
        if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
            if ($table == "schreg_regd_ghr_dat") {
                $data = array();
                $data["SCHREGNO"][TEXT]     = $schregno;
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                $data["SEMESTER"][TEXT]     = CTRL_SEMESTER;
                $data["GHR_CD"][TEXT]       = $model->field["GHR_CD"];
                $data["GHR_ATTENDNO"][TEXT] = sprintf("%03d", $model->field["GHR_ATTENDNO"]);
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $where  = " WHERE SCHREGNO  ='".$schregno."' ";
                $where .= "   AND YEAR      ='".CTRL_YEAR."' ";
                $where .= "   AND SEMESTER  ='".CTRL_SEMESTER."' ";

                //更新ボタンを押した時、
                //レコードあり、画面のGHR_CDとGHR_ATTENDNOあり => レコード更新
                //レコードあり、画面のGHR_CDとGHR_ATTENDNOなし => レコード削除
                //レコードなし、画面のGHR_CDとGHR_ATTENDNOあり => レコード追加
                //レコードなし、画面のGHR_CDとGHR_ATTENDNOなし => 何もしない
                $query = "SELECT COUNT(*) FROM " . $table . $where;
                $cnt = $db->getOne($query);
                if (0 < $cnt) {
                    if (strlen($model->field["GHR_CD"]) && strlen($model->field["GHR_ATTENDNO"])) {
                        $query = Query::updateSQL($data, $table, $where);
                        $db->query($query);
                    } else {
                        $query = "DELETE FROM " . $table . $where;
                        $db->query($query);
                    }
                } else {
                    if (strlen($model->field["GHR_CD"]) && strlen($model->field["GHR_ATTENDNO"])) {
                        $query = Query::insertSQL($data, $table);
                        $db->query($query);
                    }
                }
            }
            //FI複式クラスを使うためのプロパティ
        } elseif ($model->Properties["useFi_Hrclass"] == '1') {
            if ($table == "schreg_regd_fi_dat") {
                $ary = explode(",", $model->field["COURSEMAJORCD"]);
                $arr = explode(",", $model->field["FI_GRADE_HR_CLASS"]);
                $data = array();
                $data["SCHREGNO"][TEXT]     = $schregno;
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                $data["SEMESTER"][TEXT]     = CTRL_SEMESTER;
                $data["GRADE"][TEXT]        = $arr[0];
                $data["HR_CLASS"][TEXT]     = $arr[1];
                $data["ATTENDNO"][TEXT]     = sprintf("%03d", $model->field["FI_ATTENDNO"]);
                $data["ANNUAL"][TEXT]       = sprintf("%02d", $model->field["ANNUAL"]);
                $data["SEAT_COL"][FUNC]     = "conv_col_row('".sprintf("%03d", $model->field["FI_ATTENDNO"])."',1)";
                $data["SEAT_ROW"][FUNC]     = "conv_col_row('".sprintf("%03d", $model->field["FI_ATTENDNO"])."',2)";
                $data["COURSECD"][TEXT]     = $ary[0];
                $data["MAJORCD"][TEXT]      = $ary[1];
                $data["COURSECODE"][TEXT]   = $model->field["COURSECODE"];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $where   = " WHERE SCHREGNO  ='".$schregno."' ";
                $where  .= "   AND YEAR      ='".CTRL_YEAR."' ";
                $where  .= "   AND SEMESTER  ='".CTRL_SEMESTER."' ";

                //更新ボタンを押した時、
                //レコードあり、画面のFI_GRADE_HR_CLASSとFI_ATTENDNOあり => レコード更新
                //レコードあり、画面のFI_GRADE_HR_CLASSとFI_ATTENDNOなし => レコード削除
                //レコードなし、画面のFI_GRADE_HR_CLASSとFI_ATTENDNOあり => レコード追加
                //レコードなし、画面のFI_GRADE_HR_CLASSとFI_ATTENDNOなし => 何もしない
                $query = "SELECT COUNT(*) FROM " . $table . $where;
                $cnt = $db->getOne($query);
                if (0 < $cnt) {
                    if (strlen($model->field["FI_GRADE_HR_CLASS"]) && strlen($model->field["FI_ATTENDNO"])) {
                        $query = Query::updateSQL($data, $table, $where);
                        $db->query($query);
                    } else {
                        $query = "DELETE FROM " . $table . $where;
                        $db->query($query);
                    }
                } else {
                    if (strlen($model->field["FI_GRADE_HR_CLASS"]) && strlen($model->field["FI_ATTENDNO"])) {
                        $query = Query::insertSQL($data, $table);
                        $db->query($query);
                    }
                }
            }
        }

        Query::dbCheckIn($db);
    }

    public function getHandicapYearData($schregno, $flg = "")
    {
        $query  = " SELECT ";
        $query .= "     T1.YEAR,  ";
        $query .= "     T1.BASE_REMARK1  ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_YEAR_DETAIL_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$schregno."' ";
        $query .= " AND T1.BASE_SEQ = '004' ";
        if ($flg) {
            $query .= " AND T1.YEAR IN (SELECT ";
            $query .= "                     MAX(YEAR) AS YEAR ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_BASE_YEAR_DETAIL_MST M1 ";
            $query .= "                 WHERE ";
            $query .= "                     M1.SCHREGNO = T1.SCHREGNO ";
            $query .= "                 AND M1.BASE_SEQ = T1.BASE_SEQ) ";
        } else {
            $query .= " AND T1.YEAR = '".CTRL_YEAR."' ";
        }
        return $query;
    }

    public function getSchregHist($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ISSUEDATE, ";
        $query .= "     T1.EXPIREDATE, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     L3.HR_NAME || '-' || T1.ATTENDNO AS GRADE_HR_ATTE, ";
        $query .= "     T1.ANNUAL, ";
        $query .= "     T1.COURSECD ||','|| T1.MAJORCD AS COURSEMAJORCD, ";
        $query .= "     L1.COURSENAME || L1.MAJORABBV AS COURSE_SUBJECT, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     L2.COURSECODENAME, ";
        $query .= "     REG_G.SCHOOL_KIND, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_SHOW, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.NAME_ENG, ";
        $query .= "     T1.REAL_NAME, ";
        $query .= "     T1.REAL_NAME_KANA, ";
        $query .= "     T1.HANDICAP, ";
        $query .= "     T1.NATIONALITY2, ";
        $query .= "     T1.NATIONALITY_NAME, ";
        $query .= "     T1.NATIONALITY_NAME_KANA, ";
        $query .= "     T1.NATIONALITY_NAME_ENG, ";
        $query .= "     T1.NATIONALITY_REAL_NAME, ";
        $query .= "     T1.NATIONALITY_REAL_NAME_KANA, ";
        $query .= "     T1.GRADE_FLG, ";
        $query .= "     T1.HR_CLASS_FLG, ";
        $query .= "     T1.ATTENDNO_FLG, ";
        $query .= "     T1.ANNUAL_FLG, ";
        $query .= "     T1.COURSECD_FLG, ";
        $query .= "     T1.MAJORCD_FLG, ";
        $query .= "     T1.COURSECODE_FLG, ";
        $query .= "     T1.NAME_FLG, ";
        $query .= "     T1.NAME_SHOW_FLG, ";
        $query .= "     T1.NAME_KANA_FLG, ";
        $query .= "     T1.NAME_ENG_FLG, ";
        $query .= "     T1.REAL_NAME_FLG, ";
        $query .= "     T1.REAL_NAME_KANA_FLG, ";
        $query .= "     T1.HANDICAP_FLG, ";
        $query .= "     T1.NATIONALITY2_FLG, ";
        $query .= "     T1.NATIONALITY_NAME_FLG, ";
        $query .= "     T1.NATIONALITY_NAME_KANA_FLG, ";
        $query .= "     T1.NATIONALITY_NAME_ENG_FLG, ";
        $query .= "     T1.NATIONALITY_REAL_NAME_FLG, ";
        $query .= "     T1.NATIONALITY_REAL_NAME_KANA_FLG ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_HIST_DAT T1 ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.COURSECD || T1.MAJORCD = L1.COURSECD || L1.MAJORCD ";
        $query .= "     LEFT JOIN V_COURSECODE_MST L2 ON T1.YEAR = L2.YEAR ";
        $query .= "          AND T1.COURSECODE = L2.COURSECODE ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT L3 ON T1.YEAR = L3.YEAR ";
        $query .= "          AND T1.SEMESTER = L3.SEMESTER ";
        $query .= "          AND T1.GRADE = L3.GRADE ";
        $query .= "          AND T1.HR_CLASS = L3.HR_CLASS ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT REG_G ON T1.YEAR = REG_G.YEAR ";
        $query .= "          AND T1.GRADE = REG_G.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ISSUEDATE ";

        return $query;
    }

    public function getSchregHistData($schregNo, $issueData)
    {
        $issueData = str_replace("/", "-", $issueData);

        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ISSUEDATE, ";
        $query .= "     T1.EXPIREDATE, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.GRADE || ',' || T1.HR_CLASS AS GRADE_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.ANNUAL, ";
        $query .= "     T1.COURSECD ||','|| T1.MAJORCD AS COURSEMAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_SHOW, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.NAME_ENG, ";
        $query .= "     T1.REAL_NAME, ";
        $query .= "     T1.REAL_NAME_KANA, ";
        $query .= "     T1.HANDICAP, ";
        $query .= "     T1.NATIONALITY2, ";
        $query .= "     T1.NATIONALITY_NAME, ";
        $query .= "     T1.NATIONALITY_NAME_KANA, ";
        $query .= "     T1.NATIONALITY_NAME_ENG, ";
        $query .= "     T1.NATIONALITY_REAL_NAME, ";
        $query .= "     T1.NATIONALITY_REAL_NAME_KANA, ";
        $query .= "     T1.GRADE_FLG, ";
        $query .= "     T1.HR_CLASS_FLG, ";
        $query .= "     T1.ATTENDNO_FLG, ";
        $query .= "     T1.ANNUAL_FLG, ";
        $query .= "     T1.COURSECD_FLG, ";
        $query .= "     T1.MAJORCD_FLG, ";
        $query .= "     T1.COURSECODE_FLG, ";
        $query .= "     T1.NAME_FLG, ";
        $query .= "     T1.NAME_SHOW_FLG, ";
        $query .= "     T1.NAME_KANA_FLG, ";
        $query .= "     T1.NAME_ENG_FLG, ";
        $query .= "     T1.REAL_NAME_FLG, ";
        $query .= "     T1.REAL_NAME_KANA_FLG, ";
        $query .= "     T1.HANDICAP_FLG, ";
        $query .= "     T1.NATIONALITY2_FLG, ";
        $query .= "     T1.NATIONALITY_NAME_FLG, ";
        $query .= "     T1.NATIONALITY_NAME_KANA_FLG, ";
        $query .= "     T1.NATIONALITY_NAME_ENG_FLG, ";
        $query .= "     T1.NATIONALITY_REAL_NAME_FLG, ";
        $query .= "     T1.NATIONALITY_REAL_NAME_KANA_FLG ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_HIST_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$schregNo."' ";
        $query .= "     AND T1.ISSUEDATE = '".$issueData."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ISSUEDATE ";

        return $query;
    }

    public function getEntGrdHist($model, $isComeBack)
    {
        $query  = " SELECT ";
        $query .= "     T1.NYUGAKUMAE_SYUSSIN_JOUHOU, ";
        $query .= "     N1.NAMECD2, ";
        $query .= "     '1' AS RIREKI_FLG, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     N1.ABBV1 AS SCHOOL_KIND_NAME, ";
        $query .= "     T1.FINSCHOOLCD, ";
        $query .= "     FIN.FINSCHOOL_NAME, ";
        $query .= "     T1.FINISH_DATE, ";
        $query .= "     T1.ENT_DATE, ";
        $query .= "     T1.CURRICULUM_YEAR, ";
        $query .= "     T1.ENT_DIV, ";
        $query .= "     N2.NAME1 AS ENT_NAME, ";
        $query .= "     T1.ENT_REASON, ";
        $query .= "     T1.ENT_SCHOOL, ";
        $query .= "     T1.ENT_ADDR, ";
        if ($model->Properties["useAddrField2"] == "1" || $model->schoolKind == "H") {
            $query .= "     T1.ENT_ADDR2, ";
        }
        $query .= "     T1.GRD_DATE, ";
        $query .= "     T1.GRD_DIV, ";
        $query .= "     N3.NAME1 AS GRD_NAME, ";
        $query .= "     T1.GRD_REASON, ";
        $query .= "     T1.GRD_SCHOOL, ";
        $query .= "     T1.GRD_ADDR, ";
        if ($model->Properties["useAddrField2"] == "1" || $model->schoolKind == "H") {
            $query .= "     T1.GRD_ADDR2, ";
        }
        $query .= "     T1.GRD_NO, ";
        $query .= "     T1.GRD_TERM, ";
        $query .= "     REG_G.SCHOOL_KIND AS SET_SKIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_ENT_GRD_HIST_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'A023' ";
        $query .= "          AND T1.SCHOOL_KIND = N1.NAME1 ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FIN ON T1.FINSCHOOLCD = FIN.FINSCHOOLCD ";
        $query .= "     LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'A002' ";
        $query .= "          AND T1.ENT_DIV = N2.NAMECD2 ";
        $query .= "     LEFT JOIN NAME_MST N3 ON N3.NAMECD1 = 'A003' ";
        $query .= "          AND T1.GRD_DIV = N3.NAMECD2 ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT REG_D ON REG_D.YEAR = CASE WHEN MONTH(T1.ENT_DATE) < 4 THEN YEAR(T1.ENT_DATE) - 1 ELSE YEAR(T1.ENT_DATE) END ";
        $query .= "          AND REG_D.SEMESTER = (SELECT SEMESTER FROM SEMESTER_MST WHERE T1.ENT_DATE BETWEEN SDATE AND EDATE AND SEMESTER <> 9) ";
        $query .= "          AND T1.SCHREGNO = REG_D.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT REG_G ON REG_D.YEAR = REG_G.YEAR ";
        $query .= "          AND REG_D.GRADE = REG_G.GRADE ";
        $query .= "          AND T1.SCHOOL_KIND = REG_G.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        if ($isComeBack) {
            $query .= " UNION ALL ";
            $query .= " SELECT ";
            $query .= "     T1.NYUGAKUMAE_SYUSSIN_JOUHOU, ";
            $query .= "     N1.NAMECD2, ";
            $query .= "     '2' AS RIREKI_FLG, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     N1.ABBV1 AS SCHOOL_KIND_NAME, ";
            $query .= "     T1.FINSCHOOLCD, ";
            $query .= "     FIN.FINSCHOOL_NAME, ";
            $query .= "     T1.FINISH_DATE, ";
            $query .= "     T1.ENT_DATE, ";
            $query .= "     T1.CURRICULUM_YEAR, ";
            $query .= "     T1.ENT_DIV, ";
            $query .= "     N2.NAME1 AS ENT_NAME, ";
            $query .= "     T1.ENT_REASON, ";
            $query .= "     T1.ENT_SCHOOL, ";
            $query .= "     T1.ENT_ADDR, ";
            if ($model->Properties["useAddrField2"] == "1" || $model->schoolKind == "H") {
                $query .= "     T1.ENT_ADDR2, ";
            }
            $query .= "     T1.GRD_DATE, ";
            $query .= "     T1.GRD_DIV, ";
            $query .= "     N3.NAME1 AS GRD_NAME, ";
            $query .= "     T1.GRD_REASON, ";
            $query .= "     T1.GRD_SCHOOL, ";
            $query .= "     T1.GRD_ADDR, ";
            if ($model->Properties["useAddrField2"] == "1" || $model->schoolKind == "H") {
                $query .= "     T1.GRD_ADDR2, ";
            }
            $query .= "     T1.GRD_NO, ";
            $query .= "     T1.GRD_TERM, ";
            $query .= "     REG_G.SCHOOL_KIND AS SET_SKIND ";
            $query .= " FROM ";
            $query .= "     SCHREG_ENT_GRD_HIST_COMEBACK_DAT T1 ";
            $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'A023' ";
            $query .= "          AND T1.SCHOOL_KIND = N1.NAME1 ";
            $query .= "     LEFT JOIN FINSCHOOL_MST FIN ON T1.FINSCHOOLCD = FIN.FINSCHOOLCD ";
            $query .= "     LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'A002' ";
            $query .= "          AND T1.ENT_DIV = N2.NAMECD2 ";
            $query .= "     LEFT JOIN NAME_MST N3 ON N3.NAMECD1 = 'A003' ";
            $query .= "          AND T1.GRD_DIV = N3.NAMECD2 ";
            $query .= "     LEFT JOIN SCHREG_REGD_DAT REG_D ON REG_D.YEAR = CASE WHEN MONTH(T1.ENT_DATE) < 4 THEN YEAR(T1.ENT_DATE) - 1 ELSE YEAR(T1.ENT_DATE) END ";
            $query .= "          AND REG_D.SEMESTER = (SELECT SEMESTER FROM SEMESTER_MST WHERE T1.ENT_DATE BETWEEN SDATE AND EDATE AND SEMESTER <> 9) ";
            $query .= "          AND T1.SCHREGNO = REG_D.SCHREGNO ";
            $query .= "     LEFT JOIN SCHREG_REGD_GDAT REG_G ON REG_D.YEAR = REG_G.YEAR ";
            $query .= "          AND REG_D.GRADE = REG_G.GRADE ";
            $query .= "          AND T1.SCHOOL_KIND = REG_G.SCHOOL_KIND ";
            $query .= " WHERE ";
            $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2, ";
        $query .= "     ENT_DATE ";

        return $query;
    }

    public function getEntGrdHistData($model, $schregNo, $schoolKind)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     N1.ABBV1 AS SCHOOL_KIND_NAME, ";
        $query .= "     T1.FINSCHOOLCD, ";
        $query .= "     FIN.FINSCHOOL_NAME, ";
        $query .= "     T1.FINISH_DATE, ";
        $query .= "     T1.ENT_DATE, ";
        $query .= "     T1.CURRICULUM_YEAR, ";
        $query .= "     T1.ENT_DIV, ";
        $query .= "     N2.NAME1 AS ENT_NAME, ";
        $query .= "     T1.ENT_REASON, ";
        $query .= "     T1.ENT_SCHOOL, ";
        $query .= "     T1.ENT_ADDR, ";
        if ($model->Properties["useAddrField2"] == "1" || $schoolKind == "H") {
            $query .= "     T1.ENT_ADDR2, ";
        }
        $query .= "     T1.GRD_DATE, ";
        $query .= "     T1.GRD_DIV, ";
        $query .= "     N3.NAME1 AS GRD_NAME, ";
        $query .= "     T1.GRD_REASON, ";
        $query .= "     T1.GRD_SCHOOL, ";
        $query .= "     T1.GRD_ADDR, ";
        if ($model->Properties["useAddrField2"] == "1" || $schoolKind == "H") {
            $query .= "     T1.GRD_ADDR2, ";
        }
        $query .= "     T1.GRD_NO, ";
        $query .= "     T1.GRD_TERM, ";
        $query .= "     T1.TENGAKU_SAKI_ZENJITU, ";
        $query .= "     T1.TENGAKU_SAKI_GRADE, ";
        $query .= "     T1.NYUGAKUMAE_SYUSSIN_JOUHOU, ";
        $query .= "     VBASE.EXAMNO ";
        if ($model->school_name == "meikei") {
            $query .= "   , D014.BASE_REMARK1 || '  ' || N4.NAME1 AS TESTDIV_LABEL, ";
            $query .= "     D014.BASE_REMARK1 AS TESTDIV, ";
            $query .= "     D014.BASE_REMARK2 || '  ' || N5.NAME1 AS EXAM_TYPE_LABEL, ";
            $query .= "     D014.BASE_REMARK2 AS EXAM_TYPE ";
        } elseif ($model->school_name != "koma") {
            $query .= "   , D003.BASE_REMARK2 AS TESTDIV ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_ENT_GRD_HIST_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'A023' ";
        $query .= "          AND T1.SCHOOL_KIND = N1.NAME1 ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FIN ON T1.FINSCHOOLCD = FIN.FINSCHOOLCD ";
        $query .= "     LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'A002' ";
        $query .= "          AND T1.ENT_DIV = N2.NAMECD2 ";
        $query .= "     LEFT JOIN NAME_MST N3 ON N3.NAMECD1 = 'A003' ";
        $query .= "          AND T1.GRD_DIV = N3.NAMECD2 ";
        $query .= "     LEFT JOIN V_SCHREG_BASE_MST VBASE ON VBASE.SCHREGNO = T1.SCHREGNO ";
        if ($model->school_name == "meikei") {
            $query .= "     LEFT JOIN SCHREG_BASE_DETAIL_MST D014 ON D014.SCHREGNO = T1.SCHREGNO AND D014.BASE_SEQ = '014' ";
            $query .= "     LEFT JOIN NAME_MST N4 ON N4.NAMECD1 = 'A056' ";
            $query .= "          AND N4.NAMECD2 = D014.BASE_REMARK1 ";
            $query .= "     LEFT JOIN NAME_MST N5 ON N5.NAMECD1 = 'A057' ";
            $query .= "          AND N5.NAMECD2 = D014.BASE_REMARK2 ";
        } elseif ($model->school_name != "koma") {
            $query .= "     LEFT JOIN SCHREG_BASE_DETAIL_MST D003 ON D003.SCHREGNO = T1.SCHREGNO AND D003.BASE_SEQ = '003' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$schregNo."' ";
        $query .= "     AND T1.SCHOOL_KIND = '".$schoolKind."' ";
        $query .= " ORDER BY ";
        $query .= "     N1.NAMECD2 ";

        return $query;
    }

    public function getSchregData($schregNo)
    {
        $query  = " SELECT ";
        $query .= "     T2.SCHREGNO, ";
        $query .= "     T2.YEAR, ";
        $query .= "     T2.SEMESTER, ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.GRADE || ',' || T2.HR_CLASS AS GRADE_CLASS, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T2.ANNUAL, ";
        $query .= "     T2.COURSECD ||','|| T2.MAJORCD AS COURSEMAJORCD, ";
        $query .= "     T2.COURSECODE, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_SHOW, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.NAME_ENG, ";
        $query .= "     T1.REAL_NAME, ";
        $query .= "     T1.REAL_NAME_KANA, ";
        $query .= "     T1.HANDICAP ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     SCHREG_REGD_DAT T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$schregNo}' AND ";
        $query .= "     T2.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T2.SEMESTER = '".CTRL_SEMESTER."' ";

        return $query;
    }

    //年組,先生名
    public function getGrdClasHist($year, $semester, $model)
    {
        $query  = "";
        $query .= "  SELECT DISTINCT ";
        $query .= "      T1.GRADE || ',' || T1.HR_CLASS AS VALUE, ";
        $query .= "      T1.HR_NAME AS LABEL ";
        $query .= "  FROM ";
        $query .= "      SCHREG_REGD_HDAT T1 ";
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
        $query .= "  WHERE ";
        $query .= "      T1.YEAR     = '".$year."' AND ";
        $query .= "      T1.SEMESTER = '".$semester."' ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";
        return $query;
    }

    //年組デフォルト
    public function getGrdClasHistDef($year, $semester, $model)
    {
        $query  = "";
        $query .= "  SELECT DISTINCT ";
        $query .= "      T1.GRADE, ";
        $query .= "      T1.HR_CLASS ";
        $query .= "  FROM ";
        $query .= "      SCHREG_REGD_DAT T1 ";
        $query .= "  WHERE ";
        $query .= "      T1.YEAR     = '".$year."' AND ";
        $query .= "      T1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "      T1.SEMESTER = '".$semester."' ";

        return $query;
    }

    //課程学科コンボ
    public function getCourseSubjectHist($year)
    {
        $query  = "";
        $query .= "  SELECT DISTINCT";
        $query .= "      COURSECD || ',' || MAJORCD AS COURSEMAJORCD,";
        $query .= "      COURSENAME || MAJORABBV AS COURSE_SUBJECT";
        $query .= "  FROM";
        $query .= "      V_COURSE_MAJOR_MST";
        $query .= "  WHERE";
        $query .= "      YEAR = '".$year."'";
        $query .= "  ORDER BY";
        $query .= "      COURSEMAJORCD";
        return $query;
    }

    //コースコード取得
    public function getCourseCodeHist($year)
    {
        $query  = " SELECT COURSECODE, COURSECODENAME ";
        $query .= "   FROM V_COURSECODE_MST ";
        $query .= " WHERE YEAR     = '". $year ."'";
        $query .= " ORDER BY COURSECODE";
        return $query;
    }

    //年度取得
    public function getYear($model)
    {
        $query  = "";
        $query .= "  SELECT DISTINCT ";
        $query .= "      T1.YEAR || '年度 ' AS LABEL, ";
        $query .= "      T1.YEAR AS VALUE ";
        $query .= "  FROM";
        $query .= "      SCHREG_REGD_DAT T1 ";
        $query .= "  WHERE";
        $query .= "      T1.SCHREGNO = '".$model->schregno."'";
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    //学期取得
    public function getSeme($val)
    {
        $query  = "";
        $query .= "  SELECT ";
        $query .= "      T1.SEMESTERNAME AS LABEL, ";
        $query .= "      T1.SEMESTER AS VALUE ";
        $query .= "  FROM";
        $query .= "      SEMESTER_MST T1 ";
        $query .= "  WHERE";
        $query .= "      T1.YEAR = '".$val["YEAR"]."'";
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    public function getSchregHistDateCheck($model)
    {
        $issueData = str_replace("/", "-", $model->histField["ISSUEDATE"]);
        $expireDate = str_replace("/", "-", $model->histField["EXPIREDATE"]);

        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_HIST_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND T1.ISSUEDATE <> '".$issueData."' ";
        $query .= "     AND (T1.ISSUEDATE BETWEEN '".$issueData."' AND '".$expireDate."' ";
        $query .= "          OR ";
        $query .= "          T1.EXPIREDATE BETWEEN '".$issueData."' AND '".$expireDate."') ";

        return $query;
    }

    public function getDelHistQuery($model)
    {
        $query  = " DELETE FROM ";
        $query .= "     SCHREG_BASE_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND ISSUEDATE = '".str_replace("/", "-", $model->histField["ISSUEDATE"])."' ";

        return $query;
    }

    public function getInsHistQuery($model)
    {
        $data = array();
        $data["SCHREGNO"][TEXT]                        = $model->schregno;
        $data["ISSUEDATE"][DATE]                       = $model->histField["ISSUEDATE"];
        $data["EXPIREDATE"][DATE]                      = $model->histField["EXPIREDATE"];
        $data["YEAR"][TEXT]                            = $model->histField["YEAR"];
        $data["SEMESTER"][TEXT]                        = $model->histField["SEMESTER"];
        $data["GRADE"][TEXT]                           = $model->histField["GRADE"];
        $data["HR_CLASS"][TEXT]                        = $model->histField["HR_CLASS"];
        $data["ATTENDNO"][TEXT]                        = $model->histField["ATTENDNO"];
        $data["ANNUAL"][TEXT]                          = $model->histField["ANNUAL"];
        $data["COURSECD"][TEXT]                        = $model->histField["COURSECD"];
        $data["MAJORCD"][TEXT]                         = $model->histField["MAJORCD"];
        $data["COURSECODE"][TEXT]                      = $model->histField["COURSECODE"];
        $data["NAME"][TEXT]                            = $model->histField["NAME"];
        $data["NAME_SHOW"][TEXT]                       = $model->histField["NAME_SHOW"];
        $data["NAME_KANA"][TEXT]                       = $model->histField["NAME_KANA"];
        $data["NAME_ENG"][TEXT]                        = $model->histField["NAME_ENG"];
        $data["REAL_NAME"][TEXT]                       = $model->histField["REAL_NAME"];
        $data["REAL_NAME_KANA"][TEXT]                  = $model->histField["REAL_NAME_KANA"];
        $data["HANDICAP"][TEXT]                        = $model->histField["HANDICAP"];
        $data["NATIONALITY2"][TEXT]                    = $model->histField["NATIONALITY2"];
        $data["NATIONALITY_NAME"][TEXT]                = $model->histField["NATIONALITY_NAME"];
        $data["NATIONALITY_NAME_KANA"][TEXT]           = $model->histField["NATIONALITY_NAME_KANA"];
        $data["NATIONALITY_NAME_ENG"][TEXT]            = $model->histField["NATIONALITY_NAME_ENG"];
        $data["NATIONALITY_REAL_NAME"][TEXT]           = $model->histField["NATIONALITY_REAL_NAME"];
        $data["NATIONALITY_REAL_NAME_KANA"][TEXT]      = $model->histField["NATIONALITY_REAL_NAME_KANA"];

        $data["GRADE_FLG"][TEXT]                       = $model->histField["GRADE_FLG"] ? $model->histField["GRADE_FLG"] : "0";
        $data["HR_CLASS_FLG"][TEXT]                    = $model->histField["HR_CLASS_FLG"] ? $model->histField["HR_CLASS_FLG"] : "0";
        $data["ATTENDNO_FLG"][TEXT]                    = $model->histField["ATTENDNO_FLG"] ? $model->histField["ATTENDNO_FLG"] : "0";
        $data["ANNUAL_FLG"][TEXT]                      = $model->histField["ANNUAL_FLG"] ? $model->histField["ANNUAL_FLG"] : "0";
        $data["COURSECD_FLG"][TEXT]                    = $model->histField["COURSECD_FLG"] ? $model->histField["COURSECD_FLG"] : "0";
        $data["MAJORCD_FLG"][TEXT]                     = $model->histField["MAJORCD_FLG"] ? $model->histField["MAJORCD_FLG"] : "0";
        $data["COURSECODE_FLG"][TEXT]                  = $model->histField["COURSECODE_FLG"] ? $model->histField["COURSECODE_FLG"] : "0";
        $data["NAME_FLG"][TEXT]                        = $model->histField["NAME_FLG"] ? $model->histField["NAME_FLG"] : "0";
        $data["NAME_SHOW_FLG"][TEXT]                   = $model->histField["NAME_SHOW_FLG"] ? $model->histField["NAME_SHOW_FLG"] : "0";
        $data["NAME_KANA_FLG"][TEXT]                   = $model->histField["NAME_KANA_FLG"] ? $model->histField["NAME_KANA_FLG"] : "0";
        $data["NAME_ENG_FLG"][TEXT]                    = $model->histField["NAME_ENG_FLG"] ? $model->histField["NAME_ENG_FLG"] : "0";
        $data["REAL_NAME_FLG"][TEXT]                   = $model->histField["REAL_NAME_FLG"] ? $model->histField["REAL_NAME_FLG"] : "0";
        $data["REAL_NAME_KANA_FLG"][TEXT]              = $model->histField["REAL_NAME_KANA_FLG"] ? $model->histField["REAL_NAME_KANA_FLG"] : "0";
        $data["HANDICAP_FLG"][TEXT]                    = $model->histField["HANDICAP_FLG"] ? $model->histField["HANDICAP_FLG"] : "0";
        $data["NATIONALITY2_FLG"][TEXT]                = $model->histField["NATIONALITY2_FLG"] ? $model->histField["NATIONALITY2_FLG"] : "0";
        $data["NATIONALITY_NAME_FLG"][TEXT]            = $model->histField["NATIONALITY_NAME_FLG"] ? $model->histField["NATIONALITY_NAME_FLG"] : "0";
        $data["NATIONALITY_NAME_KANA_FLG"][TEXT]       = $model->histField["NATIONALITY_NAME_KANA_FLG"] ? $model->histField["NATIONALITY_NAME_KANA_FLG"] : "0";
        $data["NATIONALITY_NAME_ENG_FLG"][TEXT]        = $model->histField["NATIONALITY_NAME_ENG_FLG"] ? $model->histField["NATIONALITY_NAME_ENG_FLG"] : "0";
        $data["NATIONALITY_REAL_NAME_FLG"][TEXT]       = $model->histField["NATIONALITY_REAL_NAME_FLG"] ? $model->histField["NATIONALITY_REAL_NAME_FLG"] : "0";
        $data["NATIONALITY_REAL_NAME_KANA_FLG"][TEXT]  = $model->histField["NATIONALITY_REAL_NAME_KANA_FLG"] ? $model->histField["NATIONALITY_REAL_NAME_KANA_FLG"] : "0";

        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][FUNC]             = "sysdate()";

        $query = Query::insertSQL($data, "SCHREG_BASE_HIST_DAT");
        return $query;
    }

    public function setBaseYearDetail($db, $model)
    {
        $query = knja110aQuery::getBaseHistAll($model->schregno);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            for ($setYear = $row["SYEAR"]; $setYear <= $row["EYEAR"]; $setYear++) {
                $query = knja110aQuery::getHandicapShiteiYearData($model->schregno, $setYear);
                $HandicapLoginData = $db->getRow($query, DB_FETCHMODE_ASSOC);

                $data = array();
                $data["BASE_REMARK1"][TEXT] = $row["HANDICAP"];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";
                if (!is_array($HandicapLoginData)) {
                    $data["SCHREGNO"][TEXT]     = $model->schregno;
                    $data["YEAR"][TEXT]         = $setYear;
                    $data["BASE_SEQ"][TEXT]     = "004";
                    $query = Query::insertSQL($data, SCHREG_BASE_YEAR_DETAIL_MST);
                    $db->query($query);
                } else {
                    $where  = " WHERE SCHREGNO  = '{$model->schregno}' ";
                    $where .= "   AND YEAR      = '{$setYear}' ";
                    $where .= "   AND BASE_SEQ  = '004' ";
                    $query = Query::updateSQL($data, SCHREG_BASE_YEAR_DETAIL_MST, $where);
                    $db->query($query);
                }
            }
        }
        $result->free();

        //今年度データは、BASEデータをセット
        $query = knja110aQuery::getBaseHandicap($model->schregno);
        $baseHandicap = $db->getOne($query);
        $query = knja110aQuery::getHandicapShiteiYearData($model->schregno, CTRL_YEAR);
        $HandicapLoginData = $db->getRow($query, DB_FETCHMODE_ASSOC);

        $data = array();
        $data["BASE_REMARK1"][TEXT] = $baseHandicap;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        if (!is_array($HandicapLoginData)) {
            $data["SCHREGNO"][TEXT]     = $model->schregno;
            $data["YEAR"][TEXT]         = CTRL_YEAR;
            $data["BASE_SEQ"][TEXT]     = "004";
            $query = Query::insertSQL($data, SCHREG_BASE_YEAR_DETAIL_MST);
            $db->query($query);
        } else {
            $where  = " WHERE SCHREGNO  = '{$model->schregno}' ";
            $where .= "   AND YEAR      = '".CTRL_YEAR."' ";
            $where .= "   AND BASE_SEQ  = '004' ";
            $query = Query::updateSQL($data, SCHREG_BASE_YEAR_DETAIL_MST, $where);
            $db->query($query);
        }

        return;
    }

    //全データ
    public function getBaseHistAll($schregno)
    {
        $query  = " SELECT ";
        $query .= "     ISSUEDATE, ";
        $query .= "     FISCALYEAR(T1.ISSUEDATE) AS SYEAR, ";
        $query .= "     FISCALYEAR(T1.EXPIREDATE) AS EYEAR, ";
        $query .= "     HANDICAP ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_HIST_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     ISSUEDATE ";

        return $query;
    }

    public function getHandicapShiteiYearData($schregno, $year)
    {
        $query  = " SELECT ";
        $query .= "     T1.YEAR,  ";
        $query .= "     T1.BASE_REMARK1  ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_YEAR_DETAIL_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$schregno}' ";
        $query .= " AND T1.BASE_SEQ = '004' ";
        $query .= " AND T1.YEAR = '{$year}' ";

        return $query;
    }

    public function getBaseHandicap($schregno)
    {
        $query  = " SELECT ";
        $query .= "     T1.HANDICAP  ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$schregno}' ";

        return $query;
    }

    //アップデート
    public function &getInsUpdEntGrd($model)
    {
        $db = Query::dbCheckOut();
        $schregno = substr("00000000".$model->field["SCHREGNO"], strlen($model->field["SCHREGNO"]), 8);
        $entGrdData = $db->getRow(knja110aQuery::getEntGrdData($model, $schregno, ""), DB_FETCHMODE_ASSOC);
        $data = array();

        if (!is_array($entGrdData)) {
            $data["SCHREGNO"][TEXT]    = $schregno;
            $data["SCHOOL_KIND"][TEXT] = $model->schoolKind;
        }
        $data["FINSCHOOLCD"][TEXT]                  = ($model->field["FINSCHOOLCD"] ? sprintf("%0".($model->finschoolcdKeta)."s", $model->field["FINSCHOOLCD"]) : "");
        $data["FINISH_DATE"][DATE]                  = str_replace("/", "-", $model->field["FINISH_DATE"]);
        $data["ENT_DATE"][DATE]                     = str_replace("/", "-", $model->field["ENT_DATE"]);
        $data["CURRICULUM_YEAR"][TEXT]              = $model->field["CURRICULUM_YEAR"];
        $data["ENT_DIV"][TEXT]                      = $model->field["ENT_DIV"];
        $data["ENT_REASON"][TEXT]                   = $model->field["ENT_REASON"];
        $data["ENT_SCHOOL"][TEXT]                   = $model->field["ENT_SCHOOL"];
        $data["ENT_ADDR"][TEXT]                     = $model->field["ENT_ADDR"];
        if ($model->Properties["useAddrField2"] == "1" || $model->schoolKind == "H") {
            $data["ENT_ADDR2"][TEXT]                    = $model->field["ENT_ADDR2"];
        }
        $data["GRD_DATE"][DATE]                     = str_replace("/", "-", $model->field["GRD_DATE"]);
        $data["GRD_DIV"][TEXT]                      = $model->field["GRD_DIV"];
        $data["GRD_REASON"][TEXT]                   = $model->field["GRD_REASON"];
        $data["GRD_SCHOOL"][TEXT]                   = $model->field["GRD_SCHOOL"];
        $data["GRD_ADDR"][TEXT]                     = $model->field["GRD_ADDR"];
        if ($model->Properties["useAddrField2"] == "1" || $model->schoolKind == "H") {
            $data["GRD_ADDR2"][TEXT]                    = $model->field["GRD_ADDR2"];
        }
        $data["TENGAKU_SAKI_ZENJITU"][DATE]         = str_replace("/", "-", $model->field["TENGAKU_SAKI_ZENJITU"]);
        $data["TENGAKU_SAKI_GRADE"][TEXT]           = $model->field["TENGAKU_SAKI_GRADE"];
        $data["NYUGAKUMAE_SYUSSIN_JOUHOU"][TEXT]    = $model->field["NYUGAKUMAE_SYUSSIN_JOUHOU"];
        $data["REGISTERCD"][TEXT]                   = STAFFCD;
        $data["UPDATED"][FUNC]                      = "sysdate()";

        if (!is_array($entGrdData)) {
            $query = Query::insertSQL($data, "SCHREG_ENT_GRD_HIST_DAT");
        } else {
            $where .= " WHERE ";
            $where .= "     SCHREGNO = '".$schregno."' ";
            $where .= "     AND SCHOOL_KIND = '". $model->schoolKind. "' ";
            $query = Query::updateSQL($data, "SCHREG_ENT_GRD_HIST_DAT", $where);
        }

        $db->query($query);
        Query::dbCheckIn($db);
    }

    public function getEntGrdData($model, $schregno, $div)
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_ENT_GRD_HIST_DAT T1 ";
        $query .= "     INNER JOIN NAME_MST L1 ON L1.NAMECD1 = 'A023' ";
        $query .= "           AND T1.SCHOOL_KIND = L1.NAME1 ";
        $query .= " WHERE ";
        if ($div == "MAX") {
            $query .= "     T1.SCHREGNO = '".$schregno."' ";
            $query .= " ORDER BY ";
            $query .= "     L1.NAMECD2 DESC ";
        } else {
            $query .= "     T1.SCHREGNO = '".$schregno."' ";
            $query .= "     AND T1.SCHOOL_KIND = '". $model->schoolKind. "' ";
        }

        return $query;
    }

    public function getEntGrdDelHistQuery($db, $model)
    {
        $query  = " DELETE FROM ";
        $query .= "     SCHREG_ENT_GRD_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->grdEntHistField["SCHOOL_KIND"]."' ";
        $db->query($query);
    }

    public function getInsEntGrdHistQuery($db, $model)
    {
        $data = array();
        $data["SCHREGNO"][TEXT]                     = $model->schregno;
        $data["SCHOOL_KIND"][TEXT]                  = $model->grdEntHistField["SCHOOL_KIND"];
        $data["FINSCHOOLCD"][TEXT]                  = ($model->grdEntHistField["FINSCHOOLCD"] ? sprintf("%0".($model->finschoolcdKeta)."s", $model->grdEntHistField["FINSCHOOLCD"]) : "");
        $data["FINISH_DATE"][DATE]                  = str_replace("/", "-", $model->grdEntHistField["FINISH_DATE"]);
        $data["ENT_DATE"][DATE]                     = str_replace("/", "-", $model->grdEntHistField["ENT_DATE"]);
        $data["CURRICULUM_YEAR"][TEXT]              = $model->grdEntHistField["CURRICULUM_YEAR"];
        $data["ENT_DIV"][TEXT]                      = $model->grdEntHistField["ENT_DIV"];
        $data["ENT_REASON"][TEXT]                   = $model->grdEntHistField["ENT_REASON"];
        $data["ENT_SCHOOL"][TEXT]                   = $model->grdEntHistField["ENT_SCHOOL"];
        $data["ENT_ADDR"][TEXT]                     = $model->grdEntHistField["ENT_ADDR"];
        if ($model->Properties["useAddrField2"] == "1" || $model->grdEntHistField["SCHOOL_KIND"] == "H") {
            $data["ENT_ADDR2"][TEXT]                    = $model->grdEntHistField["ENT_ADDR2"];
        }
        $data["GRD_DATE"][DATE]                     = str_replace("/", "-", $model->grdEntHistField["GRD_DATE"]);
        $data["GRD_DIV"][TEXT]                      = $model->grdEntHistField["GRD_DIV"];
        $data["GRD_REASON"][TEXT]                   = $model->grdEntHistField["GRD_REASON"];
        $data["GRD_SCHOOL"][TEXT]                   = $model->grdEntHistField["GRD_SCHOOL"];
        $data["GRD_ADDR"][TEXT]                     = $model->grdEntHistField["GRD_ADDR"];
        if ($model->Properties["useAddrField2"] == "1" || $model->grdEntHistField["SCHOOL_KIND"] == "H") {
            $data["GRD_ADDR2"][TEXT]                    = $model->grdEntHistField["GRD_ADDR2"];
        }
        $data["TENGAKU_SAKI_ZENJITU"][DATE]         = str_replace("/", "-", $model->grdEntHistField["TENGAKU_SAKI_ZENJITU"]);
        $data["TENGAKU_SAKI_GRADE"][TEXT]           = $model->grdEntHistField["TENGAKU_SAKI_GRADE"];
        $data["NYUGAKUMAE_SYUSSIN_JOUHOU"][TEXT]    = $model->grdEntHistField["NYUGAKUMAE_SYUSSIN_JOUHOU"];
        $data["REGISTERCD"][TEXT]                   = STAFFCD;
        $data["UPDATED"][FUNC]                      = "sysdate()";

        $query = Query::insertSQL($data, "SCHREG_ENT_GRD_HIST_DAT");
        $db->query($query);
    }

    public function getUpdEntGrdHistQuery($db, $model)
    {
        $data = array();
        $data["FINSCHOOLCD"][TEXT]                  = ($model->grdEntHistField["FINSCHOOLCD"] ? sprintf("%0".($model->finschoolcdKeta)."s", $model->grdEntHistField["FINSCHOOLCD"]) : "");
        $data["FINISH_DATE"][DATE]                  = str_replace("/", "-", $model->grdEntHistField["FINISH_DATE"]);
        $data["ENT_DATE"][DATE]                     = str_replace("/", "-", $model->grdEntHistField["ENT_DATE"]);
        $data["CURRICULUM_YEAR"][TEXT]              = $model->grdEntHistField["CURRICULUM_YEAR"];
        $data["ENT_DIV"][TEXT]                      = $model->grdEntHistField["ENT_DIV"];
        $data["ENT_REASON"][TEXT]                   = $model->grdEntHistField["ENT_REASON"];
        $data["ENT_SCHOOL"][TEXT]                   = $model->grdEntHistField["ENT_SCHOOL"];
        $data["ENT_ADDR"][TEXT]                     = $model->grdEntHistField["ENT_ADDR"];
        if ($model->Properties["useAddrField2"] == "1" || $model->grdEntHistField["SCHOOL_KIND"] == "H") {
            $data["ENT_ADDR2"][TEXT]                    = $model->grdEntHistField["ENT_ADDR2"];
        }
        $data["GRD_DATE"][DATE]                     = str_replace("/", "-", $model->grdEntHistField["GRD_DATE"]);
        $data["GRD_DIV"][TEXT]                      = $model->grdEntHistField["GRD_DIV"];
        $data["GRD_REASON"][TEXT]                   = $model->grdEntHistField["GRD_REASON"];
        $data["GRD_SCHOOL"][TEXT]                   = $model->grdEntHistField["GRD_SCHOOL"];
        $data["GRD_ADDR"][TEXT]                     = $model->grdEntHistField["GRD_ADDR"];
        if ($model->Properties["useAddrField2"] == "1" || $model->grdEntHistField["SCHOOL_KIND"] == "H") {
            $data["GRD_ADDR2"][TEXT]                    = $model->grdEntHistField["GRD_ADDR2"];
        }
        $data["GRD_TERM"][TEXT]                     = $model->grdEntHistField["GRD_TERM"];
        $data["TENGAKU_SAKI_ZENJITU"][DATE]         = str_replace("/", "-", $model->grdEntHistField["TENGAKU_SAKI_ZENJITU"]);
        $data["TENGAKU_SAKI_GRADE"][TEXT]           = $model->grdEntHistField["TENGAKU_SAKI_GRADE"];
        $data["NYUGAKUMAE_SYUSSIN_JOUHOU"][TEXT]    = $model->grdEntHistField["NYUGAKUMAE_SYUSSIN_JOUHOU"];
        $data["REGISTERCD"][TEXT]                   = STAFFCD;
        $data["UPDATED"][FUNC]                      = "sysdate()";

        $where  = " WHERE ";
        $where .= "     SCHREGNO = '".$model->schregno."' ";
        $where .= "     AND SCHOOL_KIND = '".$model->grdEntHistField["SCHOOL_KIND"]."' ";

        $query = Query::updateSQL($data, "SCHREG_ENT_GRD_HIST_DAT", $where);
        $db->query($query);
    }

    public function getNowSchoolKind($model)
    {
        $query  = " SELECT ";
        $query .= "     L1.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.GRADE = L1.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";

        return $query;
    }

    public function getUpdBaseMstEntGrd($db, $model)
    {
        if ($model->cmd == "entGrdHistDel") {
            $model->grdEntHistField["FINSCHOOLCD"] = "";
            $model->grdEntHistField["FINISH_DATE"] = "";
            $model->grdEntHistField["ENT_DATE"] = "";
            $model->grdEntHistField["ENT_DIV"] = "";
            $model->grdEntHistField["ENT_REASON"] = "";
            $model->grdEntHistField["ENT_SCHOOL"] = "";
            $model->grdEntHistField["ENT_ADDR"] = "";
            if ($model->Properties["useAddrField2"] == "1" || $model->grdEntHistField["SCHOOL_KIND"] == "H") {
                $model->grdEntHistField["ENT_ADDR2"] = "";
            }
            $model->grdEntHistField["GRD_DATE"] = "";
            $model->grdEntHistField["GRD_DIV"] = "";
            $model->grdEntHistField["GRD_REASON"] = "";
            $model->grdEntHistField["GRD_SCHOOL"] = "";
            $model->grdEntHistField["GRD_ADDR"] = "";
            if ($model->Properties["useAddrField2"] == "1" || $model->grdEntHistField["SCHOOL_KIND"] == "H") {
                $model->grdEntHistField["GRD_ADDR2"] = "";
            }
        }
        $data = array();
        $data["FINSCHOOLCD"][TEXT] = ($model->grdEntHistField["FINSCHOOLCD"] ? sprintf("%0".($model->finschoolcdKeta)."s", $model->grdEntHistField["FINSCHOOLCD"]): "");
        $data["FINISH_DATE"][DATE] = str_replace("/", "-", $model->grdEntHistField["FINISH_DATE"]);
        $data["ENT_DATE"][DATE]    = str_replace("/", "-", $model->grdEntHistField["ENT_DATE"]);
        $data["ENT_DIV"][TEXT]     = $model->grdEntHistField["ENT_DIV"];
        $data["ENT_REASON"][TEXT]  = $model->grdEntHistField["ENT_REASON"];
        $data["ENT_SCHOOL"][TEXT]  = $model->grdEntHistField["ENT_SCHOOL"];
        $data["ENT_ADDR"][TEXT]    = $model->grdEntHistField["ENT_ADDR"];
        if ($model->Properties["useAddrField2"] == "1" || $model->grdEntHistField["SCHOOL_KIND"] == "H") {
            $data["ENT_ADDR2"][TEXT]   = $model->grdEntHistField["ENT_ADDR2"];
        }
        $data["GRD_DATE"][DATE]    = str_replace("/", "-", $model->grdEntHistField["GRD_DATE"]);
        $data["GRD_DIV"][TEXT]     = $model->grdEntHistField["GRD_DIV"];
        $data["GRD_REASON"][TEXT]  = $model->grdEntHistField["GRD_REASON"];
        $data["GRD_SCHOOL"][TEXT]  = $model->grdEntHistField["GRD_SCHOOL"];
        $data["GRD_ADDR"][TEXT]    = $model->grdEntHistField["GRD_ADDR"];
        if ($model->Properties["useAddrField2"] == "1" || $model->grdEntHistField["SCHOOL_KIND"] == "H") {
            $data["GRD_ADDR2"][TEXT]   = $model->grdEntHistField["GRD_ADDR2"];
        }
        $data["GRD_TERM"][TEXT]    = $model->grdEntHistField["GRD_TERM"];
        $data["REGISTERCD"][TEXT]  = STAFFCD;
        $data["UPDATED"][FUNC]     = "sysdate()";

        $where  = " WHERE ";
        $where .= "     SCHREGNO = '".$model->schregno."' ";

        $query = Query::updateSQL($data, "SCHREG_BASE_MST", $where);
        $db->query($query);

        if ($model->grdEntHistField["GRD_DIV"] == "1" ||
            $model->grdEntHistField["GRD_DIV"] == "2" ||
            $model->grdEntHistField["GRD_DIV"] == "3"
        ) {
            $query = "DELETE FROM CLASS_FORMATION_DAT WHERE SCHREGNO = '{$model->schregno}' AND YEAR = '".(CTRL_YEAR + 1)."'";
            $db->query($query);
        }

        $query = "DELETE FROM SCHREG_BASE_DETAIL_MST WHERE SCHREGNO ='".$model->schregno."' AND BASE_SEQ = '001' ";
        $db->query($query);

        $data = array();
        $data["SCHREGNO"][TEXT]     = $model->schregno;
        $data["BASE_SEQ"][TEXT]     = "001";
        $data["BASE_REMARK1"][TEXT] = str_replace("/", "-", $model->grdEntHistField["TENGAKU_SAKI_ZENJITU"]);
        $data["BASE_REMARK2"][TEXT] = $model->grdEntHistField["TENGAKU_SAKI_GRADE"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        $query = Query::insertSQL($data, "SCHREG_BASE_DETAIL_MST");
        $db->query($query);

        $query = "DELETE FROM SCHREG_BASE_DETAIL_MST WHERE SCHREGNO ='".$model->schregno."' AND BASE_SEQ = '002' ";
        $db->query($query);

        $data = array();
        $data["SCHREGNO"][TEXT]     = $model->schregno;
        $data["BASE_SEQ"][TEXT]     = "002";
        $data["BASE_REMARK1"][TEXT] = $model->grdEntHistField["NYUGAKUMAE_SYUSSIN_JOUHOU"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        $query = Query::insertSQL($data, "SCHREG_BASE_DETAIL_MST");
        $db->query($query);
    }

    public function getBaseHistUpd($schregno)
    {
        $query .= " WITH SYSD(SYSD) AS ( ";
        $query .= "     VALUES(CAST(sysdate() AS DATE)) ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_HIST_DAT T1, ";
        $query .= "     SYSD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$schregno}' ";
        $query .= "     AND SYSD.SYSD BETWEEN T1.ISSUEDATE AND T1.EXPIREDATE ";

        return $query;
    }

    public function getBaseUpdFromHist($data, $schregno)
    {
        $data = array();
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC]    = "sysdate()";

        $where  = "WHERE SCHREGNO = '{$schregno}' ";
        $query = Query::updateSQL($data, "SCHREG_BASE_MST", $where);

        return $query;
    }

    public function getRegdUpdFromHist($data, $histData, $schregno)
    {
        $data = array();
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC]    = "sysdate()";

        $where  = " WHERE SCHREGNO = '{$schregno}' ";
        $where .= "     and YEAR = '{$histData["YEAR"]}' ";
        $where .= "     and SEMESTER = '{$histData["SEMEST"]}' ";

        $query = Query::updateSQL($data, "SCHREG_REGD_DAT", $where);

        return $query;
    }

    //第二国籍
    public function delDetail011($model)
    {
        $query  = " DELETE FROM ";
        $query .= "     SCHREG_BASE_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND BASE_SEQ = '011' ";

        return $query;
    }

    //第二国籍
    public function insDetail011($model)
    {
        $data = array();
        $data["SCHREGNO"][TEXT]     = $model->schregno;
        $data["BASE_SEQ"][TEXT]     = "011";
        $data["BASE_REMARK1"][TEXT] = $model->detail011["NATIONALITY2"];
        $data["BASE_REMARK2"][TEXT] = $model->detail011["NAME"];
        $data["BASE_REMARK3"][TEXT] = $model->detail011["NAME_KANA"];
        $data["BASE_REMARK4"][TEXT] = $model->detail011["NAME_ENG"];
        $data["BASE_REMARK5"][TEXT] = $model->detail011["REAL_NAME"];
        $data["BASE_REMARK6"][TEXT] = $model->detail011["REAL_NAME_KANA"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "SCHREG_BASE_DETAIL_MST");
        return $query;
    }

    //学校名取得
    public function getSchoolName()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }
    //入学卒業日取得
    public function getEntGrdDate($model)
    {
        $query .= " SELECT ";
        $query .= "     ENT_DATE, ";
        $query .= "     GRD_DATE, ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO IN ('".str_replace(',', "','", $model->replace_data["selectdata"])."')";

        return $query;
    }
}
