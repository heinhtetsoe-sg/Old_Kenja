<?php

require_once('for_php7.php');

class knjb060aQuery extends Query {

    //学校区分取得(単位制の場合、学級別は出力不可とする)---2005.05.19
    function getSchooldiv($model)
    {
        $db = Query::dbCheckOut();
        $query = "SELECT SCHOOLDIV FROM SCHOOL_MST WHERE YEAR='".$model->control["年度"]."'";
        $ret_val = $db->getOne($query);
        Query::dbCheckIn($db);
        return $ret_val;
    }
    //時間割パターンヘッダクエリ
    function getBscHdQuery($model)
    {
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
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
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
*/
    }

    //所属クエリ
    function getSectQuery($year)
    {
		//add 04/04/21  yamauchi
        $query  = "SELECT ";
        $query .= " T1.SECTIONCD AS VALUE, ";
        $query .= " T1.SECTIONCD || '　' || T1.SECTIONNAME AS LABEL ";
        $query .= "FROM ";
        $query .= " SECTION_MST T1 ";
        $query .= " INNER JOIN  SECTION_YDAT T2 ON T1.SECTIONCD = T2.SECTIONCD ";
        $query .= "WHERE T2.YEAR = '" .$year ."' ";
        $query .= "ORDER BY T1.SECTIONCD";

/*
        $query  = "SELECT ";
        $query .= " SECTIONCD AS VALUE, ";
        $query .= " SECTIONCD || '　' || SECTIONNAME AS LABEL ";
        $query .= "FROM ";
        $query .= " SECTION_MST ";
        $query .= "ORDER BY SECTIONCD";
*/
        $db = Query::dbCheckOut();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        return $row1;
    }
    //年組取得
    function getHrclass($YEAR,$SEMESTER)
    {
        $db = Query::dbCheckOut();
        $query = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ".
                    "FROM SCHREG_REGD_HDAT ".
                    "WHERE YEAR='" .$YEAR ."' AND SEMESTER= '" .$SEMESTER ."'";
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            $row1[]= array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        $result->free();
        Query::dbCheckIn($db);
        return $row1;
    }

}
?>
