<?php

require_once('for_php7.php');

class knjl630iForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //学科コンボ ※固定
        $extra = " onchange=\"return btn_submit('knjl630i');\"";
        $opt = array();
        $opt[] = array("label" => "-- 全て --", "value" => "ALL");
        $opt[] = array('label' => "1:普通科", 'value' => "1");
        $opt[] = array('label' => "2:工業科", 'value' => "2");
        if ($model->field["GAKKA"] == null) {
            $model->field["GAKKA"] = "ALL";
        }
        $arg["data"]["GAKKA"] = knjCreateCombo($objForm, "GAKKA", $model->field["GAKKA"], $opt, $extra, 1);

        //入試区分コンボ
        $extra = " onchange=\"return btn_submit('knjl630i');\"";
        $query = knjl630iQuery::getTestDivMst($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1);

        $click = "onclick =\" return btn_submit('knjl630i');\"";

        //帳票種類ラジオボタン（1:合格者一覧表 ～ 9:振込依頼書）
        $opt = array(1, 2, 3, 4, 5, 6, 7, 8, 9);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"OUTPUT{$val}\"".$click);
        }
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //入試状況ラジオボタン（1:学科別 2:類別 3:コース別）
        $opt = array(1, 2, 3);
        $model->field["EXAM"] = ($model->field["EXAM"] == "") ? "1" : $model->field["EXAM"];
        $extra = array();
        $disabled = (int)$model->field["OUTPUT"] == 5 ? "" : " disabled ";
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"EXAM{$val}\"".$disabled);
        }
        $radioArray = knjCreateRadio($objForm, "EXAM", $model->field["EXAM"], $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //帳票種類により条件指定を非活性にする
        $disabled1 = (int)$model->field["OUTPUT"] > 3 ? " disabled " : "";
        $disabled2 = (int)$model->field["OUTPUT"] > 4 && (int)$model->field["OUTPUT"] != 9 ? " disabled " : "";
        $disabled3 = (int)$model->field["OUTPUT"] == 5 || (int)$model->field["OUTPUT"] == 6 || (int)$model->field["OUTPUT"] == 8 ? " disabled " : "";
        $csvOnly = (int)$model->field["OUTPUT"] == 4 ? " disabled " : "";

        //類別コンボ
        $extra = " onchange=\"return btn_submit('knjl630i');\"".$disabled1;
        $query = knjl630iQuery::getGeneralMst($model, '01', $model->field["GAKKA"]);
        makeCmb($objForm, $arg, $db, $query, $model->field["RUIBETSU"], "RUIBETSU", $extra, 1, "BLANK");

        //コースコンボ
        $extra = " onchange=\"return btn_submit('knjl630i');\"".$disabled1;
        $query = knjl630iQuery::getGeneralMst($model, '02', $model->field["RUIBETSU"], $model->field["GAKKA"]);
        makeCmb($objForm, $arg, $db, $query, $model->field["COURSE"], "COURSE", $extra, 1, "BLANK");

        //開始受験番号テキストボックス
        $extra = " onchange=\"this.value=toIntegerCheck(this.value, 4);\"".$disabled2;
        $arg["data"]["S_EXAMNO"] = knjCreateTextBox($objForm, $model->s_examno, "S_EXAMNO", 4, 4, $extra);
        //終了受験番号テキストボックス
        $extra = " onchange=\"this.value=toIntegerCheck(this.value, 4);\"".$disabled2;
        $arg["data"]["E_EXAMNO"] = knjCreateTextBox($objForm, $model->e_examno, "E_EXAMNO", 4, 4, $extra);

        //性別ラジオボタン（3:全て、1:男、2:女）
        $opt = array(3, 1, 2);
        $model->field["SEX"] = ($model->field["SEX"] == "") ? "3" : $model->field["SEX"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"SEX{$val}\"".$disabled1);
        }
        $radioArray = knjCreateRadio($objForm, "SEX", $model->field["SEX"], $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //出力順ラジオボタン（1:受験番号順、2:氏名（50音順））
        $opt = array(1, 2);
        $model->field["SORT"] = ($model->field["SORT"] == "") ? "1" : $model->field["SORT"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"SORT{$val}\"".$disabled3);
        }
        $radioArray = knjCreateRadio($objForm, "SORT", $model->field["SORT"], $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //特待生コンボ
        $disabled = (int)$model->field["OUTPUT"] == 4 ? "" : " disabled ";
        $extra = " onchange=\"return btn_submit('knjl630i');\"".$disabled;
        $query = knjl630iQuery::getGeneralMst2($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["SPECIAL"], "SPECIAL", $extra, 1);
        
        //csv出力ボタン
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力", $extra);

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"".$csvOnly;
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->ObjYear);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "TIME", date("H:i"));
        knjCreateHidden($objForm, "PRGID", "KNJL630I");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl630iForm1", "POST", "knjl630iindex.php", "", "knjl630iForm1");
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl630iForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "ALL") {
        $opt[] = array("label" => "-- 全て --", "value" => "ALL");
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

        if ($row["NAMESPARE2"] && $default_flg && $value != "ALL") {
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
