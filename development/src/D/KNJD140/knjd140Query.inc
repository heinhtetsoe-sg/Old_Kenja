<?php

require_once('for_php7.php');


class knjd140Query extends Query {

    //学期を取得
    function getSelectSemester($model)
    {
        $query  = "select semester,semestername from semester_mst ";
        $query .= "where year='".$model->control["年度"]."' and not semester='9'";

        return $query;
    }


    //教科一覧を取得
    function getSelectClass($model)
    {
		$query = "SELECT DISTINCT substr(T1.SUBCLASSCD,1,2) AS VALUE,T2.CLASSNAME AS LABEL ".
				"FROM chair_dat T1 INNER JOIN class_mst T2 ".
				"ON substr(T1.SUBCLASSCD,1,2)=T2.CLASSCD ".
				"WHERE ((T1.YEAR='".$model->control["年度"]."') ".
				"AND (T1.SEMESTER='".$model->field["GAKKI"]."') ".
//2004/04/01 nakamoto 教科コード仕様の変更に伴う修正
				"AND (substr(T1.SUBCLASSCD,1,2)<'"."90"."'))".
//				"AND (substr(T1.SUBCLASSCD,1,2)>='"."01"."') ".
//				"AND (substr(T1.SUBCLASSCD,1,2)<='"."49"."'))".
				"order by substr(T1.SUBCLASSCD,1,2)";

        return $query;
    }


}
?>
