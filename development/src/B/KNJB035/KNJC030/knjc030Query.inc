<?php

require_once('for_php7.php');

class knjc030Query extends Query {

    //クラスの基本データを取得
    function get_class_data($model,$sem)
    {
        $query  = " SELECT DISTINCT";
        $query .= " GRADE || HR_CLASS AS GR_CL, GRADE, HR_CLASS ,HR_NAME ";
        $query .= " FROM ";
        $query .= " SCHREG_REGD_HDAT  ";
        $query .= " WHERE YEAR ='";
        $query .= $model->cntl_dt_year;
        $query .= "' ";
        $query .= " AND SEMESTER ='";
        $query .= $sem;
        $query .= "' ";

        $query .= " ORDER BY GRADE, HR_CLASS ";
        return $query;
    }


    //スタッフの基本データを取得
    function get_staff_data($cntl_dt_year)
    {
        $query  = " SELECT ";
        $query .= "  T1.STAFFCD, ";
        $query .= "  T1.STAFFNAME_SHOW as SHOW_NAME, ";
        $query .= "  T1.SECTIONCD, ";
        $query .= "  T2.SECTIONNAME ";
        $query .= "  FROM ";
        $query .= "  V_STAFF_MST T1 LEFT JOIN SECTION_MST T2  ON T2.SECTIONCD = T1.SECTIONCD ";
        $query .= "  WHERE ";
        $query .= "  T1.YEAR ='".$cntl_dt_year."' AND ";
        $query .= "  T1.CHARGECLASSCD = '1' ";
        $query .= "  ORDER BY ";
        $query .= "  T1.SECTIONCD ";

        //echo $query;

        return $query;
    }

    //科目の基本データを取得
    function find_class()
    {
        $query = "SELECT SECTIONCD, SECTIONABBV FROM SECTION_MST ";
        return $query;
    }

    //学期取得
    function getTerm($year, $day){
        return " VALUES TERM_GET('".$year."','".$day."','N') ";
    }

    //表示用データを取得
    function readQuery($model,$semester)
    {
        $query = "  SELECT    ";

        $query .= "  T1.SEMESTER,   ";
        $query .= "  T1.GRADE,   ";
        $query .= "  T1.HR_CLASS,   ";
        $query .= "  T1.GRADE || T1.HR_CLASS AS TARGETCLASS,   ";
        $query .= "  T1.HR_NAME,   ";
        $query .= "  T2.CHAIRCD,   ";
        $query .= "  T3.EXECUTEDATE,   ";
        $query .= "  T3.PERIODCD,   ";
        $query .= "  T3.EXECUTED AS ATTENDCD,   ";
        $query .= "  T4.STAFFCD,   ";
        $query .= "  T6.STAFFNAME_SHOW AS NAME_SHOW ,   ";
        $query .= "  T6.SECTIONCD ,   ";
        $query .= "  T7.SUBCLASSABBV   ";

        $query .= " FROM    ";

        $query .= "  SCHREG_REGD_HDAT T1 LEFT JOIN CHAIR_CLS_DAT T2 ON  T1.YEAR     = T2.YEAR   ";
        $query .= "                                                 AND T1.GRADE    = T2.TRGTGRADE   ";
        $query .= "                                                 AND T1.HR_CLASS = T2.TRGTCLASS   ";
        $query .= "                                                 AND T1.SEMESTER = T2.SEMESTER ";

        $query .= "                     LEFT JOIN SCH_CHR_DAT T3    ON  T2.YEAR     = T3.YEAR   ";
        $query .= "                                                 AND T2.CHAIRCD  = T3.CHAIRCD   ";
        $query .= "                     LEFT JOIN CHAIR_STF_DAT T4  ON  T2.YEAR     = T4.YEAR   ";
        $query .= "                                                 AND T2.CHAIRCD  = T4.CHAIRCD   ";
        $query .= "                                                 AND T2.SEMESTER = T4.SEMESTER   ";
        $query .= "                     LEFT JOIN CHAIR_DAT T5      ON  T4.YEAR     = T5.YEAR   ";
        $query .= "                                                 AND T4.CHAIRCD  = T5.CHAIRCD   ";
        $query .= "                                                 AND T4.SEMESTER = T5.SEMESTER   ";
        $query .= "                     LEFT JOIN V_STAFF_MST T6    ON  T4.STAFFCD  = T6.STAFFCD    ";
        $query .= "                                                 AND T4.YEAR     = T6.YEAR   ";
        $query .= "                     LEFT JOIN SUBCLASS_MST T7   ON  T5.SUBCLASSCD = T7.SUBCLASSCD   ";

        $query .= " WHERE   ";
        $query .= "  T1.YEAR = '".$model->cntl_dt_year."'   ";
        $query .= "  AND T1.SEMESTER = '".$semester."'   ";
        $query .= "  AND T3.EXECUTEDATE = DATE('".$model->cntl_dt_key."')    ";
        $query .= "  ORDER BY T3.PERIODCD   ";

        return $query;
    }

    //データを移動
    function keyMoverQuery($key, $move_key = "")
    {
        switch ($move_key) {

        case "next":
            $max_min = " MIN";
            $handle = " > ";
            $handle_end = "";
            break;

        case "before":
            $max_min = " MAX";
            $handle = " < ";
            $handle_end = "";
            break;

        default:
            $max_min = " MIN";
            $handle = " BETWEEN ";
            $handle_end = " AND DATE('".(CTRL_YEAR + 1)."-3-31') ";
            break;
        }

        $db     = Query::dbCheckOut();

        $query  = "SELECT ";
        $query  .= $max_min;
        $query  .= "(EXECUTEDATE) as EXECUTEDATE  FROM ";
        $query  .= " SCH_CHR_DAT ";
        $query  .= " WHERE ";
        $query  .= " EXECUTEDATE ";
        $query  .= $handle;
        $query  .= " DATE('";
        $query  .= $key;
        $query  .= "') ";
        $query  .= $handle_end ;

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row["EXECUTEDATE"];
    }

    function get_myClass($cntl_dt_key,$staffcd)
    {
        $db     = Query::dbCheckOut();
        $cntl_dt_key = str_replace("/","-",$cntl_dt_key);

        $query  = " SELECT GRADE || HR_CLASS AS MYCLASS FROM SCHREG_REGD_HDAT ";
        $query  .= " WHERE SEMESTER = '".CTRL_SEMESTER."' ";
        $query  .= "   AND TR_CD1   = '". $staffcd ."'";
        $query  .= "   AND YEAR     = FISCALYEAR(DATE('".$cntl_dt_key."'))";

        $class = $db->getOne($query);
        Query::dbCheckIn($db);

        return $class;
    }

    function getPath(){
        return " SELECT PROGRAMPATH FROM MENU_MST WHERE PROGRAMID = 'KNJC010' ";
    }


}
?>
