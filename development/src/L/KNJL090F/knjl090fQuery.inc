<?php

require_once('for_php7.php');

class knjl090fQuery extends Query 
{

    //名称マスタよりデータを取得
    function getName($ObjYear, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     namecd1, ";
        $query .= "     namecd2, ";
        $query .= "     name1, ";
        $query .= "     name2, ";
        $query .= "     namespare1 ";
        $query .= " FROM ";
        $query .= "     v_name_mst ";
        $query .= " WHERE ";
        $query .= "     year    = '".$ObjYear ."' AND ";
        $query .= "     namecd1 IN ('".implode($namecd1, "','") ."') ";
        $query .= " ORDER BY ";
        $query .= "     namecd1, ";
        $query .= "     RIGHT('00' || namecd2, 3) ";
        return $query;
    }

    //入試制度を取得
    function get_name_cd($year, $cd, $sitei = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     ABBV1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$cd."' ";
        if ($sitei) {
            $query .= "     AND NAMECD2 = '".$sitei."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //合格コース
    function getSucCourse($year, $applicantdiv) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.EXAMCOURSECD AS VALUE, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.EXAMCOURSECD || ':' || T1.EXAMCOURSE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '1' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //入学課程コース取得
    function getCourseMajorCoursecode($year, $applicantdiv) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.ENTER_COURSECD || T1.ENTER_MAJORCD || T1.ENTER_COURSECODE AS VALUE, ";
        $query .= "     T1.ENTER_COURSECD || T1.ENTER_MAJORCD || T1.ENTER_COURSECODE || ':' || L1.COURSENAME || L1.MAJORNAME || '　' || L2.COURSECODENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST T1 ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST L1 ON L1.YEAR = T1.ENTEXAMYEAR ";
        $query .= "                                    AND L1.COURSECD = T1.ENTER_COURSECD ";
        $query .= "                                    AND L1.MAJORCD = T1.ENTER_MAJORCD ";
        $query .= "     LEFT JOIN V_COURSECODE_MST L2   ON L2.YEAR = T1.ENTEXAMYEAR ";
        $query .= "                                    AND L2.COURSECODE = T1.ENTER_COURSECODE ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '1' ";
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
        $query .= "      N0.name1 AS applicantdivname,";
        $query .= "      T1.testdiv,";
        $query .= "      N1.name1 AS testdivname,";
        $query .= "      C1.REMARK8 || C1.REMARK9 || C1.REMARK10 AS EXAMCOURSE, ";
        $query .= "      C2.EXAMCOURSE_NAME AS EXAMCOURSE_NAME, ";
        $query .= "      T1.suc_coursecd || T1.suc_majorcd || T1.suc_coursecode as COURSEMAJOR,";
        $query .= "      E1.REMARK1 || E1.REMARK2 || E1.REMARK3 as ENTER_COURSEMAJOR,";
        $query .= "      E2.REMARK1 as ENTDIV2_DATE,";
        $query .= "      T1.judgement,";
        $query .= "      T6.name1 AS judgement_name,";
        $query .= "      T1.JUDGE_KIND,";
        $query .= "      N2.NAME1 AS JUDGE_KIND_NAME,";
        $query .= "      T1.SPECIAL_REASON_DIV, ";
        $query .= "      M1.ENTRANCE_FLG, ";
        $query .= "      M1.ENTRANCE_PAY_DIV, ";
        $query .= "      M1.ENTRANCE_PAY_DATE, ";
        $query .= "      M1.ENT_PAY_DIV, ";
        $query .= "      M1.ENT_PAY_DATE, ";
        $query .= "      M1.ENT_PAY_CHAK_DATE, ";
        $query .= "      M1.EXP_PAY_DIV, ";
        $query .= "      M1.EXP_PAY_DATE, ";
        $query .= "      M1.EXP_PAY_CHAK_DATE, ";
        $query .= "      T1.PROCEDUREDATE,";
        $query .= "      T1.procedurediv,";
        $query .= "      T1.entdiv,";
        $query .= "      T2.name1 AS sexname ";
        $query .= "  FROM";
        $query .= "      entexam_applicantbase_dat T1 ";
        $query .= "      LEFT JOIN v_name_mst T2 ";
        $query .= "                   ON T2.year = T1.entexamyear AND T2.namecd2 = T1.sex   AND T2.namecd1 = 'Z002' ";
        $query .= "      LEFT JOIN v_name_mst T6 ";
        $query .= "                   ON T6.year = T1.entexamyear AND T6.namecd2 = T1.judgement AND T6.namecd1 = 'L013'";
        $query .= "      LEFT JOIN v_name_mst N0 ";
        $query .= "                   ON N0.year = T1.entexamyear AND N0.namecd2 = T1.applicantdiv AND N0.namecd1 = 'L003'";
        $query .= "      LEFT JOIN v_name_mst N1 ";
        $query .= "                   ON N1.year = T1.entexamyear AND N1.namecd2 = T1.testdiv AND N1.namecd1 = 'L024'";
        $query .= "      LEFT JOIN v_name_mst N2 ";
        $query .= "                   ON N2.year = T1.entexamyear AND N2.namecd2 = T1.JUDGE_KIND AND N2.namecd1 = 'L025'";
        $query .= "      LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT C1 ";
        $query .= "                   ON  C1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                   AND C1.EXAMNO = T1.EXAMNO ";
        $query .= "                   AND C1.SEQ = '001' ";
        $query .= "      LEFT JOIN ENTEXAM_COURSE_MST C2 ";
        $query .= "                   ON  C2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                   AND C2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                   AND C2.TESTDIV = '1' ";
        $query .= "                   AND C2.COURSECD || C2.MAJORCD || C2.EXAMCOURSECD = C1.REMARK8 || C1.REMARK9 || C1.REMARK10 ";
        $query .= "      LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT E1 ";
        $query .= "                   ON  E1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                   AND E1.EXAMNO = T1.EXAMNO ";
        $query .= "                   AND E1.SEQ = '007' ";
        $query .= "      LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT E2 ";
        $query .= "                   ON  E2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                   AND E2.EXAMNO = T1.EXAMNO ";
        $query .= "                   AND E2.SEQ = '022' ";
        $query .= "      LEFT JOIN ENTEXAM_MONEY_DAT M1 ";
        $query .= "           ON M1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "          AND M1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "          AND M1.EXAMNO = T1.EXAMNO ";
        $query .= "  WHERE";
        $query .= "      T1.entexamyear = '".$model->year."' AND ";
        $query .= "      T1.APPLICANTDIV = '1' AND ";//1:中学

        if ($model->cmd == "back1" || $model->cmd == "back2") {
            $query .= "      T1.examno < '".$model->examno."'";
            $query .= " ORDER BY T1.examno DESC ";
            //$query .= " FETCH FIRST 1 ROWS ONLY "; 
        } elseif ($model->cmd == "next1" || $model->cmd == "next2") {
            $query .= "      T1.examno > '".$model->examno."'";
            $query .= " ORDER BY T1.examno ASC ";
            //$query .= " FETCH FIRST 1 ROWS ONLY "; 
        } else {
            $query .= "      T1.examno = '".$model->examno."'";
        }

        return $query;
    }

    //選択された入試区分以外の受付データのすべての合否取得
    function getReceptJudge($model)
    {
        $query  = "SELECT";
        $query .= "        SUM(CASE judgediv WHEN '1' THEN 1 ELSE 0 END)              AS pass";         //合格
        $query .= "       ,SUM(CASE coalesce(judgediv, '') WHEN '' THEN 1 ELSE 0 END) AS unknown";      //未設定
        $query .= "       ,COUNT(*) AS cnt";                                                            //受験回数
        $query .= "  FROM v_entexam_recept_dat ";
        $query .= " WHERE entexamyear = '".$model->year."'";
        $query .= "   AND examno      = '".$model->examno."'";
        $query .= "   AND testdiv    <> '".$model->testdiv2."'";
        return $query;
    }

    //合格した入試区分(MIN)取得
    function getMintestdiv($model, $applicantdiv)
    {
        $query  = " SELECT min(testdiv) ";
        $query .= "   FROM v_entexam_recept_dat ";
        $query .= "  WHERE entexamyear  = '".$model->year."' ";
        $query .= "    AND applicantdiv = '".$applicantdiv."' ";
        $query .= "    AND examno       = '".$model->examno."' ";
        $query .= "    AND judgediv     = '1' ";
        return $query;
    }

    //最大testdiv取得
    function getMaxtestdiv($model, $applicantdiv)
    {
        $query  = " SELECT max(testdiv) ";
        $query .= "   FROM v_entexam_recept_dat ";
        $query .= "  WHERE entexamyear  = '".$model->year."' ";
        $query .= "    AND applicantdiv = '".$applicantdiv."' ";
        $query .= "    AND examno       = '".$model->examno."' ";
        return $query;
    }

    //受付データのテーブル
    function getRecept($model, $applicantdiv) {
        $query  = "";
        $query .= " SELECT";
        $query .= "     T1.receptno,";
        $query .= "     T1.examno,";
        $query .= "     T1.testdiv AS testdiv2,";
        $query .= "     T1.JUDGEDIV,";
        $query .= "     T1.JUDGEDIV || ':' || N1.NAME1 AS JUDGEDIV_NAME,";
        $query .= "     T1.TOTAL4,";
        $query .= "     T1.avarage4,";
        $query .= "     T1.div_rank4";
        $query .= " FROM";
        $query .= "     v_entexam_recept_dat T1 ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'L013' AND N1.NAMECD2 = T1.JUDGEDIV ";
        $query .= " WHERE";
        $query .= "     T1.entexamyear  = '{$model->year}' AND ";
        $query .= "     T1.applicantdiv = '{$applicantdiv}' AND ";
        $query .= "     T1.testdiv      = '{$model->testdiv2}' AND ";
        $query .= "     T1.examno       = '{$model->examno}' ";

        return $query;
    }

    //得点データ取得
    function getScore($model, $applicantdiv) {
        $query  = " SELECT ";
        $query .= "     T2.TESTSUBCLASSCD, ";
        $query .= "     T2.ATTEND_FLG, ";
        $query .= "     T2.SCORE ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_SCORE_DAT T2 ";
        $query .= "         ON  T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.TESTDIV      = T1.TESTDIV ";
        $query .= "         AND T2.EXAM_TYPE    = T1.EXAM_TYPE ";
        $query .= "         AND T2.RECEPTNO     = T1.RECEPTNO ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '{$model->year}' AND ";
        $query .= "     T1.APPLICANTDIV = '{$applicantdiv}' AND ";
        $query .= "     T1.TESTDIV      = '{$model->testdiv2}' AND ";
        $query .= "     T1.EXAMNO       = '{$model->examno}' ";
        $query .= " ORDER BY ";
        $query .= "     T2.TESTSUBCLASSCD ";

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

        //合格した入試区分(MIN)を取得
        $testdiv2 = $db->getOne(knjl090fQuery::getMintestdiv($model, "1"));

        // 1.志願者受付データの更新
        // 主に、手続日を訂正する時に更新したい。現状、合格者を対象としておく。
        // 手続日、手続区分を更新
        if (strlen($testdiv2)) {
            $data = array();
            $data["PROCEDUREDATE1"][TEXT]       = !strlen($model->field["PROCEDUREDATE"]) ? "" : str_replace("/", "-", $model->field["PROCEDUREDATE"]);
            if (strlen($model->field["PROCEDUREDATE"])) {
                $data["PROCEDUREDIV1"][TEXT]    = !strlen($model->field["PROCEDUREDIV"]) ? "1" : $model->field["PROCEDUREDIV"];
            } else {
                $data["PROCEDUREDIV1"][TEXT]    = $model->field["PROCEDUREDIV"] == "1" ? "" : $model->field["PROCEDUREDIV"];
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            //合格した入試区分(MIN)のレコードを更新
            $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV  = '1' ";
            $where .= "   AND TESTDIV       = '".$testdiv2."' ";
            $where .= "   AND EXAMNO        = '".$model->examno."' ";
            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
            $db->query($query);
        }

        // 2.志願者基礎データの更新
        $data = array();
        $data["SUC_COURSECD"][TEXT]         = $model->field["SUC_COURSECD"];
        $data["SUC_MAJORCD"][TEXT]          = $model->field["SUC_MAJORCD"];
        $data["SUC_COURSECODE"][TEXT]       = $model->field["SUC_COURSECODE"];
        $data["JUDGEMENT"][TEXT]            = $model->judgement;
        $data["PROCEDUREDATE"][TEXT]        = !strlen($model->field["PROCEDUREDATE"]) ? "" : str_replace("/", "-", $model->field["PROCEDUREDATE"]);
        if (strlen($model->field["PROCEDUREDATE"])) {
            $data["PROCEDUREDIV"][TEXT]     = !strlen($model->field["PROCEDUREDIV"]) ? "1" : $model->field["PROCEDUREDIV"];
        } else {
            $data["PROCEDUREDIV"][TEXT]     = $model->field["PROCEDUREDIV"] == "1" ? "" : $model->field["PROCEDUREDIV"];
        }
        if (strlen($model->field["EXP_PAY_DATE"])) {
            $data["ENTDIV"][TEXT]           = !strlen($model->field["ENTDIV"]) ? "1" : $model->field["ENTDIV"];
        } else {
            $data["ENTDIV"][TEXT]           = $model->field["ENTDIV"] == "1" ? "" : $model->field["ENTDIV"];
        }
        $data["registercd"][TEXT]           = STAFFCD;
        $data["updated"][FUNC]              = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' AND";
        $where .= "       EXAMNO        = '".$model->examno."'";
        $query = Query::updateSQL($data, "entexam_applicantbase_dat", $where);
        $db->query($query);

        // 3.入学基礎詳細データの更新（入学コース）
        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND ";
        $query .= "       EXAMNO      = '".$model->examno."' AND ";
        $query .= "       SEQ         = '007' ";
        $db->query($query);

        if ($model->field["ENTER_COURSEMAJOR"]) {
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]  = $model->year;
            $data["EXAMNO"][TEXT]       = $model->examno;
            $data["SEQ"][TEXT]          = "007";
            $data["REMARK1"][TEXT]      = $model->field["ENTER_COURSECD"];
            $data["REMARK2"][TEXT]      = $model->field["ENTER_MAJORCD"];
            $data["REMARK3"][TEXT]      = $model->field["ENTER_COURSECODE"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            $db->query($query);
        }

        //4.ENTEXAM_MONEY_DAT
        $data = array();
        //入学金
        $data["ENT_PAY_DIV"][TEXT]      = !strlen($model->field["ENT_PAY_DATE"]) ? "" : $model->field["ENT_PAY_DIV"];
        $data["ENT_PAY_DATE"][TEXT]     = !strlen($model->field["ENT_PAY_DATE"]) ? "" : str_replace("/", "-", $model->field["ENT_PAY_DATE"]);
        $data["ENT_PAY_CHAK_DATE"][TEXT]= !strlen($model->field["ENT_PAY_CHAK_DATE"]) ? "" : str_replace("/", "-", $model->field["ENT_PAY_CHAK_DATE"]);
        $data["ENT_DUE_DATE"][TEXT]     = !strlen($model->field["ENT_PAY_DATE"]) ? "" : str_replace("/", "-", $model->field["ENT_PAY_DATE"]);
        $query = knjl090fQuery::getPayMoney($model, $model->examno);
        $data["ENT_PAY_MONEY"][NUMBER]  = !strlen($model->field["ENT_PAY_DATE"]) ? "" : $db->getOne($query);
        $data["ENT_DUE_MONEY"][NUMBER]  = !strlen($model->field["ENT_PAY_DATE"]) ? "" : $db->getOne($query);
        //諸費
        $data["EXP_PAY_DIV"][TEXT]      = !strlen($model->field["EXP_PAY_DATE"]) ? "" : $model->field["EXP_PAY_DIV"];
        $data["EXP_PAY_DATE"][TEXT]     = !strlen($model->field["EXP_PAY_DATE"]) ? "" : str_replace("/", "-", $model->field["EXP_PAY_DATE"]);
        $data["EXP_PAY_CHAK_DATE"][TEXT]= !strlen($model->field["EXP_PAY_CHAK_DATE"]) ? "" : str_replace("/", "-", $model->field["EXP_PAY_CHAK_DATE"]);
        $data["EXP_DUE_DATE"][TEXT]     = !strlen($model->field["EXP_PAY_DATE"]) ? "" : str_replace("/", "-", $model->field["EXP_PAY_DATE"]);
        $query = knjl090fQuery::getPayMoney($model, $model->examno, "EXP");
        $data["EXP_PAY_MONEY"][NUMBER]  = !strlen($model->field["EXP_PAY_DATE"]) ? "" : $db->getOne($query);
        $data["EXP_DUE_MONEY"][NUMBER]  = !strlen($model->field["EXP_PAY_DATE"]) ? "" : $db->getOne($query);
        //支度金
        if ($model->field["APPLICANTDIV"] == "2") {
            $data["ENTRANCE_FLG"][TEXT]         = !strlen($model->field["ENTRANCE_FLG"]) ? "" : $model->field["ENTRANCE_FLG"];
            $data["ENTRANCE_PAY_DIV"][TEXT]     = !strlen($model->field["ENTRANCE_FLG"]) ? "" : $model->field["ENTRANCE_PAY_DIV"];
            $data["ENTRANCE_PAY_DATE"][TEXT]    = !strlen($model->field["ENTRANCE_FLG"]) ? "" : str_replace("/", "-", $model->field["ENTRANCE_PAY_DATE"]);
            $data["ENTRANCE_DUE_DATE"][TEXT]    = !strlen($model->field["ENTRANCE_FLG"]) ? "" : str_replace("/", "-", $model->field["ENTRANCE_PAY_DATE"]);
            $query = knjl090fQuery::getPayMoney($model, $model->examno, "ENTRANCE");
            $data["ENTRANCE_PAY_MONEY"][NUMBER] = !strlen($model->field["ENTRANCE_FLG"]) ? "" : $db->getOne($query);
            $data["ENTRANCE_DUE_MONEY"][NUMBER] = !strlen($model->field["ENTRANCE_FLG"]) ? "" : $db->getOne($query);
        }
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        //条件
        $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
        $where .= "   AND EXAMNO        = '".$model->examno."' ";
        $where .= "   AND APPLICANTDIV  = '".$model->field["APPLICANTDIV"]."' ";
        $query = Query::updateSQL($data, "ENTEXAM_MONEY_DAT", $where);
        $db->query($query);

        // 3.入学基礎詳細データの更新（辞退日）
        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND ";
        $query .= "       EXAMNO      = '".$model->examno."' AND ";
        $query .= "       SEQ         = '022' ";
        $db->query($query);

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]  = $model->year;
        $data["EXAMNO"][TEXT]       = $model->examno;
        $data["SEQ"][TEXT]          = "022";
        $data["REMARK1"][TEXT]      = str_replace('/', '-', $model->field["ENTDIV2_DATE"]);
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    function getPayMoney($model, $examno, $div = "") {
        $query  = "";

        //免除額コードを取得(BASE)
        $query .= " WITH V_COMMON AS ( ";
        $query .= "     SELECT ";
        $query .= "         '1' AS LINK, ";
        $query .= "         EXEMPTION_CD ";
        $query .= "     FROM ";
        $query .= "         V_ENTEXAM_APPLICANTBASE_EXEMPTION_DAT ";
        $query .= "     WHERE ";
        $query .= "         ENTEXAMYEAR = '{$model->year}' ";
        $query .= "         AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "         AND EXAMNO = '{$examno}' ";
        //基本額を取得（費目マスタ）
        $query .= " ), T_ITEM AS ( ";
        $query .= "     SELECT ";
        $query .= "         '1' AS LINK, ";
        $query .= "         SUM(ITEM_MONEY) AS ITEM_MONEY ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_PAYMENT_ITEM_MST ";
        $query .= "     WHERE ";
        $query .= "         ENTEXAMYEAR = '{$model->year}' ";
        $query .= "         AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "         AND DIV = '0' ";
        if ($div == "EXP") {
        } else if ($div == "ENTRANCE") {
            $query .= "         AND REMARK5 = '1' ";
        } else {
            $query .= "         AND REMARK1 = '1' ";
        }
        //免除額を取得（免除額設定）
        $query .= " ), T_EXEMPTION AS ( ";
        $query .= "     SELECT ";
        $query .= "         P2.EXEMPTION_CD, ";
        if ($div == "EXP") {
            $query .= "         SUM(P1.ITEM_MONEY) AS EXEMPTION_MONEY ";
        } else if ($div == "ENTRANCE") {
            $query .= "         SUM(P2.EXEMPTION_MONEY) AS EXEMPTION_MONEY ";//ダミー
        } else {
            $query .= "         SUM(P2.EXEMPTION_MONEY) AS EXEMPTION_MONEY ";
        }
        $query .= "     FROM ";
        $query .= "         ENTEXAM_PAYMENT_ITEM_MST P1 ";
        $query .= "         INNER JOIN ENTEXAM_PAYMENT_EXEMPTION_MST P2 ";
        $query .= "              ON P2.ENTEXAMYEAR = P1.ENTEXAMYEAR ";
        $query .= "             AND P2.APPLICANTDIV = P1.APPLICANTDIV ";
        $query .= "             AND P2.DIV = P1.DIV ";
        $query .= "             AND P2.ITEM_CD = P1.ITEM_CD ";
        if ($div == "EXP") {
            $query .= "             AND P2.KIND_CD = '2' ";
        } else if ($div == "ENTRANCE") {
        } else {
            $query .= "             AND P2.KIND_CD = '1' ";
        }
        $query .= "     WHERE ";
        $query .= "         P1.ENTEXAMYEAR = '{$model->year}' ";
        $query .= "         AND P1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "         AND P1.DIV = '0' ";
        if ($div == "EXP") {
        } else if ($div == "ENTRANCE") {
            $query .= "         AND P1.REMARK5 = '1' ";
        } else {
            $query .= "         AND P1.REMARK1 = '1' ";
        }
        $query .= "     GROUP BY ";
        $query .= "         P2.EXEMPTION_CD ";
        $query .= " ) ";
        //基本額から免除額を引いた金額を取得
        $query .= " SELECT ";
        if ($div == "EXP") {
            //諸費
            $query .= "     VALUE(L2.EXEMPTION_MONEY, 0) AS MONEY ";
        } else if ($div == "ENTRANCE") {
            //支度金
            $query .= "     VALUE(L1.ITEM_MONEY, 0) AS MONEY ";
        } else {
            //入学金
            $query .= "     VALUE(L1.ITEM_MONEY, 0) - VALUE(L2.EXEMPTION_MONEY, 0) AS MONEY ";
        }
        $query .= " FROM ";
        $query .= "     V_COMMON T1 ";
        $query .= "     LEFT JOIN T_ITEM L1 ON L1.LINK = T1.LINK ";
        $query .= "     LEFT JOIN T_EXEMPTION L2 ON L2.EXEMPTION_CD = T1.EXEMPTION_CD ";

        return $query;
    }

}
?>
