<?php

require_once('for_php7.php');

class knjh140Query extends Query {

    //年度一覧
	function getSelectFieldSQL()
	{
		$query  = "select distinct t1.year,t1.semester,t2.semestername ";
		$query .= "from schreg_regd_hdat t1 ";
		$query .= "		inner join semester_mst t2 on t1.year=t2.year and t1.semester=t2.semester ";
		$query .= "order by t1.year,t1.semester";

		return $query;	
	}

    //年組一覧
	function getSelectFieldSQL2($model)
	{
		$query  = "select distinct grade,hr_class,hr_name ";
		$query .= "from schreg_regd_hdat ";
		$query .= "where YEAR || SEMESTER = '".$model->field["YEAR"]."' ";
		$query .= "order by grade,hr_class";

		return $query;	
	}

    //データ出力
	function getselectDataSQL($model)
	{
		$query  = " WITH REGD_DAT AS ( ";
		$query .= "     SELECT YEAR AS REGYEAR,SCHREGNO AS REGSCH,GRADE,HR_CLASS,ATTENDNO ";
		$query .= "     FROM SCHREG_REGD_DAT ";
		$query .= "     WHERE YEAR || SEMESTER = '".$model->field["YEAR"]."' ";
		if ($model->field["GRADE_HR_CLASS"])
			$query .= "     AND GRADE || HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
		$query .= " ) ";
		if ($model->target == 1){
			$query .= " SELECT ";
			$query .= "     '1',REGYEAR,GRADE,HR_CLASS,ATTENDNO,REGSCH,T2.NAME, ";
			$query .= "     T1.RELANO,T1.RELANAME,T1.RELAKANA,T1.RELASEX,T1.RELABIRTHDAY, ";
			$query .= "     T1.OCCUPATION,T1.REGIDENTIALCD,T1.RELATIONSHIP,T1.RELA_SCHREGNO,T1.REMARK,'DUMMY' AS DUMMY ";
			$query .= " FROM ";
			$query .= "     REGD_DAT LEFT JOIN SCHREG_RELA_DAT T1 ON REGSCH = T1.SCHREGNO ";
			$query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON REGSCH = T2.SCHREGNO ";
		}else if ($model->target == 2){
			$query .= " SELECT ";
			$query .= "     '2',REGYEAR,GRADE,HR_CLASS,ATTENDNO,REGSCH,T1.NAME, ";
			$query .= "     T1.EMERGENCYCALL,T1.EMERGENCYNAME,T1.EMERGENCYRELA_NAME,T1.EMERGENCYTELNO, ";
			$query .= "     T1.EMERGENCYCALL2,T1.EMERGENCYNAME2,T1.EMERGENCYRELA_NAME2,T1.EMERGENCYTELNO2,'DUMMY' AS DUMMY ";
			$query .= " FROM ";
			$query .= "     REGD_DAT LEFT JOIN SCHREG_BASE_MST T1 ON REGSCH = T1.SCHREGNO ";
		}else if ($model->target == 3){
			$query .= " SELECT ";
			$query .= "     '3',REGYEAR,GRADE,HR_CLASS,ATTENDNO,REGSCH,T2.NAME, ";
			$query .= "     T1.COMMUTE_HOURS,T1.COMMUTE_MINUTES,T1.STATIONNAME,T1.OTHERHOWTOCOMMUTE,T1.HOWTOCOMMUTECD, ";
			$query .= "     T1.CHANGETRAIN1,T1.CHANGETRAIN2,T1.CHANGETRAIN3,T1.CHANGETRAIN4, ";
			$query .= "     T1.CHANGETRAIN5,T1.CHANGETRAIN6,T1.CHANGETRAIN7,T1.CHANGETRAIN8,'DUMMY' AS DUMMY ";
			$query .= " FROM ";
//NO003
//			$query .= "     REGD_DAT LEFT JOIN SCHREG_ENVIR_DAT T1 ON REGYEAR = T1.YEAR AND REGSCH = T1.SCHREGNO ";
			$query .= "     REGD_DAT LEFT JOIN SCHREG_ENVIR_DAT T1 ON REGSCH = T1.SCHREGNO ";
			$query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON REGSCH = T2.SCHREGNO ";
//NO001↓
		}else if ($model->target == 4){
			$query .= " SELECT ";
			$query .= "     '4',REGYEAR,REGSCH,GRADE,HR_CLASS,ATTENDNO,T2.NAME,T1.DETAIL_SDATE,T1.DETAIL_EDATE,T1.BICYCLE_CD,T1.BICYCLE_NO,T1.CONTENT,T1.REMARK,'DUMMY' AS DUMMY ";
			$query .= " FROM ";
			$query .= "     REGD_DAT LEFT JOIN SCHREG_DETAILHIST_DAT T1 ON REGYEAR = T1.YEAR AND REGSCH = T1.SCHREGNO ";
			$query .= "     AND T1.YEAR = '".substr($model->field["YEAR"],0,4)."' ";
			$query .= "     AND T1.DETAIL_DIV = '3' ";
			$query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON REGSCH = T2.SCHREGNO ";
		}
//NO001↑
		$query .= " ORDER BY ";
		$query .= "     REGYEAR,GRADE,HR_CLASS,ATTENDNO ";

		return $query;	
	}

    //実行
    function InsertQuery($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;    //処理件数
        $count = get_count($data_arr);
        for ($i=0; $i<$count; $i++) {
            switch ($model->target) {
                case 1:
                    $table = "SCHREG_RELA_DAT";
                    $query = " SELECT COUNT(*) FROM ".$table ;
                    $query .= "    WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
                    $query .= "    AND RELANO = '".$data_arr[$i]["RELANO"]."' ";
                    break;
                case 2:
                    $table = "SCHREG_BASE_MST";
                    $query = " SELECT COUNT(*) FROM ".$table ;
                    $query .= "    WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
                    break;
                case 3:
                    $table = "SCHREG_ENVIR_DAT";
                    $query = " SELECT COUNT(*) FROM ".$table ;
//NO003
                    $query .= "    WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
                    break;
                case 4:
                    $table = "SCHREG_DETAILHIST_DAT";
                    $query = " SELECT COUNT(*) FROM ".$table ;
                    $query .= "     WHERE YEAR = '".$data_arr[$i]["YEAR"]."' ";
                    $query .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
                    $query .= "     AND DETAIL_DIV = '3' ";
                    $query .= "     AND DETAIL_SDATE = '".$data_arr[$i]["DETAIL_SDATE"]."' ";
                    break;
            }
            $tblcnt = $db->getOne($query);
            $where = "";
            $data = array();
            switch ($model->target) {
                case 1:
                    if ($tblcnt > 0) {
                        $data["RELANAME"][TEXT] = $data_arr[$i]["RELANAME"];
                        $data["RELAKANA"][TEXT] = $data_arr[$i]["RELAKANA"];
                        $data["RELASEX"][TEXT]  = $data_arr[$i]["RELASEX"];
                        //NO002
                        if (!$data_arr[$i]["RELABIRTHDAY"]) {
                            $data["RELABIRTHDAY"][FUNC] = null;
                        } else {
                            $data["RELABIRTHDAY"][FUNC] = "date('".str_replace("/", "-", $data_arr[$i]["RELABIRTHDAY"])."')";
                        }
                        $data["OCCUPATION"][TEXT]    = $data_arr[$i]["OCCUPATION"];
                        $data["REGIDENTIALCD"][TEXT] = $data_arr[$i]["REGIDENTIALCD"];
                        $data["RELATIONSHIP"][TEXT]  = $data_arr[$i]["RELATIONSHIP"];
                        $data["RELA_SCHREGNO"][TEXT] = $data_arr[$i]["RELA_SCHREGNO"];
                        $data["REMARK"][TEXT]        = $data_arr[$i]["REMARK"];
                        $where = "    WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' AND RELANO = '".$data_arr[$i]["RELANO"]."' ";
                        $insup = "up";
                    } else {
                        $data["SCHREGNO"][TEXT] = $data_arr[$i]["SCHREGNO"];
                        $data["RELANO"][TEXT]   = $data_arr[$i]["RELANO"];
                        $data["RELANAME"][TEXT] = $data_arr[$i]["RELANAME"];
                        $data["RELAKANA"][TEXT] = $data_arr[$i]["RELAKANA"];
                        $data["RELASEX"][TEXT]  = $data_arr[$i]["RELASEX"];
                        //NO002
                        if (!$data_arr[$i]["RELABIRTHDAY"]) {
                            $data["RELABIRTHDAY"][FUNC] = null;
                        } else {
                            $data["RELABIRTHDAY"][FUNC] = "date('".str_replace("/", "-", $data_arr[$i]["RELABIRTHDAY"])."')";
                        }
                        $data["OCCUPATION"][TEXT]    = $data_arr[$i]["OCCUPATION"];
                        $data["REGIDENTIALCD"][TEXT] = $data_arr[$i]["REGIDENTIALCD"];
                        $data["RELATIONSHIP"][TEXT]  = $data_arr[$i]["RELATIONSHIP"];
                        $data["RELA_SCHREGNO"][TEXT] = $data_arr[$i]["RELA_SCHREGNO"];
                        $data["REMARK"][TEXT]        = $data_arr[$i]["REMARK"];
                        $insup = "ins";
                    }

                    break;
                case 2:
                    if ($tblcnt > 0) {
                        $data["EMERGENCYCALL"][TEXT]       = $data_arr[$i]["EMERGENCYCALL"];
                        $data["EMERGENCYNAME"][TEXT]       = $data_arr[$i]["EMERGENCYNAME"];
                        $data["EMERGENCYRELA_NAME"][TEXT]  = $data_arr[$i]["EMERGENCYRELA_NAME"];
                        $data["EMERGENCYTELNO"][TEXT]      = $data_arr[$i]["EMERGENCYTELNO"];
                        $data["EMERGENCYCALL2"][TEXT]      = $data_arr[$i]["EMERGENCYCALL2"];
                        $data["EMERGENCYNAME2"][TEXT]      = $data_arr[$i]["EMERGENCYNAME2"];
                        $data["EMERGENCYRELA_NAME2"][TEXT] = $data_arr[$i]["EMERGENCYRELA_NAME2"];
                        $data["EMERGENCYTELNO2"][TEXT]     = $data_arr[$i]["EMERGENCYTELNO2"];
                        $where = "    WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
                        $insup = "up";
                    } else {
                        $insup = "exit";
                    }

                    break;
                case 3:
                    if ($tblcnt > 0) {
                        $data["COMMUTE_HOURS"][TEXT]     = $data_arr[$i]["COMMUTE_HOURS"];
                        $data["COMMUTE_MINUTES"][TEXT]   = $data_arr[$i]["COMMUTE_MINUTES"];
                        $data["STATIONNAME"][TEXT]       = $data_arr[$i]["STATIONNAME"];
                        $data["OTHERHOWTOCOMMUTE"][TEXT] = $data_arr[$i]["OTHERHOWTOCOMMUTE"];
                        $data["HOWTOCOMMUTECD"][TEXT]    = $data_arr[$i]["HOWTOCOMMUTECD"];
                        $data["CHANGETRAIN1"][TEXT]      = $data_arr[$i]["CHANGETRAIN1"];
                        $data["CHANGETRAIN2"][TEXT]      = $data_arr[$i]["CHANGETRAIN2"];
                        $data["CHANGETRAIN3"][TEXT]      = $data_arr[$i]["CHANGETRAIN3"];
                        $data["CHANGETRAIN4"][TEXT]      = $data_arr[$i]["CHANGETRAIN4"];
                        $data["CHANGETRAIN5"][TEXT]      = $data_arr[$i]["CHANGETRAIN5"];
                        $data["CHANGETRAIN6"][TEXT]      = $data_arr[$i]["CHANGETRAIN6"];
                        $data["CHANGETRAIN7"][TEXT]      = $data_arr[$i]["CHANGETRAIN7"];
                        $data["CHANGETRAIN8"][TEXT]      = $data_arr[$i]["CHANGETRAIN8"];
//NO003
                        $where = "     WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
                        $insup = "up";
                    } else {
//NO003
                        $data["SCHREGNO"][TEXT]          = $data_arr[$i]["SCHREGNO"];
                        $data["COMMUTE_HOURS"][TEXT]     = $data_arr[$i]["COMMUTE_HOURS"];
                        $data["COMMUTE_HOURS"][TEXT]     = $data_arr[$i]["COMMUTE_HOURS"];
                        $data["COMMUTE_MINUTES"][TEXT]   = $data_arr[$i]["COMMUTE_MINUTES"];
                        $data["STATIONNAME"][TEXT]       = $data_arr[$i]["STATIONNAME"];
                        $data["OTHERHOWTOCOMMUTE"][TEXT] = $data_arr[$i]["OTHERHOWTOCOMMUTE"];
                        $data["HOWTOCOMMUTECD"][TEXT]    = $data_arr[$i]["HOWTOCOMMUTECD"];
                        $data["CHANGETRAIN1"][TEXT]      = $data_arr[$i]["CHANGETRAIN1"];
                        $data["CHANGETRAIN2"][TEXT]      = $data_arr[$i]["CHANGETRAIN2"];
                        $data["CHANGETRAIN3"][TEXT]      = $data_arr[$i]["CHANGETRAIN3"];
                        $data["CHANGETRAIN4"][TEXT]      = $data_arr[$i]["CHANGETRAIN4"];
                        $data["CHANGETRAIN5"][TEXT]      = $data_arr[$i]["CHANGETRAIN5"];
                        $data["CHANGETRAIN6"][TEXT]      = $data_arr[$i]["CHANGETRAIN6"];
                        $data["CHANGETRAIN7"][TEXT]      = $data_arr[$i]["CHANGETRAIN7"];
                        $data["CHANGETRAIN8"][TEXT]      = $data_arr[$i]["CHANGETRAIN8"];
                        $insup = "ins";
                    }

                    break;
//NO001↓
                case 4:
                    if ($tblcnt > 0) {
                        $data["DETAIL_EDATE"][TEXT] = $data_arr[$i]["DETAIL_EDATE"];
                        $data["BICYCLE_NO"][TEXT]   = $data_arr[$i]["BICYCLE_NO"];
                        $data["CONTENT"][TEXT]      = $data_arr[$i]["CONTENT"];
                        $data["REMARK"][TEXT]       = $data_arr[$i]["REMARK"];
                        $where  = "    WHERE YEAR = '".$data_arr[$i]["YEAR"]."' AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
                        $where .= "    AND DETAIL_DIV = '3' AND DETAIL_SDATE = '".$data_arr[$i]["DETAIL_SDATE"]."' ";
                        $insup = "up";
                    } else {
                        $data["YEAR"][TEXT]         = $data_arr[$i]["YEAR"];
                        $data["SCHREGNO"][TEXT]     = $data_arr[$i]["SCHREGNO"];
                        $data["DETAIL_DIV"][TEXT]   = "3";
                        $data["DETAIL_SDATE"][TEXT] = $data_arr[$i]["DETAIL_SDATE"];
                        $data["DETAIL_EDATE"][TEXT] = $data_arr[$i]["DETAIL_EDATE"];
                        $data["BICYCLE_CD"][TEXT]   = $data_arr[$i]["BICYCLE_CD"];
                        $data["BICYCLE_NO"][TEXT]   = $data_arr[$i]["BICYCLE_NO"];
                        $data["CONTENT"][TEXT]      = $data_arr[$i]["CONTENT"];
                        $data["REMARK"][TEXT]       = $data_arr[$i]["REMARK"];
                        $insup = "ins";
                    }

                    break;
//NO001↑
            }
            if ($insup == "ins") {
                $query = Query::insertSQL($data, $table);
                $db->query($query);
            } elseif ($insup == "up") {
                $query = Query::updateSQL($data, $table, $where);
                $db->query($query);
            }

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }
}
?>
