<?php

require_once('for_php7.php');

require_once("csvfile.php");
class knjh140aForm1
{
    public function main($model)
    {
        $objForm = new form();

        //フォーム作成

        //権限チェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        $securityCnt = $db->getOne(knjh140aQuery::getSecurityHigh());
        //セキュリティーチェック
        if (!$model->getPrgId && $model->Properties["useXLS"] && $securityCnt > 0) {
            $arg["jscript"] = "OnSecurityError();";
        }

        //対象年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //対象データコンボ
        $opt_target[] = array("label" => "1：家族情報",                    "value" => 1);
        $opt_target[] = array("label" => "2：緊急連絡先情報",              "value" => 2);
        $opt_target[] = array("label" => "3：通学手段情報",                "value" => 3);
        $opt_target[] = array("label" => "4：自転車許可番号登録",          "value" => 4);
        $opt_target[] = array("label" => "5：その他（通知票等 送付先）",   "value" => 5);
        $extra = "onChange=\"btn_submit('change_target')\";";
        $arg["data"]["TARGET"] = knjCreateCombo($objForm, "TARGET", $model->target, $opt_target, $extra, 1);

        //対象ファイル
        $objForm->add_element(array("type"      => "file",
                                    "name"      => "FILE",
                                    "size"      => 2048000,
                                    "extrahtml" => "" ));
        $arg["data"]["FILE"] = $objForm->ge("FILE");

        //ヘッダ有無
        $check_header = $model->headercheck == "1" ? "checked" : "";
        $extra = "id=\"HEADERCHECK\"".$check_header;
        $arg["data"]["HEADERCHECK"] = knjCreateCheckBox($objForm, "HEADERCHECK", "1", $extra);

        $model->schoolname = $db->getOne(knjh140aQuery::getSchoolname());
        if ($model->schoolname == "bunkyo") {
            $arg["bunkyo"] = "1";
            //ヘッダ有無
            $check = $model->sin_check == "1" ? " checked " : "";
            $disabled = $model->target == "3" ? "" : " disabled ";
            $extra = "id=\"SINNYUSEI\"".$check.$disabled;
            $arg["data"]["SINNYUSEI"] = knjCreateCheckBox($objForm, "SINNYUSEI", "1", $extra);
        }

        //年度一覧コンボボックス
        $db = Query::dbCheckOut();
        $optnull = array("label" => "(全て出力)","value" => "");   //初期値：空白項目
        $result = $db->query(knjh140aquery::getSelectFieldSQL());
        $opt_year  = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_year[] = array("label" => $row["YEAR"]."年度 ".$row["SEMESTERNAME"],
                                 "value" => $row["YEAR"].$row["SEMESTER"]);
        }
        $result->free();
        $model->field["YEAR"] = $model->field["YEAR"] == "" ? CTRL_YEAR.CTRL_SEMESTER : $model->field["YEAR"];
        $extra = "onchange=\"btn_submit('');\"";
        $arg["data"]["YEAR"] = knjCreateCombo($objForm, "YEAR", $model->field["YEAR"], $opt_year, $extra, 1);

        //年組一覧コンボボックス
        $result = $db->query(knjh140aquery::getSelectFieldSQL2($model));
        $opt_gr_hr = array();
        $opt_gr_hr[] = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_gr_hr[] = array("label" => $row["HR_NAME"],
                                 "value" => $row["GRADE"].$row["HR_CLASS"]);
        }
        $result->free();
        $extra = "";
        $arg["data"]["GRADE_HR_CLASS"] = knjCreateCombo($objForm, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $opt_gr_hr, $extra, 1);

        //異動生徒出力選択チェックボックス
        $grd_check = ($model->field["GRD_CHECK"] == "1") ? "checked" : "";
        $extra = "id=\"GRD_CHECK\"".$grd_check;
        $arg["data"]["GRD_CHECK"] = knjCreateCheckBox($objForm, "GRD_CHECK", "1", $extra);

        //ボタン
        $extra = "onclick=\"return btn_submit('execute');\"";
        $arg["button"]["btn_ok"] = knjCreateBtn($objForm, "btn_ok", "実 行", $extra);

        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        $extra = "onclick=\"return btn_submit('output');\"";
        $arg["button"]["btn_output"] = knjCreateBtn($objForm, "btn_output", "テンプレート書出し", $extra);

        if ($model->Properties["useXLS"]) {
            $model->schoolCd = $db->getOne(knjh140aQuery::getSchoolCd());
            $extra = "onclick=\"return newwin('" . SERVLET_URL . "', '" . $model->schoolCd . "', '" . $model->Properties["xlsVer"] . "');\"";
            $printDisp = "エクセル出力";
            //タイトル表示
            $arg["data"]["TITLE"] = "　エクセル出力／ＣＳＶ取込";
        } else {
            $extra = "onclick=\"return btn_submit('csv');\"";
            $printDisp = "ＣＳＶ出力";
            //タイトル表示
            $arg["data"]["TITLE"] = "　ＣＳＶ出力／取込";
        }
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", $printDisp, $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJH140A");
        knjCreateHidden($objForm, "TEMPLATE_PATH");

        //DB切断
        Query::dbCheckIn($db);
        $arg["start"] = $objForm->get_start("main", "POST", "knjh140aindex.php", "", "main");

        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knjh140aForm1.html", $arg);
    }
}
