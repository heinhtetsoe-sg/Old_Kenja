<?php

require_once('for_php7.php');

class knjd020Query extends Query {

    function getRow($model)
    {
        $testcd = $model->field["testcd"];
        $db  = Query::dbCheckOut();

        $query  = "select distinct k1.chaircd";
        $query .= "      ,k1.chairname";
        $query .= "      ,w1.testkindcd || w1.testitemcd as testcd";
        $query .= "      ,w1.testitemname";
        $query .= "      ,t1.operation_date";
        $query .= "      ,t1.perfect";
        $query .= "      ,t1.rate";
        $query .= "      ,t1.updated";
        $query .= "  from chair_dat k1,";
        $query .= "       testitem_mst w1,";
        $query .= " (";
        $query .= "     select testkindcd";
        $query .= "           ,testitemcd";
        $query .= "           ,chaircd";
        $query .= "           ,operation_date";
        $query .= "           ,perfect";
        $query .= "           ,rate";
        $query .= "           ,updated";
        $query .= "      from testscore_hdat";
        $query .= "     where year     = '". CTRL_YEAR ."'";
        $query .= "       and semester = '". $model->field["SEMESTER"] ."'";
        $query .= "       and chaircd = '". $model->field["chairSel"] ."'";
        $query .= " ) t1";
        $query .= " where w1.testkindcd = t1.testkindcd";
        $query .= "   and w1.testitemcd = t1.testitemcd";
        $query .= "   and w1.testkindcd = '". substr($testcd, 0, 2) ."'";
        $query .= "   and w1.testitemcd = '". substr($testcd, 2, 2) ."'";
        $query .= "   and w1.subclasscd ='".$model->field["SUBCLASSCD"] ."'";
        $query .= "   and k1.chaircd    = t1.chaircd";
        $query .= "   and k1.chaircd    = '". $model->field["chairSel"] ."'";
#        $query .= "   and k1.semester   = '". $model->field["SEMESTER"] ."'";
        $query .= "   and k1.year       = '".CTRL_YEAR ."'";
        $query .= "   and k1.semester   = '".$model->field["SEMESTER"] ."'";
        $query .= "   and k1.subclasscd ='".$model->field["SUBCLASSCD"] ."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        
        return $row;
    }

    function GetStartDate($chaircd)
    {
        $db  = Query::dbCheckOut();
        $query  = "SELECT CHAIRCD";
        $query .= "      ,MIN(DATE(EXECUTEDATE)) STARTDAY";
        $query .= "      ,MAX(DATE(EXECUTEDATE)) ENDDAY";
        $query .= "  FROM SCH_CHR_DAT";
        $query .= " WHERE YEAR = '".CTRL_YEAR. "'";
        if (is_array($chaircd)) {
            $query .= "  AND CHAIRCD    in ( '". implode($chaircd,"','") ."')";
        } else {
            $query .= "   AND CHAIRCD ='".$chaircd."'";
        }
        $query .= "   AND DATADIV = '0'";
        $query .= "   AND PERIODCD > '0'";
        $query .= " group by chaircd";
        
        if (is_array($chaircd)) {
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $arr_row[$row["CHAIRCD"]]["STARTDAY"] = $row["STARTDAY"];
                $arr_row[$row["CHAIRCD"]]["ENDDAY"] = $row["ENDDAY"];
            }
            Query::dbCheckIn($db);
            return $arr_row;
        } else {
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            Query::dbCheckIn($db);
            return $row;
        }
        return $row;
    }
    
    function getHr_class($model)
    {
        $hr_nameabbv = "";
        $db  = Query::dbCheckOut();

/*        //元々あったSQL文
        $query  = "select DISTINCT s.hr_nameabbv ";
        $query .= "  from schreg_regd_hdat s,chair_cls_dat c";
        $query .= " where s.year       = '". CTRL_YEAR ."'";
        $query .= "   and s.semester   = '".CTRL_SEMESTER."'";
        $query .= "   and c.year       = s.year";
        $query .= "   and c.semester   = s.semester";
        $query .= "   and s.grade      = c.trgtgrade";
        $query .= "   and s.hr_class   = c.trgtclass";
        $query .= "   and c.chaircd    = '" .$model->field["chairSel"]."'";
        $query .= " order by s.hr_nameabbv ";
*/

        $query  = " SELECT DISTINCT 
                          w1.hr_nameabbv 
                      FROM schreg_regd_hdat w1,
                           (SELECT k1.year, k1.semester, k2.chaircd,
                                   k1.trgtgrade, k1.trgtclass 
                              FROM chair_cls_dat k1, chair_dat k2
                             WHERE k1.year       = k2.year
                               AND k1.semester   = k2.semester
                               AND k1.chaircd    = '0000000'
                               AND k1.year       = '".CTRL_YEAR."'
                               AND k1.semester   = '".CTRL_SEMESTER."'
                               AND k1.groupcd    = k2.groupcd
                               AND k2.subclasscd = '".$model->field["SUBCLASSCD"]."' 
                             UNION
                            SELECT t1.year, t1.semester, t2.chaircd,
                                    t1.trgtgrade, t1.trgtclass
                              FROM chair_cls_dat t1, chair_dat t2
                             WHERE t1.year       = t2.year
                               AND t1.semester   = t2.semester
                               AND t1.groupcd    = t2.groupcd
                               AND t1.year       = '".CTRL_YEAR."'
                               AND t1.semester   = '".CTRL_SEMESTER."'
                               AND t1.chaircd    = t2.chaircd
                               AND t2.subclasscd = '".$model->field["SUBCLASSCD"]."') W2 
                     WHERE w1.year       = '".CTRL_YEAR."' AND 
                           w1.semester   = '".CTRL_SEMESTER."' AND 
                           w2.year       = w1.year AND 
                           w2.semester   = w1.semester AND 
                           w1.grade      = w2.trgtgrade AND
                           w1.hr_class   = w2.trgtclass AND 
                           w2.chaircd    = '".$model->field["chairSel"]."'
                     ORDER BY w1.hr_nameabbv ";

        //echo $query;
        $result = $db->query($query);
        $sep = "";
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $hr_nameabbv = $hr_nameabbv.$sep.$row["HR_NAMEABBV"];
            $sep = ",";
        }
        
        $query  = "select chairname,chaircd";
        $query .= "  from chair_dat ";
        $query .= " where year       = '". CTRL_YEAR ."'";
        $query .= "   and semester   = '" .$model->field["SEMESTER"]."'";
        $query .= "   and chaircd    = '" .$model->field["chairSel"]."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $result->free();
        Query::dbCheckIn($db);
        $name = array("hr_nameabbv" => $hr_nameabbv , "chairname" => $row["CHAIRCD"]." ".$row["CHAIRNAME"]);
        return $name;
    }
        
    function getTestItem($model)
    {
        $subclasscd = (isset($model->field["SUBCLASSCD"])) ? $model->field["SUBCLASSCD"] : "";
        $db  = Query::dbCheckOut();
        $opt = array();

        $query  = "select distinct t.testkindcd || t.testitemcd as testcd";
        $query .= "      ,t.testitemname";
        $query .= "  from testitem_mst t,chair_dat c";
        $query .= " where t.year       = '". CTRL_YEAR ."'";
        $query .= "   and t.subclasscd = c.subclasscd";
        $query .= "   and t.subclasscd ='".$subclasscd ."'";
        $query .= " order by testcd";
        $result = $db->query($query);
        
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["TESTCD"]."　".$row["TESTITEMNAME"], 
                           "value" => $row["TESTCD"]);
        }
        $result->free();
        Query::dbCheckIn($db);
        return $opt;
    }
    
    function selectQuery($model)
    {
        $order = array("desc", "asc");

        $subclasscd = (isset($model->field["SUBCLASSCD"])) ? $model->field["SUBCLASSCD"] : "";
        $semester   = (isset($model->field["SEMESTER"])) ? $model->field["SEMESTER"] : "";
        $chaircd    = (isset($model->field["CHAIRCD"])) ? $model->field["CHAIRCD"] : array();
        
        $query .= " SELECT distinct c1.chaircd";
        $query .= "       ,c1.chairname";
        $query .= "       ,h1.testkindcd || h1.testitemcd as testcd";
        $query .= "       ,m1.testitemname";
        $query .= "       ,h1.operation_date";
        $query .= "       ,h1.operation_flg";
        $query .= "       ,h1.perfect";
        $query .= "       ,h1.rate";
        $query .= "   FROM testscore_hdat h1 ";
        $query .= "       ,testitem_mst m1 ";
        $query .= "       ,chair_dat c1";
        $query .= "  WHERE h1.year     = '". CTRL_YEAR ."'";
        $query .= "    AND h1.semester = '". $semester ."'";
        $query .= "    AND h1.chaircd in ('". implode($chaircd, "','") ."')";
        $query .= "    AND c1.subclasscd = '".$subclasscd ."'";
        $query .= "    AND c1.chaircd = h1.chaircd";
        $query .= "    AND c1.year = h1.year";
        $query .= "    AND c1.semester = h1.semester";
        $query .= "    AND c1.subclasscd = m1.subclasscd";
        $query .= "    AND h1.testkindcd = m1.testkindcd";
        $query .= "    AND h1.testitemcd = m1.testitemcd";
        $query .= "    AND h1.year       = m1.year";

        switch ($model->s_id)
        {
            case "1":
                $query .= " ORDER BY c1.chaircd ".$order[$model->sort[$model->s_id]].", testcd"; 
                break;

            case "2":
                $query .= " ORDER BY testcd ".$order[$model->sort[$model->s_id]].", c1.chaircd"; 
                break;
        }        
        return $query;
    }

    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        
        $data["TESTKINDCD"][TEXT]       = substr($model->new_field["TESTCD"], 0, 2);
        $data["TESTITEMCD"][TEXT]       = substr($model->new_field["TESTCD"], 2, 2);
        $data["OPERATION_DATE"][FUNC]   = "date('".str_replace("/","-",$model->new_field["OPERATION_DATE"])."')";     
        $data["PERFECT"][NUMBER]        = $model->new_field["PERFECT"];
        $data["RATE"][NUMBER]           = $model->new_field["RATE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        
        $where  = "where year           = '". CTRL_YEAR ."'";
        $where .= "  and semester       = '". $model->field["SEMESTER"] ."'";
        $where .= "  and chaircd        = '". $model->field["chairSel"] ."'";
        $where .= "  and testkindcd     = '". substr($model->field["testcd"], 0, 2) ."'";
        $where .= "  and testitemcd     = '". substr($model->field["testcd"], 2, 2) ."'";

        $query = Query::updateSQL($data, "testscore_hdat", $where);
        $db->query($query);
        Query::dbCheckIn($db);
    }

    //戻り値：０→追加可能 １→追加不可
    function RecordCheck2($model)
    {
        $row = knjd020Query::GetStartDate($model->field["CHAIRCD"]);
        $db = Query::dbCheckOut();
        $num = "0";
        if ($model->new_field["ALL"] !=1) { //選択講座全てに適応チェックじゃないとき

            $query  = "select COUNT(*) from testscore_hdat";
            $query .= " where year       = '". CTRL_YEAR ."'";
            $query .= "   and semester   = '". $model->field["SEMESTER"] ."'";
            $query .= "   and testkindcd = '". substr($model->new_field["TESTCD"], 0, 2) ."'";
            $query .= "   and testitemcd = '". substr($model->new_field["TESTCD"], 2, 2) ."'";
            $query .= "   and chaircd    = '". $model->field["chairSel"] . "'";  
            $num = $db->getOne($query);

            Query::dbCheckIn($db);
            return $num;
            
        } else { //選択講座全てに適応チェックのとき

            foreach ($model->field["CHAIRCD"] as $val) {

                $query  = " select COUNT(*) from testscore_hdat";
                $query .= " where year       = '". CTRL_YEAR ."'";
                $query .= "   and semester   = '". $model->field["SEMESTER"] ."'";
                $query .= "   and testkindcd = '". substr($model->new_field["TESTCD"], 0, 2) ."'";
                $query .= "   and testitemcd = '". substr($model->new_field["TESTCD"], 2, 2) ."'";
                $query .= "   and chaircd    = '". $val . "'";
                $num = $db->getOne($query);
                if ($num != "0") break;

            }
            Query::dbCheckIn($db);
            return $num;        
        }
    }
    
    function &getInsertQuery($model)
    {
        if ($model->new_field["ALL"] !=1) {
            $db = Query::dbCheckOut();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
            $data["TESTKINDCD"][TEXT]       = substr($model->new_field["TESTCD"], 0, 2);
            $data["TESTITEMCD"][TEXT]       = substr($model->new_field["TESTCD"], 2, 2);
            $data["CHAIRCD"][TEXT]          = $model->field["chairSel"];
            $data["OPERATION_DATE"][FUNC]   = "date('".str_replace("/","-",$model->new_field["OPERATION_DATE"])."')"; 
            $data["PERFECT"][NUMBER]        = $model->new_field["PERFECT"];
            $data["RATE"][NUMBER]           = $model->new_field["RATE"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            $query = Query::insertSQL($data, "testscore_hdat");
            $db->query($query);
            Query::dbCheckIn($db);
        } else {
            $db = Query::dbCheckOut();
            $db->autoCommit(false);
            foreach ($model->field["CHAIRCD"] as $val) {
                    
                $query  = " select COUNT(*) from testscore_hdat";
                $query .= " where year          = '". CTRL_YEAR ."'";
                $query .= "   and semester      = '". $model->field["SEMESTER"] ."'";
                $query .= "   and testkindcd    = '". substr($model->new_field["TESTCD"], 0, 2) ."'";
                $query .= "   and testitemcd    = '". substr($model->new_field["TESTCD"], 2, 2) ."'";
                $query .= "   and chaircd       = '". $val . "'";  
                            
                $num = $db->getOne($query);
                if ($num ==0) {
                    $data = array();
                    $data["YEAR"][TEXT]             = CTRL_YEAR;
                    $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
                    $data["CHAIRCD"][TEXT]          = $val;
                    $data["TESTKINDCD"][TEXT]       = substr($model->new_field["TESTCD"], 0, 2);
                    $data["TESTITEMCD"][TEXT]       = substr($model->new_field["TESTCD"], 2, 2);
                    $data["OPERATION_DATE"][FUNC]   = "date('".str_replace("/","-",$model->new_field["OPERATION_DATE"])."')";
                    $data["PERFECT"][NUMBER]        = $model->new_field["PERFECT"];
                    $data["RATE"][NUMBER]           = $model->new_field["RATE"];
                    $data["REGISTERCD"][TEXT]       = STAFFCD;
                    $data["UPDATED"][FUNC]          = "sysdate()";
                    $query = Query::insertSQL($data, "testscore_hdat");
                    $db->query($query);
                
                }
            }
            $db->commit();
            Query::dbCheckIn($db);
        }
    }
    
    function &getDeleteQuery($model)
    {
        if ($model->new_field["ALL"]==1) {
            $result = knjd020Query::all_check($model);
        }
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        
        //TESTSCORE_DATからの削除
        $query  = "DELETE FROM testscore_dat ";
        $query .= " WHERE year        = '". $model->field["YEAR"] ."'";
        $query .= "   AND semester    = '". $model->field["SEMESTER"] ."'";
        $query .= "   AND subclasscd  = '". $model->field["SUBCLASSCD"] ."'";
        $query .= "   AND testkindcd  = '". substr($model->new_field["TESTCD"], 0, 2) ."'";
        $query .= "   AND testitemcd  = '". substr($model->new_field["TESTCD"], 2, 2) ."'";
        if ($model->new_field["ALL"]==1 && is_array($result["CHAIRCD"])) {
            $query .= " AND chaircd  IN ( '". implode($result["CHAIRCD"],"','") ."')";
        } else {
            $query .= " AND chaircd  = '". $model->field["chairSel"] ."'";
        }
        $db->query($query);
        
        //TESTSCORE_HDATからの削除
        $query  = "delete from testscore_hdat ";
        $query .= "where year           = '".$model->field["YEAR"]."'";
        $query .= "  and semester       = '". $model->field["SEMESTER"] ."'";
        $query .= "  and testkindcd     = '". substr($model->new_field["TESTCD"], 0, 2) ."'";
        $query .= "  and testitemcd     = '". substr($model->new_field["TESTCD"], 2, 2) ."'";

        if ($model->new_field["ALL"]==1 && is_array($result["CHAIRCD"])) {
            $query .= "  and chaircd    in ( '". implode($result["CHAIRCD"],"','") ."')";
        } else {
            $query .= "  and chaircd    = '". $model->field["chairSel"] ."'";
        }
        $db->query($query);
        
        $db->commit();        
        Query::dbCheckIn($db);      
    }
    
    //全てに適応のチェックが入っている場合の全ての講座コードを取得
    function all_check($model) 
    {
        $cnt = 0;
        $all_data = array();
        $db = Query::dbCheckOut();
        
        $query = knjd020Query::selectQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($model->new_field["TESTCD"]==$row["TESTCD"]) {
                $cnt++;
                $all_data["CHAIRCD"][$cnt] = $row["CHAIRCD"];
                $all_data["TESTKINDCD"][$cnt] = substr($row["TESTCD"], 0, 2);
                $all_data["TESTITEMCD"][$cnt] = substr($row["TESTCD"], 2, 2);
            }
        }
        
        Query::dbCheckIn($db);
        
        $all_data["cnt"] = $cnt;
        return $all_data;
    }
    
    //対象テストのデータをチェックする
    function getTestscoreCount($model,$testcd,$flg=0)
    {
        $db = Query::dbCheckOut();
        $query  = "SELECT COUNT(*) FROM testscore_dat";
        $query .= " WHERE year = '".CTRL_YEAR."'";
        $query .= "   AND semester = '".$model->field["SEMESTER"]."'";
        $query .= "   AND testkindcd = '".substr($testcd,0,2)."'";
        $query .= "   AND testitemcd = '".substr($testcd,2,2)."'";
        $query .= "   AND subclasscd = '".$model->field["SUBCLASSCD"]."'";
        $query .= "   AND chaircd    = '".$model->field["chairSel"]."'";
        
        if ($flg == 1) {
            $query .= " AND score > ".$model->new_field["PERFECT"];
        }
        $num_rows = $db->getOne($query);
        Query::dbCheckIn($db);
        return $num_rows;
    }
}
?>
