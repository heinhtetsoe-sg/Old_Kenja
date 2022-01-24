<?php

require_once('for_php7.php');

class knjz401p_2Form2
{
    public function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz401p_2index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();
        
        //学期数取得
        $model->getSemesterCount = $db->getOne(knjz401p_2Query::getSemester());

        if (isset($model->grade) && isset($model->subclasscd) && isset($model->viewcd) && !isset($model->warning) && ($model->cmd != "class")) {
            //教育課程用
            if (strlen($model->subclasscd) > 6) {
                $Row = $db->getRow(knjz401p_2Query::getRow2($model->grade, $model->subclasscd, $model->viewcd, $model), DB_FETCHMODE_ASSOC);
            } else {
                $Row = $db->getRow(knjz401p_2Query::getRow($model->grade, $model->subclasscd, $model->viewcd, $model), DB_FETCHMODE_ASSOC);
            }
        } else {
            $Row =& $model->field;
        }
        
        //教科コンボ
        $query = knjz401p_2Query::getClassMst($model);
        $extra = "onchange=\"return btn_submit('class');\"";
        makeCmb($objForm, $arg, $db, $query, "CLASSCD", $Row["CLASSCD"], $extra, 1, "blank");

        //科目コンボ
        $query = knjz401p_2Query::getSubClassMst($Row["CLASSCD"], $model);
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $value = $model->classcd."-".$model->school_kind."-".$model->curriculum_cd."-".$model->subclasscd;
            if ($model->cmd == "class") {
                makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], "", 1, "blank");
            } else {
                if ($model->field["SUBCLASSCD"] == "") {
                    makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $value, "", 1, "blank");
                } else {
                    $value = $model->field["SUBCLASSCD"];
                    makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $value, "", 1, "blank");
                }
            }
        } else {
            makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $Row["SUBCLASSCD"], "", 1, "blank");
        }
        
        //観点コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["VIEWCD"] = knjCreateTextBox($objForm, substr($Row["VIEWCD"], 2), "VIEWCD", 2, 2, $extra);

        //クラス形態
        $opt = array(1, 2);
        $Row["RECORD_DIV"] = ($Row["RECORD_DIV"] == "") ? "1" : $Row["RECORD_DIV"];
        $extra = array("id=\"RECORD_DIV1\"", "id=\"RECORD_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "RECORD_DIV", $Row["RECORD_DIV"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //Detailデータをセット
        if ($model->getSemesterCount > 2) {
            $arg["SEMESTER3"] = "1";
            $setSemseter = "3";
        } else {
            $setSemseter = "2";
        }
        for ($sem = 1; $sem <= $setSemseter; $sem++) {
            //観点名称
            $arg["data"]["VIEWNAME".$sem] = knjCreateTextBox($objForm, $Row["VIEWNAME".$sem], "VIEWNAME".$sem, 48, 75, "");
            //観点略称
            $arg["data"]["VIEWABBV".$sem] = knjCreateTextBox($objForm, $Row["VIEWABBV".$sem], "VIEWABBV".$sem, 32, 32, "");
            //学期名称
            $arg["data"]["SET_NAME".$sem] = $db->getOne(knjz401p_2Query::getSemester($sem));

            //表示順
            $extra = "onblur=\"this.value=toInteger(this.value)\" STYLE=\"text-align: right\"";
            $arg["data"]["SHOWORDER".$sem] = knjCreateTextBox($objForm, $Row["SHOWORDER".$sem], "SHOWORDER".$sem, 2, 2, $extra);

            //指導要録用科目コンボ
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $value = $Row["STUDYREC_CLASSCD".$sem]."-".$Row["STUDYREC_SCHOOL_KIND".$sem]."-".$Row["STUDYREC_CURRICULUM_CD".$sem]."-".$Row["STUDYREC_SUBCLASSCD".$sem];
            } else {
                $value = $Row["STUDYREC_SUBCLASSCD".$sem];
            }

            $query = knjz401p_2Query::getStudyrecSubcd($Row["CLASSCD"], $model->grade, $model);
            $extra = "onchange=\"return btn_submit('class');\"";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                if ($model->cmd == "class") {
                    makeCmb($objForm, $arg, $db, $query, "STUDYREC_SUBCLASSCD".$sem, $Row["STUDYREC_SUBCLASSCD".$sem], $extra, 1, "blank");
                } else {
                    if ($model->field["STUDYREC_SUBCLASSCD".$sem] == "") {
                        makeCmb($objForm, $arg, $db, $query, "STUDYREC_SUBCLASSCD".$sem, $value, $extra, 1, "blank");
                    } else {
                        $value = $model->field["STUDYREC_SUBCLASSCD".$sem];
                        makeCmb($objForm, $arg, $db, $query, "STUDYREC_SUBCLASSCD".$sem, $value, $extra, 1, "blank");
                    }
                }
            } else {
                makeCmb($objForm, $arg, $db, $query, "STUDYREC_SUBCLASSCD".$sem, $value, $extra, 1, "blank");
            }

            //指導要録用観点コンボ
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                if ($model->cmd == "class") {
                    $query = knjz401p_2Query::getStudyrecViewcd($Row["CLASSCD"], $Row["STUDYREC_SUBCLASSCD".$sem], $model->grade, $model);
                } else {
                    $query = knjz401p_2Query::getStudyrecViewcd($Row["CLASSCD"], $value, $model->grade, $model);
                }
            } else {
                $query = knjz401p_2Query::getStudyrecViewcd($Row["CLASSCD"], $value, $model->grade, $model);
            }
            makeCmb($objForm, $arg, $db, $query, "STUDYREC_VIEWCD".$sem, $Row["STUDYREC_VIEWCD".$sem], "", 1, "blank");
        }

        //CSV作成
        makeCsv($objForm, $arg, $db, $model);

        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $link = REQUESTROOT."/Z/KNJZ401P/knjz401pindex.php?year_code=".$model->year_code;
        $extra = "onclick=\"parent.location.href='$link';\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "GRADE2", $Row["GRADE"]);
        knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);
        knjCreateHidden($objForm, "year_code", $model->year_code);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "parent.left_frame.location.href='knjz401p_2index.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz401p_2Form2.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank) {
        $opt[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
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

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ＣＳＶ作成
function makeCsv(&$objForm, &$arg, $db, $model)
{
    //ヘッダ有チェックボックス
    $extra  = ($model->field["HEADER"] == "on" || $model->field["OUTPUT"] == "") ? "checked" : "";
    $extra .= " id=\"HEADER\"";
    $arg["csv"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra, "");

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

    //実行ボタン
    $extra = "onclick=\"return btn_submit('exec');\"";
    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
}
