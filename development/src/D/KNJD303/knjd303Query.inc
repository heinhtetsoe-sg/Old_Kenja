<?php

require_once('for_php7.php');


class knjd303Query extends Query {

    //学年取得
    function getSelectGrade($model)
    {
        $query  = "SELECT distinct grade FROM schreg_regd_hdat ";
        $query .= "WHERE year = '".CTRL_YEAR."' ";
	//	学年末は、今学期を使用	/* NO003 */
		if ($model->field["GAKKI"]=="9") {
			$query .= "AND SEMESTER='".CTRL_SEMESTER ."' ";
		} else {
			$query .= "AND SEMESTER='".$model->field["GAKKI"] ."' ";
		}
	//	３学期は、１・２年のみ表示。
		if ($model->field["GAKKI"]=="3")
       		$query .= "AND GRADE <> '03' ";
        $query .= "order by grade";

        return $query;
    }


    //科目リスト取得
    // 04/11/19Modify 制限付に対応するための修正
    function getSubclass($model)
    {
		/*
        $query  = "SELECT DISTINCT ";
        $query .= "    W1.SUBCLASSCD, ";
        $query .= "    W1.SUBCLASSNAME ";
        $query .= "FROM ";
        $query .= "    V_SUBCLASS_MST W1, ";
        $query .= "    KIN_RECORD_DAT W2 ";
        $query .= "WHERE ";
        $query .= "    W1.YEAR='".CTRL_YEAR."' AND ";
        $query .= "    W1.YEAR=W2.YEAR AND ";
        $query .= "    W1.SUBCLASSCD=W2.SUBCLASSCD ";
        $query .= "ORDER BY ";
        $query .= "    W1.SUBCLASSCD ";
		*/
        $query  = "SELECT  ";
        $query .= "    T1.SUBCLASSCD, ";
        $query .= "    T1.SUBCLASSNAME ";
        $query .= "FROM ";
        $query .= "    (SELECT DISTINCT ";
        if ($model->Properties["useCurriculumcd"] == '1')  {
            $query .= "        W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ";
        }
        $query .= "        W1.SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "        W1.SUBCLASSNAME ";
        $query .= "    FROM ";
        $query .= "        V_SUBCLASS_MST W1, ";
        $query .= "        KIN_RECORD_DAT W2 ";
        $query .= "    WHERE ";
        $query .= "        W1.YEAR='".CTRL_YEAR."' AND ";
        $query .= "        W1.YEAR=W2.YEAR AND ";
        if ($model->Properties["useCurriculumcd"] == '1')  {
            $query .= "        W1.CLASSCD=W2.CLASSCD AND ";
            $query .= "        W1.SCHOOL_KIND=W2.SCHOOL_KIND AND ";
            $query .= "        W1.CURRICULUM_CD=W2.CURRICULUM_CD AND ";
        }
        $query .= "        W1.SUBCLASSCD=W2.SUBCLASSCD AND ";
        $query .= "        W1.SUBCLASSCD < '900000' AND ";//---NO004
        if ($model->Properties["useCurriculumcd"] == '1')  {
            $query .= "        W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ";
        }
        $query .= "        W1.SUBCLASSCD NOT IN (SELECT N1.NAME1 FROM V_NAME_MST N1 WHERE W1.YEAR = N1.YEAR AND N1.NAMECD1 = 'D065') ";
        $query .= "    ) T1  ";
		//参照・更新可
		if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE) 
	        $query .= "    LEFT JOIN (SELECT ";
		//参照・更新可（制限付き）
		if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) 
	        $query .= "    INNER JOIN (SELECT ";

        if ($model->Properties["useCurriculumcd"] == '1')  {
            $query .= "        w1.classcd || '-' || w1.school_kind || '-' || w1.curriculum_cd || '-' || ";
        }
        $query .= "        w1.subclasscd as subclasscd ";
        $query .= "    FROM ";
        $query .= "        chair_dat w1, ";
        $query .= "        chair_stf_dat w2 ";
        $query .= "    WHERE ";
        $query .= "        w2.year='".CTRL_YEAR."' AND ";
	//	学年末は、今学期を使用	/* NO003 */
		if ($model->field["GAKKI"]=="9") {
	        $query .= "        w2.semester='".CTRL_SEMESTER."' AND ";
		} else {
	        $query .= "        w2.semester='".$model->field["GAKKI"]."' AND ";
		}
        $query .= "        w2.staffcd='".STAFFCD."' AND ";
        $query .= "        w1.year=w2.year AND ";
        $query .= "        w1.semester=w2.semester AND ";
        $query .= "        w1.chaircd=w2.chaircd ";
        $query .= "    GROUP BY ";
        if ($model->Properties["useCurriculumcd"] == '1')  {
            $query .= "        w1.classcd || '-' || w1.school_kind || '-' || w1.curriculum_cd || '-' || ";
        }
        $query .= "        w1.subclasscd ";
        $query .= "    ) T2 ON T1.SUBCLASSCD=T2.SUBCLASSCD ";
        $query .= "     ";
        $query .= "ORDER BY ";
        $query .= "    T1.SUBCLASSCD ";

        return $query;
    }


    //学期リスト取得
    function getSemester($model)
    {
        $query  = "SELECT semester,semestername FROM semester_mst ";
        $query .= "WHERE year='" .CTRL_YEAR ."' ";	/* NO003 */
        $query .= "order by semester";

        return $query;
    }

}
?>
