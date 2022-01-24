<?php

require_once('for_php7.php');

class knjd133SubForm2
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform2", "POST", "knjd133index.php", "", "subform2");

        //DB接続
        $db = Query::dbCheckOut();

        //学年
        $query = knjd133Query::getGrade($model);
        $cnt = get_count($db->getCol($query));
        if ($cnt > 1) {
            //コンボ
            $query = knjd133Query::getGrade($model);
            $extra = "onchange=\"btn_submit('teikei');\"";
            makeCmb($objForm, $arg, $db, $query, "GRADE", $model->subField["GRADE"], $extra, 1);
        } else {
            //表示
            $grade = $db->getRow(knjd133Query::getGrade($model), DB_FETCHMODE_ASSOC);
            $arg["GRADE"] = $grade["LABEL"];
            knjCreateHidden($objForm, "GRADE", $grade["VALUE"]);
            $model->subField["GRADE"] = $grade["VALUE"];
        }

        $titleList = array(
              '03' => '学習内容'
            , '04' => '評価'
        );
        $arg["TITLE"] = $titleList[$model->dataDiv] ? $titleList[$model->dataDiv]: '学習内容';

        //リスト作成
        $datacnt = makeList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $datacnt);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        knjCreateHidden($objForm, "DATA_DIV", $model->dataDiv);
        knjCreateHidden($objForm, "TARGETTEXT", $model->targetText);
        knjCreateHidden($objForm, "CALLFUNC", $model->callFunc);
        knjCreateHidden($objForm, "isKomazawa", $model->isKomazawa);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd133SubForm2.html", $arg);
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
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//リスト作成
function makeList(&$objForm, &$arg, $db, $model)
{
    $datacnt = 0;
    $query = knjd133Query::getHtrainRemarkTempDatIkkatsu($model->subField["GRADE"], $model->dataDiv);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        array_walk($row, "htmlspecialchars_array");
        //選択チェックボックス
        $check = "";
        $row["CHECK"] = knjCreateCheckBox($objForm, "CHECK", $row["REMARK"], $check, "1");
        $arg["data"][] = $row;
        $datacnt++;
    }
    $result->free();
    return $datacnt;
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $datacnt)
{
    //選択ボタン
    $extra = "onclick=\"return btn_submit2('".$datacnt."')\"";
    $arg["button"]["btn_sentaku"] = knjCreateBtn($objForm, "btn_sentaku", "選 択", $extra);
    //戻るボタン
    $extra = "onclick=\"return parent.closeit()\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
}
?>
