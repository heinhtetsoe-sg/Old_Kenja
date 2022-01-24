<?php

require_once('for_php7.php');

class knjc038aForm1 {
    function main(&$model) {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }
        $objForm = new form;
        //DB接続
        $db = Query::dbCheckOut();

        //今年度・今学期名及びタイトルの表示
        $arg["data"]["YEAR"] = CTRL_YEAR;
        if (!$model->field["YEAR"]) $model->field["YEAR"] = CTRL_YEAR;

        /****************/
        /* ラジオボタン */
        /****************/
        //出力取込種別(1:データ取込/2:エラー出力)
        $opt = array(1, 2);
        $model->field['OUTPUT'] = $model->field['OUTPUT'] ? $model->field['OUTPUT'] : '1';
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"OUTPUT{$val}\" ");
        }
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field['OUTPUT'], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        /********/
        /* FILE */
        /********/
        $extra = "";
        $arg["FILE"] = knjCreateFile($objForm, "FILE", $extra, 1024000);

        /***********/
        /* 対象期間 */
        /***********/
        //学期情報
        $query = knjc038aQuery::getSemesterInfo($model);
        $model->semesterInfo = array();
        $model->semesterInfo = $db->getRow($query, DB_FETCHMODE_ASSOC);
        //開始日付
        $model->field['START_DATE'] = $model->field['START_DATE'] ? $model->field['START_DATE'] : $model->semesterInfo['SDATE'];
        if (str_replace("/", "-", $model->field['START_DATE']) < $model->semesterInfo["SDATE"] || str_replace("/", "-", $model->field['START_DATE']) > $model->semesterInfo["EDATE"]) {
            $model->field['START_DATE'] = $model->semesterInfo["SDATE"];
        }
        $param = "";
        $arg["data"]["START_DATE"] = View::popUpCalendar($objForm, "START_DATE", str_replace("-", "/", $model->field['START_DATE']), $param);
        //終了日付
        $model->field['END_DATE'] = $model->field['END_DATE'] ? $model->field['END_DATE'] : $model->semesterInfo['EDATE'];
        if (str_replace("/", "-", $model->field['END_DATE']) < $model->semesterInfo["EDATE"] || str_replace("/", "-", $model->field['END_DATE']) > $model->semesterInfo["EDATE"]) {
            $model->field['END_DATE'] = $model->semesterInfo["EDATE"];
        }
        $param = "";
        $arg["data"]["END_DATE"] = View::popUpCalendar($objForm, "END_DATE", str_replace("-", "/", $model->field['END_DATE']), $param);

        /**********/
        /* ボタン */
        /**********/
        //実行
        // $extra = "onclick=\"return btn_submit('exec');\" disabled ";
        $extra = "onclick=\"return btn_submit('exec');\" ";
        $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjc038aindex.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjc038aForm1.html", $arg);
    }
}

?>
