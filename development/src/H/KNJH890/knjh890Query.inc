<?php
class knjh890Query extends Query {
    
    //学年コンボ用
    function getGrade($model, $ghr)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     t1.GRADE, ";
        if($ghr != "1"){
            $query .= "     t1.HR_CLASS, ";
            $query .= "     to_single_byte(t1.HR_NAME) as HR_NAME ";
        }else{
            $query .= "     t2.GRADE_NAME1 as GRADE_NAME ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT t1  ";
        $query .= "     left join SCHREG_REGD_GDAT t2 on t1.YEAR = t2.YEAR and t1.GRADE = t2.GRADE ";
        $query .= " WHERE ";
        $query .= "     t1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     t1.SEMESTER = (SELECT ";
        $query .= "                         MAX(SEMESTER) ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_REGD_HDAT ";
        $query .= "                     WHERE ";
        $query .= "                         YEAR = '".CTRL_YEAR."' ";
        $query .= "                     )  ";
        if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            $query .= " AND ";
            $query .= "     t2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     t1.GRADE ";
        if($ghr != "1"){
            $query .= "     ,t1.HR_CLASS ";
        }
        
        return $query;
    }
    
    //生徒表示データ取得
    function getSchregData($field=array(), $data="")
    {
        if(!empty($field)){
            if($field["G_HR"] == "2"){
                $g_hr = explode("-", $field["GHR_CHOICE"]);
                $grade = $g_hr[0];
                $hrclass = $g_hr[1];
            }else{
                $grade = $field["GHR_CHOICE"];
                $hrclass = "";
            }
        }
        
        $query .= "     SELECT ";
        $query .= "         t1.SCHREGNO, ";
        $query .= "         t1.NAME, ";
        $query .= "         t6.GRADE_NAME as GRADE, ";
        $query .= "         t6.HR_CLASS_NAME as HR_CLASS, ";
        $query .= "         t2.ATTENDNO, ";
        $query .= "         t3.* ";
        $query .= "     FROM ";
        $query .= "         SCHREG_BASE_MST t1  ";
        $query .= "         left join (SELECT ";
        $query .= "                         * ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_REGD_DAT ";
        $query .= "                     WHERE ";
        $query .= "                         (SEMESTER, YEAR) in (SELECT ";
        $query .= "                                                 MAX(SEMESTER), ";
        $query .= "                                                 YEAR ";
        $query .= "                                             FROM ";
        $query .= "                                                 SCHREG_REGD_DAT ";
        $query .= "                                             WHERE ";
        $query .= "                                                 YEAR = '".CTRL_YEAR."' ";
        $query .= "                                             GROUP BY ";
        $query .= "                                                 YEAR ";
        $query .= "                                             ) ";
        $query .= "                     ) t2 on t1.SCHREGNO = t2.SCHREGNO ";
        $query .= "         left join PV_SCHREG_MST t4 on t1.SCHREGNO = t4.SCHREGNO ";      //出力するときに必要
        $query .= "         left join PV_CBT_USER_DAT t3 on t4.KNJID = t3.KNJID ";          //出力するときに必要
        $query .= "         left join (SELECT ";
        $query .= "                         YEAR, ";
        $query .= "                         SEMESTER, ";
        $query .= "                         GRADE, ";
        $query .= "                         HR_CLASS, ";
        $query .= "                         to_single_byte(GRADE_NAME) as GRADE_NAME, ";
        $query .= "                         to_single_byte(HR_CLASS_NAME1) as HR_CLASS_NAME ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_REGD_HDAT ";
        $query .= "                     WHERE ";
        $query .= "                         YEAR = '".CTRL_YEAR."' ";
        $query .= "                     ) t6 on t2.YEAR = t6.YEAR and t2.GRADE = t6.GRADE and t2.HR_CLASS = t6.HR_CLASS and t2.SEMESTER = t6.SEMESTER ";
        if($data != ""){
            $query .= "     WHERE  ";
            $query .= "         t1.SCHREGNO IN ('".$data."')  ";
        }else{
            $query .= "     WHERE  ";
            $query .= "         t2.GRADE = '".$grade."'  ";
            if($hrclass != ""){
                $query .= "     AND  ";
                $query .= "         t2.HR_CLASS = '".$hrclass."' ";
            }
        }
        $query .= "     ORDER BY ";
        $query .= "         t2.GRADE, ";
        $query .= "         t2.HR_CLASS, ";
        $query .= "         t2.ATTENDNO ";
        
        return $query;
    }
    
    //職員対象データ
    function getStaffData($field=array(), $data="")
    {
        $query .= "     SELECT ";
        $query .= "         t1.STAFFCD, ";
        $query .= "         t1.STAFFNAME, ";
        $query .= "         t3.* ";
        $query .= "     FROM ";
        $query .= "         STAFF_MST t1  ";
        $query .= "         left join PV_STAFF_MST t2 on t1.STAFFCD = t2.STAFFCD   ";
        $query .= "         left join PV_CBT_USER_DAT t3 on t2.KNJID = t3.KNJID ";
        if($data != ""){
            $query .= "     WHERE  ";
            $query .= "         t1.STAFFCD IN ('".$data."')  ";
        }
        $query .= "     ORDER BY ";
        $query .= "         KNJID, ";
        $query .= "         STAFFCD ";
        
        return $query;
    }
    
    //入力された会員情報からデータを取得する
    function getMemberData($kubun, $schreg_staff, $csvData)
    {
        $and = "";
        
        if($kubun == "1"){
            $table = "PV_STAFF_MST";
            $fieldName = "STAFFCD";
        }else{
            $table = "PV_SCHREG_MST";
            $fieldName = "SCHREGNO";
        }
        
        $query  = " SELECT ";
        $query .= "     t1.*, ";
        $query .= "     t2.KNJCNT, ";
        $query .= "     t3.LOGINCNT, ";
        $query .= "     t4.SCHREGCNT ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         '".$schreg_staff."' as SCHREGNO, ";
        $query .= "         a1.* ";
        $query .= "     FROM ";
        $query .= "         PV_CBT_USER_DAT a1 ";
        $query .= "     ) t1  ";
        $query .= "     left join (SELECT ";
        $query .= "                     KNJID, ";
        $query .= "                     COUNT(*) as KNJCNT ";
        $query .= "                 FROM ";
        $query .= "                     ".$table." ";
        $query .= "                 GROUP BY ";
        $query .= "                     KNJID ";
        $query .= "                 ) t2 on t1.KNJID = t2.KNJID  ";
        $query .= "     left join (SELECT ";
        $query .= "                     b2.LOGINID, ";
        $query .= "                     COUNT(*) as LOGINCNT ";
        $query .= "                 FROM ";
        $query .= "                     ".$table." b1  ";
        $query .= "                     left join  (SELECT ";
        $query .= "                                     LOGINID, ";
        $query .= "                                     KNJID ";
        $query .= "                                 FROM ";
        $query .= "                                     PV_CBT_USER_DAT ";
        $query .= "                                 ) b2 on b1.KNJID = b2.KNJID ";
        $query .= "                 GROUP BY ";
        $query .= "                     b2.LOGINID ";
        $query .= "                 ) t3 on t1.LOGINID = t3.LOGINID  ";
        $query .= "     left join (SELECT ";
        $query .= "                     ".$fieldName." as SCHREGNO, ";
        $query .= "                     COUNT(*) as SCHREGCNT ";
        $query .= "                 FROM ";
        $query .= "                     ".$table." ";
        $query .= "                 GROUP BY ";
        $query .= "                     ".$fieldName." ";
        $query .= "                 ) t4 on t1.SCHREGNO = t4.SCHREGNO ";
        $query .= " WHERE ";
        if($csvData["KNJID"] != ""){
            $query .= "     t1.KNJID = '".$csvData["KNJID"]."' ";
            $and = " AND ";
        }
        if($csvData["LOGINID"] != ""){
            $query .= $and;
            $query .= "     t1.LOGINID = '".$csvData["LOGINID"]."' ";
        }
        
        return $query;
    }
    
    //割り当てるKNJID
    function getKnjid($field)
    {
        $query  = " SELECT ";
        $query .= "     t1.* ";
        $query .= " FROM ";
        $query .= "     PV_CBT_USER_DAT t1  ";
        if($field["KUBUN"] == "1"){
            $query .= "     left join PV_STAFF_MST t2 on t1.KNJID = t2.KNJID ";
        }else{
            $query .= "     left join PV_SCHREG_MST t2 on t1.KNJID = t2.KNJID ";
        }
        $query .= " WHERE  ";
        $query .= "     t1.USER_TYPE = '".$field["KUBUN"]."' ";
        $query .= " AND ";
        $query .= "     t2.KNJID IS NULL ";
        $query .= " AND ";
        $query .= "     t1.USEFLG = 'T' ";
        $query .= " ORDER BY ";
        $query .= "     int(t1.KNJID) ";
        
        return $query;
    }

}
?>
