<?php

require_once('for_php7.php');

class knje301Query extends Query {

    //出身学校年度一覧取得
    function selectQuery($model)
    {
        $tableName = "FINSCHOOL_MST";
        $tableName2 = "V_FINSCHOOL_MST";
        $query  = " select T1.FINSCHOOLCD, T1.FINSCHOOL_NAME, T2.NAME1 ";
        $query .= " from {$tableName2} T1 ";
        $query .= "     INNER JOIN {$tableName} I1 ON T1.FINSCHOOLCD = I1.FINSCHOOLCD ";
        $query .= "     LEFT  JOIN NAME_MST T2 ON T2.NAMECD2 = I1.FINSCHOOL_TYPE ";
        $query .= "                           AND T2.NAMECD1 = 'L019' ";
        $query .= " where T1.YEAR = '".$model->year."'";
        $query .= "     AND ((NOT EXISTS( ";
        $query .= "         SELECT * FROM STAFF_RECRUIT_DAT WHERE STAFFCD='".$model->field['STAFFCD']."' AND RECRUIT_DIV = '1' AND FINSCHOOLCD=T1.FINSCHOOLCD ";
        $query .= "         AND STAFF_RECRUIT_DAT.YEAR=T1.YEAR ";
        $query .= "     ) ";
        if($model->selectdata){
            $query .= " AND T1.FINSCHOOLCD NOT IN('".str_replace(',',"','",$model->selectdata)."')";
        }
        $query .=" ) ";
        if($model->selectdata_del){
            $query .= " OR T1.FINSCHOOLCD IN('".str_replace(',',"','",$model->selectdata_del)."')";
        }
        $query .=" ) ";
        if($model->field['kousyu']!=''){
            $query .= " AND T2.NAMECD2 ='".$model->field['kousyu']."'";
        }
        $query .= " order by T1.FINSCHOOLCD";
        
        return $query;
    }
    //出身学校年度一覧取得
    function selectQuery2($model)
    {
        $tableName = "FINSCHOOL_MST";
        $tableName2 = "V_FINSCHOOL_MST";
        $query  = " select T1.FINSCHOOLCD, T1.FINSCHOOL_NAME, T2.NAME1 ";
        $query .= " from {$tableName2} T1 ";
        $query .= "     INNER JOIN {$tableName} I1 ON T1.FINSCHOOLCD = I1.FINSCHOOLCD ";
        $query .= "     LEFT  JOIN NAME_MST T2 ON T2.NAMECD2 = I1.FINSCHOOL_TYPE ";
        $query .= "                           AND T2.NAMECD1 = 'L019' "; 
        $query .= " where T1.YEAR = '".$model->year."'";
        $query .= "     AND ((EXISTS( ";
        $query .= "         SELECT * FROM STAFF_RECRUIT_DAT WHERE STAFFCD='".$model->field['STAFFCD']."' AND RECRUIT_DIV = '1' AND FINSCHOOLCD=T1.FINSCHOOLCD ";
        $query .= "         AND STAFF_RECRUIT_DAT.YEAR=T1.YEAR ";
        $query .= "     ) ";
        if($model->selectdata){
            $query .= " OR T1.FINSCHOOLCD IN('".str_replace(',',"','",$model->selectdata)."')";
        }
        $query .=" ) ";
        if($model->selectdata_del){
            $query .= " AND T1.FINSCHOOLCD NOT IN('".str_replace(',',"','",$model->selectdata_del)."')";
        }
        $query .=" ) ";
        $query .= " order by T1.FINSCHOOLCD";
        
        return $query;
    }
    
    function selectQueryPrischool($model)
    {
        $query .= " SELECT ";
        $query .= "     VALUE(T1.PRISCHOOLCD,'') || '-' || VALUE(T2.PRISCHOOL_CLASS_CD,'') AS VALUE, ";
        $query .= "     T1.PRISCHOOL_NAME AS NAME1, ";
        $query .= "     T2.PRISCHOOL_NAME AS NAME2 ";
        $query .= " FROM ";
        $query .= "     PRISCHOOL_MST T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     PRISCHOOL_CLASS_MST T2 ";
        $query .= "     ON T2.PRISCHOOLCD=T1.PRISCHOOLCD ";
        $query .= " WHERE ";
        $query .= "     ((NOT EXISTS( ";
        $query .= "         SELECT * FROM STAFF_RECRUIT_DAT STF WHERE STF.STAFFCD='".$model->field['STAFFCD']."' AND STF.RECRUIT_DIV = '2' ";
        $query .= "         AND STF.PRISCHOOLCD || '-' || STF.PRISCHOOL_CLASS_CD = VALUE(T1.PRISCHOOLCD,'') || '-' || VALUE(T2.PRISCHOOL_CLASS_CD,'0') ";
        $query .= "         AND STF.YEAR='{$model->year}' ";
        $query .= "     ) ";
        if($model->selectdata){
            $query .= " AND VALUE(T1.PRISCHOOLCD,'') || '-' || VALUE(T2.PRISCHOOL_CLASS_CD,'') NOT IN('".str_replace(',',"','",$model->selectdata)."')";
        }
        $query .=" ) ";
        if($model->selectdata_del){
            $query .= " OR VALUE(T1.PRISCHOOLCD,'') || '-' || VALUE(T2.PRISCHOOL_CLASS_CD,'') IN('".str_replace(',',"','",$model->selectdata_del)."')";
        }
        $query .=" ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        
        return $query;
    }
    
    function selectQueryPrischool2($model)
    {
        $query .= " SELECT ";
        $query .= "     VALUE(T1.PRISCHOOLCD,'') || '-' || VALUE(T2.PRISCHOOL_CLASS_CD,'') AS VALUE, ";
        $query .= "     T1.PRISCHOOL_NAME AS NAME1, ";
        $query .= "     T2.PRISCHOOL_NAME AS NAME2 ";
        $query .= " FROM ";
        $query .= "     PRISCHOOL_MST T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     PRISCHOOL_CLASS_MST T2 ";
        $query .= "     ON T2.PRISCHOOLCD=T1.PRISCHOOLCD ";
        $query .= " WHERE ";
        $query .= "     ((EXISTS( ";
        $query .= "         SELECT * FROM STAFF_RECRUIT_DAT STF WHERE STF.STAFFCD='".$model->field['STAFFCD']."' AND STF.RECRUIT_DIV = '2' ";
        $query .= "         AND STF.PRISCHOOLCD || '-' || STF.PRISCHOOL_CLASS_CD = VALUE(T1.PRISCHOOLCD,'') || '-' || VALUE(T2.PRISCHOOL_CLASS_CD,'0') ";
        $query .= "         AND STF.YEAR='{$model->year}' ";
        $query .= "     ) ";
        if($model->selectdata){
            $query .= " OR VALUE(T1.PRISCHOOLCD,'') || '-' || VALUE(T2.PRISCHOOL_CLASS_CD,'') IN('".str_replace(',',"','",$model->selectdata)."')";
        }
        $query .=" ) ";
        if($model->selectdata_del){
            $query .= " AND VALUE(T1.PRISCHOOLCD,'') || '-' || VALUE(T2.PRISCHOOL_CLASS_CD,'') NOT IN('".str_replace(',',"','",$model->selectdata_del)."')";
        }
        $query .=" ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        
        return $query;
    }
    
    //対象年度のデータチェック
    function getSelectQueryDataCount($model, $year)
    {
        $query = " SELECT COUNT(*) from STAFF_RECRUIT_DAT where year='{$year}' ";
        
        return $query;
    }
    
    //出身学校年度一覧取得
    function selectYearQuery()
    {
        return "SELECT DISTINCT YEAR FROM FINSCHOOL_YDAT GROUP BY YEAR ORDER BY YEAR desc";
    }
    
    //出身学校科目一覧取得
    function selectJuniorQuery($FINSCHOOL_cd_array,$model)
    {
        $query  = "select FINSCHOOLCD, FINSCHOOL_NAME ";
        $query .= "from FINSCHOOL_MST m ";
        $query .= "where not exists (select 'X' ";
        $query .= "                  from FINSCHOOL_YDAT y ";
        $query .= "                  where y.YEAR = '".$model->year."'";
        $query .= "                  and y.finschoolcd = m.finschoolcd) ";
        $query .= "ORDER BY FINSCHOOLCD";
        
        return $query;
    }
    
    //担当者一覧取得
    function getStaffList($model)
    {
        $query .= " SELECT ";
        $query .= "     STAFFNAME AS LABEL, ";
        $query .= "     STAFFCD as VALUE ";
        $query .= " FROM ";
        $query .= "     STAFF_MST; ";
        
        return $query;
    }
    
    //校種一覧取得
    function getKousyuList($model)
    {
        $query .= " SELECT ";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1='L019' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2; ";
        
        return $query;
    }
    /**
     * UPDATE
     */
    function &getUpdateQuery(&$model)
    {
        //出身学校コード配列に格納
        $array = explode (",", $model->selectdata);
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        
        if($model->field['kubun'] == 1){
            $query = "DELETE FROM STAFF_RECRUIT_DAT WHERE YEAR = '" . $model->year . "' AND STAFFCD = '" . $model->field['STAFFCD'] . "' AND RECRUIT_DIV = '1' ";
        } else if($model->field['kubun'] == 2){
            $query = "DELETE FROM STAFF_RECRUIT_DAT WHERE YEAR = '" . $model->year . "' AND STAFFCD = '" . $model->field['STAFFCD'] . "' AND RECRUIT_DIV = '2' ";
        }
        $db->query($query);
        
        if($model->selectdata != ''){
	        for ($i = 0; $i < get_count($array); $i++){
	        	if($model->field['kubun'] == 1){
	                $data["YEAR"][TEXT]               = $model->year;
	                $data["RECRUIT_DIV"][TEXT]        = 1;
	                $data["STAFFCD"][TEXT]            = $model->field['STAFFCD'];
	                $data["FINSCHOOLCD"][TEXT]        = $array[$i];
	                $data["PRISCHOOLCD"][TEXT]        = 0;
	                $data["PRISCHOOL_CLASS_CD"][TEXT] = 0;
	            }
	        	if($model->field['kubun'] == 2){
	                $list = explode ("-", $array[$i]);
	                
	                $data["YEAR"][TEXT]               = $model->year;
	                $data["RECRUIT_DIV"][TEXT]        = 2;
	                $data["STAFFCD"][TEXT]            = $model->field['STAFFCD'];
	                $data["FINSCHOOLCD"][TEXT]        = 0;
	                $data["PRISCHOOLCD"][TEXT]        = $list[0];
	                if (!$list[1]) {
	                     $list[1] = 0;
	                }
	                $data["PRISCHOOL_CLASS_CD"][TEXT] = $list[1];
	            }
	            $data["REGISTERCD"][TEXT]         = STAFFCD;
	            $data["UPDATED"][NUMBER]          = "SYSDATE()";
	            $query = Query::insertSQL($data, "STAFF_RECRUIT_DAT");
	            $db->query($query);
	        }
        }
        $db->commit(); // トランザクションをコミットする。

        Query::dbCheckIn($db);
        return;
    }
    
    function selectQueryDataCopy($db, $model, $year)
    {
        $db->autoCommit(false);
        
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     RECRUIT_DIV, ";
        $query .= "     STAFFCD, ";
        $query .= "     FINSCHOOLCD, ";
        $query .= "     PRISCHOOLCD, ";
        $query .= "     PRISCHOOL_CLASS_CD, ";
        $query .= "     REGISTERCD ";
        $query .= " FROM ";
        $query .= "     STAFF_RECRUIT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" . ($year - 1) . "' ";
        
        $result = $db->query($query);
        
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //前年度のデータを今年度のデータとしてINSERT

	        $data = array();
	        $data["YEAR"][TEXT]               = $year;
	        $data["RECRUIT_DIV"][DATE]        = $row['RECRUIT_DIV'];
	        $data["STAFFCD"][TEXT]            = $row['STAFFCD'];
	        $data["FINSCHOOLCD"][TEXT]        = $row['FINSCHOOLCD'];
	        $data["PRISCHOOLCD"][TEXT]        = $row['PRISCHOOLCD'];
	        $data["PRISCHOOL_CLASS_CD"][TEXT] = $row['PRISCHOOL_CLASS_CD'];
	        $data["REGISTERCD"][TEXT]         = $row['REGISTERCD'];
	        $data["UPDATED"][FUNC]            = "sysdate()";
	        
	        $query = Query::insertSQL($data, "STAFF_RECRUIT_DAT");

            $db->query($query);
        }
        
        $db->commit();

        return;
    }
}
?>
