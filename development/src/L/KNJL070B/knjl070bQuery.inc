<?php

require_once('for_php7.php');

class knjl070bQuery extends Query {

    //名称マスタよりデータを取得
    function getName($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE2, ";
        $query .= "     ABBV3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if (strlen($namecd2)) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //区分 1:正規合格 2:単切合格
    function getShdiv($model) {
        //変数
        $isIppan = ($model->testdiv == "2") ? true : false;
        //SQL
        $query  = "";
        $query .= " WITH T_COMMON (VALUE,LABEL) AS ( ";
        $query .= "     VALUES('1','1:正規合格') ";
        if ($isIppan) {
            $query .= "     UNION ALL ";
            $query .= "     VALUES('2','2:単切合格') ";
        }
        $query .= "     ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     T_COMMON ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //判定区分
    function getJudgmentDiv($model, $shdiv) {
        //変数
        $isIppan = ($model->testdiv == "2") ? true : false;
        $isSeiki = ($shdiv == "1") ? true : false;
        //SQL
        $query  = " SELECT ";
        $query .= "     JUDGMENT_DIV || ':' || JUDGMENT_COURSE_NAME AS LABEL, ";
        $query .= "     JUDGMENT_DIV AS VALUE, ";
        //情報
        //正規合格コース
        $query .= "     NORMAL_PASSEXAMCOURSECD, "; //正規合格コース
        $query .= "     PASSCOURSE_DIV, "; //コース区分 1:特進 2:進学
        $query .= "     SCHOOLWORK_DIV, "; //学／体区分 1:学業 2:体育
        $query .= "     NORMAL_PASSCOURSECD || NORMAL_PASSMAJORCD || NORMAL_PASSEXAMCOURSECD AS NORMAL_PASS_COURSE, ";//正規合格コース
        if ($isSeiki) {
            $query .= "     NORMAL_PASSCOURSECD || NORMAL_PASSMAJORCD || NORMAL_PASSEXAMCOURSECD AS PASS_COURSE ";//合格コース(正規)
        } else {
            $query .= "     EARLY_PASSCOURSECD || EARLY_PASSMAJORCD || EARLY_PASSEXAMCOURSECD AS PASS_COURSE ";//合格コース(単切)
        }
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_JUDGMENT_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->year}' ";
        if ($isIppan && $isSeiki) {
            $query .= "     AND TAKE_GENERAL_TEST_FLG = '1' ";  //一般入試(正規)
        } else if ($isIppan && !$isSeiki) {
            $query .= "     AND CHANGE_SINGLE_TEST_FLG = '1' "; //一般入試(単切)
        } else if (!$isIppan && $isSeiki) {
            $query .= "     AND TAKE_RECOMMEND_TEST_FLG = '1' ";//推薦入試(正規)
        } else {
            $query .= "     AND TAKE_RECOMMEND_TEST_FLG = '9' ";//推薦入試(単切)はないので、ありえないコード指定しておく。
        }
        //判定区分コンボのリストには、特進選抜コースは表示しない
        $query .= "     AND JUDGMENT_DIV NOT IN ('13','14','15') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //合格点マスタ
    function selectQueryPassingmark($model, $course, $shdiv) {
        $query  = " SELECT ";
        $query .= "     CAPA_CNT, ";
        $query .= "     BORDER_DEVIATION, ";
        $query .= "     SUCCESS_CNT, ";
        $query .= "     SUCCESS_CNT_SPECIAL, ";
        $query .= "     SUCCESS_CNT_SPECIAL2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PASSINGMARK_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV    = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV         = '{$model->testdiv}' ";
        $query .= "     AND SHDIV           = '{$shdiv}' ";
        $query .= "     AND COURSECD || MAJORCD || EXAMCOURSECD = '{$course}' ";

        return $query;
    }

    //合格者取得(シミュレーション結果表示)
    function selectQuerySuccess_cnt($model) {
        $query  = " SELECT ";
        $query .= "     SUM(CASE WHEN JUDGEDIV IN ('2') THEN 1 ELSE 0 END) AS SUCCESS_CNT_NO, ";
        $query .= "     SUM(CASE WHEN JUDGEDIV IN ('1','3','5') THEN 1 ELSE 0 END) AS SUCCESS_CNT, ";
        $query .= "     SUM(CASE WHEN JUDGEDIV IN ('3') THEN 1 ELSE 0 END) AS SUCCESS_CNT_SPECIAL, ";
        $query .= "     SUM(CASE WHEN JUDGEDIV IN ('5') THEN 1 ELSE 0 END) AS SUCCESS_CNT_SPECIAL2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_JUDGE_TMP ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV    = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV         = '{$model->testdiv}' ";

        return $query;
    }

    //欠席者数など
    function getKessekiCnt($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS ALL_CNT, "; //志願者数
        $query .= "     COUNT(CASE WHEN T1.JUDGEDIV = '3' THEN 1 END) AS KESSEKI_CNT, "; //欠席者数
        $query .= "     COUNT(CASE WHEN L1.REMARK8 = '20' THEN 1 END) AS KAKUYAKU_FUGOU_CNT "; //確約不合格数
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "         ON  T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.EXAMNO = T1.EXAMNO ";
                        //確約
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT L1 ";
        $query .= "         ON  L1.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "         AND L1.EXAMNO = T2.EXAMNO ";
        $query .= "         AND L1.SEQ = '004' ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '{$model->testdiv}' ";

        return $query;
    }

    //-----------------------------------
    //シミュレーション処理実行
    //-----------------------------------

    //シミュレーションSQL
    function simSql($model) {
        //変数
        $staffcd = STAFFCD;
        $isSeiki = ($model->shdiv == "1") ? true : false;
        //SQL
        $query  = " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.EXAM_TYPE, ";
        $query .= "     T1.RECEPTNO, ";
        $query .= "     T1.EXAMNO, ";
        //JUDGEDIV -----------------------------------------
        //1:合格 2:不合格 3:合格「確約(合格点以上)」 5:合格「確約(合格点未満)」
        $query .= "     CASE ";
        //確約が(20:不合格)の者→2:不合格
        $query .= "     WHEN L1.REMARK8 = '20' ";
        $query .= "     THEN '2' ";
        //確約あり・なし関係なく1:全教科受験者でない→2:不合格
        $query .= "     WHEN T1.ATTEND_ALL_FLG != '1' ";
        $query .= "     THEN '2' ";
        //確約なしで合格点以上→1:合格
        $query .= "     WHEN VALUE(L1.REMARK8, '99') != '{$model->judgment_div}' AND T1.AVARAGE4 >= {$model->field["BORDER_DEVIATION"]} ";
        $query .= "     THEN '1' ";
        //確約ありで合格点以上→3:合格
        $query .= "     WHEN L1.REMARK8 = '{$model->judgment_div}' AND T1.AVARAGE4 >= {$model->field["BORDER_DEVIATION"]} ";
        $query .= "     THEN '3' ";
        //同じ確約ありで合格点未満→5:合格
        //つまり、合格点での判定結果は不合格だが、確約で合格。
        $query .= "     WHEN L1.REMARK8 = '{$model->judgment_div}' ";
        $query .= "     THEN '5' ";
        //不合格
        $query .= "     ELSE '2' ";
        $query .= "     END AS JUDGEDIV, ";
        $query .= "     '{$staffcd}' ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "         ON  T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.EXAMNO = T1.EXAMNO ";
                        //確約
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT L1 ";
        $query .= "         ON  L1.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "         AND L1.EXAMNO = T2.EXAMNO ";
        $query .= "         AND L1.SEQ = '004' ";
                        //合否詳細区分マスタの正規合格コース
        $query .= "     LEFT JOIN ENTEXAM_COURSE_JUDGMENT_MST L3 ";
        $query .= "         ON  L3.ENTEXAMYEAR = L1.ENTEXAMYEAR ";
        $query .= "         AND L3.JUDGMENT_DIV = L1.REMARK8 ";
                        //正規判定結果データの正規合格コース
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT L2 ";
        $query .= "         ON  L2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND L2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND L2.TESTDIV = T1.TESTDIV ";
        $query .= "         AND L2.EXAM_TYPE = T1.EXAM_TYPE ";
        $query .= "         AND L2.RECEPTNO = T1.RECEPTNO ";
        $query .= "         AND L2.SEQ = '001' ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND VALUE(T1.JUDGEDIV, '0') <> '3' "; //3:未受験(欠席)は対象外
        //◆対象者の条件
        //(1)共通
        //正規合格コース
        $normal_passexamcoursecd = $model->judgmentDivArray[$model->shdiv][$model->judgment_div]["NORMAL_PASSEXAMCOURSECD"];
        if (!strlen($normal_passexamcoursecd)) $normal_passexamcoursecd = '9999';
        if ($isSeiki) {
            //正規を実行の場合、正規合格コース以上が対象
            $query .= "     AND VALUE(L2.REMARK3, '9999') >= '{$normal_passexamcoursecd}' ";
        } else {
            //単切を実行の場合、正規合格コース等しいが対象
            $query .= "     AND VALUE(L2.REMARK3, '9999')  = '{$normal_passexamcoursecd}' ";
        }
        //(2)確約
        //コース区分 1:特進 2:進学
        //学／体区分 1:学業 2:体育
        $passcourse_div = $model->judgmentDivArray[$model->shdiv][$model->judgment_div]["PASSCOURSE_DIV"];
        $schoolwork_div = $model->judgmentDivArray[$model->shdiv][$model->judgment_div]["SCHOOLWORK_DIV"];
        if ($passcourse_div == "1" && $schoolwork_div == "2") {
            //特進体育を実行の場合、確約が(09～10:特進体育)の者が対象
            $query .= "     AND VALUE(L1.REMARK8, '99') = '{$model->judgment_div}' ";
        } else if ($passcourse_div == "2" && $schoolwork_div == "2") {
            //進学体育を実行の場合、確約が(11～12:進学体育と19:確約不可)の者が対象
            $query .= "     AND VALUE(L1.REMARK8, '99') IN ('{$model->judgment_div}','19') ";
        } else if ($passcourse_div == "3" && $schoolwork_div == "2") {
            //特進選抜体育を実行の場合、確約が(07～08:進学体育)の者が対象
            $query .= "     AND VALUE(L1.REMARK8, '99') = '{$model->judgment_div}' ";
        } else {
            //体育以外を実行の場合、確約が(09～12:XX体育)の者は対象外
            $query .= "     AND VALUE(L3.SCHOOLWORK_DIV, '9') <> '2' ";
        }
        //確約が(20:不合格)の者は対象外・・・最後に不合格で更新
        //$query .= "     AND VALUE(L1.REMARK8, '99') <> '20' ";

        return $query;
    }

    function sim($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //仮判定データすべて削除
        $query  = " DELETE FROM ENTEXAM_JUDGE_TMP ";
        $db->query($query);

        //仮判定データ追加
        $query  = " INSERT INTO ENTEXAM_JUDGE_TMP( ";
        $query .= " entexamyear, ";
        $query .= " applicantdiv, ";
        $query .= " testdiv, ";
        $query .= " exam_type, ";
        $query .= " receptno, ";
        $query .= " examno, ";
        $query .= " judgediv, ";
        $query .= " registercd ";
        $query .= " ) ";
        $query .= " ( ";
        //シミュレーションSQL
        $query .= knjl070bQuery::simSql($model);
        $query .= " ) ";

        $db->query($query);

        $db->commit(); 
        Query::dbCheckIn($db);
    }

    //-----------------------------------
    //確定処理実行
    //-----------------------------------
    function decision($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //合格コース
        $course = $model->judgmentDivArray[$model->shdiv][$model->judgment_div]["PASS_COURSE"];
        //正規
        $isSeiki = ($model->shdiv == "1") ? true : false;

        //シミュレーション結果を反映
        $result = $db->query(knjl070bQuery::getJudgeTmp($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //正規を実行
            if ($isSeiki) {
                //基礎データ
                knjl070bQuery::updBase($db, $model, $row, $course);
                //受付データ
                knjl070bQuery::updRecept($db, $model, $row);
                //受付詳細データ SEQ'001'
                knjl070bQuery::delReceptDetail($db, $model, $row, '001');
                knjl070bQuery::insReceptDetail($db, $model, $row, '001', $course);
                //受付詳細データ SEQ'002'
                knjl070bQuery::delReceptDetail($db, $model, $row, '002');
            //単切を実行・・・合格者のみレコードを追加
            } else if ($row["JUDGEDIV"] == "1") {
                //基礎データ
                knjl070bQuery::updBaseTangan($db, $model, $row, $course);
                //受付詳細データ SEQ'002'
                knjl070bQuery::delReceptDetail($db, $model, $row, '002'); //DBエラー回避のため
                knjl070bQuery::insReceptDetail($db, $model, $row, '002', $course);
            }
        }
        $result->free();

        //合格点マスタ
        knjl070bQuery::delPassingmark($db, $model, $model->shdiv, $course);
        knjl070bQuery::insPassingmark($db, $model, $course);
        //ピンク表示　確定ボタンを押すとそれより以前の判定結果をピンク表示する
        $bgcolor_pink = 1;
        foreach ($model->judgmentDivArray as $shdiv => $array) {
            foreach ($array as $judgmentDiv => $judgmentDivArray) {
                $pass_course = $judgmentDivArray["PASS_COURSE"];
                knjl070bQuery::updPassingmark($db, $model, $shdiv, $pass_course, $bgcolor_pink);
//echo "judgmentDiv=" . $judgmentDiv . ", pink=" . $bgcolor_pink . "<BR>";
                if ($judgmentDiv == $model->judgment_div) {
                    $bgcolor_pink = 0;
                }
            }
        }
        //単切を実行
        if (!$isSeiki) {
            //対象の正規合格コース
            $normal_pass_course = $model->judgmentDivArray[$model->shdiv][$model->judgment_div]["NORMAL_PASS_COURSE"];
            if (strlen($normal_pass_course)) {
                //合格点マスタから、正規合格者数などを取得
                $shdiv = "1"; //正規
                $query = knjl070bQuery::selectQueryPassingmark($model, $normal_pass_course, $shdiv);
                $passingRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if (isset($passingRow)) {
                    //シミュレーション結果から単願切換合格者数を取得
                    $query = knjl070bQuery::getJudgeTmpSuccess($model, $passingRow["BORDER_DEVIATION"]);
                    $jdgTmpRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                    //合格点マスタ更新。対象の正規合格者数から単願切換合格者数を除く
                    if (isset($jdgTmpRow)) {
                        knjl070bQuery::updPassingmarkSuccess($db, $model, $shdiv, $normal_pass_course, $passingRow, $jdgTmpRow);
                    }
                }
            }
        }

        $db->commit(); 
        Query::dbCheckIn($db);
    }

    //シミュレーション結果反映用
    function getJudgeTmp($model) {
        $query  = " SELECT ";
        $query .= "     ENTEXAMYEAR, ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     TESTDIV, ";
        $query .= "     EXAM_TYPE, ";
        $query .= "     RECEPTNO, ";
        $query .= "     EXAMNO, ";
        $query .= "     CASE WHEN JUDGEDIV IN ('3','5') THEN '1' ELSE JUDGEDIV END AS JUDGEDIV ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_JUDGE_TMP ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV    = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV         = '{$model->testdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     RECEPTNO ";
        return $query;
    }

    //基礎データ
    function updBase($db, $model, $row, $course) {
        $data = array();
        $data["SUC_COURSECD"][TEXT]     = $row["JUDGEDIV"] == "2" ? "" : substr($course, 0, 1);
        $data["SUC_MAJORCD"][TEXT]      = $row["JUDGEDIV"] == "2" ? "" : substr($course, 1, 3);
        $data["SUC_COURSECODE"][TEXT]   = $row["JUDGEDIV"] == "2" ? "" : substr($course, 4, 4);
        $data["JUDGEMENT"][TEXT]        = $row["JUDGEDIV"];
        $data["SUB_ORDER"][TEXT]        = "";
        $data["PROCEDUREDIV"][TEXT]     = "";
        $data["PROCEDUREDATE"][DATE]    = "";
        $data["ENTDIV"][TEXT]           = "";
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        //条件
        $where  = " WHERE ";
        $where .= "         ENTEXAMYEAR  = '".$row["ENTEXAMYEAR"]."' ";
        $where .= "     AND EXAMNO       = '".$row["EXAMNO"]."' ";
        //更新
        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
        $db->query($query);
    }

    //基礎データ
    function updBaseTangan($db, $model, $row, $course) {
        $data = array();
        $data["SUC_COURSECD"][TEXT]     = $row["JUDGEDIV"] == "2" ? "" : substr($course, 0, 1);
        $data["SUC_MAJORCD"][TEXT]      = $row["JUDGEDIV"] == "2" ? "" : substr($course, 1, 3);
        $data["SUC_COURSECODE"][TEXT]   = $row["JUDGEDIV"] == "2" ? "" : substr($course, 4, 4);
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        //条件
        $where  = " WHERE ";
        $where .= "         ENTEXAMYEAR  = '".$row["ENTEXAMYEAR"]."' ";
        $where .= "     AND EXAMNO       = '".$row["EXAMNO"]."' ";
        $where .= "     AND JUDGEMENT    = '1' ";
        //更新
        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
        $db->query($query);
    }

    //受付データ
    function updRecept($db, $model, $row) {
        $data = array();
        $data["JUDGEDIV"][TEXT]         = $row["JUDGEDIV"];
        $data["PROCEDUREDIV1"][TEXT]    = "";
        $data["PROCEDUREDATE1"][DATE]   = "";
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        //条件
        $where  = " WHERE ";
        $where .= "         ENTEXAMYEAR  = '".$row["ENTEXAMYEAR"]."' ";
        $where .= "     AND APPLICANTDIV = '".$row["APPLICANTDIV"]."' ";
        $where .= "     AND TESTDIV      = '".$row["TESTDIV"]."' ";
        $where .= "     AND EXAM_TYPE    = '".$row["EXAM_TYPE"]."' ";
        $where .= "     AND RECEPTNO     = '".$row["RECEPTNO"]."' ";
        //更新
        $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
        $db->query($query);
    }

    //受付詳細データ
    function delReceptDetail($db, $model, $row, $seq) {
        //削除
        $query  = " DELETE FROM ENTEXAM_RECEPT_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$row["ENTEXAMYEAR"]."' ";
        $query .= "     AND APPLICANTDIV = '".$row["APPLICANTDIV"]."' ";
        $query .= "     AND TESTDIV      = '".$row["TESTDIV"]."' ";
        $query .= "     AND EXAM_TYPE    = '".$row["EXAM_TYPE"]."' ";
        $query .= "     AND RECEPTNO     = '".$row["RECEPTNO"]."' ";
        $query .= "     AND SEQ          = '".$seq."' ";
        $db->query($query);
    }

    //受付詳細データ
    function insReceptDetail($db, $model, $row, $seq, $course) {
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $row["ENTEXAMYEAR"];
        $data["APPLICANTDIV"][TEXT]     = $row["APPLICANTDIV"];
        $data["TESTDIV"][TEXT]          = $row["TESTDIV"];
        $data["EXAM_TYPE"][TEXT]        = $row["EXAM_TYPE"];
        $data["RECEPTNO"][TEXT]         = $row["RECEPTNO"];
        $data["SEQ"][TEXT]              = $seq;
        $data["REMARK1"][TEXT]          = $row["JUDGEDIV"] == "2" ? "" : substr($course, 0, 1);
        $data["REMARK2"][TEXT]          = $row["JUDGEDIV"] == "2" ? "" : substr($course, 1, 3);
        $data["REMARK3"][TEXT]          = $row["JUDGEDIV"] == "2" ? "" : substr($course, 4, 4);
        $data["REMARK4"][TEXT]          = $row["JUDGEDIV"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        //追加
        $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT");
        $db->query($query);
    }

    //合格点マスタ
    function delPassingmark($db, $model, $shdiv, $course = "") {
        //削除
        $query  = " DELETE FROM ENTEXAM_PASSINGMARK_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV    = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV         = '{$model->testdiv}' ";
        $query .= "     AND SHDIV           = '{$shdiv}' ";
        if (strlen($course)) {
            $query .= "     AND COURSECD || MAJORCD || EXAMCOURSECD = '{$course}' ";
        }
        $db->query($query);
    }

    //合格点マスタ
    function insPassingmark($db, $model, $course) {
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]              = $model->year;
        $data["APPLICANTDIV"][TEXT]             = $model->applicantdiv;
        $data["TESTDIV"][TEXT]                  = $model->testdiv;
        $data["EXAM_TYPE"][TEXT]                = '1';
        $data["SHDIV"][TEXT]                    = $model->shdiv;
        $data["COURSECD"][TEXT]                 = substr($course, 0, 1);
        $data["MAJORCD"][TEXT]                  = substr($course, 1, 3);
        $data["EXAMCOURSECD"][TEXT]             = substr($course, 4, 4);
        $data["BORDER_DEVIATION"][NUMBER]       = $model->field["BORDER_DEVIATION"];
        $data["SUCCESS_CNT"][NUMBER]            = $model->field["SUCCESS_CNT"];
        $data["SUCCESS_CNT_SPECIAL"][NUMBER]    = $model->field["SUCCESS_CNT_SPECIAL"];
        $data["SUCCESS_CNT_SPECIAL2"][NUMBER]   = $model->field["SUCCESS_CNT_SPECIAL2"];
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][FUNC]                  = "sysdate()";
        //追加
        $query = Query::insertSQL($data, "ENTEXAM_PASSINGMARK_MST");
        $db->query($query);
    }

    //合格点マスタ
    function updPassingmark($db, $model, $shdiv, $course, $bgcolor_pink) {
        $data = array();
        $data["CAPA_CNT"][NUMBER]           = $bgcolor_pink;
        //条件
        $where  = " WHERE ";
        $where .= "         ENTEXAMYEAR     = '{$model->year}' ";
        $where .= "     AND APPLICANTDIV    = '{$model->applicantdiv}' ";
        $where .= "     AND TESTDIV         = '{$model->testdiv}' ";
        $where .= "     AND SHDIV           = '{$shdiv}' ";
        $where .= "     AND COURSECD || MAJORCD || EXAMCOURSECD = '{$course}' ";
        //更新
        $query = Query::updateSQL($data, "ENTEXAM_PASSINGMARK_MST", $where);
        $db->query($query);
    }

    //「単願切換」確定の時、対象の正規合格者数から単願切換合格者数を除く。その単願切換合格者数を取得する。
    function getJudgeTmpSuccess($model, $border_deviation) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS SUCCESS_CNT, "; //合格数
        $query .= "     COUNT(CASE WHEN T1.AVARAGE4 >= {$border_deviation} AND L1.REMARK8 = L4.JUDGMENT_DIV THEN 1 END) AS SUCCESS_CNT_SPECIAL, "; //合格点以上(確約有)
        $query .= "     COUNT(CASE WHEN T1.AVARAGE4 <  {$border_deviation} AND L1.REMARK8 = L4.JUDGMENT_DIV THEN 1 END) AS SUCCESS_CNT_SPECIAL2 "; //合格点未満(確約有)
        $query .= " FROM ";
        $query .= "     ENTEXAM_JUDGE_TMP T0 ";
        $query .= "     INNER JOIN ENTEXAM_RECEPT_DAT T1 ";
        $query .= "         ON  T1.ENTEXAMYEAR = T0.ENTEXAMYEAR ";
        $query .= "         AND T1.APPLICANTDIV = T0.APPLICANTDIV ";
        $query .= "         AND T1.TESTDIV = T0.TESTDIV ";
        $query .= "         AND T1.EXAM_TYPE = T0.EXAM_TYPE ";
        $query .= "         AND T1.RECEPTNO = T0.RECEPTNO ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "         ON  T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.EXAMNO = T1.EXAMNO ";
                        //確約
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT L1 ";
        $query .= "         ON  L1.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "         AND L1.EXAMNO = T2.EXAMNO ";
        $query .= "         AND L1.SEQ = '004' ";
                        //合否詳細区分マスタの正規合格コース
        $query .= "     LEFT JOIN ENTEXAM_COURSE_JUDGMENT_MST L3 ";
        $query .= "         ON  L3.ENTEXAMYEAR = L1.ENTEXAMYEAR ";
        $query .= "         AND L3.JUDGMENT_DIV = L1.REMARK8 ";
                        //正規判定結果データの正規合格コース
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT L2 ";
        $query .= "         ON  L2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND L2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND L2.TESTDIV = T1.TESTDIV ";
        $query .= "         AND L2.EXAM_TYPE = T1.EXAM_TYPE ";
        $query .= "         AND L2.RECEPTNO = T1.RECEPTNO ";
        $query .= "         AND L2.SEQ = '001' ";
                        //正規合格コース
        $query .= "     LEFT JOIN ENTEXAM_COURSE_JUDGMENT_MST L4 ";
        $query .= "         ON  L4.ENTEXAMYEAR = L2.ENTEXAMYEAR ";
        $query .= "         AND L4.NORMAL_PASSCOURSECD = L2.REMARK1 ";
        $query .= "         AND L4.NORMAL_PASSMAJORCD = L2.REMARK2 ";
        $query .= "         AND L4.NORMAL_PASSEXAMCOURSECD = L2.REMARK3 ";
        $query .= "         AND L4.TAKE_GENERAL_TEST_FLG = '1' ";  //一般入試(正規)
        $query .= " WHERE ";
        $query .= "         T0.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND T0.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND T0.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND T0.JUDGEDIV IN ('1','3','5') "; //単願切換合格者

        return $query;
    }

    //合格点マスタ
    function updPassingmarkSuccess($db, $model, $shdiv, $course, $passingRow, $jdgTmpRow) {
        $data = array();
        $data["SUCCESS_CNT"][NUMBER]            = $passingRow["SUCCESS_CNT"] - $jdgTmpRow["SUCCESS_CNT"];
        $data["SUCCESS_CNT_SPECIAL"][NUMBER]    = $passingRow["SUCCESS_CNT_SPECIAL"] - $jdgTmpRow["SUCCESS_CNT_SPECIAL"];
        $data["SUCCESS_CNT_SPECIAL2"][NUMBER]   = $passingRow["SUCCESS_CNT_SPECIAL2"] - $jdgTmpRow["SUCCESS_CNT_SPECIAL2"];
        //条件
        $where  = " WHERE ";
        $where .= "         ENTEXAMYEAR     = '{$model->year}' ";
        $where .= "     AND APPLICANTDIV    = '{$model->applicantdiv}' ";
        $where .= "     AND TESTDIV         = '{$model->testdiv}' ";
        $where .= "     AND SHDIV           = '{$shdiv}' ";
        $where .= "     AND COURSECD || MAJORCD || EXAMCOURSECD = '{$course}' ";
        //更新
        $query = Query::updateSQL($data, "ENTEXAM_PASSINGMARK_MST", $where);
        $db->query($query);
    }
}
?>
