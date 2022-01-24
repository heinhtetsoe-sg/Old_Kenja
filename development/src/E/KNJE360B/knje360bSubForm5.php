<?php

require_once('for_php7.php');

class knje360bSubForm5
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform5", "POST", "knje360bindex.php", "", "subform5");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $info = $db->getRow(knje360bQuery::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban  = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"];

        //その他進路コンボ表示設定
        $model->getname = $db->getOne(knje360bQuery::getHyoujiset());
        if ($model->getname === 'kyoto') {
            $arg["SONOTA_SHINRO"] = '1';
        }

        //警告メッセージを表示しない場合
        if ($model->cmd == "subform5A" || $model->cmd == "subform5_clear") {
            if (isset($model->schregno) && !isset($model->warning) && $model->seq) {
                $Row = $db->getRow(knje360bQuery::getSubQuery5($model), DB_FETCHMODE_ASSOC);
                $arg["NOT_WARNING"] = 1;
            } else {
                $Row =& $model->field;
            }
        } else {
            $Row =& $model->field;
        }

        //その他進路(その他進路の表示があるときのみ)
        if ($model->getname === 'kyoto') {
            $query = knje360bQuery::getNameMst('E022');
            makeCmb($objForm, $arg, $db, $query, "SENKOU_KIND_SUB", $Row["SENKOU_KIND_SUB"], "", 1, 1);
        }

        //登録日
        $Row["TOROKU_DATE"] = ($Row["TOROKU_DATE"] == "") ? str_replace("-", "/", CTRL_DATE) : str_replace("-", "/", $Row["TOROKU_DATE"]);
        $arg["data"]["TOROKU_DATE"] = View::popUpCalendar($objForm, "TOROKU_DATE", $Row["TOROKU_DATE"]);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        if (get_count($model->warning) == 0 && $model->cmd != "subform1_clear") {
            $arg["next"] = "NextStudent(0);";
        } elseif ($model->cmd == "subform1_clear") {
            $arg["next"] = "NextStudent(1);";
        }

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje360bSubForm5.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    $disabled = ($model->mode == "grd") ? " disabled" : "";
    //追加ボタンを作成する
    $arg["button"]["btn_insert"]  = knjCreateBtn($objForm, "btn_insert", "追 加", "onclick=\"return btn_submit('subform5_insert');\"");
    //追加後前の生徒へを作成する
    $extra = " onclick=\"return updateNextStudent('".$model->schregno."', 1);\" style=\"width:130px\"";
    $arg["button"]["btn_up_pre"]  = knjCreateBtn($objForm, "btn_up_pre", "追加後前の{$model->sch_label}へ", $extra.$disabled);
    //追加後次の生徒へを作成する
    $extra = " onclick=\"return updateNextStudent('".$model->schregno."', 0);\" style=\"width:130px\"";
    $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_next", "追加後次の{$model->sch_label}へ", $extra.$disabled);
    //更新ボタンを作成する
    $extra = ($model->seq == "") ? "disabled" : "onclick=\"return btn_submit('subform5_update');\"";
    $arg["button"]["btn_update"]  = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //クリアボタンを作成する
    $extra = ($model->seq == "") ? "disabled" : "onclick=\"return btn_submit('subform5_clear');\"";
    $arg["button"]["btn_reset"]   = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //戻るボタン
    $arg["button"]["btn_end"]     = knjCreateBtn($objForm, "btn_end", "戻 る", "onclick=\"return btn_submit('edit');\"");
    //進路相談ボタン
    $extra = "style=\"height:30px;background:#FFE4E1;color:#FF0000;font:bold\" onclick=\"return btn_submit('subform4');\"";
    $arg["button"]["btn_subform4"] = KnjCreateBtn($objForm, "btn_subform4", "進路相談", $extra.$disabled);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $space = "")
{
    $opt = array();
    if ($space) {
        $opt[] = array('label' => "", 'value' => "");
    }
    $value_flg = false;
    $result1   = $db->query($query);
    while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row1["LABEL"],
                       'value' => $row1["VALUE"]);
        if ($value == $row1["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "SEQ", $model->seq);
    knjCreateHidden($objForm, "cmd");

    $semes = $db->getRow(knje360bQuery::getSemesterMst(), DB_FETCHMODE_ASSOC);
    knjCreateHidden($objForm, "SDATE", str_replace("-", "/", $semes["SDATE"]));
    knjCreateHidden($objForm, "EDATE", str_replace("-", "/", $semes["EDATE"]));
}
