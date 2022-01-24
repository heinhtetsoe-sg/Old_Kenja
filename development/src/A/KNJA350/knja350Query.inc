<?php

require_once('for_php7.php');

class knja350Query extends Query {

    function getSql($db, $model, $tabname, $opt) {
        $thislas = " (T1.YEAR = '".$model->this_year."' AND T1.GRADE = '08' OR T1.YEAR = '".$model->last_year."' AND T1.GRADE = '07')";
        $schregnoClaFo09 = " T1.SCHREGNO IN (SELECT SCHREGNO FROM FED_CLASS_FORMATION_DAT WHERE YEAR = '".$model->next_year."' AND SEMESTER = '1' AND GRADE = '09') ";

        $query = "";
        if (in_array($tabname, array(
                "SCHREG_REGD_HDAT"
              , "STAFF_CLASS_HIST_DAT"
            ))) {
            $query .= knja350Query::selectFed($db, $tabname, $thislas, $opt);

        } else if (in_array($tabname, array(
                "SCHREG_BASE_MST"
          ))) {
            $query .= knja350Query::selectFed($db, $tabname, $schregnoClaFo09, $opt);

        } else if (in_array($tabname, array(
                "CLASS_FORMATION_DAT"
          ))) {
            $whereClafo  = "";
            $whereClafo .= "     (T1.SCHREGNO, T1.YEAR, T1.SEMESTER) IN     (SELECT SCHREGNO, YEAR, SEMESTER FROM FED_CLASS_FORMATION_DAT WHERE YEAR = '".$model->next_year."' AND SEMESTER = '1' AND GRADE = '09') ";
            $query .= knja350Query::selectFed($db, "CLASS_FORMATION_DAT", $whereClafo, $opt);

        } else if (in_array($tabname, array(
                "SCHREG_REGD_DAT"
          ))) {
            $opt1 = $opt;
            if ($opt1["cols"] == "") {
                $opt1["cols"] = ["SCHREGNO", "YEAR", "SEMESTER", "GRADE", "HR_CLASS", "ATTENDNO", "COURSECD", "MAJORCD", "COURSECODE", "REGISTERCD", "UPDATED"];
            }
            $whereClafo  = "";
            $whereClafo .= "     (T1.SCHREGNO, T1.YEAR, T1.SEMESTER) IN     (SELECT SCHREGNO, YEAR, SEMESTER FROM FED_CLASS_FORMATION_DAT WHERE YEAR = '".$model->next_year."' AND SEMESTER = '1' AND GRADE = '09') ";
            $whereClafo .= " AND (T1.SCHREGNO, T1.YEAR, T1.SEMESTER) NOT IN (SELECT SCHREGNO, YEAR, SEMESTER FROM SCHREG_REGD_DAT         WHERE YEAR = '".$model->next_year."' AND SEMESTER = '1' AND GRADE = '09') ";
            $query .= knja350Query::selectFed($db, "CLASS_FORMATION_DAT", $whereClafo, $opt1);
            $query .= " UNION ALL ";
            $query .= knja350Query::selectFed($db, "SCHREG_REGD_DAT", $schregnoClaFo09." AND ".$thislas, $opt);

        } else if (in_array($tabname, array(
                "SCHREG_BASE_DETAIL_MST"
              , "SCHREG_BASE_HIST_DAT"
              , "SCHREG_TRANSFER_DAT"
              , "SCHREG_ADDRESS_DAT"
              , "GUARDIAN_DAT"
              , "GUARDIAN_ADDRESS_DAT"
              , "GUARDIAN_HIST_DAT"
              , "GUARDIAN2_DAT"
              , "GUARDIAN2_ADDRESS_DAT"
              , "GUARDIAN2_HIST_DAT"
              , "GUARANTOR_ADDRESS_DAT"
              , "GUARANTOR_HIST_DAT"
              , "SCHREG_SEND_ADDRESS_DAT"
              , "SCHREG_RELA_DAT"
              , "SCHREG_ENVIR_DAT"
          ))) {
            $query .= knja350Query::selectFed($db, $tabname, $schregnoClaFo09, $opt);

        } else if (in_array($tabname, array(
                "SCHREG_ENT_GRD_HIST_DAT"
          ))) {
            $where  = $schregnoClaFo09;
            $where .= " AND SCHOOL_KIND = 'J' ";

            $query .= knja350Query::selectFed($db, $tabname, $where, $opt);

        } else if (in_array($tabname, array(
                "STAFF_MST"
              , "STAFF_NAME_HIST_DAT"
              , "STAFF_ADDRESS_DAT"
          ))) {
            $where = " T1.STAFFCD IN (SELECT STAFFCD FROM FED_STAFF_CLASS_HIST_DAT T1 WHERE ".$thislas.") ";

            $query .= knja350Query::selectFed($db, $tabname, $where, $opt);

        } else if (in_array($tabname, array(
                "STAFF_YDAT"
              , "STAFF_DETAIL_MST"
          ))) {
            $where = " (T1.YEAR, T1.STAFFCD) IN (SELECT YEAR, STAFFCD FROM FED_STAFF_CLASS_HIST_DAT T1 WHERE ".$thislas.") ";

            $query .= knja350Query::selectFed($db, $tabname, $where, $opt);

        } else 
          // 2
        if (in_array($tabname, array(
                "SCHREG_STUDYREC_DAT"
              , "STUDYRECREMARK_DAT"
              , "JVIEWSTAT_SUB_DAT"
              , "RECORD_SCORE_DAT"
              , "SCHREG_CLUB_HIST_DAT"
              , "SCHREG_CLUB_HDETAIL_DAT"
          ))) {
              $where = "";
              $where .= $schregnoClaFo09;
              $where .= " AND T1.SCHOOL_KIND = 'J' ";
              $query .= knja350Query::selectFed($db, $tabname, $where, $opt);
        } else if (in_array($tabname, array(
                "SCHREG_ATTENDREC_DAT"
              , "ATTEND_SEMES_DAT"
              , "ATTEND_SEMES_REMARK_DAT"
              , "HTRAINREMARK_DETAIL2_DAT"
              , "MEDEXAM_HDAT"
              , "MEDEXAM_DET_DAT"
              , "MEDEXAM_DET_DETAIL_DAT"
              , "MEDEXAM_TOOTH_DAT"
              , "MEDEXAM_TOOTH_DETAIL_DAT"
          ))) {
              $where = "";
              $where .= $schregnoClaFo09;
              $where .= " AND (T1.SCHREGNO, T1.YEAR) IN (SELECT SCHREGNO, YEAR FROM SCHREG_REGD_DAT WHERE GRADE IN ('08', '07')) ";
              $query .= knja350Query::selectFed($db, $tabname, $where, $opt);
        } else if (in_array($tabname, array(
                "HTRAINREMARK_DAT"
              , "BEHAVIOR_DAT"
          ))) {
              $where = "";
              $where .= $schregnoClaFo09;
              $where .= " AND (T1.SCHREGNO, T1.YEAR, T1.ANNUAL) IN (SELECT SCHREGNO, YEAR, GRADE FROM SCHREG_REGD_DAT WHERE GRADE IN ('08', '07')) ";
              $query .= knja350Query::selectFed($db, $tabname, $where, $opt);
        } else if (in_array($tabname, array(
                "SCHREG_QUALIFIED_HOBBY_DAT"
          ))) {
              $where = "";
              $where .= $schregnoClaFo09;
              $where .= " AND (T1.SCHREGNO, T1.YEAR) IN (SELECT SCHREGNO, YEAR FROM SCHREG_REGD_DAT WHERE GRADE IN ('08', '07')) ";
              $where .= " AND T1.SCHOOL_KIND = 'J' ";
              $query .= knja350Query::selectFed($db, $tabname, $where, $opt);
        } else if (in_array($tabname, array(
                "SCHREG_COMMITTEE_HIST_DAT"
              , "SCHREG_COMMITTEE_HDETAIL_DAT"
          ))) {
              $where = "";
              $where .= $schregnoClaFo09;
              $where .= " AND (T1.SCHREGNO, T1.YEAR) IN (SELECT SCHREGNO, YEAR FROM SCHREG_REGD_DAT WHERE GRADE IN ('08', '07')) ";
              $where .= " AND T1.SCHOOL_KIND = 'J' ";
              $query .= knja350Query::selectFed($db, $tabname, $where, $opt);
        }

        return $query;
    }

    function getInsertIntoSql($db, $model, $tabname, $opt) {

        $ins = "";
        $ins .= " INSERT INTO ".$tabname;
        if ($opt["cols"]) {
            $ins .= "(".implode(", ", $opt["cols"]).")";
        }
        $ins .= knja350Query::getSql($db, $model, $tabname, $opt);
        return $ins;
    }

    function selectFed($db, $tabname, $where, $opt) {
        $query  = "";
        $query .= " SELECT ";
        if ($opt["cols"]) {
            $comma = "";
            foreach ($opt["cols"] as $colname) {
                if ($colname == "REGISTERCD") {
                    $colname = "'".STAFFCD."' AS REGISTERCD";
                } else if ($colname == "UPDATED") {
                    $colname = "CURRENT TIMESTAMP AS UPDATED";
                }
                $query .= $comma. " ".$colname." ";
                $comma = " , ";
            }

        } else {
            $cols = knja350Query::tableColumns($db, "FED_".$tabname, true);
            $query .= implode(", ", $cols);
        }
        $query .= " FROM "."FED_".$tabname." T1 ";
        $query .= " WHERE ".$where;
        if ($opt["collision"] || $opt["noCollision"]) {
            $pkcols = knja350Query::tablePkColumns($db, $tabname);

            if ($opt["collision"]) {
                $query .=" AND     EXISTS ";
            } else {
                $query .=" AND NOT EXISTS ";
            }
            $query .=" (SELECT 'X' FROM ".$tabname." WHERE ";
            $and = "";
            foreach ($pkcols as $col) {
                if ($col) {
                    $query .= $and . " T1.".$col." = ".$col." ";
                    $and = " AND ";
                }
            }
            $query .=" ) ";
        }
        return $query;
    }

    //学年名称
    function getGradeName($model) {
        $query  = "";
        $query .= " SELECT  G1.GRADE_NAME1 ";
        $query .= " FROM    SCHREG_REGD_GDAT G1 ";
        $query .= " WHERE   G1.YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND   G1.GRADE = '09' ";
        return $query;
    }

    //対象生徒数
    function getTargetCount($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     FED_CLASS_FORMATION_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->next_year."' ";
        $query .= " AND SEMESTER = '1' ";
        $query .= " AND GRADE = '09' ";

        return $query;
    }

    //テーブルのカラム
    function tableColumns($db, $tabname, $data=false) {
        $query  = "SELECT ";
        $query .= "    COLNAME ";
        $query .= "FROM ";
        $query .= "    SYSCAT.COLUMNS T1 ";
        $query .= "WHERE ";
        $query .= "     T1.TABNAME = '".$tabname."' ";
        $query .= "ORDER BY ";
        $query .= "     T1.COLNO ";

        $colnames = [];
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $colname = $row["COLNAME"];
            if ($data) {
                if ($colname == "REGISTERCD") {
                    $colname = "'".STAFFCD."' AS REGISTERCD";
                } else if ($colname == "UPDATED") {
                    $colname = "CURRENT TIMESTAMP AS UPDATED";
                }
            }
            $colnames[] = $colname;
        }
        $result->free();

        return $colnames;
    }


    //テーブルのプライマリキーのカラム
    function tablePkColumns($db, $tabname) {
        $query  = "SELECT ";
        $query .= "    COLNAMES ";
        $query .= "FROM ";
        $query .= "    SYSCAT.INDEXES T1 ";
        $query .= "WHERE ";
        $query .= "     T1.TABNAME = '".$tabname."' ";
        $query .= "     AND T1.UNIQUERULE = 'P' ";

        $colnames = explode('+', $db->getOne($query));

        return $colnames;
    }

}
?>
