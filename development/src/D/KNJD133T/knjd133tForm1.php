<?php

require_once('for_php7.php');

class knjd133tForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期コンボ
        knjCreateHidden($objForm, "KNJD133T_semesCombo", $model->Properties["KNJD133T_semesCombo"]);
        $query = knjd133tQuery::getSemesterCmb();
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->semester, $extra, 1, "");

        //名称マスタ D校種08チェック
        $model->che_school_kind = "D".$model->field["SCHOOL_KIND"]."08";
        $model->count = $db->getone(knjd133tquery::getNameMstche($model));

        //科目コンボ
        $query = knjd133tQuery::selectSubclassQuery($model);
        $extra = "onchange=\"return btn_submit('subclasscd');\"";
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "BLANK");

        //講座コンボ
        $query = knjd133tQuery::selectChairQuery($model);
        $extra = "onchange=\"return btn_submit('chaircd');\"";
        makeCmb($objForm, $arg, $db, $query, "CHAIRCD", $model->field["CHAIRCD"], $extra, 1, "BLANK");

        //学期開始日、終了日
        $seme = $db->getRow(knjd133tQuery::getSemester($model), DB_FETCHMODE_ASSOC);
        //学籍処理日が学期範囲外の場合、学期終了日を使用する。
        if ($seme["SDATE"] <= CTRL_DATE && CTRL_DATE <= $seme["EDATE"]) {
            $execute_date = CTRL_DATE;  //初期値
        } else {
            $execute_date = $seme["EDATE"];     //初期値
        }

        //コメントタイトル
        $moji = $model->moji;
        $gyou = $model->gyou;
        $arg["COMMENT_TITLE"] = $model->commentTitle;
        $arg["MOJI_SIZE"] = "(全角{$moji}文字×{$gyou}行まで)";

        //初期化
        $model->data = array();
        $counter = 0;
        //一覧表示
        $colorFlg = false;
        $query = knjd133tQuery::selectQuery($model, $execute_date);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学籍番号を配列で取得
            $model->data["SCHREGNO"][] = $row["SCHREGNO"];

            if ($counter % 5 == 0) {
                $colorFlg = !$colorFlg;
            }

            //クラス-出席番号
            $row["ATTENDNO"] = $row["HR_NAME"]."-".$row["ATTENDNO"];

            //コメント
            $row["COMMENT1"] = $model->cmd != "csvInputMain" ? $row["COMMENT1"] : $model->data_arr[$row["SCHREGNO"]]["COMMENT"];
            $value = (!isset($model->warning)) ? $row["COMMENT1"] : $model->fields["COMMENT_".$row["SCHREGNO"]];
            $extra = "";
            $row["COMMENT"] = knjCreateTextArea($objForm, "COMMENT_".$row["SCHREGNO"], 10, $moji * 2, "", $extra, $value);

            knjCreateHidden($objForm, "ATTENDO_NAME_".$row["SCHREGNO"], $row["ATTENDNO"]); //エラーメッセージ用

            //背景色
            $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";

            $counter++;
            $arg["data"][] = $row;
        }

        //CSV処理作成
        makeCsv($objForm, $arg, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
        knjCreateHidden($objForm, "itemMstJson", $model->itemMstJson);

        $arg["IFRAME"] = View::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjd133tindex.php", "", "main");
        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd133tForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="")
{
    $opt = array();
    $value_flg = false;
    if ($blank) {
        $opt[] = array('label' => "", 'value' => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value != "" && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//CSV処理作成
function makeCsv(&$objForm, &$arg, $model)
{
    //ファイル
    $extra = "";
    $dis = "";
    if ($model->field["CHAIRCD"] == '') {
        $dis = " disabled=\"disabled\" ";
    }
    $arg["FILE"] = knjCreateFile($objForm, "FILE", $extra.$dis, 1024000);

    //ヘッダ有
    $extra = ($model->field["HEADER"] == "on" || $model->cmd == "main") ? "checked" : "";
    $extra .= " id=\"HEADER\"";
    $arg["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra, "");

    //取込ボタン
    $extra = "onclick=\"return btn_submit('csvInput');\"";
    $arg["btn_input"] = knjCreateBtn($objForm, "btn_input", "CSV取込", $extra.$dis);
    //出力ボタン
    $extra = "onclick=\"return btn_submit('csvOutput');\"";
    $arg["btn_output"] = knjCreateBtn($objForm, "btn_output", "CSV出力", $extra.$dis);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{

    //更新ボタン
    $extra = (AUTHORITY >= DEF_UPDATE_RESTRICT) ? "onclick=\"return btn_submit('update');\"" : "disabled";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
