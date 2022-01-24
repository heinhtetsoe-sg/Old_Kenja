<?php

require_once('for_php7.php');

class knjg050Form1
{

    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjg050Form1", "POST", "knjg050index.php", "", "knjg050Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //卒業年度
        if ($model->field["YEAR"] == "") {
            $model->field["YEAR"] = CTRL_YEAR;
        }
        knjCreateHidden($objForm, "YEAR", $model->field["YEAR"]);
        $arg["data"]["YEAR"] = $model->field["YEAR"] . "年度卒";

        //卒業見込み出力チェックボックス
        $extra  = ($model->field["SOTUGYO_MIKOMI"] == "1") ? "checked" : "";
        $extra .= " id=\"SOTUGYO_MIKOMI\"";
        $arg["data"]["SOTUGYO_MIKOMI"] = knjCreateCheckBox($objForm, "SOTUGYO_MIKOMI", "1", $extra, "");

        //印刷指定ラジオボタン 1:クラス指定 2:個人指定
        $model->output = (!$model->output) ? 1 : $model->output;
        $opt = array(1, 2);
        $click = " onclick =\" return btn_submit('knjg050');\"";
        $extra = array("id=\"OUTPUT1\"".$click, "id=\"OUTPUT2\"".$click);
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->output, $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //学期
        if ($model->field["GAKKI"] == "") {
            $model->field["GAKKI"] = CTRL_SEMESTER;
        }
        knjCreateHidden($objForm, "GAKKI", $model->field["GAKKI"]);

        //学校区分を取得
        $query = knjg050Query::getSchoolDiv($model);
        $schoolDiv = $db->getOne($query);

        //'A023"登録確認（NAMESPARE2, NAMESPARE3）
        $query = knjg050Query::getNameSpareCnt();
        $nSpare = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //学年・クラスコンボ
        if ($model->output == 1) {
            if ($nSpare["SPARE2"] == 0 || $nSpare["SPARE3"] == 0) {
                $query = knjg050Query::getGrade1($model, $schoolDiv);
            } else {
                $query = knjg050Query::getGrade2($model);
            }
        } else {
            if ($nSpare["SPARE2"] == 0 || $nSpare["SPARE3"] == 0) {
                $query = knjg050Query::getHR1($model, $schoolDiv);
            } else {
                $query = knjg050Query::getHR2($model);
            }
        }
        $opt = array();
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

        if ($model->cmd != "cmbclass") {
            $model->field["CMBCLASS"] = $opt[0]["value"];
        }

        $extra = "onChange=\"return btn_submit('cmbclass');\"";
        $arg["data"]["CMBCLASS"] = knjCreateCombo($objForm, "CMBCLASS", $model->field["CMBCLASS"], $opt, $extra, 1);
        $arg["data"]["CMBCLASS_LABEL"] = ($model->output == 1) ? 'クラス選択' : '個人選択';

        //入学・卒業日付は年月で表示する
        $extra  = ($model->field["ENT_GRD_DATE_FORMAT"] == "1") ? "checked" : "";
        $extra .= " id=\"ENT_GRD_DATE_FORMAT\"";
        $arg["data"]["ENT_GRD_DATE_FORMAT"] = knjCreateCheckBox($objForm, "ENT_GRD_DATE_FORMAT", "1", $extra, "");

        if ($model->Properties["knjg050PrintStamp"] == "checkbox") {
            $arg["show_PRINT_STAMP"] = "1";
            //印影出力
            $extra  = ($model->field["PRINT_STAMP"] == "1") ? "checked" : "";
            $extra .= " id=\"PRINT_STAMP\"";
            $arg["data"]["PRINT_STAMP"] = knjCreateCheckBox($objForm, "PRINT_STAMP", "1", $extra, "");
        }

        //リストtoリスト作成
        $opt_data = array();
        if ($model->output == 1) {
            $query = knjg050Query::getAuth($model);
        }
        if ($model->output == 2) {
            $query = knjg050Query::getstudent($model);
        }
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_data[]= array('label' => $row["LABEL"],
                               'value' => $row["VALUE"]);
        }
        $result->free();

        //一覧リスト作成
        $extra = "multiple style=\"width:180px\" width=\"180px\" ondblclick=\"move1('left')\"";
        $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", "", isset($opt_data)?$opt_data:array(), $extra, 15);

        //出力対象リスト作成
        $extra = "multiple style=\"width:180px\" width=\"180px\" ondblclick=\"move1('right')\"";
        $arg["data"]["CLASS_SELECTED"] = knjCreateCombo($objForm, "CLASS_SELECTED", "", array(), $extra, 15);

        //生徒項目名切替処理
        $sch_label = "";
        //テーブルの有無チェック
        $query = knjg050Query::checkTableExist();
        $table_cnt = $db->getOne($query);
        if ($table_cnt > 0 && ($model->field["CMBCLASS"] || ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""))) {
            //生徒項目名取得
            $sch_label = $db->getOne(knjg050Query::getSchName($model));
        }
        $sch_label = (strlen($sch_label) > 0) ? $sch_label : '生徒';

        //項目名表示
        $arg["data"]["CLASS_LABEL"] = ($model->output == 1) ? 'クラス' : $sch_label;

        //対象選択ボタン（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
        //対象取消ボタン（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
        //対象選択ボタン（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
        //対象取消ボタン（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);

        //記載日付
        $value = isset($model->field["NOTICEDAY"]) ? $model->field["NOTICEDAY"] : str_replace("-", "/", CTRL_DATE);
        $arg["el"]["NOTICEDAY"] = View::popUpCalendar($objForm, "NOTICEDAY", $value);

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        if ($model->Properties["useShuryoShoumeisho"] == '1') {
            $arg["sotsugyo"] = "修了";
        } else {
            $arg["sotsugyo"] = "卒業";
        }

        //hidden作成
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJG050");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "certifNoSyudou", $model->Properties["certifNoSyudou"]);
        knjCreateHidden($objForm, "certifPrintRealName", $model->Properties["certifPrintRealName"]);
        knjCreateHidden($objForm, "certif_no_8keta", $model->Properties["certif_no_8keta"]);
        knjCreateHidden($objForm, "useShuryoShoumeisho", $model->Properties["useShuryoShoumeisho"]);
        knjCreateHidden($objForm, "knjg050PrintStamp", $model->Properties["knjg050PrintStamp"]);
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjg050Form1.html", $arg);
    }
}
