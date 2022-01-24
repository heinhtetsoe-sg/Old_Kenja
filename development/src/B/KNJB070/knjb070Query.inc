<?php

require_once('for_php7.php');

class knjb070Query extends Query {

    //時間割パターンヘッダクエリ
    function getBscHdQuery($model) {
        $query  ="SELECT ";
        $query .="    t1.year, ";
        $query .="    t1.semester, ";
        $query .="    t2.semestername, ";
        $query .="    t1.bscseq, ";
        $query .="    t1.title ";
        $query .="FROM ";
        $query .="    sch_ptrn_hdat t1, ";
        $query .="    semester_mst t2 ";
        $query .="WHERE ";
        $query .="    t1.year='" .$model->control["年度"] ."' AND ";
        $query .="    t1.year=t2.year AND ";
        $query .="    t1.semester=t2.semester ";
        $query .="ORDER BY ";
        $query .="    t1.semester, ";
        $query .="    t1.bscseq ";

        $db = Query::dbCheckOut();
        $row1[]= array('label' => "", 'value' => "");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row1[]= array('label' => $row["SEMESTERNAME"]." Seq".$row["BSCSEQ"].":".$row["TITLE"],
                           'value' => $row["YEAR"].",".$row["BSCSEQ"].",".$row["SEMESTER"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        return $row1;
/*
        $query  = "SELECT ";
        $query .= " YEAR || ',' || char(BSCSEQ) AS VALUE, ";
        $query .= " YEAR || '年 Seq' || char(BSCSEQ) || ':' || TITLE AS LABEL ";
        $query .= "FROM ";
        $query .= " SCH_PTRN_HDAT ";
        $query .= "WHERE ";
        $query .= " YEAR = '" .$model->control["年度"] ."' ";
        $query .= "ORDER BY YEAR,BSCSEQ";

        $db = Query::dbCheckOut();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        return $row1;
*/
    }

    //学年取得
    function getSelectGrade($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     REGD_G.GRADE AS VALUE, ";
        $query .= "     REGD_G.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT REGD_H ";
        $query .= " INNER JOIN SCHREG_REGD_GDAT REGD_G ON REGD_G.YEAR  = REGD_H.YEAR ";
        $query .= "                                   AND REGD_G.GRADE = REGD_H.GRADE ";
        $query .= " WHERE ";
        $query .= "     REGD_H.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND REGD_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //所属クエリ
    function getSection() {
        $query  = " SELECT ";
        $query .= "     T1.SECTIONCD AS VALUE, ";
        $query .= "     T1.SECTIONCD || '　' || T1.SECTIONNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SECTION_MST T1 ";
        $query .= " INNER JOIN  SECTION_YDAT T2 ON T1.SECTIONCD = T2.SECTIONCD ";
        $query .= " WHERE  ";
        $query .= "     T2.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SECTIONCD ";

        return $query;
    }

    //施設を取得
    function getFacility() {
        $query  = " SELECT ";
        $query .= "     FACCD AS VALUE, ";
        $query .= "     FACCD || '　' || FACILITYNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_FACILITY_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     FACCD ";

        return $query;
    }
}
?>
