<?php

require_once('for_php7.php');
class knjz022form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjz022index.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();
        if ($model->dataBaseinfo === '2') $db2 = Query::dbCheckOut2();

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["Closing"] = " closing_window(); ";
        }

        //学校名称2表示
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $info = $db->getRow(knjz022Query::getSchoolName2($model), DB_FETCHMODE_ASSOC);
            $arg["SCH_NAME2"] = (strlen($info["SCHOOLNAME2"]) > 0) ? "<<".$info["SCHOOLNAME2"].">>" : "";
        }

        if ($model->cmd == "change") {
            $Row = $model->field;
            //学校コードチェック
            $finschoolcd = $db->getOne(knjz022Query::getFinschoolMst($Row["FINSCHOOLCD"]));
            if ($Row["FINSCHOOLCD"] == "") {
                $model->setWarning("MSG304", "( 学校コード )");
            } else if ($finschoolcd == "") {
                $model->setWarning("MSG901", "( 学校コード )");
            }
        } else if ($model->cmd == "main2") {
            $Row = $model->field;
        } else {
            //データ取得
            $query = knjz022Query::getSchoolDetailDat($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }

        if ($model->dataBaseinfo === '2') {
            //教育委員会設定学校マスタのデータ取得
            $edboard_schoolcd = $db->getOne(knjz022Query::getSchoolDetailDat002($model));
            $query = knjz022Query::getEdboardSchoolMst($edboard_schoolcd);
            $edRow = $db2->getRow($query, DB_FETCHMODE_ASSOC);
            if ($Row["FINSCHOOLCD"] == "") {
                $Row["FINSCHOOLCD"] = $edRow["GROUPWARE_SCHOOLCD"];
            }
            $finschool["FINSCHOOL_NAME"] = $edRow["EDBOARD_SCHOOLNAME"];
            $finschool["FINSCHOOL_KANA"] = "";
        } else {
            //出身学校マスタのデータ取得
            $query = knjz022Query::getFinschoolMst($Row["FINSCHOOLCD"]);
            $finschool = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }

        //年度
        $arg["YEAR"] = $model->year;

        $readonly = ($model->dataBaseinfo === '2') ? " readonly style=\"background-color:silver;\"" : "";
        //学校コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"" . $readonly;
        $arg["data"]["FINSCHOOLCD"] = knjCreateTextBox($objForm, $Row["FINSCHOOLCD"], "FINSCHOOLCD", 7, 7, $extra);

        //学校名称
        $arg["data"]["FINSCHOOL_NAME"] = $finschool["FINSCHOOL_NAME"];

        //学校名称かな
        $arg["data"]["FINSCHOOL_KANA"] = $finschool["FINSCHOOL_KANA"];

        //校種コンボ
        $opt = array();
        $opt[] = array('label' => '',           'value' => '');
        $opt[] = array('label' => '2:小学校',   'value' => '2');
        $opt[] = array('label' => '3:中学校',   'value' => '3');
        $opt[] = array('label' => '4:高校',     'value' => '4');
        $opt[] = array('label' => '5:支援学校', 'value' => '5');

        $extra = "";
        $arg["data"]["SCHOOL_TYPE"] = knjCreateCombo($objForm, "SCHOOL_TYPE", $Row["SCHOOL_TYPE"], $opt, $extra, 1);

        //課程コンボ
        $opt = array();
        $opt[] = array('label' => '',           'value' => '');
        $opt[] = array('label' => '1:全日制',   'value' => '1');
        $opt[] = array('label' => '2:定時制',   'value' => '2');
        $opt[] = array('label' => '4:通信制',   'value' => '4');

        $extra = "";
        $arg["data"]["COURSE_CD"] = knjCreateCombo($objForm, "COURSE_CD", $Row["COURSE_CD"], $opt, $extra, 1);

        //出欠席入力コンボ
        $opt = array();
        $opt[] = array('label' => '',                   'value' => '');
        $opt[] = array('label' => '0:月単位での入力',   'value' => '0');
        $opt[] = array('label' => '1:日々入力',         'value' => '1');

        $extra = "";
        $arg["data"]["ATTENDANCE_FLAG"] = knjCreateCombo($objForm, "ATTENDANCE_FLAG", $Row["ATTENDANCE_FLAG"], $opt, $extra, 1);

        //URL
        $extra = "";
        $arg["data"]["KENJA_URL"] = knjCreateTextBox($objForm, $Row["KENJA_URL"], "KENJA_URL", 60, 90, $extra);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);
        if ($model->dataBaseinfo === '2') Query::dbCheckIn($db2);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz022Form1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    $disabled = ($model->dataBaseinfo === '2') ? " disabled" : "";
    //確定ボタン
    $extra = "onClick=\"return btn_submit('change');\"" . $disabled;
    $arg["btn_kakutei"] = knjCreateBtn($objForm, "btn_kakutei", "確 定", $extra);

    //更新ボタン
    $extra = "onClick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onClick=\"return btn_submit('clear');\"";
    $arg["btn_cancel"] = knjCreateBtn($objForm, "btn_cancel", "取 消", $extra);
    //終了ボタン
    $extra = "onClick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
