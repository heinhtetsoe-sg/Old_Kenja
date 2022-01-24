<?php
class knjl693hForm1
{
    public function main(&$model)
    {
        define("LINE_MAX", 12); // １頁に表示する最大人数

        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl693hForm1", "POST", "knjl693hindex.php", "", "knjl693hForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試日程コンボ
        $extra = "";
        $query = knjl693hQuery::getEntexamTestDivMst($model->ObjYear, "2");
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1);

        //受験番号（開始）
        $extra = " onblur=\"this.value=toInteger(this.value);checkReceptRange();\"";
        $arg["data"]["RECEPTNO_START"] = knjCreateTextBox($objForm, $model->field["RECEPTNO_START"], "RECEPTNO_START", 4, 4, $extra);

        //受験番号（終了）
        $extra = " onblur=\"this.value=toInteger(this.value);checkReceptRange();\"";
        $arg["data"]["RECEPTNO_END"] = knjCreateTextBox($objForm, $model->field["RECEPTNO_END"], "RECEPTNO_END", 4, 4, $extra);

        //類別
        $query = knjl693hQuery::getEntexamClassifyMst($model->ObjYear, "2");
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV1"], "TESTDIV1", $extra, 1, "ALL");

        //男女別ラジオボタン 3:全員 1:男子のみ 2:女子のみ
        $opt = array(1, 2, 3);
        if (!$model->field["SEX"]) {
            $model->field["SEX"] = 3;
        }
        $extra = array("id=\"SEX1\"", "id=\"SEX2\"", "id=\"SEX3\"");
        $radioArray = knjCreateRadio($objForm, "SEX", $model->field["SEX"], $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //出力順ラジオボタン 1:受験番号順 2:氏名順(50音順)
        $opt = array(1, 2);
        if (!$model->field["ORDER"]) {
            $model->field["ORDER"] = 1;
        }
        $extra = array("id=\"ORDER1\"", "id=\"ORDER2\"");
        $radioArray = knjCreateRadio($objForm, "ORDER", $model->field["ORDER"], $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //帳票種類ラジオボタン（1:入試用受験番号票、2:合格書類封筒用ラベル、3:学力テスト用受験番号票）
        $opt = array(1, 2, 3);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"", "id=\"OUTPUT3\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        $pageCnt1 = "";
        $pageCnt2 = "";
        if ($model->cmd == "calc") {
            //入試用受験番号用票の頁数
            $query = knjl693hQuery::getReceptnoCnt($model);
            $result = $db->getOne($query);
            $pageCnt1 = ceil($result / LINE_MAX)."枚";

            //学力テスト用受験番号票の頁数
            $query = knjl693hQuery::getEntReceptnoCnt($model);
            $result = $db->getOne($query);
            $pageCnt2 = ceil($result / LINE_MAX)."枚";
        }

        //入試用受験番号用票の頁数
        $arg["data"]["PAGECNT1"] = $pageCnt1;

        //学力テスト用受験番号票の頁数
        $arg["data"]["PAGECNT2"] = $pageCnt2;

        //枚数計算ボタン
        $extra = "onclick=\"return btn_submit('calc');\"";
        $arg["button"]["btn_calc"] = knjCreateBtn($objForm, "btn_calc", "枚数計算", $extra);

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->ObjYear);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJL693H");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl693hForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "ALL") {
        $opt[] = array("label" => "00:全て", "value" => "00");
    }
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $default = 0;
    $i = ($blank) ? 1 : 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }

        if ($row["NAMESPARE2"] && $default_flg && $value != "00") {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
