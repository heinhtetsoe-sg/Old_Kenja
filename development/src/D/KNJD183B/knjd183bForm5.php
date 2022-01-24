<?php

require_once('for_php7.php');

class knjd183bForm5
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd183bindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //初期化
        if ($model->cmd == "reset") {
            unset($model->field);
        }

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //校種コンボ
        if ($model->SchKindOpt[0] != "") {
            $arg["schkind"] = "1";
            //校種コンボ
            $extra = "onchange=\"return btn_submit('changeKind');\"";
            $arg["data"]["SCHOOL_KIND"] = knjCreateCombo($objForm, "SCHOOL_KIND", $model->schoolKind, $model->SchKindOpt, $extra, 1);
        }

        //データ取得
        $dataTmp = array();
        $query = knjd183bQuery::getHreportConditionDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dataTmp[$row["SEQ"]] = $row;
        }
        $result->free();

        //帳票パターンラジオボタン 1:A 2:B 3:C 4:D 5:E
        $opt001 = array(1, 2, 3, 4, 5);
        $model->field["SEQ001"] = ($model->field["SEQ001"]) ? $model->field["SEQ001"] : ($dataTmp["001"]["REMARK1"] ? $dataTmp["001"]["REMARK1"] : "1");
        $extra = array("id=\"SEQ0011\" onclick=\"setPattern()\""
                     , "id=\"SEQ0012\" onclick=\"setPattern()\""
                     , "id=\"SEQ0013\" onclick=\"setPattern()\""
                     , "id=\"SEQ0014\" onclick=\"setPattern()\""
                     , "id=\"SEQ0015\" onclick=\"setPattern()\"");
        $radioArray = knjCreateRadio($objForm, "SEQ001", $model->field["SEQ001"], $extra, $opt001, get_count($opt001));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }


        //成績の表示項目 学期（パターンA・Bのみ）
        for ($i = 0; $i < get_count($model->semesterList); $i++) {
            $semester = $model->semesterList[$i];
            if ($semester["SEMESTER"] == $model->semesterMax) {
                continue;
            }
            makeSemesterCheckBox($objForm, $model, $dataTmp, $arg, '002', 'SEQ002', $semester);
        }

        //合計点 表記なし（パターンA・B・Cのみ）
        makeCheckBox($objForm, $model, $dataTmp, $arg, '003', 'REMARK1', 'SEQ003');

        //平均点 1:表記なし・2:表記あり
        $opt004 = array(1, 2);
        $model->field["SEQ004"] = ($model->field["SEQ004"]) ? $model->field["SEQ004"] : ($dataTmp["004"]["REMARK1"] ? $dataTmp["004"]["REMARK1"] : "1");
        $extra = array("id=\"SEQ0041\" onclick=\"setPattern()\""
                     , "id=\"SEQ0042\" onclick=\"setPattern()\"");
        $radioArray = knjCreateRadio($objForm, "SEQ004", $model->field["SEQ004"], $extra, $opt004, get_count($opt004));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //修得単位合計 表記なし（パターンA・Bのみ）
        makeCheckBox($objForm, $model, $dataTmp, $arg, '005', 'REMARK1', 'SEQ005');

        //出欠の記録の表示項目 学期（パターンC・Eのみ）
        for ($i = 0; $i < get_count($model->semesterList); $i++) {
            $semester = $model->semesterList[$i];
            if ($semester["SEMESTER"] == $model->semesterMax) {
                continue;
            }
            makeSemesterCheckBox($objForm, $model, $dataTmp, $arg, '006', 'SEQ006', $semester);
        }

        //留学中の授業日数 表記なし（パターンC・E以外）
        makeCheckBox($objForm, $model, $dataTmp, $arg, '007', 'REMARK1', 'SEQ007');
        //LHR欠課時数 表記なし（パターンC・E以外）
        makeCheckBox($objForm, $model, $dataTmp, $arg, '008', 'REMARK1', 'SEQ008');
        //行事欠課時数 表記なし（パターンC・E以外）
        makeCheckBox($objForm, $model, $dataTmp, $arg, '009', 'REMARK1', 'SEQ009');
        //総合学習の時間 表記なし（パターンD以外）
        makeCheckBox($objForm, $model, $dataTmp, $arg, '010', 'REMARK1', 'SEQ010');
        //各教科の観点 出力なし（パターンD・Eのみ）
        makeCheckBox($objForm, $model, $dataTmp, $arg, '011', 'REMARK1', 'SEQ011');

        //担任項目名ラジオボタン 1:担任 2:チューター
        $opt012 = array(1, 2);
        $model->field["SEQ012"] = ($model->field["SEQ012"]) ? $model->field["SEQ012"] : ($dataTmp["012"]["REMARK1"] ? $dataTmp["012"]["REMARK1"] : "1");
        $extra = array("id=\"SEQ0121\"", "id=\"SEQ0122\"");
        $radioArray = knjCreateRadio($objForm, "SEQ012", $model->field["SEQ012"], $extra, $opt012, get_count($opt012));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //教科名ラジオボタン 1:総合的な学習(探究)の時間 2:課題研究
        $opt013 = array(1, 2);
        $model->field["SEQ013"] = ($model->field["SEQ013"]) ? $model->field["SEQ013"] : ($dataTmp["013"]["REMARK1"] ? $dataTmp["013"]["REMARK1"] : "1");
        $extra = array("id=\"SEQ0131\"", "id=\"SEQ0132\"");
        $radioArray = knjCreateRadio($objForm, "SEQ013", $model->field["SEQ013"], $extra, $opt013, get_count($opt013));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        $semester = "";
        for ($i = 0; $i < get_count($model->semesterList); $i++) {
            if ($semester) {
                $semester .= ",";
            }
            $semester .= $model->semesterList[$i]["SEMESTER"];
        }
        knjCreateHidden($objForm, "semester", $semester);
        knjCreateHidden($objForm, "semesterMax", $model->semesterMax);

        //DB切断
        Query::dbCheckIn($db);

        $arg["pattern"] = $model->pattern;

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd183bForm5.html", $arg);
    }
}

//チェックボックス作成
function makeCheckBox($objForm, $model, $dataTmp, &$arg, $fieldSEQ, $remark, $id, $defCheck = false)
{
    $extra = "";
    if ($model->field[$id] == "1" ||
        (get_count($dataTmp) > 0 && $dataTmp[$fieldSEQ][$remark] == "1") ||
        (get_count($dataTmp) == 0 && $defCheck)) {
        $extra = "checked";
    }
    $extra .= " id=\"{$id}\"";
    $arg["data"][$id] = knjCreateCheckBox($objForm, $id, "1", $extra, "");
}
//学期チェックボックス作成
function makeSemesterCheckBox($objForm, $model, $dataTmp, &$arg, $fieldSEQ, $id, $semester, $defCheck = false)
{
    $extra = "";
    if ($model->field[$id] == "1" ||
        (get_count($dataTmp) > 0 && $dataTmp[$fieldSEQ]['REMARK'.$semester["SEMESTER"]] == "1") ||
        (get_count($dataTmp) == 0 && $defCheck)) {
        $extra = "checked";
    }

    $extra .= " id=\"".$id.$semester["SEMESTER"]."\"";
    $setRadio = array();
    $setRadio["VALUE"] = knjCreateCheckBox($objForm, $id.$semester["SEMESTER"], "1", $extra, "");
    $setRadio["LABEL"] = "<LABEL for=\"".$id.$semester["SEMESTER"]."\">".$semester["SEMESTERNAME"]."</LABEL>";
    $arg["data"][$id][] = $setRadio;
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //前年度からコピー
    $extra = "onclick=\"return btn_submit('copy');\"";
    $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);
    //更新
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
