<?php

require_once('for_php7.php');

class knjl414hQuery extends Query
{

    //名称マスタ
    public function getNameMst($model, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->year."' ";
        $query .= " AND NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    //設定マスタ
    public function getSettingMst($model, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     SEQ AS VALUE, ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST ";
        $query .= " WHERE ";
        $query .= "     SETTING_CD = '{$namecd1}'  ";
        $query .= "     AND ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";

        return $query;
    }

    //設定マスタL101
    public function getSettingMstl101($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SEQ AS VALUE, ";
        $query .= "     T1.SEQ || ':' || T1.NAME1 || '@' || T2.EXAMCOURSE_ABBV AS LABEL, ";
        $query .= "     T1.NAMESPARE1, ";
        $query .= "     T1.NAME2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST T1 ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST T2 ";
        $query .= "       ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "      AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "      AND T2.TESTDIV = '0' ";
        $query .= "      AND T2.EXAMCOURSECD = T1.NAMESPARE1 ";
        $query .= " WHERE ";
        $query .= "     T1.SETTING_CD = 'L101'  ";
        $query .= "     AND T1.ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.NAME2 DESC ";

        return $query;
    }

    //一覧取得
    public function getExamnoList($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     B0.EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT B0 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "         ON B1.ENTEXAMYEAR = B0.ENTEXAMYEAR ";
        $query .= "        AND B1.APPLICANTDIV = B0.APPLICANTDIV ";
        $query .= "        AND B1.EXAMNO = B0.EXAMNO ";
        $query .= " WHERE ";
        $query .= "     B0.ENTEXAMYEAR       ='{$model->year}' ";
        $query .= "     AND B0.APPLICANTDIV  ='{$model->applicantdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     EXAMNO ";

        return $query;
    }

    //一覧取得
    public function getNaisinKijyun($model)
    {
        $query  = " SELECT ";
        $query .= "   COURSECD, ";
        $query .= "   MAJORCD, ";
        $query .= "   EXAMCOURSECD, ";
        $query .= "   SHDIV, ";
        $query .= "   BORDER1, ";
        $query .= "   BORDER2, ";
        $query .= "   BORDER3, ";
        $query .= "   BORDER4 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SPECIAL_SCHOLARSHIP_BORDER_MST ";
        $query .= " WHERE ";
        $query .= "   ENTEXAMYEAR = '{$model->year}' ";
        $query .= "   AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= " ORDER BY ";
        $query .= "   COURSECD, ";
        $query .= "   MAJORCD, ";
        $query .= "   EXAMCOURSECD, ";
        $query .= "   CASE WHEN SHDIV = '3' THEN 0 ELSE 1 END ASC, ";
        $query .= "   SHDIV ";

        return $query;
    }

    //一覧取得
    public function getList($model, $noLimitFlg = false)
    {
        $query  = " WITH MAKE_EXAMKEY AS ( ";
        $query .= "   SELECT DISTINCT ";
        $query .= "     B1.ENTEXAMYEAR, ";
        $query .= "     B1.APPLICANTDIV, ";
        $query .= "     B1.EXAMNO, ";
        $query .= "     B1.NAME ";
        $query .= "   FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT B0 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "         ON B1.ENTEXAMYEAR = B0.ENTEXAMYEAR ";
        $query .= "        AND B1.APPLICANTDIV = B0.APPLICANTDIV ";
        $query .= "        AND B1.EXAMNO = B0.EXAMNO ";
        $query .= "   WHERE ";
        $query .= "       B0.ENTEXAMYEAR       = '{$model->year}' ";
        $query .= "       AND B0.APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "       AND B0.RECEPTNO      >= '{$model->s_receptno}' ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     B1.EXAMNO, ";
        $query .= "     B1.NAME, ";
        $query .= "     CD2.REMARK1 AS CD2REMARK1, ";
        $query .= "     CD2.REMARK2 AS CD2REMARK2, ";
        $query .= "     CD2.REMARK3 AS CD2REMARK3, ";
        $query .= "     CD2.REMARK4 AS CD2REMARK4, ";
        $query .= "     CD2.REMARK7 AS CD2REMARK7, ";
        $query .= "     CD3.REMARK1 AS CD3REMARK1, ";
        $query .= "     CD3.REMARK2 AS CD3REMARK2, ";
        $query .= "     CD4.REMARK1 AS CD4REMARK1, ";
        $query .= "     CD4.REMARK2 AS CD4REMARK2, ";
        $query .= "     CD4.REMARK3 AS CD4REMARK3, ";
        $query .= "     ECM1.EXAMCOURSE_ABBV AS CD4REMARKX, ";
        $query .= "     M1.SEQ || ':' ||M1.NAME1 AS CD4REMARK4 ";
        $query .= " FROM ";
        $query .= "     MAKE_EXAMKEY B1 ";
        $query .= " LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT C1 ";
        $query .= "     ON C1.ENTEXAMYEAR   = B1.ENTEXAMYEAR ";
        $query .= "     AND C1.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "     AND C1.EXAMNO       = B1.EXAMNO ";
        $query .= " LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CD2 ";
        $query .= "     ON CD2.ENTEXAMYEAR   = B1.ENTEXAMYEAR ";
        $query .= "     AND CD2.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "     AND CD2.EXAMNO       = B1.EXAMNO ";
        $query .= "     AND CD2.SEQ          = '002' ";
        $query .= " LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CD3 ";
        $query .= "     ON CD3.ENTEXAMYEAR   = B1.ENTEXAMYEAR ";
        $query .= "     AND CD3.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "     AND CD3.EXAMNO       = B1.EXAMNO ";
        $query .= "     AND CD3.SEQ          = '003' ";
        $query .= " LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CD4 ";
        $query .= "     ON CD4.ENTEXAMYEAR   = B1.ENTEXAMYEAR ";
        $query .= "     AND CD4.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "     AND CD4.EXAMNO       = B1.EXAMNO ";
        $query .= "     AND CD4.SEQ          = '004' ";
        $query .= " LEFT JOIN ENTEXAM_SETTING_MST M1 ";
        $query .= "     ON M1.SEQ = CD4.REMARK4 ";
        $query .= "     AND M1.ENTEXAMYEAR   = CD4.ENTEXAMYEAR ";
        $query .= "     AND M1.APPLICANTDIV  = CD4.APPLICANTDIV ";
        $query .= "     AND M1.SETTING_CD       = 'L101' ";
        $query .= "     AND M1.SEQ = CD4.REMARK4";
        $query .= " LEFT JOIN ENTEXAM_COURSE_MST ECM1 ";
        $query .= "     ON ECM1.ENTEXAMYEAR = M1.ENTEXAMYEAR ";
        $query .= "     AND ECM1.APPLICANTDIV = M1.APPLICANTDIV ";
        $query .= "     AND ECM1.TESTDIV = '0' ";
        $query .= "     AND ECM1.EXAMCOURSECD = M1.NAMESPARE1 ";
        $query .= " ORDER BY ";
        $query .= "     EXAMNO ";
        if (!$noLimitFlg) {
            $query .= " FETCH FIRST 50 ROWS ONLY ";
        }

        return $query;
    }

    public function getCommitteeFreq($model)
    {
        $query  = " SELECT ";
        $query .= "   BD005.EXAMNO, ";
        $query .= "   BD005.REMARK2 AS BDSEQ5R2, ";  // 生徒会(フラグ)
        $query .= "   BD005.REMARK3 AS BDSEQ5R3, ";  // 資格(フラグ)
        $query .= "   L072.NAME1 AS BDSEQ5R4, ";  // 資格コード
        $query .= "   L073.NAME2 AS BDSEQ5R5 ";   // 資格レベルコード
        $query .= " FROM ";
        $query .= "   ENTEXAM_APPLICANTBASE_DETAIL_DAT BD005 ";
        $query .= "   LEFT JOIN ENTEXAM_SETTING_MST L072 ";
        $query .= "     ON L072.ENTEXAMYEAR = BD005.ENTEXAMYEAR";
        $query .= "    AND L072.APPLICANTDIV = BD005.APPLICANTDIV ";
        $query .= "    AND L072.SETTING_CD = 'L072' ";
        $query .= "    AND L072.NAME1 = BD005.REMARK4";
        $query .= "   LEFT JOIN ENTEXAM_SETTING_MST L073 ";
        $query .= "     ON L073.ENTEXAMYEAR = BD005.ENTEXAMYEAR";
        $query .= "    AND L073.APPLICANTDIV = BD005.APPLICANTDIV ";
        $query .= "    AND L073.SETTING_CD = 'L073' ";
        $query .= "    AND L073.NAME1 = BD005.REMARK4";  //名称M.H312の数値で登録。L072/L073には合格とするコードだけが登録される。
        $query .= "    AND L073.NAME2 = BD005.REMARK5";
        $query .= " WHERE ";
        $query .= "   BD005.ENTEXAMYEAR      ='{$model->year}' ";
        $query .= "   AND BD005.APPLICANTDIV ='{$model->applicantdiv}' ";
        $query .= "   AND BD005.EXAMNO IN ('" . implode("','", $model->examnos) . "') ";
        $query .= "   AND BD005.SEQ          = '005' ";
        return $query;
    }

    //出願内容の取得
    public function getHopeInfo($model)
    {
        $query  = " SELECT ";
        $query .= "     R1.TESTDIV, ";
        $query .= "     R1.EXAM_TYPE, ";
        $query .= "     R1.RECEPTNO, ";
        $query .= "     R1.EXAMNO, ";
        $query .= "     CASE WHEN COALESCE(RD016.REMARK1, RD017.REMARK1, RD018.REMARK1) IS NOT NULL ";
        $query .= "          THEN MIN(COALESCE(RD016.REMARK1, '999'), COALESCE(RD017.REMARK1, '999'), COALESCE(RD018.REMARK1, '999')) ";
        $query .= "          ELSE NULL ";
        $query .= "          END AS MNCODE, ";
        $query .= "     RD016.REMARK1 AS HOPE_COURSE1, ";
        $query .= "     RD016.REMARK2 AS HOPE_TESTDIV1, ";
        $query .= "     RD017.REMARK1 AS HOPE_COURSE2, ";
        $query .= "     RD017.REMARK2 AS HOPE_TESTDIV2, ";
        $query .= "     RD018.REMARK1 AS HOPE_COURSE3, ";
        $query .= "     RD018.REMARK2 AS HOPE_TESTDIV3 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT R1 ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD016 ";
        $query .= "            ON RD016.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "           AND RD016.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "           AND RD016.TESTDIV      = R1.TESTDIV ";
        $query .= "           AND RD016.EXAM_TYPE    = R1.EXAM_TYPE ";
        $query .= "           AND RD016.RECEPTNO     = R1.RECEPTNO ";
        $query .= "           AND RD016.SEQ          = '016' ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD017 ";
        $query .= "            ON RD017.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "           AND RD017.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "           AND RD017.TESTDIV      = R1.TESTDIV ";
        $query .= "           AND RD017.EXAM_TYPE    = R1.EXAM_TYPE ";
        $query .= "           AND RD017.RECEPTNO     = R1.RECEPTNO ";
        $query .= "           AND RD017.SEQ          = '017' ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD018 ";
        $query .= "            ON RD018.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "           AND RD018.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "           AND RD018.TESTDIV      = R1.TESTDIV ";
        $query .= "           AND RD018.EXAM_TYPE    = R1.EXAM_TYPE ";
        $query .= "           AND RD018.RECEPTNO     = R1.RECEPTNO ";
        $query .= "           AND RD018.SEQ          = '018' ";
        $query .= " WHERE ";
        $query .= "       R1.ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "   AND R1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "   AND COALESCE(RD016.REMARK1, RD017.REMARK1, RD018.REMARK1) IS NOT NULL ";
        return $query;
    }

    public function getExamNo($model, $receptno)
    {
        $query  = " SELECT ";
        $query .= "   MIN(T1.EXAMNO) AS EXAMNO ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_RECEPT_DAT T1 ";
        $query .= "   INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "      ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "     AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "     AND T2.EXAMNO = T1.EXAMNO ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR         = '{$model->year}' ";
        $query .= "     AND T1.APPLICANTDIV    = '{$model->applicantdiv}' ";
        $query .= "     AND T1.RECEPTNO        >= '{$receptno}' ";
        return $query;
    }

    public function getReceptNo($model, $examno)
    {
        $query  = " SELECT ";
        $query .= "   MIN(T1.RECEPTNO) AS RECEPTNO ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_RECEPT_DAT T1 ";
        $query .= "   INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "      ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "     AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "     AND T2.EXAMNO = T1.EXAMNO ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR         = '{$model->year}' ";
        $query .= "     AND T1.APPLICANTDIV    = '{$model->applicantdiv}' ";
        $query .= "     AND T1.EXAMNO          = '{$examno}' ";
        return $query;
    }

    //UPDATE
    public function &getUpdateQuery($model, $inputData)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($inputData as $examno => $row) {
            $where  = " WHERE ";
            $where .= "     EXAMNO='".$examno."' ";
            $where .= "     AND ENTEXAMYEAR='".$model->year."' ";
            $where .= "     AND APPLICANTDIV='".$model->applicantdiv."' ";
            $data = array();
            //入力情報(内申以外は結果もセットで登録?)
            if ($model->radio == '1') {
                $data["REMARK1"][TEXT]  = $row['CD2REMARK1'];  //内申5科(中学3年間)
                $data["REMARK2"][TEXT]  = $row['CD2REMARK2'];  //内申5科(中学3年)
                $data["REMARK3"][TEXT]  = $row['CD2REMARK3'];  //内申9科(中学3年)
                $data["REMARK4"][TEXT]  = $row['CD2REMARK4'];  //強化クラブ
                $data["REMARK7"][TEXT]  = $row['CD2REMARK7'];  //国数英に2あり
                $data["SEQ"][TEXT]      = '002';
                $where .= "     AND SEQ='002' ";
            } elseif ($model->radio == '2') {
                $data["REMARK1"][TEXT] = $row['CD3REMARK1'];   //模試コード
                $data["REMARK2"][TEXT] = $row['CD3REMARK2'];   //入力値
                $data["REMARK10"][TEXT] = $row['CD3REMARK10']; //判定結果コース
                $data["REMARK11"][TEXT] = $row['CD3REMARK11']; //判定結果出願区分
                $data["REMARK12"][TEXT] = $row['CD3REMARK12']; //特別奨学生
                $data["SEQ"][TEXT]     = '003';
                $where .= "     AND SEQ='003' ";
            } elseif ($model->radio == '3') {
                $data["REMARK1"][TEXT] = $row['CD4REMARK1'];   //OPT入力値1
                $data["REMARK2"][TEXT] = $row['CD4REMARK2'];   //OPT入力値2
                $data["REMARK3"][TEXT] = $row['CD4REMARK3'];   //OPT入力値3
                $data["REMARK4"][TEXT] = $row['CD4REMARK4'];   //判定値
                $data["REMARK10"][TEXT] = $row['CD4REMARK10']; //判定結果コース
                $data["REMARK11"][TEXT] = $row['CD4REMARK11']; //判定結果出願区分
                $data["REMARK12"][TEXT] = $row['CD4REMARK12']; //特別奨学生
                $data["SEQ"][TEXT]     = '004';
                $where .= "     AND SEQ='004' ";
            }
            $data["REGISTERCD"][TEXT] = STAFFCD;
            $data["UPDATED"][NUMBER]  = "sysdate()";

            if ($db->getOne("SELECT COUNT(*) FROM ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT ".$where) == '0') {
                $data['EXAMNO'][TEXT]       = $examno;
                $data['ENTEXAMYEAR'][TEXT]  = $model->year;
                $data['APPLICANTDIV'][TEXT] = $model->applicantdiv;
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
            }
            $db->query($query);
            //判定情報(内申のみ)
            if ($model->radio == '1') {
                $where  = " WHERE ";
                $where .= "     EXAMNO='".$examno."' ";
                $where .= "     AND ENTEXAMYEAR='".$model->year."' ";
                $where .= "     AND APPLICANTDIV='".$model->applicantdiv."' ";
                $data = array();
                $data["REMARK1"][TEXT] = $row['CD5REMARK1']; //内諾仮決定コース
                $data["REMARK2"][TEXT] = $row['CD5REMARK2']; //内諾仮決定出願区分
                $data["REMARK3"][TEXT] = $row['CD5REMARK3']; //特別奨学生仮判定
                $data["REMARK5"][TEXT] = $row['CD5REMARK5']; //強化クラブ仮決定コース
                $data["REMARK6"][TEXT] = $row['CD5REMARK6']; //強化クラブ仮決定出願区分
                $data["SEQ"][TEXT]      = '005';
                $where .= "     AND SEQ='005' ";
                if ($db->getOne("SELECT COUNT(*) FROM ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT ".$where) == '0') {
                    $data['EXAMNO'][TEXT]       = $examno;
                    $data['ENTEXAMYEAR'][TEXT]  = $model->year;
                    $data['APPLICANTDIV'][TEXT] = $model->applicantdiv;
                    $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
                } else {
                    $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
                }
                $db->query($query);
            }
        }
        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    //DELETE
    public function &getDeleteQuery($model, $inputData)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($inputData as $examno => $row) {
            $where  = " WHERE ";
            $where .= "     EXAMNO='".$examno."' ";
            $where .= "     AND ENTEXAMYEAR='".$model->year."' ";
            $where .= "     AND APPLICANTDIV='".$model->applicantdiv."' ";
            $data = array();
            if ($model->radio == '1') {
                $data["REMARK1"][TEXT]  = null;
                $data["REMARK2"][TEXT]  = null;
                $data["REMARK3"][TEXT]  = null;
                $data["REMARK4"][TEXT]  = null;
                $data["REMARK5"][TEXT]  = null;
                $data["REMARK6"][TEXT]  = null;
                $data["REMARK7"][TEXT]  = null;
                $data["SEQ"][TEXT]            = '002';
                $where .= "     AND SEQ='002' ";
            } elseif ($model->radio == '2') {
                $data["REMARK1"][TEXT] = null;
                $data["REMARK2"][TEXT] = null;
                $data["SEQ"][TEXT]     = '003';
                $where .= "     AND SEQ='003' ";
            } elseif ($model->radio == '3') {
                $data["REMARK1"][TEXT] = null;
                $data["REMARK2"][TEXT] = null;
                $data["REMARK3"][TEXT] = null;
                $data["REMARK4"][TEXT] = null;
                $data["SEQ"][TEXT]     = '004';
                $where .= "     AND SEQ='004' ";
            }
            $data["REGISTERCD"][TEXT] = STAFFCD;
            $data["UPDATED"][NUMBER]  = "sysdate()";

            if ($db->getOne("SELECT COUNT(*) FROM ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT ".$where) == '0') {
                $data['EXAMNO'][TEXT]       = $examno;
                $data['ENTEXAMYEAR'][TEXT]  = $model->year;
                $data['APPLICANTDIV'][TEXT] = $model->applicantdiv;
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
            }
            $db->query($query);
            if ($model->radio == '1') {
                $where  = " WHERE ";
                $where .= "     EXAMNO='".$examno."' ";
                $where .= "     AND ENTEXAMYEAR='".$model->year."' ";
                $where .= "     AND APPLICANTDIV='".$model->applicantdiv."' ";
                $data = array();
                $data["REMARK1"][TEXT]  = null;
                $data["REMARK2"][TEXT]  = null;
                $data["REMARK3"][TEXT]  = null;
                $data["REMARK4"][TEXT]  = null;
                $data["REMARK5"][TEXT]  = null;
                $data["REMARK6"][TEXT]  = null;
                $data["SEQ"][TEXT]      = '005';
                $where .= "     AND SEQ='005' ";
                if ($db->getOne("SELECT COUNT(*) FROM ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT ".$where) == '0') {
                    $data['EXAMNO'][TEXT]       = $examno;
                    $data['ENTEXAMYEAR'][TEXT]  = $model->year;
                    $data['APPLICANTDIV'][TEXT] = $model->applicantdiv;
                    $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
                } else {
                    $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
                }
                $db->query($query);
            }
        }
        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
