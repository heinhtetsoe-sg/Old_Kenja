<?php

require_once('for_php7.php');


class knjd172kQuery extends Query {

    //対象外の生徒取得
    function getSchnoIdou($model,$semester)
    {
/************** 2005.10.06
        $query  = "WITH SCHNO AS (  ";
        $query .= "    SELECT SCHREGNO FROM SCHREG_REGD_DAT  ";
        $query .= "    WHERE YEAR='".CTRL_YEAR."' AND SEMESTER='".$semester."' AND ";
        $query .= "    		 GRADE || HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ),  ";
        $query .= "BASE AS (  ";
        $query .= "    SELECT SCHREGNO FROM SCHREG_BASE_MST  ";
        $query .= "    WHERE SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) AND  ";
        $query .= "        GRD_DIV IN ('2','3') AND GRD_DATE <= '".str_replace("/","-",$model->field["DATE"])."'  ";
        $query .= "    GROUP BY SCHREGNO ), ";
        $query .= "TRANS AS (  ";
        $query .= "    SELECT SCHREGNO FROM SCHREG_TRANSFER_DAT  ";
        $query .= "    WHERE SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) AND  ";
        $query .= "        ((TRANSFERCD IN ('1','2','3') AND  ";
        $query .= "          '".str_replace("/","-",$model->field["DATE"])."' BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE) OR  ";
        $query .= "         (TRANSFERCD IN ('4') AND '".str_replace("/","-",$model->field["DATE"])."' < TRANSFER_SDATE) )  ";
        $query .= "    GROUP BY SCHREGNO ) ";

        $query .= "SELECT SCHREGNO FROM BASE  ";
        $query .= "UNION SELECT SCHREGNO FROM TRANS  ";
**************/
        $date = str_replace("/","-",$model->field["DATE"]);

        $query  = "";
        $query .= " SELECT T1.SCHREGNO, T1.ATTENDNO ";
        $query .= " FROM   SCHREG_REGD_DAT T1, SEMESTER_MST T2 ";
        $query .= " WHERE  T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "        T1.SEMESTER = '".$semester."' AND ";
        $query .= "        T1.YEAR = T2.YEAR AND ";
        $query .= "        T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "        T1.GRADE||T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ( ";
                           //転学(2)・退学(3)者 但し異動日が学期終了日または異動基準日より小さい場合
                           //転入(4)・編入(5)者 但し異動日が学期終了日または異動基準日より大きい場合
        $query .= "        EXISTS(SELECT 'X' FROM SCHREG_BASE_MST S1 ";
        $query .= "                WHERE S1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                    ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE <  ";
        $query .= "                         CASE WHEN T2.EDATE < '".$date."' ";
        $query .= "                              THEN T2.EDATE ELSE '".$date."' END) OR ";
        $query .= "                     (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE >  ";
        $query .= "                         CASE WHEN T2.EDATE < '".$date."' ";
        $query .= "                              THEN T2.EDATE ELSE '".$date."' END)) ) OR ";
                           //留学(1)・休学(2)者
        $query .= "        EXISTS(SELECT 'X' FROM SCHREG_TRANSFER_DAT S1 ";
        $query .= "                WHERE S1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                      S1.TRANSFERCD IN ('1','2') AND  ";
        $query .= "                      CASE WHEN T2.EDATE < '".$date."' ";
        $query .= "                           THEN T2.EDATE ELSE '".$date."' END  ";
        $query .= "                      BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ) ";
        $query .= " ORDER BY T1.ATTENDNO ";

        return $query;
    }


    //年組取得（権限チェック）
    function getAuth($year,$semester)
    {
		//参照・更新可
		if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
			$query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
			$query .= "FROM SCHREG_REGD_HDAT ";
			$query .= "WHERE YEAR='" .$year ."'";
			$query .= "AND SEMESTER='".$semester ."'";
		}
		//参照・更新可（制限付き）
		if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
			$query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
			$query .= "FROM SCHREG_REGD_HDAT ";
			$query .= "WHERE YEAR='" .$year ."' ";
			$query .= "AND SEMESTER='".$semester ."' ";
	    	$query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
	    	$query .= "	OR TR_CD2 = '" .STAFFCD ."' ";
	    	$query .= "	OR TR_CD3 = '" .STAFFCD ."' ";
            //---2005.06.01
	    	$query .= " OR SUBTR_CD1 = '" .STAFFCD ."' ";
	    	$query .= "	OR SUBTR_CD2 = '" .STAFFCD ."' ";
	    	$query .= "	OR SUBTR_CD3 = '" .STAFFCD ."') ";
		}

        return $query;
    }


    //学年取得（権限チェック） NO001//---2005.06.01
    function getAuth2($year,$semester)
    {
		//参照・更新可
//		if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
			$query  = "SELECT DISTINCT GRADE AS VALUE,cast(cast(GRADE as int) as char) || '学年' AS LABEL ";
			$query .= "FROM SCHREG_REGD_HDAT ";
			$query .= "WHERE YEAR='" .$year ."'";
			$query .= "AND SEMESTER='".$semester ."'";
//		}
		//参照・更新可（制限付き）
/*		if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
			$query  = "SELECT DISTINCT GRADE AS VALUE,cast(cast(GRADE as int) as char) || '学年' AS LABEL ";
			$query .= "FROM SCHREG_REGD_HDAT ";
			$query .= "WHERE YEAR='" .$year ."' ";
			$query .= "AND SEMESTER='".$semester ."' ";
	    	$query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
	    	$query .= "	OR TR_CD2 = '" .STAFFCD ."' ";
	    	$query .= "	OR TR_CD3 = '" .STAFFCD ."') ";
		}
*/
        return $query;
    }


    //生徒データ NO001
    function gettaisyo($model,$semester)
    {
		$query  = "SELECT T2.SCHREGNO,T2.ATTENDNO,T1.NAME_SHOW ";
		$query .= "FROM SCHREG_BASE_MST T1,SCHREG_REGD_DAT T2 ";
		$query .= "WHERE T2.YEAR = '".$model->control["年度"]."' AND ";
		$query .= "		 T2.SEMESTER = '".$semester."' AND ";
		$query .= "		 T2.GRADE || T2.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
		$query .= "		 T2.SCHREGNO = T1.SCHREGNO ";
		$query .= "ORDER BY T2.ATTENDNO ";

        return $query;
    }

    //学年データ NO001
    function gettaisyo2($model,$semester)
    {
/*---2005.06.01
		$query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
		$query .= "FROM SCHREG_REGD_HDAT ";
		$query .= "WHERE YEAR='" .$model->control["年度"] ."' ";
		$query .= "AND SEMESTER='".$semester ."' ";
		$query .= "AND GRADE='".$model->field["GRADE_HR_CLASS"] ."' ";
*/
		//参照・更新可
		if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
			$query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
			$query .= "FROM SCHREG_REGD_HDAT ";
			$query .= "WHERE YEAR='" .$model->control["年度"] ."'";
			$query .= "AND SEMESTER='".$semester ."' ";
			$query .= "AND GRADE='".$model->field["GRADE_HR_CLASS"] ."'";
		}
		//参照・更新可（制限付き）
		if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
			$query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
			$query .= "FROM SCHREG_REGD_HDAT ";
			$query .= "WHERE YEAR='" .$model->control["年度"] ."'";
			$query .= "AND SEMESTER='".$semester ."' ";
			$query .= "AND GRADE='".$model->field["GRADE_HR_CLASS"] ."'";
	    	$query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
	    	$query .= "	OR TR_CD2 = '" .STAFFCD ."' ";
	    	$query .= "	OR TR_CD3 = '" .STAFFCD ."' ";
            //---2005.06.01
	    	$query .= " OR SUBTR_CD1 = '" .STAFFCD ."' ";
	    	$query .= "	OR SUBTR_CD2 = '" .STAFFCD ."' ";
	    	$query .= "	OR SUBTR_CD3 = '" .STAFFCD ."') ";
		}
	    return $query;
    }
}
?>
