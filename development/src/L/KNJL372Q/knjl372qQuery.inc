<?php

require_once('for_php7.php');

class knjl372qQuery extends Query {
    
    //���w�Z�ʏ��擾
    function getSchoolCnt()
    {
        $query  = " SELECT ";
        $query .= "    SUBSTR(a2.SCHOOLCD, 3, 5) as SCHOOLCD, ";
        $query .= "    a1.ABSCNT, ";
        $query .= "    case when a1.ABSCNT IS NOT NULL then a2.APPCNT - a1.ABSCNT ";
        $query .= "    else a2.APPCNT end as TAKECNT, ";
        $query .= "    a2.APPCNT, ";
        $query .= "    a3.FINSCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "    (SELECT ";
        $query .= "        COUNT(t1.SAT_NO) as APPCNT, ";
        $query .= "        t2.SCHOOLCD ";
        $query .= "    FROM ";
        $query .= "        SAT_EXAM_DAT t1 ";
        $query .= "        left join SAT_APP_FORM_MST t2 on t1.SAT_NO = t2.SAT_NO and t1.YEAR = t2.YEAR ";
        $query .= "    WHERE ";
        $query .= "        t1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    GROUP BY ";
        $query .= "        t2.SCHOOLCD ";
        $query .= "    ) a2  ";
        $query .= "    left join  (SELECT ";
        $query .= "                    COUNT(t1.SAT_NO) as ABSCNT, ";
        $query .= "                    t2.SCHOOLCD ";
        $query .= "                FROM ";
        $query .= "                    SAT_EXAM_DAT t1 ";
        $query .= "                left join SAT_APP_FORM_MST t2 on t1.SAT_NO = t2.SAT_NO and t1.YEAR = t2.YEAR ";
        $query .= "                WHERE ";
        $query .= "                    t1.ABSENCE = '0' ";
        $query .= "                AND ";
        $query .= "                    t1.YEAR = '".CTRL_YEAR."' ";
        $query .= "                GROUP BY ";
        $query .= "                    t2.SCHOOLCD ";
        $query .= "                ) a1 on a2.SCHOOLCD = a1.SCHOOLCD  ";
        $query .= "    left join FINSCHOOL_MST a3 on a2.SCHOOLCD = a3.FINSCHOOLCD ";
        $query .= " ORDER BY ";
        $query .= "    a2.SCHOOLCD ";
        
        return $query;
    }
    
    //�c�̕ʏ��擾
    function getGroupCnt()
    {
        $query  = " SELECT ";
        $query .= "    a1.GROUPCD, ";
        $query .= "    a1.APPCNT, ";
        $query .= "    case when a2.ABSCNT IS NOT NULL then a1.APPCNT - a2.ABSCNT ";
        $query .= "    else a1.APPCNT end as TAKECNT, ";
        $query .= "    a2.ABSCNT, ";
        $query .= "    a3.GROUPNAME ";
        $query .= " FROM ";
        $query .= "    (SELECT ";
        $query .= "        COUNT(t1.SAT_NO) as APPCNT, ";
        $query .= "        t2.GROUPCD ";
        $query .= "    FROM ";
        $query .= "        SAT_EXAM_DAT t1 ";
        $query .= "        left join SAT_APP_FORM_MST t2 on t1.SAT_NO = t2.SAT_NO and t1.YEAR = t2.YEAR ";
        $query .= "    WHERE ";
        $query .= "        t1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    GROUP BY ";
        $query .= "        t2.GROUPCD ";
        $query .= "    ) a1 ";
        $query .= "    left join (SELECT ";
        $query .= "                    COUNT(t1.SAT_NO) as ABSCNT, ";
        $query .= "                    t2.GROUPCD ";
        $query .= "                FROM ";
        $query .= "                    SAT_EXAM_DAT t1 ";
        $query .= "                left join SAT_APP_FORM_MST t2 on t1.SAT_NO = t2.SAT_NO and t1.YEAR = t2.YEAR ";
        $query .= "                WHERE ";
        $query .= "                    t1.ABSENCE = '0' ";
        $query .= "                AND ";
        $query .= "                    t1.YEAR = '".CTRL_YEAR."' ";
        $query .= "                GROUP BY ";
        $query .= "                    t2.GROUPCD ";
        $query .= "                ) a2 on a1.GROUPCD = a2.GROUPCD ";
        $query .= "     left join SAT_GROUP_DAT a3 on a1.GROUPCD = a3.GROUPCD and a3.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "    a1.GROUPCD ";
                
        return $query;
    }
    
    
}
?>
