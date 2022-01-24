<?php

require_once('for_php7.php');

class knjm700nQuery extends Query {

    
    //単位時間取得
    function selectCreditTime($cd1){
        $query  = " SELECT ";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "         NAMECD1 = '".$cd1."' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }
    
    
    
    //学生データ取得
    function selectSch($model){
        $query.=" SELECT ";
        $query.=" 	REGD.SCHREGNO, ";
        $query.=" 	REGD.ATTENDNO, ";
        $query.=" 	BASE.NAME, ";
        $query.=" 	SPACT.CREDIT_TIME  ";
        $query.=" FROM ";
        $query.=" 	SCHREG_REGD_DAT REGD  ";
        $query.=" 	INNER JOIN SCHREG_BASE_MST BASE  ";
        $query.=" 		ON REGD.SCHREGNO = BASE.SCHREGNO  ";
        $query.=" 	LEFT JOIN SPECIALACT_ATTEND_DAT SPACT  ";
        $query.=" 		ON REGD.YEAR = SPACT.YEAR  ";
        $query.=" 		AND REGD.SCHREGNO = SPACT.SCHREGNO  ";
        $query.=" 		AND REGD.SEMESTER = SPACT.SEMESTER  ";
        $query.=" 		AND SPACT.CLASSCD || '-' || SPACT.SCHOOL_KIND || '-' || SPACT.CURRICULUM_CD || '-' || SPACT.SUBCLASSCD  ";
        $query.=" 		IN (SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '".CTRL_YEAR."' AND NAMECD1 ='M026')  ";
        $query.=" 		AND SPACT.ATTENDDATE = '".str_replace("/","-",$model->Date)."' ";
        $query.=" 		AND SPACT.PERIODF = '1'  ";
        $query.=" WHERE ";
        $query.= "  REGD.YEAR = '".CTRL_YEAR."'  ";
        $query.= "  AND REGD.SEMESTER = '".$model->semester."' ";
        if($model->field["HR_NAME"]!=""){
            $query.= "  AND REGD.GRADE || '-' || REGD.HR_CLASS = '".$model->field["HR_NAME"]."' ";
        }
        $query.=" ORDER BY ";
        $query.=" 	REGD.ATTENDNO ";

        return $query;
    }
    
    //クラス取得
    function selectHRName($model){
        $query=" SELECT ";
        $query.="    REGDH.HR_NAME AS LABEL, ";
        $query.="    REGDH.GRADE || '-' || REGDH.HR_CLASS AS VALUE";
        $query.=" FROM ";
        $query.="    SCHREG_REGD_HDAT REGDH ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGDH.YEAR ";
                $query .= "       AND GDAT.GRADE = REGDH.GRADE ";
                $query .= " AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGDH.YEAR ";
            $query .= "       AND GDAT.GRADE = REGDH.GRADE ";
            $query .= "       AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     REGDH.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND REGDH.SEMESTER = '{$model->semester}' ";
        
        return $query;
    }
    
    function selectNameMst(){
        $query.=" select ";
        $query.="   namecd2,name1  ";
        $query.=" from ";
        $query.="   v_name_mst  ";
        $query.=" where ";
        $query.="   namecd1 = 'M026' ";
        $query.="   AND year = '".CTRL_YEAR."' ";
        
        return $query;
    }

    //DELETE
    function getDeleteQuery($model, $schregNo) {
        $query  = " DELETE FROM ";
        $query .= "     SPECIALACT_ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR                = '".$model->Year."' ";
        $query .= "     AND SEMESTER        = '".$model->semester."' ";
        $query .= "     AND SCHREGNO        = '".$schregNo."' ";
        $query .= "     AND ATTENDDATE      = '".str_replace("/","-",$model->Date)."' ";

        return $query;
    }

    //INSERT
    function getInsertQuery($model, $m026, $schregNo, $credittime){
        $data = array();
        $data["YEAR"][TEXT]             = $model->Year;
        $data["SEMESTER"][TEXT]         = $model->semester;
        $data["SCHREGNO"][TEXT]         = $schregNo;
        $data["CLASSCD"][TEXT]          = $m026["CLASSCD"];
        $data["SCHOOL_KIND"][TEXT]      = $m026["SCHOOL_KIND"];
        $data["CURRICULUM_CD"][TEXT]    = $m026["CURRICULUM_CD"];
        $data["SUBCLASSCD"][TEXT]       = $m026["SUBCLASSCD"];
        $data["ATTENDDATE"][DATE]       = $model->Date;
        $data["PERIODF"][TEXT]          = '1';
        $data["PERIODT"][TEXT]          = '1';
        $data["CREDIT_TIME"][TEXT]      = $credittime;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "SYSDATE()";
        $query = Query::insertSQL($data, "SPECIALACT_ATTEND_DAT");
        return $query;
    }
}
?>
