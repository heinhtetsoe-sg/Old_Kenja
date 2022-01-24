<?php

require_once('for_php7.php');

class knjx_e063remarkForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjx_e063remarkindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        $securityCnt = $db->getOne(knjx_e063remarkQuery::getSecurityHigh($model));
        //セキュリティーチェック
        if (!$model->getPrgId && $model->Properties["useXLS"] && $securityCnt > 0) {
            $arg["jscript"] = "OnSecurityError();";
        }

        //事前処理チェック
        if (!$model->exp_year || !$model->exp_semester|| !$model->grade || !$model->hr_class) {
            $arg["jscript"] = "OnPreCheckError();";
        }

        //処理名コンボボックス
        $opt_shori      = array();
        $opt_shori[]    = array("label" => "更新","value" => "1");
        $extra = "style=\"width:60px;\"";
        $arg["data"]["SHORI_MEI"] = knjCreateCombo($objForm, "SHORI_MEI", $model->field["SHORI_MEI"], $opt_shori, $extra, 1);

        //ヘッダ有チェックボックス
        if ($model->field["HEADER"] == "on") {
            $check_header = "checked";
        } else {
            $check_header = ($model->cmd == "") ? "checked" : "";
        }
        $extra = "id=\"HEADER\"".$check_header;
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra);

        //出力取込種別ラジオボタン(1:データ取込 2:エラー出力 3:データ出力)
        $opt = array(1, 2, 3);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "3" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"", "id=\"OUTPUT3\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //ファイルからの取り込み
        $arg["FILE"] = knjCreateFile($objForm, "FILE", "", 1024000);

        //学期名取得
        $semesterName = $db->getOne(knjx_e063remarkquery::getSemesterName($model));

        //年度＆学期表示
        $arg["data"]["YEAR"] = $model->exp_year.'年度 '.$semesterName;

        //年組名称表示
        $hr_name = $db->getOne(knjx_e063remarkquery::getHrName($model));
        $arg["data"]["GRADE_HR_CLASS"] = $hr_name;
        knjCreateHidden($objForm, "HR_NAME", $hr_name);

        //ボタン作成
        makeButton($objForm, $arg, $model, $db);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJX_E063REMARK");
        knjCreateHidden($objForm, "TEMPLATE_PATH");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjx_e063remarkForm1.html", $arg);
    }
}

//ボタン作成
function makeButton(&$objForm, &$arg, $model, $db)
{
    //実行ボタン
    if ($model->Properties["useXLS"]) {
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "', '" . $model->schoolCode . "', '" . $model->Properties["xlsVer"] . "');\"";
        //今年度・今学期名及びタイトルの表示
        $arg["data"]["YEAR_SEMESTER"] = CTRL_YEAR."年度　" .CTRL_SEMESTERNAME ."　エクセル出力／ＣＳＶ取込";
    } else {
        $extra = "onclick=\"return btn_submit('exec');\"";
        //今年度・今学期名及びタイトルの表示
        $arg["data"]["YEAR_SEMESTER"] = CTRL_YEAR."年度　" .CTRL_SEMESTERNAME ."　ＣＳＶ出力／取込";
    }
    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
