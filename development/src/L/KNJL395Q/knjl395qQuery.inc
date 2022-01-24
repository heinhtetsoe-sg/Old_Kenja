<?php

require_once('for_php7.php');

class knjl395qQuery extends Query {
    
    //試験日取得
    function getExamDate()
    {
        $query  = " SELECT ";
        $query .= "     EXAM_DATE ";
        $query .= " FROM ";
        $query .= "     SAT_INFO_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        
        return $query;
    }
    
    //件数チェック
    //1から19までと28
    function getFirstSql($i)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ( ";
        $query .= "     SELECT ";
        $query .= "         ROW_NUMBER() OVER(ORDER BY t1.SAT_NO) as NO, ";
        $query .= "         t1.YEAR, ";
        $query .= "         t1.SAT_NO, ";
        $query .= "         t1.NAME1, ";
        $query .= "         t1.SCHOOLCD, ";
        $query .= "         t6.FINSCHOOL_NAME_ABBV, ";
        $query .= "         t1.GRADUATION, ";
        $query .= "         t3.NAME1 as GRADE, ";
        $query .= "         t2.SCORE_TOTAL, ";
        $query .= "         t2.JUDGE_SAT, ";
        $query .= "         t4.NAME1 as JUDGE, ";
        $query .= "         t1.PLACECD, ";
        $query .= "         t5.PLACEAREA, ";
        $query .= "         case when t1.ABSENCE = '1' then NULL else '欠席' end as ABSENCE ";
        $query .= "     FROM ";
        $query .= "         SAT_APP_FORM_MST t1 ";
        $query .= "         left join SAT_EXAM_DAT t2 on t1.YEAR = t2.YEAR and t1.SAT_NO = t2.SAT_NO ";
        $query .= "         left join FINSCHOOL_MST t6 on t1.SCHOOLCD = t6.FINSCHOOLCD and t6.FINSCHOOL_TYPE = '3' ";
        $query .= "         left join NAME_MST t3 on t1.GRADUATION = t3.NAMECD2 and t3.NAMECD1 = 'L205' ";
        $query .= "         left join NAME_MST t4 on t2.JUDGE_SAT = t4.NAMECD2 and t4.NAMECD1 = 'L200' ";
        $query .= "         left join SAT_EXAM_PLACE_DAT t5 on t1.PLACECD = t5.PLACECD and t1.YEAR = t5.YEAR ";
        
        switch ($i) {
            case 0:
                /*1.県外会場受験の県内生*/
                $query .= "     WHERE ";
                $query .= "         t1.YEAR = '".CTRL_YEAR."' AND ";
                $query .= "         t1.SCHOOLCD != '2008005' AND ";
                $query .= "         t1.PREFCD = '19' AND ";
                $query .= "         t1.PLACECD not in ('01','02','03') ";
                break;

            case 1:
                /*2.県外会場受験の長野県中学校生*/
                $query .= " WHERE ";
                $query .= "     t1.YEAR = '".CTRL_YEAR."' AND ";
                $query .= "     t1.SCHOOLCD != '2008005' AND ";
                $query .= "     t6.FINSCHOOL_PREF_CD = '20' AND ";
                $query .= "     t1.PLACECD not in ('01','02','03') ";
                break;

            case 2:
                /*3.県内会場受験の長野除く県外生*/
                $query .= " WHERE ";
                $query .= "     t1.YEAR = '".CTRL_YEAR."' AND ";
                $query .= "     t1.SCHOOLCD != '2008005' AND ";
                $query .= "     t1.PREFCD not in ('19','20') AND ";
                $query .= "     t1.PLACECD in ('01','02','03') ";
                break;

            case 3:
                /*4.すべての長野県中学校生*/
                $query .= " WHERE ";
                $query .= "     t1.YEAR = '".CTRL_YEAR."' AND ";
                $query .= "     t1.SCHOOLCD != '2008005' AND ";
                $query .= "     t6.FINSCHOOL_PREF_CD = '20'  ";
                break;

            case 4:
                /*5.県内の中2以下*/
                $query .= " WHERE ";
                $query .= "     t1.YEAR = '".CTRL_YEAR."' AND ";
                $query .= "     t1.SCHOOLCD != '2008005' AND ";
                $query .= "     t1.PREFCD = '19' AND ";
                $query .= "     t1.GRADUATION < '09' ";
                break;

            case 5:
                /*6.県内の特奨生*/
                $query .= " WHERE ";
                $query .= "     t1.YEAR = '".CTRL_YEAR."' AND ";
                $query .= "     t1.SCHOOLCD != '2008005' AND ";
                $query .= "     t1.PREFCD = '19' AND ";
                $query .= "     t1.GRADUATION > '08' AND ";
                $query .= "     t2.JUDGE_SAT = '1' ";
                break;

            case 6:
                /*7.県内の特奨生除くA・準A現役*/
                $query .= " WHERE ";
                $query .= "     t1.YEAR = '".CTRL_YEAR."' AND ";
                $query .= "     t1.SCHOOLCD != '2008005' AND ";
                $query .= "     t1.PREFCD = '19' AND ";
                $query .= "     t1.GRADUATION = '09' AND ";
                $query .= "     t2.JUDGE_SAT in ('2','3') ";
                break;

            case 7:
                /*8.県内のB～D現役*/
                $query .= " WHERE ";
                $query .= "     t1.YEAR = '".CTRL_YEAR."' AND ";
                $query .= "     t1.SCHOOLCD != '2008005' AND ";
                $query .= "     t1.PREFCD = '19' AND ";
                $query .= "     t1.GRADUATION = '09' AND ";
                $query .= "     t2.JUDGE_SAT > '3' ";
                break;

            case 8:
                /*9.長野の特奨生*/
                $query .= " WHERE ";
                $query .= "     t1.YEAR = '".CTRL_YEAR."' AND ";
                $query .= "     t1.SCHOOLCD != '2008005' AND ";
                $query .= "     t1.PREFCD = '20' AND ";
                $query .= "     t1.GRADUATION > '08' AND ";
                $query .= "     t2.JUDGE_SAT = '1' ";
                break;

            case 9:
                /*10.長野の特奨生除くA・準A現役*/
                $query .= " WHERE ";
                $query .= "     t1.YEAR = '".CTRL_YEAR."' AND ";
                $query .= "     t1.SCHOOLCD != '2008005' AND ";
                $query .= "     t1.PREFCD = '20' AND ";
                $query .= "     t1.GRADUATION = '09' AND ";
                $query .= "     t2.JUDGE_SAT in ('2','3') ";
                break;

            case 10:
                /*11.長野のB～Dの現役*/
                $query .= " WHERE ";
                $query .= "     t1.YEAR = '".CTRL_YEAR."' AND ";
                $query .= "     t1.SCHOOLCD != '2008005' AND ";
                $query .= "     t1.PREFCD = '20' AND ";
                $query .= "     t1.GRADUATION = '09' AND ";
                $query .= "     t2.JUDGE_SAT > '3' ";
                break;

            case 11:
                /*12.長野除く県外の中2以下*/
                $query .= " WHERE ";
                $query .= "     t1.YEAR = '".CTRL_YEAR."' AND ";
                $query .= "     t1.SCHOOLCD != '2008005' AND ";
                $query .= "     t1.PREFCD not in ('19', '20', '48') AND ";
                $query .= "     t1.GRADUATION < '09' ";
                break;

            case 12:
                /*13.長野除く県外の特奨生*/
                $query .= " WHERE ";
                $query .= "     t1.YEAR = '".CTRL_YEAR."' AND ";
                $query .= "     t1.SCHOOLCD != '2008005' AND ";
                $query .= "     t1.PREFCD not in ('19', '20', '48') AND ";
                $query .= "     t1.GRADUATION > '08' AND ";
                $query .= "     t2.JUDGE_SAT = '1' ";
                break;

            case 13:
                /*14.長野除く県外の特奨生外A現役*/
                $query .= " WHERE ";
                $query .= "     t1.YEAR = '".CTRL_YEAR."' AND ";
                $query .= "     t1.SCHOOLCD != '2008005' AND ";
                $query .= "     t1.PREFCD not in ('19', '20', '48') AND ";
                $query .= "     t1.GRADUATION = '09' AND ";
                $query .= "     t2.JUDGE_SAT = '2' ";
                break;

            case 14:
                /*15.長野県除く県外のB現役*/
                $query .= " WHERE ";
                $query .= "     t1.YEAR = '".CTRL_YEAR."' AND ";
                $query .= "     t1.SCHOOLCD != '2008005' AND ";
                $query .= "     t1.PREFCD not in ('19', '20', '48') AND ";
                $query .= "     t1.GRADUATION = '09' AND ";
                $query .= "     t2.JUDGE_SAT = '4' ";
                break;

            case 15:
                /*16.長野除く県外のC・Dまたは現役以外*/
                $query .= " WHERE ";
                $query .= "     t1.YEAR = '".CTRL_YEAR."' AND ";
                $query .= "     t1.SCHOOLCD != '2008005' AND ";
                $query .= "     t1.PREFCD not in ('19', '20', '48') AND ";
                $query .= "     (t1.GRADUATION = '99' OR t2.JUDGE_SAT > '4') ";
                break;

            case 16:
                /*17.海外の中2以下*/
                $query .= " WHERE ";
                $query .= "     t1.YEAR = '".CTRL_YEAR."' AND ";
                $query .= "     t1.SCHOOLCD != '2008005' AND ";
                $query .= "     t1.PREFCD = '48' AND ";
                $query .= "     t1.GRADUATION < '09' ";
                break;

            case 17:
                /*18.海外の特奨生*/
                $query .= " WHERE ";
                $query .= "     t1.YEAR = '".CTRL_YEAR."' AND ";
                $query .= "     t1.SCHOOLCD != '2008005' AND ";
                $query .= "     t1.PREFCD = '48' AND ";
                $query .= "     t2.JUDGE_SAT = '1' ";
                break;

            case 18:
                /*19.海外の特奨生除くA・B現役*/
                $query .= " WHERE ";
                $query .= "     t1.YEAR = '".CTRL_YEAR."' AND ";
                $query .= "     t1.SCHOOLCD != '2008005' AND ";
                $query .= "     t1.PREFCD = '48' AND ";
                $query .= "     t2.JUDGE_SAT in ('2','4') ";
                break;

            case 27:
                /*28.海外のC・D判定の現役*/
                $query .= " WHERE ";
                $query .= "     t1.YEAR = '".CTRL_YEAR."' AND ";
                $query .= "     t1.SCHOOLCD != '2008005' AND ";
                $query .= "     t1.PREFCD = '48' AND ";
                $query .= "     t1.GRADUATION = '09' AND ";
                $query .= "     t2.JUDGE_SAT in ('5','6') ";
                break;

            default:
                $query .= "";
                break;
        }
        
        $query .= "     ORDER BY ";
        $query .= "         t1.SAT_NO ";
        $query .= " ) ";
        
        return $query;
    }
    
    //20から24までの
    function getSecondSql($i)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ( ";
        $query .= "     SELECT ";
        $query .= "         ROW_NUMBER() OVER(ORDER BY t1.SAT_NO) as NO, ";
        $query .= "         t1.YEAR, ";
        $query .= "         t1.SAT_NO, ";
        $query .= "         t1.NAME1, ";
        $query .= "         t1.SCHOOLCD, ";
        $query .= "         t2.FINSCHOOL_NAME_ABBV, ";
        $query .= "         t1.GRADUATION, ";
        $query .= "         t3.NAME1 as GRADE, ";
        $query .= "         t1.PREFCD, ";
        $query .= "         t4.PREF_NAME, ";
        $query .= "         t1.GROUPCD, ";
        $query .= "         t5.GROUPNAME ";
        $query .= "     FROM ";
        $query .= "         SAT_APP_FORM_MST t1  ";
        $query .= "         left join FINSCHOOL_MST t2 on t1.SCHOOLCD = t2.FINSCHOOLCD and t2.FINSCHOOL_TYPE = '3' ";
        $query .= "         left join NAME_MST t3 on t1.GRADUATION = t3.NAMECD2 and t3.NAMECD1 = 'L205' ";
        $query .= "         left join PREF_MST t4 on t1.PREFCD = t4.PREF_CD ";
        $query .= "         left join SAT_GROUP_DAT t5 on t1.YEAR = t5.YEAR and t1.GROUPCD = t5.GROUPCD ";
        
        switch ($i) {
            case 19:
                /*20.県内の欠席者リスト*/
                $query .= "     WHERE ";
                $query .= "         t1.YEAR = '".CTRL_YEAR."' AND ";
                $query .= "         t1.SCHOOLCD != '2008005' AND ";
                $query .= "         t1.ABSENCE = '0' AND ";
                $query .= "         t1.PREFCD = '19' ";
                break;

            case 20:
                /*21.長野県の欠席者リスト*/
                $query .= "     WHERE ";
                $query .= "         t1.YEAR = '".CTRL_YEAR."' AND ";
                $query .= "         t1.SCHOOLCD != '2008005' AND ";
                $query .= "         t1.ABSENCE = '0' AND ";
                $query .= "         t1.PREFCD = '20' ";
                break;

            case 21:
                /*22.県外の欠席者リスト*/
                $query .= "     WHERE ";
                $query .= "         t1.YEAR = '".CTRL_YEAR."' AND ";
                $query .= "         t1.SCHOOLCD != '2008005' AND ";
                $query .= "         t1.ABSENCE = '0' AND ";
                $query .= "         t1.PREFCD not in ('19','20','48') ";
                break;

            case 22:
                /*23.海外の欠席者リスト*/
                $query .= "     WHERE ";
                $query .= "         t1.YEAR = '".CTRL_YEAR."' AND ";
                $query .= "         t1.SCHOOLCD != '2008005' AND ";
                $query .= "         t1.ABSENCE = '0' AND ";
                $query .= "         t1.PREFCD = '48' ";
                break;

            case 23:
                /*24.浪人リスト*/
                $query .= "     WHERE ";
                $query .= "         t1.YEAR = '".CTRL_YEAR."' AND ";
                $query .= "         t1.SCHOOLCD != '2008005' AND ";
                $query .= "         t1.GRADUATION = '99' ";
                break;
        }
        $query .= "     ORDER BY ";
        $query .= "         t1.SAT_NO ";
        $query .= " ) ";
        
        return $query;
    }
    
    //25の
    function getThirdSql($i)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ( ";
        $query .= "     SELECT ";
        $query .= "         ROW_NUMBER() OVER(ORDER BY t1.SAT_NO) as NO,  ";
        $query .= "         t1.YEAR, ";
        $query .= "         t1.SAT_NO, ";
        $query .= "         t1.NAME1, ";
        $query .= "         t1.SCHOOLCD, ";
        $query .= "         t2.FINSCHOOL_NAME_ABBV, ";
        $query .= "         t1.GRADUATION, ";
        $query .= "         t3.NAME1 as GRADE, ";
        $query .= "         t1.PLACECD, ";
        $query .= "         t4.PLACEAREA, ";
        $query .= "         case when t5.ABSENCE_ENGLISH = '0' then '欠席' else NULL end as ABSENCE_ENGLISH, ";
        $query .= "         case when t5.ABSENCE_MATH = '0' then '欠席' else NULL end as ABSENCE_MATH, ";
        $query .= "         case when t5.ABSENCE_JAPANESE = '0' then '欠席' else NULL end as ABSENCE_JAPANESE ";
        $query .= "     FROM ";
        $query .= "         SAT_APP_FORM_MST t1  ";
        $query .= "         left join FINSCHOOL_MST t2 on t1.SCHOOLCD = t2.FINSCHOOLCD and t2.FINSCHOOL_TYPE = '3' ";
        $query .= "         left join NAME_MST t3 on t1.GRADUATION = t3.NAMECD2 and t3.NAMECD1 = 'L205' ";
        $query .= "         left join SAT_EXAM_PLACE_DAT t4 on t1.YEAR = t4.YEAR and t1.PLACECD = t4.PLACECD ";
        $query .= "         left join SAT_EXAM_DAT t5 on t1.YEAR = t5.YEAR and t1.SAT_NO = t5.SAT_NO ";
        $query .= "     WHERE ";
        $query .= "         t1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "         t1.SCHOOLCD != '2008005' AND ";
        $query .= "         t1.ABSENCE != '0' AND ";
        $query .= "         (t5.ABSENCE_ENGLISH = '0' OR t5.ABSENCE_MATH = '0' OR t5.ABSENCE_JAPANESE = '0') ";
        $query .= "     ORDER BY ";
        $query .= "         t1.SAT_NO ";
        $query .= "     ) ";
        $query .= "  ";
        
        return $query;
    }
    
    //26の
    function getFourthSql($i)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ( ";
        $query .= "     SELECT ";
        $query .= "         ROW_NUMBER() OVER(ORDER BY a1.GROUPCD) as NO, ";
        $query .= "         a1.YEAR, ";
        $query .= "         a1.GROUPCD, ";
        $query .= "         a2.GROUPNAME, ";
        $query .= "         a2.GROUPPREF, ";
        $query .= "         a3.PREF_NAME, ";
        $query .= "         a1.CNT ";
        $query .= "     FROM ";
        $query .= "         ( ";
        $query .= "         SELECT ";
        $query .= "             YEAR, ";
        $query .= "             GROUPCD, ";
        $query .= "             COUNT(*) as CNT ";
        $query .= "         FROM ";
        $query .= "             ( ";
        $query .= "             SELECT ";
        $query .= "                 t1.YEAR, ";
        $query .= "                 t1.GROUPCD ";
        $query .= "             FROM ";
        $query .= "                 SAT_APP_FORM_MST t1  ";
        $query .= "                 left join SAT_EXAM_DAT t2 on t1.YEAR = t2.YEAR and t1.SAT_NO = t2.SAT_NO ";
        $query .= "             WHERE ";
        $query .= "                 t1.YEAR = '".CTRL_YEAR."' ";
        $query .= "             AND ";
        $query .= "                 t2.JUDGE_SAT in ('1','2') ";
        $query .= "             AND ";
        $query .= "                 t1.SCHOOLCD != '2008005' ";
        $query .= "             AND ";
        $query .= "                 t1.PREFCD not in ('19','20', '48') ";
        $query .= "             AND ";
        $query .= "                 t1.GROUPCD IS NOT NULL ";
        $query .= "             AND ";
        $query .= "                 t1.IND_KUBUN = '2' ";
        $query .= "             ) ";
        $query .= "         GROUP BY ";
        $query .= "             YEAR, ";
        $query .= "             GROUPCD ";
        $query .= "         ) a1 ";
        $query .= "         left join SAT_GROUP_DAT a2 on a1.YEAR = a2.YEAR and a1.GROUPCD = a2.GROUPCD ";
        $query .= "         left join PREF_MST a3 on a2.GROUPPREF = a3.PREF_CD ";
        $query .= "     ORDER BY ";
        $query .= "         a1.GROUPCD ";
        $query .= "     ) ";
        
        return $query;
    }
    
    //27の
    function getFifthSql($i)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ( ";
        $query .= "     SELECT ";
        $query .= "         ROW_NUMBER() OVER(ORDER BY t1.GROUPCD) as NO, ";
        $query .= "         t1.YEAR, ";
        $query .= "         t1.GROUPCD, ";
        $query .= "         t4.GROUPNAME, ";
        $query .= "         t4.GROUPPREF, ";
        $query .= "         t5.PREF_NAME, ";
        $query .= "         t1.ALL_CNT, ";
        $query .= "         t2.TAKE_CNT, ";
        $query .= "         t3.ABSENCE_CNT ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             YEAR, ";
        $query .= "             GROUPCD, ";
        $query .= "             COUNT(*) as ALL_CNT ";
        $query .= "         FROM ";
        $query .= "             SAT_APP_FORM_MST ";
        $query .= "         WHERE ";
        $query .= "             YEAR = '".CTRL_YEAR."' AND ";
        $query .= "             GROUPCD IS NOT NULL AND ";
        $query .= "             GROUPCD != '08005' ";
        $query .= "         GROUP BY ";
        $query .= "             YEAR, ";
        $query .= "             GROUPCD ";
        $query .= "         ) t1 ";
        $query .= "         left join (SELECT ";
        $query .= "                         YEAR, ";
        $query .= "                         GROUPCD, ";
        $query .= "                         COUNT(*) as TAKE_CNT ";
        $query .= "                     FROM ";
        $query .= "                         SAT_APP_FORM_MST ";
        $query .= "                     WHERE ";
        $query .= "                         YEAR = '".CTRL_YEAR."' AND ";
        $query .= "                         GROUPCD IS NOT NULL AND ";
        $query .= "                         GROUPCD != '08005' AND ";
        $query .= "                         ABSENCE != '0' ";
        $query .= "                     GROUP BY ";
        $query .= "                         YEAR, ";
        $query .= "                         GROUPCD ";
        $query .= "                     ) t2 on t1.YEAR = t2.YEAR and t1.GROUPCD = t2.GROUPCD ";
        $query .= "         left join (SELECT ";
        $query .= "                         YEAR, ";
        $query .= "                         GROUPCD, ";
        $query .= "                         COUNT(*) as ABSENCE_CNT ";
        $query .= "                     FROM ";
        $query .= "                         SAT_APP_FORM_MST ";
        $query .= "                     WHERE ";
        $query .= "                         YEAR = '".CTRL_YEAR."' AND ";
        $query .= "                         GROUPCD IS NOT NULL AND ";
        $query .= "                         GROUPCD != '08005' AND ";
        $query .= "                         ABSENCE != '1' ";
        $query .= "                     GROUP BY ";
        $query .= "                         YEAR, ";
        $query .= "                         GROUPCD ";
        $query .= "                     ) t3 on t1.YEAR = t3.YEAR and t1.GROUPCD = t3.GROUPCD ";
        $query .= "         left join SAT_GROUP_DAT t4 on t1.YEAR = t4.YEAR and t1.GROUPCD = t4.GROUPCD ";
        $query .= "         left join PREF_MST t5 on t4.GROUPPREF = t5.PREF_CD ";
        $query .= "     ORDER BY ";
        $query .= "         t1.GROUPCD ";
        $query .= "     ) ";
        
        return $query;
    }
}
?>
