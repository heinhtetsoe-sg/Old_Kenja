<?php

require_once('for_php7.php');

class knjf140aForm1
{
    public function main(&$model)
    {
        //権限チェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //校種
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query = knjf140aQuery::getNameMstA023($model, CTRL_YEAR);
            $extra = "onChange=\"btn_submit('')\";";
            makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $extra, 1);
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $model->field["SCHOOL_KIND"] = SCHOOLKIND;
            knjCreateHidden($objForm, "SCHOOL_KIND", SCHOOLKIND);
        } else {
            $query = knjf140aQuery::getNameMstA023($model);
            $cnt = $db->getCol($query);
            if ($cnt > 1) {
                $extra = "onChange=\"btn_submit('')\";";
                makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $extra, 1);
            } else {
                $school_kind = $db->getOne($query);
                $model->field["SCHOOL_KIND"] = $school_kind;
                knjCreateHidden($objForm, "SCHOOL_KIND", $school_kind);
            }
        }

        //対象データ
        $opt      = array();
        $opt[]    = array("label" => "1:健康診断（一般）","value" => "1");
        $opt[]    = array("label" => "2:健康診断（歯・口腔）","value" => "2");
        $extra = "";
        $arg["data"]["CSVCD"] = knjCreateCombo($objForm, "CSVCD", $model->field["CSVCD"], $opt, $extra, 1);

        //処理名コンボボックス
        $opt      = array();
        $opt[]    = array("label" => "更新","value" => "1");
        $opt[]    = array("label" => "削除","value" => "2");
        $extra = "style=\"width:60px;\"";
        $arg["data"]["SHORI_MEI"] = knjCreateCombo($objForm, "SHORI_MEI", $model->field["SHORI_MEI"], $opt, $extra, 1);


        $securityCnt = $db->getOne(knjf140aQuery::getSecurityHigh());
        //セキュリティーチェック
        if (!$model->getPrgId && $model->Properties["useXLS"] && $securityCnt > 0) {
            $arg["jscript"] = "OnSecurityError();";
        }

        //年度一覧コンボボックス
        if ($model->field["YEAR"] == "") {
            $model->field["YEAR"] = CTRL_YEAR.CTRL_SEMESTER;
        }
        $query = knjf140aquery::getSelectFieldSQL($model);
        $extra = "onchange=\"btn_submit('');\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->field["YEAR"], $extra, 1);

        //年組一覧コンボボックス
        $query = knjf140aquery::getSelectFieldSQL2($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1, "ALL");

        //ヘッダ有チェックボックス
        if ($model->field["HEADER"] == "on") {
            $check_header = "checked";
        } else {
            $check_header = ($model->cmd == "") ? "checked" : "";
        }
        $extra = " id=\"HEADER\"";
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $check_header.$extra, "");

        //出力取込種別ラジオボタン
        $opt_shubetsu = array(1, 2, 3, 4); //1:ヘッダ出力 2:データ取込 3:エラー出力 4:データ出力
        $model->field["OUTPUT"] = ($model->field["OUTPUT"]) ? $model->field["OUTPUT"] : "1";
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"", "id=\"OUTPUT3\"", "id=\"OUTPUT4\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_shubetsu, get_count($opt_shubetsu));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }


        /************/
        /* ファイル */
        /************/
        //ファイルからの取り込み
        $arg["FILE"] = knjCreateFile($objForm, "FILE", "", "4096000");


        /**************/
        /* ボタン作成 */
        /**************/
        //実行ボタン
        if ($model->Properties["useXLS"]) {
            $model->schoolCd = $db->getOne(knjf140aQuery::getSchoolCd());
            $extra = "onclick=\"return newwin('" . SERVLET_URL . "', '" . $model->schoolCd . "', '" . $model->Properties["xlsVer"] . "');\"";
            //今年度・今学期名及びタイトルの表示
            $arg["data"]["YEAR_SEMESTER"] = CTRL_YEAR."年度　" .CTRL_SEMESTERNAME ."　エクセル出力／ＣＳＶ取込";
        } else {
            $extra = "onclick=\"return btn_submit('exec');\"";
            //今年度・今学期名及びタイトルの表示
            $arg["data"]["YEAR_SEMESTER"] = CTRL_YEAR."年度　" .CTRL_SEMESTERNAME ."　ＣＳＶ出力／取込";
        }
        $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, 'btn_end', '終 了', $extra);

        /**********/
        /* hidden */
        /**********/
        //hiddenを作成する
        knjCreateHidden($objForm, "cmd", "");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJF140A");
        knjCreateHidden($objForm, "TEMPLATE_PATH");
        //XLS出力用パラメータ
        knjCreateHidden($objForm, "PRINTKENKOUSINDANIPPAN", $model->Properties["printKenkouSindanIppan"]);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjf140aindex.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjf140aForm1.html", $arg);
    }
}
/************************************************ 以下関数 **************************************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $option = "")
{
    $opt = array();
    if ($option == "ALL") {
        $opt[] = array("label" => "(全て出力)",
                       "value" => "");
    }
    $value_flg = false;
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
