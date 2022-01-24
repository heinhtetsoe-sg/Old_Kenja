<?php

require_once('for_php7.php');

class knjc033kquery extends Query {
    // 生徒の名前取得
    function getStudentName($no)
    {
        $query  = " SELECT ";
        $query .= "     name_show ";
        $query .= " FROM ";
        $query .= "     schreg_base_mst";
        $query .= " WHERE ";
        $query .= "     schregno = '$no' ";
        
        $db = Query::dbCheckOut();
        $row = $db->getOne($query);
        Query::dbCheckIn($db);
        
        return $row;
    }
    
    // 生徒の詳細欠時取得
    function selectStudent($no, $cd)
    {
        /* 2004/08/30 arakaki
        $query  = " SELECT ";
        $query .= "     attenddate, periodcd ";
        $query .= " FROM ";
        $query .= "     attend_dat";
        $query .= " WHERE ";
        $query .= "     schregno = '$no' ";
        $query .= "     AND chaircd = '$cd' ";
        $query .= " ORDER BY ";
        $query .= "     attenddate ";
        */

        $query  = " SELECT AD.ATTENDDATE,AD.PERIODCD,VNM.NAME1 ";
        $query .= "   FROM ATTEND_DAT AS AD ";
        $query .= "       LEFT OUTER JOIN ( SELECT NAMECD2,NAME1 ";
        $query .= "                           FROM V_NAME_MST ";
        $query .= "                          WHERE YEAR = '".CTRL_YEAR."'  ";
        $query .= "                            AND NAMECD1='B001') VNM ON VNM.NAMECD2=AD.PERIODCD,  ";
        $query .= "       (SELECT SDATE,EDATE FROM SEMESTER_MST WHERE YEAR ='".CTRL_YEAR."' AND SEMESTER = '9') AS SM ";
        $query .= " WHERE AD.SCHREGNO='".$no."'  ";
        $query .= "   AND AD.ATTENDDATE BETWEEN SM.SDATE AND SM.EDATE ";
        $query .= "   AND AD.CHAIRCD = '".$cd."' ";
        $query .= "ORDER BY AD.ATTENDDATE ";

        //echo $query;
        return $query;
    }
    
    //科目データの取得
    function selectSubclassQuery(){

        $query  = "   SELECT ";
        $query .= "       t3.SUBCLASSCD, ";
        $query .= "       t3.SUBCLASSABBV ";
        $query .= "   FROM ";
        $query .= "       CHAIR_DAT t1, ";
        $query .= "       CHAIR_STF_DAT t2, ";
        $query .= "       SUBCLASS_MST t3 ";
        $query .= "   WHERE ";
        $query .= "       t1.YEAR = '".CTRL_YEAR."' ";
        $query .= "       AND t1.YEAR = t2.YEAR ";
        $query .= "       AND t1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "       AND t1.SEMESTER = t2.SEMESTER ";
        $query .= "       AND t1.CHAIRCD = t2.CHAIRCD ";
        //更新可能(制限付)
        if(AUTHORITY != DEF_UPDATABLE){
            $query .= "         AND t2.STAFFCD = '".STAFFCD."' ";
        }
        $query .= "       AND t1.SUBCLASSCD = t3.SUBCLASSCD ";
        $query .= "   GROUP BY ";
        $query .= "       t3.SUBCLASSCD, ";
        $query .= "       t3.SUBCLASSABBV ";
        $query .= "   ORDER BY ";
        $query .= "       t3.SUBCLASSCD ";
        return $query;
    }
    
    //講座データの取得
    function selectChairQuery(&$model)
    {
        $query  = "   SELECT DISTINCT";
        $query .= "       t1.CHAIRCD, ";
        $query .= "       t1.CHAIRNAME ";
        $query .= "   FROM ";
        $query .= "       CHAIR_DAT t1, ";
        $query .= "       CHAIR_STF_DAT t2 ";
        $query .= "   WHERE ";
        $query .= "       t1.YEAR = '".CTRL_YEAR."' ";
        $query .= "       AND t1.YEAR = t2.YEAR ";
        $query .= "       AND t1.SEMESTER = t2.SEMESTER ";
        $query .= "       AND t1.CHAIRCD = t2.CHAIRCD ";
        //更新可能(制限付)
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= "         AND t2.STAFFCD = '".STAFFCD."' ";
        }
        $query .= "       AND t1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        $query .= "   ORDER BY ";
        $query .= "       t1.CHAIRCD ";
        return $query;
    }
    
    // 月データの取得
    function selectMonthQuery()
    {

        /* 2004/08/27 arakaki
        $query  = " SELECT DISTINCT ";
        $query .= "     t1.namecd2, t1.name1, t1.namespare1 ";
        $query .= " FROM ";
        $query .= "     name_mst t1, admin_control_dat t2 ";
        $query .= " WHERE ";
        $query .= "     t1.namecd1 = 'Z005' AND ";
        $query .= "     t2.control_flg = '2' AND ";
        $query .= "     EXISTS ";
        $query .= "         (SELECT ";
        $query .= "             'X' ";
        $query .= "         FROM ";
        $query .= "             admin_control_dat t3 ";
        $query .= "         WHERE ";
        $query .= "             t1.namecd2 = t3.control_code ";
        $query .= "         ) ";
        $query .= " ORDER BY ";
        $query .= "     t1.namespare1 ";
        */

        $query  = " SELECT DISTINCT ";
        $query .= "     namecd2, name1, namespare1 ";
        $query .= " FROM ";
        $query .= "     v_name_mst ";
        $query .= " WHERE ";
        $query .= "     year  = '".CTRL_YEAR."' and ";
        $query .= "     namecd1 = 'Z005' and ";
        $query .= "     namecd2 IN (SELECT CONTROL_CODE FROM ADMIN_CONTROL_DAT WHERE YEAR='".CTRL_YEAR."' AND CONTROL_FLG ='2') ";
        $query .= " ORDER BY ";
        $query .= "     namespare1 ";
        
        return $query;
    }
    
    // 生徒情報リスト取得
    function selectQuery(&$model)
    {
        $query  = " SELECT ";
        $query .= "     w1.name_show, w1.hr_nameabbv, w1.attendno, w1.schregno ";
        for($i = 1; $i <= 12; $i++)
        {
            $tmp = sprintf("%02d", $i);
            $query .= " ,w".$tmp.".absent AS month".$tmp." ";
        }
        $query .= " , ";
        for($i = 1; $i <= 12; $i++)
        {
            $tmp = sprintf("%02d", $i);
            $query .= " COALESCE(w".$tmp.".absent,0) ";
            if ($i == 12) {
                $query .= " as total ";
            } else {
                $query .= " + ";
            }
        }
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "        t1.schregno, t1.name_show, t3.hr_nameabbv, t3.grade, t3.hr_class, t4.attendno ";
        $query .= "     FROM ";
        $query .= "         schreg_base_mst t1, chair_std_dat t2, schreg_regd_hdat t3, schreg_regd_dat t4 ";
        $query .= "     WHERE ";
        $query .= "         t1.schregno = t2.schregno ";
        $query .= "         AND t1.schregno = t4.schregno ";
        $query .= "         AND t2.year = '".CTRL_YEAR."' ";
        $query .= "         AND t2.year = t3.year ";
        $query .= "         AND t2.year = t4.year ";
        $query .= "         AND t2.chaircd = '".$model->field["CHAIRCD"]."' ";
        $query .= "         AND t3.grade = t4.grade ";
        $query .= "         AND t3.hr_class = t4.hr_class ";
        $query .= "         AND t3.semester = '".CTRL_SEMESTER."' ";
        $query .= "         AND t3.semester = t4.semester ";
        $query .= "     GROUP BY ";
        $query .= "         t1.schregno, t1.name_show, t3.hr_nameabbv, t3.grade, t3.hr_class, t4.attendno ";
        $query .= "     ORDER BY ";
        $query .= "         t3.grade, t3.hr_class, t4.attendno ";
        $query .= "     )w1 ";
        
        for($i = 1; $i <= 12; $i++)
        {
            $tmp = sprintf("%02d", $i);
            
            $query .= " LEFT OUTER JOIN ";
            $query .= "     (SELECT ";
            $query .= "         year, month, schregno, classcd, subclasscd, absent ";
            $query .= "     FROM ";
            $query .= "         attend_subclass_dat ";
            $query .= "     WHERE ";
            $query .= "         copycd = '0' ";
            $query .= "         AND year = '".CTRL_YEAR."' ";
            $query .= "         AND month = '".$tmp."' ";
            $query .= "         AND classcd = '".substr($model->field["SUBCLASSCD"], 0, 2)."' ";
            $query .= "         AND subclasscd = '".$model->field["SUBCLASSCD"]."' ";
            $query .= "     )w".$tmp." ";
            $query .= "     ON ";
            $query .= "         w1.schregno = w".$tmp.".schregno ";
        }
        
        //echo $query;
        return $query;
    }
    
    /* データ更新処理 */
    function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        
        for($i = 1; $i <= $model->field["std_num"]; $i++)
        {
            $tmp  = "SCHREGNO".sprintf("%02d", $i);
            $temp = "ABSENT".sprintf("%02d", $i);
            
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     attend_subclass_dat ";
            $query .= " WHERE COPYCD ='0' ";
            $query .= "   AND year = '".CTRL_YEAR."' ";
            $query .= "   AND month = '".$model->field["MONTHCD"]."' ";
            $query .= "   AND schregno = '".$model->field[$tmp]."' ";
            $query .= "   AND classcd = '".substr($model->field["SUBCLASSCD"], 0, 2)."' ";
            $query .= "   AND subclasscd = '".$model->field["SUBCLASSCD"]."' ";
            //echo $query;
            $flag = $db->getOne($query);
            $data = array();
            
            if ($flag) {
                $data["ABSENT"][NUMBER] = $model->field[$temp];
                
                $where  = " WHERE COPYCD ='0' ";
                $where .= "   AND year = '".CTRL_YEAR."' ";
                $where .= "   AND month = '".$model->field["MONTHCD"]."' ";
                $where .= "   AND schregno = '".$model->field[$tmp]."' ";
                $where .= "   AND classcd = '".substr($model->field["SUBCLASSCD"], 0, 2)."' ";
                $where .= "   AND subclasscd = '".$model->field["SUBCLASSCD"]."' ";
                
                $query = Query::updateSQL($data, attend_subclass_dat, $where);
            } else {
                $data["COPYCD"][TEXT]       = "0";
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                $data["MONTH"][TEXT]        = $model->field["MONTHCD"];
                $data["SCHREGNO"][TEXT]     = $model->field[$tmp];
                $data["SEMESTER"][FUNC]     = "TERM_GET('".CTRL_YEAR."', '".$model->field["MONTHCD"]."', 'N')";
                $data["CLASSCD"][TEXT]      = substr($model->field["SUBCLASSCD"], 0, 2);
                $data["SUBCLASSCD"][TEXT]   = $model->field["SUBCLASSCD"];
                $data["ABSENT"][NUMBER]     = $model->field[$temp];
                
                $query = Query::insertSQL($data, attend_subclass_dat);
            }
            $db->query($query);
        }
        Query::dbCheckIn($db);
    }
}
?>
