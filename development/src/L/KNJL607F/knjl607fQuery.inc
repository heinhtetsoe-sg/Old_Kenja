<?php

require_once('for_php7.php');

class knjl607fQuery extends Query {

    //名称マスタより取得
    function getNameMst($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT DISTINCT ";
        if (strlen($namecd2)) {
            $query .= "     NAME1 AS VALUE";
        } else {
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
            $query .= "     NAMECD2 AS VALUE,";
            $query .= "     NAMESPARE2 ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if (strlen($namecd2)) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    //CSV出力
    function selectDataQuery($model)
    {
        $query .= " WITH CONFRPT_DAT AS ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_APPLICANTCONFRPT_DAT ";
        $query .= "     WHERE ";
        $query .= "         ENTEXAMYEAR = '" . $model->ObjYear . "' ";
        $query .= "     AND APPLICANTDIV = '" . $model->field["APPLICANTDIV"] . "' ";
        $query .= " ), T_CONF AS ( ";
        $query .= "     SELECT ";
        $query .= "         EXAMNO, ";
        $query .= "         CONFIDENTIAL_RPT01 AS SCORE ";
        $query .= "     FROM ";
        $query .= "         CONFRPT_DAT ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         EXAMNO, ";
        $query .= "         CONFIDENTIAL_RPT02 AS SCORE ";
        $query .= "     FROM ";
        $query .= "         CONFRPT_DAT ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         EXAMNO, ";
        $query .= "         CONFIDENTIAL_RPT03 AS SCORE ";
        $query .= "     FROM ";
        $query .= "         CONFRPT_DAT ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         EXAMNO, ";
        $query .= "         CONFIDENTIAL_RPT04 AS SCORE ";
        $query .= "     FROM ";
        $query .= "         CONFRPT_DAT ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         EXAMNO, ";
        $query .= "         CONFIDENTIAL_RPT05 AS SCORE ";
        $query .= "     FROM ";
        $query .= "         CONFRPT_DAT ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         EXAMNO, ";
        $query .= "         CONFIDENTIAL_RPT06 AS SCORE ";
        $query .= "     FROM ";
        $query .= "         CONFRPT_DAT ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         EXAMNO, ";
        $query .= "         CONFIDENTIAL_RPT07 AS SCORE ";
        $query .= "     FROM ";
        $query .= "         CONFRPT_DAT ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         EXAMNO, ";
        $query .= "         CONFIDENTIAL_RPT08 AS SCORE ";
        $query .= "     FROM ";
        $query .= "         CONFRPT_DAT ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         EXAMNO, ";
        $query .= "         CONFIDENTIAL_RPT09 AS SCORE ";
        $query .= "     FROM ";
        $query .= "         CONFRPT_DAT ";
        $query .= " ), T_CONF_AVG AS ( ";
        $query .= "     SELECT ";
        $query .= "         EXAMNO, ";
        $query .= "         DECIMAL(ROUND(AVG(FLOAT(SCORE))*10,0)/10,4,1) AS AVERAGE_ALL ";
        $query .= "     FROM ";
        $query .= "         T_CONF ";
        $query .= "     GROUP BY ";
        $query .= "         EXAMNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     BASE.RECEPTDATE, ";
        if ($model->field["APPLICANTDIV"] == "1") {
            $query .= "     DETAIL012.REMARK" . $model->field["TESTDIV"] . " AS EXAMNO, ";
        } else {
            $query .= "     BASE.EXAMNO AS EXAMNO, ";
        }
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     NMZ002.NAME2 AS SEX_NAME, ";
        $query .= "     BASE.BIRTHDAY, ";
        $query .= "     BASE.FS_CD, ";
        $query .= "     FIN.FINSCHOOL_NAME, ";
        // $query .= "     FIN.FINSCHOOL_ZIPCD, ";
        $query .= "     ADDR.ZIPCD, ";
        $query .= "     ADDR.ADDRESS1, ";
        $query .= "     ADDR.ADDRESS2, ";
        $query .= "     NML016.NAME1 AS FS_GRD_DIV_NAME, ";
        $query .= "     CONFAVG.AVERAGE_ALL, ";
        $query .= "     CONFRPT.CONFIDENTIAL_RPT01, ";
        $query .= "     CONFRPT.CONFIDENTIAL_RPT02, ";
        $query .= "     CONFRPT.CONFIDENTIAL_RPT03, ";
        $query .= "     CONFRPT.CONFIDENTIAL_RPT04, ";
        $query .= "     CONFRPT.CONFIDENTIAL_RPT05, ";
        $query .= "     CONFRPT.CONFIDENTIAL_RPT06, ";
        $query .= "     CONFRPT.CONFIDENTIAL_RPT07, ";
        $query .= "     CONFRPT.CONFIDENTIAL_RPT08, ";
        $query .= "     CONFRPT.CONFIDENTIAL_RPT09, ";
        $query .= "     NML055.NAME1 AS EIKEN_NAME, ";
        if ($model->field["APPLICANTDIV"] == "1") {
            $query .= "     VALUE(DETAIL038.REMARK4,'') || '/' || VALUE(DETAIL038.REMARK5,'') || '/' || VALUE(DETAIL038.REMARK6,'') AS DETAIL1, ";
            $query .= "     VALUE(DETAIL039.REMARK4,'') || '/' || VALUE(DETAIL039.REMARK5,'') || '/' || VALUE(DETAIL039.REMARK6,'') AS DETAIL2, ";
        } else {
            $query .= "     VALUE(DETAIL020.REMARK1,'') || '/' || VALUE(DETAIL020.REMARK2,'') || '/' || VALUE(DETAIL020.REMARK3,'') || '/' || VALUE(DETAIL020.REMARK4,'') AS DETAIL1, ";
            $query .= "     NML032.NAME1 AS DETAIL2, ";
        }
        $query .= "     NML025.NAME1 AS SHOUGAKU_NAME, ";
        $query .= "     DETAIL005.REMARK4 AS DETAIL5_REMARK4 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ";
        $query .= "          ON ADDR.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "         AND ADDR.APPLICANTDIV= BASE.APPLICANTDIV ";
        $query .= "         AND ADDR.EXAMNO = BASE.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONFRPT ";
        $query .= "          ON CONFRPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "         AND CONFRPT.APPLICANTDIV= BASE.APPLICANTDIV ";
        $query .= "         AND CONFRPT.EXAMNO = BASE.EXAMNO ";
        $query .= "     LEFT JOIN T_CONF_AVG CONFAVG ON CONFAVG.EXAMNO = BASE.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL010 ";
        $query .= "          ON DETAIL010.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "         AND DETAIL010.APPLICANTDIV= BASE.APPLICANTDIV ";
        $query .= "         AND DETAIL010.EXAMNO      = BASE.EXAMNO ";
        $query .= "         AND DETAIL010.SEQ         = '010' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL012 ";
        $query .= "          ON DETAIL012.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "         AND DETAIL012.APPLICANTDIV= BASE.APPLICANTDIV ";
        $query .= "         AND DETAIL012.EXAMNO      = BASE.EXAMNO ";
        $query .= "         AND DETAIL012.SEQ         = '012' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL005 ";
        $query .= "          ON DETAIL005.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "         AND DETAIL005.APPLICANTDIV= BASE.APPLICANTDIV ";
        $query .= "         AND DETAIL005.EXAMNO      = BASE.EXAMNO ";
        $query .= "         AND DETAIL005.SEQ         = '005' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL038 ";
        $query .= "          ON DETAIL038.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "         AND DETAIL038.APPLICANTDIV= BASE.APPLICANTDIV ";
        $query .= "         AND DETAIL038.EXAMNO      = BASE.EXAMNO ";
        $query .= "         AND DETAIL038.SEQ         = '038' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL039 ";
        $query .= "          ON DETAIL039.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "         AND DETAIL039.APPLICANTDIV= BASE.APPLICANTDIV ";
        $query .= "         AND DETAIL039.EXAMNO      = BASE.EXAMNO ";
        $query .= "         AND DETAIL039.SEQ         = '039' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL004 ";
        $query .= "          ON DETAIL004.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "         AND DETAIL004.APPLICANTDIV= BASE.APPLICANTDIV ";
        $query .= "         AND DETAIL004.EXAMNO      = BASE.EXAMNO ";
        $query .= "         AND DETAIL004.SEQ         = '004' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL020 ";
        $query .= "          ON DETAIL020.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "         AND DETAIL020.APPLICANTDIV= BASE.APPLICANTDIV ";
        $query .= "         AND DETAIL020.EXAMNO      = BASE.EXAMNO ";
        $query .= "         AND DETAIL020.SEQ         = '020' ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FIN ON FIN.FINSCHOOLCD = BASE.FS_CD ";
        $query .= "     LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' AND NMZ002.NAMECD2 = BASE.SEX ";
        $query .= "     LEFT JOIN NAME_MST NML016 ON NML016.NAMECD1 = 'L016' AND NML016.NAMECD2 = BASE.FS_GRDDIV ";
        $query .= "     LEFT JOIN NAME_MST NML055 ON NML055.NAMECD1 = 'L055' AND NML055.NAMECD2 = DETAIL005.REMARK1 ";
        $query .= "     LEFT JOIN NAME_MST NML025 ON NML025.NAMECD1 = 'L025' AND NML025.NAMECD2 = DETAIL005.REMARK2 ";
        $query .= "     LEFT JOIN NAME_MST NML032 ON NML032.NAMECD1 = 'L032' AND NML032.NAMECD2 = DETAIL004.REMARK9 ";
        $query .= " WHERE ";
        $query .= "     BASE.ENTEXAMYEAR = '" . $model->ObjYear . "' ";
        $query .= "     AND BASE.APPLICANTDIV = '" . $model->field["APPLICANTDIV"] . "' ";
        if ($model->field["APPLICANTDIV"] == "1") {
            $query .= "     AND DETAIL010.REMARK" . $model->field["TESTDIV"] . " = '" . $model->field["TESTDIV"] . "' ";
        } else {
            $query .= "     AND BASE.TESTDIV = '" . $model->field["TESTDIV"] . "' ";
        }
        if ($model->field["EXAMNO_FROM"] != "") {
            if ($model->field["APPLICANTDIV"] == "1") {
                $query .= "     AND DETAIL012.REMARK" . $model->field["TESTDIV"] . " >= '" . $model->field["EXAMNO_FROM"] . "' ";
            } else {
                $query .= "     AND BASE.EXAMNO >= '" . $model->field["EXAMNO_FROM"] . "' ";
            }
        }
        if ($model->field["EXAMNO_TO"] != "") {
            if ($model->field["APPLICANTDIV"] == "1") {
                $query .= "     AND DETAIL012.REMARK" . $model->field["TESTDIV"] . " <= '" . $model->field["EXAMNO_TO"] . "' ";
            } else {
                $query .= "     AND BASE.EXAMNO <= '" . $model->field["EXAMNO_TO"] . "' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     EXAMNO ";
        return $query;
    }
}
?>
