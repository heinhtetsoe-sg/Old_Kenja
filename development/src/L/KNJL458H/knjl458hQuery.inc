<?php

require_once('for_php7.php');

class knjl458hQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2 = "", $nameSpare3 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        if ($nameSpare3) {
            $query .= " AND NAMESPARE3 = '".$nameSpare3."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入学コース取得
    public function getExamCourseMst($model)
    {
        $query  = "   SELECT ";
        $query .= "       COURSECD || '-' || MAJORCD || '-' || EXAMCOURSECD AS VALUE, ";
        $query .= "       EXAMCOURSECD || ':' || EXAMCOURSE_NAME AS LABEL ";
        $query .= "   FROM ";
        $query .= "       ENTEXAM_COURSE_MST ";
        $query .= "   WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "   ORDER BY ";
        $query .= "       VALUE ";

        return $query;
    }

    //一覧表示
    public function selectQuery($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     B1.ENTEXAMYEAR, ";
        $query .= "     B1.APPLICANTDIV, ";
        $query .= "     B1.EXAMNO, ";
        $query .= "     B1.NAME, ";
        $query .= "     B1.NAME_KANA, ";
        $query .= "     Z002.ABBV1 AS SEX, ";
        $query .= "     C1.EXAMCOURSE_NAME, ";
        $query .= "     B1.FS_CD, ";
        $query .= "     FM1.FINSCHOOL_NAME, ";
        $query .= "     B1.ENTDIV ";          // 入辞区分
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "     INNER JOIN ENTEXAM_RECEPT_DAT R1 ";
        $query .= "          ON R1.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "         AND R1.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "         AND R1.EXAMNO       = B1.EXAMNO ";
        $query .= "     INNER JOIN ENTEXAM_SETTING_MST SM1 ";
        $query .= "          ON SM1.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "         AND SM1.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "         AND SM1.SETTING_CD   = 'L013' ";
        $query .= "         AND SM1.NAMESPARE1   = '1' ";
        $query .= "         AND SM1.SEQ          = B1.JUDGEMENT ";
        $query .= "     LEFT JOIN NAME_MST Z002 ";
        $query .= "          ON Z002.NAMECD2 = B1.SEX ";
        $query .= "         AND Z002.NAMECD1 = 'Z002' ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FM1 ";
        $query .= "          ON FM1.FINSCHOOLCD = B1.FS_CD ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST C1 ";
        $query .= "          ON C1.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "         AND C1.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "         AND C1.COURSECD     = B1.SUC_COURSECD ";
        $query .= "         AND C1.MAJORCD      = B1.SUC_MAJORCD ";
        $query .= "         AND C1.EXAMCOURSECD = B1.SUC_COURSECODE ";
        $query .= " WHERE ";
        $query .= "     B1.ENTEXAMYEAR      = '{$model->ObjYear}' ";
        $query .= "     AND B1.APPLICANTDIV = '{$model->applicantdiv}' ";
        if ($model->coursecd != "ALL") {
            $query .= "     AND B1.SUC_COURSECD || '-' || B1.SUC_MAJORCD || '-' || B1.SUC_COURSECODE = '{$model->coursecd}' ";
        }
        $query .= " ORDER BY ";
        if ($model->sort == "1") {
            $query .= "     B1.NAME_KANA, ";
            $query .= "     B1.EXAMNO ";
        } elseif ($model->sort == "2") {
            $query .= "     B1.FS_CD, ";
            $query .= "     B1.EXAMNO ";
        } else {
            $query .= "     B1.EXAMNO ";
        }

        return $query;
    }

    //更新
    public function getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->updField as $examno => $value) {
            if ($value["UPD_FLG"] !== "1") {
                continue;
            }

            //データセット
            $data = array();
            $data["ENTDIV"][TEXT]           = $value["ENTDIV"] == "" ? "1" : $value["ENTDIV"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            //更新
            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $where .= "   AND EXAMNO        = '{$examno}' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
