<?php

require_once('for_php7.php');

class knjl010hQuery extends Query {

    //出身学校マスタよりデータを取得
    function getFsName($year, $cd2)
    {
        $query  = " SELECT  T1.FINSCHOOLCD, T1.FINSCHOOL_NAME ";
        $query .= "   FROM  FINSCHOOL_MST T1, ";
        $query .= "         FINSCHOOL_YDAT T2 ";
        $query .= "  WHERE  T2.YEAR = '".$year."' ";
        $query .= "    AND  T2.FINSCHOOLCD = T1.FINSCHOOLCD ";
        $query .= "    AND  T2.FINSCHOOLCD LIKE '".$cd2."%' ";
        $query .= " ORDER BY T1.FINSCHOOLCD ";
        return $query;
    }

    //名称
    function GetName($namecd1,$ObjYear)
    {
        $query = "SELECT namecd2, name1, name2,namespare2 FROM v_name_mst
                   WHERE year    = '".$ObjYear."'
                     AND namecd1 = '".$namecd1."'
                   ORDER BY namecd2 ";
        return $query;
    }

    //出願コース取得
    function GetDesirediv($testdiv,$ObjYear)
    {
        $query  = "SELECT T1.desirediv, T2.examcourse_name ";
        $query .= "  FROM entexam_wishdiv_mst T1 ";
        $query .= "       INNER JOIN entexam_course_mst T2 ";
        $query .= "               ON T1.entexamyear  = T2.entexamyear ";
        $query .= "              AND T1.examcoursecd = T2.examcoursecd ";
        $query .= "              AND T1.coursecd     = T2.coursecd ";
        $query .= "              AND T1.majorcd      = T2.majorcd ";
        $query .= "   AND T1.entexamyear = '".$ObjYear."'";
        $query .= "   AND T1.wishno      = '1'"; //固定でOK
        $query .= " ORDER BY T1.desirediv";

        return $query;
    }
    
    //一覧表示
    function SelectQuery($model)
    {
        $query  = "SELECT T1.examno";
        $query .= "      ,T1.name";
        $query .= "      ,T1.name_kana";
        $query .= "      ,T1.sex";
        $query .= "      ,N1.NAME2 AS sex_name";
        $query .= "      ,T1.fs_cd";
        $query .= "      ,T1.fs_name";
        $query .= "      ,T1.FS_NATPUBPRIDIV";
        $query .= "      ,N2.NAME1 AS FS_NATPUB_NAME";
        $query .= "      ,T1.testdiv";
        $query .= "      ,T1.desirediv";
        $query .= "      ,T1.applicantdiv";
        $query .= "      ,T1.TESTDIV1";
        $query .= "      ,T1.TESTDIV2";
        $query .= "      ,T1.TESTDIV3";
        $query .= "      ,T1.TESTDIV4";
        $query .= "      ,T1.RECOM_EXAMNO";
        $query .= "  FROM entexam_applicantbase_dat T1 ";
        $query .= "       LEFT JOIN NAME_MST N1 ON N1.NAMECD1='Z002' AND N1.NAMECD2=T1.sex ";
        $query .= "       LEFT JOIN NAME_MST N2 ON N2.NAMECD1='L015' AND N2.NAMECD2=T1.FS_NATPUBPRIDIV ";
        $query .= " WHERE T1.entexamyear  = '".$model->ObjYear."'";
        $query .= "   AND T1.examno BETWEEN '".$model->examno."' AND '".$model->e_examno."'";
        $query .= " ORDER BY T1.examno";
        return $query;
    }

    //推薦受験番号の存在チェック
    function checkRecomExamno($model, $recomExamno)
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT COUNT(*) as CNT ";
        $query .= "  FROM entexam_applicantbase_dat T1 ";
        $query .= " WHERE T1.entexamyear  = '".$model->ObjYear."' ";
        $query .= "   AND T1.applicantdiv = '3' ";
        $query .= "   AND T1.EXAMNO = '".$recomExamno."' ";
        $retCnt = $db->getOne($query);

        Query::dbCheckIn($db);
        return $retCnt;
    }

    //推薦受験番号の重複チェック（既に同じ推薦受験番号が登録されていないか？）
    function checkRecomExamno2($model, $recomExamno, $examno)
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT COUNT(*) as CNT ";
        $query .= "  FROM entexam_applicantbase_dat T1 ";
        $query .= " WHERE T1.entexamyear  = '".$model->ObjYear."' ";
        $query .= "   AND T1.applicantdiv = '2' ";
        $query .= "   AND T1.EXAMNO != '".$examno."' ";
        $query .= "   AND T1.RECOM_EXAMNO = '".$recomExamno."' ";
        $retCnt = $db->getOne($query);

        Query::dbCheckIn($db);
        return $retCnt;
    }

    //更新(データが既にあればUPDATE、なければINSERT（必須項目が入力されているレコードのみ）
    function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $idx = 0;
        for ($examno = (int)$model->examno; $examno <= (int)$model->e_examno; $examno++)
        {
            $query  = " SELECT  FINSCHOOLCD, FINSCHOOL_NAME, DISTRICTCD ";
            $query .= "   FROM  FINSCHOOL_MST ";
            $query .= "  WHERE  FINSCHOOLCD = '".$model->field["FS_CD"][$idx]."' ";
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);

            $query  = "SELECT * FROM entexam_applicantbase_dat";
            $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
            $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
            $query .= "   AND examno       = '".sprintf("%04d",$examno)."'";
            $result = $db->query($query);

            //必須項目がない場合は削除(他のテーブルも削除）
            if (!strlen($model->field["NAME"][$idx]) || !strlen($model->field["SEX"][$idx])
                || !strlen($model->field["NAME_KANA"][$idx])
                || (!strlen($model->field["TESTDIV1"."-".$idx][$idx]) && !strlen($model->field["TESTDIV2"."-".$idx][$idx]) && !strlen($model->field["TESTDIV3"."-".$idx][$idx]))) {

                if ($result->numRows() > 0) {
                    $query  = "DELETE FROM ENTEXAM_APPLICANTBASE_DAT";
                    $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
                    $query .= "   AND examno       = '".sprintf("%04d",$examno)."'";
                    $db->query($query);

                    //ENTEXAM_APPLICANTADDR_DAT
                    $query  = "DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
                    $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
                    $query .= "   AND examno       = '".sprintf("%04d",$examno)."'";
                    $db->query($query);

                    //ENTEXAM_APPLICANTCONFRPT_DAT
                    $query  = "DELETE FROM ENTEXAM_APPLICANTCONFRPT_DAT ";
                    $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
                    $query .= "   AND examno       = '".sprintf("%04d",$examno)."'";
                    $db->query($query);

                    //ENTEXAM_SCORE_DAT
                    $query  = " DELETE FROM ENTEXAM_SCORE_DAT ";
                    $query .= "  WHERE (entexamyear, applicantdiv, testdiv, exam_type, receptno) ";
                    $query .= "       IN ";
                    $query .= " (SELECT entexamyear, applicantdiv, testdiv, exam_type, receptno ";
                    $query .= "    FROM entexam_recept_dat ";
                    $query .= "   WHERE entexamyear = '".$model->ObjYear."'";
                    $query .= "     AND examno      = '".sprintf("%04d",$examno)."')";
                    $db->query($query);

                    //ENTEXAM_JUDGE_TMP
                    $query  = " DELETE FROM ENTEXAM_JUDGE_TMP ";
                    $query .= "  WHERE ENTEXAMYEAR = '".$model->ObjYear."'";
                    $query .= "    AND EXAMNO      = '".sprintf("%04d",$examno)."'";
                    $db->query($query);

                    //ENTEXAM_RECEPT_DAT
                    $query  = "DELETE FROM ENTEXAM_RECEPT_DAT ";
                    $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
                    $query .= "   AND examno       = '".sprintf("%04d",$examno)."'";
                    $db->query($query);

                    //ENTEXAM_DESIRE_DAT
                    $query  = "DELETE FROM ENTEXAM_DESIRE_DAT ";
                    $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
                    $query .= "   AND examno       = '".sprintf("%04d",$examno)."'";
                    $db->query($query);
                }
                $idx++;

            } else {

                if ($result->numRows() == 0) {

                    $data = array();
                    $data["entexamyear"][TEXT]  = $model->ObjYear;
                    $data["applicantdiv"][TEXT] = $model->applicantdiv;
                    $data["examno"][TEXT]       = sprintf("%04d",$examno);

                    $data["testdiv"][TEXT]      = '1';
                    $data["shdiv"][TEXT]        = '1';
                    $data["desirediv"][TEXT]    = '1';

                    //入試区分・・・受験型のコードをセット
                    $data["testdiv1"][TEXT]     = strlen($model->field["TESTDIV1"."-".$idx][$idx]) ? "1" : "";
                    $data["testdiv2"][TEXT]     = strlen($model->field["TESTDIV2"."-".$idx][$idx]) ? "1" : "";
                    $data["testdiv3"][TEXT]     = strlen($model->field["TESTDIV3"."-".$idx][$idx]) ? "1" : "";
                    $data["testdiv4"][TEXT]     = strlen($model->field["TESTDIV4"."-".$idx][$idx]) ? "1" : "";

                    $data["name"][TEXT]         = $model->field["NAME"][$idx];
                    $data["name_kana"][TEXT]    = $model->field["NAME_KANA"][$idx];
                    $data["sex"][TEXT]          = $model->field["SEX"][$idx];
                    $data["fs_cd"][TEXT]        = $model->field["FS_CD"][$idx];
                    $data["fs_name"][TEXT]      = (strlen($Row["FINSCHOOL_NAME"]) > 45) ? substr($Row["FINSCHOOL_NAME"],0,45) : $Row["FINSCHOOL_NAME"];
                    $data["fs_area_cd"][TEXT]   = $Row["DISTRICTCD"];
                    $data["FS_NATPUBPRIDIV"][TEXT]   = $model->field["FS_NATPUBPRIDIV"][$idx];
                    if ($model->applicantdiv == "4") {
                        $data["JUDGEMENT"][TEXT]    = "1";// 4:高校（内部生）は自動的に合格
                        $data["PROCEDUREDIV"][TEXT] = "1";// 4:高校（内部生）は自動的に済み
                        $data["ENTDIV"][TEXT]       = "1";// 4:高校（内部生）は自動的に済み
                    }
                    //推薦受験番号
                    $data["RECOM_EXAMNO"][TEXT]     = $model->field["RECOM_EXAMNO"][$idx];

                    $data["registercd"][TEXT]   = STAFFCD;
                    $data["updated"][FUNC]      = "sysdate()";
                    $query = Query::insertSQL($data, "entexam_applicantbase_dat");

                } else {

                    $data = array();

                    $data["testdiv"][TEXT]      = '1';
                    $data["shdiv"][TEXT]        = '1';
                    $data["desirediv"][TEXT]    = '1';

                    //入試区分・・・受験型のコードをセット
                    $data["testdiv1"][TEXT]     = strlen($model->field["TESTDIV1"."-".$idx][$idx]) ? "1" : "";
                    $data["testdiv2"][TEXT]     = strlen($model->field["TESTDIV2"."-".$idx][$idx]) ? "1" : "";
                    $data["testdiv3"][TEXT]     = strlen($model->field["TESTDIV3"."-".$idx][$idx]) ? "1" : "";
                    $data["testdiv4"][TEXT]     = strlen($model->field["TESTDIV4"."-".$idx][$idx]) ? "1" : "";

                    $data["name"][TEXT]         = $model->field["NAME"][$idx];
                    $data["name_kana"][TEXT]    = $model->field["NAME_KANA"][$idx];
                    $data["sex"][TEXT]          = $model->field["SEX"][$idx];
                    $data["fs_cd"][TEXT]        = $model->field["FS_CD"][$idx];
                    $data["fs_name"][TEXT]      = (strlen($Row["FINSCHOOL_NAME"]) > 45) ? substr($Row["FINSCHOOL_NAME"],0,45) : $Row["FINSCHOOL_NAME"];
                    $data["fs_area_cd"][TEXT]   = $Row["DISTRICTCD"];
                    $data["FS_NATPUBPRIDIV"][TEXT]   = $model->field["FS_NATPUBPRIDIV"][$idx];
                    if ($model->applicantdiv == "4") {
                        $data["JUDGEMENT"][TEXT]    = "1";// 4:高校（内部生）は自動的に合格
                        $data["PROCEDUREDIV"][TEXT] = "1";// 4:高校（内部生）は自動的に済み
                        $data["ENTDIV"][TEXT]       = "1";// 4:高校（内部生）は自動的に済み
                    }
                    //推薦受験番号
                    $data["RECOM_EXAMNO"][TEXT]     = $model->field["RECOM_EXAMNO"][$idx];

                    $data["registercd"][TEXT]   = STAFFCD;
                    $data["updated"][FUNC]      = "sysdate()";

                    $where  = " WHERE entexamyear  = '".$model->ObjYear."'";
                    $where .= "   AND applicantdiv = '".$model->applicantdiv."'";
                    $where .= "   AND examno       = '".sprintf("%04d",$examno)."'";

                    $query = Query::updateSQL($data, "entexam_applicantbase_dat", $where);

                }
                $db->query($query);
                $idx++;
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
