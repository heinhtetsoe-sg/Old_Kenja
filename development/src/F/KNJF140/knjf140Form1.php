<?php

require_once('for_php7.php');

class knjf140Form1
{
    public function main(&$model)
    {

        //権限チェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjf140index.php", "", "main");
        $db = Query::dbCheckOut();

        $securityCnt = $db->getOne(knjf140Query::getSecurityHigh());
        //セキュリティーチェック
        if (!$model->getPrgId && $model->Properties["useXLS"] && $securityCnt > 0) {
            $arg["jscript"] = "OnSecurityError();";
        }

        /**********/
        /* コンボ */
        /**********/
        //対象データ
        $opt      = array();
        $opt[]    = array("label" => "1:健康診断（一般）","value" => "1");
        $opt[]    = array("label" => "2:健康診断（歯・口腔）","value" => "2");
        $extra = "";
        $arg["data"]["CSVCD"] = knjCreateCombo($objForm, "CSVCD", $model->field["CSVCD"], $opt, $extra, 1);
        //処理名
        $opt      = array();
        $opt[]    = array("label" => "更新","value" => "1");
        $opt[]    = array("label" => "削除","value" => "2");
        $extra = "style=\"width:60px;\"";
        $arg["data"]["SHORI_MEI"] = knjCreateCombo($objForm, "SHORI_MEI", $model->field["SHORI_MEI"], $opt, $extra, 1);

        //年度一覧コンボボックス
        if ($model->field["YEAR"] == "") {
            $model->field["YEAR"] = CTRL_YEAR.CTRL_SEMESTER;
        }
        $query = knjf140query::getSelectFieldSQL();
        $extra = "onchange=\"btn_submit('');\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->field["YEAR"], $extra, 1);

        //年組一覧コンボボックス
        $query = knjf140query::getSelectFieldSQL2($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1, "ALL");

        //ヘッダ有チェックボックス
        if ($model->field["HEADER"] == "on") {
            $check_header = "checked";
        } else {
            $check_header = ($model->cmd == "") ? "checked" : "";
        }
        $extra = "id=\"HEADER\"".$check_header;
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra);

        //出力取込種別ラジオボタン(1:ヘッダ出力 2:データ取込 3:エラー出力 4:データ出力)
        $opt = array(1, 2, 3, 4);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"", "id=\"OUTPUT3\"", "id=\"OUTPUT4\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        /************/
        /* ファイル */
        /************/
        $arg["FILE"] = knjCreateFile($objForm, "FILE", "", "4096000");

        /**************/
        /* ボタン作成 */
        /**************/
        //実行ボタン
        if ($model->Properties["useXLS"]) {
            $model->schoolCd = $db->getOne(knjf140Query::getSchoolCd());
            $extra = "onclick=\"return newwin('" . SERVLET_URL . "', '" . $model->schoolCd . "', '" . $model->Properties["xlsVer"] . "');\"";
            //今年度・今学期名及びタイトルの表示
            $arg["data"]["YEAR_SEMESTER"] = CTRL_YEAR."年度　" .CTRL_SEMESTERNAME ."　エクセル出力／取込";
        } else {
            $extra = "onclick=\"return btn_submit('exec');\"";
            //今年度・今学期名及びタイトルの表示
            $arg["data"]["YEAR_SEMESTER"] = CTRL_YEAR."年度　" .CTRL_SEMESTERNAME ."　ＣＳＶ出力／取込";
        }
        $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJF140");
        knjCreateHidden($objForm, "TEMPLATE_PATH");

        $arg["finish"]  = $objForm->get_finish();

        Query::dbCheckIn($db);
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjf140Form1.html", $arg);
    }
}
/************************************************ 以下関数 **************************************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, $value, $extra, $size, $option = "")
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
