<?php

require_once('for_php7.php');

class knjx180bForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //HTTPSをHTTPにする
        if ($model->cmd == "sign") {
            $arg["signature"] = "collHttps('".REQUESTROOT."', '')";
        }

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjx180bindex.php", "", "main");

        //必須データチェック
        if (!$model->exp_year || !$model->exp_semester || !$model->schregno) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        $securityCnt = $db->getOne(knjx180bQuery::getSecurityHigh($model));
        //セキュリティーチェック
        if (!$model->getPrgId && $model->Properties["useXLS"] && $securityCnt > 0) {
            $arg["jscript"] = "OnSecurityError();";
        }

        //処理名コンボボックス
        $opt_shori      = array();
        $opt_shori[]    = array("label" => "更新","value" => "1");
        $opt_shori[]    = array("label" => "削除","value" => "2");
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

        //出力取込種別ラジオボタン(1:ヘッダ出力 2:データ取込 3:エラー出力 4:データ出力)
        $opt = array(1, 2, 3, 4);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $check = " onclick=\"OptionUse('this');\"";
        $extra = array("id=\"OUTPUT1\"".$check, "id=\"OUTPUT2\"".$check, "id=\"OUTPUT3\"".$check, "id=\"OUTPUT4\"".$check);
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //ファイルからの取り込み
        $arg["FILE"] = knjCreateFile($objForm, "FILE", "", 1024000);

        //年度＆学期
        $arg["data"]["YEAR"] = $model->exp_year.'年度 '.$db->getOne(knjx180bquery::getSemesterName($model));
        knjCreateHidden($objForm, "YEAR", $model->exp_year.$model->exp_semester);

        //年組
        $hrInfo = $db->getRow(knjx180bquery::getHrInfo($model), DB_FETCHMODE_ASSOC);
        $arg["data"]["GRADE_HR_CLASS"] = $hrInfo["HR_NAME"];
        knjCreateHidden($objForm, "GRADE_HR_CLASS", $hrInfo["GRADE"].$hrInfo["HR_CLASS"]);

        //ボタン作成
        //実行ボタン
        if ($model->Properties["useXLS"]) {
            $model->schoolCd = $db->getOne(knjx180bQuery::getSchoolCd());
            $extra = "onclick=\"return newwin('" . SERVLET_URL . "', '" . $model->schoolCd . "', '" . $model->Properties["xlsVer"] . "');\"";
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

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJX180B");
        knjCreateHidden($objForm, "TEMPLATE_PATH");
        $fieldName = array();
        foreach ($model->fieldSize as $key => $val) {
            $fieldName[] = $key;
        }
        knjCreateHidden($objForm, "XLS_FIELDNAME", $fieldName);
        knjCreateHidden($objForm, "CHECK_AUTH", $model->auth);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjx180bForm1.html", $arg);
    }
}
