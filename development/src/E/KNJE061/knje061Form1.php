<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knje061Form1
{
    public function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $db = Query::dbCheckOut();
        $objForm = new form();
        
        //年度学期表示
        $arg["SEMESTERNAME"] = CTRL_YEAR ."年度　" .CTRL_SEMESTERNAME;

        //ラジオボタンを作成
        $radioValue = array(1, 2, 3, 4);
        if (!$model->field["KIND"]) {
            $model->field["KIND"] = 1;
        }
        $extra = array("id=\"KIND1\" onclick =\"btn_disabled(this);\" checked", "id=\"KIND2\" onclick =\" btn_disabled(this);\"", "id=\"KIND3\" onclick =\" btn_disabled(this);\"", "id=\"KIND4\" onclick =\" btn_disabled(this);\"");
        $radioArray = knjCreateRadio($objForm, "KIND", $model->field["KIND"], $extra, $radioValue, get_count($radioValue));
        foreach ($radioArray as $key => $val) {
            $arg[$key] = $val;
        }
        $arg["jscript"] .= "btn_disabled(document.getElementById(\"KIND".$model->field["KIND"]."\"));";

        //成績データ　の下の観点データ作成（中学）チェックボックス
        if ($model->Properties["useFromStudyrecToJviewstat"] == 1) {
            $check = ($model->check_kanten == "on") ? "checked" : "";
            $extra = "id=\"CHECK_KANTEN\" ".$check;
            $arg["CHECK_KANTEN"] = knjCreateCheckBox($objForm, "CHECK_KANTEN", "on", $extra);
            $arg["useFromStudyrecToJviewstat"] = "1";
        }
        knjCreateHidden($objForm, "useFromStudyrecToJviewstat", $model->Properties["useFromStudyrecToJviewstat"]);

        //学年
        $model->regdGdat = array();
        $result = $db->query(knje061Query::selectQueryAnnual($model));
        $opt = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
            if (!isset($model->annual)) {
                $model->annual = $row["VALUE"];
            }
            $model->regdGdat[$row["VALUE"]]["SCHOOL_KIND"]  = $row["SCHOOL_KIND"];
        }

        //成績データの学年成績から観点データを生成
        if ($model->Properties["useKantenGakunenSeiseki"] == '1' || $model->Properties["useKantenGakunenSeiseki"] == '2' || $model->Properties["useKantenGakunenSeiseki"] == '3') {
            $arg["useKantenGakunenSeiseki"] = "1";
        }
        knjCreateHidden($objForm, "useKantenGakunenSeiseki", $model->Properties["useKantenGakunenSeiseki"]);

        //（観点から評定を生成）とコメント表示
        if ($model->Properties["useKantenToStudyRec"] == '1') {
            $arg["useKantenToStudyRec"] = "1";
        }

        if ($model->Properties["KNJE061_JVIEWSTAT_RECORD_DAT"] == 1) {
            $arg["kanten"] = "1";
        } else {
            $arg["kantenNo"] = "1";
        }

        //行動の記録他
        if ($model->Properties["useFromStudyrecToBehavior"] == 1) {
            $arg["useFromStudyrecToBehavior"] = "1";
        }

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "SCHOOL_KIND_HIDDEN",
                            "value"     => $model->regdGdat[$model->annual]["SCHOOL_KIND"]));
        
        $objForm->ae(array("type"       => "select",
                            "name"       => "ANNUAL",
                            "size"       => "1",
                            "value"      => $model->annual,
                            "extrahtml"  => "style=\"width:100px\" onChange=\"return btn_submit('annual');\"",
                            "options"    => $opt));
        
        $arg["ANNUAL"] = $objForm->ge("ANNUAL");

        $result = $db->query(knje061query::selectQueryHRClass($model));
        $opt = array();
        $opt[] = (array("label" => "", "value" => ""));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["HR_NAME"],
                           "value" => $row["HR_CLASS"]);
        }

        $objForm->ae(array("type"       => "select",
                            "name"       => "HR_CLASS",
                            "size"       => "1",
                            "value"      => $model->hr_class,
                            "extrahtml"  => "style=\"width:100px\" onChange=\"return btn_submit('hr_class');\"",
                            "options"    => $opt));

        $arg["HR_CLASS"] = $objForm->ge("HR_CLASS");
        
        //コース
        $maxdata; //コース名の最大値を格納する変数
        $volm; //コース名の最大値をbyte変換した変数
        $opt = array();
        $opt[] = (array("label" => "", "value" => ""));
        $value_flg = false;
        $result = $db->query(knje061query::selectCourse($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["COURSECODE"]."：".$row["COURSECODENAME"],
                           "value" => $row["COURSECODE"]);
            if ($value == $row["COURSECODE"]) {
                $value_flg = true;
            }
        }
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        if ($model->coursecode == "") {
            $extra = "style=\"width:220px\" onChange=\"return btn_submit('coursecode');\"";
        } else {
            $extra = "onChange=\"return btn_submit('coursecode');\"";
        }
        $arg["COURSECODE"] = knjCreateCombo($objForm, "COURSECODE", $model->coursecode, $opt, $extra, 1);

        
        //生徒
        $result = $db->query(knje061query::selectSchregno($model));
        $opt = array();
        $opt[] = (array("label" => "", "value" => ""));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["SCHREGNO"]."：".$row["NAME_SHOW"],
                           "value" => $row["SCHREGNO"]);
        }
        
        $objForm->ae(array("type"       => "select",
                            "name"       => "SCHREGNO",
                            "size"       => "1",
                            "value"      => $model->schregno,
                            "extrahtml"  => "style=\"width:300px\"",
                            "options"    => $opt));
        
        $arg["SCHREGNO"] = $objForm->ge("SCHREGNO");

        //集計月コンボ
        makeMonthSemeCmb($objForm, $arg, $db, $model);

        //履歴表示
        makeListRireki($objForm, $arg, $db, $model);

        // 通信制は出欠データの実行不可
        $query  = knje061Query::getZ010();
        $result = $db->query($query);
        $isTushin = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $isTushin = "1" == $row["NAMESPARE3"] ? "1": "";
        }
        $result->free();
        
        //ログイン年度が名称マスタ「E060」の登録されている年度の時は、【実行】ボタン使用不可とする
        $cntE060 = $db->getOne(knje061Query::getCntE060());

        Query::dbCheckIn($db);

        //ファイルからの取り込み
        $objForm->ae(array("type"       => "file",
                            "name"      => "FILE",
                            "size"      => 1024000,
                            "extrahtml" => "" ));

        $arg["FILE"] = $objForm->ge("FILE");

        //CSV取込みボタン
        $extra = "onclick=\"return btn_submit('exec');\"";
        if ($isTushin && $model->field["KIND"] == "1" || 0 < $cntE060) {
            $extra .= " disabled";
        }
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_exec",
                            "value"       => "実 行",
                            "extrahtml"   => $extra ));

        $arg["btn_exec"] = $objForm->ge("btn_exec");

        // CSVテンプレート書出しボタン
        $objForm->ae(array("type"      => "button",
                            "name"      => "btn_output",
                            "value"     => "テンプレート書出し",
                            "extrahtml" => "onclick=\"return btn_submit('output');\"" ));

        $arg["btn_output"] = $objForm->ge("btn_output");

        //終了ボタン
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ));

        $arg["btn_end"] = $objForm->ge("btn_end");

        //hidden
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "cmd"));
                            
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
        knjCreateHidden($objForm, "TUSIN", $isTushin);

        //ラジオボタン
        $arg["RADIO"] = $model->field;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knje061index.php", "", "main");
        
        //画面のリロード
        if ($model->cmd == "updMain") {
            $arg["reload"] = "parent.left_frame.btn_submit('list');";
        }
        
        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knje061Form1.html", $arg);
    }
}
//集計月コンボ
function makeMonthSemeCmb(&$objForm, &$arg, $db, &$model)
{
    $opt_month = array();

    $defaultValue = ""; //集計月の最後をデフォルト値とする
    $value_flg = false;
    $query = knje061query::selectSemesAll();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        for ($i = $row["S_MONTH"]; $i <= $row["E_MONTH"]; $i++) {
            if ($i > 12) {
                $month = $i - 12;
            } else {
                $month = $i;
            }

            $label = $month . "月 (" . $row["SEMESTERNAME"] . ") ";
            $value = sprintf('%02d', $month) . '-' . $row["SEMESTER"];

            $opt_month[] = array("label" => $label,
                                 "value" => $value);

            if ($model->target_month == $value) {
                $value_flg = true;
            }
            $defaultValue = $value;
        }
    }
    $result->free();
    $model->target_month = ($model->target_month && $value_flg) ? $model->target_month : $defaultValue;

    $arg["TARGET_MONTH"] = knjCreateCombo($objForm, "TARGET_MONTH", $model->target_month, $opt_month, "", 1);
}
//履歴表示
function makeListRireki(&$objForm, &$arg, $db, &$model)
{
    //履歴一覧
    $query = knje061Query::getListRireki($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row["CALC_DATE"] = str_replace("-", "/", $row["CALC_DATE"]);
        $row["CHECK_KANTEN"] = strlen($row["CHECK_KANTEN"]) ? "レ" : "";
        if ($row["KIND"] == "1") {
            $row["KIND_NAME"] = "出欠";
        }
        if ($row["KIND"] == "2") {
            $row["KIND_NAME"] = "成績";
        }
        if ($row["KIND"] == "3") {
            $row["KIND_NAME"] = "観点";
        }
        if ($row["KIND"] == "4") {
            $row["KIND_NAME"] = "行動";
        }
        if ($row["METHOD"] == "2") {
            $row["METHOD_CREATEDIV"] = "取込";
        } else {
            if ($row["CREATEDIV"] == "") {
                $row["METHOD_CREATEDIV"] = "新規";
            }
            if ($row["CREATEDIV"] == "1") {
                $row["METHOD_CREATEDIV"] = "新規";
            }
            if ($row["CREATEDIV"] == "2") {
                $row["METHOD_CREATEDIV"] = "追加";
            }
            if ($row["CREATEDIV"] == "3") {
                $row["METHOD_CREATEDIV"] = "上書";
            }
        }
        if ($row["RANGE"] == "1") {
            $row["RANGE_NAME"] = "処理年度";
        }
        if ($row["RANGE"] == "2") {
            $row["RANGE_NAME"] = "過年度含む";
        }
        //条件範囲
        $target = "";
        if (strlen($row["GRADE"])) {
            $target = "学年";
        }
        if (strlen($row["HR_CLASS"])) {
            $target = "年組";
        }
        if (strlen($row["COURSECODE"])) {
            $target = "コース";
        }
        if (strlen($row["SCHREGNO"])) {
            $target = "生徒";
        }
        $targetName = "";
        $seq = "、";
        if (strlen($row["GRADE"])) {
            $targetName  = $row["GRADE_NAME1"];
        }
        if (strlen($row["HR_CLASS"])) {
            $targetName .= $seq . $row["HR_NAME"];
        }
        if (strlen($row["COURSECODE"])) {
            $targetName .= $seq . $row["COURSECODENAME"];
        }
        if (strlen($row["SCHREGNO"])) {
            $targetName .= $seq . $row["NAME_SHOW"];
        }
        $row["TARGET"] = strlen($target) ? $target . "（" . $targetName . "）" : "";
        //仮評定
        $row["PROV_INFO"] = "";
        if ($row["PROV_FLG"] == "1") {
            $row["PROV_INFO"] = "（仮評定:".$row["TESTITEMNAME"]."）";
        } elseif ($row["PROV_FLG"] == "2") {
            $row["PROV_INFO"] = "（本評定）";
        }

        $arg['data2'][] = $row;
    }
    $result->free();
}
