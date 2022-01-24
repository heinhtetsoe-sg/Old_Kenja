<?php
class knjl355yQuery extends Query
{

    //入試制度取得
    public function getApctDiv($namecd, $year)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd."' AND ";
        $query .= "     NAMECD2 IN( SELECT DISTINCT ";
        $query .= "                     APPLICANTDIV ";
        $query .= "                 FROM ";
        $query .= "                     ENTEXAM_RECEPT_DAT ";
        $query .= "                 WHERE ";
        $query .= "                     ENTEXAMYEAR = '".$year."') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学校名
    public function getSchoolName($model)
    {
        $certif_kindcd = ($model->field["APPLICANTDIV"] == "1") ? "105" : "106";

        $query  = "";
        $query .= " SELECT ";
        $query .= "     SCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     CERTIF_SCHOOL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->ObjYear}' AND ";
        $query .= "     CERTIF_KINDCD = '{$certif_kindcd}' ";

        return $query;
    }

    //CSV出力
    public function getCsvQuery($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.SEX, ";
        $query .= "     N0.ABBV1 AS SEX_NAME, ";
        $query .= "     T1.FS_CD, ";
        $query .= "     F1.FINSCHOOL_NAME, ";
        $query .= "     T3.GNAME, ";
        $query .= "     T3.ZIPCD, ";
        $query .= "     VALUE(T3.ADDRESS1,'') || VALUE(T3.ADDRESS2,'') AS ADDRESS, ";
        $query .= "     T3.TELNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT T3 ON T3.ENTEXAMYEAR  = T1.ENTEXAMYEAR AND T3.EXAMNO = T1.EXAMNO ";
        $query .= "     LEFT JOIN NAME_MST N0 ON N0.NAMECD1 = 'Z002' AND N0.NAMECD2 = T1.SEX ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'L013' AND N1.NAMECD2 = T1.JUDGEMENT ";
        $query .= "     LEFT JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD = T1.FS_CD ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND N1.NAMESPARE1   = '1' ";
        //1:合格者 2:入学者
        if ($model->field["OUTPUT"] == "2") {
            $query .= "     AND T1.PROCEDUREDIV = '1' ";
            $query .= "     AND T1.ENTDIV       = '1' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.EXAMNO ";

        return $query;
    }
}
