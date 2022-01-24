<?php

require_once('for_php7.php');

class knjd183bForm3
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

        //順位の基準点ラジオボタン 1:総合点 2:平均点
        $opt001 = array(1, 2);
        $model->field["SEQ001"] = ($model->field["SEQ001"]) ? $model->field["SEQ001"] : ($dataTmp["001"]["REMARK1"] ? $dataTmp["001"]["REMARK1"] : "1");
        $extra = array("id=\"SEQ0011\"", "id=\"SEQ0012\"");
        $radioArray = knjCreateRadio($objForm, "SEQ001", $model->field["SEQ001"], $extra, $opt001, get_count($opt001));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //帳票パターンラジオボタン 1:A 2:B 3:C 4:D 5:E
        $opt002 = array(1, 2, 3, 4, 5);
        $model->field["SEQ002"] = ($model->field["SEQ002"]) ? $model->field["SEQ002"] : ($dataTmp["002"]["REMARK1"] ? $dataTmp["002"]["REMARK1"] : "1");
        $extra = array("id=\"SEQ0021\" onclick=\"setPattern()\"", "id=\"SEQ0022\" onclick=\"setPattern()\"", "id=\"SEQ0023\" onclick=\"setPattern()\"", "id=\"SEQ0024\" onclick=\"setPattern()\"", "id=\"SEQ0025\" onclick=\"setPattern()\"");
        $radioArray = knjCreateRadio($objForm, "SEQ002", $model->field["SEQ002"], $extra, $opt002, get_count($opt002));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //平均・席次・偏差値ラジオボタン 1:学年 2:クラス 3:コース 4:学科
        $opt003 = array(1, 2, 3, 4);
        $model->field["SEQ003"] = ($model->field["SEQ003"]) ? $model->field["SEQ003"] : ($dataTmp["003"]["REMARK1"] ? $dataTmp["003"]["REMARK1"] : "1");
        $extra = array("id=\"SEQ0031\"", "id=\"SEQ0032\"", "id=\"SEQ0033\"", "id=\"SEQ0034\"");
        $radioArray = knjCreateRadio($objForm, "SEQ003", $model->field["SEQ003"], $extra, $opt003, get_count($opt003));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //最高点・追指導（パターンＡのみ）1:最高点 2:追指導
        $opt004 = array(1, 2);
        $model->field["SEQ004"] = ($model->field["SEQ004"]) ? $model->field["SEQ004"] : ($dataTmp["004"]["REMARK1"] ? $dataTmp["004"]["REMARK1"] : "1");
        $extra = array("id=\"SEQ0041\"", "id=\"SEQ0042\"",);
        $radioArray = knjCreateRadio($objForm, "SEQ004", $model->field["SEQ004"], $extra, $opt004, get_count($opt004));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //追指導表示（パターンＡ以外）
        makeCheckBox($objForm, $model, $dataTmp, $arg, '005', 'REMARK1', 'SEQ005');

        //出欠の記録（パターンＡのみ）1:保護者からのコメント欄 2:出欠の記録（考査ごと）
        $opt006 = array(1, 2);
        $model->field["SEQ006"] = ($model->field["SEQ006"]) ? $model->field["SEQ006"] : ($dataTmp["006"]["REMARK1"] ? $dataTmp["006"]["REMARK1"] : "1");
        $extra = array("id=\"SEQ0061\"", "id=\"SEQ0062\"",);
        $radioArray = knjCreateRadio($objForm, "SEQ006", $model->field["SEQ006"], $extra, $opt006, get_count($opt006));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //遅刻・早退回数 表示なし（パターンＣのみ）
        makeCheckBox($objForm, $model, $dataTmp, $arg, '007', 'REMARK1', 'SEQ007');

        //出席すべき時数なし（パターンＣのみ）
        makeCheckBox($objForm, $model, $dataTmp, $arg, '008', 'REMARK1', 'SEQ008', true);

        //合併元科目の学年評価・学年評定・出欠時数なし
        makeCheckBox($objForm, $model, $dataTmp, $arg, '009', 'REMARK1', 'SEQ009', true);

        //キャリアプラン（パターンＥのみ）
        makeCheckBox($objForm, $model, $dataTmp, $arg, '010', 'REMARK1', 'SEQ010');

        //最終考査表記しない
        makeCheckBox($objForm, $model, $dataTmp, $arg, '011', 'REMARK1', 'SEQ0111');
        //最終考査表記しない 成績のみ表記無し
        makeCheckBox($objForm, $model, $dataTmp, $arg, '011', 'REMARK2', 'SEQ0112');

        //LHR、生徒会活動、学校行事表示なし（パターンＥのみ）
        makeCheckBox($objForm, $model, $dataTmp, $arg, '012', 'REMARK1', 'SEQ012');

        //考査名に学期名表示なし
        makeCheckBox($objForm, $model, $dataTmp, $arg, '013', 'REMARK1', 'SEQ013');

        //修得単位数を加算する
        makeCheckBox($objForm, $model, $dataTmp, $arg, '014', 'REMARK1', 'SEQ014');

        // 順位表記なし
        makeCheckBox($objForm, $model, $dataTmp, $arg, '015', 'REMARK1', 'SEQ015');

        // 欠点科目数表記
        makeCheckBox($objForm, $model, $dataTmp, $arg, '016', 'REMARK1', 'SEQ016');

        // 増加単位を加算する
        makeCheckBox($objForm, $model, $dataTmp, $arg, '017', 'REMARK1', 'SEQ017');

        // 保護者欄をカット
        makeCheckBox($objForm, $model, $dataTmp, $arg, '018', 'REMARK1', 'SEQ018');

        // 通信欄をカット
        makeCheckBox($objForm, $model, $dataTmp, $arg, '019', 'REMARK1', 'SEQ019');

        // 出欠備考（B、Dパターン）
        makeCheckBox($objForm, $model, $dataTmp, $arg, '020', 'REMARK1', 'SEQ020');

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["pattern"] = $model->pattern;

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd183bForm3.html", $arg);
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
