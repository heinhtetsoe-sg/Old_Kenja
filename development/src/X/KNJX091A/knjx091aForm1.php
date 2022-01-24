<?php

require_once('for_php7.php');

class knjx091aForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //処理年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //処理学期
        $arg["data"]["SEMESTERNAME"] = CTRL_SEMESTERNAME;

        //出力取込種別ラジオボタン (1:ヘッダ出力 2:データ取込 3:エラー出力 4:データ出力)
        $opt = array(1, 2, 3, 4);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"", "id=\"OUTPUT3\"", "id=\"OUTPUT4\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //処理名コンボ
        $opt   = array();
        $opt[] = array("label" => "更新","value" => "1");
        $opt[] = array("label" => "削除","value" => "2");
        $extra = "style=\"width:60px;\"";
        $arg["data"]["SHORI_MEI"] = knjCreateCombo($objForm, "SHORI_MEI", $model->field["SHORI_MEI"], $opt, $extra, 1);

        //年度＆学期コンボ
        $opt = array();
        $value = $model->field["YEAR"].'-'.$model->field["SEMESTER"];
        $value_flg = false;
        $query = knjx091aquery::getSelectFieldSQL();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($value == $row["VALUE"]) {
                $value_flg = true;
            }
        }
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $extra = "onchange=\"btn_submit('main');\"";
        $arg["data"]["YEAR_SEMESTER"] = knjCreateCombo($objForm, "YEAR_SEMESTER", $value, $opt, $extra, 1);

        //学年コンボ
        $opt   = array();
        $opt[] = array("label" => "(全て出力)","value" => "99");
        $value = $model->field["GRADE"];
        $value_flg = false;
        $query = knjx091aquery::getGrade($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($value == $row["VALUE"]) {
                $value_flg = true;
            }
        }
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $extra = "";
        $arg["data"]["GRADE"] = knjCreateCombo($objForm, "GRADE", $value, $opt, $extra, 1);

        //科目コンボ
        $opt   = array();
        if ($model->Properties["useCurriculumcd"] == '1') {
            $opt[] = array("label" => "(全て出力)","value" => "99-X-9-999999");
        } else {
            $opt[] = array("label" => "(全て出力)","value" => "999999");
        }
        $value = $model->field["SUBCLASS"];
        $value_flg = false;
        $query = knjx091aquery::getSubclassStdDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($value == $row["VALUE"]) {
                $value_flg = true;
            }
        }
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $extra = "";
        $arg["data"]["SUBCLASS"] = knjCreateCombo($objForm, "SUBCLASS", $value, $opt, $extra, 1);

        //ヘッダ有チェックボックス
        if ($model->field["HEADER"] == "on") {
            $extra = "checked";
        } else {
            $extra = ($model->cmd == "") ? "checked" : "";
        }
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra." id=\"HEADER\"");

        //ファイルからの取り込み
        $extra = "";
        $arg["FILE"] = knjCreateFile($objForm, "FILE", 1024000, $extra);

        //実行ボタン
        $extra = "onclick=\"return btn_submit('exec');\"";
        $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjx091aindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjx091aForm1.html", $arg);
    }
}
