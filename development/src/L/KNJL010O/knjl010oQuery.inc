<?php

require_once('for_php7.php');

class knjl010oQuery extends Query {

    //名称
    function GetName($namecd1,$ObjYear)
    {
        $query = "SELECT namecd2, name1, name2,abbv1,namespare2 FROM v_name_mst
                   WHERE year    = '".$ObjYear."'
                     AND namecd1 = '".$namecd1."'
                   ORDER BY namecd2 ";
        return $query;
    }

    //入試区分マスタ
    function getTestdivMst($ObjYear)
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS NAMECD2, ";
        $query .= "     NAME AS NAME1, ";
        $query .= "     ABBV AS ABBV1, ";
        $query .= "     SHOWDIV AS NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$ObjYear."' ";
        $query .= " ORDER BY ";
        $query .= "     SHOWORDER, ";
        $query .= "     TESTDIV ";
        return $query;
    }

    //名称
    function testDayCheck($ObjYear,$testdivArray)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     TESTDIV AS NAMECD2, ";
        $query .= "     ABBV AS ABBV1, ";
        $query .= "     TESTDAY AS NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$ObjYear."' AND ";
        $query .= "     TESTDAY in (SELECT ";
        $query .= "                         TESTDAY ";
        $query .= "                     FROM ";
        $query .= "                         ENTEXAM_TESTDIV_MST ";
        $query .= "                     WHERE ";
        $query .= "                         ENTEXAMYEAR = '".$ObjYear."' AND ";
        $query .= "                         TESTDIV IN ('" . implode("','", $testdivArray) . "') ";
        $query .= "                     GROUP BY ";
        $query .= "                         TESTDAY ";
        $query .= "                     HAVING ";
        $query .= "                         1 < COUNT(*) ";
        $query .= "                     ) ";
        $query .= " ORDER BY ";
        $query .= "     TESTDIV ";

        $testDayCheck = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $testDayCheck[$row["NAMECD2"]] = $row["ABBV1"];
        }

        Query::dbCheckIn($db);
        return $testDayCheck;
    }

    //一覧表示
    function SelectQuery($model)
    {
        $query  = "SELECT T1.examno";
        $query .= "      ,T1.name";
        $query .= "      ,T1.name_kana";
        $query .= "      ,T1.sex";
        $query .= "      ,MEISYOU_GET(T1.sex,'Z002',2) AS sex_name";
        $query .= "      ,T1.fs_cd";
        $query .= "      ,T1.fs_name";
        $query .= "      ,T1.fs_area_cd";
        $query .= "      ,MEISYOU_GET(T1.fs_area_cd,'Z003',1) AS fs_area_name";
        for ($code = 0; $code <= 6; $code++) {
            $query .= "  ,T1.testdiv" .$code;
        }
        $query .= "      ,T1.applicantdiv";
        $query .= "  FROM entexam_applicantbase_dat T1 ";
        $query .= " WHERE T1.entexamyear  = '".$model->ObjYear."'";
        $query .= "   AND T1.examno BETWEEN '".$model->examno."' AND '".$model->e_examno."'";
        $query .= " ORDER BY T1.examno";
        return $query;
    }

    //初回入力時に限りデータ更新（追加）ボタンをクリックしたあと、
    //テキスト部分の出身学校名の文字列の最後に”小学校”の文字列を追加する。
    function getCntData($model, $examno)
    {
        $db = Query::dbCheckOut();
        $query  = "SELECT COUNT(*) FROM entexam_applicantbase_dat";
        $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
        $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
        $query .= "   AND examno       = '".sprintf("%05d",$examno)."'";
        $cnt = $db->getOne($query);
        Query::dbCheckIn($db);

        return $cnt;
    }

    //更新(データが既にあればUPDATE、なければINSERT（必須項目が入力されているレコードのみ）
    function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $idx = 0;
        for ($examno = (int)$model->examno; $examno <= (int)$model->e_examno; $examno++)
        {
            $query  = "SELECT * FROM entexam_applicantbase_dat";
            $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
            $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
            $query .= "   AND examno       = '".sprintf("%05d",$examno)."'";
            $result = $db->query($query);

            //全ての項目がない場合は削除(他のテーブルも削除）
            if (!strlen($model->field["NAME"][$idx]) && 
                !strlen($model->field["NAME_KANA"][$idx]) && 
                !strlen($model->field["SEX"][$idx]) && 
                !strlen($model->field["FS_CD"][$idx]) && 
                !strlen($model->field["FS_NAME"][$idx]) && 
                !strlen($model->field["FS_AREA_CD"][$idx]) && 
                !strlen($model->field["TESTDIV0"."-".$idx][$idx]) && 
                !strlen($model->field["TESTDIV1"."-".$idx][$idx]) && 
                !strlen($model->field["TESTDIV2"."-".$idx][$idx]) && 
                !strlen($model->field["TESTDIV3"."-".$idx][$idx]) && 
                !strlen($model->field["TESTDIV4"."-".$idx][$idx]) && 
                !strlen($model->field["TESTDIV5"."-".$idx][$idx]) && 
                !strlen($model->field["TESTDIV6"."-".$idx][$idx])) {

                if ($result->numRows() > 0) {
                    $query  = "DELETE FROM ENTEXAM_APPLICANTBASE_DAT";
                    $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
                    $query .= "   AND examno       = '".sprintf("%05d",$examno)."'";
                    $db->query($query);

                    //ENTEXAM_APPLICANTADDR_DAT
                    $query  = "DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
                    $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
                    $query .= "   AND examno       = '".sprintf("%05d",$examno)."'";
                    $db->query($query);

                    //ENTEXAM_APPLICANTCONFRPT_DAT
                    $query  = "DELETE FROM ENTEXAM_APPLICANTCONFRPT_DAT ";
                    $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
                    $query .= "   AND examno       = '".sprintf("%05d",$examno)."'";
                    $db->query($query);

                    //ENTEXAM_SCORE_DAT
                    $query  = " DELETE FROM ENTEXAM_SCORE_DAT ";
                    $query .= "  WHERE (entexamyear, applicantdiv, testdiv, exam_type, receptno) ";
                    $query .= "       IN ";
                    $query .= " (SELECT entexamyear, applicantdiv, testdiv, exam_type, receptno ";
                    $query .= "    FROM entexam_recept_dat ";
                    $query .= "   WHERE entexamyear = '".$model->ObjYear."'";
                    $query .= "     AND examno      = '".sprintf("%05d",$examno)."')";
                    $db->query($query);

                    //ENTEXAM_JUDGE_TMP
                    $query  = " DELETE FROM ENTEXAM_JUDGE_TMP ";
                    $query .= "  WHERE ENTEXAMYEAR = '".$model->ObjYear."'";
                    $query .= "    AND EXAMNO      = '".sprintf("%05d",$examno)."'";
                    $db->query($query);

                    //ENTEXAM_RECEPT_DAT
                    $query  = "DELETE FROM ENTEXAM_RECEPT_DAT ";
                    $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
                    $query .= "   AND examno       = '".sprintf("%05d",$examno)."'";
                    $db->query($query);

                    //ENTEXAM_DESIRE_DAT
                    $query  = "DELETE FROM ENTEXAM_DESIRE_DAT ";
                    $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
                    $query .= "   AND examno       = '".sprintf("%05d",$examno)."'";
                    $db->query($query);
                }
                $idx++;

            } else {

                if ($result->numRows() == 0) {

                    $data = array();
                    $data["entexamyear"][TEXT]  = $model->ObjYear;
                    $data["applicantdiv"][TEXT] = $model->applicantdiv;
                    $data["examno"][TEXT]       = sprintf("%05d",$examno);

                    //TODO：固定値で問題ないか！？
                    $data["shdiv"][TEXT]        = '1';
                    $data["desirediv"][TEXT]    = '1';

                    //入試区分
                    $testdiv = "";
                    for ($code = 0; $code <= 6; $code++) {
                        $fieldname      = "TESTDIV" .$code;
                        $fieldnameObj   = $fieldname ."-" .$idx;
                        $data[$fieldname][TEXT] = strlen($model->field[$fieldnameObj][$idx]) ? $code : "";
                        //入試区分の最小値を取得
                        if ($testdiv == "" && strlen($model->field[$fieldnameObj][$idx])) $testdiv = $code;
                    }
                    //出願区分 入試区分の最小値をセット
                    $data["testdiv"][TEXT]      = $testdiv;

                    $data["name"][TEXT]         = $model->field["NAME"][$idx];
                    $data["name_kana"][TEXT]    = $model->field["NAME_KANA"][$idx];
                    $data["sex"][TEXT]          = $model->field["SEX"][$idx];
//                    $data["fs_cd"][TEXT]        = (strlen($model->field["FS_CD"][$idx]) ? sprintf("%07d",$model->field["FS_CD"][$idx]) : "");
                    $data["fs_name"][TEXT]      = strlen($model->field["FS_NAME"][$idx]) ? $model->field["FS_NAME"][$idx] . "小学校" : "";
                    $data["fs_area_cd"][TEXT]   = $model->field["FS_AREA_CD"][$idx];
                    $data["registercd"][TEXT]   = STAFFCD;
                    $data["updated"][FUNC]      = "sysdate()";
                    $query = Query::insertSQL($data, "entexam_applicantbase_dat");

                } else {
                    $data = array();

                    //入試区分
                    $testdiv = "";
                    for ($code = 0; $code <= 6; $code++) {
                        $fieldname      = "TESTDIV" .$code;
                        $fieldnameObj   = $fieldname ."-" .$idx;
                        $data[$fieldname][TEXT] = strlen($model->field[$fieldnameObj][$idx]) ? $code : "";
                        //入試区分の最小値を取得
                        if ($testdiv == "" && strlen($model->field[$fieldnameObj][$idx])) $testdiv = $code;
                    }
                    //出願区分 入試区分の最小値をセット
                    $data["testdiv"][TEXT]    = $testdiv;

                    $data["name"][TEXT]       = $model->field["NAME"][$idx];
                    $data["name_kana"][TEXT]  = $model->field["NAME_KANA"][$idx];
                    $data["sex"][TEXT]        = $model->field["SEX"][$idx];
//                    $data["fs_cd"][TEXT]      = (strlen($model->field["FS_CD"][$idx]) ? sprintf("%07d",$model->field["FS_CD"][$idx]) : "");
                    $data["fs_name"][TEXT]    = $model->field["FS_NAME"][$idx];
                    $data["fs_area_cd"][TEXT] = $model->field["FS_AREA_CD"][$idx];
                    $data["registercd"][TEXT] = STAFFCD;
                    $data["updated"][FUNC]    = "sysdate()";

                    $where  = " WHERE entexamyear  = '".$model->ObjYear."'";
                    $where .= "   AND applicantdiv = '".$model->applicantdiv."'";
                    $where .= "   AND examno       = '".sprintf("%05d",$examno)."'";

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
