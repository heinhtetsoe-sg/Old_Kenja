<?php

require_once('for_php7.php');

class knjl014rForm1
{

    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl014rForm1", "POST", "knjl014rindex.php", "", "knjl014rForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->year;

        //入試制度コンボ
        $query = knjl014rQuery::get_name_cd($model->year, "L003", $model->fixApplicantDiv);
        $extra = "onChange=\"btn_submit('change')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["APPLICANTDIV"], "APPLICANTDIV", $extra, 1, "TOP", "");

        //入試区分
        $query = knjl014rQuery::get_name_cd($model->year, "L004", "2");
        $extra = "onChange=\"btn_submit('change')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1, "TOP", "");

        //事前番号ページ
        $query = knjl014rQuery::getMainQuery($model, "1");
        $extra = "onChange=\"btn_submit('change')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["BEFORE_PAGE"], "BEFORE_PAGE", $extra, 1, "TOP", "ALL");

        //データ一覧
        $model->data["EXAMNO"] = "";
        $query = knjl014rQuery::getMainQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $sep = ($model->data["EXAMNO"] == "") ? "" : ",";

            $extra = "";
            if ($row["JUDGEMENT"] == "1" || $row["JUDGEMENT"] == "2" || $row["JUDGEMENT"] == "3") {
                $extra = " disabled ";
            } elseif ($row["JUDGEMENT"] == "4") {
                $model->data["EXAMNO"] .= $sep.$row["EXAMNO"];
                $extra = " checked ";
            } else {
                $model->data["EXAMNO"] .= $sep.$row["EXAMNO"];
            }
            $row["MIJUKEN"] = knjCreateCheckBox($objForm, "MIJUKEN".$row["EXAMNO"], "1", $extra);

            $arg["data"][] = $row;
        }
        $result->free();

        $query = knjl014rQuery::getScoreDat($model);
        $scoreCnt = $db->getOne($query);

        //ボタン作成
        makeBtn($objForm, $arg, $model, $scoreCnt);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl014rForm1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $scoreCnt)
{

    //更新ボタン
    if ($scoreCnt > 0) {
        $extra = " disabled ";
    } else {
        $extra = " onclick=\"return btn_submit('update');\" ";
    }
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $argTop = "data", $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    if ($blank == "ALL") {
        $opt[] = array("label" => "--全て--", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$argTop][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
