<?php

require_once('for_php7.php');
class knjf175Form1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"]  = $objForm->get_start("knjf175Form1", "POST", "knjf175index.php", "", "knjf175Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["data"]["SEMESTER"] = $db->getOne(knjf175Query::getSemeName());

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            //特別支援学校では校種を表示しない
            if ($model->Properties["useSpecial_Support_School"] != "1") {
                $arg["schkind"] = "1";
                $query = knjf175Query::getSchkind($model);
                $extra = "onchange=\"return btn_submit('knjf175');\"";
                makeCmb($objForm, $arg, $db, $query, "SCHKIND", $model->field["SCHKIND"], $extra, 1, "");
            }
        }

        if ($model->Properties["useFormNameF175"] == "KNJF175_5_1") {
            $arg["layoutForm5"] = "1";
        } else {
            $arg["layoutNotForm5"] = "1";
        }

        //開始日付作成
        $model->field["SDATE"] = $model->field["SDATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["SDATE"];
        $arg["data"]["SDATE"] = View::popUpCalendar($objForm, "SDATE", $model->field["SDATE"]);

        //終了日付作成
        $model->field["EDATE"] = $model->field["EDATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["EDATE"];
        $arg["data"]["EDATE"] = View::popUpCalendar($objForm, "EDATE", $model->field["EDATE"]);
        if ('1' == $model->Properties["useNurseoffAttend"]) {
            $arg["notPrintKessekisha"] = '1';
        } else {
            $arg["printKessekisha"] = '1';
        }

        //欠席者一覧印刷チェックボックス
        $arg["data"]["PRINT"] = knjCreateCheckBox($objForm, "PRINT", 'on', "id='PRINT'");

        if ($model->Properties["useFormNameF175"] == "KNJF175_5_1") {
            //生徒識別ラジオ
            $opt = array(1, 2);
            $model->field["SIKIBETU_PTRN"] = ($model->field["SIKIBETU_PTRN"] == "") ? "1" : $model->field["SIKIBETU_PTRN"];
            $extra = array();
            foreach ($opt as $key => $val) {
                array_push($extra, " id=\"SIKIBETU_PTRN{$val}\" ");
            }
            $radioArray = knjCreateRadio($objForm, "SIKIBETU_PTRN", $model->field["SIKIBETU_PTRN"], $extra, $opt, count($opt));
            foreach ($radioArray as $key => $val) {
                $arg["data"][$key] = $val;
            }
        }

        //印影出力チェックボックス
        $query = knjf175Query::getStampCnt($model);
        $StampCnt = $db->getOne($query);
        if ($StampCnt > 0) {
            $cnt = 1;
            $query = knjf175Query::getStampName($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($model->isKumamoto && 5 < $cnt) {
                    //熊本の場合は、チェックボックスは５つまで
                    break;
                }
                $extra = "checked id='PRINT_STAMP_{$row["SEQ"]}'";
                $name = "PRINT_STAMP_{$row["SEQ"]}";
                $row["PRINT_STAMP_NAME"] = knjCreateCheckBox($objForm, $name, $row["SEQ"], $extra);
                $arg["data2"][] = $row;
                $cnt++;
            }
            $arg["ineiKuma"] = "1";
        } else {
            if ($model->isKumamoto) {
                $arg["data"]["PRINT_STAMP_KOUCHOU"]     = knjCreateCheckBox($objForm, "PRINT_STAMP_1", 'on', "checked id='PRINT_STAMP_KOUCHOU'");
                $arg["data"]["PRINT_STAMP_KYOUTOU"]     = knjCreateCheckBox($objForm, "PRINT_STAMP_2", 'on', "checked id='PRINT_STAMP_KYOUTOU'");
                $arg["data"]["PRINT_STAMP_YOUGOKYOUYU"] = knjCreateCheckBox($objForm, "PRINT_STAMP_3", 'on', "checked id='PRINT_STAMP_YOUGOKYOUYU'");
            } else {
                $arg["data"]["PRINT_STAMP_KOUCHOU"]     = knjCreateCheckBox($objForm, "PRINT_STAMP_KOUCHOU", 'on', "checked id='PRINT_STAMP_KOUCHOU'");
                $arg["data"]["PRINT_STAMP_KYOUTOU"]     = knjCreateCheckBox($objForm, "PRINT_STAMP_KYOUTOU", 'on', "checked id='PRINT_STAMP_KYOUTOU'");
                $arg["data"]["PRINT_STAMP_YOUGOKYOUYU"] = knjCreateCheckBox($objForm, "PRINT_STAMP_YOUGOKYOUYU", 'on', "checked id='PRINT_STAMP_YOUGOKYOUYU'");
            }
            $arg["ineiNotKuma"] = "1";
        }

        if (($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") || $model->Properties["use_prg_schoolkind"] == "1") {
            //学校名称2表示
            $info = $db->getRow(knjf175Query::getSchoolMst($model, CTRL_YEAR), DB_FETCHMODE_ASSOC);
            $arg["SCH_NAME2"] = (strlen($info["SCHOOLNAME2"]) > 0) ? "<<".$info["SCHOOLNAME2"].">>" : "";
        }

        if ($model->Properties["KNJF175_NoUse_NurseOffDiary"] == "1") {
            $arg["printOffDaysComment"] = "1";
        }

        //ボタン作成
        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "PRGID", "KNJF175");
        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
        knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
        knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "useNurseoffAttend", $model->Properties["useNurseoffAttend"]);
        knjCreateHidden($objForm, "useFormNameF175", $model->Properties["useFormNameF175"]);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
        knjCreateHidden($objForm, "useSpecial_Support_School", $model->Properties["useSpecial_Support_School"]);
        knjCreateHidden($objForm, "knjf175PrintSchoolKind", $model->Properties["knjf175PrintSchoolKind"]);
        knjCreateHidden($objForm, "nouseNurseOffDiary", $model->Properties["KNJF175_NoUse_NurseOffDiary"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjf175Form1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
