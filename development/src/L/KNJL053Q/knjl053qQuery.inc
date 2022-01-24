<?php

require_once('for_php7.php');

class knjl053qQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($namecd1, $year)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd1 == "L003") {
            $query .= "     AND NAMESPARE3 = '".SCHOOLKIND."' ";
        }
        if ($namecd1 == "L004") {
            $query .= "     AND NAMECD2 IN ('3', '4', '5') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験科目
    public function getTestSubclasscd($model)
    {
        $name = (SCHOOLKIND == "J") ? "NAME2" : "NAME1";

        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     {$name} AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '{$model->ObjYear}' AND ";
        $query .= "     NAMECD1     = 'L009' AND ";
        $query .= "     {$name} IS NOT NULL AND ";
        //満点マスタの設定が前提
        $query .= "     NAMECD2 IN (SELECT ";
        $query .= "                     TESTSUBCLASSCD ";
        $query .= "                 FROM ";
        $query .= "                     ENTEXAM_PERFECT_MST ";
        $query .= "                 WHERE ";
        $query .= "                     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "                     APPLICANTDIV    = '{$model->applicantdiv}' AND ";
        $query .= "                     TESTDIV         = '{$model->testdiv}' AND ";
        $query .= "                     TESTSUBCLASSCD  <> '6' "; //小論文は除く
        $query .= "                 ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    public function selectQuery($model)
    {

        $query  = " WITH T_PERFECT AS (";
        $query .= "     SELECT ";
        $query .= "         ENTEXAMYEAR, ";
        $query .= "         APPLICANTDIV, ";
        $query .= "         TESTDIV, ";
        $query .= "         TESTSUBCLASSCD, ";
        $query .= "         MAX(PERFECT) AS PERFECT ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_PERFECT_MST ";
        $query .= "     WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->ObjYear}' AND ";
        $query .= "         APPLICANTDIV = '{$model->applicantdiv}' AND ";
        $query .= "         TESTDIV      = '{$model->testdiv}' ";
        $query .= "     GROUP BY ";
        $query .= "         ENTEXAMYEAR, ";
        $query .= "         APPLICANTDIV, ";
        $query .= "         TESTDIV, ";
        $query .= "         TESTSUBCLASSCD ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     R1.RECEPTNO, ";
        $query .= "     R1.EXAMNO, ";
        $query .= "     B1.NAME, ";
        foreach ($model->subclassList as $subclassCd => $subclassName) {
            $query .= "     S{$subclassCd}.SCORE AS SCORE1_{$subclassCd}, ";
            $query .= "     S{$subclassCd}.SCORE2 AS SCORE2_{$subclassCd}, ";
            $query .= "     S{$subclassCd}.ATTEND_FLG AS ATTEND_{$subclassCd}, ";
            $query .= "     VALUE(P{$subclassCd}.PERFECT, 100) AS PERFECT_{$subclassCd}, ";
        }
        $query .= "     B1.NAME_KANA ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT R1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "          ON B1.ENTEXAMYEAR      = R1.ENTEXAMYEAR ";
        $query .= "         AND B1.APPLICANTDIV     = R1.APPLICANTDIV ";
        $query .= "         AND B1.EXAMNO           = R1.EXAMNO ";
        foreach ($model->subclassList as $subclassCd => $subclassName) {
            $query .= "     INNER JOIN T_PERFECT P{$subclassCd} ";
            $query .= "          ON P{$subclassCd}.ENTEXAMYEAR      = R1.ENTEXAMYEAR ";
            $query .= "         AND P{$subclassCd}.APPLICANTDIV     = R1.APPLICANTDIV ";
            $query .= "         AND P{$subclassCd}.TESTDIV          = R1.TESTDIV ";
            $query .= "         AND P{$subclassCd}.TESTSUBCLASSCD   = '{$subclassCd}' ";
            $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT S{$subclassCd} ";
            $query .= "          ON S{$subclassCd}.ENTEXAMYEAR      = R1.ENTEXAMYEAR ";
            $query .= "         AND S{$subclassCd}.APPLICANTDIV     = R1.APPLICANTDIV ";
            $query .= "         AND S{$subclassCd}.TESTDIV          = R1.TESTDIV ";
            $query .= "         AND S{$subclassCd}.EXAM_TYPE        = R1.EXAM_TYPE ";
            $query .= "         AND S{$subclassCd}.RECEPTNO         = R1.RECEPTNO ";
            $query .= "         AND S{$subclassCd}.TESTSUBCLASSCD   = '{$subclassCd}' ";
        }
        $query .= " WHERE ";
        $query .= "     R1.ENTEXAMYEAR  = '{$model->ObjYear}' AND ";
        $query .= "     R1.APPLICANTDIV = '{$model->applicantdiv}' AND ";
        $query .= "     R1.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND VALUE(R1.JUDGEDIV,'') <> '4' ";
        //一般入試選択で「基準テストを含める・含めない・のみ」
        if (SCHOOLKIND != "J" && $model->testdiv == "5") {
            if ($model->field["KIJUN_TEST_DIV"] == "1") {
                //全て(一般入試、基準テスト)
            } elseif ($model->field["KIJUN_TEST_DIV"] == "2") {
                //一般入試のみ
                $query .= "     AND B1.TESTDIV  = '5' ";
            } elseif ($model->field["KIJUN_TEST_DIV"] == "3") {
                //基準テストのみ
                $query .= "     AND B1.TESTDIV != '5' ";
            }
        }

        //間違いのあるもののみ
        if ($model->field["SHOWDIV"] == "2") {
            $query .= "     AND ";
            $query .= "     ( ";
            $cnt = 0;
            foreach ($model->subclassList as $subclassCd => $subclassName) {
                if ($cnt > 0) {
                    $query .= " OR ";
                }
                $query .= "         VALUE(S{$subclassCd}.SCORE, -1) != VALUE(S{$subclassCd}.SCORE2, -1) ";
                $query .= "     OR ";
                $query .= "         VALUE(S{$subclassCd}.ATTEND_FLG, 'N') != '1' AND (VALUE(S{$subclassCd}.SCORE, -1) >= 0 OR VALUE(S{$subclassCd}.SCORE2, -1) >= 0) ";
                $cnt++;
            }
            $query .= "     ) ";
        //未入力
        } elseif ($model->field["SHOWDIV"] == "3") {
            $query .= "     AND ";
            $query .= "     ( ";
            $cnt = 0;
            foreach ($model->subclassList as $subclassCd => $subclassName) {
                if ($cnt > 0) {
                    $query .= " OR ";
                }
                $query .= "         VALUE(S{$subclassCd}.ATTEND_FLG, 'N') != '0' AND (VALUE(S{$subclassCd}.SCORE, -1) < 0 OR VALUE(S{$subclassCd}.SCORE2, -1) < 0) ";
                $cnt++;
            }
            $query .= "     ) ";
        }

        $query .= " ORDER BY ";
        $query .= "     R1.RECEPTNO ";

        return $query;
    }

    //更新(一度すべて削除してから追加）
    public function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        $tmp = explode(",", $model->hid_receptno);
        foreach ($tmp as $key => $receptno) {
            foreach ($model->subclassList as $subclassCd => $subclassName) {
                //削除
                $query  = " DELETE FROM ENTEXAM_SCORE_DAT ";
                $query .= " WHERE ENTEXAMYEAR    = '".$model->ObjYear."' ";
                $query .= "   AND APPLICANTDIV   = '".$model->applicantdiv."' ";
                $query .= "   AND TESTDIV        = '".$model->testdiv."' ";
                $query .= "   AND EXAM_TYPE      = '1' ";
                $query .= "   AND RECEPTNO       = '".$receptno."' ";
                $query .= "   AND TESTSUBCLASSCD = '".$subclassCd."' ";
                $db->query($query);

                $scoreName1 = "SCORE1_".$subclassCd;
                $scoreName2 = "SCORE2_".$subclassCd;
                $score1 = $model->setField[$receptno][$scoreName1];
                $score2 = $model->setField[$receptno][$scoreName2];

                //追加
                $data = array();
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["TESTDIV"][TEXT]          = $model->testdiv;
                $data["EXAM_TYPE"][TEXT]        = 1;
                $data["RECEPTNO"][TEXT]         = $receptno;
                $data["TESTSUBCLASSCD"][TEXT]   = $subclassCd;
                if ($score1 === '*' || $score2 === '*') {
                    $data["ATTEND_FLG"][TEXT]   = "0";
                    $data["SCORE"][NUMBER]      = "";
                    $data["SCORE2"][NUMBER]     = "";
                } else {
                    $data["ATTEND_FLG"][TEXT]   = !strlen($score1) && !strlen($score2) ? "" : "1";
                    $data["SCORE"][NUMBER]      = !strlen($score1) ? "" : $score1;
                    $data["SCORE2"][NUMBER]     = !strlen($score2) ? "" : $score2;
                }
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "SYSDATE()";

                $query = Query::insertSQL($data, "ENTEXAM_SCORE_DAT");
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>