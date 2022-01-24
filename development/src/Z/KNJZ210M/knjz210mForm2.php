<?php

require_once('for_php7.php');

class knjz210mForm2
{
    public function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz210mindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        if (!isset($model->warning) && $model->cmd != "edit2" && $model->cmd != "seq" && $model->year && $model->grade && $model->ibprg_course != "" && $model->classcd != "" && $model->school_kind != "" && $model->curriculum_cd != "" && $model->subclasscd != "") {
            $query = knjz210mQuery::getIBViewCuttingDat($model->year, $model->grade, $model->ibprg_course, $model->classcd, $model->school_kind, $model->curriculum_cd, $model->subclasscd, "", "row");
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //学年コンボ
        $query = knjz210mQuery::getGrade($model, "list");
        $extra = "onchange=\"return btn_submit('edit2');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $Row["GRADE"], $extra, 1);

        //IBコースコンボ
        $query = knjz210mQuery::getIBPrgCourse($model, "list");
        $extra = "onchange=\"return btn_submit('edit2');\"";
        makeCmb($objForm, $arg, $db, $query, "IBPRG_COURSE", $Row["IBPRG_COURSE"], $extra, 1);

        //科目コンボ
        $query = knjz210mQuery::getSubclasscd($model, "list");
        $value = $Row["CLASSCD"].'-'.$Row["SCHOOL_KIND"].'-'.$Row["CURRICULUM_CD"].'-'.$Row["SUBCLASSCD"];
        $extra = "onchange=\"return btn_submit('edit2');\"";
        makeCmb($objForm, $arg, $db, $query, "SUBCLASS", $value, $extra, 1, "BLANK");

        $disabled = ($Row["CLASSCD"] == "") ? "disabled " : "";

        //データ件数
        $query = knjz210mQuery::getIBViewCuttingDat($model->year, $Row["GRADE"], $Row["IBPRG_COURSE"], $Row["CLASSCD"], $Row["SCHOOL_KIND"], $Row["CURRICULUM_CD"], $Row["SUBCLASSCD"], "", "cnt");
        $max_seq = $db->getOne($query);
        $max = ($max_seq > 0) ? $max_seq : "";

        //段階数
        $model->field["MAX_SEQ"] = (($model->cmd != "seq" && $model->cmd != "check") || $model->field["MAX_SEQ"] == "") ? $max : $model->field["MAX_SEQ"];
        $extra = "onblur=\"this.value=toInteger(this.value)\" STYLE=\"text-align: right\"";
        $arg["MAX_SEQ"] = knjCreateTextBox($objForm, $model->field["MAX_SEQ"], "MAX_SEQ", 3, 3, $disabled.$extra);

        //確定ボタン
        $extra = "onclick=\"return seq('{$max}');\"";
        $arg["button"]["btn_seq"] = knjCreateBtn($objForm, "btn_seq", "確 定", $disabled.$extra);

        //一覧表示
        for ($i = 1; $i <= $model->field["MAX_SEQ"]; $i++) {
            //データ取得
            $query = knjz210mQuery::getIBViewCuttingDat($model->year, $Row["GRADE"], $Row["IBPRG_COURSE"], $Row["CLASSCD"], $Row["SCHOOL_KIND"], $Row["CURRICULUM_CD"], $Row["SUBCLASSCD"], $i, "list");
            $Row2 = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $Row2["SEQ"] = $i;

            //評定記号
            if ($model->cmd == "check") {
                $Row2["CUTTING_MARK"] = $model->field2["CUTTING_MARK_".$i];
            }
            $extra = "onblur=\"this.value=toAlpha(this.value)\" STYLE=\"text-align: center\"";
            $Row2["CUTTING_MARK"] = knjCreateTextBox($objForm, $Row2["CUTTING_MARK"], "CUTTING_MARK_".$i, 3, 2, $extra);

            //到達度下限
            if ($i == 1) {
                $Row2["CUTTING_LOW"] = ($Row2["CUTTING_LOW"] == "") ? "0" :  sprintf("%d", $Row2["CUTTING_LOW"]);
            } else {
                $Row2["CUTTING_LOW"] = ($Row2["CUTTING_LOW"] == "") ? "" : $Row2["CUTTING_LOW"];
            }

            //到達度上限
            if ($i == $model->field["MAX_SEQ"]) {
                $Row2["CUTTING_HIGH"] = 100;
                knjCreateHidden($objForm, "CUTTING_HIGH_".$i, 100);
            } else {
                if ($model->cmd == "check") {
                    $Row2["CUTTING_HIGH"] = $model->field2["CUTTING_HIGH_".$i];
                }
                $value = ($Row2["CUTTING_HIGH"] == "") ? "" : $Row2["CUTTING_HIGH"];
                $extra = "onblur=\"checkDecimal(this)\" STYLE=\"text-align: right\"";
                $Row2["CUTTING_HIGH"] = knjCreateTextBox($objForm, $value, "CUTTING_HIGH_".$i, 4, 4, $extra);
            }

            $arg["data"][] = $Row2;
        }

        //CSV作成
        makeCsv($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit" && $model->cmd != "edit2" && $model->cmd != "seq") {
            $arg["reload"] = "window.open('knjz210mindex.php?cmd=list&shori=add','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz210mForm2.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $BLANK = "")
{
    $opt = array();
    $value_flg = false;
    if ($BLANK) {
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
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //ボタンの有効
    $disabled = ($model->field["MAX_SEQ"] > 0) ? "" : "disabled";

    //登録ボタン
    $extra = " onclick=\"return btn_submit('add');\"";
    $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "登 録", $disabled.$extra);
    //削除ボタン
    $extra = " onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $disabled.$extra);
    //取消ボタン
    $extra = " onclick=\"return btn_submit('reset')\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = " onclick=\"closeWin();\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);
}

//ＣＳＶ作成
function makeCsv(&$objForm, &$arg, $db, $model)
{
    //出力取込種別ラジオボタン 1:取込 2:書出 3:エラー書出 4:ヘッダ出力（見本）
    $opt_shubetsu = array(1, 2, 3, 4);
    $model->field["OUTPUT"] = ($model->field["OUTPUT"]) ? $model->field["OUTPUT"] : "1";
    $click = " onclick=\"return changeRadio(this);\"";
    $extra = array("id=\"OUTPUT1\"".$click, "id=\"OUTPUT2\"".$click, "id=\"OUTPUT3\"".$click, "id=\"OUTPUT4\"".$click);
    $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_shubetsu, get_count($opt_shubetsu));
    foreach ($radioArray as $key => $val) {
        $arg["csv"][$key] = $val;
    }

    //ファイルからの取り込み
    $extra = ($model->field["OUTPUT"] == "1") ? "" : "disabled";
    $arg["FILE"] = knjCreateFile($objForm, "FILE", $extra, 1024000);

    //ヘッダ有チェックボックス
    $check_header = "checked id=\"HEADER\"";
    $arg["csv"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $check_header, "");

    //実行ボタン
    $extra = "onclick=\"return btn_submit('exec');\"";
    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
}
