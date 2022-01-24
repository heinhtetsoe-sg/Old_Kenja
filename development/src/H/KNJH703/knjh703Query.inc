<?php

require_once('for_php7.php');

class knjh703Query extends Query
{
    //年組取得
    public function getHrClass($model)
    {
        $query  = "   SELECT GRADE || '-' || HR_CLASS AS VALUE ";
        $query .= "        , HR_NAME AS LABEL ";
        $query .= "     FROM SCHREG_REGD_HDAT ";
        $query .= "    WHERE YEAR     = '".CTRL_YEAR."' ";
        $query .= "      AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "      AND GRADE    = '03' ";
        $query .= " ORDER BY VALUE ";

        return $query;
    }

    //生徒情報取得
    public function getStudentInfo($model)
    {
        list($grade, $hr_class) = explode('-', $model->grade_hr_class);

        $query  = " WITH SRD AS ( ";
        $query .= "       SELECT YEAR ";
        $query .= "            , SCHREGNO ";
        $query .= "            , GRADE ";
        $query .= "            , HR_CLASS ";
        $query .= "            , ATTENDNO ";
        $query .= "        FROM SCHREG_REGD_DAT SRD ";
        $query .= "       WHERE YEAR     = '".CTRL_YEAR."' ";
        $query .= "         AND GRADE    = '".$grade."' ";
        $query .= "         AND HR_CLASS = '".$hr_class."' ";
        $query .= "    GROUP BY YEAR ";
        $query .= "           , SCHREGNO ";
        $query .= "           , GRADE ";
        $query .= "           , HR_CLASS ";
        $query .= "           , ATTENDNO ";
        $query .= " ) , SELECT_SUBCLASS AS ( ";
        //選択科目を登録しているデータ
        $query .= "       SELECT SAD.SCHREGNO ";
        $query .= "            , SAD.BUNRIDIV ";
        $query .= "            , SAD.CLASSCD ";
        $query .= "            , SAD.SUBCLASSCD ";
        $query .= "            , SAD.DECLINE_FLG ";
        $query .= "         FROM ACADEMICTEST_SUBCLASS_DAT ASD ";
        $query .= "   INNER JOIN SCHREG_ACADEMICTEST_DAT SAD ";
        $query .= "           ON SAD.YEAR       = ASD.YEAR ";
        $query .= "          AND ASD.BUNRIDIV   = SAD.BUNRIDIV ";
        $query .= "          AND ASD.CLASSCD    = SAD.CLASSCD ";
        $query .= "          AND ASD.SUBCLASSCD = SAD.SUBCLASSCD ";
        $query .= "        WHERE ASD.YEAR       = '".CTRL_YEAR."' ";
        $query .= "          AND ASD.ELECTDIV   = '2' ";  //2:選択科目
        $query .= " ) ";
        $query .= "      SELECT SRD.SCHREGNO ";
        $query .= "           , SRD.GRADE ";
        $query .= "           , SRD.HR_CLASS ";
        $query .= "           , SRD.ATTENDNO ";
        $query .= "           , SBM.NAME ";
        $query .= "           , NMT.ABBV1 AS SEX ";
        $query .= "           , SAD.BUNRIDIV ";
        $query .= "           , SAD.CLASSCD || '-' || SAD.SUBCLASSCD AS SUBCLASSCD ";
        $query .= "           , SAD.DECLINE_FLG ";
        $query .= "        FROM SRD ";
        $query .= "   LEFT JOIN SCHREG_BASE_MST SBM ";
        $query .= "          ON SBM.SCHREGNO = SRD.SCHREGNO ";
        $query .= "   LEFT JOIN NAME_MST NMT ";
        $query .= "          ON NMT.NAMECD1  = 'Z002' ";
        $query .= "         AND NMT.NAMECD2  = SBM.SEX ";
        $query .= "   LEFT JOIN SELECT_SUBCLASS SAD ";
        $query .= "          ON SAD.SCHREGNO = SRD.SCHREGNO ";
        $query .= "       WHERE SBM.GRD_DIV  IS NULL ";
        $query .= "    ORDER BY SRD.ATTENDNO ";
        $query .= "           , SAD.BUNRIDIV ";
        $query .= "           , SAD.SUBCLASSCD ";

        return $query;
    }

    //名称マスタ取得
    public function getNameMst($namecd1)
    {
        $query  = "    SELECT MST.ABBV1   AS LABEL ";
        $query .= "         , YDT.NAMECD2 AS VALUE ";
        $query .= "      FROM NAME_YDAT YDT ";
        $query .= " LEFT JOIN NAME_MST  MST ";
        $query .= "        ON MST.NAMECD1 = YDT.NAMECD1 ";
        $query .= "       AND MST.NAMECD2 = YDT.NAMECD2 ";
        $query .= "     WHERE YDT.YEAR    = '".CTRL_YEAR."' ";
        $query .= "       AND YDT.NAMECD1 = '".$namecd1."' ";
        $query .= "  ORDER BY VALUE ";

        return $query;
    }

    //教科マスタ取得
    public function getSubclass($bunridiv)
    {
        $query  = "   SELECT SUBCLASSNAME AS LABEL ";
        $query .= "        , CLASSCD || '-' || SUBCLASSCD AS VALUE ";
        $query .= "     FROM ACADEMICTEST_SUBCLASS_DAT ";
        $query .= "    WHERE YEAR     = '".CTRL_YEAR."' ";
        $query .= "      AND BUNRIDIV = '".$bunridiv."' ";
        $query .= "      AND ELECTDIV = '2' ";  //2:選択科目
        $query .= " ORDER BY CLASSCD ";
        $query .= "        , SUBCLASSCD ";

        return $query;
    }

    //年組取得(一括更新画面)
    public function getHrClassName($model)
    {
        $query  = "   SELECT HR_NAME ";
        $query .= "     FROM SCHREG_REGD_HDAT ";
        $query .= "    WHERE YEAR     = '".CTRL_YEAR."' ";
        $query .= "      AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "      AND GRADE    = '03' ";
        $query .= "      AND GRADE || '-' || HR_CLASS = '".$model->grade_hr_class."' ";

        return $query;
    }

    //データ有無チェック
    public function checkExistData($schregno)
    {
        $query  = " SELECT COUNT(*) ";
        $query .= "   FROM SCHREG_ACADEMICTEST_DAT ";
        $query .= "  WHERE YEAR     = '".CTRL_YEAR."' ";
        $query .= "    AND SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //必須科目取得
    public function getMustClass($bunridiv)
    {
        $query  = " SELECT CLASSCD ";
        $query .= "      , SUBCLASSCD ";
        $query .= "   FROM ACADEMICTEST_SUBCLASS_DAT ";
        $query .= "  WHERE YEAR     = '".CTRL_YEAR."' ";
        $query .= "    AND BUNRIDIV = '".$bunridiv."' ";
        $query .= "    AND ELECTDIV = '1' ";  //1:必須科目

        return $query;
    }

    //削除
    public function deleteSchregAcademictestDat($schregno)
    {
        $query  = " DELETE FROM SCHREG_ACADEMICTEST_DAT ";
        $query .= "       WHERE YEAR     = '".CTRL_YEAR."' ";
        $query .= "         AND SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //登録
    public function updateSchregAcademictestDat($model, $row, $classcd, $subclasscd)
    {
        $data = array();
        $data["YEAR"][TEXT]        = CTRL_YEAR;
        $data["SCHREGNO"][TEXT]    = $model->fields["SCHREGNO"][$row];
        $data["BUNRIDIV"][TEXT]    = $model->fields["BUNRIDIV"][$row];
        $data["CLASSCD"][TEXT]     = $classcd;
        $data["SUBCLASSCD"][TEXT]  = $subclasscd;
        $data["DECLINE_FLG"][TEXT] = $model->fields["DECLINE_FLG"][$row] == "1" ? "1" : "0";
        $data["REGISTERCD"][TEXT]  = STAFFCD;
        $data["UPDATED"][NUMBER]   = "sysdate()";

        $query = Query::insertSQL($data, "SCHREG_ACADEMICTEST_DAT");

        return $query;
    }

    //一括更新
    public function replaceSchregAcademictestDat($model, $schregno, $classcd, $subclasscd)
    {
        $data = array();
        $data["YEAR"][TEXT]        = CTRL_YEAR;
        $data["SCHREGNO"][TEXT]    = $schregno;
        $data["BUNRIDIV"][TEXT]    = $model->replace_data["BUNRIDIV"];
        $data["CLASSCD"][TEXT]     = $classcd;
        $data["SUBCLASSCD"][TEXT]  = $subclasscd;
        $data["DECLINE_FLG"][TEXT] = $model->replace_data["DECLINE_FLG"] == "1" ? "1" : "0";
        $data["REGISTERCD"][TEXT]  = STAFFCD;
        $data["UPDATED"][NUMBER]   = "sysdate()";

        $query = Query::insertSQL($data, "SCHREG_ACADEMICTEST_DAT");

        return $query;
    }
}
