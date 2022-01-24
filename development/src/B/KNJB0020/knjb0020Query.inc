<?php

require_once('for_php7.php');

class knjb0020Query extends Query {

    //職員リスト
    function GetStaff($sectioncd)
    {
        $query  = "SELECT T1.staffcd
                         ,T1.sectioncd || ' ' || T2.sectionname AS section
                         ,T1.jobcd || ' ' || T3.jobname AS job
                         ,T1.staffname AS name
                         ,T1.staffname_kana AS name_kana
                     FROM v_staff_mst T1 LEFT OUTER JOIN v_section_mst T2
                                           ON T1.year      = T2.year
                                          AND T1.sectioncd = T2.sectioncd
                                         LEFT OUTER JOIN v_job_mst T3
                                           ON T1.year      = T3.year
                                          AND T1.jobcd     = T3.jobcd
                    WHERE T1.year = '".CTRL_YEAR."'";
        if ($sectioncd != "all")
            $query .= " AND T1.sectioncd = '".$sectioncd."'";

        return $query;    
    }

    //事前チェック（職員マスタ）
    function PreCheckStaffMst() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_STAFF_MST T1, ";
        $query .= "     V_SECTION_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR      = T2.YEAR AND ";
        $query .= "     T1.YEAR      = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SECTIONCD = T2.SECTIONCD ";

        return $query;
    }

    //所属
    function GetSection()
    {
        return "SELECT sectioncd,sectionname FROM v_section_mst WHERE year = '".CTRL_YEAR."'";
    }
    
    //校時
    function GetPeriod()
    {
        return "SELECT namecd2 AS periodcd, name1 AS periodname FROM v_name_mst WHERE year = '".CTRL_YEAR."' AND namecd1 = 'B001'";
    }
    
    //職員稼動不可情報
    function GetOpeimposs($staffcd)
    {
        return "SELECT * FROM STAFF_OPEIMPOSS_DAT WHERE year = '".CTRL_YEAR."' AND staffcd = '".$staffcd."'";
    
    }

    //更新
    function &UpdateQuery($model)
    {
        $db = Query::dbCheckOut();

        $query = "DELETE FROM STAFF_OPEIMPOSS_DAT
                   WHERE year     = '".CTRL_YEAR."'
                     AND staffcd  = '".$model->staffcd."'";
        $db->query($query);        
        
        if (is_array($model->data)){
			foreach ($model->data as $val)
    	    {
        	    $cd = explode("-",$val);
            	$query = "INSERT INTO STAFF_OPEIMPOSS_DAT
                	      VALUES ('".CTRL_YEAR."','".$cd[0]."','".$cd[1]."','".$model->staffcd."','".STAFFCD."',SYSDATE())";

	            $db->query($query);            
    	    }
        }
        Query::dbCheckIn($db);
    }
    
    //コピー
    function &CopyQuery($model)
    {
        $cnt = 0;
        $db = Query::dbCheckOut();
        $result = $db->query(knjb0020Query::GetStaff($model->sectioncd));

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //当年度データチェック
            $query = "SELECT COUNT(*) FROM STAFF_OPEIMPOSS_DAT
                       WHERE staffcd = '".$row["STAFFCD"]."'
                         AND year    = '".CTRL_YEAR."'";
            
            //当年度データがなければ前年度データをコピー
            if ((int)$db->getOne($query) == 0)
            {
                $query = "SELECT COUNT(*) FROM STAFF_OPEIMPOSS_DAT
                           WHERE year    = '".((int)CTRL_YEAR - 1)."'
                             AND staffcd = '".$row["STAFFCD"]."'
                             AND periodcd in (SELECT namecd2 FROM name_ydat
                                               WHERE year = '".CTRL_YEAR."' AND namecd1 = 'B001')";
                
                if ((int)$db->getOne($query) != 0) {
                
                    $query = "INSERT INTO STAFF_OPEIMPOSS_DAT
                              SELECT '".CTRL_YEAR."', daycd, periodcd, staffcd, '".STAFFCD."', SYSDATE() FROM STAFF_OPEIMPOSS_DAT
                               WHERE year    = '".((int)CTRL_YEAR - 1)."'
                                 AND staffcd = '".$row["STAFFCD"]."'
                                 AND periodcd in (SELECT namecd2 FROM name_ydat
                                                   WHERE year = '".CTRL_YEAR."' AND namecd1 = 'B001')";
                    $db->query($query);
                    $cnt++;
                }
            }        
        }
        Query::dbCheckIn($db);        
        return $cnt;
    }
    
    //削除
    function DeleteQuery($staffcd)
    {
        $db = Query::dbCheckOut();
        
        $query = "DELETE FROM STAFF_OPEIMPOSS_DAT
                   WHERE year    = '".CTRL_YEAR."'
                     AND staffcd = '".$staffcd."'";
                      
        $res = $db->query($query);
        Query::dbCheckIn($db);
        return $res;            
    }
}
?>
