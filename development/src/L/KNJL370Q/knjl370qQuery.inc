<?php

require_once('for_php7.php');

class knjl370qQuery extends Query {

    //試験会場(左)
    function getPlace()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "    t1.PLACECD, ";
        $query .= "    t2.PLACEAREA, ";
        $query .= "    t2.PLACENAME_SHORT ";
        $query .= " FROM ";
        $query .= "    SAT_APP_FORM_MST t1  ";
        $query .= "    left join SAT_EXAM_PLACE_DAT t2 on t1.PLACECD = t2.PLACECD ";
        $query .= " WHERE ";
        $query .= "    t1.PLACECD IS NOT NULL ";
        $query .= " AND ";
        $query .= "    t1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "    t1.PLACECD ";
        
        return $query;
    }
    
    //中学校名(左)
    function getSchool()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "    t1.SCHOOLCD, ";
        $query .= "    t2.FINSCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "    SAT_APP_FORM_MST t1 ";
        $query .= "    left join FINSCHOOL_MST t2 on t1.SCHOOLCD = t2.FINSCHOOLCD ";
        $query .= " WHERE ";
        $query .= "    t1.SCHOOLCD IS NOT NULL ";
        $query .= " AND ";
        $query .= "    t1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY  ";
        $query .= "    t1.SCHOOLCD ";
        
        return $query;
    }
    
    //団体名(左)
    function getGroup()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "    t1.GROUPCD, ";
        $query .= "    t2.GROUPNAME ";
        $query .= " FROM ";
        $query .= "    SAT_APP_FORM_MST t1 ";
        $query .= "    left join SAT_GROUP_DAT t2 on t1.GROUPCD = t2.GROUPCD ";
        $query .= " WHERE ";
        $query .= "    t1.GROUPCD IS NOT NULL ";
        $query .= " AND ";
        $query .= "    t1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY  ";
        $query .= "    t1.GROUPCD ";
        
        return $query;
    }
    
    //受験者数(左)
    function getAppCount()
    {
        $query .= " SELECT * FROM ";
        $query .= " ( ";
        $query .= " SELECT ";
        $query .= "     1 as GROUP,COUNT(*) AS COUNT ";
        $query .= " FROM ";
        $query .= "     SAT_APP_FORM_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."'  ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     2 as GROUP,COUNT(*) AS COUNT ";
        $query .= " FROM ";
        $query .= "     SAT_APP_FORM_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."'  ";
        $query .= "     AND INOUT_KUBUN = '4' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     3 as GROUP,COUNT(*) AS COUNT ";
        $query .= " FROM ";
        $query .= "     SAT_APP_FORM_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."'  ";
        $query .= "     AND PLACECD = '80' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     4 as GROUP,COUNT(*) AS COUNT ";
        $query .= " FROM ";
        $query .= "     SAT_APP_FORM_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."'  ";
        $query .= "     AND PLACECD != '80' ";
        $query .= "     AND INOUT_KUBUN != '4' ";
        $query .= " ) ";
        $query .= " ORDER BY GROUP ";
        
        return $query;
    }
    
    //リスト(左)
    function getList($model) {
        $query  = " SELECT ";
        $query .= "    * ";
        $query .= " FROM ";
        $query .= "    SAT_APP_FORM_MST ";
        $query .= " WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        if($model->left_field["EXAMNO"] != ""){
            $query .= " AND ";
            $query .= "    SAT_NO = '".$model->left_field["EXAMNO"]."' ";
        }
        if($model->left_field["NAME_SEI"] != ""){
            $query .= " AND ";
            $query .= "    LASTNAME  LIKE '".$model->left_field["NAME_SEI"]."%' ";
        }
        if($model->left_field["NAME_MEI"] != ""){
            $query .= " AND ";
            $query .= "    FIRSTNAME LIKE '".$model->left_field["NAME_MEI"]."%' ";
        }
        if($model->left_field["PLACECD"] != ""){
            $query .= " AND ";
            $query .= "    PLACECD = '".$model->left_field["PLACECD"]."' ";
        }
        if($model->left_field["SCHOOLCD"] != ""){
            $query .= " AND ";
            $query .= "    SCHOOLCD = '".$model->left_field["SCHOOLCD"]."' ";
        }
        if($model->left_field["GROUPCD"] != ""){
            $query .= " AND ";
            $query .= "    GROUPCD = '".$model->left_field["GROUPCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "    SAT_NO ";

        return $query;
    }

    //１レコード取得(右)
    function getRow($satno) {
        $query  = " SELECT ";
        $query .= "    t1.*, ";
        $query .= "    t2.NAME1 as INOUT_KUBUN, ";
        $query .= "    t3.NAME1 as IND_KUBUN, ";
        $query .= "    t4.NAME1 as SEND_KUBUN, ";
        $query .= "    t5.FINSCHOOL_PREF_CD ";
        $query .= " FROM ";
        $query .= "    SAT_APP_FORM_MST t1 ";
        $query .= "    left join NAME_MST t2 on t1.INOUT_KUBUN = t2.NAMECD2 and t2.NAMECD1 = 'L201' ";
        $query .= "    left join NAME_MST t3 on t1.IND_KUBUN = t3.NAMECD2 and t3.NAMECD1 = 'L202' ";
        $query .= "    left join NAME_MST t4 on t1.SEND_KUBUN = t4.NAMECD2 and t4.NAMECD1 = 'L203' ";
        $query .= "    left join FINSCHOOL_MST t5 on t1.SCHOOLCD = t5.FINSCHOOLCD ";
        $query .= " WHERE ";
        $query .= "    SAT_NO = '".$satno."' ";
        $query .= " AND ";
        $query .= "    t1.YEAR = '".CTRL_YEAR."' ";

        return $query;
    }
    
    //卒業区分(右)
    function getGrade()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'L205' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";
        
        return $query;
    }
    
    //受験区分(右)
    function getKubun($satno)
    {
        $query  = " SELECT ";
        $query .= "    t1.*, ";
        $query .= "    t2.NAME1 as INOUT_KUBUN, ";
        $query .= "    t3.NAME1 as IND_KUBUN, ";
        $query .= "    t4.NAME1 as SEND_KUBUN ";
        $query .= " FROM ";
        $query .= "    SAT_EXAM_NO_DAT t1 ";
        $query .= "    left join NAME_MST t2 on t1.INOUT_KUBUN = t2.NAMECD2 and t2.NAMECD1 = 'L201' ";
        $query .= "    left join NAME_MST t3 on t1.IND_KUBUN = t3.NAMECD2 and t3.NAMECD1 = 'L202' ";
        $query .= "    left join NAME_MST t4 on t1.SEND_KUBUN = t4.NAMECD2 and t4.NAMECD1 = 'L203' ";
        $query .= " WHERE ";
        $query .= "    '".$satno."' BETWEEN JUKEN_NO_FROM AND JUKEN_NO_TO ";
        $query .= " AND ";
        $query .= "    t1.YEAR = '".CTRL_YEAR."' ";
        
        return $query;
    }
    
    //中学校県名コンボ(右)
    function getSchoolPref()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "    t1.FINSCHOOL_PREF_CD, ";
        $query .= "    t2.PREF_NAME ";
        $query .= " FROM ";
        $query .= "    FINSCHOOL_MST t1  ";
        $query .= "    left join PREF_MST t2 on t1.FINSCHOOL_PREF_CD = t2.PREF_CD ";
        $query .= " WHERE ";
        $query .= "    t1.FINSCHOOL_TYPE = '3' ";
        $query .= " ORDER BY ";
        $query .= "    t1.FINSCHOOL_PREF_CD ";
        
        return $query;
    }
    
    //中学校名コンボ(右)
    function getSchoolNm($prefcd,$search_school)
    {
        $query  = " SELECT ";
        $query .= "    FINSCHOOLCD, ";
        $query .= "    FINSCHOOL_NAME_ABBV as FINSCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "    FINSCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "    FINSCHOOL_PREF_CD = '".$prefcd."' ";
        $query .= " AND ";
        $query .= "    FINSCHOOL_TYPE = '3' ";
        $query .= " AND ";
        $query .= "    FINSCHOOL_NAME_ABBV LIKE '%".$search_school."%' ";
        $query .= " ORDER BY ";
        $query .= "    FINSCHOOLCD ";
        
        return $query;
    }
    
    //試験会場(右)
    function getPlaceNm()
    {
        $query  = " SELECT ";
        $query .= "    PLACECD, ";
        $query .= "    PLACEAREA, ";
        $query .= "    PLACENAME_SHORT ";
        $query .= " FROM ";
        $query .= "    SAT_EXAM_PLACE_DAT ";
        $query .= " WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "    PLACECD ";
        
        return $query;
    }
    
    //団体名(右)
    function getGroupNm($search_group)
    {
        $query  = " SELECT ";
        $query .= "    GROUPCD, ";
        $query .= "    GROUPNAME ";
        $query .= " FROM ";
        $query .= "    SAT_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= " AND ";
        $query .= "    GROUPNAME LIKE '%".$search_group."%' ";
        $query .= " ORDER BY ";
        $query .= "    GROUPCD ";
        
        return $query;
    }
    //県名コンボ(右)
    function getPrefcdAll()
    {
        $query  = " SELECT ";
        $query .= "    * ";
        $query .= " FROM ";
        $query .= "    PREF_MST ";
        $query .= " ORDER BY ";
        $query .= "    PREF_CD ";
        
        return $query;
    }
    
    
    //受験番号重複チェック
    function getCnt($examno)
    {
        $query  = " SELECT ";
        $query .= "    COUNT(SAT_NO) ";
        $query .= " FROM ";
        $query .= "    SAT_APP_FORM_MST ";
        $query .= " WHERE ";
        $query .= "    SAT_NO = '".$examno."' ";
        $query .= " AND ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        
        return $query;
    }
    
    //受験番号帯に含まれているか確認
    function getNoGroup($examno, $mode="")
    {
        $query  = " SELECT ";
        if($mode != ""){
            $query .= "    COUNT(*) ";
        }else{
            $query .= "    * ";
        }
        $query .= " FROM ";
        $query .= "    SAT_EXAM_NO_DAT ";
        $query .= " WHERE ";
        $query .= "    '".$examno."' BETWEEN JUKEN_NO_FROM AND JUKEN_NO_TO ";
        $query .= " AND ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        
        return $query;
    }
    
    //削除
    function deleteApp($satno)
    {
        $query  = " DELETE FROM ";
        $query .= "    SAT_APP_FORM_MST ";
        $query .= " WHERE ";
        $query .= "    SAT_NO = '".$satno."' ";
        $query .= " AND ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        
        return $query;
    }
    function deleteExam($satno)
    {
        $query  = " DELETE FROM ";
        $query .= "    SAT_EXAM_DAT ";
        $query .= " WHERE ";
        $query .= "    SAT_NO = '".$satno."' ";
        $query .= " AND ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        
        return $query;
    }
}
?>
