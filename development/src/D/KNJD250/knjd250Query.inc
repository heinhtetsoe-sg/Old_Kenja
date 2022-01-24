<?php

require_once('for_php7.php');


class knjd250Query extends Query {

    //学年取得
    function getSelectGrade($year)
    {
        $query  = "SELECT distinct grade FROM schreg_regd_hdat ";
        $query .= "WHERE year = '" .$year ."' ";
        $query .= "order by grade";

        return $query;
    }

    //対象科目取得 2004-08-04 naka
    function getSelectClass($model)
    {
    $query = "SELECT ".
             "    T1.SUBCLASSCD AS VALUE,".
    		 "    T1.SUBCLASSCD || '　' || T1.SUBCLASSNAME AS LABEL ".
             "FROM ".
    		 "    subclass_mst T1,".
             "    (".
    		 "    SELECT ".
             "        SUBCLASSCD ".                                   //科目コード
//             "        GRADINGCLASSCD ".                                   //評価科目コード
             "    FROM ".
             "        ( ".
             "	      SELECT ".                                           //対象クラスの生徒の学籍番号取得
             "            SCHREGNO ".
             "        FROM ".
             "            SCHREG_REGD_DAT ".
             "        WHERE ".
             "            YEAR='". $model->control["年度"]. "' AND ".     //年度指定
             "            SEMESTER='". $model->control["学期"]. "' AND ". //学期指定
             "            GRADE='". $model->field["GAKUNEN"]. "' ".       //学年指定
             "        ) TT1, ".
             "        kin_record_dat TT2 ".      //成績データ
//             "        recordgrade_dat TT2 ".      //成績学年データ
             "    WHERE ".
             "        TT2.YEAR ='". $model->control["年度"]. "' AND ".
             "        TT1.SCHREGNO = TT2.SCHREGNO ".
             "    GROUP BY SUBCLASSCD ".
//             "    GROUP BY GRADINGCLASSCD ".
    		 "    ) T2 ".
             "WHERE ".
    		 "    T1.SUBCLASSCD=T2.SUBCLASSCD ";
//    		 "    T1.SUBCLASSCD=T2.GRADINGCLASSCD ";

        return $query;
    }

}
?>
