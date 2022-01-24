<?php

require_once('for_php7.php');

class knjl211yQuery extends Query 
{

    //MAX受験番号＋１を取得（新規ボタン）
    function getMaxExamnoQuery($model) {
        //MAX受験番号(初期値)
        $defExamno = 0;
        if ($model->field["TESTDIV"] == "1") {
            $defExamno = 1000;
        } else if ($model->field["TESTDIV"] == "2") {
            $defExamno = 2000;
        } else if ($model->field["TESTDIV"] == "3") {
            $defExamno = 3000;
        }

        $db = Query::dbCheckOut();
        $query  = " SELECT  VALUE(MAX(INT(T1.EXAMNO)), {$defExamno}) + 1 AS EXAMNO ";
        $query .= " FROM    ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " WHERE   T1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "   AND   T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "   AND   T1.TESTDIV      = '{$model->field["TESTDIV"]}' ";
        $max_examno = $db->getOne($query);
        Query::dbCheckIn($db);
        return $max_examno;
    }

    //最小受験番号
    function getMinExam($year, $applicantDiv) {
        $query  = " SELECT ";
        $query .= "     MIN(T1.EXAMNO) AS EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$applicantDiv}' ";

        return $query;
    }

    //名称マスタよりデータを取得
    function get_name_cd($year, $cd, $cd2 = "")
    {
        $query  = " SELECT ";
        $query .= "      NAMECD2, ";
        $query .= "      NAME1, ";
        $query .= "      NAME2, ";
        $query .= "      ABBV1, ";
        $query .= "      NAMESPARE1, ";
        $query .= "      NAMESPARE2 ";
        $query .= "  FROM ";
        $query .= "      V_NAME_MST ";
        $query .= "  WHERE ";
        $query .= "      YEAR = '".$year."' ";
        $query .= "      AND NAMECD1 = '".$cd."' ";
        if ($cd2) {
            $query .= "      AND NAMECD2 = '".$cd2."' ";
        }
        return $query;
    }

    //名称マスタより和暦の元号を取得
    function get_calendarno($year)
    {
        $query = "  SELECT
                        NAMECD2,
                        NAME1,
                        NAMESPARE1,
                        NAMESPARE2,
                        NAMESPARE3
                    FROM
                        V_NAME_MST
                    WHERE
                        YEAR = '".$year."' AND
                        NAMECD1 = 'L007'
                 ";

        return $query;
    }

    //メインデータ取得
    function get_edit_data($model)
    {
        $db = Query::dbCheckOut();

        //TESTDIV:      (1)(2)(3)
        //TESTDIV1～3:  (1,2)(2,3)このパターン（再受験）の入試区分を取得
        $query = "  SELECT
                        T1.APPLICANTDIV,
                        T1.TESTDIV,
                        R1.JUDGEDIV,
                        N1.NAME1 AS JUDGEDIV_NAME,
                        T1.GENERAL_FLG,
                        CASE WHEN T1.GENERAL_FLG IS NOT NULL AND T1.TESTDIV3 IS NOT NULL THEN T1.TESTDIV3
                             WHEN T1.GENERAL_FLG IS NOT NULL AND T1.TESTDIV2 IS NOT NULL THEN T1.TESTDIV2
                        END AS GENERAL_TESTDIV,
                        T1.EXAMNO,
                        T1.NAME,
                        T1.NAME_KANA,
                        T1.SEX,
                        T1.ERACD,
                        N7.NAME1,
                        T1.BIRTH_Y,
                        T1.BIRTH_M,
                        T1.BIRTH_D,
                        T1.BIRTHDAY,
                        T1.FS_CD,
                        T1.FS_NAME,
                        T2.ZIPCD,
                        T2.ADDRESS1,
                        T2.ADDRESS2,
                        T2.TELNO,
                        T2.GNAME,
                        T2.GKANA,
                        T2.GZIPCD,
                        T2.GADDRESS1,
                        T2.GADDRESS2,
                        T2.GTELNO,
                        T2.RELATIONSHIP,
                        C1.REMARK1 AS CHARACTER_REMARK1,
                        C1.REMARK2 AS CHARACTER_REMARK2,
                        C1.REMARK3 AS CHARACTER_REMARK3,
                        C1.REMARK4 AS CHARACTER_REMARK4
                    FROM
                        ENTEXAM_APPLICANTBASE_DAT T1
                        LEFT JOIN ENTEXAM_APPLICANTADDR_DAT T2 ON T1.ENTEXAMYEAR = T2.ENTEXAMYEAR AND T1.EXAMNO = T2.EXAMNO
                        LEFT JOIN V_NAME_MST N7 ON N7.YEAR = T1.ENTEXAMYEAR AND N7.NAMECD1 = 'L007' AND N7.NAMECD2 = T1.ERACD
                        LEFT JOIN ENTEXAM_RECEPT_DAT R1 ON R1.ENTEXAMYEAR = T1.ENTEXAMYEAR AND R1.APPLICANTDIV = T1.APPLICANTDIV AND R1.TESTDIV = T1.TESTDIV AND R1.EXAMNO = T1.EXAMNO
                        LEFT JOIN V_NAME_MST N1 ON N1.YEAR = R1.ENTEXAMYEAR AND N1.NAMECD1 = 'L013' AND N1.NAMECD2 = R1.JUDGEDIV
                        LEFT JOIN ENTEXAM_APPLICANT_CHARACTER_DAT C1 ON T1.ENTEXAMYEAR = C1.ENTEXAMYEAR AND T1.EXAMNO = C1.EXAMNO
                    WHERE
                        T1.ENTEXAMYEAR = '".$model->year."' AND ";

        if ($model->cmd == "back" || $model->cmd == "back1") {
            $query.= " T1.EXAMNO < '".$model->examno."'";
            $query.= " ORDER BY ";
            $query.= "     T1.EXAMNO DESC ";
        } elseif ($model->cmd == "next" || $model->cmd == "next1") {
            $query.= " T1.EXAMNO > '".$model->examno."'";
            $query.= " ORDER BY ";
            $query.= "     T1.EXAMNO ASC ";
        } else {
            $query.= " T1.EXAMNO = '".$model->examno."'";
        }

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //家族構成
    function getFamily($year, $examno) {
        $query  = " SELECT ";
        $query .= "     T1.SEQ, ";
        $query .= "     N1.NAME1 AS FAMILY_RELATIONSHIP, ";
        $query .= "     T1.REMARK AS FAMILY_REMARK ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_FAMILY_DAT T1 ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ";
        $query .= "             ON  N1.YEAR = T1.ENTEXAMYEAR ";
        $query .= "             AND N1.NAMECD1 = 'H201' ";
        $query .= "             AND N1.NAMECD2 = T1.RELATIONSHIP ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR = '{$year}' ";
        $query .= "     AND T1.EXAMNO = '{$examno}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SEQ ";

        return $query;
    }

    //合否がある入試は変更不可チェック
    function ExistdataTestdiv($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT  T1.EXAMNO ";
        $query .= " FROM    ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " WHERE   T1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "   AND   T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "   AND   T1.TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= "   AND   T1.EXAMNO = '{$model->examno}' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //データ重複チェック
    function CheckData($model)
    {
        $db = Query::dbCheckOut();

        $query = "  SELECT
                       T1.EXAMNO,
                       T2.NAME1
                    FROM
                        ENTEXAM_APPLICANTBASE_DAT T1
                    LEFT OUTER JOIN
                        V_NAME_MST T2
                    ON
                        T1.ENTEXAMYEAR = T2.YEAR AND
                        T1.APPLICANTDIV = T2.NAMECD2 AND
                        T2.NAMECD1 = 'L003'
                    WHERE
                        T1.ENTEXAMYEAR = '".$model->year."' AND
                        T1.EXAMNO = '".$model->examno."' AND
                        T1.APPLICANTDIV <> '".$model->applicantdiv."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
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
                        EXAMNO = '".$model->examno."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //該当日付取得
    function CheckDate($model)
    {
        $db = Query::dbCheckOut();

        $query = "  SELECT
                        NAME1,
                        NAMESPARE1,
                        NAMESPARE2,
                        NAMESPARE3
                    FROM
                        V_NAME_MST
                    WHERE
                        YEAR = '".$model->year."' AND
                        NAMECD1 = 'L007' AND
                        NAMECD2 = '".$model->field["ERACD"]. "'";
        
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }


    //--- INSERT
    //--- UPDATE
    function &getInsertUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $result = $db->query(knjl211yQuery::get_calendarno($model->year));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $eracd[] = $row["NAMECD2"];
            $defyear[] = $row["NAMESPARE1"];
            $defsdate[] = $row["NAMESPARE2"];
            $defedate[] = $row["NAMESPARE3"];
        }
        $result->free();

        //生年月日の変換(西暦→和暦)
        $w_eracd = $w_year = $mon = $day = "";
        if (strlen($model->field["BIRTHDAY"])) {
            list($s_year, $mon, $day) = explode("/", $model->field["BIRTHDAY"]);
            for ($i=0; $i<get_count($eracd); $i++) {
                if ($defsdate[$i] <= $model->field["BIRTHDAY"] && $model->field["BIRTHDAY"] <= $defedate[$i]) {
                    $w_eracd = $eracd[$i];
                    $w_year = ((int)$s_year + 1) - ((int)$defyear[$i]);
                    $w_year = sprintf("%02d",$w_year);
                    break;
                }
            }
        }

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]  = $model->year;
        $data["APPLICANTDIV"][TEXT] = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]       = $model->examno;

        //再受験フラグ(出願２回目)(再受験)
        $data["GENERAL_FLG"][TEXT]  = $model->field["GENERAL_FLG"];
        //入試区分(出願パターン)(1)(2)(3)(1,2)(2,3)
        //連続する２回まで出願することができます。
        //一旦クリア
        for ($code = 1; $code <= 3; $code++) {
            $fieldname = "TESTDIV" .$code;
            $data[$fieldname][TEXT] = "";
        }
        //セット
        for ($code = 1; $code <= 3; $code++) {
            $fieldname = "TESTDIV" .$code;
            //(出願１回目)
            if ($model->field["TESTDIV"] == $code)      $data[$fieldname][TEXT] = $code;
            //(出願２回目)(再受験)
            if (strlen($model->field["GENERAL_FLG"]) && $model->field["GENERAL_TESTDIV"] == $code)  $data[$fieldname][TEXT] = $code;
        }
        //(出願１回目)をここにもセット・・・初回出願を保持するため
        $data["TESTDIV"][TEXT]      = $model->field["TESTDIV"];

        $data["DESIREDIV"][TEXT]    = "1";
        $data["SHDIV"][TEXT]        = "1";
        $data["NAME"][TEXT]         = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]    = $model->field["NAME_KANA"];
        $data["SEX"][TEXT]          = $model->field["SEX"];
        $data["ERACD"][TEXT]        = $w_eracd;
        $data["BIRTH_Y"][TEXT]      = $w_year;
        $data["BIRTH_M"][TEXT]      = $mon;
        $data["BIRTH_D"][TEXT]      = $day;
        $data["BIRTHDAY"][TEXT]     = str_replace("/","-",$model->field["BIRTHDAY"]);
        $data["FS_CD"][TEXT]        = $model->field["FS_CD"];
        $data["FS_NAME"][TEXT]      = $model->field["FS_NAME"];

        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        if ($this->cmd != "add") {
            $where  = " WHERE ENTEXAMYEAR = '".$model->year."' ";
            $where .= "       AND EXAMNO  = '".$model->examno."'";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);
        } else {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
            $db->query($query);
        }

        //ENTEXAM_APPLICANTADDR_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["EXAMNO"][TEXT]        = $model->examno;
        $data["ZIPCD"][TEXT]         = $model->field["ZIPCD"];
        $data["ADDRESS1"][TEXT]      = $model->field["ADDRESS1"];
        $data["ADDRESS2"][TEXT]      = $model->field["ADDRESS2"];
        $data["TELNO"][TEXT]         = $model->field["TELNO"];
        $data["GNAME"][TEXT]         = $model->field["GNAME"];
        $data["GKANA"][TEXT]         = $model->field["GKANA"];
        $data["GZIPCD"][TEXT]        = $model->field["GZIPCD"];
        $data["GADDRESS1"][TEXT]     = $model->field["GADDRESS1"];
        $data["GADDRESS2"][TEXT]     = $model->field["GADDRESS2"];
        $data["GTELNO"][TEXT]        = $model->field["GTELNO"];
        $data["RELATIONSHIP"][TEXT]  = $model->field["RELATIONSHIP"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        $query  = " SELECT COUNT(*) FROM ENTEXAM_APPLICANTADDR_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query .= "       AND EXAMNO  = '".$model->examno."' ";
        if (0 == $db->getOne($query)) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
            $db->query($query);
        } else {
            $where  = " WHERE ENTEXAMYEAR = '".$model->year."' ";
            $where .= "       AND EXAMNO  = '".$model->examno."' ";
            $query  = Query::updateSQL($data, "ENTEXAM_APPLICANTADDR_DAT", $where);
            $db->query($query);
        }

        //ENTEXAM_APPLICANT_CHARACTER_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]  = $model->year;
        $data["EXAMNO"][TEXT]       = $model->examno;
        $data["REMARK1"][TEXT]      = $model->field["CHARACTER_REMARK1"];
        $data["REMARK2"][TEXT]      = $model->field["CHARACTER_REMARK2"];
        $data["REMARK3"][TEXT]      = $model->field["CHARACTER_REMARK3"];
        $data["REMARK4"][TEXT]      = $model->field["CHARACTER_REMARK4"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query  = " SELECT COUNT(*) FROM ENTEXAM_APPLICANT_CHARACTER_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query .= "       AND EXAMNO  = '".$model->examno."' ";
        if (0 == $db->getOne($query)) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANT_CHARACTER_DAT");
            $db->query($query);
        } else {
            $where  = " WHERE ENTEXAMYEAR = '".$model->year."' ";
            $where .= "       AND EXAMNO  = '".$model->examno."' ";
            $query  = Query::updateSQL($data, "ENTEXAM_APPLICANT_CHARACTER_DAT", $where);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //--- DELETE
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_FAMILY_DAT
        $query  = " DELETE FROM ENTEXAM_FAMILY_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANT_CHARACTER_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANT_CHARACTER_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_SCORE_DAT
        $query  = " DELETE FROM ENTEXAM_SCORE_DAT ";
        $query .= " WHERE (ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAM_TYPE,RECEPTNO) IN  (";
        $query .= "         SELECT ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAM_TYPE, RECEPTNO ";
        $query .= "           FROM ENTEXAM_RECEPT_DAT ";
        $query .= "          WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "                EXAMNO      = '".$model->examno."'";
        $query .= "       )";
        $db->query($query);

        //ENTEXAM_RECEPT_DAT
        $query  = "DELETE FROM ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //氏名を取得
    function getExamName($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR     = '{$model->year}' AND ";
        $query .= "     T1.APPLICANTDIV    = '{$model->attend["ATTEND_APPLICANTDIV"]}' AND ";
        $query .= "     T1.EXAMNO          = '{$model->attend["ATTEND_EXAMNO"]}' ";
        return $query;
    }

    //欠席フラグを取得
    function getAttendFlg($model) {
        $namecd1 = "L004";
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.TESTDIV || ':' || L1.NAME1 AS TESTDIV_NAME, ";
        $query .= "     T1.JUDGEDIV as ATTEND_FLG, ";
        $query .= "     T1.HONORDIV as ATTEND_REASON, ";
        $query .= "     L2.NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = '{$namecd1}' AND L1.NAMECD2 = T1.TESTDIV ";
        $query .= "     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'L013' AND L2.NAMECD2 = T1.JUDGEDIV ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR     = '{$model->year}' AND ";
        $query .= "     T1.APPLICANTDIV    = '{$model->attend["ATTEND_APPLICANTDIV"]}' AND ";
        $query .= "     T1.EXAMNO          = '{$model->attend["ATTEND_EXAMNO"]}' AND ";
        $query .= "     value(L2.NAMESPARE1,'0') = '0' "; //合格は表示しない
        $query .= " ORDER BY ";
        $query .= "     T1.TESTDIV ";
        return $query;
    }

    //更新処理(欠席フラグ)
    function &getAttendUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $arrAttendFlg = array();
        $query = knjl211yQuery::getAttendFlg($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arrAttendFlg[$row["TESTDIV"]] = $row["NAMESPARE1"];
        }
        $result->free();

        if (0 < get_count($arrAttendFlg)) {
            foreach ($arrAttendFlg as $testdiv => $namespare1) {
                //合格の場合、上書きしない
                if ($namespare1 == "1") continue;
                //項目
                $name = "ATTEND_FLG" .$testdiv;
                $nameReason = "ATTEND_REASON" .$testdiv;
                $data = array();
                $data["JUDGEDIV"][TEXT]     = $model->attend[$name];
                $data["HONORDIV"][TEXT]     = $model->attend[$nameReason];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";
                //条件
                $where  = " WHERE ";
                $where .= "     ENTEXAMYEAR     = '{$model->year}' AND ";
                $where .= "     APPLICANTDIV    = '{$model->attend["ATTEND_APPLICANTDIV"]}' AND ";
                $where .= "     TESTDIV         = '{$testdiv}' AND ";
                $where .= "     EXAMNO          = '{$model->attend["ATTEND_EXAMNO"]}' ";
                $where .= " AND value(JUDGEDIV,'4') = '4' ";//合否区分が「欠席」「ブランク」のみ更新
                //UPDATE
                $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
                $db->query($query);
                //項目
                $data = array();
                $data["JUDGEMENT"][TEXT]    = $model->attend[$name];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";
                //条件
                $where  = " WHERE ";
                $where .= "     ENTEXAMYEAR     = '{$model->year}' AND ";
                $where .= "     APPLICANTDIV    = '{$model->attend["ATTEND_APPLICANTDIV"]}' AND ";
                $where .= "     EXAMNO          = '{$model->attend["ATTEND_EXAMNO"]}' AND ";
                $where .= "     value(JUDGEMENT,'0') NOT IN ('1','3','5') ";//合格の場合、上書きしない
                $where .= " AND value(JUDGEMENT,'4') = '4' ";//合否区分が「欠席」「ブランク」のみ更新
                //UPDATE
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>