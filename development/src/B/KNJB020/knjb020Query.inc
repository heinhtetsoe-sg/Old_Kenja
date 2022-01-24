<?php

require_once('for_php7.php');

class knjb020Query extends Query 
{
    //教科コンボ
    function GetClasscd()
    {
        return "SELECT classcd, classname FROM v_class_mst WHERE year = '".CTRL_YEAR."'";
    }
    
    //リスト情報取得
    function GetListValue($classcd)
    {
        $query = "SELECT T1.chaircd, T1.chairname
                        ,T1.subclasscd || ' ' || T2.subclassname AS subclass
                        ,T1.groupcd || ' ' || T3.groupname AS group
                        ,T1.groupseq
                    FROM chair_dat T1, subclass_mst T2, electclass_mst T3
                   WHERE T1.subclasscd = T2.subclasscd
                     AND T1.groupcd = T3.groupcd
                     AND T1.year     = '".CTRL_YEAR."'
                     AND T1.semester = '".CTRL_SEMESTER."'";
        if (strlen($classcd)) {
            $query .= " AND substr(T1.subclasscd,1,2) = '".$classcd."'";
        }             
        return $query ;
    }

    //担当職員取得
    function getStaff($chaircd)
    {
        $query  = "SELECT T1.staffcd, T1.chaircd, T2.staffname 
                     FROM chair_stf_dat T1, v_staff_mst T2
                    WHERE T1.year     = '".CTRL_YEAR."'
                      AND T1.semester = '".CTRL_SEMESTER."'
                      AND T1.chaircd  = '".$chaircd."'
                      AND T1.staffcd  = T2.staffcd
                      AND T1.year     = T2.year
                    ORDER BY T1.chargediv DESC";
        return $query;
    }

    //職員一覧
    function getStaffMst()
    {
        $query = "SELECT staffcd, staffname FROM v_staff_mst WHERE year = '".CTRL_YEAR."'";

        return $query;
    }

    //選択データ詳細
    function getChosenData($a_cd)
    {
        $query  = "SELECT DISTINCT
                    T1.CHAIRCD,   T1.CHAIRNAME,
                    T1.SUBCLASSCD
                   FROM
                      CHAIR_DAT T1
                    WHERE T1.CHAIRCD  = '".$a_cd."'
                      AND T1.YEAR     = '".CTRL_YEAR."'
                      AND T1.SEMESTER = '".CTRL_SEMESTER."' ";

        return $query;
    }

    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //元情報を削除
        $query  = " DELETE FROM chair_stf_dat
                    WHERE YEAR       = '".CTRL_YEAR."'
                    AND   SEMESTER   = '".CTRL_SEMESTER."'
                    AND   CHAIRCD    = '".$model->chaircd."'";

        $db->query($query);

        if($model->selectdata != "")
        {
            //データ更新
            $array  = explode (",", $model->selectdata);

            for ($i = 0; $i < get_count($array); $i++) 
            {
                $query  = " INSERT INTO chair_stf_dat";
                $query .= "(YEAR,SEMESTER,CHAIRCD,STAFFCD,CHARGEDIV,REGISTERCD,UPDATED) ";
                $query .= " VALUES";
                $query .= "('".CTRL_YEAR."'";               //年度
                $query .= ",'".CTRL_SEMESTER."'";           //学期
                $query .= ",'".$model->chaircd."'";         //講座コード
                $query .= ",'".$array[$i]."'";              //職員コード
                $query .= ",".(($i==0)?"1":"0");            //担当区分
                $query .= ",'".STAFFCD."'";                 //登録者コード
                $query .= ",   sysdate())";                 //更新日付
                $db->query($query);
            }

        }
        $db->commit();
        Query::dbCheckIn($db);

        return;
    }
}
?>
