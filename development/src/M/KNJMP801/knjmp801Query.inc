<?php

require_once('for_php7.php');

class knjmp801Query extends Query {

    //年組取得（権限チェック）
    function getAuth($year, $semester)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query .= "  SELECT DISTINCT ";
            $query .= "      GRADE || HR_CLASS AS VALUE, ";
            $query .= "      HR_NAME AS LABEL  ";
            $query .= "  FROM   ";
            $query .= "      SCHREG_REGD_HDAT ";
            $query .= "  WHERE  ";
            $query .= "      YEAR = '" .$year ."' ";
            $query .= "  AND SEMESTER   = '".$semester."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= "  SELECT DISTINCT ";
            $query .= "      GRADE || HR_CLASS AS VALUE, ";
            $query .= "      HR_NAME AS LABEL  ";
            $query .= "  FROM   ";
            $query .= "      SCHREG_REGD_HDAT ";
            $query .= "  WHERE  ";
            $query .= "      YEAR = '" .$year ."' ";
            $query .= "  AND SEMESTER   = '".$semester."' ";
            $query .= "  AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= "  OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= "  OR TR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }

    //会計グループCD取得
    function getGrpcd($year, $grade_hr_class)
    {
        $query .= "  SELECT ";
        $query .= "      T1.COLLECT_GRP_CD || ':' || COLLECT_GRP_NAME AS LABEL, ";
        $query .= "      T1.COLLECT_GRP_CD AS VALUE ";
        $query .= "  FROM ";
        $query .= "      COLLECT_GRP_HR_DAT T1 ";
        $query .= "  LEFT JOIN COLLECT_GRP_MST L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                              AND L1.COLLECT_GRP_CD = T1.COLLECT_GRP_CD ";
        $query .= "  WHERE ";
        $query .= "      T1.YEAR = '" .$year ."' ";
        $query .= "  AND T1.GRADE || T1.HR_CLASS = '" .$grade_hr_class ."' ";

        return $query;
    }

    //生徒取得
    function getSchno($model, $year, $semester)
    {
        if ($model->field["GRADE_HR_CLASS"] != '00000') {
            $query .= " SELECT DISTINCT";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.ATTENDNO, ";
            $query .= "     T1.GRADE, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T2.NAME_SHOW, ";
            $query .= "     L3.OUTPUT_NO, ";
            $query .= "     T3.HR_NAME ";
            $query .= " FROM ";
            $query .= "     SCHREG_BASE_MST T2, ";
            $query .= "     SCHREG_REGD_DAT T1  ";
            $query .= "     LEFT JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR=T1.YEAR AND T3.SEMESTER=T1.SEMESTER AND ";
            $query .= "                                      T3.GRADE=T1.GRADE AND T3.HR_CLASS=T1.HR_CLASS ";
            $query .= "     INNER JOIN COLLECT_MONEY_DUE_M_DAT L2 ON L2.SCHREGNO = T1.SCHREGNO ";
            $query .= "                                          AND L2.YEAR = '".$model->field["YEAR"]."' ";
            $query .= "     LEFT JOIN COLLECT_MONEY_DUE_PRINT_DAT L3 ON L3.SCHREGNO = T1.SCHREGNO ";
            $query .= "                                             AND L3.YEAR = '".$model->field["YEAR"]."' ";
            $query .= "                                             AND L3.COLLECT_GRP_CD = L2.COLLECT_GRP_CD ";
            $query .= "                                             AND L3.OUTPUT_NO = '".sprintf("%03d", $model->field["OUTPUT_NO"])."' ";
            $query .= "WHERE  T1.YEAR               = '".$year."' AND ";
            $query .= "       T1.SEMESTER           = '".$semester."' AND ";
            $query .= "       T1.GRADE||T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
            $query .= "       L2.COLLECT_GRP_CD     = '".$model->field["COLLECT_GRP_CD"]."' AND ";
            $query .= "       T2.SCHREGNO           = T1.SCHREGNO ";
            $query .= " ORDER BY ";
            $query .= "     T1.GRADE, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.ATTENDNO ";
        } else {
            $query .= "  SELECT DISTINCT";
            $query .= "      T1.SCHREGNO, ";
            $query .= "      T1.NAME AS NAME_SHOW, ";
            $query .= "      '' AS GRADE, ";
            $query .= "      T1.HR_CLASS, ";
            $query .= "      L3.OUTPUT_NO, ";
            $query .= "      T1.ATTENDNO ";
            $query .= "  FROM ";
            $query .= "      FRESHMAN_DAT T1";
            $query .= "  INNER JOIN COLLECT_MONEY_DUE_M_DAT L2 ON L2.SCHREGNO = T1.SCHREGNO ";
            $query .= "                                          AND L2.YEAR = '".$model->field["YEAR"]."' ";
            $query .= "  LEFT JOIN COLLECT_MONEY_DUE_PRINT_DAT L3 ON L3.SCHREGNO = T1.SCHREGNO ";
            $query .= "                                          AND L3.YEAR = '".$model->field["YEAR"]."' ";
            $query .= "                                          AND L3.COLLECT_GRP_CD = L2.COLLECT_GRP_CD ";
            $query .= "                                          AND L3.OUTPUT_NO = '".sprintf("%03d", $model->field["OUTPUT_NO"])."' ";
            $query .= "  WHERE ";
            $query .= "      T1.ENTERYEAR = '".($year + 1)."' ";
            $query .= "  AND L2.COLLECT_GRP_CD     = '".$model->field["COLLECT_GRP_CD"]."' ";
            $query .= " ORDER BY ";
            $query .= "     GRADE, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.ATTENDNO ";
        }
        return $query;
    }

    //COLLECT_MONEY_DUE_PRINT_DATのCOUTPUT_CNT取得
    function getPrintDat($model, $schregno)
    {
        $query  = "  SELECT ";
        $query .= "      COUTPUT_CNT ";
        $query .= "  FROM ";
        $query .= "      COLLECT_MONEY_DUE_PRINT_DAT ";
        $query .= "  WHERE ";
        $query .= "      YEAR = '".$model->field["YEAR"]."' ";
        $query .= "  AND SCHREGNO = '".$schregno."' ";
        $query .= "  AND COLLECT_GRP_CD = '".$model->field["COLLECT_GRP_CD"]."' ";
        $query .= "  AND OUTPUT_NO = '".sprintf("%03d", $model->field["OUTPUT_NO"])."' ";

        return $query;
    }
        
    //特別控除額合計のUPDATE
    function insertQuery(&$db, &$model)
    {
        //リストより選択した学生データ
        $schregdata_array = explode(',', $model->selectleft);
        foreach ($schregdata_array as $schregdata) {
            $schregDataArray = preg_split("/-/", $schregdata);
            $schregno = "";
            $schregno = $schregDataArray[1];
            $output_cnt = $db->getOne(knjmp801Query::getPrintDat($model, $schregno));

            if ($output_cnt != "") {
                $where  = " WHERE YEAR = '".$model->field["YEAR"]."' ";
                $where .= "   AND SCHREGNO = '".$schregno."' ";
                $where .= "   AND COLLECT_GRP_CD = '".$model->field["COLLECT_GRP_CD"]."' ";
                $where .= "   AND OUTPUT_NO = '".sprintf("%03d", $model->field["OUTPUT_NO"])."' ";
            } else {
                $data["YEAR"][TEXT]             = $model->field["YEAR"];
                $data["SCHREGNO"][TEXT]         = $schregno;
                $data["COLLECT_GRP_CD"][TEXT]   = $model->field["COLLECT_GRP_CD"];
                $data["OUTPUT_NO"][TEXT]        = sprintf("%03d", $model->field["OUTPUT_NO"]);
            }
            $data["COUTPUT_CNT"][NUMBER]        = $output_cnt + 1;
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "sysdate()";

            if ($output_cnt != "") {
                $query = Query::updateSQL($data, "COLLECT_MONEY_DUE_PRINT_DAT", $where);
            } else {
                $query = Query::insertSQL($data, "COLLECT_MONEY_DUE_PRINT_DAT");
            }
            $db->query($query);
        }
        return ;
    }



}
?>
