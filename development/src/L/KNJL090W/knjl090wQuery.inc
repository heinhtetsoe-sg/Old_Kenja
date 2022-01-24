<?php

require_once('for_php7.php');
class knjl090wQuery extends Query 
{
    //権限チェック（入試管理者）
    function getAdminFlg() {
        $query  = " SELECT ";
        $query .= "     FIELD1 ";
        $query .= " FROM ";
        $query .= "     STAFF_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND STAFFCD = '".STAFFCD."' ";
        $query .= "     AND STAFF_SEQ = '009' ";

        $db = Query::dbCheckOut();
        $rtnFlg = $db->getOne($query);
        Query::dbCheckIn($db);

        return $rtnFlg;
    }
    
    //名称マスタよりデータを取得
    function getName($ObjYear, $namecd1, $sitei = "")
    {
        $query  = " SELECT ";
        $query .= "     namecd1, ";
        $query .= "     namecd2, ";
        $query .= "     name1, ";
        $query .= "     namespare1 ";
        $query .= " FROM ";
        $query .= "     v_name_mst ";
        $query .= " WHERE ";
        $query .= "     year    = '".$ObjYear ."' AND ";
        $query .= "     namecd1 IN ('".implode($namecd1, "','") ."') ";
        if ($sitei) {
            $query .= "     AND NAMECD2 = '".$sitei."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     namecd1, ";
        $query .= "     namecd2 ";
        return $query;
    }

    //入試制度を取得
    function get_name_cd($year, $namecd1, $sitei = "") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     ABBV1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($sitei) {
            $query .= "     AND NAMECD2 = '".$sitei."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //志望区分を取得
    function getSucCourse($year, $applicantdiv, $examno) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     '1' AS NUNBER, ";
        $query .= "     T1.LAST_DAI1_COURSECD || T1.LAST_DAI1_MAJORCD || T1.LAST_DAI1_COURSECODE AS VALUE, ";
        $query .= "     T1.LAST_DAI1_COURSECD || T1.LAST_DAI1_MAJORCD || T1.LAST_DAI1_COURSECODE || ':' || L1.COURSENAME || L1.MAJORNAME || C1.EXAMCOURSE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST L1 ";
        $query .= "              ON L1.YEAR         = T1.ENTEXAMYEAR ";
        $query .= "             AND L1.COURSECD     = T1.LAST_DAI1_COURSECD ";
        $query .= "             AND L1.MAJORCD      = T1.LAST_DAI1_MAJORCD ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST C1 ";
        $query .= "              ON C1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND C1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "             AND C1.TESTDIV      = T1.TESTDIV ";
        $query .= "             AND C1.COURSECD     = T1.LAST_DAI1_COURSECD ";
        $query .= "             AND C1.MAJORCD      = T1.LAST_DAI1_MAJORCD ";
        $query .= "             AND C1.EXAMCOURSECD = T1.LAST_DAI1_COURSECODE ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$applicantdiv}' ";
        $query .= "     AND T1.EXAMNO       = '{$examno}' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '2' AS NUNBER, ";
        $query .= "     T1.LAST_DAI2_COURSECD || T1.LAST_DAI2_MAJORCD || T1.LAST_DAI2_COURSECODE AS VALUE, ";
        $query .= "     T1.LAST_DAI2_COURSECD || T1.LAST_DAI2_MAJORCD || T1.LAST_DAI2_COURSECODE || ':' || L2.COURSENAME || L2.MAJORNAME || C2.EXAMCOURSE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST L2 ";
        $query .= "              ON L2.YEAR         = T1.ENTEXAMYEAR ";
        $query .= "             AND L2.COURSECD     = T1.LAST_DAI2_COURSECD ";
        $query .= "             AND L2.MAJORCD      = T1.LAST_DAI2_MAJORCD ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST C2 ";
        $query .= "              ON C2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND C2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "             AND C2.TESTDIV      = T1.TESTDIV ";
        $query .= "             AND C2.COURSECD     = T1.LAST_DAI2_COURSECD ";
        $query .= "             AND C2.MAJORCD      = T1.LAST_DAI2_MAJORCD ";
        $query .= "             AND C2.EXAMCOURSECD = T1.LAST_DAI2_COURSECODE ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$applicantdiv}' ";
        $query .= "     AND T1.EXAMNO       = '{$examno}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
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
        $query .= "      T1.testdiv,";
        $query .= "      T1.testdiv2,";
        $query .= "      T1.suc_coursecd || T1.suc_majorcd || T1.suc_coursecode as COURSEMAJOR,";
        $query .= "      T1.suc_coursecd,";
        $query .= "      T1.suc_majorcd,";
        $query .= "      T1.suc_coursecode,";
        $query .= "      T1.judgement,";
        $query .= "      MEISYOU_GET(T1.judgement,'L013',1) AS judgement_name,";
        $query .= "      BD032.REMARK1 AS YOUSIKI3_REMARK,";
        $query .= "      BD032.REMARK2 AS YOUSIKI5_TENKYOBI,";
        $query .= "      BD032.REMARK3 AS YOUSIKI5_TENKYOTI,";
        $query .= "      T1.procedurediv,";
        $query .= "      T1.entdiv,";
        $query .= "      T1.remark1,";
        $query .= "      T1.remark2,";
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
        $query .= "      T4.TOTAL_ALL as TOTAL9,";
        $query .= "      T4.ABSENCE_DAYS  AS ABSENCE_DAYS1, ";
        $query .= "      T4.ABSENCE_DAYS2 AS ABSENCE_DAYS2, ";
        $query .= "      T4.ABSENCE_DAYS3 AS ABSENCE_DAYS3, ";
        $query .= "      T5.name1 AS era_name";
        $query .= "  FROM";
        $query .= "      entexam_applicantbase_dat T1 ";
        $query .= "      LEFT OUTER JOIN v_name_mst T2 ";
        $query .= "                   ON T2.year = T1.entexamyear AND T2.namecd2 = T1.sex   AND T2.namecd1 = 'Z002' ";
        $query .= "      LEFT OUTER JOIN entexam_applicantconfrpt_dat T4 ";
        $query .= "                   ON T4.entexamyear = T1.entexamyear AND T4.applicantdiv = T1.applicantdiv AND T4.examno = T1.examno ";
        $query .= "      LEFT OUTER JOIN v_name_mst T5 ";
        $query .= "                   ON T5.year = T1.entexamyear AND T5.namecd2 = T1.eracd AND T5.namecd1 = 'L007'";
        $query .= "      LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD032 ";
        $query .= "                   ON BD032.ENTEXAMYEAR = T1.ENTEXAMYEAR AND BD032.APPLICANTDIV = T1.APPLICANTDIV AND BD032.EXAMNO = T1.EXAMNO AND BD032.SEQ = '032' ";
        $query .= "  WHERE";
        $query .= "      T1.entexamyear = '".$model->year."' AND ";
        $query .= "      T1.APPLICANTDIV= '".$model->applicantdiv."' AND ";

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
                        APPLICANTDIV= '".$model->applicantdiv."' AND
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

        //合格の時(JUDGEMENT=1,3)に辞退(ENTDIV=2)を選択し、更新をすると、不合格(JUDGEMENT=2)で強制的に更新する。
        if (($model->judgement == "1" || $model->judgement == "3") && $model->field["ENTDIV"] == "2") {
            $model->judgement = "2";
            $model->field["SUC_COURSECD"] = "";
            $model->field["SUC_MAJORCD"] = "";
            $model->field["SUC_COURSECODE"] = "";
        }

        // 志願者基礎データの更新
        $data = array();
        $data["SUC_COURSECD"][TEXT]         = $model->field["SUC_COURSECD"];
        $data["SUC_MAJORCD"][TEXT]          = $model->field["SUC_MAJORCD"];
        $data["SUC_COURSECODE"][TEXT]       = $model->field["SUC_COURSECODE"];
        $data["JUDGEMENT"][TEXT]            = $model->judgement;
        $data["PROCEDUREDIV"][TEXT]         = $model->field["ENTDIV"];
        $data["ENTDIV"][TEXT]               = $model->field["ENTDIV"];
        $data["REMARK1"][TEXT]              = $model->field["REMARK1"];
        $data["REMARK2"][TEXT]              = $model->field["REMARK2"];
        $data["registercd"][TEXT]           = STAFFCD;
        $data["updated"][FUNC]              = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' AND";
        $where .= "       APPLICANTDIV  = '".$model->applicantdiv."' AND";
        $where .= "       EXAMNO        = '".$model->examno."'";
        $query = Query::updateSQL($data, "entexam_applicantbase_dat", $where);
        $db->query($query);

        // 入学コース
        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ENTEXAMYEAR   = '".$model->year."' AND ";
        $query .= "       APPLICANTDIV  = '".$model->applicantdiv."' AND ";
        $query .= "       EXAMNO        = '".$model->examno."' AND ";
        $query .= "       SEQ           = '007' ";
        $db->query($query);

        //合格更新時に（JUDGEMENT=1,3）、入学コースもセットする。
        if ($model->judgement == "1" || $model->judgement == "3") {
            //入学コースを取得
            $getEnter = array();
            $getEnter = $db->getRow(knjl090wQuery::getEnterCourse($model), DB_FETCHMODE_ASSOC);

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]  = $model->year;
            $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
            $data["EXAMNO"][TEXT]       = $model->examno;
            $data["SEQ"][TEXT]          = "007";
            $data["REMARK1"][TEXT]      = $getEnter["ENTER_COURSECD"];
            $data["REMARK2"][TEXT]      = $getEnter["ENTER_MAJORCD"];
            $data["REMARK3"][TEXT]      = $getEnter["ENTER_COURSECODE"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            $db->query($query);
        }

        //別紙様式３の備考
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]  = $model->year;
        $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
        $data["EXAMNO"][TEXT]       = $model->examno;
        $data["SEQ"][TEXT]          = "032";
        $data["REMARK1"][TEXT]      = $model->field["YOUSIKI3_REMARK"];
        $data["REMARK2"][TEXT]      = $model->field["YOUSIKI5_TENKYOBI"];
        $data["REMARK3"][TEXT]      = $model->field["YOUSIKI5_TENKYOTI"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' AND ";
        $where .= "       APPLICANTDIV  = '".$model->applicantdiv."' AND ";
        $where .= "       EXAMNO        = '".$model->examno."' AND ";
        $where .= "       SEQ           = '032' ";

        $query = " SELECT COUNT(*) AS CNT FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ".$where;
        $dataCnt = $db->getOne($query);

        if ($dataCnt > 0) {
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
        } else {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        }
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //入学コース
    function getEnterCourse($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     C1.ENTER_COURSECD, ";
        $query .= "     C1.ENTER_MAJORCD, ";
        $query .= "     C1.ENTER_COURSECODE ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST C1 ON C1.ENTEXAMYEAR = B1.ENTEXAMYEAR ";
        $query .= "         AND C1.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "         AND C1.TESTDIV = B1.TESTDIV ";
        $query .= "         AND C1.COURSECD = B1.SUC_COURSECD ";
        $query .= "         AND C1.MAJORCD = B1.SUC_MAJORCD ";
        $query .= "         AND C1.EXAMCOURSECD = B1.SUC_COURSECODE ";
        $query .= " WHERE ";
        $query .= "         B1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND B1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND B1.EXAMNO       = '{$model->examno}' ";
        return $query;
    }

}
?>