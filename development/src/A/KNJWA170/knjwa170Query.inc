<?php

require_once('for_php7.php');

class knjwa170Query extends Query {

    //年組取得（権限チェック）
    function getAuth()
    {
		//参照・更新可
		if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
			$query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
			$query .= "FROM SCHREG_REGD_HDAT ";
			$query .= "WHERE YEAR='" .CTRL_YEAR ."'";
			$query .= "AND SEMESTER='".CTRL_SEMESTER ."'";
		}
		//参照・更新可（制限付き）
		if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
			$query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
			$query .= "FROM SCHREG_REGD_HDAT ";
			$query .= "WHERE YEAR='" .CTRL_YEAR ."' ";
			$query .= "AND SEMESTER='".CTRL_SEMESTER ."' ";
	    	$query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
	    	$query .= "	OR TR_CD2 = '" .STAFFCD ."' ";
	    	$query .= "	OR TR_CD3 = '" .STAFFCD ."') ";
		}

        return $query;
    }


    //担当所属とクラス
    function getStaff()
    {
        $query  =" SELECT ";
        $query .="     BELONGING_DIV ";
        $query .=" FROM ";
        $query .="     STAFF_MST ";
        $query .=" WHERE ";
        $query .="     STAFFCD = '".STAFFCD."' ";

        return $query;
    }

    //担当クラス
    function getHrclass($model)
    {
        $query  =" SELECT ";
        $query .="      GRADE || '-' || HR_CLASS AS VALUE ";
        $query .=" FROM ";
        $query .="     SCHREG_REGD_HDAT ";
        $query .=" WHERE ";
        $query .="         YEAR     = '" .CTRL_YEAR ."' ";
        $query .="    AND (TR_CD1   = '".STAFFCD."' ";
        $query .="    OR  TR_CD2    = '".STAFFCD."' ";
        $query .="    OR  TR_CD3    = '".STAFFCD."' ";
        $query .="    OR  SUBTR_CD1 = '".STAFFCD."' ";
        $query .="    OR  SUBTR_CD2 = '".STAFFCD."' ";
        $query .="    OR  SUBTR_CD3 = '".STAFFCD."') ";

        return $query;
    }

    //名称マスタ
    function getNameMst($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || '：' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .CTRL_YEAR ."' ";
        $query .= "     AND NAMECD1 = 'Z005' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
//--------------------------------------------------------------------------------------------------
    //生徒情報取得(NOT USE)
    function getStudentInfoData($model)
    {
        list($and, $where) = knjwa170Query::getStudentSearch($model);

        $query  = " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        //$query .= "     T1.APPLICANTNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.GRADE, ";
        //$query .= "     T2.COURSE_DIV, ";
        //$query .= "     T2.STUDENT_DIV, ";
        $query .= "     T1.ENT_DATE, ";
        //$query .= "     T1.GRD_SCHEDULE_DATE, ";
        $query .= "     T1.REMARK1, ";
        $query .= "     T1.NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1, ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        //$query .= "     APPLICANT_BASE_MST T3 ";
        $query .= " WHERE ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO ";
        //$query .= "     AND T1.SCHREGNO = T3.SCHREGNO ";
        $query .= "     AND T2.SEMESTER = '". CTRL_SEMESTER ."' ";
        $query .= "     AND VALUE(T1.ENT_DIV, '0') != '7' ";
        $query .= "     AND (VALUE(T1.GRD_DIV, '0') IN ('0', '4') ";
        $query .= "          OR (VALUE(T1.GRD_DIV, '0') IN ('1', '2', '3') ";
        $query .= "              AND T1.GRD_DATE > '".str_replace("/", "-", CTRL_DATE)."') ";
        $query .= "         ) ";
        //$query .= "     AND T3.APPLICANT_DIV IN('3', '4') ";
        $query .= "     ".$and." ".$where;
        
        $query .= " ), NAME_T AS ( ";
        $query .= " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .CTRL_YEAR ."' ";
        $query .= "     AND NAMECD1 = 'W006' ";

        $query .= " ), CERTIF_DETAIL_MAX AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     MAX(CERTIF_INDEX) AS CERTIF_INDEX ";
        $query .= " FROM ";
        $query .= "     CERTIF_DETAIL_EACHTYPE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .CTRL_YEAR ."' ";
        $query .= "     AND TYPE = '2' ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO ";
        
        $query .= " ), CERTIF_DETAIL_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.REMARK1 AS STATION_FROM, ";
        $query .= "     T2.REMARK2 AS STATION_TO, ";
        $query .= "     T2.REMARK3 AS STATION_VIA ";
        $query .= " FROM ";
        $query .= "     CERTIF_DETAIL_MAX T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     CERTIF_DETAIL_EACHTYPE_DAT T2 ";
        $query .= "     ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     AND T1.CERTIF_INDEX = T2.CERTIF_INDEX ";
        $query .= " ) ";
        
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.NAME, ";
        //$query .= "     B1.SCHOOLNAME3 AS BELONGING_NAME, ";
        //$query .= "     N1.NAME1 AS COURSE_NAME, ";
        //$query .= "     S1.NAME AS STUDENT_NAME, ";
        $query .= "     T1.ENT_DATE, ";
        //$query .= "     T1.GRD_SCHEDULE_DATE, ";
        $query .= "     T1.REMARK1, ";
        $query .= "     C1.STATION_FROM, ";
        $query .= "     C1.STATION_TO, ";
        $query .= "     C1.STATION_VIA ";
        $query .= " FROM ";
        $query .= "     MAIN_T T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     CERTIF_DETAIL_DATA C1 ";
        $query .= "     ON T1.SCHREGNO = C1.SCHREGNO ";
        //$query .= " LEFT JOIN ";
        //$query .= "     BELONGING_MST B1 ";
        //$query .= "     ON T1.GRADE = B1.BELONGING_DIV ";
        //$query .= " LEFT JOIN ";
        //$query .= "     NAME_T N1 ";
        //$query .= "     ON T1.COURSE_DIV = N1.NAMECD2 ";
        //$query .= " LEFT JOIN ";
        //$query .= "     STUDENTDIV_MST S1 ";
        //$query .= "     ON T1.STUDENT_DIV = S1.STUDENT_DIV ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHREGNO ";
        
        return $query;
    }
//-----------------------------------------------------------------------------------------------------
    //生徒情報取得(NOT USE)
    function getStudentInfoData2($schregno)
    {
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.ENT_DATE, ";
        $query .= "     T1.REMARK1 ";
        //$query .= "     C1.STATION_FROM, ";
        //$query .= "     C1.STATION_TO, ";
        //$query .= "     C1.STATION_VIA ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1 ";
        //$query .= " LEFT JOIN ";
        //$query .= "     CERTIF_DETAIL_DATA C1 ";
        //$query .= "     ON T1.SCHREGNO = C1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$schregno."' ";

        
        return $query;
    }
//-----------------------------------------------------------------------------------------------------
    //生徒情報取得
    function getStudentInfoCnt($model)
    {
        list($and, $where) = knjwa170Query::getStudentSearch($model);
        
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1, ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        //$query .= "     APPLICANT_BASE_MST T3 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO ";
        //$query .= "     AND T1.SCHREGNO = T3.SCHREGNO ";
        $query .= "     AND T2.YEAR     = '" .CTRL_YEAR ."' ";
        $query .= "     AND T2.SEMESTER = '". CTRL_SEMESTER ."' ";
        $query .= "     AND VALUE(T1.ENT_DIV, '0') != '7' ";
        $query .= "     AND (VALUE(T1.GRD_DIV, '0') IN ('0', '4') ";
        $query .= "          OR (VALUE(T1.GRD_DIV, '0') IN ('1', '2', '3') ";
        $query .= "              AND T1.GRD_DATE > '".str_replace("/", "-", CTRL_DATE)."') ";
        $query .= "         ) ";
        //$query .= "     AND T3.APPLICANT_DIV IN('3', '4') ";
        $query .= "     ".$and." ".$where;
        return $query;
    }
    
    //生徒情報取得
    function getStudentSearch($model)
    {
        $and = "";
        $where = "";
        foreach ($model->search as $key => $val) {
            if ($val) {
                if ($key == "NAME" || $key == "NAME_KANA") {
                    $where .= $and." T1.".$key." LIKE'".$val."%' ";
                    $and = "AND";
                } else if ($key == "SCHREGNO1") {
                    $where .= $and." T1.SCHREGNO >= '".$val."' ";
                    $and = "AND";
                } else if ($key == "SCHREGNO2") {
                    $where .= $and." T1.SCHREGNO <= '".$val."' ";
                    $and = "AND";
                } else if ($key == "BELONGING_DIV") {
                    $where .= $and." T2.GRADE = '".$val."' ";
                    $and = "AND";
                } else if ($key == "EXE_YEAR") {
                    $where .= $and." T2.YEAR = '".$val."' ";
                    $and = "AND";
                } else {
                    $where .= $and." T1.".$key." = '".$val."' ";
                    $and = "AND";
                }
            }
        }
        
        //権限設定
        if ((AUTHORITY == DEF_REFER_RESTRICT 
            || AUTHORITY == DEF_UPDATE_RESTRICT)
            && !$model->search["BELONGING_DIV"])
        {
            $grade = join("','", $model->grade);
            $where .= $and." (T2.GRADE = '".$model->belonging["BELONGING_DIV"]."' ";
            $where .= "    OR T2.GRADE IN ('".$grade."')) ";
            $and = "AND";
        }
        
        return array($and, $where);
    }

    //証明書発行データ連番取得
    function getCertifIssueEachtypeMAX($year)
    {
        $query  = " SELECT ";
        $query .= "     MAX(CERTIF_INDEX) ";
        $query .= " FROM ";
        $query .= "     CERTIF_ISSUE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        //$query .= "     AND TYPE = '2' ";
        
        return $query;
    }
    
    //証明書発行データ発行番号取得
    function getCertifIssueEachtypePrintMAX($TYPE, $year)
    {
        $query  = " SELECT ";
        $query .= "     MAX(CERTIF_NO) ";
        $query .= " FROM ";
        $query .= "     CERTIF_ISSUE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        //$query .= "     AND TYPE = '".$TYPE."' ";
        
        return $query;
    }
    
    /**
     * 更新
     */
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        
        $ret = array();
        $schregnoArray = array();
        $seitoArray = array();
        $tugakuArray = array();
        $untinArray = array();
        $up_data = ($model->field["PRINT_CHECK"]) ? $model->field["PRINT_CHECK"]: array();
        foreach ($up_data as $val)
        {
            list($index, $schregno) = preg_split("/:/", $val);

            //生徒証
            if ($this->print_div == "1") {
                $CERTIF_INDEX = $db->getOne(knjwa170Query::getCertifIssueEachtypeMAX(CTRL_YEAR));
                
                $CERTIF_NO = $db->getOne(knjwa170Query::getCertifIssueEachtypePrintMAX("9", CTRL_YEAR));
                
                //証明書種別別発行データ
                $data = array();
                $data["YEAR"][TEXT]          = CTRL_YEAR;
                //$data["TYPE"][TEXT]          = "9";
                $data["CERTIF_INDEX"][TEXT]  = sprintf("%05d", intval($CERTIF_INDEX)+1);
                $data["SCHREGNO"][TEXT]      = $schregno;
                $data["CERTIF_KINDCD"][TEXT] = "308";
                $data["GRADUATE_FLG"][TEXT]  = "0";
                $data["APPLYDATE"][TEXT]     = CTRL_DATE;
                $data["ISSUERNAME"][TEXT]    = STAFFNAME_SHOW;
                $data["ISSUECD"][TEXT]       = "1";
                $data["CERTIF_NO"][NUMBER]   = intval($CERTIF_NO)+1;
                $data["ISSUEDATE"][TEXT]     = str_replace("/", "-", $model->print_date);
                
                $data["REGISTERCD"][TEXT]    = STAFFCD;
                $data["UPDATED"][FUNC]       = "sysdate()";
                
                $query = Query::insertSQL($data, "CERTIF_ISSUE_DAT");
                $db->query($query);
                
                //証明書種別別詳細データ
                $data = array();
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                //$data["TYPE"][TEXT]         = "9";
                $data["CERTIF_INDEX"][TEXT] = sprintf("%05d", intval($CERTIF_INDEX)+1);
                $data["SCHREGNO"][TEXT]     = $schregno;
                $data["REMARK4"][TEXT]      = str_replace("/", "-", $model->field["ENT_DATE"][$index]);
                $data["REMARK5"][TEXT]      = str_replace("/", "-", $model->field["GRD_SCHEDULE_DATE"][$index]);
                
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";
                
                $query = Query::insertSQL($data, "CERTIF_DETAIL_EACHTYPE_DAT");
                $db->query($query);
                
                $seitoArray[] = array("SEITO" => sprintf("%05d", intval($CERTIF_INDEX)+1) );
                
            }
            //通学証明書
            if ($this->print_div == "2") {
                $CERTIF_INDEX = $db->getOne(knjwa170Query::getCertifIssueEachtypeMAX(CTRL_YEAR));
                
                $CERTIF_NO = $db->getOne(knjwa170Query::getCertifIssueEachtypePrintMAX("2", CTRL_YEAR));
                
                //証明書種別別発行データ
                $data = array();
                $data["YEAR"][TEXT]          = CTRL_YEAR;
                //$data["TYPE"][TEXT]          = "2";
                $data["CERTIF_INDEX"][TEXT]  = sprintf("%05d", intval($CERTIF_INDEX)+1);
                $data["SCHREGNO"][TEXT]      = $schregno;
                $data["CERTIF_KINDCD"][TEXT] = "306";
                $data["GRADUATE_FLG"][TEXT]  = "0";
                $data["APPLYDATE"][TEXT]     = CTRL_DATE;
                $data["ISSUERNAME"][TEXT]    = STAFFNAME_SHOW;
                $data["ISSUECD"][TEXT]       = "1";
                $data["CERTIF_NO"][NUMBER]   = intval($CERTIF_NO)+1;
                $data["ISSUEDATE"][TEXT]     = str_replace("/", "-", $model->print_date);
                
                $data["REGISTERCD"][TEXT]    = STAFFCD;
                $data["UPDATED"][FUNC]       = "sysdate()";
                
                $query = Query::insertSQL($data, "CERTIF_ISSUE_DAT");
                $db->query($query);
                
                //証明書種別別詳細データ
                $data = array();
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                //$data["TYPE"][TEXT]         = "2";
                $data["CERTIF_INDEX"][TEXT] = sprintf("%05d", intval($CERTIF_INDEX)+1);
                $data["SCHREGNO"][TEXT]     = $schregno;
                $data["REMARK1"][TEXT]      = $model->field["STATION_FROM"][$index];
                $data["REMARK2"][TEXT]      = $model->field["STATION_TO"][$index];
                $data["REMARK3"][TEXT]      = $model->field["STATION_VIA"][$index];
                $data["REMARK4"][TEXT]      = str_replace("/", "-", $model->str_date);
                $data["REMARK5"][TEXT]      = str_replace("/", "-", $model->end_date);
                $data["REMARK6"][TEXT]      = $model->month_div;
                
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";
                
                $query = Query::insertSQL($data, "CERTIF_DETAIL_EACHTYPE_DAT");
                $db->query($query);
                
                $tugakuArray[] = array("TUGAKU" => sprintf("%05d", intval($CERTIF_INDEX)+1) );
                
            }
            //学校学生生徒旅客運賃割引証
            if ($this->print_div == "3") {
                $CERTIF_INDEX = $db->getOne(knjwa170Query::getCertifIssueEachtypeMAX(CTRL_YEAR));
                
                $CERTIF_NO = $db->getOne(knjwa170Query::getCertifIssueEachtypePrintMAX("2", CTRL_YEAR));
                
                //証明書種別別発行データ
                $data = array();
                $data["YEAR"][TEXT]          = CTRL_YEAR;
                //$data["TYPE"][TEXT]          = "2";
                $data["CERTIF_INDEX"][TEXT]  = sprintf("%05d", intval($CERTIF_INDEX)+1);
                $data["SCHREGNO"][TEXT]      = $schregno;
                $data["CERTIF_KINDCD"][TEXT] = "307";
                $data["GRADUATE_FLG"][TEXT]  = "0";
                $data["APPLYDATE"][TEXT]     = CTRL_DATE;
                $data["ISSUERNAME"][TEXT]    = STAFFNAME_SHOW;
                $data["ISSUECD"][TEXT]       = "1";
                $data["CERTIF_NO"][NUMBER]   = intval($CERTIF_NO)+1;
                $data["ISSUEDATE"][TEXT]     = str_replace("/", "-", $model->print_date);
                
                $data["REGISTERCD"][TEXT]    = STAFFCD;
                $data["UPDATED"][FUNC]       = "sysdate()";
                
                $query = Query::insertSQL($data, "CERTIF_ISSUE_DAT");
                $db->query($query);
                
                //証明書種別別詳細データ
                $data = array();
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                //$data["TYPE"][TEXT]         = "2";
                $data["CERTIF_INDEX"][TEXT] = sprintf("%05d", intval($CERTIF_INDEX)+1);
                $data["SCHREGNO"][TEXT]     = $schregno;
                $data["REMARK1"][TEXT]      = $model->field["STATION_FROM"][$index];
                $data["REMARK2"][TEXT]      = $model->field["STATION_TO"][$index];
                $data["REMARK3"][TEXT]      = $model->field["STATION_VIA"][$index];
                $data["REMARK4"][TEXT]      = str_replace("/", "-", $model->str_date);
                $data["REMARK5"][TEXT]      = str_replace("/", "-", $model->end_date);
                
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";
                
                $query = Query::insertSQL($data, "CERTIF_DETAIL_EACHTYPE_DAT");
                $db->query($query);
                
                $untinArray[] = array("UNTIN" => sprintf("%05d", intval($CERTIF_INDEX)+1) );
                
            }
            
            $schregnoArray[] = array("SCHREGNO" => $schregno);
            
        }
        
        $ret = array($schregnoArray,
                     $seitoArray,
                     $tugakuArray,
                     $untinArray
        );
        
        $db->commit();
        Query::dbCheckIn($db);

        return $ret;
    }

}
?>
