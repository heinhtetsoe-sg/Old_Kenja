<?php

require_once('for_php7.php');


class knjb140tQuery extends Query {

    //教科一覧を取得
    function getSelectTextbook($model)
    {
		$query  = "SELECT DISTINCT ";
		$query .= "    t2.textbookcd, ";
		$query .= "    t2.textbookname ";
		$query .= "FROM ";
		$query .= "    chair_textbook_dat t1, ";
		$query .= "    v_textbook_mst t2 ";
		$query .= "WHERE ";
		$query .= "    t1.year = '".$model->control["年度"]."' AND ";
		$query .= "    t1.semester = '".$model->control["学期"]."' AND ";
		$query .= "    t1.year = t2.year AND ";
		$query .= "    t1.textbookcd = t2.textbookcd ";
		$query .= "ORDER BY ";
		$query .= "    1 ";

        return $query;
    }


}
?>
