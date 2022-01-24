<?php

require_once('for_php7.php');

class knjz230Query extends Query 
{
    //教科コンボボックスの値を取得
    function GetClassData($year)
    {
        $query  = " SELECT
                      CLASSCD,CLASSNAME,CLASSABBV
                    FROM
                      V_CLASS_MST
                    WHERE
                      YEAR = '".$year."'
                    ORDER BY CLASSCD   ";

       return $query;
    }

    //照合用講座置換えデータ取得
    function getReplaced_val($a_cd)
    {
        $query = "  SELECT
                        T1.GRADING_CHAIRCD, T1.REPLACECD, COUNT(*) AS COUNT
                    FROM
                        CHAIR_REPLACE_DAT T1 LEFT JOIN CHAIR_DAT T2
                            ON  T1.YEAR = T2.YEAR
                            AND T1.SEMESTER = T2.SEMESTER
                            AND T1.GRADING_CHAIRCD = T2.CHAIRCD
                    WHERE
                        T1.YEAR = '".CTRL_YEAR."' AND
                        T1.SEMESTER = '".CTRL_SEMESTER."' AND 
                        SUBSTR(T2.SUBCLASSCD,1,2) = '".$a_cd."'
                    GROUP BY T1.GRADING_CHAIRCD, T1.REPLACECD ";

        return  $query;
    }

    //リスト情報取得
    function GetListValue($a_cd)
    {
        $query = " SELECT DISTINCT
                         T1.CHAIRCD,  T1.CHAIRNAME, T1.SUBCLASSCD, T2.SUBCLASSNAME, T4.STAFFCD,
                         T5.STAFFNAME_SHOW, T6.HR_NAME AS GRCL
                   FROM
                         CHAIR_DAT T1 LEFT JOIN SUBCLASS_MST T2  ON  T1.SUBCLASSCD = T2.SUBCLASSCD 
                                      LEFT JOIN CHAIR_CLS_DAT T3 ON  T1.YEAR     = T3.YEAR 
                                                                 AND T1.CHAIRCD  = T3.CHAIRCD 
                                                                 AND T1.SEMESTER = T3.SEMESTER 
                                      LEFT JOIN CHAIR_STF_DAT T4 ON  T1.SEMESTER = T4.SEMESTER 
                                                                 AND T1.CHAIRCD  = T4.CHAIRCD
                                      LEFT JOIN STAFF_MST T5     ON  T4.STAFFCD  = T5.STAFFCD 
                                      LEFT JOIN SCHREG_REGD_HDAT T6  ON  T3.YEAR = T6.YEAR
                                                                     AND T3.SEMESTER  = T6.SEMESTER
                                                                     AND T3.TRGTGRADE = T6.GRADE
                                                                     AND T3.TRGTCLASS = T6.HR_CLASS
                    WHERE T1.YEAR = '".CTRL_YEAR."'
                    AND T1.SEMESTER = '".CTRL_SEMESTER."'
                    AND SUBSTR(T1.SUBCLASSCD,1,2) = '".$a_cd."' 
                    AND T1.CHAIRCD NOT IN (SELECT
                                                ATTEND_CHAIRCD
                                            FROM
                                                CHAIR_REPLACE_DAT
                                            WHERE REPLACECD = '1' 
                                            AND  YEAR = '".CTRL_YEAR."'
                                            AND  SEMESTER = '".CTRL_SEMESTER."'
                                            AND  ATTEND_CHAIRCD <> GRADING_CHAIRCD )
                    ORDER BY T1.SUBCLASSCD,T1.CHAIRCD ";
        return $query ;
    }

    //合併後のクラスコンボボックスの値取得
    function getSub_Cls_Mst($a_cd)
    {
        $query  = " SELECT 
                      SUBCLASSCD,SUBCLASSNAME
                    FROM
                      V_SUBCLASS_MST
                    WHERE
                      YEAR = '".CTRL_YEAR."'
                      AND SUBSTR(SUBCLASSCD,1,2) = '".$a_cd."'
                    ORDER BY SUBCLASSCD ";

        return $query;
    }

    //講座一覧の表示
    function getChair_dat($a_cd,$grade,$class)
    {
        $query  = " SELECT
                     CHAIRCD, CHAIRNAME
                    FROM
                     CHAIR_DAT
                    WHERE YEAR = '".CTRL_YEAR."'
                    AND SEMESTER = '".CTRL_SEMESTER."'
                    AND SUBSTR(SUBCLASSCD,1,2) = '".$a_cd."'
                    AND CHAIRCD NOT IN ( 
                                         SELECT ATTEND_CHAIRCD
                                         FROM
                                               CHAIR_REPLACE_DAT
                                         WHERE SEMESTER = '".CTRL_SEMESTER."'
                                         AND  (REPLACECD = '1' OR  REPLACECD = '2')
                                         AND   YEAR = '".CTRL_YEAR."'
                                        )
                    AND CHAIRCD IN ( 
                                    SELECT CHAIRCD
                                    FROM 
                                        CHAIR_CLS_DAT
                                    WHERE YEAR = '".CTRL_YEAR."' AND
                                          SEMESTER = '".CTRL_SEMESTER."' AND 
                                          TRGTGRADE = '".$grade."' AND
                                          TRGTCLASS = '".$class."'
                                   )
                    ORDER BY SUBCLASSCD,CHAIRCD ";
        return $query;
    }

    //講座一覧の表示
    function getMergeChair_dat($a_cd)
    {
        $query = "SELECT
                        T2.CHAIRCD,T2.CHAIRNAME
                  FROM
                        CHAIR_REPLACE_DAT T1 LEFT JOIN CHAIR_DAT T2 ON T1.YEAR            = T2.YEAR
                                                                    AND T1.SEMESTER       = T2.SEMESTER
                                                                    AND T1.ATTEND_CHAIRCD = T2.CHAIRCD
                  WHERE 
                     T1.YEAR = '".CTRL_YEAR."'
                     AND T1.SEMESTER = '".CTRL_SEMESTER."'
                     AND T1.GRADING_CHAIRCD ='".$a_cd."' 
                  ORDER BY CHAIRCD";

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
        $query  = "SELECT DISTINCT
                    T1.CHAIRCD,   T1.CHAIRNAME,
                    T1.SUBCLASSCD,T2.GRADINGCLASSCD
                   FROM
                      CHAIR_DAT T1
                      LEFT JOIN CHAIR_REPLACE_DAT T2 ON  T1.YEAR     = T2.YEAR 
                                                     AND T1.CHAIRCD  = T2.GRADING_CHAIRCD
                                                     AND T1.SEMESTER = T2.SEMESTER 
                    WHERE T1.CHAIRCD  = '".$a_cd."'
                      AND T1.YEAR     = '".CTRL_YEAR."'
                      AND T1.SEMESTER = '".CTRL_SEMESTER."' ";

        return $query;
    }

    function &getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //元情報を削除
        $query  = " DELETE FROM CHAIR_REPLACE_DAT
                    WHERE YEAR       = '".CTRL_YEAR."'
                    AND   SEMESTER   = '".CTRL_SEMESTER."'
                    AND   GRADING_CHAIRCD = '".$model->chaircd."'
                    AND   REPLACECD  = '1'
                    AND   SUBCLASSES =  1
                    AND   TRUNCATECD = '1' ";

        $db->query($query);

        if($model->selectdata != "")
        {
            //データ更新
            $array  = explode (",", $model->selectdata);
            $query  = " INSERT INTO CHAIR_REPLACE_DAT ";
            $query .= "(REPLACECD,YEAR,SEMESTER,GRADING_CHAIRCD,ATTEND_CHAIRCD,GRADINGCLASSCD,SUBCLASSES,TRUNCATECD,REGISTERCD,UPDATED) ";
            $query .= " VALUES";

            for ($i = 0; $i < get_count($array); $i++) 
            {
                if($model->chaircd == $array[$i] || $array[$i] == "") continue;

                $query .= "('1'";                           //replacecd
                $query .= ",'".CTRL_YEAR."'";               //年度
                $query .= ",'".CTRL_SEMESTER."'";           //学期
                $query .= ",'".$model->chaircd."'";         //(合併先)講座コード
                $query .= ",'".$array[$i]."'";              //対象講座コード
                $query .= ",'".$model->cmb_sub_cls."'";     //合併科目コード
                $query .= ",1";                             //subclasses
                $query .= ",'1'";                           //truncatecd
                $query .= ",'".STAFFCD."'";                 //登録者コード
                $query .= ",   sysdate()) ," ;              //更新日付
            }

            //自分自身の追加
            $query .= " ('1'";                           //replacecd
            $query .= ",'".CTRL_YEAR."'";                //年度
            $query .= ",'".CTRL_SEMESTER."'";            //学期
            $query .= ",'".$model->chaircd."'";          //(合併先)講座コード
            $query .= ",'".$model->chaircd."'";          //対象講座コード
            $query .= ",'".$model->cmb_sub_cls."'";      //合併科目コード
            $query .= ",1";                              //subclasses
            $query .= ",'1'";                            //truncatecd
            $query .= ",'".STAFFCD."'";                  //登録者コード
            $query .= ",   sysdate()) " ;                //更新日付

            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);

        return;
    }
}
?>
