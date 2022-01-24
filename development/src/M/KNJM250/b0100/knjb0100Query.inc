<?php

require_once('for_php7.php');

class knjb0100Query extends Query {

    //名称
    function Getdata($model)
    {
        $query  = " with subcd_table (subclasscd,subclassname) as ";
        $query .= "                (SELECT ";
        $query .= "                     w1.SUBCLASSCD, ";
        $query .= "                     w1.SUBCLASSNAME ";
        $query .= "                 FROM ";
        $query .= "                     SUBCLASS_MST w1, ";
        $query .= "                     SUBCLASS_YDAT w2 ";
        $query .= "                 WHERE ";
        $query .= "                     w2.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "                     w2.SUBCLASSCD >= '900000' and ";
        $query .= "                     w1.SUBCLASSCD in (w2.SUBCLASSCD)), ";
        $query .= " hit_dat as (SELECT * ";
        $query .= "             FROM ";
        $query .= "                 SUBCLASS_HINT_DAT ";
        $query .= "             WHERE ";
        $query .= "                 YEAR = '".CTRL_YEAR."') ";
        $query .= " SELECT ";
        $query .= "     s1.SUBCLASSCD, ";
        $query .= "     s1.SUBCLASSNAME, ";
        $query .= "     s2.HINTDIV, ";
        $query .= "     s2.PERIODCD ";
        $query .= " FROM ";
        $query .= "     subcd_table s1 LEFT JOIN hit_dat s2 on s2.SUBCLASSCD = s1.subclasscd ";
        $query .= " ORDER BY ";
        $query .= "     s1.SUBCLASSCD ";

        return $query;
    }

    //名称
    function Getperiod($namecd1,$namecd2,$allflg)
    {
        $query  = " SELECT name1,NAMECD2 FROM v_name_mst ";
        $query .= "  WHERE year    = '".CTRL_YEAR."' ";
        $query .= " 	AND namecd1 = '".$namecd1."' ";
		if ($allflg == "false"){
	        $query .= "     AND namecd2 = '".$namecd2."' ";
		}
        return $query;
    }

    //DELETE(更新前処理)
    function getDeleteQuery(&$model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM SUBCLASS_HINT_DAT ";
        $query .= " WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= " AND SUBCLASSCD >= '900000' ";

		$db->query($query);
        Query::dbCheckIn($db);
    }

    //UPDATE(更新処理)
    function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $setdata = array();
        for ($i = 0; $i < get_count($model->field["STATE"]); $i++) {
			if ($model->field["STATE"][$i] == "0"){
				$data = array();
				$data["YEAR"][TEXT]			= $model->ObjYear;
				$data["SUBCLASSCD"][TEXT]	= $model->field2["SUBCD"][$i];	//読込み時のCDを設定する。
				$data["HINTDIV"][NUMBER]	= $model->field["STATE"][$i];
				$data["PERIODCD"][TEXT]		= "*";
				$data["REGISTERCD"][TEXT]	= STAFFCD;
				$data["UPDATED"][FUNC]		= "sysdate()";
            	$query = Query::insertSQL($data, "SUBCLASS_HINT_DAT");
				$db->query($query);
			}else if($model->field["STATE"][$i] != "" && $model->field["PERIOD_CD"][$i] != ""){
				$data = array();
				$data["YEAR"][TEXT]			= $model->ObjYear;
				$data["SUBCLASSCD"][TEXT]	= $model->field2["SUBCD"][$i];	//読込み時のCDを設定する。
				$data["HINTDIV"][NUMBER]	= $model->field["STATE"][$i];
				$data["PERIODCD"][TEXT]		= $model->field["PERIOD_CD"][$i];
				$data["REGISTERCD"][TEXT]	= STAFFCD;
				$data["UPDATED"][FUNC]		= "sysdate()";
            	$query = Query::insertSQL($data, "SUBCLASS_HINT_DAT");
				$db->query($query);
			}
		}

        $db->commit();
        Query::dbCheckIn($db);
    }

}
?>
