<?php

require_once('for_php7.php');


class knjc165aquery extends Query {

    //年度取得
    function getYear($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR AS VALUE, ";
        // $query .= "     YEAR || '年度' AS LABEL ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " WHERE ";
                $query .= "         SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                $query .= "     AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " WHERE ";
            $query .= "         SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //年組一覧取得
    function getGradeHrclass($year, $semester)
    {
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     SEMESTER = '".$semester."' ";
        if(AUTHORITY == DEF_UPDATE_RESTRICT || $AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= "     AND (TR_CD1     = '".STAFFCD."' OR ";
            $query .= "          TR_CD2     = '".STAFFCD."' OR ";
            $query .= "          TR_CD3     = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD1  = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    //備考取得
    function getRemark($model, $field)
    {
        $query  = " SELECT ";
        if($field == "COUNT"){
            $query .= "  COUNT(*) ";
        } else {
            $query .= "  ".$field." ";
        }
        $query .= " FROM ";
        $query .= "     ATTEND_SEMES_REMARK_HR_SENDPREF_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR         = '".$model->field["YEAR"]."' ";
        $query .= "     AND MONTH    = '".$model->field["MONTH"]."' ";
        $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND GRADE    = '".substr($model->field["GRADE_HR_CLASS"], 0, 2)."' ";
        $query .= "     AND HR_CLASS = '".substr($model->field["GRADE_HR_CLASS"], 2, 3)."' ";
        

        return $query;
    }

    /* データ更新処理 */
    function update($model)
    {
        $db = Query::dbCheckOut();

        //項目
        $data = array();
        $data["YEAR"][TEXT]             = $model->field["YEAR"];
        $data["MONTH"][TEXT]            = $model->field["MONTH"];
        $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
        $data["GRADE"][TEXT]            = substr($model->field["GRADE_HR_CLASS"], 0, 2);
        $data["HR_CLASS"][TEXT]         = substr($model->field["GRADE_HR_CLASS"], 2, 3);
        $data["REMARK1"][TEXT]          = $model->field["REMARK"];
        $data["REGISTERCD"][TEXT]       = STAFFCD ;
        $data["UPDATED"][FUNC]          = "sysdate()";

        //存在チェック
        $chkCount = $db->getOne(knjc165aQuery::getRemark($model, "COUNT"));

        if (0 < $chkCount) {
            //更新
            $where  = " WHERE ";
            $where .= "     YEAR         = '".$model->field["YEAR"]."' ";
            $where .= "     AND MONTH    = '".$model->field["MONTH"]."' ";
            $where .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
            $where .= "     AND GRADE    = '".substr($model->field["GRADE_HR_CLASS"], 0, 2)."' ";
            $where .= "     AND HR_CLASS = '".substr($model->field["GRADE_HR_CLASS"], 2, 3)."' ";
            $query = Query::updateSQL($data, "ATTEND_SEMES_REMARK_HR_SENDPREF_DAT", $where);
        } else {
            //追加
            $query = Query::insertSQL($data, "ATTEND_SEMES_REMARK_HR_SENDPREF_DAT");
        }
        $db->query($query);
        Query::dbCheckIn($db);
    }
}
?>
