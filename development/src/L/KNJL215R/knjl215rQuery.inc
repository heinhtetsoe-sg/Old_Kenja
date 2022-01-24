<?php

require_once('for_php7.php');

class knjl215rQuery extends Query {

    //名称マスタより取得
    function getNameMst($model, $namecd1, $year, $namecd2 = "") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 || ':' || ABBV2 AS LABEL2, ";
        $query .= "     NAMESPARE2, ";
        $query .= "     NAMESPARE3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if (strlen($namecd2)) {
            $query .= " AND NAMECD2 != '".$namecd2."' ";
        }
        if ($namecd1 == "L003") {
            $query .= "     AND NAMECD2 = '".$model->fixApplicantDiv."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //事前相談データ
    function getBeforeDat($model) {
        $query  = " SELECT ";
        $query .= "     BEFORE_PAGE, ";
        $query .= "     BEFORE_SEQ, ";
        $query .= "     FS_CD, ";
        $query .= "     NAME_KANA, ";
        $query .= "     SEX, ";
        $query .= "     SCHOLARSHIP, ";
        $query .= "     RECOM_FLG ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANT_BEFORE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->examyear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV = '{$model->testdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     BEFORE_PAGE, ";
        $query .= "     BEFORE_SEQ ";
        return $query;
    }

    //志願者基礎データ
    function getBaseDat($model) {
        $query  = " SELECT ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.SEX, ";
        $query .= "     T1.FS_CD, ";
        $query .= "     T1.JUDGE_KIND, ";
        $query .= "     T1.SPORTS_FLG, ";
        $query .= "     T2.REMARK1, ";
        $query .= "     T2.REMARK2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T2 ";
        $query .= "         ON  T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.EXAMNO       = T1.EXAMNO ";
        $query .= "         AND T2.SEQ          = '002' ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->examyear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND T1.TESTDIV = '{$model->testdiv}' ";
        return $query;
    }

    function ExecuteQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //事前相談データ
        $beforeArray = array();
        $query = knjl215rQuery::getBeforeDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $key = $row["BEFORE_PAGE"] . "-" . $row["BEFORE_SEQ"];
            $beforeArray[$key] = array("BEFORE_PAGE"    => $row["BEFORE_PAGE"],
                                       "BEFORE_SEQ"     => $row["BEFORE_SEQ"],
                                       "NAME_KANA"      => $row["NAME_KANA"],
                                       "FS_CD"          => $row["FS_CD"],
                                       "SEX"            => $row["SEX"],
                                       "SCHOLARSHIP"    => $row["SCHOLARSHIP"],
                                       "RECOM_FLG"      => $row["RECOM_FLG"]
                                );
        }
        $result->free();

        //志願者基礎データ
        $baseArray = array();
        $query = knjl215rQuery::getBaseDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $key = $row["EXAMNO"];
            $baseArray[$key] = array("EXAMNO"           => $row["EXAMNO"],
                                     "NAME_KANA"        => $row["NAME_KANA"],
                                     "FS_CD"            => $row["FS_CD"],
                                     "SEX"              => $row["SEX"],
                                     "JUDGE_KIND"       => $row["JUDGE_KIND"],
                                     "SPORTS_FLG"       => $row["SPORTS_FLG"],
                                     "REMARK1"          => $row["REMARK1"],
                                     "REMARK2"          => $row["REMARK2"]
                                );
        }
        $result->free();

        //条件
        $checkPageno = array();
        $checkExamno = array();
        foreach ($beforeArray as $pageno => $beforeList) {
            foreach ($baseArray as $examno => $baseList) {
                if ($beforeList["NAME_KANA"] == $baseList["NAME_KANA"] && 
                    $beforeList["FS_CD"] == $baseList["FS_CD"] && 
                    $beforeList["SEX"] == $baseList["SEX"]) 
                {
                    $checkPageno[$pageno][] = $examno;
                    $checkExamno[$examno][] = $pageno;
                }
            }
        }

        //クリア
        foreach ($baseArray as $examno => $baseList) {
            //志願者基礎データの更新
            $data = array();
            $data["JUDGE_KIND"][TEXT]       = "";
            $data["SPORTS_FLG"][TEXT]       = "";
            $data["registercd"][TEXT]       = STAFFCD;
            $data["updated"][FUNC]          = "sysdate()";
            $where  = " WHERE ENTEXAMYEAR   = '{$model->examyear}' ";
            $query .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $where .= "   AND EXAMNO        = '{$examno}' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);
            //志願者基礎詳細データの削除
            $query = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
            $query.= " WHERE ENTEXAMYEAR    = '{$model->examyear}' ";
            $query .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $query.= "       AND EXAMNO     = '{$examno}' ";
            $query.= "       AND SEQ        = '002' ";
            $db->query($query);
        }

        //更新
        $shoriKensuu = 0;
        foreach ($checkPageno as $pageno => $array) {
            if (1 < get_count($array)) continue;
            foreach ($array as $key => $examno) {
                if (1 < get_count($checkExamno[$examno])) continue;
                //1:1のみ更新
                //志願者基礎データの更新
                $data = array();
                $data["JUDGE_KIND"][TEXT]       = $beforeArray[$pageno]["SCHOLARSHIP"];
                $data["SPORTS_FLG"][TEXT]       = $beforeArray[$pageno]["RECOM_FLG"];
                $data["registercd"][TEXT]       = STAFFCD;
                $data["updated"][FUNC]          = "sysdate()";
                $where  = " WHERE ENTEXAMYEAR   = '{$model->examyear}' ";
                $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
                $where .= "   AND EXAMNO        = '{$examno}' ";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
                $db->query($query);
                //志願者基礎詳細データの追加
                $data = array();
                $data["ENTEXAMYEAR"][TEXT]      = $model->examyear;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["EXAMNO"][TEXT]           = $examno;
                $data["SEQ"][TEXT]              = '002';
                $data["REMARK1"][TEXT]          = $beforeArray[$pageno]["BEFORE_PAGE"];
                $data["REMARK2"][TEXT]          = $beforeArray[$pageno]["BEFORE_SEQ"];
                $data["registercd"][TEXT]       = STAFFCD;
                $data["updated"][FUNC]          = "sysdate()";
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
                $db->query($query);
                $shoriKensuu++;
            }
        }

        $db->commit(); 
        Query::dbCheckIn($db);

        //事前相談未処理とは、
        //条件に一致しない。
        //条件に一致するが、1:NまたはN:1である。
        returnget_count($beforeArray) - $shoriKensuu;
    } 
}
?>

