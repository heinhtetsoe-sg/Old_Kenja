<?php

require_once('for_php7.php');
class knjl280gQuery extends Query
{

    //名称
    public function GetName($namecd1, $ObjYear)
    {
        $query  = " SELECT ";
        $query .= "     namecd2, ";
        $query .= "     name1, ";
        $query .= "     name2, ";
        $query .= "     namespare1, ";
        $query .= "     namespare2, ";
        $query .= "     ABBV3, ";
        $query .= "     NAMESPARE3, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     v_name_mst ";
        $query .= " WHERE ";
        $query .= "     year = '".$ObjYear."' ";
        $query .= "     AND namecd1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     namecd2 ";
        return $query;
    }

    //対象者・合格者
    public function GetLeftList($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.PAY_MONEY, ";
        $query .= "     L1.EXAMCOURSE_ABBV, ";
        $query .= "     CASE WHEN T1.PROCEDUREDIV = '1' ";
        $query .= "          THEN T1.PROCEDUREDATE ";
        $query .= "     END AS PRODATE, ";
        $query .= "     CASE WHEN T1.PROCEDUREDIV = '1' ";
        $query .= "          THEN '1' "; //対象者一覧
        $query .= "          ELSE '2' "; //合格者一覧
        $query .= "     END AS LEFT_FLG ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST L1 ";
        $query .= "             ON  L1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND L1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "             AND L1.TESTDIV      = T1.TESTDIV ";
        $query .= "             AND L1.COURSECD     = T1.SUC_COURSECD ";
        $query .= "             AND L1.MAJORCD      = T1.SUC_MAJORCD ";
        $query .= "             AND L1.EXAMCOURSECD = T1.SUC_COURSECODE ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND T1.SHDIV        = '{$model->shdiv}' ";
        $query .= "     AND T1.JUDGEMENT IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L013' AND NAMESPARE1 = '1') ";
        //辞退者は除く
        $query .= "     AND VALUE(T1.PROCEDUREDIV, '')  != '2' "; //手続区分 1:済み 2:無し
        $query .= "     AND VALUE(T1.ENTDIV, '')        != '2' "; //入学区分 1:済み 2:辞退
        $query .= " ORDER BY ";
        $query .= "     T1.EXAMNO ";

        return $query;
    }

    //入学コース取得
    public function getEnterCourse($model, $examno)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.EXAMNO, ";
        //入学コース
        $query .= "     L1.ENTER_COURSECD, ";
        $query .= "     L1.ENTER_MAJORCD, ";
        $query .= "     L1.ENTER_COURSECODE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST L1 ";
        $query .= "             ON  L1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND L1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "             AND L1.TESTDIV      = T1.TESTDIV ";
        $query .= "             AND L1.COURSECD     = T1.SUC_COURSECD ";
        $query .= "             AND L1.MAJORCD      = T1.SUC_MAJORCD ";
        $query .= "             AND L1.EXAMCOURSECD = T1.SUC_COURSECODE ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND T1.EXAMNO       = '{$examno}' ";
        return $query;
    }

    //入学金取得
    public function getPayMoney($model)
    {
        $query  = "";
        $query .= " WITH SEIKYU AS ( ";
        $query .= " SELECT ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     SUM(VALUE(PAY_ITEM.ITEM_MONEY, 0)) AS SEIKYU_MONEY ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     LEFT JOIN NAME_MST L036_SEIKYU ON L036_SEIKYU.NAMECD1 = 'L036' ";
        $query .= "          AND SUBSTR(BASE.SUC_COURSECODE, 3, 1) = L036_SEIKYU.ABBV2 ";
        $query .= "     LEFT JOIN ENTEXAM_PAYMENT_EXEMPTION_MST PAY_SEIKYU ON BASE.ENTEXAMYEAR = PAY_SEIKYU.ENTEXAMYEAR ";
        $query .= "          AND BASE.APPLICANTDIV = PAY_SEIKYU.APPLICANTDIV ";
        $query .= "          AND PAY_SEIKYU.DIV = '0' ";
        $query .= "          AND PAY_SEIKYU.KIND_CD = '2' ";
        $query .= "          AND L036_SEIKYU.NAMECD2 = PAY_SEIKYU.EXEMPTION_CD ";
        $query .= "     INNER JOIN ENTEXAM_PAYMENT_ITEM_MST PAY_ITEM ON PAY_SEIKYU.ENTEXAMYEAR = PAY_ITEM.ENTEXAMYEAR ";
        $query .= "           AND PAY_SEIKYU.APPLICANTDIV = PAY_ITEM.APPLICANTDIV ";
        $query .= "           AND PAY_ITEM.DIV = '0' ";
        $query .= "           AND PAY_SEIKYU.ITEM_CD = PAY_ITEM.ITEM_CD ";
        $query .= "           AND VALUE(PAY_ITEM.REMARK7, '0')  <> '1' ";
        $query .= " WHERE ";
        $query .= "     BASE.ENTEXAMYEAR      = '{$model->ObjYear}' ";
        $query .= "     AND BASE.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND BASE.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND BASE.SHDIV        = '{$model->shdiv}' ";
        $query .= " GROUP BY ";
        $query .= "     BASE.EXAMNO ";
        $query .= " ), GENMEN AS ( ";
        $query .= " SELECT ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     SUM(VALUE(PAY_GENMEN.EXEMPTION_MONEY, 0)) AS GENMEN_MONEY ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     LEFT JOIN NAME_MST L036_SEIKYU ON L036_SEIKYU.NAMECD1 = 'L036' ";
        $query .= "          AND SUBSTR(BASE.SUC_COURSECODE, 3, 1) = L036_SEIKYU.ABBV2 ";
        $query .= "     LEFT JOIN NAME_MST L036_GENMEN ON L036_GENMEN.NAMECD1 = 'L036' ";
        $query .= "          AND BASE.JUDGE_KIND = L036_GENMEN.ABBV3 ";
        $query .= "     LEFT JOIN ENTEXAM_PAYMENT_EXEMPTION_MST PAY_SEIKYU ON BASE.ENTEXAMYEAR = PAY_SEIKYU.ENTEXAMYEAR ";
        $query .= "          AND BASE.APPLICANTDIV = PAY_SEIKYU.APPLICANTDIV ";
        $query .= "          AND PAY_SEIKYU.DIV = '0' ";
        $query .= "          AND PAY_SEIKYU.KIND_CD = '2' ";
        $query .= "          AND L036_SEIKYU.NAMECD2 = PAY_SEIKYU.EXEMPTION_CD ";
        $query .= "     LEFT JOIN ENTEXAM_PAYMENT_EXEMPTION_MST PAY_GENMEN ON BASE.ENTEXAMYEAR = PAY_GENMEN.ENTEXAMYEAR ";
        $query .= "          AND BASE.APPLICANTDIV = PAY_GENMEN.APPLICANTDIV ";
        $query .= "          AND PAY_GENMEN.DIV = '0' ";
        $query .= "          AND PAY_GENMEN.KIND_CD = '1' ";
        $query .= "          AND L036_GENMEN.NAMECD2 = PAY_GENMEN.EXEMPTION_CD ";
        $query .= "          AND PAY_GENMEN.ITEM_CD = PAY_SEIKYU.ITEM_CD ";
        $query .= "     INNER JOIN ENTEXAM_PAYMENT_ITEM_MST PAY_ITEM ON PAY_SEIKYU.ENTEXAMYEAR = PAY_ITEM.ENTEXAMYEAR ";
        $query .= "           AND PAY_SEIKYU.APPLICANTDIV = PAY_ITEM.APPLICANTDIV ";
        $query .= "           AND PAY_ITEM.DIV = '0' ";
        $query .= "           AND PAY_SEIKYU.ITEM_CD = PAY_ITEM.ITEM_CD ";
        $query .= "           AND VALUE(PAY_ITEM.REMARK7, '0')  <> '1' ";
        $query .= " WHERE ";
        $query .= "     BASE.ENTEXAMYEAR      = '{$model->ObjYear}' ";
        $query .= "     AND BASE.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND BASE.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND BASE.SHDIV        = '{$model->shdiv}' ";
        $query .= " GROUP BY ";
        $query .= "     BASE.EXAMNO ";
        $query .= " ), KYOUDAI_GENMEN AS ( ";
        $query .= " SELECT ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     SUM(VALUE(PAY_GENMEN.EXEMPTION_MONEY, 0)) AS GENMEN_MONEY ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     LEFT JOIN NAME_MST L036_SEIKYU ON L036_SEIKYU.NAMECD1 = 'L036' ";
        $query .= "          AND SUBSTR(BASE.SUC_COURSECODE, 3, 1) = L036_SEIKYU.ABBV2 ";
        $query .= "     LEFT JOIN ENTEXAM_PAYMENT_EXEMPTION_MST PAY_SEIKYU ON BASE.ENTEXAMYEAR = PAY_SEIKYU.ENTEXAMYEAR ";
        $query .= "          AND BASE.APPLICANTDIV = PAY_SEIKYU.APPLICANTDIV ";
        $query .= "          AND PAY_SEIKYU.DIV = '0' ";
        $query .= "          AND PAY_SEIKYU.KIND_CD = '2' ";
        $query .= "          AND L036_SEIKYU.NAMECD2 = PAY_SEIKYU.EXEMPTION_CD ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD018 ON BASE.ENTEXAMYEAR = BD018.ENTEXAMYEAR ";
        $query .= "           AND BASE.APPLICANTDIV = BD018.APPLICANTDIV ";
        $query .= "           AND BASE.EXAMNO = BD018.EXAMNO ";
        $query .= "           AND BD018.SEQ = '018' ";
        $query .= "           AND BD018.REMARK1 IS NOT NULL ";
        $query .= "     LEFT JOIN ENTEXAM_PAYMENT_EXEMPTION_MST PAY_GENMEN ON BASE.ENTEXAMYEAR = PAY_GENMEN.ENTEXAMYEAR ";
        $query .= "          AND BASE.APPLICANTDIV = PAY_GENMEN.APPLICANTDIV ";
        $query .= "          AND PAY_GENMEN.DIV = '0' ";
        $query .= "          AND PAY_GENMEN.KIND_CD = '1' ";
        $query .= "          AND PAY_GENMEN.ITEM_CD = PAY_SEIKYU.ITEM_CD ";
        $query .= "     INNER JOIN ENTEXAM_PAYMENT_ITEM_MST PAY_ITEM ON PAY_SEIKYU.ENTEXAMYEAR = PAY_ITEM.ENTEXAMYEAR ";
        $query .= "           AND PAY_SEIKYU.APPLICANTDIV = PAY_ITEM.APPLICANTDIV ";
        $query .= "           AND PAY_ITEM.DIV = '0' ";
        $query .= "           AND PAY_SEIKYU.ITEM_CD = PAY_ITEM.ITEM_CD ";
        $query .= "           AND VALUE(PAY_ITEM.REMARK7, '0')  <> '1' ";
        $query .= " WHERE ";
        $query .= "     BASE.ENTEXAMYEAR      = '{$model->ObjYear}' ";
        $query .= "     AND BASE.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND BASE.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND BASE.SHDIV        = '{$model->shdiv}' ";
        $query .= "     AND PAY_GENMEN.EXEMPTION_CD IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L036' AND NAMESPARE3 = '1') ";
        $query .= " GROUP BY ";
        $query .= "     BASE.EXAMNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     BASE.SUC_COURSECODE, ";
        $query .= "     BASE.JUDGE_KIND, ";
        $query .= "     SEIKYU.SEIKYU_MONEY, ";
        $query .= "     GENMEN.GENMEN_MONEY, ";
        $query .= "     MAX(0, VALUE(SEIKYU.SEIKYU_MONEY, 0) ";
        $query .= "          - VALUE(GENMEN.GENMEN_MONEY, 0) ";
        $query .= "          - VALUE(KYOUDAI_GENMEN.GENMEN_MONEY, 0) ";
        $query .= "          - VALUE(SHITAKU.SHITAKU_TOTAL_ITEM_MONEY, 0) ";
        $query .= "     ) AS CALC_MONEY ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     LEFT JOIN SEIKYU ON BASE.EXAMNO = SEIKYU.EXAMNO ";
        $query .= "     LEFT JOIN GENMEN ON BASE.EXAMNO = GENMEN.EXAMNO ";
        $query .= "     LEFT JOIN KYOUDAI_GENMEN ON BASE.EXAMNO = KYOUDAI_GENMEN.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT S001 ON S001.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "                     AND S001.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "                     AND S001.EXAMNO       = BASE.EXAMNO ";
        $query .= "                     AND S001.SEQ          = '001' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT S021 ON S021.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "                     AND S021.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "                     AND S021.EXAMNO       = BASE.EXAMNO ";
        $query .= "                     AND S021.SEQ          = '021' ";
        $query .= "     LEFT JOIN (SELECT  ";
        $query .= "                    ENTEXAMYEAR  ";
        $query .= "                  , APPLICANTDIV  ";
        $query .= "                  , SUBSTR(ITEM_CD, 1, 1) AS COURSECD  ";
        $query .= "                  , DIV  ";
        $query .= "                  , SUM(ITEM_MONEY) AS SHITAKU_TOTAL_ITEM_MONEY  ";
        $query .= "                FROM ";
        $query .= "                    ENTEXAM_PAYMENT_ITEM_MST ";
        $query .= "                WHERE ";
        $query .= "                        DIV = '0' ";
        $query .= "                        AND REMARK5 = '1' ";
        $query .= "                GROUP BY ";
        $query .= "                    ENTEXAMYEAR  ";
        $query .= "                  , APPLICANTDIV  ";
        $query .= "                  , SUBSTR(ITEM_CD, 1, 1) ";
        $query .= "                  , DIV  ";
        $query .= "               )  SHITAKU ON BASE.ENTEXAMYEAR  = SHITAKU.ENTEXAMYEAR ";
        $query .= "                         AND BASE.APPLICANTDIV = SHITAKU.APPLICANTDIV ";
        $query .= "                     AND SHITAKU.COURSECD   = SUBSTR(BASE.SUC_COURSECODE, 3, 1) ";
        $query .= "                         AND S021.REMARK2   = '1' ";
        $query .= " WHERE ";
        $query .= "     BASE.ENTEXAMYEAR      = '{$model->ObjYear}' ";
        $query .= "     AND BASE.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND BASE.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND BASE.SHDIV        = '{$model->shdiv}' ";

        return $query;
    }

    //UPDATE
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knjl280gQuery::getPayMoney($model);
        $result = $db->query($query);
        $examMoney = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $money = $row["CALC_MONEY"];
            if ($money < 0) {
                $money = 0;
            }
            $examMoney[$row["EXAMNO"]] = $money;
        }
        $result->free();

        //対象者(左のリスト）
        $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());

        foreach ($array as $select) {
            list($examno, $proDate) = preg_split("{:}", $select);

            //手続区分・手続日・入学区分をセット
            $data = array();
            $data["PROCEDUREDIV"][TEXT]     = "1"; //手続区分 1:済み 2:無し
            //手続日は未設定のみ上書き更新する
            if ($proDate == "") {
                $data["PROCEDUREDATE"][DATE]    = $model->pro_date;
            }
            $data["PAY_MONEY"][NUMBER]      = $examMoney[$examno];
            $data["ENTDIV"][TEXT]           = "1";  //入学区分 1:済み 2:辞退
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $where .= "   AND EXAMNO        = '{$examno}' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);

            //入学コースをクリア
            $query  = " DELETE FROM ";
            $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
            $query .= " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $query .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $query .= "   AND EXAMNO        = '{$examno}' ";
            $query .= "   AND SEQ           = '007' ";
            $db->query($query);
            //入学コースをセット
            $getEnter = array();
            $getEnter = $db->getRow(knjl280gQuery::getEnterCourse($model, $examno), DB_FETCHMODE_ASSOC);
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["EXAMNO"][TEXT]           = $examno;
            $data["SEQ"][TEXT]              = '007';
            $data["REMARK1"][TEXT]          = $getEnter["ENTER_COURSECD"];
            $data["REMARK2"][TEXT]          = $getEnter["ENTER_MAJORCD"];
            $data["REMARK3"][TEXT]          = $getEnter["ENTER_COURSECODE"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            $db->query($query);
        }


        //合格者(右のリスト)
        $array2 = (strlen($model->selectdata2) ? explode(",", $model->selectdata2) : array());

        foreach ($array2 as $select) {
            list($examno, $proDate) = preg_split("{:}", $select);

            //手続区分・手続日・入学区分をクリア
            $data = array();
            $data["PROCEDUREDIV"][TEXT]     = ""; //手続区分 1:済み 2:無し
            $data["PROCEDUREDATE"][DATE]    = "";
            $data["ENTDIV"][TEXT]           = ""; //入学区分 1:済み 2:辞退
            $data["PAY_MONEY"][NUMBER]      = ""; //入金
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $where .= "   AND EXAMNO        = '{$examno}' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);

            //入学コースをクリア
            $query  = " DELETE FROM ";
            $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
            $query .= " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $query .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $query .= "   AND EXAMNO        = '{$examno}' ";
            $query .= "   AND SEQ           = '007' ";
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }
}
