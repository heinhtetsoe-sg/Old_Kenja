<?php

require_once('for_php7.php');

class knjz231Query extends Query {

    //教科コンボボックスの値を取得
    function GetClassData($year)
    {
        $query  = " SELECT
                        CLASSCD,CLASSNAME,CLASSABBV
                    FROM
                        V_CLASS_MST
                    WHERE
                        YEAR = '".$year."'
                    ORDER BY CLASSCD ";

       return $query;
    }

    //照合用講座置換えデータ取得
    function getReplaced_val($a_cd)
    {

    $query = "  SELECT
                    T1.ATTEND_CHAIRCD, T1.REPLACECD
                FROM
                    CHAIR_REPLACE_DAT T1 LEFT JOIN CHAIR_DAT T2
                        ON  T1.YEAR = T2.YEAR
                        AND T1.SEMESTER = T2.SEMESTER
                        AND T1.GRADING_CHAIRCD = T2.CHAIRCD
                WHERE
                    T1.YEAR = '".CTRL_YEAR."' AND
                    T1.SEMESTER = '".CTRL_SEMESTER."' AND 
                    SUBSTR(T2.SUBCLASSCD,1,2) = '".$a_cd."' ";

        return  $query;
    }

    //リスト情報取得
    function getListValue($a_cd)
    {
        $query  = " SELECT DISTINCT
                        T1.CHAIRCD,T1.CHAIRNAME,T1.SUBCLASSCD,T2.SUBCLASSNAME,T4.STAFFCD,
                        T5.STAFFNAME_SHOW,T6.HR_NAME AS GRCL
                    FROM  CHAIR_DAT T1 
                            LEFT JOIN SUBCLASS_MST     T2  ON  T1.SUBCLASSCD = T2.SUBCLASSCD 
                            LEFT JOIN CHAIR_CLS_DAT    T3  ON  T1.YEAR     = T3.YEAR 
                                                           AND T1.CHAIRCD  = T3.CHAIRCD 
                                                           AND T1.SEMESTER = T3.SEMESTER 
                            LEFT JOIN CHAIR_STF_DAT    T4  ON  T1.SEMESTER = T4.SEMESTER 
                                                           AND T1.CHAIRCD  = T4.CHAIRCD 
                            LEFT JOIN STAFF_MST        T5  ON  T4.STAFFCD  = T5.STAFFCD 
                            LEFT JOIN SCHREG_REGD_HDAT T6  ON  T3.YEAR = T6.YEAR
                                                           AND T3.SEMESTER = T6.SEMESTER
                                                           AND T3.TRGTGRADE = T6.GRADE
                                                           AND T3.TRGTCLASS = T6.HR_CLASS

                    WHERE
                            T1.YEAR = '".CTRL_YEAR."' 
                            AND T1.SEMESTER = '".CTRL_SEMESTER."' 
                            AND SUBSTR(T1.SUBCLASSCD,1,2) = '".$a_cd."' 
                    ORDER BY T1.SUBCLASSCD, T1.CHAIRCD ";

        return $query ;
    }

    //科目一覧の表示
    function getChair_dat($a_cd)
    {
        $query = " SELECT DISTINCT 
                       SUBCLASSCD,SUBCLASSNAME 
                   FROM 
                       V_SUBCLASS_MST 
                   WHERE
                       SUBCLASSCD NOT IN (  SELECT GRADINGCLASSCD 
                                            FROM CHAIR_REPLACE_DAT 
                                            WHERE GRADING_CHAIRCD = '".$a_cd."' ) 
                   ORDER BY SUBCLASSCD";

        return $query;
    }

    //読替/分割科目一覧の表示
    function getChairReplace_dat($a_cd)
    {
        $query = " SELECT DISTINCT
                       T1.TRUNCATECD,T2.SUBCLASSCD,T2.SUBCLASSNAME
                    FROM
                       CHAIR_REPLACE_DAT T1
                       LEFT JOIN SUBCLASS_MST T2 ON T1.GRADINGCLASSCD = T2.SUBCLASSCD
                    WHERE
                        YEAR= '".CTRL_YEAR."'  
                    AND SEMESTER = '".CTRL_SEMESTER."'
                    AND GRADING_CHAIRCD = '".$a_cd."'
                    AND REPLACECD = '2' 
                    ORDER BY T1.TRUNCATECD DESC,T2.SUBCLASSCD ASC";

        return $query;
    }

    //学年コンボ
    function GetGrade($year)
    {
        return " SELECT DISTINCT GRADE FROM SCHREG_REGD_HDAT WHERE YEAR = '" .$year ."'";
    }

    //選択データ詳細
    function getChosenData($a_cd)
    {
        $query  = " SELECT DISTINCT
                      T1.CHAIRCD,
                      T1.CHAIRNAME,
                      T1.SUBCLASSCD,
                      T2.GRADINGCLASSCD
                    FROM
                        CHAIR_DAT T1
                        LEFT JOIN CHAIR_REPLACE_DAT T2 ON  T1.YEAR     = T2.YEAR
                                                       AND T1.CHAIRCD  = T2.GRADING_CHAIRCD
                                                       AND T1.SEMESTER = T2.SEMESTER
                    WHERE
                            T1.CHAIRCD  = '".$a_cd."'
                        AND T1.YEAR     = '".CTRL_YEAR."'
                        AND T1.SEMESTER = '".CTRL_SEMESTER."' ";

        return $query;
    }

    function &getUpdateQuery($model,$chaircd)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //元情報を削除
        $query  = " DELETE FROM CHAIR_REPLACE_DAT
                    WHERE YEAR       = '".CTRL_YEAR."'
                    AND   SEMESTER   = '".CTRL_SEMESTER."'
                    AND   GRADING_CHAIRCD = '".$chaircd."'
                    AND   REPLACECD  = '2' ";

        $db->query($query);

        if($model->selectdata != "")
        {
            //データ更新
            $array  = explode (",", $model->selectdata);
            $query  = " INSERT INTO CHAIR_REPLACE_DAT ";
            $query .= "(REPLACECD,YEAR,SEMESTER,GRADING_CHAIRCD,
                        ATTEND_CHAIRCD,GRADINGCLASSCD,SUBCLASSES,TRUNCATECD,REGISTERCD,UPDATED) ";
            $query .= " VALUES";

            for ($i = 0; $i < get_count($array); $i++) 
            {
                $dot = (($i+1) == get_count($array))? "" : "," ;

                $query .= "('2'";                           //replacecd
                $query .= ",'".CTRL_YEAR."'";               //年度
                $query .= ",'".CTRL_SEMESTER."'";           //学期
                $query .= ",'".$chaircd."'";                //(合併先)講座コード
                $query .= ",'".$chaircd."'";                //対象講座コード
                $query .= ",'".$array[$i]."'";              //合併科目コード
                $query .= "," .get_count($array);               //subclasses
                $query .= ",'".(($i == 0)? 1 : 0 )."'";     //切上区分
                $query .= ",'".STAFFCD."'";                 //登録者コード
                $query .= ",   sysdate())" ;                //更新日付
                $query .= $dot;
            }

            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);

        return;
    }
}
?>
