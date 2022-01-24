<?php

require_once('for_php7.php');
class knjz334Query extends Query {
    
    //表示中内容を取得
    function getOshirase($model, $today)
    {
        $query  = " SELECT ";
        $query .= "    t1.*, ";
        $query .= "    t2.GROUP_CD, ";
        $query .= "    t3.STAFFNAME_SHOW ";
        $query .= " FROM ";
        $query .= "    OSHIRASE_TBL t1 ";
        $query .= "    left join OSHIRASE_GRP t2 on t1.OSHIRASE_NO = t2.OSHIRASE_NO ";
        $query .= "         and t1.DATA_DIV = t2.DATA_DIV ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "         and t1.SCHOOLCD = t2.SCHOOLCD and t1.SCHOOL_KIND = t2.SCHOOL_KIND ";
        }
        $query .= "    left join STAFF_MST t3 on t1.STAFFCD = t3.STAFFCD ";
        $query .= " WHERE ";
        $query .= "    t1.END_DATE >= '".$today."' ";
        $query .= "    AND t1.DATA_DIV = '".$model->dataDiv."' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "    AND t1.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= "    AND t1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "    T1.START_DATE, T1.END_DATE, T1.OSHIRASE_NO ";
        
        return $query;
    }
    //選択したお知らせを取得
    function getSelect($model, $oshiraseNo)
    {
        $query  = " SELECT ";
        $query .= "    t1.*, ";
        $query .= "    t2.GROUP_CD, ";
        $query .= "    t3.STAFFCD as IND, ";
        $query .= "    t4.STAFFNAME_SHOW  ";
        $query .= " FROM ";
        $query .= "    OSHIRASE_TBL t1 ";
        $query .= "    left join OSHIRASE_GRP t2 on t1.OSHIRASE_NO = t2.OSHIRASE_NO ";
        $query .= "         and t1.DATA_DIV = t2.DATA_DIV ";
        if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            $query .= "    and t1.SCHOOLCD = t2.SCHOOLCD and t1.SCHOOL_KIND = t2.SCHOOL_KIND ";
        }
        $query .= "    left join OSHIRASE_IND t3 on t1.OSHIRASE_NO = t3.OSHIRASE_NO ";
        $query .= "         and t1.DATA_DIV = t3.DATA_DIV ";
        if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            $query .= "    and t1.SCHOOLCD = t3.SCHOOLCD and t1.SCHOOL_KIND = t3.SCHOOL_KIND ";
        }
        $query .= "    left join STAFF_MST t4 on t3.STAFFCD = t4.STAFFCD";
        $query .= " WHERE ";
        $query .= "    t1.OSHIRASE_NO = ".$oshiraseNo." ";
        $query .= "    AND t1.DATA_DIV = '".$model->dataDiv."' ";
        if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            $query .= "    AND t1.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= "    AND t1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        
        return $query;
    }
    //グループ取得
    function getGroup($model)
    {
        $query  = " SELECT ";
        $query .= "    GROUPCD, ";
        $query .= "    GROUPNAME ";
        $query .= " FROM ";
        $query .= "    USERGROUP_MST ";
        if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            $query .= "    WHERE SCHOOLCD = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "    GROUPCD ";
        
        return $query;
    }
    //個人選択用グループ取得
    function getBaseGrp($model)
    {
        $query  = " SELECT ";
        $query .= "    GROUPCD as VALUE, ";
        $query .= "    GROUPNAME as LABEL ";
        $query .= " FROM ";
        $query .= "    USERGROUP_MST ";
        if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            $query .= "    WHERE SCHOOLCD = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "    GROUPCD ";
        
        return $query;
    }
    //個人選択用右側リスト取得
    function getStaff($model)
    {
        $query  = " SELECT ";
        $query .= "    t1.STAFFCD AS VALUE, ";
        $query .= "    STAFFNAME_SHOW AS LABEL ";
        $query .= " FROM ";
        $query .= "    USERGROUP_DAT t1 ";
        $query .= "    left join STAFF_MST t2 on t1.STAFFCD = t2.STAFFCD ";
        $query .= " WHERE ";
        $query .= "    t1.GROUPCD = '".$model->field["SELECT_INDNAME"]."' ";
        if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            $query .= "    AND t1.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= "    AND t1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " AND ";
        $query .= "    t1.YEAR = '".CTRL_YEAR."' ";

        return $query;
    }
    //OSHIRASE_TBL,OSHIRASE_GRP, OSHIRASE_IND削除
    function delete($model, $oshiraseNo, $name)
    {
        $query  = " DELETE FROM ";
        $query .= "    OSHIRASE_".$name." ";
        $query .= " WHERE ";
        $query .= "    OSHIRASE_NO = ".$oshiraseNo." ";
        $query .= "    AND DATA_DIV = '".$model->dataDiv."' ";
        if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            $query .= "    AND SCHOOLCD = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        
        return $query;
    }
    
    //OSHIRASE_TBL, OSHIRASE_GRP, OSHIRASE_INDのOSHIRASE_NO/DATA_NOの最大値取得
    function getMax($model, $name)
    {
        $query  = " SELECT ";
        if ($name == 'TBL') {
            $query .= "    MAX(OSHIRASE_NO) ";
        } else {
            $query .= "    MAX(DATA_NO) ";
        }
        $query .= " FROM ";
        $query .= "    OSHIRASE_".$name." ";
        $query .= " WHERE ";
        $query .= "    DATA_DIV = '".$model->dataDiv."' ";
        if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            $query .= "    AND SCHOOLCD = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        
        return $query;
    }
    
    //OSHIRASE_IND, OSHIRASE_GRPに登録されている数を数える
    function getCount($model, $oshiraseNo, $name)
    {
        $query  = " SELECT ";
        $query .= "    COUNT(*) ";
        $query .= " FROM ";
        $query .= "    OSHIRASE_".$name." ";
        $query .= " WHERE ";
        $query .= "    OSHIRASE_NO = ".$oshiraseNo." ";
        $query .= "    AND DATA_DIV = '".$model->dataDiv."' ";
        if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            $query .= "    AND SCHOOLCD = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        
        return $query;
    }
}
?>