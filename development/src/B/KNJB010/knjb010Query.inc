<?php

require_once('for_php7.php');

class knjb010Query extends Query {

    //同時展開群リスト
    function SelectQuery($flg)
    {
            if ($flg == 1) {
                $query = "SELECT DISTINCT T1.groupcd ";
            } else {
                $query = "SELECT t1.groupcd, t1.groupname, t2.lessoncnt, t2.framecnt, T2.l_cnt, T2.f_cnt, T3.chaircd, T3.chairname, 
                             T5.hr_name ";
            }
                $query.= "  FROM 
                            (SELECT * FROM v_electclass_mst WHERE year = '".CTRL_YEAR."') T1 
                            LEFT OUTER JOIN 
                                (SELECT year, groupcd, 
                                        MAX(lessoncnt) AS lessoncnt, 
                                        MAX(framecnt) AS framecnt,
                                        COUNT(DISTINCT lessoncnt) AS l_cnt,
                                        COUNT(DISTINCT framecnt) AS f_cnt                                    
                                   FROM chair_dat
                                  WHERE year     = '".CTRL_YEAR."' 
                                    AND semester = '".CTRL_SEMESTER."' 
                                  GROUP BY year, groupcd) T2 
                              ON t1.year    = T2.year
                             AND t1.groupcd = t2.groupcd
                            LEFT OUTER JOIN 
                                chair_dat T3 
                              ON T1.year     = t3.year
                             AND t1.groupcd  = T3.groupcd
                             AND t3.semester = '".CTRL_SEMESTER."'
                            LEFT OUTER JOIN
                                (SELECT T1.groupcd,T2.hr_name
                                   FROM chair_cls_dat T1, schreg_regd_hdat T2 
                                  WHERE T2.year     = '".CTRL_YEAR."'
                                    AND T2.semester = '".CTRL_SEMESTER."'
                                    AND T2.grade || '-' || T2.hr_class = T1.trgtgrade || '-' || T1.trgtclass
                                    AND T1.year = T2.year
                                    AND T1.semester = T2.semester ) T5
                             ON T1.groupcd = T5.groupcd
                          WHERE T1.groupcd <> '0000'  
                          ORDER BY T1.groupcd";
        return $query;    
    }
    
    //受講クラス
    function selectQueryChairClsDat($chaircd, $groupcd)
    {
        $query = "SELECT DISTINCT 
                          T1.TRGTGRADE,
                          T1.TRGTCLASS,
                          T2.HR_NAME 
                    FROM chair_cls_dat T1, schreg_regd_hdat T2
                   WHERE T1.year     = T2.year 
                     AND T1.semester = T2.semester
                     AND T1.trgtgrade = T2.grade
                     AND T1.trgtclass = T2.hr_class
                     AND T1.year     = '".CTRL_YEAR."'
                     AND T1.semester = '".CTRL_SEMESTER."'
                     AND T1.groupcd = '".$groupcd."'";                 
        return $query;
    }
    
    //各群情報
    function GetGroup($groupcd)
    {
        $query = " SELECT * FROM
                    (SELECT chaircd,chairname FROM chair_dat 
                      WHERE year     = '".CTRL_YEAR."'
                        AND semester = '".CTRL_SEMESTER."'
                        AND groupcd  = '".$groupcd."') T1,
                    (SELECT MAX(lessoncnt) AS lessoncnt, MAX(framecnt) AS framecnt
                        FROM chair_dat
                       WHERE year     = '".CTRL_YEAR."'
                         AND semester = '".CTRL_SEMESTER."'
                         AND groupcd  = '".$groupcd."') T2 ";
        return $query;
    }
        
    //講座一覧
    function GetChair($chaircd)
    {
        $query = "SELECT chaircd, chairname FROM chair_dat
                   WHERE year     = '".CTRL_YEAR."'
                     AND semester = '".CTRL_SEMESTER."'
                     AND groupcd = '0000'
                     AND chaircd NOT IN ('".implode("','",$chaircd)."')";
        return $query;
    }
    
    //クラス一覧
    function GetClass($class)
    {
        $query = "SELECT grade, hr_class, hr_name FROM schreg_regd_hdat
                   WHERE year     = '".CTRL_YEAR."'
                     AND semester = '".CTRL_SEMESTER."'
                     AND grade || '-' || hr_class NOT IN ('".implode("','",$class)."')";
        return $query; 
    }
        
    //前学期からコピー(前学期にデータがあって今学期のデータがまだ作成されていない場合のみ)
    function &CopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $result = $db->query(knjb010Query::SelectQuery(1));
        
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {    
            $cnt = array();
            //今学期が最初学期の場合は去年の最終学期を求めて条件にする
            if (CTRL_SEMESTER == $model->ctrl["SEMESTER"][0]) {
        
                $where = " WHERE year     = '".(CTRL_YEAR - 1)."'
                            AND groupcd  = '".$row["GROUPCD"]."'
                            AND semester = (SELECT MAX(semester) FROM semester_mst
                                           WHERE year = '".(CTRL_YEAR - 1)."' 
                                             AND semester <> '9')";
            //前学期を条件にする
            } else {
                $where = " WHERE year     = '".CTRL_YEAR."'
                              AND semester = '".$model->ctrl["SEMESTER"][(CTRL_SEMESTER - 2)]."'
                              AND groupcd  = '".$row["GROUPCD"]."'";
            }            

            #### 講座データ ####
            
            $cnt = knjb010Query::GetDataCount($db,"chair_dat",$where,$row["GROUPCD"]);
            if ((int)$cnt["LAST_CNT"] > 0 && (int)$cnt["CUR_CNT"] == 0) {
            
                $query = "INSERT INTO chair_dat
                          (SELECT '".CTRL_YEAR."','".CTRL_SEMESTER."', chaircd, 
                                  groupcd, subclasscd, chairname, takesemes,
                                  lessoncnt, framecnt,'".STAFFCD."',SYSDATE() FROM chair_dat ";
                $db->query($query.$where.")");
            }
            
            #### 講座受講クラスデータ ####
            
            $cnt = knjb010Query::GetDataCount($db,"chair_cls_dat",$where,$row["GROUPCD"]);
            if ((int)$cnt["LAST_CNT"] > 0 && (int)$cnt["CUR_CNT"] == 0) {
                
                $query = "INSERT INTO chair_cls_dat
                           (SELECT '".CTRL_YEAR."','".CTRL_SEMESTER."', chaircd, groupcd, trgtgrade, trgtclass, '".STAFFCD."', SYSDATE()
                             FROM chair_cls_dat ";
                $db->query($query.$where.")"); 
            }
        }
        Query::dbCheckIn($db); 
    }
    
    //前学期と今学期のデータ件数を取得
    function GetDataCount($db,$table,$where,$groupcd)
    {
        $count = array();
        $query = "SELECT t1.last_cnt, t2.cur_cnt 
                    FROM (SELECT COUNT(*) AS last_cnt FROM ".$table." ".$where." ) T1,
                         (SELECT COUNT(*) AS cur_cnt  FROM ".$table."
                           WHERE year     = '".CTRL_YEAR."' 
                             AND semester = '".CTRL_SEMESTER."' 
                             AND groupcd  = '".$groupcd."') T2 ";

        return $db->getRow($query,DB_FETCHMODE_ASSOC,$cnt);
    }
    
    //更新
    function &UpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        
        #### 講座データ ####
        
        $db->autoCommit(false);
        //一度群からはずす
        $query = "UPDATE chair_dat 
                     SET groupcd = '0000',
                         registercd = '".STAFFCD."',
                         updated    = SYSDATE() 
                   WHERE year     = '".CTRL_YEAR."'
                     AND semester = '".CTRL_SEMESTER."'
                     AND groupcd  = '".$model->groupcd."'"; 
        $db->query($query);
        
        $query = "UPDATE chair_dat 
                     SET groupcd    = '".$model->groupcd."'";
        $query .= "     ,lessoncnt  = ".((strlen($model->field["LESSONCNT"])) ? sprintf("%d",$model->field["LESSONCNT"]) : "CAST(NULL AS SMALLINT)");
        $query .= "     ,framecnt   = ".((strlen($model->field["FRAMECNT"])) ? sprintf("%d",$model->field["FRAMECNT"]) : "CAST(NULL AS SMALLINT)");
        $query .= "     ,registercd = '".STAFFCD."', 
                         updated    = SYSDATE()
                   WHERE year     = '".CTRL_YEAR."'
                     AND semester = '".CTRL_SEMESTER."'
                     AND chaircd  IN ('".str_replace(",","','",$model->field["CHAIRCD"])."')";                   
        $db->query($query);
        $db->commit();
        
        #### 講座受講クラスデータ ####
        
        $db->autoCommit(false);
        $query = "DELETE FROM chair_cls_dat 
                   WHERE year     = '".CTRL_YEAR."'
                     AND semester = '".CTRL_SEMESTER."'
                     AND groupcd  = '".$model->groupcd."'";
        $db->query($query); 
               
        if ($model->field["ATTENDCLASS"])
        {
            $class = explode(",",$model->field["ATTENDCLASS"]);
            foreach ($class as $val)
            {
                $cd = explode("-",$val);
                $query = "INSERT INTO chair_cls_dat 
                          VALUES ('".CTRL_YEAR."'
                                 ,'".CTRL_SEMESTER."'
                                 ,'0000000'
                                 ,'".$model->groupcd."'
                                 ,'".sprintf("%02d",$cd[0])."'
                                 ,'".sprintf("%02d",$cd[1])."'
                                 ,'".STAFFCD."'
                                 ,SYSDATE())";
                $db->query($query);    
            }
        }       

        $db->commit();        
        Query::dbCheckIn($db);
    }

    
    //削除
    function DeleteQuery($staffcd)
    {
        $db = Query::dbCheckOut();
        Query::dbCheckIn($db);
    }
}
?>
