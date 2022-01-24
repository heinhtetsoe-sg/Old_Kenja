<?php
class knjl109iForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->objYear;
        $comment  = "※ 学籍番号附番について\n";
        $comment .= "    高等部:'170' + 入学年度下2桁 + 連番3桁 \n";
        $comment .= "    中学部:'140' + 入学年度下2桁 + 連番3桁 \n";
        $comment .= "※ 自動附番後、必ず目視確認してください。";
        $arg["TOP"]["COMMENT"] = $comment;
       
        //入試制度
        $query = knjl109iQuery::getNameMst($model->objYear, "L003");
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1, "");

        //入試区分
        $query = knjl109iQuery::getTestdivMst($model);
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1, "");

        //一覧表示
        $model->examnoArray = array();
        $query = knjl109iQuery::selectQuery($model);
        $result = $db->query($query);
        $count = 1;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //入学確定者に新たに与える学籍番号
            if ($model->cmd == "huban") {
                $code1 = ($model->applicantdiv == "1") ? "140" : "170";
                $code2 = substr($model->objYear, 2, 2);
                $code3 = sprintf("%03d", $count);   //入学者人数が4桁以上にならないという前提
                $entSchregno = $code1.$code2.$code3;
            } else {
                $entSchregno = $row["ENT_SCHREGNO"];
            }
            $extra = " onblur=\"this.value=toInteger(this.value);\" onchange=\"changeValue();\" ";
            $row["ENT_SCHREGNO"]  = knjCreateTextBox($objForm, $entSchregno, "ENT_SCHREGNO_{$row["EXAMNO"]}", 8, 8, $extra);

            $arg["data"][] = $row;

            $model->examnoArray[] = $row["EXAMNO"];
            $count++;
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model, $count, $examno);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl109iindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl109iForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="", $retDiv="") {
    $opt = array();
    $retOpt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        $retOpt[$row["VALUE"]] = $row["LABEL"];

        if ($value == $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }

    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    if ($retDiv == "") {
        $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    } else {
        return $retOpt;
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $count, $examno) {
    $disable  = ($count > 0) ? "" : " disabled";

    //一括附番ボタン
    $extra = "onclick=\"return btn_submit('huban');\"".$disable;
    $arg["button"]["btn_huban"] = knjCreateBtn($objForm, "btn_huban", "一括附番", $extra);
   
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"".$disable;
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"".$disable;
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"return btn_submit('end');\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "CHG_FLG", "0");
}
?>
