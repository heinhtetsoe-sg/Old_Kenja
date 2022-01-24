<?php

require_once('for_php7.php');

class knjl110qForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度
        $query = knjl110qQuery::getNameMst($model->ObjYear, "L003");
        $extra = "Onchange=\"btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //仮クラス
        $query = knjl110qQuery::getNameMst($model->ObjYear, 'L063');
        $extra = "Onchange=\"btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "TEMPORARY_CLASS", $model->temporary_class, $extra, 1);

        //リストタイトル用
        $arg["LEFT_TITLE"]  = "仮クラス：";
        $arg["RIGHT_TITLE"] = "入学者一覧";

        //仮クラス一覧
        $opt_left = $opt_right = array();
        $query = knjl110qQuery::getList($model, 'LEFT');
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_left[]  = array("label" => $row["EXAMNO"]."：".$row["NAME"],
                                 "value" => $row["EXAMNO"]);
        }
        //入学者一覧
        $query = knjl110qQuery::getList($model, 'RIGHT');
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_right[] = array("label" => $row["EXAMNO"]."：".$row["NAME"],
                                 "value" => $row["EXAMNO"]);
        }
        $result->free();

        //入学手続者一覧
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move3('right','LEFT_PART','RIGHT_PART',1);\"";
        $arg["main_part"]["LEFT_PART"] = knjCreateCombo($objForm, "LEFT_PART", "left", $opt_left, $extra, 30);

        //手続き済者一覧
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move3('left','LEFT_PART','RIGHT_PART',1);\"";
        $arg["main_part"]["RIGHT_PART"] = knjCreateCombo($objForm, "RIGHT_PART", "right", $opt_right, $extra, 30);

        //追加ボタン（全て）
        $extra = "onclick=\"return move3('sel_add_all','LEFT_PART','RIGHT_PART',1);\"";
        $arg["main_part"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "≪", $extra);
        //追加ボタン（一部）
        $extra = "onclick=\"return move3('left','LEFT_PART','RIGHT_PART',1);\"";
        $arg["main_part"]["SEL_ADD"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);
        //削除ボタン（一部）
        $extra = "onclick=\"return move3('right','LEFT_PART','RIGHT_PART',1);\"";
        $arg["main_part"]["SEL_DEL"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);
        //削除ボタン（全て）
        $extra = "onclick=\"return move3('sel_del_all','LEFT_PART','RIGHT_PART',1);\"";
        $arg["main_part"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", "≫", $extra);

        //radio(1:取込 2:書出)
        $opt = array(1, 2);
        $model->csv = ($model->csv == "") ? "1" : $model->csv;
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"csv{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "csv", $model->csv, $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //ファイルからの取り込み
        $extra = "";
        $arg["FILE"] = knjCreateFile($objForm, "FILE", 1024000, $extra);

        //ヘッダ有
        if ($model->field["HEADER"] == "on") {
            $extra = "checked";
        } else {
            $extra = ($model->cmd == "") ? "checked" : "";
        }
        $extra .= " id=\"HEADER\"";
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra);

        //実行ボタン
        $extra = "onclick=\"return btn_submit('exec');\"";
        $arg["button"]["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);

        //更新ボタン
        $extra = "onclick=\"return doSubmit();\"";
        $arg["button"]["BTN_OK"] = knjCreateBtn($objForm, "btn_keep", "更 新", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["BTN_CLEAR"] = knjCreateBtn($objForm, "btn_clear", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["BTN_END"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "selectdata2");

        //DB切断
        Query::dbCheckIn($db);

        $arg["start"] = $objForm->get_start("sel", "POST", "knjl110qindex.php", "", "sel");
        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knjl110qForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }

        if ($row["NAMESPARE2"] && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }

    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
