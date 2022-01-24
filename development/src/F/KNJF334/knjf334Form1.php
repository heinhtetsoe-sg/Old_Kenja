<?php

require_once('for_php7.php');

class knjf334Form1
{
    function main(&$model) {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjf334index.php", "", "edit");

        //DB接続
        $db     = Query::dbCheckOut();

        //教育委員会判定
        $query = knjf334Query::z010Abbv1();
        $model->z010Abbv1 = $db->getOne($query);

        if ($model->z010Abbv1 == "1" || $model->z010Abbv1 == "2") {
            $arg["Z010ABBV1"] = "1";
        }

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //年度
        $arg["TITLE"] = "疾病等結果一覧";

        //更新する内容があった場合に日付を入力させるポップアップ
        if ($model->cmd == "fixed") {
            $arg["reload"] = " fixed('".REQUESTROOT."')";
        }

        //確定日付ありは入力不可
        $disabled = $model->fixedData ? " disabled " : "";

        //県への報告用登録日付(テーブルは報告履歴テーブルのみ)
        $arg["EXECUTE_DATE"] = View::popUpCalendar($objForm, "EXECUTE_DATE", $model->execute_date, "");

        unset($model->fields["CODE"]);
        $count = 0;
        $bifKey = "";
        $query = knjf334Query::ReadQuery($model);

        $result = $db->query($query);
        $totalarry = array();
        $updtotalflg = "";
        while($Row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $model->fields["CODE"][] = $Row["GRADE"];
            if ($Row["GRADE"] == "99" && $model->cmd == "recalc") continue;  //再計算なら、集計するので出力せずに飛ばす。
            if ($Row["GRADE"] == "99") $updtotalflg = "1"; //合計がデータにあることをチェック
            //text
            //DBから取得してくる箇所
            foreach ($model->dataField as $name) {
                $setName = "DATA".$name;
                if (!in_array($name, $model->iptField)) {
                    //再計算または"初期化タイミング(初期画面orFIXED値設定or取消)"ならDBから取得
                    if ($model->fixedData != "" || $model->cmd == "recalc" || $model->firstFlg == "1") {
                        $val = ($Row[$setName] == "")? "0" : $Row[$setName] ;
                    } else {
                        $val = ($model->updField[$name."_".$Row["GRADE"]] == "") ? "0" : $model->updField[$name."_".$Row["GRADE"]];
                    }
                } else {
                    //手入力前提の項目なので、"初期化タイミング(初期画面orFIXED値設定or取消)"ならDBから取得
                    if ($model->fixedData != "" || $model->firstFlg == "1") {
                        $val = ($Row[$setName] == "")? "0" : $Row[$setName];
                    } else {
                        //それ以外は、前回からの値を設定
                        $val = ($model->updField[$name."_".$Row["GRADE"]] == "") ? "0" : $model->updField[$name."_".$Row["GRADE"]];
                    }
                }
                $extra = "id=\"".$setName."_".$Row["GRADE"]."\" style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\" ";
                $Row[$setName] = knjCreateTextBox($objForm, $val, $setName."_".$Row["GRADE"], "3", "3", $disabled.$extra, "1");
                //各項目ごとに校種、GRADE_CDを保持必要はないが、ここで展開しておいてそのまま登録するために行っている。
                knjCreateHidden($objForm, "HID_SCHKIND_".$setName."_".$Row["GRADE"], $Row["SCHOOL_KIND"]);
                knjCreateHidden($objForm, "HID_GCD_".$setName."_".$Row["GRADE"], $Row["GRADE_CD"]);
                $totalarry[$setName] = $totalarry[$setName] + intval($val);
            }

            $Row["ROWSPAN"] = "1";
            $bifKey = $Row["GRADE"];

            //コードのバックアップ後に出力名称を設定
            $Row["GRADE"] = $Row["GRADE_NAME1"];
            $arg["data"][] = $Row;
            $count++;
        }

        //合計(上記ループで出力しなかった場合に下を実行)
        if ($updtotalflg != "1") {
            $totalarry["GRADE"] = "99";
            $model->fields["CODE"][] = $totalarry["GRADE"];
            $Row = $totalarry;
            foreach ($model->dataField as $name) {
                $setName = "DATA".$name;
                $extra = "id=\"".$setName."_".$Row["GRADE"]."\" style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\" ";
                $Row[$setName] = knjCreateTextBox($objForm, $totalarry[$setName], $setName."_".$Row["GRADE"], "3", "3", $disabled.$extra, "1");
            }
            $Row["GRADE"] = "合計";
            $Row["ROWSPAN"] = "1";
            $arg["data"][] = $Row;
        }
        

        //報告済み日付
        $query = knjf334Query::getReport($model);
        $setExeDate = "";
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setExeDate .= $sep.str_replace("-", "/", $row["VALUE"]);
            $sep = ",";
        }
        $result->free();
        $arg["EXE_DATES"] = $setExeDate;

        //県への報告
        $extra = "onclick=\"return btn_submit('houkoku');\"";
        $arg["btn_houkoku"] = knjCreateBtn($objForm, "btn_houkoku", "県への報告", $extra);
        //確定データ
        $query = knjf334Query::getFixed($model);
        $extra = "onChange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "FIXED_DATA", $model->fixedData, $extra, 1, 1);

        //再計算ボタン
        $extra = $disabled."onclick=\"return btn_submit('recalc');\"";
        $arg["btn_recalc"] = knjCreateBtn($objForm, "btn_recalc", "再計算", $extra);
        //更新ボタン
        $extra = $disabled."onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消ボタン
        $extra = $disabled."onclick=\"return btn_submit('reset');\"";
        $arg["btn_can"] = knjCreateBtn($objForm, "btn_can", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //印刷
        $extra = "onclick=\"newwin('".SERVLET_URL."');\"";
        $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $extra);

        //CSVボタン
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力", $extra);

        $model->firstFlg = "2";

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "FIXED_DATE");
        knjCreateHidden($objForm, "UPDATED[]", $model->updated);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "PRGID", "KNJF334");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "SCHOOLCD", $model->schoolcd);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
        knjCreateHidden($objForm, "FIRST_FLG", $model->firstFlg);

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjf334Form1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank != "") $opt[] = array('label' => "", 'value' => "");
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row["LABEL"] = $name == "FIXED_DATA" ? str_replace("-", "/", $row["LABEL"]) : $row["LABEL"];
        $row["VALUE"] = $name == "FIXED_DATA" ? str_replace("-", "/", $row["VALUE"]) : $row["VALUE"];
        $opt[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "YEAR") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
