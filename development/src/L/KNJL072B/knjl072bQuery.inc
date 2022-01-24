<?php

require_once('for_php7.php');

class knjl072bQuery extends Query {

    //名称マスタより取得
    function getNameMst($namecd1, $year, $isShdiv = false) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //判定区分
    function getJudgmentDiv($model, $judgment_div = "") {
        //変数
        $isIppan = ($model->testdiv == "2") ? true : false;
        //SQL
        $query  = " SELECT ";
        $query .= "     JUDGMENT_DIV || ':' || JUDGMENT_COURSE_NAME AS LABEL, ";
        $query .= "     JUDGMENT_DIV AS VALUE, ";
        //一般入試(単切)
        $query .= "     CHANGE_SINGLE_TEST_FLG, ";
        //合格コース(正規)
        $query .= "     NORMAL_PASSCOURSECD, ";
        $query .= "     NORMAL_PASSMAJORCD, ";
        $query .= "     NORMAL_PASSEXAMCOURSECD, ";
        //合格コース(単切)
        $query .= "     EARLY_PASSCOURSECD, ";
        $query .= "     EARLY_PASSMAJORCD, ";
        $query .= "     EARLY_PASSEXAMCOURSECD ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_JUDGMENT_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' ";
        if (strlen($judgment_div)) {
            $query .= "     AND JUDGMENT_DIV = '{$judgment_div}' ";
        }
        if ($isIppan) {
            $query .= "     AND (TAKE_GENERAL_TEST_FLG = '1' ";  //一般入試(正規)
            $query .= "     OR CHANGE_SINGLE_TEST_FLG = '1' "; //一般入試(単切)
            $query .= "     OR JUDGMENT_DIV = '20') "; //不合格
        } else {
            $query .= "     AND (TAKE_RECOMMEND_TEST_FLG = '1' ";//推薦入試(正規)
            $query .= "     OR JUDGMENT_DIV = '20') "; //不合格
        }
        //抽出・変更合否区分コンボのリストには、特進選抜コースは表示しない
        $query .= "     AND JUDGMENT_DIV NOT IN ('13','14','15') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //シミュレーションSQL
    function simSql($model, $jdgRow) {
        //変数
        $staffcd = STAFFCD;
        $isIppan = ($model->testdiv == "2") ? true : false;
        //SQL
        $query  = " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.EXAM_TYPE, ";
        $query .= "     T1.RECEPTNO, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.ATTEND_ALL_FLG, ";
        $query .= "     T1.PROCEDUREDIV1, ";    //手続区分
        $query .= "     T2.PROCEDUREDIV, ";     //手続区分(最終)
        $query .= "     T1.AVARAGE4, "; //成績
        $query .= "     T1.TOTAL_RANK4, "; //順位
        $query .= "     T2.NAME, "; //氏名
        $query .= "     T1.JUDGEDIV, ";
        $query .= "     CASE WHEN N1.NAMECD2 = '2' AND L4.JUDGMENT_DIV IS NULL AND L5.JUDGMENT_DIV = '08' ";
        $query .= "          THEN N1.NAMECD2 || ':' || '条件付合格' ";
        $query .= "          ELSE N1.NAMECD2 || ':' || N1.NAME1 ";
        $query .= "          END AS JUDGEDIV_NAME, "; //合否判定
        $query .= "     L4.JUDGMENT_DIV || ':' || L4.JUDGMENT_COURSE_NAME AS NORMAL_COURSE_NAME, "; //正規合格コース
        $query .= "     L5.JUDGMENT_DIV || ':' || L5.JUDGMENT_COURSE_NAME AS EARLY_COURSE_NAME, "; //単願切替合格コース
        $query .= "     L3.JUDGMENT_DIV || ':' || L3.PROMISE_COURSE_ABBV AS PROMISE_COURSE_ABBV "; //確約区分
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "         ON  T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.EXAMNO = T1.EXAMNO ";
                        //合否判定
        $query .= "     LEFT JOIN NAME_MST N1 ";
        $query .= "         ON  N1.NAMECD1 = 'L013' ";
        $query .= "         AND N1.NAMECD2 = T1.JUDGEDIV ";
                        //正規判定結果データ
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT L6 ";
        $query .= "         ON  L6.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND L6.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND L6.TESTDIV = T1.TESTDIV ";
        $query .= "         AND L6.EXAM_TYPE = T1.EXAM_TYPE ";
        $query .= "         AND L6.RECEPTNO = T1.RECEPTNO ";
        $query .= "         AND L6.SEQ = '001' ";
                        //単願切替判定結果データ
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT L2 ";
        $query .= "         ON  L2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND L2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND L2.TESTDIV = T1.TESTDIV ";
        $query .= "         AND L2.EXAM_TYPE = T1.EXAM_TYPE ";
        $query .= "         AND L2.RECEPTNO = T1.RECEPTNO ";
        $query .= "         AND L2.SEQ = '002' ";
                        //確約
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT L1 ";
        $query .= "         ON  L1.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "         AND L1.EXAMNO = T2.EXAMNO ";
        $query .= "         AND L1.SEQ = '004' ";
                        //合否詳細区分マスタ--------------------------
                        //正規合格コース
        $query .= "     LEFT JOIN ENTEXAM_COURSE_JUDGMENT_MST L4 ";
        $query .= "         ON  L4.ENTEXAMYEAR = L6.ENTEXAMYEAR ";
        $query .= "         AND L4.NORMAL_PASSCOURSECD = L6.REMARK1 ";
        $query .= "         AND L4.NORMAL_PASSMAJORCD = L6.REMARK2 ";
        $query .= "         AND L4.NORMAL_PASSEXAMCOURSECD = L6.REMARK3 ";
        if ($isIppan) {
            $query .= "     AND L4.TAKE_GENERAL_TEST_FLG = '1' ";  //一般入試(正規)
        } else {
            $query .= "     AND L4.TAKE_RECOMMEND_TEST_FLG = '1' ";//推薦入試(正規)
        }
                        //単願切替合格コース
        $query .= "     LEFT JOIN ENTEXAM_COURSE_JUDGMENT_MST L5 ";
        $query .= "         ON  L5.ENTEXAMYEAR = L2.ENTEXAMYEAR ";
        $query .= "         AND L5.EARLY_PASSCOURSECD = L2.REMARK1 ";
        $query .= "         AND L5.EARLY_PASSMAJORCD = L2.REMARK2 ";
        $query .= "         AND L5.EARLY_PASSEXAMCOURSECD = L2.REMARK3 ";
        if ($isIppan) {
            $query .= "     AND L5.CHANGE_SINGLE_TEST_FLG = '1' "; //一般入試(単切)
        } else {
            $query .= "     AND L5.CHANGE_SINGLE_TEST_FLG = '1' "; //推薦入試(単切)はないので、ありえないコード指定しておく。
        }
                        //確約略称名
        $query .= "     LEFT JOIN ENTEXAM_COURSE_JUDGMENT_MST L3 ";
        $query .= "         ON  L3.ENTEXAMYEAR = L1.ENTEXAMYEAR ";
        $query .= "         AND L3.JUDGMENT_DIV = L1.REMARK8 ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '{$model->testdiv}' ";
        if (strlen($model->special_reason_div)) {
            $query .= "     AND T2.SPECIAL_REASON_DIV IS NOT NULL ";
        } else {
            $query .= "     AND VALUE(T1.JUDGEDIV, '0') <> '3' "; //3:未受験(欠席)は対象外
        }
        //抽出合否区分
        if ($model->judgment_div_search == "20") {
            //不合格
            $query .= "     AND T1.JUDGEDIV = '2' ";
            $query .= "     AND VALUE(L5.JUDGMENT_DIV, '99') = '99' "; //単願切換合格コースのある人は除く
        } else if (strlen($model->judgment_div_search) && $jdgRow["CHANGE_SINGLE_TEST_FLG"] != '1') {
            //正規合格コース
            $query .= "     AND VALUE(L4.JUDGMENT_DIV, '99') = '{$model->judgment_div_search}' ";
            $query .= "     AND VALUE(L5.JUDGMENT_DIV, '99') = '99' "; //単願切換合格コースのある人は除く
        } else if (strlen($model->judgment_div_search) && $jdgRow["CHANGE_SINGLE_TEST_FLG"] == '1') {
            //単願切換合格コース
            $query .= "     AND VALUE(L5.JUDGMENT_DIV, '99') = '{$model->judgment_div_search}' ";
        }
        //抽出平均点
        if (strlen($model->avg_from)) {
            $query .= "     AND T1.AVARAGE4 >= {$model->avg_from} ";
        }
        if (strlen($model->avg_to)) {
            $query .= "     AND T1.AVARAGE4 <  {$model->avg_to} ";
        }
        $query .= " ORDER BY ";
        //表示順序ラジオボタン 1:成績順 2:受験番号順
        if ($model->sort == "1") {
            $query .= "     T1.TOTAL_RANK4, "; //順位
        }
        $query .= "     T1.EXAMNO ";

        return $query;
    }

    function decision($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //合否詳細区分マスタ
        $query = knjl072bQuery::getJudgmentDiv($model, $model->judgment_div);
        $jdgRow = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //チェックありの人を処理
        foreach ($this->field["CHK_DATA"] as $key => $val) {
            $tmp = explode("-", $val);
            $receptno = $tmp[0];
            $examno = $tmp[1];

            //基礎データ
            knjl072bQuery::updBase($db, $model, $jdgRow, $examno);
            //受付データ
            knjl072bQuery::updRecept($db, $model, $jdgRow, $receptno);
            //受付詳細データ SEQ'001'
            knjl072bQuery::delReceptDetail($db, $model, $receptno, '001');
            knjl072bQuery::insReceptDetail1($db, $model, $jdgRow, $receptno, '001');
            //受付詳細データ SEQ'002'
            knjl072bQuery::delReceptDetail($db, $model, $receptno, '002');
            //一般入試(単切)
            if ($jdgRow["CHANGE_SINGLE_TEST_FLG"] == "1") {
                knjl072bQuery::insReceptDetail2($db, $model, $jdgRow, $receptno, '002');
                //基礎データ(単願合格コースで上書き)
                knjl072bQuery::updBaseTangan($db, $model, $jdgRow, $examno);
            }
        }

        $db->commit(); 
        Query::dbCheckIn($db);

        $rtnMsg  = "\\n" . "【" . $jdgRow["LABEL"] . "】に " .get_count($this->field["CHK_DATA"]) . " 名 " . "\\n\\n";
        if ($model->judgment_div == "20") {
            $rtnMsg .= "不合格";
        } else {
            $rtnMsg .= "合格";
        }
        $rtnMsg .= "に変更しました。";
        return $rtnMsg;
    }

    //基礎データ
    function updBase($db, $model, $jdgRow, $examno) {
        $data = array();
        $data["SUC_COURSECD"][TEXT]     = $jdgRow["NORMAL_PASSCOURSECD"];
        $data["SUC_MAJORCD"][TEXT]      = $jdgRow["NORMAL_PASSMAJORCD"];
        $data["SUC_COURSECODE"][TEXT]   = $jdgRow["NORMAL_PASSEXAMCOURSECD"];
        $data["JUDGEMENT"][TEXT]        = strlen($jdgRow["NORMAL_PASSEXAMCOURSECD"]) ? "1" : "2";
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        //条件
        $where  = " WHERE ";
        $where .= "         ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $where .= "     AND EXAMNO       = '{$examno}' ";
        //更新
        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
        $db->query($query);
    }

    //基礎データ(単願合格コースで上書き)
    function updBaseTangan($db, $model, $jdgRow, $examno) {
        $data = array();
        $data["SUC_COURSECD"][TEXT]     = $jdgRow["EARLY_PASSCOURSECD"];
        $data["SUC_MAJORCD"][TEXT]      = $jdgRow["EARLY_PASSMAJORCD"];
        $data["SUC_COURSECODE"][TEXT]   = $jdgRow["EARLY_PASSEXAMCOURSECD"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        //条件
        $where  = " WHERE ";
        $where .= "         ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $where .= "     AND EXAMNO       = '{$examno}' ";
        $where .= "     AND JUDGEMENT    = '1' ";
        //更新
        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
        $db->query($query);
    }

    //受付データ
    function updRecept($db, $model, $jdgRow, $receptno) {
        $data = array();
        $data["JUDGEDIV"][TEXT]         = strlen($jdgRow["NORMAL_PASSEXAMCOURSECD"]) ? "1" : "2";
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        //条件
        $where  = " WHERE ";
        $where .= "         ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $where .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $where .= "     AND TESTDIV      = '{$model->testdiv}' ";
        $where .= "     AND EXAM_TYPE    = '{$model->exam_type}' ";
        $where .= "     AND RECEPTNO     = '{$receptno}' ";
        //更新
        $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
        $db->query($query);
    }

    //受付詳細データ
    function delReceptDetail($db, $model, $receptno, $seq) {
        //削除
        $query  = " DELETE FROM ENTEXAM_RECEPT_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND EXAM_TYPE    = '{$model->exam_type}' ";
        $query .= "     AND RECEPTNO     = '{$receptno}' ";
        $query .= "     AND SEQ          = '{$seq}' ";
        $db->query($query);
    }

    //受付詳細データ001
    function insReceptDetail1($db, $model, $jdgRow, $receptno, $seq) {
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
        $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
        $data["TESTDIV"][TEXT]          = $model->testdiv;
        $data["EXAM_TYPE"][TEXT]        = $model->exam_type;
        $data["RECEPTNO"][TEXT]         = $receptno;
        $data["SEQ"][TEXT]              = $seq;
        $data["REMARK1"][TEXT]          = $jdgRow["NORMAL_PASSCOURSECD"];
        $data["REMARK2"][TEXT]          = $jdgRow["NORMAL_PASSMAJORCD"];
        $data["REMARK3"][TEXT]          = $jdgRow["NORMAL_PASSEXAMCOURSECD"];
        $data["REMARK4"][TEXT]          = strlen($jdgRow["NORMAL_PASSEXAMCOURSECD"]) ? "1" : "2";
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        //追加
        $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT");
        $db->query($query);
    }

    //受付詳細データ002
    function insReceptDetail2($db, $model, $jdgRow, $receptno, $seq) {
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
        $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
        $data["TESTDIV"][TEXT]          = $model->testdiv;
        $data["EXAM_TYPE"][TEXT]        = $model->exam_type;
        $data["RECEPTNO"][TEXT]         = $receptno;
        $data["SEQ"][TEXT]              = $seq;
        $data["REMARK1"][TEXT]          = $jdgRow["EARLY_PASSCOURSECD"];
        $data["REMARK2"][TEXT]          = $jdgRow["EARLY_PASSMAJORCD"];
        $data["REMARK3"][TEXT]          = $jdgRow["EARLY_PASSEXAMCOURSECD"];
        $data["REMARK4"][TEXT]          = "1";
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        //追加
        $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT");
        $db->query($query);
    }
}
?>