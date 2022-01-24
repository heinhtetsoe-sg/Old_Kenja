<?php

require_once('for_php7.php');


class knja131jQuery extends Query {

    //年組取得（権限チェック）
    function getAuth($model)
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


	//１．生徒指導要録出力////////////////////////////////////////////////////////////////
    //個人学籍データ
    function getinfo_ps2_1($schregno)
    {
		$query = "SELECT "
					. "T2.NAME,"
					. "T2.NAME_KANA,"
					. "T2.BIRTHDAY,"
			//性別
					. "T7.ABBV1 AS SEX,"
					. "T1.GRADE,"
					. "T1.ATTENDNO,"
					. "T1.ANNUAL,"
					. "T6.HR_NAME,"
			//課程・学科・コース
					. "T3.COURSENAME,"
					. "T4.MAJORNAME,"
					. "T5.COURSECODENAME,"
					. "T3.COURSEABBV,"
					. "T4.MAJORABBV,"
			//卒業
					. "CASE WHEN T2.GRD_DATE IS NULL THEN RTRIM(CHAR(INT(T1.YEAR)+case t1.annual when '01' then 3 when '02' then 2 else 1 end)) || '-' || RTRIM(CHAR(MONTH(T10.GRADUATE_DATE))) || '-01' ELSE VARCHAR(T2.GRD_DATE) END AS GRADU_DATE,"
//					. "CASE WHEN T2.GRD_DATE IS NULL THEN "
//						. "CASE WHEN INT(T1.ANNUAL)<3 THEN NULL "
//							. "ELSE RTRIM(CHAR(INT(T1.YEAR)+1)) || '-' || RTRIM(CHAR(MONTH(T10.GRADUATE_DATE))) || '-01' END "
//						. "ELSE VARCHAR(T2.GRD_DATE) END AS GRADU_DATE,"
					. "CASE WHEN T2.GRD_DATE IS NULL THEN '卒業見込み' ELSE "
						. "(SELECT NAME1 FROM NAME_MST ST2 WHERE ST2.NAMECD1 = 'A003' "
							. "AND T2.GRD_DIV = ST2.NAMECD2) END AS GRADU_NAME,"
			//入学
				    . "T2.ENT_DATE,"
				    . "T2.ENT_DIV,"
				    . "(SELECT MIN(ANNUAL) FROM SCHREG_REGD_DAT ST1 WHERE ST1.SCHREGNO=T1.SCHREGNO) AS ENTER_GRADE,"
					. "(SELECT NAME1 FROM NAME_MST ST2 WHERE ST2.NAMECD1 = 'A002' "
					. "AND T2.ENT_DIV = ST2.NAMECD2) AS ENTER_NAME,"
			//住所
					. "VALUE(T8.ADDR1,'') || VALUE(T8.ADDR2,'') AS ADDR,"
				    . "T8.ADDR1,"
				    . "T8.ADDR2,"
					. "T8.TELNO,"
				    . "T8.ZIPCD,"
			//卒業中学情報
					. "T2.FINISH_DATE,"
					. "(SELECT FINSCHOOL_NAME FROM FINSCHOOL_MST ST1 WHERE ST1.FINSCHOOLCD = T2.FINSCHOOLCD) AS J_NAME,"
			//保護者情報
	                . "VALUE(T12.GUARD_NAME,'　') AS GUARD_NAME,"
	                . "VALUE(T12.GUARD_KANA,'　') AS GUARD_KANA,"
//	                . "T12.GUARD_NAME,"
//	                . "T12.GUARD_KANA,"
					. "VALUE(T12.GUARD_ADDR1,'') || VALUE(T12.GUARD_ADDR2,'') AS GUARD_ADDR,"
				    . "T12.GUARD_ADDR1,"
				    . "T12.GUARD_ADDR2,"
	                . "T12.GUARD_ZIPCD,"
					. "T1.SCHREGNO "
				. "FROM "
			//学籍情報(??? or ????)
					. "("
						. "SELECT * "
						. "FROM "
							. "SCHREG_REGD_DAT T1 "
						. "WHERE "
								. "T1.SCHREGNO='" .$schregno ."' "
							. "AND T1.YEAR='" .CTRL_YEAR ."' "
							. "AND T1.SEMESTER='" .CTRL_SEMESTER ."' "
					. ") T1 "
					. "INNER JOIN SCHREG_REGD_HDAT T6 ON T6.YEAR = T1.YEAR AND T6.SEMESTER = T1.SEMESTER "
														. "AND T6.GRADE = T1.GRADE AND T6.HR_CLASS = T1.HR_CLASS "

			//卒業情報有りの場合
					. "INNER JOIN SCHOOL_MST T10 ON T10.YEAR = T1.YEAR "
			//基礎情報
					. "INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO "
					. "LEFT JOIN NAME_MST T7 ON NAMECD1='Z002' AND NAMECD2=T2.SEX "
			//課程、学科、コース
					. "LEFT JOIN COURSE_MST T3 ON T3.COURSECD = T1.COURSECD "
					. "LEFT JOIN MAJOR_MST T4 ON T4.COURSECD = T1.COURSECD AND T4.MAJORCD = T1.MAJORCD "
					. "LEFT JOIN V_COURSECODE_MST T5 ON T5.YEAR = T1.YEAR "
												. "AND VALUE(T5.COURSECODE,'0000') = VALUE(T1.COURSECODE,'0000')"
			//生徒住所(??)
				    . "LEFT JOIN SCHREG_ADDRESS_DAT AS T8 "
						    . "INNER JOIN("
								. "SELECT "
						    		. "MAX(ISSUEDATE) AS ISSUEDATE "
						    	. "FROM "
						    		. "SCHREG_ADDRESS_DAT "
						    	. "WHERE "
						    			. "SCHREGNO='" .$schregno ."' "
									. "AND FISCALYEAR(ISSUEDATE) <='" .CTRL_YEAR ."' "
							. ")T9 ON T9.ISSUEDATE = T8.ISSUEDATE "
						. "ON T8.SCHREGNO = T1.SCHREGNO "
			//保護者情報
					. "LEFT JOIN GUARDIAN_DAT T12 ON T12.SCHREGNO = T2.SCHREGNO ";

        return $query;
    }


	//生徒住所履歴
    function getinfo_ps3_1($schregno)
    {
		$query = "SELECT "
					. "T1.ISSUEDATE,"
					. "T1.ADDR1,"
					. "T1.ADDR2,"
					. "T1.ZIPCD,"
					. "T2.COUNT,"
					. "T1.SCHREGNO "
				. "FROM "
					. "SCHREG_ADDRESS_DAT T1 "
					. "LEFT JOIN("
						. "SELECT "
							. "SCHREGNO,"
							. "COUNT(SCHREGNO) AS COUNT "
						. "FROM "
							. "SCHREG_ADDRESS_DAT T1 "
						. "WHERE "
								. "SCHREGNO ='" .$schregno ."' "
							. "AND FISCALYEAR(ISSUEDATE) <='" .CTRL_YEAR ."' "
						. "GROUP BY "
							. "SCHREGNO "
					. ")T2 ON T2.SCHREGNO = T1.SCHREGNO "
				
				. "WHERE "
						. "T1.SCHREGNO ='" .$schregno ."' "
					. "AND FISCALYEAR(ISSUEDATE) <='" .CTRL_YEAR ."' "
				. "ORDER BY "
					. "ISSUEDATE DESC";

        return $query;
    }


	//生徒異動履歴
    function getinfo_ps4_1($schregno)
    {
		$query = "SELECT "
				    . "T1.YEAR,"
				    . "T1.SDATE,"
				    . "T1.EDATE,"
				    . "T1.REASON,"
				    . "T1.PLACE,"
				    . "T1.ADDR,"
				    . "T1.NAMECD2,"
				    . "T1.NAMECD1,"
				    . "T3.NAME1,"
				    . "CASE T2.SCHOOLDIV WHEN '0' THEN T4.GRADE ELSE T5.GRADE END AS GRADE "
				. "FROM "
				    . "("
				        . "SELECT "
				            . "FISCALYEAR(ENT_DATE) AS YEAR,"
				            . "ENT_DATE AS SDATE,"
				            . "ENT_DATE AS EDATE,"
				            . "'' AS REASON,"
				            . "'' AS PLACE,"
				            . "'' AS ADDR,"
				            . "ENT_DIV AS NAMECD2,"
				            . "'A002' AS NAMECD1 "
				        . "FROM "
				            . "SCHREG_BASE_MST "
				        . "WHERE "
				            . "SCHREGNO='" .$schregno ."' "
				        . "UNION SELECT "
				            . "FISCALYEAR(GRD_DATE) AS YEAR,"
				            . "GRD_DATE AS SDATE,"
				            . "GRD_DATE AS EDATE,"
				            . "GRD_REASON AS REASON,"
				            . "GRD_NO AS PLACE,"		//卒業番号を入れる
				            . "'' AS ADDR,"
				            . "GRD_DIV AS NAMECD2,"
				            . "'A003' AS NAMECD1 "
				        . "FROM "
				            . "SCHREG_BASE_MST "
				        . "WHERE "
				            . "SCHREGNO='" .$schregno ."' "
				        . "UNION SELECT "
				            . "FISCALYEAR(TRANSFER_SDATE) AS YEAR,"
				            . "TRANSFER_SDATE AS SDATE,"
				            . "TRANSFER_EDATE AS EDATE,"
				            . "TRANSFERREASON AS REASON,"
				            . "TRANSFERPLACE AS PLACE,"
				            . "TRANSFERADDR AS ADDR,"
				            . "TRANSFERCD AS NAMECD2,"
				            . "'A004' AS NAMECD1 "
				        . "FROM "
				            . "SCHREG_TRANSFER_DAT "
				        . "WHERE "
				            . "SCHREGNO='" .$schregno ."' "
				    . ")T1 "
				    . "INNER JOIN SCHOOL_MST T2 ON T2.YEAR=T1.YEAR "
				    . "INNER JOIN NAME_MST T3 ON T3.NAMECD1=T1.NAMECD1 AND T3.NAMECD2=T1.NAMECD2 "
				    . "LEFT JOIN("
				        . "SELECT "
				            . "'0' AS SCHOOLDIV,"
				            . "YEAR,"
				            . "GRADE "
				        . "FROM "
				            . "V_REGDYEAR_GRADE_DAT "		//学年制
				        . "WHERE "
				            . "SCHREGNO='" .$schregno ."' "
				    . ")T4 ON T4.YEAR=T2.YEAR AND T4.SCHOOLDIV=T2.SCHOOLDIV "
				    . "LEFT JOIN("
				        . "SELECT "
				            . "'1' AS SCHOOLDIV,"
				            . "YEAR,"
				            . "GRADE "
				        . "FROM "
				            . "V_REGDYEAR_UNIT_DAT "		//単位制
				        . "WHERE "
				            . "SCHREGNO='" .$schregno ."' "
				    . ")T5 ON T5.YEAR=T2.YEAR AND T5.SCHOOLDIV=T2.SCHOOLDIV "
				. "WHERE "
					. "T1.YEAR<='" .CTRL_YEAR ."' "
				. "ORDER BY "
				    . "NAMECD1,NAMECD2,SDATE";

        return $query;
    }


	//学籍等履歴(１と３で使用：ps5_3)
    function getinfo_ps5_1($schregno)
    {

	$query = "SELECT "
			    . "T1.YEAR,"
			    . "T1.GRADE,"
			    . "T1.HR_CLASS,"
			    . "T1.ATTENDNO,"
				. "T1.ANNUAL,"
			    . "T3.HR_NAME,"
				. "T2.SCHOOLDIV,"
			    . "T4.STAFFNAME,"
			    . "T6.STAFFNAME AS PRINCIPALNAME "
			. "FROM "
			    . "("
			        . "SELECT "
			            . "'0' AS SCHOOLDIV,"
						. "SCHREGNO,"
			            . "YEAR,"
			            . "SEMESTER,"
			            . "GRADE,"
			            . "HR_CLASS,"
			            . "ATTENDNO,"
			            . "ANNUAL "
			        . "FROM "
			            . "V_REGDYEAR_GRADE_DAT "
			        . "WHERE "
			            . "SCHREGNO='" .$schregno ."' "
			        . "UNION SELECT "
			            . "'1' AS SCHOOLDIV,"
						. "SCHREGNO,"
			            . "YEAR,"
			            . "SEMESTER,"
			            . "GRADE,"
			            . "HR_CLASS,"
			            . "ATTENDNO,"
			            . "ANNUAL "
			        . "FROM "
			            . "V_REGDYEAR_UNIT_DAT "
			        . "WHERE "
			            . "SCHREGNO='" .$schregno ."' "
			    . ")T1 "
			    . "INNER JOIN SCHOOL_MST T2 ON T2.YEAR=T1.YEAR AND T2.SCHOOLDIV=T1.SCHOOLDIV "
			    . "INNER JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR=T1.YEAR "
													. "AND T3.SEMESTER=T1.SEMESTER "
													. "AND T3.GRADE=T1.GRADE "
													. "AND T3.HR_CLASS=T1.HR_CLASS "
			    . "LEFT JOIN STAFF_MST T4 ON T4.STAFFCD=T3.TR_CD1 "
			    . "LEFT JOIN("
			        . "SELECT "
			            . "YEAR,"
			            . "MAX(STAFFCD) AS STAFFCD "
			        . "FROM "
			            . "V_STAFF_MST "
			        . "WHERE "
			                . "YEAR<='" .CTRL_YEAR ."' "
			            . "AND JOBCD='0001' "
			        . "GROUP BY "
			            . "YEAR "
			    . ")T5 ON T5.YEAR=T2.YEAR "
			    . "LEFT JOIN STAFF_MST T6 ON T6.STAFFCD=T5.STAFFCD "
			. "WHERE "
			    . "T1.YEAR<='" .CTRL_YEAR ."' "
			. "ORDER BY "
			    . "T1.GRADE";

        return $query;
    }


	//最高年次(１・３・４で使用)
    function getinfo_max_grade($schregno)
    {
		$query = "SELECT "
					. "MAX(CASE WHEN W1.ANNUAL IS NULL THEN W2.ANNUAL ELSE W1.ANNUAL END) ANNUAL_MAX "
				. "FROM(SELECT SCHREGNO,ANNUAL FROM SCHREG_REGD_DAT W1 WHERE W1.SCHREGNO='" . $schregno . "' AND W1.YEAR<='" . CTRL_YEAR . "')W1 "
					. "FULL JOIN(SELECT SCHREGNO,ANNUAL FROM GRD_REGD_DAT W1 WHERE W1.SCHREGNO='" . $schregno . "' AND W1.YEAR<='" . CTRL_YEAR . "')W2 "
						. "ON W2.SCHREGNO=W1.SCHREGNO";

        return $query;
    }


	//２．修得単位の記録出力////////////////////////////////////////////////////////////////
	//学習記録データ(ps3_3と類似)	//2004/04/01 nakamoto 教科コード仕様の変更に伴う修正
    function getinfo_ps1_2($schregno)
    {
		$subject_D = "01";			//教科コード			//2004/04/01
		$subject_U = "89";			//教科コード			//2004/04/01
		$subject_T = "90";			//総合的な学習の時間	//2004/04/01

		//該当生徒の成績データ表
			$query  = "WITH STUDYREC AS(";
			$query .= 		"SELECT ";
			$query .= 			"SCHREGNO,YEAR,ANNUAL,CLASSCD,SUBCLASSCD,VALUATION AS GRADES,";
			$query .= 			"GET_CREDIT AS CREDIT,ADD_CREDIT ";
			$query .= 		"FROM ";
			$query .= 			"SCHREG_STUDYREC_DAT T1 ";
			$query .= 		"WHERE ";
			$query .=			"T1.SCHREGNO = '" . $schregno . "' AND YEAR <= '" . CTRL_YEAR . "' AND ";
			$query .=			"(CLASSCD BETWEEN '".$subject_D."' AND '".$subject_U."' OR CLASSCD='".$subject_T."')) ";

		//該当生徒の科目評定、修得単位及び教科評定平均
			$query .= 	"SELECT ";
			$query .= 		"T1.ANNUAL,T1.CLASSCD,";
			$query .= 		"T2.CLASSNAME,";
			$query .= 		"T1.SUBCLASSCD,";
			$query .= 		"T3.SUBCLASSNAME,";
			$query .= 		"0 AS GRADES,0 AS AVG_GRADES,'' AS ASSESS_LEVEL,";
			$query .= 		"T1.CREDIT AS GRADE_CREDIT,T4.CREDIT ";
			$query .= 	"FROM ";
			$query .= 		"STUDYREC T1 ";
			$query .= 		"INNER JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ";
			$query .= 		"INNER JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ";
				//	修得単位数の計
			$query .= 		"INNER JOIN(SELECT ";
			$query .= 				"CLASSCD,SUBCLASSCD,SUM(T1.CREDIT) AS CREDIT ";
			$query .= 			"FROM ";
			$query .= 				"STUDYREC T1 ";
			$query .= 			"WHERE ";
			$query .= 				"CLASSCD BETWEEN '".$subject_D."' AND '".$subject_U."' ";
			$query .= 			"GROUP BY ";
			$query .= 				"CLASSCD,SUBCLASSCD ";
			$query .= 		")T4 ON T4.SUBCLASSCD = T1.SUBCLASSCD ";
			$query .= 	"WHERE ";
			$query .= 		"T1.CLASSCD BETWEEN '".$subject_D."' AND '".$subject_U."' ";

		//	総合学習の修得単位数（合計）
			$query .= 	"UNION SELECT ";
			$query .= 			"'0' AS ANNUAL,'".$subject_T."' AS CLASSCD,";
			$query .= 			"'sogo' AS CLASSNAME,'".$subject_T."01' AS SUBCLASSCD,'sogo' AS SUBCLASSNAME,";
			$query .= 			"0 AS GRADES,0 AS AVG_GRADES,'' AS ASSESS_LEVEL,0 AS GRADE_CREDIT,SUM(CREDIT) AS CREDIT ";
			$query .= 		"FROM ";
			$query .= 			"STUDYREC ";
			$query .= 		"WHERE ";
			$query .= 			"CLASSCD = '".$subject_T."' ";

		//	留学中の修得単位数（合計）
			$query .= 		"UNION SELECT ";
			$query .= 			"'0' AS ANNUAL,'AA' AS CLASSCD,'abroad' AS CLASSNAME,'AAAA' AS SUBCLASSCD,'abroad' AS SUBCLASSNAME,";
			$query .= 			"0 AS GRADES,0 AS AVG_GRADES,'' AS ASSESS_LEVEL,0 AS GRADE_CREDIT,SUM(ABROAD_CREDITS) AS CREDIT ";
			$query .= 		"FROM ";
			$query .= 			"(SELECT ";
			$query .= 				"SCHREGNO,ABROAD_CREDITS,FISCALYEAR(TRANSFER_SDATE) AS TRANSFER_YEAR ";
			$query .= 			"FROM  ";
			$query .= 				"SCHREG_TRANSFER_DAT  ";
			$query .= 			"WHERE  ";
			$query .= 		    	"SCHREGNO = '" . $schregno . "' AND TRANSFERCD = '1' ";
			$query .= 			")ST1 ";
			$query .= 		"WHERE ";
			$query .= 			"TRANSFER_YEAR <= '" . CTRL_YEAR . "' ";

		//	全体の修得単位数・全体の評定平均値
			$query .= 		"UNION SELECT ";
			$query .= 			"'0' AS ANNUAL,'ZZ' AS CLASSCD,'total' AS CLASSNAME,'ZZZZ' AS SUBCLASSCD,";
			$query .= 			"'total' AS SUBCLASSNAME,";
			$query .= 			"0 AS GRADES,";
			$query .= 			"0 AS AVG_GRADES,";
			$query .= 			"'' AS ASSESS_LEVEL,";
			$query .= 			"0 AS GRADE_CREDIT,";
			$query .= 			"SUM(T1.CREDIT) AS CREDIT ";
			$query .= 		"FROM ";
			$query .= 			"STUDYREC T1 ";
			$query .= 		"WHERE ";
			$query .= 			"CLASSCD BETWEEN '".$subject_D."' AND '".$subject_U."' ";
			$query .= 	"ORDER BY 2,4,1";

        return $query;
    }


	//３．学習の記録出力////////////////////////////////////////////////////////////////
    //個人学籍データ(３と４で使用)
    function getinfo_ps2_3($schregno)
    {
			$query  =	"SELECT ";
			$query .=		"T2.NAME,";
			$query .=		"T2.NAME_KANA,T2.BIRTHDAY,T7.ABBV1 AS SEX,";
			$query .=		"T1.GRADE,T1.ATTENDNO,T1.ANNUAL,T6.HR_NAME,";
			$query .=		"T1.SCHREGNO ";
			$query .=	"FROM ";
			//学籍情報(??? or ????)
			$query .=		"(	 SELECT		* ";
			$query .=			"FROM 		SCHREG_REGD_DAT T1 ";
			$query .=			"WHERE 		T1.SCHREGNO= '" . $schregno . "' AND T1.YEAR= '" . CTRL_YEAR . "' ";
			$query .=				"AND T1.SEMESTER=(SELECT MAX(SEMESTER) FROM SCHREG_REGD_DAT WHERE SCHREGNO= '" . $schregno . "' AND YEAR= '" . CTRL_YEAR . "')";
			$query .=		") T1 ";
			$query .=		"INNER JOIN SCHREG_REGD_HDAT T6 ON T6.YEAR = T1.YEAR AND T6.SEMESTER = T1.SEMESTER ";
			$query .=									"AND T6.GRADE = T1.GRADE AND T6.HR_CLASS = T1.HR_CLASS ";
			//基礎情報
			$query .=		"INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
			$query .=		"LEFT JOIN NAME_MST T7 ON NAMECD1='Z002' AND NAMECD2=T2.SEX ";

        return $query;
    }


    //学習記録データ(ps1_2と類似)
    function getinfo_ps3_3($schregno)
    {
		$subject_D = "01";			//教科コード			//2004/04/01
		$subject_U = "89";			//教科コード			//2004/04/01
		$subject_T = "90";			//総合的な学習の時間	//2004/04/01

		//該当生徒の成績データ表
		$query  ="WITH STUDYREC AS(";
		$query .=		"SELECT ";
		$query .=			"SCHREGNO,YEAR,ANNUAL,CLASSCD,SUBCLASSCD,VALUATION AS GRADES,";
		$query .=			"GET_CREDIT AS CREDIT,ADD_CREDIT ";
		$query .=		"FROM ";
		$query .=			"SCHREG_STUDYREC_DAT T1 ";
		$query .=		"WHERE ";
		$query .=			"T1.SCHREGNO = '" . $schregno . "' AND YEAR <= '" . CTRL_YEAR . "' AND ";
		$query .=			"(CLASSCD BETWEEN '".$subject_D."' AND '".$subject_U."' OR CLASSCD='".$subject_T."')) ";

		//該当生徒の科目評定、修得単位及び教科評定平均
		$query .=	"SELECT ";
		$query .=		"T1.ANNUAL,T1.CLASSCD,";
		$query .=		"T2.CLASSNAME,";
		$query .=		"T1.SUBCLASSCD,";
		$query .=		"T3.SUBCLASSNAME,";
		$query .=		"T1.GRADES AS GRADES,";
		$query .=		"T5.AVG_GRADES,'' AS ASSESS_LEVEL,";
		$query .=		"T1.CREDIT AS GRADE_CREDIT,T4.CREDIT ";
		$query .=	"FROM ";
		$query .=		"STUDYREC T1 ";
		$query .=		"INNER JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ";
		$query .=		"INNER JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ";
				//	修得単位数の計
		$query .=		"INNER JOIN(SELECT ";
		$query .=				"CLASSCD,SUBCLASSCD,SUM(T1.CREDIT) AS CREDIT ";
		$query .=			"FROM ";
		$query .=				"STUDYREC T1 ";
		$query .=			"WHERE ";
		$query .=				"CLASSCD BETWEEN '".$subject_D."' AND '".$subject_U."' ";
		$query .=			"GROUP BY ";
		$query .=				"CLASSCD,SUBCLASSCD ";
		$query .=		")T4 ON T4.SUBCLASSCD = T1.SUBCLASSCD ";
				//	各教科の評定平均値
		$query .=	"INNER JOIN(SELECT ";
		$query .=			"CLASSCD,";
		$query .=			"DECIMAL(ROUND(DECIMAL(AVG(FLOAT(T1.GRADES)),5,2),1),5,1) AS AVG_GRADES ";
		$query .=		"FROM ";
		$query .=			"STUDYREC T1 ";
		$query .=		"WHERE ";
		$query .=			"CLASSCD BETWEEN '".$subject_D."' AND '".$subject_U."' ";
		$query .=		"GROUP BY ";
		$query .=			"CLASSCD ";
		$query .=	")T5 ON T5.CLASSCD = T1.CLASSCD ";
		$query .=	"WHERE ";
		$query .=		"T1.CLASSCD BETWEEN '".$subject_D."' AND '".$subject_U."' ";

		//	総合学習の修得単位数（学年別）
		$query .="UNION SELECT ";
		$query .=		"ANNUAL,'".$subject_T."' AS CLASSCD,'sogo' AS CLASSNAME,'".$subject_T."01' AS SUBCLASSCD,'sogo' AS SUBCLASSNAME,";
		$query .=		"0 AS GRADES,0 AS AVG_GRADES,'' AS ASSESS_LEVEL,0 AS GRADE_CREDIT,SUM(CREDIT) AS CREDIT ";
		$query .=	"FROM ";
		$query .=		"STUDYREC ";
		$query .=	"WHERE ";
		$query .=		"CLASSCD = '".$subject_T."' ";
		$query .=	"GROUP BY ";
		$query .=		"ANNUAL ";
		//	総合学習の修得単位数（合計）
		$query .=	"UNION SELECT ";
		$query .=			"'0' AS ANNUAL,'".$subject_T."' AS CLASSCD,";
		$query .=			"'sogo' AS CLASSNAME,'".$subject_T."01' AS SUBCLASSCD,'sogo' AS SUBCLASSNAME,";
		$query .=			"0 AS GRADES,0 AS AVG_GRADES,'' AS ASSESS_LEVEL,0 AS GRADE_CREDIT,SUM(CREDIT) AS CREDIT ";
		$query .=		"FROM ";
		$query .=			"STUDYREC ";
		$query .=		"WHERE ";
		$query .=			"CLASSCD = '".$subject_T."' ";

		//	留学中の修得単位数（学年別）
		$query .=	"UNION SELECT ";
		$query .=		"ANNUAL,'AA' AS CLASSCD,'abroad' AS CLASSNAME,'AAAA' AS SUBCLASSCD,'abroad' AS SUBCLASSNAME,";
		$query .=		"0 AS GRADES,0 AS AVG_GRADES,'' AS ASSESS_LEVEL,0 AS GRADE_CREDIT,SUM(ABROAD_CREDITS) AS CREDIT ";
		$query .=	"FROM ";
		$query .=			"(SELECT ";
		$query .=			    "ABROAD_CREDITS,";
		$query .=				"INT(FISCALYEAR(TRANSFER_SDATE)) AS TRANSFER_YEAR ";
		$query .=			"FROM ";
		$query .=				"SCHREG_TRANSFER_DAT ";
		$query .=			"WHERE ";
		$query .=		    	"SCHREGNO ='" .$schregno ."' AND TRANSFERCD = '1' ";
		$query .=			")ST1,";
		$query .=			"(SELECT ";
		$query .=				"ANNUAL,MAX(YEAR) AS YEAR ";
		$query .=		 	"FROM ";
		$query .=				"SCHREG_REGD_DAT ";
		$query .=			"WHERE ";
		$query .=				"SCHREGNO ='" .$schregno ."' AND YEAR <='" . CTRL_YEAR . "' ";
		$query .=			"GROUP BY ";
		$query .=				"ANNUAL ";
		$query .=			")ST2 ";
		$query .=	"WHERE ";
		$query .=		"ST1.TRANSFER_YEAR <=" . CTRL_YEAR . " ";
		$query .=		"and INTEGER(ST2.YEAR) = ST1.TRANSFER_YEAR ";
		$query .=	"GROUP BY ";
		$query .=		"ANNUAL ";
		//	留学中の修得単位数（合計）
		$query .=		"UNION SELECT ";
		$query .=			"'0' AS ANNUAL,'AA' AS CLASSCD,'abroad' AS CLASSNAME,'AAAA' AS SUBCLASSCD,'abroad' AS SUBCLASSNAME,";
		$query .=			"0 AS GRADES,0 AS AVG_GRADES,'' AS ASSESS_LEVEL,0 AS GRADE_CREDIT,SUM(ABROAD_CREDITS) AS CREDIT ";
		$query .=		"FROM ";
		$query .=			"(SELECT ";
		$query .=				"SCHREGNO,ABROAD_CREDITS,INT(FISCALYEAR(TRANSFER_SDATE)) AS TRANSFER_YEAR ";
		$query .=			"FROM  ";
		$query .=				"SCHREG_TRANSFER_DAT  ";
		$query .=			"WHERE  ";
		$query .=		    	"SCHREGNO ='" .$schregno ."' AND TRANSFERCD = '1' ";
		$query .=			")ST1 ";
		$query .=		"WHERE ";
		$query .=			"TRANSFER_YEAR <=" . CTRL_YEAR . " ";

		//	修得単位数、評定平均（学年別）
		$query .=	"UNION SELECT ";
		$query .=		"ANNUAL,'ZZ' AS CLASSCD,'total' AS CLASSNAME,";
		$query .=		"'ZZZZ' AS SUBCLASSCD,'total' AS SUBCLASSNAME,";
		$query .=		"0 AS GRADES,";
		$query .=	"ROUND(DECIMAL(AVG(FLOAT(T1.GRADES)),5,2),1) AS AVG_GRADES,";
		$query .=		"'' AS ASSESS_LEVEL,0 AS GRADE_CREDIT,";
		$query .=		"SUM(T1.CREDIT) AS CREDIT ";
		$query .=	"FROM ";
		$query .=		"STUDYREC T1 ";
		$query .=	"WHERE ";
		$query .=		"CLASSCD BETWEEN '".$subject_D."' AND '".$subject_U."' ";
		$query .=	"GROUP BY ";
		$query .=		"ANNUAL ";

		//	全体の修得単位数・全体の評定平均値
		$query .=		"UNION SELECT ";
		$query .=			"'0' AS ANNUAL,'ZZ' AS CLASSCD,'total' AS CLASSNAME,'ZZZZ' AS SUBCLASSCD,";
		$query .=			"'total' AS SUBCLASSNAME,";
		$query .=			"0 AS GRADES,";
		$query .=			"ROUND(DECIMAL(AVG(FLOAT(T1.GRADES)),5,2),1) AS AVG_GRADES,";
		$query .=			"(SELECT 	ST2.ASSESSMARK ";
		$query .=		 	"FROM 		ASSESS_MST ST2 ";
		$query .=		 	"WHERE 	ST2.ASSESSCD='4' ";
		$query .=					"AND DECIMAL(ROUND(DECIMAL(AVG(FLOAT(T1.GRADES)),5,2),1),5,1) ";
		$query .=							"BETWEEN ST2.ASSESSLOW AND ST2.ASSESSHIGH) AS ASSESS_LEVEL,";
		$query .=			"0 AS GRADE_CREDIT,";
		$query .=			"SUM(T1.CREDIT) AS CREDIT ";
		$query .=		"FROM ";
		$query .=			"STUDYREC T1 ";
		$query .=		"WHERE ";
		$query .=			"CLASSCD BETWEEN '".$subject_D."' AND '".$subject_U."' ";
		$query .=	"ORDER BY 2,4,1";

        return $query;
    }


	//４．活動の記録出力////////////////////////////////////////////////////////////////
    //活動の記録データ(生徒別)
    function getinfo_ps5_4($schregno)
    {
		$query = "SELECT "
				    . "* "
				. "FROM "
				    . "HTRAINREMARK_HDAT W1 "
				. "WHERE "
				        . "SCHREGNO='" .$schregno ."' ";

        return $query;
    }


    //活動の記録データ(学年別)
    function getinfo_ps4_4($schregno)
    {
		$query = "SELECT "
				    . "* "
				. "FROM "
				    . "HTRAINREMARK_DAT W1 "
				. "WHERE "
				        . "SCHREGNO='" .$schregno ."' "
				    . "AND YEAR IN (SELECT "
				                    . "MAX(YEAR) "
				                . "FROM "
				                    . "HTRAINREMARK_DAT "
				                . "WHERE "
				                    . "SCHREGNO='" .$schregno ."' "
				                . "GROUP BY "
				                    . "ANNUAL)"
					. "AND YEAR<='" .CTRL_YEAR ."' "
				. "ORDER BY "
				    . "ANNUAL";

        return $query;
    }


    //出欠の記録データ
    function getinfo_ps3_4($schregno)
    {
		$query = "SELECT DISTINCT "
					. "YEAR,"
					. "ANNUAL,"
					. "VALUE(CLASSDAYS,0) AS CLASSDAYS,"							//授業日数
					. "VALUE(CLASSDAYS,0) - VALUE(OFFDAYS,0) AS ATTEND_1,"			//授業日数-休学日数:1
					. "VALUE(SUSPEND,0) + VALUE(MOURNING,0) AS SUSP_MOUR,"			//出停・忌引
					. "VALUE(SUSPEND,0) AS SUSPEND,"								//出停:2
					. "VALUE(MOURNING,0) AS MOURNING,"								//忌引:3
					. "VALUE(ABROAD,0) AS ABROAD,"									//留学:4
					. "VALUE(REQUIREPRESENT,0) AS REQUIREPRESENT,"					//要出席日数:5
					. "VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) "
								. "+ VALUE(NOACCIDENTNOTICE,0) AS ATTEND_6,"		//病欠＋事故欠（届・無）:6
					. "VALUE(PRESENT,0) AS PRESENT,"								//出席日数:7
					. "VALUE(MOURNING,0) + VALUE(SUSPEND,0) AS ATTEND_8 "			//忌引＋出停:8
				. "FROM "
					. "("
						. "SELECT "
							. "SCHREGNO,"
							. "YEAR,"
							. "ANNUAL,"
							. "SUM(CLASSDAYS) AS CLASSDAYS,"
							. "SUM(OFFDAYS) AS OFFDAYS,"
							. "SUM(ABSENT) AS ABSENT,"
							. "SUM(SUSPEND) AS SUSPEND,"
							. "SUM(MOURNING) AS MOURNING,"
							. "SUM(ABROAD) AS ABROAD,"
							. "SUM(REQUIREPRESENT) AS REQUIREPRESENT,"
							. "SUM(SICK) AS SICK,"
							. "SUM(ACCIDENTNOTICE) AS ACCIDENTNOTICE,"
							. "SUM(NOACCIDENTNOTICE) AS NOACCIDENTNOTICE,"
							. "SUM(PRESENT) AS PRESENT "
						. "FROM "
							. "SCHREG_ATTENDREC_DAT "
						. "WHERE "
								. "SCHREGNO ='" .$schregno ."' "
							. "AND YEAR <='" .CTRL_YEAR ."' "
						. "GROUP BY "
							. "SCHREGNO,"
							. "ANNUAL,"
							. "YEAR "
					. ")T1 "
				. "ORDER BY "
					. "T1.ANNUAL";

        return $query;
    }



}
?>
