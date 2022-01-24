<?php

require_once('for_php7.php');

class knje420Form1
{
    public function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knje420index.php", "", "edit");

        $db     = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        //年度
        $query = knje420Query::getYear();
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->field["YEAR"], $extra, 1, "");

        //V_SCHOOL_MSTから学校コードを取得
        $query = knje420Query::getSchoolMst($model);
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $model->schoolcd = $rtnRow["KYOUIKU_IINKAI_SCHOOLCD"];
        $model->prefcd = $rtnRow["PREF_CD"];
        
        //タイトル
        $arg["TITLE"] = "就職内定状況集計表";

        //文書番号
        $query = knje420Query::getTuutatu($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db2, $query, "DOC_NUMBER", $model->field["DOC_NUMBER"], $extra, 1, "BLANK");

        //県への報告用作成日(テーブルは報告履歴テーブルのみ)
        $arg["EXECUTE_DATE"] = View::popUpCalendar($objForm, "EXECUTE_DATE", str_replace("-", "/", $model->field["EXECUTE_DATE"]), "");

        //項目名(列)
        $nameArray = array("ZENTAI_GOUKEI"
                          ,"SHINGAKU_GOUKEI"
                          ,"SHUSHOKU_KIBOU_TOTAL_GAKKOU"
                          ,"SHUSHOKU_KIBOU_KENNAI_GAKKOU"
                          ,"SHUSHOKU_KIBOU_KENGAI_GAKKOU"
                          ,"SHUSHOKU_KIBOU_TOTAL_JIBUN"
                          ,"SHUSHOKU_KIBOU_KENNAI_JIBUN"
                          ,"SHUSHOKU_KIBOU_KENGAI_JIBUN"
                          ,"SHUSHOKU_KIBOU_TOTAL_KOUMUIN"
                          ,"SHUSHOKU_KIBOU_KENNAI_KOUMUIN"
                          ,"SHUSHOKU_KIBOU_KENGAI_KOUMUIN"
                          ,"SHUSHOKU_KIBOU_TOTAL_GOUKEI"
                          ,"SHUSHOKU_KIBOU_KENNAI_GOUKEI"
                          ,"SHUSHOKU_KIBOU_KENGAI_GOUKEI"
                          ,"SONOTA_KEIKAKU_ARI"
                          ,"SONOTA_KEIKAKU_NASHI"
                          ,"SHUSHOKU_NAITEI_TOTAL_GAKKOU"
                          ,"SHUSHOKU_NAITEI_KENNAI_GAKKOU"
                          ,"SHUSHOKU_NAITEI_KENGAI_GAKKOU"
                          ,"SHUSHOKU_NAITEI_TOTAL_JIBUN"
                          ,"SHUSHOKU_NAITEI_KENNAI_JIBUN"
                          ,"SHUSHOKU_NAITEI_KENGAI_JIBUN"
                          ,"SHUSHOKU_NAITEI_TOTAL_KOUMUIN"
                          ,"SHUSHOKU_NAITEI_KENNAI_KOUMUIN"
                          ,"SHUSHOKU_NAITEI_KENGAI_KOUMUIN"
                          ,"SHUSHOKU_NAITEI_TOTAL_GOUKEI"
                          ,"SHUSHOKU_NAITEI_KENNAI_GOUKEI"
                          ,"SHUSHOKU_NAITEI_KENGAI_GOUKEI"
                          ,"SHINGAKU_SHUSHOKU"
                          ,"SHUSHOKU_KIBOU_TOTAL_MINAITEI"
                          ,"SHUSHOKU_KIBOU_KENNAI_MINAITEI"
                          ,"SHUSHOKU_KIBOU_KENGAI_MINAITEI"
                          ,"SHINGAKU_IGAI_MIKKETEI");

        unset($model->fields["CODE"]);

        $count = 0;
        $bifKey = "";
        $result = $db->query(knje420Query::readQuery($model));
        while ($Row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->fields["CODE"][] = $Row["COURSECD"]."-".$Row["MAJORCD"]."-".$Row["SEX"];
            //text
            foreach ($nameArray as $name) {
                $readOnly = "";
                $readStyle = "";
                $setSize = "3";
                if (preg_match("/_TOTAL_/", $name) || preg_match("/_KENNAI_GOUKEI$/", $name) || preg_match("/_KENGAI_GOUKEI$/", $name)) {
                    $readOnly = " readonly ";
                    $readStyle = "; background-color:cccccc;";
                    $setSize = "4";
                }
                if ($Row["SEX"] === '9') {
                    $readOnly = " readonly ";
                    $readStyle = "; background-color:cccccc;";
                    $setSize = "4";
                }
            
                $val = ($Row[$name] == "")? "0" : $Row[$name] ;
                $objForm->ae(array("type"        => "text",
                                    "name"        => $name,
                                    "size"        => $setSize,
                                    "maxlength"   => $setSize,
                                    "multiple"    => "1",
                                    "extrahtml"   => $readOnly."style=\"text-align:right{$readStyle}\" onblur=\"this.value = toInteger(this.value)\" ",
                                    "value"       => $val ));
                $Row[$name] = $objForm->ge($name);
            }

            if ($bifKey !== $Row["COURSECD"]."-".$Row["MAJORCD"]) {
                $Row["ROWSPAN"] = 3;
            }
            $bifKey = $Row["COURSECD"]."-".$Row["MAJORCD"];

            $arg["data"][] = $Row;
            $count++;
        }

        //県への報告
        $extra = "onclick=\"return btn_submit('houkoku');\"";
        $arg["btn_houkoku"] = knjCreateBtn($objForm, "btn_houkoku", "県への報告／PDFプレビュー", $extra);
        //報告履歴
        $query = knje420Query::getReport($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "REPORT", $model->field["REPORT"], $extra, 1, 1);

        //再計算ボタン
        $extra = "onclick=\"return btn_submit('recalc');\"";
        $arg["btn_recalc"] = knjCreateBtn($objForm, "btn_recalc", "再計算", $extra);
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["btn_can"] = knjCreateBtn($objForm, "btn_can", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //印刷
        $extra = "onclick=\"newwin('".SERVLET_URL."');\"";
        $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $extra);

        //CSVファイルアップロードコントロール
        makeCsv($objForm, $arg, $model, $db);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED[]", $model->updated);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "SCHOOLCD", $model->schoolcd);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "PROGRAMID");

        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knje420Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank != "") {
        if ($blank == "ALL") {
            $opt[] = array("label" => "-- 全て --", "value" => "");
        } else {
            $opt[] = array('label' => "", 'value' => "");
        }
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    if ($name == "YEAR") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ＣＳＶ作成
function makeCsv(&$objForm, &$arg, $model, $db)
{
    //ヘッダ有チェックボックス
    if ($model->field["HEADER"] == "on") {
        $check_header = "checked";
    } else {
        $check_header = ($model->cmd == "main") ? "checked" : "";
    }
    $extra = "id=\"HEADER\"".$check_header;
    $arg["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra);

    //出力取込種別ラジオボタン 1:取込 2:書出 3:エラー出力
    $opt_shubetsu = array(1, 2, 3);
    $model->field["OUTPUT"] = ($model->field["OUTPUT"]) ? $model->field["OUTPUT"] : "1";
    $click = " onclick=\"return changeRadio(this);\"";
    $extra = array("id=\"OUTPUT1\"".$click, "id=\"OUTPUT2\"".$click, "id=\"OUTPUT3\"".$click);
    $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_shubetsu, get_count($opt_shubetsu));
    foreach ($radioArray as $key => $val) {
        $arg[$key] = $val;
    }

    //ファイルからの取り込み
    $extra = ($model->field["OUTPUT"] == "1") ? "" : "disabled";
    $arg["FILE"] = knjCreateFile($objForm, "FILE", $extra, 1024000);

    //実行ボタン
    $extra = "onclick=\"return btn_submit('exec');\"";
    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
}
