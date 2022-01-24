<?php

require_once('for_php7.php');

class knjl374qQuery extends Query {
    //データ取得
    function getData()
    {
        $query  = " SELECT ";
        $query .= "    a1.PLACECD, ";
        $query .= "    a3.PLACEAREA, ";
        $query .= "    a1.ALLCNT, ";
        $query .= "    case when a2.ABSCNT IS NOT NULL then a1.ALLCNT - a2.ABSCNT ";
        $query .= "    else a1.ALLCNT end as TAKECNT, ";
        $query .= "    a2.ABSCNT ";
        $query .= " FROM ";
        $query .= "    (SELECT ";
        $query .= "        COUNT(t1.SAT_NO) as ALLCNT, ";
        $query .= "        t1.YEAR, ";
        $query .= "        t2.PLACECD ";
        $query .= "    FROM ";
        $query .= "        SAT_EXAM_DAT t1 left join SAT_APP_FORM_MST t2 on t1.SAT_NO = t2.SAT_NO and t1.YEAR = t2.YEAR ";
        $query .= "    WHERE ";
        $query .= "        t1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    GROUP BY ";
        $query .= "        t1.YEAR, ";
        $query .= "        t2.PLACECD ";
        $query .= "    ORDER BY ";
        $query .= "        t2.PLACECD ";
        $query .= "    ) a1  ";
        $query .= "    left join (SELECT ";
        $query .= "                    COUNT(t1.SAT_NO) as ABSCNT, ";
        $query .= "                    t1.YEAR, ";
        $query .= "                    t2.PLACECD ";
        $query .= "                FROM ";
        $query .= "                    SAT_EXAM_DAT t1 ";
        $query .= "                    left join SAT_APP_FORM_MST t2 on t1.SAT_NO = t2.SAT_NO and t1.YEAR = t2.YEAR ";
        $query .= "                WHERE ";
        $query .= "                    t1.ABSENCE = '0' ";
        $query .= "                AND ";
        $query .= "                    t1.YEAR = '".CTRL_YEAR."' ";
        $query .= "                GROUP BY ";
        $query .= "                    t1.YEAR, ";
        $query .= "                    t2.PLACECD ";
        $query .= "                ORDER BY ";
        $query .= "                    t2.PLACECD ";
        $query .= "                )a2 on a1.PLACECD = a2.PLACECD and a1.YEAR = a2.YEAR ";
        $query .= "    left join SAT_EXAM_PLACE_DAT a3 on a1.PLACECD = a3.PLACECD and a1.YEAR = a3.YEAR ";
        $query .= " ORDER BY  ";
        $query .= "    a1.PLACECD ";
        
        return $query;
    }
}
?>
