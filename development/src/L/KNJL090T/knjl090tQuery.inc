<?php

require_once('for_php7.php');

class knjl090tQuery extends Query 
{
    //名称マスタよりデータを取得
    function GetName($ObjYear, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     namecd1, ";
        $query .= "     namecd2, ";
        $query .= "     name1 ";
        $query .= " FROM ";
        $query .= "     v_name_mst ";
        $query .= " WHERE ";
        $query .= "     year    = '".$ObjYear ."' AND ";
        $query .= "     namecd1 IN ('".implode($namecd1, "','") ."') ";
        $query .= " ORDER BY ";
        $query .= "     namecd1, ";
        $query .= "     namecd2 ";
        return $query;
    }

    //志望区分を取得
    function getWishno($year, $examno)
    {
        $query  = "SELECT ";
        $query .= "    W1.WISHNO, ";
        $query .= "    W1.MAJORLCD || W1.MAJORSCD AS VALUE, ";
        $query .= "    W1.MAJORLCD || W1.MAJORSCD || '：' || L1.MAJORLNAME || '　' || L1.MAJORSNAME AS LABEL ";
        $query .= "FROM ";
        $query .= "    ENTEXAM_APPLICANTWISH_DAT W1 ";
        $query .= "    LEFT JOIN V_ENTEXAM_MAJOR_MST L1 ";
        $query .= "         ON L1.MAJORCD = W1.MAJORLCD || W1.MAJORSCD ";
        $query .= "WHERE ";
        $query .= "    W1.ENTEXAMYEAR = '{$year}' AND ";
        $query .= "    W1.EXAMNO = '{$examno}' ";
        $query .= "ORDER BY ";
        $query .= "    W1.WISHNO ";
        return $query;
    }

    //メインデータ取得
    function get_edit_data($model)
    {
        $query  = "";
        $query .= "  SELECT";
        $query .= "      T1.entexamyear,";
        $query .= "      T1.examno,";
        $query .= "      T1.name,";
        $query .= "      T1.name_kana,";
        $query .= "      T1.birth_y,";
        $query .= "      T1.birth_m,";
        $query .= "      T1.birth_d,";
        $query .= "      T1.birthday,";
        $query .= "      T1.sex,";
        $query .= "      T1.applicantdiv,";
        $query .= "      T1.testdiv2,";
        $query .= "      T1.suc_coursecd,";
        $query .= "      T1.suc_majorcd,";
        $query .= "      T1.suc_coursecode,";
        $query .= "      T1.judgement,";
        $query .= "      MEISYOU_GET(T1.judgement,'L013',1) AS judgement_name,";
        $query .= "      T1.procedurediv,";
        $query .= "      T1.entdiv,";
        $query .= "      T1.remark1,";
        $query .= "      T1.remark2,";
        $query .= "      T1.success_noticeno,";
        $query .= "      T1.failure_noticeno,";
        $query .= "      T1.interview_attend_flg,";
        $query .= "      T2.name1 AS sexname,";
        $query .= "      T4.confidential_rpt01,";
        $query .= "      T4.confidential_rpt02,";
        $query .= "      T4.confidential_rpt03,";
        $query .= "      T4.confidential_rpt04,";
        $query .= "      T4.confidential_rpt05,";
        $query .= "      T4.confidential_rpt06,";
        $query .= "      T4.confidential_rpt07,";
        $query .= "      T4.confidential_rpt08,";
        $query .= "      T4.confidential_rpt09,";
        $query .= "      T4.confidential_rpt10,";
        $query .= "      T4.TOTAL3,";
        $query .= "      T4.TOTAL5,";
        $query .= "      T4.TOTAL_ALL as TOTAL9,";
        $query .= "      T4.average_all,";
        $query .= "      T4.ABSENCE_DAYS as ABSENCE_DAYS1,";
        $query .= "      T4.ABSENCE_DAYS2,";
        $query .= "      T4.ABSENCE_DAYS3,";
        $query .= "      T5.name1 AS era_name";
        $query .= "  FROM";
        $query .= "      entexam_applicantbase_dat T1 ";
        $query .= "      LEFT OUTER JOIN v_name_mst T2 ";
        $query .= "                   ON T2.year = T1.entexamyear AND T2.namecd2 = T1.sex   AND T2.namecd1 = 'Z002' ";
        $query .= "      LEFT OUTER JOIN entexam_applicantconfrpt_dat T4 ";
        $query .= "                   ON T4.entexamyear = T1.entexamyear AND T4.examno = T1.examno ";
        $query .= "      LEFT OUTER JOIN v_name_mst T5 ";
        $query .= "                   ON T5.year = T1.entexamyear AND T5.namecd2 = T1.eracd AND T5.namecd1 = 'L007'";
        $query .= "  WHERE";
        $query .= "      T1.entexamyear = '".$model->year."' AND ";

        if ($model->cmd == "back1" || $model->cmd == "back2") {
            $query .= "      T1.examno < '".$model->examno."'";
            $query .= " ORDER BY T1.examno DESC ";
            $query .= " FETCH FIRST 1 ROWS ONLY "; 
        } elseif ($model->cmd == "next1" || $model->cmd == "next2") {
            $query .= "      T1.examno > '".$model->examno."'";
            $query .= " ORDER BY T1.examno ASC ";
            $query .= " FETCH FIRST 1 ROWS ONLY "; 
        } else {
            $query .= "      T1.examno = '".$model->examno."'";
        }

        return $query;
    }

    //存在チェック
    function Existdata($model)
    {
        $db = Query::dbCheckOut();

        $query = "  SELECT
                        EXAMNO
                    FROM
                        ENTEXAM_APPLICANTBASE_DAT
                    WHERE
                        ENTEXAMYEAR = '".$model->year."' AND
                        EXAMNO      = '".$model->examno."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //--- UPDATE
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        // 志願者基礎データの更新
        $data = array();
        $data["SUC_COURSECD"][TEXT]         = $model->field["SUC_COURSECD"];
        $data["SUC_MAJORCD"][TEXT]          = $model->field["SUC_MAJORCD"];
        $data["SUC_COURSECODE"][TEXT]       = $model->field["SUC_COURSECODE"];
        $data["JUDGEMENT"][TEXT]            = $model->judgement;
        $data["PROCEDUREDIV"][TEXT]         = $model->field["PROCEDUREDIV"];
        $data["ENTDIV"][TEXT]               = $model->field["ENTDIV"];
        $data["SUCCESS_NOTICENO"][TEXT]     = (strlen($model->field["SUCCESS_NOTICENO"])? sprintf("%04d",$model->field["SUCCESS_NOTICENO"]) : "");
        $data["FAILURE_NOTICENO"][TEXT]     = (strlen($model->field["FAILURE_NOTICENO"])? sprintf("%04d",$model->field["FAILURE_NOTICENO"]) : "");
        $data["REMARK1"][TEXT]              = $model->field["REMARK1"];
        $data["REMARK2"][TEXT]              = $model->field["REMARK2"];
        $data["registercd"][TEXT]           = STAFFCD;
        $data["updated"][FUNC]              = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' AND";
        $where .= "       EXAMNO        = '".$model->examno."'";
        $query = Query::updateSQL($data, "entexam_applicantbase_dat", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>