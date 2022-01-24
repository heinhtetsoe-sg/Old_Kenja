<?php

require_once('for_php7.php');

class knjl025qQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd1 == "L003") {
            $query .= "     AND NAMESPARE3 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //出身学校マスタより取得
    function getFinSchoolMst($model) {
        $query  = " SELECT ";
        $query .= "     FINSCHOOLCD AS VALUE,";
        $query .= "     FINSCHOOLCD || ':' || FINSCHOOL_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     FINSCHOOLCD IN ( ";
        $query .= "         SELECT ";
        $query .= "             FS_CD";
        $query .= "         FROM ";
        $query .= "             V_ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "         WHERE ";
        $query .= "                 ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "             AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "             AND TESTDIV      = '{$model->testdiv}' ";
        $query .= "         GROUP BY ";
        $query .= "             FS_CD ";
        $query .= "         ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //一覧表示
    function SelectQuery($model) {
        $query  = " SELECT ";
        $query .= "     B1.EXAMNO, ";
        $query .= "     B1.NAME, ";
        $query .= "     B1.FS_CD, ";
        $query .= "     FM.FINSCHOOL_NAME, ";
        $query .= "     P1.SCORE1, ";
        $query .= "     P1.SCORE2, ";
        $query .= "     P1.KAKUYAKU_FLG ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "     LEFT JOIN ENTEXAM_PUBLIC_TEST_DAT P1 ";
        $query .= "          ON P1.ENTEXAMYEAR      = B1.ENTEXAMYEAR ";
        $query .= "         AND P1.APPLICANTDIV     = B1.APPLICANTDIV ";
        $query .= "         AND P1.EXAMNO           = B1.EXAMNO ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FM ON FM.FINSCHOOLCD = B1.FS_CD ";
        $query .= " WHERE ";
        $query .= "         B1.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND B1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND B1.TESTDIV      = '{$model->testdiv}' ";
        //表示内容 1:すべて 2:特定中学
        if ($model->show == "2") {
            $query .= "     AND B1.FS_CD        = '{$model->fs_cd}' ";
        }
        $query .= " ORDER BY ";
        //表示順 1:中学校順 2:番号順
        if ($model->sort == "1") {
            $query .= "     B1.FS_CD, ";
        }
        $query .= "     B1.EXAMNO ";

        return $query;
    }

    //更新(一度すべて削除してから追加）
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //受験番号配列
        $arr_examno = explode(",", $model->hid_examno);

        //削除
        $query  = " DELETE FROM ENTEXAM_PUBLIC_TEST_DAT ";
        $query .= " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
        $query .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "   AND EXAMNO        IN ('".implode("','",$arr_examno)."')";
        $db->query($query);

        //追加
        foreach ($arr_examno as $key => $examno) {
            $score1 = $model->fields["SCORE1"][$examno];
            $score2 = $model->fields["SCORE2"][$examno];

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["EXAMNO"][TEXT]           = $examno;
            $data["SCORE1"][NUMBER]         = $score1;
            $data["SCORE2"][NUMBER]         = $score2;
            $data["KAKUYAKU_FLG"][TEXT]     = $model->fields["KAKUYAKU_FLG"][$examno];
            //得点配列にセット
            $score = array();
            if (strlen($score1)) $score[] = (int) $score1;
            if (strlen($score2)) $score[] = (int) $score2;
            //平均値を求めて、四捨五入する。
            $data["AVG"][NUMBER]            = (get_count($score) > 0) ? round(array_sum($score)/get_count($score)) : "";
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            $query = Query::insertSQL($data, "ENTEXAM_PUBLIC_TEST_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>