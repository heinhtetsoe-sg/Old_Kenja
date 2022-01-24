<?php
class knjh880Query extends Query {
    
    //件数
    function getCnt($table, $kubun="")
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= $table;
        if($kubun != ""){
            $query .= " WHERE ";
            $query .= "     USER_TYPE = '".$kubun."' ";
        }
        
        return $query;
    }
    
    //表示データ取得
    function getData($field, $order="KNJID", $mode="")
    {
        if($mode != ""){
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ( ";
        }
        $query .= "     SELECT ";
        $query .= "         t1.*, ";
        if($field["KUBUN"] == "1"){
            $query .= "         t2.STAFFCD, ";
            $query .= "         t3.STAFFNAME ";
        }else{
            $query .= "         t2.SCHREGNO, ";
            $query .= "         t3.NAME, ";
            $query .= "         t6.GRADE_NAME as GRADE, ";
            $query .= "         t6.HR_CLASS_NAME as HR_CLASS, ";
            $query .= "         t4.ATTENDNO ";
        }
        $query .= "     FROM ";
        $query .= "         PV_CBT_USER_DAT t1 ";
        if($field["KUBUN"] == "1"){
            $query .= "         left join PV_STAFF_MST t2 on t1.KNJID = t2.KNJID  ";
            $query .= "         left join STAFF_MST t3 on t2.STAFFCD = t3.STAFFCD ";
        }else{
            $query .= "         left join PV_SCHREG_MST t2 on t1.KNJID = t2.KNJID ";
            $query .= "         left join SCHREG_BASE_MST t3 on t2.SCHREGNO = t3.SCHREGNO ";
            $query .= "         left join (SELECT ";
            $query .= "                         * ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_DAT ";
            $query .= "                     WHERE ";
            $query .= "                         (SCHREGNO, SEMESTER, YEAR) IN (SELECT ";
            $query .= "                                                             SCHREGNO, ";
            $query .= "                                                             MAX(SEMESTER), ";
            $query .= "                                                             YEAR ";
            $query .= "                                                         FROM ";
            $query .= "                                                             SCHREG_REGD_DAT ";
            $query .= "                                                         WHERE ";
            $query .= "                                                             YEAR = '".CTRL_YEAR."' ";
            $query .= "                                                         GROUP BY ";
            $query .= "                                                             SCHREGNO, ";
            $query .= "                                                             YEAR ";
            $query .= "                                                         ) ";
            $query .= "                     ) t4 on t2.SCHREGNO = t4.SCHREGNO ";
            $query .= "         left join (SELECT ";
            $query .= "                         YEAR, ";
            $query .= "                         GRADE, ";
            $query .= "                         SEMESTER, ";
            $query .= "                         HR_CLASS, ";
            $query .= "                         to_single_byte(GRADE_NAME) as GRADE_NAME, ";
            $query .= "                         to_single_byte(HR_CLASS_NAME1) as HR_CLASS_NAME ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_HDAT ";
            $query .= "                     WHERE ";
            $query .= "                         YEAR = '".CTRL_YEAR."' ";
            $query .= "                     ) t6 on t4.YEAR = t6.YEAR and t4.GRADE = t6.GRADE and t4.HR_CLASS = t6.HR_CLASS and t4.SEMESTER = t6.SEMESTER ";
        }
        $query .= "     WHERE ";
        $query .= "         USER_TYPE = '".$field["KUBUN"]."' ";
        if($field["LINK"] != ""){
            if($field["KUBUN"] == "1"){
                $query .= "     AND ";
                if($field["LINK"] != "1"){
                    $query .= "         t2.STAFFCD IS NULL ";
                }else{
                    $query .= "         t2.STAFFCD IS NOT NULL ";
                }
            }else{
                $query .= "     AND ";
                if($field["LINK"] != "1"){
                    $query .= "         t2.SCHREGNO IS NULL ";
                }else{
                    $query .= "         t2.SCHREGNO IS NOT NULL ";
                }
            }
        }
        $query .= "     ORDER BY ";
        if($order != "KNJID"){
            if($field["KUBUN"] == "1"){
                $query .= "         t2.STAFFCD, ";
            }else{
                $query .= "         t4.GRADE, ";
                $query .= "         t4.HR_CLASS, ";
                $query .= "         t4.ATTENDNO, ";
            }
        }
        $query .= "         int(KNJID) ";
        if($mode != ""){
            $query .= "     ) ";
        }

        return $query;
    }

}
?>
