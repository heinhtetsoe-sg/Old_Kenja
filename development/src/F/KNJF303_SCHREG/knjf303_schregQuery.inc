<?php

require_once('for_php7.php');

class knjf303_schregQuery extends Query {

    //年組取得（権限チェック）
    function getAuth($year, $semester, $model)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = "SELECT T1.GRADE || T1.HR_CLASS AS VALUE,T1.HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT T1 ";
            if ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON T1.YEAR = REG_G.YEAR ";
                $query .= "           AND T1.GRADE = REG_G.GRADE ";
                $query .= "           AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= "WHERE T1.YEAR='" .$year ."'";
            $query .= "AND T1.SEMESTER='".$semester ."'";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = "SELECT T1.GRADE || T1.HR_CLASS AS VALUE,T1.HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT T1 ";
            if ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON T1.YEAR = REG_G.YEAR ";
                $query .= "           AND T1.GRADE = REG_G.GRADE ";
                $query .= "           AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= "WHERE T1.YEAR='" .$year ."' ";
            $query .= "AND T1.SEMESTER='".$semester ."' ";
            $query .= " AND (T1.TR_CD1 = '" .STAFFCD ."' ";
            $query .= "    OR T1.TR_CD2 = '" .STAFFCD ."' ";
            $query .= "    OR T1.TR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }

    //データが存在するか
    function getAddtion3SchregDatCnt($model, $schregno) {
        $query  = "SELECT  ";
        $query .= "     * ";
        $query .= "FROM ";
        $query .= "     MEDEXAM_DISEASE_ADDITION3_SCHREG_DAT ";
        $query .= "WHERE ";
        $query .= "         EDBOARD_SCHOOLCD = '".$model->getfield["EDBOARD_SCHOOLCD"]."' ";
        $query .= "     AND YEAR     = '".$model->getfield["YEAR"]."' ";
        $query .= "     AND SUSPEND_DIRECT_DATE = '".str_replace("/","-",$model->suspend_direct_date)."' ";
        $query .= "     AND TOTAL_DIV = '".$model->total_div."' ";
        $query .= "     AND DISEASECD = '".$model->diseasecd."' ";
        $query .= "     AND SCHREGNO  = '{$schregno}' ";
        $query .= "     AND ((GRADE || HR_CLASS) = '{$model->field["GRADE_HR_CLASS"]}' ) ";

        return $query;
    }

    //生徒一覧
    function getList($model) {
        $query  = " SELECT ";
        $query .= "     SCHREG_REGD_DAT.SCHREGNO AS SCHREGNO, ";
        $query .= "     ATTENDNO, ";
        $query .= "     SCHREG_REGD_DAT.SCHREGNO ";
        $query .= "     || ' ' ";
        $query .= "     || SCHREG_REGD_HDAT.HR_NAME ";
        $query .= "     || ATTENDNO ";
        $query .= "     || '番' ";
        $query .= "     || ' ' ";
        $query .= "     || NAME_SHOW AS NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " INNER JOIN SCHREG_REGD_DAT ";
        $query .= "     ON SCHREG_BASE_MST.SCHREGNO = SCHREG_REGD_DAT.SCHREGNO ";
        $query .= " LEFT JOIN SCHREG_REGD_HDAT ";
        $query .= "     ON SCHREG_REGD_HDAT.YEAR     = SCHREG_REGD_DAT.YEAR ";
        $query .= "    AND SCHREG_REGD_HDAT.SEMESTER = SCHREG_REGD_DAT.SEMESTER ";
        $query .= "    AND SCHREG_REGD_HDAT.GRADE    = SCHREG_REGD_DAT.GRADE ";
        $query .= "    AND SCHREG_REGD_HDAT.HR_CLASS = SCHREG_REGD_DAT.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "         (((SCHREG_REGD_DAT.YEAR)     = '{$model->control["年度"]}') ";
        $query .= "     AND  ((SCHREG_REGD_DAT.SEMESTER) = '{$model->control["学期"]}') ";
        $query .= "     AND  ((SCHREG_REGD_DAT.GRADE || SCHREG_REGD_DAT.HR_CLASS) = '{$model->field["GRADE_HR_CLASS"]}')) ";
        $query .= " ORDER BY ";
        $query .= "     ATTENDNO ";

        return $query;
    }

    //DELETE
    function getDalteDataQuery($model, $flg)
    {
        $query  = " DELETE FROM ";
        if ($flg === 'SCHREG') {
            $query .= "      MEDEXAM_DISEASE_ADDITION3_SCHREG_DAT ";
        } else {
            $query .= "      MEDEXAM_DISEASE_ADDITION3_COUNT_DAT ";
        }
        $query .= " WHERE ";
        $query .= "         EDBOARD_SCHOOLCD = '".$model->getfield["EDBOARD_SCHOOLCD"]."' ";
        $query .= "     AND YEAR     = '".$model->getfield["YEAR"]."' ";
        $query .= "     AND SUSPEND_DIRECT_DATE = '".str_replace("/","-",$model->suspend_direct_date)."' ";
        $query .= "     AND TOTAL_DIV = '".$model->total_div."' ";
        $query .= "     AND DISEASECD = '".$model->diseasecd."' ";
        $query .= "     AND ((GRADE || HR_CLASS) = '{$model->field["GRADE_HR_CLASS"]}' ) ";

        return $query;
    }

    //INSERT
    function &getUpdateQuery(&$model, $db)
    {
        //学籍番号-出席番号を配列に格納
        $array = explode (",", $model->selectdata);
        //学年とクラスを分ける
        $setgrade = "";
        $sethrclass = "";
        $setgrade   = substr($model->field["GRADE_HR_CLASS"], 0, 2);
        $sethrclass = substr($model->field["GRADE_HR_CLASS"], 2, 3);

        $db->autoCommit(false);

        //DELETE (MEDEXAM_DISEASE_ADDITION3_SCHREG_DAT)
        $query = knjf303_schregQuery::getDalteDataQuery($model, "SCHREG");
        $db->query($query);
        
        if ($array[0] != ""){
            for ($i = 0; $i < get_count($array); $i++) {
                //学籍番号と出席番号を配列に格納
                $schregno_array = explode ("-", $array[$i]);
                $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->getfield["EDBOARD_SCHOOLCD"];
                $data["YEAR"][TEXT]                 = $model->getfield["YEAR"];
                $data["SUSPEND_DIRECT_DATE"][TEXT]  = str_replace("/","-",$model->suspend_direct_date);
                $data["TOTAL_DIV"][TEXT]            = $model->total_div;
                $data["DISEASECD"][TEXT]            = $model->diseasecd;
                $data["SCHREGNO"][TEXT]             = $schregno_array[0];
                $data["GRADE"][TEXT]                = $setgrade;
                $data["HR_CLASS"][TEXT]             = $sethrclass;
                $data["ATTENDNO"][TEXT]             = $schregno_array[1];
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][NUMBER]            = "sysdate()";
                $query = Query::insertSQL($data, "MEDEXAM_DISEASE_ADDITION3_SCHREG_DAT");
                $db->query($query);
            }
        }
        
        //集計結果を反映する
        //DELETE (MEDEXAM_DISEASE_ADDITION3_COUNT_DAT)
        $query = knjf303_schregQuery::getDalteDataQuery($model, "COUNT");
        $db->query($query);
        if ($array[0] != ""){
            $data2["EDBOARD_SCHOOLCD"][TEXT]     = $model->getfield["EDBOARD_SCHOOLCD"];
            $data2["YEAR"][TEXT]                 = $model->getfield["YEAR"];
            $data2["SUSPEND_DIRECT_DATE"][TEXT]  = str_replace("/","-",$model->suspend_direct_date);
            $data2["TOTAL_DIV"][TEXT]            = $model->total_div;
            $data2["DISEASECD"][TEXT]            = $model->diseasecd;
            $data2["GRADE"][TEXT]                = $setgrade;
            $data2["HR_CLASS"][TEXT]             = $sethrclass;
            $data2["COUNT"][NUMBER]              = get_count($array);
            $data2["REGISTERCD"][TEXT]           = STAFFCD;
            $data2["UPDATED"][NUMBER]            = "sysdate()";
            $query = Query::insertSQL($data2, "MEDEXAM_DISEASE_ADDITION3_COUNT_DAT");
            $db->query($query);
        }

        $db->commit(); // トランザクションをコミットする。
        return;
    }

}
?>
