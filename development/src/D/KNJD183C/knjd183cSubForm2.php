<?php

require_once('for_php7.php');

class knjd183cSubForm2
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform2", "POST", "knjd183cindex.php", "", "subform2");

        //DB接続
        $db = Query::dbCheckOut();

        //タイトル
        if ($model->Properties["KNJD183C_semesCombo"] == "1") {
            $arg["SEME_NAME"] = $db->getOne(knjd183cQuery::getSemesterCmb($model->semester));
            $arg["NO_SEME"]   = "";
        } else {
            $arg["SEME_NAME"] = "";
            $arg["NO_SEME"]   = "1";
        }
        $arg["TITLE"] = $model->itemname;
        knjCreateHidden($objForm, "COLUMNNAME", $model->columnname);

        //学年
        $query = knjd183cQuery::getGrade($model);
        $cnt = get_count($db->getCol($query));

        if ($cnt > 1) {
            //コンボ
            $query = knjd183cQuery::getGrade($model);
            $extra = "onchange=\"btn_submit('teikei');\"";
            makeCmb($objForm, $arg, $db, $query, "GRADE", $model->subField["GRADE"], $extra, 1);
        } else {
            //表示
            $grade = $db->getRow(knjd183cQuery::getGrade($model), DB_FETCHMODE_ASSOC);
            $arg["GRADE"] = $grade["LABEL"];
            knjCreateHidden($objForm, "GRADE", $grade["VALUE"]);
            $model->subField["GRADE"] = $grade["VALUE"];
        }

        //リスト作成
        $datacnt = makeList($objForm, $arg, $db, $model);

        if (VARS::get("REPLACE_FLG") != "") {
            $replaceFlg = VARS::get("REPLACE_FLG");
        } else {
            $replaceFlg = "";
        }

        //ボタン作成
        makeBtn($objForm, $arg, $datacnt, $replaceFlg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "TARGETID", $model->targetid);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd183cSubForm2.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
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
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//リスト作成
function makeList(&$objForm, &$arg, $db, $model)
{
    $datacnt = 0;
    $query = knjd183cQuery::getHtrainRemarkTempDat($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        array_walk($row, "htmlspecialchars_array");
        //選択チェックボックス
        $extra = "";
        $row["CHECK"] = knjCreateCheckBox($objForm, "CHECK", $row["REMARK"], $extra, "1");

        $arg["data"][] = $row;
        $datacnt++;
    }
    $result->free();
    return $datacnt;
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $datacnt, $replaceFlg)
{
    //選択ボタン
    $extra = "onclick=\"return btn_submit2('".$datacnt."', '".$replaceFlg."')\"";
    $arg["button"]["btn_sentaku"] = knjCreateBtn($objForm, "btn_sentaku", "選 択", $extra);
    //戻るボタン
    $extra = "onclick=\"return parent.closeit()\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
}
